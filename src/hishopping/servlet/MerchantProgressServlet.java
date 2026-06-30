package hishopping.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Merchant;
import hishopping.service.MerchantService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class MerchantProgressServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MerchantService merchantService = new MerchantService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contact = request.getParameter("contact");
        try {
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            for (Merchant merchant : merchantService.findByContact(contact)) {
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("merchantId", merchant.getMerchantId());
                row.put("merchantCode", merchant.getMerchantCode());
                row.put("merchantName", merchant.getMerchantName());
                row.put("contactName", merchant.getContactName());
                row.put("contactPhone", merchant.getContactPhone());
                row.put("email", merchant.getEmail());
                row.put("shopName", merchant.getShopName());
                row.put("shopDesc", merchant.getShopDesc());
                row.put("businessCategory", merchant.getBusinessCategory());
                row.put("businessAddress", merchant.getBusinessAddress());
                row.put("status", merchant.getStatus());
                row.put("rejectReason", merchant.getRejectReason());
                row.put("createTime", merchant.getCreateTime());
                row.put("reviewTime", merchant.getReviewTime());
                rows.add(row);
            }
            Map<String, Object> result = ServletUtil.ok();
            result.put("merchants", rows);
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }
}
