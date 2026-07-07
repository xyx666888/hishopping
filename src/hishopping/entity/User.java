package hishopping.entity;

public class User {
    private int id;
    private String accountId;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String role;
    private int points;
    private int vipLevel;
    private int growthValue;
    private String status;
    private String avatarUrl;
    private String punishReason;
    private String punishStartTime;
    private String punishEndTime;
    private String cancelRequestTime;
    private String cancelDeadlineTime;
    private String cancelCancelTime;
    private String createTime;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public int getVipLevel() { return vipLevel; }
    public void setVipLevel(int vipLevel) { this.vipLevel = vipLevel; }
    public int getGrowthValue() { return growthValue; }
    public void setGrowthValue(int growthValue) { this.growthValue = growthValue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPunishReason() { return punishReason; }
    public void setPunishReason(String punishReason) { this.punishReason = punishReason; }
    public String getPunishStartTime() { return punishStartTime; }
    public void setPunishStartTime(String punishStartTime) { this.punishStartTime = punishStartTime; }
    public String getPunishEndTime() { return punishEndTime; }
    public void setPunishEndTime(String punishEndTime) { this.punishEndTime = punishEndTime; }
    public String getCancelRequestTime() { return cancelRequestTime; }
    public void setCancelRequestTime(String cancelRequestTime) { this.cancelRequestTime = cancelRequestTime; }
    public String getCancelDeadlineTime() { return cancelDeadlineTime; }
    public void setCancelDeadlineTime(String cancelDeadlineTime) { this.cancelDeadlineTime = cancelDeadlineTime; }
    public String getCancelCancelTime() { return cancelCancelTime; }
    public void setCancelCancelTime(String cancelCancelTime) { this.cancelCancelTime = cancelCancelTime; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

