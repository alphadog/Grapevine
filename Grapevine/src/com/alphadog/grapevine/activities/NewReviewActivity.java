package com.alphadog.grapevine.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.PendingReviewsTable;
import com.alphadog.grapevine.models.PendingReview;
import com.alphadog.grapevine.services.LocationUpdateTrigger;
import com.alphadog.grapevine.services.ReviewUploadService;
import com.alphadog.grapevine.services.LocationUpdateTrigger.LocationResultExecutor;

public class NewReviewActivity extends Activity {
	
	private GrapevineDatabase database;
	private PendingReviewsTable pendingReviewTable;
	private SurfaceView preview=null;
	private SurfaceHolder previewHolder=null;
	private Camera camera=null;
	private byte[] pictureData;
	private long reviewId;

	private static String localtimezone = new Time().timezone;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//initialize pending review table wrapper
		database = new GrapevineDatabase(this);
		pendingReviewTable = new PendingReviewsTable(database);
		
		//generate the new pending review's id
		Time reviewTime = new Time();
		reviewTime.setToNow();
		//This is the id used across all the operations to ensure that
		//we are updating params against same review. This is the pivotal 
		//identifier for the new review that we are going to create.
		reviewId = reviewTime.toMillis(true);
		
		//create blank pending review in database
		createBlankPendingReview();
		
		//call for a location update
		updateLocationInBackground();
		
		//update the UI
		setContentView(R.layout.new_review);

		//bind the components on UI
		bindViewComponents();
		
		if(twitterCredentialsProvided()) {
			//update UI with camera layover.
			initalizeCameraToTakePicture();
		} else {
			((SurfaceView)findViewById(R.id.preview)).setVisibility(View.GONE);
			((Button)findViewById(R.id.click_button)).setVisibility(View.GONE);
		}
		
