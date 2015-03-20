package pt.ulisboa.tecnico.cmov.airdesk;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

/**
 * Created by ashansa on 3/19/15.
 */
public class PopulateData {

    public void populateOwnedWorkspaces(){
        User user = new User();
        user.setEmail("abc@gmail.com");
        user.setNickName("aaa");
        UserManager userManager=new UserManager();
        userManager.createUser(user);

        WorkspaceManager workspaceManager = new WorkspaceManager();

        for(int i=0;i<10;i++) {
            OwnedWorkspace workspace = new OwnedWorkspace("abc_ws"+i, "aaa", 2.5);
            workspace.addFile("file1");
            workspace.addFile("file2");
            workspace.addFile("file3");
            workspace.addFile("file4");
            workspace.addFile("file5");
            workspaceManager.createWorkspace(workspace);
        }

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        List<String> ws =userManager.getOwnedWorkspaces();
        for(String s : ws)
            System.out.println(s);

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }
}
