package com.example.version3;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import static androidx.core.content.ContextCompat.startActivity;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {
    private static final String MyOnClick1 = "myOnClickTag1";//監聽事件用的
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        MainActivity2.tools = Boolean.FALSE;//區分是否按下widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setOnClickPendingIntent(R.id.button, getPendingSelfIntent(context, MyOnClick1));//設定onclick
        //小工具不能使用在主APP中綁定的方式，只能這樣設定
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, NewAppWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.button,pendingIntent);//設定onclick
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(MyOnClick1.equals(intent.getAction())){//判斷是不是指定物件被按下
            MainActivity2.tools = true;
            Intent mailClient = new Intent(Intent.ACTION_VIEW);
            mailClient.setClassName("com.example.version3", "com.example.version3.MainActivity2");
            mailClient.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mailClient);//開啟APP
        }
    }
}