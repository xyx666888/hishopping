package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.dao.ProductMediaDao;
import hishopping.entity.Admin;
import hishopping.entity.Merchant;
import hishopping.entity.Product;
import hishopping.service.BusinessService;
import hishopping.service.MerchantService;
import hishopping.service.ProductService;
import hishopping.service.AccountRestrictionService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/admin/merchants")
public class AdminMerchantAuditServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MerchantService service = new MerchantService();
    private ProductService productService = new ProductService();
    private BusinessService businessService = new BusinessService();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin == null) {
            JsonUtil.write(response, ServletUtil.fail("Please login as admin first."));
            return;
        }
        writeRows(response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin == null) {
            JsonUtil.write(response, ServletUtil.fail("Please login as admin first."));
            return;
        }
        try {
            String action = request.getParameter("action");
            int merchantId = ServletUtil.intParam(request, "merchantId", 0);
            if ("update".equals(action)) {
                Merchant merchant = new Merchant();
                merchant.setMerchantId(merchantId);
                merchant.setMerchantName(request.getParameter("merchantName"));
                merchant.setPassword(request.getParameter("password"));
                merchant.setContactName(request.getParameter("contactName"));
                merchant.setContactPhone(request.getParameter("contactPhone"));
                merchant.setEmail(request.getParameter("email"));
                merchant.setShopName(request.getParameter("shopName"));
                merchant.setShopDesc(request.getParameter("shopDesc"));
                merchant.setBusinessCategory(request.getParameter("businessCategory"));
                merchant.setBusinessAddress(request.getParameter("businessAddress"));
                service.updateProfile(merchant, false);
            } else if ("productUpdate".equals(action)) {
                Product product = new Product();
                product.setId(ServletUtil.intParam(request, "productId", 0));
                product.setMerchantId(merchantId);
                product.setCategoryId(ServletUtil.intParam(request, "categoryId", 0));
                product.setName(request.getParameter("name"));
                product.setShortDesc(request.getParameter("shortDesc"));
                product.setDetailDesc(request.getParameter("detailDesc"));
                product.setPrice(ServletUtil.doubleParam(request, "price", 0));
                product.setOldPrice(ServletUtil.doubleParam(request, "oldPrice", 0));
                product.setStock(ServletUtil.intParam(request, "stock", 0));
                product.setImageUrl(request.getParameter("imageUrl"));
                product.setMediaList(ProductMediaDao.parseMediaJson(request.getParameter("mediaList")));
                product.setColorOptions(request.getParameter("colorOptions"));
                product.setSpecOptions(request.getParameter("specOptions"));
                product.setSkuAttrs(request.getParameter("skuAttrs"));
                product.setSkuOptions(request.getParameter("skuOptions"));
                product.setProductAttrs(request.getParameter("productAttrs"));
                productService.updateAdminMerchantProduct(product);
            } else if ("productAudit".equals(action)) {
                productService.auditMerchantProduct(ServletUtil.intParam(request, "productId", 0), request.getParameter("auditAction"), request.getParameter("opinion"), admin.getId());
                businessService.logAdmin(admin.getId(), "AUDIT_PRODUCT", "PRODUCT", ServletUtil.intParam(request, "productId", 0), request.getParameter("auditAction"));
            } else if ("productSale".equals(action)) {
                productService.changeMerchantProductSale(ServletUtil.intParam(request, "productId", 0), merchantId, request.getParameter("saleStatus"));
            } else if ("restrict".equals(action)) {
                int rawDays = ServletUtil.intParam(request, "durationDays", 0);
                Integer days = rawDays > 0 ? Integer.valueOf(rawDays) : null;
                String adminName = admin.getRealName() == null || admin.getRealName().length() == 0 ? admin.getAdminName() : admin.getRealName();
                restrictionService.restrict("MERCHANT", merchantId, request.getParameter("permissionKey"), request.getParameter("reason"), "ADMIN", 0, days, admin.getId(), adminName);
                businessService.logAdmin(admin.getId(), "RESTRICT_MERCHANT", "MERCHANT", merchantId, "限制权限 " + request.getParameter("permissionKey"));
            } else if ("cancelRestriction".equals(action)) {
                restrictionService.cancel(ServletUtil.intParam(request, "restrictionId", 0));
                businessService.logAdmin(admin.getId(), "CANCEL_MERCHANT_RESTRICTION", "MERCHANT", merchantId, "解除权限限制");
            } else {
                service.audit(merchantId, request.getParameter("status"), request.getParameter("opinion"), admin.getId());
                businessService.logAdmin(admin.getId(), "AUDIT_MERCHANT", "MERCHANT", merchantId, request.getParameter("status"));
            }
            writeRows(response);
            return;
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage() == null ? "操作失败，请稍后重试。" : e.getMessage()));
            return;
        }
    }

    private void writeRows(HttpServletResponse response) throws IOException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("merchants", ServletUtil.merchants(service.merchants()));
        result.put("products", ServletUtil.products(productService.adminProducts()));
        result.put("categories", ServletUtil.categories(productService.categories()));
        JsonUtil.write(response, result);
    }
}
