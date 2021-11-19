package com.Main;

import com.classes.IPAddressAndPortContainer;
import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;
import com.staticFields.SettingsForServer;
import com.threads.ServerThread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TCPServer {
    //declarations
    private ArrayList<MachineContainer> arrClientContainer, arrChatRoomContainer;
    private int portNum;
    private MachineContainer machineContainer, serverRouterMachineContainer;
    private IPAddressAndPortContainer serverRouterIPandPort;
    private String localFolder;

    public static void main(String[] args) throws IOException {
        //Who am I?
        ProcessComputerInfo processComputerInfo = new ProcessComputerInfo();
        String message = processComputerInfo.getWhoAmI();
        System.out.println(message);

        //set a container with this info
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setLocalHostName(processComputerInfo.getLocalHostName());
        machineContainer.setLocalIPAddress(processComputerInfo.getLocalIPAddress());
        machineContainer.setExternalIPAddress(processComputerInfo.getExternalIP());
        machineContainer.setIsServer(1);

        new TCPServer().Login(machineContainer);
    }

    private void Login(MachineContainer machineContainer) throws IOException {
        //declarations
        Scanner scanner = new Scanner(System.in);
        SettingsForServer settingsForServer = new SettingsForServer();

        //Setup Server
        //  Server Name
        System.out.println("Enter Server Name (leave bank for "+settingsForServer.getServerName()+"):");
        String username = scanner.nextLine();
        if (username.length() == 0){
            username = settingsForServer.getServerName();
        } else {
            settingsForServer.setServerName(username);
            System.out.println("Updated Server Name in Settings.");
        }
        machineContainer.setUserName(username);

        //  Server Local Folder
        System.out.println("Local Folder address (leave blank for "+settingsForServer.getFolder()+"):");
        localFolder = scanner.nextLine();
        if (localFolder.length() == 0){
            localFolder = settingsForServer.getFolder();
        } else {
            settingsForServer.setFolder(localFolder);
            System.out.println("Local Folder has been updated in Settings.\n" +
                    "Please make sure this is a valid location.");
        }

        //  Server Port Number
        System.out.println("Enter your Port Number (Leave Blank for "+settingsForServer.getPortNum()+"):");
        int portNum;
        while (true) {
            try {
                String strPortNum = scanner.nextLine();
                if (strPortNum.length() == 0){
                    portNum = settingsForServer.getPortNum();
                } else {
                    portNum = Integer.parseInt(strPortNum);
                    settingsForServer.setPortNum(portNum);
                    System.out.println("Port Number has been updated in settings.\n" +
                            "Please Make sure this port number is valid");
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter your Port *Number*: ");
            }
        }
        machineContainer.setPortNum(portNum);

        //set Globals
        this.portNum = portNum;
        this.machineContainer = machineContainer;

        //register server to serverRouter
        registerServer();

        //Hold for connection
        waitForConnection();
    }

    private void waitForConnection() throws IOException {
        //declarations
        boolean doRun = true;
        ServerSocket serverSocket = null;
        Socket routerSocket = null;
        arrClientContainer = new ArrayList<>();
        arrChatRoomContainer = new ArrayList<>();

        //set up ServerSocket and wait on port ????
        while(doRun){
            try {
                serverSocket = new ServerSocket(portNum);
                System.out.println("Server: "+machineContainer.getLocalHostName()+" is listening on port: "+ portNum);
            } catch (IOException e) {
                System.err.println("Could not listen on port: "+ portNum +".");
                disconnectServerRouter();
                System.exit(1);
            }

            //Wait for Router to connect
            System.out.println("Waiting for Connection...");
            try {
                routerSocket = serverSocket.accept();
                serverSocket.close();
            } catch (IOException e) {
                doRun = false;
                disconnectServerRouter();
                System.err.println("Server is unable to accept a connection.");
                System.exit(1);
            }

            //Send to Server Thread
            ServerThread serverThread = new ServerThread(routerSocket, machineContainer, this);
            serverThread.start();
        }
    }

    /***************************************************
     *             Server Router Services
     ***************************************************/

    private void dialServerRouter() {
        //declaration
        Scanner scanner = new Scanner(System.in);
        serverRouterIPandPort = new IPAddressAndPortContainer();
        SettingsForServer settingsForServer = new SettingsForServer();

        // who are you connecting to?
        //      Set IP Address of Server Router
        System.out.println("Router IP Address (Leave Blank for "+settingsForServer.getServerRouterIPAddress()+"):");
        String routerIP = scanner.nextLine();
        if (routerIP.length() == 0){
            routerIP = settingsForServer.getServerRouterIPAddress();
        } else {
            settingsForServer.setServerRouterIPAddress(routerIP);
            System.out.println("Router IP Address has been revised in Settings.");
        }

        //      Set Port number of Server Router
        System.out.println("Port Number for Router (Leave Blank for "+settingsForServer.getPortNumServerRouter()+"):");
        int routerPortNum;
        while (true){
            try {
                String strRouterPortNum = scanner.nextLine();
                if (strRouterPortNum.length() == 0) {
                    routerPortNum = settingsForServer.getPortNumServerRouter();
                } else {
                    routerPortNum = Integer.parseInt(strRouterPortNum);
                    settingsForServer.setPortNumServerRouter(routerPortNum);
                    System.out.println("Server Router Port Number has been updated in settings.\n" +
                            "Please Make sure this port number is valid");
                }
                break;
            } catch (NumberFormatException e){
                System.out.println("Please enter your Port Number: ");
            }
        }

        //add to container
        serverRouterIPandPort.IPAddress = routerIP;
        serverRouterIPandPort.PortNum = routerPortNum;
    }

    public Socket connectServerRouter() throws IOException {
        //declarations
        String routerIP = serverRouterIPandPort.IPAddress;
        int routerPortNum = serverRouterIPandPort.PortNum;

        System.out.println("Connecting to "+routerIP+" through port number: "+routerPortNum);

        //Connect to Router and setup input and output streams
        Socket socket = new Socket(routerIP,routerPortNum);
        return socket;
    }

    private void registerServer() throws IOException {
        //call serverRouter
        dialServerRouter();
        Socket socket = connectServerRouter();

        //register machine
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        String message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF("Register_Server");
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        String machineInfo = machineContainer.getMachineInfo();
        dataOutputStream.writeUTF(machineInfo);
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);

        // get ServerRouter Info
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF("Get_ServerRouter_Info");
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        serverRouterMachineContainer = new MachineContainer();
        serverRouterMachineContainer.setMachineInfo(message);

        //hangup
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF("good_bye");
        socket.close();
    }

    private void disconnectServerRouter() throws IOException {
        //call serverRouter
        Socket socket = connectServerRouter();

        //register machine
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        String message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF("Remove_Server");
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF(machineContainer.getMachineInfo());
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
    }

    /***************************************************
     *             Client Records
     ***************************************************/

    public MachineContainer addClient(String machineInfo){
        //check to see username already exist. If so end
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setMachineInfo(machineInfo);

        for (MachineContainer mc: arrClientContainer) {
            if (mc.equals(machineContainer)){
                return null;
            }
        }
        arrClientContainer.add(machineContainer);
        return machineContainer;
    }

    public void removeClient(MachineContainer machineInfo) {
        arrClientContainer.remove(machineInfo);

    }

    public String getClients(String machineInfo){
        //This will return a list of Clients, excluding the one asking.
        StringBuilder clients = new StringBuilder();
        boolean doFirst = true;

        // if there is more than one server concat them with **
        for (int i = 0; i < arrClientContainer.size(); i++){
            String instServer = arrClientContainer.get(i).getMachineInfo();
            if (!machineInfo.equals(instServer)){
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

    /***************************************************
     *             ChatRoom Records
     ***************************************************/

    public void addChatRoom (MachineContainer machineInfo){
        //Add machine to container
        arrChatRoomContainer.add(machineInfo);

    }

    public void removeChatRoom (MachineContainer machineInfo) {
        //Find container with IP and Port, and remove
        arrChatRoomContainer.remove(machineInfo);
    }

    public String getChatRooms(String machineInfo){
        //This will return a list of Clients, excluding the one asking.
        StringBuilder clients = new StringBuilder();
        boolean doFirst = true;

        // if there is more than one server concat them with **
        for (int i = 0; i < arrChatRoomContainer.size(); i++){
            String instServer = arrChatRoomContainer.get(i).getMachineInfo();
            if (!machineInfo.equals(instServer)){
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

    /***************************************************
     *             Machine Info
     ***************************************************/
    public String getMachineInfo(){
        return machineContainer.getMachineInfo();
    }
    public String getLocalFolder() {
        return localFolder;
    }
}
