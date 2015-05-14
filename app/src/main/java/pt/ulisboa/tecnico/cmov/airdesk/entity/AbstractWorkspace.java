package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.io.Serializable;
import java.util.ArrayList;


//TODO rename this to workspace once workspace and its methods are refactored to ownedWS
public class AbstractWorkspace implements Serializable {

    private String workspaceName;
    private double quota;
    private String ownerId;
    private ArrayList<String> fileNames = new ArrayList<String>();

    public AbstractWorkspace(String workspaceName, String ownerId, double quota) {
        this.workspaceName = workspaceName;
        this.ownerId = ownerId;
        this.quota = quota;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public double getQuota() {
        return quota;
    }

    public void setQuota(double quota) {
        this.quota = quota;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public ArrayList<String> getFileNames() {
        return fileNames;
    }

    public void addFile(String fileName) {
        fileNames.add(fileName);
    }

    public void addFiles(String[] names) {
        for(String fileName : names)
            fileNames.add(fileName);
    }

    public void removeFile(String fileName) {
        fileNames.remove(fileName);
    }

    public void replaceFileNames(String[] updatedNames) {
        fileNames = new ArrayList<String>();
        for (String fileName : updatedNames) {
            fileNames.add(fileName);
        }
    }
}
