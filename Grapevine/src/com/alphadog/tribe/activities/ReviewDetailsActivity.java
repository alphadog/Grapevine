package com.alphadog.tribe.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alphadog.tribe.R;
import com.alphadog.tribe.TribeApplication;
import com.alphadog.tribe.db.ReviewsTable;
import com.alphadog.tribe.models.Review;
import com.alphadog.tribe.views.AsyncViewImageUpdater;

public class ReviewDetailsActivity extends Activity {

	private static final int MAP_VIEW = 1;
	private ReviewsTable reviewTable;
	private Handler uiUpdateHandler = new Handler();
	private long reviewId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reviewTable = new ReviewsTable(((TribeApplication) getApplication()).getDB());

		reviewId = getIntent().getLongExtra("REVIEW_ID", 1);
		Review review = reviewTable.findById(reviewId);
		try {
			setContentView(R.layout.review_details);

			TextView reviewText = (TextView) findViewById(R.id.review_text);
			reviewText.setText(review.getHeading());
			View metaInfoTitleBar = findViewById(R.id.metadata_title_bar);
			View reviewInfoTitleBar = findViewById(R.id.review_title_bar);
			View imageDetailsTitleBar = findViewById(R.id.image_details_bar);
			
			if(review.getImageUrl() != null && review.getImageUrl().length() > 0) {
				TextView imageLinkHeading = (TextView)imageDetailsTitleBar.findViewById(R.id.title_bar_heading);
				imageLinkHeading.setText("Image Details");

				ImageView reviewDetailImage = (ImageView)imageDetailsTitleBar.findViewById(R.id.title_bar_image);
				reviewDetailImage.setImageResource(R.drawable.image_info);
				
				TextView imageLinkValue = (TextView)findViewById(R.id.image_link);
				imageLinkValue.setText(getString(R.string.thumbnail_url)+review.getImageUrl());
				Linkify.addLinks(imageLinkValue, Linkify.ALL);
			}
			
			TextView metaDataHeading = (TextView)metaInfoTitleBar.findViewById(R.id.title_bar_heading);
			metaDataHeading.setText("Review Details");
			
			ImageView metaImage = (ImageView)metaInfoTitleBar.findViewById(R.id.title_bar_image);
			metaImage.setImageResource(R.drawable.info);
			
			int imageId = review.isLike() ? R.drawable.comments : R.drawable.exclamation;
			ImageView reviewInfoTitleLikeImage = (ImageView)reviewInfoTitleBar.findViewById(R.id.title_bar_image);
			reviewInfoTitleLikeImage.setImageResource(imageId);
			
			TextView reviewHeading = (TextView)reviewInfoTitleBar.findViewById(R.id.title_bar_heading);
			reviewHeading.setText("Review Text");
			
			if(review.getUsername() != null) {
				TextView date = (TextView) findViewById(R.id.metadata_posted_by);
				date.setText("Posted By: " + review.getUsername() + "@twitter");
			}
			
			TextView date = (TextView) findViewById(R.id.metadata_date);
			date.setText("Date: " + review.getReviewDateInLocalTimezone());

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
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MAP_VIEW, 0, getString(R.string.show_on_map)).setIcon(R.drawable.globe);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MAP_VIEW:
			//Invoke map activity in new thread
            new Thread(new Runnable() {
                public void run() {
                    Intent newIntent = new Intent(ReviewDetailsActivity.this, ReviewsMapActivity.class);
                    newIntent.putExtra(ReviewsMapActivity.SELECTED_REVIEW_ID, reviewId);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(newIntent);
                }
            }).start();
            return true;
        }
	    return false;
	}
}
