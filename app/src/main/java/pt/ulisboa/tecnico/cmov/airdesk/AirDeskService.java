package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.AirDeskMessage;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.CommunicationTask;

/**
 * Created by ashansa on 5/3/15.
 */
public class AirDeskService {

    private static AirDeskService instance = null;
    private Lock peerLock = new ReentrantLock();
    /////Set<String> connectedIpsVirtual = new HashSet<>();

    // will have the the owner ID,virtualIP of that device
    private HashMap<String,String> idIPMap = new HashMap<String,String>();
    private HashMap<String,String> macIpMap = new HashMap<String,String>();

    //private WifiP2pDevice myDevice = null;
    boolean isGroupOwner;
    InetAddress groupOwnerAddress;
    UserManager userManager = new UserManager();
    Gson gson = new Gson();

    public InetAddress getGroupOwnerAddress() {
        return groupOwnerAddress;
    }

    private static Logger logger = Logger.getLogger(AirDeskService.class.getName());

    private AirDeskService() {}

    public static AirDeskService getInstance() {
        if(instance == null) {
            instance = new AirDeskService();
            instance.loadIdIPMap();
            /*/////// temp adding ips
            instance.idIPMap.put("user1@gmail.com","192.168.49.1");
            instance.idIPMap.put("user2@gmail.com","192.168.49.81");
            instance.idIPMap.put("user3@gmail.com","192.168.49.133");*/
        }
        return instance;
    }

    private void loadIdIPMap() {
        SharedPreferences mPrefs = AirDeskApp.s_applicationContext.getSharedPreferences(
                AirDeskApp.s_applicationContext.getApplicationInfo().name, Context.MODE_PRIVATE);

        String map = mPrefs.getString("idIpMap", null);
        if(map != null) {
            String[] idIpPair = map.split(";");
            for (String pair : idIpPair) {
                idIPMap.put(pair.split(",")[0], pair.split(",")[1]);
            }
        }
    }

    /* service method section */

    //request from group owner
    public void requestIdIpMap() {
        if(groupOwnerAddress != null && !isGroupOwner) {
            IDIPMapRequestTask idipMapRequestTask = new IDIPMapRequestTask(groupOwnerAddress.getHostAddress());
            idipMapRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void sendIdIpMap(String senderIP) {
        IDIPMapSendTask idipMapSendTask = new IDIPMapSendTask(senderIP);
        idipMapSendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void broadcastTagSubscription(String[] subscribedTags) {
        BroadcastTagSubscription broadcastTagSubscription = new BroadcastTagSubscription();
        broadcastTagSubscription.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subscribedTags);
    }

    public void sendPublicWorkspacesForTags(HashMap<OwnedWorkspace, String[]> workspaces, String receiverIp) {
        ForeignWorkspace[] matchingWorkspaces = createForeignWorkspaceList(workspaces);
        WorkspaceSender workspaceSender = new WorkspaceSender(receiverIp);
        workspaceSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, matchingWorkspaces);
    }

    public void sendWorkspaceToClient(OwnedWorkspace workspace, String clientId) {
        String clientIP = idIPMap.get(clientId);
        //if( clientIP != null && connectedIpsVirtual.contains(clientIP)) {
        if( clientIP != null) {
            ForeignWorkspace foreignWorkspace = new ForeignWorkspace(workspace.getWorkspaceName(),
                    workspace.getOwnerId(), workspace.getQuota());
            foreignWorkspace.addFiles(workspace.getFileNames().toArray(new String[workspace.getFileNames().size()]));

            WorkspaceSender workspaceSender = new WorkspaceSender(clientIP);
            workspaceSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, foreignWorkspace);
        }
    }

