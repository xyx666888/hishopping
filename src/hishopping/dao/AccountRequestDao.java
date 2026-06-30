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

public class AccountRequestDao {
    private static boolean schemaReady = false;

    public AccountRequestDao() {
        ensureSchema();
    }

    public List<Map<String, Object>> all() {
        return query("select * from hishopping_account_request order by case when status=N'PENDING' then 0 else 1 end, create_time desc", null, 0);
    }

    public List<Map<String, Object>> mine(String role, int actorId) {
        return query("select * from hishopping_account_request where actor_role=? and actor_id=? order by create_time desc", role, actorId);
    }

    private List<Map<String, Object>> query(String sql, String role, int actorId) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            if (role != null) {
                ps.setString(1, role);
                ps.setInt(2, actorId);
            }
            rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void submit(String role, int actorId, String actorName, String requestType, String title, String content, String attachmentUrl) {
        String sql = "insert into hishopping_account_request(actor_role,actor_id,actor_name,request_type,title,content,attachment_url) values(?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ps.setInt(2, actorId);
            ps.setString(3, actorName);
            ps.setString(4, requestType);
            ps.setString(5, title);
            ps.setString(6, content);
            ps.setString(7, attachmentUrl);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public Map<String, Object> find(int id) {
        List<Map<String, Object>> list = query("select * from hishopping_account_request where request_id=" + id, null, 0);
        return list.isEmpty() ? null : list.get(0);
    }

    public void review(int id, String status, String opinion, int adminId) {
        String sql = "update hishopping_account_request set status=?, opinion=?, review_admin_id=?, review_time=sysdatetime() where request_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, opinion);
            ps.setInt(3, adminId);
            ps.setInt(4, id);
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
            st.executeUpdate("if object_id(N'dbo.hishopping_account_request', N'U') is null create table dbo.hishopping_account_request (request_id int identity(1,1) primary key, actor_role nvarchar(20) not null, actor_id int not null, actor_name nvarchar(100) null, request_type nvarchar(30) not null, title nvarchar(120) not null, content nvarchar(1000) not null, attachment_url nvarchar(300) null, status nvarchar(20) not null default N'PENDING', opinion nvarchar(500) null, create_time datetime2 not null default sysdatetime(), review_time datetime2 null, review_admin_id int null)");
            st.executeUpdate("if col_length('dbo.hishopping_account_request','attachment_url') is null alter table dbo.hishopping_account_request add attachment_url nvarchar(300) null");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    private Map<String, Object> map(ResultSet rs) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("requestId", rs.getInt("request_id"));
        map.put("actorRole", rs.getString("actor_role"));
        map.put("actorId", rs.getInt("actor_id"));
        map.put("actorName", rs.getString("actor_name"));
        map.put("requestType", rs.getString("request_type"));
        map.put("title", rs.getString("title"));
        map.put("content", rs.getString("content"));
        map.put("attachmentUrl", rs.getString("attachment_url"));
        map.put("status", rs.getString("status"));
        map.put("opinion", rs.getString("opinion"));
        map.put("createTime", String.valueOf(rs.getTimestamp("create_time")));
        map.put("reviewTime", rs.getTimestamp("review_time") == null ? "" : String.valueOf(rs.getTimestamp("review_time")));
        map.put("reviewAdminId", rs.getInt("review_admin_id"));
        return map;
    }
}
