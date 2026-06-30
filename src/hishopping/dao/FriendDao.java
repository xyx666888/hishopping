package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hishopping.util.DBUtil;

public class FriendDao {
    private static boolean schemaReady = false;

    public FriendDao() {
        ensureSchema();
    }

    public List<Map<String, Object>> searchUsers(int currentUserId, String keyword) {
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "select top 30 u.id,u.account_id,u.username,u.avatar_url,u.status, " +
            "(select count(1) from hishopping_friend f where f.user_id=? and f.friend_user_id=u.id) friend_count, " +
            "(select top 1 status from hishopping_friend_request r where r.from_user_id=? and r.to_user_id=u.id order by r.id desc) request_status " +
            "from hishopping_user u where u.id<>? and (u.account_id like ? or u.username like ? or u.phone like ? or u.email like ?) order by u.id desc";
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId);
            ps.setInt(3, currentUserId);
            ps.setString(4, kw);
            ps.setString(5, kw);
            ps.setString(6, kw);
            ps.setString(7, kw);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("id", rs.getInt("id"));
                map.put("accountId", rs.getString("account_id"));
                map.put("username", rs.getString("username"));
                map.put("avatarUrl", rs.getString("avatar_url"));
                map.put("status", rs.getString("status"));
                map.put("friend", rs.getInt("friend_count") > 0);
                map.put("requestStatus", rs.getString("request_status"));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> requestsForUser(int userId) {
        String sql = "select r.*,u.username,u.account_id,u.avatar_url from hishopping_friend_request r join hishopping_user u on r.from_user_id=u.id where r.to_user_id=? and r.status=N'PENDING' order by r.id desc";
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(requestMap(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public int request(int fromUserId, int toUserId, String remark, String message) {
        if (fromUserId == toUserId) throw new RuntimeException("不能添加自己为好友。");
        if (isFriend(fromUserId, toUserId)) throw new RuntimeException("你们已经是好友。");
        int pending = pendingRequest(fromUserId, toUserId);
        if (pending > 0) throw new RuntimeException("已发送申请，等待对方处理。");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("insert into hishopping_friend_request(from_user_id,to_user_id,remark,message,status) values(?,?,?,?,N'PENDING')", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            ps.setString(3, remark);
            ps.setString(4, message);
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(keys, ps, conn);
        }
    }

    public Map<String, Object> handle(int requestId, int currentUserId, String status) {
        if (!"ACCEPTED".equals(status) && !"REJECTED".equals(status)) throw new RuntimeException("好友申请状态不正确。");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("select * from hishopping_friend_request where id=? and to_user_id=? and status=N'PENDING'");
            ps.setInt(1, requestId);
            ps.setInt(2, currentUserId);
            rs = ps.executeQuery();
            if (!rs.next()) throw new RuntimeException("好友申请不存在或已处理。");
            int fromUserId = rs.getInt("from_user_id");
            close(rs, ps);
            ps = conn.prepareStatement("update hishopping_friend_request set status=?,handle_time=sysdatetime() where id=?");
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
            if ("ACCEPTED".equals(status)) {
                close(null, ps);
                ps = conn.prepareStatement("if not exists(select 1 from hishopping_friend where user_id=? and friend_user_id=?) insert into hishopping_friend(user_id,friend_user_id) values(?,?); if not exists(select 1 from hishopping_friend where user_id=? and friend_user_id=?) insert into hishopping_friend(user_id,friend_user_id) values(?,?)");
                ps.setInt(1, fromUserId);
                ps.setInt(2, currentUserId);
                ps.setInt(3, fromUserId);
                ps.setInt(4, currentUserId);
                ps.setInt(5, currentUserId);
                ps.setInt(6, fromUserId);
                ps.setInt(7, currentUserId);
                ps.setInt(8, fromUserId);
                ps.executeUpdate();
            }
            conn.commit();
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("fromUserId", fromUserId);
            map.put("toUserId", currentUserId);
            map.put("status", status);
            return map;
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public boolean isFriend(int userId, int friendUserId) {
        String sql = "select count(1) from hishopping_friend where user_id=? and friend_user_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, friendUserId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishopping_friend_request', N'U') is null create table dbo.hishopping_friend_request(id int identity(1,1) primary key,from_user_id int not null,to_user_id int not null,remark nvarchar(100) null,message nvarchar(500) null,status nvarchar(20) not null default N'PENDING',create_time datetime2 not null default sysdatetime(),handle_time datetime2 null)");
            st.executeUpdate("if object_id(N'dbo.hishopping_friend', N'U') is null create table dbo.hishopping_friend(id int identity(1,1) primary key,user_id int not null,friend_user_id int not null,remark nvarchar(100) null,create_time datetime2 not null default sysdatetime(),constraint UQ_hishopping_friend unique(user_id,friend_user_id))");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_friend_request_to_status' and object_id=object_id(N'dbo.hishopping_friend_request')) create index IX_hishopping_friend_request_to_status on dbo.hishopping_friend_request(to_user_id, status, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_friend_request_from_status' and object_id=object_id(N'dbo.hishopping_friend_request')) create index IX_hishopping_friend_request_from_status on dbo.hishopping_friend_request(from_user_id, status, create_time desc)");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    private int pendingRequest(int fromUserId, int toUserId) {
        String sql = "select top 1 id from hishopping_friend_request where from_user_id=? and to_user_id=? and status=N'PENDING'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private Map<String, Object> requestMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", rs.getInt("id"));
        map.put("fromUserId", rs.getInt("from_user_id"));
        map.put("toUserId", rs.getInt("to_user_id"));
        map.put("remark", rs.getString("remark"));
        map.put("message", rs.getString("message"));
        map.put("status", rs.getString("status"));
        map.put("createTime", String.valueOf(rs.getTimestamp("create_time")));
        map.put("fromName", rs.getString("username"));
        map.put("fromAccountId", rs.getString("account_id"));
        map.put("fromAvatar", rs.getString("avatar_url"));
        return map;
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
}
