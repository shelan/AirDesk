package pt.ulisboa.tecnico.cmov.airdesk;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.ulisboa.tecnico.cmov.airdesk.entity.ForeignWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.entity.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.AirDeskMessage;

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
        }
        return instance;
    }

    /* service method section */

    //request from group owner
    public void requestIdIpMap() {
        if(groupOwnerAddress != null) {
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

    public void publishTags(String[] tags) {
        TagPublisher tagPublisher = new TagPublisher();
        tagPublisher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tags);
    }

    public void revokeAccessFromClient(String workspaceName, String workspaceOwnerId, String clientId) {
        String clientIP = idIPMap.get(clientId);
        //if( clientIP != null && connectedIpsVirtual.contains(clientIP)) {
        if( clientIP != null) {
            AccessRevoker accessRevoker = new AccessRevoker(clientIP);
            accessRevoker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, workspaceOwnerId);
        }
    }

    /* service method section end*/


    public void addIdIpMapping(String ownerId, String virtualIp) {
        idIPMap.put(ownerId, virtualIp);
    }

    public HashMap<String, String> getIdIPMap() {
        return idIPMap;
    }

    public void updateIdIPMap(LinkedTreeMap receivedMap) {
        idIPMap.putAll(receivedMap);
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

    /*public void updateConnectedDevices(Collection<WifiP2pDevice> nearbyDevices) {
        peerLock.lock();
        if(nearbyDevices.size() > 0) {
            for (WifiP2pDevice device : nearbyDevices) {
                connectedIpsVirtual.add(device.deviceAddress);
            }
        }
        peerLock.unlock();
    }*/

    /*public void setMyDevice(WifiP2pDevice device) {
        myDevice = device;
    }

    public WifiP2pDevice getMyDevice() {
        return myDevice;
    }*/

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

            try {
                for (Map.Entry<String, String> entry : idIPMap.entrySet()) {
                    if(!entry.getKey().equals(userManager.getOwner().getUserId())) {
                        Socket socket = new Socket(entry.getValue(), Constants.port);
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(msgJson.getBytes());
                        outputStream.flush();
                        outputStream.close();
                        System.out.println("___________________BroadcastTagSubscription msg wrote to o/p stream ___________________");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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

            try {
                for (Map.Entry<String, String> entry : idIPMap.entrySet()) {
                    if(!entry.getKey().equals(userManager.getOwner().getUserId())) {
                        Socket socket = new Socket(entry.getValue(), Constants.port);
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(msgJson.getBytes());
                        outputStream.flush();
                        outputStream.close();
                        System.out.println("___________________ PUBLISH_TAGS_MSG wrote to o/p stream ___________________");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
    /** message sending tasks end **/

}
