package com.alphadog.grapevine.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.alphadog.grapevine.helpers.Converter;
import com.alphadog.grapevine.models.PendingReview;

public class PendingReviewsTable implements Table<PendingReview> {

	private static String TABLE_NAME = "PENDING_REVIEWS";
	private SQLiteOpenHelper grapevineDatabase;
	private static String LOG_TAG = "Pending Reviews Table";

	public PendingReviewsTable(SQLiteOpenHelper database) {
		grapevineDatabase = database;
	}

	public static class PendingReviewCursor extends SQLiteCursor {

		private static final String FIELD_LIST = "id, heading, description, image_url, image_path, latitude, longitude, creation_date, is_like, errors, status, retries";
		private static final String ALL_QUERY = "SELECT " + FIELD_LIST + " FROM " + TABLE_NAME + " ORDER BY creation_date desc";
		private static final String ID_QUERY = "SELECT " + FIELD_LIST + " FROM " + TABLE_NAME + " WHERE id = ?";

		public PendingReviewCursor(SQLiteDatabase db,
				SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		private static class Factory implements SQLiteDatabase.CursorFactory {
			public Cursor newCursor(SQLiteDatabase db,
					SQLiteCursorDriver driver, String editTable,
					SQLiteQuery query) {
				return new PendingReviewCursor(db, driver, editTable, query);
			}
		}
		
		
		//Getters for column names
		public static String getImagePathFieldName() {
			return "image_path";
		}
		
		public static String getLatitudeFieldName() {
			return "latitude";
		}
		
		public static String getLongitudeFieldName() {
			return "longitude";
		}
		
		
		public static String getLikeFieldName() {
			return "is_like";
		}
		

		public static String getReviewHeadingFieldName() {
			return "heading";
		}
		//End
		
		private long getPendingReviewId() {
			return 	getLong(getColumnIndexOrThrow("id"));
		}
		
		private String getHeading() {
			return getString(getColumnIndexOrThrow("heading"));
		}

		private String getDescription() {
			return getString(getColumnIndexOrThrow("description"));
		}

		private String getImageUrl() {
			return getString(getColumnIndexOrThrow("image_url"));
		}

		private String getLongitude() {
			return getString(getColumnIndexOrThrow("longitude"));
		}

		private String getLatitude() {
			return getString(getColumnIndexOrThrow("latitude"));
		}

		private int isLike() {
			return getInt(getColumnIndexOrThrow("is_like"));
		}

		private String getError() {
			return getString(getColumnIndexOrThrow("errors"));
		}

		private String getStatus() {
			return getString(getColumnIndexOrThrow("status"));
		}

		private long getRetries() {
			return getLong(getColumnIndexOrThrow("retries"));
		}

		public PendingReview getPendingReview() {
			return new PendingReview(getPendingReviewId(), getHeading(),
					getDescription(), getImageUrl(), getLongitude(),
					getLatitude(), isLike(), getError(), getStatus(),
					getRetries());
		}
	}

	protected List<PendingReview> findReviews(String query, String[] params) {
		Cursor pendingReviewCursor = null;
		List<PendingReview> pendingReviewList = new ArrayList<PendingReview>();
		try {
			pendingReviewCursor = grapevineDatabase.getReadableDatabase()
					.rawQueryWithFactory(new PendingReviewCursor.Factory(),
							query, params, null);
			if (pendingReviewCursor != null
					&& pendingReviewCursor.moveToFirst()) {
				do {
					pendingReviewList
							.add(((PendingReviewCursor) pendingReviewCursor)
									.getPendingReview());
				} while (pendingReviewCursor.moveToNext());
			}
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "Could not look up the pending reviews with params "
					+ params + ". The error is: " + sqle.getMessage());
		} finally {
			if (!pendingReviewCursor.isClosed()) {
				pendingReviewCursor.close();
			}
		}
		return pendingReviewList;
	}

	public List<PendingReview> findAll() {
		return findReviews(PendingReviewCursor.ALL_QUERY, null);
	}

	public PendingReview findById(long id) {
		String id_string = Long.toString(id);
		List<PendingReview> pendingReviewList = findReviews(
				PendingReviewCursor.ID_QUERY, new String[] { id_string });
		return (pendingReviewList == null || pendingReviewList.isEmpty()) ? null : pendingReviewList
				.get(0);
	}
	
	public void updateFieldsForId(long id, Map<String, String> valuesToUpdate) {
		Log.i("PendingReviewsTable", "Updating records for pending review with id :"+ id);
		if(id > 0 && valuesToUpdate != null && valuesToUpdate.size() > 0) {
			grapevineDatabase.getWritableDatabase().beginTransaction();
			try {
				ContentValues values = Converter.toContentValues(valuesToUpdate, null);
				grapevineDatabase.getWritableDatabase().update(getTableName(), values, " ID = ?", new String[]{Long.toString(id)});
				grapevineDatabase.getWritableDatabase().setTransactionSuccessful();
			} catch (SQLException sqle) {
				Log.e("Pending Reviews Table", "Error while updating the field for the table. Error is :" + sqle.getMessage());
			} finally {
				grapevineDatabase.getWritableDatabase().endTransaction();
			}
		}
	}

	public PendingReview create(PendingReview newPendingReview) {
		if (newPendingReview != null) {
			grapevineDatabase.getWritableDatabase().beginTransaction();
			try {
				ContentValues dbValues = new ContentValues();
				dbValues.put("id", newPendingReview.getId());
				dbValues.put("heading", newPendingReview.getHeading());
				dbValues.put("description", newPendingReview.getDescription());
				dbValues.put("image_url", newPendingReview.getImageUrl());
				dbValues.put("longitude", newPendingReview.getLongitude());
				dbValues.put("latitude", newPendingReview.getLatitude());
				dbValues.put("is_like", newPendingReview.isLike() ? 1 : 0);
				dbValues.put("errors", newPendingReview.getError());
				dbValues.put("status", newPendingReview.getStatus());
				dbValues.put("retries", newPendingReview.getRetries());
				long id = grapevineDatabase.getWritableDatabase().insertOrThrow(getTableName(), "creation_date", dbValues);
				newPendingReview.setId(id);
				grapevineDatabase.getWritableDatabase().setTransactionSuccessful();
			} catch (SQLException sqle) {
				Log.e(LOG_TAG, "Could not create pending review. Exception is :"  + sqle.getMessage());
			} finally {
				grapevineDatabase.getWritableDatabase().endTransaction();
			}
		}
		return newPendingReview;
	}

	public String getTableName() {
		return TABLE_NAME;
	}
}