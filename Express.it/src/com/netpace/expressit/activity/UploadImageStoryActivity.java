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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
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
import com.ad.videorecorderlib.logger.ADLogger;
import com.amazonaws.org.apache.http.HttpStatus;
import com.beanie.imagechooser.api.ChooserType;
import com.beanie.imagechooser.api.ChosenImage;
import com.beanie.imagechooser.api.FileUtils;
import com.beanie.imagechooser.api.ImageChooserListener;
import com.beanie.imagechooser.api.ImageChooserManager;
import com.beanie.imagechooser.api.config.Config;
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

public class UploadImageStoryActivity extends ActionBarActivity implements
		ImageChooserListener {

	protected static final String TAG = UploadImageStoryActivity.class.getSimpleName();
	
	private ImageView imageViewThumbnail;
	
	private TypefaceEditText titleTextView;
	
	private TypefaceEditText descTextView;
	
	private Button button_share;

	private String filePath;
	
	private String img_filePath;
	
	private String widthAfter;
	
	private String lengthAfter;

	private int chooserType;

	private ProgressDialog dialog = null;
	
	private int serverResponseCode = 0;
	
	private ImageChooserManager imageChooserManager;
	
	private int selectedCategoryId = 1;
	
	private static String fileKey;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_image_story);
		
