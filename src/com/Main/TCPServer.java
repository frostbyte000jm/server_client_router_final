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

public class TCPServer {
    private ArrayList<MachineContainer> arrClientContainer;
    private ArrayList<MachineContainer> arrServerContainer;
    public static void main(String[] args) throws IOException {
        //declarations


        //Who am I?
        ProcessComputerInfo processComputerInfo = new ProcessComputerInfo();
        String message = processComputerInfo.getWhoAmI();
        System.out.println(message);

        new TCPServer().waitForConnection(processComputerInfo);
    }

    private void waitForConnection(ProcessComputerInfo processComputerInfo) throws IOException {
        //declarations
        boolean doRun = true;
        int portNum = settingsForServer.getPortNum();
        ServerSocket serverSocket = null;
        Socket routerSocket = null;
        arrClientContainer = new ArrayList<MachineContainer>();
        arrServerContainer = new ArrayList<MachineContainer>();

        while(doRun){
            //set up ServerSocket and wait on port 5555
            try {
                serverSocket = new ServerSocket(portNum);
                System.out.println("Router: "+processComputerInfo.getLocalHostName()+" is listening on port: "+ portNum);
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
