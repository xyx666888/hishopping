package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Merchant;
import hishopping.dao.MessageDao;
import hishopping.dao.OrderDao;
import hishopping.service.BusinessService;
import hishopping.service.MerchantService;
import hishopping.service.OrderService;
import hishopping.service.AccountRestrictionService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/merchant/orders")
public class MerchantOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OrderService orderService = new OrderService();
    private BusinessService businessService = new BusinessService();
    private MessageDao messageDao = new MessageDao();
    private OrderDao orderDao = new OrderDao();
    private MerchantService merchantService = new MerchantService();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录商家账号。"));
            return;
        }
        writeRows(response, merchant);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录商家账号。"));
            return;
        }
        merchant = activeMerchant(request, merchant);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("商家账号当前不可处理订单，请联系管理员。"));
            return;
        }
        try {
            restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_manage_order");
            if ("ship".equals(request.getParameter("action"))) {
                restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_ship_order");
                orderService.ship(merchant.getMerchantId(), ServletUtil.intParam(request, "orderId", 0), request.getParameter("expressCompany"), request.getParameter("trackingNo"));
            } else if ("afterSale".equals(request.getParameter("action"))) {
                restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_handle_after_sale");
                int afterSaleId = ServletUtil.intParam(request, "afterSaleId", 0);
                businessService.handleAfterSaleByMerchant(afterSaleId, merchant.getMerchantId(), request.getParameter("handleAction"), request.getParameter("opinion"));
                notifyAfterSaleHandled(merchant, afterSaleId);
            }
            writeRows(response, merchant);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private void writeRows(HttpServletResponse response, Merchant merchant) throws IOException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("merchant", ServletUtil.merchant(merchant));
        result.put("orders", ServletUtil.orders(orderService.merchantOrders(merchant.getMerchantId())));
        result.put("afterSales", ServletUtil.afterSales(businessService.merchantAfterSales(merchant.getMerchantId())));
        JsonUtil.write(response, result);
    }

    private void notifyAfterSaleHandled(Merchant merchant, int afterSaleId) {
        Map<String, Object> snapshot = orderDao.afterSaleSnapshot(afterSaleId, "MERCHANT", merchant.getMerchantId());
        if (snapshot == null) return;
        int userId = ((Number) snapshot.get("userId")).intValue();
        int conversationId = messageDao.openConversation("MERCHANT", merchant.getMerchantId(), merchant.getShopName(), merchant.getAvatarUrl(), "USER", userId);
        String extra = "{\"afterSaleId\":" + afterSaleId + ",\"orderId\":" + snapshot.get("orderId") + ",\"orderNo\":\"" + jsonEscape(snapshot.get("orderNo")) + "\",\"productName\":\"" + jsonEscape(snapshot.get("productName")) + "\",\"amount\":" + snapshot.get("refundAmount") + ",\"status\":\"" + jsonEscape(snapshot.get("status")) + "\",\"reason\":\"" + jsonEscape(snapshot.get("reason")) + "\"}";
        messageDao.sendChat(conversationId, "MERCHANT", merchant.getMerchantId(), merchant.getShopName(), "USER", userId, "REFUND_CARD", "售后处理结果：" + snapshot.get("status"), "", "", 0, "REFUND", afterSaleId, extra);
    }

    private String jsonEscape(Object value) {
        return String.valueOf(value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private Merchant activeMerchant(HttpServletRequest request, Merchant merchant) {
        Merchant refreshed = merchantService.findById(merchant.getMerchantId());
        if (refreshed == null || !"APPROVED".equals(refreshed.getStatus())) return null;
        request.getSession().setAttribute("merchant", refreshed);
        return refreshed;
    }
}
