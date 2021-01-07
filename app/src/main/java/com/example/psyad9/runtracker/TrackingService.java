package com.example.psyad9.runtracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.psyad9.runtracker.Contract.*;

public class TrackingService extends Service {

    public static final String CHANNEL_ID = "channel_id";

    private String time;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    Notification notification;
    NotificationManager notificationManager;
    public Binder TrackingBinder = new TrackingBinder();
    private boolean running = false;

    //Allows activity to access public methods in service
    public class TrackingBinder extends Binder
    {
        TrackingService getService()
        {
            return TrackingService.this;
        }
    }

    //tells the OS to recreate the service if the system runs out of memory
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    //4 methods to indicate the stage of the lifecyle the service is in
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CW2", "SERVICE onCreate called");

    }

    public void onDestroy()
    {
        super.onDestroy();
        Log.d("CW2", "SERVICE onDestory called");
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("CW2", "SERVICE onRebind called");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("CW2", "SERVICE onUnbind called");
        return true;
    }

    //When the service is bound to, it returns the binder
    @Override
    public IBinder onBind(Intent intent) {
        return TrackingBinder;
    }

    //
    public void startrun()
    {
        running = true;
        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 10, locationListener); // request location updates from location listener
        } catch (SecurityException e) {
        }
        createchannel();
        buildnot();

        Log.d("CW2", "SERVICE START RUN called");

    }

    //
    public void stoprun() throws ParseException {
        running = false;
        stopForeground(true);

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String starttime = time;
        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date date1 = format.parse(starttime);
        Date date2 = format.parse(time);
        String length = Long.toString((date2.getTime() - date1.getTime())/60000);
        @SuppressLint("DefaultLocale") String coords = locationListener.returnCoords();
        @SuppressLint("DefaultLocale") String stringdistance = String.format("%.2f", locationListener.returnDistanceTotal());

        insertvalues(date, starttime, length, coords, stringdistance);
    }

    //method takes in all values recorded by the service about the users run and inserts in into the database
    public void insertvalues(String date, String time, String length, String coords, String distance)
    {
        ContentValues RunValues = new ContentValues();
        RunValues.put(DATABASE_RUN_NAME, "Your run on "+date+"@"+time);
        RunValues.put(DATABASE_DATE, date);
        RunValues.put(DATABASE_START_TIME, time);
        RunValues.put(DATABASE_LENGTH, length);
        RunValues.put(DATABASE_COORDS, coords);
        RunValues.put(DATABASE_DISTANCE, distance);
        RunValues.put(DATABASE_WEATHER, "");
        RunValues.put(DATABASE_NOTES, "");
        RunValues.put(DATABASE_RATING, 0);

        getContentResolver().insert(RECORDS_URI, RunValues);
        Log.d("CW2", "SERVICE INSERVALUES called");
    }

    //method that creates a channel and manager for the notification
    public void createchannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //method that builds and starts a new notification with content, priority, icon and a pending intent that will return the user to the main class
    public void buildnot()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if(running)
            intent.putExtra("running", "true");
        else
            intent.putExtra("running", "false");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_directions_run_24px)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setContentTitle("Your run is being recorded")
                .setContentText("Click here to return to RunTracker")
                .build();
        startForeground(100,notification);
    }

}
