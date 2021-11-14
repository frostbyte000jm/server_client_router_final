package com.staticFields;

public class SettingsForRouter {
    //static values
    private static int portNum = 5678;
    private static String serverIpAddress = "127.0.1.1";
    private static int serverPortNum = 5555;
    private static int threadMax = 3;

    //gets
    public static int getPortNum() { return portNum; }
    public static String getServerIpAddress() { return serverIpAddress; }
    public static int getServerPortNum() { return serverPortNum; }
    public static int getThreadMax() { return threadMax; }

}
