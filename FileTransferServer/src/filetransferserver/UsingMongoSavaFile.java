/*
 *  this package used to save add or retrive data from the mogodb database
 * this really the first time i use a database to save the data
 */
package filetransferserver;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import sun.security.pkcs11.wrapper.Constants;

/**
 *
 * @author smile
 */
public class UsingMongoSavaFile implements Runnable{
    
    // member for saving the database user
    // the user only need to create once on the top level
    // ignore the multi-thread read/write the data base
    private MongoClient user = null;
    private MongoDatabase db = null;
                // create new GridFS
    private GridFS model = null;
    // default set true, to make sure the first time can run the thread
    private boolean status = true;
    private boolean goOnSync = false;
    private String syncDirectory = "";
    
    // constructor
    public UsingMongoSavaFile(MongoClient user){
        // record the user
        this.user = user;
        // set the sync directory
        syncDirectory = "./FileStoreDirectory/";
        // set the status
        status = true;
        // set go on status
        goOnSync = false;
        // get the data base
        checkAndInitCollection();
    }
 
    // check the collection
    private boolean checkAndInitCollection(){
        // get the database
        // if not exist, it will create new
        db = user.getDatabase("filestore");
        // create the GridFS
        model = new GridFS(user.getDB("filestore"), "default");
        // return true allways
        return true;
    }
    
    // get the status
    public boolean getStatus(){
        return status;
    }
    
    // continue to transfer
    public void setGoOn(boolean go){
        goOnSync = go;
    }
    
    // interface API to set the directory
    public boolean setSyncDirectory(String dir){
        if(new File(dir).isDirectory()){
            // set member
            syncDirectory = dir;
            // return true
            return true;
        }else{
            // use the default directory
            return false;
        }
    }
    
    @Override
    public void run() {
        while(true){
            // init the messge
            System.out.println(Constants.NEWLINE
                    + "start to save file in Mongo DataBase"
                    + Constants.NEWLINE);
            // set the directory path
            Path dir = Paths.get(syncDirectory);
            // iterate the file
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    // check exist
                    if(null == model.findOne(file.getFileName().toString())){
                        // save to the data base
                        saveToDataBase(file);
                        // notify the user
                        System.out.println(file.getFileName().toString()
                                + " saved done"
                                + Constants.NEWLINE);
                    }else{
                        System.out.println(file.getFileName().toString()+
                                " already exist, do not save again"+
                                Constants.NEWLINE);
                    }
                }
                // notify the user
                System.out.println("all file saved to the database" + Constants.NEWLINE);
                // set the status
                status = true;
            } catch (IOException ex) {
                Logger.getLogger(UsingMongoSavaFile.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            // if go on sync
            if(!goOnSync){
                // give up this thread
                break;
            }else{
                goOnSync = false;
                status = false;
            }
        }
    }
    
    // save the file
    private void saveToDataBase(Path file) throws IOException{
        // create file
        GridFSInputFile in = model.createFile(file.toFile());
        // set the file name
        in.setFilename(file.getFileName().toString());
        // save
        in.save();
    }
    
    // retrive the file
    public OutputStream retriveDataBase(String filename) throws IOException{
        // return file stream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // get the file
        GridFSDBFile out = model.findOne(filename);
        // save to the file
        out.writeTo(os);
        // return 
        return os;
    }
    
    // test for mongo db
    // every thing is simple and useful
    public static void main(String[] args) {
        // new client
        MongoClient client = new MongoClient();
        // get the data base
        MongoDatabase db = client.getDatabase("other");
        // here create a new collection
        //db.createCollection("filestore");
        // insert data into the collection
        db.getCollection("filestore").insertOne(
                new Document()
                        .append("name", "smile")
                        .append("age", 20));
        // retrive the data
        FindIterable<Document> iterable = db.getCollection("filestore").find();
        // show data
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document);
            }
        });
    }
}
