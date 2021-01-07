package com.example.psyad9.runtracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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


public class CurrentRun extends AppCompatActivity  implements OnMapReadyCallback {

    List<String> list;
    private boolean isRunning = false;
    private Button startrun;
    TrackingService runService;
    private boolean isBound;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_run);

        this.startService(new Intent(this, TrackingService.class));
        this.bindService(new Intent(this, TrackingService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        startrun = findViewById(R.id.startbutton);
        String coords = "";
        list = Arrays.asList(coords.split(","));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        //if the app is launched from being closed from the notification onNewintent is called to restore modified UI elements
            Log.d("CW2", "CURRENT RUN ONNEWINTENT called @onCREATE");
        onNewIntent(getIntent());
    }

    //If the app is started from notification, the intent used will be checked for a string that indicates whether the service is running or not
    //If the service is running, the start exercise button is updated accordingly
    @Override
    public void onNewIntent(Intent intent) {
        Log.d("CW2", "CURRENT RUN ONNEWINTENT called");

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

    //method called by the home button to return to main
    //This useful as if the user returned to the activity using the notification after closing the app, the activity stack will be empty
    public void onHome(View view) throws ParseException {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //create a service connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder s) {
            runService = ((TrackingService.TrackingBinder)s).getService();
            isBound = true;
            Log.d("CW2", "CURRENT RUN ONSERVICECONNECTED called");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            runService = null;
            Log.d("CW2", "CURRENT RUN ONSERVICEDISCONNECTED called");

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //rebinds to service when the activity is resumed
        Intent createService = new Intent(CurrentRun.this, TrackingService.class);
        bindService(createService, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("CW2", "CURRENT RUN BIND SERVICE called @ONRESUME");
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
            Log.d("CW2", "CURRENT RUN UNBIND SERVICE called @ONDESTROY");
        }else{
            //if run isnt being recorded, end service when activity is ended
            stopService(new Intent(this, TrackingService.class));
        }
        Log.d("CW2", "CURRENT RUN ONDESTORY called");
    }

}
