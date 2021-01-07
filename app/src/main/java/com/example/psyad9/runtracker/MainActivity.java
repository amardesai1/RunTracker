package com.example.psyad9.runtracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static com.example.psyad9.runtracker.Contract.DATABASE_COORDS;
import static com.example.psyad9.runtracker.Contract.DATABASE_DATE;
import static com.example.psyad9.runtracker.Contract.DATABASE_DISTANCE;
import static com.example.psyad9.runtracker.Contract.DATABASE_RUN_NAME;
import static com.example.psyad9.runtracker.Contract.DATABASE_START_TIME;
import static com.example.psyad9.runtracker.Contract.RECORDS_URI;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    List<String> list;
    private boolean isRunning = false;
    Button startrun;
    TrackingService runService;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("CW2", "MAIN ONCREATE called");

        //gets instances of mainactivity textviews so that they can be edited
        startrun = findViewById(R.id.button4);
        TextView lastruntitle = findViewById(R.id.lastruntitle);
        TextView lastrundistance = findViewById(R.id.lastrundistance);
        TextView lastrundateandtime = findViewById(R.id.lastrundateandtime);

        //retrieves the data from the last recorded run from the database and fills in mainactivity ields and map with data
        try{
        String[] projection = new String[]{DATABASE_RUN_NAME,DATABASE_DATE, DATABASE_START_TIME, DATABASE_COORDS, DATABASE_DISTANCE};
        final Cursor cursor = getContentResolver().query(RECORDS_URI, projection, null, null, null);
        cursor.moveToLast();
        lastruntitle.setText(cursor.getString(cursor.getColumnIndex(DATABASE_RUN_NAME)));
        lastrundateandtime.setText(cursor.getString(cursor.getColumnIndex(DATABASE_DATE))+" "+cursor.getString(cursor.getColumnIndex(DATABASE_START_TIME)));
        lastrundistance.setText(cursor.getString(cursor.getColumnIndex(DATABASE_DISTANCE))+" km");
        String coords = cursor.getString(cursor.getColumnIndex(DATABASE_COORDS));
        list = Arrays.asList(coords.split(","));

        } catch(NullPointerException | CursorIndexOutOfBoundsException | java.lang.NumberFormatException e){
            Log.d("log", "Null pointer on last run ");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try{
            PolylineOptions p = new PolylineOptions();
            for(int i = 0; i<list.size(); i=i+2){
                //adds coordinates to polyline using coordinate list
                p.add(new LatLng(Double.parseDouble(list.get(i)), Double.parseDouble(list.get(i+1))));
            }
            //adds polyline to map and puts it in view on map
            googleMap.addPolyline(p);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(Double.parseDouble(list.get(0)), Double.parseDouble(list.get(1)))), 12));
        }catch(NullPointerException | NumberFormatException e){
            Log.d("log", "Null pointer on retrieving last run");
        }


    }

    //method called by the view records button to open the runlist activity
    public void onRunList(View view)
    {
        Intent intent = new Intent(this, RunList.class);
        startActivity(intent);
    }

    //method called by the record exercise button to open the current run activity
    public void onNewRun(View view) throws ParseException {
        Intent intent = new Intent(this, CurrentRun.class);

        if(isRunning)
            intent.putExtra("running", "true");
        else
            intent.putExtra("running", "false");
        startActivity(intent);
    }





}