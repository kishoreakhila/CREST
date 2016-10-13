package com.preethi.android.crustappandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by preethi on 10/10/16.
 */

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("Service Crust Started","!!!!");

            Intent serviceIntent = new Intent(context, ShakeService.class);
            context.startService(serviceIntent);
        }

    }
}