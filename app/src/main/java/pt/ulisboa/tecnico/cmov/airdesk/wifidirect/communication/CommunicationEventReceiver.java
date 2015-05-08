package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pInfo;

/**
 * Created by ashansa on 4/27/15.
 */
public class CommunicationEventReceiver extends BroadcastReceiver{

    //private CommunicationService mService;
    private Context context;
    private MainActivity.CommunicationManager communicationManager;

    public CommunicationEventReceiver(Context context, MainActivity.CommunicationManager communicationManager) {
        this.context = context;
        this.communicationManager = communicationManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        communicationManager.requestPeers();
        communicationManager.requestGroupInfo();

        if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            WifiP2pDevice device2 = intent.getParcelableExtra(SimWifiP2pBroadcast.EXTRA_DEVICE_INFO);
            System.out.println("...........................................");
            System.out.println("...........................................");
            System.out.println("..........................................." + device);
            //System.out.println("========= my device========= " + device.deviceAddress + ", " + device.deviceName);
            System.out.println("..........................................." + device2);
            System.out.println("...........................................");
            System.out.println("...........................................");
        }
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the WDSim service changes state:
            // - creating the service generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFi Direct enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "WiFi Direct disabled",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

            Toast.makeText(context, "Peer list changed",
                    Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(context, "Network membership changed",
                    Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(context, "Group ownership changed",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
