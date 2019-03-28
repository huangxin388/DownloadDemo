package com.downloaddemo.bean;

public class ThreadBean {
    private int id;
    private String url;
    private int start;
    private int end;
    private int loaded;

    public ThreadBean() {

    }

    public ThreadBean(int id, String url, int start, int end, int loaded) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
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

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getLoaded() {
        return loaded;
    }

    public void setLoaded(int loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return "ThreadBean{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", loaded=" + loaded +
                '}';
    }
}
