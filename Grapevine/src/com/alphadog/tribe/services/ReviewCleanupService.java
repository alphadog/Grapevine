package com.alphadog.tribe.services;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.alphadog.tribe.TribeApplication;
import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.db.PendingReviewsTable;

public class ReviewCleanupService extends WakeEventService {

    private PendingReviewsTable pendingReviewsTable;
    private final String className = this.getClass().getName();

    @Override
    protected void doServiceTask() {
        Log.i(className, "Executing cleanup service");
        initDBVars();
        if (null == pendingReviewsTable) {
            Log.e(className, "Pending reviews table - NULL!");
            return;
        }
        try {
            // First delete all the records which are stale and not going to be uploaded anymore
            pendingReviewsTable.cleanupStaleAndCompletedReviews();
            Log.i(className, "All stale and orphan reviews cleaned up.");

            // Delete images from the images directory which do not have any associated reviews
            cleanupOrphanReviewImages();
            Log.i(className, "All stale and orphan images have also been cleaned up");
        }
        catch (Exception e) {
            Log.e(className, "Error occured while cleaning up records. Error is:", e);
        }
        finally {
            this.stopSelf();
        }
    }
    
    private void initDBVars() {
        TribeDatabase database = ((TribeApplication) getApplication()).getDB();
        if (null == pendingReviewsTable && null != database) pendingReviewsTable = new PendingReviewsTable(database);
    }

    private void cleanupOrphanReviewImages() {
        File directory = getDir("gv_img_cache", Context.MODE_PRIVATE);
        if (null == directory) {
            Log.i(className, "No image cache directory!");
            return;
        }
        
        String[] files = directory.list();
        if (null == files || files.length == 0) {
            Log.i(className, "No files in image cache directory.");
            return;
        }
        
        Log.i(className, "Evaluating list of files for cleanup. Total number of files are :" + files.length);
        for (String file : files) {
            if (file != null && file.length() > 0 && file.contains(".")) {
                Log.i(className, "Evaluating file for delete: " + file);
                long pendingReviewId = Long.parseLong(file.split("\\.")[0]);
                if (pendingReviewsTable.findById(pendingReviewId) == null) {
                    Log.i(this.getClass().getName(), "No pending review with id: " + pendingReviewId);
                    File fileToBeDeleted = new File(directory, file);
                    try {
                        fileToBeDeleted.delete();
                        Log.i(className, "Deleted successfully: " + fileToBeDeleted);
                    }
                    catch (SecurityException se) {
                        Log.e(className, "Could not delete image file in cache dir as permission denied."
                                + fileToBeDeleted, se);
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
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}