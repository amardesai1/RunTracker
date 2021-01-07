package com.example.psyad9.runtracker;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import static com.example.psyad9.runtracker.Contract.*;

public class DBHelper extends SQLiteOpenHelper {


    //these strings store SQL statements for the creation of all three tables
    private static final String SQL_CREATE_TABLE = "CREATE TABLE records " +
            "(_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,name VARCHAR(128) NOT NULL,date VARCHAR(128) NOT NULL," +
            "time VARCHAR(128) NOT NULL, length VARCHAR(128) NOT NULL, coords VARCHAR(128) NOT NULL," +
            //"steps IMTEGER,"+
            "distance VARCHAR(128) NOT NULL,weather VARCHAR(128) NOT NULL,notes VARCHAR(128) NOT NULL," +
            "rating INTEGER);";

    public static final int DATABASE_VERSION = 1;


    //Used to create instance of the DBHelper class for the content provider to access
    public DBHelper(@Nullable Context context) {
        super(context, "records", null, DATABASE_VERSION);
    }

    @Override
    //This method creates all tables using SQL on first launch of the app by calling the SQL strings
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }


    //This method handles upgrade of the database if the app is modified
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);
        onCreate(db);
    }
}