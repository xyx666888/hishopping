package hishopping.service;

import java.util.List;

import hishopping.dao.BusinessDao;
import hishopping.entity.AdminOperationLog;
import hishopping.entity.AfterSale;
import hishopping.entity.GrowthLog;
import hishopping.entity.ProductReview;
import hishopping.entity.ReviewReply;

public class BusinessService {
    private BusinessDao dao = new BusinessDao();

    public int applyAfterSale(int orderId, int userId, int productId, String type, String reason, double refundAmount) {
        return applyAfterSale(orderId, userId, productId, type, reason, "", "", refundAmount);
    }

    public int applyAfterSale(int orderId, int userId, int productId, String type, String reason, String description, String evidenceUrls, double refundAmount) {
        if (orderId <= 0 || userId <= 0 || productId <= 0) throw new RuntimeException("\u8ba2\u5355\u6216\u5546\u54c1\u4e0d\u6b63\u786e\u3002");
        if (reason == null || reason.trim().length() == 0) throw new RuntimeException("\u8bf7\u586b\u5199\u552e\u540e\u539f\u56e0\u3002");
        return dao.createAfterSale(orderId, userId, productId, type, reason, description, evidenceUrls, refundAmount);
    }

    public void handleAfterSaleByMerchant(int afterSaleId, int merchantId, String action, String opinion) {
        if (afterSaleId <= 0 || merchantId <= 0) throw new RuntimeException("\u552e\u540e\u7533\u8bf7\u4e0d\u6b63\u786e\u3002");
        dao.handleAfterSale(afterSaleId, merchantId, 0, status(action), opinion);
    }

    public void handleAfterSaleByAdmin(int afterSaleId, int adminId, String action, String opinion) {
        if (afterSaleId <= 0 || adminId <= 0) throw new RuntimeException("\u552e\u540e\u7533\u8bf7\u4e0d\u6b63\u786e\u3002");
        dao.handleAfterSale(afterSaleId, 0, adminId, status(action), opinion);
    }

    public void review(int orderId, int productId, int userId, int rating, String content) {
        review(orderId, productId, userId, rating, content, false, "");
    }

    public int review(int orderId, int productId, int userId, int rating, String content, boolean anonymous, String mediaIds) {
        if (rating < 1 || rating > 5) throw new RuntimeException("\u8bc4\u5206\u9700\u5728 1-5 \u5206\u4e4b\u95f4\u3002");
        return dao.createReview(orderId, productId, userId, rating, content, anonymous, mediaIds);
    }

    public List<AfterSale> userAfterSales(int userId) { return dao.afterSalesForUser(userId); }
    public List<AfterSale> merchantAfterSales(int merchantId) { return dao.afterSalesForMerchant(merchantId); }
    public List<AfterSale> allAfterSales() { return dao.allAfterSales(); }
    public List<ProductReview> productReviews(int productId) { return dao.reviewsForProduct(productId); }
    public List<ProductReview> productReviews(int productId, String actorType, int actorId) { return dao.reviewsForProduct(productId, actorType, actorId); }
    public List<ProductReview> productReviews(int productId, String actorType, int actorId, String filter, int rating) { return dao.reviewsForProduct(productId, actorType, actorId, filter, rating); }
    public List<ReviewReply> reviewReplies(int reviewId) { return dao.repliesForReview(reviewId); }
    public void likeReview(int reviewId, String actorType, int actorId) { dao.likeReview(reviewId, actorType, actorId); }
    public java.util.Map<String, Object> toggleReviewLike(int reviewId, String actorType, int actorId) { return dao.toggleReviewLike(reviewId, actorType, actorId); }
    public void replyReview(int reviewId, String actorType, int actorId, String actorName, String actorAvatar, String content) { dao.replyReview(reviewId, actorType, actorId, actorName, actorAvatar, content); }
    public java.util.Map<String, Object> reviewStatsForUser(int userId) { return dao.reviewStatsForUser(userId); }
    public java.util.Map<String, Object> reviewStatsForProduct(int productId) { return dao.reviewStatsForProduct(productId); }
    public int saveReviewMedia(String ownerType, int ownerId, int productId, String mediaType, String mediaUrl, String fileName, long fileSize) { return dao.saveReviewMedia(ownerType, ownerId, productId, mediaType, mediaUrl, fileName, fileSize); }
    public List<GrowthLog> growthLogs(int userId) { return dao.growthLogsForUser(userId); }
    public List<AdminOperationLog> adminLogsForUser(int userId) { return dao.adminLogsForUser(userId); }
    public List<AdminOperationLog> allAdminLogs() { return dao.allAdminLogs(); }
    public void logAdmin(int adminId, String operationType, String targetType, int targetId, String content) { dao.logAdminOperation(adminId, operationType, targetType, targetId, content); }
    public void recordRegistrationGrowth(int userId) { dao.logGrowthOnly(userId, 100, 0, "REGISTER", userId, "\u6ce8\u518c\u5956\u52b1"); }
    public int saveUpload(int merchantId, String fileName, String accessUrl, long fileSize) { return dao.saveUploadResource(merchantId, fileName, accessUrl, fileSize); }
    public void bindUpload(int merchantId, String accessUrl, int productId) { dao.bindUploadResource(merchantId, accessUrl, productId); }

    private String status(String action) {
        if ("approve".equals(action)) return "\u5df2\u901a\u8fc7";
        if ("reject".equals(action)) return "\u5df2\u62d2\u7edd";
        if ("complete".equals(action)) return "\u5df2\u5b8c\u6210";
        if ("\u5df2\u901a\u8fc7".equals(action) || "\u5df2\u62d2\u7edd".equals(action) || "\u5df2\u5b8c\u6210".equals(action)) return action;
        throw new RuntimeException("\u552e\u540e\u5904\u7406\u72b6\u6001\u4e0d\u6b63\u786e\u3002");
    }
}
