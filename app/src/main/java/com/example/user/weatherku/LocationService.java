package com.example.user.weatherku;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.example.user.weatherku.Constant.Constant;

import rx.Observable;
import rx.Subscriber;


/**
 * Created by user on 12/12/17.
 */
public class LocationService {
    private static final String TAG = LocationService.class.toString();
    private LocationManager mLocationManager;
    private Context mContext;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: + " + provider);
        }
    };
    public LocationService(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public Observable<Location> getLocation() {
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,mLocationListener);
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(Subscriber<? super Location> subscriber) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                String provider = mLocationManager.getBestProvider(criteria, false);
                Log.i(TAG, "provider: " + provider);
                Location location = getLastLocation(provider, 100);
                //Location location = mLocationManager.getLastKnownLocation(provider);
                subscriber.onNext(location);
            }
        });
    }

    private Location getLastLocation(String provider, int interval) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constant.LAST_LOCATION, Context.MODE_PRIVATE);
        Location location = mLocationManager.getLastKnownLocation(provider);
        if (location == null) {
            Log.d(TAG, "location null");
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "gps on");
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                interval = interval + interval;
                return  getLastLocation(provider, interval);
            } else {
                String lat = sharedPreferences.getString(Constant.LAST_LOCATION_LAT, null);
                String lon = sharedPreferences.getString(Constant.LAST_LOCATION_LON, null);
                Log.d(TAG, "lat: " + lat + " @ lon: " + lon);
                if (lat != null && lon != null) {
                    Location lastLocation = new Location(LocationManager.GPS_PROVIDER);
                    lastLocation.setLatitude(Double.parseDouble(lat));
                    lastLocation.setLongitude(Double.parseDouble(lon));
                    return lastLocation;
                }
            }
        } else {
            mLocationManager.removeUpdates(mLocationListener);
            Log.d(TAG, "lat: " + location.getLatitude() + " @ lon: " + location.getLongitude());
            String originLat = sharedPreferences.getString(Constant.LAST_LOCATION_LAT, null);
            String originLon = sharedPreferences.getString(Constant.LAST_LOCATION_LON, null);
            if (originLat != null && originLon != null) {
                if (Utils.isLocationChanged(location, originLat, originLon)) {
                    Log.d(TAG, "new location");
                    sharedPreferences.edit().putString(Constant.LAST_LOCATION_CITY, null).apply();
                }
            }
            sharedPreferences.edit().putString(Constant.LAST_LOCATION_LAT, String.valueOf(location.getLatitude())).apply();
            sharedPreferences.edit().putString(Constant.LAST_LOCATION_LON, String.valueOf(location.getLongitude())).apply();
            return location;
        }
        return null;
    }

}
