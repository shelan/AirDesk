package pt.ulisboa.tecnico.cmov.airdesk;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.AirDeskMessage;

public class AirDeskReceiver {

    WorkspaceManager workspaceManager = new WorkspaceManager();
    AirDeskService airDeskService = AirDeskService.getInstance();

    public void handleMessage(AirDeskMessage msg) {
        System.out.println("........handleMessage.........");
        switch (msg.getType()) {
            case Constants.SUBSCRIBE_TAGS_MSG:
                System.out.printf("tag subscription wifi direct walin awooo................");
                ArrayList<String> tags = (ArrayList<String>) msg.getInputs().get(Constants.TAGS);
                ArrayList<OwnedWorkspace> matchingWorkspaces = workspaceManager.
                        getPublicWorkspacesForTags(tags.toArray(new String[tags.size()]));
                airDeskService.sendPublicWorkspacesForTags(null, msg.getSenderIp());
                break;
            case Constants.PUBLIC_WORKSPACES_FOR_TAGS:
                System.out.println(".............. reply awooooooo............");
                System.out.println("..........................................");
                System.out.println("..........................................");
                System.out.println("..........................................");
                System.out.println("..........................................");
            default:
                System.out.println("........ default case .......");
        }
    }
}
