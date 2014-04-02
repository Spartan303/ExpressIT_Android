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
import java.util.HashMap;

public class ADMediaShareRequest {
	
	private String mRemoteRequestUri;
	private RequestMethod requestMethod;
	private File mediaFile;
	private HashMap<String, String> mRequestHeaders;
	private HashMap<String, String> postBodyParams;

	public ADMediaShareRequest(String remoteRequestUri,
			RequestMethod requestMethod,
			File mediaFile,
			HashMap<String, String> requestHeaders,
			HashMap<String, String> postBodyParams) {
		
		super();
		
		this.mRequestHeaders = new HashMap<String, String>();
		addRequestHeaders(requestHeaders);
		
		this.mRemoteRequestUri = remoteRequestUri;
		this.requestMethod = requestMethod;
		this.mediaFile = mediaFile;
		this.postBodyParams = postBodyParams;
	}
	
	public ADMediaShareRequest(String remoteRequestUri,
			RequestMethod requestMethod,
			HashMap<String, String> requestHeaders,
			HashMap<String, String> postBodyParams) {
		
		super();
		
		this.mRequestHeaders = new HashMap<String, String>();
		addRequestHeaders(requestHeaders);
		
		this.mRemoteRequestUri = remoteRequestUri;
		this.requestMethod = requestMethod;
		this.postBodyParams = postBodyParams;
	}
	
	public String getRemoteRequestUri() {
		return mRemoteRequestUri;
	}

	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	public File getMediaFile() {
		return mediaFile;
	}

	public HashMap<String, String> getRequestHeaders() {
		return mRequestHeaders;
	}
	
	public HashMap<String, String> getPostBodyParams() {
		return postBodyParams;
	}
	
	private void addRequestHeaders(HashMap<String, String> requestHeaders) {
		
		// Custom predefined headers
		mRequestHeaders.put("Accept", "application/json");
		
		if (requestHeaders != null) {
			for (String sKey : requestHeaders.keySet()) {
				String sVal = requestHeaders.get(sKey);
				
				mRequestHeaders.put(sKey, sVal);
			}
		}
	}
	
	@Override
	public String toString() {
		return "ADMediaShareRequest [remoteRequestUri=" + mRemoteRequestUri
				+ ", requestMethod=" + requestMethod + ", mediaFile="
				+ mediaFile + ", requestHeaders=" + mRequestHeaders
				+ ", postBodyParams=" + postBodyParams + "]";
	}
}

