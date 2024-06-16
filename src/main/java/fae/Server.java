package fae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

import org.json.JSONObject;



public class Server {

    private ArrayList<User> users;
    private ServerSocket socket;
    private ServerSetting settings;
    private User newestUser;


    public Server () throws IOException{
        this.socket = new ServerSocket();
    }


    public Server(int port, String ServerFolderLocation) throws IOException{
        this.socket = new ServerSocket(port);
        this.users = new ArrayList<User>();
        this.settings = new ServerSetting(ServerFolderLocation);
    }


    public void start() throws IOException {
        while (true) {
            //Wait for user connection request
            System.out.println("Looking for connection");
            Socket clientConnection = this.socket.accept();
            System.out.println("Found Connection");

            //Try to authenticate user
            Boolean accepted = this.authenticate(clientConnection);
            if(!accepted) {
                System.out.println("Connection could not be authenticated");
                System.out.println("Dropping User");
            }

            //Start user thread
            System.out.println("Connection authenticated");
            ServerThread connection = new ServerThread(clientConnection, this.settings, this.newestUser);
            System.out.println("Starting Connection-Thread");
            connection.start();
        }
    }


    private Boolean authenticate(Socket connection) throws IOException {

        //Build Helpers
        RequestBuilder protocol_builder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Build Object-Streams
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        DataInputStream in = new DataInputStream(connection.getInputStream());

        //Extract username 
        JSONObject userRequest = inStreamHelper.handleInput(in, "first_contact");
        String username = userRequest.getJSONObject("protocol_body").getString("username");

        //Compare to known Users
        ArrayList<String> knownUsers = getKnownUsers();
        String password = knownUsers.contains(username) ? getPassword(username) : firstTimeUser(username);

        //Check user password 
        JSONObject userResponse = inStreamHelper.handleInput(in, "password_confirmation");
        String user_password = userResponse.getJSONObject("protocol_body").getString("user_password");
        Boolean correctPwd = user_password.equals(password);

        //Send status to user and check for incorrect pwd
        JSONObject authenticationResponse = protocol_builder.buildUserConfirmationProtocol(correctPwd);
        out.writeUTF(authenticationResponse.toString());
        if (!correctPwd){
            return false;
        }
        
        //Add user to userlist
        this.setUser(username, password);
        return true;
    }

    private ArrayList<String> getKnownUsers() {

        //Extract json from file
        FileHelper helper = new FileHelper(this.settings.getUserLocation());
        JSONObject usrs = new JSONObject(String.join("", helper.getContent()));

        //get known users
        Set<String> knownUsrs = usrs.keySet();
        ArrayList<String> knownUsers = new ArrayList<String>();
        knownUsers.addAll(knownUsrs);

        return knownUsers;
    }

    private String firstTimeUser(String username) {
        String password = this.generatePassword();
        emailUser(username, password);
        return password;
    }

    private String getPassword(String username) {

        //Extract json from file
        FileHelper helper = new FileHelper(this.settings.getUserLocation());
        JSONObject usrs = new JSONObject(String.join("", helper.getContent()));

        //Get password from json
        String pasword = usrs.getString(username);
        return pasword;

    }

    private void emailUser(String username, String password) {

        //Preare content
        Mailing mailer = new Mailing();
        String emailContent = "password: " + password;

        //Send email to client
        while (!mailer.sendEmail(username, password, emailContent)) {
            continue;
        }
    }

    private void setUser(String username, String password) {
        User client = new User(username);
        client.setPassword(password);
        this.users.add(client);
        this.newestUser = client;

        String filename = this.settings.getUserLocation();
        FileHelper users = new FileHelper(filename);
        String content = String.join("", users.getContent());
        JSONObject userRep = new JSONObject(content);
        userRep.put(username, password);
        FileHelper newUsers = new FileHelper(userRep);
        newUsers.saveToFile(filename);
    }

    private String generatePassword() {
        //Helper values
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    + "0123456789"
                                    + "abcdefghijklmnopqrstuvxyz";
        int passwordSize = 8;
        StringBuilder sb = new StringBuilder(passwordSize);

        //Generate Password
        for (int i = 0; i < passwordSize; i++) {
            int index = (int) (alphaNumericString.length() * Math.random());
            sb.append(alphaNumericString.charAt(index));
        }

        return sb.toString();
    }

    
    @Override
    public void finalize(){
        try{
            this.socket.close();
        } catch(IOException exception){}
        
    }
}
