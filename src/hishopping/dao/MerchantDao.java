package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.Merchant;
import hishopping.util.DBUtil;

public class MerchantDao {
    private static boolean schemaReady = false;

    public MerchantDao() {
        ensureSchema();
    }

    public boolean existsCode(String code) {
        String sql = "select count(1) from hishop_merchant where merchant_code=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public Merchant save(Merchant merchant) {
        String sql = "insert into hishop_merchant(merchant_code,merchant_name,password,register_password_demo,contact_name,contact_phone,email,shop_name,shop_desc,business_category,business_address,status) values(?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, merchant.getMerchantCode());
            ps.setString(2, merchant.getMerchantName());
            ps.setString(3, merchant.getPassword());
            ps.setString(4, merchant.getRegisterPasswordDemo());
            ps.setString(5, merchant.getContactName());
            ps.setString(6, merchant.getContactPhone());
            ps.setString(7, merchant.getEmail());
            ps.setString(8, merchant.getShopName());
            ps.setString(9, merchant.getShopDesc());
            ps.setString(10, merchant.getBusinessCategory());
            ps.setString(11, merchant.getBusinessAddress());
            ps.setString(12, "PENDING");
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) merchant.setMerchantId(keys.getInt(1));
            merchant.setStatus("PENDING");
            return merchant;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(keys, ps, conn);
        }
    }

    public Merchant findByLogin(String account, String password) {
        String sql = "select * from hishop_merchant where (merchant_code=? or merchant_name=?) and password=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, account);
            ps.setString(2, account);
            ps.setString(3, password);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public Merchant findById(int id) {
        String sql = "select * from hishop_merchant where merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Merchant> findAll() {
        String sql = "select * from hishop_merchant order by merchant_id desc";
        List<Merchant> list = new ArrayList<Merchant>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
    }

    public List<Merchant> findByContact(String contact) {
        String sql = "select * from hishop_merchant where email=? or contact_phone=? order by merchant_id desc";
        List<Merchant> list = new ArrayList<Merchant>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, contact);
            ps.setString(2, contact);
            rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void updateStatus(int merchantId, String status, String opinion, int adminId) {
        String sql = "update hishop_merchant set status=?, reject_reason=?, review_admin_id=?, review_time=sysdatetime(), update_time=sysdatetime() where merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement productPs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, opinion);
            ps.setInt(3, adminId);
            ps.setInt(4, merchantId);
            ps.executeUpdate();
            if ("DISABLED".equals(status) || "REJECTED".equals(status)) {
                productPs = conn.prepareStatement("update hishopping_product set sale_status=N'OFF_SALE' where merchant_id=?");
                productPs.setInt(1, merchantId);
                productPs.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException(e);
        } finally {
            try {
                if (productPs != null) productPs.close();
            } catch (SQLException ignored) {
            }
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void applyPunishment(int merchantId, String status, String reason, Integer durationDays, boolean offSaleProducts) {
        String sql = durationDays == null
            ? "update hishop_merchant set status=?, reject_reason=?, punish_reason=?, punish_start_time=sysdatetime(), punish_end_time=null, update_time=sysdatetime() where merchant_id=?"
            : "update hishop_merchant set status=?, reject_reason=?, punish_reason=?, punish_start_time=sysdatetime(), punish_end_time=dateadd(day, ?, sysdatetime()), update_time=sysdatetime() where merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement productPs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, reason);
            ps.setString(3, reason);
            if (durationDays == null) {
                ps.setInt(4, merchantId);
            } else {
                ps.setInt(4, durationDays.intValue());
                ps.setInt(5, merchantId);
            }
            if (ps.executeUpdate() == 0) throw new RuntimeException("商家不存在。");
            if (offSaleProducts) {
                productPs = conn.prepareStatement("update hishopping_product set sale_status=N'OFF_SALE' where merchant_id=?");
                productPs.setInt(1, merchantId);
                productPs.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException(e);
        } finally {
            try { if (productPs != null) productPs.close(); } catch (SQLException ignored) {}
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void restorePunishmentIfExpired(int merchantId) {
        String sql = "update hishop_merchant set status=N'APPROVED', reject_reason=null, punish_reason=null, punish_start_time=null, punish_end_time=null, update_time=sysdatetime() where merchant_id=? and status in (N'FROZEN',N'DISABLED') and punish_end_time is not null and punish_end_time<=sysdatetime()";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, merchantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateProfile(Merchant merchant) {
        String sql = "update hishop_merchant set merchant_name=?, password=?, register_password_demo=?, contact_name=?, contact_phone=?, email=?, shop_name=?, shop_desc=?, business_category=?, business_address=?, update_time=sysdatetime() where merchant_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, merchant.getMerchantName());
            ps.setString(2, merchant.getPassword());
            ps.setString(3, merchant.getPassword());
            ps.setString(4, merchant.getContactName());
            ps.setString(5, merchant.getContactPhone());
            ps.setString(6, merchant.getEmail());
            ps.setString(7, merchant.getShopName());
            ps.setString(8, merchant.getShopDesc());
            ps.setString(9, merchant.getBusinessCategory());
            ps.setString(10, merchant.getBusinessAddress());
            ps.setInt(11, merchant.getMerchantId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void rawUpdate(String sql) {
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    public void updateAvatar(int merchantId, String avatarUrl) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("update hishop_merchant set avatar_url=?, update_time=sysdatetime() where merchant_id=?");
            ps.setString(1, avatarUrl);
            ps.setInt(2, merchantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void requestCancel(int merchantId) {
        String sql = "update hishop_merchant set status=N'CANCEL_PENDING', cancel_request_time=sysdatetime(), cancel_deadline_time=dateadd(day,7,sysdatetime()), cancel_cancel_time=null, update_time=sysdatetime() where merchant_id=? and status=N'APPROVED'";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, merchantId);
            if (ps.executeUpdate() == 0) throw new RuntimeException("当前商家状态不允许发起注销。");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void cancelPendingCancel(int merchantId) {
        String sql = "update hishop_merchant set status=N'APPROVED', cancel_cancel_time=sysdatetime(), cancel_request_time=null, cancel_deadline_time=null, update_time=sysdatetime() where merchant_id=? and status=N'CANCEL_PENDING'";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, merchantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void finishExpiredCancel(int merchantId) {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement productPs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("update hishop_merchant set status=N'CANCELLED', update_time=sysdatetime() where merchant_id=? and status=N'CANCEL_PENDING'");
            ps.setInt(1, merchantId);
            ps.executeUpdate();
            productPs = conn.prepareStatement("update hishopping_product set sale_status=N'OFF_SALE' where merchant_id=?");
            productPs.setInt(1, merchantId);
            productPs.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException(e);
        } finally {
            try { if (productPs != null) productPs.close(); } catch (SQLException ignored) {}
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
            st.executeUpdate("if object_id(N'dbo.hishop_merchant', N'U') is null create table dbo.hishop_merchant (merchant_id int identity(1,1) primary key, merchant_code nvarchar(8) not null unique, merchant_name nvarchar(80) not null, password nvarchar(80) not null, register_password_demo nvarchar(80) null, contact_name nvarchar(50) not null, contact_phone nvarchar(30) not null, email nvarchar(100) null, shop_name nvarchar(100) not null, shop_desc nvarchar(500) null, business_category nvarchar(100) null, business_address nvarchar(200) null, status nvarchar(20) not null default N'PENDING', reject_reason nvarchar(500) null, create_time datetime2 not null default sysdatetime(), update_time datetime2 not null default sysdatetime(), review_time datetime2 null, review_admin_id int null)");
            st.executeUpdate("if object_id(N'dbo.hishop_merchant_audit_log', N'U') is null create table dbo.hishop_merchant_audit_log (log_id int identity(1,1) primary key, merchant_id int not null, before_status nvarchar(20) null, after_status nvarchar(20) not null, admin_id int null, audit_opinion nvarchar(500) null, create_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if col_length('dbo.hishopping_product','merchant_id') is null alter table dbo.hishopping_product add merchant_id int null");
            st.executeUpdate("if col_length('dbo.hishopping_product','sale_status') is null alter table dbo.hishopping_product add sale_status nvarchar(20) not null constraint DF_hishopping_product_sale_status default N'ON_SALE'");
            st.executeUpdate("if col_length('dbo.hishopping_product','audit_status') is null alter table dbo.hishopping_product add audit_status nvarchar(20) not null constraint DF_hishopping_product_audit_status default N'APPROVED'");
            st.executeUpdate("if col_length('dbo.hishopping_product','audit_opinion') is null alter table dbo.hishopping_product add audit_opinion nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishopping_product','submit_time') is null alter table dbo.hishopping_product add submit_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishopping_product','review_time') is null alter table dbo.hishopping_product add review_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishopping_product','review_admin_id') is null alter table dbo.hishopping_product add review_admin_id int null");
            st.executeUpdate("if col_length('dbo.hishop_merchant','avatar_url') is null alter table dbo.hishop_merchant add avatar_url nvarchar(300) null");
            st.executeUpdate("if col_length('dbo.hishop_merchant','punish_reason') is null alter table dbo.hishop_merchant add punish_reason nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishop_merchant','punish_start_time') is null alter table dbo.hishop_merchant add punish_start_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishop_merchant','punish_end_time') is null alter table dbo.hishop_merchant add punish_end_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishop_merchant','cancel_request_time') is null alter table dbo.hishop_merchant add cancel_request_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishop_merchant','cancel_deadline_time') is null alter table dbo.hishop_merchant add cancel_deadline_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishop_merchant','cancel_cancel_time') is null alter table dbo.hishop_merchant add cancel_cancel_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishopping_product','sku_options') is null alter table dbo.hishopping_product add sku_options nvarchar(max) null");
            st.executeUpdate("if col_length('dbo.hishopping_product','sku_attrs') is null alter table dbo.hishopping_product add sku_attrs nvarchar(max) null");
            st.executeUpdate("if col_length('dbo.hishopping_cart_item','selected_color') is null alter table dbo.hishopping_cart_item add selected_color nvarchar(50) null");
            st.executeUpdate("if col_length('dbo.hishopping_cart_item','selected_spec') is null alter table dbo.hishopping_cart_item add selected_spec nvarchar(50) null");
            st.executeUpdate("if col_length('dbo.hishopping_cart_item','sku_id') is null alter table dbo.hishopping_cart_item add sku_id nvarchar(120) not null constraint DF_hishopping_cart_sku_id default N'DEFAULT'");
            st.executeUpdate("if col_length('dbo.hishopping_cart_item','sku_text') is null alter table dbo.hishopping_cart_item add sku_text nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishopping_cart_item','sku_price') is null alter table dbo.hishopping_cart_item add sku_price decimal(10,2) not null constraint DF_hishopping_cart_sku_price default 0");
            st.executeUpdate("if exists (select 1 from sys.key_constraints where name=N'UQ_hishopping_cart_user_product' and parent_object_id=object_id(N'dbo.hishopping_cart_item')) alter table dbo.hishopping_cart_item drop constraint UQ_hishopping_cart_user_product");
            st.executeUpdate("if not exists (select 1 from sys.key_constraints where name=N'UQ_hishopping_cart_user_product_sku' and parent_object_id=object_id(N'dbo.hishopping_cart_item')) alter table dbo.hishopping_cart_item add constraint UQ_hishopping_cart_user_product_sku unique(user_id, product_id, sku_id)");
            st.executeUpdate("if col_length('dbo.hishopping_order','batch_no') is null alter table dbo.hishopping_order add batch_no nvarchar(40) null");
            st.executeUpdate("if col_length('dbo.hishopping_order','merchant_id') is null alter table dbo.hishopping_order add merchant_id int null");
            st.executeUpdate("if col_length('dbo.hishopping_order','shop_name') is null alter table dbo.hishopping_order add shop_name nvarchar(100) null");
            st.executeUpdate("if col_length('dbo.hishopping_order','goods_amount') is null alter table dbo.hishopping_order add goods_amount decimal(10,2) not null constraint DF_hishopping_order_goods_amount default 0");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','selected_color') is null alter table dbo.hishopping_order_item add selected_color nvarchar(50) null");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','selected_spec') is null alter table dbo.hishopping_order_item add selected_spec nvarchar(50) null");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','sku_id') is null alter table dbo.hishopping_order_item add sku_id nvarchar(120) null");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','sku_text') is null alter table dbo.hishopping_order_item add sku_text nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','snapshot_name') is null alter table dbo.hishopping_order_item add snapshot_name nvarchar(100) null");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','snapshot_image') is null alter table dbo.hishopping_order_item add snapshot_image nvarchar(300) null");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','item_unit_price') is null alter table dbo.hishopping_order_item add item_unit_price decimal(10,2) null");
            st.executeUpdate("if col_length('dbo.hishopping_order_item','item_subtotal') is null alter table dbo.hishopping_order_item add item_subtotal decimal(10,2) null");
            st.executeUpdate("if object_id(N'dbo.hishop_product_audit_log', N'U') is null create table dbo.hishop_product_audit_log (log_id int identity(1,1) primary key, product_id int not null, merchant_id int null, before_audit_status nvarchar(20) null, after_audit_status nvarchar(20) not null, before_sale_status nvarchar(20) null, after_sale_status nvarchar(20) not null, admin_id int null, audit_opinion nvarchar(500) null, create_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_order_user_time' and object_id=object_id(N'dbo.hishopping_order')) create index IX_hishopping_order_user_time on dbo.hishopping_order(user_id, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_order_merchant_status' and object_id=object_id(N'dbo.hishopping_order')) create index IX_hishopping_order_merchant_status on dbo.hishopping_order(merchant_id, status, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_order_batch' and object_id=object_id(N'dbo.hishopping_order')) create index IX_hishopping_order_batch on dbo.hishopping_order(batch_no)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_order_item_order' and object_id=object_id(N'dbo.hishopping_order_item')) create index IX_hishopping_order_item_order on dbo.hishopping_order_item(order_id)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_cart_user' and object_id=object_id(N'dbo.hishopping_cart_item')) create index IX_hishopping_cart_user on dbo.hishopping_cart_item(user_id)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_product_merchant_status' and object_id=object_id(N'dbo.hishopping_product')) create index IX_hishopping_product_merchant_status on dbo.hishopping_product(merchant_id, audit_status, sale_status)");
            st.executeUpdate("if not exists(select 1 from dbo.hishop_merchant where merchant_code=N'0798682') insert into dbo.hishop_merchant(merchant_code,merchant_name,password,register_password_demo,contact_name,contact_phone,email,shop_name,shop_desc,business_category,business_address,status,review_time,review_admin_id) values(N'0798682',N'课程演示商家',N'123456',N'123456',N'演示联系人',N'13900000001',N'merchant-demo@hishopping.com',N'课程演示店铺',N'课程验收演示店铺',N'综合类目',N'江苏省南京市软件大道88号',N'APPROVED',sysdatetime(),1)");
            st.executeUpdate("update dbo.hishopping_product set merchant_id=(select top 1 merchant_id from dbo.hishop_merchant where merchant_code=N'0798682'), sale_status=N'ON_SALE', audit_status=N'APPROVED' where merchant_id is null");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    private Merchant map(ResultSet rs) throws SQLException {
        Merchant m = new Merchant();
        m.setMerchantId(rs.getInt("merchant_id"));
        m.setMerchantCode(rs.getString("merchant_code"));
        m.setMerchantName(rs.getString("merchant_name"));
        m.setPassword(rs.getString("password"));
        m.setRegisterPasswordDemo(rs.getString("register_password_demo"));
        m.setContactName(rs.getString("contact_name"));
        m.setContactPhone(rs.getString("contact_phone"));
        m.setEmail(rs.getString("email"));
        m.setShopName(rs.getString("shop_name"));
        m.setShopDesc(rs.getString("shop_desc"));
        m.setBusinessCategory(rs.getString("business_category"));
        m.setBusinessAddress(rs.getString("business_address"));
        m.setStatus(rs.getString("status"));
        m.setAvatarUrl(rs.getString("avatar_url"));
        m.setRejectReason(rs.getString("reject_reason"));
        m.setPunishReason(getString(rs, "punish_reason"));
        m.setPunishStartTime(time(rs, "punish_start_time"));
        m.setPunishEndTime(time(rs, "punish_end_time"));
        m.setCancelRequestTime(time(rs, "cancel_request_time"));
        m.setCancelDeadlineTime(time(rs, "cancel_deadline_time"));
        m.setCancelCancelTime(time(rs, "cancel_cancel_time"));
        m.setCreateTime(String.valueOf(rs.getTimestamp("create_time")));
        m.setUpdateTime(String.valueOf(rs.getTimestamp("update_time")));
        m.setReviewTime(rs.getTimestamp("review_time") == null ? "" : String.valueOf(rs.getTimestamp("review_time")));
        m.setReviewAdminId(rs.getInt("review_admin_id"));
        return m;
    }

    private String getString(ResultSet rs, String column) {
        try {
            Object value = rs.getObject(column);
            return value == null ? "" : String.valueOf(value);
        } catch (SQLException e) {
            return "";
        }
    }

    private String time(ResultSet rs, String column) {
        try {
            java.sql.Timestamp value = rs.getTimestamp(column);
            return value == null ? "" : String.valueOf(value);
        } catch (SQLException e) {
            return "";
        }
    }
}
