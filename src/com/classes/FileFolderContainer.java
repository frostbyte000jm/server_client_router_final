package com.classes;

import java.util.ArrayList;

public class FileFolderContainer {
    public MachineContainer machineContainer;
    public String serverName;
    public boolean isFolder;
    public String location;
    public String fileFolderName;

    public FileFolderContainer(){
        machineContainer = new MachineContainer();
    }

    public String getFileFolderNameDisplay() {
        String fileFolderLabel = isFolder ? "Folder" : "File";
        return "["+serverName+"] - ["+fileFolderLabel+"]: "+fileFolderName;
    }

    public String getFileFolderInfo() {
        String strMachine = machineContainer.getMachineInfo();
        String strIsFolder = isFolder ? "1":"0";
        return strMachine+"*"+serverName+"*"+strIsFolder+"*"+location+"*"+fileFolderName;
    }

    public void setFileFolderInfo(String fileFolderInfo){
        String[] strSplit = fileFolderInfo.split("\\*");
        machineContainer.setMachineInfo(strSplit[0]);
        serverName = strSplit[1];
        isFolder = strSplit[2].equals("1");
        location = strSplit[3];
        fileFolderName = strSplit[4];
    }

    /***************************************************
     *             External Use Only
     ***************************************************/
    public String stringifyContainer(ArrayList<FileFolderContainer> arrFileFolders) {
        //declarations
        boolean doFirst = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arrFileFolders.size(); i++){
            if (doFirst){
                stringBuilder.append(arrFileFolders.get(i).getFileFolderInfo());
                doFirst = false;
            } else {
                stringBuilder.append(":").append(arrFileFolders.get(i).getFileFolderInfo());
            }
        }
        return stringBuilder.toString();
    }

    public ArrayList<FileFolderContainer> destringifyContainer(String strFileFolder){
        ArrayList<FileFolderContainer> arrFileFolders = new ArrayList<FileFolderContainer>();
        String[] arrStrFileFolders = strFileFolder.split(":");
        for (int i = 0; i < arrStrFileFolders.length; i++){
            FileFolderContainer flc = new FileFolderContainer();
            flc.setFileFolderInfo(arrStrFileFolders[i]);
            arrFileFolders.add(flc);
        }
        return arrFileFolders;
    }
}
