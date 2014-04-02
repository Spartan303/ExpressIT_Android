package com.netpace.expressit.exception;

import java.io.File;
import java.io.IOException;

import android.os.Debug;

public class HeapDumpingUncaughtExceptionHandler implements
		Thread.UncaughtExceptionHandler {

	private static final String HPROF_DUMP_BASENAME = "CTV.dalvik-hprof";
	private final String dataDir;

	public HeapDumpingUncaughtExceptionHandler(String dataDir) {
		this.dataDir = dataDir;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		String absPath = new File(dataDir, HPROF_DUMP_BASENAME).getAbsolutePath();
		if (ex.getClass().equals(OutOfMemoryError.class)) {
			try {
				Debug.dumpHprofData(absPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ex.printStackTrace();
	}
}
