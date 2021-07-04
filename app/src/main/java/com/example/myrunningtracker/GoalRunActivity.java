package com.example.myrunningtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GoalRunActivity extends AppCompatActivity {

    private static final String TAG = "GoalRunActivity";
    private Long goal;
    private Chronometer chronometer, chronometer3;
    private SimpleDateFormat timeStamp;
    private String str_timeStamp;
    private MyService.MyBinder myservice = null;
    private RunSessionThread runSessionThread;
    private TextView distanceView, pausestartRunText, stopRunText;
    private double distance;
    private boolean isPause = false;
    private long elapsedTime = 0;
    private ProgressBar progressBar;
    private long timeStopped;
    private int seconds;
    private double sessionTotalDistance;
    private ConstraintLayout constraintLayout;
    private Information info;
    private boolean i = false;

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onService connected...");

            // pass a reference of the Ibinder to the service component
            myservice = (MyService.MyBinder) iBinder;

            // start timer
            chronometer3.start();

            // get starting time
            timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // convert to string
            str_timeStamp = timeStamp.format(new Date());
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
        setContentView(R.layout.activity_goal_run);

        // set the activity id to inform service that this activity is bound to the service
        info = new Information();
        info.setActivityId(0);

        // initialize layout
        distanceView = findViewById(R.id.distanceGoalRun);
        pausestartRunText = findViewById(R.id.pausestartRunText);
        stopRunText = findViewById(R.id.stopRunText);
        constraintLayout = findViewById(R.id.background);

        // get bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        goal = bundle.getLong("GOAL");
        Log.d(TAG, "goal time : " + goal);

        chronometer = (Chronometer) findViewById(R.id.goalTimer);
        chronometer3 = (Chronometer) findViewById(R.id.chronometer3);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);

        // set chronometer
        initializeGoalTimer();

        // start service
        startService(new Intent(this, MyService.class));

        // bind service
        bindService(new Intent(this, MyService.class), serviceConnection, Context.BIND_AUTO_CREATE);

    }

    public void initializeGoalTimer() {
        long session_goal = SystemClock.elapsedRealtime() - goal;
        chronometer.setBase(session_goal);
    }

    public void getLocation() {

        // get location updates at certain intervals
        myservice.updateLocation();

        // start thread
        runSessionThread = new GoalRunActivity.RunSessionThread();
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
            this.stopService(new Intent(this, MyService.class));
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



    // class to start a thread to get the distance
    public class RunSessionThread extends Thread implements Runnable {

        private boolean running = false;

        public RunSessionThread() {
            this.start();
        }

        public void stopThread() {
            running = false;
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

                // checks time and boolean i
                // this is in order to avoid sending the broadcast in a loop
                if (elapsedTime >= goal && i==false) {

                    constraintLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gradient2));
                    // send a broadcast message, explicitly send to MyBroadcastReceiver
                    Intent intent = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
                    sendBroadcast(intent);

                    // set i to true
                    i = true;
                }

                // runs the thread on the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // set the text to the value of the progress

                        String test = String.valueOf(goal);
                        Log.d(TAG, "test1 : " + test);

                        double test2 = Double.parseDouble(test);
                        Log.d(TAG, "test2 : " + test2);

                        distance = myservice.getTotalDistance();
                        Log.d(TAG, "thread: distance = " + distance);
                        distanceView.setText(String.format("%.2f", distance) + " km");

                        // get elapsed time
                        elapsedTime = SystemClock.elapsedRealtime() - chronometer3.getBase();

                        String test3 = String.valueOf(elapsedTime);
                        Log.d(TAG, "test3 : " + test3);

                        double test4 = Double.parseDouble(test3);
                        Log.d(TAG, "test4 : " + test4);

                        double percentage = (test4/test2)*100;
                        Log.d(TAG, "percentage : " + percentage);
                        progressBar.setProgress((int) percentage);
                        Log.d(TAG, "elapsed time : " + elapsedTime);

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
    public void onPauseGoalClick(View view) {

        if (isPause == true) {
            isPause = false;
            pausestartRunText.setText("Pause");

            chronometer3.setBase(SystemClock.elapsedRealtime() + timeStopped);
            chronometer3.start();

            // start thread
            runSessionThread = new RunSessionThread();
            runSessionThread.running = true;

        } else {

            // when user wants to pause
            isPause = true;
            pausestartRunText.setText("Resume");

            timeStopped = chronometer3.getBase() - SystemClock.elapsedRealtime();
            chronometer3.stop();
        }
    }

    // method to handle stop button
    public void onStopGoalButtonClick(View view) {

        // stop thread
        runSessionThread.stopThread();

        // stop requesting location updates
        myservice.stopLocationUpdates();

        // saves the elapsed time
        seconds = (int) (SystemClock.elapsedRealtime() - chronometer3.getBase()) / 1000;
        Log.d(TAG, "time = " + seconds);

        // saves the session's total distance
        sessionTotalDistance = distance;
        Log.d(TAG, "total distance = " + sessionTotalDistance);

        // save into database / put into bundle


        // unbind and stop service
        unbindService(serviceConnection);
        this.stopService(new Intent(this, MyService.class));
        serviceConnection = null;

        // use Bundle to send data to the next Activity
        Bundle bundle = new Bundle();
        bundle.putInt("ELAPSEDTIME", seconds);
        bundle.putDouble("DISTANCE", sessionTotalDistance);
        bundle.putString("TIMESTAMP", str_timeStamp);

        // intent to DisplaySessionActivity
        Intent intent = new Intent(this, DisplaySessionActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
