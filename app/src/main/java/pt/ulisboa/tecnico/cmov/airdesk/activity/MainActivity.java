package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.AirDeskService;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForeignWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.MyWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.manager.HoardingManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.CommunicationEventReceiver;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.CommunicationTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pDevice;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pDeviceList;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pInfo;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.service.SimWifiP2pService;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocketManager;

public class MainActivity extends ActionBarActivity {

    MyWorkspaceListFragment myWorkspacesFragment;
    ForeignWorkspaceListFragment foreignWorkspacesFragment;
    CommunicationManager communicationManager;

    public MainActivity() {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_refresh);

        UserManager manager = new UserManager();

        if (manager.getOwner() == null) {
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
        //TODO move these tests and write proper tests in android test package

        new HoardingManager().scheduleCleaningTask();

        if(communicationManager == null)
            communicationManager = new CommunicationManager();

    }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }


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


        @Override
        protected void onDestroy () {
            super.onDestroy();
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
            unregisterReceiver(communicationManager.mReceiver);
        }

    public class CommunicationManager implements
            SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {
        //for wifi_direct
        CommunicationEventReceiver mReceiver = null;

        private SimWifiP2pManager mManager = null;
        private Messenger mService = null;
        private boolean mBound = false;
        private SimWifiP2pManager.Channel mChannel = null;
        private AirDeskService airDeskService;

        private ArrayList peerList = new ArrayList();

        public CommunicationManager() {
            init();
        }

        public void init() {
            SimWifiP2pSocketManager.Init(getApplicationContext());
            airDeskService = AirDeskService.getInstance();

            IntentFilter filter1 = new IntentFilter();
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
            mReceiver = new CommunicationEventReceiver(getApplicationContext(), this);
            registerReceiver(mReceiver, filter1);

            Intent intent = new Intent(MainActivity.this, SimWifiP2pService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            CommunicationTask.IncomingCommTask incomingCommTask = new CommunicationTask(foreignWorkspacesFragment).getIncomingCommTask();
            incomingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //new AirDeskSocketListenerService();
        }

        public void requestPeers() {
            if(mBound && mManager != null)
                mManager.requestPeers(mChannel, (SimWifiP2pManager.PeerListListener) CommunicationManager.this);
        }

        public void requestGroupInfo() {
            if(mBound && mManager != null)
                mManager.requestGroupInfo(mChannel, (SimWifiP2pManager.GroupInfoListener) CommunicationManager.this);
        }

        private ServiceConnection mConnection = new ServiceConnection() {
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
        };

        @Override
        public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
            // compile list of network members
            ArrayList<SimWifiP2pDevice> memberList = new ArrayList<SimWifiP2pDevice>();
            String members = "";
            for (String deviceName : groupInfo.getDevicesInNetwork()) {
                SimWifiP2pDevice device = devices.getByName(deviceName);
                memberList.add(device);
                members = members.concat(device.deviceName + " ");
                CommunicationTask.OutgoingCommTask outgoingCommTask = new CommunicationTask(foreignWorkspacesFragment).getOutgoingCommTask();
                outgoingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device.getVirtIp());
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

        @Override
        public void onPeersAvailable(SimWifiP2pDeviceList peers) {

        }
    }

    }
