package pt.ulisboa.tecnico.cmov.airdesk.manager;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class UserManager {

    MetadataManager metaManager = new MetadataManager();

    /*public void createOwner(User user) {
        metaManager.saveUser(user);
    }*/

    public void createOwner(String nickName, String userId) {
        User owner = new User(nickName, userId);
        metaManager.saveUser(owner);
    }

    public void updateOwner(User user) {
        metaManager.saveUser(user);
    }

    public User getOwner() {
        User user = metaManager.getUser();
        return user;
    }

    public void deleteOwner() {
        metaManager.deleteFile(Constants.USER_JSON_FILE_NAME);
    }

    public List<String> getOwnedWorkspaces(){
        //read ownedWorkSpaces from user
        User user = getOwner();
        List<String> ownedWorkSpaces = user.getOwnedWorkspaces();
        return ownedWorkSpaces;
    }

    public List<String> getForeignWorkspaces() {
        User user = getOwner();
        return user.getForeignWorkspaces();

    }

    public void notifyClientOnAddForeignWorkspace() {
        //
    }

    //TODO://should be called after sending delete msg to client
    public void removeClientFromDeletedMap(String workspaceName,String clientId){
      User user=getOwner();
        List<String>accessList=user.getDeletedWorkspacesMap().get(workspaceName);
        for (int i = 0; i <accessList.size() ; i++) {
            if(accessList.get(i).toLowerCase().equals(clientId.toLowerCase())){
                accessList.remove(i);
                user.updateDeletedWorkspacesMap(workspaceName, accessList);
            }
        }
    }

    //use the method in workspace manager for this
    public void subscribeToTags(String[] tags) {
        User owner = getOwner();
        owner.addSubscriptionTags(tags);
        updateOwner(owner);
    }

    //use the method in workspace manager for this
    public void unsubscribeFromTags(String[] tags) {
        User owner = getOwner();
        owner.removeSubscription(tags);
        updateOwner(owner);
    }



}
