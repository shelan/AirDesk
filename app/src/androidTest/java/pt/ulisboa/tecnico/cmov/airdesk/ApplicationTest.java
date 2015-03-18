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

        Context appContext = AirDeskApp.s_applicationContext;
        String owner = "owner1";
        String workspaceName = "ws2";
        String fileName = "file2";

        //test for foreign workspace

        String workspaceType = "foreignWorkspaces";

        File baseDir = appContext.getDir(workspaceType, appContext.MODE_PRIVATE);
        String pathToFile = baseDir.getAbsolutePath() + File.separator + owner + File.separator +
                workspaceName + File.separator + fileName;


        StorageManager storageManager = new StorageManager();
        storageManager.createDataFile(workspaceName, fileName, owner, false);
        Assert.assertEquals(true, new File(pathToFile).exists());

        System.out.println();
        storageManager.deleteDataFile(workspaceName, fileName, owner, false);
        Assert.assertEquals(false, new File(pathToFile).exists());


    }
}