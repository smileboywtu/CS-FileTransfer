/*
 *  this class is user to react to the setting button in the main interface
 */
package filetransferclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 *
 * @author smile
 */
public class GetUserServer extends JDialog implements ActionListener {

    // private members
    private JPanel contentPane = null;
    
    // Transfer Client 
    private String serverName = "";
    private int serverPort = 0;
    private Socket client = null;
    private boolean serverStatus = false;
    
    // record the Stream
    private ObjectOutput sendBuffer = null;

    // connet and cancil
    private final String connetCommand = "connet";
    private final String cancilCommand = "cancil";
    private JButton connetButton = null;
    private JButton cancilButton = null;

    // Label and text input fields
    private JLabel serverAddrLabel = null;
    private JLabel serverPortLabel = null;
    private JFormattedTextField serverAddrTextField = null;
    private JFormattedTextField serverPortTextField = null;
    private JLabel serverAddrWarn = null;
    private JLabel serverPortWarn = null;

    // constructor
    public GetUserServer(JComponent parent) {
        // noting to do here
        // make the Dialog model
        super((JFrame)null, true);
        // maybe here you can set the components relative to
        this.setTitle("Connet the Server");    
        this.setLocationRelativeTo(parent);
    }

    public JDialog createCustomDialog() {
        // first let the dialog not auto close
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        // set the content pane
        this.setContentPane(createContentPane());
        // add a window listener
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // let the user setting the server address and port first
                // if not
                
            }
        });
        // this will make the sever IP field focus
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                serverAddrTextField.requestFocusInWindow();
            }
        });
        // return this
        return this;
    }

    private JPanel createContentPane() {
        // new the instance
        contentPane = new JPanel(new BorderLayout());

        // add components here
        // buttons
        connetButton = new JButton("connet");
        connetButton.setActionCommand(connetCommand);
        connetButton.addActionListener(this);
        cancilButton = new JButton("cancil");
        cancilButton.setActionCommand(cancilCommand);
        cancilButton.addActionListener(this);
        // labels
        serverAddrLabel = new JLabel("Address: ");
        // set the ipv4 formator
        serverAddrTextField = new JFormattedTextField();
        serverAddrTextField.setText("127.0.0.1");
        serverAddrTextField.setColumns(15);
        serverAddrTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean shouldYieldFocus(JComponent input) {
                boolean inputVerify = verify(input);
                if (inputVerify) {
                    serverAddrWarn.setText(" ");
                    return true;
                } else {
                    // set the warning label to notify the user
                    serverAddrWarn.setText("*");
                    return false;
                }
            }

            @Override
            public boolean verify(JComponent input) {
                JTextField field = (JTextField) input;
                return InetAddressValidator.getInstance().isValidInet4Address(
                        field.getText());
            }
        });
        serverAddrWarn = new JLabel(" ");
        serverAddrWarn.setForeground(Color.red);

        serverPortLabel = new JLabel("Port: ");
        serverPortTextField = new JFormattedTextField();
        // set default value
        serverPortTextField.setText("8888");
        serverPortTextField.setColumns(15);
        // set the formator
        serverPortTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean shouldYieldFocus(JComponent input) {
                boolean inputVerify = verify(input);
                // check
                if (inputVerify) {
                    serverPortWarn.setText(" ");
                    return true;
                } else {
                    // set the label
                    serverPortWarn.setText("*");
                    return false;
                }
            }

            @Override
            public boolean verify(JComponent input) {
                // get the input
                String str = ((JFormattedTextField) input).getText();
                // check the data
                int portNumber = Integer.parseInt(str);
                // varify
                return !(portNumber <= 1024 || portNumber >= 9999);
            }
        });
        // set warn
        serverPortWarn = new JLabel(" ");
        serverPortWarn.setForeground(Color.red);
        
        // layout setting
        JPanel namePane = new JPanel(new GridLayout(0, 1));
        namePane.add(serverAddrLabel);
        namePane.add(serverPortLabel);
        namePane.setBorder(new EmptyBorder(3, 5, 3, 5));
        
        JPanel textFieldPane = new JPanel(new GridLayout(0, 1));
        textFieldPane.add(serverAddrTextField);
        textFieldPane.add(serverPortTextField);
        textFieldPane.setBorder(new EmptyBorder(3, 5, 3, 5));
        
        JPanel warnPane = new JPanel(new GridLayout(0, 1));
        warnPane.add(serverAddrWarn);
        warnPane.add(serverPortWarn);
        warnPane.setBorder(new EmptyBorder(3, 5, 3, 10));
        
        JPanel buttonPane = new JPanel();
        buttonPane.add(connetButton);
        buttonPane.add(cancilButton);
        buttonPane.setBorder(new EmptyBorder(3, 5, 3, 5));
           
        // add to the top pane
        contentPane.add(namePane, BorderLayout.LINE_START);
        contentPane.add(textFieldPane, BorderLayout.CENTER);
        contentPane.add(warnPane, BorderLayout.LINE_END);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
     
        // return content panel
        return contentPane;
    }
    
    // everytime check the server when send the data
    public boolean isClosed(){
        return client.isClosed();
    }
    
    // check if the server is conneted
    public boolean isConneted(){
        return serverStatus;
    }
   
    // reconnet to the server
    public void reConnet() throws IOException{
        if(client.isClosed()){
            client = new Socket(serverName, serverPort);
            // reset the stream
            // cause the close method will also close the stream
            sendBuffer = new ObjectOutputStream(client.getOutputStream());   
            // for test
            System.out.println("create new socket");
        }
    }
    
    // close the server
    public void closeServer() throws IOException{
        // this will make the server know the signal to close 
        sendBuffer.writeObject("stop");
        // close
        if(!client.isClosed()){
            // close the socket
            client.close();
            // test
            System.out.println("close the socket");
        }
    }
    
    // write a byte array
    public void serverIOWrite(Object o) throws IOException{
        sendBuffer.writeObject(o);
    }
    
    // wirte an integer to the stream
    public void serverIOWrite(int i) throws IOException{
        // here you must use the scaner to write integer
        sendBuffer.writeObject(i);
    }

    // flush
    public void flush() throws IOException{
        // flush
        sendBuffer.flush();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // get the command first
        String cmd = e.getActionCommand();
        // deal with the command
        switch (cmd) {
            case cancilCommand:
                // close the socket
                try {
                    if (serverStatus) {
                        client.close();
                        // set the status
                        serverStatus = false;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GetUserServer.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                // deal with cancil button
                this.setVisible(false);
                break;
            case connetCommand:
                // deal with connet button
                // try to connet to the server
                serverName = serverAddrTextField.getText();
                serverPort = Integer.parseInt(serverPortTextField.getText());
                // then connet
                {
                    try {
                        client = new Socket(serverName, serverPort);
                        // get the stream
                        sendBuffer = new ObjectOutputStream(client.getOutputStream());
                        // set the status
                        serverStatus = true;
                        // set the dialog disappear
                        this.setVisible(false);
                    } catch (IOException ex) {
                        Logger.getLogger(GetUserServer.class.getName()).log(
                                Level.SEVERE, null, ex);
                        // set the status
                        serverStatus = false;
                    }
                }
                break;
        }
    }
    
    // test ok
    public static void main(String[] args){
        // only for test the GUI
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // instance
                GetUserServer test = new GetUserServer(null);
                
                // create
                test.createCustomDialog();
                
                // show
                test.pack();
                test.setVisible(true);
            }
        });
    }
}
