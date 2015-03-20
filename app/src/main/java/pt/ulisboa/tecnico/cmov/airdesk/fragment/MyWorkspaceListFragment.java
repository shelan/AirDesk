package pt.ulisboa.tecnico.cmov.airdesk.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.WorkspaceDetailViewActivity;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;

/**
 * Created by shelan on 3/15/15.
 */
public class MyWorkspaceListFragment extends Fragment {

    private ArrayAdapter<String> arrayAdapter;

    private WorkspaceManager manager = new WorkspaceManager();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_workspace, container, false);
        ArrayList<String> dummyData = new ArrayList<String>();
        dummyData.add("My workspace 1");
        dummyData.add("My workspace 2");


        arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_workspace,
                R.id.list_item_workspace,
                dummyData);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView workspaceListView = (ListView) rootView.findViewById(R.id.listview_workspace);
        workspaceListView.setAdapter(arrayAdapter);

        workspaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.

                String workspace = arrayAdapter.getItem(position);


                Intent intent = new Intent(getActivity(), WorkspaceDetailViewActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, workspace)
                        .putExtra(Constants.WORKSPACE, manager.getOwnedWorkspace(workspace));

                startActivity(intent);
                Toast.makeText(getActivity(), "You are now in " + workspace,
                        Toast.LENGTH_SHORT).show();
            }
        });


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWorkspaceList();
    }

    private void updateWorkspaceList() {

        ForeignWorkspaceDataAsync foreignWorkspaceDataAsync = new ForeignWorkspaceDataAsync();
        foreignWorkspaceDataAsync.execute();

    }

    public class ForeignWorkspaceDataAsync extends AsyncTask<Void, Void, ArrayList<String>> {

        private final String LOG_TAG = ForeignWorkspaceDataAsync.class.getSimpleName();

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
           if(result != null){
               arrayAdapter.clear();
               for (String s : result) {
                   arrayAdapter.add(s);
               }
           }

        }
    }
}
