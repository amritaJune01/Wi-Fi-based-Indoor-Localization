package com.example.mc_project;

public class   ListContents {
    private String txt, name, loc;
    private long timestamp;

    private String email;

    public ListContents() {}
    public ListContents(String txt, String name, String loc, long timestamp, String email) {
        this.txt = txt;
        this.name = name;
        this.loc = loc;
        this.timestamp = timestamp;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
