package hishopping.service;

import java.util.List;

import hishopping.dao.ProductDao;
import hishopping.entity.Product;

public class AdminProductAuditService {
    private ProductDao productDao = new ProductDao();

    public List<Product> auditRows() {
        return productDao.findAuditRows();
    }

    public void approve(int productId, int adminId) {
        productDao.auditProduct(productId, "APPROVED", "ON_SALE", "审核通过", adminId);
    }

    public void reject(int productId, String opinion, int adminId) {
        productDao.auditProduct(productId, "REJECTED", "OFF_SALE", opinion == null ? "审核驳回" : opinion, adminId);
    }
}
