package com.staticFields;

import java.io.File;

public class SettingsForRouter {
    //static values Server
    private int portNum = 5678;
    private String routerName = "Router01";
    private String serverIPAddress = "127.0.1.1";
    private int serverPortNum = 5556;
    private final int threadMax = 10;

    //gets server
    public int getPortNum() { return portNum; }
    public String getRouterName() { return routerName; }
    public String getServerIPAddress() { return serverIPAddress; }
    public int getServerPortNum() { return serverPortNum; }
    public int getThreadMax() { return threadMax; }

    //sets server
    public void setPortNum(int portNum) { this.portNum = portNum; }
    public void setRouterName(String routerName) { this.routerName = routerName; }
    public void setServerIPAddress(String serverIPAddress) { this.serverIPAddress = serverIPAddress; }
    public void setServerPortNum(int serverPortNum) { this.serverPortNum = serverPortNum; }
}
