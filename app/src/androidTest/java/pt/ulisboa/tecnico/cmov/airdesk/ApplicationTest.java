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
import pt.ulisboa.tecnico.cmov.airdesk.manager.MetadataManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
    StorageManager storageManager;
    Context appContext;

    public void testWorkspaceCreation() {
        User user = new User();
        user.setEmail("abc@gmail.com");
        user.setNickName("aaa");
        new UserManager().createUser(user);

        WorkspaceManager workspaceManager = new WorkspaceManager();
        OwnedWorkspace workspace = new OwnedWorkspace("abc_ws", "aaa", 2.5);
        workspaceManager.createWorkspace(workspace);
        OwnedWorkspace workspace2 = new OwnedWorkspace("abc_ws2", "aaa", 2.5);
        workspaceManager.createWorkspace(workspace2);

        List<String> workspaceList =  new UserManager().getOwnedWorkspaces();
        Assert.assertEquals(2, workspaceList.size());

    }

    public void testGetOwnedWorkspaces() {
        List<String> workspaceList = new AirDeskManager().getOwnedWorkspaces();
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        for (String ws : workspaceList) {
            System.out.println(ws);
        }
    }

    public void testDataFileLC() throws Exception {
        storageManager = new StorageManager();
        appContext = AirDeskApp.s_applicationContext;

        String owner = "owner1";
        String workspaceName = "ws2";
        String fileName = "file2";
        File baseDir;
        String pathToFile;

        //test for foreign workspace

        String workspaceType = "foreignWorkspaces";
        baseDir = appContext.getDir(workspaceType, appContext.MODE_PRIVATE);
        pathToFile = baseDir.getAbsolutePath() + File.separator + owner + File.separator +
                workspaceName + File.separator + fileName;

        storageManager.createDataFile(workspaceName, fileName, owner, false);
        Assert.assertEquals(true, new File(pathToFile).exists());

        storageManager.deleteDataFile(workspaceName, fileName, owner, false);
        Assert.assertEquals(false, new File(pathToFile).exists());



        //test for owned workspace

        workspaceType = "ownedWorkspaces";

        baseDir = appContext.getDir(workspaceType, appContext.MODE_PRIVATE);
        pathToFile = baseDir.getAbsolutePath() + File.separator + workspaceName + File.separator + fileName;

        storageManager.createDataFile(workspaceName, fileName, owner, true);
        Assert.assertEquals(true, new File(pathToFile).exists());

        FileInputStream fis = storageManager.getDataFile(workspaceName, fileName, false, owner, true);
        Assert.assertEquals(-1, fis.read());

        storageManager.updateDataFile(workspaceName, fileName, generateFileInputStream(), owner, true);
        fis = storageManager.getDataFile(workspaceName, fileName, false, owner, true);
        Assert.assertNotSame(-1, fis.read());

        storageManager.deleteDataFile(workspaceName, fileName, owner, true);
        Assert.assertEquals(false, new File(pathToFile).exists());

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