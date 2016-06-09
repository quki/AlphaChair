package com.quki.alphachair.alphachairandroid.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.quki.alphachair.alphachairandroid.MainActivity;
import com.quki.alphachair.alphachairandroid.R;

/**
 * Created by quki on 2016-05-31.
 */
public class MainService extends Service implements Runnable {

    private Handler mHandler;
    private final IBinder mBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        setServiceProgressNotify();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(this);
        stopForeground(true);
    }

    @Override
    public void run() {
    }

    protected void setServiceProgressNotify() {
        PendingIntent invokeActivity =
                PendingIntent.getActivity(
                        this
                        , 0
                        , new Intent(this, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_main)
                .setContentTitle("Alpha Chair")
                .setContentText("자세 측정 중...")
                .setContentIntent(invokeActivity);
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.priority = Notification.PRIORITY_MAX;
        startForeground(1, mNotification);
    }



    public class MyBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }
}
