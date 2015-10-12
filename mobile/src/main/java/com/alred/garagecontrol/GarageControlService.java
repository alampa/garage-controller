package com.alred.garagecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.alred.garagecontrol.wear.Constants;
import com.alred.garagecontrol.wear.WearManager;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GarageControlService extends Service {

    final String TAG = getClass().getSimpleName();

    WearManager mWearManager;
    GarageManager mGarageManager;

    public GarageControlService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWearManager = WearManager.getInstance(this);
        mWearManager.start();
        mWearManager.addWearManagerListener(new WearManager.WearManagerInterface() {
            @Override
            public void onWearMessageReceived(String message) {
                processWearMessage(message);
            }
        });

        mGarageManager = GarageManager.getInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void processWearMessage(String message) {

        Log.d(TAG, "Processing message: " + message);

        if (message.contains(Constants.COMMAND_TRIGGER_LEFT) || message.contains(Constants.COMMAND_TRIGGER_RIGHT)) {

            int doorId = 100;
            if (message.contains(Constants.COMMAND_TRIGGER_LEFT)) {
                doorId = 0;
            }
            if (message.contains(Constants.COMMAND_TRIGGER_RIGHT)) {
                doorId = 1;
            }

            // check for weirdness.
            if (doorId == 100) return;

            mGarageManager.triggerDoor(doorId, new Callback<String>() {
                @Override
                public void success(String s, Response response) {
                    mWearManager.sendMessage(Constants.RESULT_PREFIX + "OK");

                    try {
                        Thread.sleep(13000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    getDoorStatus();

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "Error on triggerDoor: " + error.getMessage());
                    mWearManager.sendMessage(Constants.RESULT_PREFIX + "ERROR");
                }
            });
        }

        if (message.contains(Constants.COMMAND_GET_STATUS)) {
            getDoorStatus();
        }

    }

    private void getDoorStatus() {
        mGarageManager.getDoorStatus(new Callback<GarageManager.GarageStatus>() {
            @Override
            public void success(GarageManager.GarageStatus gs, Response response) {
                mWearManager.sendMessage(Constants.STATUS_PREFIX + gs.left + "," + gs.right);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Error on getDoorStatus: " + error.getMessage());
                mWearManager.sendMessage(Constants.STATUS_PREFIX + "ERROR");
            }
        });
    }

    @Override
    public void onDestroy() {
        mWearManager.stop();
        super.onDestroy();

    }
}
