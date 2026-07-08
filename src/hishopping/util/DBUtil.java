package hishopping.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL = "jdbc:sqlserver://localhost:1433;DatabaseName=hishopping";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

    private DBUtil() {
    }

    public static Connection getConn() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Server 驱动没有找到，请确认 sqljdbc4.jar 已放入 WEB-INF/lib。", e);
        } catch (SQLException e) {
            throw new RuntimeException("连接 hishopping 数据库失败，请确认 SQL Server 已启动且连接配置正确。", e);
        }
    }

    public static void closeDBResource(ResultSet rs, Statement stm, Connection con) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

