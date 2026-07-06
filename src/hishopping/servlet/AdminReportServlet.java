package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Admin;
import hishopping.entity.Report;
import hishopping.service.ReportService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AdminReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportService service = new ReportService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        try {
            Map<String, Object> result = ServletUtil.ok();
            if ("detail".equals(request.getParameter("action"))) {
                result.put("report", ServletUtil.report(service.detail(ServletUtil.intParam(request, "reportId", 0))));
            } else {
                result.put("reports", ServletUtil.reports(service.adminReports(request.getParameter("status"), request.getParameter("keyword"))));
            }
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        try {
            String adminName = admin.getRealName() == null || admin.getRealName().length() == 0 ? admin.getAdminName() : admin.getRealName();
            Report report = service.handle(ServletUtil.intParam(request, "reportId", 0), upper(request.getParameter("status")), admin.getId(), adminName, request.getParameter("handleOpinion"), request.getParameter("handleResult"));
            Map<String, Object> result = ServletUtil.ok();
            result.put("report", ServletUtil.report(report));
            result.put("reports", ServletUtil.reports(service.adminReports(request.getParameter("filterStatus"), request.getParameter("keyword"))));
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
