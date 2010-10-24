package com.alphadog.tribe.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alphadog.tribe.R;
import com.alphadog.tribe.db.ReviewsTable;
import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.helpers.LocationHelper;
import com.alphadog.tribe.models.Review;
import com.alphadog.tribe.services.ReviewsSyncService;
import com.alphadog.tribe.views.LocationItemizedMapOverlay;
import com.alphadog.tribe.views.ReviewItemizedMapOverlay;
import com.alphadog.tribe.views.ReviewOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class ReviewsMapActivity extends MapActivity {

	public static final String SELECTED_REVIEW_ID = "selectedReviewId";
	private TribeDatabase database;
	private ReviewsTable reviewTable;
	private List<Overlay> mapOverlays;
	private LocationManager locationManager;
	private MapView mapView;
	private long reviewId;
	
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		database = new TribeDatabase(this);
		reviewTable = new ReviewsTable(database);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		reviewId = getIntent().getLongExtra(SELECTED_REVIEW_ID,-1);
		
	    setContentView(R.layout.map);
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mapOverlays = mapView.getOverlays();
	    
	    updateMap();
	}
	
	//Handler to refresh views
	private Handler viewUpdater = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 0:
				updateOverlays();
				focusOnMyLocationOrLatestReview(mapView, fetchLastestReview());
				break;
			default:
				break;
			}
		}
	};	

	private void updateMap() {
		Thread mapUpdateThread = new Thread() {
			public void run() {
				Message msg = new Message();
				msg.what = 0;
				viewUpdater.sendMessage(msg);
			}
		};
		mapUpdateThread.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(viewRefreshReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(viewRefreshReceiver, new IntentFilter(ReviewsSyncService.BROADCAST_ACTION));
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private void updateOverlays() {
		mapOverlays.clear();
		updateReviewOverlaysList(fetchCurrentReviewOverlays());
	    updateLocationOverlaysList(fetchCurrentLocationOverlays());
	}

	private void updateReviewOverlaysList(List<ReviewOverlay> overlayList) {
		Drawable drawable = this.getResources().getDrawable(R.drawable.info_overlay);
		ReviewItemizedMapOverlay itemizedOverlays = new ReviewItemizedMapOverlay(drawable, this);
		itemizedOverlays.addAllOverlays(overlayList);
		mapOverlays.add(itemizedOverlays);
	}

	private void updateLocationOverlaysList(List<OverlayItem> locationOverlayList) {
		Drawable drawable = this.getResources().getDrawable(R.drawable.my_location);
		LocationItemizedMapOverlay locationOverlays = new LocationItemizedMapOverlay(drawable);
		locationOverlays.addAllOverlays(locationOverlayList);
		mapOverlays.add(locationOverlays);
	}
	
	private void focusOnMyLocationOrLatestReview(MapView mapView, Review selectedReview) {
		GeoPoint focusLocation = getMyLocationOnMap();
		if(focusLocation == null  && selectedReview != null && selectedReview.getGeoPoint() != null) {
			focusLocation = selectedReview.getGeoPoint();
		}
		
		if (focusLocation != null) {
			MapController mapViewController = mapView.getController();
			mapViewController.animateTo(focusLocation);
			mapViewController.setZoom(10); 
	        mapView.postInvalidate();
		}
	}
	
	private List<ReviewOverlay> fetchCurrentReviewOverlays() {
		List<ReviewOverlay> overlayList = new ArrayList<ReviewOverlay>();
		List<Review> reviewList = null;
		
		if(reviewId == -1) {
			reviewList = reviewTable.findAll();
		} else {
			reviewList = new ArrayList<Review>(1);
			reviewList.add(reviewTable.findById(reviewId));
		}
		
		for(Review eachReview : reviewList) {
			overlayList.add(new ReviewOverlay(eachReview.getGeoPoint(), "", "", eachReview));
		}
		return overlayList;
	}
	
	private List<OverlayItem> fetchCurrentLocationOverlays() {
		List<OverlayItem> overlayList = new ArrayList<OverlayItem>();
		GeoPoint myLocation = getMyLocationOnMap();
		if(myLocation != null) {
			overlayList.add(new OverlayItem(myLocation , "", ""));
		}
		return overlayList;
	}
	
	private BroadcastReceiver viewRefreshReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.i("Reviews Map Activity", "Broadcast received ::" + intent.getAction());
			updateMap();
		}
	};
	
	private Review fetchLastestReview() {
		List<Review> listWithLatestReview = reviewTable.findAllWithMaxLimit(1);
		if (listWithLatestReview != null && listWithLatestReview.size() > 0) {
			return listWithLatestReview.get(0);
		}
		return null;
	}
	
	private GeoPoint getMyLocationOnMap() {
		Location lastKnownLocation = new LocationHelper(locationManager).getBestLastKnownLocation();
		if(lastKnownLocation != null) {
			Double longitudeE6 = new Double (lastKnownLocation.getLongitude() * 10E5);
			Double latitudeE6 = new Double (lastKnownLocation.getLatitude() * 10E5);
			Log.i(this.getClass().getName(), "Adding Location overlay with cordinates as : Latitude/Longitude[" + latitudeE6+"/"+longitudeE6+"]" );
			return new GeoPoint(latitudeE6.intValue(), longitudeE6.intValue());
		}
		
		return null;
	}
		
}
