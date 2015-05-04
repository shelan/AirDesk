package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import java.util.HashMap;

public class AirDeskMessage {

    private String type;
    private HashMap<String, Object> inputs = new HashMap<String, Object>();
    private String senderIp;

    public AirDeskMessage(String type, String senderIp) {
        this.type = type;
        this.senderIp = senderIp;
    }

    public void addInput(String key, Object value) {
        inputs.put(key,value);
    }

    public String getType() {
        return type;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public HashMap<String, Object> getInputs() {
        return inputs;
    }
}
