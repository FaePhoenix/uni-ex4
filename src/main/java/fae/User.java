package fae;

import java.lang.reflect.Array;
import org.json.JSONObject;

public class User {
    
    private String username;
    private String password;



    public User(){
        this.username = "";
        this.password = "";
    }

    public User(JSONObject jsonRep){
        this.username = (String) Array.get(jsonRep.keySet().toArray(), 0);
        this.password = jsonRep.getString(this.username);
    }


    public User(String email) throws IllegalEmailFormatException{
        //Guard Statement
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { throw new IllegalEmailFormatException("Given e-mail: " + email + " does not conform to official e-mail format.");}

        this.username = email;
        this.password = "";
    }


    public String getUsername(){return this.username;}

    public String getPassword(){return this.password;}

    public Boolean hasPassword(){return this.password != "";}


    public void setPassword(String newPassword){
        this.password = newPassword;
    }


    public Boolean isEqual(User comparingUser){
        return this.getUsername() == comparingUser.getUsername();
    }

    public JSONObject toJSON(){
        JSONObject user = new JSONObject();
        user.put("username", this.getUsername());
        user.put("password", this.getPassword());
        return user;
    }
}
