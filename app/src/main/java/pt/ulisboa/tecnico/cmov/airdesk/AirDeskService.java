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
    private SimWifiP2pDevice myDevice = null;
    Gson gson = new Gson();

    private AirDeskService() {}

    public static AirDeskService getInstance() {
        if(instance == null) {
            instance = new AirDeskService();
        }
        return instance;
    }

    public void broadcastTagSubscription(String[] subscribedTags) {
        BroadcastTagSubscription broadcastTagSubscription = new BroadcastTagSubscription();
        broadcastTagSubscription.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subscribedTags);
    }

    public void sendPublicWorkspacesForTags(HashMap<OwnedWorkspace, String[]> workspaces, String receiverIp) {
        ForeignWorkspace[] matchingWorkspaces = createForeignWorkspaceList(workspaces);
        PublicWorkspaceSender publicWorkspaceSender = new PublicWorkspaceSender(receiverIp);
        publicWorkspaceSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, matchingWorkspaces);
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

    private class BroadcastTagSubscription extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("___________ going to send msg______________");
            peerLock.lock();
            String[] tags = params;
            AirDeskMessage msg = createMessage(Constants.SUBSCRIBE_TAGS_MSG, tags);
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
            if(myDevice == null)
                throw new NullPointerException("my device not set");

            AirDeskMessage msg = new AirDeskMessage(type, myDevice.getVirtIp());
            msg.addInput(Constants.TAGS, tags);
            return msg;
        }
    }

    private class PublicWorkspaceSender extends AsyncTask<ForeignWorkspace, Void, Void> {

        private  String receiverIp;
        public PublicWorkspaceSender(String receiverIp) {
            this.receiverIp = receiverIp;
        }
        @Override
        protected Void doInBackground(ForeignWorkspace... params) {
            System.out.println("________________ going to send pub workspaces __________");
            System.out.println("____________________________________");
            System.out.println("____________________________________");

            ////TODO: get workspaces and set in msg
            ForeignWorkspace[] matchingWorkspaces = new ForeignWorkspace[params.length];
            for (int i = 0; i < params.length; i++) {
                matchingWorkspaces[i] = params[i];
            }
            AirDeskMessage msg = createMessage(Constants.PUBLIC_WORKSPACES_FOR_TAGS_MSG, matchingWorkspaces);
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

        private AirDeskMessage createMessage(String type, ForeignWorkspace[] matchingWorkspaces) {
            if(myDevice == null)
                throw new NullPointerException("my device not set");

            AirDeskMessage msg = new AirDeskMessage(type, myDevice.getVirtIp());
            msg.addInput(Constants.MATCHING_WORKSPACES_FOR_TAGS, matchingWorkspaces);
            return msg;
        }
    }

}
