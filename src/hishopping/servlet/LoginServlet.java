package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Admin;
import hishopping.entity.Merchant;
import hishopping.entity.User;
import hishopping.service.MerchantService;
import hishopping.service.UserService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonUtil.write(response, ServletUtil.fail("\u767b\u5f55\u63a5\u53e3\u6b63\u5e38\uff0c\u8bf7\u4f7f\u7528 POST \u63d0\u4ea4\u8d26\u53f7\u548c\u5bc6\u7801\u3002"));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String mode = request.getParameter("mode");
        Map<String, Object> result;
        try {
            UserService userService = new UserService();
            if (isEmpty(account)) {
                result = ServletUtil.fail("admin".equals(mode) ? "\u8bf7\u8f93\u5165\u7ba1\u7406\u5458\u8d26\u53f7\u3002" : "\u8bf7\u8f93\u5165\u7528\u6237ID\u3001\u90ae\u7bb1\u6216\u624b\u673a\u53f7\u3002");
            } else if (isEmpty(password)) {
                result = ServletUtil.fail("\u8bf7\u8f93\u5165\u5bc6\u7801\u3002");
            } else if ("merchant".equals(mode)) {
                MerchantService merchantService = new MerchantService();
                Merchant merchant = merchantService.login(account, password);
                request.getSession().setAttribute("merchant", merchant);
                result = ServletUtil.ok();
                result.put("type", "merchant");
                result.put("merchant", ServletUtil.merchant(merchant));
            } else if ("admin".equals(mode)) {
                Admin admin = userService.adminLogin(account, password);
                if (admin == null) {
                    result = ServletUtil.fail("\u7ba1\u7406\u5458\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef\u3002");
                } else {
                    request.getSession().setAttribute("admin", admin);
                    result = ServletUtil.ok();
                    result.put("type", "admin");
                    result.put("admin", ServletUtil.admin(admin));
                }
            } else {
                User user = userService.login(account, password);
                if (user == null) {
                    result = ServletUtil.fail("\u7528\u6237ID\u3001\u90ae\u7bb1\u3001\u624b\u673a\u53f7\u6216\u5bc6\u7801\u9519\u8bef\u3002");
                } else {
                    request.getSession().setAttribute("user", user);
                    result = ServletUtil.ok();
                    result.put("type", "user");
                    result.put("user", ServletUtil.user(user));
                }
            }
        } catch (Exception e) {
            result = ServletUtil.fail(cleanError(e));
        }
        JsonUtil.write(response, result);
    }

    private String cleanError(Exception e) {
        String message = e.getMessage();
        if (message == null && e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        if (message == null || message.trim().length() == 0) {
            return "\u767b\u5f55\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u670d\u52a1\u5668\u6216\u6570\u636e\u5e93\u8fde\u63a5\u3002";
        }
        return message;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}