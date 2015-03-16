package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class OwnedWorkspace extends AbstractWorkspace {

    private String ownerEmail;
    private ArrayList<String> clients=new ArrayList<String>();
    private String ownerName;

    public OwnedWorkspace(String workspaceName, String owner, double quota) {
        super(workspaceName, owner, quota);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public ArrayList<String> getClients() {
        return clients;
    }

    public void addClient(String clientName) {
        clients.add(clientName);
    }

    public void removeClient(String clientName) {
        clients.remove(clientName);
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
