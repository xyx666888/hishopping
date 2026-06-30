package hishopping.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hishopping.dao.CouponDao;
import hishopping.entity.CouponIssueLog;
import hishopping.entity.CouponTemplate;
import hishopping.entity.UserCoupon;

public class CouponService {
    private CouponDao couponDao = new CouponDao();

    public List<CouponTemplate> templates() {
        return couponDao.templates();
    }

    public List<CouponTemplate> platformTemplates() {
        return couponDao.platformTemplates();
    }

    public List<CouponTemplate> merchantTemplates(int merchantId) {
        return couponDao.merchantTemplates(merchantId);
    }

    public CouponTemplate findTemplate(int couponId) {
        return couponDao.findTemplate(couponId);
    }

    public void saveTemplate(CouponTemplate c) {
        normalizeTemplate(c);
        if ("MERCHANT".equals(c.getCouponOwnerType())) {
            throw new RuntimeException("\u5e73\u53f0\u540e\u53f0\u4e0d\u80fd\u521b\u5efa\u5e97\u94fa\u5238\uff0c\u8bf7\u5728\u5546\u5bb6\u7aef\u7ba1\u7406\u3002");
        }
        c.setCouponOwnerType("PLATFORM");
        c.setMerchantId(0);
        if (c.getCouponId() > 0) couponDao.updateTemplate(c); else couponDao.saveTemplate(c);
    }

    public void saveMerchantTemplate(CouponTemplate c, int merchantId) {
        normalizeTemplate(c);
        if (merchantId <= 0) throw new RuntimeException("\u5546\u5bb6\u8eab\u4efd\u5f02\u5e38\uff0c\u65e0\u6cd5\u7ba1\u7406\u5e97\u94fa\u5238\u3002");
        c.setCouponOwnerType("MERCHANT");
        c.setMerchantId(merchantId);
        c.setUseScope("MERCHANT");
        c.setStackable(false);
        c.setNewUserCoupon(false);
        c.setVipCoupon(false);
        CouponTemplate old = c.getCouponId() > 0 ? couponDao.findTemplate(c.getCouponId()) : null;
        if (old != null && old.getMerchantId() != merchantId) throw new RuntimeException("\u53ea\u80fd\u7ba1\u7406\u672c\u5e97\u4f18\u60e0\u5238\u3002");
        if (c.getCouponId() > 0) couponDao.updateTemplate(c); else couponDao.saveTemplate(c);
    }

    private void normalizeTemplate(CouponTemplate c) {
        if (empty(c.getCouponName())) throw new RuntimeException("\u8bf7\u8f93\u5165\u4f18\u60e0\u5238\u540d\u79f0\u3002");
        if (empty(c.getCouponType())) c.setCouponType("AMOUNT");
        if (c.getAmount() < 0 || c.getDiscountRate() < 0 || c.getMinAmount() < 0) throw new RuntimeException("\u4f18\u60e0\u91d1\u989d\u548c\u95e8\u69db\u4e0d\u80fd\u4e3a\u8d1f\u6570\u3002");
        if ("DISCOUNT".equals(c.getCouponType()) && (c.getDiscountRate() <= 0 || c.getDiscountRate() >= 1)) throw new RuntimeException("\u6298\u6263\u5238\u8bf7\u8f93\u51650\u52301\u4e4b\u95f4\u7684\u6298\u6263\u7387\u3002");
        if (empty(c.getTargetType())) c.setTargetType(c.getVipLevel() > 0 ? "VIP_LEVEL" : "ALL");
        if (c.getValidDays() <= 0) c.setValidDays("NEW_USER".equals(c.getCouponType()) ? 7 : 30);
        if (c.getPerUserLimit() <= 0) c.setPerUserLimit(1);
        if (c.getTotalQuantity() <= 0) c.setTotalQuantity(9999);
        if (empty(c.getStatus())) c.setStatus("ENABLED");
        if (empty(c.getCouponOwnerType())) c.setCouponOwnerType("PLATFORM");
        if (empty(c.getUseScope())) c.setUseScope("MERCHANT".equals(c.getCouponOwnerType()) ? "MERCHANT" : "ALL");
        if (empty(c.getHomeTitle())) c.setHomeTitle(c.getCouponName());
        if (empty(c.getHomeSubtitle())) c.setHomeSubtitle(c.getMinAmount() > 0 ? "\u6ee1" + Math.round(c.getMinAmount()) + "\u53ef\u7528" : "\u65e0\u95e8\u69db\u4f18\u60e0");
        if (empty(c.getDescription())) c.setDescription("MERCHANT".equals(c.getCouponOwnerType()) ? "\u4ec5\u9650\u672c\u5e97\u5546\u54c1\u4f7f\u7528" : "\u5e73\u53f0\u4f18\u60e0\u5238");
    }