		//And we are done!!
	}
	

	private boolean twitterCredentialsProvided() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		return (preferences.getString("twitter_username", "").trim().length() > 0 && preferences.getString("twitter_password", null).trim().length() > 0); 
	}

	private void bindViewComponents() {
		
		EditText reviewText = (EditText) findViewById(R.id.review_text);
		//A review of max 200 chars.
		int maxLength = 200;
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(maxLength);
		reviewText.setFilters(FilterArray);
		
		Button photoCaptureButton = (Button) findViewById(R.id.click_button);
		photoCaptureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				takePicture();
			}
		});
		
		Button submitButton = (Button) findViewById(R.id.review_submit);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("NewReviewActivity", "Review submitted. Updating details and picture.");
				//save the review
				updateReviewDetails();

				//Done with activity
				NewReviewActivity.this.finish();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
		
	}
	
	private void initalizeCameraToTakePicture() {
		Log.i("NewReviewActivity", "Initializing the camera surface now");
		preview=(SurfaceView)findViewById(R.id.preview);
		previewHolder=preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_CAMERA || keyCode==KeyEvent.KEYCODE_SEARCH) {
			 takePicture();
			 return(true);
		}
		return(super.onKeyDown(keyCode, event));
	}

	private void takePicture() {
		 camera.takePicture(null, null, photoCallback);
	}

	SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			camera=Camera.open();

			try {
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {
				Log.e("NewReviewActivity","Exception in setPreviewDisplay()", t);
				Toast.makeText(NewReviewActivity.this, t.getMessage(),Toast.LENGTH_LONG).show();
			}
		}
		
	    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	        final double ASPECT_TOLERANCE = 0.05;
	        double targetRatio = (double) w / h;
	        if (sizes == null) return null;

	        Size optimalSize = null;
	        double minDiff = Double.MAX_VALUE;

	        int targetHeight = h;

	        // Try to find an size match aspect ratio and size
	        for (Size size : sizes) {
	            double ratio = (double) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - targetHeight) < minDiff) {
	                optimalSize = size;
	                minDiff = Math.abs(size.height - targetHeight);
	            }
	        }

	        // Cannot find the one match the aspect ratio, ignore the requirement
	        if (optimalSize == null) {
	            minDiff = Double.MAX_VALUE;
	            for (Size size : sizes) {
	                if (Math.abs(size.height - targetHeight) < minDiff) {
	                    optimalSize = size;
	                    minDiff = Math.abs(size.height - targetHeight);
	                }
	            }
	        }
	        return optimalSize;
	    }

		public void surfaceChanged(SurfaceHolder holder,int format, int width,int height) {
			Camera.Parameters parameters=camera.getParameters();

			List<Size> sizes = parameters.getSupportedPreviewSizes();
			Size optimalSize = getOptimalPreviewSize(sizes, width, height);
			parameters.setPreviewSize(optimalSize.width, optimalSize.height);
			parameters.setPictureFormat(PixelFormat.JPEG);
			camera.setParameters(parameters);
			camera.startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i("NewReviewActivity", "Surface destroy called and so stopping preview");
			camera.stopPreview();
			camera.release();
			camera=null;
		}
	};

	Camera.PictureCallback photoCallback=new Camera.PictureCallback() {
		 public void onPictureTaken(byte[] data, Camera camera) {
			 Log.i("NewReviewActivity", "Callback for picture click happened. Storing picture data in session; so that we can upload the picture later");
			 NewReviewActivity.this.pictureData = data;
			 camera.startPreview();
		 }
	};
	
	private void updateLocationInBackground() {
		//we'll wait for 2 minutes
		new LocationUpdateTrigger(NewReviewActivity.this, 120000L, new LocationResultExecutor() {
			@Override
			public void executeWithUpdatedLocation(Location location) {
				try {
					//once we have the location, then lets update the pending review with the
					//user location. 
					Map<String, String> valuesMap = new HashMap<String, String>();
					valuesMap.put(PendingReviewsTable.PendingReviewCursor.getLatitudeFieldName(), Double.toString(location.getLatitude()));
					valuesMap.put(PendingReviewsTable.PendingReviewCursor.getLongitudeFieldName(), Double.toString(location.getLongitude()));

					//since this runs in a different thread and so does saving of a photo, we need to ensure
					//that the access to database is synchronized, else it'll throw database is locked error.
					synchronized(pendingReviewTable) {
						pendingReviewTable.updateFieldsForId(reviewId, valuesMap);
					}
					Log.i("NewReviewActivity", "Updating location as new review activity was initiated. New Location is " + location);
					
					//Fire up service to upload the review to net
					ReviewUploadService.acquireStaticLock(NewReviewActivity.this);
					startService(new Intent(NewReviewActivity.this, ReviewUploadService.class));
				} catch (Exception e) {
					Log.e("NewActivityLocationThread", "Error while updating location for new review", e);
				}
			}
		}).fetchLatestLocation();
	}

	private void createBlankPendingReview() {
		//Create a blank copy in database that all the other threads can update with information
		Time currentTime = new Time("UTC");
		currentTime.setToNow();
		currentTime.switchTimezone(localtimezone);
		synchronized(pendingReviewTable) {
			pendingReviewTable.create(new PendingReview(reviewId, "", "", "", "", "","", 1, currentTime.format3339(false),"", PendingReviewsTable.INITIAL_STATUS, 0));
		}
	}
	
	private void updateReviewDetails() {
		RadioButton goodReview = (RadioButton) findViewById(R.id.good_review);
		int like = goodReview.isChecked() ? 1 : 0;
		String reviewText = ((EditText) findViewById(R.id.review_text)).getText().toString();
		String imagePath = getImagePath();
		
		Map<String, String> valuesToUpdate = new HashMap<String, String>();
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getReviewHeadingFieldName(), reviewText);
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getLikeFieldName(), Integer.toString(like));
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getImagePathFieldName(), imagePath);
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getStatusFieldName(), PendingReviewsTable.PENDING_STATUS);
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getRetriesFieldName(), Long.toString(0L));
		synchronized(pendingReviewTable) {
			pendingReviewTable.updateFieldsForId(reviewId, valuesToUpdate);
		}
	}


	private String getImagePath() {
		if(pictureData != null) {
			//create a new file with review id as it's name
			String photoName = reviewId + ".jpg";
			File photo=new File(NewReviewActivity.this.getDir("gv_img_cache", Context.MODE_PRIVATE), photoName);
	
			//Delete the photo that exists with same name.
			//Should never happen in ideal scenario
			if (photo.exists()) {
				photo.delete();
			}
	
			try {
				FileOutputStream fos=new FileOutputStream(photo.getPath());
				fos.write(pictureData);
				fos.close();
	
				//update pending review record with the path
				return photo.getPath();
			}
			catch (java.io.IOException e) {
				Log.e("SavePhotoTask", "Exception in photoCallback for photo name :"+ photoName, e);
			}
		}
		return(null);
	}
}
