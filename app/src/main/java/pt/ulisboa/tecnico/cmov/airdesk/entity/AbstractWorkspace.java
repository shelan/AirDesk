package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;

/**
 * Created by ashansa on 3/15/15.
 */

//TODO rename this to workspace once workspace and its methods are refactored to ownedWS
public class AbstractWorkspace {

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
}
