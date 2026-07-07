package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.Admin;
import hishopping.entity.User;
import hishopping.util.DBUtil;

public class UserDao {
    private static final String NORMAL_STATUS = "\u6b63\u5e38";
    private static final String DELETED_STATUS = "\u5df2\u5220\u9664";
    private static final String USER_COLUMNS = "id, account_id, username, email, phone, password, role, points, vip_level, growth_value, status, avatar_url, punish_reason, punish_start_time, punish_end_time, create_time";
    private static boolean accountIdChecked = false;

    public static int calculateVipLevel(int growthValue) {
        if (growthValue >= 24000) return 10;
        if (growthValue >= 16000) return 9;
        if (growthValue >= 11000) return 8;
        if (growthValue >= 7000) return 7;
        if (growthValue >= 4000) return 6;
        if (growthValue >= 2000) return 5;
        if (growthValue >= 1000) return 4;
        if (growthValue >= 500) return 3;
        if (growthValue >= 200) return 2;
        return 1;
    }

    public static double pointRateForVipLevel(int vipLevel) {
        if (vipLevel >= 10) return 2.0;
        if (vipLevel >= 9) return 1.8;
        if (vipLevel >= 8) return 1.6;
        if (vipLevel >= 7) return 1.5;
        if (vipLevel >= 6) return 1.4;
        if (vipLevel >= 5) return 1.3;
        if (vipLevel >= 4) return 1.2;
        if (vipLevel >= 3) return 1.1;
        return 1.0;
    }

