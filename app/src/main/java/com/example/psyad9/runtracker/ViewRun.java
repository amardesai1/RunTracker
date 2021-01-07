package com.example.psyad9.runtracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.psyad9.runtracker.Contract.*;
public class ViewRun extends AppCompatActivity implements OnMapReadyCallback {

    List<String> list;
    private int id;
    private SupportMapFragment mapFragment;
    private EditText runview;
    private EditText weatheredit;
    private EditText notesedit;
    private RadioGroup ratingview;



    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_runs);

        //gets instances of textfields so they can be edited
        id = getIntent().getIntExtra(DATABASE_ID, 0);
        runview = findViewById(R.id.runtitle);
        TextView timeview = findViewById(R.id.runstarttime);
        TextView lengthview = findViewById(R.id.runlength);
        TextView distanceview = findViewById(R.id.rundistance);
        TextView speedview = findViewById(R.id.runavespeed);
        TextView calsview = findViewById(R.id.runcalories);
        weatheredit = findViewById(R.id.editText);
        notesedit = findViewById(R.id.editText2);
        ratingview = findViewById(R.id.ratinggroup);

        //
        try {
            //Finds row cursor in database corresponding to the run clicked on in the runlist
            //Retrieves data from all other fields in row
            String[] idprojection = new String[]{DATABASE_RUN_NAME,DATABASE_ID, DATABASE_DATE, DATABASE_START_TIME, DATABASE_LENGTH,DATABASE_COORDS, DATABASE_DISTANCE, DATABASE_WEATHER, DATABASE_NOTES, DATABASE_RATING};
            @SuppressLint("Recycle") final Cursor idcursor = getContentResolver().query(RECORDS_URI, idprojection, null, null, null);
            assert idcursor != null;
            while (idcursor.moveToNext()) {
                if (id==idcursor.getInt(idcursor.getColumnIndex(DATABASE_ID))) {
                    //sets textfields to the retrieved run data
                    runview.setText(idcursor.getString(idcursor.getColumnIndex(DATABASE_RUN_NAME)));
                    String coords = idcursor.getString(idcursor.getColumnIndex(DATABASE_COORDS));
                    list = Arrays.asList(coords.split(","));
                    distanceview.setText(idcursor.getString(idcursor.getColumnIndex(DATABASE_DISTANCE))+" km");
                    timeview.setText(idcursor.getString(idcursor.getColumnIndex(DATABASE_START_TIME)));
                    lengthview.setText(idcursor.getString(idcursor.getColumnIndex(DATABASE_LENGTH)) + " min");

                    //Calculates calories burned and average speed from database data and sets to textfields
                    int scale = (int) Math.pow(10, 1);
                    if(idcursor.getString(idcursor.getColumnIndex(DATABASE_LENGTH)).equals("0")){
                        speedview.setText("∞ kmph");
                    }else{
                        double speedtemp = Double.parseDouble(idcursor.getString(idcursor.getColumnIndex(DATABASE_DISTANCE)))/(Double.parseDouble(idcursor.getString(idcursor.getColumnIndex(DATABASE_LENGTH)))/60);
                        double speed = (double) Math.round(speedtemp * scale) / scale;
                        speedview.setText(String.format("%.2f", speed)+" kmph");
                    }
                    double caltemp = Double.parseDouble(idcursor.getString(idcursor.getColumnIndex(DATABASE_DISTANCE)))*100;
                    double cal = (double) Math.round(caltemp * scale) / scale;
                    calsview.setText(cal+" cals");
                    weatheredit.setText(idcursor.getString(idcursor.getColumnIndex(DATABASE_WEATHER)));
                    notesedit.setText(idcursor.getString(idcursor.getColumnIndex(DATABASE_NOTES)));


                    //sets radiobutton to rating data from database
                    switch(idcursor.getInt(idcursor.getColumnIndex(DATABASE_ID))) {
                        case 1:
                            ratingview.check(R.id.radioOne);
                            break;
                        case 2:
                            ratingview.check(R.id.radioTwo);
                            break;
                        case 3:
                            ratingview.check(R.id.radioThree);
                            break;
                        case 4:
                            ratingview.check(R.id.radioFour);
                            break;
                        case 5:
                            ratingview.check(R.id.radioFive);
                            break;
                        default:
                            ratingview.clearCheck();
                    }
                    break;
                }
            }
        } catch(NullPointerException e){
            Log.d("log", "Null pointer ingredients check");
        }


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);


    }

    //Method called to add content to the map
    public void onMapReady(GoogleMap googleMap) {
        PolylineOptions p = new PolylineOptions();
        for(int i = 0; i<list.size(); i=i+2){
            //adds coordinates to polyline using coordinate list
            p.add(new LatLng(Double.parseDouble(list.get(i)), Double.parseDouble(list.get(i+1))));
        }
        //adds polyline to map and puts it in view on map
        googleMap.addPolyline(p);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(Double.parseDouble(list.get(0)), Double.parseDouble(list.get(1)))), 12));
    }


    public void onClickDone(View view)
    {
        //Retrieves strings from editable fields on viewrun activitty
        int rating = Integer.parseInt(((RadioButton) findViewById(ratingview.getCheckedRadioButtonId())).getText().toString());
        String weather = weatheredit.getText().toString();
        String notes = notesedit.getText().toString();
        String title = runview.getText().toString();

        //adds edited strings to a contentvalue and updates the database with it using the content resolver
        ContentValues editablefields = new ContentValues();
        editablefields.put(DATABASE_RUN_NAME, title);
        editablefields.put(DATABASE_RATING, rating);
        editablefields.put(DATABASE_WEATHER, weather);
        editablefields.put(DATABASE_NOTES, notes);
        getContentResolver().update(RECORDS_URI,editablefields,DATABASE_ID+"="+id,null);
        finish();
    }

}
