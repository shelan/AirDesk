package pt.ulisboa.tecnico.cmov.airdesk.manager;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmov.airdesk.AWSTasks;
import pt.ulisboa.tecnico.cmov.airdesk.AirDeskService;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.FileUtils;
import pt.ulisboa.tecnico.cmov.airdesk.entity.AbstractWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.enums.WorkspaceCreateStatus;
import pt.ulisboa.tecnico.cmov.airdesk.enums.WorkspaceEditStatus;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;


public class WorkspaceManager {

    private StorageManager storageManager = new StorageManager();
    private MetadataManager metadataManager = new MetadataManager();
    private UserManager userManager = new UserManager();
    private AirDeskService airDeskService = AirDeskService.getInstance();

    public WorkspaceManager() {
    }

    //set all ui data other than owner data in workspace object
    public WorkspaceCreateStatus createWorkspace(OwnedWorkspace workspace) {
        String workspaceName = workspace.getWorkspaceName();
        boolean isMemoryNotSufficient = isNotSufficientMemory(workspace.getQuota());
        if (isMemoryNotSufficient) {
            return WorkspaceCreateStatus.INSUFFICIENT_MEMORY;
        } else {
            User user = userManager.getOwner();

            boolean isWorkspaceAlreadyExist = isWorkspaceAlreadyExists(workspaceName, user.getOwnedWorkspaces());
            if (isWorkspaceAlreadyExist) {
                return WorkspaceCreateStatus.WORKSPACE_ALREADY_EXISTS;
            }

            user.addNewOwnedWorkspace(workspaceName);
            userManager.updateOwner(user);//update existing user with new workspace
            System.out.println("------user created------");

            workspace.setOwnerName(user.getNickName());
            workspace.setOwnerId(user.getUserId());
            metadataManager.saveOwnedWorkspace(workspace);

            System.out.println("----ws metadata------");
            boolean create = FileUtils.createWSFolder(workspaceName, user.getUserId());
            System.out.println("file created " + create);

            List<String> tags = workspace.getTags();
            if (tags.size() > 0) {
                publishTags(tags.toArray(new String[tags.size()]));
            }

            return WorkspaceCreateStatus.OK;
        }
    }

