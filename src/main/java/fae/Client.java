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
    private BufferedReader userInput;
    private ObjectInputStream in;
    private ObjectOutputStream out;


    public Client() throws IOException{
        this.user = new User();
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
    }


    public Client(String email) throws IllegalEmailFormatException, UnknownHostException, IOException{
        this.user = new User(email);
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
    }


    public Boolean connect(int port) throws UnknownHostException, IOException{

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Build socket connection and object-streams
        this.socket = new Socket("localhost", port);
        this.in = new ObjectInputStream(this.socket.getInputStream());
        this.out = new ObjectOutputStream(this.socket.getOutputStream());

        //Send server greeting
        JSONObject greeting = protocolBuilder.buildFirstContactProtocol(this.user.getUsername());
        out.writeObject(greeting);

        //Get password from user and send to server
        System.out.println("Please enter the password you recieved via email:");
        String password = this.userInput.readLine();
        JSONObject pwdConfirmation = protocolBuilder.buildPasswordConfirmationProtocol(password);
        out.writeObject(pwdConfirmation);

        //Check success of handshake
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "authenticate_response");
        Boolean success = serverResponse.getJSONObject("protocol_body").getBoolean("handshake_status");
        if(!success) {
            System.out.println("Pasword wrong. Terminating");
            return false;
        }

        //Setting password
        System.out.println("Client-Success for: " + this.user.getUsername());
        this.user.setPassword(password);
        return true;
    }


    public void run() throws IOException{
        while(true){
            System.out.println("Please Input your desired action:");
            System.out.println("S (send data); R (request data); C (change password); E (end connection)");
            String userAction = this.userInput.readLine();
            switch(userAction){
                case "S":
                    this.sendData(userInput);

                case "R":
                    this.handleRequestData(userInput);

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

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Get new Password from user
        System.out.println("Please enter your new password");
        String newPassword = this.userInput.readLine();
        
        //Send server the changing request
        JSONObject request = protocolBuilder.buildPasswordChangeProtocol(newPassword);
        out.writeObject(request);

        //Check success of password change
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "change_password_response");
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

        //Build helper
        RequestBuilder protocolBuilder = new RequestBuilder();

        //Get filename from user
        System.out.println("Please enter the name of the file you want to send");
        String fileName = userInput.readLine();
        
        //Check if file exists
        FileHelper helper = new FileHelper();
        if (!helper.isValidFile(fileName)){
            return;
        }

        //Send server the data
        JSONObject request = protocolBuilder.buildDataSendProtocol(fileName);
        out.writeObject(request);
    }


    private void handleRequestData(BufferedReader userInput) throws IOException{

        //Get available entries and end when not available
        ArrayList<String> entries = requestEntries();
        if (entries == null || entries.size() == 0) {
            return;
        }
        
        //Get user selection from entries
        System.out.println("Please type one of the available entries you want to request:");
        for (String entry : entries){
            System.out.println(entry);
        }
        String requestedEntryName = this.userInput.readLine();

        //Check if selected entry is valid
        if (!entries.contains(requestedEntryName)){
            System.out.println("Given name is not in the presented available entries. Please try again");
            System.out.println("Got name: " + requestedEntryName);
            return;
        }
           
        //request valid data from server
        this.requestData(requestedEntryName);
    }


    private ArrayList<String> requestEntries() throws IOException{

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Get available entries
        out.writeObject(protocolBuilder.buildEntriesRequestProtocol());
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "entries_list");

        //Extract protocol-body
        JSONObject responseBody = serverResponse.getJSONObject("protocol_body");
        int amount = responseBody.getInt("amount");

        //Check for empty entries list
        if (amount == 0) {
            System.out.println("Server has no saved entries. Can't request data yet");
            return new ArrayList<String>();
        }
        
        //Extract entries
        ArrayList<String> entries = new ArrayList<String>();
        for (int idx = 1; idx <= amount; idx++){
            String entryName = "data_entry_" + idx;
            entries.add(responseBody.getString(entryName));
        }
        
        return entries;
    }


    private void requestData(String entryName) throws IOException {

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Request data from server and recieve
        JSONObject request = protocolBuilder.buildDataRequestProtocol(entryName);
        out.writeObject(request);
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "send_data");

        //Extract data from protocol
        JSONObject responseBody = serverResponse.getJSONObject("protocol_body");
        JSONObject dataBody = responseBody.getJSONObject("data_body");
        FSUGenBank entry = new FSUGenBank(dataBody);

        //Save recieved information to file
        String saveLocation = "txtfiles/" + entryName + ".txt"; //TO-DO Look at implementation of entryNames
        entry.saveToFile(saveLocation);
        System.out.println("Successfully saved requested data as: " + saveLocation);
    }


    public void endConnection() throws IOException{
        //Notify server of connection end
        RequestBuilder protocolBuilder = new RequestBuilder();
        JSONObject goodbyeMessage = protocolBuilder.buildEndConnectionProtocol();
        out.writeObject(goodbyeMessage);

        //Close socket connection
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