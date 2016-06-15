package com.pgizka.simplecallrecorder.recordings;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.pgizka.simplecallrecorder.R;


public class RecordingDetailActivity extends ActionBarActivity {
    static final String TAG = RecordingDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().
                    add(R.id.container_recordings_detail, new RecordingDetailFragment()).commit();
        }
    }


}
