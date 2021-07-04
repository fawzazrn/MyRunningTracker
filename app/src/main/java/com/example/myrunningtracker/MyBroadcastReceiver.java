package com.example.myrunningtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MyBroadcastReceiver extends BroadcastReceiver {

    private NotificationManager notificationManager;
    private String TAG = "BroadcastReceiver";
    private String title = "Goal is achieved";
    private String NOTIFICATION_CHANNEL_ID = "channel_id";
    private String CHANNEL_NAME = "Goal is achieved";
    private int importance = NotificationManager.IMPORTANCE_DEFAULT;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "broadcast received");

        // once broadcast is received, make toast
        Toast toast = Toast.makeText(context, "Running goal is reached", Toast.LENGTH_SHORT);
        toast.show();

        // vibrate phone when broadcast is received
        Vibrator v;
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(3000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, importance);
            //Boolean value to set if lights are enabled for Notifications from this Channel
            notificationChannel.enableLights(true);
            //Boolean value to set if vibration are enabled for Notifications from this Channel
            notificationChannel.enableVibration(true);
            //Sets the color of Notification Light
            notificationChannel.setLightColor(Color.GREEN);
            //Set the vibration pattern for notifications. Pattern is in milliseconds with the format {delay,play,sleep,play,sleep...}
            notificationChannel.setVibrationPattern(new long[] {
                    500,
                    500,
                    500,
                    500,
                    500
            });
            //Sets whether notifications from these Channel should be visible on Lockscreen or not
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

    }






}
}
