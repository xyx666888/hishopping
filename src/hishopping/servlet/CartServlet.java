package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.User;
import hishopping.service.CartService;
import hishopping.service.UserService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CartService cartService = new CartService();
    private UserService userService = new UserService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录。"));
            return;
        }
        if (!normalUser(request, user)) {
            JsonUtil.write(response, ServletUtil.fail("账号当前不可操作购物车，请联系管理员。"));
            return;
        }
        Map<String, Object> result = ServletUtil.ok();
        result.put("cart", ServletUtil.cart(cartService.list(user.getId())));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录。"));
            return;
        }
        String action = request.getParameter("action");
        int productId = ServletUtil.intParam(request, "productId", 0);
        int cartItemId = ServletUtil.intParam(request, "cartItemId", 0);
        if ("add".equals(action)) {
            cartService.add(
                user.getId(),
                productId,
                request.getParameter("selectedColor"),
                request.getParameter("selectedSpec"),
                request.getParameter("skuId"),
                ServletUtil.intParam(request, "quantity", 1)
            );
        } else if ("update".equals(action)) {
            if (cartItemId > 0) {
                cartService.updateByCartItem(user.getId(), cartItemId, ServletUtil.intParam(request, "quantity", 1));
            } else {
                cartService.update(user.getId(), productId, ServletUtil.intParam(request, "quantity", 1));
            }
        } else if ("remove".equals(action)) {
            if (cartItemId > 0) {
                cartService.removeByCartItem(user.getId(), cartItemId);
            } else {
                cartService.remove(user.getId(), productId);
            }
        }
        Map<String, Object> result = ServletUtil.ok();
        result.put("cart", ServletUtil.cart(cartService.list(user.getId())));
        JsonUtil.write(response, result);
    }

    private boolean normalUser(HttpServletRequest request, User user) {
        User refreshed = userService.findById(user.getId());
        if (refreshed == null || !"正常".equals(refreshed.getStatus())) return false;
        request.getSession().setAttribute("user", refreshed);
        return true;
    }
}

