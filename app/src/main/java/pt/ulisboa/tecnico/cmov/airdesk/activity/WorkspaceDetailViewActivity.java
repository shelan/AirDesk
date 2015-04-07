package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class WorkspaceDetailViewActivity extends ActionBarActivity {

    static OwnedWorkspace workspace;
    private boolean isOwnedWorkspace = false;

    private WorkspaceDetailFragment workspaceDetailFragment;
    static ArrayList<Map<String, Object>> list = new ArrayList<>();
    WorkspaceManager workspaceManager = new WorkspaceManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_workspace_grid_view);
        if (savedInstanceState == null) {
            workspaceDetailFragment = new WorkspaceDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WorkspaceDetailFragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workspace_detail_view, menu);
        Intent intent = getIntent();
        // Removing delete workspace option for foriegn workspaces
        if (intent != null && intent.hasExtra(Constants.IS_OWNED_WORKSPACE)) {
            isOwnedWorkspace = intent.getBooleanExtra(Constants.IS_OWNED_WORKSPACE, false);
        }
        if (!isOwnedWorkspace) {
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(false);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent currentIntent = getIntent();
        boolean isOwnedWorkspace = false;
        if (currentIntent != null && currentIntent.hasExtra(Constants.IS_OWNED_WORKSPACE)) {
            isOwnedWorkspace = currentIntent.getBooleanExtra(Constants.IS_OWNED_WORKSPACE, false);
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.add_file) {
            Intent intent = new Intent(this, CreateFileActivity.class);
            intent.putExtra(Constants.WORKSPACE_NAME, workspace.getWorkspaceName())
                    .putExtra(Constants.OWNER, workspace.getOwnerName())
                    .putExtra(Constants.IS_OWNED_WORKSPACE, isOwnedWorkspace);
            startActivity(intent);
        }
        if (id == R.id.edit_access_list) {
            Intent intent = new Intent(this, EditAccessListActivity.class);
            intent.putExtra(Constants.WORKSPACE_NAME, workspace.getWorkspaceName())
                    .putExtra(Constants.IS_OWNED_WORKSPACE, isOwnedWorkspace);

            startActivity(intent);
        }
        if (id == R.id.delete_workspace) {
            //TODO: when in foreign workspace, should call delete foreign workspace... :)
            AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                    //set message, title, and icon
                    .setTitle("Delete")
                    .setMessage("Do you want to delete this workspace ?")
                    .setIcon(R.drawable.delete)

                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            workspaceManager.deleteOwnedWorkspace(workspace.getWorkspaceName());
                            dialog.dismiss();
                            finish();
                        }

                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    })
                    .create();
            myQuittingDialogBox.show();
        }
        if (id == R.id.edit_workspace) {
            Intent intent = new Intent(this, EditWorkspaceActivity.class);
            intent.putExtra(Constants.WORKSPACE_NAME, workspace.getWorkspaceName())
                    .putExtra(Constants.IS_OWNED_WORKSPACE, isOwnedWorkspace);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);

    }

    public WorkspaceDetailFragment getWorkspaceDetailFragment() {
        return workspaceDetailFragment;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class WorkspaceDetailFragment extends Fragment implements Serializable {
        private SimpleAdapter adapter;
        private WorkspaceManager workspaceManager;

        public WorkspaceDetailFragment() {
            workspaceManager = new WorkspaceManager();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_workspace_detail_view, container, false);

            GridView gridView = (GridView) rootView.findViewById(R.id.folder_view);

            if (intent != null) {

                if (intent.hasExtra(Constants.WORKSPACE_NAME)) {
                    workspace = (OwnedWorkspace) intent.getSerializableExtra(Constants.WORKSPACE_NAME);

                }
            }

            getActivity().setTitle(workspace.getWorkspaceName());

            if (workspace == null) {
                //should do something here. because this is a terrible position to be :(
            }

            list.clear();

            for (String file : workspace.getFileNames()) {
                Map map = new HashMap();
                map.put("fileIcon", R.drawable.file);
                map.put("fileName", file);
                list.add(map);
            }

            adapter = new SimpleAdapter(getActivity(), list,
                    R.layout.workspace_folder, new String[]{"fileIcon", "fileName"},
                    new int[]{R.id.file_image, R.id.file_name});

            gridView.setAdapter(adapter);

            final OwnedWorkspace finalWorkspace = workspace;

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Map<String, Object> item = (Map<String, Object>) adapter.getItem(position);
                    String fileName = (String) item.get("fileName");

                    Intent intent = new Intent(getActivity(), TextFileEditActivity.class)
                            .putExtra(Constants.FILENAME, fileName)
                            .putExtra(Constants.WORKSPACE_NAME, finalWorkspace.getWorkspaceName())
                            .putExtra(Constants.OWNER, finalWorkspace.getOwnerName());

                    startActivity(intent);
                    Toast.makeText(getActivity(), "Opening file " + fileName,
                            Toast.LENGTH_SHORT).show();
                }
            });

            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getActivity())
                            //set message, title, and icon
                            .setTitle("Delete")
                            .setMessage("Do you want to delete this file ?")
                            .setIcon(R.drawable.delete)

                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Map<String, Object> item = (Map<String, Object>) adapter
                                            .getItem(position);
                                    String fileName = (String) item.get("fileName");
                                    //TODO:check isowner and set it properly without using true
                                    try {
                                        new WorkspaceManager().deleteDataFile(finalWorkspace
                                                        .getWorkspaceName(), fileName,
                                                finalWorkspace.getOwnerName(), true);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    refresh();
                                    dialog.dismiss();
                                }

                            })


                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();

                                }
                            })
                            .create();
                    myQuittingDialogBox.show();
                    return true;
                }
            });

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            refresh();
        }

        public void refresh() {
            if (workspace != null) {
                list.clear();

                workspace = workspaceManager.getOwnedWorkspace(workspace.getWorkspaceName());

                for (String file : workspace.getFileNames()) {
                    HashMap map = new HashMap();
                    map.put("fileIcon", R.drawable.file);
                    map.put("fileName", file);
                    list.add(map);
                }
            }
            Toast.makeText(getActivity(), "Refresing  file list ",
                    Toast.LENGTH_SHORT).show();

            adapter.notifyDataSetChanged();
        }
    }


}
