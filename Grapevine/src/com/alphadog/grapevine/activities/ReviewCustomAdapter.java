package com.alphadog.grapevine.activities;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

	public ReviewCustomAdapter(Context context, int textViewResourceId, List<Review> list) {
		super(context, textViewResourceId, list);
		this.reviewList = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v==null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.review, null);
		}
		Review review = reviewList.get(position);
		if(review != null) {
			ImageView imgView =(ImageView) v.findViewById(R.id.review_image);
			Drawable drawable = LoadImageFromWebOperations("http://t1.gstatic.com/images?q=tbn:ANd9GcTFlwiKsKy-IfJkF-zmUxKMa-uVxJkYZ2G4MmuRISBJaOKLofY&t=1&usg=__jiXjdZTLq_MTryETgiHOrBsTVjc=");
			imgView.setImageDrawable(drawable);
			TextView text = (TextView) v.findViewById(R.id.review_text);
			text.setText(review.getHeading());
		}
		return v;
	}
	 
	private Drawable LoadImageFromWebOperations(String url)
    {
         try
         {
             InputStream is = (InputStream) new URL(url).getContent();
             Drawable d = Drawable.createFromStream(is, "src name");
             return d;
         }catch (Exception e) {
             System.out.println("Exc="+e);
             return null;
         }
     }

}
