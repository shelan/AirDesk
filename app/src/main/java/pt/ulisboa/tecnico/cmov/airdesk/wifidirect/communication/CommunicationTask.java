package pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import pt.ulisboa.tecnico.cmov.airdesk.AirDeskReceiver;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.airdesk.fragment.ForeignWorkspaceListFragment;

/**
 * Created by ashansa on 5/1/15.
 */
public class CommunicationTask {

    public static final String TAG = "CommunicationTask";
    ReceiveCommTask receiveCommTask = null;
    Socket mCliSocket = null;
    private boolean mBound = false;
    Socket s;
    Gson gson = new Gson();
    AirDeskReceiver airDeskReceiver;
    Context context;

    public CommunicationTask(ForeignWorkspaceListFragment foreignWorkspaceFragment) {
        airDeskReceiver = new AirDeskReceiver(foreignWorkspaceFragment);
    }

    public CommunicationTask(ForeignWorkspaceListFragment foreignWorkspaceFragment, Context context) {
        airDeskReceiver = new AirDeskReceiver(foreignWorkspaceFragment);
        this.context = context;
    }

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

    public class IncomingCommTask extends AsyncTask<Void, Socket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncomingCommTask started (" + this.hashCode() + ").");
            try {
                ServerSocket serverSocket = null;
                serverSocket = new ServerSocket(Constants.port);
                //Socket client = serverSocket.accept();

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket client = serverSocket.accept();
                        SocketAddress clientRemoteAddress = client.getRemoteSocketAddress();
                        String clientIP = clientRemoteAddress.toString();
                        System.out.println(".......... client connected ..... IP : " + clientIP);

                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        System.out.printf("############ socket created");
                        String msgJson = bufferedReader.readLine();
                        System.out.println("#### MSG : " + msgJson);
                        //gson.fromJson(msgJson,);
                        AirDeskMessage msg = gson.fromJson(msgJson, AirDeskMessage.class);
                        //set sender IP using the socket created
                        msg.setSenderIP(client.getInetAddress().getHostAddress());

                        airDeskReceiver.handleMessage(msg);

                        if (mCliSocket != null && mCliSocket.isClosed()) {
                            mCliSocket = null;
                        }
                        if (mCliSocket != null) {
                            Log.d(TAG, "Closing accepted socket because mCliSocket still active.");
                            client.close();
                        } else {
                            publishProgress(client);
                        }
                    } catch (IOException e) {
                        Log.d("Error accepting socket:", e.getMessage());
                        break;
                        //e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            ////TODO
           /* int port = 10001;
            try {
                mSrvSocket = new SimWifiP2pSocketServer(port);
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
            } catch (IOException e) {
                e.printStackTrace();
            }*/


            return null;
        }

      /*  @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Socket... values) {
            mCliSocket = values[0];
            receiveCommTask = new ReceiveCommTask();

            receiveCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }*/
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            System.out.println("-----");
        }


        @Override
        protected String doInBackground(String... params) {
            ////TODO
            int port = Constants.port;
            try {
                System.out.println("=========> ging to write msg");
                //params are <receiverIP> , <my user ID>
                //mCliSocket = new Socket(params[0],port);
                mCliSocket = new Socket();
                mCliSocket.bind(null);
                mCliSocket.connect(new InetSocketAddress(params[0],port));

                System.out.println("receiver IP=========> " + mCliSocket.getInetAddress().getHostAddress());

                String msg =  gson.toJson(createIntroduceMsg(params[1]));
                System.out.println("========socket created. Msg =========> : " + msg);
                /////////TODO...... check whether other end receives this
                mCliSocket.getOutputStream().write(msg.getBytes());
                mCliSocket.getOutputStream().flush();
                mCliSocket.getOutputStream().close();
                System.out.println("=========> msg written");
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

        private AirDeskMessage createIntroduceMsg(String ownerId) {
            AirDeskMessage msg = new AirDeskMessage(Constants.INTRODUCE_MSG, ownerId);
            return msg;
        }
    }

    public class ReceiveCommTask extends AsyncTask<Socket, String, Void> {

        @Override
        protected Void doInBackground(Socket... params) {
            BufferedReader bufferedReader;
            String st;

            s = params[0];
            try {
                System.out.println("############ going to receive msg");
                System.out.println("............... received address >>>>>>>>>>>>> " + s.getInetAddress().getHostAddress());
                bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                System.out.printf("############ socket created");
                String msgJson = bufferedReader.readLine();
                System.out.println("#### MSG : " + msgJson);
                //gson.fromJson(msgJson,);
                AirDeskMessage msg = gson.fromJson(msgJson, AirDeskMessage.class);
                //set sender IP using the socket created
                msg.setSenderIP(s.getInetAddress().getHostAddress());

                airDeskReceiver.handleMessage(msg);
                System.out.println("sent msg to handle.....");
                publishProgress(msgJson);
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
