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

import android.app.Activity;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders.Any.B;
import com.koushikdutta.ion.builder.LoadBuilder;

/**
 * ADMediaSharingUtil designed specifically for uploading media 
 * on either amazon cloud or remote server
 *  
 * @author Deminem
 *
 */

public class ADMediaSharingUtil {

	protected static final String TAG = ADMediaSharingUtil.class.getName();
	
	public static LoadBuilder<B> getRestClient(Activity activity) {
		return (LoadBuilder<B>) Ion.with(activity);
	}
	
	public static void uploadMediaOnRemote(Activity activity, 
			ADMediaShareRequest request, 
			ADRequestCallbackHandler<String, Exception, Integer> callbackHandler,
			boolean showNotificationProgress,
			boolean showProgressAlertBox ) {
		
		// Upload media through REST on remote server
		new ADUploadMediaTask(activity, request, callbackHandler, 
				showNotificationProgress, showProgressAlertBox).execute();
	}

	public static void uploadMediaOnAmazonS3Cloud(Activity activity,
			AmazonS3Settings amazonS3Settings,
			String localImageFilePath,
			ADRequestCallbackHandler<String, Exception, Integer> callbackHandler ) {
	
		new ADAmazonS3UploadTask(activity, amazonS3Settings,localImageFilePath, callbackHandler).upload();
	}
	
}
