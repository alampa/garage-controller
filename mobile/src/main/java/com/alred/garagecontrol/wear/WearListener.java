package com.alred.garagecontrol.wear;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearListener extends WearableListenerService {
    public WearListener() {
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Intent intent = new Intent();
        intent.setAction(Constants.MESSAGE_ACTION);
        intent.putExtra(Constants.MESSAGE_EXTRA, messageEvent.getPath());
        sendBroadcast(intent);
    }
}
