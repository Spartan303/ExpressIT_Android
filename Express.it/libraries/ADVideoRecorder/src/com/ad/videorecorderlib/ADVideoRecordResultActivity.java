/*******************************************************************************
 * Copyright 2013 Adnan Urooj (Deminem)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 * 	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ad.videorecorderlib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ad.videorecorderlib.logger.ADLogger;
import com.ad.videorecorderlib.ui.ADCustomVideoView;

@SuppressLint("NewApi")
public class ADVideoRecordResultActivity extends ActionBarActivity {

	protected static final String TAG = ADVideoRecordResultActivity.class.getSimpleName();
	
	private static final String PROCESSED_MEDIA = "MediaResultActivity.PROCESSED_MEDIA";
	
	private Button prDoneBtn;
	
	private ADCustomVideoView videoPreview;
	private Context prContext;
	
	private ADProcessedVideoMedia mProcessedMedia;
	
	public static Intent newInstance(Activity activity, ADProcessedVideoMedia media) {

		Intent intent = new Intent(activity, ADVideoRecordResultActivity.class);
		intent.putExtra(PROCESSED_MEDIA, media);

		return intent;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle("Retake");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		prContext = this.getApplicationContext();
		setContentView(R.layout.media_result);
		
		if (getIntent().getExtras() != null) {
			mProcessedMedia = (ADProcessedVideoMedia) getIntent().getSerializableExtra(PROCESSED_MEDIA);
			ADLogger.debug(TAG, "Result Processed : " + mProcessedMedia);
		}
		
        Point screenDimenions = getDisplayDimensions();
		prDoneBtn = (Button) findViewById(R.id.doneBtn);
		
		videoPreview = (ADCustomVideoView) findViewById(R.id.video_preview);
		videoPreview.setDimensions(screenDimenions.x, screenDimenions.y - screenDimenions.y/3);
		videoPreview.setVideoURI(Uri.parse( mProcessedMedia.getOutputFile().getPath() ));
		
		videoPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(false);
			}
		});
		videoPreview.start();
		
		prDoneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Send the result back to calling activity.
				Intent result = new Intent();
				result.putExtra(ADVideoRecordActivity.RESULT_VIDEO_CAPTURE, mProcessedMedia);
				setResult(ADVideoRecordActivity.RESULT_VIDEO_CAPTURE_PATH_CODE, result);
				
				setResult(RESULT_OK, result);
				finish();
				ADVideoRecordResultActivity.this.overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_out_right);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.menu_video_preview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			this.onBackPressed();
			return true;
		} else if (itemId == R.id.menu_item_edit) {
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onPrepareOptionsMenu(menu);
	}

	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(PROCESSED_MEDIA, mProcessedMedia);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		if (videoPreview != null)
			videoPreview = null;

		super.onDestroy();
	}
	
	@Override
	protected void onStop() {

		if (videoPreview != null)
			videoPreview.stopPlayback();
		
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	        Point screenDimenions = getDisplayDimensions();
	        videoPreview.setDimensions(screenDimenions.x, screenDimenions.y - screenDimenions.y/3);
	        videoPreview.getHolder().setFixedSize(screenDimenions.x, screenDimenions.y - screenDimenions.y/4);

	    } else {
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

	        Point screenDimenions = getDisplayDimensions();
	        videoPreview.setDimensions(screenDimenions.x, screenDimenions.y - screenDimenions.y/3);
	        videoPreview.getHolder().setFixedSize(screenDimenions.x, screenDimenions.y - screenDimenions.y/3);
	    }
	}
	
	private Point getDisplayDimensions() {
		
		Point size = new Point();
		Display display = getWindowManager().getDefaultDisplay();
		
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        	display.getSize(size);
        } 
        else {
        	size.set(display.getWidth(), display.getHeight());
        }
        
		return size;
	}
}
