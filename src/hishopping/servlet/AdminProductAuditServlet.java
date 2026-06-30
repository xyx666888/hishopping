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
import hishopping.service.BusinessService;
import hishopping.service.AdminProductAuditService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/admin/productAudit")
public class AdminProductAuditServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AdminProductAuditService service = new AdminProductAuditService();
    private BusinessService businessService = new BusinessService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        writeRows(response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        int productId = ServletUtil.intParam(request, "productId", 0);
        if ("approve".equals(request.getParameter("action"))) service.approve(productId, admin.getId());
        else service.reject(productId, request.getParameter("opinion"), admin.getId());
        businessService.logAdmin(admin.getId(), "AUDIT_PRODUCT", "PRODUCT", productId, request.getParameter("action"));
        writeRows(response);
    }

    private void writeRows(HttpServletResponse response) throws IOException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("products", ServletUtil.products(service.auditRows()));
        JsonUtil.write(response, result);
    }
}
