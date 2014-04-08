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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ad.mediasharing.ADMediaSharingUtil;
import com.ad.mediasharing.ADRequestCallbackHandler;
import com.ad.mediasharing.AmazonS3Settings;
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

public class UploadImageStoryActivity extends ActionBarActivity {

	protected static final String TAG = UploadImageStoryActivity.class.getSimpleName();
	
	public final static String IMAGE_PATH = TAG+"path";
	public final static String IMAGE_WIDTH = TAG+"width";
	public final static String IMAGE_HEIGHT = TAG+"height";
	
	private ImageView imageViewThumbnail;
	
	private TypefaceEditText titleTextView;
	
	private Button button_saveLater;
	
	private String img_filePath;
	
	private int widthAfter;
	
	private int heightAfter;

	private ProgressDialog dialog = null;
	
	private static String fileKey;
	
	private boolean isGalleryPhoto = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_image_story);
		
		getSupportActionBar().setTitle("retake");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		Intent intent = getIntent();
		img_filePath = intent.getStringExtra(IMAGE_PATH);
		widthAfter = intent.getIntExtra(IMAGE_WIDTH, 0);
		heightAfter = intent.getIntExtra(IMAGE_HEIGHT, 0);
		isGalleryPhoto = intent.getBooleanExtra("isGalleryPhoto", false);
		
		imageViewThumbnail = (ImageView) findViewById(R.id.imageViewThumb);
		imageViewThumbnail.setImageURI(Uri.parse(img_filePath));
		titleTextView = (TypefaceEditText) findViewById(R.id.story_title_txt_field);

		button_saveLater = (Button) findViewById(R.id.shareButton);
		button_saveLater.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if (isValidate()) {
					// close the keyboard.
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(titleTextView.getWindowToken(), 0);

					getMediaKeyFromRemoteServer();
				}
			}
		});
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_img_story, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if(!isGalleryPhoto){
				this.onBackPressed();
			}else{
				Intent intent = new Intent(UploadImageStoryActivity.this,
		                MediaOptionsActivity.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        startActivity(intent);
			}
			return true;

		case R.id.menu_item_img_gallery:
			if (isValidate()) {
				// close the keyboard.
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
	public void onDestroy() {
		super.onDestroy();
	}

    
    private boolean isValidate() {
    	
    	boolean validateFlag = true;
    	String errorMsg = "";
    	
    	if (titleTextView == null || StringUtil.isEmptyOrNull(titleTextView.getText().toString())) {
    		validateFlag = false;
    		errorMsg = getString(R.string.validation_title_error);
    	}
    	else if (StringUtil.isEmptyOrNull(img_filePath)) {
    		validateFlag = false;
    		errorMsg = getString(R.string.validation_spotlight_error);
    	}
    	
    	if (!validateFlag) {
			Toast.makeText(UploadImageStoryActivity.this, errorMsg,
					Toast.LENGTH_SHORT).show();
    	}
    	
    	return validateFlag;
    }
    
    
    /*
     * Network Calls methods
     * 
     * */
    
    private void showProgressDialog(){
    	dialog = new ProgressDialog(this);
		dialog.setMessage("uploading...");
		dialog.setCancelable(false);
		dialog.show();
    }
    
    
    private void getMediaKeyFromRemoteServer(){
    	showProgressDialog();
    	dialog.setMessage("Geting key...");
    	ADMediaSharingUtil.getRestClient(UploadImageStoryActivity.this)
		.load(AppConstants.DOMAIN_URL+AppConstants.GET_KEY_URL)
		.asString()
		.withResponse()
		.setCallback(new FutureCallback<Response<String>>() {
			
			@Override
			public void onCompleted(Exception e, Response<String> result) {
				if(e == null && result.getHeaders().getResponseCode() == HttpStatus.SC_OK){
					fileKey = result.getResult().toString();
					Log.i(TAG, fileKey);
					uploadMediaToTVM();
				}else{
					if(dialog.isShowing()) dialog.dismiss();
					Toast.makeText(UploadImageStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
    }
    
    private void uploadMediaToTVM(){
    	
    	dialog.setMessage("Uploading on TVM");
    	
    	AmazonS3Settings amazonS3Settings = Util.getAmazonS3Setting(fileKey+"."+FileUtils.getFileExtension(img_filePath));
		ADMediaSharingUtil.uploadMediaOnAmazonS3Cloud(UploadImageStoryActivity.this, amazonS3Settings, img_filePath, 
				new ADRequestCallbackHandler<String, Exception, Integer>() {

			@Override
			public void onComplete(String result,Integer statusCode) {
				Log.d(TAG, "Success : " + result);
				if(statusCode == HttpStatus.SC_OK){
					publishMediaOnRemoteServer();
				}
			}

			@Override
			public void onFailed(Exception error) {
				Log.d(TAG, "Fail : " + error);
				if(dialog.isShowing()) dialog.dismiss();
				Toast.makeText(UploadImageStoryActivity.this, "Uploaded fail", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProgress(Integer progress) {
				Log.i(TAG, progress.toString());
				
			}
		});
    }
    
    private void publishMediaOnRemoteServer(){
    	
    	dialog.setMessage("Publish data on XIT");
    	
    	Media media = new Media();
    	media.setMediaShortURL(fileKey);
    	media.setMediaName(fileKey+"."+FileUtils.getFileExtension(img_filePath));
    	media.setMediaCaption(titleTextView.getText().toString());
    	media.setMediaType(MediaTypeEnum.IMAGE);
    	
    	Meta meta = new Meta();
    	meta.setWidth(widthAfter);
    	meta.setHeight(heightAfter);
    	
    	media.setMeta(meta);
    	
    	ADMediaSharingUtil.getRestClient(UploadImageStoryActivity.this)
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
					Toast.makeText(UploadImageStoryActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent( UploadImageStoryActivity.this, SlideMenuActivity.class );  
				    intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );  
				    startActivity( intent ); 
				}else{
					Toast.makeText(UploadImageStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});	
    }
    
}
