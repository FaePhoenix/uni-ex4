package fae;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
            System.out.println("S (send data); R (request data); C (change password); E (end connection)");
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
                    System.out.println("Could not interpret Input. Please select an available action (S;R;C;E)");
            }

        }
    }


    private void changePassword(BufferedReader userInput) throws IOException{

        //Get new Password from user
        System.out.println("Please enter your new password");
        String newPassword = userInput.readLine();
        
        //Send server the changing request
        RequestBuilder protocolBuilder = new RequestBuilder();
        JSONObject request = protocolBuilder.buildPasswordChangeProtocol(newPassword);
        out.writeObject(request);

       //Get server response
       Object response;
        try {
            response = in.readObject();
        } catch (ClassNotFoundException e) { 
            e.printStackTrace();
            return;
        }

        //Check socket problems
        if (!(response instanceof JSONObject)){
            System.out.println("Got answer in wrong format back. Changing Password failed. Please try again");
            System.out.println("Got Objectt of instance: " + response.getClass());
            return;
        }

        //Check response type
        JSONObject serverResponse = (JSONObject) response;
        if(serverResponse.getString("protocol_type") != "change_password_response"){ 
            System.out.println("Got wrong protocol back. Please try again");
            System.out.println("Got protocol of type: " + serverResponse.getString("protocol_type"));  
        }

        //Check success of password change
        Boolean success = serverResponse.getJSONObject("protocol_body").getBoolean("change_status");
        if(!success){
            System.out.println("Password change was not successfull. Please try again");
            System.out.println("Changing password failed bc: " + serverResponse.getString("reason"));
            return;
        }

        //Set new password
        System.out.println("Successfully changed password");
        this.user.setPassword(newPassword);
        return;
    }


    private void sendData(BufferedReader userInput) throws IOException{
        
        //Get filename from user
        System.out.println("Please enter the name of the file you want to send");
        String fileName = userInput.readLine();
        
        //Check if file exists
        FileHelper helper = new FileHelper();
        if (!helper.isValidFile(fileName)){
            return;
        }

        //Send server the data
        RequestBuilder protocolBuilder = new RequestBuilder();
        JSONObject request = protocolBuilder.buildDataSendProtocol(fileName);
        out.writeObject(request);
    }


    private void requestData(BufferedReader userInput) throws IOException{
        RequestBuilder protocolBuilder = new RequestBuilder();
        out.writeObject(protocolBuilder.buildEntriesRequestProtocol());

        try {
            Object response = in.readObject();
            if (response instanceof JSONObject) {
                JSONObject serverResponse = (JSONObject) response;
                if (serverResponse.getString("protocol_type") == "entries_list") {
                    JSONObject responseBody = serverResponse.getJSONObject("protocol_body");
                    int amount = responseBody.getInt("amount");

                    if (amount == 0) {
                        System.out.println("Server has no saved entries. Can't request Data");
                    } else {
                        
                        ArrayList<String> entries = new ArrayList<String>();
                        for (int idx = 1; idx <= amount; idx++){
                            String entryName = "data_entry_" + idx;
                            entries.add(responseBody.getString(entryName));
                        }
                        System.out.println("Please type one of the available entries you want to request:");
                        for (String entry : entries){
                            System.out.println(entry);
                        }
                        String requestedEntryName = userInput.readLine();
                        if (entries.contains(requestedEntryName)){

                            this.requestData(requestedEntryName);

                        } else {
                            System.out.println("Given name is not in the presented available entries. Please try again");
                            System.out.println("Got name: " + requestedEntryName);
                        }
                    }
                } else {
                    System.out.println("Got wrong protocol back. Please try again");
                    System.out.println("Got protocol of type: " + serverResponse.getString("protocol_type"));
                }
            } else {
                System.out.println("Got answer in wrong format back. Changing Password failed. Please try again");
                System.out.println("Got Objectt of instance: " + response.getClass());
            }
        } catch(ClassNotFoundException exception){
            exception.printStackTrace();
        }
        
    }


    private void requestData(String entryName) throws IOException {
        RequestBuilder protocolBuilder = new RequestBuilder();
        JSONObject request = protocolBuilder.buildDataRequestProtocol(entryName);

        out.writeObject(request);

        try {
            Object response = in.readObject();
            if (response instanceof JSONObject) {
                JSONObject serverResponse = (JSONObject) response;
                if (serverResponse.getString("protocol_type") == "send_data") {
                    JSONObject responseBody = serverResponse.getJSONObject("protocol_body");
                    JSONObject dataBody = responseBody.getJSONObject("data_body");

                    FSUGenBank entry = new FSUGenBank(dataBody);
                    String saveLocation = "txtfiles/" + entryName + ".txt";
                    entry.saveToFile(saveLocation);

                    System.out.println("Successfully saved requested data as: " + saveLocation);

                    
                } else {
                    System.out.println("Got wrong protocol back. Please try again");
                    System.out.println("Got protocol of type: " + serverResponse.getString("protocol_type")); 
                }
            } else {
                System.out.println("Got answer in wrong format back. Changing Password failed. Please try again");
                System.out.println("Got Objectt of instance: " + response.getClass());
            }
        } catch(ClassNotFoundException exception){
            exception.printStackTrace();
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