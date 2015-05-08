package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.ui.adapter.AccessListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.ui.adapter.UserItem;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class EditAccessListActivity extends ActionBarActivity {

    OwnedWorkspace workspace;
    private ArrayList<UserItem> usersItemList;
    private MenuItem addUserMenuItem;
    private MenuItem removeUserMenuItem;
    private AccessListAdapter adapter;

    public EditAccessListActivity() {
        usersItemList = new ArrayList<UserItem>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_access_list);


        if (getIntent() != null) {
            final String workspaceName = getIntent().getStringExtra(Constants.WORKSPACE_NAME);
            workspace = new WorkspaceManager().getOwnedWorkspace(workspaceName);


            // setAccessList();

            adapter = new AccessListAdapter(this, usersItemList);


           /* ArrayAdapter adapter = new ArrayAdapter<String>(
                    this, // The current context (this activity)
                    R.layout.list_item_accesslist, // The name of the layout ID.
                    R.id.list_item_access_list, // The ID of the textview to populate.
                    accessList);*/

            ListView listView = (ListView) findViewById(R.id.access_list);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter.getCheckedUsers().size() > 0) {
                        addUserMenuItem.setVisible(false);
                        removeUserMenuItem.setVisible(true);
                    } else {
                        addUserMenuItem.setVisible(true);
                        removeUserMenuItem.setVisible(false);
                    }


                }
            });

            fillData();

            Button addUserButton = (Button) findViewById(R.id.add_user_btn);
            Button removeUserButton = (Button) findViewById(R.id.remov_user_btn);

        }
    }

    private void setAccessList() {
        TextView accessList = (TextView) findViewById(R.id.access_list_text);
        String clients = new WorkspaceManager().getOwnedWorkspace(workspace.getWorkspaceName())
                .getClients().keySet().toString();
        accessList.setText(clients.replace("[", "").replace("]", "").replace(",", "\n"));
    }


    private void fillData() {
        usersItemList.clear();
        Object[] users = new WorkspaceManager().getOwnedWorkspace(workspace.getWorkspaceName())
                .getClients().keySet().toArray();
        for (Object user : users) {
            usersItemList.add(new UserItem((String) user, false));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_access_list, menu);
        addUserMenuItem = menu.getItem(0);
        removeUserMenuItem = menu.getItem(1);

        adapter.setAddUserMenuItem(addUserMenuItem);
        adapter.setRemoveUserMenuItem(removeUserMenuItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_user) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input.setHint("Username");
            builder.setTitle("User Access List");
            builder.setView(input);
            builder.setPositiveButton(R.string.add_to_acess_list, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        new WorkspaceManager().addClientToWorkspace(workspace.getWorkspaceName(),
                                String.valueOf(input.getText()).trim(), true);
                        fillData();
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

            builder.show();
            return true;
        }


        if (id == R.id.action_remove_user) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Delete")
                    .setMessage("Do you want to delete " + adapter.getCheckedUsers().size() + "  User(s) ?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        ArrayList<UserItem> usersToDelete = adapter.getCheckedUsers();
                        WorkspaceManager manager = new WorkspaceManager();
                        for (UserItem userItem : usersToDelete) {
                            manager.deleteUserFromAccessList(workspace.getWorkspaceName(), userItem.getName());
                        }
                        fillData();


                        addUserMenuItem.setVisible(true);
                        removeUserMenuItem.setVisible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();

           /* new WorkspaceManager().deleteUserFromAccessList(workspace.getWorkspaceName(),
                    String.valueOf(input.getText()).trim());*/
        }

        return super.onOptionsItemSelected(item);
    }
}
