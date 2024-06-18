package fae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;


public class ServerThread extends Thread{
    
    private Socket ClientConnection;
    private DataInputStream in;
    private DataOutputStream out;
    private ServerSetting settings;
    private User user;

    public ServerThread(){
        this.ClientConnection = new Socket();

    }

    public ServerThread(Socket connection, ServerSetting settings, User me) throws IOException{
        this.ClientConnection = connection;
        this.in = new DataInputStream(this.ClientConnection.getInputStream());
        this.out = new DataOutputStream(this.ClientConnection.getOutputStream()); 
        this.settings = settings;
        this.user = me;
    }

    public void run() {
        ObjectParser inStreamHelper = new ObjectParser();
        RequestBuilder protocolBuilder = new RequestBuilder();
        Boolean alive = true;
        while(alive) {

            //Get client request
            JSONObject clientRequest;
            try {
                clientRequest = inStreamHelper.handleRequest(this.in);
                System.out.println("Got protocol from user");
            } catch (IOException e) {
                e.printStackTrace();
                clientRequest = protocolBuilder.buildErrorProtocol();
            }

            //Check protocolType
            String protocolType = clientRequest.getString("protocol_type");
            switch (protocolType){
                case "send_data":
                    this.handleSentClientData(clientRequest.getJSONObject("protocol_body"));
                    break;

                case "request_entries":
                    this.sendUserEntries();
                    break;


                case "change_password":
                    this.changeUserPassword(clientRequest.getJSONObject("protocol_body").getString("new_password"));
                    break;
                
                case "request_sequences":
                    this.sendSequences(clientRequest.getJSONObject("protocol_body")); 
                    break;

                case "request_data":
                    this.sendUserRequestedEntry(clientRequest.getJSONObject("protocol_body").getString("data_name"));
                    break;


                case "end_connection":
                    //Socket schließen
                    try {
                        this.ClientConnection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    alive = false;
                    break;

                default:
                    continue;
            }

        }
    }


    private void sendSequences(JSONObject protocol_body) {

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();        


        //Extract sequences
        String filename1 = this.settings.getEntryFolder() + protocol_body.getString("entry_1");
        FSUGenBank entry1 = new FSUGenBank(filename1);
        String sequence1 = entry1.getFasta().getDnaSequence();

        String filename2 = this.settings.getEntryFolder() + protocol_body.getString("entry_2");
        FSUGenBank entry2 = new FSUGenBank(filename2);
        String sequence2 = entry2.getFasta().getDnaSequence();


        //Send sequences to client
        JSONObject response = protocolBuilder.buildEntriesSendProtocol(sequence1, sequence2);
        try {
            this.out.writeUTF(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSentClientData(JSONObject protocolBody){
        //save user entry
        FSUGenBank sentEntry = new FSUGenBank(protocolBody.getJSONObject("data_body"));
        String entryName = protocolBody.getString("data_name");
        String saveLocation = this.settings.getEntryFolder() + entryName;
        sentEntry.saveToFile(saveLocation);

        //extract entrylist
        String filename = this.settings.getEntryListLocation();
        FileHelper entryList = new FileHelper(filename);
        String content = String.join("", entryList.getContent());
        JSONObject entries = new JSONObject(content);

        //save expanded entrylist
        JSONArray entrArray = entries.getJSONArray("entries");
        entrArray.put(entryName);
        JSONObject newEntrList = new JSONObject();
        newEntrList.put("entries", entrArray);
        newEntrList.put("amount", entries.getInt("amount") + 1);
        FileHelper expEntryList = new FileHelper(newEntrList);
        expEntryList.saveToFile(filename);
    }

    private void sendUserEntries(){

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();

        //Get information from file
        String fileName = this.settings.getEntryListLocation();
        FileHelper helper = new FileHelper(fileName);
        JSONObject entryList = new JSONObject(String.join("", helper.getContent()));

        //Send available entries to client
        JSONObject availableEntriesProtocol = protocolBuilder.buildAvailableEntriesProtocol(entryList);
        try {
            this.out.writeUTF(availableEntriesProtocol.toString());
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
            this.out.writeUTF(pwdChangeConfirmation.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendUserRequestedEntry(String dataname) {

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();

        String requestedEntry = this.settings.getEntryFolder() + dataname;

        //Send requested data to user
        JSONObject dataProtocol = protocolBuilder.buildDataSendProtocol(requestedEntry);      
        try {
            this.out.writeUTF(dataProtocol.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}