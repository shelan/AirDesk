package pt.ulisboa.tecnico.cmov.airdesk.storage;

import android.content.Context;
import android.provider.MediaStore;

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
        String pathToDir;
        String filePath;
        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToDir = baseDir.getAbsolutePath() + File.separator + workspaceName;

            System.out.println("path to file: " + pathToDir);
            if(!new File(pathToDir).exists()) {
                File directory = FileUtils.createFolder(baseDir, workspaceName);
                filePath =  directory.getAbsolutePath() + File.separator + fileName;
            } else {
                filePath = pathToDir + File.separator + fileName;
            }

            if(new File(filePath).exists())
                throw new Exception("Cannot create new file. File with the same name already exists. " + filePath);
            else
                return new File(filePath).createNewFile();

        } else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR , appContext.MODE_PRIVATE);
            pathToDir = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator + workspaceName;
            System.out.println("path to file: " + pathToDir);

            if(!new File(pathToDir).exists()) {
                File directory = FileUtils.createFolder(baseDir, ownerId + File.separator + workspaceName);
                filePath =  directory.getAbsolutePath() + "/" + fileName;
            } else {
                filePath = pathToDir + File.separator + fileName;
            }

            if(new File(filePath).exists())
                throw new Exception("Cannot create new file. File with the same name already exists. " + filePath);
            else
                return new File(filePath).createNewFile();
        }
    }

    public synchronized FileInputStream getDataFile(String workspaceName, String fileName, boolean writeMode, String ownerId, boolean isOwned) throws WriteLockedException, IOException {
        File baseDir;
        String pathToFile;

        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;
        }
        else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator +
                    workspaceName + File.separator + fileName;
        }

        if(writeMode) {
            if(isWriteLocked(workspaceName, fileName))
                throw new WriteLockedException("File is already write locked. Cannot open in write mode");
            else {
                addWriteLock(workspaceName, fileName);
                return FileUtils.readFile(pathToFile);
            }
        }
        return FileUtils.readFile(pathToFile);
    }

    public void updateDataFile(String workspaceName, String fileName, FileInputStream inputStream, String ownerId, boolean isOwned) throws IOException {
        File baseDir;
        String pathToFile;

        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;
        }
        else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator +
                    workspaceName + File.separator + fileName;
        }

        FileUtils.writeToFile(pathToFile, inputStream);
    }

    public boolean deleteDataFile(String workspaceName, String fileName, String ownerId, boolean isOwned) throws IOException {
        File baseDir;
        String pathToFile;
        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;
        }
        else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator +
                    workspaceName + File.separator + fileName;
        }
        System.out.println("file to del: "+ pathToFile);
        return new File(pathToFile).delete();
    }

    private boolean isWriteLocked(String workspaceName, String fileName) {
        return fileWriteLock.get(workspaceName + File.separator + fileName);
    }

    private void addWriteLock(String workspaceName, String fileName) {
        fileWriteLock.put(workspaceName + File.separator + fileName, new Boolean(true));
    }

    public File createFolderForForeignWorkspace(String ownerId, String workspaceName) throws Exception {
        File parentDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        String uniqueWorkspacePath = ownerId + File.separator + workspaceName;
        return FileUtils.createFolder(parentDir, uniqueWorkspacePath);
    }

}
