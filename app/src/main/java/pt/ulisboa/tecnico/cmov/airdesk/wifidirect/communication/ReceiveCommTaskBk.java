package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import android.os.AsyncTask;
import android.os.Messenger;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocketServer;

public class ReceiveCommTaskBk extends AsyncTask<SimWifiP2pSocket, String, Void> {

    public static final String TAG = "airdesk";
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private ReceiveCommTaskBk mComm = null;
    private SimWifiP2pSocket mCliSocket = null;

    SimWifiP2pSocket s;

    @Override
    protected Void doInBackground(SimWifiP2pSocket... params) {
        BufferedReader sockIn;
        String st;

        s = params[0];
        try {
            System.out.println("############ going to receive msg");
            sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.printf("############ socket created");
            while ((st = sockIn.readLine()) != null) {
                System.out.println("###############################");
                System.out.println("###############################");
                System.out.println("recieved msg: " + st);
                System.out.println("###############################");
                System.out.println("###############################");
                publishProgress(st);
            }
        } catch (IOException e) {
            Log.d("Error reading socket:", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(String... values) {
        ////mTextOutput.append(values[0]+"\n");
    }

    @Override
    protected void onPostExecute(Void result) {
        if (!s.isClosed()) {
            try {
                s.close();
            }
            catch (Exception e) {
                Log.d("Error closing socket:", e.getMessage());
            }
        }
        s = null;
        if (mBound) {
            ////guiUpdateDisconnectedState();
        } else {
            ///guiUpdateInitState();
        }
    }
}
