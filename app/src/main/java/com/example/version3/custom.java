package com.example.version3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class custom extends AppCompatActivity {
    Button con,show,reset;
    EditText ssh,openwrt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        con = findViewById(R.id.conform_btn);
        show = findViewById(R.id.show_btn);
        ssh = findViewById(R.id.port);
        openwrt = findViewById(R.id.command);
        reset = findViewById(R.id.reset);
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Integer.parseInt(ssh.getText().toString());
                    SharedPreferences pref = getSharedPreferences("default_command", MODE_PRIVATE);
                    pref.edit().putString("command",openwrt.getText().toString()).putString("port",ssh.getText().toString()).apply();
                    setResult(101);
                    finish();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(custom.this,"請輸入正確的port號",Toast.LENGTH_LONG).show();
                }
            }
        });
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    openwrt.setText(getSharedPreferences("default_command",MODE_PRIVATE)
                            .getString("command","none"));
                    ssh.setText(getSharedPreferences("default_command",MODE_PRIVATE).getString("port","22"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("default_command", MODE_PRIVATE);
                pref.edit().putString("command","none").putString("port","22").apply();
            }
        });
    }
}