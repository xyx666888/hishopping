package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.CartItem;
import hishopping.entity.Product;
import hishopping.util.DBUtil;

public class CartDao {
    public CartDao() {
        MerchantDao.ensureSchema();
    }

    public List<CartItem> findByUserId(int userId) {
        String sql = "select ci.id cart_id, ci.user_id, ci.quantity, ci.selected_color, ci.selected_spec, ci.sku_id, ci.sku_text, ci.sku_price, p.*, c.name category_name, m.merchant_code, m.shop_name from hishopping_cart_item ci join hishopping_product p on ci.product_id=p.id left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id where ci.user_id=? order by ci.id desc";
        List<CartItem> list = new ArrayList<CartItem>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            ProductDao productDao = new ProductDao();
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getInt("cart_id"));
                item.setUserId(rs.getInt("user_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setSelectedColor(rs.getString("selected_color"));
                item.setSelectedSpec(rs.getString("selected_spec"));
                item.setSkuId(rs.getString("sku_id"));
                item.setSkuText(rs.getString("sku_text"));
                item.setSkuPrice(rs.getDouble("sku_price"));
                Product product = productDao.mapProduct(rs);
                item.setProduct(product);
                list.add(item);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void add(int userId, int productId, int quantity, String color, String spec, String skuId, String skuText, double skuPrice) {
        String sql = "if exists(select 1 from hishopping_cart_item where user_id=? and product_id=? and sku_id=?) "
                + "update hishopping_cart_item set quantity=quantity+?, selected_color=?, selected_spec=?, sku_text=?, sku_price=? where user_id=? and product_id=? and sku_id=? "
                + "else insert into hishopping_cart_item(user_id, product_id, quantity, selected_color, selected_spec, sku_id, sku_text, sku_price) values(?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setString(3, skuId);
            ps.setInt(4, quantity);
            ps.setString(5, color);
            ps.setString(6, spec);
            ps.setString(7, skuText);
            ps.setDouble(8, skuPrice);
            ps.setInt(9, userId);
            ps.setInt(10, productId);
            ps.setString(11, skuId);
            ps.setInt(12, userId);
            ps.setInt(13, productId);
            ps.setInt(14, quantity);
            ps.setString(15, color);
            ps.setString(16, spec);
            ps.setString(17, skuId);
            ps.setString(18, skuText);
            ps.setDouble(19, skuPrice);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateQuantity(int userId, int productId, int quantity) {
        String sql = "update hishopping_cart_item set quantity=? where user_id=? and product_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateQuantityById(int userId, int cartItemId, int quantity) {
        String sql = "update hishopping_cart_item set quantity=? where user_id=? and id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void removeById(int userId, int cartItemId) {
        String sql = "delete from hishopping_cart_item where user_id=? and id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public int quantityForSku(int userId, int productId, String skuId) {
        String sql = "select quantity from hishopping_cart_item where user_id=? and product_id=? and sku_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setString(3, skuId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt("quantity") : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void remove(int userId, int productId) {
        String sql = "delete from hishopping_cart_item where user_id=? and product_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void clear(int userId, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("delete from hishopping_cart_item where user_id=?");
            ps.setInt(1, userId);
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void clearSelected(int userId, int[] cartItemIds, Connection conn) throws SQLException {
        if (cartItemIds == null || cartItemIds.length == 0) {
            return;
        }
        StringBuilder sql = new StringBuilder("delete from hishopping_cart_item where user_id=? and id in (");
        for (int i = 0; i < cartItemIds.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql.toString());
            ps.setInt(1, userId);
            for (int i = 0; i < cartItemIds.length; i++) {
                ps.setInt(i + 2, cartItemIds[i]);
            }
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}

