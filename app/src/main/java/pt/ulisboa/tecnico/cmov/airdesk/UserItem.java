package pt.ulisboa.tecnico.cmov.airdesk;

/**
 * Created by shelan on 4/6/15.
 */
public class UserItem {
    private String name;
    private boolean box;


    public UserItem(String name, boolean checkBox) {
        this.setName(name);
        this.setBox(checkBox);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBox() {
        return box;
    }

    public void setBox(boolean box) {
        this.box = box;
    }
}
