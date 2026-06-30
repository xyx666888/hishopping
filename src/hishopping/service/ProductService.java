package hishopping.service;

import java.util.List;

import hishopping.dao.ProductDao;
import hishopping.entity.Category;
import hishopping.entity.Product;
import hishopping.util.SkuUtil;

public class ProductService {
    private ProductDao productDao = new ProductDao();

    public List<Category> categories() {
        return productDao.findCategories();
    }

    public List<Product> products() {
        return productDao.findFrontRows();
    }

    public List<Product> products(String feed, int userId) {
        return productDao.findFrontRows(feed, userId);
    }

    public List<Product> adminProducts() {
        return productDao.findAll();
    }

    public Product detail(int id) {
        return productDao.findById(id);
    }

    public void changeStatus(int id, String status) {
        productDao.changeStatus(id, status);
    }

    public void updateAdminMerchantProduct(Product product) {
        if (product.getId() <= 0 || product.getMerchantId() <= 0) {
            throw new RuntimeException("\u8bf7\u9009\u62e9\u5546\u5bb6\u5546\u54c1\u3002");
        }
        if (product.getName() == null || product.getName().trim().length() == 0) {
            throw new RuntimeException("\u8bf7\u8f93\u5165\u5546\u54c1\u540d\u79f0\u3002");
        }
        normalizeSku(product);
        productDao.updateAdminMerchantProduct(product);
    }

    public void auditMerchantProduct(int productId, String action, String opinion, int adminId) {
        if ("approve".equals(action)) {
            productDao.auditProduct(productId, "APPROVED", "ON_SALE", "\u5ba1\u6838\u901a\u8fc7", adminId);
        } else {
            productDao.auditProduct(productId, "REJECTED", "OFF_SALE", opinion == null || opinion.trim().length() == 0 ? "\u5ba1\u6838\u9a73\u56de" : opinion, adminId);
        }
    }

    public void changeMerchantProductSale(int productId, int merchantId, String saleStatus) {
        if (!"ON_SALE".equals(saleStatus) && !"OFF_SALE".equals(saleStatus)) {
            throw new RuntimeException("\u5546\u54c1\u4e0a\u4e0b\u67b6\u72b6\u6001\u4e0d\u6b63\u786e\u3002");
        }
        productDao.changeAdminMerchantSale(productId, merchantId, saleStatus);
    }

    private void normalizeSku(Product product) {
        if (product.getColorOptions() == null || product.getColorOptions().trim().length() == 0) product.setColorOptions("默认");
        if (product.getSpecOptions() == null || product.getSpecOptions().trim().length() == 0) product.setSpecOptions("标准");
        product.setSkuAttrs(SkuUtil.normalizeAttrsJson(product, product.getSkuAttrs(), product.getSkuOptions()));
        product.setSkuOptions(SkuUtil.normalizeJson(product, product.getSkuOptions()));
        int totalStock = 0;
        double minPrice = 0;
        double oldPrice = product.getOldPrice();
        for (SkuUtil.Sku sku : SkuUtil.skus(product)) {
            if (!sku.isEnabled()) continue;
            totalStock += Math.max(0, sku.getStock());
            if (minPrice <= 0 || sku.getPrice() < minPrice) {
                minPrice = sku.getPrice();
                oldPrice = sku.getOldPrice();
            }
        }
        product.setStock(totalStock);
        if (minPrice > 0) product.setPrice(minPrice);
        if (oldPrice > 0) product.setOldPrice(oldPrice);
    }
}

