package com.netpace.expressit.context;

import android.app.Application;
import android.content.Context;

import com.ad.videorecorderlib.settings.Config;

public class AppContext extends Application {
	
	protected static final String TAG = AppContext.class.getSimpleName();
	
	private static Context context;
	
	public void onCreate() {
		super.onCreate();
		
		AppContext.context = getApplicationContext();
		
		defaultSettings();
	}

	public static Context getAppContext() {
		return AppContext.context;
	}
	
	public void defaultSettings() {
		
		/**
		 * Recorder Settings
		 */
		Config.getInstance().setDebugMode(true);
	}
}
