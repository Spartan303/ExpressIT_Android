package com.netpace.expressit.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import com.netpace.expressit.R;
import com.netpace.expressit.utils.Util;

public class SplashScreenActivity extends ActionBarActivity {

	
	public static final String TAG = SplashScreenActivity.class.getName(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		getSupportActionBar().hide();
		appStartUp();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
	
	private void appStartUp(){
		
		if(!Util.isNetworkAvailable()){
			Util.showToast(R.string.no_internet_available);
		}
		doInit();
	}
	
	
	private void doInit(){
		
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SplashScreenActivity.this, SlideMenuActivity.class);
				startActivity(intent);
				
				finish();
			}
		}, 3000);
	}
}
