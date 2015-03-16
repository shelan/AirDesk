package pt.ulisboa.tecnico.cmov.airdesk.manager;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.entity.User;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class UserManager {

    public void createUser(User user) {
        MetadataManager metaManager=new MetadataManager();
        metaManager.saveUser(user);
    }
    public User getOwner(){
        MetadataManager metaManager=new MetadataManager();
        User user=metaManager.getUser();
        return user;
    }

    public List<String> getOwnedWorkspaces(){
        //read ownedWorkSpaces from user
        User user=getOwner();
        List<String>ownedWorkSpaces=user.getOwnedWorkspaces();
        return ownedWorkSpaces;
    }

    public List<String>getForeignWorkspaces(){
     User user=getOwner();
        return user.getForeignWorkspaces();

   }

    public void notifyClientOnAddForeignWorkspace() {
        //
    }


}
