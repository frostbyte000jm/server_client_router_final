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
    private TCPServerRouter tcpServerRouter;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ServerRouterThread(Socket socket, TCPServerRouter tcpServerRouter) throws IOException {
        //declarations
        this.socket = socket;
        this.tcpServerRouter = tcpServerRouter;

        //connect to Server
        System.out.println("Connection established.");
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public void run() {
        System.out.println("ServerRouter - running");
        try {
            service();
        } catch (IOException e) {
            System.out.println("ServerRouterThread Failed");
            e.printStackTrace();
        }
    }

    public void service() throws IOException {
        //declaration
        boolean doRun = true;

        while(doRun){
            dataOutputStream.writeUTF("service_needed?");
            String message = dataInputStream.readUTF();
            System.out.println("Server: "+message);

            if (message.equals("Register_Server")) {
                registerServer();
            } else if (message.equals("Remove_Server")) {
                removeServer();
            } else if (message.equals("Server_List")) {
                serverList();
            } else if (message.equals("good_bye")) {
                System.out.println("good bye.");
                doRun = false;
                socket.close();
                System.out.println("Still waiting on another connection");
            }
        }
    }

    private void registerServer() throws IOException {
        dataOutputStream.writeUTF("Who_are_you");
        String message = dataInputStream.readUTF();
        System.out.println("Server: "+message);
        boolean doSuccess = tcpServerRouter.addServer(message);

        if (doSuccess){
            System.out.println("Success Adding Server");
            dataOutputStream.writeUTF("Success");
        } else {
            System.out.println("Failed adding Server");
            dataOutputStream.writeUTF("Fail Already Registered");
        }
    }

    private void removeServer() throws IOException {
        dataOutputStream.writeUTF("Who_are_you");
        String message = dataInputStream.readUTF();
        System.out.println("Server: "+message);
        boolean doSuccess = tcpServerRouter.removeServer(message);

        if (doSuccess){
            dataOutputStream.writeUTF("Success");
        } else {
            dataOutputStream.writeUTF("Fail Not Found");
        }
    }

    private void serverList() throws IOException {
        dataOutputStream.writeUTF("Who_are_you");
        String message = dataInputStream.readUTF();
        System.out.println("Server: "+message);
        String serverList = tcpServerRouter.getServerList(message);
        dataOutputStream.writeUTF(serverList);
    }

}
