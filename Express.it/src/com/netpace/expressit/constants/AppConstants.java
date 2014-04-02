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

package com.netpace.expressit.constants;

import com.ad.videorecorderlib.settings.Config;
import com.ad.videorecorderlib.settings.Config.ScreenResolution;

public class AppConstants {

	public final static boolean DEBUG = false;
    
	public final static int STORY_IMG_BANNER_WIDTH	= 639;
	
	public final static int UPLOAD_TIME_OUT	= 120 * 1000;
	
	public final static ScreenResolution screen_resolution = Config.ScreenResolution.SCREEN_RES_640_480;
	
	public final static int MEDIA_THRESHOLD = 100;
	public final static int MEDIA_CHUNK_SIZE = 20;
	
	public final static String DOMAIN_URL = "http://192.168.1.88:8080/xit";
	
	public final static String GET_KEY_URL = "/api/upload/getMediaKey";
	
	public final static String GET_MEDIA_URL = "/api/media";
	
	public final static String PUBLISH_MEDIA_URL = "/api/upload/publishMedia";
	
	public final static String MEDIA_TYPE_IMAGE ="IMAGE";
	
	public final static String MEDIA_TYPE_VIDEO ="VIDEO";
	
	public final static String UPLOAD_URL = "http://www.carbonated.tv/api/stories/v1/topHeadlines.json";
	
	public final static String LOCAL_UPLOAD_MEDIA_DIR = "uploadMedia";

	public final static String SELECTED_CATEGORY_STR = "selectedCategoryStr";
	
	public final static String SELECTED_CATEGORY_ID = "selectedCategoryId";
	
	public final static String AUTHENTICATED_ENCRYPT_USER_ID = "Mzg0Nw==";
	
	public final static String AUTHENTICATED_USER_NAME = "demo";
	
	public final static String CRITTERCISM_APP_KEY = "529f0cd38b2e332473000001";
	
	//ExpressIt
	public final static String AMAZON_TVM_URL = "http://mpanontvm-env.elasticbeanstalk.com/";
	public final static String AMAZON_S3_BUCKET_NAME = "expressit-development";
	public final static String AMAZON_AWS_ACCESS_KEY = "AKIAIYNADA4LZNEAVKBA";
	public final static String AMAZON_AWS_SECRATE_KEY = "F+wkdN5KC8wenk47+XAQJfVCEVv9XxN9EK3OUAZF";
	public final static String AMAZON_USER_NAME = "";
	
}
