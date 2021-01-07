package com.example.psyad9.runtracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.example.psyad9.runtracker.Contract.*;

public class ContentProvider extends android.content.ContentProvider {

    private DBHelper dbhelper = null;

    @Override
    public boolean onCreate() {
        dbhelper = new DBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
            return dbhelper.getReadableDatabase().query(DATABASE_NAME, strings, s, strings1, null, null, s1);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String type;
        if(uri.getLastPathSegment()==null){
            type = Contract.CONTENT_TYPE_MULTIPLE;
        }
        else{
            type = Contract.CONTENT_TYPE_SINGLE;
        }
        return type;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = 0;
        id = dbhelper.getWritableDatabase().insert(DATABASE_NAME, null, contentValues);
        return Uri.parse(uri+ "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return dbhelper.getWritableDatabase().delete(DATABASE_NAME, s, strings);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return dbhelper.getWritableDatabase().update(DATABASE_NAME, contentValues, s, strings);
    }
}
