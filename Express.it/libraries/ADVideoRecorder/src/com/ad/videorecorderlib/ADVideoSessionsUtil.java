package com.ad.videorecorderlib;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.ad.videorecorderlib.logger.ADLogger;
import com.ad.videorecorderlib.settings.Config;

public class ADVideoSessionsUtil {

	protected static final String TAG = ADVideoSessionsUtil.class.getSimpleName();
	
	public static ADVideoSessionsUtil instance = new ADVideoSessionsUtil();
	
	private LinkedHashSet<ADProcessedVideoMedia> mSavedMediaSessions;
	
	private ADVideoSessionsUtil() {
		
		mSavedMediaSessions = new LinkedHashSet<ADProcessedVideoMedia>();
	}
	
	public static ADVideoSessionsUtil getInstance() {
		return instance;
	}
	
	/**
	 * Load from the disk
	 */
	public void loadAllProcessedMedia() {
		
		try {
			
			
			
			
		} catch (Exception e) {
			ADLogger.debug(TAG, "Exception : " + e.getMessage());
		}
	}
	
	/**
	 * 
	 */
	public boolean isProccessedMediaExist(ADProcessedVideoMedia media) {
		try {
			if (media == null) {
				ADLogger.debug(TAG, "media object can not be null");
				return false;
			}
			
			Iterator<ADProcessedVideoMedia> iterator = mSavedMediaSessions.iterator();
			while (iterator.hasNext()) {
				ADProcessedVideoMedia pm = iterator.next();
				if (pm.getId() != null && media.getId() != null 
						&& (pm.getId().equalsIgnoreCase(media.getId()))) {
					
					return true;
				}
			}
		} catch (Exception e) {
			ADLogger.debug(TAG, e.getMessage());
		}
		
		return false;
	}
	
	/**
	 * Add Processed Media into the bucket.
	 */
	public void addProcessedMedia(ADProcessedVideoMedia session) {
		
		// add new media session object.
		if (!isProccessedMediaExist(session))
			mSavedMediaSessions.add(session);
	}
	
	/**
	 * Delete the processed media from disk
	 */
	public boolean deleteProccessedMediaClips(ADProcessedVideoMedia session) {
		
		boolean success = false;
		
		try {
			if (session == null) {
				ADLogger.debug(TAG, "Session object can not be null");
				return success;
			}

			// delete the whole processed directory
			String dirPath = session.getDirectoryPath();
			if (dirPath != null) {
				File dir = new File(dirPath);
	
				if (dir.exists() && dir.isDirectory()) {
					String[] children = dir.list();
		            for (int i=0; i < children.length; i++) {
		                success = deleteDir(new File(dir, children[i]));
		                break;
		            }
				}
			}
		} catch (Exception e) {
			ADLogger.debug(TAG, "Exception : " + e.getMessage());
		}

		return success;
	}	
	
	/**
	 * Delete the processed media from disk
	 */
	public boolean deleteProccessedMedia(ADProcessedVideoMedia session) {
		
		boolean success = false;
		
		try {
			if (session == null) {
				ADLogger.debug(TAG, "Session object can not be null");
				return success;
			}

			// delete the whole processed directory
			String dirPath = session.getDirectoryPath();
			if (dirPath != null) {
				File f = new File(dirPath);
	
				if (f.exists())
					success = deleteDir( f );
			}
		} catch (Exception e) {
			ADLogger.debug(TAG, "Exception : " + e.getMessage());
		}

		return success;
	}	
	
	/**
	 * Delete all processed media sessions
	 */
	public boolean deleteAllProccessedMedia() {
		
		boolean success = false;
		String path = Config.getInstance().getVideoOutputFilePath();
		File parentDir = new File(path);

		try {
			success = deleteDir( parentDir );
			
		} catch (Exception e) {
			ADLogger.debug(TAG, "Exception : " + e.getMessage());
			success = false;
		}

		return success;
	}
	
    // Deletes all files and sub directories under directory.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
}
