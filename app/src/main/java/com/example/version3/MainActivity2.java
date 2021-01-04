package com.example.version3;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.potterhsu.Pinger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {
    EditText ip_edt,mac_edt;
    Button conn_btn,cancel_btn;
    Boolean finish = false;
    CheckBox bw;
    Handler handler = new Handler();
    Integer times = 0;
    private LinearLayout ll_progress;
    SQLiteDatabase dbrw;
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> items=new ArrayList<>();
    FloatingActionButton add_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        conn_btn = findViewById(R.id.conn_btn);
        ip_edt = findViewById(R.id.ip_edt);
        mac_edt = findViewById(R.id.mac_edt);
        bw = findViewById(R.id.checkBox);
        handler.removeCallbacks(Timer);
        cancel_btn = findViewById(R.id.cancel_btn);
        ll_progress = findViewById(R.id.ll_progress);
        add_btn=findViewById(R.id.btn_add);
        listView=findViewById(R.id.listView);
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
        dbrw=new MyDBHelper(this).getReadableDatabase();

        Cursor c=dbrw.rawQuery("SELECT * FROM myTable",null);
        c.moveToFirst();
        items.clear();
        for(int i=0;i<c.getCount();i++){
            items.add("user:"+c.getString(0)+"\nIP:"+c.getString(1)+"\t\t\t\tmac:"+c.getString(2));
            c.moveToNext();
        }
        adapter.notifyDataSetChanged();
        c.close();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c=dbrw.rawQuery("SELECT * FROM myTable",null);
                c.moveToFirst();
                items.clear();
                for(int i=0;i<c.getCount();i++){
                    if(i==position) {
                        Toast.makeText(MainActivity2.this,"歡迎"+c.getString(0)+"使用",Toast.LENGTH_SHORT).show();
                        ip_edt.setText(c.getString(1));
                        mac_edt.setText(c.getString(2));
                        if(c.getInt(3)==1){
                            bw.setChecked(true);
                        }
                        else {
                            bw.setChecked(false);
                        }
                    }
                    c.moveToNext();
                }
                c.moveToFirst();
                items.clear();
                for(int i=0;i<c.getCount();i++){
                    items.add("user:"+c.getString(0)+"\nIP:"+c.getString(1)+"\t\t\t\tmac:"+c.getString(2));
                    c.moveToNext();
                }
                adapter.notifyDataSetChanged();
                c.close();
            }
        });
        conn_btn.setOnClickListener(new View.OnClickListener() {//連線的button
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                String[] temp = ip_edt.getText().toString().split("\\.");//temp用於偵測IP是否符合要求
                String[] temp2 = mac_edt.getText().toString().split("-");//temp2用於偵測MAC是否符合要求
                String lip = ip_edt.getText().toString();//暫存IP位置
                String lmac = mac_edt.getText().toString();//暫存MAC位置
                ll_progress.setVisibility(View.VISIBLE);//顯示進度條
                new AsyncTask<Void, Integer,Boolean>(){
                    @Override
                    protected Boolean doInBackground(Void...voids){
                        int progress = 0;
                        while (progress <= 100){
                            try {
                                Thread.sleep(50);
                                publishProgress(progress);
                                progress++;
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                        return true;
                    }
                    @Override
                    protected void onProgressUpdate(Integer...values){
                        super.onProgressUpdate(values);
                    }
                }.execute();

                if(temp.length == 4 && temp2.length == 6){//判斷IP長度跟MAC長度的正確性
                    int i = 0;//先宣告i是因為在迴圈外也要用到
                   for(i=0;i<4;i++)//如果大於255代表IP不合法
                        if(Integer.parseInt(temp[i]) >= 255)
                            break;
                    if(i == 4){//ip valid!!
                        Toast.makeText(MainActivity2.this,"開始傳送magic packet",Toast.LENGTH_SHORT).show();
                        if(bw.isChecked())
                            lip = temp[0] + "." +temp[1]+"."+temp[2]+".255";//改為廣播IP
                        new WakeThread(lip,lmac).start();
                        if(bw.isChecked()) {
                            ifOpened(ip_edt.getText().toString());
                            handler.postDelayed(Timer, 5000);//5000意味著五秒一次
                        }else{//不更改廣播IP(在外網呼叫，廣播IP必須透過NAT轉發時設定)
                            openRDP();//直接打開RDP，因為不能被ping到
                            Toast.makeText(MainActivity2.this,"ping為icmp，不支援port fording，直接開啟RDP",Toast.LENGTH_SHORT).show();
                        }
                    }else
                        Toast.makeText(MainActivity2.this,"請輸入正確的IP",Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(MainActivity2.this,"請輸入正確的IP或MAC",Toast.LENGTH_LONG).show();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_progress.setVisibility(View.GONE);//取消進度條
                handler.removeCallbacks(Timer);//取消ping
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity2.this,MainActivity3.class),1);//切換到新增的頁面
            }
        });

    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1) {
            if (resultCode == 101) {
                Cursor c=dbrw.rawQuery("SELECT * FROM myTable",null);
                c.moveToFirst();
                items.clear();
                for(int i=0;i<c.getCount();i++){
                    items.add("user:"+c.getString(0)+"\nIP:"+c.getString(1)+"\t\t\t\tmac:"+c.getString(2));
                    c.moveToNext();
                }
                adapter.notifyDataSetChanged();
                c.close();
            }
        }
    }
    private Runnable Timer = new Runnable() {//用於計時，本程式在發送開機封包後會測試十次ping，每次5s，現在的電腦都能在50s內完成開機
        @Override
        public void run() {
            ifOpened(ip_edt.getText().toString());
            handler.postDelayed(this,5000);
        }
    };
    protected void ifOpened(String ip){//使用ping測試電腦是否開機成功
        Pinger pinger = new Pinger();//pinger是別人寫好的程式，其實程式內很簡單，也能自己寫
        //但要寫成可以include的東西有點困難，反正他寫好了我就override，用他的類別就好
        pinger.setOnPingListener(new Pinger.OnPingListener() {
            @Override
            public void onPingSuccess() {//他定義好的ping成功情形
                finish = true;//設為true用於後面判斷
                Looper.prepare();//要使用這條指令才能在thread中設定變數、使用toast
                openRDP();
                Toast.makeText(MainActivity2.this,"開機成功",Toast.LENGTH_LONG).show();
                ll_progress.setVisibility(View.GONE);//隱藏進度條
                handler.removeCallbacks(Timer);//停止計時
                Looper.loop();//使用完要讓它繼續迴圈
                pinger.cancel();//馬上取消ping，雖然不這麼做也沒差，但之前遇過問題
            }
            @Override
            public void onPingFailure() {
                Looper.prepare();
                Toast.makeText(MainActivity2.this,"等待開機",Toast.LENGTH_SHORT).show();
                times ++;
                if(times >= 10){
                    handler.removeCallbacks(Timer);
                }
                Looper.loop();
            }
            @Override
            public void onPingFinish() {}
        });

        pinger.pingUntilSucceeded(ip,5000);
    }
    protected void openRDP(){
        Intent activityIntent = new Intent();
        activityIntent.setComponent(new ComponentName("com.microsoft.rdc.androidx","com.microsoft.rdc.ui.activities.HomeActivity" ));
        startActivity(activityIntent);
    }
    public class MyTimerTask extends TimerTask
    {
        public void run()
        {
        }
    };
}


class WakeThread extends Thread{
    String ip = null;
    String macAddr = null;
    public WakeThread(String ip,String macAddr){
        this.ip = ip;
        this.macAddr = macAddr;
    }
    @Override
    public void run() {
        super.run();
        wakeOnLan(ip,macAddr);
    }
    public void wakeOnLan(String ip,String macAddr){
        DatagramSocket datagramSocket = null;
        try {//magic packet有特殊規格，使用UDP port 進行廣播
            byte[] mac = getMacBytes(macAddr);
            byte[] magic = new byte[6+16*mac.length];
            //1.寫入六個FF
            for (int i=0;i<6;i++){
                magic[i] = (byte)0xff;
            }
            //2.寫入16次host MAC位置，當電腦收到屬於自己的MAC，會發送開機訊號
            for(int i=6;i<magic.length; i += mac.length){
                System.arraycopy(mac,0,magic,i,mac.length);
            }
            datagramSocket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(magic,magic.length, InetAddress.getByName(ip),8888);
            datagramSocket.send(datagramPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
    }
    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}

