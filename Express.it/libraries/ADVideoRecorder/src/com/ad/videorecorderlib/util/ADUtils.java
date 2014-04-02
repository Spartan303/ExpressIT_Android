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

package com.ad.videorecorderlib.util;

import java.io.File;

import com.ad.videorecorderlib.logger.ADLogger;

public class ADUtils {
	
	protected static final String TAG = ADUtils.class.getSimpleName();
	
	public static int puEncodingFormat = 0;
	public static int puContainerFormat = 0;
	public static int puResolutionChoice = 0;
	
	public static void createDirIfNotExist(String _path) {
		File lf = new File(_path);
		try {
			if (lf.exists()) {
				//directory already exists
			} else {
				if (lf.mkdirs()) {
					ADLogger.debug(TAG, "createDirIfNotExist created " + _path);
				} else {
					ADLogger.debug(TAG, "createDirIfNotExist failed to create " + _path);
				}
			}
		} catch (Exception e) {
			//create directory failed
			ADLogger.debug(TAG, "createDirIfNotExist failed to create " + _path);
		}
	}
}