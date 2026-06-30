package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Merchant;
import hishopping.service.MerchantService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/merchant/register")
public class MerchantRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MerchantService merchantService = new MerchantService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            Merchant m = new Merchant();
            m.setMerchantName(request.getParameter("merchantName"));
            m.setPassword(request.getParameter("password"));
            m.setContactName(request.getParameter("contactName"));
            m.setContactPhone(request.getParameter("contactPhone"));
            m.setEmail(request.getParameter("email"));
            m.setShopName(request.getParameter("shopName"));
            m.setShopDesc(request.getParameter("shopDesc"));
            m.setBusinessCategory(request.getParameter("businessCategory"));
            m.setBusinessAddress(request.getParameter("businessAddress"));
            Merchant saved = merchantService.register(m);
            Map<String, Object> result = ServletUtil.ok();
            result.put("merchant", ServletUtil.merchant(saved));
            result.put("message", "商家注册申请已提交，独立ID：" + saved.getMerchantCode());
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }
}
