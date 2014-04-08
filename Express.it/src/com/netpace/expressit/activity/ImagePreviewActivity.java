package com.netpace.expressit.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ad.videorecorderlib.logger.ADLogger;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.config.Config;
import com.netpace.expressit.R;
import com.netpace.expressit.constants.AppConstants;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ImagePreviewActivity extends ActionBarActivity implements ImageChooserListener{

	private final static String TAG = ImagePreviewActivity.class.getName();
	
	public final static String IMAGE_PATH = TAG+"path";
	public final static String IMAGE_WIDTH = TAG+"width";
	public final static String IMAGE_HEIGHT = TAG+"height";
	
	private ImageView previwImageView;
	private Button button_continue;
	
	private String imgFilePath;
	
	private int finalWidth;
	private int finalHeight;
	
	private int chooserType;
	private ImageChooserManager imageChooserManager;
	
	private boolean isPhotoCapture;
	private boolean isGalleryPhoto = false;
	
	public static Intent newInstance(Context context, boolean isPhotoCapture){
		
		Intent intent = new Intent(context, ImagePreviewActivity.class);
		intent.putExtra("isPhotoCapture", isPhotoCapture);
		return intent;
	}
	
	
	public void openCamera(){
		
		isPhotoCapture = !isPhotoCapture;
		
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_CAPTURE_PICTURE, AppConstants.LOCAL_UPLOAD_MEDIA_DIR, true);
		imageChooserManager.setImageChooserListener(this);
		try {
			imgFilePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);
		
		if(getIntent() != null){
			isPhotoCapture = getIntent().getBooleanExtra("isPhotoCapture", false);
		}
		
		getSupportActionBar().setTitle("retake");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		previwImageView = (ImageView) findViewById(R.id.preview_imageView);
		
		if(!isPhotoCapture){
			imgFilePath = getIntent().getStringExtra(IMAGE_PATH);
			finalWidth = getIntent().getIntExtra(IMAGE_WIDTH, 0);
			finalHeight = getIntent().getIntExtra(IMAGE_HEIGHT, 0);
			previwImageView.setImageURI(Uri.parse(imgFilePath));
			isGalleryPhoto = true;
		}
		
		button_continue = (Button) findViewById(R.id.flow_continue);
		
		button_continue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onContinueClicked(v);
			}
		});
	}

	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if(isPhotoCapture){
			openCamera();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if(requestCode == ChooserType.REQUEST_CAPTURE_PICTURE){
				if (imageChooserManager == null) {
					reinitializeImageChooser();
				}
				imageChooserManager.submit(requestCode, data);
			}
		}else{
			finish();
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.image_preview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		switch (item.getItemId()) {
		case android.R.id.home:
			if(!isGalleryPhoto){
				isPhotoCapture = !isPhotoCapture;
				openCamera();
			}else{
				finish();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
		
		
	}

	
	private void onContinueClicked(View view){
		
		isPhotoCapture = !isPhotoCapture;
		
		Intent intent = new Intent(ImagePreviewActivity.this, UploadImageStoryActivity.class);
		intent.putExtra(UploadImageStoryActivity.IMAGE_PATH, imgFilePath);
		intent.putExtra(UploadImageStoryActivity.IMAGE_WIDTH, finalWidth);
		intent.putExtra(UploadImageStoryActivity.IMAGE_HEIGHT, finalHeight);
		intent.putExtra("isGalleryPhoto", isGalleryPhoto);
		startActivity(intent);
	}


	@Override
	public void onError(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onImageChosen(final ChosenImage image) {
		// TODO Auto-generated method stub
		imgFilePath = image.getFilePathOriginal();
		try {
			imgFilePath = compressAndSaveImage(image.getFilePathOriginal(),
					AppConstants.STORY_IMG_BANNER_WIDTH, 1);
		} catch (Exception e) {
			ADLogger.debug(TAG, "Exception : " + e.toString());
		}
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				previwImageView.setImageURI(Uri.parse(new File(image
						.getFileThumbnail()).toString()));

				Log.d(TAG, "Image file path " + imgFilePath.toString());
			}
		});
	}
	
	// Should be called if for some reason the ImageChooserManager is null (Due
			// to destroying of activity for low memory situations)
			private void reinitializeImageChooser() {
				imageChooserManager = new ImageChooserManager(this, chooserType,
						AppConstants.LOCAL_UPLOAD_MEDIA_DIR, true);
				imageChooserManager.setImageChooserListener(this);
				imageChooserManager.reinitialize(imgFilePath);
			}

			@Override
			protected void onSaveInstanceState(Bundle outState) {
				outState.putInt("chooser_type", chooserType);
				outState.putString("media_path", imgFilePath);
				super.onSaveInstanceState(outState);
			}

			@Override
			protected void onRestoreInstanceState(Bundle savedInstanceState) {
				if (savedInstanceState != null) {
					if (savedInstanceState.containsKey("chooser_type")) {
						chooserType = savedInstanceState.getInt("chooser_type");
					}

					if (savedInstanceState.containsKey("media_path")) {
						imgFilePath = savedInstanceState.getString("media_path");
					}
				}
				super.onRestoreInstanceState(savedInstanceState);
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
