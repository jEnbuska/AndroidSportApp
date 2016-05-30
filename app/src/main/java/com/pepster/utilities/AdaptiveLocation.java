package com.pepster.utilities;

import android.annotation.SuppressLint;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;

/**
 * Created by WinNabuska on 20.3.2016.
 */

/**AdaptiveLocation is a class, made for saving the amount of boiler plate code needed.
 * Using this class it should be easy and efficient to switch between different kind of Coordinate
 * datatypes like Location, GeoPoint and LatLng.
 **/
@SuppressLint("ParcelCreator")
public class AdaptiveLocation {


    private double lat;
    private double lng;

    /***Location and GeoPoint values might be created later after initialization. They are used to
     * save computation resources when forexample, distances between locations are calculated
     * multiple time*/
    private Location mLocationValue;
    private GeoPoint mGeoPointValue;

    public AdaptiveLocation(){
    }

    public static AdaptiveLocation from(Location location){
        AdaptiveLocation aLocation = new AdaptiveLocation(location.getLatitude(), location.getLongitude());
        aLocation.mLocationValue =location;
        return aLocation;
    }

    public static AdaptiveLocation from(GeoPoint geoPoint){
        AdaptiveLocation aLocation = new AdaptiveLocation(geoPoint.getLatitude(), geoPoint.getLongitude());
        aLocation.mGeoPointValue =geoPoint;
        return aLocation;
    }

    public AdaptiveLocation(double latitude, double longitude){
        this.lat=latitude;
        this.lng=longitude;
    }

    public Location asLocation(){
        if(mLocationValue == null) {
            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);
            mLocationValue=location;
        }
        return mLocationValue;
    }

    public void setLat(double latitude) {
        this.lat = latitude;
    }

    public void setLng(double longitude) {
        this.lng = longitude;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public GeoPoint asGeoPoint(){
        if(mGeoPointValue ==null){
            mGeoPointValue = new GeoPoint(lat, lng);
        }
        return mGeoPointValue;
    }

    public LatLng asLatLng(){
        return new LatLng(lat, lng);
    }

    /**equals return true when it receives a Location, AdaptiveLocation or GeoPoint objects thats
     * distance from THIS object is less than half a meter*/
    @Override
    public boolean equals(Object o){
        if(o instanceof AdaptiveLocation){
            return this.distanceTo(((AdaptiveLocation) o).asGeoPoint())<0.5;
        }else if(o instanceof Location){
            return this.distanceTo((Location)o)<0.5;
        }else if(o instanceof GeoPoint){
            return this.distanceTo((GeoPoint)o)<0.5;
        }else{
            return false;
        }
    }


    /**distanceTo return the distance in meters, between this and the other object */
    public double distanceTo(Location location){
        return asLocation().distanceTo(location);
    }
    public double distanceTo(GeoPoint geoPoint){
        return asGeoPoint().distanceTo(geoPoint);
    }
    public double distanceTo(AdaptiveLocation adaptiveLocation){
        return asGeoPoint().distanceTo(adaptiveLocation.asGeoPoint());
    }

    /**returns THIS distance in meters from an imaginary line that goes between a and b*/
    public double distanceFromLine(AdaptiveLocation a, AdaptiveLocation b){
        if(this.equals(a) || this.equals(b))
            return 0;
        else if(a.equals(b))
            //a and b are withing 0.5 meters from each other
            return Math.min(a.distanceTo(this), b.distanceTo(this));
        else {
            double startToEndAngle = Math.atan2(b.lat - a.lat, b.lng - a.lng);
            double startToThisAngle = Math.atan2(this.lat - a.lat, this.lng - a.lng);
            double endToStartAngle = Math.atan2(a.lat - b.lat, a.lng - b.lng);
            double endToThisAngle = Math.atan2(this.lat - b.lat, this.lng - b.lng);
            //THIS location is on the same imaginary between a and b
            if (startToEndAngle == startToThisAngle && endToStartAngle == endToThisAngle)
                return  0;
            else if (!isPerpendicularToLine(a,b))
                return Math.min(a.distanceTo(this), b.distanceTo(this));
            else {
                double angleDifference = Math.abs(startToEndAngle-startToThisAngle)%Math.PI;
                return Math.sin(angleDifference)*a.distanceTo(this);
            }
        }
    }

    /**Checks if THIS location is perpendicular to the line between a and b*/
    public boolean isPerpendicularToLine(AdaptiveLocation a, AdaptiveLocation b){
        //Angles in radians
        double sToE = Math.atan2(b.lat - a.lat, b.lng - a.lng);
        double sToThis = Math.atan2(this.lat - a.lat, this.lng - a.lng);
        double eToS = Math.atan2(a.lat - b.lat, a.lng - b.lng);
        double eToThis = Math.atan2(this.lat - b.lat, this.lng - b.lng);
        if(Math.abs(sToE-sToThis)%Math.PI>Math.PI/2 ||
                Math.abs(eToS-eToThis)%Math.PI>Math.PI/2)
            return false;
        else{
            if(Math.abs(eToS-eToThis)%Math.PI>Math.PI/2)
                return false;
            else
                return true;
        }
    }
}
