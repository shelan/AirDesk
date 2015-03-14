package pt.ulisboa.tecnico.cmov.airdesk.manager;

import com.google.gson.Gson;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.entity.User;
import pt.ulisboa.tecnico.cmov.airdesk.storage.*;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class UserManager {

    public void createUser(User user) {
        Gson gson=new Gson();
        String jsonString=gson.toJson(user);
        StorageManager storageManager=new StorageManager();
        storageManager.saveToInternalFile(jsonString);
    }

   public List<String> getOwnedWorkspaces(){
     return null;
   }

    public List<String>getForeignWorkspaces(){
     return null;
   }




}
