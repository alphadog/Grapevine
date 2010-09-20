package com.alphadog.grapevine.activities;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
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
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private void updateOverlaysList(List<ReviewOverlay> overlayList) {
		Drawable drawable = this.getResources().getDrawable(R.drawable.info_overlay);
		ReviewItemizedMapOverlay itemizedOverlays = new ReviewItemizedMapOverlay(drawable);
		itemizedOverlays.addAllOverlays(overlayList);
		reviewOverlays.add(itemizedOverlays);
	}
	
	private void focusOnSelectedReview(MapView mapView, Review selectedReview) {
		GeoPoint point = selectedReview.getGeoPoint();
		MapController mapViewController = mapView.getController();
		mapViewController.animateTo(point);
		mapViewController.setZoom(8); 
        mapView.postInvalidate();
	}
	
	private List<ReviewOverlay> fetchCurrentOverlays() {
		List<ReviewOverlay> overlayList = new ArrayList<ReviewOverlay>();
		List<Review> reviewList = reviewTable.findAll();
		for(Review eachReview : reviewList) {
			overlayList.add(new ReviewOverlay(eachReview.getGeoPoint(), "", "", eachReview));
		}
		return overlayList;
	}
		
}
