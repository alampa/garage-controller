package com.alred.garagecontrol;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by alanlampa on 10/12/15.
 */
public class GarageManager {

    final String TAG = getClass().getSimpleName();

    Context mCtx;
    static GarageManager mGarageManager;

    public class GarageStatus {
        @SerializedName("left")
        @Expose
        public Boolean left;
        @SerializedName("right")
        @Expose
        public Boolean right;
    }


    private interface GarageInterface {

        @POST("/trigger/{doorId}/{serial}")
        void triggerDoor(@Path("doorId") int doorId, @Path("serial") String serial, Callback<String> cb);

        @GET("/status")
        void getStatus(Callback<GarageStatus> cb);

    }

    private GarageManager(Context ctx) {
        mCtx = ctx;
    }

    public static GarageManager getInstance(Context context) {
        if (mGarageManager == null) {
            mGarageManager = new GarageManager(context);
        }

        return mGarageManager;
    }

    private String getEndpoint() {

        ConnectivityManager connManager = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo.isConnected() && (networkInfo.getType() == connManager.TYPE_WIFI)) {

            Log.d(TAG, "Connected to WIFI...");

            WifiManager wm = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wm.getConnectionInfo();

            if (wifiInfo.getSSID().contains("<my local ssid>")) {
                return "http://<<my local server address>>";
            }
        }

        return "http://<<my public server address>>";
    }

    public void triggerDoor(int doorId, Callback<String> cb) {

        String serial = Build.SERIAL.toUpperCase();

        Log.d(TAG, "Using endpoint: " + getEndpoint());

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(getEndpoint()).build();
        GarageInterface garageInterface = restAdapter.create(GarageInterface.class);

        Log.d(TAG, "POSTing triggerDoor command: " + doorId);
        garageInterface.triggerDoor(doorId, serial, cb);


    }

    public void getDoorStatus(Callback<GarageStatus> cb) {

        Log.d(TAG, "Using endpoint: " + getEndpoint());

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(getEndpoint()).build();
        GarageInterface garageInterface = restAdapter.create(GarageInterface.class);

        Log.d(TAG, "GETting status..");

        garageInterface.getStatus(cb);
    }





}
