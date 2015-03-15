package pt.ulisboa.tecnico.cmov.airdesk.manager;

import com.google.gson.Gson;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.storage.*;

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
     return null;
   }

    public void notifyClientOnAddWorkspace() {
        //
    }


}
