package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Admin;
import hishopping.entity.CouponTemplate;
import hishopping.service.BusinessService;
import hishopping.service.CouponService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/admin/coupons")
public class AdminCouponServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CouponService service = new CouponService();
    private BusinessService businessService = new BusinessService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        writeRows(response, null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        String action = request.getParameter("action");
        CouponService.IssueResult issueResult = null;
        if ("issue".equals(action)) {
            String couponIds = request.getParameter("couponIds");
            if (couponIds != null && couponIds.trim().length() > 0) {
                issueResult = service.issueBatch(couponIds, request.getParameter("issueType"), request.getParameter("targetValue"), admin.getId());
            } else {
                issueResult = service.issue(ServletUtil.intParam(request, "couponId", 0), request.getParameter("issueType"), request.getParameter("targetValue"), admin.getId());
            }
        } else if ("status".equals(action)) {
            service.updateTemplateStatus(ServletUtil.intParam(request, "couponId", 0), request.getParameter("status"));
        } else if ("delete".equals(action)) {
            service.deleteTemplate(ServletUtil.intParam(request, "couponId", 0));
        } else {
            CouponTemplate c = templateFromRequest(request);
            service.saveTemplate(c);
        }
        businessService.logAdmin(admin.getId(), "COUPON", "COUPON", ServletUtil.intParam(request, "couponId", 0), action == null ? "save" : action);
        writeRows(response, issueResult);
    }

    private CouponTemplate templateFromRequest(HttpServletRequest request) {
        CouponTemplate c = new CouponTemplate();
        c.setCouponId(ServletUtil.intParam(request, "couponId", 0));
        c.setCouponName(request.getParameter("couponName"));
        c.setCouponType(request.getParameter("couponType"));
        c.setAmount(ServletUtil.doubleParam(request, "amount", 0));
        c.setDiscountRate(ServletUtil.doubleParam(request, "discountRate", 1));
        c.setMinAmount(ServletUtil.doubleParam(request, "minAmount", 0));
        c.setTargetType(request.getParameter("targetType"));
        c.setTargetValue(request.getParameter("targetValue"));
        c.setVipLevel(ServletUtil.intParam(request, "vipLevel", 0));
        c.setTotalQuantity(ServletUtil.intParam(request, "totalQuantity", 9999));
        c.setPerUserLimit(ServletUtil.intParam(request, "perUserLimit", 1));
        c.setValidDays(ServletUtil.intParam(request, "validDays", 30));
        c.setNewUserCoupon("1".equals(request.getParameter("newUserCoupon")));
        c.setVipCoupon("1".equals(request.getParameter("vipCoupon")));
        c.setStatus(request.getParameter("status") == null ? "ENABLED" : request.getParameter("status"));
        c.setCouponOwnerType("PLATFORM");
        c.setStackable("1".equals(request.getParameter("stackable")));
        c.setHomeTitle(request.getParameter("homeTitle"));
        c.setHomeSubtitle(request.getParameter("homeSubtitle"));
        c.setUseScope(request.getParameter("useScope"));
        c.setDescription(request.getParameter("description"));
        return c;
    }

    private void writeRows(HttpServletResponse response, CouponService.IssueResult issueResult) throws IOException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("templates", ServletUtil.couponTemplates(service.templates()));
        result.put("logs", ServletUtil.couponLogs(service.logs()));
        if (issueResult != null) {
            result.put("batchNo", issueResult.getBatchNo());
            result.put("issueCount", issueResult.getIssueCount());
            result.put("skipCount", issueResult.getSkipCount());
        }
        JsonUtil.write(response, result);
    }
}
