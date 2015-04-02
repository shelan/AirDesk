package pt.ulisboa.tecnico.cmov.airdesk.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.airdesk.AirDeskManager;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.activity.CreateWorkspaceActivity;
import pt.ulisboa.tecnico.cmov.airdesk.activity.WorkspaceDetailViewActivity;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;

/**
 * Created by shelan on 3/15/15.
 */
public class ForiegnWorkspaceListFragment extends Fragment {

    private ArrayAdapter<String> arrayAdapter;

    private ArrayList<String> workspaceList = new ArrayList<>();

    private AirDeskManager manager = new AirDeskManager();
    private UserManager userManager = new UserManager();

    private SimpleAdapter adapter;

    private List<Map<String, Object>> data = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_foreign_workspace, container, false);
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
                        .putExtra(Constants.WORKSPACE, manager.getOwnedWorkspace(workspace))
                        .putExtra(Constants.IS_OWNED_WORKSPACE,false);

                startActivity(intent);
                Toast.makeText(getActivity(), "You are now in " + workspace,
                        Toast.LENGTH_SHORT).show();
            }
        });

        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageButton);
        imageView.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                final EditText input = new EditText(getActivity());
                final HashSet<String> oldSubscriptions = userManager.getOwner().getSubscribedTags();
                input.setText(oldSubscriptions.toString().replace("[", "").replace("]", ""));
                builder.setTitle("Subscribe to Tags");
                builder.setView(input);
                builder.setPositiveButton(R.string.add_subscribed_tag, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String[] tags = input.getText().toString().replace(" ", "").split(",");
                            HashSet<String> subscriptions = new HashSet<String>(Arrays.asList(tags));
                            HashSet<String> intersect = new HashSet<String>(oldSubscriptions);
                            intersect.retainAll(subscriptions);

                            //keep unsubscribed tags in oldSubscriptions
                            oldSubscriptions.removeAll(intersect);

                            //keep new subscriptions in subscription hashset
                            subscriptions.removeAll(intersect);

                            userManager.subscribeToTags(subscriptions.toArray(new String[subscriptions.size()]));
                            userManager.unsubscribeFromTags(oldSubscriptions.toArray(new String[oldSubscriptions.size()]));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                builder.setNegativeButton("Close", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.show();
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
            map.put("fileIcon", R.drawable.foreign_workspace);
            map.put("workspaceName", file);

            data.add(map);
        }
        return data;
    }

    public void updateWorkspaceList() {

        ForeignWorkspaceDataAsync foreignWorkspaceDataAsync = new ForeignWorkspaceDataAsync();
        foreignWorkspaceDataAsync.execute();

    }

    public class ForeignWorkspaceDataAsync extends AsyncTask<Void, Void, List<String>> {

        private final String LOG_TAG = ForeignWorkspaceDataAsync.class.getSimpleName();

        @Override
        protected List<String> doInBackground(Void... params) {
            return new UserManager().getForeignWorkspaces();
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
