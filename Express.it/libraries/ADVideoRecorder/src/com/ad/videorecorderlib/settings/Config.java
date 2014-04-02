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

package com.ad.videorecorderlib.settings;

import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.VideoEncoder;

public class Config {

	public enum MediaSettings {
    	MEDIA_SETITNGS_DEFAULT (0),
    	MEDIA_SETITNGS_VINE (1),
    	MEDIA_SETITNGS_INSTAGRAM (2),
    	MEDIA_SETITNGS_CUSTOM (3);
    	
        private final int value;

        private MediaSettings(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
    
    public enum ScreenResolution {
    	SCREEN_RES_176_144 (0),
    	SCREEN_RES_320_240 (1),
    	SCREEN_RES_640_480 (2),
    	SCREEN_RES_720_480 (3);
    	
        private final int value;

        private ScreenResolution(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
    
    public static Config instance = new Config();
    
    private MediaSettings mMediaSettings;
    
	private int puEncodingFormat;
	private int puContainerFormat;
	private int puResolutionChoice;
	
	private int maxRecordDurationInMs;
	private long maxFileSizeInBytes;
	private int videoBitRate;
	private double videoFrameRate;
	
	private int paEncodingFormat;
	private int audioChannel;
	
	private String mVideoOutputFilePath;

	private boolean DEBUG;
	
	private final int minMediaProcessingThreshold = 1500;	//mseconds

	public static Config getInstance() {
		return instance;
	}

	private Config() {
		
		this.mVideoOutputFilePath = "/sdcard/MyApp/";
		this.DEBUG = false;
		
		// By default Settings
		setMediaSettings( MediaSettings.MEDIA_SETITNGS_VINE );
	}

	/**
	 * Default Settings
	 */
	public void defaultSettings() {
		
		this.puEncodingFormat = VideoEncoder.H263;
		this.puContainerFormat = MediaRecorder.OutputFormat.THREE_GPP;
		this.puResolutionChoice = ScreenResolution.SCREEN_RES_640_480.value();
		
		// Video Settings
		this.maxRecordDurationInMs = 6000;		// max 6 secs recording
		this.maxFileSizeInBytes = 5000000;		// max file size
		this.videoBitRate = 1098;				// fluctuate b/w 1.8 - 1.9 Mbit/s
		this.videoFrameRate = 30;				// frame rate
		
		// Audio Settings
		this.paEncodingFormat = AudioEncoder.AAC;
		this.audioChannel = 1;
	}

	/**
	 * Vine Video Recording Settings
	 */
	public void vineSettings() {
		
		// Encoding, Container, and Resolution
		this.puEncodingFormat = VideoEncoder.MPEG_4_SP;
		this.puContainerFormat = MediaRecorder.OutputFormat.MPEG_4;
		this.puResolutionChoice = ScreenResolution.SCREEN_RES_640_480.value();
		
		// Video Settings
		this.maxRecordDurationInMs = 6000;		// max 6 secs recording
		this.maxFileSizeInBytes = 5000000;		// max file size
		this.videoBitRate = 1098;				// fluctuate b/w 1.8 - 1.9 Mbit/s
		this.videoFrameRate = 30;				// frame rate
		
		// Audio Settings
		this.paEncodingFormat = AudioEncoder.AAC;
		this.audioChannel = 1;
	}
	
	/**
	 * Instagram Video Recording Settings
	 */
	public void instagramSettings() {
		
		// Encoding, Container, and Resolution
		this.puEncodingFormat = VideoEncoder.MPEG_4_SP;
		this.puContainerFormat = MediaRecorder.OutputFormat.MPEG_4;
		this.puResolutionChoice = ScreenResolution.SCREEN_RES_640_480.value();
		
		// Video Settings
		this.maxRecordDurationInMs = 15000;		// max 15 secs recording
		this.maxFileSizeInBytes = 5000000;		// max file size
		this.videoBitRate = 1649900;			// fluctuate b/w 1.8 - 1.9 Mbit/s
		this.videoFrameRate = 30;				// frame rate
		
		// Audio Settings
		this.paEncodingFormat = AudioEncoder.AAC;
		this.audioChannel = 1;
	}
	
	public MediaSettings getMediaSettings() {
		return mMediaSettings;
	}

	public void setMediaSettings(MediaSettings mediaSettings) {
		this.mMediaSettings = mediaSettings;
		
		if (mMediaSettings == MediaSettings.MEDIA_SETITNGS_VINE) {
			vineSettings();
		}
		else if (mMediaSettings == MediaSettings.MEDIA_SETITNGS_INSTAGRAM) {
			instagramSettings();
		}
		else if (mMediaSettings == MediaSettings.MEDIA_SETITNGS_CUSTOM) {
			/* Let the user customized the media recording settings */
		}
		else {
			defaultSettings();
		}
	}
	
	public String getVideoOutputFilePath() {
		return mVideoOutputFilePath;
	}

	public void setVideoOutputFilePath(String videoOutputFilePath) {
		this.mVideoOutputFilePath = videoOutputFilePath;
	}

	public int getMaxRecordDurationInMs() {
		return maxRecordDurationInMs;
	}

	public void setMaxRecordDurationInMs(int maxRecordDurationInMs) {
		this.maxRecordDurationInMs = maxRecordDurationInMs;
	}

	public long getMaxFileSizeInBytes() {
		return maxFileSizeInBytes;
	}

	public void setMaxFileSizeInBytes(long maxFileSizeInBytes) {
		this.maxFileSizeInBytes = maxFileSizeInBytes;
	}

	public int getVideoBitRate() {
		return videoBitRate;
	}

	public void setVideoBitRate(int videoBitRate) {
		this.videoBitRate = videoBitRate;
	}

	public double getVideoFrameRate() {
		return videoFrameRate;
	}

	public void setVideoFrameRate(double videoFrameRate) {
		this.videoFrameRate = videoFrameRate;
	}

	public int getPuEncodingFormat() {
		return puEncodingFormat;
	}

	public void setPuEncodingFormat(int puEncodingFormat) {
		this.puEncodingFormat = puEncodingFormat;
	}

	public int getPuContainerFormat() {
		return puContainerFormat;
	}

	public void setPuContainerFormat(int puContainerFormat) {
		this.puContainerFormat = puContainerFormat;
	}

	public int getPuResolutionChoice() {
		return puResolutionChoice;
	}

	public void setPuResolutionChoice(int puResolutionChoice) {
		
		this.puResolutionChoice = puResolutionChoice;
	}

	public int getPaEncodingFormat() {
		return paEncodingFormat;
	}

	public void setPaEncodingFormat(int paEncodingFormat) {
		this.paEncodingFormat = paEncodingFormat;
	}

	public int getAudioChannel() {
		return audioChannel;
	}

	public void setAudioChannel(int audioChannel) {
		this.audioChannel = audioChannel;
	}

	public int getMinMediaProcessingThreshold() {
		return minMediaProcessingThreshold;
	}

	public boolean isDebugMode() {
		return DEBUG;
	}

	public void setDebugMode(boolean debugMode) {
		DEBUG = debugMode;
	}
}
