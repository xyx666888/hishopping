package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hishopping.util.DBUtil;

public class AnalyticsDao {
    public AnalyticsDao() {
        MerchantDao.ensureSchema();
        BusinessDao.ensureSchema();
        FavoriteDao.ensureSchema();
    }

    public Map<String, Object> merchantAnalytics(int merchantId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("summary", one("select "
                + "(select count(1) from hishopping_product p where p.merchant_id=? and p.status<>N'删除') product_count,"
                + "(select count(distinct o.id) from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id join hishopping_product p on oi.product_id=p.id where p.merchant_id=? and o.status<>N'已取消') order_count,"
                + "(select isnull(sum(isnull(oi.item_subtotal, oi.price * oi.quantity)),0) from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id join hishopping_product p on oi.product_id=p.id where p.merchant_id=? and o.status=N'已完成') sales_amount,"
                + "isnull(sum(sales),0) total_sales,isnull(sum(favorite_count),0) favorite_count,isnull(sum(review_count),0) review_count,isnull(sum(review_like_count),0) review_like_count,isnull(avg(nullif(average_rating,0)),0) shop_rating "
                + "from (" + productStatsSql("where p.merchant_id=? and p.status<>N'删除'") + ") x", merchantId, merchantId, merchantId, merchantId));
        result.put("products", rows(productStatsSql("where p.merchant_id=? and p.status<>N'删除' order by p.sales desc,favorite_count desc,average_rating desc,p.id desc"), merchantId));
        result.put("recentReviews", rows("select top 20 r.review_id,r.product_id,p.name product_name,u.username,r.rating,r.content,r.like_count,r.create_time from hishop_product_review r join hishopping_product p on r.product_id=p.id left join hishopping_user u on r.user_id=u.id where p.merchant_id=? and isnull(r.status,N'ACTIVE')=N'ACTIVE' order by r.create_time desc,r.review_id desc", merchantId));
        return result;
    }

    public Map<String, Object> adminAnalytics(int merchantId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("summary", one("select "
                + "(select count(1) from hishopping_product where status<>N'删除') product_count,"
                + "(select count(1) from hishopping_order where status<>N'已取消') order_count,"
                + "(select isnull(sum(isnull(oi.item_subtotal, oi.price * oi.quantity)),0) from hishopping_order_item oi join hishopping_order o on oi.order_id=o.id where o.status=N'已完成') sales_amount,"
                + "(select isnull(sum(sales),0) from hishopping_product where status<>N'删除') total_sales,"
                + "(select count(1) from hishop_merchant where isnull(status,N'')<>N'CANCELLED') merchant_count,"
                + "(select count(1) from hishopping_user where role=N'user' and isnull(status,N'')<>N'已注销') user_count,"
                + "(select count(1) from hishopping_favorite) favorite_count,"
                + "(select count(1) from hishop_product_review where isnull(status,N'ACTIVE')=N'ACTIVE') review_count,"
                + "(select isnull(sum(like_count),0) from hishop_product_review where isnull(status,N'ACTIVE')=N'ACTIVE') review_like_count,"
                + "(select isnull(avg(cast(rating as decimal(5,2))),0) from hishop_product_review where isnull(status,N'ACTIVE')=N'ACTIVE') shop_rating"));
        result.put("shops", rows("select m.merchant_id,m.merchant_code,m.merchant_name,m.shop_name,"
                + "isnull(ps.product_count,0) product_count,isnull(ps.total_sales,0) total_sales,"
                + "isnull((select count(distinct o.id) from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id join hishopping_product op on oi.product_id=op.id where op.merchant_id=m.merchant_id and o.status<>N'已取消'),0) order_count,"
                + "isnull((select sum(isnull(oi.item_subtotal, oi.price * oi.quantity)) from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id join hishopping_product op on oi.product_id=op.id where op.merchant_id=m.merchant_id and o.status=N'已完成'),0) sales_amount,"
                + "isnull(ps.shop_rating,0) shop_rating,isnull(ps.favorite_count,0) favorite_count,isnull(ps.review_count,0) review_count "
                + "from hishop_merchant m left join (select merchant_id,count(1) product_count,isnull(sum(sales),0) total_sales,isnull(sum(favorite_count),0) favorite_count,isnull(sum(review_count),0) review_count,isnull(avg(nullif(average_rating,0)),0) shop_rating from ("
                + productStatsSql("where p.status<>N'删除'")
                + ") product_stats group by merchant_id) ps on m.merchant_id=ps.merchant_id "
                + "where isnull(m.status,N'')<>N'CANCELLED' order by shop_rating desc,total_sales desc,m.merchant_id desc"));
        String filter = merchantId > 0 ? "where p.merchant_id=? and p.status<>N'删除'" : "where p.status<>N'删除'";
        result.put("products", merchantId > 0
                ? rows(productStatsSql(filter + " order by p.sales desc,favorite_count desc,average_rating desc,p.id desc"), merchantId)
                : rows(productStatsSql(filter + " order by p.sales desc,favorite_count desc,average_rating desc,p.id desc")));
        result.put("recentReviews", merchantId > 0
                ? rows("select top 50 r.review_id,r.product_id,p.name product_name,u.username,r.rating,r.content,r.like_count,r.create_time from hishop_product_review r join hishopping_product p on r.product_id=p.id left join hishopping_user u on r.user_id=u.id where p.merchant_id=? and isnull(r.status,N'ACTIVE')=N'ACTIVE' order by r.create_time desc,r.review_id desc", merchantId)
                : rows("select top 50 r.review_id,r.product_id,p.name product_name,u.username,r.rating,r.content,r.like_count,r.create_time from hishop_product_review r join hishopping_product p on r.product_id=p.id left join hishopping_user u on r.user_id=u.id where isnull(r.status,N'ACTIVE')=N'ACTIVE' order by r.create_time desc,r.review_id desc"));
        return result;
    }

    private String productStatsSql(String whereAndOrder) {
        return "select p.id product_id,p.name product_name,p.category_id,c.name category_name,p.merchant_id,m.merchant_code,m.shop_name,p.price,p.stock,p.sales,p.sale_status,p.audit_status,"
                + "(select count(1) from hishopping_favorite f where f.product_id=p.id) favorite_count,"
                + "(select count(1) from hishop_product_review r where r.product_id=p.id and isnull(r.status,N'ACTIVE')=N'ACTIVE') review_count,"
                + "isnull((select avg(cast(r.rating as decimal(5,2))) from hishop_product_review r where r.product_id=p.id and isnull(r.status,N'ACTIVE')=N'ACTIVE'),0) average_rating,"
                + "isnull((select sum(r.like_count) from hishop_product_review r where r.product_id=p.id and isnull(r.status,N'ACTIVE')=N'ACTIVE'),0) review_like_count "
                + "from hishopping_product p left join hishopping_category c on p.category_id=c.id left join hishop_merchant m on p.merchant_id=m.merchant_id " + whereAndOrder;
    }

    private Map<String, Object> one(String sql, Object... params) {
        List<Map<String, Object>> list = rows(sql, params);
        return list.isEmpty() ? new LinkedHashMap<String, Object>() : list.get(0);
    }

    private List<Map<String, Object>> rows(String sql, Object... params) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= count; i++) {
                    String key = meta.getColumnLabel(i);
                    map.put(key, rs.getObject(i));
                }
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }
}
