package pt.ulisboa.tecnico.cmov.airdesk;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

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
        return userManager.getOwnedWorkspaces();
    }

    public OwnedWorkspace getOwnedWorkspace(String workspace) {
        return workspaceManager.getOwnedWorkspace(workspace);
    }
}
