package com.pgizka.simplecallrecorder.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences systemPref = getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);
        if(systemPref.getBoolean(PreferanceStrings.LAUNCHER_SEEN, false)){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_launcher);
            Button okButton = (Button) findViewById(R.id.launcher_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = systemPref.edit();
                    editor.putBoolean(PreferanceStrings.LAUNCHER_SEEN, true);
                    editor.commit();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            });
        }

    }

}
