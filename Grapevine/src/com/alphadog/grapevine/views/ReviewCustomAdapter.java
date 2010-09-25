package com.alphadog.grapevine.views;

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

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.models.Review;

public class ReviewCustomAdapter extends ArrayAdapter<Review> {

	private List<Review> reviewList = new ArrayList<Review>();
	final Handler uiUpdateHandler = new Handler();

	public ReviewCustomAdapter(Context context, int textViewResourceId, List<Review> list) {
		super(context, textViewResourceId, list);
		this.reviewList = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.review, null);			
		} 
		
		Review review = reviewList.get(position);
		if(review != null) {
			//Android has a rule that it shows an ANR (App Not Responding) warning if a UI action
			//takes more than 5 seconds to load. For this it is critical that for all list items
			//we load images in a separate thread, so that we do not stand a chance to see that
			//error on our app. This will ensure that all the heavy lifting is done in separate 
			//thread and UI thread just updates the view when content is ready. 
			final ImageView imgView =(ImageView) convertView.findViewById(R.id.review_image);
			(new AsyncViewImageUpdater(uiUpdateHandler) {
				@Override
				public void doUIUpdateTask(Drawable drawable) {
					if(drawable != null) {
						imgView.setImageDrawable(drawable);
					}
				}
			}).executeUIUpdateAsAsync("http://t1.gstatic.com/images?q=tbn:ANd9GcTFlwiKsKy-IfJkF-zmUxKMa-uVxJkYZ2G4MmuRISBJaOKLofY&t=1&usg=__jiXjdZTLq_MTryETgiHOrBsTVjc=");
			imgView.setImageResource(R.drawable.stub);
			TextView text = (TextView) convertView.findViewById(R.id.review_text);
			text.setText(review.getHeading());
		}
		return convertView;
	}
	
	public void abortUIThreads(){
		uiUpdateHandler.getLooper().quit();
	}
}