//		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		imageViewThumbnail = (ImageView) findViewById(R.id.imageViewThumb);
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
					
					//TODO: Get file name from server (ExpressIt)
					/*AmazonS3Settings amazonS3Settings = Util.getAmazonS3Setting("xshanCheck.jpg");
					ADMediaSharingUtil.uploadMediaOnAmazonS3Cloud(UploadImageStoryActivity.this, amazonS3Settings, img_filePath, new ADRequestCallbackHandler<String>() {

						@Override
						public void onComplete(String result) {
							// TODO Auto-generated method stub
							Log.d(TAG, "Success : " + result);
						}

						@Override
						public void onFailed(String error) {
							// TODO Auto-generated method stub
							Log.d(TAG, "Fail : " + error);
						}
					});*/
					
					/*// start upload process.
					new UploadMedia(UploadImageStoryActivity.this, 
							titleTextView.getText().toString(), 
							descTextView.getText().toString(),
							selectedCategoryId,
							new File(img_filePath)).execute();*/
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
			this.onBackPressed();
			return true;

		case R.id.menu_item_img_gallery:
			showImageDialog(UploadImageStoryActivity.this, getString(R.string.image_text));
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
		super.onActivityResult(requestCode, resultCode, data);
		 
		if (resultCode == RESULT_OK
				&& (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
			
			if (imageChooserManager == null) {
				reinitializeImageChooser();
			}
			imageChooserManager.submit(requestCode, data);
		} 
	}

	String newImagePath;
	
	@Override
	public void onImageChosen(final ChosenImage image) {
	
		//Compress the image before according to given config.
		img_filePath = image.getFilePathOriginal();
		
		
		try {
			img_filePath = compressAndSaveImage(image.getFilePathOriginal(), AppConstants.STORY_IMG_BANNER_WIDTH, 1);
		} catch (Exception e) {
			ADLogger.debug(TAG, "Exception : " + e.toString());
		}

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				imageViewThumbnail.setImageURI(Uri.parse(new File(image
						.getFileThumbnail()).toString()));

				Log.d(TAG, "Image file path " + img_filePath.toString());
			}
		});
	}

	@Override
	public void onError(final String reason) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(UploadImageStoryActivity.this, reason,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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

	/**
	 * Public API
	 */
	public void showImageOptionsDialog(View view) {
		showImageDialog(UploadImageStoryActivity.this, getString(R.string.image_text));
	}
	
    public int postStory(String sourceFileUri) {

		final String upLoadServerUri = AppConstants.UPLOAD_URL;
		final String uploadFileName = sourceFileUri;

		final String lineEnd = "\r\n";
		final String twoHyphens = "--";
		final String boundary = "*****++++++************++++++++++++";

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile;

		if (isValidate()) {
			sourceFile = new File(uploadFileName); // file path on disk

			// POST Form
			try {
				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(upLoadServerUri);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(30000);
				conn.setReadTimeout(30000);
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Accept", "application/json");
				conn.setRequestProperty("userId", AppConstants.AUTHENTICATED_ENCRYPT_USER_ID);
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);

				// ********** Upload Custom Parameters *************//
				conn.setRequestProperty("title", titleTextView.getText().toString());
				conn.setRequestProperty("description", descTextView.getText().toString());
				conn.setRequestProperty("categoryId", "" + selectedCategoryId);
				conn.setRequestProperty("file", uploadFileName);

				// ********** Writing Form Data *************//*
				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"title\""
						+ lineEnd + lineEnd + "auto" + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"description\""
						+ lineEnd + lineEnd + "ja" + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
						+ img_filePath + "\"" + lineEnd);
				dos.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				Log.d(TAG, "File Data : " + dos.toString());

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.d(TAG, "HTTP Response is : " + serverResponseMessage + ": "
						+ serverResponseCode);

				if (serverResponseCode == 200) {

					runOnUiThread(new Runnable() {
						public void run() {

							String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
									+ " http://www.androidexample.com/media/uploads/"
									+ uploadFileName;

							Toast.makeText(UploadImageStoryActivity.this, msg,
									Toast.LENGTH_SHORT).show();
						}
					});
				}

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();
			} catch (FileNotFoundException ex) {
				
				dialog.dismiss();
				ex.printStackTrace();
				
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(UploadImageStoryActivity.this,
								"MalformedURLException", Toast.LENGTH_SHORT)
								.show();
					}
				});

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {

				dialog.dismiss();
				e.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(UploadImageStoryActivity.this,
								"Got Exception : see logcat ",
								Toast.LENGTH_SHORT).show();
					}
				});
				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
			}
		}
		
		// dismiss the progress dialog
		dialog.dismiss();
		return serverResponseCode; 
    }
	
	// Should be called if for some reason the ImageChooserManager is null (Due
	// to destroying of activity for low memory situations)
	private void reinitializeImageChooser() {
		imageChooserManager = new ImageChooserManager(this, chooserType, LOCAL_UPLOAD_MEDIA_DIR, true);
		imageChooserManager.setImageChooserListener(this);
		imageChooserManager.reinitialize(filePath);
	}
	
	/**
	 * Private API
	 */
	private void chooseImage() {
		chooserType = ChooserType.REQUEST_PICK_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_PICK_PICTURE, LOCAL_UPLOAD_MEDIA_DIR, true);
		imageChooserManager.setImageChooserListener(this);
		try {
			filePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void takePicture() {
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_CAPTURE_PICTURE, LOCAL_UPLOAD_MEDIA_DIR, true);
		imageChooserManager.setImageChooserListener(this);
		try {
			filePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public void showImageDialog(final Activity activity, String title) {
    	
		final String options[] = { "Choose Image", "Take Picture" };

		AlertDialog.Builder ab = new AlertDialog.Builder(activity);
		ab.setTitle(title);
		ab.setIcon(getResources().getDrawable(R.drawable.ic_gallery));
		ab.setItems(options, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface d, int choice) {
				if (choice == 0) {
					// Choose image from the library
					chooseImage();
				}
				else {
					// Take picture
					takePicture();
				}
			}
		});
		ab.show();
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
    
    private String compressAndSaveImage(String fileImage, int imageWidth, int scale) throws Exception {
        
    	try {
            ExifInterface exif = new ExifInterface(fileImage);
            String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            String length = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            if (Config.DEBUG) {
                Log.i(TAG, "Before: " + width + "x" + length);
            }

            int w = Integer.parseInt(width);
            int l = Integer.parseInt(length);

            int what = imageWidth > l ? imageWidth : l;

            Options options = new Options();
            if (what > 1500) {
                options.inSampleSize = scale * 4;
            } else if (what > 1000 && what <= 1500) {
                options.inSampleSize = scale * 3;
            } else if (what > 400 && what <= 1000) {
                options.inSampleSize = scale * 2;
            } else {
                options.inSampleSize = scale;
            }
            if (Config.DEBUG) {
                Log.i(TAG, "Scale: " + (what / options.inSampleSize));
            }
            Bitmap bitmap = BitmapFactory.decodeFile(fileImage, options);
            File original = new File(fileImage);
            File file = new File((original.getParent() + File.separator + original.getName()
                    .replace(".", "_fact_" + scale + ".")));
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            ExifInterface exifAfter = new ExifInterface(file.getAbsolutePath());
            widthAfter = exifAfter.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            lengthAfter = exifAfter.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            if (Config.DEBUG) {
                Log.i(TAG, "After: " + widthAfter + "x" + lengthAfter);
            }
            
            stream.flush();
            stream.close();
            return file.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Corrupt or deleted file???");
        }
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
    	
    	Media media = new Media();
    	media.setMediaShortURL(fileKey);
    	media.setMediaName(fileKey+"."+FileUtils.getFileExtension(img_filePath));
    	media.setMediaCaption(titleTextView.getText().toString());
    	media.setMediaDescription(descTextView.getText().toString());
    	media.setMediaType(MediaTypeEnum.IMAGE);
    	
    	Meta meta = new Meta();
    	meta.setWidth(Integer.valueOf(widthAfter));
    	meta.setHeight(Integer.valueOf(lengthAfter));
    	
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
					onBackPressed();
				}else{
					Toast.makeText(UploadImageStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});	
    }
    
}