    public void updateTemplateStatus(int id, String status) {
        couponDao.updateTemplateStatus(id, status);
    }

    public void updateMerchantTemplateStatus(int id, int merchantId, String status) {
        CouponTemplate old = couponDao.findTemplate(id);
        if (old == null || old.getMerchantId() != merchantId) throw new RuntimeException("\u53ea\u80fd\u64cd\u4f5c\u672c\u5e97\u4f18\u60e0\u5238\u3002");
        couponDao.updateTemplateStatus(id, status);
    }

    public void deleteTemplate(int id) {
        couponDao.deleteTemplate(id);
    }

    public void deleteMerchantTemplate(int id, int merchantId) {
        CouponTemplate old = couponDao.findTemplate(id);
        if (old == null || old.getMerchantId() != merchantId) throw new RuntimeException("\u53ea\u80fd\u5220\u9664\u672c\u5e97\u4f18\u60e0\u5238\u3002");
        couponDao.deleteTemplate(id);
    }

    public IssueResult issue(int couponId, String issueType, String targetValue, int adminId) {
        CouponTemplate template = couponDao.findTemplate(couponId);
        if (template == null) throw new RuntimeException("\u4f18\u60e0\u5238\u6a21\u677f\u4e0d\u5b58\u5728\u3002");
        if (!"PLATFORM".equals(template.getCouponOwnerType())) throw new RuntimeException("\u540e\u53f0\u53ea\u80fd\u53d1\u653e\u5e73\u53f0\u5238\u3002");
        return issueTemplate(template, issueType, targetValue, adminId, 0, "\u540e\u53f0\u53d1\u653e");
    }

    public IssueResult issueMerchant(int couponId, String issueType, String targetValue, int merchantId) {
        CouponTemplate template = couponDao.findTemplate(couponId);
        if (template == null || template.getMerchantId() != merchantId) throw new RuntimeException("\u53ea\u80fd\u53d1\u653e\u672c\u5e97\u4f18\u60e0\u5238\u3002");
        return issueTemplate(template, issueType, targetValue, 0, merchantId, "\u5546\u5bb6\u53d1\u653e");
    }

    public IssueResult issueToSingleUser(int couponId, int userId, int adminId) {
        if (userId <= 0) throw new RuntimeException("\u8bf7\u9009\u62e9\u8981\u53d1\u5238\u7684\u7528\u6237\u3002");
        CouponTemplate template = couponDao.findTemplate(couponId);
        if (template == null) throw new RuntimeException("\u4f18\u60e0\u5238\u6a21\u677f\u4e0d\u5b58\u5728\u3002");
        if (!"ENABLED".equals(template.getStatus())) throw new RuntimeException("\u4f18\u60e0\u5238\u672a\u542f\u7528\uff0c\u4e0d\u80fd\u53d1\u653e\u3002");
        if (couponDao.receivedCount(userId, couponId) >= template.getPerUserLimit()) {
            String batchNo = batchNo();
            couponDao.logIssue(couponId, batchNo, "USER", String.valueOf(userId), 0, 1, adminId, "\u5355\u7528\u6237\u53d1\u653e\u8d85\u51fa\u9650\u989d");
            return new IssueResult(batchNo, 0, 1);
        }
        String batchNo = batchNo();
        couponDao.issueToUser(template, userId, batchNo);
        couponDao.logIssue(couponId, batchNo, "USER", String.valueOf(userId), 1, 0, adminId, "\u540e\u53f0\u5355\u7528\u6237\u53d1\u653e");
        return new IssueResult(batchNo, 1, 0);
    }

    public IssueResult claimAllAvailable(int userId) {
        if (userId <= 0) throw new RuntimeException("\u8bf7\u5148\u767b\u5f55\u666e\u901a\u7528\u6237\u8d26\u53f7\u3002");
        String batchNo = batchNo();
        int issued = 0;
        int skipped = 0;
        for (CouponTemplate template : couponDao.templates()) {
            if (!"ENABLED".equals(template.getStatus())) {
                skipped++;
                continue;
            }
            if (couponDao.receivedCount(userId, template.getCouponId()) >= template.getPerUserLimit()) {
                skipped++;
                continue;
            }
            couponDao.issueToUser(template, userId, batchNo);
            couponDao.logIssue(template.getCouponId(), batchNo, "USER", String.valueOf(userId), 1, 0, 0, "\u7528\u6237\u4e00\u952e\u9886\u53d6");
            issued++;
        }
        return new IssueResult(batchNo, issued, skipped);
    }

