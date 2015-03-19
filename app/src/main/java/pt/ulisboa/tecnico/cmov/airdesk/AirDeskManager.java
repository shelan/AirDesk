package pt.ulisboa.tecnico.cmov.airdesk;

import android.test.ApplicationTestCase;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;
import pt.ulisboa.tecnico.cmov.airdesk.test.WorkspaceTest;

/**
 * Created by ashansa on 3/19/15.
 */
public class AirDeskManager {

    private WorkspaceManager workspaceManager;
    private UserManager userManager;

    public AirDeskManager() {
        workspaceManager = new WorkspaceManager();
        userManager = new UserManager();
    }

    public void createUser(String userName) {

    }

    public List<String> getOwnedWorkspaces() {
        // TODO temp for testing
        new WorkspaceTest().populateOwnedWorkspaces();
        return userManager.getOwnedWorkspaces();
    }
}
