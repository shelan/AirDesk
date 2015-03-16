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
public class MyWorkspaceListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_workspace, container ,false);
        ArrayList<String> dummyData = new ArrayList<String>();
        dummyData.add("My workspace 1");
        dummyData.add("My workspace 2");



        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_workspace,
                R.id.list_item_workspace,
                dummyData);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_workspace);
        listView.setAdapter(arrayAdapter);


        return rootView;
    }
}
