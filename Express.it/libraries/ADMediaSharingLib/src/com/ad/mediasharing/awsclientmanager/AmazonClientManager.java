/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ad.mediasharing.awsclientmanager;

import android.content.SharedPreferences;
import android.util.Log;

import com.ad.mediasharing.tvmclient.AmazonSharedPreferencesWrapper;
import com.ad.mediasharing.tvmclient.AmazonTVMClient;
import com.ad.mediasharing.tvmclient.Response;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * This class is used to get clients to the various AWS services. Before
 * accessing a client the credentials should be checked to ensure validity.
 */
public class AmazonClientManager {
	private static final String LOG_TAG = "AmazonClientManager";

	private String tokenVendingMachineURL;
	private boolean useSSL;
	
	private AmazonS3Client s3Client = null;
	private SharedPreferences sharedPreferences = null;

	public AmazonClientManager(SharedPreferences settings, String tokenVendingMachineURL, boolean useSSL) {
		this.sharedPreferences = settings;
		this.tokenVendingMachineURL = tokenVendingMachineURL;
		this.useSSL = useSSL;
	}

	public AmazonS3Client s3() {
		validateCredentials();
		return s3Client;
	}

	public boolean hasCredentials() {
		return (this.tokenVendingMachineURL != null);
	}

	public Response validateCredentials() {

		Response ableToGetToken = Response.SUCCESSFUL;

		if (AmazonSharedPreferencesWrapper
				.areCredentialsExpired(this.sharedPreferences)) {

			synchronized (this) {

				if (AmazonSharedPreferencesWrapper
						.areCredentialsExpired(this.sharedPreferences)) {

					Log.i(LOG_TAG, "Credentials were expired.");

					AmazonTVMClient tvm = new AmazonTVMClient(this.sharedPreferences,
							this.tokenVendingMachineURL,
							this.useSSL);

					ableToGetToken = tvm.anonymousRegister();

					if (ableToGetToken.requestWasSuccessful()) {

						ableToGetToken = tvm.getToken();

						if (ableToGetToken.requestWasSuccessful()) {
							Log.i(LOG_TAG, "Creating New Credentials.");
							initClients();
						}
					}
				}
			}

		} else if (s3Client == null ) {

			synchronized (this) {

				if (s3Client == null) {

					Log.i(LOG_TAG, "Creating New Credentials.");
					initClients();
				}
			}
		}

		return ableToGetToken;
	}

	private void initClients() {
		AWSCredentials credentials = AmazonSharedPreferencesWrapper
				.getCredentialsFromSharedPreferences(this.sharedPreferences);

		Region region = Region.getRegion(Regions.US_WEST_2); 
        
        s3Client = new AmazonS3Client( credentials );
	    s3Client.setRegion(region);		
	}

	public void clearCredentials() {

		synchronized (this) {
			
			AmazonSharedPreferencesWrapper.wipe(this.sharedPreferences);

			s3Client = null;
		}
	}

	public boolean wipeCredentialsOnAuthError(AmazonServiceException ex) {
		if (
				// For S3
				// http://docs.amazonwebservices.com/AmazonS3/latest/API/ErrorResponses.html#ErrorCodeList
				ex.getErrorCode().equals("AccessDenied")
				|| ex.getErrorCode().equals("BadDigest")
				|| ex.getErrorCode().equals("CredentialsNotSupported")
				|| ex.getErrorCode().equals("ExpiredToken")
				|| ex.getErrorCode().equals("InternalError")
				|| ex.getErrorCode().equals("InvalidAccessKeyId")
				|| ex.getErrorCode().equals("InvalidPolicyDocument")
				|| ex.getErrorCode().equals("InvalidToken")
				|| ex.getErrorCode().equals("NotSignedUp")
				|| ex.getErrorCode().equals("RequestTimeTooSkewed")
				|| ex.getErrorCode().equals("SignatureDoesNotMatch")
				|| ex.getErrorCode().equals("TokenRefreshRequired")) {

			clearCredentials();

			return true;
		}

		return false;
	}
}
