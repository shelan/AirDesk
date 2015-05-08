package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.UnknownHostException;

import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocketServer;

public class OutgoingCommTaskBk extends AsyncTask<String, Void, String> {

    public static final String TAG = "CommunicationTask";
    SimWifiP2pManager.Channel mChannel = null;
    SimWifiP2pSocketServer mSrvSocket = null;
    ReceiveCommTaskBk mComm = null;
    SimWifiP2pSocket mCliSocket = null;

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(String... params) {
        ////TODO
        int port = 10001;
        try {
            System.out.println("=========> ging to write msg");
            mCliSocket = new SimWifiP2pSocket(params[0],port);
            System.out.println("========socket created.");
            /////////TODO...... check whether other end receives this
            mCliSocket.getOutputStream().write("Hello message".getBytes());
            System.out.println("========== msg written");
        } catch (UnknownHostException e) {
            return "Unknown Host:" + e.getMessage();
        } catch (IOException e) {
            return "IO error:" + e.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {

        }
        else {
            mComm = new ReceiveCommTaskBk();
            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mCliSocket);
        }
    }
}