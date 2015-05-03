package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocketServer;

public class IncommingCommTaskBk extends AsyncTask<Void, SimWifiP2pSocket, Void> {

    public static final String TAG = "CommunicationTask";
    SimWifiP2pManager.Channel mChannel = null;
    SimWifiP2pSocketServer mSrvSocket = null;
    ReceiveCommTaskBk mComm = null;
    SimWifiP2pSocket mCliSocket = null;

    @Override
    protected Void doInBackground(Void... params) {

        Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

        ////TODO
        int port = 10001;
        try {
            mSrvSocket = new SimWifiP2pSocketServer(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SimWifiP2pSocket sock = mSrvSocket.accept();
                if (mCliSocket != null && mCliSocket.isClosed()) {
                    mCliSocket = null;
                }
                if (mCliSocket != null) {
                    Log.d(TAG, "Closing accepted socket because mCliSocket still active.");
                    sock.close();
                } else {
                    publishProgress(sock);
                }
            } catch (IOException e) {
                Log.d("Error accepting socket:", e.getMessage());
                break;
                //e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(SimWifiP2pSocket... values) {
        mCliSocket = values[0];
        mComm = new ReceiveCommTaskBk();

        mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
    }
}
