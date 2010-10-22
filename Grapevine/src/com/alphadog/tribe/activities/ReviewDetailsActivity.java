package com.alphadog.tribe.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alphadog.tribe.R;
import com.alphadog.tribe.db.ReviewsTable;
import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.models.Review;
import com.alphadog.tribe.views.AsyncViewImageUpdater;

public class ReviewDetailsActivity extends Activity {

	private TribeDatabase database;
	private ReviewsTable reviewTable;
	private Handler uiUpdateHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		database = new TribeDatabase(this);
		reviewTable = new ReviewsTable(database);

		Long reviewId = getIntent().getLongExtra("REVIEW_ID", 1);
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
	protected void onDestroy() {
		super.onDestroy();
		if (database != null)
			database.close();
	}
}
