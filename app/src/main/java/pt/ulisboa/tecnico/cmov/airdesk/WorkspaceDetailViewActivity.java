package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;


public class WorkspaceDetailViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_grid_view);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workspace_detail_view, menu);
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
                                 Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_workspace_detail_view, container, false);

            GridView gridView = (GridView) rootView.findViewById(R.id.folder_view);

            OwnedWorkspace workspace = null;

            if (intent != null) {

                if (intent.hasExtra(Constants.WORKSPACE)) {
                    workspace = (OwnedWorkspace) intent.getSerializableExtra(Constants.WORKSPACE);

                }
            }

            if(workspace == null){
                //should do something here. because this is a terrible position to be :(
            }


            ArrayList<Map<String, Object>> list = new ArrayList<>();

            for(String file : workspace.getFileNames()) {
                Map map = new HashMap();
                map.put("fileIcon", R.drawable.file);
                map.put("fileName", file);
                list.add(map);
            }


            SimpleAdapter adapter = new SimpleAdapter(getActivity(),list,
                    R.layout.workspace_folder, new String[] { "fileIcon", "fileName" },
                    new int[] {R.id.file_image, R.id.file_name });



            gridView.setAdapter(adapter);

            return rootView;
        }
    }


}
