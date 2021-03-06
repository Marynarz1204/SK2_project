package com.example.projektjava;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Server extends Thread{
    private final int serverPort;
    public ReentrantLock mutexUsers = new ReentrantLock();
    public ReentrantLock mutexLogs = new ReentrantLock();

    private final ArrayList<ServerThread> threadList = new ArrayList<>();

    public Server(int port) {
        serverPort = port;
    }

    public List<ServerThread> getThreadList(){
        return threadList;
    }

    public void removeThread(ServerThread s){
        threadList.remove(s);
    }

    public void run(){
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {

            System.out.println("Server is listening on port " + serverPort);

            while (true) {
                if(threadList.size()<10) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected");
                    ServerThread serverThread = new ServerThread(this, socket);
                    threadList.add(serverThread);
                    serverThread.start();
                }
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
