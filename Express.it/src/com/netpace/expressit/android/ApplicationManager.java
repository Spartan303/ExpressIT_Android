package com.netpace.expressit.android;

import android.app.Application;
import android.content.Context;

import com.netpace.expressit.exception.HeapDumpingUncaughtExceptionHandler;

public class ApplicationManager extends Application {
	
	protected static final String TAG = ApplicationManager.class.getSimpleName();
	
	private static Context context;
	
	public void onCreate() {
		super.onCreate();
		
        Thread.currentThread().setUncaughtExceptionHandler(
                new HeapDumpingUncaughtExceptionHandler(getApplicationInfo().dataDir));
        
		context = getApplicationContext();
	}

	public static Context getAppContext() {
		return context;
	}
}
