package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class CreateWorkspaceActivity extends ActionBarActivity {

    static MenuItem doneMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workspace);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_workspace, menu);
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
            String name = String.valueOf(((TextView) findViewById(R.id.ws_name)).getText()).trim();
            String tags = String.valueOf(((TextView) findViewById(R.id.tag_text)).getText()).trim();
            CheckBox publicCheckBox = (CheckBox) findViewById(R.id.is_public_checkbx);
            final SeekBar quotaBar = (SeekBar) findViewById(R.id.quota_seekbar);


            WorkspaceManager workspaceManager = new WorkspaceManager();
            // boolean memoryInsufficient = workspaceManager.isNotSufficientMemory(Double.valueOf(quota));
                   /* if(memoryInsufficient) {
                        ((TextView) rootView.findViewById(R.id.quota)).setError("quota is too big");
                    } else {*/

            OwnedWorkspace ownedWorkspace = new OwnedWorkspace(name,
                    new UserManager().getOwner().getUserId(), Double.parseDouble(String.valueOf(quotaBar.getProgress())));
            ownedWorkspace.setPublic(publicCheckBox.isChecked());

            ownedWorkspace.addTags(Arrays.asList(tags.replace(" ", "").split(",")));
            workspaceManager.createWorkspace(ownedWorkspace);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 final Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_create_workspace, container, false);
            final CheckBox publicCheckBox = (CheckBox) rootView.findViewById(R.id.is_public_checkbx);
            publicCheckBox.setOnClickListener(new CheckBox.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    TextView tagText = (TextView) rootView.findViewById(R.id.tag_text);
                    if (checkBox.isChecked()) {
                        tagText.setVisibility(View.VISIBLE);
                    } else {
                        tagText.setVisibility(View.INVISIBLE);
                    }
                }
            });

            final SeekBar quotaBar = (SeekBar) rootView.findViewById(R.id.quota_seekbar);
            final TextView nameText = (TextView) rootView.findViewById(R.id.ws_name);

            WorkspaceManager manager = new WorkspaceManager();

            quotaBar.setMax((int) manager.getMaximumDeviceSpace());
            TextView quotaMaxText = (TextView) rootView.findViewById(R.id.max_quota_txt);

            quotaMaxText.setText(String.valueOf((int) manager.getMaximumDeviceSpace()));

            quotaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    final TextView quotaTxt = (TextView) rootView.findViewById(R.id.quota_size_txt);
                    quotaTxt.setText(String.valueOf(progress));
                    if (progress > 0 && nameText.getText().length() > 0)
                        doneMenuItem.setVisible(true);
                    else
                        doneMenuItem.setVisible(false);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    final InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);

                    //  imm.toggleSoftInput (InputMethodManager.SHOW_FORCED, 0);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            nameText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (quotaBar.getProgress() > 0 && s.length() > 0)
                        doneMenuItem.setVisible(true);
                    else
                        doneMenuItem.setVisible(false);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (quotaBar.getProgress() == 0) {
                        quotaBar.requestFocus();
                    }
                }
            });

            return rootView;


        }
    }
}
