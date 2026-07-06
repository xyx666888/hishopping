package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Merchant;
import hishopping.service.ReportService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class MerchantReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportService service = new ReportService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录商家账号。"));
            return;
        }
        try {
            Map<String, Object> result = ServletUtil.ok();
            result.put("myReports", ServletUtil.reports(service.myReports("MERCHANT", merchant.getMerchantId())));
            result.put("relatedReports", ServletUtil.reports(service.merchantRelated(merchant.getMerchantId())));
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }
}
