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

import static com.netpace.expressit.constants.AppConstants.LOCAL_UPLOAD_MEDIA_DIR;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.ad.mediasharing.ADMediaSharingUtil;
import com.ad.mediasharing.ADRequestCallbackHandler;
import com.ad.mediasharing.AmazonS3Settings;
import com.ad.videorecorderlib.ADProcessedVideoMedia;
import com.ad.videorecorderlib.ADVideoRecordActivity;
import com.ad.videorecorderlib.ui.ADCustomVideoView;
import com.amazonaws.org.apache.http.HttpStatus;
import com.beanie.imagechooser.api.ChooserType;
import com.beanie.imagechooser.api.ChosenVideo;
import com.beanie.imagechooser.api.FileUtils;
import com.beanie.imagechooser.api.VideoChooserListener;
import com.beanie.imagechooser.api.VideoChooserManager;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.netpace.expressit.R;
import com.netpace.expressit.android.ui.TypefaceEditText;
import com.netpace.expressit.constants.AppConstants;
import com.netpace.expressit.model.Media;
import com.netpace.expressit.model.Media.MediaTypeEnum;
import com.netpace.expressit.utils.StringUtil;
import com.netpace.expressit.utils.Util;

public class UploadVideoStoryActivity extends ActionBarActivity implements
	VideoChooserListener {

	protected static final String TAG = UploadVideoStoryActivity.class.getSimpleName();
	
	private VideoChooserManager videoChooserManager;
	
	private ADCustomVideoView videoView;
	private Button playBtn;
	
	private TypefaceEditText titleTextView;
	
	private TypefaceEditText descTextView;
	
	private Button button_share;
	
	private ProgressDialog dialog = null;
	
	private int chooserType;

	private String filePath;
	
	private String video_filePath;
	
	private String thumb_filePath;
	
	int serverResponseCode = 0;
	
	private int selectedCategoryId = 1;
	
	private static String fileKey;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_video_story);
		
