package com.alphadog.tribe.services;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.db.PendingReviewsTable;

public class ReviewCleanupService extends WakeEventService {

	private TribeDatabase database;
	private PendingReviewsTable pendingReviewsTable;
	
	@Override
	public void doServiceTask() {
		Log.i(this.getClass().getName(), "Executing cleanup service");
		try 
		{
			database = new TribeDatabase(this);
			pendingReviewsTable = new PendingReviewsTable(database);
			if(pendingReviewsTable != null) {
				//First delete all the records which are stale and not going to be
				//uploaded anymore
				cleanupPendingReviews();
				Log.i(this.getClass().getName(), "All stale and orphan reviews cleaned up.");
				
				//Now Delete all the images file from the image directory; which
				//do not have corresponding reviews against them
				cleanupOrphanReviewImages();
				Log.i(this.getClass().getName(), "All stale and orphan images have also been cleaned up");
			}
		} catch(Exception e) {
			Log.e(this.getClass().getName(), "Error occured while cleaning up records. Error is:", e);
		}
		finally {
			this.stopSelf();	
		}	
		
	}

	private void cleanupOrphanReviewImages() {
		File directory = getDir("gv_img_cache", Context.MODE_PRIVATE);
		Log.i(this.getClass().getName(), "Obtained image cache directory");
		if(directory != null) {
			long pendingReviewId = 0;
			File fileToBeDeleted;
			String[] fileLists = directory.list();
			if(fileLists != null && fileLists.length != 0) {
				Log.i(this.getClass().getName(), "Evaluating list of files for cleanup. Total number of files are :" + fileLists.length);
				for(String eachFile : fileLists) {
					if(eachFile != null && eachFile.length() > 0 && eachFile.contains(".")) {
						Log.i(this.getClass().getName(), "Evaluation file for delete: "+ eachFile);
						pendingReviewId = Long.parseLong(eachFile.split("\\.")[0]);
						if(pendingReviewsTable.findById(pendingReviewId) == null) {
							fileToBeDeleted = new File(directory, eachFile);
							try {
								fileToBeDeleted.delete();
								Log.i(this.getClass().getName(), "Deleted successfully: " + fileToBeDeleted);
							} catch(SecurityException se) {
								Log.e(this.getClass().getName(), "Could not delete image file in cache dir as permission denied." + fileToBeDeleted, se);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ReviewsSyncService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private void cleanupPendingReviews() {
		pendingReviewsTable.cleanupStaleAndCompletedReviews();
	}

}
