package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testDataFileLC() throws Exception {
        System.out.println("rtttttt");
        //String path = FileUtils.createFolder("aaaaaaa");
        //new WorkspaceManager().addToForeignWorkspace("workspaceName", "owner", 2, new String[]{"f1", "f2"});

       /* Context appContext = AirDeskApp.s_applicationContext;
        File parentDir=appContext.getDir(Constants.FOREIGN_WORKSPACE_DIR,appContext.MODE_PRIVATE);
        FileUtils.createFolder(parentDir, "owner/workspace");*/

        //test foreign workspace scenario

       /* OwnedWorkspace ws = new OwnedWorkspace("my_WS","me",2.0);
        new WorkspaceManager().createWorkspace("my_WS", ws);*/

        StorageManager storageManager = new StorageManager();
        storageManager.createDataFile("ws2", "file2", "owner1", false);

        /*FileInputStream fs = new FileInputStream("skdsl skd sk sd sdl sdl sd");
        storageManager.updateDataFile("ws2" ,"file1", fs, "owner1",false);

        FileInputStream is = storageManager.getDataFile("ws2" ,"file1", false, "owner1",false);
        System.out.println(is.toString());

        BufferedReader br =
                new BufferedReader( new InputStreamReader(is ));
        System.out.println(br.readLine());
        System.out.println(br.readLine());*/

        System.out.println();

        Assert.assertEquals(true, new File("/data/data/pt.ulisboa.tecnico.cmov.airdesk/app_foreignWorkspaces/owner1/ws2").exists());

        storageManager.deleteDataFile("ws2", "file2", "owner1", false);
        Assert.assertEquals(false, new File("/data/data/pt.ulisboa.tecnico.cmov.airdesk/app_foreignWorkspaces/owner1/ws2").exists());


    }
}