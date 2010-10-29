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

import static com.alphadog.tribe.helpers.StringHelpers.isBlank;

public class TweetAndPicUploader {

    private String username;
    private String password;
    private Context context;

    private static int INVALID_CREDENTIALS_NOTIFICATION = 666;

    public TweetAndPicUploader(Context context) throws TwitterCredentialsBlankException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        username = preferences.getString("twitter_username", "").trim();
        password = preferences.getString("twitter_password", "").trim();
        this.context = context;
        if (!areCredentialsAcceptable(this.username, this.password)) {
            throw new TwitterCredentialsBlankException(
                    "Twitter credentials are not correctly setup in preferences - likely blank?");
        }
    }

    private static boolean areCredentialsAcceptable(String username, String password) {
        return (!isBlank(username) && !isBlank(password));
    }

    // Idea is that user will always need his twitter credentials if he needs to upload pictures.
    // And since he is using twitter credentials to upload picture, he might as well tweet the reviews.
    // So we give a preference that, do you want to tweet always, all the reviews that you post? 
    // If he says yes, then we also tweet his review except else we just upload the picture.
    // But his twitter credentials need to be setup in preferences. Using that the tweet will be sent
    // and picture will be uploaded, else we'll throw TwitterCredentialsBlankException error which can
    // be handled in view code and error can be displayed appropriately.
    public String uploadImageFor(String filePath, String tweetMessage) {
        if (isBlank(filePath)) {
            Log.i("TweetAndPicUploader", "filePath is empty!");
            return null;
        }
        
        Log.i("TweetAndPicUploader", "Got the file path as " + filePath);
        
        File image = new File(filePath);
        // Create TwitPic object and allocate TwitPicResponse object
        TwitPic tpRequest = new TwitPic(username, password);
        TwitPicResponse tpResponse = null;

        // Make request and handle exceptions
        try {
            if (tweetMessage != null) {
                Log.i(this.getClass().getName(), "No message to post?");
                tpResponse = tpRequest.uploadAndPost(image, tweetMessage);
            }
            else {
                Log.i(this.getClass().getName(), "Posting message: " + tweetMessage);
                tpResponse = tpRequest.upload(image);
            }
        }
        catch (IOException e) {
            Log.e("TweetAndPicUploader", "Error occured while uploading image. Error is:", e);
        }
        catch (InvalidUsernameOrPasswordException iuope) {
            Log.e("TweetAndPicUploader",
                    "Could not upload review because supplied username and password are not valid.", iuope);
            (new NotificationCreator(this.context)).create(INVALID_CREDENTIALS_NOTIFICATION,
                    this.context.getString(R.string.upload_error_bar_message),
                    this.context.getString(R.string.upload_error_heading),
                    this.context.getString(R.string.upload_error_msg), NewReviewActivity.getCurrentTime(), true, 0);
        }
        catch (TwitPicException e) {
            Log.e("TweetAndPicUploader", "TwitPic threw an exception while uploading image. Error is:", e);
        }

        // If we got a response back, print out response variables
        if (tpResponse != null) { return tpResponse.getMediaAid(); }
        Log.i(this.getClass().getName(), "Got NULL response from twitpic service!");
        return null;
    }
}