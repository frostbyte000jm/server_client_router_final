package com.ClientChatRoom;

import com.classes.MachineContainer;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientFileExchange {
    //declarations
    MachineContainer myMachine, hostMachine;
    Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Scanner scanner;

    //The clients needed a second room to send and receive files. Keeping them in the chatroom was causing syncing issues.

    public ClientFileExchange(MachineContainer myMachine, MachineContainer hostMachine, boolean doSender) throws IOException, InterruptedException {
        //declarations
        this.myMachine = myMachine;
        this.hostMachine = hostMachine;
        scanner = new Scanner(System.in);

        if (hostMachine == null){
            startRoom(doSender);
            new ClientChatRoom(myMachine, "chat_room_create", true);
        } else {
            joinGuest(doSender);
            new ClientChatRoom(myMachine, hostMachine.getMachineInfo(), true);
        }
        socket.close();
    }

    private void startRoom(boolean doSender) throws IOException {
        //set up ServerSocket
        String username = myMachine.getUserName();
        int portNum = myMachine.getPortNum();
        ServerSocket serverSocket = new ServerSocket(portNum);
        System.out.println(username+" waiting on another connection...");

        //accept connection
        socket = serverSocket.accept();
        serverSocket.close();
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        //send to proper method.
        if(doSender){
            sendFile();
        } else {
            receiveFile();
        }
    }

    private void joinGuest(boolean doSender) throws IOException, InterruptedException {
        //setup IP and Port
        String ipAddress = hostMachine.getLocalIPAddress();
        int portNum = hostMachine.getPortNum();

        while (true){
            try{
                //set up connection
                socket = new Socket(ipAddress, portNum);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                break;
            } catch (Exception e) {
                Thread.currentThread().sleep(1000);
            }
        }

        //send to proper method.
        if(doSender){
            sendFile();
        } else {
            receiveFile();
        }
    }


    public void sendFile() throws IOException {
        //initiate
        String message = dataInputStream.readUTF();
        System.out.println("sendFile() - expect: Accept or Not "+ message);

        //crap out.
        if(message.equals("__client_refused__")){
            System.out.println("Client refused.");
            return;
        }

        System.out.println("Sending File Start");
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

        // File Sent
        dataOutputStream.writeUTF("_File_is_sent_");
        message = dataInputStream.readUTF();
        System.out.println("sendFile() - expect: _file_is_received_ " + message);
    }

    public void receiveFile() throws IOException {
        Scanner scanner = new Scanner(System.in);

        //accept
        System.out.println("Client wishes to send you a file. do you accept (y/n)");
        String input = scanner.nextLine();
        if(input.contains("n")){
            dataOutputStream.writeUTF("__client_refused__");
            return;
        } else {
            dataOutputStream.writeUTF("__client_accepted__");
        }

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
            System.out.println("some errors");
        }

        // File Sent
        String message = dataInputStream.readUTF();
        System.out.println("sendFile() - expect: _File_is_sent_ " + message);
        dataOutputStream.writeUTF("_file_is_received_");


    }
}
