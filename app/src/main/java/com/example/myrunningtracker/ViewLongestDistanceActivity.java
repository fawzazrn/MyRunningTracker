package com.example.myrunningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ViewLongestDistanceActivity extends AppCompatActivity {

    private double distance = 0;
    private String session_id = null;
    private static final String TAG = "ViewLongestDistance";
    private String timestamp = null;
    private String name = null;
    private int time = 0;
    private String weather = null;
    private String notes = null;
    private boolean isView = false;

    // layout
    private TextView distanceView, timeView, dateView, durationView, saveView;
    private EditText noteText, sessionNameText;
    private Button deleteButton;
    Chronometer chronometer;
    List<String> weatherItems;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_session);

        // initialize layout
        distanceView = findViewById(R.id.distanceView);
        durationView = findViewById(R.id.durationView);
        noteText = findViewById(R.id.noteText);
        timeView = findViewById(R.id.timeView);
        dateView = findViewById(R.id.dateView);
        sessionNameText = findViewById(R.id.sessionNameText);
        spinner = findViewById(R.id.spinner);
        saveView = findViewById(R.id.saveView);
        deleteButton = findViewById(R.id.deleteButton);

        // get Intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        isView = bundle.getBoolean("isView");

        // checks if it comes from which activity
        if (isView == true) {

            // hides the SAVE button
            saveView.setVisibility(View.GONE);

        }

        getLongestDistance();
        getDataFromRow();

        // set notes
        noteText.setText(notes);
        sessionNameText.setText(name);

        // display duration in chronometer format
        chronometer = (Chronometer) findViewById(R.id.chronometer2);
        long t = SystemClock.elapsedRealtime() - time*1000;
        chronometer.setBase(t);

        // display data
        distanceView.setText(String.format("%.2f", distance) + " km");
        dateView.setText(timestamp);

        // initializes spinner
        weatherItems = new ArrayList<>();
        weatherItems.add("Sunny");
        weatherItems.add("Gloomy");
        weatherItems.add("Snowy");
        weatherItems.add("Rainy");
        initializeSpinner();

        Log.d(TAG, "Longest distance : " + distance);

    }

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

                Log.d(TAG, "distance : " + distance);
                // method to retrieve the best distance and its session id
                compareDistance(id, d);

            } while (cursor.moveToNext());

        }

    }

    public void compareDistance(String id, double d) {
        if (d > distance) {
            Log.d(TAG, "more");
            distance = d;
            session_id = id;
        } else {
            Log.d(TAG, "less");
        }
    }

    public void getDataFromRow() {

        String[] projection = new String[] {
                SessionContract._ID,
                SessionContract.TIMESTAMP,
                SessionContract.NAME,
                SessionContract.TIME,
                SessionContract.DISTANCE,
                SessionContract.WEATHER,
                SessionContract.NOTES
        };

        Cursor cursor = getContentResolver().query(SessionContract.SESSION_ID_URI, projection, session_id, null, null);

        if(cursor.moveToFirst()) {
            do {
                timestamp = cursor.getString(1);
                name = cursor.getString(2);
                time = cursor.getInt(3);
                distance = cursor.getDouble(4);
                weather = cursor.getString(5);
                notes = cursor.getString(6);
            } while (cursor.moveToNext());
        }

    }

    public void initializeSpinner() {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, weatherItems);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        // set spinner
        if (weather != null) {
            int pos = dataAdapter.getPosition(weather);
            spinner.setSelection(pos);
        }

    }

    // method that handles when the Delete button is clicked
    public void onDeleteClick(View view) {
        Log.d(TAG, "delete");
        int o = getContentResolver().delete(SessionContract.SESSION_URI, session_id, null);

        // set intent
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // create toast
        Toast toast = Toast.makeText(this, "Session deleted from records", Toast.LENGTH_LONG);
        toast.show();
    }


}
