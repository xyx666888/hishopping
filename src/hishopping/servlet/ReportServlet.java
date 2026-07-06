package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Merchant;
import hishopping.entity.Report;
import hishopping.entity.User;
import hishopping.service.ReportService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class ReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportService service = new ReportService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Actor actor = actor(request);
        if (actor.id <= 0) {
            JsonUtil.write(response, ServletUtil.fail("请先登录后查看举报。"));
            return;
        }
        try {
            Map<String, Object> result = ServletUtil.ok();
            result.put("reports", ServletUtil.reports(service.myReports(actor.role, actor.id)));
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Actor actor = actor(request);
        if (actor.id <= 0) {
            JsonUtil.write(response, ServletUtil.fail("请先登录后提交举报。"));
            return;
        }
        try {
            Report report = new Report();
            report.setReporterRole(actor.role);
            report.setReporterId(actor.id);
            report.setReporterName(actor.name);
            report.setTargetRole(upper(request.getParameter("targetRole")));
            report.setTargetId(ServletUtil.intParam(request, "targetId", 0));
            report.setReportType(request.getParameter("reportType"));
            report.setReason(request.getParameter("reason"));
            report.setDescription(request.getParameter("description"));
            report.setEvidenceUrls(request.getParameter("evidenceUrls"));
            report.setOrderId(ServletUtil.intParam(request, "orderId", 0));
            report.setProductId(ServletUtil.intParam(request, "productId", 0));
            report.setMerchantId(ServletUtil.intParam(request, "merchantId", 0));
            report.setReviewId(ServletUtil.intParam(request, "reviewId", 0));
            int reportId = service.create(report);
            Map<String, Object> result = ServletUtil.ok();
            result.put("reportId", reportId);
            result.put("reports", ServletUtil.reports(service.myReports(actor.role, actor.id)));
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private Actor actor(HttpServletRequest request) {
        User user = ServletUtil.currentUser(request);
        if (user != null) return new Actor("USER", user.getId(), user.getUsername());
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant != null) return new Actor("MERCHANT", merchant.getMerchantId(), merchant.getShopName());
        return new Actor("", 0, "");
    }

    private String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static class Actor {
        String role;
        int id;
        String name;
        Actor(String role, int id, String name) {
            this.role = role;
            this.id = id;
            this.name = name;
        }
    }
}
