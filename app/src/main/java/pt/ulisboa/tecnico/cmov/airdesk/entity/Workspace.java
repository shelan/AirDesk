package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class Workspace {

    private String workspaceName;
    private double quota;
    private String ownerEmail;
    private ArrayList<String> clients=new ArrayList<String>();
    private ArrayList<String> files = new ArrayList<String>();
    private String ownerName;

    public Workspace(){

    }

    public Workspace(String name, String owner, long quota) {
        this.setWorkspaceName(name);
        this.setOwnerName(owner);
        this.setQuota(quota);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String workspaceName() {
        return getWorkspaceName();
    }

    public double getQuota() {
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

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public void setQuota(double quota) {
        this.quota = quota;
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
