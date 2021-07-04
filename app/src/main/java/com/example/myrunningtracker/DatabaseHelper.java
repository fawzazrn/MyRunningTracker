package com.example.myrunningtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String TAG = "DBHelper";
    private static final String TABLE_NAME_SESSION = "sessions";

    // create session table
    // columns : id, timestamp, name, time, distance, note
    public static String CREATE_TABLE_SESSION = "CREATE TABLE " + TABLE_NAME_SESSION + " ("
            + "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + " timestamp DATETIME,"
            + " name VARCHAR(128),"
            + " time INTEGER NOT NULL,"
            + " distance REAL NOT NULL,"
            + " weather VARCHAR(128),"
            + " notes VARCHAR(128)"
            + " )";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // create sessions table
        sqLiteDatabase.execSQL(CREATE_TABLE_SESSION);

        Log.d(TAG, "database created...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
