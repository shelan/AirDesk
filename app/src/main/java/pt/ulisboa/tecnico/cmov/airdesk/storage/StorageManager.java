package pt.ulisboa.tecnico.cmov.airdesk.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.FileUtils;
import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;


public class StorageManager {

    private static HashMap<String, Boolean> fileWriteLock = new HashMap<String, Boolean>();
    private static HashMap<String, Long> accessMap = new HashMap<>();
    Context appContext;
    private static boolean isRestored = false;

    public static final String OWNED_WORKSPACE_DIR = Constants.OWNED_WORKSPACE_DIR;
    public static final String FOREIGN_WORKSPACE_DIR = Constants.FOREIGN_WORKSPACE_DIR;

    public StorageManager() {
        appContext = AirDeskApp.s_applicationContext;
    }

    public boolean createDataFile(String workspaceName, String fileName, String ownerId, boolean isOwned) throws Exception {
        File baseDir;
        String pathToDir;
        String filePath;
        ownerId = FileUtils.getFileNameForUserId(ownerId);
        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToDir = baseDir.getAbsolutePath() + File.separator + workspaceName;

            System.out.println("path to file: " + pathToDir);
            if (!new File(pathToDir).exists()) {
                File directory = FileUtils.createFolder(baseDir, workspaceName);
                filePath = directory.getAbsolutePath() + File.separator + fileName;
            } else {
                filePath = pathToDir + File.separator + fileName;
            }

            if (new File(filePath).exists())
                throw new Exception("Cannot create new file. File with the same name already exists. " + filePath);
            else
                addAccessTimeStamp(filePath);
            return new File(filePath).createNewFile();

        } else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToDir = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator + workspaceName;
            System.out.println("path to file: " + pathToDir);

            if (!new File(pathToDir).exists()) {
                File directory = FileUtils.createFolder(baseDir, ownerId + File.separator + workspaceName);
                filePath = directory.getAbsolutePath() + "/" + fileName;
            } else {
                filePath = pathToDir + File.separator + fileName;
            }

            if (new File(filePath).exists())
                throw new Exception("Cannot create new file. File with the same name already exists. " + filePath);
            else
                addAccessTimeStamp(filePath);
            return new File(filePath).createNewFile();
        }


    }

    public synchronized StringBuffer getDataFileContent(String workspaceName, String fileName, boolean writeMode, String ownerId, boolean isOwned) throws WriteLockedException, IOException {
        File baseDir;
        String pathToFile;
        ownerId = FileUtils.getFileNameForUserId(ownerId);

        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;
        } else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator +
                    workspaceName + File.separator + fileName;
        }

        if (writeMode) {
            if (isWriteLocked(workspaceName, fileName))
                throw new WriteLockedException("File is already write locked. Cannot open in write mode");
            else {
                addWriteLock(workspaceName, fileName);
                addAccessTimeStamp(pathToFile);
                return FileUtils.getStringBuffer(FileUtils.readFile(pathToFile));
            }
        }
        addAccessTimeStamp(pathToFile);
        return FileUtils.getStringBuffer(FileUtils.readFile(pathToFile));
    }

    public void updateDataFile(String workspaceName, String fileName, String content, String ownerId, boolean isOwned) throws IOException {
        File baseDir;
        String pathToFile;
        ownerId = FileUtils.getFileNameForUserId(ownerId);

        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;
        } else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator +
                    workspaceName + File.separator + fileName;
        }
        addAccessTimeStamp(pathToFile);
        FileUtils.writeToFile(pathToFile, content);
    }

    public boolean deleteDataFile(String workspaceName, String fileName, String ownerId, boolean isOwned) throws IOException {
        File baseDir;
        String pathToFile;
        ownerId = FileUtils.getFileNameForUserId(ownerId);

        if (isOwned) {
            baseDir = appContext.getDir(OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;
        } else {
            baseDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
            pathToFile = baseDir.getAbsolutePath() + File.separator + ownerId + File.separator +
                    workspaceName + File.separator + fileName;
        }

        System.out.println("file to del: " + pathToFile);

        removeAccessTimeStamp(pathToFile);
        persistAccessMap();
        return new File(pathToFile).delete();
    }

    private boolean isWriteLocked(String workspaceName, String fileName) {
        return fileWriteLock.get(workspaceName + File.separator + fileName);
    }

    private void addWriteLock(String workspaceName, String fileName) {
        fileWriteLock.put(workspaceName + File.separator + fileName, new Boolean(true));
    }

    public File createFolderForForeignWorkspace(String ownerId, String workspaceName) throws Exception {
        ownerId = FileUtils.getFileNameForUserId(ownerId);
        File parentDir = appContext.getDir(FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        String uniqueWorkspacePath = ownerId + File.separator + workspaceName;
        return FileUtils.createFolder(parentDir, uniqueWorkspacePath);
    }

    public boolean deleteFolderForForeignWorkspace(String workspaceName, String ownerId) {
        ownerId = FileUtils.getFileNameForUserId(ownerId);
        return FileUtils.deleteForeignWorkspaceFolder(workspaceName, ownerId);
    }

    private void addAccessTimeStamp(String filePath) {
        accessMap.put(filePath, new Date().getTime());
        persistAccessMap();
    }

    private void removeAccessTimeStamp(String filePath) {
        accessMap.remove(filePath);
    }

    public HashMap<String, Long> getAccessMap() {
        return accessMap;
    }

    public static void persistAccessMap() {
        try {
            SharedPreferences mPrefs = AirDeskApp.s_applicationContext.getSharedPreferences(
                    AirDeskApp.s_applicationContext.getApplicationInfo().name, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = mPrefs.edit();
            Gson gson = new Gson();
            ed.putString("accessMap", gson.toJson(accessMap));
            ed.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void restoreAccessMap() {
        if (isRestored) {
            return;
        }
        try {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            SharedPreferences mPrefs = AirDeskApp.s_applicationContext.getSharedPreferences(
                    AirDeskApp.s_applicationContext.getApplicationInfo().name, Context.MODE_PRIVATE);

            Set<Map.Entry<String, JsonElement>> entrySet = parser.
                    parse(mPrefs.getString("accessMap", null)).getAsJsonObject().entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                accessMap.put(entry.getKey(),entry.getValue().getAsLong());
            }

            isRestored = true;
        } catch (Exception ex) {
            //
        }

    }

}
