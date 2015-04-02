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
import java.util.Set;

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
    String ownerId;
    String workspaceName;
    String fileName;

    public void testApp() throws Exception {
        workspaceManager = new WorkspaceManager();
        userManager = new UserManager();
        appContext = AirDeskApp.s_applicationContext;
        ownerId = "owner3@gmail.com";
        workspaceName = "gayya";
        fileName = "file";

        cleanWorkspaeDataNMetadata();
        createUser();
        testCreateWorkspace();
        //testWorkspaceEdit();
        testGetOwnedWorkspaces();
        testOwnedWSDataFileLC();

        testTooLargeForMemory();
        testTooSmallerThanWorkspace();

        testAddUserToWorkspace();

        testForeignWSDataFileLC();
      //  testDeleteUserFromAccessList();
        testSubscribeNEditTagsForeignWS();
        testPublicWorkspaceTagAddition();

        testDeleteOwnedWorkspace();
        testDeleteUser();
    }

    public void testTooLargeForMemory(){
        double quotaSize=50000000;
        boolean isNotSufficientMemory=workspaceManager.isNotSufficientMemory(quotaSize);
        Assert.assertTrue(isNotSufficientMemory);

        quotaSize=5000;
        isNotSufficientMemory=workspaceManager.isNotSufficientMemory(quotaSize);
        Assert.assertFalse(isNotSufficientMemory);
    }

    public void testTooSmallerThanWorkspace() throws Exception{

        File baseDir;
        String pathToFile;
        baseDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;
        workspaceManager.createDataFile(workspaceName, fileName, ownerId, true);
        FileInputStream fis = workspaceManager.getDataFile(workspaceName, fileName, false, ownerId, true);
        workspaceManager.updateDataFile(workspaceName, fileName, "my file content", ownerId, true);


        double quotaSize=FileUtils.folderSize(workspaceName);
        System.out.println("folder size "+quotaSize);
        quotaSize=quotaSize+1;
        boolean  isTooSmallerThanWorkspace=workspaceManager.isQuotaSmallerThanUsage(workspaceName,quotaSize);
        Assert.assertFalse(isTooSmallerThanWorkspace);

        quotaSize=FileUtils.folderSize(workspaceName);
        quotaSize=quotaSize-0.001;
        System.out.println("reduced quota size is"+quotaSize);
        isTooSmallerThanWorkspace=workspaceManager.isQuotaSmallerThanUsage(workspaceName,quotaSize);
        Assert.assertTrue(isTooSmallerThanWorkspace);
    }

    private void cleanWorkspaeDataNMetadata() {
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

    private void createUser() {
        userManager.createOwner("owner3", "owner3@gmail.com");
    }

    private void testCreateWorkspace() {
        OwnedWorkspace workspace = new OwnedWorkspace(workspaceName, ownerId, 2.0);
        Assert.assertEquals(WorkspaceCreateStatus.OK, workspaceManager.createWorkspace(workspace));
    }

    public void testGetOwnedWorkspaces() {
        List<String> wsList = userManager.getOwnedWorkspaces();
        Assert.assertTrue(wsList.contains(workspaceName));
    }

    private void testOwnedWSDataFileLC() throws Exception {
        File baseDir;
        String pathToFile;

        baseDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;

        workspaceManager.createDataFile(workspaceName, fileName, ownerId, true);
        Assert.assertEquals(true, new File(pathToFile).exists());

        FileInputStream fis = workspaceManager.getDataFile(workspaceName, fileName, false, ownerId, true);
        Assert.assertEquals(-1, fis.read());

        workspaceManager.updateDataFile(workspaceName, fileName, "my file content", ownerId, true);
        fis = workspaceManager.getDataFile(workspaceName, fileName, false, ownerId, true);
        Assert.assertNotSame(-1, fis.read());

        workspaceManager.deleteDataFile(workspaceName, fileName, ownerId, true);
        Assert.assertEquals(false, new File(pathToFile).exists());

    }

    public void testWorkspaceEdit() {
        //adding tags,

        OwnedWorkspace ownedWorkspace = workspaceManager.getOwnedWorkspace(workspaceName);
        List<String> newTags = new ArrayList<String>();
        for (int i = 0; i < 8; i++) {
            newTags.add("my new tag" + i);
        }
        ownedWorkspace.addTags(newTags);
        ownedWorkspace.setQuota(500);
        ownedWorkspace.setPublic(true);
        workspaceManager.editOwnedWorkspace(workspaceName, ownedWorkspace, true);
        OwnedWorkspace renewedWS = workspaceManager.getOwnedWorkspace(workspaceName);
        Assert.assertEquals((double) 500, ownedWorkspace.getQuota());
        Assert.assertEquals(ownedWorkspace.getTags().size(), renewedWS.getTags().size());
        for (String tag : renewedWS.getTags()) {
            System.out.println("tags are " + tag);
        }
    }

    private void testAddUserToWorkspace() throws Exception {
        workspaceManager.addClientToWorkspace(workspaceName, ownerId);

        //since we directly add this to foreign workspace for 1st iteration
        List<String> foreignWS = userManager.getForeignWorkspaces();
        Assert.assertTrue(foreignWS.contains(workspaceName));
    }

    private void testForeignWSDataFileLC() throws Exception {

        String foreignFile = fileName + "foreign";
        String workspaceType = Constants.FOREIGN_WORKSPACE_DIR;
        File baseDir = appContext.getDir(workspaceType, appContext.MODE_PRIVATE);
        String pathToFile = baseDir.getAbsolutePath() + File.separator + FileUtils.getFileNameForUserId(ownerId) + File.separator +
                workspaceName + File.separator + foreignFile;

        workspaceManager.createDataFile(workspaceName, foreignFile, ownerId, false);

        Assert.assertTrue(new File(pathToFile).exists());

        //file should be added to workspace owners place too
        String ownerPath = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE).getAbsolutePath() +
                File.separator + workspaceName + File.separator + foreignFile;
        Assert.assertTrue(new File(ownerPath).exists());

        workspaceManager.deleteDataFile(workspaceName, foreignFile, ownerId, false);
        Assert.assertFalse(new File(pathToFile).exists());

        Assert.assertFalse(new File(ownerPath).exists());

    }

    private void testSubscribeNEditTagsForeignWS() {
        String[] tags = new String[]{"tag1", "tag2"};
        workspaceManager.subscribeToTags(tags);
        Assert.assertTrue(userManager.getOwner().getSubscribedTags().contains("tag1"));
        Assert.assertTrue(userManager.getOwner().getSubscribedTags().contains("tag2"));

        workspaceManager.unsubscribeFromTags(new String[]{"tag1"});
        Assert.assertFalse(userManager.getOwner().getSubscribedTags().contains("tag1"));
        Assert.assertTrue(userManager.getOwner().getSubscribedTags().contains("tag2"));
    }

    private void testPublicWorkspaceTagAddition() {
        String publicWorkspaceName = workspaceName + "-public1";

        //when a workspace owner add a tag that matches with a user subscription tag, the workspace
        //should be added to users foreign workspaces
        OwnedWorkspace workspace = new OwnedWorkspace(publicWorkspaceName, ownerId, 2.0);
        Assert.assertFalse(userManager.getForeignWorkspaces().contains(publicWorkspaceName));

        workspace.setPublic(true);
        List<String> tags = new ArrayList<>();
        tags.add("tag");
        tags.add("tag2");
        workspace.addTags(tags);
        workspaceManager.createWorkspace(workspace);

        //user is subscribed to tag2
        Assert.assertTrue(userManager.getForeignWorkspaces().contains(publicWorkspaceName));

        //when a user add a tag and any others has a workspace matching to that tag, that workspace
        //should be added to users foreign workspaces

        publicWorkspaceName = publicWorkspaceName + "2";
        OwnedWorkspace workspace2 = new OwnedWorkspace(publicWorkspaceName, ownerId, 2.0);
        workspace2.setPublic(true);
        List<String> tags2 = new ArrayList<>();
        tags2.add("tag5");
        tags2.add("tag6");
        workspace2.addTags(tags2);
        workspaceManager.createWorkspace(workspace2);
        Assert.assertFalse(userManager.getForeignWorkspaces().contains(publicWorkspaceName));

        workspaceManager.subscribeToTags(new String[]{"tag5"});
        Assert.assertTrue(userManager.getForeignWorkspaces().contains(publicWorkspaceName));
    }


    public void testDeleteUserFromAccessList() {
        workspaceManager.deleteUserFromAccessList(workspaceName, ownerId);
        Set<String> clients = workspaceManager.getOwnedWorkspace(workspaceName).getClients().keySet();
        Assert.assertFalse(clients.contains(ownerId));

        /*TODO:to be removed after introducing wifi direct  */
        List<String> workspaces = userManager.getForeignWorkspaces();
        Assert.assertFalse(workspaces.contains(workspaceName));

        /*
        TODO: can we delete client folder???
        File parentDir = appContext.getDir(Constants.FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        String fileName = parentDir.getAbsolutePath() + "/" + FileUtils.getFileNameForUserId(userManager.getOwner().getUserId()) + "/" + workspaceName;
        File workspaceDir = new File(fileName);
        Assert.assertEquals(false, workspaceDir.exists());*/

        System.out.println("Delete user is successful");
    }


    private void testDeleteOwnedWorkspace() {
        // OwnedWorkspace ownedWorkspace=workspaceManager.getOwnedWorkspace(workspaceName);
        List<String> workSpaces = userManager.getOwnedWorkspaces();
        System.out.println("before " + workSpaces.contains(workspaceName));

        for (int i = 0; i < workSpaces.size(); i++) {
            System.out.println(workSpaces.get(i));
        }
        workspaceManager.deleteOwnedWorkspace(workspaceName);
        workSpaces = userManager.getOwnedWorkspaces();
        System.out.println("status in owned workspace " + workSpaces.contains(workspaceName));
        Assert.assertEquals(false, workSpaces.contains(workspaceName));

        workSpaces = userManager.getForeignWorkspaces();
        System.out.println("status in foreign workspace " + workSpaces.contains(workspaceName));
        Assert.assertEquals(false, workSpaces.contains(workspaceName));

        File parentDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        File workspaceDir = new File(parentDir.getAbsolutePath() + "/" + workspaceName);
        System.out.println("is foreign ws folder available " + workSpaces.contains(workspaceName));
        Assert.assertEquals(false, workspaceDir.exists());


      /*
      TODO: Can we delete Client folder???
        parentDir = appContext.getDir(Constants.FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        String fileName = parentDir.getAbsolutePath() + "/" + FileUtils.getFileNameForUserId(userManager.getOwner().getUserId()) + "/" + workspaceName;

        workspaceDir = new File(fileName);
        System.out.println("is foreign ws folder available " + workSpaces.contains(workspaceName));
        Assert.assertEquals(false, workspaceDir.exists());*/

    }

    private void testDeleteUser() {
        userManager.deleteOwner();
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