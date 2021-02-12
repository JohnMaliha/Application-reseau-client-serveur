import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Serveur {
	private static ServerSocket listener;
		
	public static void main(String[] args) throws Exception
	{
		// compteur incrementer a chaque connection dun client au server
		int clientNumber =0;
		
		// Adresse et port serveur
		String serverAddress = "127.0.0.1";
		int serverPort =5000;
		
		// Creer la connection pr communiquer avec les clients
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAddress);
		
		// Associer adresse et port a la connection
		listener.bind(new InetSocketAddress(serverIp,serverPort));
		
		System.out.format("The server is running on %s:%d%n",serverAddress,serverPort);
		
		try {
			// chaque nouvelle connection dun client declanche une execution de Run() de ClientHandler.
			while(true) {
				// fct accept est bloquante.
				new ClientHandler(listener.accept(),clientNumber++).start();
				
			}
		}
		finally {
			// fermer la connection
			listener.close();
		}
	}
	
	// Thread 1ui se charge de traiter la demande de chaque client sur un socket particulier.
	private static class ClientHandler extends Thread {
		private Socket socket;
		private int clientNumber;
		
		public ClientHandler(Socket socket,int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New Connection with client #" + clientNumber + "at" + socket);
		}
		
		// une thread se charge denvoyer au client un msg de bienvenue.
		public void run() {
			try {
				// creer un canal sortant pour envoyer des msg au client.
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				// envoie un msg au client
				out.writeUTF("Hello from server - you are client #" + clientNumber );
			}
			catch(IOException e) {
				System.out.println("Error handling client#" + clientNumber + ": " + e);
			}
			finally{
				try {
					// fermeture de la connection avec client.
					socket.close();
				}
				catch(IOException e) {
					System.out.println("Couldn't close a socket, what's going on?");
				}
				System.out.println("Connection with client#" + clientNumber + " closed");
			}
		}
	}	
	
}
