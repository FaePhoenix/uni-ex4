package fae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



public class Server {

    private ArrayList<User> users;
    private ServerSocket socket;


    
    public Server () throws IOException{
        this.socket = new ServerSocket();
    }


    public Server(int port) throws IOException{
        this.socket = new ServerSocket(port);
        this.users = new ArrayList<User>();
    }

    public void start() throws IOException{
        while (true) {
            System.out.println("Looking for connection");
            Socket clientConnection = this.socket.accept();
            System.out.println("Found Connection");
            Boolean accepted = this.authenticate(clientConnection);
            if(accepted) {
                System.out.println("Connection authenticated");
                ServerThread connection = new ServerThread(clientConnection);
                System.out.println("Starting Connection-Thread");
                connection.start();
            }
        }
    }


    private Boolean authenticate(Socket connection) throws IOException {
        DataInputStream in = new DataInputStream(connection.getInputStream());
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        
        String username = in.readUTF();
        String password = this.generatePassword(username);
        out.writeUTF(password);
        User client = new User(username);
        client.setPassword(password);
        this.users.add(client);
        return true;
    }


    private String generatePassword(String username) {
        String password = "";
        for (char c : username.toCharArray()) {
            int charNum = (int) c;
            char nChar = (char)(charNum * Math.pow(3, charNum) % 128);
            password += nChar;
        }
        return password;
    }




    @Override
    public void finalize(){
        try{
            this.socket.close();
        } catch(IOException exception){}
        
    }
}
