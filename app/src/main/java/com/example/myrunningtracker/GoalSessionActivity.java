package com.example.myrunningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class GoalSessionActivity extends AppCompatActivity {

    private EditText hourText, minuteText;
    private String hourStr=null, minStr=null;
    private long hour=0, minute=0;
    private long convertedTime;
    private static final String TAG = "GoalSession";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_session);

        // initialize layout
        hourText = findViewById(R.id.hourEditText);
        minuteText = findViewById(R.id.minuteEditText);

    }

    public void onProceedClick(View view) {

        // retrieve data from edittext
        hourStr = hourText.getText().toString();
        minStr = minuteText.getText().toString();
        Log.d(TAG, "test : " + hourStr);

        // error handling
        if (hourStr.equals("")) {
            hourStr = "0";
        }

        if (minStr.equals("")) {
            minStr = "0";
        }

        parseLong();

        // convert to millisecond
        convertedTime = convertTime();

        // checks if converted time is 0
        if (convertedTime == 0) {
            Log.d(TAG, "converted time : " + 0);
            Toast toast = Toast.makeText(this, "Please insert time", Toast.LENGTH_LONG);
            toast.show();
        } else {

            // bundle to pass to the next activity
            Bundle bundle = new Bundle();
            bundle.putLong("GOAL", convertedTime);

            // intent to GoalRunActivity
            Intent intent = new Intent(this, GoalRunActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }

    public long convertTime() {

        long millisecond = 0;

        millisecond = TimeUnit.HOURS.toMillis(hour);
        millisecond = millisecond + TimeUnit.MINUTES.toMillis(minute);
        Log.d(TAG, "converted time : " + millisecond);

        return millisecond;

    }

    public void parseLong() {
        // convert to double
        hour = Long.parseLong(hourStr);
        minute = Long.parseLong(minStr);
    }

}
