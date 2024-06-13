package fae;


import java.io.IOException;
import jakarta.mail.MessagingException;




public class Main {
    public static void main(String[] args) {
    testing();    
    //aufgabe4();
    }


    public static void testing() {
        try {
            Server server = new Server(1024, "serverLocation/");
            server.start();
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }  
        
    }


    public static void aufgabe4(){
        try{
            


        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

}