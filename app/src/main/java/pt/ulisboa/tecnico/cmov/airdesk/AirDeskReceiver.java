package pt.ulisboa.tecnico.cmov.airdesk;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForeignWorkspaceListFragment;
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
        switch (msg.getType()) {

            case Constants.INTRODUCE_MSG:
                String senderID = msg.getSenderID();
                String ownerIdOfSender  = (String) msg.getInputs().get(Constants.SENDER_ID);
                airDeskService.addIdIpMapping(ownerIdOfSender, senderID);
                break;

            case Constants.SUBSCRIBE_TAGS_MSG:
                System.out.printf("tag subscription wifi direct walin awooo................");
                ArrayList<String> subscribedTags = (ArrayList<String>) msg.getInputs().get(Constants.TAGS);
                String clientId = (String) msg.getInputs().get(Constants.CLIENT_ID);
                HashMap<OwnedWorkspace, String[]> matchingWorkspacesMap = workspaceManager.
                        getPublicWorkspacesForTags(subscribedTags.toArray(new String[subscribedTags.size()]), clientId);
                airDeskService.sendPublicWorkspacesForTags(matchingWorkspacesMap, msg.getSenderID());
                break;

            case Constants.ADD_TO_FOREIGN_WORKSPACE_MSG:
                //gson makes ForeignWorkspace to a LinkedTreeMap
                ArrayList<LinkedTreeMap> matchingWorkspaces = (ArrayList) msg.getInputs().get(Constants.WORKSPACES);
                System.out.println(".......matching workspaces size ........" + matchingWorkspaces.size());
                for (LinkedTreeMap workspace : matchingWorkspaces) {
                    try {
                        ArrayList<String> files = (ArrayList<String>) workspace.get(Constants.FILE_NAMES);
                        ArrayList<String> matchingTags = (ArrayList<String>) workspace.get(Constants.MATCHING_TAGS);
                        if(matchingTags == null) {
                            workspaceManager.addToForeignWorkspace((String)workspace.get(Constants.WORKSPACE_NAME),
                                    (String)workspace.get(Constants.OWNER_ID), (Double)workspace.get(Constants.QUOTA),
                                    files.toArray(new String[files.size()]), new String[0]);
                        } else {
                            workspaceManager.addToForeignWorkspace((String)workspace.get(Constants.WORKSPACE_NAME),
                                    (String)workspace.get(Constants.OWNER_ID), (Double)workspace.get(Constants.QUOTA),
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

            default:
                System.out.println("........ default case. Do nothing .......");
        }
    }
}
