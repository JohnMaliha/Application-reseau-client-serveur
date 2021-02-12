import java.io.DataInputStream;
import java.net.Socket;

public class Client {
	private static Socket socket;
	
	
	
	public static void main(String[] args) throws Exception
	{
		String serverAdress = "127.0.0.1"; 
		int port =5000; 
		
		// creer nouvelle connection avec serveur
		socket = new Socket(serverAdress,port);
		System.out.format("The server is running on %s:%d%n",serverAdress,port);
		
		// Creer un canal entrant pour recevoir message envoyer par serveur sur canal
		DataInputStream in = new DataInputStream(socket.getInputStream()); 
		
		
		// attente de la reception dun message envoyer par le serveur sur le canal
		String helloMsgFromServer = in.readUTF();
		System.out.println(helloMsgFromServer);
		
		// Fermer la connection
		socket.close(); 
	}
}
