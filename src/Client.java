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
				System.out.format("The server is running on %s:%d\n",clientAddress,clientPort);
				String request = "";
				String response = "";

				// Creer un canal entrant pour recevoir message envoyer par serveur sur canal
				DataInputStream in = new DataInputStream(socket.getInputStream()); 
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				// Attente de la reception d'un message envoyer par le serveur sur le canal
				String helloMsgFromServer = in.readUTF();
				System.out.println("Message 1 from serveur : " + helloMsgFromServer);
				
				// Sends the input of the client to the server.
				request = scanner.nextLine();
				out.writeUTF(request); // envoie ce que le client a ecrit au serveur.
				
				// Depending on the requests that was send to the server.
				if(request.equals("exit")) {
					System.out.println("Vous avez été déconnecté avec succès.");
					response = in.readUTF();
					System.out.println("Message du serveur :" + response);
					socket.close();	// Fermer la connection
				}
		 }
		 catch(Exception e){
			 System.out.println("Connection refusé. Vérifier si le serveur est connecté ou si l'adresse inscrite correspond a l'adresse du serveur. \n");
		 }
	}	
}
