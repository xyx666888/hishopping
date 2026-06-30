package hishopping.entity;

public class ProductMedia {
    private int id;
    private int productId;
    private String mediaType;
    private String mediaUrl;
    private int sortNo;
    private boolean coverFlag;
    private String createTime;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public int getSortNo() { return sortNo; }
    public void setSortNo(int sortNo) { this.sortNo = sortNo; }
    public boolean isCoverFlag() { return coverFlag; }
    public void setCoverFlag(boolean coverFlag) { this.coverFlag = coverFlag; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
