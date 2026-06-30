package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import hishopping.entity.CouponIssueLog;
import hishopping.entity.CouponTemplate;
import hishopping.entity.UserCoupon;
import hishopping.util.DBUtil;

public class CouponDao {
    private static boolean schemaReady = false;
    private static final String NORMAL_STATUS = "\u6b63\u5e38";

    public CouponDao() {
        ensureSchema();
    }

    public List<CouponTemplate> templates() {
        return queryTemplates("select t.*, m.shop_name from hishop_coupon_template t left join hishop_merchant m on t.merchant_id=m.merchant_id order by t.coupon_id desc");
    }

    public List<CouponTemplate> platformTemplates() {
        return queryTemplates("select t.*, m.shop_name from hishop_coupon_template t left join hishop_merchant m on t.merchant_id=m.merchant_id where t.coupon_owner_type=N'PLATFORM' order by t.coupon_id desc");
    }

    public List<CouponTemplate> merchantTemplates(int merchantId) {
        return queryTemplates("select t.*, m.shop_name from hishop_coupon_template t left join hishop_merchant m on t.merchant_id=m.merchant_id where t.coupon_owner_type=N'MERCHANT' and t.merchant_id=" + merchantId + " order by t.coupon_id desc");
    }

    public CouponTemplate findTemplate(int id) {
        String sql = "select t.*, m.shop_name from hishop_coupon_template t left join hishop_merchant m on t.merchant_id=m.merchant_id where t.coupon_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapTemplate(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void saveTemplate(CouponTemplate c) {
        String sql = "insert into hishop_coupon_template(coupon_name,coupon_type,amount,discount_rate,min_amount,target_type,target_value,vip_level,total_quantity,per_user_limit,start_time,end_time,valid_days,is_new_user_coupon,is_vip_coupon,status,coupon_owner_type,merchant_id,stackable,home_title,home_subtitle,use_scope,description) values(?,?,?,?,?,?,?,?,?,?,sysdatetime(),dateadd(day,?,sysdatetime()),?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            fillTemplateParams(ps, c, false);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateTemplate(CouponTemplate c) {
        String sql = "update hishop_coupon_template set coupon_name=?, coupon_type=?, amount=?, discount_rate=?, min_amount=?, target_type=?, target_value=?, vip_level=?, total_quantity=?, per_user_limit=?, end_time=dateadd(day,?,sysdatetime()), valid_days=?, is_new_user_coupon=?, is_vip_coupon=?, status=?, coupon_owner_type=?, merchant_id=?, stackable=?, home_title=?, home_subtitle=?, use_scope=?, description=?, update_time=sysdatetime() where coupon_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            fillTemplateParams(ps, c, true);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private void fillTemplateParams(PreparedStatement ps, CouponTemplate c, boolean includeId) throws SQLException {
        ps.setString(1, c.getCouponName());
        ps.setString(2, c.getCouponType());
        ps.setDouble(3, c.getAmount());
        ps.setDouble(4, c.getDiscountRate());
        ps.setDouble(5, c.getMinAmount());
        ps.setString(6, c.getTargetType());
        ps.setString(7, c.getTargetValue());
        ps.setInt(8, c.getVipLevel());
        ps.setInt(9, c.getTotalQuantity());
        ps.setInt(10, c.getPerUserLimit());
        ps.setInt(11, c.getValidDays());
        ps.setInt(12, c.getValidDays());
        ps.setInt(13, c.isNewUserCoupon() ? 1 : 0);
        ps.setInt(14, c.isVipCoupon() ? 1 : 0);
        ps.setString(15, c.getStatus());
        ps.setString(16, empty(c.getCouponOwnerType()) ? "PLATFORM" : c.getCouponOwnerType());
        if (c.getMerchantId() > 0) ps.setInt(17, c.getMerchantId()); else ps.setNull(17, java.sql.Types.INTEGER);
        ps.setInt(18, c.isStackable() ? 1 : 0);
        ps.setString(19, c.getHomeTitle());
        ps.setString(20, c.getHomeSubtitle());
        ps.setString(21, empty(c.getUseScope()) ? "ALL" : c.getUseScope());
        ps.setString(22, c.getDescription());
        if (includeId) ps.setInt(23, c.getCouponId());
    }

    public void updateTemplateStatus(int id, String status) {
        exec("update hishop_coupon_template set status=?, update_time=sysdatetime() where coupon_id=?", status, id);
    }

