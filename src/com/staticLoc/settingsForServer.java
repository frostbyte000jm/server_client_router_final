package com.staticLoc;

import java.io.File;

public class settingsForServer {
    //static values
    private static int portNum = 5555;
    private static String home = System.getProperty("user.home");
    private static String tempFolder = home+ File.separator+ "Documents"+File.separator+"Temp"; //if this ever moves, update ServerThread to mkdirs
    private static int threadMax = 3;

    //gets
    public static int getPortNum() { return portNum; }
    public static String getTempFolder() { return tempFolder; }
    public static int getThreadMax() { return threadMax; }
}
