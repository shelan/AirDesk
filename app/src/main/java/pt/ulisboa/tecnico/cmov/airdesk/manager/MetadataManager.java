package pt.ulisboa.tecnico.cmov.airdesk.manager;

import android.content.Context;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class MetadataManager {

    private Gson gson = new Gson();

    public void saveOwnedWorkspace(OwnedWorkspace workspace) {
        String jsonString = gson.toJson(workspace);
        System.out.println(jsonString);
        String jsonWorkspaceFileName = workspace.getWorkspaceName() + Constants.OWNED_SUFFIX + Constants.JSON_SUFFIX;
        saveToInternalFile(jsonString, jsonWorkspaceFileName);
    }

    public OwnedWorkspace getOwnedWorkspace(String workspaceFileName) {
        String ownedWSFileName = workspaceFileName + Constants.OWNED_SUFFIX + Constants.JSON_SUFFIX;
        String workspaceJson = readFromInternalFile(ownedWSFileName);
        OwnedWorkspace workspace = gson.fromJson(workspaceJson, OwnedWorkspace.class);
        return workspace;
    }

    public boolean deleteOwnedWorkspace(String workspaceName){
        String ownedWSFileName=workspaceName+Constants.OWNED_SUFFIX+Constants.JSON_SUFFIX;
        return deleteFile(ownedWSFileName);
    }

    public void saveForeignWorkspace(ForeignWorkspace workspace) {
        String jsonString = gson.toJson(workspace);
        System.out.println(jsonString);
        String jsonWorkspaceFileName = workspace.getWorkspaceName() + Constants.FOREIGN_WORKSPACE_SUFFIX;
        saveToInternalFile(jsonString, jsonWorkspaceFileName);
    }

    public ForeignWorkspace getForeignWorkspace(String workspaceFileName) {
        String workspaceJson = readFromInternalFile(workspaceFileName + Constants.FOREIGN_WORKSPACE_SUFFIX);
        ForeignWorkspace workspace = gson.fromJson(workspaceJson, ForeignWorkspace.class);
        return workspace;
    }

    public boolean deleteForeignWorkspace(String workspaceName){
        String ownedWSFileName=workspaceName+Constants.FOREIGN_WORKSPACE_SUFFIX;
        return deleteFile(ownedWSFileName);
    }

    public void saveUser(User user) {
        String jsonString = gson.toJson(user);
        saveToInternalFile(jsonString, Constants.USER_JSON_FILE_NAME);
    }

    public User getUser() {
        String userJson = readFromInternalFile(Constants.USER_JSON_FILE_NAME);
        User user = gson.fromJson(userJson, User.class);
        return user;
    }

    public boolean deleteFile(String fileName ){
     try{
         Context appContext = AirDeskApp.s_applicationContext;
         String absFileLocation=appContext.getFilesDir().getAbsolutePath()+"/"+fileName;
         System.out.println("file to be deleted:" + absFileLocation);
         File file = new File(absFileLocation);
         boolean deleted = file.delete();
         System.out.println("file delete status is"+deleted);
         return  deleted;
     }
     catch (Exception ex){
         ex.printStackTrace();
         return false;
     }
    }

    public String readFromInternalFile(String fileName) {
        FileInputStream fis = null;
        try {
            Context appContext = AirDeskApp.s_applicationContext;
            fis = appContext.openFileInput(fileName);
            String jsonString = readStreamAsString(fis);
            return jsonString;
        } catch (IOException x) {
            return null;
        }

    }

    private String readStreamAsString(InputStream is)
            throws FileNotFoundException, IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            copy(is, baos);
            baos.close();
            return baos.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void copy(InputStream reader, OutputStream writer)
            throws IOException {
        byte byteArray[] = new byte[4092];
        while (true) {
            int numOfBytesRead = reader.read(byteArray, 0, 4092);
            if (numOfBytesRead == -1) {
                break;
            }
            writer.write(byteArray, 0, numOfBytesRead);
        }
        return;
    }

    public void saveToInternalFile(String jsonString, String fileName) {
        FileOutputStream fos = null;
        try {
            Context appContext = AirDeskApp.s_applicationContext;
            fos = appContext.openFileOutput(fileName
                    , Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
