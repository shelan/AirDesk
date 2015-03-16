package pt.ulisboa.tecnico.cmov.airdesk;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.manager.MetadataManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForiegnWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.MyWorkspaceListFragment;

import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.manager.MetadataManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FileUtils.createFolderForOwnedWorkSpaces();
        FileUtils.createFolder("test");
        FileUtils.folderSize("test");
        testAddUser();
        testAddWS("test", 500);
        testAddWS("test2", 200);
        testEditWS("test", 300);
        testOwnedWS();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MyWorkspaceListFragment(), "fragment_1")
                    .add(R.id.container, new ForiegnWorkspaceListFragment(), "fragment_2")
                    .commit();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void testEditWS(String test, int i) {
        WorkspaceManager mgr = new WorkspaceManager();
        mgr.editOwnedWorkspace(test, i);
        MetadataManager mtr = new MetadataManager();
        double edited = mtr.getOwnedWorkspace(test + Constants.jsonSuffix).getQuota();
        System.out.println("quota" + edited);

    }

    private void testOwnedWS() {
        UserManager manager = new UserManager();
        List<String> files = manager.getOwnedWorkspaces();
        for (int i = 0; i < files.size(); i++) {
            System.out.println("file is " + files.get(i));
        }
    }

    public void testAddWS(String text, double size) {
        WorkspaceManager mgr = new WorkspaceManager();
        mgr.createWorkspace(text, size);

    }

    private void testAddUser() {
        UserManager manager = new UserManager();
        User user = new User();
        user.setEmail("lanch.gune@gmail.com");
        user.setNickName("junda");
        manager.createUser(user);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
