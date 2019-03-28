package com.downloaddemo.bean;

import java.io.Serializable;

public class FileBean implements Serializable {
    private int id;
    private String url;
    private String fileName;
    private int length;
    private int loaded;

    public FileBean() {

    }

    public FileBean(int id, String url, String fileName, int length, int loaded) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.length = length;
        this.loaded = loaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLoaded() {
        return loaded;
    }

    public void setLoaded(int loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                ", length=" + length +
                ", loaded=" + loaded +
                '}';
    }
}
