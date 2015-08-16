/*
 *  this package used to save add or retrive data from the mogodb database
 * this really the first time i use a database to save the data
 */
package filetransferservercli;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import sun.security.pkcs11.wrapper.Constants;

/**
 *
 * @author smile
 */
public class UsingMongoSavaFile implements Runnable {

    // member for saving the database user
    // the user only need to create once on the top level
    // ignore the multi-thread read/write the data base
    private MongoClient user = null;
    // create new GridFS
    private GridFS model = null;
    private MongoDatabase db = null;
    private MongoCollection defaultCollection = null;
    // default dir for this user
    private String syncDirectory = null;

    // excel list
    private Object[] titles = null;
    private Object[] values = null;
    private ReadExcel excelReader = null;

    // constructor
    public UsingMongoSavaFile(MongoClient user, String dir) {
        // record the user
        this.user = user;
        // set the sync directory
        syncDirectory = dir;
    }

    private String findExcelFile() throws IOException {
        // use the matcher to find the file
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
                "glob:*.{xls,xlsx}");
        // find the return
        Path dir = Paths.get(syncDirectory);
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path p : stream) {
            // here match the file name
            // not the path
            Path file = p.getFileName();
            // match
            if (matcher.matches(file)) {
                return p.toString();
            }
        }
        // return
        return null;
    }

    @Override
    public void run() {
        // init the messge
        System.out.println(Constants.NEWLINE
                + "start to save file in Mongo DataBase"
                + Constants.NEWLINE);
        try {
            // read the excel file
            String excel = findExcelFile();
            // if find
            if (null != excel) {
                // open the excel file
                excelReader = new ReadExcel(excel);
                // retrive the data
                values = excelReader.readList();
                titles = excelReader.getExcelColums();
                // save data to the database
                for (Object aValue : values) {
                    savePersonInformation((Object[]) aValue);
                }
            }
            // after done 
            System.out.println("data have saved to the data base"
                    + Constants.NEWLINE);

        } catch (IOException ex) {
            Logger.getLogger(UsingMongoSavaFile.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        // delete all the content
        excelReader = null;
        // close all the file
        values = null;
        titles = null;
        // remove the directory
        File dir = new File(syncDirectory);
        // delete all the file
        File[] files = dir.listFiles();
        // delete all the content
        if (files != null && files.length > 0) {
            for (File aFile : files) {
                aFile.delete();
            }
        }
        // delete dir
        dir.delete();
        // notify the user
        System.out.println("temp directory removed" + Constants.NEWLINE);
    }

    // save a person's information
    private void savePersonInformation(Object[] person) throws IOException {
        // here set the collection name
        String collectionName = (String) person[0];
        // here use the name as the collection name
        db = user.getDatabase("PersonInformation");
        // if exist, just return 
        if (null != db.getCollection(collectionName)) {
            System.out.println(collectionName
                    + " already exist, do not create again"
                    + Constants.NEWLINE);
            return;
        }
        // not exist just create
        defaultCollection = db.getCollection(collectionName);
        model = new GridFS(user.getDB("PersonInformation"), collectionName);
        // save the info
        // first create the document
        Document doc = new Document();
        for (int i = 0; i < person.length; i++) {
            // get the data
            String item = (String) person[i];
            // condition to justify wether a file or a value
            if (item.contains(".")) {
                // create a file
                File file = new File(syncDirectory + item);
                if (file.exists()) {
                    // save to the collection
                    GridFSInputFile in = model.createFile(file);
                    // set the file name
                    in.setFilename(item);
                    // close
                    in.save();
                } else {
                    // append
                    doc.append((String) titles[i], item);
                }
            }
        }
        // insert into the collection
        defaultCollection.insertOne(doc);
    }

    // retrive the file
    public OutputStream retriveDataBase(String filename) throws IOException {
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
