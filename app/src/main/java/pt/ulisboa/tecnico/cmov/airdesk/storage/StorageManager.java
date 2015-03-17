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
    Context appContext;

    public static final String OWNED_WORKSPACE_DIR = Constants.OWNED_WORKSPACE_DIR;
    public static final String FOREIGN_WORKSPACE_DIR = Constants.FOREIGN_WORKSPACE_DIR;

    public StorageManager() {
         appContext = AirDeskApp.s_applicationContext;
    }

    public boolean createDataFile(String workspaceName, String fileName, String ownerId, boolean isOwned) throws Exception {
        File baseDir;
        File directory;

        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            directory = FileUtils.createFolder(baseDir, workspaceName);
        } else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR , appContext.MODE_PRIVATE);
            directory = FileUtils.createFolder(baseDir, ownerId + "/" + workspaceName);
        }
        String filePath =  directory.getAbsolutePath() + "/" + fileName;
        return new File(filePath).createNewFile();
    }

    public synchronized FileInputStream getDataFile(String workspaceName, String fileName, boolean writeMode, String ownerId, boolean isOwned) throws WriteLockedException, IOException {
        String path =  OWNED_WORKSPACE_DIR + workspaceName + "/" + fileName;

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

    public void updateDataFile(String workspaceName, String fileName, FileInputStream inputStream, String ownerId, boolean isOwned) throws IOException {
        String path =  OWNED_WORKSPACE_DIR + workspaceName + "/" + fileName;
        FileUtils.writeToFile(path, inputStream);
    }

    public boolean deleteDataFile(String workspaceName, String fileName, String ownerId, boolean isOwned) throws IOException {
        String path =  OWNED_WORKSPACE_DIR + workspaceName + "/" + fileName;
        return FileUtils.deleteFolder(path);
    }

    private boolean isWriteLocked(String workspaceName, String fileName) {
        return fileWriteLock.get(workspaceName + "/" + fileName);
    }

    private void addWriteLock(String workspaceName, String fileName) {
        fileWriteLock.put(workspaceName + "/" + fileName, new Boolean(true));
    }

    public File createFolderStructureOnForeignWSAddition(String ownerId, String workspaceName) throws Exception {
        File parentDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        String uniqueWorkspacePath = ownerId + "/" + workspaceName;
        System.out.println("dir: " + parentDir.getAbsolutePath());
        return FileUtils.createFolder(parentDir, uniqueWorkspacePath);
    }

}
