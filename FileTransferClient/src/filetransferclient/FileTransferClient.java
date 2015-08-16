/*
 *  Name: file transfer client
 *  Time: 2015/6/10    
 */
package filetransferclient;

import SerializePackage.DataPackage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import sun.security.pkcs11.wrapper.Constants;

/**
 *
 * @author smile
 */
public class FileTransferClient {

    // handle the server
    GetUserServer server = null;

    // handler for add entry to the file table
    private FileTransferTable tableModel = null;
    private JTable fileTransferTable = null;

    // excel read and write
    private SyncListIO excelBook = null;
    private FileAndDirectoryWatcher watchers = null;
    // file path vector
    private final Vector<Path> toTransferList = new Vector<>();

    // sync button
    private JButton syncButton = null;

    // JComponents
    // must initialize here, cause the other pane will use this
    private final JTextArea log = new JTextArea(3, 40);

    // status and progress
    private JLabel status = null;
    private JProgressBar process = null;

    // constructor
    public FileTransferClient() {
        // server is a dialog

    }

    // init the client
    public JPanel clientInit() throws IOException,
            URISyntaxException,
            BiffException,
            FileNotFoundException,
            WriteException {
        // create the excel book
        excelBook = new SyncListIO();
        // add the watcher
        watchers = new FileAndDirectoryWatcher();
        (new Thread(watchers)).start();
        // create and show GUI
        return GUI();
    }

    // private check if the file is exist
    private boolean isAlreadyExist(Path p) {
        // check the vector
        return toTransferList.contains(p);
    }

