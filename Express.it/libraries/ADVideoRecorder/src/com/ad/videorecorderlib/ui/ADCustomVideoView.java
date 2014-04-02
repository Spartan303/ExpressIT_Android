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

package com.ad.videorecorderlib.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class ADCustomVideoView extends VideoView {

	private int mForceHeight = 60;
	private int mForceWidth = 60;

	private Context mContext;

	public ADCustomVideoView(Context context) {
		super(context);
		this.mContext = context;
	}

	public ADCustomVideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this.mContext = context;
	}

	public ADCustomVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;

	}

	public void setDimensions(int w, int h) {
		this.mForceHeight = h;
		this.mForceWidth = w;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mForceWidth, mForceHeight);
	}

	@Override
	public boolean isPlaying() {
		return super.isPlaying();
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public void stopPlayback() {
		// TODO Auto-generated method stub
		super.stopPlayback();
	}
}