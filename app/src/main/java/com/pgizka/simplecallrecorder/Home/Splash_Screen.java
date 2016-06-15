package com.pgizka.simplecallrecorder.Home;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.pgizka.simplecallrecorder.R;


/**
 * Created by bulbulkhan on 10/27/2015.
 */
public class Splash_Screen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);
        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(1900);
                    Intent startMainscreen = new Intent(getApplicationContext(),Home.class);
                    startActivity(startMainscreen);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }
}
