package com.example.projektjava;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Objects;


public class Client extends Application{

    OutputStream out;
    InputStream in;
    Socket socket;
    private String myUsername;
    private BufferedReader bufferedIn;
    private String IP;

    public void sendMsg(String text){
        try {
            out.write((text+"\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String reciveMsg(){
        try {
            return bufferedIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void runClient(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            System.out.println("Connected to server ...");
            in = socket.getInputStream();
            out = socket.getOutputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logging(String username, String password){
        boolean passedLogging = false;
        sendMsg("log "+username+" "+password);
        String respons = reciveMsg();
        System.out.println(respons);
        if("pass".equalsIgnoreCase(respons)){
            passedLogging = true;
        }
        else if("wrong".equalsIgnoreCase(respons)){
            loggingScene("Wrong password");
        }else{
            loggingScene("Wrong username");
        }
        if(passedLogging){
            myUsername = username;
            loggedInScene();
        }
    }
    public void signing(String username, String password, String passwordConf){
        boolean passedSigning = false;
        if(!Objects.equals(username, "") && !Objects.equals(password, "")){
            if(Objects.equals(passwordConf, password)){
                sendMsg("sign "+username+" "+password);
                String respons = reciveMsg();
                System.out.println(respons);
                if("pass".equalsIgnoreCase(respons)){
                    passedSigning = true;
                }
                else{
                    signingScene("Username already taken");
                }
            }
            else{signingScene("Diffrent passwords");}
        }
        else{signingScene("All fields must be filled");}
        if(passedSigning){
            myUsername = username;
            sendMsg("quit");
            runClient(IP,5555);
            welcomeScene();
        }
    }
    private void addFriend(String username) {
        sendMsg("add "+ username);
        String respons = reciveMsg();
        System.out.println(respons);
        if("me".equalsIgnoreCase(respons)){
            adddingScene("Well, of course I know him. He's me.");
        } else if("already a friend".equalsIgnoreCase(respons)){
            adddingScene("Already a friend");
        }else if("dont exist".equalsIgnoreCase(respons)){
            adddingScene("User dosen't exist");
        }else if("pass".equalsIgnoreCase(respons)){
            loggedInScene();
        }
    }
    private void deleteFriend(String username){
        sendMsg("delete "+ username);
        String respons = reciveMsg();
        System.out.println(respons);
        if("pass".equalsIgnoreCase(respons)){
            loggedInScene();
        } else if("wrong".equalsIgnoreCase(respons)){
            deletingScene("Not a friend");
        }
    }

    Stage window;

    public static void main(String[] args) {
        launch(args);
    }

    public void closeProgram(){
        sendMsg("quit");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Exiting program!");
        window.close();
    }
    public ChoiceBox<String> getFriends(){
        ChoiceBox<String> friends = new ChoiceBox<>();
        sendMsg("show");
        String friend;
        while((friend = reciveMsg())!=null){
            if(friend.equals(myUsername)){
                break;
            }
            friends.getItems().add(friend);
        }
        return friends;
    }
    public void getLogs(TextArea textArea,String user){
        sendMsg("getFile "+user);
        String line;
        while((line = reciveMsg())!=null){
            if(line.equals(myUsername)){
                break;
            }
            textArea.appendText(line+"\n");
        }
    }

    public void welcomeScene(){
        Label label = new Label();
        label.setText("Witaj w Chatu Chatu!");

        Button button1 = new Button("Log in");
        button1.setOnAction(e -> loggingScene(""));

        Button button2 = new Button("Sign up");
        button2.setOnAction(e -> signingScene(""));

        Button buttonExit = new Button("Exit");
        buttonExit.setOnAction(e -> closeProgram());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, button1, button2, buttonExit);
        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 500);
        window.setScene(scene1);
    }
    public void signingScene(String version){
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label nameLabel = new Label("Username:");
        GridPane.setConstraints(nameLabel, 0, 0);
        TextField nameField = new TextField();
        GridPane.setConstraints(nameField,1,0);
        Label passConfLabel = new Label("Confirm Password:");
        GridPane.setConstraints(passConfLabel, 0, 2);
        TextField passConfField = new TextField();
        GridPane.setConstraints(passConfField,1,2);
        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 1);
        TextField passField = new TextField();
        GridPane.setConstraints(passField,1,1);
        Label errorLabel = new Label(version);
        GridPane.setConstraints(errorLabel, 1, 4);

        Button button1 = new Button("Sign up");
        GridPane.setConstraints(button1, 1, 3);
        button1.setOnAction(e -> signing(nameField.getText(),passField.getText(),passConfField.getText()));

        Button button2 = new Button("Go Back");
        GridPane.setConstraints(button2, 2, 3);
        button2.setOnAction(e -> welcomeScene());

        grid.getChildren().addAll(nameLabel,nameField,passLabel,passField,passConfField,passConfLabel,button1,button2,errorLabel);
        grid.setAlignment(Pos.CENTER);

        Scene scene = new Scene(grid, 500, 500);
        window.setScene(scene);
    }
    public void loggingScene(String version){
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setHgap(10);
        grid.setVgap(8);

        Label nameLabel = new Label("Username:");
        GridPane.setConstraints(nameLabel, 0, 0);
        TextField nameField = new TextField();
        GridPane.setConstraints(nameField,1,0);
        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 1);
        TextField passField = new TextField();
        GridPane.setConstraints(passField,1,1);
        Label errorLabel = new Label(version);
        GridPane.setConstraints(errorLabel, 1, 3);

        Button button1 = new Button("Log in");
        GridPane.setConstraints(button1, 1, 2);
        button1.setOnAction(e -> logging(nameField.getText(),passField.getText()));

        Button button2 = new Button("Go Back");
        GridPane.setConstraints(button2, 2, 2);
        button2.setOnAction(e -> welcomeScene());

        grid.getChildren().addAll(nameLabel,nameField,passLabel,passField,button1,button2,errorLabel);
        grid.setAlignment(Pos.CENTER);

        Scene scene = new Scene(grid, 500, 500);
        window.setScene(scene);
    }
    public void adddingScene(String version){
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setHgap(10);
        grid.setVgap(8);

        Label nameLabel = new Label("Username of a friend:");
        GridPane.setConstraints(nameLabel, 0, 0);
        TextField nameField = new TextField();
        GridPane.setConstraints(nameField,1,0);
        Label errorLabel = new Label(version);
        GridPane.setConstraints(errorLabel, 1, 2);

        Button button1 = new Button("Add");
        GridPane.setConstraints(button1, 1, 1);
        button1.setOnAction(e -> addFriend(nameField.getText()));

        Button button2 = new Button("Go Back");
        GridPane.setConstraints(button2, 2, 1);
        button2.setOnAction(e -> loggedInScene());

        grid.getChildren().addAll(nameLabel,nameField,button1,button2,errorLabel);
        grid.setAlignment(Pos.CENTER);

        Scene scene = new Scene(grid, 500, 500);
        window.setScene(scene);
    }
    public void deletingScene(String version){
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setHgap(10);
        grid.setVgap(8);

        Label nameLabel = new Label("Username of a friend:");
        GridPane.setConstraints(nameLabel, 0, 0);
        TextField nameField = new TextField();
        GridPane.setConstraints(nameField,1,0);
        Label errorLabel = new Label(version);
        GridPane.setConstraints(errorLabel, 1, 2);

        Button button1 = new Button("Delete");
        GridPane.setConstraints(button1, 1, 1);
        button1.setOnAction(e -> deleteFriend(nameField.getText()));

        Button button2 = new Button("Go Back");
        GridPane.setConstraints(button2, 2, 1);
        button2.setOnAction(e -> loggedInScene());

        grid.getChildren().addAll(nameLabel,nameField,button1,button2,errorLabel);
        grid.setAlignment(Pos.CENTER);

        Scene scene = new Scene(grid, 500, 500);
        window.setScene(scene);
    }
    public void preChatScene(){
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setHgap(10);
        grid.setVgap(8);

        ChoiceBox<String> choiceBox = getFriends();
        GridPane.setConstraints(choiceBox, 0, 0);

        Button button1 = new Button("Go chat!");
        GridPane.setConstraints(button1, 0, 1);
        button1.setOnAction(e -> chatScene(choiceBox.getValue()));

        Button button2 = new Button("Go Back");
        GridPane.setConstraints(button2, 1, 1);
        button2.setOnAction(e -> loggedInScene());

        grid.getChildren().addAll( button1, button2, choiceBox);
        grid.setAlignment(Pos.CENTER);

        Scene scene = new Scene(grid, 500, 500);
        window.setScene(scene);
    }
    public void chatScene(String user){
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setHgap(10);
        grid.setVgap(8);

        Label nameLabel = new Label(user);
        GridPane.setConstraints(nameLabel, 0, 0);
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefSize(300,400);
        GridPane.setConstraints(textArea,0,1);
        getLogs(textArea, user);
        TextField textField = new TextField();
        GridPane.setConstraints(textField,0,2);

        Button button1 = new Button("Send");
        GridPane.setConstraints(button1, 1, 1);
        button1.setOnAction(e -> {
            sendMsg("send to " + textField.getText());
            textArea.appendText(myUsername + ": " + textField.getText() + "\n");
        });
        sendMsg("onChatWith "+user);

        ClientThread clientThread = new ClientThread(user,bufferedIn,textArea);
        clientThread.start();

        Button button2 = new Button("Go Back");
        GridPane.setConstraints(button2, 2, 1);
        button2.setOnAction(e -> {
            sendMsg("exitChat");
            clientThread.end=true;
            loggedInScene();
        });

        grid.getChildren().addAll(nameLabel,textArea,button1,button2,textField);
        grid.setAlignment(Pos.BASELINE_CENTER);

        Scene scene = new Scene(grid, 500, 500);
        window.setScene(scene);
    }
    public void loggedInScene(){

        Label label = new Label();
        label.setText("Witaj "+ myUsername + "! Co chcesz zrobić?");

        Button button1 = new Button("Chat");
        button1.setOnAction(e -> preChatScene());

        Button button2 = new Button("Add friend");
        button2.setOnAction(e -> adddingScene(""));

        Button button3 = new Button("Delete friend");
        button3.setOnAction(e -> deletingScene(""));

        Button button4 = new Button("Log out");
        button4.setOnAction(e -> {sendMsg("quit");runClient(IP, 5555);welcomeScene();});

        Button buttonExit = new Button("Exit");
        buttonExit.setOnAction(e -> closeProgram());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, button1, button2, button3, button4, buttonExit);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 500, 500);
        window.setScene(scene);
    }
    public void startingScene(){
        window.setTitle("Chatu Chatu");
        window.setOnCloseRequest(e -> closeProgram());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setHgap(10);
        grid.setVgap(8);

        Label ipLabel = new Label("Server IP:");
        GridPane.setConstraints(ipLabel, 0, 0);
        TextField ipField = new TextField();
        GridPane.setConstraints(ipField,1,0);

        Button button1 = new Button("Connect");
        GridPane.setConstraints(button1, 1, 2);
        button1.setOnAction(e -> {
            runClient(IP = ipField.getText(), 5555);
            welcomeScene();
        });

        grid.getChildren().addAll(ipLabel,ipField,button1);
        grid.setAlignment(Pos.CENTER);

        Scene scene = new Scene(grid, 500, 500);
        window.setScene(scene);
        window.show();
    }


    @Override
    public void start(Stage stage){
        window=stage;
        startingScene();
    }


}