package com.example.psyad9.runtracker;

import android.net.Uri;

public class Contract {

    //table names have been appended as final strings so that they can be used in various methods and if statements
    public static final String AUTHORITY = "com.example.psyad9.runtracker";
    public static final Uri RECORDS_URI = Uri.parse("content://"+AUTHORITY+"/records/");

    public static final String DATABASE_NAME = "records";
    public static final String DATABASE_RUN_NAME = "name";
    public static final String DATABASE_ID = "_id";
    public static final String DATABASE_DATE = "date";
    public static final String DATABASE_START_TIME = "time";
    public static final String DATABASE_LENGTH = "length";
    //public static final String STEPS = "steps";
    public static final String DATABASE_COORDS = "coords";
    public static final String DATABASE_DISTANCE = "distance";
    public static final String DATABASE_WEATHER = "weather";
    public static final String DATABASE_NOTES = "notes";
    public static final String DATABASE_RATING = "rating";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/recipes.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/recipes.data.text";
}
