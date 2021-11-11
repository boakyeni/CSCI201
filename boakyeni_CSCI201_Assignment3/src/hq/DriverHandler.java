/*package hq;

import java.io.*;
import java.net.*;
// thread class for handling new connection
public class DriverHandler implements Runnable {
	private Socket socket; // A connected socket
	//Constructing a thread
	public DriverHandler(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			// Create data in and out streams
			//grabs system.in of client
			ObjectInputStream inputFromDriver = new ObjectInputStream(socket.getInputStream());
			
			//replies from server to client 
			ObjectOutputStream outputToDriver = new ObjectOutputStream(socket.getOutputStream());
			
			//Continuously serve the client
			while(true) {
				
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}*/
