package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    String owner;
    String workspaceName;
    String fileName;

    public void testOwnedWSList(){
      new PopulateData().populateOwnedWorkspaces();
    }

    public void testApp() throws Exception {
        workspaceManager = new WorkspaceManager();
        userManager = new UserManager();
        appContext = AirDeskApp.s_applicationContext;
        owner = "owner3";
        workspaceName = "my_workspace";
        fileName = "file5";

        createUser();
        testCreateWorkspace();
        testGetOwnedWorkspaces();
        testOwnedWSDataFileLC();
//        testWorkspaceEdit();
//        testAddUserToWorkspace();
//        testGetForeignWorkspaces();
//        testForeignWSDataFileLC();
//        testSubscribeNEditTagsForeignWS();
//        testDeleteOwnedWorkspace();
//        testDeleteUser();
    }

    private void createUser() {
        User user = new User();
        user.setEmail("owner3@gmail.com");
        user.setNickName("owner3");
        userManager.createUser(user);
    }

    private void testCreateWorkspace() {
        OwnedWorkspace workspace = new OwnedWorkspace("my_workspace", owner, 2.0);
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

        workspaceManager.createDataFile(workspaceName, fileName, owner, true);
        Assert.assertEquals(true, new File(pathToFile).exists());

        FileInputStream fis = workspaceManager.getDataFile(workspaceName, fileName, false, owner, true);
        Assert.assertEquals(-1, fis.read());

        workspaceManager.updateDataFile(workspaceName, fileName, generateFileInputStream(), owner, true);
        fis = workspaceManager.getDataFile(workspaceName, fileName, false, owner, true);
        Assert.assertNotSame(-1, fis.read());

        workspaceManager.deleteDataFile(workspaceName, fileName, owner, true);
        Assert.assertEquals(false, new File(pathToFile).exists());

    }

    private void testWorkspaceEdit() {
        //adding tags
    }

    private void testAddUserToWorkspace() {
        //adding user

    }

    private void testGetForeignWorkspaces() {
    }

    private void testForeignWSDataFileLC() {
        //test for foreign workspace

//        workspaceType = "foreignWorkspaces";
//        baseDir = appContext.getDir(workspaceType, appContext.MODE_PRIVATE);
//        pathToFile = baseDir.getAbsolutePath() + File.separator + owner + File.separator +
//                workspaceName + File.separator + fileName;
//
//        workspaceManager.createDataFile(workspaceName, fileName, owner, false);
//        Assert.assertEquals(true, new File(pathToFile).exists());
//
//        workspaceManager.deleteDataFile(workspaceName, fileName, owner, false);
//        Assert.assertEquals(false, new File(pathToFile).exists());
    }

    private void testSubscribeNEditTagsForeignWS() {

    }

    private void testDeleteOwnedWorkspace() {

    }

    private void testDeleteUser() {
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