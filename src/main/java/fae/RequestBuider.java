package fae;


import org.json.JSONObject;



public class RequestBuider {

    public RequestBuider() {}

    public JSONObject buildSendProtocol(String filename) {
        JSONObject baseProtocol = this.baseProtocol("0.0.1", "sendData");
        FSUGenBank dataFile = new FSUGenBank(filename);

        JSONObject protocol = new JSONObject();
        protocol.put("dataName", filename);

        JSONObject dataBody = new JSONObject();
        dataBody.put("fasta", dataFile.getFasta());
        dataBody.put("accession_numbers", dataFile.getAccessionNumbers());
        dataBody.put("sequence_version", dataFile.getSequenceVersion());
        dataBody.put("organism_species", dataFile.getOrganismSpecies());
        dataBody.put("keywords", dataFile.getKeywords());
        dataBody.put("description", dataFile.getDescription());


        return baseProtocol;
    }

    private JSONObject baseProtocol(String protocolVersion, String protocolType) {
        JSONObject baseProtocol = new JSONObject();
        baseProtocol.put("protocol_version", protocolVersion);
        baseProtocol.put("protocol_type", protocolType);
        return baseProtocol;
    }


}
