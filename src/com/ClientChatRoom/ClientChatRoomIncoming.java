package com.ClientChatRoom;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientChatRoomIncoming extends Thread{
    //declarations
    private DataInputStream dataInputStream;

    public ClientChatRoomIncoming(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public void run(){
        try {
            while (true){
                String message = dataInputStream.readUTF();
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
