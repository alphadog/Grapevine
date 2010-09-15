package com.alphadog.grapevine;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Dashboard extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Log.v("cliekced", "clicked");
				setContentView(R.layout.ooga);
			}
		});

        setContentView(R.layout.dashboard);

    }
}