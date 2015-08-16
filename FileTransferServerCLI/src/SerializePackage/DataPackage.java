/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SerializePackage;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author smile
 */
// class for the package serilize
public class DataPackage implements Serializable {

    // sync member
    private String fileName = "";
    private String option =   "";
    private long fileLength = 0;
    private int sliceLength = 0;
    private byte[] data = null;

    // setter and getter
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }

    public void setSliceLength(int sliceLength) {
        this.sliceLength = sliceLength;
    }

    public int getSliceLength() {
        return sliceLength;
    }

    public void setFileLength(long len) {
        fileLength = len;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
    
    // test the array
    public void printByteArray(){
        System.out.println(Arrays.toString(data));
    }
}

