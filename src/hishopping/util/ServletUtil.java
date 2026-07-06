package hishopping.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import hishopping.entity.Address;
import hishopping.entity.Admin;
import hishopping.entity.AdminOperationLog;
import hishopping.entity.CartItem;
import hishopping.entity.Category;
import hishopping.entity.CouponIssueLog;
import hishopping.entity.CouponTemplate;
import hishopping.entity.AfterSale;
import hishopping.entity.Favorite;
import hishopping.entity.HallBanner;
import hishopping.entity.GrowthLog;
import hishopping.entity.Merchant;
import hishopping.entity.Order;
import hishopping.entity.OrderItem;
import hishopping.entity.Product;
import hishopping.entity.ProductMedia;
import hishopping.entity.ProductReview;
import hishopping.entity.ProductReviewMedia;
import hishopping.entity.ReviewReply;
import hishopping.entity.Shipment;
import hishopping.entity.User;
import hishopping.entity.UserCoupon;

public class ServletUtil {
    private ServletUtil() {
    }

    public static int intParam(HttpServletRequest request, String name, int defaultValue) {
        try {
            return Integer.parseInt(request.getParameter(name));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static double doubleParam(HttpServletRequest request, String name, double defaultValue) {
        try {
            return Double.parseDouble(request.getParameter(name));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    public static Admin currentAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Admin) session.getAttribute("admin");
    }

    public static Merchant currentMerchant(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Merchant) session.getAttribute("merchant");
    }

    public static Map<String, Object> ok() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("success", true);
        return map;
    }

