package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class EditAccessListActivity extends ActionBarActivity {

    OwnedWorkspace workspace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_access_list);

        if(getIntent() != null) {
            final String workspaceName = getIntent().getStringExtra(Constants.WORKSPACE_NAME);
            workspace = new WorkspaceManager().getOwnedWorkspace(workspaceName);
            TextView accessList = (TextView) findViewById(R.id.access_list);
            String clients = new WorkspaceManager().getOwnedWorkspace(workspace.getWorkspaceName()).getClients().keySet().toString();
            accessList.setText(clients.replace("[","").replace("]","").replace(",","\n"));

            Button addUserButton = (Button) findViewById(R.id.add_user_btn);
            Button removeUserButton = (Button) findViewById(R.id.remov_user_btn);

            addUserButton.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    /*AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    final EditText input = new EditText(this);
                    input.setHint("Username");
                    builder.setTitle("User Access List");
                    builder.setView(input);
                    builder.setPositiveButton(R.string.add_to_acess_list, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                new WorkspaceManager().addClientToWorkspace(workspace.getWorkspaceName(),
                                        String.valueOf(input.getText()).trim());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    builder.show();*/
                }
            });

            removeUserButton.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                   /* AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    final EditText input = new EditText(this);
                    input.setHint("Username");
                    builder.setTitle("Remove from access list");
                    builder.setView(input);
                    builder.setPositiveButton(R.string.remove_from_acess_list, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                new WorkspaceManager().deleteUserFromAccessList(workspace.getWorkspaceName(),String.valueOf(input.getText()).trim());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    builder.show();*/
                }

            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_access_list, menu);
        return true;
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
