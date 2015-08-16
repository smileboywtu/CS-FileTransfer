/*
 * this class is different from the POI class to read the excel file
 * this class can read small file
 */
package filetransferclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Vector;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 *
 * @author smile
 */
public class SyncListIO {

    // member
    private int rows = 0;
    private String filePath = null;
    private String fileDir = null;
    private File excelFile = null;

    // constructor
    public SyncListIO() throws IOException, 
            BiffException, 
            FileNotFoundException, 
            WriteException {
        // set the file path
        filePath = FileTransferClient.class.getResource(
                "./FileList/list.xls").getFile();
        filePath = "./src/filetransferclient/FileList/list.xls";
        // set the directory
        fileDir = FileTransferClient.class.getResource(
                "./FileTransferDirectory/").getFile();
        fileDir = "./src/filetransferclient/FileTransferDirectory/";
        // create the file
        excelFile = new File(filePath);
        // if not exist, then create
        if (excelFile.exists()) {
            // nothing to do
            System.out.println("file find");
        } else {
            // create the file
            excelFile.createNewFile();
            // create a new file
            System.out.println("create new file");
        }
        // load the default list
        loadDefaultList();
    }

    // get file transfer directory
    public Path getFileTransferDirectory() {
        if ((new File(fileDir)).isDirectory()) {
            return Paths.get(fileDir);
        }
        // return null
        return null;
    }

    // load the default list of the file directory
    private void loadDefaultList() throws BiffException,
            FileNotFoundException,
            IOException,
            WriteException {
        // iterator the file in the directory
        Path dir = Paths.get(fileDir);
        // create a file directory stream to iterat the content
        try (DirectoryStream<Path> readDirStream = Files.newDirectoryStream(dir)) {
            boolean onlyFirstTurn = true;
            // for each loop to iterate
            for (Path afile : readDirStream) {
                addItem(afile, onlyFirstTurn);
                onlyFirstTurn = false;
            }
        }
        // because you have use the try method
        // here you need not to close the stream
    }

    // read data from excel
    public Object[] readList() throws FileNotFoundException, IOException, BiffException {
        // instance
        Vector<String[]> nameList = new Vector<>();

        //Create Workbook instance holding reference to .xlsx file
        Workbook workbook = Workbook.getWorkbook(excelFile);
        // only get the first sheet in the excel
        Sheet sheet = workbook.getSheet(0);

        // read all exist rows
        for (int i = 0; i < sheet.getRows(); i++) {
            // row string array
            String[] str = new String[2];
            // read two cells
            Cell cell = sheet.getCell(0, i);
            str[0] = cell.getContents();
            cell = sheet.getCell(1, i);
            str[1] = cell.getContents();
            // add to the vector
            nameList.add(str);
        }// end while

        // write to the list
        // close the file
        workbook.close();

        return nameList.toArray();
    }

    public void addItem(Path file, boolean newSheet) throws FileNotFoundException,
            IOException,
            BiffException,
            WriteException {
        //Get the existing excel file
        Workbook workbook = Workbook.getWorkbook(excelFile);
        // Open a copy of the excel file,and specify the parameters
        // to write the data back to the original file.
        WritableWorkbook writeBook = Workbook.createWorkbook(excelFile, workbook);
        // writable sheet
        WritableSheet sheet = null;
        // new or append
        if (newSheet) {
            // remove exist
            writeBook.removeSheet(0);
            // create new sheet
            sheet = writeBook.createSheet("default sheet", 0);
            // reset row number
            rows = 0;
        } else {
            // get the sheet of zero
            sheet = writeBook.getSheet(0);
            // update rows
            rows = sheet.getRows();
        }
        // add the name and the path
        Label lable0 = new Label(0, rows, file.getFileName().toString());
        sheet.addCell(lable0);
        Label lable1 = new Label(1, rows, file.toString());
        sheet.addCell(lable1);

        //Write the workbook in file system
        writeBook.write();
        // close the file
        writeBook.close();
    }

    // write data to the sheet
    public void writeList(String[] data) throws FileNotFoundException, 
            IOException, 
            WriteException {
        //Blank workbook
        WritableWorkbook workbook = Workbook.createWorkbook(excelFile);
        //Create a blank sheet
        WritableSheet sheet = workbook.createSheet("defaut sheet", 0);
        //Iterate over data and write to sheet
        int rownum = 0;
        for (String str : data) {
            // create a colum cell
            Label label0 = new Label(0, rownum, Paths.get(str).getFileName().toString());
            sheet.addCell(label0);
            Label label1 = new Label(1, rownum, str);
            sheet.addCell(label1);
        }
        //Write the workbook in file system
        workbook.write();
        // close the file
        workbook.close();
    }

    // here just test the api here
    // test ok
    public static void main(String[] args) throws IOException, 
            BiffException, 
            FileNotFoundException, 
            WriteException {
        // test
        SyncListIO test = new SyncListIO();
        
        // test write to the file
        // here just create the string array
//        String[] strs = {"readme.txt"};
//        test.writeList(strs);
        
        // then test the read 
        Object[] strObj = test.readList();
        for(Object a: strObj){
            System.out.println(Arrays.toString((String[])a));
        }
    }
}
