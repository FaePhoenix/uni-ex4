package fae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;



public class Client{

    private User user;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;



    public Client() throws IOException{
        this.user = new User();
    }


    public Client(String email) throws IllegalEmailFormatException, UnknownHostException, IOException{
        this.user = new User(email);
    }

    public Boolean connect(int port) throws UnknownHostException, IOException{
        //Connect to Server
        this.socket = new Socket("localhost", port);
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());

        //Authenticate
        System.out.println("sending username");
        this.out.writeUTF(this.user.getUsername());
        String password = this.in.readUTF();
        System.out.println("recieved password");
        this.user.setPassword(password);
        

        System.out.println("Client-Success for: " + this.user.getUsername());
        return true;
    }



    public void endConnection(){
        if (this.socket != null){
            try{
                socket.close();
            } catch (IOException exception){
                exception.printStackTrace();
            }
        }
    }



    @Override
    public void finalize(){
        try{
            this.socket.close();
        } catch(IOException exception){}
        
    }
}