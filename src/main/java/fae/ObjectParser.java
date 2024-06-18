package fae;

import java.io.IOException;
import java.io.DataInputStream;
import org.json.JSONObject;


public class ObjectParser {
    
    public ObjectParser(){}

    public JSONObject handleInput(DataInputStream in, String expectedType) throws IOException {
        //Get user first-contact
        String streamOutput = in.readUTF();
        JSONObject JSONInput = new JSONObject(streamOutput);

        //Check response type
        String protocolType = JSONInput.getString("protocol_type");
        if(!protocolType.equals(expectedType)){ 
            System.out.println("Got wrong protocol back. Please try again");
            System.out.println("Got protocol of type: " + protocolType);
            System.out.println("Expected protocol of type: " + expectedType);
            return new JSONObject();
        }

        //Return JSON-Protocol
        return JSONInput;
    }

    public JSONObject handleRequest(DataInputStream in) throws IOException {

        //Get user first-contact
        String streamOutput = in.readUTF();
        
        //Return JSON-Protocol
        JSONObject JSONInput = new JSONObject(streamOutput);

        return JSONInput;
    }
}