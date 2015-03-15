package pt.ulisboa.tecnico.cmov.airdesk.manager;

import android.os.Environment;
import android.os.StatFs;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.FileUtils;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
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

    public boolean createWorkspace(String workspaceName,double quotaSize){
        boolean isMemoryNotSufficient=isNotSufficientMemory(quotaSize);
        if(isMemoryNotSufficient){
            return false;
        }
        else{
            UserManager userMgr=new UserManager();
            MetadataManager metaManager=new MetadataManager();
            User user=userMgr.getOwner();
            user.addNewWs(workspaceName);
            userMgr.createUser(user);//update existing user with new workspace
            System.out.println("------user created------");

            Workspace workspace=new Workspace();
            workspace.setWorkspaceName(workspaceName);
            workspace.setOwnerName(user.getNickName());
            workspace.setOwnerEmail(user.getEmail());
            workspace.setQuota(quotaSize);
            Gson gson=new Gson();
            String jsonString=gson.toJson(workspace);
            System.out.println(jsonString);
            String jsonWorkspaceFileName=workspaceName+ Constants.jsonSuffix;
            metaManager.saveToInternalFile(jsonString,jsonWorkspaceFileName);
            System.out.println("----ws created------");
            return true;
        }
    }

    public boolean isNotSufficientMemory(double quotaSize){
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        int availBlocks = stat.getFreeBlocks();
        int blockSize = stat.getBlockSize();
        double free_memory = (((long)availBlocks * (long)blockSize)/(double)Constants.bytesPerKb);//available free memory on internal storage
        System.out.println("free memory is "+free_memory);
        if(free_memory<quotaSize){
            return true;
        }
        return false;
    }

    public boolean isQuotaTooSmall(double quotaSize){
        //when user edit quota size, use this to  check he has not reduced it below workspace size
        return false;
    }

    public void addToForeignWorkspace(String workspaceName, String ownerId, String[] fileNames) throws Exception {
        /////TODO add to metadata structure...
        storageManager.createFolderStructureOnForeignWSAddition(workspaceName, ownerId, fileNames);
    }

    public void addDataFile(String workspace, String fileName) throws IOException {
        storageManager.createDataFile(workspace, fileName);
    }

    public FileInputStream getDataFile(String workspace, String fileName, boolean writeMode) throws IOException, WriteLockedException {
        return storageManager.getDataFile(workspace, fileName, writeMode);
    }

    public void updateDataFile(String workspace, String fileName, FileInputStream inputStream) throws IOException {
        storageManager.updateDataFile(workspace, fileName, inputStream);
    }

    public void deleteDataFile(String workspace, String fileName) throws IOException {
        storageManager.deleteDataFile(workspace, fileName);
    }
}
