/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransferservercli;

import SerializePackage.DataPackage;
import com.mongodb.MongoClient;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.security.pkcs11.wrapper.Constants;

/**
 *
 * @author smileboy
 */
public class FileTransferServerCLI {

    // handler of the listening socket
    private ServerSocket server = null;
    // server mode
    private boolean userMode = true;
    // here create the mongo user
    MongoClient user = new MongoClient();
    // here use a counter to create templete directory
    private static int directory = 0;

    // create the socket
    public boolean createServer(String port) throws IOException {
        // get the port
        int portNumber = Integer.parseInt(port);
        // new socket and open this
        server = new ServerSocket(portNumber);
        // notify the user
        System.out.println("Server Start Ok. ");
        System.out.println("Server Address: " + server.getInetAddress().getHostAddress());
        System.out.println("Server Port: " + server.getLocalPort());
        // return 
        return true;
    }

    // single or multi-client mode
    // what ever type you want to use the client, you will use a thread to handle
    // the transfer data
    public void setMultiClientMode(String[] args) {
        if (args.length > 1 && args[1].equals("true")) {
            userMode = true;
            // notify the user
            System.out.println("Turn on the multi-user mode");
        } else {
            userMode = false;
            // notify the usr
            System.out.println("Server start as single user mode, "
                    + "you can restart the program with params of true "
                    + "to open the multi-user mode");
        }
    }

    public void logMessage() {
        System.out.println("********************************************");
        System.out.println("*            server log message            *");
        System.out.println("********************************************");
        System.out.println();
    }

    // user mode
    public boolean getServerMode() {
        return userMode;
    }

    // listen to the port
    public void acceptClient() throws IOException {
        // instance here
        Socket acceptSocket = null;
        // main loop
        while (true) {
            // listen
            if (server.isClosed()) {
                System.out.println("Server Closed");
            }
            // listen
            // this will wait until a client request hava occured
            acceptSocket = server.accept();
            System.out.println("new client hava joint");
            if (getServerMode()) {
                // use thread to offer service
                (new Thread(new FileStoreService(acceptSocket, user, directory++))).start();
                // notify user
                System.out.println("Server waiting new client..." + Constants.NEWLINE);
            } else {
                // use thread to offer service
                (new Thread(new FileStoreService(acceptSocket, user, directory++))).start();
                // notify user
                System.out.println("Server use single client mode, Server close listen");
                // break;
                break;
            }
        }
        // free
        acceptSocket = null;
    }

    // check the args
    public static boolean checkServerSocketArgs(String[] args) {
        // check
        if (args.length > 0) {
            return true;
        } else {
            System.out.println("Usage: java FileTransferServer <port number> <mode>");
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        // check first
        if (checkServerSocketArgs(args)) {
            //  create the instance
            FileTransferServerCLI server = new FileTransferServerCLI();
            // open the socket
            server.createServer(args[0]);
            // set the mode
            server.setMultiClientMode(args);
            // message title
            server.logMessage();
            // now start to listen
            server.acceptClient();
        }
        // for test
        FileStoreService.updateProcess("test", 30, 100, 5);
    }

}

// thread to offer service
class FileStoreService implements Runnable {

    // client socket
    private Socket client = null;
    private ObjectInput in = null;
    private String fileStoreDir = null;
    // use this to save file to the database
    private UsingMongoSavaFile sync = null;

    // constructor
    public FileStoreService(Socket soc, MongoClient user, int key) throws IOException {
        // here just record the client
        client = soc;
        // set the stream
        in = new ObjectInputStream(client.getInputStream());
        // set the dir
        fileStoreDir = "./FileStoreDirectory"+key+"/";
        // create directory
        if (!new File(fileStoreDir).exists()) {
            Files.createDirectories(Paths.get(fileStoreDir));
        }
        // create the database
        sync = new UsingMongoSavaFile(user, fileStoreDir);
    }

    @Override
    public void run() {
        // instance
        String option = "";
        // data slice
        DataPackage dataSlice = null;
        // start to receive message
        System.out.println("start to transfer file...");
        // loop stream
        while (true) {
            try {
                //first read size
                option = (String) in.readObject();
                // option
                if ("stop".equals(option)) {
                    // close the server service for the client
                    client.close();
                    break;
                }
                // read the slice out
                dataSlice = (DataPackage) in.readObject();
                // save the file slice
                saveDataSlice(dataSlice);
            } // end while
            catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(FileTransferServerCLI.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        // close the server
        System.out.println("connect interrupt." + Constants.NEWLINE);
        // show time
        System.out.println("transfer time: "
                + LocalDateTime.now().toLocalDate() + "  "
                + LocalDateTime.now().toLocalTime() + Constants.NEWLINE);
        // sync the file
        // this means the file transfered done
        (new Thread(sync)).start();
    }// end run

    // save the file
    private void saveDataSlice(DataPackage slice) throws IOException {
        // file path
        String path = fileStoreDir + slice.getFileName();
        // new the file if not exist
        File target = new File(path);
        // write stream use here
        OutputStream write = null;
        // test the file
        if (target.exists()) {
            // just open
            if (!slice.getOption().equals("merge")) {
                updateProcess(slice.getFileName(),
                        target.length(),
                        slice.getFileLength(),
                        slice.getSliceLength());
            } else {
                updateProcess(slice.getFileName(),
                        0,
                        slice.getFileLength(),
                        slice.getSliceLength());
            }
        } else {
            // create file
            Files.createFile(Paths.get(path));
            // update the Process
            updateProcess(slice.getFileName(),
                    0,
                    slice.getFileLength(),
                    slice.getSliceLength());
        }
        // deal with merge option
        if (slice.getOption().equals("merge")) {
            write = Files.newOutputStream(Paths.get(path),
                    StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            write = Files.newOutputStream(Paths.get(path),
                    StandardOpenOption.APPEND);
        }
        // write the content
        write.write(slice.getData(), 0, slice.getSliceLength());
        // flush
        write.flush();
        // close the file
        write.close();
    }

    // update file transfer process
    public static void updateProcess(String name, long cur, long total, long add) {
        // show file name
        System.out.println("process file: " + name);
        // show status
        System.out.print("\t");
        // process
        System.out.print("[");
        long process = Math.round(((float) (cur + add) / total) * 30);
        int i = 0;
        for (i = 0; i < process; i++) {
            System.out.print("#");
        }
        while (i++ < 30) {
            System.out.print("-");
        }
        System.out.print("]\t");
        // show percent
        float percent = (float) (cur + add) / total * 100;
        System.out.printf("%.3f ", percent);
        System.out.println("%" + Constants.NEWLINE);
    }
}
