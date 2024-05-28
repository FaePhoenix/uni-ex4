package fae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
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
            System.out.println("Looking for connection");
            Socket clientConnection = this.socket.accept();
            System.out.println("Found Connection");
            Boolean accepted = this.authenticate(clientConnection);
            if(accepted) {
                System.out.println("Connection authenticated");
                ServerThread connection = new ServerThread(clientConnection);
                System.out.println("Starting Connection-Thread");
                connection.start();
            }
        }
    }


    private Boolean authenticate(Socket connection) throws IOException, MessagingException {
        DataInputStream in = new DataInputStream(connection.getInputStream());
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        
        String username = in.readUTF();
        String password = this.generatePassword(username);

        String emailContent = "password: " + password;
        sendEmail(username, "Server-Verification", emailContent);

        String paswordFromUser = in.readUTF();
        if (paswordFromUser != password){
            out.writeBoolean(false);
            return false;
        } else {
            out.writeBoolean(true);
            User client = new User(username);
            client.setPassword(password);
            this.users.add(client);
            return true;
        }

        
    }


    private String generatePassword(String username) {
        String password = "";
        for (char c : username.toCharArray()) {
            int charNum = (int) c;
            int compNum = (int) (charNum * Math.pow(1.1, charNum) % 128);
            char nChar = (char) compNum;
            
            password += nChar;
        }
        System.out.println("Generated password: " + password);
        return password;
    }
    

    private void sendEmail(String recipient, String mailSubject, String content) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.web.de");
        prop.put("mail.smtp.port", "587");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("f56755829@web.de", "EwE3vkIrH0ZqppM");
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("f56755829@web.de"));
        message.setRecipients(
        Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(mailSubject);


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
