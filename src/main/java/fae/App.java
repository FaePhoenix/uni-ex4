package fae;

import java.io.IOException;
import jakarta.mail.MessagingException;



public class App 
{
    public static void main( String[] args )
    {
        testing(); 
    }


    public static void testing(){
        try {
            Server server = new Server();

            server.sendEmail("f56755829@gmail.com", "testing mail sending", "hopefully it works");
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        
    }


    public static void ex4(){
        try {
            Client me = new Client("fae.koerper@hamburg.de");
            me.connect(1024);

        } catch (IllegalEmailFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
