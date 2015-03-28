package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Context;

import junit.framework.Assert;

import java.io.File;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

/**
 * Created by ashansa on 3/19/15.
 */
public class PopulateData {

    public void populateOwnedWorkspaces() throws Exception {

        cleanWorkspaeDataNMetadata();

        User user = new User();
        user.setUserId("abc@gmail.com");
        user.setNickName("aaa");
        UserManager userManager=new UserManager();
        userManager.createOwner(user);

        WorkspaceManager workspaceManager = new WorkspaceManager();

        for(int i=0;i<10;i++) {
            OwnedWorkspace workspace = new OwnedWorkspace("abc_ws"+i, "aaa", 2.5);
            workspaceManager.createWorkspace(workspace);
            for (int j = 0; j < 5 ; j++) {
                System.out.println("====== going to create " + workspace.getWorkspaceName() + " : file" + j);
                workspaceManager.createDataFile(workspace.getWorkspaceName(), "file"+j, workspace.getOwnerId(), true);
            }
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

    private void cleanWorkspaeDataNMetadata() {
        Context appContext = AirDeskApp.s_applicationContext;

        File workspaceDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        System.out.println("owned WS path:" + workspaceDir);
        File[] files = workspaceDir.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }

        Assert.assertTrue(workspaceDir.listFiles().length == 0);

        workspaceDir = appContext.getDir("foreignWorkspaces", appContext.MODE_PRIVATE);
        System.out.println("foreignWS path:" + workspaceDir);
        files = workspaceDir.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        Assert.assertTrue(workspaceDir.listFiles().length == 0);

        for (File file : appContext.getFilesDir().listFiles()) {
            file.delete();
        }
        Assert.assertTrue(appContext.getFilesDir().listFiles().length == 0);

    }

    public static boolean deleteFolder(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        return folder.delete();
    }
}
