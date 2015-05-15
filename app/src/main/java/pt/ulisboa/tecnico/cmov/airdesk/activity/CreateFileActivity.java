package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.AbstractWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class CreateFileActivity extends ActionBarActivity {

    MenuItem doneMenuItem;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_file);
        final TextView fileName = (TextView) findViewById(R.id.file_name);
        TextView fileText = (TextView) findViewById(R.id.file_text_edit);

        fileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(fileName.getText().length() != 0){
                    doneMenuItem.setVisible(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_file, menu);
        doneMenuItem = menu.getItem(0);
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
        else if(id == R.id.action_file_create) {
            WorkspaceManager workspaceManager = new WorkspaceManager();
            TextView fileName = (TextView) findViewById(R.id.file_name);
            TextView fileText = (TextView) findViewById(R.id.file_text_edit);

            String workspaceName = null;
            String ownerId = null;

            Intent intent = getIntent();
            if (intent != null) {

                if (intent.hasExtra(Constants.WORKSPACE_NAME)) {
                    workspaceName = intent.getStringExtra(Constants.WORKSPACE_NAME);
                }
                if (intent.hasExtra(Constants.OWNER)) {
                    ownerId = intent.getStringExtra(Constants.OWNER);
                }
                try {
                    //TODO: change true to proper value depending on the workspaceName type
                    /*workspaceManager.createDataFile(workspaceName, String.valueOf(fileName.getText()),
                            ownerId, true);*/
                    boolean isOwnedWorkspace = new UserManager().getOwner().getUserId().equals(ownerId);
                    AbstractWorkspace workspace;
                    if(isOwnedWorkspace)
                        workspace = workspaceManager.getOwnedWorkspace(workspaceName);
                    else
                        workspace = workspaceManager.getForeignWorkspace(workspaceName, ownerId);

                    if(workspace.getFileNames().contains(String.valueOf(fileName.getText()))) {
                        //Toast.makeText(getApplicationContext(), "File name already exists. File name should be unique", Toast.LENGTH_SHORT);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        builder.setTitle("File name already exists")
                                .setMessage("File name should be unique. Please choose another file name.");
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    } else {
                        workspaceManager.updateDataFile(workspaceName, String.valueOf(fileName.getText()),
                                String.valueOf(fileText.getText()), ownerId, isOwnedWorkspace);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
