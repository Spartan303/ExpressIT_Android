/*******************************************************************************
 * Copyright 2014 Adnan Urooj (Deminem)
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

package com.ad.mediasharing;

import java.io.File;
import java.net.SocketTimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.ad.mediasharing.constants.ADMediaSharingConstants;
import com.example.admediasharinglib.R;

public class ADUploadMediaTask extends AsyncTask<Void, Integer, String> {

	protected static final String TAG = ADUploadMediaTask.class.getSimpleName();
	
	private int NOTIFICATION_ID = 1;
	private CharSequence contentTitle;
	
	private NotificationManager notificationManager;
	Notification notification;
	PendingIntent pendingIntent;

	ProgressDialog pd;
	
	private long totalSize;
	private int uploadProgress = 0;
	private InputStreamBody isb;

	private Activity mActivity;
	private ADMediaShareRequest mRequest;
	private ADRequestCallbackHandler<String, Exception, Integer> mCallbackHandler;
	private boolean isPreferenceProgressEnabled;
	private boolean isProgressBarEnabled;
	
	private Handler mProgressHandler = new Handler();
	
	public ADUploadMediaTask(Activity activity, ADMediaShareRequest request,
			ADRequestCallbackHandler<String, Exception, Integer> callbackHandler) {

		this (activity, request, callbackHandler, false, false);
	}

	public ADUploadMediaTask(Activity activity, ADMediaShareRequest request,
			ADRequestCallbackHandler<String, Exception, Integer> callbackHandler, boolean isPreferenceProgressEnabled,
			boolean isProgressBarEnabled) {
		
		super();
		
		this.mActivity = activity;
		this.mRequest = request;
		this.mCallbackHandler = callbackHandler;
		this.isPreferenceProgressEnabled = isPreferenceProgressEnabled;
		this.isProgressBarEnabled = isProgressBarEnabled;
		
		if (activity != null) {
			notificationManager = (NotificationManager) mActivity
					.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}

    @SuppressWarnings("deprecation")
	protected void onPreExecute() {

        Intent intent = new Intent();
        pendingIntent = PendingIntent.getActivity(mActivity, 0, intent, 0);

        contentTitle = "Uploading Story...";
        CharSequence contentText = uploadProgress + "% complete";

        // Show the notification progress 
        if (isPreferenceProgressEnabled) {
            notification = new Notification(R.drawable.ic_launcher_ctv, contentTitle,
                    System.currentTimeMillis());
            notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
            notification.contentIntent = pendingIntent;
            notification.setLatestEventInfo(mActivity, contentTitle,
                    contentText, pendingIntent);

            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        // Show the alert progress bar 
        if (isProgressBarEnabled) {
    		pd = new ProgressDialog( mActivity );
    		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		pd.setMessage(contentTitle);
    		pd.setCancelable(false);
    		pd.show();
        }
    }
    
    @Override
    protected String doInBackground(Void... params) {
        
    	String serverResponse = "";

        try {
        	
            // Set timeout parameters
            int timeout = ADMediaSharingConstants.UPLOAD_REQUEST_TIME_OUT;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
            HttpConnectionParams.setSoTimeout(httpParameters, timeout);
            
            // Initiate connection parts
            HttpClient client = new DefaultHttpClient( httpParameters );
            HttpPost postRequest = new HttpPost( mRequest.getRemoteRequestUri() );
            
            // Add headers
            if (!mRequest.getRequestHeaders().isEmpty()) {
                for (String sKey : mRequest.getRequestHeaders().keySet()) {
                	
                	String sValue = mRequest.getRequestHeaders().get(sKey);
                	postRequest.addHeader(sKey, sValue);
                }
            }

            FileBody media = getMediaType( mRequest.getMediaFile() );
            postRequest.addHeader("Media-Type", media.getMimeType());
            
           ADCustomMultiPartEntity multipartE = new ADCustomMultiPartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE,
                    new ADCustomMultiPartEntity.ProgressListener() {

                    	int lastPourcent = 0;
                    	
                    	@TargetApi(Build.VERSION_CODES.CUPCAKE)
                        @Override
                        public void transferred(long num) {
                    		int currentPourcent = (int) ((num / (float) totalSize) * 100);
                    		if (currentPourcent > lastPourcent && currentPourcent < 90) {
                    		    publishProgress(currentPourcent);
                    		    lastPourcent = currentPourcent;
                    		}
                        }
                    });

           // Add the post elements
           multipartE.addPart("file", media);
           if (mRequest.getPostBodyParams() != null) {
        	   for (String sKey : mRequest.getPostBodyParams().keySet()) {
        		   String sVal = mRequest.getPostBodyParams().get(sKey);
        		   
        		   multipartE.addPart(sKey, new StringBody( sVal ));
        	   }
           }
           
           totalSize = multipartE.getContentLength();
           postRequest.setEntity(multipartE);
           
           HttpResponse response = client.execute(postRequest);
           
           // Get the response from server
           HttpEntity theEnt = response.getEntity();
           serverResponse = EntityUtils.toString(theEnt);
           
           if (ADMediaSharingConstants.DEBUG) {
        	   Log.d(TAG, "Result: " + serverResponse);
           }

           //Show the remaining progress.
           mProgressHandler.post(new Runnable() {
				public void run() {
					int i = uploadProgress;
					while (i <= 100) {
						try {
							publishProgress(i);
							i++;
						} catch (Exception e) {
							if (ADMediaSharingConstants.DEBUG)
								Log.d(TAG, e.getMessage());
						}
					}
				}
			});
        } 
        catch (SocketTimeoutException e) {
            e.printStackTrace();
            serverResponse = "unreachable";
        }
        catch (ConnectTimeoutException e) {
            e.printStackTrace();
            serverResponse = "unreachable";
        }
        catch (Exception e) {
            e.printStackTrace();
            serverResponse = "unreachable";
        }
        
        return serverResponse;
    }
    
    @SuppressWarnings("deprecation")
	@Override
    protected void onProgressUpdate(Integer... progress) {
    	uploadProgress = progress[0];
    	
    	// show upload proress in the alert box
    	if (isProgressBarEnabled)
    		pd.setProgress((int) (progress[0]));
    	
    	// show notification manager
    	if (isPreferenceProgressEnabled) {
    		contentTitle = "Uploaded Successfully";
    		CharSequence contentText = uploadProgress + "% complete";
    		notification.setLatestEventInfo(mActivity, contentTitle,
    				contentText, pendingIntent);
    		notificationManager.notify(NOTIFICATION_ID, notification);
    	}
    }

    @Override
    protected void onPostExecute(String result) {
        
    	if (isPreferenceProgressEnabled)
    		notificationManager.cancel(NOTIFICATION_ID);
        
        if (isProgressBarEnabled && pd.isShowing())
            pd.dismiss();
 
		if (result != null) {
			if (result.equalsIgnoreCase("unreachable")) {
				Toast.makeText(
						mActivity,
						"Upload Failed - Sever Unreachable. Please try again later.",
						Toast.LENGTH_LONG).show();
				
				// notify failure
				mCallbackHandler.onFailed(new Exception(result));
			} 
			else {
				try {
					JSONObject json = new JSONObject(result);

					if (!json.isNull("httpStatus")) {
						Integer statusCode = (Integer) json.get("httpStatus");
						String storyWebUri = (String) json.get("message");

						if (statusCode == HttpStatus.SC_OK) {
							
							// notify success
							mCallbackHandler.onComplete(result,statusCode);
						}
					} else {
						// notify failed
						mCallbackHandler.onFailed(new Exception(result));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    private FileBody getMediaType(File f) {

    	FileBody fb = null;
    	
    	if (f != null) {
        	if (f.getName().endsWith(".gif")) {
        		fb = new FileBody(f, "image/gif");
        	} else if (f.getName().endsWith(".jpg")) {
        		fb = new FileBody(f, "image/jpeg");
        	} else if (f.getName().endsWith(".png")) {
        		fb = new FileBody(f, "image/png");
        	} else if (f.getName().endsWith(".3gpp")) {
        		fb = new FileBody(f, "audio/3gpp");
        	} else if (f.getName().endsWith(".3gp")) {
        		fb = new FileBody(f, "video/3gpp");
        	} else if (f.getName().endsWith(".mp4")) {
        		fb = new FileBody(f, "video/mp4");
        	} else {
        		if (ADMediaSharingConstants.DEBUG)
        			Log.d(TAG, "unsupported file type, not adding file: " + f.getName());
        	}
    	}

    	return fb;
    }
}
