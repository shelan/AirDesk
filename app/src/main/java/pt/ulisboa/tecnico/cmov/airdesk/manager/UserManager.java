package pt.ulisboa.tecnico.cmov.airdesk.manager;

import java.security.acl.Owner;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class UserManager {
    MetadataManager metaManager=new MetadataManager();

    public void createOwner(User user) {
        metaManager.saveUser(user);
    }

    public void updateOwner(User user) {
        metaManager.saveUser(user);
    }

    public User getOwner() {
        User user = metaManager.getUser();
        return user;
    }

    public void deleteOwner(String userName) {
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

    public void subscribeToTags(String[] tags) {
        User owner = getOwner();
        owner.addSubscriptionTags(tags);
        updateOwner(owner);
    }

    public void unsubscribeFromTags(String[] tags) {
        User owner = getOwner();
        owner.removeSubscription(tags);
        updateOwner(owner);
    }

    public void receivePublishedTags(String ownerId, String[] tags) {
        //match to subscribed tags n request Workspace
    }
}
