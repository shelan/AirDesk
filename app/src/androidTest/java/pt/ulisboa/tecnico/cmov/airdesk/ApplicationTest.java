package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.io.File;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testT() throws Exception {
        System.out.println("rtttttt");
        //String path = FileUtils.createFolder("aaaaaaa");
        //new WorkspaceManager().addToForeignWorkspace("workspaceName", "owner", 2, new String[]{"f1", "f2"});

       /* Context appContext = AirDeskApp.s_applicationContext;
        File parentDir=appContext.getDir(Constants.FOREIGN_WORKSPACE_DIR,appContext.MODE_PRIVATE);
        FileUtils.createFolder(parentDir, "owner/workspace");*/

        new StorageManager().createDataFile("ws2" ,"file1","owner1",false);

        Assert.assertEquals("a","a");
    }
}