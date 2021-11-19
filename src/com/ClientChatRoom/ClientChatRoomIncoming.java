package com.ClientChatRoom;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientChatRoomIncoming extends Thread{
    //declarations
    private ClientChatRoom ccr;
    private DataInputStream dataInputStream;

    public ClientChatRoomIncoming(Socket socket, ClientChatRoom ccr) throws IOException {
        this.ccr = ccr;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
    }

    // The purpose of this is to listen for any incoming messages. This allows the chat to keep going while the
    //current client is still typing.

    public void run(){
        try {
            boolean doRun = true;
            while (doRun){
                String message = dataInputStream.readUTF();
                if (message.equals("___good_bye___")){
                    doRun = false;
                } else if (message.equals("___Client_wants_to_send_file___")) {
                    dataInputStream.close();
                    System.out.println("press any key to Continue?...");
                    ccr.doReceiveFile(true);
                    doRun = false;
                } else if (message.equals("___receive_file___")){
                    doRun = false;
                } else {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
