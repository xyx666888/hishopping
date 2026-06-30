package hishopping.entity;

public class AdminOperationLog {
    private int logId;
    private int adminId;
    private String operationType;
    private String targetType;
    private int targetId;
    private String content;
    private String operationTime;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOperationTime() { return operationTime; }
    public void setOperationTime(String operationTime) { this.operationTime = operationTime; }
}
