package com.threads;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThreadListen {
    //declarations
    private DataInputStream dataInputStream;

    public ClientThreadListen(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public void run() throws IOException {
        boolean doRun = true;
        while(doRun){
            String message = dataInputStream.readUTF();
            System.out.println(message);
            if (message.equals("good_bye")) {
                doRun = false;
            }
        }
    }
}
