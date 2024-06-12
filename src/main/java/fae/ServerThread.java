package fae;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.json.JSONObject;


public class ServerThread extends Thread{
    
    private Socket ClientConnection;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerSetting settings;
    private User user;

    public ServerThread(){
        this.ClientConnection = new Socket();

    }

    public ServerThread(Socket connection, ServerSetting settings, User me) throws IOException{
        this.ClientConnection = connection;
        this.in = new ObjectInputStream(this.ClientConnection.getInputStream());
        this.out = new ObjectOutputStream(this.ClientConnection.getOutputStream()); 
        this.settings = settings;
        this.user = me;
    }

    public void run() {
        ObjectParser inStreamHelper = new ObjectParser();
        RequestBuilder protocolBuilder = new RequestBuilder();
        while(true) {

            //Get client request
            JSONObject clientRequest;
            try {
                clientRequest = inStreamHelper.handleRequest(this.in);
            } catch (IOException e) {
                e.printStackTrace();
                clientRequest = protocolBuilder.buildErrorProtocol();
            }

            //Check protocolType
            String protocolType = clientRequest.getString("protocol_type");
            switch (protocolType){
                case "send_data":
                    this.handleSentClientData(clientRequest.getJSONObject("protocol_body"));

                case "request_entries":
                    this.sendUserEntries();

                case "change_password":
                    this.changeUserPassword(clientRequest.getJSONObject("protocol_body").getString("new_password"));

                case "end_connection":
                    //Socket schließen
                    try {
                        this.ClientConnection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    continue;
            }

        }
    }


    private void handleSentClientData(JSONObject protocolBody){

    }

    private void sendUserEntries(){

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Get information from file
        String fileName = this.settings.getEntryListLocation();
        FileHelper helper = new FileHelper(fileName);
        JSONObject entryList = new JSONObject(String.join("", helper.getContent()));

        //Send available entries to client
        JSONObject availableEntriesProtocol = protocolBuilder.buildAvailableEntriesProtocol(entryList);
        try {
            this.out.writeObject(availableEntriesProtocol);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Extract user request
        JSONObject userRequest;
        try {
            userRequest = inStreamHelper.handleRequest(in);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String requestedEntry = this.settings.getEntryFolder() + userRequest.getJSONObject("protocol_body").getString("data_name");

        //Send requested data to user
        JSONObject dataProtocol = protocolBuilder.buildDataSendProtocol(requestedEntry);      
        try {
            this.out.writeObject(dataProtocol);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void changeUserPassword(String newPwd){

        //Get users from file
        String filename = this.settings.getUserLocation();
        FileHelper users = new FileHelper(filename);
        String content = String.join("", users.getContent());
        JSONObject userRep = new JSONObject(content);

        //Change password in file
        userRep.remove(this.user.getUsername());
        userRep.put(this.user.getUsername(), newPwd);
        FileHelper newUsers = new FileHelper(userRep);
        newUsers.saveToFile(filename);

        //Send confirmation to client
        RequestBuilder protocolBuilder = new RequestBuilder();
        JSONObject pwdChangeConfirmation = protocolBuilder.buildPasswordChangeResponse(true);
        try {
            this.out.writeObject(pwdChangeConfirmation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}