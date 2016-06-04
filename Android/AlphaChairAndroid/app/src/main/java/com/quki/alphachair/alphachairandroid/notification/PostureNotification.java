package com.quki.alphachair.alphachairandroid.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.quki.alphachair.alphachairandroid.MainActivity;
import com.quki.alphachair.alphachairandroid.R;

/**
 * Created by quki on 2016-06-04.
 */
public class PostureNotification {

    private Context mContext;

    public PostureNotification(Context mContext){
        this.mContext = mContext;
    }

    public void setPostureNotify(String msgFromArduino) {

        String  msg  = translateMsg(msgFromArduino);

        PendingIntent invokeActivity =
                PendingIntent.getActivity(
                        mContext
                        , 0
                        , new Intent(mContext, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("자세를 바르게 하세요!")
                .setContentText(msg)
                .setContentIntent(invokeActivity);
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(2, mNotification);
    }

    private String translateMsg(String msgFromArduino){
        StringBuffer msg = new StringBuffer();

        switch (msgFromArduino){
            case "fr" : {
                msg.append("앞쪽의 오른쪽다리");
                break;
            }
            case "fl" : {
                msg.append("앞쪽의 왼쪽다리");
                break;
            }
            case "br" : {
                msg.append("엉덩이의 오른쪽");
                break;
            }
            case "bl" : {
                msg.append("엉덩이의 왼쪽");
                break;
            }
        }

        msg.append("\n바르게하세요");

        return msg.toString();
    }
}
