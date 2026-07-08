package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import hishopping.entity.User;
import hishopping.service.BusinessService;
import hishopping.service.UserService;
import hishopping.util.CaptchaUtil;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();
    private BusinessService businessService = new BusinessService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Map<String, Object> result;
        try {
            if (isEmpty(request.getParameter("username"))) {
                JsonUtil.write(response, ServletUtil.fail("请输入用户名。"));
                return;
            }
            if (isEmpty(request.getParameter("email"))) {
                JsonUtil.write(response, ServletUtil.fail("请输入邮箱。"));
                return;
            }
            if (isEmpty(request.getParameter("phone"))) {
                JsonUtil.write(response, ServletUtil.fail("请输入手机号。"));
                return;
            }
            if (isEmpty(request.getParameter("password"))) {
                JsonUtil.write(response, ServletUtil.fail("请输入密码。"));
                return;
            }
            CaptchaUtil.CaptchaResult captcha = CaptchaUtil.verify(request, request.getParameter("captcha"));
            if (!captcha.isValid()) {
                JsonUtil.write(response, ServletUtil.fail(captcha.getMessage()));
                return;
            }
            User user = userService.register(request.getParameter("username"), request.getParameter("email"), request.getParameter("phone"), request.getParameter("password"));
            businessService.recordRegistrationGrowth(user.getId());
            setActiveUser(request);
            request.getSession().setAttribute("user", user);
            result = ServletUtil.ok();
            result.put("user", ServletUtil.user(user));
        } catch (RuntimeException e) {
            result = ServletUtil.fail(e.getMessage());
        }
        JsonUtil.write(response, result);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private void setActiveUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("admin");
        session.removeAttribute("merchant");
        session.setAttribute("authType", "user");
    }
}

