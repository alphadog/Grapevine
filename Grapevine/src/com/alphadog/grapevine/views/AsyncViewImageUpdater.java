package com.alphadog.grapevine.views;

import java.io.InputStream;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

public abstract class AsyncViewImageUpdater {
	
	private Handler viewHandler;
	private Drawable drawable;
	
	public AsyncViewImageUpdater(Handler viewHandler) {
		this.viewHandler = viewHandler;
	}

	public void executeUIUpdateAsAsync(final String imageUrl) {
		
		final Runnable mUpdateResults = new Runnable() {
	        public void run() {
	        	doUIUpdateTask(drawable);
	        }
	    };
		
		Thread t = new Thread() {
			public void run() {
	            AsyncViewImageUpdater.this.drawable = loadImageFromWebOperations(imageUrl);
	            viewHandler.post(mUpdateResults);
	        }
	    };
	    t.start();	
	}
	
	private Drawable loadImageFromWebOperations(String url)
    {
         try
         {
             InputStream is = (InputStream) new URL(url).getContent();
             return Drawable.createFromStream(is, "src name");
         }catch (Exception e) {
        	 Log.e("ReviewCustomAdapter", "Error occured while parsing image from online resource. Error is: " + e.getMessage());
         }
         
         return null;
    }
	
	public abstract void doUIUpdateTask(Drawable drawable);
}
