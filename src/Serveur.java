import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Serveur {
	private static ServerSocket listener;
	private static boolean isConnected = true;

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
		
		System.out.format("The server is running on %s:%d\n",serverAddress,serverPort);
		
		try {
			// chaque nouvelle connection dun client declanche une execution de Run() de ClientHandler.
			while(isConnected) {
				// fct accept est bloquante. on creer un thread a chaque nouveau client.
				new ClientHandler(listener.accept(),clientNumber++).start(); // start fait que on a 1 thread par connection
				
			}
		}	
		catch(Exception e){
			listener.close();
			isConnected = false; 
			e.printStackTrace();
		}
		/*
		finally {
			// arrete decouter sur le port
			listener.close();
		}
		*/
		
	}
	
	/* Client handler. Permet d'avoir plusieurs connection possible.
	 * Thread ce charge de traiter la demande de chaque client sur un socket particulier.
	 * 
	 */
	private static class ClientHandler extends Thread {
		private Socket socket;
		private int clientNumber;
		
		// constructeur
		public ClientHandler(Socket socket,int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New Connection with client # " + clientNumber + " at " + socket);
		}
		// Une thread se charge denvoyer au client un msg de bienvenue.
		@Override
		public void run() {
			String request; 
			String response;
			try {
				// creer un canal sortant pour envoyer des msg au client.
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				// envoie un msg au client
				out.writeUTF("Hello from server - you are client #" + clientNumber );
				
				// we recived a request from the client.
				DataInputStream in = new DataInputStream(socket.getInputStream());
				request = in.readUTF();
				
				// Depending on the recives request
				if(request.equals("exit")) {
					System.out.println("Le client : " + clientNumber + " déconnecter"); 
					// sends a confirmation message to the client.
					response = "Le client "+ clientNumber +" est déconnecté du serveur";
					out.writeUTF(response);
					this.clientNumber--;
					//socket.close();
				}
				// other requests.
			}
			catch(IOException e) {
				System.out.println("Error handling client#" + clientNumber + ": " + e);
			}
//			finally{
//				try {
//					socket.close(); // fermeture de la connection avec client.
//				}
//				catch(IOException e) {
//					System.out.println("Couldn't close a socket, what's going on?");
//				}
//				// System.out.println("Connection with client# " + clientNumber + " closed");
//		}
		}
	}	
	
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
	
	public static  boolean PortVerifier(int port) {
		boolean validPort = false;
		if(port >= 5000 && port <= 5050) {
			validPort = true;
			System.out.println("Le port : " + port + " est valide! \n");
		}	
		else System.out.println("Le port : " + port + " n'est pas valide. Veillez inscrire un port valide compris entre 5000 a 5050. \n");
			
		return validPort;
	}
	
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
