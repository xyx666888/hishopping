package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.CouponTemplate;
import hishopping.entity.Merchant;
import hishopping.service.CouponService;
import hishopping.service.MerchantService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/merchant/coupons")
public class MerchantCouponServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CouponService service = new CouponService();
    private MerchantService merchantService = new MerchantService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u5148\u767b\u5f55\u5546\u5bb6\u8d26\u53f7\u3002"));
            return;
        }
        writeRows(response, merchant, null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u5148\u767b\u5f55\u5546\u5bb6\u8d26\u53f7\u3002"));
            return;
        }
        merchant = activeMerchant(request, merchant);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("\u5546\u5bb6\u8d26\u53f7\u5f53\u524d\u4e0d\u53ef\u7ef4\u62a4\u5e97\u94fa\u4f18\u60e0\u5238\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u3002"));
            return;
        }
        CouponService.IssueResult issueResult = null;
        try {
            String action = request.getParameter("action");
            if ("issue".equals(action)) {
                issueResult = service.issueMerchant(ServletUtil.intParam(request, "couponId", 0), request.getParameter("issueType"), request.getParameter("targetValue"), merchant.getMerchantId());
            } else if ("status".equals(action)) {
                service.updateMerchantTemplateStatus(ServletUtil.intParam(request, "couponId", 0), merchant.getMerchantId(), request.getParameter("status"));
            } else if ("delete".equals(action)) {
                service.deleteMerchantTemplate(ServletUtil.intParam(request, "couponId", 0), merchant.getMerchantId());
            } else {
                service.saveMerchantTemplate(templateFromRequest(request), merchant.getMerchantId());
            }
            writeRows(response, merchant, issueResult);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private CouponTemplate templateFromRequest(HttpServletRequest request) {
        CouponTemplate c = new CouponTemplate();
        c.setCouponId(ServletUtil.intParam(request, "couponId", 0));
        c.setCouponName(request.getParameter("couponName"));
        c.setCouponType(request.getParameter("couponType"));
        c.setAmount(ServletUtil.doubleParam(request, "amount", 0));
        c.setDiscountRate(ServletUtil.doubleParam(request, "discountRate", 1));
        c.setMinAmount(ServletUtil.doubleParam(request, "minAmount", 0));
        c.setTotalQuantity(ServletUtil.intParam(request, "totalQuantity", 9999));
        c.setPerUserLimit(ServletUtil.intParam(request, "perUserLimit", 1));
        c.setValidDays(ServletUtil.intParam(request, "validDays", 30));
        c.setStatus(request.getParameter("status") == null ? "ENABLED" : request.getParameter("status"));
        c.setDescription(request.getParameter("description"));
        c.setHomeTitle(request.getParameter("homeTitle"));
        c.setHomeSubtitle(request.getParameter("homeSubtitle"));
        c.setTargetType("ALL");
        c.setUseScope("MERCHANT");
        return c;
    }

    private void writeRows(HttpServletResponse response, Merchant merchant, CouponService.IssueResult issueResult) throws IOException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("merchant", ServletUtil.merchant(merchant));
        result.put("templates", ServletUtil.couponTemplates(service.merchantTemplates(merchant.getMerchantId())));
        result.put("userCoupons", ServletUtil.userCoupons(service.merchantUserCoupons(merchant.getMerchantId())));
        result.put("logs", ServletUtil.couponLogs(service.logs()));
        if (issueResult != null) {
            result.put("batchNo", issueResult.getBatchNo());
            result.put("issueCount", issueResult.getIssueCount());
            result.put("skipCount", issueResult.getSkipCount());
        }
        JsonUtil.write(response, result);
    }

    private Merchant activeMerchant(HttpServletRequest request, Merchant merchant) {
        Merchant refreshed = merchantService.findById(merchant.getMerchantId());
        if (refreshed == null || !"APPROVED".equals(refreshed.getStatus())) return null;
        request.getSession().setAttribute("merchant", refreshed);
        return refreshed;
    }
}
