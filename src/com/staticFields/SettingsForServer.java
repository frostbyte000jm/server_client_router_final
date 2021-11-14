package com.staticFields;

import java.io.File;

public class SettingsForServer {
    //static values Server
    private int portNum = 5556;
    private String serverName = "Server01";
    private final String home = System.getProperty("user.home");
    private String folder = home+File.separator+File.separator+"Documents"+
            File.separator+"Temp"+File.separator+"server01";
    private final int threadMax = 10;

    //static values ServerRouter (Watchtower)
    private int portNumServerRouter = 5555;
    private String serverRouterIPAddress = "127.0.1.1";

    //gets server
    public int getPortNum() { return portNum; }
    public String getServerName() { return serverName; }
    public String getFolder() { return folder; }
    public int getThreadMax() { return threadMax; }

    //gets serverRouter
    public int getPortNumServerRouter() { return portNumServerRouter; }
    public String getServerRouterIPAddress() { return serverRouterIPAddress; }

    //sets server
    public void setPortNum(int portNum) { this.portNum = portNum; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    public void setFolder(String folder) { this.folder = folder; }

    //sets serverRouter
    public void setPortNumServerRouter(int portNumServerRouter) { this.portNumServerRouter = portNumServerRouter; }
    public void setServerRouterIPAddress(String serverRouterIPAddress) { this.serverRouterIPAddress = serverRouterIPAddress; }
}