//		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		videoView = (ADCustomVideoView) findViewById(R.id.videoView);
		videoView.setDimensions((int)getResources().getDimension(R.dimen.media_obj_wh), 
				(int)getResources().getDimension(R.dimen.media_obj_wh));
		
		playBtn = (Button) findViewById(R.id.playButton);
		playBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playVideoClip();
			}
		});
		
		titleTextView = (TypefaceEditText) findViewById(R.id.story_title_txt_field);
		descTextView = (TypefaceEditText) findViewById(R.id.story_desc_txt_field);

		button_share = (Button) findViewById(R.id.shareButton);
		button_share.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {

				if (isValidate()) {
					
					// close the keyboard.
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(titleTextView.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(descTextView.getWindowToken(), 0);
					
					getMediaKeyFromRemoteServer();
					
					/*// start upload process.
					new UploadMedia(UploadVideoStoryActivity.this, 
							titleTextView.getText().toString(), 
							descTextView.getText().toString(),
							selectedCategoryId, 
							new File(video_filePath)).execute();*/
				}
			}
		});
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
			showVideoDialog(UploadVideoStoryActivity.this, getString(R.string.video_text));
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK
				&& (requestCode == ChooserType.REQUEST_CAPTURE_VIDEO || requestCode == ChooserType.REQUEST_PICK_VIDEO)) {
			if (videoChooserManager == null) {
				reinitializeVideoChooser();
			}
			videoChooserManager.submit(requestCode, data);
		}
		else if (resultCode == RESULT_OK && requestCode == ADVideoRecordActivity.RESULT_VIDEO_CAPTURE_PATH_CODE) {
			if (data != null) {
				ADProcessedVideoMedia media = (ADProcessedVideoMedia)data.getSerializableExtra(ADVideoRecordActivity.RESULT_VIDEO_CAPTURE);
				video_filePath = media.getOutputFile().getPath();
				playVideoClip();
			}
		}
	}

	@Override
	public void onVideoChosen(final ChosenVideo video) {
		
		/*MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(video_filePath);
		String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		long timeInmillisec = Long.parseLong( time );
		if(timeInmillisec > 10000){
			Toast.makeText(UploadVideoStoryActivity.this, "Video length must be less then 10Sec", Toast.LENGTH_LONG).show();
			Log.d(TAG, "Video file Durration ::  " + timeInmillisec);
		}*/
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (video != null) {
					video_filePath = new File(video.getVideoFilePath()).toString();
					thumb_filePath = new File(video.getThumbnailPath()).toString();
					playVideoClip();
				}
				
				if (video_filePath != null) {
					Log.d(TAG, "Video file path " + video_filePath.toString());
					Log.d(TAG, "Thumb file path " + thumb_filePath.toString());
				}
			}
		});
	}

	@Override
	public void onError(final String reason) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				Toast.makeText(UploadVideoStoryActivity.this, reason,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		if (videoView != null)
			videoView = null;

		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		
		stopVideoClip();
		
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("chooser_type", chooserType);
		outState.putString("media_path", filePath);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("chooser_type")) {
				chooserType = savedInstanceState.getInt("chooser_type");
			}

			if (savedInstanceState.containsKey("media_path")) {
				filePath = savedInstanceState.getString("media_path");
			}
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	// Should be called if for some reason the VideoChooserManager is null (Due
	// to destroying of activity for low memory situations)
	private void reinitializeVideoChooser() {
		videoChooserManager = new VideoChooserManager(this, chooserType, LOCAL_UPLOAD_MEDIA_DIR, true);
		videoChooserManager.setVideoChooserListener(this);
		videoChooserManager.reinitialize(filePath);
	}
	
	/**
	 * Public API
	 */
	public void showVideoOptionsDialog(View view) {
		showVideoDialog(UploadVideoStoryActivity.this, getString(R.string.video_text));
	}

	/**
	 * Private API
	 */
	public void pickVideo() {
		chooserType = ChooserType.REQUEST_PICK_VIDEO;
		videoChooserManager = new VideoChooserManager(this, ChooserType.REQUEST_PICK_VIDEO, LOCAL_UPLOAD_MEDIA_DIR, true);
		videoChooserManager.setVideoChooserListener(this);
		try {
			videoChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void captureVideo() {
		
		Intent intent = ADVideoRecordActivity.newInstance(this);
		startActivityForResult(intent, ADVideoRecordActivity.RESULT_VIDEO_CAPTURE_PATH_CODE);
		UploadVideoStoryActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
/*		chooserType = ChooserType.REQUEST_CAPTURE_VIDEO;
		videoChooserManager = new VideoChooserManager(this, ChooserType.REQUEST_CAPTURE_VIDEO, LOCAL_UPLOAD_MEDIA_DIR, true);
		videoChooserManager.setVideoChooserListener(this);
		try {
			filePath = videoChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

    public void showVideoDialog(final Activity activity, String title) {
    	
		final String options[] = { "Choose Video", "Record Video" };

		AlertDialog.Builder ab = new AlertDialog.Builder(activity);
		ab.setTitle(title);
		ab.setIcon(getResources().getDrawable(R.drawable.ic_video));
		ab.setItems(options, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface d, int choice) {

				if (videoView != null) {
					videoView.stopPlayback();
				}
				
				if (choice == 0) {
					// Pick video
					pickVideo();
				}
				else {
					// Record video
					captureVideo();
				}
			}
		});
		ab.show();
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
    	else if (descTextView == null || StringUtil.isEmptyOrNull(descTextView.getText().toString())) {
    		validateFlag = false;
    		errorMsg = getString(R.string.validation_desc_error);
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
    	
    	
    	Media media = new Media();
    	media.setMediaShortURL(fileKey);
    	media.setMediaName(fileKey+"."+FileUtils.getFileExtension(video_filePath));
    	media.setMediaCaption(titleTextView.getText().toString());
    	media.setMediaDescription(descTextView.getText().toString());
    	media.setMediaType(MediaTypeEnum.IMAGE);
    	
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
				// TODO Auto-generated method stub
				if(dialog.isShowing()) dialog.dismiss();
				if(e == null && result.getHeaders().getResponseCode() == HttpStatus.SC_OK){
					Toast.makeText(UploadVideoStoryActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
					onBackPressed();
				}else{
					Toast.makeText(UploadVideoStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});	
    }
}
