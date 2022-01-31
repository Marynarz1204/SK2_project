package com.example.projektjava;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread extends Thread {
    private final Socket socket;
    private String myUsername;
    private final Server server;
    public String onChatWithUsername;
    public PrintWriter writer;
    public OutputStream output;
    private final ReentrantLock mutexUsers;
    private final ReentrantLock mutexLogs;

    public ServerThread(Server server, Socket socket) {
        this.server=server;
        this.socket=socket;
        mutexUsers = server.mutexUsers;
        mutexLogs = server.mutexLogs;
    }

    public String getMyUsername(){
        return myUsername;
    }

    public void sendMsg(PrintWriter writer, String text){
        writer.println(text);
        writer.flush();
    }

    public void saveToFile(String text, String file1, String file2){
        int compare = file1.compareTo(file2);
        String filename;
        if(compare >= 0){
            filename = file1 + "_" + file2;
        }
        else{
            filename = file2 + "_" + file1;
        }
        File f = new File("logs\\" + filename + ".txt");
        mutexLogs.lock();
        try {
            if(f.createNewFile()){
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(myUsername + ": " + text+"\n");
                bw.close();
            }
            else{
                BufferedWriter bw = new BufferedWriter(new FileWriter(f,true));
                bw.write(myUsername + ": " + text+"\n");
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mutexLogs.unlock();
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            output = socket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             writer = new PrintWriter(output, true);

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
                            mutexUsers.lock();
                            BufferedReader br = new BufferedReader(new  FileReader(f));
                            if(Objects.equals(br.readLine(), tokens[2])){
                                sendMsg(writer, "pass");
                                myUsername=tokens[1];
                            }else{
                                sendMsg(writer, "wrong");
                            }
                            br.close();
                            mutexUsers.unlock();
                        }else{
                            sendMsg(writer, "dont exist");
                        }

                    }
                    else if("sign".equalsIgnoreCase(cmd)){
                        String path = "users\\" + tokens[1] + ".txt";
                        File f = new File(path);
                        if(f.createNewFile()){
                            mutexUsers.lock();
                            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                            bw.write(tokens[2]+"\n");
                            bw.close();
                            sendMsg(writer, "pass");
                            mutexUsers.unlock();
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
                            mutexUsers.lock();
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
                            mutexUsers.unlock();
                        }else{
                            sendMsg(writer,"dont exist");
                        }
                    }
                    else if("delete".equalsIgnoreCase(cmd)){
                        File inputFile = new File("users\\" + myUsername + ".txt");
                        File tempFile = new File("users\\" + "temp" + ".txt");
                        mutexUsers.lock();
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
                        mutexUsers.unlock();
                    }
                    else if("show".equalsIgnoreCase(cmd)){
                        String path = "users\\" + myUsername + ".txt";
                        File f = new File(path);
                        mutexUsers.lock();
                        BufferedReader br = new BufferedReader(new  FileReader(f));
                        br.readLine();
                        String users;
                        while((users = br.readLine())!=null) {
                            sendMsg(writer,users);
                            System.out.println(users);
                        }
                        sendMsg(writer,myUsername);
                        br.close();
                        mutexUsers.unlock();
                    }
                    else if("onChatWith".equalsIgnoreCase(cmd)){
                        onChatWithUsername = tokens[1];
                    }
                    else if("exitChat".equalsIgnoreCase(cmd)){
                        onChatWithUsername = null;
                    }
                    else if("send".equalsIgnoreCase(cmd)){
                        saveToFile(tokens[2], myUsername, onChatWithUsername);
                        for(int i = 0; i<server.getThreadList().size();i++){
                            if(Objects.equals(onChatWithUsername, server.getThreadList().get(i).getMyUsername())){
                                if(Objects.equals(server.getThreadList().get(i).onChatWithUsername, myUsername)){
                                    sendMsg(server.getThreadList().get(i).writer, tokens[2]);
                                }
                            }
                        }
                    }
                    else if("getFile".equalsIgnoreCase(cmd)){
                        int compare = myUsername.compareTo(tokens[1]);
                        String filename;
                        if(compare >= 0){
                            filename = myUsername + "_" + tokens[1];
                        }
                        else{
                            filename = tokens[1] + "_" + myUsername;
                        }
                        String path = "logs\\" + filename + ".txt";
                        File f = new File(path);
                        mutexLogs.lock();
                        f.createNewFile();
                        BufferedReader br = new BufferedReader(new  FileReader(f));
                        String lines;
                        while((lines = br.readLine())!=null) {
                            sendMsg(writer,lines);
                        }
                        sendMsg(writer,myUsername);
                        br.close();
                        mutexLogs.unlock();
                    }
                    else{
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
