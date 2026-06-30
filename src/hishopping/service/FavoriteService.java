package hishopping.service;

import java.util.List;

import hishopping.dao.FavoriteDao;
import hishopping.dao.ProductDao;
import hishopping.entity.Favorite;
import hishopping.entity.Product;

public class FavoriteService {
    private FavoriteDao favoriteDao = new FavoriteDao();
    private ProductDao productDao = new ProductDao();

    public List<Integer> favoriteIds(int userId) {
        return favoriteDao.favoriteIds(userId);
    }

    public List<Favorite> favorites(int userId) {
        return favoriteDao.favorites(userId);
    }

    public void add(int userId, int productId) {
        Product product = productDao.findById(productId);
        validateCollectable(product);
        favoriteDao.add(userId, productId);
    }

    public void remove(int userId, int productId) {
        favoriteDao.remove(userId, productId);
    }

    public boolean toggle(int userId, int productId) {
        if (favoriteDao.exists(userId, productId)) {
            favoriteDao.remove(userId, productId);
            return false;
        }
        Product product = productDao.findById(productId);
        validateCollectable(product);
        favoriteDao.add(userId, productId);
        return true;
    }

    private void validateCollectable(Product product) {
        if (product == null || "\u5220\u9664".equals(product.getStatus())) {
            throw new RuntimeException("\u5546\u54c1\u4e0d\u5b58\u5728\u6216\u5df2\u5931\u6548\u3002");
        }
        if ("\u4e0b\u67b6".equals(product.getStatus()) || "OFF_SALE".equals(product.getSaleStatus()) || !"APPROVED".equals(product.getAuditStatus())) {
            throw new RuntimeException("\u8be5\u5546\u54c1\u6682\u4e0d\u53ef\u6536\u85cf\u3002");
        }
    }
}
