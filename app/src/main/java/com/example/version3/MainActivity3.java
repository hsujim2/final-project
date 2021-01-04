package com.example.version3;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {
    Button btn_add;
    EditText name_ed,ip_edt,mac_edt;
    CheckBox bw_cb;
    SQLiteDatabase dbrw;
    int ch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        btn_add=findViewById(R.id.btn_add);
        ip_edt = findViewById(R.id.ip_edt);
        mac_edt = findViewById(R.id.mac_edt);
        bw_cb = findViewById(R.id.bw_cb);
        name_ed = findViewById(R.id.name_ed);
        dbrw=new MyDBHelper(this).getWritableDatabase();

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] temp = ip_edt.getText().toString().split("\\.");
                String[] temp2 = mac_edt.getText().toString().split("-");
                if(temp.length == 4 && temp2.length == 6) {
                    int i = 0;
                    for (i = 0; i < 4; i++)
                        if (Integer.parseInt(temp[i]) >= 255)
                            break;
                    if (i != 4) {
                        Toast.makeText(MainActivity3.this, "請輸入正確的IP", Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (bw_cb.isChecked()) {
                            ch = 1;
                        } else {
                            ch = 0;
                        }
                        if (ip_edt.length() < 1 || mac_edt.length() < 1 || name_ed.length() < 1)
                            Toast.makeText(MainActivity3.this, "欄位請物留空", Toast.LENGTH_SHORT).show();
                        else {
                            try {
                                dbrw.execSQL("INSERT INTO myTable(user,IPAddr,mac,ch)VALUES(?,?,?,?)", new Object[]{name_ed.getText().toString(), ip_edt.getText().toString(), mac_edt.getText().toString(), ch});
                                Toast.makeText(MainActivity3.this, "User:" + name_ed.getText().toString() + "IP:" + ip_edt.getText().toString() + "Mac:" + mac_edt.getText().toString() + "   Broadcast:" + ch, Toast.LENGTH_SHORT).show();
                                name_ed.setText("");
                                ip_edt.setText("");
                                mac_edt.setText("");
                                bw_cb.setChecked(false);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity3.this, "新增失敗" + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                else{
                    Toast.makeText(MainActivity3.this,"請輸入正確的IP或MAC",Toast.LENGTH_LONG).show();
                }
                setResult(101);
                finish();

            }
        });

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbrw.close();
    }
}