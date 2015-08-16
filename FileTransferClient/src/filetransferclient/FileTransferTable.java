/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransferclient;

import java.awt.Component;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import sun.security.pkcs11.wrapper.Constants;

/**
 *
 * @author smile
 */
public class FileTransferTable extends AbstractTableModel {

    // fixed 
    private final String[] columNameList = {
        "Transfer",
        "File", // not editable
        "Status" // not editable
    };
    
    // message pane
    private JTextArea log = null;
    // the entry use vector because it's not fixed
    // the user can add to the table use the 
    private final Vector<TableEntry> entryList = new Vector<>();
    
    // for test
    public FileTransferTable(JTextArea message){
        // only add the message
        log = message;
        // add some entry
    }

    @Override
    public int getRowCount() {
        return entryList.size();
    }

    @Override
    public int getColumnCount() {
        return columNameList.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // this is very interesting thing 
        Object o = null;
        switch(columnIndex){
            case 0:
                o = entryList.elementAt(rowIndex).getTransfer();
                break;
            case 1:
                o = entryList.elementAt(rowIndex).getKey();
                break;
            case 2:
                o = entryList.elementAt(rowIndex).getStatus();
                break;
            case 3:
                o = entryList.elementAt(rowIndex).getPath();
                break;
            default:
                    break;
        }
        // return 
        return o;
    }

    @Override
    public String getColumnName(int column) {
        return columNameList[column];
    }

    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // only transfer is editable
        return 0 == columnIndex;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//        System.out.println("row: " + rowIndex + " " + "colum: " + columnIndex);
//        System.out.println("value is: "+aValue);
        // set the value and fire the event
        switch(columnIndex){
            case 0:
                entryList.elementAt(rowIndex).setTransfer((boolean)aValue);
                break;
            case 1:
                entryList.elementAt(rowIndex).setFile((String)aValue);
                break;
            case 2:
                entryList.elementAt(rowIndex).setImageIcon((ImageIcon)aValue);
                break;
            default:
                break;
        }
        // fire
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    // let the user able to add new entry to the table use API
    public void addEntry(TableEntry entry) {
        boolean exist = false;
        // check first, do not add the same entry once again
        for(TableEntry t: entryList){
            if(t.isSameKey(entry)){
                exist = true;
                break;
            }
        }
        // condition running
        if(!exist){
            // first add the entry
            entryList.add(entry);
            // log
            log.append("add " + entry.getKey() + Constants.NEWLINE);
            // always show the new line
            log.setCaretPosition(log.getDocument().getLength());
            // then fire the event
            fireTableRowsInserted(getRowCount(), getRowCount() + 1);
        }
    }
    
    // add many entries
    public void addEntries(Object[] obj){
        // instance
        String[] str = null;
        // reference
        TableEntry t = null;
        for(Object a: obj){
            // convert
            str = (String[])a;
            // create new entry
            t = new TableEntry(str[0], str[1]);
            // add
            addEntry(t);
        }
    }
    
    // get the file path list
    public String[] getPathList(){
        // counter
        int i = 0;
        // create the string list
        String[] list = new String[entryList.size()];
        
        // add the data to it
        for(TableEntry entry: entryList){
            if(entry.getTransfer()){
                list[i++] = entry.getPath();
            }
        }
        
        // return the array of path string
        String[] list1 = new String[i];
        // copy array
        System.arraycopy(list, 0, list1, 0, i);
        // reset
        list = null;
        // return 
        return list1;
    }
    
    // remove all the elements
    public void removeAll(){
        entryList.removeAllElements();
    }
    
    // test the class
    // this class test ok
    public static void main(String[] args) {
        // test
        JFrame test = new JFrame("Test Table");
        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // add the content
        FileTransferTable m = new FileTransferTable(null);
        m.addEntry(new TableEntry("test1", "1"));
        m.addEntry(new TableEntry("test2", "2"));
        m.addEntry(new TableEntry("test3", "3"));
        
        JTable t = new JTable(m);
        test.add(t);
        // pack and show
        test.pack();
        test.setVisible(true);
    }
}

// render for the 
class ImageLableRender extends JLabel
        implements TableCellRenderer {
    
    public ImageLableRender(){
        // set the property here
        // center the lable
        setHorizontalAlignment(CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        // get the value first
        ImageIcon status = (ImageIcon) value;

        // set the image according to the value
        setIcon(status);
        // here set a tooltip for the user
        setToolTipText("Green refer to the sync file");
        // return 
        return this;
    }

}

// here you need to create a data structure to store the entry data
class TableEntry {

    // canstans used here
    private final java.net.URL waitStatus = FileTransferClient.class.getResource("./image/wrong.png");
    private final java.net.URL sentStatus = FileTransferClient.class.getResource("./image/right.png");

    // member 
    private boolean transfer = false;
    private String file = "";
    private ImageIcon status = null;
    private String filePath = "";

    // object array
    private final Object[] objectArray;

    public TableEntry(String name, String dir) {
        // here just record the name
        file = name;
        transfer = false;
        status = new ImageIcon(waitStatus);
        filePath =  dir;
        // set the array
        this.objectArray = new Object[]{transfer, file, status, filePath};
    }
    
    // if transfer this file
    public boolean getTransfer(){
        return transfer;
    }
    
    // the file name is the key
    public String getKey(){
        return file;
    }
    
    // get the path
    public String getPath(){
        return filePath;
    }
    
    public ImageIcon getStatus(){
        return status;
    }
    
    // setter
    public void setTransfer(boolean b){
        transfer = b;
    }
    
    public void setFile(String s){
        file = s;
    }
    
    public void setImageIcon(ImageIcon i){
        status = i;
    }
    
    // use for refuse the same entry in the table
    public boolean isSameKey(TableEntry another){
        return this.file.equals(another.getKey());
    }
    
    // to objects array
    public Object[] toObjectArray() {
        return objectArray;
    }
}
