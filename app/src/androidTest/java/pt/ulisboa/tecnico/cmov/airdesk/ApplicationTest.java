package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.enums.WorkspaceCreateStatus;
import pt.ulisboa.tecnico.cmov.airdesk.manager.MetadataManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    WorkspaceManager workspaceManager;
    UserManager userManager;
    Context appContext;
    String ownerName;
    String workspaceName;
    String fileName;

    public void testOwnedWSList(){
      new PopulateData().populateOwnedWorkspaces();
    }

    public void testApp() throws Exception {
        workspaceManager = new WorkspaceManager();
        userManager = new UserManager();
        appContext = AirDeskApp.s_applicationContext;
        ownerName = "owner3";
        workspaceName = "lanchWS3";
        fileName = "file5";

        createUser();
        testCreateWorkspace();
        testWorkspaceEdit();
        testDeleteOwnedWorkspace();

        WorkspaceTest wsTest=new WorkspaceTest();
        wsTest.populateForeignWorkspaces();

        testGetOwnedWorkspaces();
        testOwnedWSDataFileLC();

//        testWorkspaceEdit();

        testAddUserToWorkspace();

//        testForeignWSDataFileLC();
//        testDeleteUserFromAccessList();
//        testSubscribeNEditTagsForeignWS();
//        testDeleteOwnedWorkspace();
        testDeleteUser();
    }

    private void createUser() {
        User user = new User();
        user.setEmail("owner3@gmail.com");
        user.setNickName("owner3");
        userManager.createUser(user);
    }

    private void testCreateWorkspace() {
        OwnedWorkspace workspace = new OwnedWorkspace(workspaceName, ownerName, 2.0);
        Assert.assertEquals(WorkspaceCreateStatus.OK, workspaceManager.createWorkspace(workspace));
    }

    public void testGetOwnedWorkspaces() {
        List<String> wsList = userManager.getOwnedWorkspaces();
        System.out.println("########################");
        for (String s : wsList) {
            System.out.println(s);
        }
    }

    private void testOwnedWSDataFileLC() throws Exception {
        File baseDir;
        String pathToFile;

        //test for owned workspace
        String workspaceType = "ownedWorkspaces";

        baseDir = appContext.getDir(workspaceType, appContext.MODE_PRIVATE);
        pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;

        workspaceManager.createDataFile(workspaceName, fileName, ownerName, true);
        Assert.assertEquals(true, new File(pathToFile).exists());

        FileInputStream fis = workspaceManager.getDataFile(workspaceName, fileName, false, ownerName, true);
        Assert.assertEquals(-1, fis.read());

        workspaceManager.updateDataFile(workspaceName, fileName, generateFileInputStream(), ownerName, true);
        fis = workspaceManager.getDataFile(workspaceName, fileName, false, ownerName, true);
        Assert.assertNotSame(-1, fis.read());

        workspaceManager.deleteDataFile(workspaceName, fileName, ownerName, true);
        Assert.assertEquals(false, new File(pathToFile).exists());

    }

    public void testWorkspaceEdit() {
        //adding tags,
      /*  List<String>ws=userManager.getOwnedWorkspaces();
        for(int i=0;i<ws.size();i++){
            System
        }*/

        OwnedWorkspace ownedWorkspace=workspaceManager.getOwnedWorkspace(workspaceName);
        List<String>newTags=new ArrayList<String>();
        for(int i=0;i<8;i++){
            newTags.add("my new tag"+i);
        }
        ownedWorkspace.addTags(newTags);
        ownedWorkspace.setQuota(500);
        ownedWorkspace.setPublic(true);
        workspaceManager.editOwnedWorkspace(workspaceName,ownedWorkspace);
        OwnedWorkspace renewedWS=workspaceManager.getOwnedWorkspace(workspaceName);
        Assert.assertEquals((double)500,ownedWorkspace.getQuota());
        Assert.assertEquals(ownedWorkspace.getTags().size(),renewedWS.getTags().size());
        for (String tag:renewedWS.getTags()){
            System.out.println("tags are "+tag);
        }
    }

    private void testAddUserToWorkspace() throws Exception {
        workspaceManager.addUserToAccessList(workspaceName, ownerName);
        List<String> foreignWS = userManager.getForeignWorkspaces();
        Assert.assertTrue(foreignWS.contains(workspaceName));
    }

    private void testForeignWSDataFileLC() {
        //test for foreign workspace

//        workspaceType = "foreignWorkspaces";
//        baseDir = appContext.getDir(workspaceType, appContext.MODE_PRIVATE);
//        pathToFile = baseDir.getAbsolutePath() + File.separator + ownerName + File.separator +
//                workspaceName + File.separator + fileName;
//
//        workspaceManager.createDataFile(workspaceName, fileName, ownerName, false);
//        Assert.assertEquals(true, new File(pathToFile).exists());
//
//        workspaceManager.deleteDataFile(workspaceName, fileName, ownerName, false);
//        Assert.assertEquals(false, new File(pathToFile).exists());
    }

    private void testSubscribeNEditTagsForeignWS() {

    }

    private void testDeleteUserFromAccessList() {

    }

    private void testDeleteOwnedWorkspace() {
       // OwnedWorkspace ownedWorkspace=workspaceManager.getOwnedWorkspace(workspaceName);
        List<String>workSpaces= userManager.getOwnedWorkspaces();
        System.out.println("before "+workSpaces.contains(workspaceName));

        for(int i=0;i<workSpaces.size();i++){
            System.out.println(workSpaces.get(i));
        }
        workspaceManager.deleteOwnedWorkspace(workspaceName);
        workSpaces= userManager.getOwnedWorkspaces();
        System.out.println("after "+workSpaces.contains(workspaceName));
       Assert.assertEquals(false,workSpaces.contains(workspaceName));
    }

    private void testDeleteUser() {
        userManager.deleteOwner(ownerName);
    }

    private FileInputStream generateFileInputStream() throws IOException {
        OwnedWorkspace ownedWorkspace = new OwnedWorkspace("newWs", "newOwner", 2.0);
        ownedWorkspace.addFiles(new String[]{"f1","f2","f3"});
        new MetadataManager().saveOwnedWorkspace(ownedWorkspace);

        String fileName = "newWs-owned.json";
        FileInputStream fis = AirDeskApp.s_applicationContext.openFileInput(fileName);

        return fis;
    }
}