    // create UI
    private JPanel GUI() {
        // create the pane
        JPanel topLevelPane = new JPanel(new BorderLayout());

        // add file chooser
        JPanel fileChooserPane = new JPanel();
        fileChooserPane.setLayout(
                new BoxLayout(fileChooserPane, BoxLayout.LINE_AXIS));
        // create the file chooser
        JFileChooser fileChooser = new JFileChooser();
        // create the directory path
        JTextField fileDirectory = new JTextField("/home/smile");
        // create a image button for the file chooser
        JButton open = new JButton();
        open.setIcon(new ImageIcon(
                FileTransferClient.class.getResource("./image/open16.gif")));
        open.addActionListener((ActionEvent e) -> {
            // first check the directory
            String dir = fileDirectory.getText();
            // check
            File file = new File(dir);
            if (file.isDirectory()) {
                fileChooser.setCurrentDirectory(file);
            } else {
                // notify the user get an error directory
                log.append("the directory path isn't valid" + Constants.NEWLINE);
                log.setCaretPosition(log.getDocument().getLength());
            }
            // here deal with the open button action
            int returnVal = fileChooser.showDialog(open, "add");
            // check
            if (JFileChooser.APPROVE_OPTION == returnVal) {
                // here you should add the file name to the form
                File addFile = fileChooser.getSelectedFile();
                // just record the file path
                if (!isAlreadyExist(addFile.toPath())) {
                    // here add to the excel file
                    try {
                        excelBook.addItem(addFile.toPath(), false);
                    } catch (IOException | BiffException | WriteException ex) {
                        Logger.getLogger(FileTransferClient.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
            }
        });
        // add to the file chooser pane
        fileChooserPane.add(fileDirectory);
        fileChooserPane.add(open);
        // create a border for them
        fileChooserPane.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Add"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // add status form
        JPanel transferStatusPane = new JPanel();
        // create an instance
        tableModel = new FileTransferTable(log);
        tableModel.addTableModelListener(new TableModelChange());
        fileTransferTable = new JTable(tableModel);
        // set property
        fileTransferTable.setFillsViewportHeight(true);
        // set the colum length
        fileTransferTable.getColumnModel().getColumn(0).setPreferredWidth(1);
        fileTransferTable.getColumnModel().getColumn(2).setPreferredWidth(1);
        // set the render for file transfer status
        fileTransferTable.setDefaultRenderer(ImageIcon.class, new ImageLableRender());
        // add to a scroll pane
        JScrollPane tableScrollPane = new JScrollPane(fileTransferTable);
        tableScrollPane.setAutoscrolls(true);
        tableScrollPane.setWheelScrollingEnabled(true);
        // set prefer size
        tableScrollPane.setPreferredSize(new Dimension(
                tableScrollPane.getPreferredSize().width, 180));
        // add to the pane
        transferStatusPane.add(tableScrollPane);
        // add a title for them
        transferStatusPane.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("File Table"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // add control buttons pane
        JPanel buttonsControlPane = new JPanel();
        buttonsControlPane.setLayout(
                new BoxLayout(buttonsControlPane, BoxLayout.PAGE_AXIS));
        // create control
        // here just use one button for sync
        JPanel syncPane = new JPanel(new BorderLayout());
        syncButton = new JButton("Sync");
        syncButton.addActionListener(new SyncButtonAction());

        // add a setting button
        JButton socketSetting = new JButton();
        socketSetting.setIcon(new ImageIcon(
                FileTransferClient.class.getResource("./image/setting.png")));
        socketSetting.addActionListener((ActionEvent e) -> {
            // do something here
            if (!server.isVisible()) {
                server.pack();
                server.setVisible(true);
            }
            // check status
            if (server.isConneted()) {
                // show a message
                log.append("Server Conneted." + Constants.NEWLINE);
                status.setText("Connect Server: OPEN  ");
            } else {
                log.append("Server Connet Failed." + Constants.NEWLINE);
            }
        });
        // set the dialog
        server = new GetUserServer(socketSetting);
        server.createCustomDialog();

        // add to the sync pane
        syncPane.add(socketSetting, BorderLayout.LINE_START);
        syncPane.add(syncButton);
        // add a border for them
        syncPane.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Sync Control"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // create message pane
        log.setMargin(new Insets(5, 5, 5, 5));
        log.setEditable(false);
        log.setBackground(Color.lightGray);
        JScrollPane logScrollPane = new JScrollPane(log);
        logScrollPane.setAutoscrolls(true);
        logScrollPane.setWheelScrollingEnabled(true);
        // set the window size to use the JScrollPane
        buttonsControlPane.add(logScrollPane);
        logScrollPane.setPreferredSize(new Dimension(
                logScrollPane.getPreferredSize().width, 150));
        // add a border
        logScrollPane.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Console"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // here create a status and process bar
        JPanel processPane = new JPanel(new BorderLayout());
        status = new JLabel("Connect Server: CLOSE  ");
        status.setForeground(Color.darkGray);
        status.setFont(new Font(Font.SANS_SERIF, Font.ROMAN_BASELINE, 15));
        process = new JProgressBar();
        process.setMinimum(0);
        process.setMaximum(100);
        process.setValue(0);
        process.setStringPainted(true);
        processPane.add(status, BorderLayout.LINE_START);
        processPane.add(process);
        // here you need a border
        processPane.setBorder(new CompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // add to the control pane
        buttonsControlPane.add(syncPane);
        buttonsControlPane.add(logScrollPane);
        buttonsControlPane.add(processPane);

        // fill the pane
        topLevelPane.add(fileChooserPane, BorderLayout.PAGE_START);
        topLevelPane.add(transferStatusPane, BorderLayout.CENTER);
        topLevelPane.add(buttonsControlPane, BorderLayout.PAGE_END);

        // here create a border for top
        topLevelPane.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        // return the pane
        return topLevelPane;
    }

    // create the user interface 
    // this will use to show the user interface
    private static void createUserInterface() throws IOException,
            URISyntaxException,
            BiffException,
            FileNotFoundException,
            WriteException {
        // create a new frame
        JFrame appFrame = new JFrame("FileTransferTool");
        // set attributes
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add content pane to it
        FileTransferClient client = new FileTransferClient();
        appFrame.setContentPane(client.clientInit());

        // make it in the center of the screen
        appFrame.setLocationRelativeTo(null);
        int x = appFrame.getLocation().x - appFrame.getPreferredSize().width / 2;
        int y = appFrame.getLocation().y - appFrame.getPreferredSize().height / 2;
        appFrame.setLocation(x, y);

        // pack and show 
        appFrame.pack();
        appFrame.setVisible(true);
    }

    // table model listener
    private class TableModelChange implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            // here repaint the table
            fileTransferTable.invalidate();
            // repaint
            fileTransferTable.repaint();
        }
    }

    // sync button action perform
    private class SyncButtonAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // check the server first
            if (null != server && server.isConneted()) {
                try {
                    // anyway open the stream
                    server.reConnet();
                    // use the swing worker thread to do this
                    (new SyncFile(tableModel.getPathList())).execute();
                } catch (IOException ex) {
                    Logger.getLogger(FileTransferClient.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                // do not enable the button 
                // until the process done
                syncButton.setEnabled(false);
            } else {
                // test show the list check if it's ok
                for (String s : tableModel.getPathList()) {
                    System.out.println(s);
                }
                // notify the user
                log.append("set and connet to the server first!" + Constants.NEWLINE);
            }
        }

    }

    // transfer class used to transfer data
    private class SyncFile extends SwingWorker<Void, String> {

        // constants
        private final int OneTimeDataSize = 1024;  // this is 1K 

        // canstans used here
        private final java.net.URL waitStatus = FileTransferClient.class.getResource("./image/wrong.png");
        private final java.net.URL sentStatus = FileTransferClient.class.getResource("./image/right.png");

        // only one list member
        private Vector<String> list = null;
        // send the data
        private DataPackage dataSlice = null;

        // only constructor
        public SyncFile(String[] files) throws IOException {
            // new the instance
            list = new Vector<>();
            // add the item
            list.addAll(Arrays.asList(files));
        }

        // main work thread
        @Override
        protected Void doInBackground() throws Exception {
            // retrive each item
            // this is the path
            list.stream().forEach((_item) -> {
                sendFile(_item);
                // publish the file path when transfered done
                publish(_item);
            });
            // nothing to return here
            return null;
        }

        // update the label
        @Override
        protected void process(List<String> chunks) {
            // instance
            int _entryNumber = -1;
            // retrive each send file
            for (String s : chunks) {
                // search the table to find the entry
                _entryNumber = findTableRowNum(s);
                // set the value of status
                if (-1 == _entryNumber) {
                    // severse error happens
                    // do nothing here
                } else {
                    tableModel.setValueAt(new ImageIcon(sentStatus),
                            _entryNumber,
                            2);
                }
            }
        }

        @Override
        protected void done() {
            // log
            log.append("file sync done" + Constants.NEWLINE);
            // enable the sync button
            syncButton.setEnabled(true);
            // close the server
            try {
                // shut the stream
                server.closeServer();
                // flush
                server.flush();
            } catch (IOException ex) {
                log.append("failed to close the server service"+Constants.NEWLINE);
            }
        }

        // send the file
        private void sendFile(String file) {
            // make sure the file is exist
            if (null == file) {
                return;
            }
            // record the serilized object size
            int readSize = 0;
            long totalFileSize = 0;
            long sentFileSize = 0;
            String option = "merge";
            // file stream
            InputStream fileReader = null;
            byte[] buffer = null;
            Path path = Paths.get(file);
            // recorde the size
            totalFileSize = (new File(file)).length();
            try {
                // reset the progress bar
                process.setValue(0);
                // open the file
                fileReader = Files.newInputStream(path, StandardOpenOption.READ);
                while (fileReader.available() > 0) {
                    // new object every time send a package
                    dataSlice = new DataPackage();
                    // new buffer
                    buffer = new byte[OneTimeDataSize];
                    // read OneTimeDataSize data
                    readSize = fileReader.read(buffer, 0, OneTimeDataSize);
                    sentFileSize += readSize;
                    // set the properties of object
                    dataSlice.setData(buffer);
                    dataSlice.setFilePath(file);
                    dataSlice.setOption(option);
                    dataSlice.setFileLength(totalFileSize);
                    dataSlice.setFileName(path.getFileName().toString());
                    dataSlice.setSliceLength(readSize);
                    // send the package size first
                    server.serverIOWrite("start");
                    // send the slice
                    server.serverIOWrite(dataSlice);
                    // flush
                    server.flush();
                    // update the progress bar
                    process.setValue(Math.round(
                            (float)(sentFileSize)/totalFileSize*100));
                    // set option
                    if(option.equals("merge")){
                        option = "continue";
                    }
                    // discard the memory
                    buffer = null;
                }
            } catch (IOException ex) {
                // bake the package
                bakeTheSlice();
                // logger
                Logger.getLogger(FileTransferClient.class.getName()).log(
                        Level.SEVERE, null, ex);
            } finally {
                try {
                    if (null != fileReader) {
                        fileReader.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(FileTransferClient.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }

        // search the table and set the value
        private int findTableRowNum(String filePath) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                // compare and return
                if (tableModel.getValueAt(i, 3).equals(filePath)) {
                    return i;
                }
            }
            // return -1 default
            return -1;
        }

        // back the package if server is disconnected
        private void bakeTheSlice() {
            // do something here further

        }
    }

    // update Table
    private class UpdateTable extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            // here you may need to remove the vector fist and then add entrys
            tableModel.removeAll();
            // add the entry
            tableModel.addEntries(excelBook.readList());
            // return null
            return null;
        }

        @Override
        protected void done() {
            // here you need also update the vector
            // when initialize the method will be invoked
            // when list.xls changed the method will be invoked
            // only the table will check the repeat item
            // so just do not care about the .xls file 
            // only get the entry form the table
            toTransferList.setSize(0);
            toTransferList.removeAllElements();
            // add all data from table
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                toTransferList.add(
                        Paths.get((String) tableModel.getValueAt(i, 3)));
            }
        }
    }

    // add file and directory watch service
    // here you can either listent ot the .xl file or the file directory
    private class FileAndDirectoryWatcher implements Runnable {

        // watcher
        private WatchService watcher = null;
        private Path excelListDir = null;
        private Path fileSendDir = null;

        // allow read excel file
        private boolean isExcelLocked = false;
        private boolean allowUpdate = true;
        private boolean retry = false;

        // constructor
        public FileAndDirectoryWatcher() throws IOException, URISyntaxException {
            // set the send list path
            this.excelListDir = Paths.get(
                    FileTransferClient.class.getResource("./FileList/").getFile());
            // set the send file path
            this.fileSendDir = Paths.get(
                    FileTransferClient.class.getResource("./FileTransferDirectory/").getFile());
            // create the watcher
            watcher = FileSystems.getDefault().newWatchService();
            // register the events
            excelListDir.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            fileSendDir.register(watcher,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
        }

        @Override
        public void run() {
            while (true) {
                // always waits for the directory or file change
                // wait for key to be signaled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                key.pollEvents().stream().forEach((event) -> {
                    WatchEvent.Kind<?> kind = event.kind();

                    // if the OVERFLOW EVT just occurred, ignore it 
                    // then wait for other events
                    if (!(kind == StandardWatchEventKinds.OVERFLOW)) {
                        // The filename is the
                        // context of the event.
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        // check the parent directory
                        if (filename.toString().equals("list.xls")) {
                            try {
                                // just wait for the transfer list change
                                processSendListEvt(kind);
                            } catch (IOException ex) {
                                Logger.getLogger(FileTransferClient.class.getName()).log(
                                        Level.SEVERE, null, ex);
                            }
                        } else {
                            // wait for the send file directory change
                            processFileDirectoryEvt(kind, filename.toString());
                        }
                    }
                });

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();
                // allow another reverse
                allowUpdate = true;
                if (!valid) {
                    break;
                }
            }// end while
        }// end run

        // Process Event 
        private void processSendListEvt(WatchEvent.Kind<?> kind) throws IOException {
            if ((kind == StandardWatchEventKinds.ENTRY_CREATE)
                    || (kind == StandardWatchEventKinds.ENTRY_MODIFY)) {
                if (!isExcelLocked && allowUpdate) {
                    // update
                    (new UpdateTable()).execute();
                    allowUpdate = false;
                    retry = false;
                    // update table
                    log.append("list update."+Constants.NEWLINE);
                } else {
                    // set retry
                    retry = true;
                    log.append("excel file is locked. wait..." + Constants.NEWLINE);
                }
            } else {
                // the DELETE evt
                System.out.println("The sync excel has been deleted");
            }
        }

        // do not care about this 
        // may be just notify the user
        private void processFileDirectoryEvt(WatchEvent.Kind<?> kind, String name) {
            // this directory events will final notify the user
            // and let the user define which file to transfer
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                // add a item to the list
                log.append("new file '"
                        + name
                        + "' add to the transfer directory"
                        + Constants.NEWLINE);
                if (name.equals(".~lock.list.xls#")) {
                    isExcelLocked = true;
                }
            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                // remove a item in the list
                log.append("file '"
                        + name
                        + "' remove from the directory"
                        + Constants.NEWLINE);
                if (name.equals(".~lock.list.xls#")) {
                    isExcelLocked = false;
                }
                // deal with retry
                if (retry) {
                    (new UpdateTable()).execute();
                }
            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                // mark the item refresh
                log.append("file '"
                        + name
                        + "' modefied in the file transfer directory"
                        + Constants.NEWLINE);
            }
        }

    }

    // test the client
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createUserInterface();
                } catch (IOException | URISyntaxException | BiffException | WriteException ex) {
                    Logger.getLogger(FileTransferClient.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        });
    }
}