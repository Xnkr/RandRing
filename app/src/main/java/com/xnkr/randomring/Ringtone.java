package com.xnkr.randomring;

import java.io.Serializable;

public class Ringtone implements Serializable{
    private String fileName;
    private String filePath;
    private long ringLength;

    public Ringtone(String fileName, String filePath, long ringLength) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.ringLength = ringLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getRingLength() {
        return ringLength;
    }

    public void setRingLength(long ringLength) {
        this.ringLength = ringLength;
    }

    @Override
    public String toString() {
        return String.format("%s:%s ms at %s",fileName,ringLength,filePath);
    }
}
