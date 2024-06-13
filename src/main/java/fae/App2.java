package fae;

import java.io.IOException;

public class App2 {
    public static void main( String[] args )
    {
        try {
            Client me = new Client("spassfuerdieuni@web.de");
            System.out.println("connecting");
            if(me.connect(1024)){
                System.out.println("done");
                me.run();
            } else {
                System.out.println("Connection failed");
            }
            
            

        } catch (IllegalEmailFormatException | IOException e) {
            e.printStackTrace();
            System.out.println("error");
        }
    }
    
}
