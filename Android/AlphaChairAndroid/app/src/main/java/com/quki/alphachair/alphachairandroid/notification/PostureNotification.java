package com.quki.alphachair.alphachairandroid.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.quki.alphachair.alphachairandroid.MainActivity;
import com.quki.alphachair.alphachairandroid.R;

/**
 * Created by quki on 2016-06-04.
 */
public class PostureNotification {

    private Context mContext;
    private Vibrator mVibrator;

    public PostureNotification(Context mContext){
        this.mContext = mContext;
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setPostureNotify(String msgFromArduino) {

        PendingIntent invokeActivity =
                PendingIntent.getActivity(
                        mContext
                        , 0
                        , new Intent(mContext, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_main)
                .setContentTitle("자세를 바르게 하세요!")
                .setContentText(msgFromArduino)
                .setAutoCancel(true)
                .setContentIntent(invokeActivity);
        Notification mNotification = mBuilder.build();
        //mNotification.flags = Notification.FLAG_NO_CLEAR;
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(2, mNotification);
        mVibrator.vibrate(1500);
    }
}
