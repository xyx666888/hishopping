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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("Please login as merchant first."));
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
            JsonUtil.write(response, ServletUtil.fail("Please login as merchant first."));
            return;
        }
        merchant = activeMerchant(request, merchant);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("Merchant account is not approved for product maintenance."));
            return;
        }
        try {
            String action = request.getParameter("action");
            int productId = ServletUtil.intParam(request, "productId", 0);
            if ("submit".equals(action)) {
                service.submitAudit(productId, merchant.getMerchantId());
            } else if ("offSale".equals(action)) {
                service.offSale(productId, merchant.getMerchantId());
            } else {
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
