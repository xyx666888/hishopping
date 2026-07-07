package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.AccountRestriction;
import hishopping.util.DBUtil;

public class AccountRestrictionDao {
    private static boolean schemaReady = false;

    public AccountRestrictionDao() {
        ensureSchema();
    }

    public List<AccountRestriction> active(String targetRole, int targetId) {
        expireFor(targetRole, targetId);
        String sql = "select * from hishop_account_restriction where target_role=? and target_id=? and restricted=1 and status=N'ACTIVE' order by create_time desc";
        return query(sql, targetRole, Integer.valueOf(targetId));
    }

    public List<AccountRestriction> history(String targetRole, int targetId) {
        expireFor(targetRole, targetId);
        String sql = "select * from hishop_account_restriction where target_role=? and target_id=? order by create_time desc";
        return query(sql, targetRole, Integer.valueOf(targetId));
    }

    public AccountRestriction activeOne(String targetRole, int targetId, String permissionKey) {
        expireFor(targetRole, targetId);
        String sql = "select top 1 * from hishop_account_restriction where target_role=? and target_id=? and permission_key=? and restricted=1 and status=N'ACTIVE' order by create_time desc";
        List<AccountRestriction> rows = query(sql, targetRole, Integer.valueOf(targetId), permissionKey);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int add(String targetRole, int targetId, String permissionKey, String reason, String sourceType, int sourceId, Integer durationDays, int adminId, String adminName) {
        String sql = durationDays == null
            ? "insert into hishop_account_restriction(target_role,target_id,permission_key,restricted,reason,source_type,source_id,end_time,status,admin_id,admin_name) values(?,?,?,1,?,?,?,null,N'ACTIVE',?,?)"
            : "insert into hishop_account_restriction(target_role,target_id,permission_key,restricted,reason,source_type,source_id,end_time,status,admin_id,admin_name) values(?,?,?,1,?,?,?,dateadd(day,?,sysdatetime()),N'ACTIVE',?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, targetRole);
            ps.setInt(2, targetId);
            ps.setString(3, permissionKey);
            ps.setString(4, reason);
            ps.setString(5, sourceType);
            ps.setInt(6, sourceId);
            if (durationDays == null) {
                ps.setInt(7, adminId);
                ps.setString(8, adminName);
            } else {
                ps.setInt(7, durationDays.intValue());
                ps.setInt(8, adminId);
                ps.setString(9, adminName);
            }
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(keys, ps, conn);
        }
    }

    public void cancel(int restrictionId) {
        String sql = "update hishop_account_restriction set status=N'CANCELLED', restricted=0, update_time=sysdatetime() where restriction_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, restrictionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void expireFor(String targetRole, int targetId) {
        String sql = "update hishop_account_restriction set status=N'EXPIRED', restricted=0, update_time=sysdatetime() where target_role=? and target_id=? and status=N'ACTIVE' and end_time is not null and end_time<=sysdatetime()";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, targetRole);
            ps.setInt(2, targetId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private List<AccountRestriction> query(String sql, Object... params) {
        List<AccountRestriction> rows = new ArrayList<AccountRestriction>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Integer) ps.setInt(i + 1, ((Integer) param).intValue());
                else ps.setString(i + 1, String.valueOf(param));
            }
            rs = ps.executeQuery();
            while (rs.next()) rows.add(map(rs));
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private AccountRestriction map(ResultSet rs) throws SQLException {
        AccountRestriction r = new AccountRestriction();
        r.setRestrictionId(rs.getInt("restriction_id"));
        r.setTargetRole(rs.getString("target_role"));
        r.setTargetId(rs.getInt("target_id"));
        r.setPermissionKey(rs.getString("permission_key"));
        r.setRestricted(rs.getBoolean("restricted"));
        r.setReason(rs.getString("reason"));
        r.setSourceType(rs.getString("source_type"));
        r.setSourceId(rs.getInt("source_id"));
        r.setStartTime(time(rs, "start_time"));
        r.setEndTime(time(rs, "end_time"));
        r.setStatus(rs.getString("status"));
        r.setAdminId(rs.getInt("admin_id"));
        r.setAdminName(rs.getString("admin_name"));
        r.setCreateTime(time(rs, "create_time"));
        r.setUpdateTime(time(rs, "update_time"));
        return r;
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishop_account_restriction', N'U') is null create table dbo.hishop_account_restriction(restriction_id int identity(1,1) primary key,target_role nvarchar(20) not null,target_id int not null,permission_key nvarchar(50) not null,restricted bit not null constraint DF_hishop_account_restriction_restricted default 1,reason nvarchar(500) null,source_type nvarchar(30) null,source_id int null,start_time datetime2 not null constraint DF_hishop_account_restriction_start default sysdatetime(),end_time datetime2 null,status nvarchar(20) not null constraint DF_hishop_account_restriction_status default N'ACTIVE',admin_id int null,admin_name nvarchar(100) null,create_time datetime2 not null constraint DF_hishop_account_restriction_create default sysdatetime(),update_time datetime2 null)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_account_restriction_target' and object_id=object_id(N'dbo.hishop_account_restriction')) create index IX_hishop_account_restriction_target on dbo.hishop_account_restriction(target_role,target_id,status)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_account_restriction_permission' and object_id=object_id(N'dbo.hishop_account_restriction')) create index IX_hishop_account_restriction_permission on dbo.hishop_account_restriction(permission_key,status)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_account_restriction_end' and object_id=object_id(N'dbo.hishop_account_restriction')) create index IX_hishop_account_restriction_end on dbo.hishop_account_restriction(end_time)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_account_restriction_source' and object_id=object_id(N'dbo.hishop_account_restriction')) create index IX_hishop_account_restriction_source on dbo.hishop_account_restriction(source_type,source_id)");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
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
