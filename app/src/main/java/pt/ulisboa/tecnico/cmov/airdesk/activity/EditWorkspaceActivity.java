package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class EditWorkspaceActivity extends ActionBarActivity {

    OwnedWorkspace workspaceToUpdate;
    MenuItem doneMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WorkspaceManager workspaceManager = new WorkspaceManager();

        setContentView(R.layout.activity_edit_workspace);
        if (getIntent() != null) {
            final String workspaceName = getIntent().getStringExtra(Constants.WORKSPACE_NAME);
            workspaceToUpdate = workspaceManager.getOwnedWorkspace(workspaceName);

            TextView wsName = ((TextView) findViewById(R.id.ws_name_edit));
            wsName.setText(workspaceName);

            EditText tagText = (EditText) findViewById(R.id.tag_text_edit);
            final HashSet<String> oldTags = new HashSet<String>();
            oldTags.addAll(workspaceToUpdate.getTags());

            if (workspaceToUpdate.isPublic()) {
                //allow editing tags

                tagText.setText(oldTags.toString().replace("[", "").replace("]", ""));
                tagText.setVisibility(View.VISIBLE);
            }

            final SeekBar quotaBar = (SeekBar) findViewById(R.id.quota_seekbar_edit);
            quotaBar.setMax((int) workspaceManager.getMaximumDeviceSpace());
            TextView maxQuotaText = (TextView) findViewById(R.id.max_quota_txt_edit);
            maxQuotaText.setText(String.valueOf((int) workspaceManager.getMaximumDeviceSpace()));

            final int currentWorkspaceSize = (int) Math.ceil(workspaceManager
                    .getCurrentWorkspaceSize(workspaceName));

            TextView minQuotaText = (TextView) findViewById(R.id.min_quota_txt_edit);
            minQuotaText.setText(String.valueOf(currentWorkspaceSize));

            int quota = (int) workspaceToUpdate.getQuota();
            TextView quotaText = ((TextView) findViewById(R.id.quota_size_txt_edit));
            quotaText.setText(String.valueOf(quota));

            //workaround due to a bug in android.
            //http://stackoverflow.com/questions/4348032/android-progressbar-does-not-update-progress-view-drawable
            quotaBar.setProgress(0);
            quotaBar.setProgress(quota);

            //Button button = (Button) findViewById(R.id.update_ws_btn);

            quotaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    final TextView quotaTxt = (TextView) findViewById(R.id.quota_size_txt_edit);
                    quotaTxt.setText(String.valueOf(progress + currentWorkspaceSize));
                    doneMenuItem.setVisible(true);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

           /* button.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {

                    HashSet<String> newTags = new HashSet<String>();
                    if (workspaceToUpdate.isPublic()) {
                        String[] tags = String.valueOf(((TextView) findViewById(R.id.tag_text_edit))
                                .getText()).replace(" ", "").split(",");
                        newTags.addAll(Arrays.asList(tags));
                    }

                    WorkspaceManager workspaceManager = new WorkspaceManager();
                    // boolean memoryInsufficient = workspaceManager.isNotSufficientMemory(Double.valueOf(quota));
                   *//* if(memoryInsufficient) {
                        ((TextView) rootView.findViewById(R.id.quota)).setError("quota is too big");
                    } else {*//*
                    workspaceToUpdate.setQuota(Double.parseDouble(String.valueOf(quotaBar.getProgress())));

                    boolean tagsChanged = false;
                    if (!oldTags.equals(newTags)) {
                        tagsChanged = true;

                        HashSet<String> intersect = oldTags;
                        intersect.retainAll(newTags);

                        //keep removed tags in oldTags
                        oldTags.removeAll(intersect);

                        //keep new tags in newTags hashset
                        newTags.removeAll(intersect);

                        ArrayList<String> tags = new ArrayList<String>();
                        tags.addAll(newTags);
                        workspaceToUpdate.addTags(tags);

                        tags = new ArrayList<String>();
                        tags.addAll(oldTags);
                        workspaceToUpdate.deleteTags(tags);
                    }

                    workspaceManager.editOwnedWorkspace(workspaceName, workspaceToUpdate, tagsChanged);
                    finish();
                }
            });*/
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_workspace, menu);
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
        if (id == R.id.action_workspace_create) {


            SeekBar quotaBar = (SeekBar) findViewById(R.id.quota_seekbar_edit);
            HashSet<String> oldTags = new HashSet<String>();
            oldTags.addAll(workspaceToUpdate.getTags());

            HashSet<String> newTags = new HashSet<String>();
            if (workspaceToUpdate.isPublic()) {
                String[] tags = String.valueOf(((TextView) findViewById(R.id.tag_text_edit))
                        .getText()).replace(" ", "").split(",");
                newTags.addAll(Arrays.asList(tags));
            }

            WorkspaceManager workspaceManager = new WorkspaceManager();
            // boolean memoryInsufficient = workspaceManager.isNotSufficientMemory(Double.valueOf(quota));
                   /* if(memoryInsufficient) {
                        ((TextView) rootView.findViewById(R.id.quota)).setError("quota is too big");
                    } else {*/
            workspaceToUpdate.setQuota(Double.parseDouble(String.valueOf(quotaBar.getProgress())));

            boolean tagsChanged = false;
            if (!oldTags.equals(newTags)) {
                tagsChanged = true;

                HashSet<String> intersect = oldTags;
                intersect.retainAll(newTags);

                //keep removed tags in oldTags
                oldTags.removeAll(intersect);

                //keep new tags in newTags hashset
                newTags.removeAll(intersect);

                ArrayList<String> tags = new ArrayList<String>();
                tags.addAll(newTags);
                workspaceToUpdate.addTags(tags);

                tags = new ArrayList<String>();
                tags.addAll(oldTags);
                workspaceToUpdate.deleteTags(tags);
            }

            workspaceManager.editOwnedWorkspace(workspaceToUpdate.getWorkspaceName(),
                    workspaceToUpdate, tagsChanged);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
