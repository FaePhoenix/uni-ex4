package fae;

import org.json.JSONObject;


public class ObjectParser {
    
    public ObjectParser(){}

    public JSONObject handleInput(ObjectInputStream in, String exspectedType) {
        //Get user first-contact
        Object streamInput;
        try{
            streamInput = in.readObject();
        } catch(ClassNotFoundException exception){
            exception.printStackTrace();
            return new JSONObject();
        }

        //Check socket problems
        if (!(streamInput instanceof JSONObject)){
            System.out.println("Got answer in wrong format back. Changing Password failed. Please try again");
            System.out.println("Got Objectt of instance: " + firstContact.getClass());
            return new JSONObject();
        }

        //Check response type
        JSONObject JSONInput = (JSONObject) streamInput;
        if(JSONInput.getString("protocol_type") != exspectedType){ 
            System.out.println("Got wrong protocol back. Please try again");
            System.out.println("Got protocol of type: " + JSONInput.getString("protocol_type"));
            return new JSONObject();
        }

        //Return JSON-Protocol
        return JSONInput;
    }
}
