package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import pt.ulisboa.tecnico.cmov.airdesk.PopulateData;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForiegnWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.MyWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;

public class MainActivity extends ActionBarActivity {

    MyWorkspaceListFragment myWorkspacesFragment;
    ForiegnWorkspaceListFragment foreignWorkspacesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {

            myWorkspacesFragment = new MyWorkspaceListFragment();
            foreignWorkspacesFragment = new ForiegnWorkspaceListFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, myWorkspacesFragment, "fragment_1")
                    .add(R.id.container, foreignWorkspacesFragment, "fragment_2")
                    .commit();

        }

        UserManager manager = new UserManager();

        if (manager.getOwner() == null) {
            User user = new User();
            user.setNickName("aaa");
            user.setEmail("testuser@gmail.com");
            manager.createOwner(user);
        }

        /*SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean isInitialized = sharedPref.getBoolean("isInitialized", false);

        if(!isInitialized){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isInitialized",true);
            try {
                new PopulateData().populateOwnedWorkspaces();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

            getSupportActionBar().setElevation(0f);


            //TODO move these tests and write proper tests in android test package



       /* FileUtils.createFolderForOwnedWorkSpaces();
        FileUtils.createFolder("test");
        FileUtils.folderSize("test");
        testAddUser();
        testAddWS("test", 500);
        testAddWS("test2", 200);
        testEditWS("test", 300);
        testOwnedWS();*/


        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }


        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            } else if (id == R.id.action_populate) {
                try {
                    new PopulateData().populateOwnedWorkspaces();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        protected void onResume () {
           // myWorkspacesFragment.updateWorkspaceList();
            //foreignWorkspacesFragment.updateWorkspaceList();

            super.onResume();
        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
            // editor.putBoolean("isInitialized",false);
        }

    /* private void testEditWS(String test, int i) {
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
        manager.createOwner(user);

    }
*/


    }
