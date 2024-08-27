package com.finalproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Plant Receiver receives broadcasts from MainActivity, and updates data in shared preferences.
 * Updated data is accessed by the stats fragment.
 */
public class PlantReceiver extends BroadcastReceiver {

    protected Context context;

    PlantReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        assert action != null;
        // is true when a focus session has finished
        if (MainActivity.CUSTOM_BROADCAST.equals(action)) {
            Bundle extras = intent.getExtras();
            // bundle code
            if (extras != null) {
                AccessSharedPref sharedPref = new AccessSharedPref(context);
                // bundle is accessed updated info is available re: focus stats
                if (extras.containsKey("update_focus")) {
                    int focusTotal = extras.getInt("update_focus");
                    int focusSession = extras.getInt("focus_sessions");
                    sharedPref.storeFocusStats(focusTotal, focusSession);
                }
            }
        }
    }
}
