package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForiegnWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.MyWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.manager.HoardingManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;

public class MainActivity extends ActionBarActivity {

    MyWorkspaceListFragment myWorkspacesFragment;
    ForiegnWorkspaceListFragment foreignWorkspacesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_refresh);

        UserManager manager = new UserManager();

        if (manager.getOwner() == null) {
            Intent intent = new Intent(this, CreateUserActivity.class);
            startActivity(intent);
        }
            if (savedInstanceState == null) {

                myWorkspacesFragment = new MyWorkspaceListFragment();
                foreignWorkspacesFragment = new ForiegnWorkspaceListFragment();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, myWorkspacesFragment, "fragment_1")
                        .add(R.id.container, foreignWorkspacesFragment, "fragment_2")
                        .commit();

            }

            getSupportActionBar().setElevation(0f);

           new HoardingManager().scheduleCleaningTask();


            //TODO move these tests and write proper tests in android test package


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
            /*if (id == R.id.action_settings) {
                return true;
            } else if (id == R.id.action_populate) {
                try {
                    new PopulateData().populateOwnedWorkspaces();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/

            return super.onOptionsItemSelected(item);
        }


        @Override
        protected void onDestroy () {
            super.onDestroy();
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        }


    }
