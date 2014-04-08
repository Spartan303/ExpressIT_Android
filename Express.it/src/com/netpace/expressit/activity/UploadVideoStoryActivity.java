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

package com.netpace.expressit.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ad.mediasharing.ADMediaSharingUtil;
import com.ad.mediasharing.ADRequestCallbackHandler;
import com.ad.mediasharing.AmazonS3Settings;
import com.ad.videorecorderlib.ADProcessedVideoMedia;
import com.ad.videorecorderlib.ui.ADCustomVideoView;
import com.amazonaws.org.apache.http.HttpStatus;
import com.kbeanie.imagechooser.api.FileUtils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.netpace.expressit.R;
import com.netpace.expressit.android.ui.TypefaceEditText;
import com.netpace.expressit.constants.AppConstants;
import com.netpace.expressit.model.Media;
import com.netpace.expressit.model.Media.MediaTypeEnum;
import com.netpace.expressit.model.Meta;
import com.netpace.expressit.utils.StringUtil;
import com.netpace.expressit.utils.Util;


public class UploadVideoStoryActivity extends ActionBarActivity {

	protected static final String TAG = UploadVideoStoryActivity.class.getSimpleName();
	
	public final static String VIDEO_PATH = TAG+"path";
	
	private ADCustomVideoView videoView;
	private Button playBtn;
	
	private TypefaceEditText titleTextView;
	
	private LinearLayout videoSlicePanal;
	
	private Button button_save;
	
	private ProgressDialog dialog = null;
	
	private String video_filePath;
	
	private String thumb_filePath;
	
	private static String fileKey;
	
	private ADProcessedVideoMedia videoMedia;
	
