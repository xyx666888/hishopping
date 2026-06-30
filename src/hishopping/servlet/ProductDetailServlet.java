package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.dao.MessageDao;
import hishopping.entity.Product;
import hishopping.entity.User;
import hishopping.service.BusinessService;
import hishopping.service.ProductService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class ProductDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductService productService = new ProductService();
    private BusinessService businessService = new BusinessService();
    private MessageDao messageDao = new MessageDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Product product = productService.detail(ServletUtil.intParam(request, "id", 1));
        Map<String, Object> result = product == null ? ServletUtil.fail("商品不存在。") : ServletUtil.ok();
        result.put("product", ServletUtil.product(product));
        if (product != null) {
            User user = ServletUtil.currentUser(request);
            if (user != null) messageDao.logProductView(user.getId(), product.getId(), product.getMerchantId());
            result.put("reviews", ServletUtil.productReviews(businessService.productReviews(product.getId(), "USER", user == null ? 0 : user.getId())));
        }
        JsonUtil.write(response, result);
    }
}

