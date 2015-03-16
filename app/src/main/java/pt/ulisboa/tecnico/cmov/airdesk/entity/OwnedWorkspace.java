package pt.ulisboa.tecnico.cmov.airdesk.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class OwnedWorkspace extends AbstractWorkspace {

    private String ownerEmail;
    private HashMap<String,Boolean> clients=new HashMap<String,Boolean>();
    private String ownerName;
    private boolean isPublic;
    List<String> tags=new ArrayList<String>();

    public OwnedWorkspace(String workspaceName, String owner, double quota) {
        super(workspaceName, owner, quota);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Map<String,Boolean> getClients() {
        return clients;
    }

    public void addClient(String clientName,boolean IsInactive) {
        clients.put(clientName, IsInactive);
    }

    public void activateClient(String clientName,boolean IsInactive){//when clients receive msg/subscribe to public profile make them active
        clients.put(clientName,IsInactive);
    }

    public void removeClient(String clientName) {
        clients.remove(clientName);
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<String>getTags(){return tags;}
    public void addTags(List<String>newTags){
        tags.addAll(newTags);
    }

    public void deleteTags(List<String>deleted){
        for (int i = 0; i <tags.size() ; i++) {
            for(int j=0;j<deleted.size();j++){
                if(tags.get(i).toLowerCase().equals(deleted.get(j).toLowerCase())){
                    tags.remove(i);
                }
            }
        }
    }
}
