package com.app.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public Client(Socket socket,String username) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(),true);
        this.username = username;
    }

    public void send(){
        writer.println(username);
        Scanner scanner = new Scanner(System.in);
        while (socket.isConnected()){
            String message = scanner.nextLine();
            writer.println(message);
        }
    }
    public void listen()  {
        String message = "";
        while (socket.isConnected()){
            try {
                message = reader.readLine();
                System.out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void recieve(){
        Thread listener = new Thread(this::listen);
        listener.start();
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your username :\n>");
        String username = sc.nextLine();
        Socket socket = new Socket("localhost",1234);
        Client client = new Client(socket,username);
        client.recieve();
        client.send();
    }
}
