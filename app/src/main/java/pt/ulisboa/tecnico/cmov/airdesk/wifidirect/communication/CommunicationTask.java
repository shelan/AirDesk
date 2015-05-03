package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.sockets.SimWifiP2pSocketServer;

/**
 * Created by ashansa on 5/1/15.
 */
public class CommunicationTask {

    public static final String TAG = "CommunicationTask";
    SimWifiP2pManager.Channel mChannel = null;
    SimWifiP2pSocketServer mSrvSocket = null;
    ReceiveCommTask receiveCommTask = null;
    SimWifiP2pSocket mCliSocket = null;
    private boolean mBound = false;
    SimWifiP2pSocket s;
    Gson gson = new Gson();

    IncomingCommTask incomingCommTask;
    OutgoingCommTask outgoingCommTask;

    public IncomingCommTask getIncomingCommTask() {
        if(this.incomingCommTask == null) {
            this.incomingCommTask = new IncomingCommTask();
        }
        return this.incomingCommTask;
    }

    public OutgoingCommTask getOutgoingCommTask() {
        if(this.outgoingCommTask == null) {
            this.outgoingCommTask = new OutgoingCommTask();
        }
        return this.outgoingCommTask;
    }

    public void handleMessage(AirDeskMessage msg) {
        System.out.println("........handleMessage.........");
        switch (msg.getType()) {
            case Constants.SUBSCRIBE_TAGS:
                System.out.printf("tag subscription wifi direct walin awooo................");
                break;
            default:
                System.out.println("........ default case .......");
        }
    }

    public class IncomingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncomingCommTask started (" + this.hashCode() + ").");

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
            receiveCommTask = new ReceiveCommTask();

            receiveCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }

    /////TODO: temp variable
    private static int i;

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

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


                String msg =  gson.toJson(new AirDeskMessage("test")); //"Hello message" + (i++);
                System.out.println("========socket created. Msg : " + msg);
                /////////TODO...... check whether other end receives this
                mCliSocket.getOutputStream().write(msg.getBytes());
                mCliSocket.getOutputStream().flush();
                mCliSocket.getOutputStream().close();
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
                receiveCommTask = new ReceiveCommTask();
                receiveCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
            }
        }
    }

    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, Void> {

        @Override
        protected Void doInBackground(SimWifiP2pSocket... params) {
            BufferedReader sockIn;
            String st;

            s = params[0];
            try {
                System.out.println("############ going to receive msg");
                sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
                System.out.printf("############ socket created");
                String msgJson = sockIn.readLine();
                System.out.println("#### MSG : " + msgJson);
                AirDeskMessage msg = gson.fromJson(msgJson, AirDeskMessage.class);
                handleMessage(msg);
                System.out.println("sent msg to handle.....");
                publishProgress(msgJson);


                /*while ((st = sockIn.readLine()) != null) {
                    System.out.println("###############################");
                    System.out.println("###############################");
                    System.out.println("recieved msg: " + st);
                    System.out.println("###############################");
                    System.out.println("###############################");
                    publishProgress(st);
                }*/
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
            if(s != null) {
                if (!s.isClosed()) {
                    try {
                        s.close();
                    }
                    catch (Exception e) {
                        Log.d("Error closing socket:", e.getMessage());
                    }
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
}
