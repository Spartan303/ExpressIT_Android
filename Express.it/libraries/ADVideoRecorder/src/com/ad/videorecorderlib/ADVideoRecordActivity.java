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

package com.ad.videorecorderlib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.VideoEncoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ad.videorecorderlib.logger.ADLogger;
import com.ad.videorecorderlib.settings.Config;
import com.ad.videorecorderlib.util.ADUtils;

/***
 * TODO: 1. sound on/off 2. resolution change
 * 
 * @author roman10
 * 
 */

public class ADVideoRecordActivity extends Activity implements SurfaceHolder.Callback {

	protected static final String TAG = ADVideoRecordActivity.class.getSimpleName();

	private final int PROGESS_TIMER_RUNTIME = Config.getInstance().getMaxRecordDurationInMs();
	
	public final static int RESULT_VIDEO_CAPTURE_PATH_CODE = 589; 
	
	public final static String RESULT_VIDEO_CAPTURE = "resultVideoOutputCapture"; 
	
	private ProgressBar mProgressBar;
	private TextView mProgressTextView;
	private TextView mTimeTextView;
	
	private Button prDiscardBtn;
	private Button prNextBtn;
	private Button prSettingsBtn;
	
	private Context prContext;
	
	private SurfaceView prSurfaceView;
	private SurfaceHolder prSurfaceHolder;
	private Camera prCamera;

	private MediaRecorder prMediaRecorder;
	private RecordProgressBar mRecordProgressBar;
	private boolean prRecordInProcess;

	private ADProcessedVideoMedia mProccessedMedia;
	private ADVideoClipsSession mClipSession;

	private boolean isCameraSurfaceTouched;
	
	public static Intent newInstance(Activity activity) {

		Intent intent = new Intent(activity, ADVideoRecordActivity.class);
		return intent;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		prContext = this.getApplicationContext();
		setContentView(R.layout.media_record);

		// Initialize Settings
		initializeSettings();
		
		Point screenDimenions = getDisplayDimensions();
		mProgressBar = (ProgressBar)findViewById(R.id.recording_progressBar);
		mProgressBar.setMax(PROGESS_TIMER_RUNTIME);
		
		mProgressTextView = (TextView) findViewById(R.id.textView);
		mTimeTextView = (TextView) findViewById(R.id.textView2);
		
		prDiscardBtn = (Button) findViewById(R.id.discardBtn);
		prNextBtn = (Button) findViewById(R.id.nextBtn);
		prSettingsBtn = (Button) findViewById(R.id.main_btn2);
		prSettingsBtn.setVisibility(View.GONE);

		prSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)prSurfaceView.getLayoutParams();
		
		params.width = screenDimenions.x;
		params.height = screenDimenions.y - screenDimenions.y/3;

		prRecordInProcess = false;
		prSurfaceView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				final int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					isCameraSurfaceTouched = true;
				}
				else if (action == MotionEvent.ACTION_UP) {
					isCameraSurfaceTouched = false;
					
		        	// Check if the video recording elapsed time is finished
		        	// then proceed for processing and finalizing.
		        	if (mRecordProgressBar.getProgressStatus() == PROGESS_TIMER_RUNTIME) {
		        		
		        		stopRecording();		//stop recording
		        		processRecording( Boolean.TRUE );		// final process recording 
		        	}
		        	else {
		        		
	        			// Stop Recording
		        		stopRecording();
		        	}
				}
				
				return false;
			}
		});
		
		prSurfaceView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (isCameraSurfaceTouched && prRecordInProcess == false) {
					// Start Recording
		        	startRecording();
				}

				return true;
			}
		});

		prDiscardBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				final int action = event.getAction();
				
				if (action == MotionEvent.ACTION_DOWN) {
					showCancelDialog();
				}

				return true;
			}
		});
		
		prNextBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				final int action = event.getAction();
				
				if (action == MotionEvent.ACTION_DOWN) {
					
					//Process the recorded video on next button.
					processRecording( Boolean.TRUE );
				}

				return true;
			}
		});
		
