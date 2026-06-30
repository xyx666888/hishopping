package hishopping.entity;

public class UploadResource {
    private int resourceId;
    private int merchantId;
    private String fileName;
    private String accessUrl;
    private long fileSize;
    private String uploadTime;
    private int productId;

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }
    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getAccessUrl() { return accessUrl; }
    public void setAccessUrl(String accessUrl) { this.accessUrl = accessUrl; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
}
