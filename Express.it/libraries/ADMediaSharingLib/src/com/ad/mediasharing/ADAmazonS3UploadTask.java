package com.ad.mediasharing;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.ad.mediasharing.awsclientmanager.AmazonClientManager;
import com.amazonaws.org.apache.http.HttpStatus;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class ADAmazonS3UploadTask {

	private Activity activity;
	private AmazonS3Settings amazonS3Setting;
	private ADRequestCallbackHandler<String, Exception, Integer> callbackHandler;
	private String localMediaFilePath;
	
	private AmazonClientManager clientManager;
	
	public ADAmazonS3UploadTask(Activity activity, AmazonS3Settings amazonS3Setting,
			String localMediaFilePath, ADRequestCallbackHandler<String, Exception, Integer> callbackHandler) {
		super();
		
		this.activity = activity;
		this.amazonS3Setting = amazonS3Setting;
		this.localMediaFilePath = localMediaFilePath;
		this.callbackHandler = callbackHandler;
		
		clientManager = new AmazonClientManager(activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE),
					amazonS3Setting.getTokenVendingMachineURL(), amazonS3Setting.isUseSSL());
	}

	protected void upload(){
		
		new S3UploadTask().execute(localMediaFilePath);
	}
	
	private class S3UploadTask extends AsyncTask<String, Void, S3TaskResult> {

//		ProgressDialog dialog;
		
		protected void onPreExecute() {
//			dialog = new ProgressDialog(activity);
//			dialog.setMessage("uploading...");
//			dialog.setCancelable(false);
//			dialog.show();
		}

		protected S3TaskResult doInBackground(String... uris) {

			if (uris == null || uris.length != 1) {
				return null;
			}

			String selectedImage = uris[0];

			S3TaskResult result = new S3TaskResult();
			// Put the image data into S3.
			try {
				
				File remoteMediaFile = new File(selectedImage);
				if(remoteMediaFile.exists()){
					PutObjectRequest poRequest = new PutObjectRequest(amazonS3Setting.getAmazonS3BucketName(),
							amazonS3Setting.getRemoteMediaFileName(), remoteMediaFile);
					clientManager.s3().putObject(poRequest);
				}
			} catch (Exception exception) {

				result.setErrorMessage(exception.getMessage());
			}

			return result;
		}

		protected void onPostExecute(S3TaskResult result) {

//			if(dialog.isShowing()){
//				dialog.dismiss();
//			}
			if (result.getErrorMessage() != null) {
				callbackHandler.onFailed(new Exception(result.getErrorMessage()));
			}else{
				callbackHandler.onComplete(result.getErrorMessage(), HttpStatus.SC_OK);
			}
		}

	}

	private class S3TaskResult {
		String errorMessage = null;
		Uri uri = null;

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public Uri getUri() {
			return uri;
		}

		public void setUri(Uri uri) {
			this.uri = uri;
		}
	}
}
