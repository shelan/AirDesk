package pt.ulisboa.tecnico.cmov.airdesk.storage;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.FileUtils;
import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class StorageManager {

    private static HashMap<String, Boolean> fileWriteLock = new HashMap<String, Boolean>();
    //TODO read from property file
    private String ownedWorkspaceDir = Constants.OWNED_WORKSPACE_DIR;

    public boolean createDataFile(String workspaceName, String fileName) throws IOException {
        String path =  ownedWorkspaceDir + workspaceName + "/" + fileName;
        return new File(path).createNewFile();
    }

    public synchronized FileInputStream getDataFile(String workspaceName, String fileName, boolean writeMode) throws WriteLockedException, IOException {
        String path =  ownedWorkspaceDir + workspaceName + "/" + fileName;

        if(writeMode) {
            if(isWriteLocked(workspaceName, fileName))
                throw new WriteLockedException("File is already write locked. Cannot open in write mode");
            else {
                addWriteLock(workspaceName, fileName);
                return FileUtils.readFile(path);
            }
        }

        return FileUtils.readFile(path);
    }

    public void updateDataFile(String workspaceName, String fileName, FileInputStream inputStream) throws IOException {
        String path =  ownedWorkspaceDir + workspaceName + "/" + fileName;
        FileUtils.writeToFile(path, inputStream);
    }

    public void deleteDataFile(String workspaceName, String fileName) throws IOException {
        String path =  ownedWorkspaceDir + workspaceName + "/" + fileName;
        FileUtils.deleteFolder(path);
    }

    private boolean isWriteLocked(String workspaceName, String fileName) {
        return fileWriteLock.get(workspaceName + "/" + fileName);
    }

    private void addWriteLock(String workspaceName, String fileName) {
        fileWriteLock.put(workspaceName + "/" + fileName, new Boolean(true));
    }

    public boolean createFolderStructureOnForeignWSAddition(String ownerId, String workspaceName) throws Exception {
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir = appContext.getDir(Constants.FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        String uniqueWorkspacePath = ownerId + "/" + workspaceName;
        return FileUtils.createFolder(parentDir, uniqueWorkspacePath);
    }

}
