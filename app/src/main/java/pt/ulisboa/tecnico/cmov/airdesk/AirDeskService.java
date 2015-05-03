package pt.ulisboa.tecnico.cmov.airdesk;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.AirDeskMessage;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pDevice;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocket;

/**
 * Created by ashansa on 5/3/15.
 */
public class AirDeskService {

    private static AirDeskService instance = null;
    private Vector<SimWifiP2pSocket> connectedPeers = new Vector<SimWifiP2pSocket>();
    private Lock peerLock = new ReentrantLock();
    ArrayList<String> virtualIPList = new ArrayList<String>();
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


    public void updateNearbyDevices(Collection<SimWifiP2pDevice> nearbyDevices) {
        peerLock.lock();
        connectedPeers.clear();

        if(nearbyDevices.size() > 0) {
            for (SimWifiP2pDevice device : nearbyDevices) {
                virtualIPList.add(device.getVirtIp());
                //connectedPeers.add(new SimWifiP2pSocket(device.getVirtIp(), Constants.port));
            }
//            SocketCreateForPeersTask socketCreateForPeersTask = new SocketCreateForPeersTask();
//            socketCreateForPeersTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, virtualIPList.toArray(new String[virtualIPList.size()]));
        }

        peerLock.unlock();

    }

    private class BroadcastTagSubscription extends AsyncTask<String, Void, Void> {

        private AirDeskMessage createMessage(String type, String[] tags) {
            AirDeskMessage msg = new AirDeskMessage(type);
            msg.addInput(Constants.TAGS, tags);
            return msg;
        }

        @Override
        protected Void doInBackground(String... params) {

            peerLock.lock();
            String[] tags = params;
            String tagString = "aa,bb";
            AirDeskMessage msg = createMessage(Constants.SUBSCRIBE_TAGS, tags);
            String msgJson = gson.toJson(msg);
            for (String virtualIp : virtualIPList) {
                try {
                    SimWifiP2pSocket peer = new SimWifiP2pSocket(virtualIp, Constants.port);
                    OutputStream outputStream = peer.getOutputStream();
                    outputStream.write(msgJson.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    /*ObjectOutputStream oos = new AppendingObjectOutputStream(peer.getOutputStream());
                    oos.writeObject(msg);
                    oos.flush();
                    oos.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            /*//TODO... create tag string from params
            String tagString = "t1,t2";
            for (SimWifiP2pSocket peer : connectedPeers) {
                try {
                    System.out.println("........ writing.................");
                    System.out.println("........ writing.................");
                    System.out.println("........ writing.................");
                    peer.getOutputStream().write(tagString.getBytes());
                    peer.getOutputStream().flush();
                    peer.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
            peerLock.unlock();
            return null;
        }
    }

    private class SocketCreateForPeersTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            for (String virtualIp : params) {
                try {
                    connectedPeers.add(new SimWifiP2pSocket(virtualIp, Constants.port));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

}
