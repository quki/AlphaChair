package com.quki.alphachair.alphachairandroid.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.quki.alphachair.alphachairandroid.MainActivity;
import com.quki.alphachair.alphachairandroid.R;

/**
 * Created by quki on 2016-05-31.
 */
public class MainService extends Service implements Runnable{

    private Handler mHandler;
    private String postureMsg;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        setServiceProgressNotify();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        postureMsg = intent.getStringExtra("msg");
        if(postureMsg!=null)
        mHandler.postDelayed(this, 1);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(this);
        stopForeground(true);
    }

    @Override
    public void run() {
        setPostureNotify(postureMsg);
    }
    protected void setServiceProgressNotify() {
        PendingIntent invokeActivity =
                PendingIntent.getActivity(
                        this
                        , 0
                        , new Intent(this, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Alpha Chair")
                .setContentText("자세 측정 중...")
                .setContentIntent(invokeActivity);
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.priority = Notification.PRIORITY_MAX;
        startForeground(1,mNotification);
    }
    protected void setPostureNotify(String msg){
        PendingIntent invokeActivity =
                PendingIntent.getActivity(
                        this
                        , 0
                        , new Intent(this, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("자세를 바르게 하세요!")
                .setContentText(msg)
                .setContentIntent(invokeActivity);
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(2, mNotification);
    }
}
