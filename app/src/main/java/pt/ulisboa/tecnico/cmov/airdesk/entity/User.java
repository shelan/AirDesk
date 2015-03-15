package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class User {
    private String nickName;
    private String email;
    private List<String> ownedWorkspaces=new ArrayList<String>();

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addNewWs(String WSName){
        ownedWorkspaces.add(WSName);
    }

    public void removeWS(String WSName){
        for(int i=0;i<ownedWorkspaces.size();i++) {

            if(ownedWorkspaces.get(i).toLowerCase().equals(WSName.toLowerCase())){
                ownedWorkspaces.remove(i);
            }
        }
    }
}
