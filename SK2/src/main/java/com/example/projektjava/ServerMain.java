package com.example.projektjava;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerMain {
    public static void main(String[] args){
        //Start the server and wait for connection
        int port = 5555;
        try {
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Server server = new Server(port);
        server.start();

    }
}