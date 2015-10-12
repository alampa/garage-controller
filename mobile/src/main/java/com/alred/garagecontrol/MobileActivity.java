package com.alred.garagecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.alred.garagecontrol.wear.Constants;
import com.alred.garagecontrol.wear.WearManager;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MobileActivity extends AppCompatActivity {

    WearManager mWearManager;
    GarageManager mGarageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);

        mWearManager = WearManager.getInstance(this);
        mWearManager.start();

        mGarageManager = GarageManager.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startService(new Intent(this, GarageControlService.class));
    }

    public void onLeftClick(View v) {

        final Button b = (Button) v;
        b.setEnabled(false);

        mGarageManager.triggerDoor(0, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                b.setEnabled(true);
                updateStatus(true);
            }

            @Override
            public void failure(RetrofitError error) {
                b.setEnabled(true);
                updateStatus(true);
            }
        });
    }

    public void onRightClick(View v) {

        final Button b = (Button) v;
        b.setEnabled(false);

        mGarageManager.triggerDoor(1, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                b.setEnabled(true);
                updateStatus(true);
            }

            @Override
            public void failure(RetrofitError error) {
                b.setEnabled(true);
                updateStatus(true);
            }
        });

    }

    private void updateStatus(boolean wait) {

        if (wait) {
            try {
                Thread.sleep(13000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        final ImageView leftIndicator = (ImageView) findViewById(R.id.leftIndicator);
        final ImageView rightIndicator = (ImageView) findViewById(R.id.rightIndicator);

        mGarageManager.getDoorStatus(new Callback<GarageManager.GarageStatus>() {
            @Override
            public void success(GarageManager.GarageStatus garageStatus, Response response) {

                    if (garageStatus.left) {
                        leftIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_open, getApplicationContext().getTheme()));
                    } else {
                        leftIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_closed, getApplicationContext().getTheme()));
                    }

                    if (garageStatus.right) {
                        rightIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_open, getApplicationContext().getTheme()));
                    } else {
                        rightIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_closed, getApplicationContext().getTheme()));
                    }
            }

            @Override
            public void failure(RetrofitError error) {
                leftIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_unknown, getApplicationContext().getTheme()));
                rightIndicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_unknown, getApplicationContext().getTheme()));
            }
        });
    }

    public void onLaunchWear(View v) {
        mWearManager.sendMessage(Constants.COMMAND_START_WEAR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus(false);

    }

    @Override
    protected void onDestroy() {
        mWearManager.stop();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mobile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
