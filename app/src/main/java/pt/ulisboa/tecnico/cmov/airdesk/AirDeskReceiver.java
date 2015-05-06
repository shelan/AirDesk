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
            case Constants.SUBSCRIBE_TAGS_MSG:
                System.out.printf("tag subscription wifi direct walin awooo................");
                ArrayList<String> tags = (ArrayList<String>) msg.getInputs().get(Constants.TAGS);
                HashMap<OwnedWorkspace, String[]> matchingWorkspacesMap = workspaceManager.
                        getPublicWorkspacesForTags(tags.toArray(new String[tags.size()]));
                ///////TODO : send correct details
                airDeskService.sendPublicWorkspacesForTags(matchingWorkspacesMap, msg.getSenderIp());
                break;
            case Constants.PUBLIC_WORKSPACES_FOR_TAGS_MSG:
                System.out.println(".............. reply awooooooo............");
                System.out.println("..........................................");
                //gson makes ForeignWorkspace to a LinkedTreeMap
                ArrayList<LinkedTreeMap> matchingWorkspaces = (ArrayList) msg.getInputs().get(Constants.MATCHING_WORKSPACES_FOR_TAGS);
                System.out.println(".......matching workspaces size ........" + matchingWorkspaces.size());
                System.out.println("..........................................");
                System.out.println("..........................................");
                for (LinkedTreeMap workspace : matchingWorkspaces) {
                    try {
                        ArrayList<String> files = (ArrayList<String>) workspace.get(Constants.FILE_NAMES);
                        ArrayList<String> matchingTags = (ArrayList<String>) workspace.get(Constants.MATCHING_TAGS);
                        workspaceManager.addToForeignWorkspace((String)workspace.get(Constants.WORKSPACE_NAME),
                                (String)workspace.get(Constants.OWNER_ID), (Double)workspace.get(Constants.QUOTA),
                                files.toArray(new String[files.size()]),
                                matchingTags.toArray(new String[matchingTags.size()]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                foreignWorkspaceFragment.updateWorkspaceList();
                break;
            default:
                System.out.println("........ default case .......");
        }
    }
}
