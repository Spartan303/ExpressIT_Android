package com.ad.videorecorderlib;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.ad.videorecorderlib.logger.ADLogger;
import com.ad.videorecorderlib.settings.Config;

public class ADProcessedVideoMedia implements Serializable {

	protected static final String TAG = ADProcessedVideoMedia.class.getSimpleName();
	
	private static final long serialVersionUID = 1L;

	private String id;
	
	private File outputFile;
	
	private int mediaLengthInMs;

	private LinkedHashSet<ADVideoClipsSession> sessions;

	public ADProcessedVideoMedia(String id) { 
		
		super();
		this.id = id;
		this.sessions = new LinkedHashSet<ADVideoClipsSession>();
	}
	
	public ADProcessedVideoMedia(String id, File outputFile, int mediaLengthInMs) { 
		
		super();
		this.id = id;
		this.outputFile = outputFile;
		this.mediaLengthInMs = mediaLengthInMs;
		this.sessions = new LinkedHashSet<ADVideoClipsSession>();
	}

	public ADProcessedVideoMedia(String id, File outputFile, int mediaLengthInMs,
			LinkedHashSet<ADVideoClipsSession> sessions) {
		
		super();
		this.id = id;
		this.outputFile = outputFile;
		this.mediaLengthInMs = mediaLengthInMs;
		this.sessions = (sessions != null) ? sessions : new LinkedHashSet<ADVideoClipsSession>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDirectoryPath() {
		
		if (this.id == null)
			return null;
		
		String path = Config.getInstance().getVideoOutputFilePath() + getId();
		return path;
	}
	
	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public int getMediaLengthInMs() {
		return mediaLengthInMs;
	}

	public void setMediaLengthInMs(int mediaLengthInMs) {
		this.mediaLengthInMs = mediaLengthInMs;
	}

	public LinkedHashSet<ADVideoClipsSession> getSessions() {
		return sessions;
	}

	public void setSessions(LinkedHashSet<ADVideoClipsSession> sessions) {
		this.sessions = sessions;
	}
	
	public void addSession(ADVideoClipsSession session) {
		
		if (!isSessionExist( session )) {
			sessions.add(session);
			mediaLengthInMs += session.getMediaLengthInMs();	// increment the media session length
		}
	}

	public boolean isSessionExist(ADVideoClipsSession session) {
		try {
			if (session == null) {
				ADLogger.debug(TAG, "media session can not be null");
				return false;
			}
			
			Iterator<ADVideoClipsSession> iterator = sessions.iterator();
			while (iterator.hasNext()) {
				ADVideoClipsSession m = iterator.next();
				if (m.getId() != null && session.getId() != null 
						&& (m.getId().equalsIgnoreCase(session.getId()))) {
					
					return true;
				}
			}
		} catch (Exception e) {
			ADLogger.debug(TAG, e.getMessage());
		}
		
		return false;
	}
	@Override
	public String toString() {
		return "ProcessedMedia [id=" + id + ", outputFile=" + outputFile
				+ ", mediaLengthInMs=" + mediaLengthInMs + ", sessions="
				+ sessions + "]";
	}
}
