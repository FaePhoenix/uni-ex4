package fae;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        System.out.println("Connect to Server");
        this.socket = new Socket("localhost", port);
        System.out.println("connectionsuccessfull");
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());

        //Authenticate
        System.out.println("sending username");
        this.out.writeUTF(this.user.getUsername());
        
        System.out.println("Please enter the password you recieves via email:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String password = reader.readLine();
        this.out.writeUTF(password);
        Boolean passwordCorrect = this.in.readBoolean();

        if (passwordCorrect){
            System.out.println("Client-Success for: " + this.user.getUsername());
            this.user.setPassword(password);
            return true;
        } else{
            System.out.println("Pasword wrong. Terminating");
            return false;
        }  
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