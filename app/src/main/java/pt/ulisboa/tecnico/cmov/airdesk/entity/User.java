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
    private List<String>foreignWorkspaces=new ArrayList<>();

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


    public void addNewOwnedWs(String WSName){
        getOwnedWorkspaces().add(WSName);
    }

    public void removeOwnedWS(String WSName){
        for(int i=0;i< getOwnedWorkspaces().size();i++) {

            if(getOwnedWorkspaces().get(i).toLowerCase().equals(WSName.toLowerCase())){
                getOwnedWorkspaces().remove(i);
            }
        }
    }

    public List<String> getOwnedWorkspaces() {
        return ownedWorkspaces;
    }

    public void addForeignWS(String WSName){getForeignWorkspaces();}

    public void removeForeignWS(String WSName){
        for(int i=0;i< getForeignWorkspaces().size();i++) {

            if(getOwnedWorkspaces().get(i).toLowerCase().equals(WSName.toLowerCase())){
                getOwnedWorkspaces().remove(i);
            }
        }
    }

    public List<String> getForeignWorkspaces() {
        return foreignWorkspaces;
    }

}
