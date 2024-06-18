package fae;

import org.json.JSONObject;


public class RequestBuilder {

    public RequestBuilder() {}

    
    public JSONObject buildDataSendProtocol(String filename) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "send_data");
        FSUGenBank dataFile = new FSUGenBank(filename);

        JSONObject protocol = new JSONObject();
        protocol.put("data_name", filename.substring("serverLocation/".length(), filename.length()));

        JSONObject dataBody = new JSONObject();
        dataBody.put("fasta", dataFile.getFasta().toJSON());
        dataBody.put("accession_numbers", dataFile.getAccessionNumbers());
        dataBody.put("sequence_version", dataFile.getSequenceVersion());
        dataBody.put("organism_species", dataFile.getOrganismSpecies());
        dataBody.put("keywords", dataFile.getKeywords());
        dataBody.put("description", dataFile.getDescription());

        protocol.put("data_body", dataBody);
        baseProtocol.put("protocol_body", protocol);

        return baseProtocol;
    }


    public JSONObject buildDataRequestProtocol(String dataName){
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "request_data");
        JSONObject protocol = new JSONObject();

        protocol.put("data_name", dataName);
        baseProtocol.put("protocol_body", protocol);

        return baseProtocol;
    }


    public JSONObject buildPasswordChangeProtocol(String newPassword){
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "change_password");
        JSONObject protocol = new JSONObject();

        protocol.put("new_password", newPassword);
        baseProtocol.put("protocol_body", protocol);

        return baseProtocol;
    }


    public JSONObject buildEntriesRequestProtocol(){
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "request_entries");
        return baseProtocol;
    }


    public JSONObject buildFirstContactProtocol(String userName) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "first_contact");
        JSONObject protocol = new JSONObject();

        protocol.put("username", userName);
        baseProtocol.put("protocol_body", protocol);

        return baseProtocol;
    }


    public JSONObject buildPasswordConfirmationProtocol(String password) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "password_confirmation");
        JSONObject protocol = new JSONObject();

        protocol.put("user_password", password);
        baseProtocol.put("protocol_body", protocol);
        
        return baseProtocol;
    }


    public JSONObject buildErrorProtocol() {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "error");

        return baseProtocol;
    }

    public JSONObject buildUserConfirmationProtocol(Boolean status){
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "authenticate_response");
        JSONObject protocol = new JSONObject();

        protocol.put("handshake_status", status);
        baseProtocol.put("protocol_body", protocol);
        
        return baseProtocol;
    }


    public JSONObject buildEndConnectionProtocol() {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "end_connection");

        return baseProtocol;
    }


    public JSONObject buildAvailableEntriesProtocol(JSONObject entryList) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "entries_list");
        baseProtocol.put("protocol_body", entryList);
        
        return baseProtocol;
    }

    public JSONObject buildRequestSequenceProtocol(String entry1, String entry2) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "request_sequences");
        JSONObject protocol = new JSONObject();

        protocol.put("entry_1", entry1);
        protocol.put("entry_2", entry2);
        baseProtocol.put("protocol_body", protocol);

        return baseProtocol;
    } 

    public JSONObject buildPasswordChangeResponse(boolean status) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "change_password_response");
        JSONObject protocol = new JSONObject();

        protocol.put("change_status", status);
        baseProtocol.put("protocol_body", protocol);
        
        return baseProtocol;
    }


    public JSONObject buildEntriesSendProtocol(String sequence1, String sequence2) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "sequences_response");
        JSONObject protocol = new JSONObject();

        protocol.put("sequence_1", sequence1);
        protocol.put("sequence_2", sequence2);
        baseProtocol.put("protocol_body", protocol);
        return baseProtocol;
    }


    private JSONObject baseProtocol(String protocolVersion, String protocolType) {
        JSONObject baseProtocol = new JSONObject();
        baseProtocol.put("protocol_version", protocolVersion);
        baseProtocol.put("protocol_type", protocolType);
        return baseProtocol;
    }
}