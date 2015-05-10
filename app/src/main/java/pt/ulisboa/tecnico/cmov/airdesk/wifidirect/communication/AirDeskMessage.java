package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import java.util.HashMap;

public class AirDeskMessage {

    private String type;
    private HashMap<String, Object> inputs = new HashMap<String, Object>();
    //userID of sender
    private String senderID;
    private String senderIP;

    public AirDeskMessage(String type, String senderID) {
        this.type = type;
        this.senderID = senderID;
    }

    public void addInput(String key, Object value) {
        inputs.put(key,value);
    }

    public String getType() {
        return type;
    }

    public String getSenderID() {
        return senderID;
    }

    public HashMap<String, Object> getInputs() {
        return inputs;
    }
}
