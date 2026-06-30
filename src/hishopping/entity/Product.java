package hishopping.entity;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private int categoryId;
    private String categoryName;
    private String name;
    private String shortDesc;
    private String detailDesc;
    private double price;
    private double oldPrice;
    private double rating;
    private int sales;
    private int stock;
    private String tag;
    private String imageUrl;
    private String gradient;
    private String iconText;
    private String colorOptions;
    private String specOptions;
    private String skuAttrs;
    private String skuOptions;
    private String status;
    private int merchantId;
    private String merchantCode;
    private String shopName;
    private String saleStatus;
    private String auditStatus;
    private String auditOpinion;
    private String submitTime;
    private String reviewTime;
    private int reviewAdminId;
    private int reviewCount;
    private double averageRating;
    private int favoriteCount;
    private int reviewLikeCount;
    private List<ProductMedia> mediaList = new ArrayList<ProductMedia>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortDesc() { return shortDesc; }
    public void setShortDesc(String shortDesc) { this.shortDesc = shortDesc; }
    public String getDetailDesc() { return detailDesc; }
    public void setDetailDesc(String detailDesc) { this.detailDesc = detailDesc; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getOldPrice() { return oldPrice; }
    public void setOldPrice(double oldPrice) { this.oldPrice = oldPrice; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getSales() { return sales; }
    public void setSales(int sales) { this.sales = sales; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getGradient() { return gradient; }
    public void setGradient(String gradient) { this.gradient = gradient; }
    public String getIconText() { return iconText; }
    public void setIconText(String iconText) { this.iconText = iconText; }
    public String getColorOptions() { return colorOptions; }
    public void setColorOptions(String colorOptions) { this.colorOptions = colorOptions; }
    public String getSpecOptions() { return specOptions; }
    public void setSpecOptions(String specOptions) { this.specOptions = specOptions; }
    public String getSkuAttrs() { return skuAttrs; }
    public void setSkuAttrs(String skuAttrs) { this.skuAttrs = skuAttrs; }
    public String getSkuOptions() { return skuOptions; }
    public void setSkuOptions(String skuOptions) { this.skuOptions = skuOptions; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { this.merchantCode = merchantCode; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getSaleStatus() { return saleStatus; }
    public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public String getSubmitTime() { return submitTime; }
    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }
    public String getReviewTime() { return reviewTime; }
    public void setReviewTime(String reviewTime) { this.reviewTime = reviewTime; }
    public int getReviewAdminId() { return reviewAdminId; }
    public void setReviewAdminId(int reviewAdminId) { this.reviewAdminId = reviewAdminId; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }
    public int getReviewLikeCount() { return reviewLikeCount; }
    public void setReviewLikeCount(int reviewLikeCount) { this.reviewLikeCount = reviewLikeCount; }
    public List<ProductMedia> getMediaList() { return mediaList; }
    public void setMediaList(List<ProductMedia> mediaList) { this.mediaList = mediaList; }
}

