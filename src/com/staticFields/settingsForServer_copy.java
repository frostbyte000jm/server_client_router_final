package com.staticFields;

import java.io.File;

public class settingsForServer_copy {
    //static values
    private static int portNum = 5555;
    private static String tempFolder = "C:"+File.separator+"ServerTemp"; //if this ever moves, update ServerThread to mkdirs
    private static int threadMax = 3;

    //gets
    public static int getPortNum() { return portNum; }
    public static String getTempFolder() { return tempFolder; }
    public static int getThreadMax() { return threadMax; }
}
