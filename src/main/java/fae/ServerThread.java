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

    public ServerThread(){
        this.ClientConnection = new Socket();
    }

    public ServerThread(Socket connection) throws IOException{
        this.ClientConnection = connection;
        this.in = new ObjectInputStream(this.ClientConnection.getInputStream());
        this.out = new ObjectOutputStream(this.ClientConnection.getOutputStream()); 
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

    }

    private void changeUserPassword(String newPwd){

    }
}