package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import java.util.HashMap;

public class AirDeskMessage {

    private String type;
    private HashMap<String, Object> inputs = new HashMap<String, Object>();

    public AirDeskMessage(String type) {

        this.type = type;
    }

    public void addInput(String key, Object value) {
        inputs.put(key,value);
    }

    public String getType() {
        return type;
    }

    public HashMap<String, Object> getInputs() {
        return inputs;
    }
}
