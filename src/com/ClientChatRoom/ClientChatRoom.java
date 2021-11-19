package com.ClientChatRoom;

import com.classes.MachineContainer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientChatRoom {
    //declarations
    private Scanner scanner;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private MachineContainer myMachine, hostMachine;
    boolean doReceive = false;

    public ClientChatRoom(MachineContainer myMachine, String chatrooms, boolean doReturn) throws IOException, InterruptedException {
        //declarations
        this.myMachine = myMachine;
        this.hostMachine = null;
        this.scanner = new Scanner(System.in);

        if (chatrooms.equals("chat_room_create")){
            createRoom();                   //if you created this chatroom you are the host, and you're waiting for the guest to join.
        } else {
            joinRoom(chatrooms, doReturn);  //when you join you will be asked to pick from a list of host who are starting this chat room.
        }
    }

    private void createRoom() throws IOException {
        //set up ServerSocket
        String username = myMachine.getUserName();
        int portNum = myMachine.getPortNum();
        ServerSocket serverSocket = new ServerSocket(portNum);
        System.out.println(username+" waiting on another connection...");

        //accept connection
        socket = serverSocket.accept();
        serverSocket.close();

        // start incoming thread
        ClientChatRoomIncoming clientChatRoomIncoming = new ClientChatRoomIncoming(socket, this);
        clientChatRoomIncoming.start();

        //send to communication
        communication();
    }

    private void joinRoom(String chatrooms, boolean doReturn) throws IOException, InterruptedException {
        if (doReturn){
            hostMachine = new MachineContainer();
            hostMachine.setMachineInfo(chatrooms);
        } else {
            //get Host machine info
            hostMachine = chooseClient(chatrooms);
            if (hostMachine == null)
                return;
        }

        //setup IP and Port
        String ipAddress = hostMachine.getLocalIPAddress();
        int portNum = hostMachine.getPortNum();

        //set up connection
        try{
            socket = new Socket(ipAddress, portNum);
        } catch (Exception e) {
            Thread.currentThread().sleep(1000);
        }


        // start incoming thread
        ClientChatRoomIncoming clientChatRoomIncoming = new ClientChatRoomIncoming(socket, this);
        clientChatRoomIncoming.start();

        //setup communication
        communication();
    }

    private void communication() throws IOException {
        //declaration
        boolean doRun = true;
        String username = myMachine.getUserName();
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        System.out.println("you're chat room has started. type /exit to leave and / for help.");
        while (doRun){
            if (doReceive){
                System.out.println("Initiating File Transfer Window.");
                try {
                    new ClientFileExchange(myMachine, hostMachine, false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            System.out.print(">> ");
            String message = scanner.nextLine();
            if (!message.isEmpty()) {
                if (message.charAt(0) == '/') {
                    if (message.contains("exit")) {
                        dataOutputStream.writeUTF(username + " has left the chat.");
                        dataOutputStream.writeUTF("___good_bye___");
                        System.out.println("Good Bye!");
                        return;
                    } else if (message.contains("file")) {
                        dataOutputStream.writeUTF("Client is initiating another chat room.");
                        dataOutputStream.writeUTF("___Client_wants_to_send_file___");
                        System.out.println("Initiating File Transfer Window.");
                        try {
                            new ClientFileExchange(myMachine, hostMachine, true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return;
                    } else {
                        String printout = """
                                The following commands are available:
                                /exit   - end the chat room
                                /update - change who is in the room
                                /file   - send a file to a friend
                                """;
                        System.out.println(printout);
                    }
                } else {
                    dataOutputStream.writeUTF(username + ": " + message);
                }
            }
        }
    }



    public void doReceiveFile(boolean doReceive) throws IOException {
        this.doReceive = doReceive;
    }

    private MachineContainer chooseClient (String chatrooms) {
        //build clinets
        ArrayList<MachineContainer> otherMachines = new MachineContainer().getMachineList(chatrooms);

        //print clinets
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < otherMachines.size(); i++) {
            stringBuilder.append(i+1).append(") ").append(otherMachines.get(i).getUserName()).append("\n");
        }
        System.out.println(stringBuilder);

        // request choice
        System.out.println("Choose the Client index you wish to enter a room with. Type 'exit' to exit.");
        int index;
        while (true){
            String input = scanner.nextLine();
            if (input.equals("exit"))
                return null;
            try{
                index = Integer.parseInt(input);
                if (0 < index && index <= otherMachines.size())
                    break;
            } catch (NumberFormatException e){}
            System.out.println("Please enter a valid index.: ");
        }

        MachineContainer host = new MachineContainer();
        host.setMachineInfo(otherMachines.get(index-1).getMachineInfo());

        return host;
    }
}
