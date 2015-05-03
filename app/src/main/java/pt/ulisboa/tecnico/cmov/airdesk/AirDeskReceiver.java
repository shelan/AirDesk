package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

public class AirDeskReceiver extends BroadcastReceiver {

    WorkspaceManager workspaceManager = new WorkspaceManager();

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(">>>>>>>>>>>>>>>>>>>> received at AirDeskReceiver");
        String action = intent.getAction();
        if(Constants.SUBSCRIBE_TAGS.equals(action)) {
            String[] tags = intent.getStringArrayExtra(Constants.TAGS);
            workspaceManager.getPublicWorkspacesForTags(tags);
            //get matchingTags and call addToForeignWorkspace of caller
        }
    }
}
