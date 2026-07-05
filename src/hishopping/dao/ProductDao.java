package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.Category;
import hishopping.entity.Product;
import hishopping.entity.ProductMedia;
import hishopping.util.DBUtil;
import hishopping.util.SkuUtil;

public class ProductDao {
    private BusinessDao businessDao = new BusinessDao();
    private ProductMediaDao mediaDao = new ProductMediaDao();

    public ProductDao() {
        MerchantDao.ensureSchema();
        BusinessDao.ensureSchema();
        ProductMediaDao.ensureSchema();
    }

    public List<Category> findCategories() {
        String sql = "select id, name, icon_text, description from hishopping_category order by sort_no, id";
        List<Category> list = new ArrayList<Category>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setIconText(rs.getString("icon_text"));
                c.setDescription(rs.getString("description"));
                list.add(c);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
    }

    public List<Product> findAll() {
        String sql = "select p.*, c.name category_name, m.merchant_code, m.shop_name, (select count(1) from hishop_product_review r where r.product_id=p.id) review_count, (select avg(cast(rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id) average_rating from hishopping_product p left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id where p.status<>'删除' order by p.id";
        List<Product> list = new ArrayList<Product>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
    }

    public Product findById(int id) {
        String sql = "select p.*, c.name category_name, m.merchant_code, m.shop_name, (select count(1) from hishop_product_review r where r.product_id=p.id) review_count, (select avg(cast(rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id) average_rating from hishopping_product p left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id where p.id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapProduct(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Product> findAdminRows() {
        return findAll();
    }

    public List<Product> findFrontRows() {
        String sql = "select p.*, c.name category_name, m.merchant_code, m.shop_name, (select count(1) from hishop_product_review r where r.product_id=p.id) review_count, (select avg(cast(rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id) average_rating from hishopping_product p left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id where p.status<>'删除' and p.sale_status='ON_SALE' and p.audit_status='APPROVED' and (p.merchant_id is null or m.status='APPROVED') order by p.id";
        return queryProducts(sql, 0);
    }

    public List<Product> findFrontRows(String feed, int userId) {
        String normalized = feed == null ? "recommend" : feed.trim().toLowerCase();
        String base = frontSelect() + " where p.status<>'删除' and p.sale_status='ON_SALE' and p.audit_status='APPROVED' and (p.merchant_id is null or m.status='APPROVED') ";
        if ("featured".equals(normalized)) {
            return queryProducts(base + " and isnull((select avg(cast(rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id and isnull(r.status,N'ACTIVE')=N'ACTIVE'),0) >= 4 order by average_rating desc, p.sales desc, favorite_count desc, p.id desc", 0);
        }
        if ("discover".equals(normalized)) {
            return queryProducts(base + " order by favorite_count desc, average_rating desc, p.sales desc, p.id desc", 0);
        }
        if ("hot".equals(normalized)) {
            return queryProducts(base + " order by p.sales desc, average_rating desc, p.id desc", 0);
        }
        if (userId > 0) {
            String sql = base
                    + " order by (case when p.category_id in (select distinct p2.category_id from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id join hishopping_product p2 on oi.product_id=p2.id where o.user_id=? union select distinct p3.category_id from hishopping_favorite f join hishopping_product p3 on f.product_id=p3.id where f.user_id=? union select distinct p4.category_id from hishop_product_review r join hishopping_product p4 on r.product_id=p4.id where r.user_id=? and isnull(r.status,N'ACTIVE')=N'ACTIVE') then 40 else 0 end "
                    + "+ case when p.merchant_id in (select distinct p2.merchant_id from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id join hishopping_product p2 on oi.product_id=p2.id where o.user_id=? and p2.merchant_id is not null) then 20 else 0 end "
                    + "+ case when p.id in (select product_id from hishopping_favorite where user_id=?) then -100 else 0 end) desc, p.sales desc, average_rating desc, favorite_count desc, p.id desc";
            return queryProducts(sql, userId, userId, userId, userId, userId);
        }
        return queryProducts(base + " order by p.sales desc, average_rating desc, p.id desc", 0);
    }

