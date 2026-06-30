package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.service.ProductService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AdminProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductService productService = new ProductService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("products", ServletUtil.products(productService.adminProducts()));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先用管理员账号登录。"));
            return;
        }
        request.setCharacterEncoding("UTF-8");
        productService.changeStatus(ServletUtil.intParam(request, "productId", 0), request.getParameter("status"));
        Map<String, Object> result = ServletUtil.ok();
        result.put("products", ServletUtil.products(productService.adminProducts()));
        JsonUtil.write(response, result);
    }
}

