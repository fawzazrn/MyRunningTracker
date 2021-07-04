package com.example.myrunningtracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class SessionContentProvider extends ContentProvider {

    private DatabaseHelper databaseHelper = null;
    public static final String TAG = "CONTENTPROVIDER";
    public static final UriMatcher uriMatcher;
    public static int DATABASE_VERSION = 8;
    public static String DATABASE_NAME = " ";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(SessionContract.AUTHORITY, "sessions", 1);
        uriMatcher.addURI(SessionContract.AUTHORITY, "sessions/_id", 2);
        uriMatcher.addURI(SessionContract.AUTHORITY, "*", 3);
    }

    @Override
    public boolean onCreate() {

        // initializes databasehelper class
        this.databaseHelper = new DatabaseHelper(this.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // instantiates database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case 1:
                Log.d(TAG, "case 1 uri");
                return db.query("sessions", projection, selection, selectionArgs, null, null, sortOrder);
            case 2:
                Log.d(TAG, "case 2 uri");
                selection = "_ID = " + selection;
                return db.query("sessions", projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String tableName;

        Log.d(TAG, "insert: " + uri.toString());

        switch (uriMatcher.match(uri)) {
            case 1:
                tableName = "sessions";
                break;
            default:
                tableName = "sessions";
                break;
        }

        long id = db.insert(tableName, null, contentValues);

        Log.d(TAG, "data inserted...");
        db.close();

        // return uri
        Uri nu = ContentUris.withAppendedId(uri, id);

        // insert code to notify changes
        return nu;

    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {

        Integer id = Integer.parseInt(s);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case 1:
            // delete from sessions table
                db.delete("sessions", "_id=" + id, null );
                Log.d(TAG, "session deleted");
                break;
            default:
                return 0;
        }

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
