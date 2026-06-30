package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.User;
import hishopping.service.FavoriteService;
import hishopping.service.UserService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/favorites")
public class FavoriteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private FavoriteService service = new FavoriteService();
    private UserService userService = new UserService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u5148\u767b\u5f55\u666e\u901a\u7528\u6237\u8d26\u53f7\u3002"));
            return;
        }
        JsonUtil.write(response, payload(user.getId()));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u5148\u767b\u5f55\u666e\u901a\u7528\u6237\u8d26\u53f7\u3002"));
            return;
        }
        User activeUser = userService.findById(user.getId());
        if (activeUser == null || !"正常".equals(activeUser.getStatus())) {
            JsonUtil.write(response, ServletUtil.fail("\u8d26\u53f7\u5f53\u524d\u4e0d\u53ef\u64cd\u4f5c\u6536\u85cf\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u3002"));
            return;
        }
        request.getSession().setAttribute("user", activeUser);
        user = activeUser;
        try {
            String action = request.getParameter("action");
            int productId = ServletUtil.intParam(request, "productId", 0);
            Boolean favorited = null;
            if ("remove".equals(action)) {
                service.remove(user.getId(), productId);
                favorited = Boolean.FALSE;
            } else if ("add".equals(action)) {
                service.add(user.getId(), productId);
                favorited = Boolean.TRUE;
            } else {
                favorited = Boolean.valueOf(service.toggle(user.getId(), productId));
            }
            Map<String, Object> result = payload(user.getId());
            result.put("favorited", favorited);
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private Map<String, Object> payload(int userId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("favoriteIds", service.favoriteIds(userId));
        result.put("favorites", ServletUtil.favorites(service.favorites(userId)));
        return result;
    }
}
