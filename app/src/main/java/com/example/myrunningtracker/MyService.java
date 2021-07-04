package com.example.myrunningtracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.icu.text.IDNA;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    private LocationListener locationListener;
    private final IBinder binder = new MyBinder();
    private String TAG = "MyService";
    private Location currentLocation, newLocation;
    private final String CHANNEL_ID = "100";
    private int NOTIFICATION_ID = 001;
    private static int activity_id;
    private LocationManager locationManager;
    private double currentLatitude;
    private double currentLongitude;
    private double distance;
    private double totalDistance = 0;
    private float d;
    private Information info;

    // Binder class
    class MyBinder extends Binder implements IInterface {

        @Override
        public IBinder asBinder() {
            return this;
        }

        public void updateLocation() { MyService.this.updateLocation(); }

        public double getTotalDistance() {
            double distance = MyService.this.getTotalDistance();
            return distance;
        }

        public void stopLocationUpdates() { MyService.this.stopLocationUpdates(); }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {

        // get activity id
        info = new Information();
        activity_id = info.getActivityId();

        Log.d(TAG, "service created");

        // initialize location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Builds the service notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        // creates pendingIntent when user clicks on the service
        // the intent will bring the user to the activity the service is bound to
        Intent intent;

        // this checks which activity the service is bound to in order to handle with the Notification service
        if(activity_id == 1) {
            intent = new Intent(this, RunActivity.class);
        } else {
            intent = new Intent(this, GoalRunActivity.class);
        }

        //intent.putExtra("From", "Notification");
        //intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Session")
                .setContentText("Active")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        startForeground(NOTIFICATION_ID, mBuilder.build());

        // listens to location
        // is only initiated after requestLocationUpdate
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                // resets d to 0
                d = 0;

                currentLocation = newLocation;
                newLocation = location;

                // calculates the distance of current coordinate from the previous one
                if (currentLocation != null) {
                    d = location.distanceTo(currentLocation);
                }

                // log distance
                Log.d(TAG, "distance = " + d + "s");

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // activity id is used to make a reference to the entry point (RunActivity / GoalRunActivity)
        Log.d(TAG, "activity id : " + activity_id);
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    // request for location updates
    public void updateLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5
                ,locationListener);
    }

    public double getTotalDistance() {
        totalDistance = totalDistance + d;
        d = 0;
        return totalDistance/1000;
    }

    public void setDistance(double totalDistance) {

        this.totalDistance = totalDistance;
    }

    // method to stop location update after user stops service
    public void stopLocationUpdates() {

        // stop requesting location updates
        locationManager.removeUpdates(locationListener);
    }

}
