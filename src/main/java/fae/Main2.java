package fae;

import java.io.IOException;

public class Main2 {
    public static void main(String[] args) {
            
        try {
            Client me = new Client("fae.koerper@hamburg.de");
            me.connect(1024);

        } catch (IllegalEmailFormatException | IOException e) {
            e.printStackTrace();
        }
        }
}
