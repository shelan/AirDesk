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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.airdesk.AirDeskManager;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.CreateWorkspaceActivity;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.WorkspaceDetailViewActivity;

/**
 * Created by shelan on 3/15/15.
 */
public class MyWorkspaceListFragment extends Fragment {

    private ArrayList<String> workspaceList = new ArrayList<>();

    private AirDeskManager manager = new AirDeskManager();

    private SimpleAdapter adapter;

    private List<Map<String, Object>> data = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_workspace, container, false);
        data = fillDataAdapter(workspaceList);

        GridView gridView = (GridView) rootView.findViewById(R.id.owned_folder_view);

        adapter = new SimpleAdapter(getActivity(), data, R.layout.workspace_folder,
                new String[]{"fileIcon", "workspaceName"},
                new int[]{R.id.file_image, R.id.file_name});

        // Get a reference to the ListView, and attach this adapter to it.
       /* ListView workspaceListView = (ListView) rootView.findViewById(R.id.listview_workspace);
        workspaceListView.setAdapter(adapter);*/

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.

                Map<String, Object> item = (Map<String, Object>) adapter.getItem(position);
                String workspace = (String) item.get("workspaceName");

                Intent intent = new Intent(getActivity(), WorkspaceDetailViewActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, workspace)
                        .putExtra(Constants.WORKSPACE, manager.getOwnedWorkspace(workspace));

                startActivity(intent);
                Toast.makeText(getActivity(), "You are now in " + workspace,
                        Toast.LENGTH_SHORT).show();
            }
        });


        ImageView imageView = (ImageView) rootView.findViewById(R.id.addImage);
        imageView.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateWorkspaceActivity.class);
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWorkspaceList();
    }

    private List<Map<String, Object>> fillDataAdapter(ArrayList<String> dataList) {
        data.clear();
        for (String file : dataList) {
            Map map = new HashMap();

            map.put("fileIcon", R.drawable.home_blue);
            map.put("workspaceName", file);
            data.add(map);
        }
        return data;
    }

    public void updateWorkspaceList() {

        MyWorkspaceListDataAsync myWorkspaceListDataAsync = new MyWorkspaceListDataAsync();
        myWorkspaceListDataAsync.execute();

    }

    public class MyWorkspaceListDataAsync extends AsyncTask<Void, Void, List<String>> {

        private final String LOG_TAG = MyWorkspaceListDataAsync.class.getSimpleName();

        @Override
        protected List<String> doInBackground(Void... params) {
            return manager.getOwnedWorkspaces();
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (result != null) {
                workspaceList.clear();
                for (String s : result) {
                    workspaceList.add(s);
                }
            }
            fillDataAdapter(workspaceList);
            adapter.notifyDataSetChanged();
        }
    }
}
