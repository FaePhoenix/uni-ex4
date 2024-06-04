package fae;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONObject;



public class Client{

    private User user;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;



    public Client() throws IOException{
        this.user = new User();
    }


    public Client(String email) throws IllegalEmailFormatException, UnknownHostException, IOException{
        this.user = new User(email);
    }


    //TO-DO rebuild authentication
    public Boolean connect(int port) throws UnknownHostException, IOException{
        System.out.println("Connect to Server");
        this.socket = new Socket("localhost", port);
        System.out.println("connectionsuccessfull");
        this.in = new ObjectInputStream(this.socket.getInputStream());
        this.out = new ObjectOutputStream(this.socket.getOutputStream());

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


    public void run() throws IOException{
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.println("Please Input your desired action:");
            System.out.println("S (send data); R (request data); E (end connection); C (change password)");
            String userAction = userInput.readLine();
            switch(userAction){
                case "S":
                    this.sendData(userInput);

                case "R":
                    this.requestData(userInput);

                case "E":
                    this.endConnection();
                    System.out.println("Ended Socket-Connection");
                    break;

                case "C":
                    this.changePassword(userInput);

                default:
                    System.out.println("Could not interpret Input. Please select an available action (S;R;E;C)");

            }

        }
    }


    private void changePassword(BufferedReader userInput) throws IOException{
        System.out.println("Please enter your new password");
        String newPassword = userInput.readLine();
        
        RequestBuilder protocolBuilder = new RequestBuilder();
        JSONObject request = protocolBuilder.buildPasswordChangeProtocol(newPassword);
        out.writeObject(request);

        try {
            Object response = in.readObject();
            if (response instanceof JSONObject){
                JSONObject serverResponse = (JSONObject) response;
                if(serverResponse.getString("protocol_type") == "change_password_response"){
                    Boolean success = serverResponse.getJSONObject("protocol_body").getBoolean("change_status");
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            
            e.printStackTrace();
        }
    }

    private void sendData(BufferedReader userInput){

    }

    private void requestData(BufferedReader userInput){
        System.out.println("");

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