package com.threads;

import com.Main.TCPServer;
import com.Main.TCPServerRouter;
import com.classes.MachineContainer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerRouterThread extends Thread {
    //declarations
    TCPServerRouter tcpServerRouter;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    MachineContainer machineContainer;

    public ServerRouterThread(Socket socket, TCPServerRouter tcpServerRouter, MachineContainer machineContainer) throws IOException {
        this.tcpServerRouter = tcpServerRouter;
        this.machineContainer = machineContainer;

        //connect to Server
        System.out.println("Connection established.");
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());


    }
}
