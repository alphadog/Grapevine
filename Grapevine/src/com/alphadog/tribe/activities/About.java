package com.alphadog.tribe.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alphadog.tribe.R;

public class About extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);		
		setContentView(R.layout.about);
		
		//update various parts of the view
		updateLicenseDetails();
	}

	private void updateLicenseDetails() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.image_details_bar).findViewById(R.id.bar_inner);
		layout.setGravity(Gravity.CENTER_HORIZONTAL);
		TextView barHeading = (TextView)findViewById(R.id.image_details_bar).findViewById(R.id.title_bar_heading);
		barHeading.setText("What's Tribe About");
	}

}
