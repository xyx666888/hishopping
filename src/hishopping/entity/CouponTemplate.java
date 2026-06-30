package hishopping.entity;

public class CouponTemplate {
    private int couponId;
    private String couponName;
    private String couponType;
    private double amount;
    private double discountRate;
    private double minAmount;
    private String targetType;
    private String targetValue;
    private int vipLevel;
    private int totalQuantity;
    private int perUserLimit;
    private String startTime;
    private String endTime;
    private int validDays;
    private boolean newUserCoupon;
    private boolean vipCoupon;
    private String status;
    private String createTime;
    private String couponOwnerType;
    private int merchantId;
    private String shopName;
    private boolean stackable;
    private String homeTitle;
    private String homeSubtitle;
    private String useScope;
    private String description;

    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }
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
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public int getVipLevel() { return vipLevel; }
    public void setVipLevel(int vipLevel) { this.vipLevel = vipLevel; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public int getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(int perUserLimit) { this.perUserLimit = perUserLimit; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public int getValidDays() { return validDays; }
    public void setValidDays(int validDays) { this.validDays = validDays; }
    public boolean isNewUserCoupon() { return newUserCoupon; }
    public void setNewUserCoupon(boolean newUserCoupon) { this.newUserCoupon = newUserCoupon; }
    public boolean isVipCoupon() { return vipCoupon; }
    public void setVipCoupon(boolean vipCoupon) { this.vipCoupon = vipCoupon; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getCouponOwnerType() { return couponOwnerType; }
    public void setCouponOwnerType(String couponOwnerType) { this.couponOwnerType = couponOwnerType; }
    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public boolean isStackable() { return stackable; }
    public void setStackable(boolean stackable) { this.stackable = stackable; }
    public String getHomeTitle() { return homeTitle; }
    public void setHomeTitle(String homeTitle) { this.homeTitle = homeTitle; }
    public String getHomeSubtitle() { return homeSubtitle; }
    public void setHomeSubtitle(String homeSubtitle) { this.homeSubtitle = homeSubtitle; }
    public String getUseScope() { return useScope; }
    public void setUseScope(String useScope) { this.useScope = useScope; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
