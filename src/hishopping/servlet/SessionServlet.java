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

@WebServlet("/session")
public class SessionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();
    private MerchantService merchantService = new MerchantService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> result;
        try {
            User user = ServletUtil.currentUser(request);
            Admin admin = ServletUtil.currentAdmin(request);
            Merchant merchant = ServletUtil.currentMerchant(request);
            if (user != null) {
                User refreshedUser = userService.findById(user.getId());
                if (refreshedUser != null) {
                    request.getSession().setAttribute("user", refreshedUser);
                    result = ServletUtil.ok();
                    result.put("type", "user");
                    result.put("user", ServletUtil.user(refreshedUser));
                    JsonUtil.write(response, result);
                    return;
                }
            }
            if (admin != null) {
                result = ServletUtil.ok();
                result.put("type", "admin");
                result.put("admin", ServletUtil.admin(admin));
                JsonUtil.write(response, result);
                return;
            }
            if (merchant != null) {
                Merchant refreshedMerchant = merchantService.findById(merchant.getMerchantId());
                if (refreshedMerchant != null && "APPROVED".equals(refreshedMerchant.getStatus())) {
                    request.getSession().setAttribute("merchant", refreshedMerchant);
                    result = ServletUtil.ok();
                    result.put("type", "merchant");
                    result.put("merchant", ServletUtil.merchant(refreshedMerchant));
                    JsonUtil.write(response, result);
                    return;
                }
            }
            JsonUtil.write(response, ServletUtil.fail("\u6682\u65e0\u767b\u5f55\u4f1a\u8bdd\u3002"));
        } catch (Exception e) {
            result = ServletUtil.fail("\u4f1a\u8bdd\u68c0\u67e5\u5931\u8d25\uff1a" + cleanError(e));
            JsonUtil.write(response, result);
        }
    }

    private String cleanError(Exception e) {
        String message = e.getMessage();
        if (message == null && e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        if (message == null || message.trim().length() == 0) {
            return "\u8bf7\u68c0\u67e5\u670d\u52a1\u5668\u6216\u6570\u636e\u5e93\u8fde\u63a5\u3002";
        }
        return message;
    }
}