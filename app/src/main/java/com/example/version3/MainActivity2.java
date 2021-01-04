package com.example.version3;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
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
    SQLiteDatabase dbrw; //建立MyDBHelper物件
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> items=new ArrayList<>(); //要放置在list裡的物件陣列
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
        add_btn=findViewById(R.id.btn_add);
        listView=findViewById(R.id.listView);

        //一開始開啟時將database裡的資料匯入
        //宣告Adapter，用simple_list_item_1連接到xml的listview，items目前是空的
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);//將adapter放入listview
        dbrw=new MyDBHelper(this).getReadableDatabase();//取得在SQL裡的資料

        Cursor c=dbrw.rawQuery("SELECT * FROM myTable",null);//查詢SQL裡的所有資料(*指全部的資料)
        c.moveToFirst();//從第一筆開始查詢
        items.clear();//清除items
        for(int i=0;i<c.getCount();i++){
            items.add("user:"+c.getString(0)+"\nIP:"+c.getString(1)+"\t\t\t\tmac:"+c.getString(2));
            c.moveToNext();
        }//將SQL裡每筆的user、IP、MAC取出來組成一個字串，並放入items陣列
        adapter.notifyDataSetChanged();//將新的items放入adapter(更新list)
        c.close();//關閉查詢

        //如果有點擊listview中的資料時，將選到的資料填入IP_edt
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c=dbrw.rawQuery("SELECT * FROM myTable",null);//查詢SQL裡的所有資料(*指全部的資料)
                c.moveToFirst();//從第一筆開始查詢
                items.clear();//清除items
                //position為選擇到的資料在list裡是第幾筆(從0開始數)
                for(int i=0;i<c.getCount();i++){
                    //每筆每筆對，如果i等於選擇到的位置後，將IP、mac放入edit_text中，並用Toast顯示
                    if(i==position) {
                        Toast.makeText(MainActivity2.this,"歡迎"+c.getString(0)+"使用",Toast.LENGTH_SHORT).show();
                        ip_edt.setText(c.getString(1));
                        mac_edt.setText(c.getString(2));
                        //判斷如果add時有將broadcast打勾的話就將這邊的broadcast打勾
                        if(c.getInt(3)==1){
                            bw.setChecked(true);
                        }
                        else {
                            bw.setChecked(false);
                        }
                    }
                    c.moveToNext();// 下一行
                }
                //因為上面的items有變動過，所以這邊要在更新一次items並放入adapter裡
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
        conn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] temp = ip_edt.getText().toString().split("\\.");
                String[] temp2 = mac_edt.getText().toString().split("-");
                String lip = ip_edt.getText().toString();
                String lmac = mac_edt.getText().toString();
                if(temp.length == 4 && temp2.length == 6){
                    int i = 0;
                    for(i=0;i<4;i++)
                        if(Integer.parseInt(temp[i]) >= 255)
                            break;
                    if(i == 4){//ip valid!!
                        Toast.makeText(MainActivity2.this,"開始傳送magic packet",Toast.LENGTH_SHORT).show();
                        if(bw.isChecked())
                            lip = temp[0] + "." +temp[1]+"."+temp[2]+".255";//改為廣播IP
                        new WakeThread(lip,lmac).start();
                        if(bw.isChecked()) {
                            ifOpened(ip_edt.getText().toString());
                            handler.postDelayed(Timer, 5000);
                        }else{
                            openRDP();
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
                handler.removeCallbacks(Timer);
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity2.this,MainActivity3.class),1);//切換Activity
            }
        });

    }

    //加入新資料後要做的事
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1) {
            if (resultCode == 101) {
                // 更新items並放入list中
                Cursor c=dbrw.rawQuery("SELECT * FROM myTable",null);//建立查詢
                c.moveToFirst();//將查詢一致第一筆資料
                items.clear();//清除items
                //將更新後的資料庫內的資料提入items
                for(int i=0;i<c.getCount();i++){
                    items.add("user:"+c.getString(0)+"\nIP:"+c.getString(1)+"\t\t\t\tmac:"+c.getString(2));
                    c.moveToNext();
                }
                adapter.notifyDataSetChanged();//將items寫入adapter
                c.close();//關閉查詢
            }
        }
    }
    private Runnable Timer = new Runnable() {
        @Override
        public void run() {
            ifOpened(ip_edt.getText().toString());
            handler.postDelayed(this,5000);

        }
    };
    protected void ifOpened(String ip){
        Pinger pinger = new Pinger();
        pinger.setOnPingListener(new Pinger.OnPingListener() {
            @Override
            public void onPingSuccess() {
                finish = true;
                Looper.prepare();
                openRDP();
                Toast.makeText(MainActivity2.this,"開機成功",Toast.LENGTH_LONG).show();
                handler.removeCallbacks(Timer);
                Looper.loop();
                pinger.cancel();
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

