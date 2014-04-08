package com.netpace.expressit.utils;


import java.io.ByteArrayOutputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.widget.Toast;

import com.ad.mediasharing.AmazonS3Settings;
import com.ad.mediasharing.AmazonS3Settings.AmazonS3UploadType;
import com.netpace.expressit.android.ApplicationManager;
import com.netpace.expressit.constants.AppConstants;

public class Util {

	public static AmazonS3Settings getAmazonS3Setting(String remoteMediaFileName){
		
		AmazonS3Settings settings = new AmazonS3Settings(AmazonS3UploadType.TVM_ANONYMOUS_TYPE, 
				AppConstants.AMAZON_TVM_URL, AppConstants.AMAZON_S3_BUCKET_NAME, remoteMediaFileName, false);
		
		return settings;
	}
	
	public static boolean isNetworkAvailable() {

		Context ctx = ApplicationManager.getAppContext();

		ConnectivityManager connectivityManager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public static Uri getThumbnailPathFromVideo(String videoFilePath,String key, Context context){
		Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoFilePath,  MediaStore.Images.Thumbnails.MINI_KIND);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		thumb.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = Images.Media.insertImage(context.getContentResolver(), thumb, key, null);
		return Uri.parse(path);
	}
	
	public static void showToast(int message){
		Context ctx = ApplicationManager.getAppContext();
		Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
	}
	
	public static boolean isImage(String ext){
		return AppConstants.IMAGE_EXT_ARRAY.contains(ext);
	}
	
}
