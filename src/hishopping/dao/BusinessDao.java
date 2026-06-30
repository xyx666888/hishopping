package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hishopping.entity.AdminOperationLog;
import hishopping.entity.AfterSale;
import hishopping.entity.GrowthLog;
import hishopping.entity.ProductReview;
import hishopping.entity.ReviewReply;
import hishopping.entity.Shipment;
import hishopping.util.DBUtil;

public class BusinessDao {
    private static boolean schemaReady = false;

    public BusinessDao() {
        ensureSchema();
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if col_length('dbo.hishopping_order','growth_awarded') is null alter table dbo.hishopping_order add growth_awarded bit not null constraint DF_hishopping_order_growth_awarded default 0");
            st.executeUpdate("if object_id(N'dbo.hishop_order_shipment',N'U') is null create table dbo.hishop_order_shipment(shipment_id int identity(1,1) primary key,order_id int not null,merchant_id int not null,express_company nvarchar(80) not null,tracking_no nvarchar(80) not null,ship_time datetime2 not null default sysdatetime(),constraint UQ_hishop_order_shipment_order_merchant unique(order_id,merchant_id))");
            st.executeUpdate("if object_id(N'dbo.hishop_after_sale',N'U') is null create table dbo.hishop_after_sale(after_sale_id int identity(1,1) primary key,order_id int not null,user_id int not null,merchant_id int not null,product_id int not null,after_sale_type nvarchar(20) not null,reason nvarchar(500) not null,description nvarchar(1000) null,evidence_urls nvarchar(max) null,refund_amount decimal(10,2) not null default 0,status nvarchar(20) not null default N'\u5f85\u5ba1\u6838',apply_time datetime2 not null default sysdatetime(),handle_opinion nvarchar(500) null,admin_opinion nvarchar(500) null,update_time datetime2 null,handle_time datetime2 null)");
            st.executeUpdate("if col_length('dbo.hishop_after_sale','description') is null alter table dbo.hishop_after_sale add description nvarchar(1000) null");
            st.executeUpdate("if col_length('dbo.hishop_after_sale','evidence_urls') is null alter table dbo.hishop_after_sale add evidence_urls nvarchar(max) null");
            st.executeUpdate("if col_length('dbo.hishop_after_sale','admin_opinion') is null alter table dbo.hishop_after_sale add admin_opinion nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishop_after_sale','update_time') is null alter table dbo.hishop_after_sale add update_time datetime2 null");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_after_sale_order' and object_id=object_id(N'dbo.hishop_after_sale')) create index IX_hishop_after_sale_order on dbo.hishop_after_sale(order_id, user_id)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_after_sale_merchant_status' and object_id=object_id(N'dbo.hishop_after_sale')) create index IX_hishop_after_sale_merchant_status on dbo.hishop_after_sale(merchant_id, status, apply_time desc)");
            st.executeUpdate("if object_id(N'dbo.hishop_product_review',N'U') is null create table dbo.hishop_product_review(review_id int identity(1,1) primary key,order_id int not null,product_id int not null,user_id int not null,rating int not null,content nvarchar(1000) null,create_time datetime2 not null default sysdatetime(),constraint UQ_hishop_product_review_order_product unique(order_id,product_id,user_id))");
            st.executeUpdate("if col_length('dbo.hishop_product_review','order_item_id') is null alter table dbo.hishop_product_review add order_item_id int null");
            st.executeUpdate("if col_length('dbo.hishop_product_review','status') is null alter table dbo.hishop_product_review add status nvarchar(20) not null constraint DF_hishop_product_review_status default N'ACTIVE'");
            st.executeUpdate("if col_length('dbo.hishop_product_review','like_count') is null alter table dbo.hishop_product_review add like_count int not null constraint DF_hishop_product_review_like_count default 0");
            st.executeUpdate("if col_length('dbo.hishop_product_review','update_time') is null alter table dbo.hishop_product_review add update_time datetime2 null");
            st.executeUpdate("if object_id(N'dbo.hishopping_review_reply',N'U') is null create table dbo.hishopping_review_reply(reply_id int identity(1,1) primary key,review_id int not null,parent_reply_id int null,user_type nvarchar(20) not null,user_id int not null,user_name nvarchar(80) null,user_avatar nvarchar(300) null,content nvarchar(1000) not null,status nvarchar(20) not null default N'ACTIVE',create_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if object_id(N'dbo.hishopping_review_like',N'U') is null create table dbo.hishopping_review_like(like_id int identity(1,1) primary key,review_id int not null,user_type nvarchar(20) not null,user_id int not null,create_time datetime2 not null default sysdatetime(),constraint UQ_hishopping_review_like unique(review_id,user_type,user_id))");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_product_review_product_status' and object_id=object_id(N'dbo.hishop_product_review')) create index IX_hishop_product_review_product_status on dbo.hishop_product_review(product_id,status,create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_review_reply_review' and object_id=object_id(N'dbo.hishopping_review_reply')) create index IX_hishopping_review_reply_review on dbo.hishopping_review_reply(review_id,create_time)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_review_like_review' and object_id=object_id(N'dbo.hishopping_review_like')) create index IX_hishopping_review_like_review on dbo.hishopping_review_like(review_id)");
            st.executeUpdate("if object_id(N'dbo.hishop_growth_log',N'U') is null create table dbo.hishop_growth_log(log_id int identity(1,1) primary key,user_id int not null,growth_delta int not null default 0,points_delta int not null default 0,source_type nvarchar(40) not null,source_id int null,remark nvarchar(500) null,create_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if object_id(N'dbo.hishop_admin_operation_log',N'U') is null create table dbo.hishop_admin_operation_log(log_id int identity(1,1) primary key,admin_id int not null,operation_type nvarchar(50) not null,target_type nvarchar(50) not null,target_id int null,content nvarchar(1000) null,operation_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if object_id(N'dbo.hishop_upload_resource',N'U') is null create table dbo.hishop_upload_resource(resource_id int identity(1,1) primary key,merchant_id int not null,file_name nvarchar(200) not null,access_url nvarchar(500) not null,file_size bigint not null default 0,upload_time datetime2 not null default sysdatetime(),product_id int null)");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    public void saveShipment(int orderId, int merchantId, String expressCompany, String trackingNo, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("if exists(select 1 from hishop_order_shipment where order_id=? and merchant_id=?) update hishop_order_shipment set express_company=?,tracking_no=?,ship_time=sysdatetime() where order_id=? and merchant_id=? else insert into hishop_order_shipment(order_id,merchant_id,express_company,tracking_no) values(?,?,?,?)");
            ps.setInt(1, orderId);
            ps.setInt(2, merchantId);
            ps.setString(3, expressCompany);
            ps.setString(4, trackingNo);
            ps.setInt(5, orderId);
            ps.setInt(6, merchantId);
            ps.setInt(7, orderId);
            ps.setInt(8, merchantId);
            ps.setString(9, expressCompany);
            ps.setString(10, trackingNo);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }

    public List<Shipment> shipmentsForOrder(int orderId, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Shipment> list = new ArrayList<Shipment>();
        try {
            ps = conn.prepareStatement("select * from hishop_order_shipment where order_id=? order by ship_time desc");
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapShipment(rs));
            return list;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    public int createAfterSale(int orderId, int userId, int productId, String type, String reason, double refundAmount) {
        return createAfterSale(orderId, userId, productId, type, reason, "", "", refundAmount);
    }

    public int createAfterSale(int orderId, int userId, int productId, String type, String reason, String description, String evidenceUrls, double refundAmount) {
        Connection conn = null;
        PreparedStatement check = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            check = conn.prepareStatement("select top 1 o.status,p.merchant_id,oi.item_subtotal from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id join hishopping_product p on oi.product_id=p.id where o.id=? and o.user_id=? and oi.product_id=?");
            check.setInt(1, orderId);
            check.setInt(2, userId);
            check.setInt(3, productId);
            rs = check.executeQuery();
            if (!rs.next()) throw new RuntimeException("\u8ba2\u5355\u5546\u54c1\u4e0d\u5b58\u5728\u6216\u4e0d\u5c5e\u4e8e\u5f53\u524d\u7528\u6237\u3002");
            String status = rs.getString("status");
            if (!"\u5f85\u53d1\u8d27".equals(status) && !"\u5f85\u6536\u8d27".equals(status) && !"\u5df2\u5b8c\u6210".equals(status)) {
                throw new RuntimeException("\u53ea\u6709\u5df2\u4ed8\u6b3e\u3001\u5f85\u6536\u8d27\u6216\u5df2\u5b8c\u6210\u8ba2\u5355\u53ef\u7533\u8bf7\u552e\u540e\u3002");
            }
            double maxRefund = rs.getDouble("item_subtotal");
            int merchantId = rs.getInt("merchant_id");
            if (refundAmount <= 0 || refundAmount > maxRefund) refundAmount = maxRefund;
            ps = conn.prepareStatement("insert into hishop_after_sale(order_id,user_id,merchant_id,product_id,after_sale_type,reason,description,evidence_urls,refund_amount,status) values(?,?,?,?,?,?,?,?,?,N'\u5f85\u5ba1\u6838')", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setInt(3, merchantId);
            ps.setInt(4, productId);
            ps.setString(5, blank(type) ? "\u9000\u6b3e" : type.trim());
            ps.setString(6, reason.trim());
            ps.setString(7, description);
            ps.setString(8, evidenceUrls);
            ps.setDouble(9, refundAmount);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            try {
                return keys.next() ? keys.getInt(1) : 0;
            } finally {
                if (keys != null) keys.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, check);
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void handleAfterSale(int afterSaleId, int merchantId, int adminId, String status, String opinion) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            String sql = merchantId > 0
                ? "update hishop_after_sale set status=?,handle_opinion=?,handle_time=sysdatetime(),update_time=sysdatetime() where after_sale_id=? and merchant_id=?"
                : "update hishop_after_sale set status=?,handle_opinion=?,handle_time=sysdatetime(),update_time=sysdatetime() where after_sale_id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, opinion);
            ps.setInt(3, afterSaleId);
            if (merchantId > 0) ps.setInt(4, merchantId);
            if (ps.executeUpdate() == 0) throw new RuntimeException("\u552e\u540e\u7533\u8bf7\u4e0d\u5b58\u5728\u6216\u65e0\u6743\u64cd\u4f5c\u3002");
            if (adminId > 0) logAdminOperation(conn, adminId, "AFTER_SALE", "AFTER_SALE", afterSaleId, status + " " + nvl(opinion));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public List<AfterSale> afterSalesForUser(int userId) {
        return queryAfterSales("select * from hishop_after_sale where user_id=? order by after_sale_id desc", userId);
    }

    public List<AfterSale> afterSalesForMerchant(int merchantId) {
        return queryAfterSales("select * from hishop_after_sale where merchant_id=? order by after_sale_id desc", merchantId);
    }

    public List<AfterSale> allAfterSales() {
        return queryAfterSales("select * from hishop_after_sale order by after_sale_id desc", 0);
    }

    private List<AfterSale> queryAfterSales(String sql, int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<AfterSale> list = new ArrayList<AfterSale>();
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            if (id > 0) ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapAfterSale(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void createReview(int orderId, int productId, int userId, int rating, String content) {
        Connection conn = null;
        PreparedStatement check = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            check = conn.prepareStatement("select top 1 oi.id from hishopping_order o join hishopping_order_item oi on o.id=oi.order_id where o.id=? and o.user_id=? and oi.product_id=? and o.status=N'\u5df2\u5b8c\u6210'");
            check.setInt(1, orderId);
            check.setInt(2, userId);
            check.setInt(3, productId);
            rs = check.executeQuery();
            if (!rs.next()) throw new RuntimeException("\u53ea\u80fd\u8bc4\u4ef7\u81ea\u5df1\u5df2\u5b8c\u6210\u8ba2\u5355\u4e2d\u7684\u5546\u54c1\u3002");
            int orderItemId = rs.getInt(1);
            ps = conn.prepareStatement("insert into hishop_product_review(order_id,order_item_id,product_id,user_id,rating,content,status) values(?,?,?,?,?,?,N'ACTIVE')");
            ps.setInt(1, orderId);
            ps.setInt(2, orderItemId);
            ps.setInt(3, productId);
            ps.setInt(4, userId);
            ps.setInt(5, Math.max(1, Math.min(5, rating)));
            ps.setString(6, content);
            ps.executeUpdate();
            awardGrowth(conn, userId, 10, 10, "REVIEW", orderId * 100000 + productId, "\u5546\u54c1\u8bc4\u4ef7\u5956\u52b1");
            updateProductRating(conn, productId);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            if (String.valueOf(e.getMessage()).indexOf("UQ_hishop_product_review_order_product") >= 0) {
                throw new RuntimeException("\u540c\u4e00\u8ba2\u5355\u5546\u54c1\u4e0d\u80fd\u91cd\u590d\u8bc4\u4ef7\u3002");
            }
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            close(rs, check);
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public List<ProductReview> reviewsForProduct(int productId) {
        return reviewsForProduct(productId, "USER", 0);
    }

    public List<ProductReview> reviewsForProduct(int productId, String actorType, int actorId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ProductReview> list = new ArrayList<ProductReview>();
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("select r.*,u.username,u.avatar_url,(select count(1) from hishopping_review_reply rr where rr.review_id=r.review_id and rr.status=N'ACTIVE') reply_count,(select count(1) from hishopping_review_like l where l.review_id=r.review_id and l.user_type=? and l.user_id=?) liked_count from hishop_product_review r left join hishopping_user u on r.user_id=u.id where r.product_id=? and r.status=N'ACTIVE' order by r.create_time desc,r.review_id desc");
            ps.setString(1, actorType == null ? "USER" : actorType);
            ps.setInt(2, actorId);
            ps.setInt(3, productId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapReview(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<ReviewReply> repliesForReview(int reviewId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ReviewReply> list = new ArrayList<ReviewReply>();
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("select * from hishopping_review_reply where review_id=? and status=N'ACTIVE' order by create_time asc,reply_id asc");
            ps.setInt(1, reviewId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapReply(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean likeReview(int reviewId, String userType, int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("if not exists(select 1 from hishopping_review_like where review_id=? and user_type=? and user_id=?) begin insert into hishopping_review_like(review_id,user_type,user_id) values(?,?,?); update hishop_product_review set like_count=like_count+1,update_time=sysdatetime() where review_id=?; end");
            ps.setInt(1, reviewId);
            ps.setString(2, userType);
            ps.setInt(3, userId);
            ps.setInt(4, reviewId);
            ps.setString(5, userType);
            ps.setInt(6, userId);
            ps.setInt(7, reviewId);
            ps.executeUpdate();
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void replyReview(int reviewId, String userType, int userId, String userName, String userAvatar, String content) {
        Connection conn = null;
        PreparedStatement check = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (content == null || content.trim().length() == 0) throw new RuntimeException("\u8bf7\u8f93\u5165\u56de\u590d\u5185\u5bb9\u3002");
            conn = DBUtil.getConn();
            check = conn.prepareStatement("select count(1) from hishop_product_review where review_id=? and status=N'ACTIVE'");
            check.setInt(1, reviewId);
            rs = check.executeQuery();
            if (!rs.next() || rs.getInt(1) == 0) throw new RuntimeException("\u8bc4\u8bba\u4e0d\u5b58\u5728\u6216\u5df2\u5931\u6548\u3002");
            ps = conn.prepareStatement("insert into hishopping_review_reply(review_id,user_type,user_id,user_name,user_avatar,content,status) values(?,?,?,?,?,?,N'ACTIVE')");
            ps.setInt(1, reviewId);
            ps.setString(2, userType);
            ps.setInt(3, userId);
            ps.setString(4, userName);
            ps.setString(5, userAvatar);
            ps.setString(6, content.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, check);
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public Map<String, Object> reviewStatsForUser(int userId) {
        Map<String, Object> map = new java.util.LinkedHashMap<String, Object>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("select count(1) review_count,isnull(sum(like_count),0) liked_count from hishop_product_review where user_id=? and status=N'ACTIVE'");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                map.put("reviewCount", rs.getInt("review_count"));
                map.put("likedCount", rs.getInt("liked_count"));
            }
            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean awardGrowth(Connection conn, int userId, int growth, int points, String sourceType, int sourceId, String remark) throws SQLException {
        PreparedStatement check = null;
        PreparedStatement log = null;
        PreparedStatement upd = null;
        ResultSet rs = null;
        try {
            check = conn.prepareStatement("select count(1) from hishop_growth_log where user_id=? and source_type=? and source_id=?");
            check.setInt(1, userId);
            check.setString(2, sourceType);
            check.setInt(3, sourceId);
            rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false;
            log = conn.prepareStatement("insert into hishop_growth_log(user_id,growth_delta,points_delta,source_type,source_id,remark) values(?,?,?,?,?,?)");
            log.setInt(1, userId);
            log.setInt(2, growth);
            log.setInt(3, points);
            log.setString(4, sourceType);
            log.setInt(5, sourceId);
            log.setString(6, remark);
            log.executeUpdate();
            upd = conn.prepareStatement("update hishopping_user set growth_value=growth_value+?,points=points+?,vip_level=? where id=?");
            int currentGrowth = currentGrowth(conn, userId);
            upd.setInt(1, growth);
            upd.setInt(2, points);
            upd.setInt(3, UserDao.calculateVipLevel(currentGrowth + growth));
            upd.setInt(4, userId);
            upd.executeUpdate();
            return true;
        } finally {
            close(rs, check);
            if (log != null) log.close();
            if (upd != null) upd.close();
        }
    }

    public void logGrowthOnly(int userId, int growth, int points, String sourceType, int sourceId, String remark) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("if not exists(select 1 from hishop_growth_log where user_id=? and source_type=? and source_id=?) insert into hishop_growth_log(user_id,growth_delta,points_delta,source_type,source_id,remark) values(?,?,?,?,?,?)");
            ps.setInt(1, userId);
            ps.setString(2, sourceType);
            ps.setInt(3, sourceId);
            ps.setInt(4, userId);
            ps.setInt(5, growth);
            ps.setInt(6, points);
            ps.setString(7, sourceType);
            ps.setInt(8, sourceId);
            ps.setString(9, remark);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public List<GrowthLog> growthLogsForUser(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<GrowthLog> list = new ArrayList<GrowthLog>();
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("select * from hishop_growth_log where user_id=? order by log_id desc");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapGrowthLog(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void logAdminOperation(int adminId, String operationType, String targetType, int targetId, String content) {
        Connection conn = null;
        try {
            conn = DBUtil.getConn();
            logAdminOperation(conn, adminId, operationType, targetType, targetId, content);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, null, conn);
        }
    }

    public void logAdminOperation(Connection conn, int adminId, String operationType, String targetType, int targetId, String content) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("insert into hishop_admin_operation_log(admin_id,operation_type,target_type,target_id,content) values(?,?,?,?,?)");
            ps.setInt(1, adminId);
            ps.setString(2, operationType);
            ps.setString(3, targetType);
            ps.setInt(4, targetId);
            ps.setString(5, content);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }

    public List<AdminOperationLog> adminLogsForUser(int userId) {
        return adminLogs("select * from hishop_admin_operation_log where (target_type=N'USER' and target_id=?) or (target_type=N'ORDER' and target_id in (select id from hishopping_order where user_id=?)) order by log_id desc", userId, userId);
    }

    public List<AdminOperationLog> allAdminLogs() {
        return adminLogs("select * from hishop_admin_operation_log order by log_id desc", 0, 0);
    }

    private List<AdminOperationLog> adminLogs(String sql, int id1, int id2) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<AdminOperationLog> list = new ArrayList<AdminOperationLog>();
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            if (id1 > 0) {
                ps.setInt(1, id1);
                ps.setInt(2, id2);
            }
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapAdminLog(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public int saveUploadResource(int merchantId, String fileName, String accessUrl, long fileSize) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("insert into hishop_upload_resource(merchant_id,file_name,access_url,file_size) values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, merchantId);
            ps.setString(2, fileName);
            ps.setString(3, accessUrl);
            ps.setLong(4, fileSize);
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(keys, ps, conn);
        }
    }

    public void bindUploadResource(int merchantId, String accessUrl, int productId) {
        if (blank(accessUrl) || productId <= 0) return;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("update hishop_upload_resource set product_id=? where merchant_id=? and access_url=? and product_id is null");
            ps.setInt(1, productId);
            ps.setInt(2, merchantId);
            ps.setString(3, accessUrl);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private int currentGrowth(Connection conn, int userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("select growth_value from hishopping_user where id=?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            close(rs, ps);
        }
    }

    private void updateProductRating(Connection conn, int productId) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("update hishopping_product set rating=(select cast(avg(cast(rating as decimal(5,2))) as decimal(5,2)) from hishop_product_review where product_id=?) where id=?");
            ps.setInt(1, productId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }

    private Shipment mapShipment(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setShipmentId(rs.getInt("shipment_id"));
        s.setOrderId(rs.getInt("order_id"));
        s.setMerchantId(rs.getInt("merchant_id"));
        s.setExpressCompany(rs.getString("express_company"));
        s.setTrackingNo(rs.getString("tracking_no"));
        s.setShipTime(String.valueOf(rs.getTimestamp("ship_time")));
        return s;
    }

    private AfterSale mapAfterSale(ResultSet rs) throws SQLException {
        AfterSale a = new AfterSale();
        a.setAfterSaleId(rs.getInt("after_sale_id"));
        a.setOrderId(rs.getInt("order_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setMerchantId(rs.getInt("merchant_id"));
        a.setProductId(rs.getInt("product_id"));
        a.setAfterSaleType(rs.getString("after_sale_type"));
        a.setReason(rs.getString("reason"));
        a.setRefundAmount(rs.getDouble("refund_amount"));
        a.setStatus(rs.getString("status"));
        a.setApplyTime(String.valueOf(rs.getTimestamp("apply_time")));
        a.setHandleOpinion(rs.getString("handle_opinion"));
        a.setHandleTime(rs.getTimestamp("handle_time") == null ? "" : String.valueOf(rs.getTimestamp("handle_time")));
        return a;
    }

    private ProductReview mapReview(ResultSet rs) throws SQLException {
        ProductReview r = new ProductReview();
        r.setReviewId(rs.getInt("review_id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setOrderItemId(getInt(rs, "order_item_id"));
        r.setProductId(rs.getInt("product_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setUsername(getString(rs, "username"));
        r.setUserAvatar(getString(rs, "avatar_url"));
        r.setRating(rs.getInt("rating"));
        r.setContent(rs.getString("content"));
        r.setStatus(getString(rs, "status"));
        r.setLikeCount(getInt(rs, "like_count"));
        r.setReplyCount(getInt(rs, "reply_count"));
        r.setLiked(getInt(rs, "liked_count") > 0);
        r.setCreateTime(String.valueOf(rs.getTimestamp("create_time")));
        r.setUpdateTime(getString(rs, "update_time"));
        return r;
    }

    private ReviewReply mapReply(ResultSet rs) throws SQLException {
        ReviewReply reply = new ReviewReply();
        reply.setReplyId(rs.getInt("reply_id"));
        reply.setReviewId(rs.getInt("review_id"));
        reply.setParentReplyId(getInt(rs, "parent_reply_id"));
        reply.setUserType(rs.getString("user_type"));
        reply.setUserId(rs.getInt("user_id"));
        reply.setUserName(rs.getString("user_name"));
        reply.setUserAvatar(rs.getString("user_avatar"));
        reply.setContent(rs.getString("content"));
        reply.setStatus(rs.getString("status"));
        reply.setCreateTime(String.valueOf(rs.getTimestamp("create_time")));
        return reply;
    }

    private GrowthLog mapGrowthLog(ResultSet rs) throws SQLException {
        GrowthLog g = new GrowthLog();
        g.setLogId(rs.getInt("log_id"));
        g.setUserId(rs.getInt("user_id"));
        g.setGrowthDelta(rs.getInt("growth_delta"));
        g.setPointsDelta(rs.getInt("points_delta"));
        g.setSourceType(rs.getString("source_type"));
        g.setSourceId(rs.getInt("source_id"));
        g.setRemark(rs.getString("remark"));
        g.setCreateTime(String.valueOf(rs.getTimestamp("create_time")));
        return g;
    }

    private AdminOperationLog mapAdminLog(ResultSet rs) throws SQLException {
        AdminOperationLog log = new AdminOperationLog();
        log.setLogId(rs.getInt("log_id"));
        log.setAdminId(rs.getInt("admin_id"));
        log.setOperationType(rs.getString("operation_type"));
        log.setTargetType(rs.getString("target_type"));
        log.setTargetId(rs.getInt("target_id"));
        log.setContent(rs.getString("content"));
        log.setOperationTime(String.valueOf(rs.getTimestamp("operation_time")));
        return log;
    }

    private boolean blank(String value) {
        return value == null || value.trim().length() == 0;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }

    private void close(ResultSet rs, Statement st) {
        try {
            if (rs != null) rs.close();
            if (st != null) st.close();
        } catch (SQLException ignored) {
        }
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
}
