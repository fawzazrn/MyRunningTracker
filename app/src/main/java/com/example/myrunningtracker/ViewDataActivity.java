package com.example.myrunningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

public class ViewDataActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private SimpleCursorAdapter cursorAdapter;
    private DatabaseHelper databaseHelper;
    private TableLayout items;
    private TableRow item_row;
    private ListView listView;
    private Handler handler;
    private SQLiteDatabase db;
    public static String TAG = "ViewDataActivity";
    private Information information;
    private Spinner spinner;
    private List<String> sortItems;
    private String sortItem = "Oldest";
    private static String oldest = "Oldest";
    private static String most_recent = "Most Recent";
    private static String elapsed_time = "Elapsed Time";
    private static String distance = "Distance";
    private static String sort_oldest = "timestamp ASC";
    private static String sort_newest = "timestamp DESC";
    private static String sort_time = "time DESC";
    private static String sort_distance = "distance DESC";
    private static String order = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        DatabaseHelper databaseHelper = new DatabaseHelper(this.getApplicationContext(), " ", null, 8);
        db = databaseHelper.getWritableDatabase();

        //intializes handler
        handler = new Handler();

        // initializes layout
        listView = findViewById(R.id.listView);
        items = findViewById(R.id.item);
        item_row = findViewById(R.id.item_row);
        spinner = findViewById(R.id.spinner2);

        // initializes sort items
        sortItems = new ArrayList<>();
        sortItems.add(most_recent);
        sortItems.add(oldest);
        sortItems.add(elapsed_time);
        sortItems.add(distance);
        initializeSpinner();

        // initializes table
        item_row = new TableRow(this);
        item_row.setClickable(true);

        // set spinner listener
        spinner.setOnItemSelectedListener(this);

        queryData();
        getContentResolver().registerContentObserver(SessionContract.ALL_URI, true, new ChangeObserver(handler));

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        sortItem = adapterView.getItemAtPosition(i).toString();

        if (sortItem.equals(oldest)) {
            order = sort_oldest;
            Log.d(TAG, "order : " + order);
            queryData();
        } else if (sortItem.equals(most_recent)) {
            order = sort_newest;
            Log.d(TAG, "order : " + order);
            queryData();
        } else if (sortItem.equals(elapsed_time)) {
            order = sort_time;
            Log.d(TAG, "order : " + order);
            queryData();
        } else {
            order = sort_distance;
            Log.d(TAG, "order : " + order);
            queryData();
        }

        Log.d(TAG, "item selected : " + sortItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    class ChangeObserver extends ContentObserver {

        public ChangeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            queryData();
        }

    }

    public void queryData() {

        String[] projection = new String[] {
            SessionContract._ID,
            SessionContract.TIMESTAMP,
            SessionContract.NAME,
            SessionContract.TIME,
            SessionContract.DISTANCE,
            SessionContract.WEATHER,
            SessionContract.NOTES
        };

        String colstoDisplay [] = new String[] {
            SessionContract._ID,
            SessionContract.TIMESTAMP,
            SessionContract.NAME,
            SessionContract.TIME,
            SessionContract.DISTANCE
            // SessionContract.NOTES
        };

        int[] colresIds = new int[] {
            R.id.id_text,
            R.id.timestamp_text,
            R.id.name_text,
            R.id.time_text,
            R.id.distance_text,
        };

        // cursor to display
        Cursor cursor = getContentResolver().query(SessionContract.SESSION_URI, projection, null, null, order);
        cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.item_layout,
                cursor,
                colstoDisplay,
                colresIds,
                0
        );

        // use listview to view the data adapter
        listView.setAdapter(cursorAdapter);

    }

    public void onSessionClick(View view) {

        String timestamp = null;
        String name = null;
        int time = 0;
        double distance = 0;
        String weather = null;
        String notes = null;

        // get data
        TableRow tableRow =(TableRow) view;

        TextView sel_id = (TextView) tableRow.getChildAt(0);

        // get session id from table
        String session_id = sel_id.getText().toString();
        Log.d(TAG, "selected table : " + session_id);

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

        if (cursor.moveToFirst()) {
            Log.d(TAG, "cursor working");
            do {
                {
                    timestamp = cursor.getString(1);
                    name = cursor.getString(2);
                    time = cursor.getInt(3);
                    distance = cursor.getDouble(4);
                    weather = cursor.getString(5);
                    notes = cursor.getString(6);
                }
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "name : " + name);
        Bundle bundle = new Bundle();
        bundle.putString("ID", session_id);
        bundle.putString("TIMESTAMP", timestamp);
        bundle.putString("NAME", name);
        bundle.putInt("ELAPSEDTIME", time);
        bundle.putDouble("DISTANCE", distance);
        bundle.putString("WEATHER", weather);
        bundle.putString("NOTES", notes);
        bundle.putBoolean("isView", true);

        Intent intent = new Intent(this, DisplaySessionActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    public void initializeSpinner() {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sortItems);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        // set spinner
        if (sortItem != null) {
            int pos = dataAdapter.getPosition(sortItem);
            spinner.setSelection(pos);
        }

    }


}
