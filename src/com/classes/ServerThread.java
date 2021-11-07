package com.classes;

import com.staticLoc.settingsForServer;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    //declarations
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    Boolean doRun = true;

    //constructor
    public ServerThread(Socket socket) throws IOException {
        // Connect to Server
        System.out.println("Connection established.");
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
        String greetingsClient = dataInputStream.readUTF();
        System.out.println("Client Said: "+greetingsClient);
        dataOutputStream.writeUTF("Hello Router, tell Client Hello.");

        // send opening message
        String message = "\n\nWelcome to the internet\n" +
                        "Have a look around\n" +
                        "Anything that brain of yours can think of can be found\n" +
                        "We've got mountains of content\n" +
                        "Some better, some worse\n" +
                        "If none of it's of interest to you, you'd be the first\n\n "+
                        "Press enter to continue.";
        sendMessage(message);

        // wait for router to say ready
        String msg = dataInputStream.readUTF();
        System.out.println("Router says: "+msg);

        // wait for it.
        requestReply();

        // Wait for Client to ask the Router to ask the Server for something.
        sendAction("welcome_msg");

        // end thread
        return;
    }

    /******************************************************
     *      Functions
     * ******************************************************/

    private void sendAction(String action) throws IOException {
        //declaration
        String msg;

        // wait for router to say ready
        msg = dataInputStream.readUTF();
        System.out.println("Router says: "+msg);

        //send Action
        if (action.equals("welcome_msg")) {
            sendWelcomeMessage();
        } else if (action.equals("start_echo")) {
            startEcho();
        } else if (action.equals("upper_txt_doc")) {
            uppercaseTextFile();
        } else if (action.equals("send_video_tny")) {
            sendVideoFile("tiny");
        } else if (action.equals("send_video_sm")) {
            sendVideoFile("small");
        } else if (action.equals("send_video_med")) {
            sendVideoFile("med");
        } else if (action.equals("send_video_lg")) {
            sendVideoFile("large");
        } else if (action.equals("good_bye")) {
            goodbye();
        }
    }

    private void sendMessage(String message) throws IOException {
        //declarations
        String msg = "";

        // Tell Client, Server is sending a message.
        dataOutputStream.writeUTF("message");
        msg = dataInputStream.readUTF();
        System.out.println("Router says, Client says: "+msg);

        // send message
        dataOutputStream.writeUTF(message);
    }

    private String requestReply() throws IOException {
        // wait for router to say ready
        String msg = dataInputStream.readUTF();
        System.out.println("Router says: "+msg);

        // Server tells router to get reply form client
        dataOutputStream.writeUTF("request_reply");
        msg = dataInputStream.readUTF();
        return msg;
    }

    private String makeServerDirectory(){
        File directory = new File(settingsForServer.getTempFolder());
        if (!directory.exists())
            directory.mkdir(); //if this ever moves, change it to mkdirs
        return directory.getPath();
    }

    private File retrieveFile() throws IOException {
        //declaration
        long timeDisplay;

        // wait for router to say ready
        String msg = dataInputStream.readUTF();
        System.out.println("Router says: "+msg);

        // Server tells router to send file information.
        dataOutputStream.writeUTF("retrieve_file");

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

        // convert file name to append "_upper" at the end
        String fileName = new String(fileNameBytes);

        // Create File
        String directory = makeServerDirectory();
        File file = new File(directory+File.separator+fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(fileBytes);
        fileOutputStream.close();

        //write time to convert file
        long timeConvertFile = System.currentTimeMillis();
        timeDisplay = timeConvertFile - timeReceivedFile;
        System.out.println("Time to convert file: "+timeDisplay);

        return file;
    }

    private void sendFile(File file) throws IOException {
        //declaration
        long timeDisplay;

        // wait for router and Client to say ready for action
        String msg = dataInputStream.readUTF();
        System.out.println("Router says: "+msg);

        // Server tells router to send file information.
        dataOutputStream.writeUTF("sending_file");
        msg = dataInputStream.readUTF();
        System.out.println("Router says, Client Says: "+msg);

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

        // send file name
        dataOutputStream.writeInt(fileNameBytes.length);
        dataOutputStream.write(fileNameBytes);

        //send file
        dataOutputStream.writeInt(fileBytes.length);
        dataOutputStream.write(fileBytes);

        // stop file sent timer
        long timeSentEnd = System.currentTimeMillis();
        timeDisplay = timeSentEnd - timeCreateFileEnd;
        System.out.println("Time to send File: "+timeDisplay);
    }

    /***************************************************
     *                  Actions
     ***************************************************/

    private void goodbye() throws IOException {
        // send a message that the machine is about to disconnect.
        String message = "\n\nCould I interest you in everything?\n" +
                        "All of the time\n" +
                        "A little bit of everything\n" +
                        "All of the time\n" +
                        "Apathy's a tragedy\n" +
                        "And boredom is a crime\n" +
                        "Anything and everything\n" +
                        "And anything and everything\n" +
                        "And anything and everything\n" +
                        "And all of the time\n\n\n" +
                        "********** Good Bye! **********";

        sendMessage(message);

        // declarations
        String msg = "";

        // end server loop
        doRun = false;

        // inform Router to inform client we are disconnecting
        dataOutputStream.writeUTF("good_bye");
    }

    private void sendWelcomeMessage() throws IOException {
        // Create Message
        String message = "\n\nEnter number of what you want to do:\n"+
                        "1) Echo\n" +
                        "2) Uppercase Text File\n" +
                        "3) Send Tiny Video File\n" +
                        "4) Send Small Video File\n" +
                        "5) Send Medium Video File\n" +
                        "6) Send Large Video File\n" +
                        "7) Goodbye";

        // Send message
        sendMessage(message);

        //declarations
        String msg = "";

        // wait for client reply
        msg = requestReply();
        int idx = Integer.parseInt(msg);

        if (idx == 1){
            sendAction("start_echo");
        } else if (idx == 2){
            sendAction("upper_txt_doc");
        } else if (idx == 3) {
            sendAction("send_video_tny");
        } else if (idx == 4) {
            sendAction("send_video_sm");
        } else if (idx == 5) {
            sendAction("send_video_med");
        } else if (idx == 6) {
            sendAction("send_video_lg");
        } else if (idx == 7) {
            sendAction("good_bye");
        }
    }

    private void startEcho() throws IOException {
        // This will echo at the client
        //declarations
        Boolean doEcho = true;

        // create message
        String message = "The Server has a large hole. If you type something " +
                "it will echo back to you. But it may sound a little distorted. To " +
                "stop type in 'bye.'";
        sendMessage(message);

        // start loop
        while (doEcho){
            //request reply
            String msg = requestReply();

            //end loop
            if (msg.toUpperCase().equals("BYE.")){
                doEcho = false;
                msg = "Goooodbyoo!!!";
            } else {
                //Revise msg
                msg = msg.replace("o","oo");
                msg = msg.replace("a","oo");
                msg = msg.replace("e","oo");
                msg = msg.replace("i","oo");
                msg = msg.replace("u","oo");
            }

            // send message and wait for router and client to checkin.
            sendMessage(msg);
            String checkin = dataInputStream.readUTF();
            System.out.println("Router said, Client said: "+checkin);
        }

        //back to home
        sendAction("welcome_msg");
    }

    private void uppercaseTextFile() throws IOException {
        // Upper a text file and send back
        // create message
        String message = "Please enter the location of the .txt file. Once received the " +
                "server will change all the text to uppercase and then send it back with " +
                "'_upper' appended to the file name.";
        sendMessage(message);

        //set action to upper text doc
        File file = retrieveFile();

        //create second file
        String fileNameRevised = file.getName();
        fileNameRevised = fileNameRevised.replace(".txt","_upper.txt");
        String directory = makeServerDirectory();
        File fileRevised = new File(directory+File.separator+fileNameRevised);

        //Create Buffer and Printer
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        PrintWriter printWriter = new PrintWriter(new FileWriter(fileRevised));

        // Convert to Upper
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
            line = line.toUpperCase();
            printWriter.println(line);
        }

        //close Readers and Writers
        bufferedReader.close();
        printWriter.close();

        // send File back
        sendFile(fileRevised);

        //remove files from TempServer
        File fileDirectory = new File(settingsForServer.getTempFolder());
        for (File fileDel : fileDirectory.listFiles()) {
            String strFile = fileDel.getAbsolutePath();
            System.out.println("f: " + strFile);
            fileDel.delete();
        }

        sendAction("welcome_msg");
    }

    private void sendVideoFile(String size) throws IOException {
        // this will send a small, medium, or large video file.

        // create message
        String message = "Excellent Choice Human. The Server will compress the file " +
                        "and it will be on its way shortly. ";
        sendMessage(message);

        // choose the correct file and send.
        File file;
        if (size.equals("tiny")){
            file = new File("src/data/video_tiny.avi");
            sendFile(file);
        } else if (size.equals("small")){
            file = new File("src/data/video_sm.mp4");
            sendFile(file);
        } else if (size.equals("med")){
            file = new File("src/data/video_med.mp4");
            sendFile(file);
        } else if (size.equals("large")){
            file = new File("src/data/video_lg.avi");
            sendFile(file);
        }

        //Compress and send the file.
        sendAction("welcome_msg");
    }
}
