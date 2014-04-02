package com.netpace.expressit.model;

public class Media {

	public enum MediaTypeEnum{
		IMAGE,
		VIDEO
	}
	
	private Long mediaID;
    private Long userID;
    
    private MediaTypeEnum mediaType;
    
    private String mediaURL;
    private String mediaShortURL;
    private String mediaName;
    private String mediaCaption;
    private String mediaDescription;
    
    private Integer mediaPoints;
    private Integer mediaUpVote;
    private Integer mediaDownVote;
    private Integer commentCount;
    private Integer mediaSortPosition;
    
    private Boolean hidden;
    private Boolean deleted;
    private Meta meta;
    
	public Media() {
		super();
	}

	public Long getMediaID() {
		return mediaID;
	}

	public void setMediaID(Long mediaID) {
		this.mediaID = mediaID;
	}

	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public MediaTypeEnum getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaTypeEnum mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaURL() {
		return mediaURL;
	}

	public void setMediaURL(String mediaURL) {
		this.mediaURL = mediaURL;
	}

	public String getMediaShortURL() {
		return mediaShortURL;
	}

	public void setMediaShortURL(String mediaShortURL) {
		this.mediaShortURL = mediaShortURL;
	}

	public String getMediaName() {
		return mediaName;
	}

	public void setMediaName(String mediaName) {
		this.mediaName = mediaName;
	}

	public String getMediaCaption() {
		return mediaCaption;
	}

	public void setMediaCaption(String mediaCaption) {
		this.mediaCaption = mediaCaption;
	}

	public String getMediaDescription() {
		return mediaDescription;
	}

	public void setMediaDescription(String mediaDescription) {
		this.mediaDescription = mediaDescription;
	}

	public Integer getMediaPoints() {
		return mediaPoints;
	}

	public void setMediaPoints(Integer mediaPoints) {
		this.mediaPoints = mediaPoints;
	}

	public Integer getMediaUpVote() {
		return mediaUpVote;
	}

	public void setMediaUpVote(Integer mediaUpVote) {
		this.mediaUpVote = mediaUpVote;
	}

	public Integer getMediaDownVote() {
		return mediaDownVote;
	}

	public void setMediaDownVote(Integer mediaDownVote) {
		this.mediaDownVote = mediaDownVote;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}

	public Integer getMediaSortPosition() {
		return mediaSortPosition;
	}

	public void setMediaSortPosition(Integer mediaSortPosition) {
		this.mediaSortPosition = mediaSortPosition;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	@Override
	public String toString() {
		return "Media [mediaID=" + mediaID + ", userID=" + userID
				+ ", mediaType=" + mediaType + ", mediaURL=" + mediaURL
				+ ", mediaShortURL=" + mediaShortURL + ", mediaName="
				+ mediaName + ", mediaCaption=" + mediaCaption
				+ ", mediaDescription=" + mediaDescription + ", mediaPoints="
				+ mediaPoints + ", mediaUpVote=" + mediaUpVote
				+ ", mediaDownVote=" + mediaDownVote + ", commentCount="
				+ commentCount + ", mediaSortPosition=" + mediaSortPosition
				+ ", hidden=" + hidden + ", deleted=" + deleted + ", meta="
				+ meta + "]";
	}
	
}