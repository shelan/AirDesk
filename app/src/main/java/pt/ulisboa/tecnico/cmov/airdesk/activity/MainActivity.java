package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.AirDeskService;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForeignWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.MyWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.manager.HoardingManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.CommunicationTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.WiFiDirectBroadcastReceiver;

public class MainActivity extends ActionBarActivity {

    MyWorkspaceListFragment myWorkspacesFragment;
    ForeignWorkspaceListFragment foreignWorkspacesFragment;
    static CommunicationManager communicationManager;

    //for wifi_direct
    private BroadcastReceiver receiver = null;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_refresh);

        UserManager userManager = new UserManager();

        if (userManager.getOwner() == null) {
            Intent intent = new Intent(this, CreateUserActivity.class);
            startActivity(intent);
        }
        if (savedInstanceState == null) {

            myWorkspacesFragment = new MyWorkspaceListFragment();
            foreignWorkspacesFragment = new ForeignWorkspaceListFragment();
           
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, myWorkspacesFragment, "fragment_1")
                    .add(R.id.container, foreignWorkspacesFragment, "fragment_2")
                    .commit();

        }
        getSupportActionBar().setElevation(0f);

        StorageManager.restoreAccessMap();

        if(communicationManager == null) {
            communicationManager = new CommunicationManager();
            communicationManager.init();
        }

        ////new HoardingManager().scheduleCleaningTask();

    }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        //TODO move these tests and write proper tests in android test package


        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            /*if (id == R.id.action_settings) {
                return true;
            } else if (id == R.id.action_populate) {
                try {
                    new PopulateData().populateOwnedWorkspaces();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/

            return super.onOptionsItemSelected(item);
        }

   /* @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, communicationManager);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        StorageManager.persistAccessMap();
    }

    public class CommunicationManager extends ListFragment implements WifiP2pManager.PeerListListener,WifiP2pManager.ConnectionInfoListener {

        private List<WifiP2pDevice> peerList = new ArrayList<WifiP2pDevice>();
        AirDeskService airDeskService;

        public CommunicationManager() {
            airDeskService = AirDeskService.getInstance();
            init();
        }

        public void init() {

            intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
            manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            channel = manager.initialize(MainActivity.this, getMainLooper(), null);
            receiver = new WiFiDirectBroadcastReceiver(manager, channel, communicationManager);
            registerReceiver(receiver, intentFilter);

            CommunicationTask.IncomingCommTask incomingCommTask = new CommunicationTask(foreignWorkspacesFragment).getIncomingCommTask();
            incomingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

           /* if(!mBound) {
            Intent intent = new Intent(MainActivity.this, SimWifiP2pService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }*/
        }

        @Override
        public void onPeersAvailable(WifiP2pDeviceList availablePeers) {
            peerList.clear();
            peerList.addAll(availablePeers.getDeviceList());

            airDeskService.updateConnectedDevices(peerList);

            if (peerList.size() == 0) {
                Toast.makeText(getBaseContext(), "No devices found",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            if (info.groupFormed) {
                airDeskService.setGroupOwnerDetails(info);
                CommunicationTask.OutgoingCommTask outgoingCommTask = new CommunicationTask(foreignWorkspacesFragment).getOutgoingCommTask();
                outgoingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        info.groupOwnerAddress.getHostAddress(), new UserManager().getOwner().getUserId());
            }
        }

       /* public void requestPeers() {
            if(mBound && mManager != null)
                mManager.requestPeers(mChannel, (SimWifiP2pManager.PeerListListener) CommunicationManager.this);
        }

        public void requestGroupInfo() {
            if(mBound && mManager != null)
                mManager.requestGroupInfo(mChannel, (SimWifiP2pManager.GroupInfoListener) CommunicationManager.this);
        }*/

        /*private ServiceConnection mConnection = new ServiceConnection() {
            // callbacks for service binding, passed to bindService()

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                System.out.println("=========> onServiceConnected ======");
                mService = new Messenger(service);
                mManager = new SimWifiP2pManager(mService);
                mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
                mBound = true;

                mManager.requestPeers(mChannel, (SimWifiP2pManager.PeerListListener) CommunicationManager.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
                mManager = null;
                mChannel = null;
                mBound = false;
                unbindService(mConnection);
            }
        };*/

      /*  @Override
        public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
            // compile list of network members
            ArrayList<SimWifiP2pDevice> memberList = new ArrayList<SimWifiP2pDevice>();
            String members = "";
            for (String deviceName : groupInfo.getDevicesInNetwork()) {
                SimWifiP2pDevice device = devices.getByName(deviceName);
                memberList.add(device);
                members = members.concat(device.deviceName + " ");
                CommunicationTask.OutgoingCommTask outgoingCommTask = new CommunicationTask(foreignWorkspacesFragment).getOutgoingCommTask();
                outgoingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device.getVirtIp(),
                        new UserManager().getOwner().getUserId(),devices.getByName(groupInfo.getDeviceName()).getVirtIp() );
            }
            TextView onlineUsers = (TextView) findViewById(R.id.usersOnline);
            onlineUsers.setText(members);
            peerList.clear();
            peerList.addAll(memberList);

            if(airDeskService.getMyDevice() == null) {
                airDeskService.setMyDevice(devices.getByName(groupInfo.getDeviceName()));
            } else if(!airDeskService.getMyDevice().deviceName.equals(groupInfo.getDeviceName())) {
                airDeskService.setMyDevice(devices.getByName(groupInfo.getDeviceName()));
            }
            airDeskService.updateConnectedDevices(memberList);
        }
*/
    }

}

