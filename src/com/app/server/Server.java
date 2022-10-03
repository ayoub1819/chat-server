package com.app.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server extends Thread{
    private int clientCount;
    private boolean isActive = true;
    private List<Conversation> clients = new ArrayList<>();

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("[Server] starting ...");
            isActive = true;
            while (isActive){
                Socket socket = serverSocket.accept();
                ++clientCount;
                Conversation conversation = new Conversation(socket,clientCount);
                clients.add(conversation);
                conversation.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Conversation extends  Thread{
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private int id;
        private String username;

        public Conversation(Socket socket, int id) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(),true);
            this.id = id;
        }


        public void broadCastMsg(String msg) throws IOException {
            for (Conversation conversation : clients)
                if (conversation != this)  conversation.writer.println(this.username+" : " +msg);

        }
        public void send(String msg,int receiver) throws IOException {
            for (Conversation client : clients) {
                if(client.id == receiver){
                    client.writer.println("["+this.username+"]"+" : " +msg);
                    return;
                }
            }

            writer.println("client"+receiver+" is not available  T_T");

        }
        @Override
        public void run() {
            try {
                String ip = socket.getRemoteSocketAddress().toString();

                this.username = reader.readLine();
                System.out.println(this.username+" is connected");



                while(true){
                    String req = reader.readLine();
                    if (req == null){
                        System.out.println("deconnexion du client numero : "+id);
                        broadCastMsg(this.username+" leave the conversation");
                        clients.remove(this);
                        socket.close();
                        break;
                    }
                    System.out.println("le client num: "+id+" ip: "+ip+" envoi une requete: "+req);
                    String[] reqParams = req.split("->");
                    if (reqParams.length==1)
                        broadCastMsg(req);
                    else {
                        try {
                            int receiver = Integer.parseInt(reqParams[0]);
                            send(reqParams[1],receiver);

                        }catch (NumberFormatException e){
                            writer.println("ivalid reciver id");
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

