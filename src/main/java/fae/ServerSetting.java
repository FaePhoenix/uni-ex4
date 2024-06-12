package fae;

public class ServerSetting {
    
    private final String entryFolder;
    private final String entryListLocation;
    private final String userLocation;


    public ServerSetting(String serverFolder){
        this.entryFolder = serverFolder;
        this.entryListLocation = serverFolder + "entries.json";
        this.userLocation = serverFolder + "users.json";
    }


    public String getEntryFolder(){
        return this.entryFolder;
    }


    public String getEntryListLocation(){
        return this.entryListLocation;
    }


    public String getUserLocation(){
        return this.userLocation;
    }
}
