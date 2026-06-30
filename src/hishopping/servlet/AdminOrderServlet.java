package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Admin;
import hishopping.service.BusinessService;
import hishopping.service.OrderService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AdminOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OrderService orderService = new OrderService();
    private BusinessService businessService = new BusinessService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("orders", ServletUtil.orders(orderService.all()));
        result.put("afterSales", ServletUtil.afterSales(businessService.allAfterSales()));
        result.put("adminLogs", ServletUtil.adminLogs(businessService.allAdminLogs()));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        Map<String, Object> result;
        try {
            if ("delete".equals(action)) {
                orderService.delete(ServletUtil.intParam(request, "orderId", 0));
                businessService.logAdmin(admin.getId(), "DELETE_ORDER", "ORDER", ServletUtil.intParam(request, "orderId", 0), "\u5220\u9664\u8ba2\u5355");
            } else if ("afterSale".equals(action)) {
                businessService.handleAfterSaleByAdmin(ServletUtil.intParam(request, "afterSaleId", 0), admin.getId(), request.getParameter("handleAction"), request.getParameter("opinion"));
            } else {
                orderService.updateStatus(ServletUtil.intParam(request, "orderId", 0), request.getParameter("status"));
                businessService.logAdmin(admin.getId(), "ORDER_STATUS", "ORDER", ServletUtil.intParam(request, "orderId", 0), "\u4fee\u6539\u8ba2\u5355\u72b6\u6001\u4e3a " + request.getParameter("status"));
            }
            result = ServletUtil.ok();
            result.put("orders", ServletUtil.orders(orderService.all()));
            result.put("afterSales", ServletUtil.afterSales(businessService.allAfterSales()));
            result.put("adminLogs", ServletUtil.adminLogs(businessService.allAdminLogs()));
        } catch (RuntimeException e) {
            result = ServletUtil.fail(e.getMessage());
        }
        JsonUtil.write(response, result);
    }
}

