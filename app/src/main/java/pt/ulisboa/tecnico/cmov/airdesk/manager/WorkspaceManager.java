package pt.ulisboa.tecnico.cmov.airdesk.manager;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.FileUtils;
import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class WorkspaceManager {

    public boolean createWorkspace(String workspaceName,double quotaSize){
        boolean isMemoryNotSufficient=isNotSufficientMemory(quotaSize);
        if(isMemoryNotSufficient){
            return false;
        }
        else{
            UserManager userMgr=new UserManager();
            MetadataManager metaManager=new MetadataManager();
            User user=userMgr.getOwner();
            user.addNewOwnedWs(workspaceName);
            userMgr.createUser(user);//update existing user with new workspace
            System.out.println("------user created------");

            OwnedWorkspace workspace=new OwnedWorkspace(workspaceName, user.getEmail(), quotaSize);
            workspace.setOwnerName(user.getNickName());
            workspace.setOwnerEmail(user.getEmail());
            metaManager.saveOwnedWorkspace(workspace);

            System.out.println("----ws metadata------");
            boolean create= FileUtils.createFolder(workspaceName);
            System.out.println("file created "+create);
            return create;
        }
    }

    public boolean editOwnedWorkspace(String workspaceName, double quotaSize){
        boolean isQuotaSmallerThanUsage=isQuotaSmallerThanUsage(workspaceName,quotaSize);
        if(isQuotaSmallerThanUsage){
            return false;
        }
        else{
            //get metadata for workspace and save after changing quota size
            String jsonWorkspaceFileName=workspaceName+Constants.jsonSuffix;
            MetadataManager metaManager=new MetadataManager();
            OwnedWorkspace workspace=metaManager.getOwnedWorkspace(jsonWorkspaceFileName);
            workspace.setQuota(quotaSize);
            metaManager.saveOwnedWorkspace(workspace);
            return true;
        }
    }

    /*make this public if don't do a prevalidation on textbox*/
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

    public boolean isQuotaSmallerThanUsage(String workspaceName, double quotaSize){
        //when user edit quota size, use this to  check he has not reduced it below workspace size
        double wsFolderSize=FileUtils.folderSize(workspaceName);
        if(wsFolderSize>quotaSize){
            return true;
        }
        return false;
    }

    public List<String> addUsersToAccessList(String workspace,String userId){
        //Both from the subscription and adding email by owner
        //To simulate mount, add to foreign ws of same user. Later change this to use wifidirect

        return null;
    }

    public boolean addToForeignWorkspaces(OwnedWorkspace workspace){
        return false;
    }

    private static StorageManager storageManager;
    private static MetadataManager metadataManager;

    public WorkspaceManager() {
        storageManager = new StorageManager();
        metadataManager = new MetadataManager();
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

    public void addToForeignWorkspace(String workspaceName, String ownerId, long quota, String[] fileNames) throws Exception {

        String workspacePath = Constants.FOREIGN_WS_DIR + "/" + ownerId + "/" + workspaceName;
        boolean successfullyAdded = storageManager.createFolderStructureOnForeignWSAddition(workspacePath);
        if(successfullyAdded) {
            ForeignWorkspace workspace = new ForeignWorkspace(workspaceName, ownerId, quota);
            workspace.addFiles(fileNames);
            metadataManager.saveForeignWorkspace(workspace);
        }
    }
}
