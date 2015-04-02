package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class User {
    private String nickName;
    private String userId;
    private List<String> ownedWorkspaces=new ArrayList<String>();
    private List<String>foreignWorkspaces=new ArrayList<>();
    private Map<String, List<String>> deletedWorkspaces = new HashMap<String, List<String>>();//whenever notidication is received removefrom this
    private HashSet<String> subscribedTags = new HashSet<String>();


    public User(String nickName, String userId) {
        setNickName(nickName);
        setUserId(userId);
    }

    public void updateDeletedWorkspacesMap(String workspaceName, List<String> accessList){
        deletedWorkspaces.put(workspaceName,accessList);
    }


    public Map<String,List<String>> getDeletedWorkspacesMap(){
        return deletedWorkspaces;
    }
    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public void addNewOwnedWorkspace(String WorkspaceName){
        getOwnedWorkspaces().add(WorkspaceName);
    }

    public void removeFromOwnedWorkspaceList(String WorkspaceName){
        for(int i=0;i< getOwnedWorkspaces().size();i++) {

            if(getOwnedWorkspaces().get(i).toLowerCase().equals(WorkspaceName.toLowerCase())){
                getOwnedWorkspaces().remove(i);
            }
        }
    }

    public List<String> getOwnedWorkspaces() {
        return ownedWorkspaces;
    }

    public void addForeignWS(String WorkspaceName){
        foreignWorkspaces.add(WorkspaceName);
    }

    public void removeFromForeignWorkspaceList(String WorkspaceName){
        for(int i=0;i< getForeignWorkspaces().size();i++) {

            if(getForeignWorkspaces().get(i).toLowerCase().equals(WorkspaceName.toLowerCase())){
                getForeignWorkspaces().remove(i);
            }
        }
    }

    public List<String> getForeignWorkspaces() {
        return foreignWorkspaces;
    }

    public HashSet<String> getSubscribedTags() {
        return subscribedTags;
    }

    public void addSubscriptionTags(String[] tags) {
        for (String tag : tags) {
            subscribedTags.add(tag.toLowerCase());
        }
    }

    public void removeSubscription(String[] tags) {
        for (String tag : tags) {
            subscribedTags.remove(tag.toLowerCase());
        }
    }
}
