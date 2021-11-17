package com.ClientChatRoom;

import com.classes.MachineContainer;

import javax.crypto.Mac;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientChatRoom {
    //declarations
    Scanner scanner;
    Socket socket;
    MachineContainer myMachine, serverRouterMachine;

    public ClientChatRoom(MachineContainer myMachine, MachineContainer serverRouterMachine) throws IOException {
        //declarations
        this.myMachine = myMachine;
        this.serverRouterMachine = serverRouterMachine;
        this.scanner = new Scanner(System.in);
        String output = """
                        Choose:
                        1) Create a room
                        2) Join a Room
                        3) Exit
                        """;
        System.out.println(output);
        System.out.print(">> ");
        String input = scanner.nextLine();

        switch (input){
            case "1" -> createRoom();
            case "2" -> joinRoom();
            case "3" -> leave();
        }
    }

    private void createRoom() throws IOException {
        //update watchtower.
        addChatRoom();

        //set up ServerSocket
        String username = myMachine.getUserName();
        int portNum = myMachine.getPortNum();
        ServerSocket serverSocket = new ServerSocket(portNum);
        System.out.print(username+" waiting on "+portNum+" another connection...");

        //accept connection
        socket = serverSocket.accept();
        serverSocket.close();

        // start incoming thread
        ClientChatRoomIncoming clientChatRoomIncoming = new ClientChatRoomIncoming(socket);
        clientChatRoomIncoming.start();

        //send to communication
        communication();

        //remove chat room
        removeChatRoom();
    }

    private void communication() throws IOException {
        boolean doRun = true;
        String username = myMachine.getUserName();
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("you're chat room has started. type /exit to leave and / for help.");
        while (doRun){
            System.out.print(">> ");
            String message = scanner.nextLine();
            if (!message.isEmpty()){
                if (message.charAt(0) == '/'){
                    if (message.contains("exit")){
                        System.out.println("Good Bye!");
                        return;
                    } else if (message.contains("leave")) {
                        System.out.println("Ok...");
                    } else if (message.contains("file")) {
                        System.out.println("Working on it");
                    } else {
                        String printout =   """
                                        The following commands are available:
                                        /exit   - end the chat room
                                        /update - change who is in the room
                                        /file   - send a file to a friend
                                        """;
                        System.out.println(printout);
                    }
                } else {
                    dataOutputStream.writeUTF(username+": "+message);
                }
            }

        }
    }

    private void addChatRoom() throws IOException {
        //contact serverRouter
        String ipAddress = serverRouterMachine.getLocalIPAddress();
        int portNum = serverRouterMachine.getPortNum();
        Socket srSocket = new Socket(ipAddress, portNum);
        DataOutputStream dataOutputStreamSR = new DataOutputStream(srSocket.getOutputStream());
        DataInputStream dataInputStreamSR = new DataInputStream(srSocket.getInputStream());

        //get clients
        String message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: service_needed? "+message);
        dataOutputStreamSR.writeUTF("Add_Client_Chat");

        message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: Who_is_Client? "+message);
        dataOutputStreamSR.writeUTF(myMachine.getMachineInfo());
    }

    private void removeChatRoom() throws IOException {
        //contact serverRouter
        String ipAddress = serverRouterMachine.getLocalIPAddress();
        int portNum = serverRouterMachine.getPortNum();
        Socket srSocket = new Socket(ipAddress, portNum);
        DataOutputStream dataOutputStreamSR = new DataOutputStream(srSocket.getOutputStream());
        DataInputStream dataInputStreamSR = new DataInputStream(srSocket.getInputStream());

        //get clients
        String message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: service_needed? "+message);
        dataOutputStreamSR.writeUTF("Remove_Client_Chat");

        message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: Who_is_Client? "+message);
        dataOutputStreamSR.writeUTF(myMachine.getMachineInfo());
    }

    private MachineContainer chooseClient (boolean doReturn) throws IOException {
        //contact serverRouter
        String ipAddress = serverRouterMachine.getLocalIPAddress();
        int portNum = serverRouterMachine.getPortNum();
        Socket srSocket = new Socket(ipAddress, portNum);
        DataOutputStream dataOutputStreamSR = new DataOutputStream(srSocket.getOutputStream());
        DataInputStream dataInputStreamSR = new DataInputStream(srSocket.getInputStream());

        //get clients
        String message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: service_needed "+message);
        dataOutputStreamSR.writeUTF("Get_Client_Chat_List");
        message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: Who is this? "+message);
        String strMyMachine = myMachine.getMachineInfo();
        dataOutputStreamSR.writeUTF(strMyMachine);
        message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: stringify of clients "+message);
        if (message.isEmpty()){
            return null;
        }
        //build clinets
        ArrayList<MachineContainer> otherMachines = new MachineContainer().getMachineList(message);

        //hangup
        message = dataInputStreamSR.readUTF();
        System.out.println("chatRoom() - Expect: service_needed "+message);
        dataOutputStreamSR.writeUTF("good_bye");

        //print clinets
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < otherMachines.size(); i++) {
            stringBuilder.append(i+1).append(") ").append(otherMachines.get(i).getUserName()).append("\n");
        }
        System.out.println(stringBuilder.toString());

        if (doReturn) {
            // request choice
            System.out.println("Choose the Client index you wish to enter a room with.");
            String input = scanner.nextLine();
            int index = Integer.parseInt(input);

            MachineContainer host = new MachineContainer();
            host.setMachineInfo(otherMachines.get(index-1).getMachineInfo());

            return host;
        } else {
            return null;
        }
    }

    private void joinRoom() throws IOException {
        //get Host machine info
        MachineContainer host = chooseClient(true);
        String ipAddress = host.getLocalIPAddress();
        int portNum = host.getPortNum();

        //set up connection
        socket = new Socket(ipAddress, portNum);


        // send incoming thread
        ClientChatRoomIncoming clientChatRoomIncoming = new ClientChatRoomIncoming(socket);
        clientChatRoomIncoming.start();

        //setup communication
        communication();
    }

    private void leave() {
    }
}
