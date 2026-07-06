package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Admin;
import hishopping.entity.Merchant;
import hishopping.entity.ProductReview;
import hishopping.entity.User;
import hishopping.service.BusinessService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class ReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BusinessService businessService = new BusinessService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Map<String, Object> result = ServletUtil.ok();
        Actor actor = actor(request);
        if ("myStats".equals(action)) {
            User user = ServletUtil.currentUser(request);
            if (user == null) {
                JsonUtil.write(response, ServletUtil.fail("请先登录普通用户账号。"));
                return;
            }
            result.put("stats", businessService.reviewStatsForUser(user.getId()));
            JsonUtil.write(response, result);
            return;
        }
        int reviewId = ServletUtil.intParam(request, "reviewId", 0);
        if ("replies".equals(action) && reviewId > 0) {
            result.put("replies", ServletUtil.reviewReplies(businessService.reviewReplies(reviewId)));
            JsonUtil.write(response, result);
            return;
        }
        int productId = ServletUtil.intParam(request, "productId", 0);
        String filter = request.getParameter("filter");
        int rating = ServletUtil.intParam(request, "rating", 0);
        List<ProductReview> reviews = businessService.productReviews(productId, actor.type, actor.id, filter, rating);
        result.put("stats", businessService.reviewStatsForProduct(productId));
        result.put("filter", filter == null || filter.length() == 0 ? "all" : filter);
        result.put("rating", rating);
        result.put("reviews", reviewMaps(reviews));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                User user = ServletUtil.currentUser(request);
                if (user == null) {
                    JsonUtil.write(response, ServletUtil.fail("请先登录普通用户账号。"));
                    return;
                }
                String content = request.getParameter("content");
                String mediaIds = request.getParameter("mediaIds");
                if ((content == null || content.trim().length() == 0) && (mediaIds == null || mediaIds.trim().length() == 0)) {
                    JsonUtil.write(response, ServletUtil.fail("请填写评论内容。"));
                    return;
                }
                int newReviewId = businessService.review(
                    ServletUtil.intParam(request, "orderId", 0),
                    ServletUtil.intParam(request, "productId", 0),
                    user.getId(),
                    ServletUtil.intParam(request, "rating", 5),
                    content == null ? "" : content.trim(),
                    "true".equalsIgnoreCase(request.getParameter("anonymous")),
                    mediaIds
                );
                Map<String, Object> ok = ServletUtil.ok();
                ok.put("reviewId", newReviewId);
                JsonUtil.write(response, ok);
                return;
            }
            Actor actor = actor(request);
            if (actor.id <= 0) {
                JsonUtil.write(response, ServletUtil.fail("请先登录后操作评论。"));
                return;
            }
            int reviewId = ServletUtil.intParam(request, "reviewId", 0);
            if ("like".equals(action)) {
                Map<String, Object> ok = ServletUtil.ok();
                ok.putAll(businessService.toggleReviewLike(reviewId, actor.type, actor.id));
                JsonUtil.write(response, ok);
                return;
            }
            if ("reply".equals(action)) {
                businessService.replyReview(reviewId, actor.type, actor.id, actor.name, actor.avatar, request.getParameter("content"));
                Map<String, Object> ok = ServletUtil.ok();
                ok.put("replies", ServletUtil.reviewReplies(businessService.reviewReplies(reviewId)));
                JsonUtil.write(response, ok);
                return;
            }
            JsonUtil.write(response, ServletUtil.fail("评论操作不正确。"));
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private List<Map<String, Object>> reviewMaps(List<ProductReview> reviews) {
        List<Map<String, Object>> rows = ServletUtil.productReviews(reviews);
        for (Map<String, Object> row : rows) {
            int reviewId = Integer.parseInt(String.valueOf(row.get("reviewId")));
            row.put("replies", ServletUtil.reviewReplies(businessService.reviewReplies(reviewId)));
        }
        return rows;
    }

    private Actor actor(HttpServletRequest request) {
        User user = ServletUtil.currentUser(request);
        if (user != null) return new Actor("USER", user.getId(), user.getUsername(), user.getAvatarUrl());
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant != null) return new Actor("MERCHANT", merchant.getMerchantId(), merchant.getShopName(), merchant.getAvatarUrl());
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin != null) return new Actor("ADMIN", admin.getId(), admin.getRealName() == null || admin.getRealName().length() == 0 ? admin.getAdminName() : admin.getRealName(), "");
        return new Actor("USER", 0, "", "");
    }

    private static class Actor {
        String type;
        int id;
        String name;
        String avatar;
        Actor(String type, int id, String name, String avatar) {
            this.type = type;
            this.id = id;
            this.name = name;
            this.avatar = avatar;
        }
    }
}
