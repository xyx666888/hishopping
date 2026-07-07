package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.User;
import hishopping.dao.MessageDao;
import hishopping.dao.OrderDao;
import hishopping.service.BusinessService;
import hishopping.service.OrderService;
import hishopping.service.UserService;
import hishopping.service.AccountRestrictionService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OrderService orderService = new OrderService();
    private UserService userService = new UserService();
    private BusinessService businessService = new BusinessService();
    private MessageDao messageDao = new MessageDao();
    private OrderDao orderDao = new OrderDao();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录。"));
            return;
        }
        Map<String, Object> result = ServletUtil.ok();
        result.put("orders", ServletUtil.orders(orderService.list(user.getId())));
        result.put("afterSales", ServletUtil.afterSales(businessService.userAfterSales(user.getId())));
        result.put("growthLogs", ServletUtil.growthLogs(businessService.growthLogs(user.getId())));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录。"));
            return;
        }
        User activeUser = userService.findById(user.getId());
        if (activeUser == null || !"正常".equals(activeUser.getStatus())) {
            JsonUtil.write(response, ServletUtil.fail("账号当前不可下单或操作订单，请联系管理员。"));
            return;
        }
        request.getSession().setAttribute("user", activeUser);
        user = activeUser;
        Map<String, Object> result;
        try {
            result = ServletUtil.ok();
            if ("pay".equals(request.getParameter("action"))) {
                restrictionService.require("USER", user.getId(), "can_pay");
                orderService.pay(user.getId(), ServletUtil.intParam(request, "orderId", 0));
            } else if ("confirm".equals(request.getParameter("action"))) {
                orderService.confirm(user.getId(), ServletUtil.intParam(request, "orderId", 0));
            } else if ("cancel".equals(request.getParameter("action"))) {
                orderService.cancel(user.getId(), ServletUtil.intParam(request, "orderId", 0));
            } else if ("afterSale".equals(request.getParameter("action"))) {
                int afterSaleId = businessService.applyAfterSale(
                    ServletUtil.intParam(request, "orderId", 0),
                    user.getId(),
                    ServletUtil.intParam(request, "productId", 0),
                    request.getParameter("afterSaleType"),
                    request.getParameter("reason"),
                    request.getParameter("description"),
                    request.getParameter("evidenceUrls"),
                    ServletUtil.doubleParam(request, "refundAmount", 0)
                );
                Map<String, Object> snapshot = orderDao.afterSaleSnapshot(afterSaleId, "USER", user.getId());
                if (snapshot != null && ((Number) snapshot.get("merchantId")).intValue() > 0) {
                    int merchantId = ((Number) snapshot.get("merchantId")).intValue();
                    int conversationId = messageDao.openConversation("USER", user.getId(), user.getUsername(), user.getAvatarUrl(), "MERCHANT", merchantId);
                    String extra = "{\"afterSaleId\":" + afterSaleId + ",\"orderId\":" + snapshot.get("orderId") + ",\"orderNo\":\"" + jsonEscape(snapshot.get("orderNo")) + "\",\"productName\":\"" + jsonEscape(snapshot.get("productName")) + "\",\"amount\":" + snapshot.get("refundAmount") + ",\"status\":\"" + jsonEscape(snapshot.get("status")) + "\",\"reason\":\"" + jsonEscape(snapshot.get("reason")) + "\"}";
                    messageDao.sendChat(conversationId, "USER", user.getId(), user.getUsername(), "MERCHANT", merchantId, "REFUND_CARD", "退款申请：" + snapshot.get("orderNo"), "", "", 0, "REFUND", afterSaleId, extra);
                }
                result.put("afterSaleId", afterSaleId);
            } else if ("review".equals(request.getParameter("action"))) {
                restrictionService.require("USER", user.getId(), "can_review");
                businessService.review(
                    ServletUtil.intParam(request, "orderId", 0),
                    ServletUtil.intParam(request, "productId", 0),
                    user.getId(),
                    ServletUtil.intParam(request, "rating", 5),
                    request.getParameter("content")
                );
            } else {
                restrictionService.require("USER", user.getId(), "can_order");
                int platformCouponId = ServletUtil.intParam(request, "selectedPlatformCouponId", ServletUtil.intParam(request, "userCouponId", 0));
                int stackableCouponId = ServletUtil.intParam(request, "selectedStackableCouponId", 0);
                int merchantCouponId = ServletUtil.intParam(request, "selectedMerchantCouponId", 0);
                int addressId = ServletUtil.intParam(request, "addressId", 0);
                String cartItemIds = request.getParameter("cartItemIds");
                if (cartItemIds == null) {
                    result.put("order", orderService.create(user.getId(), platformCouponId, stackableCouponId, merchantCouponId, addressId).getOrderNo());
                } else {
                    result.put("order", orderService.create(user.getId(), platformCouponId, stackableCouponId, merchantCouponId, addressId, intArray(cartItemIds)).getOrderNo());
                }
            }
            User refreshedUser = userService.findById(user.getId());
            if (refreshedUser != null) {
                request.getSession().setAttribute("user", refreshedUser);
                result.put("user", ServletUtil.user(refreshedUser));
            }
            result.put("orders", ServletUtil.orders(orderService.list(user.getId())));
            result.put("afterSales", ServletUtil.afterSales(businessService.userAfterSales(user.getId())));
            result.put("growthLogs", ServletUtil.growthLogs(businessService.growthLogs(user.getId())));
        } catch (RuntimeException e) {
            result = ServletUtil.fail(e.getMessage());
        }
        JsonUtil.write(response, result);
    }

    private String jsonEscape(Object value) {
        return String.valueOf(value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private int[] intArray(String value) {
        if (value == null || value.trim().length() == 0) {
            return new int[0];
        }
        String[] parts = value.split(",");
        int[] ids = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                ids[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                ids[i] = 0;
            }
        }
        return ids;
    }
}

