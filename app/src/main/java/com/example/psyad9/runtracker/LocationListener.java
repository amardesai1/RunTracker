package com.example.psyad9.runtracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Luke on 04/01/2018.
 */

class MyLocationListener implements LocationListener {

    private double total;
    Location prevLocation;
    String coords = "";

    //method checks if location has changed
    @Override
    public void onLocationChanged(Location location) {
        //records coordinates of current location and appends to comma seperated string
        //compares location to last location and adds distance to a running total
        if (prevLocation != null){
            coords+=","+location.getLatitude() + "," + location.getLongitude();
            double distance = prevLocation.distanceTo(location);
            total += distance;
            prevLocation = location;
        } else {
            coords=location.getLatitude() + "," + location.getLongitude();
            prevLocation = location;
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // information about the signal, i.e. number of satellites
        Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
    }
    @Override
    public void onProviderEnabled(String provider) {
        // the user enabled (for example) the GPS
        Log.d("g53mdp", "onProviderEnabled: " + provider);
    }
    @Override
    public void onProviderDisabled(String provider) {
        // the user disabled (for example) the GPS
        Log.d("g53mdp", "onProviderDisabled: " + provider);
    }

    //returns the string with all coordinates
    public String returnCoords(){
        return coords;
    }

    //returns distance in kilometers
    public double returnDistanceTotal(){
        return total/1000;
    }


}