    public void sendUpdatedFileListToClients(String workspaceName, String ownerId, String[] fileNames, String[] clientIds) {
        for (String clientId : clientIds) {
            String clientIP = idIPMap.get(clientId);
            if( clientIP != null) {
                SendUpdatedFileListTask sendUpdatedFileListTask = new SendUpdatedFileListTask(clientIP, workspaceName, ownerId);
                sendUpdatedFileListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileNames);
            }
        }
        //if( clientIP != null && connectedIpsVirtual.contains(clientIP)) {

    }

    public void publishTags(String[] tags) {
        TagPublisher tagPublisher = new TagPublisher();
        tagPublisher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tags);
    }

    public void revokeAccessFromClient(String workspaceName, String workspaceOwnerId, String clientId) {
        String clientIP = idIPMap.get(clientId);
        if( clientIP != null) {
            AccessRevoker accessRevoker = new AccessRevoker(clientIP);
            accessRevoker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, workspaceOwnerId);
        }
    }

    public String requestReadFileFromOwner(String workspaceName, String fileName, String ownerId) {
        String ownerIP = idIPMap.get(ownerId);

        if( ownerIP != null) {
            RequestFileTask requestFileTask = new RequestFileTask(ownerIP);
            //params : workspace name, file name, owner Id, writeMode
            requestFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, fileName, ownerId, "false");

            String key = ownerId.concat("/").concat(workspaceName).concat("/").concat(fileName);
            while (!CommunicationTask.getFileContents().containsKey(key)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String content = CommunicationTask.getFileContents().get(key);
            if(content != null) {
                System.out.println("************************************");
                System.out.println("***************** content *******************");
                System.out.println("******" + content + "***");
                System.out.println("************************************");
            }
            CommunicationTask.removeFileContentItem(key);
            return content;
        }
        return "";
    }

    public String requestWriteFileFromOwner(String workspaceName, String fileName, String ownerId) {
        String ownerIP = idIPMap.get(ownerId);

        if( ownerIP != null) {
            RequestFileTask requestFileTask = new RequestFileTask(ownerIP);
            //params : workspace name, file name, owner Id, writeMode
            requestFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, fileName, ownerId, "true");

            String key = ownerId.concat("/").concat(workspaceName).concat("/").concat(fileName);
            while (!CommunicationTask.getFileContents().containsKey(key)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String content = CommunicationTask.getFileContents().get(key);
            System.out.println("************************************");
            System.out.println("***************** content *******************");
            System.out.println("******" + content + "***");
            System.out.println("************************************");
            CommunicationTask.removeFileContentItem(key);
            return content;
        }
        return null;
    }

    public void sendFileContentToClient(String clientIP, String workspaceName, String fileName, String ownerId, String fileContent) {
        SendFileContentTask sendFileContentTask = new SendFileContentTask(clientIP);
        sendFileContentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, fileName, ownerId, fileContent);
    }

    public void saveFileInOwnerSpace(String workspaceName, String fileName, String ownerId, String content) {
        String ownerIP = idIPMap.get(ownerId);

        if( ownerIP != null) {
            SaveFileTask saveFileTask = new SaveFileTask(ownerIP);
            saveFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, fileName, ownerId, content);
        }
    }

    public void deleteForeignFile(String workspaceName, String fileName, String ownerId) {
        String ownerIP = idIPMap.get(ownerId);
        if(ownerIP != null) {
            DeleteFileTask deleteFileTask = new DeleteFileTask(ownerIP);
            deleteFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, fileName, ownerId);
        }
    }

    /* service method section end*/


    public void addIdIpMapping(String ownerId, String virtualIp) {
        System.out.println("*************adding ips to map ************");
        System.out.println(ownerId + "," + virtualIp);
        System.out.println("*************************");
        if(!idIPMap.containsKey(ownerId) || !idIPMap.get(ownerId).equals(virtualIp)) {
            idIPMap.put(ownerId, virtualIp);
            storeIdIpMap();

            //send updated list to all clients
            for (String client : idIPMap.keySet()) {
                sendIdIpMap(idIPMap.get(client));
            }
        }
    }

    private void storeIdIpMap() {
        String idIpMap = "";
        for (Map.Entry<String, String> entry : idIPMap.entrySet()) {
            String mapping = entry.getKey().concat(",").concat(entry.getValue());
            idIpMap = idIpMap.concat(mapping).concat(";");
        }
        if(idIpMap.endsWith(";"))
            idIpMap = idIpMap.substring(0, idIpMap.length()-1);


        SharedPreferences mPrefs = AirDeskApp.s_applicationContext.getSharedPreferences(
                AirDeskApp.s_applicationContext.getApplicationInfo().name, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("idIpMap", idIpMap);
        System.out.println("@@@@@@@@@@@@@@ storing id ip map @@@@@@@@@@@@@@");
        System.out.println(idIpMap);
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        ed.commit();

    }

    public HashMap<String, String> getIdIPMap() {
        return idIPMap;
    }

    public void updateIdIPMap(LinkedTreeMap receivedMap) {
        idIPMap.putAll(receivedMap);

        System.out.println("*************adding UPDATED ips to map ************");
        for (Object key : receivedMap.keySet()) {
            String id = (String) key;
            String ip = (String) receivedMap.get(key);
            System.out.println(id + " , " + ip);

            if(!idIPMap.containsKey(id) || !idIPMap.get(id).equals(ip)) {
                idIPMap.put(id, ip);
                storeIdIpMap();
                break;
            }
        }
        System.out.println("*************************");

    }

    private ForeignWorkspace[] createForeignWorkspaceList(HashMap<OwnedWorkspace, String[]> ownedWorkspaceMap) {
        ArrayList<ForeignWorkspace> foreignWorkspaces = new ArrayList<ForeignWorkspace>();
        for (OwnedWorkspace ownedWorkspace : ownedWorkspaceMap.keySet()) {
            ForeignWorkspace workspace = new ForeignWorkspace(ownedWorkspace.getWorkspaceName(), ownedWorkspace.getOwnerId(), ownedWorkspace.getQuota());
            workspace.addFiles(ownedWorkspace.getFileNames().toArray(new String[ownedWorkspace.getFileNames().size()]));
            workspace.setMatchingTags(ownedWorkspaceMap.get(ownedWorkspace));
            foreignWorkspaces.add(workspace);
        }
        return foreignWorkspaces.toArray(new ForeignWorkspace[foreignWorkspaces.size()]);
    }

    public void setGroupOwnerDetails(WifiP2pInfo info) {
        groupOwnerAddress = info.groupOwnerAddress;
        isGroupOwner = info.isGroupOwner;
    }


    /** message sending tasks **/

    private class IDIPMapRequestTask extends AsyncTask<Void, Void, Void> {

        private  String receiverIp;
        public IDIPMapRequestTask(String receiverIp) {
            this.receiverIp = receiverIp;
        }

        @Override
        protected Void doInBackground(Void... params) {

            System.out.println("___________ going to send IDIPMapRequest ______________");
            AirDeskMessage msg = createMessage(Constants.ID_IP_MAP_REQUEST_MSG);

            if(msg == null) {
                logger.log(Level.SEVERE, "Revoke access message not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ IDIPMapRequest wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            return msg;
        }
    }

    private class IDIPMapSendTask extends AsyncTask<Void, Void, Void> {

        private  String receiverIp;
        public IDIPMapSendTask(String receiverIp) {
            this.receiverIp = receiverIp;
        }

        @Override
        protected Void doInBackground(Void... params) {

            System.out.println("___________ going to send IDIPMapSendTask ______________");
            AirDeskMessage msg = createMessage(Constants.ID_IP_MAP_REPLY_MSG, idIPMap);

            if(msg == null) {
                logger.log(Level.SEVERE, "IDIPMapSendTask msg not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ IDIPMapSendTask wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, HashMap<String, String> idIPMap) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.ID_IP_MAP, idIPMap);
            return msg;
        }
    }

    private class BroadcastTagSubscription extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("___________ going to send BroadcastTagSubscription msg______________");
            peerLock.lock();
            String[] tags = params;
            AirDeskMessage msg = createMessage(Constants.SUBSCRIBE_TAGS_MSG, tags, new UserManager().getOwner().getUserId());

            if(msg == null) {
                logger.log(Level.SEVERE, "BroadcastTagSubscription message not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);


                for (Map.Entry<String, String> entry : idIPMap.entrySet()) {
                    try {
                    if(!entry.getKey().equals(userManager.getOwner().getUserId())) {
                        System.out.println("Sending tags to " + entry.getKey() + " , " + entry.getValue());
                        Socket socket = new Socket(entry.getValue(), Constants.port);
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(msgJson.getBytes());
                        outputStream.flush();
                        outputStream.close();
                        System.out.println("___________________BroadcastTagSubscription msg wrote to o/p stream ___________________");
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            peerLock.unlock();
            return null;
        }

        private AirDeskMessage createMessage(String type, String[] tags, String userId) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.TAGS, tags);
            msg.addInput(Constants.CLIENT_ID, userId);
            return msg;
        }
    }

    private class WorkspaceSender extends AsyncTask<ForeignWorkspace, Void, Void> {

        private  String receiverIp;
        public WorkspaceSender(String receiverIp) {
            this.receiverIp = receiverIp;
        }
        @Override
        protected Void doInBackground(ForeignWorkspace... params) {
            System.out.println("________________ going to send workspaces ADD_TO_FOREIGN_WORKSPACE_MSG __________");

            ForeignWorkspace[] foreignWorkspaces = new ForeignWorkspace[params.length];
            for (int i = 0; i < params.length; i++) {
                foreignWorkspaces[i] = params[i];
            }
            AirDeskMessage msg = createMessage(Constants.ADD_TO_FOREIGN_WORKSPACE_MSG, foreignWorkspaces);
            if(msg == null) {
                logger.log(Level.SEVERE, "ADD_TO_FOREIGN_WORKSPACE_MSG message not created. Returning.");
                return null;
            }

            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);
            Socket socket = null;
            try {
                socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("ADD_TO_FOREIGN_WORKSPACE_MSG msg wrote to o/p stream ___________________");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, ForeignWorkspace[] foreignWorkspaces) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.WORKSPACES, foreignWorkspaces);
            return msg;
        }
    }

    private class TagPublisher extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("___________ going to send PUBLISH_TAGS_MSG______________");
            peerLock.lock();
            String[] tags = params;
            AirDeskMessage msg = createMessage(Constants.PUBLISH_TAGS_MSG, tags);
            if(msg == null) {
                logger.log(Level.SEVERE, "PUBLISH_TAGS_MSG message not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            for (Map.Entry<String, String> entry : idIPMap.entrySet()) {
                try {
                    if (!entry.getKey().equals(userManager.getOwner().getUserId())) {
                        Socket socket = new Socket(entry.getValue(), Constants.port);
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(msgJson.getBytes());
                        outputStream.flush();
                        outputStream.close();
                        System.out.println("___________________ PUBLISH_TAGS_MSG wrote to o/p stream ___________________");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            peerLock.unlock();
            return null;
        }

        private AirDeskMessage createMessage(String type, String[] tags) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.TAGS, tags);
            return msg;
        }
    }

    private class AccessRevoker extends AsyncTask<String, Void, Void> {

        private  String receiverIp;
        public AccessRevoker(String receiverIp) {
            this.receiverIp = receiverIp;
        }

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("___________ going to send REVOKE_ACCESS_MSG______________");
            String workspaceName = params[0];
            String workspaceOwnerId = params[1];
            AirDeskMessage msg = createMessage(Constants.REVOKE_ACCESS_MSG, workspaceName, workspaceOwnerId);

            if(msg == null) {
                logger.log(Level.SEVERE, "Revoke access message not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ REVOKE_ACCESS_MSG wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String workspaceName, String workspaceOwnerId) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.OWNER_ID, workspaceOwnerId);
            return msg;
        }
    }

    private class RequestFileTask extends AsyncTask<String, Void, String> {

        private  String receiverIp;
        public RequestFileTask(String receiverIp) {
            this.receiverIp = receiverIp;
        }

        @Override
        protected String doInBackground(String... params) {

            //params : workspace name, file name, owner Id, writeMode
            System.out.println("___________ going to send RequestFileTask ______________");
            AirDeskMessage msg = createMessage(Constants.REQUEST_FILE_MSG, params[0], params[1],
                    params[2], Boolean.valueOf(params[3]));

            if(msg == null) {
                logger.log(Level.SEVERE, "RequestFileTask msg not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ RequestFileTask wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String workspaceName, String fileName, String ownerId, boolean writeMode) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.FILENAME, fileName);
            msg.addInput(Constants.OWNER_ID, ownerId);
            msg.addInput(Constants.WRITE_MODE, writeMode);
            return msg;
        }
    }

    private class SendFileContentTask extends AsyncTask<String, Void, Void> {

        private  String receiverIp;
        public SendFileContentTask(String receiverIp) {
            this.receiverIp = receiverIp;
        }

        @Override
        protected Void doInBackground(String... params) {

            //params will be workspaceName, fileName, ownerId, fileContent
            System.out.println("___________ going to send SendFileContentTask ______________");
            AirDeskMessage msg = createMessage(Constants.FILE_CONTENT_RESULT_MSG, params[0], params[1], params[2], params[3]);

            if(msg == null) {
                logger.log(Level.SEVERE, "SendFileContentTask msg not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ SendFileContentTask wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String workspaceName, String fileName, String ownerId, String content) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.FILE_CONTENT, content);
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.FILENAME, fileName);
            msg.addInput(Constants.OWNER_ID, ownerId);
            return msg;
        }
    }

    private class SaveFileTask extends AsyncTask<String, Void, String> {

        private  String receiverIp;
        public SaveFileTask(String receiverIp) {
            this.receiverIp = receiverIp;
        }

        @Override
        protected String doInBackground(String... params) {

            //params : workspace name, file name, owner Id, content
            System.out.println("___________ going to send SaveFileTask ______________");
            AirDeskMessage msg = createMessage(Constants.SAVE_FILE_MSG, params[0], params[1],
                    params[2], params[3]);

            if(msg == null) {
                logger.log(Level.SEVERE, "SaveFileTask msg not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ SaveFileTask wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String workspaceName, String fileName, String ownerId, String content) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.FILENAME, fileName);
            msg.addInput(Constants.OWNER_ID, ownerId);
            msg.addInput(Constants.FILE_CONTENT, content);
            return msg;
        }
    }

    private class DeleteFileTask extends AsyncTask<String, Void, String> {

        private  String receiverIp;
        public DeleteFileTask(String receiverIp) {
            this.receiverIp = receiverIp;
        }

        @Override
        protected String doInBackground(String... params) {

            //params : workspace name, file name, owner Id
            System.out.println("___________ going to send DeleteFileTask ______________");
            AirDeskMessage msg = createMessage(Constants.DELETE_FILE_MSG, params[0], params[1],
                    params[2]);

            if(msg == null) {
                logger.log(Level.SEVERE, "DeleteFileTask msg not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ DeleteFileTask wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String workspaceName, String fileName, String ownerId) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.FILENAME, fileName);
            msg.addInput(Constants.OWNER_ID, ownerId);
            return msg;
        }
    }

    private class SendUpdatedFileListTask extends AsyncTask<String, Void, Void> {

        private  String receiverIp;
        private String workspaceName;
        private String ownerId;

        public SendUpdatedFileListTask(String receiverIp, String workspaceName, String ownerId) {
            this.receiverIp = receiverIp;
            this.workspaceName = workspaceName;
            this.ownerId = ownerId;
        }

        @Override
        protected Void doInBackground(String... params) {

            //params will be fileNames
            System.out.println("___________ going to send SendUpdatedFileListTask ______________");
            AirDeskMessage msg = createMessage(Constants.UPDATED_FILE_LIST_MSG, workspaceName, ownerId, params);

            if(msg == null) {
                logger.log(Level.SEVERE, "SendUpdatedFileListTask msg not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);
            System.out.println("------------ msg : " + msgJson);

            try {
                Socket socket = new Socket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
                System.out.println("___________________ SendUpdatedFileListTask wrote to o/p stream ___________________");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String workspaceName, String ownerId, String[] fileNames) {
            AirDeskMessage msg = new AirDeskMessage(type, userManager.getOwner().getUserId());
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.OWNER_ID, ownerId);
            msg.addInput(Constants.FILE_NAMES, fileNames);
            return msg;
        }
    }

    /** message sending tasks end **/

}
