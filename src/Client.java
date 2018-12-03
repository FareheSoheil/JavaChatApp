
import java.net.*;
import java.io.*;
import java.util.*;

/*@author Farehe
 * The Client that can be run both as a console or a GUI
 */
public class Client  {

	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;
	private ClientGraphic cg;
	// the server, the port and the username
	private String server, username;
	private int port;

	/*
	 *  Constructor called by console mode
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	Client(String server, int port, String username) {
		// which calls the common constructor with the GUI set to null
		this(server, port, username, null);
	}

	/*
	 * Constructor call when used from a GUI
	 * in console mode the ClienGUI parameter is null
	 */
	Client(String server, int port, String username, ClientGraphic cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;
	}
	
	/*
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the server
		try {
                                    socket = new Socket(server, port);
                                      // if it failed not much I can so
                                  } catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}
		// creates the Thread to listen from the server 
		new ListenFromServer(this).start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be Message objects
		try
		{
			sOutput.writeObject(username);
		}catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
        private void display(String msg) {
		if(cg == null)
			System.out.println(msg);      // println in console mode
		else
			cg.append(msg + "\n");		// append to the ClientGraphic JTextArea (or whatever)
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(Message msg) {
            
		try {
		        sOutput.writeObject(msg.getMessage());
                                        System.out.println("send massage class Client :");
                                                
		   }catch(IOException e) {
			display("Exception writing to server: " + e);
		   }
		
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
            cg.flg=false;
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
		
		// inform the GUI
		cg.connectionFailed();

                                 Thread.yield();
	}
	
	public static void main(String[] args) {
		// default values
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";

		
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
                
		// test if we can start the connection to the Server
		if(!client.start())
			return;
		
		
		// done disconnect
		client.disconnect();	
	}

	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
    class ListenFromServer extends Thread {

                public Client client;
	
                public void run() {
		while(true) {
                                    try {
                                            String msg = (String) sInput.readObject();
			// if console mode print the message and add back the prompt
                                            if(cg == null) {
				System.out.println(msg);
				System.out.print("> ");
				}
                                                else {

                                                    String[] msgParts=msg.split("\n");
                                                    String[] msgHeader=msgParts[0].split(" ");
						
                                               try{
                                                    int length = Integer.parseInt(msgHeader[4]);                                        
                                                    String mainMsg="";
                                                    int cnt=1;
                                                    while( mainMsg.length() < length && cnt< msgParts.length)
                                                        mainMsg+=msgParts[cnt++]+"\n";
                                                  
                                                    if(Integer.parseInt(msgHeader[1])==Message.MESSAGE){
                                                        client.sendMessage(new Message(Message.DELIVER, msgHeader[3]+" "+msgHeader[2]+" 10 \nDelivered!"));				
                                                        cg.append(msgHeader[2]+":"+mainMsg+"\n");
                                                    }else if(Integer.parseInt(msgHeader[1])==Message.DELIVER){
                                                        cg.append("Your Message has been Recieved By "+msgHeader[2]+"\n");                                                        
                                                    }
                                                    
                                                }catch(Exception e){
                                                    cg.append(msg);
                                                }
                                               
                                          }
                                }catch(IOException e) {	
                                        display("Server has close the connection: " + e);

                                        try{
                                            Thread.sleep(100);
                                        }catch(Exception e1){}

                                        cg.connectionFailed();
	                        break;
                                    // can't happen with a String object but need the catch anyhow
                               }catch(ClassNotFoundException e2) {				
                                        System.out.println("error!");
		}
                    }
        }
            
     public ListenFromServer(Client clnt){
                    this.client=clnt;
                }
            }
}

