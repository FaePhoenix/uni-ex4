package fae;


import java.util.ArrayList;
import org.json.JSONObject;




public class Main {
    public static void main(String[] args) {
    testing();    
    //aufgabe4();
    }


    public static void testing() {
        RequestBuilder builder = new RequestBuilder();
        JSONObject request = builder.buildSendProtocol("txtfiles/FSUGenBankExample.txt");
        ArrayList<String> json = new ArrayList<String>();
        json.add(request.toString());


        FileHelper helper = new FileHelper(json);
        helper.saveToFile("jsonTest.json");


        /* 
        try {
            Server server = new Server(1024);
            server.start();
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }  
        */
    }


    public static void aufgabe4(){
        try{
            


        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

}