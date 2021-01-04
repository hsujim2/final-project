package com.example.version3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper { //繼承SQL類別

    private static final String name="mdatabase2.db"; //資料庫名稱
    private static final int version=1; //資料庫版本
    MyDBHelper(Context context){
        super(context,name,null,version);
    } //自訂建構子
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE myTable(user text PRIMARY KEY,IPAddr text NOT NULL,mac text NOT NULL,ch int NOT NULL)");
    }//建立SQL資料庫，有4個屬性，其中user是主鍵不可重複
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion){
        db.execSQL("DROP TABLE IF EXISTS myTable");
        onCreate(db);
    }//如果有新的SQL資料庫就將就得刪掉並呼叫onCreate在創建新的一個資料庫
}
