import java.io.DataInputStream;
import java.io.DataOutputStream;
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
		System.out.println("Le client essaie de se connecter au serveur");
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
				System.out.println("Une adresse ip ne peux pas contenir une valeur n�gative. \n");
				isIPGood = false;
			}
			
		 } while(!isIPGood || !isPortGood );
		 
		 //------------------------------- Client code---------------------------------------------//
		 try {
			 	// Read the response written by the client to send to the server.
			 	Scanner scanner = new Scanner(System.in);
			 	
			 	// LE CLient se connecte au serveur. (Nouvelle connection)
				socket = new Socket(clientAddress,clientPort);
				System.out.format("Connected to server on  %s:%d \n\n" , clientAddress,clientPort);
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
					longRequest = 	request.split("\\s+"); 
					
					// Depending on the requests that was send to the server.
					if(request.equals("exit")) {
						System.out.println("Vous avez �t� d�connect� avec succ�s.");
					//	while(in.available() != 0)
						response = in.readUTF();
						System.out.println("Message du serveur :" + response);
						socket.close();	// Fermer la connection
						break;
					}	
								
					if(longRequest[0].equals("mkdir")) {
						System.out.println("mkdir " + longRequest[1]);
						response = in.readUTF();
						System.out.println( response);
						// break;
						//return;
					}
					else if (request.equals("cd")) {
						response = in.readUTF();
						System.out.println( response);
						// break;
						//	return;
					}
					else if(request.equals("ls")) {
						int taille = in.read(); // recevoir la taille du nb des fichiers dans le repertoires
						System.out.println(request); 
						for(int i= 0; i< taille;i++) {
							response = in.readUTF();
							System.out.println(response);
						}
						out.flush();
					}
					if(request.equals("cd")) {
				 		
					}
					 	
					if(request.equals("upload")) {
					 		
					}
					 	
					if(request.equals("download")) {
					 		
					}
					
				}
				// fermeture des ressources
				out.flush();
				in.close();
				out.close();
				scanner.close();
		 }
		 catch(Exception e){
			 System.out.println("Connection refus�. V�rifier si le serveur est connect� ou si l'adresse inscrite correspond a l'adresse du serveur. \n");
			// throw e; // debug
		 }
	}	
	
	private static void menu() {
		System.out.println("Voici les commandes disponibles : cd, ls,mkdir,upload,download,exit.");
		System.out.println("Utiliser la commande cd <repertoire> pour se d�palcer vers un repertoire parent ou enfant");
		System.out.println("Utiliser la commande ls pour afficher les fichiers et documents present sur le repertoire courant du serveur");
		System.out.println("Utiliser la commande mkdir pour creer un nouveau dossier");
		System.out.println("Utiliser la commande upload <nom fichier> pour televerser un fichier sur le serveur");
		System.out.println("Utiliser la commande download<nom fichier> pour telecharger un fichier du serveur vers le repertoire local du client");
		System.out.println("Utiliser la commande exit pour vous d�connecter du serveur \n");
	}
}
