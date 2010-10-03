package com.alphadog.grapevine.services;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationUpdateTrigger {
	
	private LocationManager locationManager;
	private boolean gpsSupported = false;
	private boolean networkSupported = false;
	private LocationResultExecutor locationResult;
	private long timetoWaitForRealTimeUpdate;
	
	public LocationUpdateTrigger(Context context, LocationResultExecutor resultExecutor) {
		//By default we'll wait for a minute to get real time updates for location
		//before reverting back to the last known location.
		this(context, 60000, resultExecutor);
	}

	public LocationUpdateTrigger(Context context, long timetoWaitForRealTimeUpdate, LocationResultExecutor resultExecutor) {
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		this.timetoWaitForRealTimeUpdate = timetoWaitForRealTimeUpdate;
		this.locationResult = resultExecutor;
		Log.d("LocationUpdateTrigger", "We'll wait for "+ timetoWaitForRealTimeUpdate + "milliseconds before we go with the last known location");
	}

	//listener to use with gps
	private LocationListener gpsLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			Log.d("LocationUpdateTrigger", "We'd like to think we got a fix on the location from GPS! : " + location);
			if(lastKnowLocationTimerTask != null)
				lastKnowLocationTimerTask.cancel();
			locationManager.removeUpdates(this);
			locationManager.removeUpdates(networkLocationListener);
			locationResult.executeWithUpdatedLocation(location);
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		@Override
		public void onProviderEnabled(String provider) {
		}
		@Override
		public void onProviderDisabled(String provider) {
		}
	};

	//listener to use with network
	private LocationListener networkLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			Log.d("LocationUpdateTrigger", "We'd like to think we got a fix on the location from Network! : " + location);
			if(lastKnowLocationTimerTask != null)
				lastKnowLocationTimerTask.cancel();
			locationManager.removeUpdates(this);
			locationManager.removeUpdates(gpsLocationListener);
			locationResult.executeWithUpdatedLocation(location);
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {			
		}
		@Override
		public void onProviderEnabled(String provider) {
		}
		@Override
		public void onProviderDisabled(String provider) {
		}
	};	
	
	private TimerTask lastKnowLocationTimerTask = new TimerTask() {
		@Override
		public void run() {
			Log.d("LocationUpdateTrigger", "Seems like neither the GPS not the Network could give a fix on location so we are going with last known.");
			//Since we have to eventually wait for timer task to run
			//we should cancel the updates from network and gps and 
			//go with last known location
			locationManager.removeUpdates(networkLocationListener);
			locationManager.removeUpdates(gpsLocationListener);
			
			Location networkLocation =null, gpsLocation =null;
            if(gpsSupported)
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(networkSupported)
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            //if there are both values use the latest one
            if(gpsLocation != null && networkLocation != null){
                if(gpsLocation.getTime() > networkLocation.getTime())
                    locationResult.executeWithUpdatedLocation(gpsLocation);
                else
                    locationResult.executeWithUpdatedLocation(networkLocation);
                return;
            }

            //In case one of them was null then use the one that is not null
            if (gpsLocation != null) {
                locationResult.executeWithUpdatedLocation(gpsLocation);
                return;
            }
            
            if (networkLocation != null){
                locationResult.executeWithUpdatedLocation(networkLocation);
                return;
            }
            
            //Right now if a scenario occurs where no location is present
            //neither in last known and nor current. Then ideal thing would be to
            //show user a notification; however for now we'll let it pass silently
            //without any action. To create a notification later we'll need a handler
            //object here.
		}
	};
	
	public void fetchLatestLocation() {
		Log.d("LocationUpdateTrigger", "Will fetch the current location either using network or GPS provider");
		try {
			gpsSupported = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch(Exception e) {
			Log.e("LocationUpdateTrigger", "Error occured while querying gps availability. " + e.getMessage());
		}
		try {
			networkSupported = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch(Exception e) {
			Log.e("LocationUpdateTrigger", "Error occured while querying network availability. " + e.getMessage());
		}

		//We have no way to query the locations as nothing is supported
		if(!networkSupported && !gpsSupported)
		{
			Log.i("Location Update", "No location information available!");
			return;
		}
		
		if(gpsSupported) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
		}
		
		if(gpsSupported) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
		}
		
		//It'll wait specified seconds before it'll trigger itself and cancel the real time
		//update listeners.
		new Timer().schedule(lastKnowLocationTimerTask, timetoWaitForRealTimeUpdate);
	}

	public static abstract class LocationResultExecutor {
		public abstract void executeWithUpdatedLocation(Location location);
	}

}