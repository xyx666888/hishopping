package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.Favorite;
import hishopping.entity.Product;
import hishopping.util.DBUtil;

public class FavoriteDao {
    private static boolean schemaReady = false;
    private ProductDao productDao = new ProductDao();

    public FavoriteDao() {
        ensureSchema();
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishopping_favorite', N'U') is null create table dbo.hishopping_favorite (id int identity(1,1) primary key, user_id int not null, product_id int not null, create_time datetime2 not null default sysdatetime(), constraint UQ_hishopping_favorite_user_product unique(user_id, product_id))");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_favorite_user' and object_id=object_id(N'dbo.hishopping_favorite')) create index IX_hishopping_favorite_user on dbo.hishopping_favorite(user_id, create_time desc)");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    public List<Integer> favoriteIds(int userId) {
        List<Integer> ids = new ArrayList<Integer>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("select product_id from hishopping_favorite where user_id=? order by create_time desc");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) ids.add(Integer.valueOf(rs.getInt("product_id")));
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Favorite> favorites(int userId) {
        List<Favorite> list = new ArrayList<Favorite>();
        String sql = "select f.id favorite_id, f.user_id favorite_user_id, f.product_id favorite_product_id, f.create_time favorite_time, p.*, c.name category_name, m.merchant_code, m.shop_name from hishopping_favorite f join hishopping_product p on f.product_id=p.id left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id where f.user_id=? and p.status<>N'\u5220\u9664' order by f.create_time desc";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Favorite favorite = new Favorite();
                favorite.setId(rs.getInt("favorite_id"));
                favorite.setUserId(rs.getInt("favorite_user_id"));
                favorite.setProductId(rs.getInt("favorite_product_id"));
                favorite.setCreateTime(String.valueOf(rs.getObject("favorite_time")));
                Product product = productDao.mapProduct(rs);
                favorite.setProduct(product);
                list.add(favorite);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean exists(int userId, int productId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("select count(1) from hishopping_favorite where user_id=? and product_id=?");
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void add(int userId, int productId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("if not exists(select 1 from hishopping_favorite where user_id=? and product_id=?) insert into hishopping_favorite(user_id, product_id) values(?,?)");
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, userId);
            ps.setInt(4, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void remove(int userId, int productId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("delete from hishopping_favorite where user_id=? and product_id=?");
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }
}
