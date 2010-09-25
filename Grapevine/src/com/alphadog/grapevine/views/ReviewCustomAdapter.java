package com.alphadog.grapevine.views;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
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
	private Drawable drawable;
	private static class ViewHolder {
		TextView text;
		ImageView image;
	}

	public ReviewCustomAdapter(Context context, int textViewResourceId, List<Review> list) {
		super(context, textViewResourceId, list);
		this.reviewList = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.review, null);
			
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.review_text);
			holder.image = (ImageView) convertView.findViewById(R.id.review_image);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Review review = reviewList.get(position);
		if(review != null) {
			//Android has a rule that it shows an ANR (App Not Responding) warning if a UI action
			//takes more than 5 seconds to load. For this it is critical that for all list items
			//we load images in a separate thread, so that we do not stand a chance to see that
			//error on our app. This will ensure that all the heavy lifting is done in separate 
			//thread and UI thread just updates the view when content is ready. 
			initiateHeavyUIUpdatesInSeparateThread(holder.image, review);
			holder.image.setImageResource(R.drawable.stub);
			
			holder.text.setText(review.getHeading());
		}
		return convertView;
	}
	 
	private Drawable LoadImageFromWebOperations(String url)
    {
         try
         {
             InputStream is = (InputStream) new URL(url).getContent();
             Drawable d = Drawable.createFromStream(is, "src name");
             return d;
         }catch (Exception e) {
        	 Log.e("ReviewCustomAdapter", "Error occured while parsing image from online resource. Error is: " + e.getMessage());
         }
         
         return null;
    }
	
	private void initiateHeavyUIUpdatesInSeparateThread(final ImageView imageView, final Review review) {
	    final Runnable mUpdateResults = new Runnable() {
	        public void run() {
	            updateUI(imageView);
	        }
	    };
		
		Thread t = new Thread() {
			public void run() {
				//new to use the review url here instead of hard coded value.
	            drawable = LoadImageFromWebOperations("http://t1.gstatic.com/images?q=tbn:ANd9GcTFlwiKsKy-IfJkF-zmUxKMa-uVxJkYZ2G4MmuRISBJaOKLofY&t=1&usg=__jiXjdZTLq_MTryETgiHOrBsTVjc=");
	            uiUpdateHandler.post(mUpdateResults);
	        }
	    };
	    t.start();
	}
	
	private void updateUI(ImageView imgView) {
		if(drawable != null) {
			imgView.setImageDrawable(drawable);
		}
	}
	
	public void abortUIThreads(){
		uiUpdateHandler.getLooper().quit();
	}
}
