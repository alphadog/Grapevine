package com.alphadog.grapevine.views;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ReviewItemizedMapOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	public ReviewItemizedMapOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
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
		return true;
	}
}
