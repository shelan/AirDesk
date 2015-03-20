package pt.ulisboa.tecnico.cmov.airdesk;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

/**
 * Created by Chathuri on 3/19/2015.
 */
public class WorkspaceTest {
    public void populateOwnedWorkspaces(){
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
        List<String> ws =userManager.getOwnedWorkspaces();
        for(String s : ws)
            System.out.println(s);

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }



    public void populateForeignWorkspaces(){
        UserManager userManager=new UserManager();
        WorkspaceManager workspaceManager=new WorkspaceManager();
        try {
            for (int i = 0; i < 10; i++) {
                workspaceManager.addToForeignWorkspace("abc_ForeignWorkspace" + i, "aaa", 500, null);

            }
            List<String>foreignWS=userManager.getForeignWorkspaces();
            System.out.println("foreign ws size "+foreignWS.size());
            System.out.println("###########");
            for(int i=0;i<foreignWS.size();i++){
                System.out.println(foreignWS.get(i));
            }

            System.out.println("#########");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
