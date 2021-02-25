import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	private static boolean isConnected = true;
	
	public static void main(String[] args) throws Exception
	{
		String clientAddress = " "; 
		int clientPort = 0; 
		//--------------------Ecrire ladresse et le port voulu--------------------------//
		// l'utilisateur inscrit l'adresse ip et le port du serveur auquel il veut se connecter.
		String address = " "; 
		boolean isIPGood = false;
		boolean isPortGood = false;
		
		System.out.println("\n----------------------------Affichage du client-------------------------------- \n");
		System.out.println("Le client tente de se connecter au serveur");
		 do{
			System.out.println("Entrer l'adresse IP du serveur et un port en suivant ce format : XXX.XXX.XX.XX:PORT");
			try {
				// splits the IP and the port
				address = Serveur.readFromConsole(); 
				if(address != null) {
					String[] split = address.split("\\:"); 
					clientAddress = split[0];
					clientPort = Integer.parseInt(split[1]);
					
					isIPGood = Serveur.IPVerifier(clientAddress);
					isPortGood = Serveur.PortVerifier(clientPort);
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
		 
		 //------------------------------- Client code---------------------------------------------//
		 try {
			 	// Read the response written by the client to send to the server.
			 	Scanner scanner = new Scanner(System.in);
			 	
			 	// LE CLient se connecte au serveur. (Nouvelle connection)
				socket = new Socket(clientAddress,clientPort);
				System.out.format("Connecté au serveur sur  %s:%d \n\n" , clientAddress,clientPort);
				menu();

				String request = "";
				String response = "";

				// Creer un canal entrant pour recevoir message envoyer par serveur sur canal
				DataInputStream in = new DataInputStream(socket.getInputStream()); 
				DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // pr envoyer des requettes au serveur.
				
				// Attente de la reception d'un message envoyer par le serveur sur le canal
				response = in.readUTF();
				System.out.println("Message du serveur : " + response);
				
				while(isConnected) {
					// Sends the input of the client to the server.
					request = scanner.nextLine();
					out.writeUTF(request); // envoie ce que le client a ecrit au serveur.
					out.flush();
					String[] longRequest = new String[2]; 
					longRequest = 	request.split("\\s+"); // utilise pour les commandes qui ont un parametre. 
					
					// Depending on the requests that was send to the server.
					if(request.equals("exit")) {
						System.out.println("Vous avez été déconnecté avec succès.");
					//	while(in.available() != 0)
						response = in.readUTF();
						System.out.println("Message du serveur :" + response);
						socket.close();	// Fermer la connection
						break;
					}	
								
					else if(longRequest[0].equals("mkdir")) {
						System.out.println("Commande : " + request);
						response = in.readUTF();
						System.out.println( response);
					}
					else if (longRequest[0].equals("cd")) {
						DataInputStream recive = new DataInputStream(socket.getInputStream());
						response = in.readUTF();
						System.out.println("Commande : " + request);
						System.out.println( response);
						System.out.println("Message du serveur: " + recive.readUTF());
					}
					else if(request.equals("ls")) {
						int taille = in.read(); // recevoir la taille du nb des fichiers dans le repertoires
						System.out.println("Commande : " + request); 
						for(int i= 0; i< taille;i++) {
							response = in.readUTF();
							System.out.println(response);
						}
						if(taille == 0) System.out.println("Le dossier est vide \n");			
						out.flush();
					}
					 	
					else if(longRequest[0].equals("upload")) {
			            System.out.println("Commande : " + request); 
						sendFile(longRequest[1]);
						System.out.println("Le fichier " + longRequest[1] + " a bien ete téléversé.");
					}
					 	
					else if(longRequest[0].equals("download")) {
						System.out.println("Commande : " + request); 
						receiveFile(longRequest[1]);
				        System.out.println("Le fichier " + longRequest[1] + " a bien ete téléchargé.");
					}		
					
					else {
						System.out.println("Mauvaise commande. Reesayer \n");
					}
					
				}
				// fermeture des ressources
				out.flush();
				in.close();
				out.close();
				scanner.close();
		 }
		 catch(Exception e){
			 System.out.println("Connection refusé. Vérifier si le serveur est connecté ou si l'adresse inscrite correspond a l'adresse du serveur. \n");
			// throw e; // debug
		 }
	}	
	
	/*
	 * Méthodes utilisées pour téléverser/télécharger un fichier à partir de/vers le serveur
	 * 
	 * 
	 */
	
	 private static void sendFile(String path) throws Exception{
	        int bytes = 0;
	        
	        File file = new File(path);
	        System.out.println("ALLO du sent file" + file.getAbsolutePath());
	        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
	        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
	        
	        dataOutputStream.writeLong(file.length());  

	        byte[] buffer = new byte[4*1024];
	        while ((bytes=fileInputStream.read(buffer))!=-1){
	            dataOutputStream.write(buffer,0,bytes);
	        }
            dataOutputStream.flush();
	        fileInputStream.close();
	    }
	
	 
	private static void receiveFile(String fileName) throws Exception{
	        int bytes = 0;
	        
	        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
	        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
	        
	        long size = dataInputStream.readLong();     // read file size

	        byte[] buffer = new byte[4*1024];
	        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
	            fileOutputStream.write(buffer,0,bytes);
	            size -= bytes;      // read upto file size
	        }
	            fileOutputStream.close();	   
	    } 		
	
	private static void menu() {
		System.out.println("Voici les commandes disponibles : cd, ls,mkdir,upload,download et exit.");
		System.out.println("Utiliser la commande cd <repertoire> pour se dépalcer vers un repertoire parent ou enfant.");
		System.out.println("Utiliser la commande cd .. pour revenir vers un repertoire parent.");
		System.out.println("Utiliser la commande ls pour afficher les fichiers et documents present sur le repertoire courant du serveur.");
		System.out.println("Utiliser la commande mkdir pour creer un nouveau dossier.");
		System.out.println("Utiliser la commande upload <nom fichier> pour televerser un fichier sur le serveur.");
		System.out.println("Utiliser la commande download <nom fichier> pour telecharger un fichier du serveur vers le repertoire local du client.");
		System.out.println("Utiliser la commande exit pour vous déconnecter du serveur. \n");
	}
}
