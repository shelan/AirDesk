package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.communication.AirDeskMessage;
import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.util.AsyncService;

/**
 * Created by ashansa on 5/4/15.
 */
public class AirDeskSocketListenerService { //AsyncTask {

    AirDeskReceiver airDeskReceiver = new AirDeskReceiver();
    AirDeskListenerThread thread;
    Gson gson = new Gson();

    public AirDeskSocketListenerService() {
        new AirDeskListenerThread().start();
    }

    /*
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            BufferedReader sockIn;

            *//**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             *//*
            ServerSocket serverSocket = new ServerSocket(Constants.AIRDESK_SOCKET_PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Socket client = serverSocket.accept();

                *//**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 *//*

                try {
                    System.out.println("___________________ going to receive msg ___________________");
                    sockIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    System.out.printf("___________________ socket created");
                    String msgJson = sockIn.readLine();
                    System.out.println("___________________ MSG : " + msgJson);
                    AirDeskMessage msg = gson.fromJson(msgJson, AirDeskMessage.class);
                    airDeskReceiver.handleMessage(msg);
                    System.out.println("sent msg to handle.....");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    */

  /*  @Override
    public void onCreate() {
        super.onCreate();
        thread = new AirDeskListenerThread();
        thread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(thread != null) {
            thread.terminate();
            thread = null;
        }
    }*/

    private class AirDeskListenerThread extends Thread {

        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                BufferedReader sockIn;

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                serverSocket = new ServerSocket(Constants.AIRDESK_SOCKET_PORT);
                System.out.println("--------- server socket ------ " + serverSocket.getInetAddress().getHostAddress());
                System.out.println("--------- server port ------ " + serverSocket.getLocalPort());
                while (!Thread.currentThread().isInterrupted()) {
                    Socket client = serverSocket.accept();

                    /**
                     * If this code is reached, a client has connected and transferred data
                     * Save the input stream from the client as a JPEG file
                     */

                    try {
                        System.out.println("___________________ going to receive msg ___________________");
                        sockIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        System.out.printf("___________________ socket created");
                        String msgJson = sockIn.readLine();
                        System.out.println("___________________ MSG : " + msgJson);
                        AirDeskMessage msg = gson.fromJson(msgJson, AirDeskMessage.class);
                        airDeskReceiver.handleMessage(msg);
                        System.out.println("sent msg to handle.....");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void terminate() {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.interrupt();
        }
    }
}