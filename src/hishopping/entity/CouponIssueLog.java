package hishopping.entity;

public class CouponIssueLog {
    private int issueLogId;
    private int couponId;
    private String couponName;
    private String issueBatchNo;
    private String issueType;
    private String targetValue;
    private int issueCount;
    private int skipCount;
    private int adminId;
    private String issueTime;
    private String remark;

    public int getIssueLogId() { return issueLogId; }
    public void setIssueLogId(int issueLogId) { this.issueLogId = issueLogId; }
    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public String getIssueBatchNo() { return issueBatchNo; }
    public void setIssueBatchNo(String issueBatchNo) { this.issueBatchNo = issueBatchNo; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public int getIssueCount() { return issueCount; }
    public void setIssueCount(int issueCount) { this.issueCount = issueCount; }
    public int getSkipCount() { return skipCount; }
    public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public String getIssueTime() { return issueTime; }
    public void setIssueTime(String issueTime) { this.issueTime = issueTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
