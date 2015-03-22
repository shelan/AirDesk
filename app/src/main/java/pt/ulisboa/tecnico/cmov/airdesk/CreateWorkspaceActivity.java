package pt.ulisboa.tecnico.cmov.airdesk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Arrays;

import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class CreateWorkspaceActivity extends ActionBarActivity {

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
            CheckBox publicCheckBox = (CheckBox) rootView.findViewById(R.id.is_public_checkbx);
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

            Button button = (Button) rootView.findViewById(R.id.create_btn);
            button.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String name = String.valueOf(((TextView) rootView.findViewById(R.id.ws_name)).getText()).trim();
                    String email = String.valueOf(((TextView) rootView.findViewById(R.id.ws_email)).getText()).trim();
                    String tags = String.valueOf(((TextView) rootView.findViewById(R.id.tag_text)).getText()).trim();
                    String quota = String.valueOf(((TextView) rootView.findViewById(R.id.quota)).getText()).trim();


                    WorkspaceManager manager = new WorkspaceManager();
                    OwnedWorkspace ownedWorkspace = new OwnedWorkspace(name,
                            email, Double.parseDouble(quota));

                    ownedWorkspace.addTags(Arrays.asList(tags.split(",")));
                    manager.createWorkspace(ownedWorkspace);
                    getActivity().finish();
                }
            });
            return rootView;


        }
    }
}
