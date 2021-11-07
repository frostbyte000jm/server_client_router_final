package com.main;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPClient2 {
    //declarations
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;
    private static Scanner scanner;
    private static Boolean doRun;


    public static void main(String[] args) throws IOException {
        //declarations
        scanner = new Scanner(System.in);
        doRun = true;

        // Who am I
        try{
            InetAddress addr = InetAddress.getLocalHost();
            String localIPAddress = addr.getHostAddress(); // Machine's IP Address
            String localHostName = addr.getCanonicalHostName(); // Machine's Host Name
            System.out.println("Local Host Information\nHost Name: "+localHostName+"\nIP Address: "+localIPAddress);
        } catch (UnknownHostException uhe) {
            System.out.println("Something Majorly Wrong. I can't find your IP address or HostName.");
        }
        loginScreen();
    }

    private static void loginScreen() throws IOException {
        // Sets up Socket
        //declarations
        Socket socket = null;

        // User needs to enter Where to go.
        System.out.print("Enter Server IP Address: ");
        String routerIP = scanner.nextLine();
        System.out.print("Enter Port Number: ");
        int portNum;
        while (true){
            try{
                portNum = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e){
                System.out.println("Please enter a Port Number: ");
            }
        }

        // Connect to Router and Receive Greetings from both Router and Server
        socket = new Socket(routerIP,portNum);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream.writeUTF("Hello Router");
        String greetings = dataInputStream.readUTF();
        System.out.println("Router Said: "+ greetings);
        greetings = dataInputStream.readUTF();
        System.out.println("Router Said: "+greetings);

        //loop for messages from server
        waitForMessage();

        //close connections
        socket.close();
    }

    private static void waitForMessage() throws IOException {
        // Loop that waits and interprets messages

        while (doRun){
            //System.out.println("Waiting for message from Router from Server.");
            dataOutputStream.writeUTF("ready for action");
            String action = dataInputStream.readUTF();
            if (action.equals("message")) {
                displayMessage();
            } else if (action.equals("good_bye")) {
                goodBye();
            } else if (action.equals("request_reply")) {
                sendMessage();
            } else if (action.equals("retrieve_file")) {
                sendFile();
            } else if (action.equals("sending_file")) {
                receiveFile();
            }
        }
    }

    private static void goodBye() {
        // Closes Socket sends back to Login Screen
        doRun = false;
    }

    private static void sendMessage() throws IOException {
        //routine to let user create a message and send it
        System.out.print(">> ");
        String msg = scanner.nextLine();
        dataOutputStream.writeUTF(msg);
    }

    private static void displayMessage() throws IOException {
        // Routine for displaying a message
        dataOutputStream.writeUTF("ready");
        String message = dataInputStream.readUTF();
        System.out.println(message);
    }

    private static void sendFile() throws IOException {
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
    }

    private static void receiveFile() throws IOException {
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
        dataOutputStream.writeUTF("ready");

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
        if (file.exists()){
            Desktop desktop = Desktop.getDesktop();
            desktop.open(file);
        }
    }
}
