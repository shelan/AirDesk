package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
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
            WorkspaceManager manager = new WorkspaceManager();
            TextView fileName = (TextView) findViewById(R.id.file_name);
            TextView fileText = (TextView) findViewById(R.id.file_text_edit);

            String workspace = null;
            String owner = null;

            Intent intent = getIntent();
            if (intent != null) {

                if (intent.hasExtra(Constants.WORKSPACE_NAME)) {
                    workspace = intent.getStringExtra(Constants.WORKSPACE_NAME);
                }
                if (intent.hasExtra(Constants.OWNER)) {
                    owner = intent.getStringExtra(Constants.OWNER);
                }
                try {
                    //TODO: change true to proper value depending on the workspace type
                    manager.createDataFile(workspace, String.valueOf(fileName.getText()),
                            owner, true);
                    manager.updateDataFile(workspace, String.valueOf(fileName.getText()),
                            String.valueOf(fileText.getText()), owner, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
