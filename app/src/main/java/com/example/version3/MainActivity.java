package com.example.version3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));

            }
        };
        Timer timer=new Timer();
        timer.schedule(task,3000);

    }
}