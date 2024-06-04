package fae;


import org.json.JSONObject;



public class RequestBuilder {

    public RequestBuilder() {}

    public JSONObject buildDataSendProtocol(String filename) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "sendData");
        FSUGenBank dataFile = new FSUGenBank(filename);

        JSONObject protocol = new JSONObject();
        protocol.put("data_name", filename);

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
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "requestData");
        JSONObject protocol = new JSONObject();

        protocol.put("data_name", dataName);
        baseProtocol.put("protocol_body", protocol);

        return baseProtocol;
    }


    public JSONObject buildPasswordChangeProtocol(String newPassword){
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "changePassword");
        JSONObject protocol = new JSONObject();

        protocol.put("new_password", newPassword);
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