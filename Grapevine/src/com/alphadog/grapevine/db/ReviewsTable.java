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

		private static final String FIELD_LIST = " id, heading, description, image_id, map_location_id, creation_date, is_gripe ";
		private static final String ALL_QUERY = "SELECT "+ FIELD_LIST +" FROM "+ TABLE_NAME +" ORDER BY creation_date desc";
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

		private long getImageId() {
			return getInt(getColumnIndexOrThrow("image_id"));
		}

		private long getMapLocationId() {
			return getInt(getColumnIndexOrThrow("map_location_id"));
		}

		private int isGripe() {
			return getInt(getColumnIndexOrThrow("is_gripe"));
		}	

		public Review getReview() {
			return new Review(getReviewId(), getHeading(), getDescription(),
					getImageId(), getMapLocationId(), isGripe());
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
			if(!reviewCursor.isClosed()) {
				reviewCursor.close();
			}
		}
		return reviewList;
	}


	public List<Review> findAll() {
		return findReviews(ReviewCursor.ALL_QUERY, null);
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
				dbValues.put("image_id", newReview.getImageId());
				dbValues.put("map_location_id", newReview.getMapLocationId());
				dbValues.put("is_gripe", newReview.isGripe() ? 1 : 0);
				long id = grapevineDatabase.getWritableDatabase().insertOrThrow(getTableName(),
						"creation_date", dbValues);
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
}