    public List<Product> findByMerchantId(int merchantId) {
        String sql = "select p.*, c.name category_name, m.merchant_code, m.shop_name, (select count(1) from hishop_product_review r where r.product_id=p.id) review_count, (select avg(cast(rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id) average_rating from hishopping_product p left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id where p.merchant_id=? and p.status<>'删除' order by p.id desc";
        return queryProducts(sql, merchantId);
    }

    public List<Product> findAuditRows() {
        String sql = "select p.*, c.name category_name, m.merchant_code, m.shop_name, (select count(1) from hishop_product_review r where r.product_id=p.id) review_count, (select avg(cast(rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id) average_rating from hishopping_product p left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id where p.merchant_id is not null order by case when p.audit_status='PENDING' then 0 else 1 end, p.submit_time desc, p.id desc";
        return queryProducts(sql, 0);
    }

    private List<Product> queryProducts(String sql, int id) {
        if (id > 0) return queryProducts(sql, new Object[] { Integer.valueOf(id) });
        return queryProducts(sql, new Object[0]);
    }

    private List<Product> queryProducts(String sql, Object... params) {
        List<Product> list = new ArrayList<Product>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Integer) ps.setInt(i + 1, ((Integer) param).intValue());
                else ps.setObject(i + 1, param);
            }
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapProduct(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public Product saveMerchantProduct(Product p) {
        p.setMediaList(ProductMediaDao.normalize(p.getMediaList()));
        p.setImageUrl(ProductMediaDao.firstMediaUrl(p.getMediaList(), p.getImageUrl()));
        String sql = "insert into hishopping_product(category_id,name,short_desc,detail_desc,price,old_price,rating,sales,stock,tag,image_url,gradient,icon_text,color_options,spec_options,sku_options,status,merchant_id,sale_status,audit_status,submit_time) values(?,?,?,?,?,?,5.0,0,?,N'商家',?,N'linear-gradient(135deg,#16a34a,#0f766e)',N'品',?,?,?,N'上架中',?,N'OFF_SALE',N'PENDING',sysdatetime())";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getShortDesc());
            ps.setString(4, p.getDetailDesc());
            ps.setDouble(5, p.getPrice());
            ps.setDouble(6, p.getOldPrice());
            ps.setInt(7, p.getStock());
            ps.setString(8, p.getImageUrl());
            ps.setString(9, empty(p.getColorOptions()) ? "默认" : p.getColorOptions());
            ps.setString(10, empty(p.getSpecOptions()) ? "标准" : p.getSpecOptions());
            ps.setString(11, SkuUtil.normalizeJson(p, p.getSkuOptions()));
            ps.setInt(12, p.getMerchantId());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getInt(1));
            updateSkuPayload(p);
            mediaDao.replaceProductMedia(p.getId(), p.getMediaList());
            bindProductMediaResources(p);
            return p;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(keys, ps, conn);
        }
    }

    public void updateMerchantProduct(Product p, boolean requireAudit) {
        p.setMediaList(ProductMediaDao.normalize(p.getMediaList()));
        p.setImageUrl(ProductMediaDao.firstMediaUrl(p.getMediaList(), p.getImageUrl()));
        String auditSql = requireAudit ? ", sale_status=N'OFF_SALE', audit_status=N'PENDING', audit_opinion=NULL, submit_time=sysdatetime()" : "";
        String sql = "update hishopping_product set category_id=?, name=?, short_desc=?, detail_desc=?, price=?, old_price=?, stock=?, image_url=?, color_options=?, spec_options=?, sku_options=?" + auditSql + " where id=? and merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getShortDesc());
            ps.setString(4, p.getDetailDesc());
            ps.setDouble(5, p.getPrice());
            ps.setDouble(6, p.getOldPrice());
            ps.setInt(7, p.getStock());
            ps.setString(8, p.getImageUrl());
            ps.setString(9, empty(p.getColorOptions()) ? "默认" : p.getColorOptions());
            ps.setString(10, empty(p.getSpecOptions()) ? "标准" : p.getSpecOptions());
            ps.setString(11, SkuUtil.normalizeJson(p, p.getSkuOptions()));
            ps.setInt(12, p.getId());
            ps.setInt(13, p.getMerchantId());
            ps.executeUpdate();
            updateSkuPayload(p);
            mediaDao.replaceProductMedia(p.getId(), p.getMediaList());
            bindProductMediaResources(p);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void submitAudit(int productId, int merchantId) {
        updateMerchantSale(productId, merchantId, "OFF_SALE", "PENDING", null);
    }

    public void offSale(int productId, int merchantId) {
        updateMerchantSale(productId, merchantId, "OFF_SALE", null, null);
    }

    private void updateMerchantSale(int productId, int merchantId, String saleStatus, String auditStatus, String opinion) {
        String sql = "update hishopping_product set sale_status=?, audit_status=isnull(?,audit_status), audit_opinion=?, submit_time=case when ? is null then submit_time else sysdatetime() end where id=? and merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, saleStatus);
            ps.setString(2, auditStatus);
            ps.setString(3, opinion);
            ps.setString(4, auditStatus);
            ps.setInt(5, productId);
            ps.setInt(6, merchantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void auditProduct(int productId, String auditStatus, String saleStatus, String opinion, int adminId) {
        String sql = "ON_SALE".equals(saleStatus)
                ? "update p set p.audit_status=?, p.sale_status=?, p.audit_opinion=?, p.review_admin_id=?, p.review_time=sysdatetime() from hishopping_product p left join hishop_merchant m on p.merchant_id=m.merchant_id where p.id=? and (p.merchant_id is null or m.status='APPROVED')"
                : "update hishopping_product set audit_status=?, sale_status=?, audit_opinion=?, review_admin_id=?, review_time=sysdatetime() where id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, auditStatus);
            ps.setString(2, saleStatus);
            ps.setString(3, opinion);
            ps.setInt(4, adminId);
            ps.setInt(5, productId);
            if (ps.executeUpdate() == 0 && "ON_SALE".equals(saleStatus)) {
                throw new RuntimeException("\u5546\u5bb6\u672a\u542f\u7528\uff0c\u4e0d\u80fd\u5ba1\u6838\u4e0a\u67b6\u8be5\u5546\u54c1\u3002");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateAdminMerchantProduct(Product p) {
        p.setMediaList(ProductMediaDao.normalize(p.getMediaList()));
        p.setImageUrl(ProductMediaDao.firstMediaUrl(p.getMediaList(), p.getImageUrl()));
        String sql = "update hishopping_product set category_id=?, name=?, short_desc=?, detail_desc=?, price=?, old_price=?, stock=?, image_url=?, color_options=?, spec_options=?, sku_options=? where id=? and merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getShortDesc());
            ps.setString(4, p.getDetailDesc());
            ps.setDouble(5, p.getPrice());
            ps.setDouble(6, p.getOldPrice());
            ps.setInt(7, p.getStock());
            ps.setString(8, p.getImageUrl());
            ps.setString(9, empty(p.getColorOptions()) ? "默认" : p.getColorOptions());
            ps.setString(10, empty(p.getSpecOptions()) ? "标准" : p.getSpecOptions());
            ps.setString(11, SkuUtil.normalizeJson(p, p.getSkuOptions()));
            ps.setInt(12, p.getId());
            ps.setInt(13, p.getMerchantId());
            ps.executeUpdate();
            mediaDao.replaceProductMedia(p.getId(), p.getMediaList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void changeAdminMerchantSale(int productId, int merchantId, String saleStatus) {
        String sql = "ON_SALE".equals(saleStatus)
                ? "update p set p.sale_status=? from hishopping_product p join hishop_merchant m on p.merchant_id=m.merchant_id where p.id=? and p.merchant_id=? and p.audit_status='APPROVED' and m.status='APPROVED'"
                : "update hishopping_product set sale_status=? where id=? and merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, saleStatus);
            ps.setInt(2, productId);
            ps.setInt(3, merchantId);
            if (ps.executeUpdate() == 0 && "ON_SALE".equals(saleStatus)) {
                throw new RuntimeException("\u5546\u5bb6\u672a\u542f\u7528\u6216\u5546\u54c1\u672a\u5ba1\u6838\u901a\u8fc7\uff0c\u4e0d\u80fd\u4e0a\u67b6\u3002");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void changeStatus(int id, String status) {
        String sql = "update hishopping_product set status=? where id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setName(rs.getString("name"));
        p.setShortDesc(rs.getString("short_desc"));
        p.setDetailDesc(rs.getString("detail_desc"));
        p.setPrice(rs.getDouble("price"));
        p.setOldPrice(rs.getDouble("old_price"));
        p.setRating(rs.getDouble("rating"));
        p.setSales(rs.getInt("sales"));
        p.setStock(rs.getInt("stock"));
        p.setTag(rs.getString("tag"));
        p.setImageUrl(rs.getString("image_url"));
        p.setGradient(rs.getString("gradient"));
        p.setIconText(rs.getString("icon_text"));
        p.setColorOptions(rs.getString("color_options"));
        p.setSpecOptions(rs.getString("spec_options"));
        p.setSkuAttrs(getString(rs, "sku_attrs"));
        p.setSkuOptions(getString(rs, "sku_options"));
        p.setStatus(rs.getString("status"));
        p.setMerchantId(getInt(rs, "merchant_id"));
        p.setMerchantCode(getString(rs, "merchant_code"));
        p.setShopName(getString(rs, "shop_name"));
        p.setSaleStatus(getString(rs, "sale_status"));
        p.setAuditStatus(getString(rs, "audit_status"));
        p.setAuditOpinion(getString(rs, "audit_opinion"));
        p.setSubmitTime(getString(rs, "submit_time"));
        p.setReviewTime(getString(rs, "review_time"));
        p.setReviewAdminId(getInt(rs, "review_admin_id"));
        p.setReviewCount(getInt(rs, "review_count"));
        p.setAverageRating(getDouble(rs, "average_rating", p.getRating()));
        p.setFavoriteCount(getInt(rs, "favorite_count"));
        p.setReviewLikeCount(getInt(rs, "review_like_count"));
        p.setMediaList(mediaDao.findByProductId(p.getId(), p.getImageUrl()));
        return p;
    }

    private String frontSelect() {
        return "select p.*, c.name category_name, m.merchant_code, m.shop_name, "
                + "(select count(1) from hishop_product_review r where r.product_id=p.id and isnull(r.status,N'ACTIVE')=N'ACTIVE') review_count, "
                + "isnull((select avg(cast(rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id and isnull(r.status,N'ACTIVE')=N'ACTIVE'),0) average_rating, "
                + "(select count(1) from hishopping_favorite f where f.product_id=p.id) favorite_count, "
                + "isnull((select sum(like_count) from hishop_product_review r where r.product_id=p.id and isnull(r.status,N'ACTIVE')=N'ACTIVE'),0) review_like_count "
                + "from hishopping_product p left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id ";
    }

    private void updateSkuPayload(Product p) {
        if (p == null || p.getId() <= 0) return;
        String sql = "update hishopping_product set sku_attrs=?, sku_options=? where id=? and merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, SkuUtil.normalizeAttrsJson(p, p.getSkuAttrs(), p.getSkuOptions()));
            ps.setString(2, SkuUtil.normalizeJson(p, p.getSkuOptions()));
            ps.setInt(3, p.getId());
            ps.setInt(4, p.getMerchantId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private void bindProductMediaResources(Product p) {
        if (p == null || p.getMerchantId() <= 0 || p.getId() <= 0) return;
        for (ProductMedia media : ProductMediaDao.normalize(p.getMediaList())) {
            businessDao.bindUploadResource(p.getMerchantId(), media.getMediaUrl(), p.getId());
        }
        businessDao.bindUploadResource(p.getMerchantId(), p.getImageUrl(), p.getId());
    }

    private String getString(ResultSet rs, String column) {
        try {
            Object value = rs.getObject(column);
            return value == null ? "" : String.valueOf(value);
        } catch (SQLException e) {
            return "";
        }
    }

    private int getInt(ResultSet rs, String column) {
        try {
            return rs.getInt(column);
        } catch (SQLException e) {
            return 0;
        }
    }

    private double getDouble(ResultSet rs, String column, double fallback) {
        try {
            Object value = rs.getObject(column);
            return value == null ? fallback : rs.getDouble(column);
        } catch (SQLException e) {
            return fallback;
        }
    }

    private boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }
}

