package com.alphadog.tribe.services;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alphadog.tribe.R;
import com.alphadog.tribe.activities.NewReviewActivity;
import com.alphadog.tribe.exceptions.TwitterCredentialsBlankException;
import com.alphadog.tribe.views.NotificationCreator;
import com.harrison.lee.twitpic4j.TwitPic;
import com.harrison.lee.twitpic4j.TwitPicResponse;
import com.harrison.lee.twitpic4j.exception.InvalidUsernameOrPasswordException;
import com.harrison.lee.twitpic4j.exception.TwitPicException;

public class TweetWithImageUpload {

	private String username;
	private String password;
	private boolean includeTweet = false;
	private NotificationCreator notificationCreator;
	private Context context;
	
	private static int INVALID_CREDENTIALS_NOTIFICATION = 666;
	
	public TweetWithImageUpload(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.username = preferences.getString("twitter_username", "");
		this.password = preferences.getString("twitter_password", "");
		this.includeTweet = preferences.getBoolean("tweet_always", false);
		this.notificationCreator = new NotificationCreator(context, 0);
		this.context = context;
	}
	
	//Idea is that user will always need his twitter credentials if he need to upload pictures. And since he is using twitter credentials to 
	//upload picture, he might as well tweet the reviews. So we give a preference that, do you want to tweet always, all the reviews that
	//you post? If he says yes, then we also tweet his review excerpt else we just upload the picture. But his twitter credentials
	//need to be setup in preferences. Using that the tweet will be sent and picture will be uploaded, else we'll throw TwitterCredentialsNull
	//error which can be handled in view code and error can be displayed appropriately.
	public String uploadImageFor(String filePath, String tweetMessage) 
	throws TwitterCredentialsBlankException {
		if(filePath != null  && filePath.length() > 0) {
			
			File image = new File(filePath);
			
			if(username == null || username.length() < 1 || password == null || password.length() < 1) {
				throw new TwitterCredentialsBlankException("Twitter credentials are not correctly setup in preferences");
			}
			
			// Create TwitPic object and allocate TwitPicResponse object
			TwitPic tpRequest = new TwitPic(username, password);
			TwitPicResponse tpResponse = null;
			Log.i("TweetWithImageUpload", "Got the fileData as " + image);

			// Make request and handle exceptions                           
			try 
			{
				if(includeTweet && tweetMessage != null) {
					tpResponse = tpRequest.uploadAndPost(image, tweetMessage);
				} else {
					tpResponse = tpRequest.upload(image);
				}
			} catch (IOException e) {
				Log.e("TweetWithImageUpload", "Error occured while uploading image. Error is:" ,e);
			} catch(InvalidUsernameOrPasswordException iuope) {
				Log.e("TweetWithImageUpload", "Could not upload review because supplied username and password are not valid.", iuope);
				notificationCreator.createNotification(INVALID_CREDENTIALS_NOTIFICATION, this.context.getString(R.string.upload_error_bar_message), this.context.getString(R.string.upload_error_heading), this.context.getString(R.string.upload_error_msg), NewReviewActivity.getCurrentTime(), true);
			} catch (TwitPicException e) {
				Log.e("TweetWithImageUpload", "TwitPic threw an exception while uploading image. Error is:", e);
			}
	
			// If we got a response back, print out response variables                              
			if(tpResponse != null)
			{
				return tpResponse.getMediaAid();
			}
		}
		return null;
	}
}
