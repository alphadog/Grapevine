package com.alphadog.tribe.views;

import android.app.ProgressDialog;
import android.content.Context;

public abstract class TaskWithProgressIndicator {
	
	private ProgressDialog progressDialog;
	
	public TaskWithProgressIndicator(Context context, String message) {
		this.progressDialog = new ProgressDialog(context);
		this.progressDialog.setMessage(message);
	}
	
	public void executeTaskWithProgressIndicator() {
		this.progressDialog.show();
		try {
			executeTask();
		}
		finally {
			this.progressDialog.dismiss();
		}
	}

	public abstract void executeTask();
}
