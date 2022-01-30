package com.example.projektjava;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread extends Thread {
    private Socket socket;
    private String myUsername;
    private Server server;
    File f;
    private OutputStream output;

    public ServerThread(Server server, Socket socket) {
        this.server=server;
        this.socket=socket;
    }

    public String getMyUsername(){
        return myUsername;
    }

    public void sendMsg(PrintWriter writer, String text){
        writer.println(text);
        writer.flush();
    }



    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            output = socket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output, true);

            String line;
            while((line = reader.readLine())!=null){
                System.out.println(line);
                String[] tokens = line.split(" ",3);
                if(tokens.length > 0){
                    String cmd = tokens[0];
                    if("quit".equalsIgnoreCase(cmd)) {
                        break;
                    }
                    else if("log".equalsIgnoreCase(cmd)){
                        String path = "users\\" + tokens[1] + ".txt";
                        File f = new File(path);
                        if(f.exists()){
                            BufferedReader br = new BufferedReader(new  FileReader(f));
                            if(Objects.equals(br.readLine(), tokens[2])){
                                sendMsg(writer, "pass");
                                myUsername=tokens[1];
                            }else{
                                sendMsg(writer, "wrong");
                            }
                            br.close();
                        }else{
                            sendMsg(writer, "dont exist");
                        }
                    }
                    else if("sign".equalsIgnoreCase(cmd)){
                        String path = "users\\" + tokens[1] + ".txt";
                        File f = new File(path);
                        if(f.createNewFile()){
                            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                            bw.write(tokens[2]+"\n");
                            bw.close();
                            sendMsg(writer, "pass");
                        }else{
                            sendMsg(writer, "exist");
                        }
                    }
                    else if("add".equalsIgnoreCase(cmd)){
                        String path = "users\\" + myUsername + ".txt";
                        String fpath = "users\\" + tokens[1] + ".txt";
                        File f = new File(path);
                        File ff = new File(fpath);
                        boolean noFriendFlag = true;
                        if(Objects.equals(myUsername, tokens[1])){
                            sendMsg(writer,"me");
                        }
                        else if(ff.exists()){
                            BufferedReader br = new BufferedReader(new  FileReader(f));
                            br.readLine();
                            String users;
                            while((users = br.readLine())!=null) {
                                if (Objects.equals(users, tokens[1])) {
                                    noFriendFlag = false;
                                }
                                System.out.println(users);
                            }
                            br.close();
                            if(noFriendFlag){
                                BufferedWriter bw = new BufferedWriter(new FileWriter(f,true));
                                bw.write(tokens[1]+"\n");
                                bw.close();
                                sendMsg(writer, "pass");
                            }else{
                                sendMsg(writer, "already a friend");
                            }
                        }else{
                            sendMsg(writer,"dont exist");
                        }
                    }
                    else if("delete".equalsIgnoreCase(cmd)){
                        File inputFile = new File("users\\" + myUsername + ".txt");
                        File tempFile = new File("users\\" + "temp" + ".txt");
                        BufferedReader br = new BufferedReader(new FileReader(inputFile));
                        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
                        boolean wasFlag = false;
                        String currentLine;
                        bw.write(br.readLine() + System.getProperty("line.separator"));
                        while ((currentLine = br.readLine()) != null) {
                            if (currentLine.equals(tokens[1])) {
                                wasFlag = true;
                                continue;
                            }
                            bw.write(currentLine + System.getProperty("line.separator"));
                        }
                        if(wasFlag){
                            sendMsg(writer,"pass");
                        }else{
                            sendMsg(writer,"wrong");
                        }
                        br.close();
                        bw.close();
                        inputFile.delete();
                        tempFile.renameTo(inputFile);
                    }
                    else if("show".equalsIgnoreCase(cmd)){
                        String path = "users\\" + myUsername + ".txt";
                        File f = new File(path);
                        BufferedReader br = new BufferedReader(new  FileReader(f));
                        br.readLine();
                        String users;
                        while((users = br.readLine())!=null) {
                            sendMsg(writer,users);
                            System.out.println(users);
                        }
                        sendMsg(writer,myUsername);
                        br.close();

                    }
                    else if("send".equalsIgnoreCase(cmd)){
                        //wyslij wiadomosc
                    }
                    else if("recive".equalsIgnoreCase(cmd)){
                        //wyslij wiadomosc do klienta
                    }
                    else if("send".equalsIgnoreCase(cmd)){
                        //wyslij wiadomosc do innego watku
                    } else{
                        String msg = "unknown " + cmd + "\n";
                        output.write(msg.getBytes());
                    }

                }
            }
            server.removeThread(this);
            System.out.println(server.getThreadList().size());
            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