//		prSettingsBtn.setOnClickListener(new View.OnClickListener() {
//			
//			// @Override
//			public void onClick(View v) {
//				Intent lIntent = new Intent();
//				lIntent.setClass(prContext,
//						com.ad.settings.SettingsDialog.class);
//				startActivityForResult(lIntent, REQUEST_DECODING_OPTIONS);
//			}
//		});

		prSurfaceHolder = prSurfaceView.getHolder();
		prSurfaceHolder.addCallback(this);
		prSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		prMediaRecorder = new MediaRecorder();
	}
 
	/**
	 * Camera and Preview View Methods
	 */
	
	@Override
	public void surfaceChanged(SurfaceHolder _holder, int _format, int _width,
			int _height) {
		
		Camera.Parameters lParam = prCamera.getParameters();

		prCamera.setParameters(lParam);
		try {
			prCamera.setPreviewDisplay(_holder);
			prCamera.startPreview();
			setCameraDisplayOrientation(this, CameraInfo.CAMERA_FACING_BACK, prCamera);
			
			// prPreviewRunning = true;
		} catch (IOException _le) {
			_le.printStackTrace();
		}
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void surfaceCreated(SurfaceHolder arg0) {
		prCamera = Camera.open();
		
		/* Camera Settings */
		List<Size> psizes = prCamera.getParameters().getSupportedPreviewSizes();
		List<Size> sizes = prCamera.getParameters().getSupportedVideoSizes();

//		for (Size s : psizes) {
//			Logger.debug(TAG, "Preview Sizes [ w: " + s.width + ", h: " + s.height
//					+ " ]");
//		}
//
//		for (Size s : sizes) {
//			Logger.debug(TAG, "Video Sizes [ w: " + s.width + ", h: " + s.height
//					+ " ]");
//		}

		if (prCamera == null) {
			Toast.makeText(this.getApplicationContext(),
					"Camera is not available!", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		
		if (prRecordInProcess) {
			stopRecording();
		} else {
			prCamera.stopPreview();
		}

		if (prMediaRecorder != null) {
			prMediaRecorder.release();
			prMediaRecorder = null;
		}
			
		if (prCamera != null) {
			prCamera.release();
			prCamera = null;
		}
	}
	
	private static final int REQUEST_DECODING_OPTIONS = 0;

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		
		switch (requestCode) {
			case REQUEST_DECODING_OPTIONS:
				if (resultCode == RESULT_OK) {
					updateEncodingOptions();
				}
			break;
			case RESULT_VIDEO_CAPTURE_PATH_CODE:
				if (resultCode == RESULT_OK) {
					ADProcessedVideoMedia processedMedia = (ADProcessedVideoMedia)intent.getSerializableExtra(ADVideoRecordActivity.RESULT_VIDEO_CAPTURE);

					Intent result = new Intent();
					result.putExtra(ADVideoRecordActivity.RESULT_VIDEO_CAPTURE, processedMedia);
					setResult(ADVideoRecordActivity.RESULT_VIDEO_CAPTURE_PATH_CODE, result);
					setResult(RESULT_OK, result);
					
					finish();
					ADVideoRecordActivity.this.overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_out_right);
				}
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	@Override
	public void onBackPressed() {
		showCancelDialog();
	}

	/**
	 * Utility Methods
	 * 
	 */
	private void initializeSettings() {

		// Create the processed media id
		long processedMediaId = System.currentTimeMillis();
		String videoOutputPath = Config.getInstance().getVideoOutputFilePath();
		videoOutputPath = videoOutputPath + String.valueOf( processedMediaId );
			
		// Create the processed media directory
		ADUtils.createDirIfNotExist( videoOutputPath );
		
		// Create processed media object
		mProccessedMedia = new ADProcessedVideoMedia(String.valueOf(processedMediaId));
		mRecordProgressBar = new RecordProgressBar();
	}
	
	private boolean startRecording() {

		ADLogger.debug(TAG, "Start Recording !!!");
		if (!prRecordInProcess && mRecordProgressBar.getProgressStatus() < PROGESS_TIMER_RUNTIME) {
			
			prCamera.stopPreview();
			
			try {

				setMuteAll(Boolean.TRUE );	//disable all the system sounds

				prCamera.unlock();
				prMediaRecorder.setCamera(prCamera);

				// set audio source as Microphone, video source as camera
				// state: Initial=>Initialized

				int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
				prMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				prMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				prMediaRecorder.setOrientationHint( 90 );
				
				// set the file output format: 3gp or mp4
				// state: Initialized=>DataSourceConfigured
				String lDisplayMsg = "Current container format: ";
				String lVideoFileFullPath;

				if (Config.getInstance().getPuContainerFormat() == MediaRecorder.OutputFormat.MPEG_4) {
					lDisplayMsg += "MP4\n";
					lVideoFileFullPath = ".mp4";
					prMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				} else if (Config.getInstance().getPuContainerFormat() == MediaRecorder.OutputFormat.THREE_GPP) {
					lDisplayMsg += "3GP\n";
					lVideoFileFullPath = ".3gp";
					prMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				} else {
					lDisplayMsg += "3GP\n";
					lVideoFileFullPath = ".3gp";
					prMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				}
				
				// the encoders: audio and video
				prMediaRecorder.setAudioEncoder(Config.getInstance()
						.getPaEncodingFormat());
				prMediaRecorder.setAudioChannels(Config.getInstance()
						.getAudioChannel());

				lDisplayMsg += "Current encoding format: ";
				if (Config.getInstance().getPuEncodingFormat() == VideoEncoder.H263) {
					lDisplayMsg += "H263\n";
					prMediaRecorder.setVideoEncoder(VideoEncoder.H263);
				} else if (Config.getInstance().getPuEncodingFormat() == VideoEncoder.MPEG_4_SP) {
					lDisplayMsg += "MPEG4-SP\n";
					prMediaRecorder.setVideoEncoder(VideoEncoder.MPEG_4_SP);
				} else if (Config.getInstance().getPuEncodingFormat() == VideoEncoder.H264) {
					lDisplayMsg += "H264\n";
					prMediaRecorder.setVideoEncoder(VideoEncoder.H264);
				} else {
					lDisplayMsg += "H263\n";
					prMediaRecorder.setVideoEncoder(VideoEncoder.H263);
				}

				String clipId = String.valueOf(System.currentTimeMillis());
				lVideoFileFullPath = mProccessedMedia.getDirectoryPath() + "/" + clipId + lVideoFileFullPath;

				File prRecordedFile = new File(lVideoFileFullPath);
				prMediaRecorder.setOutputFile(prRecordedFile.getPath());
				mClipSession = new ADVideoClipsSession(clipId, prRecordedFile, mRecordProgressBar.getProgressStatus());
						
				lDisplayMsg += "Current resolution format: ";
				if (Config.getInstance().getPuResolutionChoice() == Config.ScreenResolution.SCREEN_RES_176_144.value()) {
					lDisplayMsg += "176 x 144 \n";
					prMediaRecorder.setVideoSize(176, 144);
				} else if (Config.getInstance().getPuResolutionChoice() == Config.ScreenResolution.SCREEN_RES_320_240.value()) {
					lDisplayMsg += "320 x 240 \n";
					prMediaRecorder.setVideoSize(320, 240);
				} else if (Config.getInstance().getPuResolutionChoice() == Config.ScreenResolution.SCREEN_RES_640_480.value()) {
					lDisplayMsg += "640 x 480 \n";
					prMediaRecorder.setVideoSize(640, 480);
				} else if (Config.getInstance().getPuResolutionChoice() == Config.ScreenResolution.SCREEN_RES_720_480.value()) {
					lDisplayMsg += "640 x 480 \n";
					prMediaRecorder.setVideoSize(720, 480);
				} else {
					
					// By default
					Camera.Size bestSize = getBestPreviewSize(640, 480, prCamera.getParameters());
					prMediaRecorder.setVideoSize(bestSize.width, bestSize.height);
				}

				Toast.makeText(prContext, lDisplayMsg, Toast.LENGTH_LONG).show();
				prMediaRecorder.setVideoEncodingBitRate(Config.getInstance()
						.getVideoBitRate());
				prMediaRecorder.setVideoFrameRate((int)Config.getInstance()
						.getVideoFrameRate());
				prMediaRecorder.setPreviewDisplay(prSurfaceHolder.getSurface());
				prMediaRecorder.setMaxDuration(Config.getInstance()
						.getMaxRecordDurationInMs());
				prMediaRecorder.setMaxFileSize(Config.getInstance()
						.getMaxFileSizeInBytes());

				// prepare for capturing
				// state: DataSourceConfigured => prepared
				prMediaRecorder.prepare();

				// start recording
				// state: prepared => recording
				prMediaRecorder.start();
				mRecordProgressBar.start();

				prRecordInProcess = true;
				return true;

			} catch (IOException _le) {
				_le.printStackTrace();
				return false;
			}
		}
		
		return false;
	}

	private void stopRecording() {
		
		ADLogger.debug(TAG, "Stop Recording !!!");
		
		try {
			if (prRecordInProcess) {
				// Stop recording progress first, coz media takes sometime to finish it's process
				mRecordProgressBar.stop();
				
				// Stop media recorder
				prMediaRecorder.stop();
				prMediaRecorder.reset();

				if (mProccessedMedia != null && mClipSession != null) {

					int progress = mRecordProgressBar.getProgressStatus() - mClipSession.getMediaLengthInMs();
					mClipSession.setMediaLengthInMs( progress );
					mClipSession.setSeqOrder( mRecordProgressBar.getRecordSequence() );
					
					ADLogger.debug(TAG, "stopRecording progress : " + mRecordProgressBar.getProgressStatus());
					ADLogger.debug(TAG, " clip progress : " + mClipSession);
					
					// add the recorded clip session into processed media session.
					mProccessedMedia.addSession(mClipSession);
					mClipSession = null;
				}	
				
				setMuteAll (Boolean.FALSE );	//enable all the system sounds
				
				prRecordInProcess = false;
				prCamera.startPreview();
				
				prCamera.reconnect();
			}
		} 
		catch (RuntimeException e) {
			prMediaRecorder.reset();
		}
		catch (IOException e) {
			ADLogger.debug(TAG, e.getLocalizedMessage());
		}
		catch (Exception e) {
			ADLogger.debug(TAG, e.getLocalizedMessage());
		}
	}
	
	private void processRecording(boolean showResult) {

		ADLogger.debug(TAG, "Process Recording !!!");
		ADLogger.debug(TAG, "PROCESSED SESSIONS : " + mProccessedMedia.toString());
		
		// Merge all frames
		new ADMergeRecordVideoTask(ADVideoRecordActivity.this, mProccessedMedia, showResult).execute();
	}
	
	private void updateEncodingOptions() {
		if (prRecordInProcess) {
			stopRecording();
			startRecording();
			Toast.makeText(prContext, "Recording restarted with new options!",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(prContext, "Recording options updated!",
					Toast.LENGTH_SHORT).show();
		}
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}
	
	private boolean discardSession() {
		
		boolean success = false;
		
		try {
			if (mClipSession != null)
				mClipSession = null;
			
			if (mProccessedMedia != null) {
				ADVideoSessionsUtil.getInstance().deleteProccessedMedia(mProccessedMedia);
				mProccessedMedia = null;
			}
			
			success = true;
		} catch (Exception e) {
			success = false;
		}
		
		return success;
	}
	
	private void setMuteAll(boolean mute) {
	    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

	    int[] streams = new int[] { AudioManager.STREAM_ALARM,
	        AudioManager.STREAM_DTMF, AudioManager.STREAM_MUSIC,
	        AudioManager.STREAM_RING, AudioManager.STREAM_SYSTEM,
	        AudioManager.STREAM_VOICE_CALL };

	    for (int stream : streams)
	        manager.setStreamMute(stream, mute);
	}
	
	private void setMuteWithReflection() {
		List<Integer> streams = new ArrayList<Integer>();
		Field[] fields = AudioManager.class.getFields();
		
		for (Field field : fields) 
		{
			if (field.getName().startsWith("STREAM_")
					&& Modifier.isStatic(field.getModifiers())
					&& field.getType() == int.class) {
				ADLogger.debug(TAG, "Stream : " + field.getName());
				
				try {
					Integer stream = (Integer) field.get(null);
		        	streams.add(stream);
				} catch (IllegalArgumentException e) {
					// do nothing
				} catch (IllegalAccessException e) {
					// do nothing
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/**
	 * Video Recording Progress
	 */
	public class RecordProgressBar {
		
		private Thread timerThread;
		private int progressStatus = 0;
		private int recordSeq = 0;
		private Handler handler = new Handler();
		private boolean isThreadActive;
		
		public void start() {
			
			isThreadActive = true;
			++recordSeq;
			
			// Start long running operation in a background thread
			timerThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (isThreadActive == true && progressStatus < PROGESS_TIMER_RUNTIME) {
						
						try {
							Thread.sleep(150);	// 150 msec speed
							progressStatus += 150;
							ADLogger.debug(TAG, "progressStatus = " + progressStatus);
							
							updateProgress(progressStatus);
						} catch (InterruptedException e) {

						}
					}
				}
			});
			timerThread.start();
		}

		public void stop() {

			if (timerThread != null) {
				isThreadActive = false;
				
				timerThread.interrupt();
				timerThread = null;
				
				ADLogger.debug(TAG, "Progress thread stop !!!");
			}
		}
		
		public int getProgressStatus() {
			return progressStatus;
		}
		
		public int getRecordSequence() {
			return recordSeq;
		}
		
		private void updateProgress(final int timePassed) {
			if (mProgressBar != null) {

			   ADLogger.debug(TAG, "timePassed = " + timePassed);
				
	           handler.post(new Runnable() {
	        	   public void run() {
	        		   mProgressBar.setProgress(timePassed);
	        		   mProgressTextView.setText("Progress (ms): " + timePassed + "/" + mProgressBar.getMax());
	        		   ADLogger.debug(TAG, "Progress (Milli seconds): " + timePassed);
	        		   
	        		   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	        		   Date date = new Date();

	        		   mTimeTextView.setText("Current Time: " + dateFormat.format(date));
	        		   
	        		   if (timePassed == Config.getInstance().getMinMediaProcessingThreshold()) {
	        			   prNextBtn.setVisibility( View.VISIBLE );
	        		   }
	        	   }
	           });
			}
		}
	}
	
	@SuppressLint("NewApi")
	private int setCameraDisplayOrientation(ADVideoRecordActivity activity, int cameraId,
			Camera camera) {

		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;

		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} 
		else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		
		camera.setDisplayOrientation(result);
		return result;
	}
	
	@SuppressLint("NewApi")
	private Point getDisplayDimensions() {
		
		Point size = new Point();
		Display display = getWindowManager().getDefaultDisplay();
		
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        	display.getSize(size);
        } 
        else {
        	size.set(display.getWidth(), display.getHeight());
        }
        
		return size;
	}
	
	public void showCancelDialog() {
		
		// if the video is not recorded, then pop back.
		if (mRecordProgressBar.getProgressStatus() == 0) {
			finish();
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ADVideoRecordActivity.this);
		
		builder.setMessage(getString(R.string.discard_dialog_title))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.discard_dialog_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								processRecording( Boolean.FALSE );	// save the session and return back
								
								// Save Session
								Toast.makeText(ADVideoRecordActivity.this, "Saved", Toast.LENGTH_SHORT)
										.show();
							}
						})
				.setNegativeButton(getString(R.string.discard_dialog_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								// Discard
								if (discardSession()) {
									finish();
								}
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
}