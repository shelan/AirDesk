package pt.ulisboa.tecnico.cmov.airdesk.manager;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.FileUtils;
import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.enums.WorkspaceCreateStatus;
import pt.ulisboa.tecnico.cmov.airdesk.enums.WorkspaceEditStatus;
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
            userManager.updateOwner(user);//update existing user with new workspace
            System.out.println("------user created------");

            workspace.setOwnerName(user.getNickName());
            workspace.setOwnerId(user.getUserId());
            metadataManager.saveOwnedWorkspace(workspace);

            System.out.println("----ws metadata------");
            boolean create= FileUtils.createWSFolder(workspaceName);
            System.out.println("file created "+create);

            List<String> tags = workspace.getTags();
            if(tags.size() > 0) {
                publishTags( tags.toArray(new String[tags.size()]));
            }
            return WorkspaceCreateStatus.OK;
        }
    }

    private boolean isWorkspaceAlreadyExists(String workspaceName, List<String> ownedWorkspaces) {
        for (int i = 0; i <ownedWorkspaces.size() ; i++) {
          if(ownedWorkspaces.get(i).toLowerCase().equals(workspaceName.toLowerCase())){
              return true;
          }
        }
        return false;
    }

    //ui need this before editing ws details, to populate view. Use this object before editing ws
    public OwnedWorkspace getOwnedWorkspace(String workspaceName){
        OwnedWorkspace workspace=metadataManager.getOwnedWorkspace(workspaceName);
        return workspace;
    }

    public ForeignWorkspace getForeignWorkspace(String workspaceName, String ownerId){
        return metadataManager.getForeignWorkspace(workspaceName, ownerId);
    }


    public WorkspaceEditStatus editOwnedWorkspace(String workspaceName, OwnedWorkspace editedWS, boolean tagsChange){
        boolean isQuotaSmallerThanUsage=isQuotaSmallerThanUsage(workspaceName, editedWS.getQuota());
        boolean isMemoryNotSufficient=isNotSufficientMemory(editedWS.getQuota());
        if(isQuotaSmallerThanUsage){
            return WorkspaceEditStatus.WORKSPACE_LARGER_THAN_QUOTA ;//current workspace occupied more than quota
        }
        else if(isMemoryNotSufficient){
            return WorkspaceEditStatus.INSUFFICIENT_MEMORY;//devide memory is smaller than quota
        }
        else{
            metadataManager.saveOwnedWorkspace(editedWS);
            if(tagsChange)
                publishTags(editedWS.getTags().toArray(new String[editedWS.getTags().size()]));

            //TODO: to be notified to clients in same network about the edit
            return WorkspaceEditStatus.OK;
        }
    }

    private void publishTags(String[] tags) {
        //TODO call through network
        receivePublishedTags(userManager.getOwner().getUserId(), tags);
    }

    public void receivePublishedTags(String ownerId, String[] tags) {
        //match to subscribed tags n request Workspace
        boolean hasMatchingTags = false;
        HashSet<String> subscribedTags =  userManager.getOwner().getSubscribedTags();
        for (String tag : subscribedTags) {
            if(Arrays.asList(tags).contains(tag)) {
                hasMatchingTags = true;
                break;
            }
        }
        if(hasMatchingTags) {
            //TODO call through network
            getPublicWorkspacesForTags(subscribedTags.toArray(new String[subscribedTags.size()]));
        }
    }

    public void getPublicWorkspacesForTags(String[] subscribedTags) {
        OwnedWorkspace workspace;
        for (String workspaceName : userManager.getOwnedWorkspaces()) {
            workspace = getOwnedWorkspace(workspaceName);
            if(workspace.isPublic()) {
                boolean isTagMatching = false;
                ArrayList<String> matchingTags = new ArrayList<String>();
                for (String subscribedTag : subscribedTags) {
                    if(workspace.getTags().contains(subscribedTag)) {
//                        isTagMatching = true;
//                        break;
                        matchingTags.add(subscribedTag);
                    }
                }

                //TODO call in Network
                //if(isTagMatching) {
                if(matchingTags.size() > 0) {
                    try {
                        addToForeignWorkspace(workspaceName, workspace.getOwnerId(), workspace.getQuota(),
                                workspace.getFileNames().toArray(new String[workspace.getFileNames().size()]),
                                matchingTags.toArray(new String[matchingTags.size()]));
                    } catch (Exception e) {
                        System.out.println("Could not add the workspace " + workspaceName + " to foreign workspace");
                    }
                }

            }
        }
    }

    public void subscribeToTags(String[] tags) {
        userManager.subscribeToTags(tags);
        //Do with wifi direct to all other available users
        getPublicWorkspacesForTags(tags);
    }

    public void unsubscribeFromTags(String[] tags) {
        HashSet<String> taggedWorkspaces = userManager.unsubscribeFromTags(tags);
        for (String workspaceName : taggedWorkspaces) {
            //foreign workspace names will be saved as <wsOwnerId>/<workspaceName>
            String[] splits = workspaceName.split("/");
            if(splits.length == 2) {
                removeFromForeignWorkspace(splits[1], splits[0]);
            }
        }
    }

    public boolean deleteOwnedWorkspace(String workspaceName){
     //remove from ownedWSList, remove all clients foreignWSList,delete metadata file
        User user=userManager.getOwner();
        user.removeFromOwnedWorkspaceList(workspaceName);

        //remove from foreign list to simulate self mount.
        //TODO: Change this to notify wifidirect and then call that clients changeforeignwsList
        //
         user.removeFromForeignWorkspaceList(user.getUserId().concat("/").concat(workspaceName));//TODO:has to be removed later

        OwnedWorkspace ownedWorkspace= getOwnedWorkspace(workspaceName);
        Set<String> clients=ownedWorkspace.getClients().keySet();
        List<String>accessList=getClientList(clients);
        user.updateDeletedWorkspacesMap(workspaceName, accessList);//to send notifications for deleted workspaces

         metadataManager.deleteOwnedWorkspace(workspaceName);

        //detete owned WS folder
         boolean statusOwned=FileUtils.deleteOwnedWorkspaceFolder(workspaceName);

        //TODO: later change this to notify all clients about deletion
         metadataManager.deleteForeignWorkspace(workspaceName,user.getUserId());
         boolean statusForeign=storageManager.deleteFolderForForeignWorkspace(workspaceName,user.getUserId());  //only to simulate self mount. remove later
         System.out.println("workspace folder delete status :owned"+statusOwned+" foreign"+statusForeign);

        //save user with new changes
        userManager.updateOwner(user);//.createOwner(user);//save user with updated owned and foreign WS and updated deletedWSMap
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

    public void addClientToWorkspace(String workspace, String userId) throws Exception {
        //Both from the subscription and adding email by owner
        //To simulate mount, add to foreign ws of same user.
        // TODO:Later change this to use wifidirect
        OwnedWorkspace ownedWorkspace= getOwnedWorkspace(workspace);
        ownedWorkspace.addClient(userId,false);//still client is inactive

        //TODO:notify user background job to send workspace to inactive clients, when they receive msg, make them active
        //TODO: received clients should add that workspace to their foreign space, and to their foreign workspace list

        //temp calling directly
        if(userManager.getOwner().getUserId().equals(userId) && !userManager.getForeignWorkspaces()
                .contains(userId.concat("/").concat(workspace))) {
            addToForeignWorkspace(workspace,ownedWorkspace.getOwnerId(),ownedWorkspace.getQuota(),
                    ownedWorkspace.getFileNames().toArray(new String[ownedWorkspace.getFileNames().size()]), null);
        }

    }

    public void deleteUserFromAccessList(String workspace, String userId){

        //remove him from clients for workspace
        OwnedWorkspace ownedWorkspace=getOwnedWorkspace(workspace);
        ownedWorkspace.removeClient(userId);
        ownedWorkspace.addClientToRemoveList(userId);//this list will be used by wifidirect to notify removed users

        //because of the self mount we have to remove him from foreign ws
        //TODO: remove this after introducing wifidirect
        //delete foreignWS folder and files, delete foreignWS metadata
        removeFromForeignWorkspace(workspace,userId);

    }


    /**
     *
     * @param workspaceName
     * @param ownerId
     * @param quota
     * @param fileNames
     * @param matchingTags pass the tags if added due to tag subscription. Else pass null ( if added with cient addition)
     * @throws Exception
     */
    public void addToForeignWorkspace(String workspaceName, String ownerId, double quota, String[] fileNames, String[] matchingTags) throws Exception {
        User user = userManager.getOwner();
        String uniqueWorkspaceName = ownerId.concat("/").concat(workspaceName);
        if(!user.getForeignWorkspaces().contains(uniqueWorkspaceName)) {
            user.addForeignWS(uniqueWorkspaceName);
            if(matchingTags != null && matchingTags.length > 0) {
                for (String tag : matchingTags) {
                    user.addTagForeignWorkspaceMapping(tag, uniqueWorkspaceName);
                }
            }

            userManager.updateOwner(user);

            ForeignWorkspace foreignWorkspace = new ForeignWorkspace(workspaceName, ownerId, quota);

            if(fileNames!=null)
                foreignWorkspace.addFiles(fileNames);

            metadataManager.saveForeignWorkspace(foreignWorkspace, ownerId);

            File createdDir = storageManager.createFolderForForeignWorkspace(ownerId, workspaceName);
            if(createdDir == null)
                throw new Exception("Could not create the foreign workspace directory : " + workspaceName);
        }
    }

    public void removeFromForeignWorkspace(String workspaceName,String workspaceOwnerId){
        User user = userManager.getOwner();
        user.removeFromForeignWorkspaceList(workspaceOwnerId.concat("/").concat(workspaceName));
        userManager.updateOwner(user);

        metadataManager.deleteForeignWorkspace(workspaceName, workspaceOwnerId);

        //TODO Check whether we need to delete the foreign_ws folder,
        String foreignWSFolderPath=Constants.FOREIGN_WORKSPACE_DIR+File.separator+workspaceOwnerId +
                File.separator + workspaceName;
        FileUtils.deleteFolder(foreignWSFolderPath);
    }

    public void createDataFile(String workspace, String fileName, String ownerId, boolean isOwned) throws Exception {

        boolean isCreated= storageManager.createDataFile(workspace, fileName, ownerId, isOwned);

        //add new file to metadata and save it
        if(isCreated){
            if(isOwned){
                OwnedWorkspace ownedWorkspace=metadataManager.getOwnedWorkspace(workspace);
                ownedWorkspace.addFile(fileName);
                metadataManager.saveOwnedWorkspace(ownedWorkspace);//add new file to metadata and save it
            }
            else{
                ForeignWorkspace foreignWorkspace=metadataManager.getForeignWorkspace(workspace, ownerId);
                foreignWorkspace.addFile(fileName);
                metadataManager.saveForeignWorkspace(foreignWorkspace, ownerId);
                //notify owner about file creation
                //TODO: do with wifi direct
                receiveFileCreateEvent(workspace, fileName);
            }
        }
    }

    /**
     * If a client create a file in a foreign workspace, he should call this method for workspace owner
     * @param workspace
     * @param fileName
     * @throws Exception
     */
    public void receiveFileCreateEvent(String workspace, String fileName) throws Exception {
        createDataFile(workspace, fileName, userManager.getOwner().getUserId(), true);
    }

    public FileInputStream getDataFile(String workspace, String fileName, boolean writeMode, String ownerId, boolean isOwned) throws IOException, WriteLockedException {
        return storageManager.getDataFile(workspace, fileName, writeMode, ownerId, isOwned);
    }

    public void updateDataFile(String workspace, String fileName, String content, String ownerId, boolean isOwned) throws IOException {
        storageManager.updateDataFile(workspace, fileName, content, ownerId, isOwned);
        if(!isOwned) {
            //notify owner about file update
            //TODO: do with wifi direct
            receiveFileUpdateEvent(workspace, fileName, content);
        }
    }

    public void receiveFileUpdateEvent(String workspace, String fileName, String content) throws IOException {
        updateDataFile(workspace, fileName, content, userManager.getOwner().getUserId(), true);
    }

    public void deleteDataFile(String workspace, String fileName, String ownerId, boolean isOwned) throws IOException {
        boolean isDeleted = storageManager.deleteDataFile(workspace, fileName, ownerId, isOwned);

        //delete file from metadata and save it
        if(isDeleted){
            if(isOwned){
                OwnedWorkspace ownedWorkspace=metadataManager.getOwnedWorkspace(workspace);
                ownedWorkspace.removeFile(fileName);
                metadataManager.saveOwnedWorkspace(ownedWorkspace);//add new file to metadata and save it
            }
            else{
                ForeignWorkspace foreignWorkspace=metadataManager.getForeignWorkspace(workspace, ownerId);
                foreignWorkspace.removeFile(fileName);
                metadataManager.saveForeignWorkspace(foreignWorkspace, ownerId);//add new file to metadata and save it
                //notify owner about file deletion
                //TODO: do with wifi direct
                receiveFileDeletionEvent(workspace, fileName);
            }
        }
    }

    public void receiveFileDeletionEvent(String workspace, String fileName) throws IOException {
        deleteDataFile(workspace, fileName, userManager.getOwner().getUserId(), true);
    }

}
