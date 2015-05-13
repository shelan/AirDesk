package pt.ulisboa.tecnico.cmov.airdesk;

import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForeignWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.AirDeskMessage;

public class AirDeskReceiver {

    WorkspaceManager workspaceManager = new WorkspaceManager();
    AirDeskService airDeskService = AirDeskService.getInstance();
    ForeignWorkspaceListFragment foreignWorkspaceFragment;

    public AirDeskReceiver(ForeignWorkspaceListFragment foreignWorkspaceFragment) {
        this.foreignWorkspaceFragment = foreignWorkspaceFragment;
    }

    public void handleMessage(AirDeskMessage msg) {
        System.out.println("..........................");
        System.out.println("... handle msg: " + msg.getType());
        System.out.println("..........................");
        String senderIP  = msg.getSenderIP();
        switch (msg.getType()) {

            case Constants.INTRODUCE_MSG:
                String senderID = msg.getSenderID();
                airDeskService.addIdIpMapping(senderID, senderIP);
                break;

            case Constants.ID_IP_MAP_REQUEST_MSG:
                airDeskService.sendIdIpMap(msg.getSenderIP());
                break;

            case Constants.ID_IP_MAP_REPLY_MSG:
                LinkedTreeMap idIPMap = (LinkedTreeMap) msg.getInputs().get(Constants.ID_IP_MAP);
                if(idIPMap.size() > 0) {
                    airDeskService.updateIdIPMap(idIPMap);
                } else {
                    System.out.println("not sending idIPMap since no entries...");
                }
                break;

            case Constants.SUBSCRIBE_TAGS_MSG:
                System.out.println("tag subscription wifi direct walin awooo................");
                ArrayList<String> subscribedTags = (ArrayList<String>) msg.getInputs().get(Constants.TAGS);
                String clientId = (String) msg.getInputs().get(Constants.CLIENT_ID);
                HashMap<OwnedWorkspace, String[]> matchingWorkspacesMap = workspaceManager.
                        getPublicWorkspacesForTags(subscribedTags.toArray(new String[subscribedTags.size()]), clientId);
                if(matchingWorkspacesMap.size() > 0) {
                    airDeskService.sendPublicWorkspacesForTags(matchingWorkspacesMap, msg.getSenderIP());
                } else {
                    System.out.println("no matching workspaces for published tags.....");
                }
                break;

            case Constants.ADD_TO_FOREIGN_WORKSPACE_MSG:
                //gson makes ForeignWorkspace to a LinkedTreeMap
                ArrayList<LinkedTreeMap> matchingWorkspaces = (ArrayList) msg.getInputs().get(Constants.WORKSPACES);
                System.out.println(".......matching workspaces size ........" + matchingWorkspaces.size());
                for (LinkedTreeMap workspace : matchingWorkspaces) {
                    try {
                        ArrayList<String> files = (ArrayList<String>) workspace.get(Constants.FILE_NAMES);
                        ArrayList<String> matchingTags = (ArrayList<String>) workspace.get(Constants.MATCHING_TAGS);
                        if (matchingTags == null) {
                            workspaceManager.addToForeignWorkspace((String) workspace.get(Constants.WORKSPACE_NAME),
                                    (String) workspace.get(Constants.OWNER_ID), (Double) workspace.get(Constants.QUOTA),
                                    files.toArray(new String[files.size()]), new String[0]);
                        } else {
                            workspaceManager.addToForeignWorkspace((String) workspace.get(Constants.WORKSPACE_NAME),
                                    (String) workspace.get(Constants.OWNER_ID), (Double) workspace.get(Constants.QUOTA),
                                    files.toArray(new String[files.size()]),
                                    matchingTags.toArray(new String[matchingTags.size()]));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                foreignWorkspaceFragment.updateWorkspaceList();
                break;

            case Constants.PUBLISH_TAGS_MSG:
                System.out.println("=============== publish tag msg received =========");
                ArrayList<String> publishedTags = (ArrayList<String>) msg.getInputs().get(Constants.TAGS);
                //receivePublishedTags will return subscribed tags only if there are matching tags with published tags
                String[] tags = workspaceManager.receivePublishedTags(publishedTags.toArray(new String[publishedTags.size()]));
                System.out.println("subscribed tag size if there are any matching tags "+ tags.length);
                if(tags.length > 0)
                    AirDeskService.getInstance().broadcastTagSubscription(tags);
                break;

            case Constants.REVOKE_ACCESS_MSG:
                String workspaceName = (String) msg.getInputs().get(Constants.WORKSPACE_NAME);
                String workspaceOwnerId = (String) msg.getInputs().get(Constants.OWNER_ID);
                workspaceManager.removeFromForeignWorkspace(workspaceName, workspaceOwnerId);
                foreignWorkspaceFragment.updateWorkspaceList();
                break;

            case Constants.REQUEST_FILE_MSG:
                try {
                    String workspaceName1 = (String) msg.getInputs().get(Constants.WORKSPACE_NAME);
                    String fileName = (String) msg.getInputs().get(Constants.FILENAME);
                    String ownerId = (String) msg.getInputs().get(Constants.OWNER_ID);
                    boolean writeMod = (Boolean) msg.getInputs().get(Constants.WRITE_MODE);
                    //this msg will be received by file workspace owner
                    if(new UserManager().getOwner().getUserId().equals(ownerId)) {
                        StringBuffer fileContent = workspaceManager.getDataFile(workspaceName1, fileName, writeMod, ownerId, true);
                        //content will be null if a WriteLockedException is thrown. ie: cannot take file in write mode
                        if(fileContent != null) {
                            airDeskService.sendFileContentToClient(senderIP, workspaceName1, fileName, ownerId, fileContent.toString());
                        } else {
                            airDeskService.sendFileContentToClient(senderIP, workspaceName1, fileName, ownerId, null);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Constants.FILE_CONTENT_RESULT_MSG:
                String content = (String) msg.getInputs().get(Constants.FILE_CONTENT);
                System.out.println("================ file content ==================");
                System.out.println(content);
                System.out.println("==================================");
                break;

            case Constants.SAVE_FILE_MSG:
                String ownerId = (String) msg.getInputs().get(Constants.OWNER_ID);
                if(new UserManager().getOwner().getUserId().equals(ownerId)) {
                    String workspaceName2 = (String) msg.getInputs().get(Constants.WORKSPACE_NAME);
                    String fileName = (String) msg.getInputs().get(Constants.FILENAME);
                    String newContent = (String) msg.getInputs().get(Constants.FILE_CONTENT);
                    try {
                        workspaceManager.updateDataFile(workspaceName2, fileName, newContent, ownerId, true);
                    } catch (IOException e) {
                        System.out.println("Error in updating the file in owner space");
                    }
                }
                break;

            default:
                System.out.println("........ default case. Do nothing .......");
        }
    }
}
