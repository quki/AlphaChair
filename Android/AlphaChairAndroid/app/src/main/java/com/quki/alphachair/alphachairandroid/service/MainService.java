package com.quki.alphachair.alphachairandroid.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        setServiceNotification();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
        mHandler.removeCallbacks(this); // stop run()
        stopForeground(true);
    }

    @Override
    public void run() {
        /*Toast.makeText(getApplicationContext(),count+"",Toast.LENGTH_SHORT).show();
        count++;
        mHandler.postDelayed(this, 1000); // call run(), count mode on*/
    }
    protected void setServiceNotification() {
        PendingIntent invokeActivity =
                PendingIntent.getActivity(
                        this
                        , 0
                        , new Intent(this, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Service on")
                .setContentText("Hello Service")
                .setContentIntent(invokeActivity);
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.priority = Notification.PRIORITY_MAX;
        startForeground(1,mNotification);
    }
}
