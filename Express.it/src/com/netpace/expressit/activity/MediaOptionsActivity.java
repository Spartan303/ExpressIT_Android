package com.netpace.expressit.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ad.videorecorderlib.ADProcessedVideoMedia;
import com.ad.videorecorderlib.ADVideoRecordActivity;
import com.ad.videorecorderlib.logger.ADLogger;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenVideo;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.MediaChooserListener;
import com.kbeanie.imagechooser.api.MediaChooserManager;
import com.kbeanie.imagechooser.api.config.Config;
import com.netpace.expressit.R;
import com.netpace.expressit.constants.AppConstants;
import com.netpace.expressit.utils.Util;

public class MediaOptionsActivity extends ActionBarActivity implements MediaChooserListener{

	private static final String TAG = MediaOptionsActivity.class.getName();
	
	private Button button_photo;
	private Button button_video;
	private Button button_gallery;
	private Button button_web;
	
	private int chooserType;
	private int finalWidth;
	private int finalHeight;;
	
	private String mediaFilePath;
	private String thumbFilePath;
	
	private MediaChooserManager mediaChooserManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_options);
		
		getSupportActionBar().setTitle("Back");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		button_photo = (Button) findViewById(R.id.pick_Photo);
		button_video = (Button) findViewById(R.id.pick_video);
		button_gallery = (Button) findViewById(R.id.pick_gallery);
		button_web = (Button) findViewById(R.id.pick_url);
		
		button_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePhotoFromCamera(v);
			}
		});
		
		button_video.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takeVideoFromCamera(v);
			}
		});
		
		button_gallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takeMediaFromGellary(v);
			}
		});
		
		button_web.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePhotoFromWebURL(v);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.media_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * 
	 *  Select Media Options methods
	 * 
	 * */

	private void takePhotoFromCamera(View view){
		
		Intent intent = ImagePreviewActivity.newInstance(this,true);
		startActivity(intent);
	}
	
	private void takeVideoFromCamera(View view){
		Intent intent = ADVideoRecordActivity.newInstance(this);
		startActivityForResult(intent, ADVideoRecordActivity.RESULT_VIDEO_CAPTURE_PATH_CODE);
		MediaOptionsActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	private void takeMediaFromGellary(View view){
		chooserType = ChooserType.REQUEST_PICK_PICTURE_OR_VIDEO;
		mediaChooserManager = new MediaChooserManager(this,
				ChooserType.REQUEST_PICK_PICTURE_OR_VIDEO, AppConstants.LOCAL_UPLOAD_MEDIA_DIR, true);
		mediaChooserManager.setMediaChooserListener(this);
		try{
			mediaFilePath = mediaChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void takePhotoFromWebURL(View view){
	
		Util.showToast(R.string.in_progress);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK &&  requestCode == ChooserType.REQUEST_PICK_PICTURE_OR_VIDEO) {
			if(requestCode == ChooserType.REQUEST_PICK_PICTURE_OR_VIDEO ){
				mediaChooserManager.submit(requestCode, data);
			}
			
		}else if (resultCode == RESULT_OK && requestCode == ADVideoRecordActivity.RESULT_VIDEO_CAPTURE_PATH_CODE) {
			if (data != null) {
				ADProcessedVideoMedia media = (ADProcessedVideoMedia)data.getSerializableExtra(ADVideoRecordActivity.RESULT_VIDEO_CAPTURE);
				
				Intent intent = new Intent(MediaOptionsActivity.this, UploadVideoStoryActivity.class);
				intent.putExtra(UploadVideoStoryActivity.VIDEO_PATH, media);
				startActivity(intent);
			}
		}
	}
	
	
	@Override
	public void onError(final String reason) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MediaOptionsActivity.this, reason,
						Toast.LENGTH_SHORT).show();
			}
		});
		
	}

	@Override
	public void onImageChosen(final ChosenImage image) {
		// Compress the image before according to given config.
		mediaFilePath = image.getFilePathOriginal();
		try {
			mediaFilePath = compressAndSaveImage(image.getFilePathOriginal(),
					AppConstants.STORY_IMG_BANNER_WIDTH, 1);
		} catch (Exception e) {
			ADLogger.debug(TAG, "Exception : " + e.toString());
		}
		
		Intent intent = new Intent(MediaOptionsActivity.this, ImagePreviewActivity.class);
		intent.putExtra(ImagePreviewActivity.IMAGE_PATH, mediaFilePath);
		intent.putExtra(ImagePreviewActivity.IMAGE_WIDTH, finalWidth);
		intent.putExtra(ImagePreviewActivity.IMAGE_HEIGHT, finalHeight);
		startActivity(intent);
	}
	
	@Override
	public void onVideoChosen(final ChosenVideo video) {
		
		mediaFilePath = new File(video.getVideoFilePath()).toString();
		if(mediaFilePath != null){
			Intent intent = new Intent(MediaOptionsActivity.this, UploadVideoStoryActivity.class);
			intent.putExtra(UploadVideoStoryActivity.VIDEO_PATH,mediaFilePath);
			intent.putExtra("isGalleryVideo", true);
			startActivity(intent);	
		}
		/*runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (video != null) {
					mediaFilePath = new File(video.getVideoFilePath()).toString();
					Intent intent = new Intent(MediaOptionsActivity.this, UploadVideoStoryActivity.class);
					intent.putExtra(UploadVideoStoryActivity.VIDEO_PATH,mediaFilePath);
					intent.putExtra("isGalleryVideo", true);
					startActivity(intent);	
				}
				if (mediaFilePath != null) {
					Log.d(TAG, "Video file path " + mediaFilePath.toString());
				}
			}
		});*/
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
	            finalWidth = Integer.valueOf(exifAfter.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
	            finalHeight = Integer.valueOf(exifAfter.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
	            
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
}