	private boolean isGalleryVideo = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_video_story);
		
		getSupportActionBar().setTitle("retake");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		Intent intent = getIntent();
		isGalleryVideo = intent.getBooleanExtra("isGalleryVideo", false);
		if(!isGalleryVideo){
			videoMedia = (ADProcessedVideoMedia) intent.getSerializableExtra(VIDEO_PATH);
			video_filePath = videoMedia.getOutputFile().getPath();
		}else{
			video_filePath = intent.getStringExtra(VIDEO_PATH);
		}
		
		Point screenDimenions = getDisplayDimensions();
		
		videoView = (ADCustomVideoView) findViewById(R.id.videoView);
		videoView.setDimensions(screenDimenions.x, screenDimenions.y - screenDimenions.y/3);
		videoView.setVideoURI(Uri.parse( video_filePath ));
		
		playBtn = (Button) findViewById(R.id.playButton);
		playBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playVideoClip();
			}
		});
		
		titleTextView = (TypefaceEditText) findViewById(R.id.story_title_txt_field);

		button_save = (Button) findViewById(R.id.shareButton);
		button_save.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Util.showToast(R.string.in_progress);
			}
		});
		
		if(!isGalleryVideo){
			videoSlicePanal = (LinearLayout) findViewById(R.id.videoSlicePanal);
			String sessionDirPath = videoMedia.getDirectoryPath() + "/";
			File dir = new File(sessionDirPath);
			String[] fileNames = dir.list();
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(140, 140);
			lp.setMargins(3, 3, 3, 3);
			for (int i = 0; i < fileNames.length; i++) {
				if(fileNames[i] == "output.mp4") continue;
				String filePath = sessionDirPath + fileNames[i];
				
				ADCustomVideoView vv = new ADCustomVideoView(this);
				vv.setLayoutParams(lp);
				vv.setDimensions(140, 140);
				vv.setVideoURI(Uri.parse(filePath));
				
				videoSlicePanal.addView(vv);
			}
		}

	}

	
	
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_video_story, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;

		case R.id.menu_item_video_gallery:
			if (isValidate()) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(titleTextView.getWindowToken(), 0);
				
				getMediaKeyFromRemoteServer();
			}
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	protected void onDestroy() {
		
		if (videoView != null)
			videoView = null;

		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		
		stopVideoClip();
		
		super.onStop();
	}
	
    
    private void playVideoClip() {
    	
    	if (video_filePath != null) {
    		videoView.setVideoURI(Uri.parse( video_filePath ));
    		playBtn.setVisibility(View.GONE);
    		
    		videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					playBtn.setVisibility(View.VISIBLE);
					playBtn.setFocusable(true);
				}
			});

			videoView.start();
    	}
    }
    
    private void stopVideoClip() {
    	
    	if (videoView != null) {
    		playBtn.setVisibility(View.VISIBLE);
			videoView.stopPlayback();
    	}
    }
    
    private boolean isValidate() {
    	
    	boolean validateFlag = true;
    	String errorMsg = "";
    	
    	if (titleTextView == null || StringUtil.isEmptyOrNull(titleTextView.getText().toString())) {
    		validateFlag = false;
    		errorMsg = getString(R.string.validation_title_error);
    	}
    	else if (StringUtil.isEmptyOrNull(video_filePath)) {
    		validateFlag = false;
    		errorMsg = getString(R.string.validation_spotlight_error);
    	}
    	
    	if (!validateFlag) {
			Toast.makeText(UploadVideoStoryActivity.this, errorMsg,
					Toast.LENGTH_SHORT).show();
    	}
    	
    	return validateFlag;
    }
    
    private void showProgressDialog(){
    	dialog = new ProgressDialog(this);
		dialog.setMessage("uploading...");
		dialog.setCancelable(false);
		dialog.show();
    }
    
    private void getMediaKeyFromRemoteServer(){
    	
    	showProgressDialog();
    	dialog.setMessage("Getting key...");
    	ADMediaSharingUtil.getRestClient(UploadVideoStoryActivity.this)
		.load(AppConstants.DOMAIN_URL+AppConstants.GET_KEY_URL)
		.asString()
		.withResponse()
		.setCallback(new FutureCallback<Response<String>>() {
			
			@Override
			public void onCompleted(Exception e, Response<String> result) {
				if(e == null){
					fileKey = result.getResult().toString();
					Log.i(TAG, fileKey);
					uploadMediaToTVM();
				}else{
					if(dialog.isShowing()) dialog.dismiss();
					Toast.makeText(UploadVideoStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
    }
    
    private void uploadMediaToTVM(){
    	
    	dialog.setMessage("Uploading video on TVM...");
    	
    	AmazonS3Settings amazonS3Settings = Util.getAmazonS3Setting(fileKey+"."+FileUtils.getFileExtension(video_filePath));
		ADMediaSharingUtil.uploadMediaOnAmazonS3Cloud(UploadVideoStoryActivity.this, amazonS3Settings, video_filePath, 
				new ADRequestCallbackHandler<String, Exception, Integer>() {

			@Override
			public void onComplete(String result,Integer statusCode) {
				Log.d(TAG, "Success : " + result);
				if(statusCode == HttpStatus.SC_OK){
					uploadThumbnailToTVM();
				}
			}

			@Override
			public void onFailed(Exception error) {
				Log.d(TAG, "Fail : " + error);
				if(dialog.isShowing()) dialog.dismiss();
				Toast.makeText(UploadVideoStoryActivity.this, "Uploaded fail", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProgress(Integer progress) {
				Log.i(TAG, progress.toString());
				
			}
		});
    }
    
    private void uploadThumbnailToTVM(){
    	
    	if(StringUtil.isEmptyOrNull(thumb_filePath)){
    		thumb_filePath = Util.getThumbnailPathFromVideo(video_filePath, 
    				fileKey, getApplicationContext()).getPath();
    	}
    	
    	dialog.setMessage("Uploading thumbnail on TVM...");
    	
    	AmazonS3Settings amazonS3Settings = Util.getAmazonS3Setting(fileKey+"-thumb."+FileUtils.getFileExtension(thumb_filePath));
    	ADMediaSharingUtil.uploadMediaOnAmazonS3Cloud(UploadVideoStoryActivity.this, amazonS3Settings, thumb_filePath, new ADRequestCallbackHandler<String, Exception, Integer>() {

			@Override
			public void onComplete(String result, Integer statusCode) {
				Log.d(TAG, "Success : " + result);
				if(statusCode == HttpStatus.SC_OK){
					publishMediaOnRemoteServer();
				}
			}

			@Override
			public void onFailed(Exception error) {
				Log.d(TAG, "Fail : " + error);
				if(dialog.isShowing()) dialog.dismiss();
				Toast.makeText(UploadVideoStoryActivity.this, "Uploaded fail", Toast.LENGTH_SHORT).show();
				
			}

			@Override
			public void onProgress(Integer progress) {
				Log.i(TAG, progress.toString());
				
			}
		});
    }
    
    private void publishMediaOnRemoteServer(){
    	
    	dialog.setMessage("Publishing on XIT...");
    	
    	Media media = new Media();
    	media.setMediaShortURL(fileKey);
    	media.setMediaName(fileKey+"."+FileUtils.getFileExtension(video_filePath));
    	media.setMediaCaption(titleTextView.getText().toString());
    	media.setMediaType(MediaTypeEnum.VIDEO);
    	
    	Meta meta = new Meta();
    	meta.setThumb(fileKey+"-thumb."+FileUtils.getFileExtension(thumb_filePath));
    	media.setMeta(meta);
    	
    	
    	ADMediaSharingUtil.getRestClient(UploadVideoStoryActivity.this)
		.load(AppConstants.DOMAIN_URL+AppConstants.PUBLISH_MEDIA_URL)
		.setJsonObjectBody(media)
		.asString()
		.withResponse()
		.setCallback(new FutureCallback<Response<String>>() {

			@Override
			public void onCompleted(
					Exception e,
					Response<String> result) {
				if(dialog.isShowing()) dialog.dismiss();
				if(e == null && result.getHeaders().getResponseCode() == HttpStatus.SC_OK){
					Toast.makeText(UploadVideoStoryActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent( UploadVideoStoryActivity.this, SlideMenuActivity.class );  
				    intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );  
				    startActivity( intent );
				}else{
					Toast.makeText(UploadVideoStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});	
    }
    

	@SuppressLint("NewApi")
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
