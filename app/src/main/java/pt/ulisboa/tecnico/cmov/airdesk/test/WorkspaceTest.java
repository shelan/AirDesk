package pt.ulisboa.tecnico.cmov.airdesk.test;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

/**
 * Created by Chathuri on 3/19/2015.
 */
public class WorkspaceTest {
    public void populateWorkspaces(){
        User user = new User();
        user.setEmail("abc@gmail.com");
        user.setNickName("aaa");
        UserManager userManager=new UserManager();
        userManager.createUser(user);

        WorkspaceManager workspaceManager = new WorkspaceManager();

        for(int i=0;i<10;i++) {
            OwnedWorkspace workspace = new OwnedWorkspace("abc_ws"+i, "aaa", 2.5);
            workspaceManager.createWorkspace(workspace);
        }

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        List<String> ws =userManager.getOwner().getOwnedWorkspaces();
        for(String s : ws)
            System.out.println(s);

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }
}
