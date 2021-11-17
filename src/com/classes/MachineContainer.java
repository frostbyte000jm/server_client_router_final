package com.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class MachineContainer implements Serializable {
    //declaration
    private String localHostName, localIPAddress, externalIPAddress, userName;
    private int portNum, portNumToCall, isServer, isRouter;
    private String serverIPAddress;

    public MachineContainer() {
        this.localHostName = "_";       //Name of machine
        this.localIPAddress = "_";      //IP Addy of machine
        this.externalIPAddress = "_";   //External IP addy of machine
        this.userName = "_";            //Username of person accessing server
        this.portNum = 0;               //Port Number Machine is listening on.
        this.portNumToCall = 0;         //for Router to call Server
        this.isServer = 0;              //is machine a server?
        this.isRouter = 0;              //is machine a router?
        this.serverIPAddress = "_";     //What is the IP address for a Router to call
    }

    public String getMachineInfo(){
        return localHostName+"|"+localIPAddress+"|"+externalIPAddress+"|"+userName+"|"+portNum+"|"+portNumToCall+"|"+isServer+"|"+isRouter+"|"+serverIPAddress+"|";
    }

    //for external use only
    public ArrayList<MachineContainer> getMachineList(String machines){
        ArrayList<MachineContainer> arrMachines = new ArrayList<MachineContainer>();
        String[] arrMachineInfo = machines.split(":");

        for (int i = 0; i < arrMachineInfo.length; i++){
            MachineContainer machineContainer = new MachineContainer();
            machineContainer.setMachineInfo(arrMachineInfo[i]);
            arrMachines.add(machineContainer);
        }
        return arrMachines;
    }

    public void setMachineInfo(String machineInfo){
        String[] info = machineInfo.split("\\|");
        /*System.out.println("Machine Container: ");

        for (int i = 0; i < info.length; i++){
            System.out.println("Info["+i+"]: "+info[i]);
        }*/

        this.localHostName = info[0];
        this.localIPAddress = info[1];
        this.externalIPAddress = info[2];
        this.userName = info[3];
        this.portNum = Integer.parseInt(info[4]);
        this.portNumToCall = Integer.parseInt(info[5]);
        this.isServer = Integer.parseInt(info[6]);
        this.isRouter = Integer.parseInt(info[7]);
        this.serverIPAddress = info[8];
    }

    //get
    public String getLocalHostName(){ return localHostName; }
    public String getLocalIPAddress(){ return localIPAddress; }
    public String getExternalIPAddress(){ return externalIPAddress; }
    public String getUserName(){ return userName; }
    public int getPortNum(){ return portNum; }
    public int getPortNumToCall(){ return portNumToCall; }
    public int getIsServer(){ return isServer; }
    public int getIsRouter(){ return isRouter; }
    public String getServerIPAddress(){ return serverIPAddress; }

    //set
    public void setLocalHostName(String localHostName){this.localHostName = localHostName;}
    public void setLocalIPAddress(String localIPAddress){this.localIPAddress = localIPAddress;}
    public void setExternalIPAddress(String externalIPAddress) {this.externalIPAddress = externalIPAddress;}
    public void setUserName(String userName){ this.userName = userName; }
    public void setPortNum(int portNum){ this.portNum = portNum; }
    public void setPortNumToCall(int portNumToCall){ this.portNumToCall = portNumToCall; }
    public void setIsServer(int isServer){ this.isServer = isServer; }
    public void setIsRouter(int isRouter){ this.isRouter = isRouter; }
    public void setServerIPAddress(String serverIPAddress){ this.serverIPAddress = serverIPAddress; }

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