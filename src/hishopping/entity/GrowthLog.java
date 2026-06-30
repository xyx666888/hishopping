package hishopping.entity;

public class GrowthLog {
    private int logId;
    private int userId;
    private int growthDelta;
    private int pointsDelta;
    private String sourceType;
    private int sourceId;
    private String remark;
    private String createTime;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getGrowthDelta() { return growthDelta; }
    public void setGrowthDelta(int growthDelta) { this.growthDelta = growthDelta; }
    public int getPointsDelta() { return pointsDelta; }
    public void setPointsDelta(int pointsDelta) { this.pointsDelta = pointsDelta; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public int getSourceId() { return sourceId; }
    public void setSourceId(int sourceId) { this.sourceId = sourceId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
