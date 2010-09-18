package com.alphadog.grapevine.services;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class LocationTracker {
	
	private LocationManager locationManager;
	
	public LocationTracker(Context context) {
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

//	public void getCurrentLocation() {
//		//the min time and min distance params here do not matter as if now
//		//as they will not be used, since listener is un-hooked immediately. We might need to use them
//		//later and that's when we'll make them configurable.
//		this.locationManager.requestLocationUpdates(getLocationProviderName(getCriteria()), 0, 0, getLocationListener());
//	}
//
//	protected abstract LocationListener getLocationListener();
	
	protected String getLocationProviderName(Criteria criteria) {
		return this.locationManager.getBestProvider(criteria, true);
	}

	protected Criteria getCriteria() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}
	
	protected Location getLastKnownLocation() {
		Location lastKnownLocation = this.locationManager.getLastKnownLocation(getLocationProviderName(getCriteria()));
		Log.d("LocationTracker", "Returning last known location :" + lastKnownLocation);
		return lastKnownLocation;
	}
	
	protected void unhookLocationListener(LocationListener listener) {
		this.locationManager.removeUpdates(listener);
	}
}
