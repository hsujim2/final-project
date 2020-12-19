package com.example.wolexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.potterhsu.Pinger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    EditText ip_edt,mac_edt;
    EditText macAddr = null;
    Button conn_btn,save_btn,cancel_btn;
    Boolean finish = false;
    CheckBox bw;
    Handler handler = new Handler();
    Integer times = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conn_btn = findViewById(R.id.conn_btn);
        ip_edt = findViewById(R.id.ip_edt);
        mac_edt = findViewById(R.id.mac_edt);
        bw = findViewById(R.id.checkBox);
        handler.removeCallbacks(Timer);
        cancel_btn = findViewById(R.id.cancel_btn);

        conn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"send wake package",Toast.LENGTH_SHORT).show();
                //conn_btn.setEnabled(false);
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
                        Toast.makeText(MainActivity.this,"開始傳送magic packet",Toast.LENGTH_SHORT).show();
                        if(bw.isChecked())
                            lip = temp[0] + "." +temp[1]+"."+temp[2]+".255";//改為廣播IP
                        new WakeThread(lip,lmac).start();
                        if(bw.isChecked()) {
                            ifOpened(ip_edt.getText().toString());
                            handler.postDelayed(Timer, 5000);
                        }else{
                            openRDP();
                            Toast.makeText(MainActivity.this,"ping為icmp，不支援port fording，直接開啟RDP",Toast.LENGTH_SHORT).show();
                        }
                    }else
                        Toast.makeText(MainActivity.this,"請輸入正確的IP",Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(MainActivity.this,"請輸入正確的IP或MAC",Toast.LENGTH_LONG).show();
            }

//                String[] temp = ip_edt.getText().toString().split("\\.");
//                String lip = temp[0] + "." +temp[1]+"."+temp[2]+".255";
//                String lmac = mac_edt.getText().toString();
//                if (lip == null){
//                    ip_edt.setText("please input ip!");
//                }
//                Log.d("hello",lip);
//                if(lmac == null){
//                    macAddr.setText("please input mac!");
//                }
//                Log.d("hello",lmacAddr);
//                if(lip != null && lmacAddr != null){
//                    new WakeThread(lip,lmacAddr).start();
//                }

//                Pinger pinger = new Pinger();
//                pinger.setOnPingListener(new Pinger.OnPingListener() {
//                    @Override
//                    public void onPingSuccess() {
//                        finish = true;
//                        Looper.prepare();
//                        Intent activityIntent = new Intent();
//                        activityIntent.setComponent(new ComponentName("com.microsoft.rdc.androidx","com.microsoft.rdc.ui.activities.HomeActivity" ));
//                        startActivity(activityIntent);
//                        Toast.makeText(MainActivity.this,"開機成功",Toast.LENGTH_LONG).show();
//                        Looper.loop();
//                        pinger.cancel();
//                    }
//                    @Override
//                    public void onPingFailure() {
//                        Looper.prepare();
//                        Toast.makeText(MainActivity.this,"等待開機",Toast.LENGTH_LONG).show();
//                        Looper.loop();
//                    }
//                    @Override
//                    public void onPingFinish() {
//
//                    }
//                });
//                int i = 0;
//                for(i =0;i<10;i++){
//                    if(!finish)
//                        pinger.pingUntilSucceeded(ip_edt.getText().toString(),5000);
//                    try{ Thread.sleep(5000); }catch (InterruptedException e) {}
//                }
//                if(i == 10){
//                    Toast.makeText(MainActivity.this,"開機失敗，請檢察IP位置或在試一次",Toast.LENGTH_LONG).show();
//                }
//                conn_btn.setEnabled(true);

//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        boolean exists = false;
//                        try {
//                            SocketAddress sockaddr = new InetSocketAddress("192.168.10.16", 80);
//                            // Create an unbound socket
//                            Socket sock = new Socket();
//
//                            // This method will block no more than timeoutMs.
//                            // If the timeout occurs, SocketTimeoutException is thrown.
//                            int timeoutMs = 2000;   // 2 seconds
//                            sock.connect(sockaddr, timeoutMs);
//                            Looper.prepare();
//                            Toast.makeText(MainActivity.this,"開機成功",Toast.LENGTH_LONG).show();
//                            Looper.loop();
//                        } catch(IOException e) {
//                            Looper.prepare();
//                            Toast.makeText(MainActivity.this,"開機失敗",Toast.LENGTH_LONG).show();
//                            Looper.loop();
//                        }
//                    }
//                });
//                thread.start();
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            final Socket socket = new Socket();
//                            final InetAddress inetAddress = InetAddress.getByName("192.168.10.102");
//                            final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress,23);
//                            socket.connect(inetSocketAddress,1000);
//                            Looper.prepare();
//                            Toast.makeText(getBaseContext(),"開機成功",Toast.LENGTH_LONG).show();
//                            Looper.loop();
//                        } catch (UnknownHostException e) {
//                            Looper.prepare();
//                            Toast.makeText(getBaseContext(),"開機失敗",Toast.LENGTH_LONG).show();
//                            Looper.loop();
//                        } catch (IOException e) {
//                            Looper.prepare();
//                            Toast.makeText(getBaseContext(),"開機失敗",Toast.LENGTH_LONG).show();
//                            Looper.loop();
//                        }
//                    }
//                });
//                thread.start();
            //}

        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(Timer);
            }
        });
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
                Toast.makeText(MainActivity.this,"開機成功",Toast.LENGTH_LONG).show();
                handler.removeCallbacks(Timer);
                Looper.loop();
                pinger.cancel();
            }
            @Override
            public void onPingFailure() {
                Looper.prepare();
                Toast.makeText(MainActivity.this,"等待開機",Toast.LENGTH_SHORT).show();
                times ++;
                if(times >= 10){
                    handler.removeCallbacks(Timer);
                }
                Looper.loop();
            }
            @Override
            public void onPingFinish() {}
        });

//        int i = 0;
//        for(i =0;i<10;i++){
//            if(!finish)
        pinger.pingUntilSucceeded(ip,5000);
            //try{ Thread.sleep(5000); }catch (InterruptedException e) {}
//        }
//        if(i == 10){
//            Toast.makeText(MainActivity.this,"開機失敗，請檢察IP位置或在試一次",Toast.LENGTH_LONG).show();
//        }
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


//class mypinger extends Pinger{
//    private Boolean finish = false;
//    public boolean start(String ip){
//        for(int i =0;i<5;i++) {
//            pingUntilSucceeded(ip, 5000);
//            if (finish) {
//
//                break;
//            } else {
//                Toast.makeText(MainActivity.this,"等待開機，第"+ i +"次",Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    @Override
//    public void
//
//    public void setFinish(Boolean f){
//        if(finish){
//
//        }else
//            finish = f;
//    }
//}

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

