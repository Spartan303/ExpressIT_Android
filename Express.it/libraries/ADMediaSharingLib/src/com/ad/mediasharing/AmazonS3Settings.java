package com.ad.mediasharing;

public class AmazonS3Settings {

	public enum AmazonS3UploadType {
		
		TVM_ANONYMOUS_TYPE,
		TVM_ANONYMOUS_IDENTITY_TYPE,
		DIRECT_S3_TYPE
	}
	
	private AmazonS3UploadType amazonS3UploadType;
	private String tokenVendingMachineURL;
	private String amazonS3BucketName;
	private String accessKey;
	private String accessSecrateKey;
	private String amazonStorageFilePath;
	private String remoteMediaFileName;
	private boolean useSSL;
	
		
	public AmazonS3Settings(AmazonS3UploadType amazonS3UploadType,
			String tokenVendingMachineURL, String amazonS3BucketName,
			String remoteMediaFileName, boolean useSSL) {
		super();
		this.amazonS3UploadType = amazonS3UploadType;
		this.tokenVendingMachineURL = tokenVendingMachineURL;
		this.amazonS3BucketName = amazonS3BucketName;
		this.remoteMediaFileName = remoteMediaFileName;
		this.useSSL = useSSL;
	}

	public AmazonS3Settings(AmazonS3UploadType amazonS3UploadType,
			String tokenVendingMachineURL, String amazonS3BucketName,
			String accessKey, String accessSecrateKey,
			String amazonStorageFilePath, String remoteMediaFileName,
			boolean useSSL) {
		super();
		this.amazonS3UploadType = amazonS3UploadType;
		this.tokenVendingMachineURL = tokenVendingMachineURL;
		this.amazonS3BucketName = amazonS3BucketName;
		this.accessKey = accessKey;
		this.accessSecrateKey = accessSecrateKey;
		this.amazonStorageFilePath = amazonStorageFilePath;
		this.remoteMediaFileName = remoteMediaFileName;
		this.useSSL = useSSL;
	}

	public AmazonS3UploadType getAmazonS3UploadType() {
		return amazonS3UploadType;
	}
	
	public String getTokenVendingMachineURL() {
		return tokenVendingMachineURL;
	}

	public String getAmazonS3BucketName() {
		return amazonS3BucketName;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getAccessSecrateKey() {
		return accessSecrateKey;
	}

	public String getAmazonStorageFilePath() {
		return amazonStorageFilePath;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public String getRemoteMediaFileName() {
		return remoteMediaFileName;
	}

	@Override
	public String toString() {
		return "AmazonS3Settings [amazonS3UploadType=" + amazonS3UploadType
				+ ", tokenVendingMachineURL=" + tokenVendingMachineURL
				+ ", amazonS3BucketName=" + amazonS3BucketName + ", accessKey="
				+ accessKey + ", accessSecrateKey=" + accessSecrateKey
				+ ", amazonStorageFilePath=" + amazonStorageFilePath
				+ ", remoteMediaFileName=" + remoteMediaFileName + ", useSSL="
				+ useSSL + "]";
	}
}
