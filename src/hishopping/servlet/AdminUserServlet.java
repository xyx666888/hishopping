package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.service.UserService;
import hishopping.service.OrderService;
import hishopping.service.AddressService;
import hishopping.service.CouponService;
import hishopping.service.CouponService.IssueResult;
import hishopping.service.BusinessService;
import hishopping.service.AccountRestrictionService;
import hishopping.entity.Admin;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AdminUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();
    private OrderService orderService = new OrderService();
    private AddressService addressService = new AddressService();
    private CouponService couponService = new CouponService();
    private BusinessService businessService = new BusinessService();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        JsonUtil.write(response, adminUserCenter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        request.setCharacterEncoding("UTF-8");
        Admin admin = ServletUtil.currentAdmin(request);
        Map<String, Object> result;
        try {
            String action = request.getParameter("action");
            if ("orderStatus".equals(action)) {
                orderService.updateStatus(ServletUtil.intParam(request, "orderId", 0), request.getParameter("status"));
                businessService.logAdmin(ServletUtil.currentAdmin(request).getId(), "ORDER_STATUS", "ORDER", ServletUtil.intParam(request, "orderId", 0), "\u4fee\u6539\u8ba2\u5355\u72b6\u6001\u4e3a " + request.getParameter("status"));
            } else if ("deleteOrder".equals(action)) {
                orderService.delete(ServletUtil.intParam(request, "orderId", 0));
                businessService.logAdmin(ServletUtil.currentAdmin(request).getId(), "DELETE_ORDER", "ORDER", ServletUtil.intParam(request, "orderId", 0), "\u5220\u9664\u8ba2\u5355");
            } else if ("addressDefault".equals(action)) {
                addressService.setDefault(ServletUtil.intParam(request, "userId", 0), ServletUtil.intParam(request, "addressId", 0));
            } else if ("addressDelete".equals(action)) {
                addressService.delete(ServletUtil.intParam(request, "userId", 0), ServletUtil.intParam(request, "addressId", 0));
            } else if ("issueCoupon".equals(action)) {
                IssueResult issueResult = couponService.issueToSingleUser(
                    ServletUtil.intParam(request, "couponId", 0),
                    ServletUtil.intParam(request, "userId", 0),
                    ServletUtil.currentAdmin(request).getId()
                );
                result = adminUserCenter();
                result.put("issueCount", issueResult.getIssueCount());
                result.put("skipCount", issueResult.getSkipCount());
                businessService.logAdmin(ServletUtil.currentAdmin(request).getId(), "ISSUE_COUPON", "USER", ServletUtil.intParam(request, "userId", 0), "\u53d1\u653e\u4f18\u60e0\u5238");
                JsonUtil.write(response, result);
                return;
            } else if ("voidCoupon".equals(action)) {
                couponService.voidUserCoupon(ServletUtil.intParam(request, "userCouponId", 0), ServletUtil.intParam(request, "userId", 0));
            } else if ("deleteUser".equals(action)) {
                int userId = ServletUtil.intParam(request, "userId", 0);
                userService.deleteUser(userId);
                businessService.logAdmin(ServletUtil.currentAdmin(request).getId(), "DELETE_USER", "USER", userId, "\u5220\u9664\u7528\u6237\u8d26\u53f7");
            } else if ("restoreUser".equals(action)) {
                int userId = ServletUtil.intParam(request, "userId", 0);
                userService.restoreUser(userId);
                businessService.logAdmin(ServletUtil.currentAdmin(request).getId(), "RESTORE_USER", "USER", userId, "\u6062\u590d\u7528\u6237\u8d26\u53f7");
            } else if ("restrict".equals(action)) {
                int userId = ServletUtil.intParam(request, "userId", 0);
                int rawDays = ServletUtil.intParam(request, "durationDays", 0);
                Integer days = rawDays > 0 ? Integer.valueOf(rawDays) : null;
                String adminName = admin.getRealName() == null || admin.getRealName().length() == 0 ? admin.getAdminName() : admin.getRealName();
                restrictionService.restrict("USER", userId, request.getParameter("permissionKey"), request.getParameter("reason"), "ADMIN", 0, days, admin.getId(), adminName);
                businessService.logAdmin(admin.getId(), "RESTRICT_USER", "USER", userId, "限制权限 " + request.getParameter("permissionKey"));
            } else if ("cancelRestriction".equals(action)) {
                int userId = ServletUtil.intParam(request, "userId", 0);
                restrictionService.cancel(ServletUtil.intParam(request, "restrictionId", 0));
                businessService.logAdmin(admin.getId(), "CANCEL_USER_RESTRICTION", "USER", userId, "解除权限限制");
            } else {
                userService.updateUser(
                    ServletUtil.intParam(request, "userId", 0),
                    request.getParameter("username"),
                    request.getParameter("email"),
                    request.getParameter("phone"),
                    ServletUtil.intParam(request, "growthValue", 0),
                    ServletUtil.intParam(request, "points", 0),
                    request.getParameter("status"),
                    request.getParameter("password")
                );
                businessService.logAdmin(ServletUtil.currentAdmin(request).getId(), "UPDATE_USER", "USER", ServletUtil.intParam(request, "userId", 0), "\u4fee\u6539\u7528\u6237\u4fe1\u606f");
            }
            result = adminUserCenter();
        } catch (RuntimeException e) {
            result = ServletUtil.fail(e.getMessage());
        }
        JsonUtil.write(response, result);
    }

    private Map<String, Object> adminUserCenter() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("users", ServletUtil.adminUsers(userService.users()));
        result.put("orders", ServletUtil.orders(orderService.all()));
        result.put("addresses", ServletUtil.addresses(addressService.all()));
        result.put("userCoupons", ServletUtil.userCoupons(couponService.allUserCoupons()));
        result.put("couponTemplates", ServletUtil.couponTemplates(couponService.templates()));
        result.put("adminLogs", ServletUtil.adminLogs(businessService.allAdminLogs()));
        return result;
    }
}

