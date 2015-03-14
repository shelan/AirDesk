package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class Workspace {
    String workspaceName;
    long quota;
    List<String> accessorList=new ArrayList<String>();
    private String ownerName;

    public List<String>getAccessorList(){

    }

    public boolean removeUser(){

    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
