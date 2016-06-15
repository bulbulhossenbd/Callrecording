package com.pgizka.simplecallrecorder.servicerecording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    static final String TAG = CallReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Receiver called");
        Bundle bundle = intent.getExtras();
        Intent newIntent = new Intent(context, RecordingService.class);
        newIntent.setAction(intent.getAction());
        newIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
        newIntent.putExtras(bundle);
        context.startService(newIntent);
    }
}
