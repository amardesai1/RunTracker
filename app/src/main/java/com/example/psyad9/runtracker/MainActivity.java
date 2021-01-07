package com.example.psyad9.runtracker;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static com.example.psyad9.runtracker.Contract.*;
import com.example.psyad9.runtracker.TrackingService.TrackingBinder;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    List<String> list;
    private boolean isBound;
    private boolean isRunning = false;
    Button startrun;
    TrackingService runService;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.startService(new Intent(this, TrackingService.class));
        this.bindService(new Intent(this, TrackingService.class), serviceConnection, Context.BIND_AUTO_CREATE);

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

        //if the app is launched from being closed from the notification onNewintent is called to restore modified UI elements
        Log.d("CW2", "MAIN ONNEWINTENT called @onCREATE");
        onNewIntent(getIntent());
    }


    //If the app is started from notification, the intent used will be checked for a string that indicates whether the service is running or not
    //If the service is running, the start exercise button is updated accordingly
    @Override
    public void onNewIntent(Intent intent) {
        Log.d("CW2", "MAIN ONNEWINTENT called");

        try {
            Bundle extras = intent.getExtras();
            String running = extras.getString("running");
            if (running.equals("true")) {
                Log.d("CW2", "STATE RESTORED: "+running);
                startrun.setText("STOP EXERCISE");
                isRunning = true;
            }
        }catch(NullPointerException e){
            Log.d("log", "Null pointer on pending intent");
        }
        super.onNewIntent(intent);
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

    //method called by the record exercise button to call the start run method in the service
    //if a run is being recorded, the button changes to a stop run button and can call the stop run method in the service
    public void onNewRun(View view) throws ParseException {
        if(isRunning)
        {
            runService.stoprun();
            startrun.setText("START EXERCISE");
            isRunning=false;
        }else {
            runService.startrun();
            startrun.setText("STOP EXERCISE");
            isRunning=true;
        }
    }

    //create a service connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder s) {
            runService = ((TrackingService.TrackingBinder)s).getService();
            isBound = true;
            Log.d("CW2", "MAIN ONSERVICECONNECTED called");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            runService = null;
            Log.d("CW2", "MAIN ONSERVICEDISCONNECTED called");

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //rebinds to service when the activity is resumed
        Intent createService = new Intent(MainActivity.this, TrackingService.class);
        bindService(createService, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("CW2", "MAIN BIND SERVICE called @ONRESUME");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //unbinds service if run is being recorded to that service can continue to run
        if(isRunning&&isBound&&serviceConnection!=null)
        {
            unbindService(serviceConnection);
            isBound = false;
            Log.d("CW2", "MAIN UNBIND SERVICE called @ONDESTROY");
        }else{
            //if run isnt being recorded, end service when activity is ended
            stopService(new Intent(this, TrackingService.class));
        }
        Log.d("CW2", "MAIN ONDESTORY called");
    }


}