package hishopping.service;

import java.util.List;

import hishopping.dao.CartDao;
import hishopping.dao.ProductDao;
import hishopping.entity.CartItem;
import hishopping.entity.Product;
import hishopping.util.SkuUtil;

public class CartService {
    private CartDao cartDao = new CartDao();
    private ProductDao productDao = new ProductDao();

    public List<CartItem> list(int userId) {
        return cartDao.findByUserId(userId);
    }

    public void add(int userId, int productId) {
        add(userId, productId, null, null, null, 1);
    }

    public void add(int userId, int productId, String color, String spec, String skuId, int quantity) {
        Product product = productDao.findById(productId);
        if (product == null || "删除".equals(product.getStatus())) {
            throw new RuntimeException("商品不存在。");
        }
        if (!"ON_SALE".equals(product.getSaleStatus()) || !"APPROVED".equals(product.getAuditStatus())) {
            throw new RuntimeException("商品尚未审核通过或已下架。");
        }
        SkuUtil.Sku sku = SkuUtil.choose(product, skuId, color, spec);
        if (!sku.isEnabled()) {
            throw new RuntimeException("该规格暂不可购买。");
        }
        int addQty = Math.max(1, quantity);
        int currentQty = cartDao.quantityForSku(userId, productId, sku.getSkuId());
        if (currentQty + addQty > sku.getStock()) {
            throw new RuntimeException("该规格库存不足，请调整购买数量。");
        }
        cartDao.add(userId, productId, addQty, sku.getColor(), sku.getSpec(), sku.getSkuId(), SkuUtil.skuText(sku), sku.getPrice());
    }

    public void update(int userId, int productId, int quantity) {
        cartDao.updateQuantity(userId, productId, Math.max(1, quantity));
    }

    public void updateByCartItem(int userId, int cartItemId, int quantity) {
        int next = Math.max(1, quantity);
        CartItem target = null;
        for (CartItem item : list(userId)) {
            if (item.getId() == cartItemId) {
                target = item;
                break;
            }
        }
        if (target == null) {
            throw new RuntimeException("购物车商品不存在。");
        }
        SkuUtil.Sku sku = SkuUtil.choose(target.getProduct(), target.getSkuId(), target.getSelectedColor(), target.getSelectedSpec());
        if (next > sku.getStock()) {
            throw new RuntimeException("该规格库存不足。");
        }
        cartDao.updateQuantityById(userId, cartItemId, next);
    }

    public void remove(int userId, int productId) {
        cartDao.remove(userId, productId);
    }

    public void removeByCartItem(int userId, int cartItemId) {
        cartDao.removeById(userId, cartItemId);
    }
}

