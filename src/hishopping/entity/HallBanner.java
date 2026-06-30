package hishopping.entity;

public class HallBanner {
    private int id;
    private String mediaType;
    private String mediaUrl;
    private String title;
    private String subtitle;
    private boolean enabled;
    private int sortNo;
    private boolean linkEnabled;
    private String linkType;
    private String linkTarget;
    private int productId;
    private boolean overlayEnabled;
    private String textPosition;
    private String titleColor;
    private String subtitleColor;
    private boolean videoMutedDefault;
    private boolean videoDisableSeek;
    private boolean videoDisablePause;
    private String createTime;
    private String updateTime;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getSortNo() { return sortNo; }
    public void setSortNo(int sortNo) { this.sortNo = sortNo; }
    public boolean isLinkEnabled() { return linkEnabled; }
    public void setLinkEnabled(boolean linkEnabled) { this.linkEnabled = linkEnabled; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    public String getLinkTarget() { return linkTarget; }
    public void setLinkTarget(String linkTarget) { this.linkTarget = linkTarget; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public boolean isOverlayEnabled() { return overlayEnabled; }
    public void setOverlayEnabled(boolean overlayEnabled) { this.overlayEnabled = overlayEnabled; }
    public String getTextPosition() { return textPosition; }
    public void setTextPosition(String textPosition) { this.textPosition = textPosition; }
    public String getTitleColor() { return titleColor; }
    public void setTitleColor(String titleColor) { this.titleColor = titleColor; }
    public String getSubtitleColor() { return subtitleColor; }
    public void setSubtitleColor(String subtitleColor) { this.subtitleColor = subtitleColor; }
    public boolean isVideoMutedDefault() { return videoMutedDefault; }
    public void setVideoMutedDefault(boolean videoMutedDefault) { this.videoMutedDefault = videoMutedDefault; }
    public boolean isVideoDisableSeek() { return videoDisableSeek; }
    public void setVideoDisableSeek(boolean videoDisableSeek) { this.videoDisableSeek = videoDisableSeek; }
    public boolean isVideoDisablePause() { return videoDisablePause; }
    public void setVideoDisablePause(boolean videoDisablePause) { this.videoDisablePause = videoDisablePause; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
