package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.service.ProductService;
import hishopping.entity.User;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductService productService = new ProductService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        User user = ServletUtil.currentUser(request);
        String feed = request.getParameter("feed");
        result.put("success", true);
        result.put("categories", ServletUtil.categories(productService.categories()));
        result.put("products", ServletUtil.products(productService.products(feed, user == null ? 0 : user.getId())));
        result.put("feed", feed == null || feed.trim().length() == 0 ? "recommend" : feed);
        JsonUtil.write(response, result);
    }
}

