package hishopping.service;

import java.util.List;

import hishopping.dao.ProductDao;
import hishopping.entity.Product;
import hishopping.util.SkuUtil;

public class MerchantProductService {
    private ProductDao productDao = new ProductDao();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();

    public List<Product> products(int merchantId) {
        return productDao.findByMerchantId(merchantId);
    }

    public Product add(Product p) {
        restrictionService.require("MERCHANT", p.getMerchantId(), "can_add_product");
        validate(p);
        return productDao.saveMerchantProduct(p);
    }

    public void update(Product p, boolean requireAudit) {
        restrictionService.require("MERCHANT", p.getMerchantId(), "can_edit_product");
        validate(p);
        productDao.updateMerchantProduct(p, false);
    }

    public void submitAudit(int productId, int merchantId) {
        restrictionService.require("MERCHANT", merchantId, "can_edit_product");
        productDao.submitAudit(productId, merchantId);
    }

    public void onSale(int productId, int merchantId) {
        restrictionService.require("MERCHANT", merchantId, "can_on_sale_product");
        productDao.onSale(productId, merchantId);
    }

    public void offSale(int productId, int merchantId) {
        restrictionService.require("MERCHANT", merchantId, "can_off_sale_product");
        productDao.offSale(productId, merchantId);
    }

    private void validate(Product p) {
        if (p.getMerchantId() <= 0) throw new RuntimeException("请先登录商家账号。");
        if (p.getName() == null || p.getName().trim().length() == 0) throw new RuntimeException("请输入商品名称。");
        if (p.getCategoryId() <= 0) throw new RuntimeException("请选择商品分类。");
        if (p.getPrice() <= 0) throw new RuntimeException("请输入有效商品价格。");
        if (p.getOldPrice() <= 0) p.setOldPrice(p.getPrice());
        if (p.getShortDesc() == null || p.getShortDesc().trim().length() == 0) p.setShortDesc("商家精选商品");
        if (p.getDetailDesc() == null || p.getDetailDesc().trim().length() == 0) p.setDetailDesc(p.getShortDesc());
        normalizeSku(p);
    }

    private void normalizeSku(Product p) {
        if (p.getColorOptions() == null || p.getColorOptions().trim().length() == 0) p.setColorOptions("默认");
        if (p.getSpecOptions() == null || p.getSpecOptions().trim().length() == 0) p.setSpecOptions("标准");
        p.setSkuAttrs(SkuUtil.normalizeAttrsJson(p, p.getSkuAttrs(), p.getSkuOptions()));
        p.setSkuOptions(SkuUtil.normalizeJson(p, p.getSkuOptions()));
        int totalStock = 0;
        double minPrice = 0;
        double oldPrice = p.getOldPrice();
        for (SkuUtil.Sku sku : SkuUtil.skus(p)) {
            if (!sku.isEnabled()) continue;
            totalStock += Math.max(0, sku.getStock());
            if (minPrice <= 0 || sku.getPrice() < minPrice) {
                minPrice = sku.getPrice();
                oldPrice = sku.getOldPrice();
            }
        }
        p.setStock(totalStock);
        if (minPrice > 0) p.setPrice(minPrice);
        if (oldPrice > 0) p.setOldPrice(oldPrice);
    }
}
