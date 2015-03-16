package pt.ulisboa.tecnico.cmov.airdesk.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.R;

/**
 * Created by shelan on 3/15/15.
 */
public class ForiegnWorkspaceListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_foreign_workspace, container ,false);
        ArrayList<String> dummyData = new ArrayList<String>();
        dummyData.add("Foreign workspace 1");
        dummyData.add("Foreign workspace 2");
        dummyData.add("Foreign workspace 3");
        dummyData.add("Foreign workspace 4");
        dummyData.add("Foreign workspace 5");
        dummyData.add("Foreign workspace 5");
        dummyData.add("Foreign workspace 5");
        dummyData.add("Foreign workspace 5");


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_workspace,
                R.id.list_item_workspace,
                dummyData);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView workspaceListView = (ListView) rootView.findViewById(R.id.listview_foreign_workspace);
        workspaceListView.setAdapter(arrayAdapter);

        return rootView;
    }
}
