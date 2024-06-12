package fae;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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


    public Server () throws IOException{
        this.socket = new ServerSocket();
    }


    public Server(int port) throws IOException{
        this.socket = new ServerSocket(port);
        this.users = new ArrayList<User>();
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
            ServerThread connection = new ServerThread(clientConnection);
            System.out.println("Starting Connection-Thread");
            connection.start();
        }
    }


    private Boolean authenticate(Socket connection) throws IOException, MessagingException {
        ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream()); 
        
        ObjectParser inStreamHelper = new ObjectParser();

        JSONObject userRepsonse = inStreamHelper.handleIN
        //Get user first-contact
        Object firstContact;
        try{
            firstContact = in.readObject();
        } catch(ClassNotFoundException exception){
            exception.printStackTrace();
            return false;
        }

        //Check socket problems
        if (!(firstContact instanceof JSONObject)){
            System.out.println("Got answer in wrong format back. Changing Password failed. Please try again");
            System.out.println("Got Objectt of instance: " + firstContact.getClass());
            return false;
        }

        //Check response type
        JSONObject userRequest = (JSONObject) firstContact;
        if(userRequest.getString("protocol_type") != "first_contact"){ 
            System.out.println("Got wrong protocol back. Please try again");
            System.out.println("Got protocol of type: " + userRequest.getString("protocol_type"));
            return false;
        }

        //Extract username and send user password
        JSONObject requestBody = userRequest.getJSONObject("protocol_body");
        String username = requestBody.getString("username");
        String password = this.generatePassword(username);
        String emailContent = "password: " + password;
        sendEmail(username, "Server-Verification", emailContent);

        //Get user password confirmation
        Object response;
        try{
            response = in.readObject();
        } catch(ClassNotFoundException exception){
            exception.printStackTrace();
            return false;
        }

        //Check socket problems
        if (!(response instanceof JSONObject)){
            System.out.println("Got answer in wrong format back. Changing Password failed. Please try again");
            System.out.println("Got Objectt of instance: " + response.getClass());
            return false;
        }

        //Check response type
        JSONObject userResponse = (JSONObject) response;
        if(userResponse.getString("protocol_type") != "first_contact"){ 
            System.out.println("Got wrong protocol back. Please try again");
            System.out.println("Got protocol of type: " + userResponse.getString("protocol_type"));
            return false;
        }

        //Check user password 
        RequestBuilder protocol_builder = new RequestBuilder();
        JSONObject protocolBody = userResponse.getJSONObject("protocol_body");
        String user_password = protocolBody.getString("user_password");
        Boolean correctPwd = (user_password == password);

        //Send status to user and check for incorrect pwd
        JSONObject authenticationResponse = protocol_builder.buildUserConfirmationProtocol(correctPwd);
        out.writeObject(authenticationResponse);
        if (!correctPwd){
            return false;
        }
        
        //Add user to userlist
        User client = new User(username);
        client.setPassword(password);
        this.users.add(client);
        return true;
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
