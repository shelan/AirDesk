package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class CreateFileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_file);
        Button button = (Button) findViewById(R.id.create_file_btn);

        button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                WorkspaceManager manager = new WorkspaceManager();
                TextView fileName = (TextView) findViewById(R.id.file_name);
                TextView fileText = (TextView) findViewById(R.id.file_text);

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
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_file, menu);
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
