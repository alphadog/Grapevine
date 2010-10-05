package com.alphadog.grapevine.views;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

public abstract class AsyncViewImageUpdater {

	private Handler viewHandler;
	private Drawable drawable;
	private static int IO_BUFFER_SIZE = 4*1024;

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

	private Drawable loadImageFromWebOperations(String url) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(new URL(url).openStream(),IO_BUFFER_SIZE);

           final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
           out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
           copy(in, out);
           out.flush();

           final byte[] data = dataStream.toByteArray();
           bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
           return new BitmapDrawable(null, bitmap);
		} catch (Exception e) {
			Log.e("AsyncViewImageUpdater","Error occured while parsing image from online resource. Error is: ",e);
		}
		return null;
	}
	
	private static void copy(InputStream in, OutputStream out) throws IOException {
	    byte[] b = new byte[IO_BUFFER_SIZE];
	    int read;
	    while ((read = in.read(b)) != -1) {
	        out.write(b, 0, read);
	    }
	}

	public abstract void doUIUpdateTask(Drawable drawable);
}
