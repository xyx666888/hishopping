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
import hishopping.entity.Merchant;
import hishopping.entity.Product;
import hishopping.service.AccountRestrictionService;
import hishopping.service.MerchantProductService;
import hishopping.service.MerchantService;
import hishopping.service.ProductService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/merchant/products")
public class MerchantProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MerchantProductService service = new MerchantProductService();
    private ProductService productService = new ProductService();
    private MerchantService merchantService = new MerchantService();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录商家账号。"));
            return;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("merchant", ServletUtil.merchant(merchant));
        result.put("categories", ServletUtil.categories(productService.categories()));
        result.put("products", ServletUtil.products(service.products(merchant.getMerchantId())));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录商家账号。"));
            return;
        }
        merchant = activeMerchant(request, merchant);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("商家账号当前不可维护商品，请联系管理员。"));
            return;
        }
        try {
            String action = request.getParameter("action");
            int productId = ServletUtil.intParam(request, "productId", 0);
            if ("submit".equals(action)) {
                restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_on_sale_product");
                service.onSale(productId, merchant.getMerchantId());
            } else if ("submitAudit".equals(action)) {
                restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_on_sale_product");
                service.submitAudit(productId, merchant.getMerchantId());
            } else if ("offSale".equals(action)) {
                restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_off_sale_product");
                service.offSale(productId, merchant.getMerchantId());
            } else {
                restrictionService.require("MERCHANT", merchant.getMerchantId(), "update".equals(action) ? "can_edit_product" : "can_add_product");
                Product p = new Product();
                p.setId(productId);
                p.setMerchantId(merchant.getMerchantId());
                p.setCategoryId(ServletUtil.intParam(request, "categoryId", 0));
                p.setName(request.getParameter("name"));
                p.setShortDesc(request.getParameter("shortDesc"));
                p.setDetailDesc(request.getParameter("detailDesc"));
                p.setPrice(ServletUtil.doubleParam(request, "price", 0));
                p.setOldPrice(ServletUtil.doubleParam(request, "oldPrice", 0));
                p.setStock(ServletUtil.intParam(request, "stock", 0));
                p.setImageUrl(request.getParameter("imageUrl"));
                p.setMediaList(ProductMediaDao.parseMediaJson(request.getParameter("mediaList")));
                p.setColorOptions(request.getParameter("colorOptions"));
                p.setSpecOptions(request.getParameter("specOptions"));
                p.setSkuAttrs(request.getParameter("skuAttrs"));
                p.setSkuOptions(request.getParameter("skuOptions"));
                p.setProductAttrs(request.getParameter("productAttrs"));
                if ("update".equals(action)) service.update(p, true); else service.add(p);
            }
            Map<String, Object> result = ServletUtil.ok();
            result.put("merchant", ServletUtil.merchant(activeMerchant(request, merchant)));
            result.put("products", ServletUtil.products(service.products(merchant.getMerchantId())));
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private Merchant activeMerchant(HttpServletRequest request, Merchant merchant) {
        Merchant refreshed = merchantService.findById(merchant.getMerchantId());
        if (refreshed == null || !"APPROVED".equals(refreshed.getStatus())) return null;
        request.getSession().setAttribute("merchant", refreshed);
        return refreshed;
    }
}
