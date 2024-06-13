package fae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import org.json.JSONObject;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;


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


    public void start() throws IOException, MessagingException{
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


    private Boolean authenticate(Socket connection) throws IOException, MessagingException {

        //Build Helpers
        RequestBuilder protocol_builder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Build Object-Streams
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        DataInputStream in = new DataInputStream(connection.getInputStream());

        //Extract username and 
        JSONObject userRequest = inStreamHelper.handleInput(in, "first_contact");
        String username = userRequest.getJSONObject("protocol_body").getString("username");


        //Generate password and send to user
        String password = this.generatePassword(username);
        String emailContent = "password: " + password;
        sendEmail(username, "Server-Verification", emailContent);

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


    private String generatePassword(String username) {
        //Helper values
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    + "0123456789"
                                    + "abcdefghijklmnopqrstuvxyz";
        int passwordSize = username.length();
        StringBuilder sb = new StringBuilder(passwordSize);

        //Generate Password
        for (int i = 0; i < passwordSize; i++) {
            int index = (int) (alphaNumericString.length() * Math.random());
            sb.append(alphaNumericString.charAt(index));
        }

        return sb.toString();
    }
    

    private void sendEmail(String recipient, String mailSubject, String content) throws MessagingException {

        //Set Properties
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.web.de");
        prop.put("mail.smtp.port", "587");

        //Get Session
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("f56755829@web.de", "EwE3vkIrH0ZqppM");
            }
        });

        //Create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("f56755829@web.de"));
        message.setRecipients(
        Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(mailSubject);

        //Fill message
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(content, "text/html; charset=utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }

    
    @Override
    public void finalize(){
        try{
            this.socket.close();
        } catch(IOException exception){}
        
    }
}
