package com.example.myrunningtracker;

import android.net.Uri;

import java.security.PublicKey;

public final class SessionContract {

    public static final String AUTHORITY = "com.example.myrunningtracker.SessionContentProvider";

    // initialize Uri
    public static final Uri SESSION_URI = Uri.parse("content://"+AUTHORITY+"/sessions");
    public static final Uri SESSION_ID_URI = Uri.parse("content://"+AUTHORITY+"/sessions/_id");
    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");

    public static final String _ID = "_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String NAME = "name";
    public static final String TIME = "time";
    public static final String DISTANCE = "distance";
    public static final String WEATHER = "weather";
    public static final String NOTES = "notes";


}
