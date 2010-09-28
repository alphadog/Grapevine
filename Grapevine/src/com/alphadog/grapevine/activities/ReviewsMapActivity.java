package com.alphadog.grapevine.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
import com.alphadog.grapevine.services.ReviewsSyncService;
import com.alphadog.grapevine.views.ReviewItemizedMapOverlay;
import com.alphadog.grapevine.views.ReviewOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ReviewsMapActivity extends MapActivity {

	private GrapevineDatabase database;
	private ReviewsTable reviewTable;
	private List<Overlay> reviewOverlays;
	
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		database = new GrapevineDatabase(this);
		reviewTable = new ReviewsTable(database);
		
	    setContentView(R.layout.map);
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    reviewOverlays = mapView.getOverlays();
	    
	    updateOverlaysList(fetchCurrentOverlays());
	    focusOnSelectedReview(mapView, fetchLastestReview());
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
	
	private void updateOverlaysList(List<ReviewOverlay> overlayList) {
		reviewOverlays.clear();
		Drawable drawable = this.getResources().getDrawable(R.drawable.info_overlay);
		ReviewItemizedMapOverlay itemizedOverlays = new ReviewItemizedMapOverlay(drawable);
		itemizedOverlays.addAllOverlays(overlayList);
		reviewOverlays.add(itemizedOverlays);
	}
	
	private void focusOnSelectedReview(MapView mapView, Review selectedReview) {
		if(selectedReview != null) {
			GeoPoint point = selectedReview.getGeoPoint();
			MapController mapViewController = mapView.getController();
			mapViewController.animateTo(point);
			mapViewController.setZoom(10); 
	        mapView.postInvalidate();
		}
	}
	
	private List<ReviewOverlay> fetchCurrentOverlays() {
		List<ReviewOverlay> overlayList = new ArrayList<ReviewOverlay>();
		List<Review> reviewList = reviewTable.findAll();
		for(Review eachReview : reviewList) {
			overlayList.add(new ReviewOverlay(eachReview.getGeoPoint(), "", "", eachReview));
		}
		return overlayList;
	}
	
	private BroadcastReceiver viewRefreshReceiver=new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.i("ReviewsMapActivity", "Broadcast received ::" + intent.getAction());
			updateOverlaysList(fetchCurrentOverlays());
		}
	};
	
	private Review fetchLastestReview() {
		List<Review> listWithLatestReview = reviewTable.findAllWithMaxLimit(1);
		if(listWithLatestReview != null && listWithLatestReview.size() > 0) {
			return listWithLatestReview.get(0);
		}
		return null;
	}
		
}
