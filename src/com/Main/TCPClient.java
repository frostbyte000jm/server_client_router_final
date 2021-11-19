package com.Main;

import com.ClientChatRoom.ClientChatRoom;
import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {
    //declarations
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private MachineContainer machineContainer;
    private Scanner scanner;

    public static void main (String[] args) throws IOException, InterruptedException {
        //Who am I?
        ProcessComputerInfo processComputerInfo = new ProcessComputerInfo();
        String message = processComputerInfo.getWhoAmI();
        System.out.println(message);

        //set a container with this info
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setLocalHostName(processComputerInfo.getLocalHostName());
        machineContainer.setLocalIPAddress(processComputerInfo.getLocalIPAddress());
        machineContainer.setExternalIPAddress(processComputerInfo.getExternalIP());

        new TCPClient().connectToRouter(machineContainer);
    }

    private void connectToRouter(MachineContainer machineContainer) throws IOException, InterruptedException {
        //declarations
        this.machineContainer = machineContainer;
        scanner = new Scanner(System.in);
        Socket socket = null;

        while (true){
            //Connect to Router
            System.out.println("Please enter IP address of what server you would like to connect to.");

            // who are you connecting to?
            System.out.println("Router IP Address (type 'exit' to log off):");
            String routerIP = scanner.nextLine();

            // check for exit
            if (routerIP.contains("exit")){
                break;
            }

            // if not exit, continue.
            System.out.println("Port Number for Router:");
            int routerPortNum;
            while (true){
                try{
                    routerPortNum = Integer.parseInt(scanner.nextLine());
                    break;
                } catch (NumberFormatException e){
                    System.out.println("Please enter your Port Number: ");
                }
            }
            System.out.println("Connecting to "+routerIP+" through port number: "+routerPortNum);

            //Connect to Router and setup input and output streams
            socket = new Socket(routerIP,routerPortNum);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            //once connected greet router and server, and then wait for server to send commands.
            handshake();
            waitForMessages();
        }
        //close connection
        socket.close();
    }

    private void handshake() throws IOException {
        //declaration
        String message = "";

        //Wait for handshake Router
        message = dataInputStream.readUTF();
        //System.out.println("Router: "+message);
        dataOutputStream.writeUTF("Hello Router.");

        //Wait for handshake Server
        message = dataInputStream.readUTF();    //Hello Who is Calling?
        //System.out.println("Router: "+message);
        dataOutputStream.writeUTF("__Client__");
    }

    /***************************************************
     *                  Functions
     ***************************************************/

    private void waitForMessages() throws IOException, InterruptedException {
        //Loop and Wait for messages.
        boolean doRun = true;
        while (doRun){
            //System.out.println("Debug: waitForMessages");
            dataOutputStream.writeUTF("ready for action");
            String action = dataInputStream.readUTF();
            switch (action) {
                case "message_server_to_client" -> displayMessage();
                case "request_reply_client_to_server" -> sendMessage();
                case "file_client_to_server" -> sendFile();
                case "file_server_to_client" -> receiveFile();

                //      **** Client Functions ****
                case "good_bye" -> doRun = goodBye();
                case "login_info" -> login();
                case "client_chat_room" -> chatRoomStart();
            }
        }
    }

    private void displayMessage() throws IOException {
        // Routine for displaying a message
        //System.out.println("Display Message");
        dataOutputStream.writeUTF("ready to Display Message");
        String message = dataInputStream.readUTF();
        System.out.println(message);
    }

    private void sendMessage() throws IOException {
        //routine to let user create a message and send it
        //System.out.println("Sending Message");
        System.out.print(">> ");
        String msg = scanner.nextLine();
        dataOutputStream.writeUTF(msg);
    }

    private void sendFile() throws IOException {
        System.out.println("Preparing to send file.");
        //declaration
        long timeDisplay;

        //routine for creating a file and sending it
        System.out.println("Enter the path of the file (C:\\Temp\\Dude.txt): ");

        //Create file
        File file;
        while (true){
            System.out.print(">> ");
            String msg = scanner.nextLine();
            file = new File(msg);
            if (file.exists())
                break;
        }

        //Start Timer
        long timeStart = System.currentTimeMillis();

        //convert file into stream
        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

        //convert file name into bytes
        String fileName = file.getName();
        byte[] fileNameBytes = fileName.getBytes();

        //convert file into bytes
        byte[] fileBytes = new byte[(int)file.length()];
        fileInputStream.read(fileBytes);
        fileInputStream.close();

        // stop create file timer
        long timeCreateFileEnd = System.currentTimeMillis();
        timeDisplay = timeCreateFileEnd - timeStart;
        System.out.println("Time to create File: "+timeDisplay);

        // start send file timer
        long timeSentStart = System.currentTimeMillis();

        // send file name
        dataOutputStream.writeInt(fileNameBytes.length);
        dataOutputStream.write(fileNameBytes);

        //send file
        dataOutputStream.writeInt(fileBytes.length);
        dataOutputStream.write(fileBytes);

        // stop file sent timer
        long timeSentEnd = System.currentTimeMillis();
        timeDisplay = timeSentEnd - timeSentStart;
        System.out.println("Time to send File: "+timeDisplay);
        timeDisplay = timeSentEnd - timeStart;
        System.out.println("Total time to send File: "+ timeDisplay);
        System.out.println("Sending File End");
    }

    private void receiveFile() throws IOException {
        //declaration
        long timeDisplay;

        // Ask where to save file
        System.out.println("Enter the path to store file (C:\\Temp): ");

        //check for valid directory location
        File directory;
        while (true){
            System.out.print(">> ");
            String msg = scanner.nextLine();
            directory = new File(msg);
            if (directory.exists())
                break;
            else {
                System.out.println("Please enter in an existing directory.");
            }
        }

        //routine for receiving a file and opening it.
        dataOutputStream.writeUTF("ready to Receive File");

        // wait to receive File Name Bytes
        int fileNameBytesLength = dataInputStream.readInt();

        //start Timer
        long timeStart = System.currentTimeMillis();

        // complete receipt of file name bytes
        byte[] fileNameBytes = new byte[fileNameBytesLength];
        dataInputStream.readFully(fileNameBytes,0,fileNameBytesLength);

        // receive the File
        int fileBytesLength = dataInputStream.readInt();
        byte[] fileBytes = new byte[fileBytesLength];
        dataInputStream.readFully(fileBytes,0,fileBytesLength);

        // display time to receive file
        long timeReceivedFile = System.currentTimeMillis();
        timeDisplay = timeReceivedFile - timeStart;
        System.out.println("Time to receive file: "+timeDisplay);

        //Let user know something is happening
        System.out.println("File is being uncompressed.");

        // convert file name to append "_upper" at the end
        String fileName = new String(fileNameBytes);

        // Create File
        File file = new File(directory+File.separator+fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(fileBytes);
        fileOutputStream.close();

        //write time to convert file
        long timeConvertFile = System.currentTimeMillis();
        timeDisplay = timeConvertFile - timeReceivedFile;
        System.out.println("Time to convert file and complete process: "+timeDisplay);

        //Open File
        try {
            if (file.exists()) {

                Desktop desktop = Desktop.getDesktop();
                desktop.open(file);
            }
        } catch (Exception e){
            System.out.println("Unable to Open File.");
        }
    }

    /***************************************************
     *                  Client Actions
     ***************************************************/
    private boolean goodBye() {
        // Closes Socket sends back to Login Screen
        System.out.println("Good bye! You have been logged out.");
        return false;
    }

    public void login() throws IOException {
        // who are you?
        System.out.println("Enter UserName:");
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

        //send machine info
        String machineInfo = machineContainer.getMachineInfo();
        //System.out.println("Sending: "+machineInfo);
        dataOutputStream.writeUTF(machineInfo);

        // Success?
        String msgServer = dataInputStream.readUTF();
        //System.out.println("Debug: login() - msgServer: "+msgServer);
        if (msgServer.equals("Success")){
            System.out.println("Success! You are Logged in.");
        } else {
            System.out.println("Username is taken, please try again.\n");
        }
    }

    private void chatRoomStart() throws IOException, InterruptedException {
        //Who is the serverRouter?
        dataOutputStream.writeUTF("Who_with?");
        String message = dataInputStream.readUTF();

        //Start Chat Room
        new ClientChatRoom(machineContainer,message, false);

        //Send message that chat room is done
        dataOutputStream.writeUTF("done");
    }
}
