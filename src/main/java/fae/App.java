package fae;

import java.io.IOException;



public class App 
{
    public static void main( String[] args )
    {
        testing();
        //ex4(); 
    }


    public static void testing(){
        try {
            Client me = new Client("f56755829@gmail.com");
            System.out.println("connecting");
            me.connect(1024);
            System.out.println("done");

        } catch (IllegalEmailFormatException | IOException e) {
            e.printStackTrace();
            System.out.println("error");
        }
    }


    public static void ex4(){
        
    }
}
