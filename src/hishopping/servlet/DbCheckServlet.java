package hishopping.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.util.DBUtil;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class DbCheckServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        Map<String, Object> result;
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            result = ServletUtil.ok();
            result.put("database", conn.getCatalog());
            result.put("url", request.getRequestURL().toString());
            result.put("contextPath", request.getContextPath());
            result.put("serverTime", new java.util.Date().toString());
            DatabaseMetaData meta = conn.getMetaData();
            result.put("jdbcUrl", meta.getURL());
            rs = st.executeQuery("select @@SERVERNAME as server_name, SUSER_SNAME() as login_name, DB_NAME() as db_name");
            if (rs.next()) {
                result.put("serverName", rs.getString("server_name"));
                result.put("loginName", rs.getString("login_name"));
                result.put("dbName", rs.getString("db_name"));
            }
            rs.close();
            rs = null;
            String[] tables = {"hishopping_user", "hishopping_cart_item", "hishopping_order", "hishopping_order_item", "hishopping_address"};
            Map<String, Object> counts = new LinkedHashMap<String, Object>();
            for (int i = 0; i < tables.length; i++) {
                rs = st.executeQuery("select count(*) from dbo." + tables[i]);
                rs.next();
                counts.put(tables[i], rs.getInt(1));
                rs.close();
                rs = null;
            }
            result.put("counts", counts);
            result.put("latestUsers", rows(st, "select top 5 id, account_id, email, username, create_time from dbo.hishopping_user order by id desc", new String[] {"id", "account_id", "email", "username", "create_time"}));
            result.put("latestOrders", rows(st, "select top 5 id, order_no, user_id, status, total_amount, create_time from dbo.hishopping_order order by id desc", new String[] {"id", "order_no", "user_id", "status", "total_amount", "create_time"}));
        } catch (Exception e) {
            result = ServletUtil.fail(e.getMessage());
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
        JsonUtil.write(response, result);
    }

    private List<Map<String, Object>> rows(Statement st, String sql, String[] columns) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ResultSet rs = null;
        try {
            rs = st.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                for (int i = 0; i < columns.length; i++) {
                    row.put(columns[i], rs.getString(columns[i]));
                }
                list.add(row);
            }
            return list;
        } finally {
            if (rs != null) rs.close();
        }
    }
}

