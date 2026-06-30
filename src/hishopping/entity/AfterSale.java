package hishopping.entity;

public class AfterSale {
    private int afterSaleId;
    private int orderId;
    private int userId;
    private int merchantId;
    private int productId;
    private String afterSaleType;
    private String reason;
    private double refundAmount;
    private String status;
    private String applyTime;
    private String handleOpinion;
    private String handleTime;

    public int getAfterSaleId() { return afterSaleId; }
    public void setAfterSaleId(int afterSaleId) { this.afterSaleId = afterSaleId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getAfterSaleType() { return afterSaleType; }
    public void setAfterSaleType(String afterSaleType) { this.afterSaleType = afterSaleType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApplyTime() { return applyTime; }
    public void setApplyTime(String applyTime) { this.applyTime = applyTime; }
    public String getHandleOpinion() { return handleOpinion; }
    public void setHandleOpinion(String handleOpinion) { this.handleOpinion = handleOpinion; }
    public String getHandleTime() { return handleTime; }
    public void setHandleTime(String handleTime) { this.handleTime = handleTime; }
}