    public static Map<String, Object> fail(String message) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("success", false);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> user(User user) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (user == null) {
            return map;
        }
        map.put("id", user.getId());
        map.put("accountId", user.getAccountId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole());
        map.put("points", user.getPoints());
        map.put("vipLevel", user.getVipLevel());
        map.put("growthValue", user.getGrowthValue());
        map.put("status", user.getStatus());
        map.put("avatarUrl", user.getAvatarUrl());
        map.put("createTime", user.getCreateTime());
        return map;
    }

    public static Map<String, Object> admin(Admin admin) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (admin == null) {
            return map;
        }
        map.put("id", admin.getId());
        map.put("adminName", admin.getAdminName());
        map.put("realName", admin.getRealName());
        return map;
    }

    public static List<Map<String, Object>> categories(List<Category> categories) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Category c : categories) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("iconText", c.getIconText());
            map.put("description", c.getDescription());
            list.add(map);
        }
        return list;
    }

    public static Map<String, Object> product(Product p) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (p == null) {
            return map;
        }
        map.put("id", p.getId());
        map.put("categoryId", p.getCategoryId());
        map.put("categoryName", p.getCategoryName());
        map.put("name", p.getName());
        map.put("shortDesc", p.getShortDesc());
        map.put("detailDesc", p.getDetailDesc());
        map.put("price", p.getPrice());
        map.put("oldPrice", p.getOldPrice());
        map.put("rating", p.getRating());
        map.put("sales", p.getSales());
        map.put("stock", p.getStock());
        map.put("tag", p.getTag());
        map.put("imageUrl", p.getImageUrl());
        map.put("mediaList", productMedia(p.getMediaList()));
        map.put("mediaCount", p.getMediaList() == null ? 0 : p.getMediaList().size());
        map.put("gradient", p.getGradient());
        map.put("iconText", p.getIconText());
        map.put("colorOptions", split(p.getColorOptions()));
        map.put("specOptions", split(p.getSpecOptions()));
        map.put("skuAttrs", SkuUtil.attrMaps(p));
        map.put("skuOptions", SkuUtil.skuMaps(p));
        map.put("productAttrs", ProductAttrUtil.attrMaps(p));
        map.put("productAttrsJson", ProductAttrUtil.normalizeJson(p.getProductAttrs()));
        map.put("status", p.getStatus());
        map.put("merchantId", p.getMerchantId());
        map.put("merchantCode", p.getMerchantCode());
        map.put("shopName", p.getShopName());
        map.put("saleStatus", p.getSaleStatus());
        map.put("auditStatus", p.getAuditStatus());
        map.put("auditOpinion", p.getAuditOpinion());
        map.put("submitTime", p.getSubmitTime());
        map.put("reviewTime", p.getReviewTime());
        map.put("reviewAdminId", p.getReviewAdminId());
        map.put("reviewCount", p.getReviewCount());
        map.put("averageRating", p.getAverageRating());
        map.put("favoriteCount", p.getFavoriteCount());
        map.put("reviewLikeCount", p.getReviewLikeCount());
        return map;
    }

    public static List<Map<String, Object>> productMedia(List<ProductMedia> mediaList) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (mediaList == null) return list;
        for (ProductMedia media : mediaList) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", media.getId());
            map.put("productId", media.getProductId());
            map.put("mediaType", media.getMediaType());
            map.put("mediaUrl", media.getMediaUrl());
            map.put("sortNo", media.getSortNo());
            map.put("coverFlag", media.isCoverFlag());
            map.put("createTime", media.getCreateTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> products(List<Product> products) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Product p : products) {
            list.add(product(p));
        }
        return list;
    }

    public static List<Map<String, Object>> favorites(List<Favorite> favorites) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Favorite favorite : favorites) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", favorite.getId());
            map.put("userId", favorite.getUserId());
            map.put("productId", favorite.getProductId());
            map.put("createTime", favorite.getCreateTime());
            map.put("product", product(favorite.getProduct()));
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> hallBanners(List<HallBanner> banners) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (HallBanner b : banners) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", b.getId());
            map.put("mediaType", b.getMediaType());
            map.put("mediaUrl", b.getMediaUrl());
            map.put("title", b.getTitle());
            map.put("subtitle", b.getSubtitle());
            map.put("enabled", b.isEnabled());
            map.put("sortNo", b.getSortNo());
            map.put("linkEnabled", b.isLinkEnabled());
            map.put("linkType", b.getLinkType());
            map.put("linkTarget", b.getLinkTarget());
            map.put("productId", b.getProductId());
            map.put("overlayEnabled", b.isOverlayEnabled());
            map.put("textPosition", b.getTextPosition());
            map.put("titleColor", b.getTitleColor());
            map.put("subtitleColor", b.getSubtitleColor());
            map.put("videoMutedDefault", b.isVideoMutedDefault());
            map.put("videoDisableSeek", b.isVideoDisableSeek());
            map.put("videoDisablePause", b.isVideoDisablePause());
            map.put("createTime", b.getCreateTime());
            map.put("updateTime", b.getUpdateTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> cart(List<CartItem> items) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (CartItem item : items) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", item.getId());
            map.put("quantity", item.getQuantity());
            map.put("selectedColor", item.getSelectedColor());
            map.put("selectedSpec", item.getSelectedSpec());
            map.put("skuId", item.getSkuId());
            map.put("skuText", item.getSkuText());
            map.put("skuPrice", item.getSkuPrice());
            map.put("subtotal", item.getSkuPrice() * item.getQuantity());
            map.put("product", product(item.getProduct()));
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> users(List<User> users) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (User user : users) {
            list.add(user(user));
        }
        return list;
    }

    public static List<Map<String, Object>> orders(List<Order> orders) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Order order : orders) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("batchNo", order.getBatchNo());
            map.put("userId", order.getUserId());
            map.put("merchantId", order.getMerchantId());
            map.put("shopName", order.getShopName());
            map.put("goodsAmount", order.getGoodsAmount());
            map.put("totalAmount", order.getTotalAmount());
            map.put("discountAmount", order.getDiscountAmount());
            map.put("status", order.getStatus());
            map.put("createTime", order.getCreateTime());
            map.put("receiverName", order.getReceiverName());
            map.put("receiverPhone", order.getReceiverPhone());
            map.put("receiverAddress", order.getReceiverAddress());
            map.put("shipments", shipments(order.getShipments()));
            map.put("afterSales", afterSales(order.getAfterSales()));
            List<Map<String, Object>> itemMaps = new ArrayList<Map<String, Object>>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new LinkedHashMap<String, Object>();
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                itemMap.put("selectedColor", item.getSelectedColor());
                itemMap.put("selectedSpec", item.getSelectedSpec());
                itemMap.put("skuId", item.getSkuId());
                itemMap.put("skuText", item.getSkuText());
                itemMap.put("snapshotName", item.getSnapshotName());
                itemMap.put("snapshotImage", item.getSnapshotImage());
                itemMap.put("subtotal", item.getSubtotal());
                itemMap.put("reviewed", item.isReviewed());
                itemMap.put("afterSaleStatus", item.getAfterSaleStatus());
                itemMap.put("product", product(item.getProduct()));
                itemMaps.add(itemMap);
            }
            map.put("items", itemMaps);
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> addresses(List<Address> addresses) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Address a : addresses) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", a.getId());
            map.put("userId", a.getUserId());
            map.put("receiverName", a.getReceiverName());
            map.put("phone", a.getPhone());
            map.put("province", a.getProvince());
            map.put("city", a.getCity());
            map.put("district", a.getDistrict());
            map.put("detail", a.getDetail());
            map.put("defaultAddress", a.isDefaultAddress());
            list.add(map);
        }
        return list;
    }

    public static Map<String, Object> merchant(Merchant m) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (m == null) return map;
        map.put("merchantId", m.getMerchantId());
        map.put("merchantCode", m.getMerchantCode());
        map.put("merchantName", m.getMerchantName());
        map.put("registerPasswordDemo", m.getRegisterPasswordDemo());
        map.put("contactName", m.getContactName());
        map.put("contactPhone", m.getContactPhone());
        map.put("email", m.getEmail());
        map.put("shopName", m.getShopName());
        map.put("shopDesc", m.getShopDesc());
        map.put("businessCategory", m.getBusinessCategory());
        map.put("businessAddress", m.getBusinessAddress());
        map.put("status", m.getStatus());
        map.put("avatarUrl", m.getAvatarUrl());
        map.put("rejectReason", m.getRejectReason());
        map.put("createTime", m.getCreateTime());
        map.put("reviewTime", m.getReviewTime());
        map.put("reviewAdminId", m.getReviewAdminId());
        return map;
    }

    public static List<Map<String, Object>> merchants(List<Merchant> merchants) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Merchant m : merchants) list.add(merchant(m));
        return list;
    }

    public static List<Map<String, Object>> couponTemplates(List<CouponTemplate> templates) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (CouponTemplate c : templates) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("couponId", c.getCouponId());
            map.put("couponName", c.getCouponName());
            map.put("couponType", c.getCouponType());
            map.put("amount", c.getAmount());
            map.put("discountRate", c.getDiscountRate());
            map.put("minAmount", c.getMinAmount());
            map.put("targetType", c.getTargetType());
            map.put("targetValue", c.getTargetValue());
            map.put("vipLevel", c.getVipLevel());
            map.put("totalQuantity", c.getTotalQuantity());
            map.put("perUserLimit", c.getPerUserLimit());
            map.put("validDays", c.getValidDays());
            map.put("newUserCoupon", c.isNewUserCoupon());
            map.put("vipCoupon", c.isVipCoupon());
            map.put("status", c.getStatus());
            map.put("startTime", c.getStartTime());
            map.put("endTime", c.getEndTime());
            map.put("couponOwnerType", c.getCouponOwnerType());
            map.put("merchantId", c.getMerchantId());
            map.put("shopName", c.getShopName());
            map.put("stackable", c.isStackable());
            map.put("homeTitle", c.getHomeTitle());
            map.put("homeSubtitle", c.getHomeSubtitle());
            map.put("useScope", c.getUseScope());
            map.put("description", c.getDescription());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> userCoupons(List<UserCoupon> coupons) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (UserCoupon c : coupons) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("userCouponId", c.getUserCouponId());
            map.put("couponId", c.getCouponId());
            map.put("userId", c.getUserId());
            map.put("couponName", c.getCouponName());
            map.put("couponType", c.getCouponType());
            map.put("amount", c.getAmount());
            map.put("discountRate", c.getDiscountRate());
            map.put("minAmount", c.getMinAmount());
            map.put("vipLevel", c.getVipLevel());
            map.put("status", c.getStatus());
            map.put("receiveTime", c.getReceiveTime());
            map.put("expireTime", c.getExpireTime());
            map.put("useTime", c.getUseTime());
            map.put("orderId", c.getOrderId());
            map.put("issueBatchNo", c.getIssueBatchNo());
            map.put("couponOwnerType", c.getCouponOwnerType());
            map.put("merchantId", c.getMerchantId());
            map.put("shopName", c.getShopName());
            map.put("stackable", c.isStackable());
            map.put("useScope", c.getUseScope());
            map.put("description", c.getDescription());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> couponLogs(List<CouponIssueLog> logs) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (CouponIssueLog log : logs) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("issueLogId", log.getIssueLogId());
            map.put("couponId", log.getCouponId());
            map.put("couponName", log.getCouponName());
            map.put("issueBatchNo", log.getIssueBatchNo());
            map.put("issueType", log.getIssueType());
            map.put("targetValue", log.getTargetValue());
            map.put("issueCount", log.getIssueCount());
            map.put("skipCount", log.getSkipCount());
            map.put("adminId", log.getAdminId());
            map.put("issueTime", log.getIssueTime());
            map.put("remark", log.getRemark());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> shipments(List<Shipment> shipments) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (shipments == null) return list;
        for (Shipment s : shipments) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("shipmentId", s.getShipmentId());
            map.put("orderId", s.getOrderId());
            map.put("merchantId", s.getMerchantId());
            map.put("expressCompany", s.getExpressCompany());
            map.put("trackingNo", s.getTrackingNo());
            map.put("shipTime", s.getShipTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> afterSales(List<AfterSale> rows) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (rows == null) return list;
        for (AfterSale a : rows) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("afterSaleId", a.getAfterSaleId());
            map.put("orderId", a.getOrderId());
            map.put("userId", a.getUserId());
            map.put("merchantId", a.getMerchantId());
            map.put("productId", a.getProductId());
            map.put("afterSaleType", a.getAfterSaleType());
            map.put("reason", a.getReason());
            map.put("refundAmount", a.getRefundAmount());
            map.put("status", a.getStatus());
            map.put("applyTime", a.getApplyTime());
            map.put("handleOpinion", a.getHandleOpinion());
            map.put("handleTime", a.getHandleTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> productReviews(List<ProductReview> reviews) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (reviews == null) return list;
        for (ProductReview r : reviews) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            boolean anonymous = r.isAnonymous();
            map.put("reviewId", r.getReviewId());
            map.put("id", r.getReviewId());
            map.put("orderId", r.getOrderId());
            map.put("orderItemId", r.getOrderItemId());
            map.put("productId", r.getProductId());
            map.put("userId", anonymous ? 0 : r.getUserId());
            map.put("anonymous", anonymous);
            map.put("username", anonymous ? "匿名用户" : r.getUsername());
            map.put("userAvatar", anonymous ? "" : r.getUserAvatar());
            map.put("rating", r.getRating());
            map.put("content", r.getContent());
            map.put("status", r.getStatus());
            map.put("likeCount", r.getLikeCount());
            map.put("replyCount", r.getReplyCount());
            map.put("mediaCount", r.getMediaCount());
            map.put("mediaList", productReviewMedia(r.getMediaList()));
            map.put("skuText", r.getSkuText());
            map.put("selectedColor", r.getSelectedColor());
            map.put("selectedSpec", r.getSelectedSpec());
            map.put("liked", r.isLiked());
            map.put("createTime", r.getCreateTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> productReviewMedia(List<ProductReviewMedia> mediaList) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (mediaList == null) return list;
        for (ProductReviewMedia media : mediaList) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("mediaId", media.getMediaId());
            map.put("id", media.getMediaId());
            map.put("reviewId", media.getReviewId());
            map.put("productId", media.getProductId());
            map.put("mediaType", media.getMediaType());
            map.put("mediaUrl", media.getMediaUrl());
            map.put("fileName", media.getFileName());
            map.put("fileSize", media.getFileSize());
            map.put("sortNo", media.getSortNo());
            map.put("status", media.getStatus());
            map.put("createTime", media.getCreateTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> reviewReplies(List<ReviewReply> replies) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (replies == null) return list;
        for (ReviewReply r : replies) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("replyId", r.getReplyId());
            map.put("reviewId", r.getReviewId());
            map.put("parentReplyId", r.getParentReplyId());
            map.put("userType", r.getUserType());
            map.put("userId", r.getUserId());
            map.put("userName", r.getUserName());
            map.put("userAvatar", r.getUserAvatar());
            map.put("content", r.getContent());
            map.put("status", r.getStatus());
            map.put("createTime", r.getCreateTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> growthLogs(List<GrowthLog> logs) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (logs == null) return list;
        for (GrowthLog g : logs) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("logId", g.getLogId());
            map.put("userId", g.getUserId());
            map.put("growthDelta", g.getGrowthDelta());
            map.put("pointsDelta", g.getPointsDelta());
            map.put("sourceType", g.getSourceType());
            map.put("sourceId", g.getSourceId());
            map.put("remark", g.getRemark());
            map.put("createTime", g.getCreateTime());
            list.add(map);
        }
        return list;
    }

    public static List<Map<String, Object>> adminLogs(List<AdminOperationLog> logs) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (logs == null) return list;
        for (AdminOperationLog log : logs) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("logId", log.getLogId());
            map.put("adminId", log.getAdminId());
            map.put("operationType", log.getOperationType());
            map.put("targetType", log.getTargetType());
            map.put("targetId", log.getTargetId());
            map.put("content", log.getContent());
            map.put("operationTime", log.getOperationTime());
            list.add(map);
        }
        return list;
    }

    private static List<String> split(String value) {
        List<String> list = new ArrayList<String>();
        if (value == null || value.trim().length() == 0) {
            return list;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            if (part.trim().length() > 0) {
                list.add(part.trim());
            }
        }
        return list;
    }
}

