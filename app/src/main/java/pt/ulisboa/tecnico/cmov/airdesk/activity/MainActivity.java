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
import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.airdesk.AirDeskReceiver;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForiegnWorkspaceListFragment;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.MyWorkspaceListFragment;
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
    ForiegnWorkspaceListFragment foreignWorkspacesFragment;

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
                foreignWorkspacesFragment = new ForiegnWorkspaceListFragment();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, myWorkspacesFragment, "fragment_1")
                        .add(R.id.container, foreignWorkspacesFragment, "fragment_2")
                        .commit();

            }
            getSupportActionBar().setElevation(0f);
            //TODO move these tests and write proper tests in android test package

            CommunicationManager communicationManager = new CommunicationManager();
            communicationManager.init();
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
        }

    public class CommunicationManager implements
            SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

        //for wifi_direct
        private SimWifiP2pManager mManager = null;
        private Messenger mService = null;
        private CommunicationEventReceiver mReceiver = null;
        private boolean mBound = false;
        private SimWifiP2pManager.Channel mChannel = null;

        private ArrayList peerList = new ArrayList();

        public void init() {
            SimWifiP2pSocketManager.Init(getApplicationContext());

            System.out.println("=========>  service onCreate ======");
            IntentFilter filter1 = new IntentFilter();
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
            filter1.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
            CommunicationEventReceiver mReceiver = new CommunicationEventReceiver(getApplicationContext(), this);
            registerReceiver(mReceiver, filter1);

            //registering receiver for airdesk events
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction(Constants.SUBSCRIBED_TO_TAGS);
            AirDeskReceiver airDeskReceiver = new AirDeskReceiver();
            registerReceiver(airDeskReceiver, filter2);

            Intent intent = new Intent(MainActivity.this, SimWifiP2pService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            // spawn the chat server background task
            //new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            CommunicationTask.IncomingCommTask incomingCommTask = new CommunicationTask().getIncomingCommTask();
            incomingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        public void requestPeers() {
            if(mBound && mManager != null)
                mManager.requestPeers(mChannel, (SimWifiP2pManager.PeerListListener) CommunicationManager.this);
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

                /*new Thread()
                {
                    public void run() {
                        while (mBound && mManager != null) {
                            mManager.requestPeers(mChannel, (SimWifiP2pManager.PeerListListener) CommunicationManager.this);
                            try {
                                sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
*/
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
                mManager = null;
                mChannel = null;
                mBound = false;
            }
        };

        @Override
        public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {

        }

        @Override
        public void onPeersAvailable(SimWifiP2pDeviceList peers) {
            StringBuilder peersStr = new StringBuilder();

            peerList.clear();
            peerList.addAll(peers.getDeviceList());

            // compile list of devices in range
            for (SimWifiP2pDevice device : peers.getDeviceList()) {
                //String devstr = "" + device.deviceName + " (" + device.getVirtIp() + ")\n";
                String devstr = "" + device.deviceName + " ";
                peersStr.append(devstr);
                TextView onlineUsers = (TextView) findViewById(R.id.usersOnline);
                onlineUsers.setText(devstr);

                //TODO: connect to new devices from here itself
                //////////TODO....... keep a list of outgoing task for each device
                CommunicationTask.OutgoingCommTask outgoingCommTask = new CommunicationTask().getOutgoingCommTask();
                outgoingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device.getVirtIp());
            }
        }
    }

    }
