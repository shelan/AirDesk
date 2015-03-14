package pt.ulisboa.tecnico.cmov.airdesk.storage;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.entity.User;

/**
 * Created by ashansa on 3/14/15.
 */
public class WSMetadata {

    private String owner;
    private ArrayList<String> files = new ArrayList<String>();
    private ArrayList<String> clients = new ArrayList<String>();

    public WSMetadata(String ownerId) {
        this.owner = ownerId;
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

    public ArrayList<String> getClients() {
        return clients;
    }

    public void addClient(String clientName) {
        clients.add(clientName);
    }

    public void removeClient(String clientName) {
        clients.remove(clientName);
    }

}
