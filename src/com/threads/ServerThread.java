package com.threads;

import com.Main.TCPServer;
import com.classes.FileFolderContainer;
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
    private MachineContainer machineContainerServer, machineContainerClient;

    public ServerThread(Socket routerSocket, MachineContainer machineContainer, TCPServer tcpServer) throws IOException {
        this.tcpServer = tcpServer;
        this.serverMachineInfo = tcpServer.getMachineInfo();
        this.machineContainerServer = machineContainer;

        // Connect to Incoming
        System.out.println("Connection established.");

        //setup input and output stream
        dataOutputStream = new DataOutputStream(routerSocket.getOutputStream());
        dataInputStream = new DataInputStream(routerSocket.getInputStream());

        //handshake Router
        dataOutputStream.writeUTF("Hello Who is Calling?");
        String message = dataInputStream.readUTF();     //__Client__
        System.out.println("Client Said: "+message);

        //Who is Connected
        this.whoConnected = message;
    }

    //The Thread will ask who is connected and get them started with the correct interface
    public void run(){
        if (whoConnected.equals("__Client__")) {
            try {
                sendAction("login_info");
                sendAction("start_service");
            } catch (IOException e) {
                System.out.println("ServerThread - Unable to create new Client");
                e.printStackTrace();
            }
        } else if (whoConnected.equals("__Server__")) {
            try {
                System.out.println("Server");
                serverService();
            } catch (IOException e) {
                System.out.println("ServerThread - Unable to connect Server");
                e.printStackTrace();
            }
        }
    }

    /******************************************************
     *      Server File and Folder Solutions
     * ******************************************************/

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

    private ArrayList<FileFolderContainer> collectFiles() {
        //declarations
        ArrayList<FileFolderContainer> arrFileFolders = new ArrayList<FileFolderContainer>();

        //Find Files and Folders
        String directory = tcpServer.getLocalFolder();
        File directoryPath = new File(directory);
        String[] contents = directoryPath.list();

        for (int i = 0; i < Objects.requireNonNull(contents).length; i++){
            String fullPathContents = directory+File.separator+contents[i];

            // check to see if first object is a file or folder
            File fileFolder = new File(fullPathContents);
            if (fileFolder.exists()){
                // create new fileFolderContainer
                FileFolderContainer fileFolderContainer = new FileFolderContainer();
                fileFolderContainer.machineContainer = machineContainerServer;
                fileFolderContainer.serverName = machineContainerServer.getUserName();
                fileFolderContainer.isFolder = fileFolder.isDirectory();
                fileFolderContainer.location = fullPathContents;
                fileFolderContainer.fileFolderName = contents[i];
                arrFileFolders.add(fileFolderContainer);
            }
        }
        return arrFileFolders;
    }
    /***************************************************
     *             Incoming Other Servers
     ***************************************************/
    private void serverService() throws IOException {
        //declaration
        boolean doRun = true;

        while(doRun) {
            dataOutputStream.writeUTF("service_needed?");
            String message = dataInputStream.readUTF();
            System.out.println("Other Server: " + message);

            switch (message){
                case "file_list" -> sendServerFileList();
                case "send_file" -> sendRequestedFile();
                case "send_chat_rooms" -> sendChatRooms();
                case "good_bye" -> {
                    System.out.println("good bye.");
                    doRun = false;
                }
            }
        }
    }

    /***************************************************
     *             Server Request
     ***************************************************/
    private ArrayList<FileFolderContainer> getExternalFileLists() throws IOException {
        ArrayList<FileFolderContainer> arrFilesFolders = new ArrayList<FileFolderContainer>();
        ArrayList<MachineContainer> arrMachineContainers = getServers();

        if (arrMachineContainers == null) {
            return null;
        }

        for (int i = 0; i < arrMachineContainers.size(); i++){
            // set up socket
            String ipAddress = arrMachineContainers.get(i).getLocalIPAddress();
            int portNum = arrMachineContainers.get(i).getPortNum();
            Socket serverSocket = new Socket(ipAddress, portNum);
            // call server
            DataOutputStream dataOutputStreamServer = new DataOutputStream(serverSocket.getOutputStream());
            DataInputStream dataInputStreamServer = new DataInputStream(serverSocket.getInputStream());
            String msgOtherServer = dataInputStreamServer.readUTF();
            System.out.println("getFileList() - Expect: Who are you - "+msgOtherServer);
            dataOutputStreamServer.writeUTF("__Server__");
            // ask for list
            msgOtherServer = dataInputStreamServer.readUTF();
            System.out.println("getFileList() - Expect: Service Needed? - "+msgOtherServer);
            dataOutputStreamServer.writeUTF("file_list");
            //get List
            msgOtherServer = dataInputStreamServer.readUTF();
            System.out.println("getFileList() - Expect: List of files and Folders - "+msgOtherServer);

            //turn reply into arrayList
            FileFolderContainer ffc = new FileFolderContainer();
            ArrayList<FileFolderContainer> arrFFC = ffc.destringifyContainer(msgOtherServer);

            //add array list to main array list
            arrFilesFolders.addAll(arrFFC);

            //Hangup
            msgOtherServer = dataInputStreamServer.readUTF();
            System.out.println("getFileList() - Expect: Service Needed? - "+msgOtherServer);
            dataOutputStreamServer.writeUTF("good_bye");
            serverSocket.close();
        }
        return arrFilesFolders;
    }

    private File getExternalFile(String serverName, String fileLocation) throws IOException {
        ArrayList<MachineContainer> arrMachineContainers = getServers();
        MachineContainer serverMachine = null;

        // find machine and connect.
        for (int i = 0; i < arrMachineContainers.size(); i++){
           if (arrMachineContainers.get(i).getUserName().equals(serverName)){
               serverMachine = arrMachineContainers.get(i);
               break;
           }
        }

        if (serverMachine == null){
            return null;
        }

        // set up socket
        String ipAddress = serverMachine.getLocalIPAddress();
        int portNum = serverMachine.getPortNum();
        Socket serverSocket = new Socket(ipAddress, portNum);
        // call server
        DataOutputStream dataOutputStreamServer = new DataOutputStream(serverSocket.getOutputStream());
        DataInputStream dataInputStreamServer = new DataInputStream(serverSocket.getInputStream());
        String msgOtherServer = dataInputStreamServer.readUTF();
        System.out.println("getExternalFile() - Expect: Who are you - "+msgOtherServer);
        dataOutputStreamServer.writeUTF("__Server__");
        // ask for file
        msgOtherServer = dataInputStreamServer.readUTF();
        System.out.println("getExternalFile() - Expect: Service Needed? - "+msgOtherServer);
        dataOutputStreamServer.writeUTF("send_file");
        //send file location
        msgOtherServer = dataInputStreamServer.readUTF();
        System.out.println("getExternalFile() - Expect: File Location? - "+msgOtherServer);
        dataOutputStreamServer.writeUTF(fileLocation);

        // wait to receive File Name Bytes
        int fileNameBytesLength = dataInputStreamServer.readInt();

        // complete receipt of file name bytes
        byte[] fileNameBytes = new byte[fileNameBytesLength];
        dataInputStreamServer.readFully(fileNameBytes,0,fileNameBytesLength);

        // receive the File
        int fileBytesLength = dataInputStreamServer.readInt();
        byte[] fileBytes = new byte[fileBytesLength];
        dataInputStreamServer.readFully(fileBytes,0,fileBytesLength);

        // convert file name to append "_upper" at the end
        String fileName = new String(fileNameBytes);

        // Create folder for File
        String directory = makeTempServerDirectory();

        // Create File
        File file = new File(directory+File.separator+fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(fileBytes);
        fileOutputStream.close();

        return file;
    }

    /***************************************************
     *             Server Services
     ***************************************************/
    private void sendServerFileList() throws IOException {
        ArrayList<FileFolderContainer> arrFileFolder = collectFiles();
        FileFolderContainer flc = new FileFolderContainer();
        String strFileFolders = flc.stringifyContainer(arrFileFolder);
        dataOutputStream.writeUTF(strFileFolders);
    }

    private void sendRequestedFile() throws IOException {
        dataOutputStream.writeUTF("File Location?");
        String message = dataInputStream.readUTF();
        System.out.println("sendRequestedFile() - Expect: fileLoc: " + message);

        //Create File
        File file = new File(message);

        //convert file into stream
        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

        //convert file name into bytes
        String fileName = file.getName();
        byte[] fileNameBytes = fileName.getBytes();

        //convert file into bytes
        byte[] fileBytes = new byte[(int)file.length()];
        fileInputStream.read(fileBytes);
        fileInputStream.close();

        // send file name
        dataOutputStream.writeInt(fileNameBytes.length);
        dataOutputStream.write(fileNameBytes);

        //send file
        dataOutputStream.writeInt(fileBytes.length);
        dataOutputStream.write(fileBytes);
    }

    private void sendChatRooms() throws IOException {
        dataOutputStream.writeUTF(tcpServer.getChatRooms("NA"));
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
        if (message.length() > 0){
            MachineContainer machineContainer = new MachineContainer();
            ArrayList<MachineContainer> arrServers =  machineContainer.getMachineList(message);
            return arrServers;
        }
        return null;
    }

    /******************************************************
     *      Functions to Client
     * ******************************************************/
    //This action needs to exist outside Send Actions because it needs to prep a message.
    private void sendMessage(String message, boolean doConfirm) throws IOException {
        //declaration
        String msg = "";

        if (doConfirm){
            // wait for router to say Ready For Action
            msg = dataInputStream.readUTF();
            System.out.println("sendMessage - Expect: ready for action - Router says: "+msg);
        }

        // Tell Client, Server is sending a message.
        dataOutputStream.writeUTF("message_server_to_client");
        msg = dataInputStream.readUTF();
        System.out.println("sendMessage - Expect: ready to Display Message - Router: "+msg);

        // send message
        dataOutputStream.writeUTF(message);
    }

    private String requestReply() throws IOException {
        // wait for router to say Ready For Action
        String msg = dataInputStream.readUTF();
        System.out.println("requestReply - Expect: ready for action - Router says: "+msg);

        // Server tells router to get reply form client
        dataOutputStream.writeUTF("request_reply_client_to_server");
        msg = dataInputStream.readUTF();
        System.out.println("requestReply - Expect: Txt - Router: "+msg);
        return msg;
    }

    private File retrieveFile(Boolean doTemp) throws IOException {
        // wait for router to say Ready For Action
        String msg = dataInputStream.readUTF();
        System.out.println("retrieveFile - Expect: ready for action - Router says: "+msg);

        //declaration
        long timeDisplay;

        // Server tells router to send file information.
        dataOutputStream.writeUTF("file_client_to_server");

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
        // wait for router to say Ready For Action
        String msg = dataInputStream.readUTF();
        System.out.println("sendFile - Expect: ready - Router says: "+msg);

        //declaration
        long timeDisplay;

        // Server tells router to send file information.
        dataOutputStream.writeUTF("file_server_to_client");


        msg = dataInputStream.readUTF();
        System.out.println("sendFile - Expect: ready for file - Router: "+msg);

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
    private void sendAction(String action) throws IOException {     //First Run: login_info
        // wait for router to say Ready For Action
        String msg = dataInputStream.readUTF();     //Client says: Waiting for action
        System.out.println("sendAction - Router says: "+msg);

        //send Action
        switch (action) {
            case "welcome_msg" -> sendWelcomeMessage();
            case "login_info" -> newClientInfo();
            case "start_service" -> startService();
            case "start_echo" -> startEcho();
            case "upper_txt_doc" -> uppercaseTextFile();
            case "send_file" -> sendFileChoice();
            case "request_file" -> receiveFileToServer();
            case "chat_room_start" -> chatRoomStart();
            case "chat_room_join" -> chatRoomJoin();
            case "server_list" -> serverListHack();
            case "good_bye" -> goodbye();
        }
    }

    private void chatRoomJoin() throws IOException {
        //get local chatrooms
        boolean doFirstRun = true;
        String strChatRooms = tcpServer.getChatRooms(clientMachineInfo);
        StringBuilder stringBuilder = new StringBuilder();
        if (strChatRooms.length()>0){
            stringBuilder.append(strChatRooms);
            doFirstRun = false;
        }

        // get list of servers
        ArrayList<MachineContainer> arrServers = getServers();

        if (arrServers != null){
            //get list of chatroom
            for (MachineContainer server: arrServers) {
                // set up socket
                String ipAddress = server.getLocalIPAddress();
                int portNum = server.getPortNum();
                Socket serverSocket = new Socket(ipAddress, portNum);
                // call server
                DataOutputStream dataOutputStreamServer = new DataOutputStream(serverSocket.getOutputStream());
                DataInputStream dataInputStreamServer = new DataInputStream(serverSocket.getInputStream());
                String msgOtherServer = dataInputStreamServer.readUTF();
                System.out.println("chatRoomJoin() - Expect: Who are you - "+msgOtherServer);
                dataOutputStreamServer.writeUTF("__Server__");
                // ask for list
                msgOtherServer = dataInputStreamServer.readUTF();
                System.out.println("chatRoomJoin() - Expect: Service Needed? - "+msgOtherServer);
                dataOutputStreamServer.writeUTF("send_chat_rooms");
                msgOtherServer = dataInputStreamServer.readUTF();
                System.out.println("chatRoomJoin() - Expect: Stringify of Chat rooms? - "+msgOtherServer);

                if (msgOtherServer.length() > 0){
                    if(doFirstRun){
                        stringBuilder.append(msgOtherServer);
                        doFirstRun = false;
                    } else {
                        stringBuilder.append(":").append(msgOtherServer);
                    }
                }
            }
        }

        if(stringBuilder.isEmpty()){
            sendMessage("There is no one to talk to.", false);
        } else {
            //send client to chatroom
            dataOutputStream.writeUTF("client_chat_room");
            String clientMsg = dataInputStream.readUTF();
            System.out.println("chatRoomJoin() - Expect: who with? "+clientMsg);
            dataOutputStream.writeUTF(stringBuilder.toString());

            //wait for done command
            clientMsg = dataInputStream.readUTF();
            System.out.println("chatRoom() - Expect: Done "+clientMsg);
        }
    }

    private void serverListHack() throws IOException {
        getServers();
    }

    private void chatRoomStart() throws IOException {
        // load chatroom to server
        tcpServer.addChatRoom(machineContainerClient);

        //send client to chat room
        dataOutputStream.writeUTF("client_chat_room");
        String clientMsg = dataInputStream.readUTF();
        System.out.println("chatRoomStart() - Expect: who with? "+clientMsg);
        dataOutputStream.writeUTF("chat_room_create");

        //wait for done command
        clientMsg = dataInputStream.readUTF();
        System.out.println("chatRoom() - Expect: Done "+clientMsg);
        tcpServer.removeChatRoom(machineContainerClient);
    }

    //When a client is connecting, we need to get their username and computer info
    private void newClientInfo() throws IOException {
        while (true){
            //ask client for information
            dataOutputStream.writeUTF("login_info");

            //receive reply form client
            String clientInfo = dataInputStream.readUTF();
            System.out.println("newClientInfo - Expect: Comp Info - Router: "+clientInfo);

            //take client info and turn it into a container.
            machineContainerClient = tcpServer.addClient(clientInfo);

            if (machineContainerClient != null){
                clientMachineInfo = clientInfo;
                dataOutputStream.writeUTF("Success");
                break;
            } else {
                dataOutputStream.writeUTF("Fail");
                String input = dataInputStream.readUTF();
                System.out.println("newClientInfo() - fail - Expected: ready for action - Router: "+input);
            }
        }
    }

    private void startService() throws IOException {
        // send opening message
        String message = """


                Welcome to the internet
                Have a look around
                Anything that brain of yours can think of can be found
                We've got mountains of content
                Some better, some worse
                If none of it's of interest to you, you'd be the first

                 Press enter to continue.""";
        sendMessage(message, false);

        // wait for it.
        String reply = requestReply();
        System.out.println("startService - Expect: blank - Router: "+reply);

        sendWelcomeMessage();
    }

    private void sendWelcomeMessage() throws IOException {
        while(true){
            // wait for router to say Ready For Action
            String msg = dataInputStream.readUTF();     //Client says: Waiting for action
            System.out.println("sendAction - Router says: "+msg);

            // Create Message
            String message = """


                Enter number of what you want to do:
                1) Echo
                2) Uppercase Text File
                3) Request File from Server
                4) Send File to Server
                5) Start a Chatroom
                6) Join a Chatroom                
                7) Goodbye""";

            // Send message
            sendMessage(message, false);

            // wait for client reply
            msg = requestReply();
            int idx = 0;
            try{
                idx = Integer.parseInt(msg);
            } catch (Exception e) {
                System.out.println("sendWelcomeMessage() - Not an int");
            }

            if (idx == 1){
                sendAction("start_echo");
            } else if (idx == 2){
                sendAction("upper_txt_doc");
            } else if (idx == 3) {
                sendAction("send_file");
            } else if (idx == 4) {
                sendAction("request_file");
            } else if (idx == 5) {
                sendAction("chat_room_start");
            } else if (idx == 6) {
                sendAction("chat_room_join");
            } else if (idx == 7) {
                sendAction("good_bye");
            } else if (idx == 8) {
                sendAction("message_test");
            } else {
                sendMessage("Try again.",true);
            }
        }
    }

    private void startEcho() throws IOException {
        // This will echo at the client
        //declarations
        boolean doEcho = true;

        // create message
        String message = "The Server has a large hole. If you type something " +
                "it will echo back to you. But it may sound a little distorted. To " +
                "stop type in 'bye.'";
        sendMessage(message, false);

        // start loop
        while (doEcho){
            //request reply
            String msg = requestReply();

            //end loop
            if (msg.equalsIgnoreCase("BYE.")){
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

            // send message and wait for router and client to check in.
            sendMessage(msg, false);
            String checkin = dataInputStream.readUTF();
            System.out.println("Router said, Client said: "+checkin);
        }
    }

    private void uppercaseTextFile() throws IOException {
        // Upper a text file and send back
        // create message
        String message = "Please enter the location of the .txt file. Once received the " +
                "server will change all the text to uppercase and then send it back with " +
                "'_upper' appended to the file name.";
        sendMessage(message, false);

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
    }

    private void sendFileChoice() throws IOException {
        // create message
        String message = """
                Human!, welcome. Please look though my goods and select the file you wish to downloadOnce selected, the file will be compressed and sent to you.\s
                
                Type "exit" to EXIT.
                """;
        sendMessage(message, false);

        // gather a list of files, indicate which server they are on, and send that list to the client.
        ArrayList<FileFolderContainer> arrFilesFolderContainer = collectFiles();
        ArrayList<FileFolderContainer> arrExternalFilesFolders = getExternalFileLists();

        //combine arrays
        if (arrExternalFilesFolders != null){
            arrFilesFolderContainer.addAll(arrExternalFilesFolders);
        }

        StringBuilder sbMessage = new StringBuilder();

        for (int i = 0; i < arrFilesFolderContainer.size(); i++) {
            sbMessage.append(i+1).append(") ").append(arrFilesFolderContainer.get(i).getFileFolderNameDisplay()).append("\n");
        }

        String location = "";
        while (true) {
            sendMessage(sbMessage.toString(), true);

            //accept choice from client
            String reply = requestReply();
            int choice = -99;
            if (reply.equals("exit")){
                sendMessage("You chose to exit. That's fine, I didn't want to share anyway. ", true);
                return;
            } else {
                try {
                    choice = Integer.parseInt(reply) - 1;
                    int maxValue = arrFilesFolderContainer.size() - 1;
                    if (choice > maxValue){
                        choice = -99;
                    }
                } catch (Exception e) {
                    System.out.println("Client entered: "+ reply);
                }
            }
            System.out.println("Client choose: " + reply + ". Which is index "+choice);

            //if file is a folder
            if (choice == -99){
                sendMessage("You entered an invalid choice. Please try again. ", true);
            } else if (arrFilesFolderContainer.get(choice).isFolder){
                sendMessage("Sorry, we don't allow folder crawling at this time, choose again.", true);
            } else if (arrFilesFolderContainer.get(choice).serverName.equals(machineContainerServer.getUserName())){
                location = arrFilesFolderContainer.get(choice).location;
                //go get the file and send
                File file = new File(location);
                System.out.println("File: "+file.getAbsolutePath());
                sendFile(file);
                break;
            } else if (!arrFilesFolderContainer.get(choice).serverName.equals(machineContainerServer.getUserName())){
                String serverName = arrFilesFolderContainer.get(choice).serverName;
                String fileLoc = arrFilesFolderContainer.get(choice).location;
                File file = getExternalFile(serverName,fileLoc);
                System.out.println("File: "+file.getAbsolutePath());
                sendFile(file);
                break;
            }
        }
    }

    private void receiveFileToServer() throws IOException {
        // create message
        String message = "Human!, thank you. I accept this gift and will add this file to my library for future downloads.";
        sendMessage(message, false);

        // Create File
        System.out.println("Ask for file");
        retrieveFile(false);
        System.out.println("Got File");

        // tell user it has been saved.
        System.out.println("Sending Message");
        message = "File has been added to the library.";
        System.out.println("Letting client know file has been added");
        sendMessage(message, true);
        System.out.println("Message sent");

        //return to welcome screen
        System.out.println("Sending Client to Welcome");
    }

    private void goodbye() throws IOException {
        // send a message that the machine is about to disconnect.
        String message = """


                Could I interest you in everything?
                All of the time
                A little bit of everything
                All of the time
                Apathy's a tragedy
                And boredom is a crime
                Anything and everything
                And anything and everything
                And anything and everything
                And all of the time


                ********** Good Bye! **********""";

        sendMessage(message, false);

        //remove client
        tcpServer.removeClient(machineContainerClient);

        // inform Router to inform client we are disconnecting
        dataOutputStream.writeUTF("good_bye");
    }
}
