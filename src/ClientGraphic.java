
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*@ author Farehe
 * The Client with its GUI
 */
public class ClientGraphic extends JFrame implements ActionListener,KeyListener {

	private static final long serialVersionUID = 1L;
	// will first hold "Username:", later on "Enter message"
	private JLabel senderlabel,source,dest;
	// to hold the  Username and later on the messages

	// to hold the server address an the port number
	private JTextField senderuser,reciever,Servername, Portname;
	// to Logout and get the list of the users
	private JButton login, logout, whoIsIn;
	// for the chat room
	private JTextArea display,massanger;
	// if it is for connection
	private boolean connected;
                public boolean flg;
                private  JPanel northPanel,serverAndPort,labelpanel,textboxpanel,propertypanel;
	// the Client object
	private Client client;
	// the default port number
	private int defaultPort;
	private String defaultHost,userName;

	// Constructor connection receiving a socket number
	ClientGraphic(String host, int port) {

		super("Chat Client");
                                this.setResizable(false);
                                
		defaultPort = port;
		defaultHost = host;
		
		// The NorthPanel with:
		 northPanel = new JPanel(new GridLayout(4,1));
		// the server name anmd the port number
		 serverAndPort = new JPanel(new GridLayout(1,6, 1, 3));
                                 //massages and usernames labels
                                labelpanel = new JPanel(new GridLayout(1,2, 1, 3));
                                //massages and usernames textboxes
                                textboxpanel = new JPanel(new GridLayout(1,2,20, 3));
                                // client's current username
                                propertypanel =  new JPanel(new GridLayout(1,2,20,10));
		

                                // the two JTextField with default value for server address and port number
		Servername = new JTextField(host);
		Portname= new JTextField("" + port);
		Portname.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(Servername);
                                serverAndPort.add(new JLabel(""));
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(Portname);
		serverAndPort.add(new JLabel(""));
		// adds the Server an port field to the GUI
		northPanel.add(serverAndPort);
//---------------------------------------------------------------------------------------------------
		// the Label and the TextField
		source = new JLabel("Enter your username below");
		dest = new JLabel("   Reciever username ");
                                labelpanel.add(source);
		labelpanel.add(dest);
                                northPanel.add(labelpanel);
//--------------------------------------------------------------------------------------------------

		massanger= new JTextArea("");
		massanger.setBackground(Color.WHITE);
                                 JScrollPane scrollpane = new JScrollPane(massanger);
                                scrollpane.setVerticalScrollBarPolicy(
                                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                                scrollpane.setHorizontalScrollBarPolicy(
                                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                                scrollpane.setSize(100,60);
                                
                                reciever = new JTextField("");
		reciever.setBackground(Color.WHITE);
                
                                textboxpanel.add(scrollpane);
                                textboxpanel.add(reciever);
	              northPanel.add(textboxpanel);
		 
//----------------------------------------------------------------------------------------------------
                                senderuser = new JTextField("");
                                senderlabel = new JLabel("Your Current Username :");
                                propertypanel.add(senderlabel);
                                propertypanel.add(senderuser);
                                northPanel.add(propertypanel);
                                
                                add(northPanel, BorderLayout.NORTH);
 //-----------------------------------------------------------------------------------------------------               
                               // The CenterPanel which is the chat room
		display = new JTextArea("This is your Page\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(display));
		display.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);
 //_______________________________________________________________                             
                                // the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);		// you have to login before being able to logout
		whoIsIn = new JButton("Contacts ");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);		// you have to login before being able to Who is in

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		massanger.requestFocus();

	}

	// called by the Client to append text in the TextArea 
	void append(String str) {
		display.append(str);
		display.setCaretPosition(display.getText().length() - 1);
	}
	// called by the GUI is the connection failed
	// we reset our buttons, label, textfield
	void connectionFailed() {

		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		source.setText("Enter your username below");

		// reset port number and host name as a construction time
		Portname.setText("" + defaultPort);
		Servername.setText(defaultHost);
		// let the user change them
		Servername.setEditable(false);
		Portname.setEditable(false);
		// don't react to a <CR> after the username
		massanger.removeKeyListener(this);
		connected = false;}

	
        
        @Override
        public void keyPressed(KeyEvent e){
                // Enter for sending and alt+Enter next line
            if (e.getKeyCode() == KeyEvent.VK_ENTER ) {
                
                if(e.isAltDown()){
                    massanger.append("\n");
                    massanger.setCaretPosition(display.getText().length() - 1); 
                    
                }else if(connected) {
                            // just have to send the message
                            client.sendMessage(new Message(Message.MESSAGE, this.userName+" "+reciever .getText()+" "+massanger.getText().length()+"\n"+massanger.getText()));				
                            massanger.setText("");

                    }          

            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            
            if(massanger.getText().equals("\n")){
                massanger.setText("");
            }
        }
	/*
	* Button or JTextField clicked
	*/
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		if(o == logout) {
                                                senderuser.setText("");
			client.sendMessage(new Message(Message.LOGOUT, ""));
			return;
		}
		// if it the who is in button
		if(o == whoIsIn) {
			client.sendMessage(new Message(Message.WHOISIN, ""));				
			return;
		}

		// ok it is coming from the JTextField
		if(connected) {
			// just have to send the message
			client.sendMessage(new Message(Message.MESSAGE, this.userName+" "+reciever .getText()+" "+massanger.getText().length()+"\n"+massanger.getText()));				
			massanger.setText("");
			return;
		}
		

		if(o == login) {
			// ok it is a connection request
			String username = massanger.getText().trim();
                                                senderuser.setText(username);
                                                
                                                
			// empty username ignore it
			if(username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = Servername.getText().trim();
			if(server.length() == 0)
				return;
			// empty or invalid port numer, ignore it
			String portNumber = Portname.getText().trim();
			if(portNumber.length() == 0)
				return;
                        
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}catch(Exception en) {
				return;   // nothing I can do if port number is not valid
			}
			

			// try creating a new Client with GUI
			client = new Client(server, port, username, this);
			// test if we can start the Client
		if(!client.start()) 
			return;
                
                               else{
                                             flg=true;
                                             this.userName=username;}
                                                 
                                                    massanger.setText("");
                                                    source.setText("Enter your message below");
                                                    connected = true;
                                                     System.out.println("Start of Dids!");
                                                        // disable login button
                                                      login.setEnabled(false);
                                                        // enable the 2 buttons
                                                      logout.setEnabled(true);
                                                      whoIsIn.setEnabled(true);
                                                        // disable the Server and Port JTextField
                                                       Servername.setEditable(false);
                                                       Portname.setEditable(false);
                                                        // Action listener for when the user enter a message
                                                        massanger.addKeyListener(this);
//                          
                        System.out.println("End of dids!");
                        if(!this.flg){
                            connectionFailed();
                        }else
                            System.out.println(flg);
		}

	}

	// to start the whole thing the server
	public static void main(String[] args) {
		new ClientGraphic("localhost", 8080);
	}

}

