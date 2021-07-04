package com.example.myrunningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private Button startButton, viewDataButton;
    private double best_distance;
    private String session_id;
    private Information information;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startButton = findViewById(R.id.startButton);
        //viewDataButton = findViewById(R.id.viewDataButton);

        // ask for permission to access location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 10);
                return;
            }
            else {
                Log.d(TAG, "permission granted...");
            }
        }

        // method to save personal best distance
        getLongestDistance();

        // set best personal distance
        information = new Information();
        information.setPersonalBest(best_distance);
        Log.d(TAG, "best distance : " + best_distance);

    }

    // click this button to start the run session
    // after the button is clicked, service is started
    public void onStartRunButton(View view) {
        Intent intent = new Intent(MainActivity.this, RunActivity.class);
        startActivity(intent);

    }

    public void onViewDataButton(View view) {
        Intent intent = new Intent(MainActivity.this, ViewDataActivity.class);
        startActivity(intent);
    }

    public void onViewLongestDistanceClick(View view) {
        Intent intent = new Intent(MainActivity.this, ViewLongestDistanceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isView", true);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onGoalClick(View view) {
        Intent intent = new Intent(this, GoalSessionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "main activity ondestroy");
    }

    // gets personal best
    public void getLongestDistance() {

        String id;
        double d = 0;

        String[] projection = new String[] {
                SessionContract._ID,
                SessionContract.TIMESTAMP,
                SessionContract.NAME,
                SessionContract.TIME,
                SessionContract.DISTANCE,
                SessionContract.WEATHER,
                SessionContract.NOTES
        };

        Cursor cursor = getContentResolver().query(SessionContract.SESSION_URI, projection, null, null, null);

        // cursor traverses through the database
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getString(0);
                d = cursor.getDouble(4);

                Log.d(TAG, "distance : " + best_distance);
                // method to retrieve the best distance and its session id
                compareDistance(id, d);

            } while (cursor.moveToNext());

        }

    }

    public void compareDistance(String id, double d) {
        if (d > best_distance) {
            Log.d(TAG, "more");
            best_distance = d;
            session_id = id;
        } else {
            Log.d(TAG, "less");
        }
    }

}