    public IssueResult issueBatch(String couponIds, String issueType, String targetValue, int adminId) {
        String batchNo = batchNo();
        int issued = 0;
        int skipped = 0;
        int touched = 0;
        for (CouponTemplate template : couponDao.platformTemplates()) {
            if (!matchTemplate(template, couponIds, issueType, targetValue)) continue;
            if (!"ENABLED".equals(template.getStatus())) continue;
            IssueResult each = issueTemplateWithBatch(template, issueType, targetValue, adminId, 0, batchNo, "\u6279\u91cf\u53d1\u653e");
            issued += each.getIssueCount();
            skipped += each.getSkipCount();
            touched++;
        }
        if (touched == 0) throw new RuntimeException("\u6ca1\u6709\u5339\u914d\u5230\u53ef\u53d1\u653e\u7684\u5e73\u53f0\u5238\u6a21\u677f\u3002");
        return new IssueResult(batchNo, issued, skipped);
    }

    private IssueResult issueTemplate(CouponTemplate template, String issueType, String targetValue, int adminId, int merchantId, String remark) {
        if (!"ENABLED".equals(template.getStatus())) throw new RuntimeException("\u4f18\u60e0\u5238\u672a\u542f\u7528\uff0c\u4e0d\u80fd\u53d1\u653e\u3002");
        String batchNo = batchNo();
        return issueTemplateWithBatch(template, issueType, targetValue, adminId, merchantId, batchNo, remark);
    }

    private IssueResult issueTemplateWithBatch(CouponTemplate template, String issueType, String targetValue, int adminId, int merchantId, String batchNo, String remark) {
        List<Integer> users = couponDao.targetUsers(issueType, targetValue, merchantId);
        int issued = 0;
        int skipped = 0;
        for (Integer userId : users) {
            if (couponDao.receivedCount(userId.intValue(), template.getCouponId()) >= template.getPerUserLimit()) {
                skipped++;
                continue;
            }
            couponDao.issueToUser(template, userId.intValue(), batchNo);
            issued++;
        }
        couponDao.logIssue(template.getCouponId(), batchNo, issueType, targetValue, issued, skipped, adminId, remark);
        return new IssueResult(batchNo, issued, skipped);
    }

    public void issueNewUserCoupons(int userId) {
        for (CouponTemplate template : couponDao.platformTemplates()) {
            if (template.isNewUserCoupon() && "ENABLED".equals(template.getStatus()) && couponDao.receivedCount(userId, template.getCouponId()) < template.getPerUserLimit()) {
                String batchNo = "NEW" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                couponDao.issueToUser(template, userId, batchNo);
                couponDao.logIssue(template.getCouponId(), batchNo, "NEW_USER", String.valueOf(userId), 1, 0, 0, "\u6ce8\u518c\u81ea\u52a8\u53d1\u653e");
            }
        }
    }

    public List<UserCoupon> userCoupons(int userId) {
        return couponDao.userCoupons(userId);
    }

    public List<UserCoupon> allUserCoupons() {
        return couponDao.allUserCoupons();
    }

    public List<UserCoupon> merchantUserCoupons(int merchantId) {
        return couponDao.merchantUserCoupons(merchantId);
    }

    public void voidUserCoupon(int userCouponId, int userId) {
        if (userCouponId <= 0 || userId <= 0) throw new RuntimeException("\u8bf7\u9009\u62e9\u8981\u4f5c\u5e9f\u7684\u7528\u6237\u5238\u3002");
        couponDao.voidUserCoupon(userCouponId, userId);
    }

    public void markUsed(int userCouponId, int userId, int orderId) {
        couponDao.markUsed(userCouponId, userId, orderId);
    }

    public List<CouponIssueLog> logs() {
        return couponDao.logs();
    }

    private boolean matchTemplate(CouponTemplate template, String couponIds, String issueType, String targetValue) {
        if (!empty(couponIds) && !"AUTO".equals(couponIds)) {
            String[] ids = couponIds.split(",");
            for (String id : ids) if (String.valueOf(template.getCouponId()).equals(id.trim())) return true;
            return false;
        }
        if ("VIP_LEVEL".equals(issueType)) {
            try {
                return template.getVipLevel() == Integer.parseInt(targetValue);
            } catch (Exception e) {
                return false;
            }
        }
        if ("NEW_USER".equals(issueType)) return template.isNewUserCoupon();
        return true;
    }

    private String batchNo() {
        return "CP" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    private boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }

    public static class IssueResult {
        private String batchNo;
        private int issueCount;
        private int skipCount;
        public IssueResult(String batchNo, int issueCount, int skipCount) {
            this.batchNo = batchNo;
            this.issueCount = issueCount;
            this.skipCount = skipCount;
        }
        public String getBatchNo() { return batchNo; }
        public int getIssueCount() { return issueCount; }
        public int getSkipCount() { return skipCount; }
    }
}
