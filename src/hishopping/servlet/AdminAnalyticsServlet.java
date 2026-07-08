package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.dao.AnalyticsDao;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AdminAnalyticsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AnalyticsDao analyticsDao = new AnalyticsDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录管理员账号。"));
            return;
        }
        Map<String, Object> result;
        try {
            result = ServletUtil.ok();
            result.put("analytics", analyticsDao.adminAnalytics(ServletUtil.intParam(request, "merchantId", 0)));
        } catch (RuntimeException e) {
            result = ServletUtil.fail("数据分析加载失败：" + e.getMessage());
        }
        JsonUtil.write(response, result);
    }
}