    public User findByAccountIdAndPassword(String accountId, String password) {
        ensureAccountIdReady();
        String sql = "select " + USER_COLUMNS + " from hishopping_user where account_id=? and password=? and status=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            ps.setString(2, password);
            ps.setString(3, NORMAL_STATUS);
            rs = ps.executeQuery();
            return rs.next() ? mapUser(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public User findByLoginAndPassword(String account, String password) {
        ensureAccountIdReady();
        String sql = "select " + USER_COLUMNS + " from hishopping_user where (account_id=? or email=? or (phone is not null and phone<>'' and phone=?)) and password=? and status=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, account);
            ps.setString(2, account);
            ps.setString(3, account);
            ps.setString(4, password);
            ps.setString(5, NORMAL_STATUS);
            rs = ps.executeQuery();
            return rs.next() ? mapUser(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public User findAnyByLoginAndPassword(String account, String password) {
        ensureAccountIdReady();
        String sql = "select " + USER_COLUMNS + " from hishopping_user where (account_id=? or email=? or (phone is not null and phone<>'' and phone=?)) and password=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, account);
            ps.setString(2, account);
            ps.setString(3, account);
            ps.setString(4, password);
            rs = ps.executeQuery();
            return rs.next() ? mapUser(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public User findById(int id) {
        ensureAccountIdReady();
        String sql = "select " + USER_COLUMNS + " from hishopping_user where id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapUser(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean existsEmail(String email) {
        ensureAccountIdReady();
        String sql = "select count(1) from hishopping_user where email=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean existsPhone(String phone) {
        ensureAccountIdReady();
        String sql = "select count(1) from hishopping_user where phone is not null and phone<>'' and phone=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, phone);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean existsEmailExceptUser(String email, int userId) {
        ensureAccountIdReady();
        String sql = "select count(1) from hishopping_user where email=? and id<>?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean existsPhoneExceptUser(String phone, int userId) {
        ensureAccountIdReady();
        String sql = "select count(1) from hishopping_user where phone is not null and phone<>'' and phone=? and id<>?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, phone);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean existsAccountId(String accountId) {
        ensureAccountIdReady();
        String sql = "select count(1) from hishopping_user where account_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<User> findAllUsers() {
        ensureAccountIdReady();
        String sql = "select " + USER_COLUMNS + " from hishopping_user where isnull(status,N'')<>? order by id";
        List<User> list = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, DELETED_STATUS);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapUser(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void updateUser(User user) {
        updateUser(user, null);
    }

    public void updateUser(User user, String newPassword) {
        ensureAccountIdReady();
        int growthValue = Math.max(0, user.getGrowthValue());
        int vipLevel = calculateVipLevel(growthValue);
        boolean changePassword = newPassword != null && newPassword.trim().length() > 0;
        String sql = changePassword
            ? "update hishopping_user set username=?, email=?, phone=?, points=?, growth_value=?, vip_level=?, status=?, password=? where id=?"
            : "update hishopping_user set username=?, email=?, phone=?, points=?, growth_value=?, vip_level=?, status=? where id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setInt(4, user.getPoints());
            ps.setInt(5, growthValue);
            ps.setInt(6, vipLevel);
            ps.setString(7, user.getStatus());
            if (changePassword) {
                ps.setString(8, newPassword.trim());
                ps.setInt(9, user.getId());
            } else {
                ps.setInt(8, user.getId());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void markDeleted(int userId) {
        ensureAccountIdReady();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("update hishopping_user set status=? where id=? and role=N'user'");
            ps.setString(1, DELETED_STATUS);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("\u7528\u6237\u4e0d\u5b58\u5728\u6216\u4e0d\u5141\u8bb8\u5220\u9664\u3002");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateStatus(int userId, String status) {
        ensureAccountIdReady();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("update hishopping_user set status=? where id=? and role=N'user'");
            ps.setString(1, status);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("\u7528\u6237\u4e0d\u5b58\u5728\u6216\u4e0d\u5141\u8bb8\u64cd\u4f5c\u3002");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void applyPunishment(int userId, String status, String reason, Integer durationDays) {
        ensureAccountIdReady();
        String sql = durationDays == null
            ? "update hishopping_user set status=?, punish_reason=?, punish_start_time=sysdatetime(), punish_end_time=null where id=? and role=N'user'"
            : "update hishopping_user set status=?, punish_reason=?, punish_start_time=sysdatetime(), punish_end_time=dateadd(day, ?, sysdatetime()) where id=? and role=N'user'";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, reason);
            if (durationDays == null) {
                ps.setInt(3, userId);
            } else {
                ps.setInt(3, durationDays.intValue());
                ps.setInt(4, userId);
            }
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("用户不存在或不允许处罚。");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void restorePunishmentIfExpired(int userId) {
        ensureAccountIdReady();
        String sql = "update hishopping_user set status=N'正常', punish_reason=null, punish_start_time=null, punish_end_time=null where id=? and status in (N'冻结',N'停用') and punish_end_time is not null and punish_end_time<=sysdatetime()";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public User save(User user) {
        ensureAccountIdReady();
        String sql = "insert into hishopping_user(account_id,username,email,phone,password,role,points,vip_level,growth_value,status) values(?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getAccountId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getPassword());
            ps.setString(6, "user");
            ps.setInt(7, 0);
            ps.setInt(8, 1);
            ps.setInt(9, 100);
            ps.setString(10, NORMAL_STATUS);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
            keys.close();
            user.setRole("user");
            user.setPoints(0);
            user.setVipLevel(1);
            user.setGrowthValue(100);
            user.setStatus(NORMAL_STATUS);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public User addGrowth(int userId, int addGrowth) {
        ensureAccountIdReady();
        if (userId <= 0 || addGrowth <= 0) {
            return findById(userId);
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(
                "update hishopping_user set growth_value=growth_value+?, points=points+?, vip_level=? where id=?"
            );
            User user = findById(userId);
            int newGrowthValue = (user == null ? 0 : user.getGrowthValue()) + addGrowth;
            ps.setInt(1, addGrowth);
            ps.setInt(2, addGrowth);
            ps.setInt(3, calculateVipLevel(newGrowthValue));
            ps.setInt(4, userId);
            ps.executeUpdate();
            return findById(userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateAvatar(int userId, String avatarUrl) {
        ensureAccountIdReady();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("update hishopping_user set avatar_url=? where id=?");
            ps.setString(1, avatarUrl);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public Admin findAdmin(String adminName, String password) {
        String sql = "select id, admin_name, password, real_name from hishopping_admin where admin_name=? and password=? and status=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, adminName);
            ps.setString(2, password);
            ps.setString(3, NORMAL_STATUS);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            Admin admin = new Admin();
            admin.setId(rs.getInt("id"));
            admin.setAdminName(rs.getString("admin_name"));
            admin.setPassword(rs.getString("password"));
            admin.setRealName(rs.getString("real_name"));
            return admin;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private static synchronized void ensureAccountIdReady() {
        if (accountIdChecked) {
            return;
        }
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if col_length('dbo.hishopping_user', 'account_id') is null alter table dbo.hishopping_user add account_id nvarchar(8) null");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'vip_level') is null alter table dbo.hishopping_user add vip_level int not null constraint DF_hishopping_user_vip_level default 1");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'growth_value') is null alter table dbo.hishopping_user add growth_value int not null constraint DF_hishopping_user_growth_value default 0");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'create_time') is null alter table dbo.hishopping_user add create_time datetime2 not null constraint DF_hishopping_user_create_time default sysdatetime()");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'avatar_url') is null alter table dbo.hishopping_user add avatar_url nvarchar(300) null");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'punish_reason') is null alter table dbo.hishopping_user add punish_reason nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'punish_start_time') is null alter table dbo.hishopping_user add punish_start_time datetime2 null");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'punish_end_time') is null alter table dbo.hishopping_user add punish_end_time datetime2 null");
            st.executeUpdate("update dbo.hishopping_user set account_id = right('100000' + cast(100000 + id as varchar(8)), case when id < 900000 then 6 else 8 end) where account_id is null or ltrim(rtrim(account_id)) = ''");
            st.executeUpdate("update dbo.hishopping_user set growth_value = case when growth_value > 0 then growth_value else points end");
            st.executeUpdate("update dbo.hishopping_user set vip_level = case when growth_value >= 24000 then 10 when growth_value >= 16000 then 9 when growth_value >= 11000 then 8 when growth_value >= 7000 then 7 when growth_value >= 4000 then 6 when growth_value >= 2000 then 5 when growth_value >= 1000 then 4 when growth_value >= 500 then 3 when growth_value >= 200 then 2 else 1 end");
            st.executeUpdate("if exists (select 1 from sys.stats where name = N'UQ_hishopping_user_account_id' and object_id = object_id(N'dbo.hishopping_user')) and not exists (select 1 from sys.indexes where name = N'UQ_hishopping_user_account_id' and object_id = object_id(N'dbo.hishopping_user')) drop statistics dbo.hishopping_user.UQ_hishopping_user_account_id");
            st.executeUpdate("if not exists (select 1 from sys.indexes where name = N'UQ_hishopping_user_account_id' and object_id = object_id(N'dbo.hishopping_user')) exec(N'create unique index UQ_hishopping_user_account_id on dbo.hishopping_user(account_id)')");
            st.executeUpdate("if exists (select 1 from sys.stats where name = N'UQ_hishopping_user_phone' and object_id = object_id(N'dbo.hishopping_user')) and not exists (select 1 from sys.indexes where name = N'UQ_hishopping_user_phone' and object_id = object_id(N'dbo.hishopping_user')) drop statistics dbo.hishopping_user.UQ_hishopping_user_phone");
            st.executeUpdate("if not exists (select 1 from sys.indexes where name = N'UQ_hishopping_user_phone' and object_id = object_id(N'dbo.hishopping_user')) and not exists (select phone from dbo.hishopping_user where phone is not null and ltrim(rtrim(phone)) <> N'' group by phone having count(1) > 1) exec(N'create unique index UQ_hishopping_user_phone on dbo.hishopping_user(phone) where phone is not null and phone <> N''''')");
            st.executeUpdate("if object_id(N'dbo.hishopping_vip_rule', N'U') is null create table dbo.hishopping_vip_rule (vip_level int not null primary key, vip_name nvarchar(20) not null, min_growth int not null, max_growth int null, discount decimal(4,2) not null, coupon_count int not null, point_rate decimal(4,2) not null, service_level nvarchar(50) not null default N'普通售后', benefit_desc nvarchar(500) not null)");
            st.executeUpdate("if col_length('dbo.hishopping_vip_rule', 'service_level') is null alter table dbo.hishopping_vip_rule add service_level nvarchar(50) not null constraint DF_hishopping_vip_rule_service_level default N'普通售后'");
            st.executeUpdate("alter table dbo.hishopping_vip_rule alter column point_rate decimal(4,2) not null");
            st.executeUpdate("alter table dbo.hishopping_vip_rule alter column benefit_desc nvarchar(500) not null");
            st.executeUpdate("merge dbo.hishopping_vip_rule as target using (values (1,N'普通会员',0,199,1.00,0,1.00,N'普通售后',N'基础购物服务'),(2,N'青铜会员',200,499,0.98,1,1.00,N'普通售后',N'商品9.8折，每月1张优惠券'),(3,N'白银会员',500,999,0.97,2,1.10,N'普通售后',N'商品9.7折，每月2张优惠券，积分1.1倍累计'),(4,N'黄金会员',1000,1999,0.95,3,1.20,N'优先售后',N'商品9.5折，每月3张优惠券，积分1.2倍累计，优先售后服务'),(5,N'铂金会员',2000,3999,0.93,4,1.30,N'优先售后',N'商品9.3折，每月4张优惠券，积分1.3倍累计，优先发货提醒'),(6,N'钻石会员',4000,6999,0.90,5,1.40,N'优先售后',N'商品9.0折，每月5张优惠券，积分1.4倍累计，生日专属礼券'),(7,N'星耀会员',7000,10999,0.88,6,1.50,N'高级售后',N'商品8.8折，每月6张优惠券，积分1.5倍累计，专属活动权益'),(8,N'黑金会员',11000,15999,0.85,8,1.60,N'高级售后',N'商品8.5折，每月8张优惠券，积分1.6倍累计，黑金会员专区，重点售后服务'),(9,N'至尊会员',16000,23999,0.82,10,1.80,N'专属售后',N'商品8.2折，每月10张优惠券，积分1.8倍累计，高级客服服务，新品优先体验'),(10,N'荣耀会员',24000,null,0.80,12,2.00,N'专属售后',N'商品8.0折，每月12张优惠券，积分2.0倍累计，全站尊享权益，专属客服服务，新品优先体验')) as source(vip_level,vip_name,min_growth,max_growth,discount,coupon_count,point_rate,service_level,benefit_desc) on target.vip_level=source.vip_level when matched then update set vip_name=source.vip_name,min_growth=source.min_growth,max_growth=source.max_growth,discount=source.discount,coupon_count=source.coupon_count,point_rate=source.point_rate,service_level=source.service_level,benefit_desc=source.benefit_desc when not matched then insert(vip_level,vip_name,min_growth,max_growth,discount,coupon_count,point_rate,service_level,benefit_desc) values(source.vip_level,source.vip_name,source.min_growth,source.max_growth,source.discount,source.coupon_count,source.point_rate,source.service_level,source.benefit_desc);");
            accountIdChecked = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setAccountId(rs.getString("account_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setPoints(rs.getInt("points"));
        user.setVipLevel(rs.getInt("vip_level"));
        user.setGrowthValue(rs.getInt("growth_value"));
        user.setStatus(rs.getString("status"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setPunishReason(getString(rs, "punish_reason"));
        user.setPunishStartTime(time(rs, "punish_start_time"));
        user.setPunishEndTime(time(rs, "punish_end_time"));
        user.setCreateTime(String.valueOf(rs.getTimestamp("create_time")));
        return user;
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
