package com.Main;

import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;
import com.staticFields.settingsForServer;
import com.threads.RouterThread;
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
    private ArrayList<MachineContainer> arrClientContainer;
    private int portNum;
    private MachineContainer machineContainer;

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

        //Setup Server
        System.out.println("Enter Server Name:");
        //String username = scanner.nextLine();
        String username = "Server01";
        machineContainer.setUserName(username);
        System.out.println("Enter your Port Number:");
        int portNum;
        while (true) {
            try {
                //portNum = Integer.parseInt(scanner.nextLine());
                portNum = 5566;
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter your Port Number: ");
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
        arrClientContainer = new ArrayList<MachineContainer>();

        //set up ServerSocket and wait on port ????
        while(doRun){
            try {
                serverSocket = new ServerSocket(portNum);
                System.out.println("Server: "+machineContainer.getLocalHostName()+" is listening on port: "+ portNum);
            } catch (IOException e) {
                System.err.println("Could not listen on port: "+ portNum +".");
                System.exit(1);
            }

            //Wait for Router to connect
            System.out.println("Waiting for Connection...");
            try {
                routerSocket = serverSocket.accept();
                serverSocket.close();
            } catch (IOException e) {
                doRun = false;
                System.err.println("Server is unable to accept a connection.");
                System.exit(1);
            }

            //Send to Server Thread
            ServerThread serverThread = new ServerThread(routerSocket,this);
            serverThread.start();
        }
    }

    private Socket callServerRouter() throws IOException {
        //declaration
        Scanner scanner = new Scanner(System.in);

        // who are you connecting to?
        System.out.println("Router IP Address:");
        //String routerIP = scanner.nextLine();
        String routerIP = "127.0.1.1";
        System.out.println("Port Number for Router:");
        int routerPortNum;
        while (true){
            try{
                //routerPortNum = Integer.parseInt(scanner.nextLine());
                routerPortNum = 5555;
                break;
            } catch (NumberFormatException e){
                System.out.println("Please enter your Port Number: ");
            }
        }
        System.out.println("Connecting to "+routerIP+" through port number: "+routerPortNum);

        //Connect to Router and setup input and output streams
        Socket socket = new Socket(routerIP,routerPortNum);
        return socket;
    }

    private void registerServer() throws IOException {
        //call serverRouter
        Socket socket = callServerRouter();

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

        //hangup
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF("good_bye");
        socket.close();
    }

    public boolean addClient(String machineInfo){
        //check to see username already exist. If so end
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setMachineInfo(machineInfo);
        String incUserName = machineContainer.getUserName();

        //See if taken
        for (int i = 0; i < arrClientContainer.size(); i++){
            String userName = arrClientContainer.get(i).getUserName();
            if (incUserName == userName){
                return false;
            }
        }

        //if not add machine
        arrClientContainer.add(machineContainer);
        return true;
    }

    public boolean removeClient(String machineInfo) {
        //Loop through clients
        for (int i = 0; i < arrClientContainer.size(); i++){
            String instServer = arrClientContainer.get(i).getMachineInfo();
            //find client
            if (machineInfo == instServer){
                //remove client
                arrClientContainer.remove(i);
                return true;
            }
        }
        return false;
    }

    public String getClientList(String machineInfo){
        //This will return a list of Clients, excluding the one asking.
        String clients = arrClientContainer.get(0).getMachineInfo();;

        // if there is more than one server concat them with **
        for (int i = 1; i < arrClientContainer.size(); i++){
            String instServer = arrClientContainer.get(i).getMachineInfo();
            if (machineInfo != instServer){
                clients = clients +"**"+ instServer;
            }
        }
        return clients;
    }
}
