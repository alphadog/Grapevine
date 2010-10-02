package com.alphadog.grapevine.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.alphadog.grapevine.activities.ReviewDetailsActivity;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ReviewItemizedMapOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context context;
	
	public ReviewItemizedMapOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;   
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void addAllOverlays(List<ReviewOverlay> overlayList) {
		mOverlays.addAll(overlayList);
		populate();
	}
	
	
	@Override
	protected boolean onTap(int index) {
		Intent reviewDetailIntent = new Intent(this.context, ReviewDetailsActivity.class);
		reviewDetailIntent.putExtra("POS", index);
		this.context.startActivity(reviewDetailIntent);
		return true;
	}
}
