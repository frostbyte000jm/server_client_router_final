package com.Main;

import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;
import com.staticFields.settingsForServer;
import com.threads.RouterThread;
import com.threads.ServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TCPServer {
    //declarations
    private ArrayList<MachineContainer> arrClientContainer;
    private ArrayList<MachineContainer> arrServerContainer;
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
        arrServerContainer = new ArrayList<MachineContainer>();

        //Setup Server
        System.out.println("Enter Server Name:");
        String username = scanner.nextLine();
        machineContainer.setUserName(username);
        System.out.println("Enter your Port Number:");
        int portNum;
        while (true) {
            try {
                portNum = Integer.parseInt(scanner.nextLine());
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

        waitForConnection();
    }

    private void waitForConnection() throws IOException {
        //declarations
        boolean doRun = true;
        ServerSocket serverSocket = null;
        Socket routerSocket = null;
        arrClientContainer = new ArrayList<MachineContainer>();


        while(doRun){
            //set up ServerSocket and wait on port ????
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

    public void addClient(MachineContainer machineContainer){
        arrClientContainer.add(machineContainer);
    }

    public void addServer(MachineContainer machineContainer){
        arrServerContainer.add(machineContainer);
    }

    public void yellClients(){
        System.out.println("trying to yell");
        for (MachineContainer clientContainer: arrClientContainer) {
            String s = clientContainer.getMachineInfo();
            System.out.println(s);
        }
    }
}
