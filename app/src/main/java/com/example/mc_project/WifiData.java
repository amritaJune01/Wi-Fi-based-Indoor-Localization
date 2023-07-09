package com.example.mc_project;

import java.io.Serializable;

public class WifiData implements Comparable<WifiData>, Serializable {
    private String ssid;
    private String bssid;
    private double distance;

    String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String mac;

    private int frequency;

    private String routerLoc;

    public String getRouterLoc() {
        return routerLoc;
    }

    public void setRouterLoc(String routerLoc) {
        this.routerLoc = routerLoc;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    private Integer[] level;

    private int i = 0;

    public WifiData(String ssid, String bssid, double distance, int frequency) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.distance = distance;
//        this.mac = mac;
        this.frequency = frequency;
        level = new Integer[5];
        location = "";
    }

    public WifiData(String ssid, String bssid, double distance) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.distance = distance;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }



    @Override
    public int compareTo(WifiData o) {
        return (int) Math.floor(distance - o.distance );
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Integer[] getLevel() {
        return level;
    }

    public void setLevel(Integer[] level) {
        this.level = level;
    }
}
