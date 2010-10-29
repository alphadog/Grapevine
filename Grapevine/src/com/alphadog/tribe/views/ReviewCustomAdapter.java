package com.alphadog.tribe.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alphadog.tribe.R;
import com.alphadog.tribe.models.Review;

public class ReviewCustomAdapter extends ArrayAdapter<Review> {

	private List<Review> reviewList = new ArrayList<Review>();
	final Handler uiUpdateHandler = new Handler();
	private Context context;
	
	// a view holder saves the expensive findViewById
	// call every time a view is inflated.
	// as a holder is initialised only once when
	// convertView is null, and then used repeatedly
	private static class ViewHolder {
		TextView text;
		ImageView thumbNailImage;
		ImageView likeImage;
		TextView dateText;
	}

	public ReviewCustomAdapter(Context context, int textViewResourceId, List<Review> list) {
		super(context, textViewResourceId, list);
		this.reviewList = list;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.review, null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.review_text);
			holder.dateText = (TextView) convertView.findViewById(R.id.date_of_review);
			holder.thumbNailImage = (ImageView) convertView.findViewById(R.id.review_image);
			holder.likeImage = (ImageView) convertView.findViewById(R.id.like_image);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Review review = reviewList.get(position);
		if (review != null) {
			//Android has a rule that it shows an ANR (App Not Responding) warning if a UI action
			//takes more than 5 seconds to load. For this it is critical that for all list items
			//we load images in a separate thread, so that we do not stand a chance to see that
			//error on our app. This will ensure that all the heavy lifting is done in separate 
			//thread and UI thread just updates the view when content is ready. 
			final ImageView imgView = (ImageView) holder.thumbNailImage;
			imgView.setImageResource(R.drawable.stub);
			(new AsyncViewImageUpdater(uiUpdateHandler) {
				@Override
				public void doUIUpdateTask(Drawable drawable) {
					if(drawable != null) {
						imgView.setImageDrawable(drawable);
					}
				}
			}).executeUIUpdateAsAsync(context.getString(R.string.mini_url)+review.getImageUrl());
			
			TextView text = (TextView) holder.text;
			text.setText(review.getHeading());

			int imageId = review.isLike() ? R.drawable.comments : R.drawable.exclamation;
			ImageView likeImage = (ImageView) holder.likeImage;
			likeImage.setImageResource(imageId);
			
			TextView reviewDate = (TextView)holder.dateText;
			reviewDate.setText(review.getReviewDateInLocalTimezone());
		}
		return convertView;
	}
	
	public void abortUIThreads(){
		uiUpdateHandler.getLooper().quit();
	}
}
