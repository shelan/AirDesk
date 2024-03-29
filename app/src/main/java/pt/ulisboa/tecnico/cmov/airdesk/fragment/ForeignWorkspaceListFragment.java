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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.airdesk.AirDeskService;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.activity.WorkspaceDetailViewActivity;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.CommunicationTask;

public class ForeignWorkspaceListFragment extends Fragment {

    private ArrayAdapter<String> arrayAdapter;

    private ArrayList<String> workspaceList = new ArrayList<>();

    private WorkspaceManager workspaceManager = new WorkspaceManager();

    private SimpleAdapter adapter;

    private List<Map<String, Object>> data = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       updateWorkspaceList();

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
                String uniqueWorkspaceName = (String) item.get("workspaceName");
                String workspace = uniqueWorkspaceName.split("/")[1];
                String ownerId = uniqueWorkspaceName.split("/")[0];

                Intent intent = new Intent(getActivity(), WorkspaceDetailViewActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, workspace)
                        //.putExtra(Constants.WORKSPACE_NAME, workspaceManager.getOwnedWorkspace(workspace))
                        .putExtra(Constants.WORKSPACE_NAME, workspaceManager.getForeignWorkspace(workspace, ownerId))
                        .putExtra(Constants.IS_OWNED_WORKSPACE, false);

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
                final HashSet<String> oldSubscriptions = new UserManager().getOwner().getSubscribedTags();
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

                            workspaceManager.subscribeToTags(subscriptions.toArray(new String[subscriptions.size()]));
                            workspaceManager.unsubscribeFromTags(oldSubscriptions.toArray(new String[oldSubscriptions.size()]));

                            updateWorkspaceList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.show();
            }
        });

      /*  Button connect_btn = (Button) rootView.findViewById(R.id.connect_btn);
        connect_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AirDeskService.getInstance().getGroupOwnerAddress() != null) {
                    CommunicationTask.OutgoingCommTask outgoingCommTask = new CommunicationTask(new ForeignWorkspaceListFragment()).getOutgoingCommTask();
                    outgoingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            AirDeskService.getInstance().getGroupOwnerAddress().getHostAddress(), new UserManager().getOwner().getUserId());

                    AirDeskService.getInstance().requestIdIpMap();
                }

            }
        });*/

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
        System.out.println("......... updating foreign workspace list ......");
        ForeignWorkspaceDataAsync foreignWorkspaceDataAsync = new ForeignWorkspaceDataAsync();
        foreignWorkspaceDataAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public class ForeignWorkspaceDataAsync extends AsyncTask<Void, Void, List<String>> {

        private final String LOG_TAG = ForeignWorkspaceDataAsync.class.getSimpleName();

        @Override
        protected List<String> doInBackground(Void... params) {
            System.out.println("........ getting updated list from bkend...... ");
            return new UserManager().getForeignWorkspaces();
        }

        @Override
        protected void onPostExecute(List<String> result) {
            System.out.println("...... updating foreign ws list........");
            if (result != null) {
                workspaceList.clear();
                for (String s : result) {
                    //workspaceList.add(s.split("/")[1]);
                    workspaceList.add(s);
                }
            }
            fillDataAdapter(workspaceList);
            adapter.notifyDataSetChanged();
            System.out.println("...... updated foreign ws list........");

        }
    }
}
