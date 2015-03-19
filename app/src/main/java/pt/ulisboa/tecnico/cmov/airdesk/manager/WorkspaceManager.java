package pt.ulisboa.tecnico.cmov.airdesk.manager;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.FileUtils;
import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.enums.WorkspaceCreateStatus;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class WorkspaceManager {

    private  StorageManager storageManager=new StorageManager();
    private  MetadataManager metadataManager=new MetadataManager();
    private UserManager userManager=new UserManager();

    //set all ui data other than owner data in workspace object
    public WorkspaceCreateStatus createWorkspace(OwnedWorkspace workspace){
        String workspaceName=workspace.getWorkspaceName();
        boolean isMemoryNotSufficient=isNotSufficientMemory(workspace.getQuota());
        if(isMemoryNotSufficient){
            return WorkspaceCreateStatus.INSUFFICIENT_MEMORY;
        }
        else{
            User user=userManager.getOwner();

            boolean isWorkspaceAlreadyExist= isWorkspaceAlreadyExists(workspaceName, user.getOwnedWorkspaces());
            if(isWorkspaceAlreadyExist){
                return WorkspaceCreateStatus.WORKSPACE_ALREADY_EXISTS;
            }

            user.addNewOwnedWorkspace(workspaceName);
            userManager.createUser(user);//update existing user with new workspace
            System.out.println("------user created------");

            workspace.setOwnerName(user.getNickName());
            workspace.setOwnerEmail(user.getEmail());
            metadataManager.saveOwnedWorkspace(workspace);

            System.out.println("----ws metadata------");
            boolean create= FileUtils.createWSFolder(workspaceName);
            System.out.println("file created "+create);
            return WorkspaceCreateStatus.OK;
        }
    }

    private boolean isWorkspaceAlreadyExists(String workspaceName, List<String> ownedWorkspaces) {
        for (int i = 0; i <ownedWorkspaces.size() ; i++) {
          if(ownedWorkspaces.get(i).toLowerCase().equals(workspaceName)){
              return true;
          }
        }
        return false;
    }

    //ui need this before editing ws details, to populate view. Use this object before editing ws
    public OwnedWorkspace getWorkspace(String workspaceName){
        OwnedWorkspace workspace=metadataManager.getOwnedWorkspace(workspaceName);
        return workspace;
    }


    public boolean editOwnedWorkspace(String workspaceName, OwnedWorkspace editedWS){
        boolean isQuotaSmallerThanUsage=isQuotaSmallerThanUsage(workspaceName, editedWS.getQuota());
        if(isQuotaSmallerThanUsage){
            return false;
        }
        else{
            metadataManager.saveOwnedWorkspace(editedWS);
            //TODO: to be notified to clients in same network about the edit
            return true;
        }
    }

    public boolean deleteOwnedWorkspace(String workspaceName){
     //remove from ownedWSList, remove all clients foreignWSList,delete metadata file
        User user=userManager.getOwner();
        user.removeFromOwnedWorkspaceList(workspaceName);

        //remove from foreign list to simulate self mount.
        //TODO: Change this to notify wifidirect and then call that clients changeforeignwsList
        //
         user.removeFromForeignWorkspaceList(workspaceName);//TODO:has to be removed later

        OwnedWorkspace ownedWorkspace=getWorkspace(workspaceName);
        Set<String> clients=ownedWorkspace.getClients().keySet();
        List<String>accessList=getClientList(clients);
        user.addClientsToDeletedWorkspacesMap(workspaceName, accessList);//to send notifications for deleted workspaces

         metadataManager.deleteOwnedWorkspace(workspaceName);

        //detete owned WS folder
         boolean statusOwned=FileUtils.deleteOwnedWorkspaceFolder(workspaceName);

        //TODO: later change this to notify all clients about deletion

         boolean statusForeign=FileUtils.deleteForeignWorkspaceFolder(workspaceName,user.getNickName());  //only to simulate self mount. remove later
         System.out.println("workspace folder delete status :owned"+statusOwned+" foreign"+statusForeign);

        //save user with new changes
        userManager.createUser(user);//save user with updated owned and foreign WS and updated deletedWSMap
        return true;
    }

    private List<String> getClientList(Set<String> clients) {
        List<String>clientList=new ArrayList<String>();

        for (String client : clients) {
          clientList.add(client);
        }
        return clientList;
        }


    /*make this public if don't do a prevalidation on textbox*/
    public boolean isNotSufficientMemory(double quotaSize){
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        int availBlocks = stat.getFreeBlocks();
        int blockSize = stat.getBlockSize();
        double free_memory = (((long)availBlocks * (long)blockSize)/(double)Constants.BYTES_PER_KB);//available free memory on internal storage
        System.out.println("free memory is " + free_memory);
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

    public void addUserToAccessList(String workspace,String userId) throws Exception {
        //Both from the subscription and adding email by owner
        //To simulate mount, add to foreign ws of same user.
        // TODO:Later change this to use wifidirect
        OwnedWorkspace ownedWorkspace=getWorkspace(workspace);
        ownedWorkspace.addClient(userId,false);//still client is inactive

        //TODO:notify user background job to send workspace to inactive clients, when they receive msg, make them active
        //TODO: received clients should add that workspace to their foreign space, and to their foreign workspace list

        addToForeignWorkspace(workspace,ownedWorkspace.getOwnerId(),ownedWorkspace.getQuota(),ownedWorkspace.getFileNames().toArray(new String[ownedWorkspace.getFileNames().size()]));
    }

    public void addToForeignWorkspace(String workspaceName, String ownerId, double quota, String[] fileNames) throws Exception {
        User user = userManager.getOwner();
        user.addForeignWS(workspaceName);
        userManager.updateUser(user);

        ForeignWorkspace foreignWorkspace = new ForeignWorkspace(workspaceName, ownerId, quota);
        foreignWorkspace.addFiles(fileNames);
        metadataManager.saveForeignWorkspace(foreignWorkspace);

        //TODO check whether we need this
        /*File createdDir = storageManager.createFolderStructureOnForeignWSAddition(ownerId, workspaceName);
        if (createdDir.exists()) {
            ForeignWorkspace workspace = new ForeignWorkspace(workspaceName, ownerId, quota);
            workspace.addFiles(fileNames);
            metadataManager.saveForeignWorkspace(workspace);
        }*/
    }

    public void createDataFile(String workspace, String fileName, String ownerId, boolean isOwned) throws Exception {

        boolean isCreated= storageManager.createDataFile(workspace, fileName, ownerId, isOwned);

        //updating metadata
        if(isCreated){
            if(isOwned){
                OwnedWorkspace ownedWorkspace=metadataManager.getOwnedWorkspace(workspace);
                ownedWorkspace.addFile(fileName);
                metadataManager.saveOwnedWorkspace(ownedWorkspace);//add new file to metadata and save it
            }
            else{
                ForeignWorkspace foreignWorkspace=metadataManager.getForeignWorkspace(fileName);
                foreignWorkspace.addFile(fileName);
                metadataManager.saveForeignWorkspace(foreignWorkspace);//add new file to metadata and save it
            }
        }
    }

    public FileInputStream getDataFile(String workspace, String fileName, boolean writeMode, String ownerId, boolean isOwned) throws IOException, WriteLockedException {
        return storageManager.getDataFile(workspace, fileName, writeMode, ownerId, isOwned);
    }

    public void updateDataFile(String workspace, String fileName, FileInputStream inputStream, String ownerId, boolean isOwned) throws IOException {
        storageManager.updateDataFile(workspace, fileName, inputStream, ownerId, isOwned);
    }

    public void deleteDataFile(String workspace, String fileName, String ownerId, boolean isOwned) throws IOException {
        boolean isDeleted = storageManager.deleteDataFile(workspace, fileName, ownerId, isOwned);

        //updating metadata
        if(isDeleted){
            if(isOwned){
                OwnedWorkspace ownedWorkspace=metadataManager.getOwnedWorkspace(workspace);
                ownedWorkspace.removeFile(fileName);
                metadataManager.saveOwnedWorkspace(ownedWorkspace);//add new file to metadata and save it
            }
            else{
                ForeignWorkspace foreignWorkspace=metadataManager.getForeignWorkspace(fileName);
                foreignWorkspace.removeFile(fileName);
                metadataManager.saveForeignWorkspace(foreignWorkspace);//add new file to metadata and save it
            }
        }
    }

}
