package com.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

public class ProcessComputerInfo {
    //declarations
    private String localIPAddress, externalIPAddress, localHostName;

    public ProcessComputerInfo(){
        // Who am I
        try{
            URL externalIP = new URL("http://checkip.amazonaws.com");
            InetAddress addr = InetAddress.getLocalHost();
            localIPAddress = addr.getHostAddress(); // Machine's IP Address
            localHostName = addr.getCanonicalHostName(); // Machine's Host Name

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(externalIP.openStream()));
            externalIPAddress = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("CompInfo - Something Majorly Wrong. Cannot find your IP address or HostName.");
        }
    }
    public String getWhoAmI() {
        String message = "Local Host Information\nHost Name: "+localHostName+"\nIP Address: "+localIPAddress+"\nExternal IP: "+externalIPAddress;
        return message;
    }
    public String getExternalIP() throws MalformedURLException {
        return externalIPAddress;
    }
    public String getLocalIPAddress() throws MalformedURLException {
        return localIPAddress;
    }
    public String getLocalHostName() throws MalformedURLException {
        return localHostName;
    }
}
