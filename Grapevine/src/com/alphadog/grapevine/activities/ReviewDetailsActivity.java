package com.alphadog.grapevine.activities;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
import com.alphadog.grapevine.views.AsyncViewImageUpdater;

public class ReviewDetailsActivity extends Activity {

	private GrapevineDatabase database;
	private ReviewsTable reviewTable;
	private Handler uiUpdateHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		database = new GrapevineDatabase(this);
		reviewTable = new ReviewsTable(database);

		Long reviewId = getIntent().getLongExtra("REVIEW_ID", 1);
		Review review = reviewTable.findById(reviewId);
		try {
			setContentView(R.layout.review_details);

			TextView reviewText = (TextView) findViewById(R.id.review_text);
			reviewText.setText(review.getHeading());
			View metaInfoTitleBar = findViewById(R.id.metadata_title_bar);
			View reviewInfoTitleBar = findViewById(R.id.review_title_bar);
			TextView metaDataHeading = (TextView)metaInfoTitleBar.findViewById(R.id.title_bar_heading);
			metaDataHeading.setText("Review Details");
			
			
			ImageView metaImage = (ImageView)metaInfoTitleBar.findViewById(R.id.title_bar_image);
			metaImage.setImageResource(R.drawable.info);
			
			int imageId = review.isLike() ? R.drawable.comments : R.drawable.exclamation;
			ImageView reviewInfoTitleLikeImage = (ImageView)reviewInfoTitleBar.findViewById(R.id.title_bar_image);
			reviewInfoTitleLikeImage.setImageResource(imageId);
			
			
			TextView reviewHeading = (TextView)reviewInfoTitleBar.findViewById(R.id.title_bar_heading);
			reviewHeading.setText("Review Text");
			
			TextView locationName = (TextView) findViewById(R.id.metadata_place);
			String location=getLocationName(review);
			locationName.setText("Location: " + (location == null? "Unknown" : location));

			TextView date = (TextView) findViewById(R.id.metadata_date);
			date.setText("Date: " + review.getReviewDateInLocalTime());
			
			final ImageView reviewImage = (ImageView) findViewById(R.id.reviewImage);
			(new AsyncViewImageUpdater(uiUpdateHandler) {
				@Override
				public void doUIUpdateTask(Drawable drawable) {
					if(drawable != null) {
						reviewImage.setImageDrawable(drawable);
					}
				}
			}).executeUIUpdateAsAsync(getString(R.string.thumbnail_url)+review.getImageUrl());
		} catch (Exception e) {
			Log.e("ReviewDetailsActivity", "Error occured while updating view for the review details page. Error is: " + e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (database != null)
			database.close();
	}
	
	private String getLocationName(Review review) {
		Geocoder geoCoder = new Geocoder(this);
		List<Address> addressList = null;
		try {
			addressList = geoCoder.getFromLocation((review.getMicroLatitudes()/10E6), (review.getMicroLongitudes()/10E6), 1);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), "Error while fetching location name for latitude and longitude", e);
		}
		
		if(addressList != null && addressList.size() > 0) {
			Address address = addressList.get(0);
			return address.getAdminArea() == null ? address.getCountryName() : address.getAdminArea();
		}

		return null;
	}
}
