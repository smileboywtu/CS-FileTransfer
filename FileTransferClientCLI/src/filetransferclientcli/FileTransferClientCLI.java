/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransferclientcli;

import SerializePackage.DataPackage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import sun.security.pkcs11.wrapper.Constants;

/**
 *
 * @author smile
 */
public class FileTransferClientCLI {

    // set the file path
    private String excel = null;
    private String excelFile = null;
    private String fileDirectory = null;

    // socket
    private Socket client = null;
    private ObjectOutput write = null;

    // package
    private DataPackage dataSlice = null;
    private final int OneTimeDataSize = 1024;

    // constructor
    public FileTransferClientCLI(String hostAddress, int port, String file) throws IOException {
        // set the file path
        excel = file;
        excelFile = "./FileList/";
        // create new file directory
        if (!new File(excelFile).exists()) {
            new File(excelFile).createNewFile();
        }
        fileDirectory = "./FileDirectory/";
        // create new directory
        if (!new File(fileDirectory).exists()) {
            new File(fileDirectory).createNewFile();
        }
        // here create
        createAndConnetServer(hostAddress, port);
        // notify the user
        System.out.println("Connect to server success" + Constants.NEWLINE);
    }

    // start
    public void start() throws IOException {
        // notify the user first
        System.out.println("start to send the file to the server"
                + Constants.NEWLINE
                + "waiting ..."
                + Constants.NEWLINE);
        // send the excel
        sendExcelFile();
        // send the directory
        sendDirectory();
        // send stop signal
        stop();
        // file send done
        System.out.println("file send done" + Constants.NEWLINE);
    }

    // connect to the server
    private void createAndConnetServer(String hostAddress, int port) throws IOException {
        // new socket
        client = new Socket(hostAddress, port);
        // set the output 
        write = new ObjectOutputStream(client.getOutputStream());
    }

    // send stop signal
    private void stop() throws IOException {
        write.writeObject("stop");
    }

    // send the excel file
    private void sendExcelFile() throws IOException {
        // build the file path
        String xls = excelFile + excel;
        // send 
        sendSingleFile(xls);
    }

    // send the file in dirctory
    private void sendDirectory() throws IOException {
        // get the path first
        Path dir = Paths.get(fileDirectory);
        // then load the file
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path file : stream) {
            // send file
            sendSingleFile(file.toString());
        }
    }

    // send sigle file
    private void sendSingleFile(String file) throws IOException {
        // make sure the file is exist
        if (null == file) {
            return;
        }
        // record the serilized object size
        int readSize = 0;
        long totalFileSize = 0;
        String option = "merge";
        // file stream
        InputStream fileReader = null;
        byte[] buffer = null;
        Path path = Paths.get(file);
        // recorde the size
        totalFileSize = (new File(file)).length();
        // open the file
        fileReader = Files.newInputStream(path, StandardOpenOption.READ);
        while (fileReader.available() > 0) {
            // new object every time send a package
            dataSlice = new DataPackage();
            // new buffer
            buffer = new byte[OneTimeDataSize];
            // read OneTimeDataSize data
            readSize = fileReader.read(buffer, 0, OneTimeDataSize);
            // set the properties of object
            dataSlice.setData(buffer);
            dataSlice.setOption(option);
            dataSlice.setFileLength(totalFileSize);
            dataSlice.setFileName(path.getFileName().toString());
            dataSlice.setSliceLength(readSize);
            // send the package size first
            write.writeObject("start");
            // send the slice
            write.writeObject(dataSlice);
            // flush
            write.flush();
            // set option
            if (option.equals("merge")) {
                option = "continue";
            }
            // discard the memory
            buffer = null;
        }
    }

    // get the user params
    public static void main(String[] args) throws IOException {
        // the params must contains three params
        if (args.length < 3) {
            // notify the user
            System.out.println("Usage: java -jar FileTransferClientCLI "
                    + "<host> <port> <excelfile>" + Constants.NEWLINE);
        } else {
            // start to work
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String file = args[2];
            // new the user
            FileTransferClientCLI user = new FileTransferClientCLI(
                    host, port, file);
            // transfer the data
            user.start();
            // close
            System.out.println("restart the thread if you need to transfer again"
                    + Constants.NEWLINE);
        }
    }
}
