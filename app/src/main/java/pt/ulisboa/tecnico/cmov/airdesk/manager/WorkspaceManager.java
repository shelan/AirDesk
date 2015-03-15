package pt.ulisboa.tecnico.cmov.airdesk.manager;

import java.io.FileInputStream;
import java.io.IOException;

import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.entity.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class WorkspaceManager {

    private static StorageManager storageManager;

    public WorkspaceManager() {
        storageManager = new StorageManager();
    }

    public boolean addToForeignWorkspaces(Workspace workspace){
        return false;
    }

    public void addDataFile(String workspace, String fileName) throws IOException {
        storageManager.createDataFile(workspace, fileName);
    }

    public void getDataFile(String workspace, String fileName, boolean writeMode) throws IOException, WriteLockedException {
        storageManager.getDataFile(workspace, fileName, writeMode);
    }

    public void updateDataFile(String workspace, String fileName, FileInputStream inputStream) throws IOException {
        storageManager.updateDataFile(workspace, fileName, inputStream);
    }

    public void deleteDataFile(String workspace, String fileName) throws IOException {
        storageManager.deleteDataFile(workspace, fileName);
    }
}
