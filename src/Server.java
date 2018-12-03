import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*@ autjor Farehe
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// an ID for each connection
	private static int clientid;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientHandler> clientHandlers;
	// an ArrayList to keep offline messages
	private ArrayList<String> offlineMassages;
	// if I am in a GUI
	private ServerGraphic sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned off to stop the server
	private boolean keepGoing;
	

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
	public Server(int port) {
            this(port, null);
	}
	
	public Server(int port, ServerGraphic sg) {
            // GUI or not
            this.sg = sg;
            // the port
            this.port = port;
            // to display hh:mm:ss
            sdf = new SimpleDateFormat("HH:mm:ss");
            // ArrayList for the Client list
            clientHandlers = new ArrayList<ClientHandler>();
            //ArrayList for offline massages
            offlineMassages=new ArrayList<String>();

	}
	
	public void start() {
            keepGoing = true;
            /* create socket server and wait for connection requests */
            try 
            {
                // the socket used by the server
                ServerSocket serverSocket = new ServerSocket(port);

                    // infinite loop to wait for connections
                    while(keepGoing) 
                    {
                        // format message saying we are waiting
                        display("Server waiting for Clients on port " + port + ".");

                        Socket socket = serverSocket.accept();  	// accept connection
                        // if I was asked to stop
                        if(!keepGoing)
                                break;

                        ClientHandler t = new ClientHandler(socket);  // make a thread of it
                        clientHandlers.add(t);									// save it in the ArrayList
                        t.start();
                    }
                    // I was asked to stop
                    try {
                            serverSocket.close();
                            for(int i = 0; i < clientHandlers.size(); ++i) {
                                    ClientHandler tc = clientHandlers.get(i);
                                    try {
                                    tc.Inputstream.close();
                                    tc.Outputstream.close();
                                    tc.socket.close();
                                    }
                                    catch(IOException ioE) {
                                            // not much I can do
                                    }
                            }
                    }
                    catch(Exception e) {
                            display("Exception closing the server and clients: " + e);
                    }
            }
            // something went bad
            catch (IOException e) {
        String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
                        display(msg);
            }
	}		
    /*
     * For the GUI to stop the server
     */
	protected void stop() {
            keepGoing = false;
            // connect to myself as Client to exit statement 
            // Socket socket = serverSocket.accept();
            try {
                    new Socket("localhost", port);
            }
            catch(Exception e) {
                    // nothing I can really do
            }
	}
	/*
	 * Display a message to the console or the GUI
	 */
	private void display(String msg) {

            String time = sdf.format(new Date()) + " " + msg;

            if(sg == null)

                System.out.println(time);
            else
                sg.appendEvent(time + "\n");
	}
        
	/*
	 *  to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
            // add HH:mm:ss and \n to the message
            String time = sdf.format(new Date());
            String messageLf = time + " " + message + "\n";
            // display message on console or GUI
            if(sg == null)
                    System.out.print(messageLf);
            else
                    sg.appendRoom(messageLf);     // append in the room window

            // we loop in reverse order in case we would have to remove a Client
            // because it has disconnected
            for(int i = clientHandlers.size(); --i >= 0;) {
                    ClientHandler ct = clientHandlers.get(i);
                    // try to write to the Client if it fails remove it from the list
                    if(!ct.writeMsg(messageLf)) {
                            clientHandlers.remove(i);
                            display("Disconnected Client " + ct.username + " removed from list.");
                    }
            }
	}
	/*
	 *  to broadcast a message to all Clients
	 */
	private synchronized boolean broadcast(String message,String userName) {
		// add HH:mm:ss and \n to the message
            
		String time = sdf.format(new Date());
                
		String messageLf = time + " " + message + "\n";
                
                boolean messageSent=false;
		// display message on console or GUI
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window
		
		// we loop in reverse order in case we would have to remove a Client
                
		// because it has disconnected
		for(int i = clientHandlers.size(); --i >= 0;) {
			ClientHandler ct = clientHandlers.get(i);
			// try to write to the Client if it fails remove it from the list
			if( ct.username.equals(userName)){
                            messageSent=true;
                            if(!ct.writeMsg(messageLf)) {
				clientHandlers.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
                                messageSent=false;
                            }
                            break;
                        }
		}
                return messageSent;
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < clientHandlers.size(); ++i) {
			ClientHandler clienthandler = clientHandlers.get(i);
			// found it
			if(clienthandler.id == id) {
				clientHandlers.remove(i);
				return;
			}
		}
	}
	
	
    public static void main(String[] args) {
	// start server on port 1500 unless a PortNumber is specified 
	int portNumber = 1500;
	Server server = new Server(portNumber);
		server.start();
	}

   /** One instance of this thread will run for each client */
   class ClientHandler extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream Inputstream;
		ObjectOutputStream Outputstream;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		String massage;
		// the date I connect
		String date;

	// Constructor	
      public ClientHandler(Socket socket) {
			
			id = ++clientid;
			this.socket = socket;
			
	
                    try
                        {               // create output first
		Outputstream = new ObjectOutputStream(socket.getOutputStream());
		Inputstream = new ObjectInputStream(socket.getInputStream());
		// read the username from client
                                 username = (String) Inputstream.readObject();
                                                       
                                // controlls number of online clients (less than 25)                               
                                if(clientHandlers.size()>=25){
                                                writeMsg("Can not accept any other client.Try later\n");
                                                close();
                                                return;    }  
                                
                                // checks that the usernames don't have " "
                                if(username.indexOf(" ")!=-1) {
                                        writeMsg("Username has space!.Try again\n");
                                        close();
                                        return;}
                                
                                //check if the username had been taken before
                                for(int i = 0; i < clientHandlers.size(); ++i) {
                                        ClientHandler clienthandler = clientHandlers.get(i);
                                        if(clienthandler.getId()  != id && clienthandler.username.equals(username)) {
                                                writeMsg("Username had been taken before.Try again!\n");
                                                close();
                                                return;}
                                        }       
                                
                                 // first sends the offline massages
                                for(int k=0; k<offlineMassages.size(); k++){
                                        String[] str=offlineMassages.get(k).split(" ");
                                        if(str[0].equals(username)){
                                              int cnt=1;
                                               String msg="";
                                            // constructing the massage
                                             while( cnt< str.length)
                                                  msg+=str[cnt++]+" ";

                                            if(writeMsg(msg)){
                                                System.out.println("writing offline massages");
                                                offlineMassages.remove(k--);
                                                }
                                            }
                                        }  
                                    

                                display(username + " just connected.");

		}catch (IOException e) {
			
			display("Exception creating new Input/output Streams: " + e);
			return;
                                  // have to catch ClassNotFoundException
		}catch (ClassNotFoundException e) {}

                                date = new Date().toString() + "\n";
		}

		// run forever
    public void run() {
		// to loop until LOGOUT
		boolean keepGoing = true;
                        
		while(keepGoing) {
			// read a String (which is an object)
			try {
				massage= (String) Inputstream.readObject();
			}catch (IOException e) {
				display(username + " Exception reading Streams: " + e);
				break;				
			}catch(ClassNotFoundException e2) {
                                                            break;}

                                // the messaage part of the Message:	
                                //split header from content
                                String[] msgPart=massage.split("\n");
                                //split the different part of header
                                String[] msgHeader=msgPart[0].split(" ");
                                // Switch on the type of message receive
                                int verb = Integer.parseInt(msgHeader[0]),length,cnt;
                                String msg;
                                
	 switch(verb) {

                	case Message.DELIVER:
                                        length = Integer.parseInt(msgHeader[3]);                                        
                                        msg="";
                                        cnt=1;
                                      while( msg.length() < length && cnt< msgPart.length)
                                            msg+="\n"+msgPart[cnt++];
                                        //means client is offline
	                      if(!broadcast(Message.DELIVER+" "+username + " "+msgHeader[2]+" "+msg.length()+"\n"+ msg, msgHeader[2])){
                                            String time = sdf.format(new Date());
                                            addToBuffer(time+" "+Message.DELIVER+" "+username + " "+msgHeader[2]+" "+msg.length()+"\n"+ msg, msgHeader[2]);}
                                        break;
                            
		case Message.MESSAGE:
                                        length = Integer.parseInt(msgHeader[3]);                                        
                                        msg="";
                                        cnt=1;
                                        while( msg.length() < length && cnt< msgPart.length)
                                            msg+="\n"+msgPart[cnt++];
                                        //means client is offline
                	        if(!broadcast(Message.MESSAGE+" "+username + " "+msgHeader[2]+" "+msg.length()+"\n"+ msg, msgHeader[2])){
                                            String time = sdf.format(new Date());
                                            addToBuffer(time+" "+Message.MESSAGE+" "+username + " "+msgHeader[2]+" "+msg.length()+"\n"+ msg, msgHeader[2]);}
                                        break;
                    
		case Message.LOGOUT:
                                        display(username + " disconnected with a LOGOUT message.");
		        keepGoing = false;
                                        break;
				
                                case Message.WHOISIN:
                                    writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
		     // scan clientHandlers the users connected
                                    for(int i = 0; i < clientHandlers.size(); ++i) {
                                            ClientHandler clienthandler = clientHandlers.get(i);
                                            writeMsg((i+1) + ") " + clienthandler.username + " since " + clienthandler.date);}
					
		   break;
                                    
		}
	}        
                                //!keepgoing
		// remove myself from the arrayList containing the list of theconnected Clients
		remove(id);
		close();
        }
               
                
                       // add messages of those who are offline
       private void addToBuffer(String msgStr,String dstUserName) {
                                     offlineMassages.add(dstUserName+" "+msgStr);  }                
                             
                
      // try to close everything
        public void close() {
		// try to close the connection
		try {
                                        if(Outputstream != null) Outputstream.close();
                                    }catch(Exception e) {}
			
		try {
                                   if(Inputstream != null) Inputstream.close();
		}catch(Exception e) {};
			
                                try {
                                       if(socket != null) socket.close();
		}catch (Exception e) {}
			
	}
                

    /*
    * Write a String to the Client output stream
    */
      private boolean writeMsg(String msg) {
                    
	// if Client is still connected send the message to it
        if(!socket.isConnected()) {
            close();
            return false;
        }
        // write the message to the stream
        try {
            Outputstream.writeObject(msg);
            System.out.println("I am in write massage in server class");
        }catch(IOException e) {
            display("Error sending message to " + username);
            display(e.toString());
        }

        return true;}

 public  int geTid() {
    return id;
     }

public void setId(int id) {
     this. id = id;
 }
    }// End of client handler 
	

    
        
         
}