    private boolean isWorkspaceAlreadyExists(String workspaceName, List<String> ownedWorkspaces) {
        for (int i = 0; i < ownedWorkspaces.size(); i++) {
            if (ownedWorkspaces.get(i).toLowerCase().equals(workspaceName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    //ui need this before editing ws details, to populate view. Use this object before editing ws
    public OwnedWorkspace getOwnedWorkspace(String workspaceName) {
        OwnedWorkspace workspace = metadataManager.getOwnedWorkspace(workspaceName);
        return workspace;
    }

    public ForeignWorkspace getForeignWorkspace(String workspaceName, String ownerId) {
        return metadataManager.getForeignWorkspace(workspaceName, ownerId);
    }


    public WorkspaceEditStatus editOwnedWorkspace(String workspaceName, OwnedWorkspace editedWS, boolean tagsChange) {
        boolean isQuotaSmallerThanUsage = isQuotaSmallerThanUsage(workspaceName, editedWS.getQuota());
        boolean isMemoryNotSufficient = isNotSufficientMemory(editedWS.getQuota());
        if (isQuotaSmallerThanUsage) {
            return WorkspaceEditStatus.WORKSPACE_LARGER_THAN_QUOTA;//current workspace occupied more than quota
        } else if (isMemoryNotSufficient) {
            return WorkspaceEditStatus.INSUFFICIENT_MEMORY;//devide memory is smaller than quota
        } else {
            metadataManager.saveOwnedWorkspace(editedWS);
            if (tagsChange)
                publishTags(editedWS.getTags().toArray(new String[editedWS.getTags().size()]));

            //TODO: to be notified to clients in same network about the edit
            return WorkspaceEditStatus.OK;
        }
    }

    private void publishTags(String[] tags) {
        airDeskService.publishTags(tags);
    }

    /**
     * @param tags published tags
     * @return the subscribed tags IF there are any matching tags with published tags
     */
    public String[] receivePublishedTags(String[] tags) {
        //match to subscribed tags n request Workspace
        boolean hasMatchingTags = false;
        HashSet<String> subscribedTags = userManager.getOwner().getSubscribedTags();
        for (String tag : subscribedTags) {
            if (Arrays.asList(tags).contains(tag)) {
                hasMatchingTags = true;
                break;
            }
        }
        if (hasMatchingTags) {
            return subscribedTags.toArray(new String[subscribedTags.size()]);
        }
        return new String[0];
    }

    public HashMap<OwnedWorkspace, String[]> getPublicWorkspacesForTags(String[] subscribedTags, String clientId) {
        OwnedWorkspace workspace;
        ArrayList<OwnedWorkspace> matchingWorkspaces = new ArrayList<OwnedWorkspace>();
        HashMap<OwnedWorkspace, String[]> matchingWorkspaceMap = new HashMap<OwnedWorkspace, String[]>();
        for (String workspaceName : userManager.getOwnedWorkspaces()) {
            workspace = getOwnedWorkspace(workspaceName);
            if (workspace.isPublic()) {
                boolean isTagMatching = false;
                ArrayList<String> matchingTags = new ArrayList<String>();
                for (String subscribedTag : subscribedTags) {
                    if (workspace.getTags().contains(subscribedTag)) {
                        matchingTags.add(subscribedTag);
                    }
                }

                System.out.println("========== matching workspace ========> " + workspaceName);
                if (matchingTags.size() > 0) {
                    matchingWorkspaces.add(workspace);
                    matchingWorkspaceMap.put(workspace, matchingTags.toArray(new String[matchingTags.size()]));
                    try {
                        addClientToWorkspace(workspaceName, clientId, false);
                    } catch (Exception e) {
                        System.out.println("Could not add the workspace " + workspaceName + " to foreign workspace");
                    }
                }

            }
        }
        return matchingWorkspaceMap;
    }

    public void subscribeToTags(String[] tags) {
        userManager.subscribeToTags(tags);
        airDeskService.broadcastTagSubscription(tags);
    }

    public void unsubscribeFromTags(String[] tags) {
        HashSet<String> taggedWorkspaces = userManager.unsubscribeFromTags(tags);
        for (String workspaceName : taggedWorkspaces) {
            //foreign workspace names will be saved as <wsOwnerId>/<workspaceName>
            String[] splits = workspaceName.split("/");
            if (splits.length == 2) {
                removeFromForeignWorkspace(splits[1], splits[0]);
            }
        }
    }

    public boolean deleteOwnedWorkspace(String workspaceName) {
        //remove from ownedWSList, remove all clients foreignWSList,delete metadata file
        User user = userManager.getOwner();
        user.removeFromOwnedWorkspaceList(workspaceName);

        //remove from foreign list to simulate self mount.
        //TODO: Change this to notify wifidirect and then call that clients changeforeignwsList
        //
        user.removeFromForeignWorkspaceList(user.getUserId().concat("/").concat(workspaceName));//TODO:has to be removed later

        OwnedWorkspace ownedWorkspace = getOwnedWorkspace(workspaceName);
        Set<String> clients = ownedWorkspace.getClients().keySet();
        List<String> accessList = getClientList(clients);
        user.updateDeletedWorkspacesMap(workspaceName, accessList);//to send notifications for deleted workspaces



        for (String clientId : accessList) {
            airDeskService.revokeAccessFromClient(workspaceName,
                    userManager.getOwner().getUserId(), clientId);
        }

        metadataManager.deleteOwnedWorkspace(workspaceName);

        //save user with new changes
        userManager.updateOwner(user);//.createOwner(user);//save user with updated owned and foreign WS and updated deletedWSMap

        //detete owned WS folder
        boolean statusOwned = FileUtils.deleteOwnedWorkspaceFolder(workspaceName, user.getUserId());

        return true;
    }

    private List<String> getClientList(Set<String> clients) {
        List<String> clientList = new ArrayList<String>();

        for (String client : clients) {
            clientList.add(client);
        }
        return clientList;
    }

    public double getCurrentWorkspaceSize(String workspaceName) {
        return FileUtils.getCurrentFolderSize(workspaceName);
    }

    public double getMaximumDeviceSpace() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        int availBlocks = stat.getFreeBlocks();
        int blockSize = stat.getBlockSize();
        //return (((long) availBlocks * (long) blockSize) / (double) Constants.BYTES_PER_MB);//available free memory on internal storage
        return 5;
    }

    /*make this public if don't do a prevalidation on textbox*/
    public boolean isNotSufficientMemory(double quotaSize) {

        double free_memory = getMaximumDeviceSpace();
        System.out.println("free memory is " + free_memory);
        if (free_memory < quotaSize) {
            return true;
        }
        return false;
    }

    public boolean isQuotaSmallerThanUsage(String workspaceName, double quotaSize) {
        //when user edit quota size, use this to  check he has not reduced it below workspace size
        double wsFolderSize = FileUtils.folderSize(workspaceName);
        if (wsFolderSize > quotaSize) {
            return true;
        }
        return false;
    }

    /**
     * @param workspace
     * @param clientId
     * @param addedByOwner true if owner add a client to his private workspace.
     *                     false if workspace is added due to a matching tag
     * @throws Exception
     */
    public void addClientToWorkspace(String workspace, String clientId, boolean addedByOwner) throws Exception {
        //Both from the subscription and adding email by owner
        //To simulate mount, add to foreign ws of same user.
        // TODO:Later change this to use wifidirect
        OwnedWorkspace ownedWorkspace = getOwnedWorkspace(workspace);
        ownedWorkspace.addClient(clientId, false);//still client is inactive
        editOwnedWorkspace(ownedWorkspace.getWorkspaceName(), ownedWorkspace, false);

        if (addedByOwner) {
            airDeskService.sendWorkspaceToClient(ownedWorkspace, clientId);
        }
        //else part : if added due to matching tags it is send to client when matching the public workspaces
    }

    public void deleteUserFromAccessList(String workspaceName, String clientId) {

        //remove him from clients for workspaceName
        OwnedWorkspace ownedWorkspace = getOwnedWorkspace(workspaceName);
        ownedWorkspace.removeClient(clientId);
        ownedWorkspace.addClientToRemoveList(clientId);//this list will be used by wifidirect to notify removed users
        editOwnedWorkspace(workspaceName, ownedWorkspace, false);

        airDeskService.revokeAccessFromClient(workspaceName,
                userManager.getOwner().getUserId(), clientId);

    }


    /**
     * @param workspaceName
     * @param ownerId
     * @param quota
     * @param fileNames
     * @param matchingTags  pass the tags if added due to tag subscription. Else pass null ( if added with cient addition)
     * @throws Exception
     */
    public void addToForeignWorkspace(String workspaceName, String ownerId, double quota, String[] fileNames, String[] matchingTags) throws Exception {
        User user = userManager.getOwner();
        String uniqueWorkspaceName = ownerId.concat("/").concat(workspaceName);
        if (!user.getForeignWorkspaces().contains(uniqueWorkspaceName)) {
            user.addForeignWS(uniqueWorkspaceName);
            if (matchingTags != null && matchingTags.length > 0) {
                for (String tag : matchingTags) {
                    user.addTagForeignWorkspaceMapping(tag, uniqueWorkspaceName);
                }
            }

            userManager.updateOwner(user);

            ForeignWorkspace foreignWorkspace = new ForeignWorkspace(workspaceName, ownerId, quota);

            if (fileNames != null)
                foreignWorkspace.addFiles(fileNames);

            metadataManager.saveForeignWorkspace(foreignWorkspace, ownerId);

            File createdDir = storageManager.createFolderForForeignWorkspace(ownerId, workspaceName);
            if (createdDir == null)
                throw new Exception("Could not create the foreign workspace directory : " + workspaceName);
        }
    }

    public void updateForeignWorkspaceFileList(String workspaceName, String ownerId, String[] fileNames) {
        ForeignWorkspace foreignWorkspace = metadataManager.getForeignWorkspace(workspaceName, ownerId);
        if (foreignWorkspace != null) {
            foreignWorkspace.replaceFileNames(fileNames);
            metadataManager.saveForeignWorkspace(foreignWorkspace, ownerId);
        }
    }

    public void removeFromForeignWorkspace(String workspaceName, String workspaceOwnerId) {
        User user = userManager.getOwner();
        user.removeFromForeignWorkspaceList(workspaceOwnerId.concat("/").concat(workspaceName));
        userManager.updateOwner(user);

        metadataManager.deleteForeignWorkspace(workspaceName, workspaceOwnerId);

        //TODO Check whether we need to delete the foreign_ws folder,
        String foreignWSFolderPath = Constants.FOREIGN_WORKSPACE_DIR + File.separator + workspaceOwnerId +
                File.separator + workspaceName;
        FileUtils.deleteFolder(foreignWSFolderPath);
    }

    public void createDataFile(String workspaceName, String fileName, String ownerId, boolean isOwned) throws Exception {

        if (isOwned) {
            boolean isCreated = storageManager.createDataFile(workspaceName, fileName, ownerId, isOwned);
            if (isCreated) {
                OwnedWorkspace ownedWorkspace = metadataManager.getOwnedWorkspace(workspaceName);
                ownedWorkspace.addFile(fileName);
                metadataManager.saveOwnedWorkspace(ownedWorkspace);//add new file to metadata and save it

                //notify clients
                sendFileListToClients(ownedWorkspace);
            }
        } else {
            //this won't be called. If a file created in foreign workspaceName, will call update file with content
        }
    }

    private void sendFileListToClients(OwnedWorkspace workspace) {
        Set<String> clients = workspace.getClients().keySet();
        if (clients.size() > 0) {
            String[] fileNames = workspace.getFileNames().toArray(new String[workspace.getFileNames().size()]);
            airDeskService.sendUpdatedFileListToClients(workspace.getWorkspaceName(), workspace.getOwnerId(), fileNames, clients.toArray(new String[clients.size()]));
        }

    }

    public StringBuffer getDataFile(String workspaceName, String fileName, boolean writeMode, String ownerId, boolean isOwned) throws IOException {
        StringBuffer dataFileContent = new StringBuffer();
       /* try {
            dataFileContent = storageManager.
                    getDataFileContent(workspaceName, fileName, writeMode, ownerId, isOwned);
        } catch (WriteLockedException e) {
            return null;
        }*/
        //if (dataFileContent == null) {
        if (isOwned) {
            try {
                dataFileContent = storageManager.
                        getDataFileContent(workspaceName, fileName, writeMode, ownerId, isOwned);
            } catch (WriteLockedException e) {
                return null;
            }
            if (dataFileContent == null) {
                //get from aws
                try {
                    dataFileContent = AWSTasks.getInstance().getFile(FileUtils.getFileNameForUserId(ownerId), workspaceName, fileName);
                    //do a restoration
                    updateDataFile(workspaceName, fileName, dataFileContent.toString(), ownerId, isOwned);
                    dataFileContent = storageManager.
                            getDataFileContent(workspaceName, fileName, writeMode, ownerId, isOwned);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (writeMode) {
                //content will be null if cannot get file in write mode
                String content = airDeskService.requestWriteFileFromOwner(workspaceName, fileName, ownerId);
                if (content == null)
                    return null;
                else
                    return new StringBuffer(content);
            } else {
                String content = airDeskService.requestReadFileFromOwner(workspaceName, fileName, ownerId);
                return new StringBuffer(content);
            }
        }
        // }

        return dataFileContent;
    }

    public void updateDataFile(String workspaceName, String fileName, String content, String ownerId, boolean isOwned) throws Exception {

        if (isOwned) {

            boolean fileExists = fileExists(fileName,workspaceName,isOwned,ownerId);
            if (!fileExists) {
                createDataFile(workspaceName, fileName, userManager.getOwner().getUserId(), true);
            }
            storageManager.updateDataFile(workspaceName, fileName, content, ownerId, isOwned);

            try {
                AWSTasks.getInstance().
                        createFile(FileUtils.getFileNameForUserId(ownerId), workspaceName, fileName, content);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //notify owner about file update
            airDeskService.saveFileInOwnerSpace(workspaceName, fileName, ownerId, content);
        }
    }

    public void deleteDataFile(String workspaceName, String fileName, String ownerId, boolean isOwned) throws IOException {

        if (isOwned) {
            boolean isDeleted = storageManager.deleteDataFile(workspaceName, fileName, ownerId, isOwned);
            if (isDeleted) {
                OwnedWorkspace ownedWorkspace = metadataManager.getOwnedWorkspace(workspaceName);
                ownedWorkspace.removeFile(fileName);
                metadataManager.saveOwnedWorkspace(ownedWorkspace);//add new file to metadata and save it

                sendFileListToClients(ownedWorkspace);

                try {
                    AWSTasks.getInstance().deleteFile(FileUtils.getFileNameForUserId(ownerId), workspaceName, fileName);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            airDeskService.deleteForeignFile(workspaceName, fileName, ownerId);
        }
    }

    private boolean fileExists(String filename, String workspaceName, boolean isOwner, String ownerId) {
        AbstractWorkspace workspace = null;
        WorkspaceManager manager = new WorkspaceManager();
        if (isOwner) {
            workspace = manager.getOwnedWorkspace(workspaceName);
           return workspace.getFileNames().contains(filename);
        }else{
            workspace = manager.getForeignWorkspace(workspaceName,ownerId);
           return workspace.getFileNames().contains(filename);
        }
    }
}
