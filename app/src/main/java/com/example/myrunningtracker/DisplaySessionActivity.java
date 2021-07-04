package com.example.myrunningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.se.omapi.Session;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DisplaySessionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int time;
    private double distance;
    private static final String TAG = "DisplaySessionActivity";
    private TextView distanceView, timeView, dateView, durationView, saveView;
    private EditText noteText, sessionNameText;
    private static final String DATABASE_NAME = " ";
    private static final int DATABASE_VERSION = 8;
    private DatabaseHelper databaseHelper = null;
    private ContentValues contentValues;
    private boolean isView = false;
    private Spinner spinner;
    private TextView personalBestView;
    private SQLiteDatabase db;
    private Button deleteButton;
    private String startTime, startDate, str_endTime, timeStamp, sessionName, weather = null, notes, session_id;
    private List<String> weatherItems;
    private SimpleDateFormat endTime;
    private Chronometer chronometer;
    private static String formattedDateTime;
    private Information information;
    private static double best_distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_session);

        Log.d(TAG, "Activity created");

        // initializes view
        distanceView = findViewById(R.id.distanceView);
        durationView = findViewById(R.id.durationView);
        noteText = findViewById(R.id.noteText);
        timeView = findViewById(R.id.timeView);
        dateView = findViewById(R.id.dateView);
        sessionNameText = findViewById(R.id.sessionNameText);
        spinner = findViewById(R.id.spinner);
        saveView = findViewById(R.id.saveView);
        deleteButton = findViewById(R.id.deleteButton);
        personalBestView = findViewById(R.id.personalbestView);

        // hide delete button
        deleteButton.setVisibility(View.GONE);

        // get data from RunActivity
        Intent intent = getIntent();

        // bundle to retrieve the data
        Bundle bundle = intent.getExtras();

        // Extracting the data
        session_id = bundle.getString("ID");
        sessionName = bundle.getString("NAME");
        time = bundle.getInt("ELAPSEDTIME");
        distance = bundle.getDouble("DISTANCE");
        startTime = bundle.getString("STARTTIME");
        startDate = bundle.getString("STARTDATE");
        timeStamp = bundle.getString("TIMESTAMP");
        weather = bundle.getString("WEATHER");
        notes = bundle.getString("NOTES");
        isView = bundle.getBoolean("isView");

        // checks if it comes from ViewDataActivity or RunActivity / GoalRunActivity
        if (isView == true) {

            // hides the SAVE button
            saveView.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);
            personalBestView.setVisibility(View.GONE);

        }

        // set personal best
        information = new Information();
        best_distance = information.getPersonalBest();

        // compares current total distance with best distance
        compareWithPersonalBest();

        // set notes
        noteText.setText(notes);
        sessionNameText.setText(sessionName);

        // convert distance to kilometre
        String d = String.format("%.2f", distance);
        distance = Double.parseDouble(d);

        // get end time
        endTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        str_endTime = endTime.format(new Date());

        // display total time in chronometer format
        chronometer = (Chronometer) findViewById(R.id.chronometer2);
        long t = SystemClock.elapsedRealtime() - time*1000;
        chronometer.setBase(t);

        // display data
        distanceView.setText(String.format("%.2f", distance) + " km");
        dateView.setText(timeStamp);
        // timeView.setText(startTime);

        // initializes spinner
        weatherItems = new ArrayList<>();
        weatherItems.add("Sunny");
        weatherItems.add("Gloomy");
        weatherItems.add("Snowy");
        weatherItems.add("Rainy");
        initializeSpinner();

        // set spinner listener
        spinner.setOnItemSelectedListener(this);

        // initializes database helper
        databaseHelper = new DatabaseHelper(this.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        db = databaseHelper.getWritableDatabase();

    }

    // method that handles when the SAVE button is clicked
    public void onDoneButtonClick(View view) {

        // add contentValues
        setSessionData();

        // insert session data
        Uri uri = getContentResolver().insert(SessionContract.SESSION_URI, contentValues);
        Log.d(TAG, "id : " + uri.getLastPathSegment());

        // Toast
        Toast toast = Toast.makeText(this, "Good job!", Toast.LENGTH_SHORT);
        toast.show();

        Intent intent = new Intent(DisplaySessionActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // listener for AdapterView
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // when item is selected
        weather = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "item selected : " + weather);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public void onBackPressed() {

        // data discarded and goes back to MainActivity
        if (isView == false) {
            Toast toast = Toast.makeText(this, "Data is discarded", Toast.LENGTH_LONG);
            toast.show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // method that gets the data from View class and put them into ContentValues
    public void setSessionData() {

        // get sessionName and notes from EditText
        sessionName = sessionNameText.getText().toString();
        notes = noteText.getText().toString();

        /*
            TIMESTAMP,
            NAME,
            DURATION,
            DISTANCE,
            NOTES
         */
        contentValues = new ContentValues();
        contentValues.put(SessionContract.TIMESTAMP, timeStamp);
        contentValues.put(SessionContract.NAME, sessionName);
        contentValues.put(SessionContract.TIME, time);
        contentValues.put(SessionContract.DISTANCE, distance);
        contentValues.put(SessionContract.WEATHER, weather);
        contentValues.put(SessionContract.NOTES, notes);

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

    public void compareWithPersonalBest() {

        if (distance > best_distance) {
            personalBestView.setText("You have beaten your personal best!");
        } else {
            personalBestView.setText("Best : " + best_distance + " km");
        }

    }

}
