package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.Address;
import hishopping.util.DBUtil;

public class AddressDao {
    public List<Address> findAll() {
        String sql = "select * from hishopping_address order by user_id, is_default desc, id desc";
        List<Address> list = new ArrayList<Address>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(mapAddress(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
    }

    public List<Address> findByUserId(int userId) {
        String sql = "select * from hishopping_address where user_id=? order by is_default desc, id desc";
        List<Address> list = new ArrayList<Address>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapAddress(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public Address findDefault(int userId, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("select top 1 * from hishopping_address where user_id=? order by is_default desc, id desc");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            return rs.next() ? mapAddress(rs) : null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    public Address findById(int userId, int addressId, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("select * from hishopping_address where user_id=? and id=?");
            ps.setInt(1, userId);
            ps.setInt(2, addressId);
            rs = ps.executeQuery();
            return rs.next() ? mapAddress(rs) : null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    public void save(Address address) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            if (address.isDefaultAddress()) {
                clearDefault(address.getUserId(), conn);
            }
            ps = conn.prepareStatement("insert into hishopping_address(user_id,receiver_name,phone,province,city,district,detail,is_default) values(?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, address.getUserId());
            ps.setString(2, address.getReceiverName());
            ps.setString(3, address.getPhone());
            ps.setString(4, address.getProvince());
            ps.setString(5, address.getCity());
            ps.setString(6, address.getDistrict());
            ps.setString(7, address.getDetail());
            ps.setBoolean(8, address.isDefaultAddress());
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void setDefault(int userId, int addressId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            clearDefault(userId, conn);
            ps = conn.prepareStatement("update hishopping_address set is_default=1 where user_id=? and id=?");
            ps.setInt(1, userId);
            ps.setInt(2, addressId);
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void delete(int userId, int addressId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("delete from hishopping_address where user_id=? and id=?");
            ps.setInt(1, userId);
            ps.setInt(2, addressId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private void clearDefault(int userId, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("update hishopping_address set is_default=0 where user_id=?");
            ps.setInt(1, userId);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }

    private Address mapAddress(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setReceiverName(rs.getString("receiver_name"));
        a.setPhone(rs.getString("phone"));
        a.setProvince(rs.getString("province"));
        a.setCity(rs.getString("city"));
        a.setDistrict(rs.getString("district"));
        a.setDetail(rs.getString("detail"));
        a.setDefaultAddress(rs.getBoolean("is_default"));
        return a;
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }
}

