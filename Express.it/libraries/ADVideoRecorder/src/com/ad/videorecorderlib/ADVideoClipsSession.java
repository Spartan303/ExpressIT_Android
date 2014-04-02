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
import java.io.Serializable;

public class ADVideoClipsSession implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private File mediaFile;
	private int mediaLengthInMs;
	private int seqOrder;
	
	public ADVideoClipsSession(String id, File mediaFile, int mediaLengthInMs) {
		super();
		
		this.id = id;
		this.mediaFile = mediaFile;
		this.mediaLengthInMs = mediaLengthInMs;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public File getMediaFile() {
		return mediaFile;
	}

	public void setMediaFile(File mediaFile) {
		this.mediaFile = mediaFile;
	}

	public int getMediaLengthInMs() {
		return mediaLengthInMs;
	}

	public void setMediaLengthInMs(int mediaLengthInMs) {
		this.mediaLengthInMs = mediaLengthInMs;
	}

	public int getSeqOrder() {
		return seqOrder;
	}

	public void setSeqOrder(int seqOrder) {
		this.seqOrder = seqOrder;
	}

	@Override
	public String toString() {
		return "MediaSession [id=" + id + ", mediaFile=" + mediaFile
				+ ", mediaLengthInMs=" + mediaLengthInMs + ", seqOrder="
				+ seqOrder + "]";
	}
}
