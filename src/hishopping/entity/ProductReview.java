package hishopping.entity;

import java.util.ArrayList;
import java.util.List;

public class ProductReview {
    private int reviewId;
    private int orderId;
    private int orderItemId;
    private int productId;
    private int userId;
    private String username;
    private String userAvatar;
    private boolean anonymous;
    private int rating;
    private String content;
    private String status;
    private int likeCount;
    private int replyCount;
    private int mediaCount;
    private boolean liked;
    private String skuText;
    private String selectedColor;
    private String selectedSpec;
    private List<ProductReviewMedia> mediaList = new ArrayList<ProductReviewMedia>();
    private String createTime;
    private String updateTime;

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    public int getMediaCount() { return mediaCount; }
    public void setMediaCount(int mediaCount) { this.mediaCount = mediaCount; }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public String getSkuText() { return skuText; }
    public void setSkuText(String skuText) { this.skuText = skuText; }
    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }
    public String getSelectedSpec() { return selectedSpec; }
    public void setSelectedSpec(String selectedSpec) { this.selectedSpec = selectedSpec; }
    public List<ProductReviewMedia> getMediaList() { return mediaList; }
    public void setMediaList(List<ProductReviewMedia> mediaList) { this.mediaList = mediaList == null ? new ArrayList<ProductReviewMedia>() : mediaList; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
