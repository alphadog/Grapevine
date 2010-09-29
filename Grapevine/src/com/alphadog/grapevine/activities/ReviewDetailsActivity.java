package com.alphadog.grapevine.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

		Integer clickedReviewIndex = getIntent().getIntExtra("POS", 0);
		//This will work for now as we always clean up the old review set before storing new one
		//and hence the id's will always be in matching sequence. But Ideally here the id of the
		//review should be passed.
		Review review = reviewTable.findById(clickedReviewIndex + 1);
		try {
			setContentView(R.layout.review_details);

			TextView reviewText = (TextView) findViewById(R.id.reviewText);
			reviewText.setText(review.getHeading());
			TextView reviewPosition = (TextView) findViewById(R.id.reviewPosition);
			reviewPosition.setText("Posted from: " + review.getLatitude() + ", " + review.getLongitude());
			
			final ImageView reviewImage = (ImageView) findViewById(R.id.reviewImage);
			(new AsyncViewImageUpdater(uiUpdateHandler) {
				@Override
				public void doUIUpdateTask(Drawable drawable) {
					if(drawable != null) {
						reviewImage.setImageDrawable(drawable);
					}
				}
			}).executeUIUpdateAsAsync("http://t1.gstatic.com/images?q=tbn:ANd9GcTFlwiKsKy-IfJkF-zmUxKMa-uVxJkYZ2G4MmuRISBJaOKLofY&t=1&usg=__jiXjdZTLq_MTryETgiHOrBsTVjc=");
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
