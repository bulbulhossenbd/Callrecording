package com.pgizka.simplecallrecorder.Home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.main.MainActivity;
import com.pgizka.simplecallrecorder.util.SettingsActivity;


/**
 * Created by bulbul-ulab on 6/6/16.
 */
public class Home extends Activity {

    TextView auto_Answer,auto_rec,about_app,userHelp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);

        auto_Answer = (TextView) findViewById(R.id.auto_answer);
        auto_Answer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent auto_Intent_Answer = new Intent(Home.this,MainActivity.class);
                startActivity(auto_Intent_Answer);
            }
        });

        auto_rec = (TextView) findViewById(R.id.auto_recoriding);
        auto_rec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent auto_Intent_Answer = new Intent(Home.this,SettingsActivity.class);
                startActivity(auto_Intent_Answer);
            }
        });

        about_app = (TextView) findViewById(R.id.about);
        about_app.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent auto_Intent_Answer = new Intent(Home.this,About.class);
                startActivity(auto_Intent_Answer);
            }
        });

        userHelp = (TextView) findViewById(R.id.userid);
        userHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent auto_Intent_Answer = new Intent(Home.this,User_Help.class);
                startActivity(auto_Intent_Answer);


            }
        });



    }
}
