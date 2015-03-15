package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class Workspace {

    private String workspaceName;
    private long quota;
    private ArrayList<String> clients=new ArrayList<String>();
    private ArrayList<String> files = new ArrayList<String>();
    private String ownerName;

    public Workspace(String name, String owner, long quota) {
        this.workspaceName = name;
        this.ownerName = owner;
        this.quota = quota;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String workspaceName() {
        return workspaceName;
    }

    public long getQuota() {
        return quota;
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

    public ArrayList<String> getFiles() {
        return files;
    }

    public void addFile(String fileName) {
        files.add(fileName);
    }

    public void removeFile(String fileName) {
        files.remove(fileName);
    }
}
