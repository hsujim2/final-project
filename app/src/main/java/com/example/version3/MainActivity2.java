package com.example.version3;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.potterhsu.Pinger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {
    EditText ip_edt,mac_edt;
    Button conn_btn,cancel_btn;
    Boolean finish = false;
    public static Boolean tools;
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
            items.add("user:"+c.getString(0)+"\tIP:"+c.getString(1)+"\tmac:"+c.getString(2));
            c.moveToNext();
        }
        adapter.notifyDataSetChanged();
        //c.close();
        ll_progress.setVisibility(View.GONE);//取消進度條
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                new AlertDialog.Builder(MainActivity2.this)
                        .setTitle("要設定為小工具預設嗎？")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//按下positiveBtn更改預設值
                                SharedPreferences pref = getSharedPreferences("default_computer", MODE_PRIVATE);
                                pref.edit().putInt("def",index).apply();//這兩行在儲存永久變數
                            }
                        }).setNegativeButton("cancel",null).create().show();
                return true;
            }
        });
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
                        //判斷如果add時有將broadcast打勾的話就將這邊的broadcast打勾
                        bw.setChecked(c.getInt(3) == 1);
                    }
                    c.moveToNext();// 下一行
                }
                //因為上面的items有變動過，所以這邊要在更新一次items並放入adapter裡
                c.moveToFirst();
                items.clear();
                for(int i=0;i<c.getCount();i++){
                    items.add("user:"+c.getString(0)+"\tIP:"+c.getString(1)+"\tmac:"+c.getString(2));
                    c.moveToNext();
                }
                adapter.notifyDataSetChanged();
                c.close();
            }
        });
        bw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bw.isChecked())
                    startActivityForResult(new Intent(MainActivity2.this,custom.class),1);
            }
        });
        conn_btn.setOnClickListener(new View.OnClickListener() {//連線的button
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                ll_progress.setVisibility(View.VISIBLE);//顯示進度條
                String[] temp = ip_edt.getText().toString().split("\\.");//temp用於偵測IP是否符合要求
                String[] temp2 = mac_edt.getText().toString().split("-");//temp2用於偵測MAC是否符合要求
                String lip = ip_edt.getText().toString();//暫存IP位置
                String lmac = mac_edt.getText().toString();//暫存MAC位置
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
                        }else {
                            if(!getSharedPreferences("default_command", MODE_PRIVATE)
                                .getString("command", "none").equals("none")) //不更改廣播IP(在外網呼叫，廣播IP必須透過NAT轉發時設定)
                                ///////////////使用SSH下指令///////////////////
                                new AsyncTask<Integer, Void, Void>(){
                                    @Override
                                    protected Void doInBackground(Integer... integers) {
                                        try { executeRemoteCommand("root","zaq1@WSX",ip_edt.getText().toString(),
                                                Integer.parseInt(getSharedPreferences("default_command", MODE_PRIVATE).getString("port", "22")));
                                        } catch (Exception e) { e.printStackTrace(); }
                                        return null;
                                    }
                                }.execute(1);
                            ll_progress.setVisibility(View.GONE);
                            openRDP();//直接打開RDP，因為不能被ping到
                            Toast.makeText(MainActivity2.this, "ping為icmp，不支援port fording，直接開啟RDP", Toast.LENGTH_SHORT).show();
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
        if(tools != null){
            if(tools){
                c.moveToPosition(getSharedPreferences
                        ("default_computer",MODE_PRIVATE)
                        .getInt("def",0));
                tools = false;
                ip_edt.setText(c.getString(1));
                mac_edt.setText(c.getString(2));
                bw.setChecked(c.getInt(3) == 1);
                conn_btn.performClick();
            }
        }
        c.close();
    }
    //加入新資料後要做的事
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1) {
            if (resultCode == 101) {
                Cursor c=dbrw.rawQuery("SELECT * FROM myTable",null);
                c.moveToFirst();
                items.clear();
                for(int i=0;i<c.getCount();i++){
                    items.add("user:"+c.getString(0)+"\tIP:"+c.getString(1)+"\tmac:"+c.getString(2));
                    c.moveToNext();
                }
                adapter.notifyDataSetChanged();//將items寫入adapter
                c.close();//關閉查詢
            }
        }
    }
    public String executeRemoteCommand(String username, String password, String hostname, int port)throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);//新增連線
        session.setPassword(password);
        Properties prop = new Properties();//SSH連線
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);//基礎設定
        session.connect();//連線
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//存放指令的變數
        channelssh.setOutputStream(baos);
        channelssh.setCommand(getSharedPreferences("default_command", MODE_PRIVATE).getString("command", "none"));//開機指令
        channelssh.connect();//傳送開機指令
        channelssh.disconnect();//中斷連接
        return baos.toString();
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
                //ll_progress.setVisibility(View.GONE);//隱藏進度條
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
                    handler.removeCallbacks(Timer);//大於十次取消計時，取消連接測試
                }
                Looper.loop();
            }
            @Override
            public void onPingFinish() {}
        });
        pinger.pingUntilSucceeded(ip,5000);
    }
    protected void openRDP(){
        Intent activityIntent = new Intent();//pkg名稱可用軟體查，也可以順便查出他預設啟動的activity名稱
        activityIntent.setComponent(new ComponentName("com.microsoft.rdc.androidx","com.microsoft.rdc.ui.activities.HomeActivity" ));
        startActivity(activityIntent);
    }
}
class WakeThread extends Thread{
    String ip = null;
    String macAddr = null;
    public WakeThread(String ip,String macAddr){//設定初值
        this.ip = ip;
        this.macAddr = macAddr;
    }
    @Override
    public void run() {
        super.run();
        wakeOnLan(ip,macAddr);//呼叫自己
    }
    public void wakeOnLan(String ip,String macAddr){
        DatagramSocket datagramSocket = null;
        try {//magic packet有特殊規格，使用UDP port 進行廣播
            byte[] mac = getMacBytes(macAddr);
            byte[] magic = new byte[6+16*mac.length];//愈傳送的封包
            //1.寫入六個FF
            for (int i=0;i<6;i++){
                magic[i] = (byte)0xff;
            }
            //2.寫入host MAC位置，當電腦收到屬於自己的MAC，會發送開機訊號
            for(int i=6;i<magic.length; i += mac.length){
                System.arraycopy(mac,0,magic,i,mac.length);//複製輸入的MAC到愈發送的封包中
            }
            datagramSocket = new DatagramSocket();//新增封包
            DatagramPacket datagramPacket = new DatagramPacket(magic,magic.length, InetAddress.getByName(ip),8888);
            //傳送封包，以UDP 8888 port傳送，這個port可以亂設定，只要不要是常用的port不會衝突就好。(封包,封包長度,IP,port)
            datagramSocket.send(datagramPacket);
        } catch (SocketException e) {//各種例外情況
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(datagramSocket != null)
                datagramSocket.close();//傳送完成需要關閉
        }
    }
    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {//用於將mac位置拆開，從原本的字串拆成六組十六進制
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");//以-作為拆分符號，前面已經判斷過是不是合法的了，這邊不在重複判斷
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);//轉成16進制
            }
        }
        catch (NumberFormatException e) {//如果轉換十六進制失敗，代表內容不是十六進制，所以轉換失敗
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}

