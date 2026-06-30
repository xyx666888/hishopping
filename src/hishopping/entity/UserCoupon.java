package hishopping.entity;

public class UserCoupon {
    private int userCouponId;
    private int couponId;
    private int userId;
    private String couponName;
    private String couponType;
    private double amount;
    private double discountRate;
    private double minAmount;
    private int vipLevel;
    private String status;
    private String receiveTime;
    private String expireTime;
    private String useTime;
    private int orderId;
    private String issueBatchNo;
    private String couponOwnerType;
    private int merchantId;
    private String shopName;
    private boolean stackable;
    private String useScope;
    private String description;

    public int getUserCouponId() { return userCouponId; }
    public void setUserCouponId(int userCouponId) { this.userCouponId = userCouponId; }
    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public String getCouponType() { return couponType; }
    public void setCouponType(String couponType) { this.couponType = couponType; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
    public double getMinAmount() { return minAmount; }
    public void setMinAmount(double minAmount) { this.minAmount = minAmount; }
    public int getVipLevel() { return vipLevel; }
    public void setVipLevel(int vipLevel) { this.vipLevel = vipLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReceiveTime() { return receiveTime; }
    public void setReceiveTime(String receiveTime) { this.receiveTime = receiveTime; }
    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }
    public String getUseTime() { return useTime; }
    public void setUseTime(String useTime) { this.useTime = useTime; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getIssueBatchNo() { return issueBatchNo; }
    public void setIssueBatchNo(String issueBatchNo) { this.issueBatchNo = issueBatchNo; }
    public String getCouponOwnerType() { return couponOwnerType; }
    public void setCouponOwnerType(String couponOwnerType) { this.couponOwnerType = couponOwnerType; }
    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public boolean isStackable() { return stackable; }
    public void setStackable(boolean stackable) { this.stackable = stackable; }
    public String getUseScope() { return useScope; }
    public void setUseScope(String useScope) { this.useScope = useScope; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
