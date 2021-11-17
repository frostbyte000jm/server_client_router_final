package com.Main;

import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;
import com.threads.RouterThread;
import com.threads.ServerRouterThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServerRouter {
    //declarations
    private ArrayList<MachineContainer> arrServerContainer, arrChatContainer;
    private MachineContainer machineContainer;
    private String machineInfo;

    public static void main (String[] args) throws IOException {
        // Set static Port Number
        int portNum = 5555;

        //Who am I?
        ProcessComputerInfo processComputerInfo = new ProcessComputerInfo();
        String message = processComputerInfo.getWhoAmI();
        System.out.println(message);

        //set a container with this info
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setLocalHostName(processComputerInfo.getLocalHostName());
        machineContainer.setLocalIPAddress(processComputerInfo.getLocalIPAddress());
        machineContainer.setExternalIPAddress(processComputerInfo.getExternalIP());
        machineContainer.setPortNum(portNum);

        // wait for call
        new TCPServerRouter().waitForConnection(machineContainer);
    }
    private void waitForConnection(MachineContainer machineContainer) throws IOException {
        //declarations
        this.arrServerContainer = new ArrayList<MachineContainer>();
        this.arrChatContainer = new ArrayList<>();
        this.machineContainer = machineContainer;
        int portNum = machineContainer.getPortNum();
        machineInfo = machineContainer.getMachineInfo();
        ServerSocket serverSocket = null;
        Socket socket = null;
        boolean doRun = true;

        while(doRun) {
            //set up ServerSocket and wait on port ????
            try {
                serverSocket = new ServerSocket(portNum);
                System.out.println("Router: " + machineContainer.getLocalHostName() + " is listening on port: " + portNum);
            } catch (IOException e) {
                System.err.println("Could not listen on port: " + portNum + ".");
                System.exit(1);
            }

            // Wait for Connection
            System.out.println("Waiting for Connection...");
            try {
                socket = serverSocket.accept();
                serverSocket.close();
            } catch (IOException e) {
                doRun = false;
                System.err.println("Unable to accept a connection.");
                System.exit(1);
            }

            ServerRouterThread serverRouterThread = new ServerRouterThread(socket, this);
            serverRouterThread.start();
            System.out.println("Thread Created");
        }
    }

    public boolean addServer(String machineInfo) {
        //check to see server already exist. If so end
        for (int i = 0; i < arrServerContainer.size(); i++){
            String instServer = arrServerContainer.get(i).getMachineInfo();
            if (machineInfo.equals(instServer)){
                return false;
            }
        }
        //if not add machine
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setMachineInfo(machineInfo);
        arrServerContainer.add(machineContainer);
        return true;
    }

    public boolean removeServer(String machineInfo) {
        //Loop through machines
        for (int i = 0; i < arrServerContainer.size(); i++){
            String instServer = arrServerContainer.get(i).getMachineInfo();
            //find machine
            if (machineInfo.equals(instServer)){
                //remove machine
                arrServerContainer.remove(i);
                return true;
            }
        }
        return false;
    }

    public String getServerList(String machineInfo){
        //This will return a list of Servers, excluding the one asking.
        StringBuilder servers = new StringBuilder();
        boolean doFirst = true;

        // if there is more than one server concat them with **
        for (int i = 0; i < arrServerContainer.size(); i++){
            String instServer = arrServerContainer.get(i).getMachineInfo();
            if (!machineInfo.equals(instServer) ){
                if (doFirst){
                    servers.append(instServer);
                    doFirst = false;
                } else {
                    servers.append(":").append(instServer);
                }
            }
        }

        return servers.toString();
    }

    public String getMachineInfo(){
        return machineInfo;
    }

    public void addClientToChat(String machineInfo){
        //check to see server already exist. If so end
        for (int i = 0; i < arrChatContainer.size(); i++){
            String instServer = arrChatContainer.get(i).getMachineInfo();
            if (machineInfo.equals(instServer)){
                return;
            }
        }
        //if not add machine
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setMachineInfo(machineInfo);
        arrChatContainer.add(machineContainer);
    }

    public void removeClientFromChat(String machineInfo) {
        //Loop through machines
        for (int i = 0; i < arrChatContainer.size(); i++){
            String instServer = arrChatContainer.get(i).getMachineInfo();
            //find machine
            if (machineInfo.equals(instServer)){
                //remove machine
                arrChatContainer.remove(i);
                return;
            }
        }
    }

    public String getClientChatList(String machineInfo) {
        //This will return a list of Servers, excluding the one asking.
        StringBuilder clients = new StringBuilder();
        boolean doFirst = true;

        // if there is more than one server concat them with **
        for (int i = 0; i < arrChatContainer.size(); i++){
            String instServer = arrChatContainer.get(i).getMachineInfo();
            if (!machineInfo.equals(instServer) ){
                if (doFirst){
                    clients.append(instServer);
                    doFirst = false;
                } else {
                    clients.append(":").append(instServer);
                }
            }
        }
        return clients.toString();
    }
}
