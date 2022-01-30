package com.example.projektjava;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerMain {
    public static void main(String[] args){
        //Start the server and wait for connection
        int port = 5555;
        Server server = new Server(port);
        server.start();

    }
}