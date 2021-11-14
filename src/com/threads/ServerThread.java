package com.threads;

import com.Main.TCPServer;
import com.classes.MachineContainer;
import com.staticFields.SettingsForServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class ServerThread extends Thread{
    //declarations
    private TCPServer tcpServer;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String whoConnected, clientMachineInfo, serverMachineInfo;

    public ServerThread(Socket routerSocket, TCPServer tcpServer) throws IOException {
        this.tcpServer = tcpServer;
        this.serverMachineInfo = tcpServer.getMachineInfo();

        // Connect to Incoming
        System.out.println("Connection established.");

        //setup input and output stream
        dataOutputStream = new DataOutputStream(routerSocket.getOutputStream());
        dataInputStream = new DataInputStream(routerSocket.getInputStream());

        //handshake Router
        dataOutputStream.writeUTF("Hello Who is Calling?");
        String message = dataInputStream.readUTF();
        System.out.println("Router Said: "+message);

        //Who is Connected
        this.whoConnected = message;

        //finish handshake
        message = dataInputStream.readUTF();
        System.out.println("Router Said: "+message);
    }

    //The Thread will asks who is connected and get them started with the correct interface
    public void run(){
        if (whoConnected.equals("__Client__")) {
            try {
                newClientInfo();
            } catch (IOException e) {
                System.out.println("ServerThread - Unable to create new Client");
                e.printStackTrace();
            }
        } else if (whoConnected.equals("__Server__")) {

        }
    }

    //When a client is connecting, we need to get their username and computer info
    private void newClientInfo() throws IOException {
        while (true){
            //ask client for information
            dataOutputStream.writeUTF("login_info");

            //receive reply form client
            String clientInfo = dataInputStream.readUTF();
            System.out.println("Router: "+clientInfo);

            //take client info and turn it into a container.
            Boolean doSuccess = tcpServer.addClient(clientInfo);
            if (doSuccess){
                clientMachineInfo = clientInfo;
                break;
            } else {
                sendMessage("This username is taken. try again.");
            }
        }

        //continue to new connection
        startService();
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
        } else if (action.equals("send_file")) {
            sendFileChoice();
        } else if (action.equals("request_file")) {
            receiveFileToServer();
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
        SettingsForServer settingsForServer = new SettingsForServer();
        File directory = new File(settingsForServer.getFolder());
        if (!directory.exists())
            directory.mkdirs();
        return directory.getPath();
    }

    private String makeTempServerDirectory(){
        SettingsForServer settingsForServer = new SettingsForServer();
        String tempFolder = settingsForServer.getFolder()+File.separator+"Temp";
        File directory = new File(tempFolder);
        if (!directory.exists())
            directory.mkdirs();
        return directory.getPath();
    }

    private void removeTempServerDirectory(){
        //remove files from TempServer
        SettingsForServer settingsForServer = new SettingsForServer();
        String tempFolder = settingsForServer.getFolder()+File.separator+"Temp";
        File fileDirectory = new File(tempFolder);
        for (File fileDel : Objects.requireNonNull(fileDirectory.listFiles())) {
            String strFile = fileDel.getAbsolutePath();
            System.out.println("f: " + strFile);
            fileDel.delete();
        }
    }

    private File retrieveFile(Boolean doTemp) throws IOException {
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

        // Create folder for File
        String directory = "";
        if (doTemp){
            directory = makeTempServerDirectory();
        } else {
            directory = makeServerDirectory();
        }

        // Create File
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
     *             Contact Other Servers
     ***************************************************/
    private Socket contactServer(MachineContainer machineContainer) throws IOException {
        String ipAddress = machineContainer.getLocalIPAddress();
        int portNum = machineContainer.getPortNum();

        Socket socket = new Socket(ipAddress, portNum);
        return socket;
    }



    /***************************************************
     *             Server Router Services
     ***************************************************/

    private ArrayList<MachineContainer> getServers() throws IOException {
        //set up connection to Server Router
        Socket socket = tcpServer.connectServerRouter();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        //get servers
        String message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF("Get_Server_List");
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);
        dataOutputStream.writeUTF(serverMachineInfo);
        message = dataInputStream.readUTF();
        System.out.println("ServerRouter: "+message);

        //set up list of servers
        MachineContainer machineContainer = new MachineContainer();
        ArrayList<MachineContainer> arrServers =  machineContainer.getMachineList(message);
        return arrServers;
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

        //remove client
        tcpServer.removeClient(clientMachineInfo);

        // inform Router to inform client we are disconnecting
        dataOutputStream.writeUTF("good_bye");
    }

    private void startService() throws IOException {
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
    }

    private void sendWelcomeMessage() throws IOException {
        // Create Message
        String message = "\n\nEnter number of what you want to do:\n"+
                "1) Echo\n" +
                "2) Uppercase Text File\n" +
                "3) Request File from Server\n" +
                "4) Send File to Server\n"+
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
            sendAction("send_file");
        } else if (idx == 4) {
            sendAction("request_file");
        } else if (idx == 7) {
            sendAction("good_bye");
        } else if (idx == 8) {
            /*for (ClientContainer clientContainer: arrClientContainer
            ) {
                System.out.println("Container: "+clientContainer.getIpAddy());
            }
            //back to home
            sendAction("welcome_msg");*/
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
        File file = retrieveFile(true);

        //create second file
        String fileNameRevised = file.getName();
        fileNameRevised = fileNameRevised.replace(".txt","_upper.txt");
        String directory = makeTempServerDirectory();
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

        //clean up Temp folder
        removeTempServerDirectory();

        sendAction("welcome_msg");
    }

    private void sendFileChoice() throws IOException {
        // create message
        String message = "Human!, welcome. Please look though my goods and select the file you wish to download" +
                "Once selected, the file will be compressed and sent to you. \n\n";
        sendMessage(message);

        // gather a list of files, indicate which server they are on, and send that list to the client.
        String directory = tcpServer.getLocalFolder();
        File directoryPath = new File(directory);
        String[] contents = directoryPath.list();

        StringBuilder sbMessage = new StringBuilder();

        for (int i = 0; i < contents.length; i++) {
            sbMessage.append(i+1).append(": ").append(contents[i]).append("\n");
            System.out.println("contents[i]: "+contents[i]);
        }

        sendMessage(sbMessage.toString());

        //accept choice from client
        String reply = requestReply();
        int choice = Integer.parseInt(reply) - 1;
        System.out.println("Client choose: "+choice);

        //go get the file and send
        File file = new File(directory+File.separator+contents[choice]);
        System.out.println("File: "+file.getAbsolutePath());
        sendFile(file);

        //return to welcome screen
        sendAction("welcome_msg");
    }

    private void receiveFileToServer() throws IOException {
        // create message
        String message = "Human!, thank you. I accept this gift and will add this file to my library for future downloads.";
        sendMessage(message);

        // Create File
        File file = retrieveFile(false);

        // tell user it has been saved.
        message = "File has been added to the library.";
        System.out.println("Letting client know file has been added");
        sendMessage(message);

        //return to welcome screen
        System.out.println("Sending Client to Welcome");
        sendAction("welcome_msg");
    }
}
