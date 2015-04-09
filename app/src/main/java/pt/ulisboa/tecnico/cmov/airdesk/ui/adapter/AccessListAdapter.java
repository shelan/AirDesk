package pt.ulisboa.tecnico.cmov.airdesk.ui.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.R;

public class AccessListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<UserItem> objects;
    private MenuItem addUserMenuItem;
    private MenuItem removeUserMenuItem;

    public AccessListAdapter(Context context, ArrayList<UserItem> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_accesslist, parent, false);
        }

        UserItem userItem = getUserItem(position);

        ((TextView) view.findViewById(R.id.list_item_access_list)).setText(userItem.getName());

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.user_access_checkbox);
        checkBox.setOnCheckedChangeListener(myCheckChangList);
        checkBox.setTag(position);
        checkBox.setChecked(userItem.isBox());
        return view;
    }

    UserItem getUserItem(int position) {
        return ((UserItem) getItem(position));
    }

    public ArrayList<UserItem> getCheckedUsers() {
        ArrayList<UserItem> box = new ArrayList<UserItem>();
        for (UserItem p : objects) {
            if (p.isBox())
                box.add(p);
        }
        return box;
    }

    OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            getUserItem((Integer) buttonView.getTag()).setBox(isChecked);
            if (getCheckedUsers().size() > 0) {
                addUserMenuItem.setVisible(false);
                removeUserMenuItem.setVisible(true);
            } else {
                addUserMenuItem.setVisible(true);
                removeUserMenuItem.setVisible(false);
            }
        }
    };

    public void setAddUserMenuItem(MenuItem addUserMenuItem) {
        this.addUserMenuItem = addUserMenuItem;
    }

    public void setRemoveUserMenuItem(MenuItem removeUserMenuItem) {
        this.removeUserMenuItem = removeUserMenuItem;
    }
}

