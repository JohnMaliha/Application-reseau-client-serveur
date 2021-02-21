import java.io.DataInputStream;
import java.net.Socket;

public class Client {
	private static Socket socket;
	
	
	
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
			System.out.println("Entrer une adresse IP et un port en suivant ce format : XXX.XXX.XX.XX:PORT");
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
			 // LE CLient se connecte au serveur. (Nouvelle connection)
				socket = new Socket(clientAddress,clientPort);
				System.out.format("The server is running on %s:%d%n",clientAddress,clientPort);
				
				// Creer un canal entrant pour recevoir message envoyer par serveur sur canal
				DataInputStream in = new DataInputStream(socket.getInputStream()); 
				DataInputStream in2 = new DataInputStream(socket.getInputStream());
				
				// Attente de la reception d'un message envoyer par le serveur sur le canal
				String helloMsgFromServer = in.readUTF();
				System.out.println("Message 1 from serveur : " + helloMsgFromServer);
				System.out.println("Message 2 from server : " + in2.readUTF()); 
				
				// Fermer la connection
				socket.close(); 
		 }
		 catch(Exception e){
			 System.out.println("Connection refusé. Vérifier si le serveur est connecté ou si l'adresse inscrite correspond a l'adresse du serveur. \n");
		 }
	}	
}
