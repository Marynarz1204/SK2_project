package com.example.projektjava;

import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientThread extends Thread{

    String user;
    BufferedReader bufferedIn;
    public boolean end = false;
    TextArea textArea;

    public ClientThread(String user, BufferedReader bufferedIn, TextArea textArea){
        this.user=user;
        this.bufferedIn=bufferedIn;
        this.textArea=textArea;
    }

    private String reciveMsg(){
        try {
            return bufferedIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void run(){
        String text;
        do {
            text = reciveMsg();
            textArea.appendText(user + ": " + text+"\n");
            System.out.println(text);
        } while (!text.equals("exit") && !end);
    }

}
