package com.classes;

import java.io.Serializable;

public class MachineContainer implements Serializable {
    //declaration
    private String localHostName, localIPAddress, externalIPAddress, userName;
    private int portNum;

    public MachineContainer() {
        this.localHostName = "";
        this.localIPAddress = "";
        this.externalIPAddress = "";
        this.userName = "";
        this.portNum = 0;
    }

    public String getMachineInfo(){
        return localHostName+"|"+localIPAddress+"|"+externalIPAddress+"|"+userName+"|"+portNum;
    }

    //get
    public String getLocalHostName(){ return localHostName; }
    public String getLocalIPAddress(){ return localIPAddress; }
    public String getExternalIPAddress(){ return externalIPAddress; }
    public String getUserName(){ return userName; }
    public int getPortNum(){ return portNum; }

    //set
    public void setLocalHostName(String localHostName){this.localHostName = localHostName;}
    public void setLocalIPAddress(String localIPAddress){this.localIPAddress = localIPAddress;}
    public void setExternalIPAddress(String externalIPAddress) {this.externalIPAddress = externalIPAddress;}
    public void setUserName(String userName){ this.userName = userName; }
    public void setPortNum(int portNum){ this.portNum = portNum; }


}
/*private String localIPAddress, externalIPAddress, localHostName, userName;
    private int portNum;

    public MachineContainer(String localIPAddress, String externalIPAddress, String localHostName) {
        this.externalIPAddress = externalIPAddress;
        this.localHostName = localHostName;
        this.localIPAddress = localIPAddress;
        this.userName = "";
        this.portNum = 0;
    }

    public String getLocalIPAddress(){ return localIPAddress; }
    public String getLocalHostName(){ return localHostName; }
    public String getExternalIPAddress(){ return externalIPAddress; }
    public String getUserName(){ return userName; }
    public int getPortNum(){ return portNum; }

    public void setUserName(String userName){ this.userName = userName; }
    public void setPortNum(int portNum){ this.portNum = portNum; }*/