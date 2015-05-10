package pt.ulisboa.tecnico.cmov.airdesk;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
    Set<String> connectedIpsVirtual = new HashSet<>();

    // will have the virtualIP and the owner ID of that device
    private HashMap<String,String> idIPMap = new HashMap<String,String>();
    private HashMap<String,String> macIpMap = new HashMap<String,String>();

    private WifiP2pDevice myDevice = null;
    boolean isGroupOwner;
    InetAddress groupOwnerAddress;
    Gson gson = new Gson();

    private static Logger logger = Logger.getLogger(AirDeskService.class.getName());

    private AirDeskService() {}

    public static AirDeskService getInstance() {
        if(instance == null) {
            instance = new AirDeskService();
        }
        return instance;
    }

    /* service method section */

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
        if( clientIP != null && connectedIpsVirtual.contains(clientIP)) {
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
        if( clientIP != null && connectedIpsVirtual.contains(clientIP)) {
            AccessRevoker accessRevoker = new AccessRevoker(clientIP);
            accessRevoker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workspaceName, workspaceOwnerId);
        }
    }

    /* service method section end*/


    public void addIdIpMapping(String ownerId, String virtualIp) {
        idIPMap.put(ownerId, virtualIp);
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

    public void updateConnectedDevices(Collection<WifiP2pDevice> nearbyDevices) {
        peerLock.lock();
        if(nearbyDevices.size() > 0) {
            for (WifiP2pDevice device : nearbyDevices) {
                connectedIpsVirtual.add(device.deviceAddress);
            }
        }
        peerLock.unlock();
    }

    public void setMyDevice(WifiP2pDevice device) {
        myDevice = device;
    }

    public WifiP2pDevice getMyDevice() {
        return myDevice;
    }

    public void setGroupOwnerDetails(WifiP2pInfo info) {
        groupOwnerAddress = info.groupOwnerAddress;
        isGroupOwner = info.isGroupOwner;
    }

    private void contactGroupOwner() {

    }


    /** message sending tasks **/

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

            try {
                for (String virtualIp : connectedIpsVirtual) {
                    Socket socket = new Socket(virtualIp, Constants.port);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(msgJson.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("___________________BroadcastTagSubscription msg wrote to o/p stream ___________________");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String[] tags, String userId) {
            if(myDevice == null) {
                logger.log(Level.SEVERE, "my device not set");
                return null;
            }

            AirDeskMessage msg = new AirDeskMessage(type, myDevice.deviceAddress);
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
            System.out.println("________________ going to send workspaces __________");

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
            if(myDevice == null) {
                logger.log(Level.SEVERE, "my device not set");
                return null;
            }
            AirDeskMessage msg = new AirDeskMessage(type, myDevice.deviceAddress);
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

            try {
                for (String virtualIp : connectedIpsVirtual) {
                    Socket socket = new Socket(virtualIp, Constants.port);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(msgJson.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("___________________ PUBLISH_TAGS_MSG wrote to o/p stream ___________________");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private AirDeskMessage createMessage(String type, String[] tags) {
            if(myDevice == null) {
                logger.log(Level.SEVERE, "my device not set");
                return null;
            }
            AirDeskMessage msg = new AirDeskMessage(type, myDevice.deviceAddress);
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
            peerLock.lock();
            String workspaceName = params[0];
            String workspaceOwnerId = params[1];
            AirDeskMessage msg = createMessage(Constants.REVOKE_ACCESS_MSG, workspaceName, workspaceOwnerId);

            if(msg == null) {
                logger.log(Level.SEVERE, "Revoke access message not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);

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
            if(myDevice == null) {
                logger.log(Level.SEVERE, "my device not set");
                return null;
            }

            AirDeskMessage msg = new AirDeskMessage(type, myDevice.deviceAddress);
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.OWNER_ID, workspaceOwnerId);
            return msg;
        }
    }
    /** message sending tasks end **/

}
