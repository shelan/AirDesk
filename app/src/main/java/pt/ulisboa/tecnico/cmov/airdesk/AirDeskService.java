package pt.ulisboa.tecnico.cmov.airdesk;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
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
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.AirDeskMessage;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pDevice;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocket;

/**
 * Created by ashansa on 5/3/15.
 */
public class AirDeskService {

    private static AirDeskService instance = null;
    private Lock peerLock = new ReentrantLock();
    Set<String> connectedIpsVirtual = new HashSet<>();

    // will have the virtualIP and the owner ID of that device
    private HashMap<String,String> idIPMap = new HashMap<String,String>();
    private SimWifiP2pDevice myDevice = null;
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

    public void updateConnectedDevices(Collection<SimWifiP2pDevice> nearbyDevices) {
        peerLock.lock();
        if(nearbyDevices.size() > 0) {
            for (SimWifiP2pDevice device : nearbyDevices) {
                connectedIpsVirtual.add(device.getVirtIp());
            }
        }
        peerLock.unlock();
    }

    public void setMyDevice(SimWifiP2pDevice device) {
        myDevice = device;
    }

    public SimWifiP2pDevice getMyDevice() {
        return myDevice;
    }


    /** message sending tasks **/

    private class BroadcastTagSubscription extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("___________ going to send msg______________");
            peerLock.lock();
            String[] tags = params;
            AirDeskMessage msg = createMessage(Constants.SUBSCRIBE_TAGS_MSG, tags);

            if(msg == null) {
                logger.log(Level.SEVERE, "BroadcastTagSubscription message not created. Returning.");
                return null;
            }
            String msgJson = gson.toJson(msg);

            try {
                for (String virtualIp : connectedIpsVirtual) {
                    /*Socket socket = new Socket();
                    System.out.println("___________________ socket created ___________________");
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(virtualIp, Constants.AIRDESK_SOCKET_PORT)), 5000);*/
                    SimWifiP2pSocket socket = new SimWifiP2pSocket(virtualIp, Constants.port);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(msgJson.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("___________________ msg wrote to o/p stream ___________________");
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

            AirDeskMessage msg = new AirDeskMessage(type, myDevice.getVirtIp());
            msg.addInput(Constants.TAGS, tags);
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
            System.out.println("________________ going to send pub workspaces __________");
            System.out.println("____________________________________");
            System.out.println("____________________________________");

            ////TODO: get workspaces and set in msg
            ForeignWorkspace[] foreignWorkspaces = new ForeignWorkspace[params.length];
            for (int i = 0; i < params.length; i++) {
                foreignWorkspaces[i] = params[i];
            }
            AirDeskMessage msg = createMessage(Constants.ADD_TO_FOREIGN_WORKSPACE_MSG, foreignWorkspaces);
            String msgJson = gson.toJson(msg);
            SimWifiP2pSocket socket = null;
            try {
                socket = new SimWifiP2pSocket(receiverIp, Constants.port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msgJson.getBytes());
                outputStream.flush();
                outputStream.close();
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
            AirDeskMessage msg = new AirDeskMessage(type, myDevice.getVirtIp());
            msg.addInput(Constants.WORKSPACES, foreignWorkspaces);
            return msg;
        }
    }

    private class TagPublisher extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("___________ going to send msg______________");
            peerLock.lock();
            String[] tags = params;
            AirDeskMessage msg = createMessage(Constants.PUBLISH_TAGS_MSG, tags);
            String msgJson = gson.toJson(msg);

            try {
                for (String virtualIp : connectedIpsVirtual) {
                    SimWifiP2pSocket socket = new SimWifiP2pSocket(virtualIp, Constants.port);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(msgJson.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("___________________ msg wrote to o/p stream ___________________");
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
            AirDeskMessage msg = new AirDeskMessage(type, myDevice.getVirtIp());
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

            System.out.println("___________ going to send msg______________");
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
                    SimWifiP2pSocket socket = new SimWifiP2pSocket(receiverIp, Constants.port);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(msgJson.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("___________________ msg wrote to o/p stream ___________________");
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

            AirDeskMessage msg = new AirDeskMessage(type, myDevice.getVirtIp());
            msg.addInput(Constants.WORKSPACE_NAME, workspaceName);
            msg.addInput(Constants.OWNER_ID, workspaceOwnerId);
            return msg;
        }
    }
    /** message sending tasks end **/

}
