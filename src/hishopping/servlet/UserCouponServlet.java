package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.User;
import hishopping.service.CouponService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/user/coupons")
public class UserCouponServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CouponService service = new CouponService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录普通用户账号。"));
            return;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("coupons", ServletUtil.userCoupons(service.userCoupons(user.getId())));
        result.put("templates", ServletUtil.couponTemplates(service.templates()));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录普通用户账号。"));
            return;
        }
        CouponService.IssueResult issueResult = null;
        if ("claimAll".equals(request.getParameter("action"))) {
            issueResult = service.claimAllAvailable(user.getId());
        } else if ("claim".equals(request.getParameter("action"))) {
            service.issueToSingleUser(ServletUtil.intParam(request, "couponId", 0), user.getId(), 0);
        } else {
            service.markUsed(ServletUtil.intParam(request, "userCouponId", 0), user.getId(), ServletUtil.intParam(request, "orderId", 0));
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        if (issueResult != null) {
            result.put("issueCount", issueResult.getIssueCount());
            result.put("skipCount", issueResult.getSkipCount());
        }
        result.put("coupons", ServletUtil.userCoupons(service.userCoupons(user.getId())));
        result.put("templates", ServletUtil.couponTemplates(service.templates()));
        JsonUtil.write(response, result);
    }
}
