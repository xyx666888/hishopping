package hishopping.entity;

public class ReviewReply {
    private int replyId;
    private int reviewId;
    private int parentReplyId;
    private String userType;
    private int userId;
    private String userName;
    private String userAvatar;
    private String content;
    private String status;
    private String createTime;

    public int getReplyId() { return replyId; }
    public void setReplyId(int replyId) { this.replyId = replyId; }
    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }
    public int getParentReplyId() { return parentReplyId; }
    public void setParentReplyId(int parentReplyId) { this.parentReplyId = parentReplyId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
