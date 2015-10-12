package com.alred.garagecontrol.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by alanlampa on 10/10/15.
 */
public class WearManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final String TAG = getClass().getSimpleName();

    private static WearManager mWearManager;
    private GoogleApiClient mGoogleApiClient;
    private Context mCtx;
    private BroadcastReceiver mReceiver;

    public interface WearManagerInterface {
        void onWearMessageReceived(String message);
    }
    private WearManagerInterface mWearManagerInterface;

    public static WearManager getInstance(Context ctx) {

        if (mWearManager == null) {
            mWearManager = new WearManager(ctx);
        }

        return mWearManager;

    }

    private WearManager(Context ctx) {
        mCtx = ctx;

        // initialize api client
        mGoogleApiClient = new GoogleApiClient.Builder(mCtx)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    public void addWearManagerListener(WearManagerInterface managerInterface) {
        mWearManagerInterface = managerInterface;
    }

    public void removeWearManagerListener() {
        mWearManagerInterface = null;
    }

    public void start() {
        mGoogleApiClient.connect();

        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String message = intent.getStringExtra(Constants.MESSAGE_EXTRA);
                    if (mWearManagerInterface != null) {
                        Log.d(TAG, "Calling interface onWearMessageReceived with: " + message);
                        mWearManagerInterface.onWearMessageReceived(message);
                    }
                    else Log.d(TAG, "WearManagerInterface not implemented!");
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.MESSAGE_ACTION);
            mCtx.registerReceiver(mReceiver, intentFilter);

            Log.d(TAG, "WearListener registered");

        }
    }

    public void stop() {
        mGoogleApiClient.disconnect();

        if (mReceiver != null) {
            mCtx.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }

    public void sendMessage(String message) {

        final String msg = message;

        Log.d(TAG, "Sending message to wear: " + message);

        new Thread( new Runnable() {
            @Override
            public void run() {
                //Get Connected Nodes
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                List<Node> nodes = result.getNodes();

                //For each node - send the message across
                for (Node node : nodes) {
                    MessageApi.SendMessageResult messageResult = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), msg, null).await();
                    if (!messageResult.getStatus().isSuccess()) {
                        //Log Message Success
                        Log.d(TAG, "Successfully sent message: " + msg);
                    }
                }
            }
        }).start();
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
