package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.dao.AnalyticsDao;
import hishopping.entity.Merchant;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class MerchantAnalyticsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AnalyticsDao analyticsDao = new AnalyticsDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录商家账号。"));
            return;
        }
        Map<String, Object> result = ServletUtil.ok();
        result.put("analytics", analyticsDao.merchantAnalytics(merchant.getMerchantId()));
        JsonUtil.write(response, result);
    }
}
