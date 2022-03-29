package cn.edu.nottingham.hnyzx3.mp3player.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import cn.edu.nottingham.hnyzx3.mp3player.R;
import cn.edu.nottingham.hnyzx3.mp3player.pages.app.App;

public class Notification {

    private static final String NOTIFICATION_CHANNEL_ID = "cn.edu.nottingham.hnyzx3.mp3player";

    private static final int NOTIFICATION_ID = 1;

    private static final String CHANNEL_NAME = "MP3Player";

    private final Service context;

    private final NotificationManager manager;

    public Notification(Service context) {
        this.context = context;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.manager.createNotificationChannel(channel);
    }

    /**
     * notify a notification with a title and a content
     * will replace the previous notification
     */
    public void createNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(PendingIntent.getActivity(this.context, 0, new Intent(this.context, App.class), 0));


        manager.notify(NOTIFICATION_ID, builder.build());
        context.startForeground(NOTIFICATION_ID, builder.getNotification());
    }

    /**
     * clear all the notifications
     */
    public void clearNotification() {
        manager.cancel(NOTIFICATION_ID);
    }
}
