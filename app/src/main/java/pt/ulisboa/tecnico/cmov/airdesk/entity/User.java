package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class User {
    private String nickName;
    private String userId;
    private List<String> ownedWorkspaces=new ArrayList<String>();
    private List<String>foreignWorkspaces=new ArrayList<>();
    private Map<String, List<String>> deletedWorkspaces = new HashMap<String, List<String>>();//whenever notidication is received removefrom this
    private HashSet<String> subscribedTags = new HashSet<String>();
    private HashMap<String, ArrayList<String>> tagsNWorkspaces = new HashMap<String, ArrayList<String>>();


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

    /**
     *
     * @param uniqueWorkspaceName will hava <ownerId>/<workspaceName>
     */
    public void addForeignWS(String uniqueWorkspaceName){
        foreignWorkspaces.add(uniqueWorkspaceName);
    }

    public void removeFromForeignWorkspaceList(String uniqueWorkspaceName){
        for(int i=0;i< getForeignWorkspaces().size();i++) {
            if(getForeignWorkspaces().get(i).toLowerCase().equals(uniqueWorkspaceName.toLowerCase())){
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
            tagsNWorkspaces.put(tag.toLowerCase(), new ArrayList<String>());
        }
    }

    public HashSet<String> removeSubscription(String[] tags) {
        HashSet<String> taggedWorkspaces = new HashSet<String>();
        for (String tag : tags) {
            subscribedTags.remove(tag.toLowerCase());
            ArrayList<String> workspaces = tagsNWorkspaces.remove(tag.toLowerCase());
            taggedWorkspaces.addAll(workspaces);
        }
        return taggedWorkspaces;
    }

    public void addTagForeignWorkspaceMapping(String tag, String uniqueWorkspaceName) {
        //tagsNWorkspaces must have this entry since at tag subscription time it adds to the list
        ArrayList<String> workspaces = tagsNWorkspaces.get(tag);
        if(!workspaces.contains(uniqueWorkspaceName)) {
            workspaces.add(uniqueWorkspaceName);
            tagsNWorkspaces.put(tag, workspaces);
        }
    }
}
