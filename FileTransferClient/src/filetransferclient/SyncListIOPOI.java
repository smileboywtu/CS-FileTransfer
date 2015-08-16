/*
 *  create the class of read and write to excel
 */
package filetransferclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author smile
 */
public class SyncListIOPOI {
    // member
    private int rows = 0;
    private String filePath = null;
    private String fileDir = null;
    private File excelFile = null;
    private FileInputStream in = null;
    private FileOutputStream out = null;

    // constructor
    public SyncListIOPOI() throws IOException {
        // set the file path
        filePath = "./src/filetransferclient/FileList/list.xls";
        // set the directory
        fileDir = "./src/filetransferclient/FileTransferDirectory/";
        // create the file
        excelFile = new File(filePath);
        // if not exist, then create
        if(excelFile.exists()){
            // nothing to do
            System.out.println("file find");
        }else{
            // create the file
            excelFile.createNewFile();
            // create a new file
            System.out.println("create new file");
        }
        // load the default list
        loadDefaultList();
    }
    
    // get file transfer directory
    public Path getFileTransferDirectory(){
        if((new File(fileDir)).isDirectory()){
            return Paths.get(fileDir);
        }
        // return null
        return null;
    }
    
    // load the default list of the file directory
    private void loadDefaultList() throws IOException{
        //Create a blank sheet
        in = new FileInputStream(excelFile);
        //Create Workbook instance holding reference to .xlsx file
        HSSFWorkbook workbook = new HSSFWorkbook(in);
        // iterator the file in the directory
        Path dir = Paths.get(fileDir);
        // create a file directory stream to iterat the content
        try (DirectoryStream<Path> readDirStream = Files.newDirectoryStream(dir)) {
            boolean onlyFirstTurn = true;
            // for each loop to iterate
            for(Path afile: readDirStream){
                addItem(afile, onlyFirstTurn);
                onlyFirstTurn = false;
            }
        }
        // because you have use the try method
        // here you need not to close the stream
    }
    
    // read data from excel
    public Object[] readList() throws FileNotFoundException, IOException {
        // instance
        Vector<String[]> nameList = new Vector<>();
        
        // open the file
        in = new FileInputStream(excelFile);
        //Create Workbook instance holding reference to .xlsx file
        HSSFWorkbook workbook = new HSSFWorkbook(in);
        // only get the first sheet in the excel
        HSSFSheet sheet = workbook.getSheetAt(0);       
        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            // row string array
            String[] str = new String[2];
            // row
            Row row = rowIterator.next();
            //For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            // read two cells
            Cell cell = cellIterator.next();
            str[0] = cell.getStringCellValue();
            cell = cellIterator.next();
            str[1] = cell.getStringCellValue();
            // add to the vector
            nameList.add(str);
        }// end while
        
        // write to the list
        // close the file
        in.close();
        
        return nameList.toArray();
    }

    public void addItem(Path file, boolean newSheet)
            throws FileNotFoundException, IOException {
        // Create a blank sheet
        in = new FileInputStream(excelFile);
        // Create Workbook instance holding reference to .xlsx file
        HSSFWorkbook workbook = new HSSFWorkbook(in);
        // create sheet
        HSSFSheet sheet = null;
        // new or append
        if (newSheet) {
            // remove exist
            workbook.removeSheetAt(0);
            // create new
            sheet = workbook.createSheet("Default Sheet");
            // reset row number
            rows = 0;
        } else {
            // get the sheet of zero
            sheet = workbook.getSheetAt(0);
            // update rows
            rows = sheet.getPhysicalNumberOfRows();
        }
        
        // create a new row
        Row row = sheet.createRow(rows++);
        // add the name
        Cell cell = row.createCell(0);
        cell.setCellValue((String)file.getFileName().toString());
        // add the path
        cell = row.createCell(1);
        cell.setCellValue((String)file.toString());
        
        //Write the workbook in file system
        out = new FileOutputStream(excelFile);
        workbook.write(out);
        // close the file
        out.close();
    }

    // write data to the sheet
    public void writeList(String[] data) throws FileNotFoundException, IOException {
        //Blank workbook
        HSSFWorkbook workbook = new HSSFWorkbook();
        //Create a blank sheet
        HSSFSheet sheet = workbook.createSheet("Default Sheet");

        //Iterate over data and write to sheet
        int rownum = 0;
        for (String str : data) {
            // create a new row
            Row row = sheet.createRow(rownum++);
            // create a colum cell
            Cell cell = row.createCell(0);
            cell.setCellValue((String)str);
        }
        
        //Write the workbook in file system
        out = new FileOutputStream(excelFile);
        workbook.write(out);
        // close the file
        out.close();
    }

    // here just test the api here
    // test ok
    public static void main(String[] args) throws IOException {
        // first write to the xlsx file
        SyncListIOPOI test = new SyncListIOPOI();
        
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

