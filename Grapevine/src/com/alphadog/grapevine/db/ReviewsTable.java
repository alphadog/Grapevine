package com.alphadog.grapevine.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.alphadog.grapevine.models.Review;


public class ReviewsTable implements Table<Review> {
	
	private static String TABLE_NAME = "REVIEWS";
	private SQLiteOpenHelper grapevineDatabase;
	private static String LOG_TAG = "Reviews Table";
	
	public ReviewsTable(SQLiteOpenHelper database) {
		grapevineDatabase = database;
	}

	private static class ReviewCursor extends SQLiteCursor {

		private static final String FIELD_LIST = " id, heading, description, image_url, latitude, longitude, is_like, review_date, username, location_name ";
		private static final String ALL_QUERY = "SELECT "+ FIELD_LIST +" FROM "+ TABLE_NAME +" ORDER BY review_date desc";
		private static final String ID_QUERY = "SELECT "+ FIELD_LIST +" FROM "+ TABLE_NAME +" WHERE id = ?";

		public ReviewCursor (SQLiteDatabase db, SQLiteCursorDriver driver,
				String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		private static class Factory implements SQLiteDatabase.CursorFactory {
			public Cursor newCursor(SQLiteDatabase db,
					SQLiteCursorDriver driver, String editTable,
					SQLiteQuery query) {
				return new ReviewCursor(db, driver, editTable, query);
			}
		}

		private long getReviewId() {
			return getLong(getColumnIndexOrThrow("id"));
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
		
		private String getLocationName() {
			return getString(getColumnIndexOrThrow("location_name"));
		}
		
		private String getUsername() {
			return getString(getColumnIndexOrThrow("username"));
		}
		
		private int isLike() {
			return getInt(getColumnIndexOrThrow("is_like"));
		}	

		private String getReviewDate() {
			return getString(getColumnIndexOrThrow("review_date"));
		}

		public Review getReview() {
			return new Review(getReviewId(), getHeading(), getDescription(),
					getImageUrl(), getLongitude(),getLatitude(), isLike(), 
					getReviewDate(), getUsername(), getLocationName());
		}
	}

	protected List<Review> findReviews(String query, String[] params) {
		Cursor reviewCursor = null;
		List<Review> reviewList = new ArrayList<Review>();
		try {
			reviewCursor = grapevineDatabase.getReadableDatabase().rawQueryWithFactory(new ReviewCursor.Factory(), query, params, null);
			if(reviewCursor != null && reviewCursor.moveToFirst()) {
				do {
					reviewList.add(((ReviewCursor)reviewCursor).getReview());
				} while(reviewCursor.moveToNext());
			}
		} catch(SQLException sqle) {
			Log.e(LOG_TAG, "Could not look up the reviews with params "+ params +". The error is: "+ sqle.getMessage());
		}
		finally {
			if(reviewCursor != null && !reviewCursor.isClosed()) {
				reviewCursor.close();
			}
		}
		return reviewList;
	}

	public List<Review> findAll() {
		return findReviews(ReviewCursor.ALL_QUERY, null);
	}

	public List<Review> findAllWithMaxLimit(int limit) {
		return findReviews(ReviewCursor.ALL_QUERY + " LIMIT "+ limit, null);
	}

	public Review findById(long id) {
		String id_string = Long.toString(id);
		List<Review> reviewList = findReviews(ReviewCursor.ID_QUERY, new String[] {id_string});
		return (reviewList == null || reviewList.isEmpty()) ? null : reviewList.get(0);
	}

	public Review create(Review newReview) {
		if (newReview != null) {
			
			grapevineDatabase.getWritableDatabase().beginTransaction();
			try {
				ContentValues dbValues = new ContentValues();
				dbValues.put("heading", newReview.getHeading());
				dbValues.put("description", newReview.getDescription());
				dbValues.put("image_url", newReview.getImageUrl());
				dbValues.put("longitude", newReview.getLongitude());
				dbValues.put("latitude", newReview.getLatitude());
				dbValues.put("is_like", newReview.isLike() ? 1 : 0);
				dbValues.put("review_date", newReview.getReviewDate());
				dbValues.put("location_name", newReview.getLocationName());
				dbValues.put("username", newReview.getUsername());
				long id = grapevineDatabase.getWritableDatabase().insertOrThrow(getTableName(),
						"location_name", dbValues);
				newReview.setId(id);
				grapevineDatabase.getWritableDatabase().setTransactionSuccessful();
			} catch (SQLException sqle) {
				Log.e(LOG_TAG, "Could not create new review. Exception is :"
						+ sqle.getMessage());
			} finally {
				grapevineDatabase.getWritableDatabase().endTransaction();
			}
		}
		return newReview;
	}

	public String getTableName() {
		return TABLE_NAME;
	}

	public void replaceAllWith(List<Review> reviewList) {
		if(reviewList != null && reviewList.size() > 0) {
			ContentValues dbValues = null;
			SQLiteDatabase writableDatabase = grapevineDatabase.getWritableDatabase();
			writableDatabase.beginTransaction();
			try {
				writableDatabase.delete(TABLE_NAME, null, null);
				
				for(Review eachReview : reviewList) {
					dbValues = new ContentValues();
					dbValues.put("heading", eachReview.getHeading());
					dbValues.put("description", eachReview.getDescription());
					dbValues.put("image_url", eachReview.getImageUrl());
					dbValues.put("longitude", eachReview.getLongitude());
					dbValues.put("latitude", eachReview.getLatitude());
					dbValues.put("is_like", eachReview.isLike() ? 1 : 0);
					dbValues.put("review_date", eachReview.getReviewDate());
					dbValues.put("location_name", eachReview.getLocationName());
					dbValues.put("username", eachReview.getUsername());
					grapevineDatabase.getWritableDatabase().insertOrThrow(getTableName(), "location_name", dbValues);
				}
				
				writableDatabase.setTransactionSuccessful();
			}
			catch(SQLException sqle) {
				Log.e("ReviewsTable", "Error while replacing existing review set in database with new review set. Error is "+ sqle.getMessage());				
			} finally {
				writableDatabase.endTransaction();
			}
		}
	}
}
