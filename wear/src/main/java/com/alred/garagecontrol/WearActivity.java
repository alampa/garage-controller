package com.alred.garagecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alred.garagecontrol.wear.Constants;
import com.alred.garagecontrol.wear.WearManager;

public class WearActivity extends Activity {

    final String TAG = getClass().getSimpleName();

    private TextView mTextView;

    private WearManager mWearManager;
    private ImageView mLeftIndicator;
    private ImageView mRightIndicator;

    private CountDownTimer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mLeftIndicator = (ImageView) stub.findViewById(R.id.leftIndicator);
                mRightIndicator = (ImageView) stub.findViewById(R.id.rightIndicator);
            }
        });

        mWearManager = WearManager.getInstance(this);
        mWearManager.addWearManagerListener(new WearManager.WearManagerInterface() {
            @Override
            public void onWearMessageReceived(String message) {

                if (message.contains(Constants.STATUS_PREFIX)) {
                    updateIndicators(message);
                }
                else if (message.contains(Constants.RESULT_PREFIX)) {

                    String result = message.replace(Constants.RESULT_PREFIX, "");

                    if (result.equals("OK")) {
                        mWearManager.sendMessage(Constants.COMMAND_GET_STATUS);
                    }
                    else {
                        showAlert(result, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                    }
                }
                else if (message.contains("ERROR - ")) {
                    showAlert(message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                }
            }
        });

    }

    private void showAlert(String message, DialogInterface.OnClickListener cb) {

        AlertDialog.Builder builder = new AlertDialog.Builder(WearActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", cb);
        builder.show();

    }

    private void updateIndicators(String statusMessage) {

        if ((mLeftIndicator == null) || (mRightIndicator == null)) {
            Log.d(TAG, "Indicators are null!");
            return;
        }

        // status message comes in the form of /status.left,right
        String[] status = statusMessage.replace(Constants.STATUS_PREFIX, "").split(",");

        if (status.length > 1) {

            if (status[0].contains("true")) {
                mLeftIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_open, getApplicationContext().getTheme()));
            } else if (status[0].contains("false")) {
                mLeftIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_closed, getApplicationContext().getTheme()));
            }
            else {
                mLeftIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_unknown, getApplicationContext().getTheme()));
            }

            if (status[1].contains("true")) {
                mRightIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_open, getApplicationContext().getTheme()));
            } else if (status[1].contains("false")){
                mRightIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_closed, getApplicationContext().getTheme()));
            }
            else {
                mRightIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_unknown, getApplicationContext().getTheme()));
            }
        }
        else {
            mLeftIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_unknown, getApplicationContext().getTheme()));
            mRightIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_unknown, getApplicationContext().getTheme()));
        }

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 250, 50, 250};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mWearManager.start();
    }

    @Override
    protected void onStop() {
        mWearManager.stop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 500, 50, 500};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "wakeLock");
        wl.acquire(20000);

        mWearManager.sendMessage(Constants.COMMAND_GET_STATUS);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onLeftPress(View v) {
        v.setEnabled(false);

        if (mWearManager != null) {
            mWearManager.sendMessage(Constants.COMMAND_TRIGGER_LEFT);
        }

        try {
            Thread.sleep(5000);
            v.setEnabled(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void onRightPress(View v) {
        v.setEnabled(false);

        if (mWearManager != null) {
            mWearManager.sendMessage(Constants.COMMAND_TRIGGER_RIGHT);
        }

        try {
            Thread.sleep(5000);
            v.setEnabled(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
