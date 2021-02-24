import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Serveur {
	private static ServerSocket listener;
	private static boolean isConnected = true;
	public static String mainDirectory = System.getProperty("user.dir"); 
//	public static String currentDir = mainDirectory; 

	public static void main(String[] args) throws Exception
	{
		// Compteur incrementer a chaque connection dun client au server
		int clientNumber =0;

		String serverAddress = " ";
		int serverPort = 0; 

		boolean isIPGood= false;
		boolean isPortGood = false;
		 String rawAddress = " "; 
		//---------------------------------Adresse et port du serveur---------------------------------------------------// 
		System.out.println("\n----------------------------Affichage du serveur-------------------------------- \n");
		System.out.println("Le serveur essaie de démarer");
		// Demande ladresse du serveur voulu.
		 do{
			System.out.println("Entrer une adresse IP et un port en suivant ce format : XXX.XXX.XX.XX:PORT");
			try {
				// splits the IP and the port
				rawAddress = readFromConsole(); 
				if(rawAddress != null) {
					String[] split = rawAddress.split("\\:"); 
					serverAddress = split[0];
					serverPort = Integer.parseInt(split[1]);
					isIPGood = IPVerifier(serverAddress);
					isPortGood = PortVerifier(serverPort);
				}
			}
			catch(NumberFormatException e){
				System.out.println("Une adresse ip ne peux pas contenir une lettre. \n");
				isIPGood = false;
			}
			catch(ArrayIndexOutOfBoundsException ee) {
				System.out.println("Une adresse ip ne peux pas contenir une valeur négative. \n");
				isIPGood = false;
			}
		 } while(!isIPGood || !isPortGood );
	
		// Creer la connection pour communiquer avec les clients
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAddress);
			
		// Associer adresse et port a la connection
		listener.bind(new InetSocketAddress(serverIp,serverPort));
			
		System.out.format("Le serveur roule sur %s:%d\n",serverAddress,serverPort);
			
		try {	
			// chaque nouvelle connection dun client declanche une execution de Run() de ClientHandler.
			while(isConnected) {
				// fct accept est bloquante. on creer un thread a chaque nouveau client.
				new ClientHandler(listener.accept(),clientNumber++,rawAddress, mainDirectory).start(); // start fait que on a 1 thread par connection	
			}
		}	
		catch(Exception e){
			listener.close();
			isConnected = false; 
			System.out.println("Une erreur c'est produite lors du démarage du serveur. \n");
		}
		
	}
	
	/* Client handler. Permet d'avoir plusieurs connection possible.
	 * Thread ce charge de traiter la demande de chaque client sur un socket particulier.
	 * 
	 */
	private static class ClientHandler extends Thread {
		private Socket socket;
		private int clientNumber;
		private String rawAddress;
		private String currentDirectory;
		
		// constructeur
		public ClientHandler(Socket socket,int clientNumber,String rawAddress,String dir) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			this.rawAddress = rawAddress;
			this.currentDirectory = dir;
			System.out.println("Nouvelle connection avec le client # " + clientNumber + " sur " + socket);
		}
		// Un thread ce charge denvoyer au client un msg de bienvenue.
		public void run() {
			String request = ""; // vient du client
			String response = ""; // envoyer au client
			
			try {
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // creer un canal sortant pour envoyer des msg au client.

				// envoie un msg au client
				out.writeUTF("Allo du serveur! Vous etes le client #" + clientNumber + "\n" );
				out.flush();
				
				while(isConnected) {
				//	while(in.available() != 0)  // attente active.
					
					request = in.readUTF(); // on recoit une requete du client
					String[] longRequest = new String[2];
					longRequest= request.split("\\s+"); 
					
					// Depending on the recived request
					if(request.equals("exit")) {
						recivedCommand(rawAddress,request); 
						System.out.println("Le client : " + clientNumber + " est déconnecter"); 
						// sends a confirmation message to the client.
						response = "Le client "+ clientNumber +" est déconnecté du serveur";
						out.writeUTF(response);
						out.flush();
						break;
					} 
					if(longRequest[0].equals("cd")) {
						recivedCommand(rawAddress,request);
					 	String currentPos = cd(longRequest[1]);
					 	response = "Message du serveur : Vous etes dans le dossier : " + currentPos ;  
					 	out.writeUTF(response);
					 	out.flush();
					}
					 	
					if(longRequest[0].equals("mkdir")) {
						recivedCommand(rawAddress,request); 
						response = createDir(longRequest[1]);						
						out.writeUTF(response);
						out.flush();
					}
					
					if(request.equals("ls")) {
						recivedCommand(rawAddress,request);
						String[] toSend = ls();
						out.write(toSend.length); // envoie au client le nb delements dans le dir.
					  	out.flush();
						for(int i =0; i< toSend.length;i++) {
							out.writeUTF(toSend[i]); // envoie au client tt les fichier et folders.
						}
					 	out.flush();
					}
			
					if(request.equals("upload")) {
					 		
					}
					 	
					if(request.equals("download")) {
					 		
					}
					
					
			/*		switch (request) {
						case "cd" :
							System.out.println("cd detected");
							response = "Message du serveur: cd detected";
							out.writeUTF(response);
							//break;
						
						case "ls":
							System.out.println("ls detected");
							//break;
						
					
						case "mkdir":
							
							System.out.println("Création d'un nouveau dossier : " + requestArgs[1] +"\n");
							CreateDir(requestArgs[1]);
							response = "Message du serveur : Le dossier" + request + " a été créé ";
							out.writeUTF(response);
							break;

				} */
					
				} 
			// Fermeture des streams.
				socket.close();
				in.close();
				out.close();
				out.flush();
				// listener.close();
			} 
			catch(IOException e) {
				System.out.println("Erreur lors du traitement du client # " + clientNumber + ": " + e);
				
			} 
			catch(Exception ee) {
				System.out.println("Une erreur s'est produite" +  " " + ee);
			}
		} 
		
		// Fonctions pour les commandes //
		/*
		 * 
		 */
		private String cd(String changeDir) {
			// va changer le currentDirectory pour permettre a ls d'afficher les elements dans le dir ou se on situe.
			File file = new File(currentDirectory);
			String newPath = currentDirectory + "\\" + changeDir;
			Path path = Paths.get(newPath); // le path apres avoir fais un cd. On veut s'assurer que ce dernier existe.
					
//			System.out.println("The path is : " + path + "does it exist ? " +Files.exists(path));
			if(file.exists() && Files.exists(path)){ // si le cd peut etre execute.
				if(changeDir.equals("..")) {	
					currentDirectory = file.getParent(); // on sort du dossier courant si on fait cd ..			
					if(!file.getParent().equals(mainDirectory)) {
						currentDirectory = mainDirectory; // on veut rester dans le path du projet.
					}
				}			
				else {
					File newFilePath = new File(newPath);
					currentDirectory = newFilePath.getAbsolutePath();	
				}
			System.out.println("cd success : " + currentDirectory+ " " + "\n"); 
			}
			else {
				System.out.println("Le chemin : " + currentDirectory + " " +  " n'existe pas" + "\n"); 
			}
			return currentDirectory;
		}
			
		private String[] ls() {
			// System.out.println(mainDirectory);
			File path = new File(currentDirectory); // sera mis a jour selon le currentDirectory. 
			File[] allElementsinDir = path.listFiles(); // returns all the elements in current server dir.
			String[] arrayElementsinDir = new String[allElementsinDir.length];
			
			for(int pos=0; pos< allElementsinDir.length; pos++) {
				if(allElementsinDir[pos].isFile()) {
					String isFile = "[File] " + allElementsinDir[pos].getName() +"\n";
					arrayElementsinDir[pos] = isFile;
					//System.out.println(isFile);
				}
				else if(allElementsinDir[pos].isDirectory()) {
					String isDir =  "[Folder] " + allElementsinDir[pos].getName() + "\n";
					arrayElementsinDir[pos] = isDir;
					//System.out.println(isDir);
				}
				// System.out.println(filesFolders[pos] + "\n");
			}
			System.out.println("commande ls executer");
			return arrayElementsinDir;
			
			
			/*
			for(File pos:allElementsinDir) {
				if(pos.isFile()) {
					String isFile = "[File] " + pos.getName() +"\n";
					System.out.println(isFile);
				}
				// else its a folder
				else if(pos.isDirectory()) {
					String isDir =  "[Folder] " + pos.getName() + "\n";
					System.out.println(isDir);
				}	
			}*/
		} 
		
		private String createDir(String dir) {
			String mkdir;
			mkdir = currentDirectory + "\\" +dir;
			boolean isFileCreated;
			File file = new File(mkdir); 
			// System.out.println( file.getAbsolutePath());
			isFileCreated = file.mkdir();  // permet de creer 2 fichiers si on met ex: 11/a il va creer dossier 11 et dedant le dossier a.
			
			if(isFileCreated) {
				System.out.println("Le dossier : "+ dir +" a été créer");
				return "Le dossier : "+ dir +" a été créé";
			}
			else {
				System.out.println("Une erreur s'est produite lors de la création du dossier :" + dir);
				return "Une erreur s'est produite lors de la création du dossier :" + dir;
			}
		}	
		
		
	}	
	
	// Fonctions pour dans la classe Serveur. //
	
	/*
	 * 
	 */
	private static void recivedCommand(String address, String command) {
		SimpleDateFormat currentDateTime = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
		Date date = new Date();
		String info = "[" + address + "-"+  currentDateTime.format(date) +  "]" +  ": " + command + "\n"; 
		System.out.println(info);
		
	}
	
	/*
	 * 
	 */
	// verifies IpAdress
	public static boolean IPVerifier(String inputIP) {
		boolean isIPvalid = false; 
		String[] toBeSplit = inputIP.split("\\."); 
		
		if((inputIP != null) && (!inputIP.isEmpty())) {
			if((!inputIP.endsWith("."))) {
				if(toBeSplit.length == 4) {
					for(String pos : toBeSplit) {
						if(Integer.parseInt(pos) >= 0 && Integer.parseInt(pos) < 255 ) {
							isIPvalid =true;
						}
							else {
								isIPvalid =false;
								break;
							}
					}
				}
			} 
		}
		if(!isIPvalid) System.out.println("Le format de l'adresse ip saisie : " + inputIP + " n'est pas valide. Veillez saisir une adresse valide de format : XXX.XXX.XX.XX" + "\n");		
		else System.out.println("L'adresse ip saisie : " + inputIP + " est valide!");

		return isIPvalid;
	}
	
	/*
	 * 
	 */
	public static  boolean PortVerifier(int port) {
		boolean validPort = false;
		if(port >= 5000 && port <= 5050) {
			validPort = true;
			System.out.println("Le port : " + port + " est valide! \n");
		}	
		else System.out.println("Le port : " + port + " n'est pas valide. Veillez inscrire un port valide compris entre 5000 a 5050. \n");
			
		return validPort;
	}
	
	/*
	 * 
	 */
	
	public static String readFromConsole() throws IOException {	
		String ip = " ";
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			ip = input.readLine();
			if(ip != null) System.out.println("L'adresse inscrite est la suivante : " + ip);	
			// input.close();
		}
		catch(IOException e) {
			System.out.println("Erreur lors de la lecture dans la console.");
				throw e;
			}
		return ip;
	}

}
