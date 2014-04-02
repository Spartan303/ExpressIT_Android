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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;

import com.ad.videorecorderlib.logger.ADLogger;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

public class ADMergeRecordVideoTask extends AsyncTask<Void, Integer, String> {

	protected static final String TAG = ADMergeRecordVideoTask.class.getSimpleName();

	private ProgressDialog pd;
	private Activity mActivity;
	
	private ADProcessedVideoMedia mProcessedMedia;

	private int mergingProgress = 0;
	
	private Handler handler = new Handler();
	
	private boolean mShowResultScreen;
	
	public ADMergeRecordVideoTask(Activity activity, ADProcessedVideoMedia processedMedia, boolean showResult) {
		super();

		this.mActivity = activity;
		this.mProcessedMedia = processedMedia;
		this.mShowResultScreen = showResult;
	}

	@Override
	protected void onPreExecute() {

		// Show the progress bar
		pd = new ProgressDialog( mActivity );
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("Rendering");
		pd.setCancelable(false);
		pd.show();
	}

	@Override
	protected String doInBackground(Void... params) {

		Boolean success = false;
		String sessionDirPath = mProcessedMedia.getDirectoryPath() + "/";
		String mOutputFilePath = sessionDirPath + "output.mp4";

		try {
			File dir = new File(sessionDirPath);
			if (dir != null) {

				mergingProgress += 20;
				updateProgress( mergingProgress );
				
				String[] fileNames = dir.list();
				Movie[] inMovies = new Movie[fileNames.length];

				for (int i = 0; i < fileNames.length; i++) {
					String filePath = sessionDirPath + fileNames[i];
					File f = new File(filePath);

					if (f.exists()) {
						ADLogger.debug(TAG, "File Path: " + filePath);
						inMovies[i] = MovieCreator.build(filePath);
					}
				}

				List<Track> videoTracks = new LinkedList<Track>();
				List<Track> audioTracks = new LinkedList<Track>();

				mergingProgress += 20;
				updateProgress( mergingProgress );
				
				for (Movie m : inMovies) {
					if (m != null) {
						for (Track t : m.getTracks()) {
							if (t != null && t.getHandler().equals("soun")) {
								audioTracks.add(t);
							}
							if (t != null && t.getHandler().equals("vide")) {
								videoTracks.add(t);
							}
						}
					}
				}

				mergingProgress += 20;
				updateProgress( mergingProgress );
				
				Movie result = new Movie();
				if (audioTracks.size() > 0) {
					result.addTrack(new AppendTrack(audioTracks
							.toArray(new Track[audioTracks.size()])));
				}
				if (videoTracks.size() > 0) {
					result.addTrack(new AppendTrack(videoTracks
							.toArray(new Track[videoTracks.size()])));
				}

				mergingProgress += 20;
				updateProgress( mergingProgress );
				
				Container out = new DefaultMp4Builder().build(result);

				FileChannel fc = new RandomAccessFile(String.format(mOutputFilePath), "rw").getChannel();
				out.writeContainer(fc);
				fc.close();

				mergingProgress += 20;
				updateProgress( mergingProgress );
				
				ADLogger.debug(TAG, "Success creation: ");
				mProcessedMedia.setOutputFile(new File( mOutputFilePath ));
				ADLogger.debug(TAG, "Processed Complete : " + mProcessedMedia);
				
				// Saved the processed media session
				ADVideoSessionsUtil.getInstance().addProcessedMedia(mProcessedMedia);

				success = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			ADLogger.debug(TAG, "Exception: " + e.toString());

			success = false;
		}

		return success.toString();
	}

	@Override
	protected void onPostExecute(String result) {
		
		// dismiss progress
		pd.dismiss();
		
		if (result != null) {
			
			if (this.mShowResultScreen) {
				// show the results
				Intent intent = ADVideoRecordResultActivity.newInstance(mActivity, mProcessedMedia);
				mActivity.startActivityForResult(intent, ADVideoRecordActivity.RESULT_VIDEO_CAPTURE_PATH_CODE);
			}
			else {
				mActivity.finish();
				mActivity.overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_out_right);
			}
		}
	}

	private void updateProgress(final int timePassed) {
		if (pd != null) {

           handler.post(new Runnable() {
        	   public void run() {
        		   pd.setProgress(timePassed);

        		   if (timePassed == 80) {
        			   pd.setMessage("Finishing up");
        		   }
        	   }
           });
		}
	}
}