    public void deleteTemplate(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("delete from hishop_coupon_template where coupon_id=? and not exists(select 1 from hishop_user_coupon where coupon_id=?)");
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public List<Integer> targetUsers(String type, String target) {
        return targetUsers(type, target, 0);
    }

    public List<Integer> targetUsers(String type, String target, int merchantId) {
        if ("USER_GROUP".equals(type) && target != null && target.indexOf(",") >= 0) {
            Set<Integer> merged = new LinkedHashSet<Integer>();
            String[] groups = target.split(",");
            for (String group : groups) {
                String value = group == null ? "" : group.trim();
                if (value.length() > 0) merged.addAll(targetUsers(type, value, merchantId));
            }
            return new ArrayList<Integer>(merged);
        }
        String sql;
        if ("USER".equals(type)) {
            sql = "select id from hishopping_user where status=? and id=?";
        } else if ("USERNAME".equals(type)) {
            sql = "select id from hishopping_user where status=? and (username like ? or phone like ? or account_id like ?)";
        } else if ("VIP_LEVEL".equals(type)) {
            sql = "select id from hishopping_user where status=? and vip_level=?";
        } else if ("USER_GROUP".equals(type) && "NEW".equals(target)) {
            sql = "select id from hishopping_user where status=? and create_time>=dateadd(day,-30,sysdatetime())";
        } else if ("USER_GROUP".equals(type) && "ORDERED".equals(target)) {
            sql = merchantId > 0
                ? "select distinct u.id from hishopping_user u join hishopping_order o on u.id=o.user_id join hishopping_order_item oi on o.id=oi.order_id join hishopping_product p on oi.product_id=p.id where u.status=? and p.merchant_id=?"
                : "select distinct u.id from hishopping_user u join hishopping_order o on u.id=o.user_id where u.status=?";
        } else if ("USER_GROUP".equals(type) && "NO_ORDER".equals(target)) {
            sql = "select u.id from hishopping_user u where u.status=? and not exists(select 1 from hishopping_order o where o.user_id=u.id)";
        } else if ("USER_GROUP".equals(type) && "CART".equals(target)) {
            sql = merchantId > 0
                ? "select distinct u.id from hishopping_user u join hishopping_cart_item c on u.id=c.user_id join hishopping_product p on c.product_id=p.id where u.status=? and p.merchant_id=?"
                : "select distinct u.id from hishopping_user u join hishopping_cart_item c on u.id=c.user_id where u.status=?";
        } else if ("USER_GROUP".equals(type) && "HIGH".equals(target)) {
            sql = "select u.id from hishopping_user u join hishopping_order o on u.id=o.user_id where u.status=? group by u.id having sum(o.total_amount)>=1000";
        } else if ("ALL".equals(type)) {
            sql = "select id from hishopping_user where status=?";
        } else {
            sql = "select id from hishopping_user where 1=0";
        }
        List<Integer> ids = new ArrayList<Integer>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            int index = 1;
            if (sql.indexOf("status=?") >= 0) ps.setString(index++, NORMAL_STATUS);
            if ("USER".equals(type) || "VIP_LEVEL".equals(type)) {
                ps.setInt(index++, parseInt(target));
            } else if ("USERNAME".equals(type)) {
                String keyword = "%" + (target == null ? "" : target.trim()) + "%";
                ps.setString(index++, keyword);
                ps.setString(index++, keyword);
                ps.setString(index++, keyword);
            }
            if (merchantId > 0 && (sql.indexOf("p.merchant_id=?") >= 0)) ps.setInt(index++, merchantId);
            rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public int receivedCount(int userId, int couponId) {
        String sql = "select count(1) from hishop_user_coupon where user_id=? and coupon_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, couponId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void issueToUser(CouponTemplate c, int userId, String batchNo) {
        String sql = "insert into hishop_user_coupon(coupon_id,user_id,coupon_name,coupon_type,amount,discount_rate,min_amount,vip_level,status,expire_time,issue_batch_no,coupon_owner_type,merchant_id,stackable,use_scope,description) values(?,?,?,?,?,?,?,?,N'UNUSED',dateadd(day,?,sysdatetime()),?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, c.getCouponId());
            ps.setInt(2, userId);
            ps.setString(3, c.getCouponName());
            ps.setString(4, c.getCouponType());
            ps.setDouble(5, c.getAmount());
            ps.setDouble(6, c.getDiscountRate());
            ps.setDouble(7, c.getMinAmount());
            ps.setInt(8, c.getVipLevel());
            ps.setInt(9, c.getValidDays());
            ps.setString(10, batchNo);
            ps.setString(11, empty(c.getCouponOwnerType()) ? "PLATFORM" : c.getCouponOwnerType());
            if (c.getMerchantId() > 0) ps.setInt(12, c.getMerchantId()); else ps.setNull(12, java.sql.Types.INTEGER);
            ps.setInt(13, c.isStackable() ? 1 : 0);
            ps.setString(14, empty(c.getUseScope()) ? "ALL" : c.getUseScope());
            ps.setString(15, c.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void logIssue(int couponId, String batchNo, String type, String target, int issueCount, int skipCount, int adminId, String remark) {
        String sql = "insert into hishop_coupon_issue_log(coupon_id,issue_batch_no,issue_type,target_value,issue_count,skip_count,admin_id,remark) values(?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, couponId);
            ps.setString(2, batchNo);
            ps.setString(3, type);
            ps.setString(4, target);
            ps.setInt(5, issueCount);
            ps.setInt(6, skipCount);
            ps.setInt(7, adminId);
            ps.setString(8, remark);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public List<UserCoupon> userCoupons(int userId) {
        String sql = "update hishop_user_coupon set status='EXPIRED' where user_id=? and status='UNUSED' and expire_time<sysdatetime(); select uc.*, m.shop_name from hishop_user_coupon uc left join hishop_merchant m on uc.merchant_id=m.merchant_id where uc.user_id=? order by uc.user_coupon_id desc";
        return queryUserCoupons(sql, userId, userId);
    }

    public List<UserCoupon> allUserCoupons() {
        String sql = "update hishop_user_coupon set status='EXPIRED' where status='UNUSED' and expire_time<sysdatetime(); select uc.*, m.shop_name from hishop_user_coupon uc left join hishop_merchant m on uc.merchant_id=m.merchant_id order by uc.user_id, uc.user_coupon_id desc";
        return queryUserCoupons(sql);
    }

    public List<UserCoupon> merchantUserCoupons(int merchantId) {
        String sql = "update hishop_user_coupon set status='EXPIRED' where status='UNUSED' and expire_time<sysdatetime(); select uc.*, m.shop_name from hishop_user_coupon uc left join hishop_merchant m on uc.merchant_id=m.merchant_id where uc.merchant_id=? order by uc.user_coupon_id desc";
        return queryUserCoupons(sql, merchantId);
    }

    private List<UserCoupon> queryUserCoupons(String sql, int... params) {
        List<UserCoupon> list = new ArrayList<UserCoupon>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) ps.setInt(i + 1, params[i]);
            ps.execute();
            while (ps.getMoreResults() || ps.getUpdateCount() != -1) {
                rs = ps.getResultSet();
                if (rs != null) break;
            }
            while (rs != null && rs.next()) list.add(mapUserCoupon(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void voidUserCoupon(int userCouponId, int userId) {
        String sql = "update hishop_user_coupon set status=N'EXPIRED' where user_coupon_id=? and user_id=? and status=N'UNUSED'";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userCouponId);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0) throw new RuntimeException("\u4ec5\u672a\u4f7f\u7528\u4f18\u60e0\u5238\u53ef\u4f5c\u5e9f\u3002");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void markUsed(int userCouponId, int userId, int orderId) {
        String sql = "update hishop_user_coupon set status=N'USED', use_time=sysdatetime(), order_id=? where user_coupon_id=? and user_id=? and status=N'UNUSED'";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            ps.setInt(2, userCouponId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public List<CouponIssueLog> logs() {
        String sql = "select l.*, t.coupon_name from hishop_coupon_issue_log l left join hishop_coupon_template t on l.coupon_id=t.coupon_id order by l.issue_log_id desc";
        List<CouponIssueLog> list = new ArrayList<CouponIssueLog>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                CouponIssueLog log = new CouponIssueLog();
                log.setIssueLogId(rs.getInt("issue_log_id"));
                log.setCouponId(rs.getInt("coupon_id"));
                log.setCouponName(rs.getString("coupon_name"));
                log.setIssueBatchNo(rs.getString("issue_batch_no"));
                log.setIssueType(rs.getString("issue_type"));
                log.setTargetValue(rs.getString("target_value"));
                log.setIssueCount(rs.getInt("issue_count"));
                log.setSkipCount(rs.getInt("skip_count"));
                log.setAdminId(rs.getInt("admin_id"));
                log.setIssueTime(String.valueOf(rs.getTimestamp("issue_time")));
                log.setRemark(rs.getString("remark"));
                list.add(log);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
    }

    private List<CouponTemplate> queryTemplates(String sql) {
        List<CouponTemplate> list = new ArrayList<CouponTemplate>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapTemplate(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
    }

    private void exec(String sql, String text, int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, text);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishop_coupon_template', N'U') is null create table dbo.hishop_coupon_template (coupon_id int identity(1,1) primary key, coupon_name nvarchar(100) not null, coupon_type nvarchar(20) not null, amount decimal(10,2) not null default 0, discount_rate decimal(5,2) not null default 1, min_amount decimal(10,2) not null default 0, target_type nvarchar(30) null, target_value nvarchar(100) null, vip_level int null, total_quantity int not null default 0, per_user_limit int not null default 1, start_time datetime2 not null default sysdatetime(), end_time datetime2 not null default dateadd(day,30,sysdatetime()), valid_days int not null default 30, is_new_user_coupon bit not null default 0, is_vip_coupon bit not null default 0, status nvarchar(20) not null default N'ENABLED', create_time datetime2 not null default sysdatetime(), update_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if object_id(N'dbo.hishop_user_coupon', N'U') is null create table dbo.hishop_user_coupon (user_coupon_id int identity(1,1) primary key, coupon_id int not null, user_id int not null, coupon_name nvarchar(100) not null, coupon_type nvarchar(20) not null, amount decimal(10,2) not null default 0, discount_rate decimal(5,2) not null default 1, min_amount decimal(10,2) not null default 0, vip_level int null, status nvarchar(20) not null default N'UNUSED', receive_time datetime2 not null default sysdatetime(), expire_time datetime2 null, use_time datetime2 null, order_id int null, issue_batch_no nvarchar(50) null)");
            st.executeUpdate("if object_id(N'dbo.hishop_coupon_issue_log', N'U') is null create table dbo.hishop_coupon_issue_log (issue_log_id int identity(1,1) primary key, coupon_id int not null, issue_batch_no nvarchar(50) not null, issue_type nvarchar(30) not null, target_value nvarchar(100) null, issue_count int not null default 0, skip_count int not null default 0, admin_id int null, issue_time datetime2 not null default sysdatetime(), remark nvarchar(500) null)");
            st.executeUpdate("if col_length('dbo.hishop_coupon_template', 'coupon_owner_type') is null alter table dbo.hishop_coupon_template add coupon_owner_type nvarchar(20) not null constraint DF_hishop_coupon_template_owner default N'PLATFORM'");
            st.executeUpdate("if col_length('dbo.hishop_coupon_template', 'merchant_id') is null alter table dbo.hishop_coupon_template add merchant_id int null");
            st.executeUpdate("if col_length('dbo.hishop_coupon_template', 'stackable') is null alter table dbo.hishop_coupon_template add stackable bit not null constraint DF_hishop_coupon_template_stackable default 0");
            st.executeUpdate("if col_length('dbo.hishop_coupon_template', 'home_title') is null alter table dbo.hishop_coupon_template add home_title nvarchar(100) null");
            st.executeUpdate("if col_length('dbo.hishop_coupon_template', 'home_subtitle') is null alter table dbo.hishop_coupon_template add home_subtitle nvarchar(200) null");
            st.executeUpdate("if col_length('dbo.hishop_coupon_template', 'use_scope') is null alter table dbo.hishop_coupon_template add use_scope nvarchar(20) not null constraint DF_hishop_coupon_template_scope default N'ALL'");
            st.executeUpdate("if col_length('dbo.hishop_coupon_template', 'description') is null alter table dbo.hishop_coupon_template add description nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishop_user_coupon', 'coupon_owner_type') is null alter table dbo.hishop_user_coupon add coupon_owner_type nvarchar(20) not null constraint DF_hishop_user_coupon_owner default N'PLATFORM'");
            st.executeUpdate("if col_length('dbo.hishop_user_coupon', 'merchant_id') is null alter table dbo.hishop_user_coupon add merchant_id int null");
            st.executeUpdate("if col_length('dbo.hishop_user_coupon', 'stackable') is null alter table dbo.hishop_user_coupon add stackable bit not null constraint DF_hishop_user_coupon_stackable default 0");
            st.executeUpdate("if col_length('dbo.hishop_user_coupon', 'use_scope') is null alter table dbo.hishop_user_coupon add use_scope nvarchar(20) not null constraint DF_hishop_user_coupon_scope default N'ALL'");
            st.executeUpdate("if col_length('dbo.hishop_user_coupon', 'description') is null alter table dbo.hishop_user_coupon add description nvarchar(500) null");
            st.executeUpdate("if not exists(select 1 from hishop_coupon_template where is_new_user_coupon=1) insert into hishop_coupon_template(coupon_name,coupon_type,amount,discount_rate,min_amount,target_type,target_value,vip_level,total_quantity,per_user_limit,end_time,valid_days,is_new_user_coupon,is_vip_coupon,status,coupon_owner_type,stackable,home_title,home_subtitle,use_scope,description) values(N'新人专享券',N'NEW_USER',5,1,39,N'NEW_USER',N'VIP1',1,9999,1,dateadd(day,365,sysdatetime()),7,1,0,N'ENABLED',N'PLATFORM',0,N'新人专享',N'满39减5，注册即可领取',N'ALL',N'新人专享平台券')");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    private CouponTemplate mapTemplate(ResultSet rs) throws SQLException {
        CouponTemplate c = new CouponTemplate();
        c.setCouponId(rs.getInt("coupon_id"));
        c.setCouponName(rs.getString("coupon_name"));
        c.setCouponType(rs.getString("coupon_type"));
        c.setAmount(rs.getDouble("amount"));
        c.setDiscountRate(rs.getDouble("discount_rate"));
        c.setMinAmount(rs.getDouble("min_amount"));
        c.setTargetType(rs.getString("target_type"));
        c.setTargetValue(rs.getString("target_value"));
        c.setVipLevel(rs.getInt("vip_level"));
        c.setTotalQuantity(rs.getInt("total_quantity"));
        c.setPerUserLimit(rs.getInt("per_user_limit"));
        c.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
        c.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
        c.setValidDays(rs.getInt("valid_days"));
        c.setNewUserCoupon(rs.getBoolean("is_new_user_coupon"));
        c.setVipCoupon(rs.getBoolean("is_vip_coupon"));
        c.setStatus(rs.getString("status"));
        c.setCreateTime(String.valueOf(rs.getTimestamp("create_time")));
        c.setCouponOwnerType(rs.getString("coupon_owner_type"));
        c.setMerchantId(rs.getInt("merchant_id"));
        c.setShopName(rs.getString("shop_name"));
        c.setStackable(rs.getBoolean("stackable"));
        c.setHomeTitle(rs.getString("home_title"));
        c.setHomeSubtitle(rs.getString("home_subtitle"));
        c.setUseScope(rs.getString("use_scope"));
        c.setDescription(rs.getString("description"));
        return c;
    }

    private UserCoupon mapUserCoupon(ResultSet rs) throws SQLException {
        UserCoupon c = new UserCoupon();
        c.setUserCouponId(rs.getInt("user_coupon_id"));
        c.setCouponId(rs.getInt("coupon_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setCouponName(rs.getString("coupon_name"));
        c.setCouponType(rs.getString("coupon_type"));
        c.setAmount(rs.getDouble("amount"));
        c.setDiscountRate(rs.getDouble("discount_rate"));
        c.setMinAmount(rs.getDouble("min_amount"));
        c.setVipLevel(rs.getInt("vip_level"));
        c.setStatus(rs.getString("status"));
        c.setReceiveTime(String.valueOf(rs.getTimestamp("receive_time")));
        c.setExpireTime(String.valueOf(rs.getTimestamp("expire_time")));
        c.setUseTime(rs.getTimestamp("use_time") == null ? "" : String.valueOf(rs.getTimestamp("use_time")));
        c.setOrderId(rs.getInt("order_id"));
        c.setIssueBatchNo(rs.getString("issue_batch_no"));
        c.setCouponOwnerType(rs.getString("coupon_owner_type"));
        c.setMerchantId(rs.getInt("merchant_id"));
        c.setShopName(rs.getString("shop_name"));
        c.setStackable(rs.getBoolean("stackable"));
        c.setUseScope(rs.getString("use_scope"));
        c.setDescription(rs.getString("description"));
        return c;
    }

    private boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
