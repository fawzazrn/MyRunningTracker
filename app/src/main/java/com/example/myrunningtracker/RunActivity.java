package com.example.myrunningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RunActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String TAG = "RunActivity";
    private MyService.MyBinder myservice = null;
    private double distance;
    private double sessionTotalDistance;
    private TextView distanceView, stopText, pausestartText, pBestView;
    private Button stopButton;
    private RunSessionThread runSessionThread;
    private Chronometer chronometer;
    private int seconds;
    private SimpleDateFormat startTime, startDate, timeStamp;
    private String str_startTime, str_startDate, str_timeStamp;
    private boolean isPause = false;
    private long timeStopped;
    private Information info;
    private static double best_distance;

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onService connected...");

            // pass a reference of the Ibinder to the service component
            myservice = (MyService.MyBinder) iBinder;

            // start timer
            chronometer.start();

            // get starting time
            startTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            startDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // convert to string
            str_startTime = startTime.format(new Date());
            str_startDate = startDate.format(new Date());
            str_timeStamp = timeStamp.format(new Date());
            Log.d(TAG, "time : " + str_startTime);
            Log.d(TAG, "date : " + str_startDate);
            Log.d(TAG, "timestamp : " + str_timeStamp);

            // get location updates
            getLocation();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onService disconnected...");
            serviceConnection = null;
            myservice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        // set activity id
        info = new Information();
        info.setActivityId(1);
        best_distance = info.getPersonalBest();

        // initialize textview
        distanceView = findViewById(R.id.timeView);
        chronometer = (Chronometer) findViewById(R.id.chronometer2);
        stopText = findViewById(R.id.stopText);
        pausestartText = findViewById(R.id.pausestartText);
        pBestView = findViewById(R.id.pBestView);

        // set personal best
        pBestView.setText("Best : " + best_distance + " km");

        // start service
        Intent intent = new Intent(RunActivity.this, MyService.class);
        intent.putExtra("ACTIVITY", 1);
        startService(intent);

        // bind service
        bindService(new Intent(RunActivity.this, MyService.class), serviceConnection, Context.BIND_AUTO_CREATE);


    }

    // methods to get current location and distance
    public void getLocation() {

        // get location updates at certain intervals
        myservice.updateLocation();

        // start thread
        runSessionThread = new RunSessionThread();
        runSessionThread.running = true;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // stop thread
        runSessionThread.stopThread();

        if(serviceConnection!=null) {
            // unbind service
            unbindService(serviceConnection);
            this.stopService(new Intent(RunActivity.this, MyService.class));
            serviceConnection = null;
            Log.d(TAG, "service not null");
        } else {
            Log.d(TAG, "service null");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    // when Back button is pressed, do not destroy the activity
    @Override
    public void onBackPressed() {
        Log.d(TAG, "back button pressed");
        moveTaskToBack(true);
    }

    // when the home button is pressed
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    public void onStopButtonClick(View view) {

        // stop thread
        runSessionThread.stopThread();

        // stop requesting location updates
        myservice.stopLocationUpdates();

        // saves the elapsed time
        seconds = (int) (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
        Log.d(TAG, "time = " + seconds);

        // saves the session's total distance
        sessionTotalDistance = distance;
        Log.d(TAG, "total distance = " + sessionTotalDistance);

        // save into database / put into bundle


        // unbind and stop service
        unbindService(serviceConnection);
        this.stopService(new Intent(RunActivity.this, MyService.class));
        serviceConnection = null;

        // use Bundle to send data to the next Activity
        Bundle bundle = new Bundle();
        bundle.putInt("ELAPSEDTIME", seconds);
        bundle.putDouble("DISTANCE", sessionTotalDistance);
        bundle.putString("STARTTIME", str_startTime);
        bundle.putString("STARTDATE", str_startDate);
        bundle.putString("TIMESTAMP", str_timeStamp);

        // intent to DisplaySessionActivity
        Intent intent = new Intent(this, DisplaySessionActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // class to start a thread to get the distance
    public class RunSessionThread extends Thread implements Runnable {

        private boolean running = false;

        public RunSessionThread() {
            this.start();
        }

        // method to stop the thread
        public void stopThread() {
            this.interrupt();
            Log.d(TAG, "thread is interrupted");
        }

        public void run() {
            while(this.running) {
                try {
                    Thread.sleep(10);
                }
                catch (Exception e) {
                    return;
                }

                // runs the thread on the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // set the text to the value of the progress
                        distance = myservice.getTotalDistance();
                       Log.d(TAG, "thread: distance = " + distance);
                       distanceView.setText(String.format("%.2f", distance) + " km");

                       // when PAUSE button is clicked, stop the thread
                       if (isPause == true) {
                           Log.d(TAG, "pause");
                           stopThread();
                       }

                    }
                });

            }
        }
    }

    // method to handle pausing of running session
    public void onPauseClick(View view) {

        if (isPause == true) {
            isPause = false;
            pausestartText.setText("Pause");

            // keep the value of the chronometer the same
            chronometer.setBase(SystemClock.elapsedRealtime() + timeStopped);
            chronometer.start();

            // start thread
            runSessionThread = new RunSessionThread();
            runSessionThread.running = true;

        } else {

            // when user wants to pause
            isPause = true;
            pausestartText.setText("Resume");

            // saves the time at the moment when the session is on pause
            timeStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
        }




    }

}



