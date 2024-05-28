package fae;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread extends Thread{
    
    private Socket ClientConnection;
    private DataInputStream in;
    private DataOutput out;

    public ServerThread(){
        this.ClientConnection = new Socket();
    }

    public ServerThread(Socket connection) throws IOException{
        this.ClientConnection = connection;
        
    }

    public void run(){
        try {
            this.in = new DataInputStream(this.ClientConnection.getInputStream());
            this.out = new DataOutputStream(this.ClientConnection.getOutputStream());

            

            System.out.println("Conection-Thread-Success");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (this.ClientConnection != null) {
                try{
                    this.ClientConnection.close();
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            }
        }
    }
}
