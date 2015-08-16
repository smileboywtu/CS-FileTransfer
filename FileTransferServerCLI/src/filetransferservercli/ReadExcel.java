/*
 *  create the class of read and write to excel
 */
package filetransferservercli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
public class ReadExcel {

    // member
    private File excelFile = null;
    private FileInputStream in = null;

    // constructor
    public ReadExcel(String file) throws IOException {
        // create the file
        excelFile = new File(file);
    }

    // get the colums
    public Object[] getExcelColums() throws FileNotFoundException, IOException {
        // here create the array
        ArrayList<String> title = new ArrayList<>();
        // open the excel
        in = new FileInputStream(excelFile);
        //Create Workbook instance holding reference to .xlsx file
        HSSFWorkbook workbook = new HSSFWorkbook(in);
        // only get the first sheet in the excel
        HSSFSheet sheet = workbook.getSheetAt(0);
        // get the first row
        Row row = sheet.getRow(0);
        // iterate the cell
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            // get the cell first
            Cell cell = cellIterator.next();
            // get the type
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    title.add(cell.getStringCellValue());
                    break;
                default:
                    break;
            }
        }

        // close the file
        in.close();

        // return this array
        return title.toArray();
    }

    // read data from excel
    public Object[] readList() throws FileNotFoundException, IOException {
        // flag to ignore the first row
        boolean ignoreFirstRow = true;
        // instance
        Vector<Object[]> rowArray = new Vector<>();
        Vector<Object> columsArray = null;
        // open the file
        in = new FileInputStream(excelFile);
        //Create Workbook instance holding reference to .xlsx file
        HSSFWorkbook workbook = new HSSFWorkbook(in);
        // only get the first sheet in the excel
        HSSFSheet sheet = workbook.getSheetAt(0);
        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            // ignore the first row
            if (ignoreFirstRow) {
                // set flag
                ignoreFirstRow = false;
                // continue
                continue;
            }
            // get the current row
            Row row = rowIterator.next();
            //For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            // new the colums array
            columsArray = new Vector<>();
            while (cellIterator.hasNext()) {
                // get the cell
                Cell cell = cellIterator.next();
                // get the type
                // save all to string
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING:
                        columsArray.add(cell.getStringCellValue());
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        columsArray.add((""+cell.getNumericCellValue()));
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        columsArray.add((""+cell.getBooleanCellValue()));
                        break;
                    default:
                        break;
                }
            }
            // add to the row 
            rowArray.add((Object[]) columsArray.toArray());
        }// end while

        // close the file
        in.close();

        return rowArray.toArray();
    }
}
