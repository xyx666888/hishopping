package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.Report;
import hishopping.util.DBUtil;

public class ReportDao {
    private static boolean schemaReady = false;

    public ReportDao() {
        ensureSchema();
    }

    public int create(Report report) {
        fillTargetSnapshot(report);
        String sql = "insert into hishop_report(reporter_role,reporter_id,reporter_name,target_role,target_id,target_name,merchant_id,user_id,order_id,product_id,review_id,report_type,reason,description,evidence_urls,status) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,N'PENDING')";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, report.getReporterRole());
            ps.setInt(2, report.getReporterId());
            ps.setString(3, report.getReporterName());
            ps.setString(4, report.getTargetRole());
            ps.setInt(5, report.getTargetId());
            ps.setString(6, report.getTargetName());
            setNullableInt(ps, 7, report.getMerchantId());
            setNullableInt(ps, 8, report.getUserId());
            setNullableInt(ps, 9, report.getOrderId());
            setNullableInt(ps, 10, report.getProductId());
            setNullableInt(ps, 11, report.getReviewId());
            ps.setString(12, report.getReportType());
            ps.setString(13, report.getReason());
            ps.setString(14, report.getDescription());
            ps.setString(15, report.getEvidenceUrls());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(keys, ps, conn);
        }
    }

    public List<Report> findByReporter(String reporterRole, int reporterId) {
        return query("select * from hishop_report where reporter_role=? and reporter_id=? order by create_time desc, report_id desc", reporterRole, reporterId);
    }

    public List<Report> findByMerchant(int merchantId) {
        return query("select * from hishop_report where merchant_id=? and not (reporter_role=N'MERCHANT' and reporter_id=?) order by create_time desc, report_id desc", Integer.valueOf(merchantId), Integer.valueOf(merchantId));
    }

    public List<Report> findForAdmin(String status, String keyword) {
        return findForAdmin(status, keyword, 1, 200);
    }

    public List<Report> findForAdmin(String status, String keyword, int page, int pageSize) {
        QuerySpec spec = adminQuery(status, keyword);
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        int start = (safePage - 1) * safePageSize;
        int end = start + safePageSize;
        StringBuilder sql = new StringBuilder("select * from (select row_number() over(order by create_time desc, report_id desc) row_num, * from hishop_report where ");
        sql.append(spec.where);
        sql.append(") t where row_num>? and row_num<=? order by row_num");
        List<Object> params = new ArrayList<Object>(spec.params);
        params.add(Integer.valueOf(start));
        params.add(Integer.valueOf(end));
        return query(sql.toString(), params.toArray(new Object[params.size()]));
    }

    public int countForAdmin(String status, String keyword) {
        QuerySpec spec = adminQuery(status, keyword);
        String sql = "select count(1) from hishop_report where " + spec.where;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < spec.params.size(); i++) {
                Object p = spec.params.get(i);
                if (p instanceof Integer) ps.setInt(i + 1, ((Integer) p).intValue());
                else ps.setString(i + 1, String.valueOf(p));
            }
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private QuerySpec adminQuery(String status, String keyword) {
        QuerySpec spec = new QuerySpec();
        spec.where = "1=1";
        if (status != null && status.length() > 0 && !"all".equalsIgnoreCase(status)) {
            spec.where += " and status=?";
            spec.params.add(status);
        }
        if (keyword != null && keyword.trim().length() > 0) {
            spec.where += " and (reporter_name like ? or target_name like ? or report_type like ? or reason like ? or description like ? or handle_opinion like ? or handle_result like ?)";
            String kw = "%" + keyword.trim() + "%";
            spec.params.add(kw);
            spec.params.add(kw);
            spec.params.add(kw);
            spec.params.add(kw);
            spec.params.add(kw);
            spec.params.add(kw);
            spec.params.add(kw);
        }
        return spec;
    }

    public Report findById(int reportId) {
        List<Report> rows = query("select * from hishop_report where report_id=?", Integer.valueOf(reportId));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void handle(int reportId, String status, int adminId, String adminName, String opinion, String result) {
        String sql = "update hishop_report set status=?, admin_id=?, admin_name=?, handle_opinion=?, handle_result=?, handle_time=sysdatetime(), update_time=sysdatetime() where report_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, adminId);
            ps.setString(3, adminName);
            ps.setString(4, opinion);
            ps.setString(5, result);
            ps.setInt(6, reportId);
            if (ps.executeUpdate() == 0) throw new RuntimeException("举报记录不存在。");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private List<Report> query(String sql, Object... params) {
        List<Report> list = new ArrayList<Report>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                Object p = params[i];
                if (p instanceof Integer) ps.setInt(i + 1, ((Integer) p).intValue());
                else ps.setString(i + 1, String.valueOf(p));
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

    private void fillTargetSnapshot(Report report) {
        Snapshot s = snapshot(report);
        if (s == null) throw new RuntimeException("举报对象不存在或无权举报。");
        report.setTargetName(s.name);
        if (report.getMerchantId() <= 0) report.setMerchantId(s.merchantId);
        if (report.getUserId() <= 0) report.setUserId(s.userId);
        if (report.getOrderId() <= 0) report.setOrderId(s.orderId);
        if (report.getProductId() <= 0) report.setProductId(s.productId);
        if (report.getReviewId() <= 0) report.setReviewId(s.reviewId);
    }

    private Snapshot snapshot(Report report) {
        if ("MERCHANT".equals(report.getTargetRole())) {
            return one("select top 1 shop_name name, merchant_id, 0 user_id, 0 order_id, 0 product_id, 0 review_id from hishop_merchant where merchant_id=?", report.getTargetId());
        }
        if ("PRODUCT".equals(report.getTargetRole())) {
            return one("select top 1 name, merchant_id, 0 user_id, 0 order_id, id product_id, 0 review_id from hishopping_product where id=?", report.getTargetId());
        }
        if ("ORDER".equals(report.getTargetRole())) {
            String sql = "select top 1 order_no name, merchant_id, user_id, id order_id, 0 product_id, 0 review_id from hishopping_order where id=?";
            Snapshot s = one(sql, report.getTargetId());
            if (s != null && report.getReporterRole().equals("USER") && s.userId != report.getReporterId()) return null;
            if (s != null && report.getReporterRole().equals("MERCHANT") && s.merchantId != report.getReporterId()) return null;
            return s;
        }
        if ("REVIEW".equals(report.getTargetRole())) {
            String sql = "select top 1 left(isnull(r.content,N'评价内容'),80) name, p.merchant_id, r.user_id, r.order_id, r.product_id, r.review_id from hishop_product_review r left join hishopping_product p on r.product_id=p.id where r.review_id=?";
            Snapshot s = one(sql, report.getTargetId());
            if (s != null && report.getReporterRole().equals("MERCHANT") && s.merchantId != report.getReporterId()) return null;
            return s;
        }
        if ("USER".equals(report.getTargetRole())) {
            Snapshot s = one("select top 1 username name, 0 merchant_id, id user_id, 0 order_id, 0 product_id, 0 review_id from hishopping_user where id=?", report.getTargetId());
            if (s != null && report.getReporterRole().equals("MERCHANT") && !merchantCanReportUser(report.getReporterId(), report.getTargetId())) return null;
            if (s != null && report.getReporterRole().equals("MERCHANT")) s.merchantId = report.getReporterId();
            return s;
        }
        return null;
    }

    private boolean merchantCanReportUser(int merchantId, int userId) {
        String sql = "select count(1) from (select user_id from hishopping_order where merchant_id=? and user_id=? union select user_id from hishopping_product_view where merchant_id=? and user_id=?) t";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, merchantId);
            ps.setInt(2, userId);
            ps.setInt(3, merchantId);
            ps.setInt(4, userId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private Snapshot one(String sql, int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            Snapshot s = new Snapshot();
            s.name = nullToEmpty(rs.getString("name"));
            s.merchantId = getInt(rs, "merchant_id");
            s.userId = getInt(rs, "user_id");
            s.orderId = getInt(rs, "order_id");
            s.productId = getInt(rs, "product_id");
            s.reviewId = getInt(rs, "review_id");
            return s;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private Report map(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setReportId(rs.getInt("report_id"));
        r.setReporterRole(rs.getString("reporter_role"));
        r.setReporterId(rs.getInt("reporter_id"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setTargetRole(rs.getString("target_role"));
        r.setTargetId(rs.getInt("target_id"));
        r.setTargetName(rs.getString("target_name"));
        r.setMerchantId(getInt(rs, "merchant_id"));
        r.setUserId(getInt(rs, "user_id"));
        r.setOrderId(getInt(rs, "order_id"));
        r.setProductId(getInt(rs, "product_id"));
        r.setReviewId(getInt(rs, "review_id"));
        r.setReportType(rs.getString("report_type"));
        r.setReason(rs.getString("reason"));
        r.setDescription(rs.getString("description"));
        r.setEvidenceUrls(rs.getString("evidence_urls"));
        r.setStatus(rs.getString("status"));
        r.setAdminId(getInt(rs, "admin_id"));
        r.setAdminName(rs.getString("admin_name"));
        r.setHandleOpinion(rs.getString("handle_opinion"));
        r.setHandleResult(rs.getString("handle_result"));
        r.setCreateTime(time(rs, "create_time"));
        r.setUpdateTime(time(rs, "update_time"));
        r.setHandleTime(time(rs, "handle_time"));
        return r;
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishop_report', N'U') is null create table dbo.hishop_report(report_id int identity(1,1) primary key, reporter_role nvarchar(20) not null, reporter_id int not null, reporter_name nvarchar(100) null, target_role nvarchar(20) not null, target_id int not null, target_name nvarchar(160) null, merchant_id int null, user_id int null, order_id int null, product_id int null, review_id int null, report_type nvarchar(60) not null, reason nvarchar(300) not null, description nvarchar(1000) null, evidence_urls nvarchar(max) null, status nvarchar(20) not null default N'PENDING', admin_id int null, admin_name nvarchar(100) null, handle_opinion nvarchar(500) null, handle_result nvarchar(500) null, create_time datetime2 not null default sysdatetime(), update_time datetime2 not null default sysdatetime(), handle_time datetime2 null)");
            st.executeUpdate("if col_length('dbo.hishop_report','reporter_role') is null alter table dbo.hishop_report add reporter_role nvarchar(20) not null constraint DF_hishop_report_reporter_role_java default N'USER'");
            st.executeUpdate("if col_length('dbo.hishop_report','reporter_id') is null alter table dbo.hishop_report add reporter_id int not null constraint DF_hishop_report_reporter_id_java default 0");
            st.executeUpdate("if col_length('dbo.hishop_report','reporter_name') is null alter table dbo.hishop_report add reporter_name nvarchar(100) null");
            st.executeUpdate("if col_length('dbo.hishop_report','target_role') is null alter table dbo.hishop_report add target_role nvarchar(20) not null constraint DF_hishop_report_target_role_java default N'PRODUCT'");
            st.executeUpdate("if col_length('dbo.hishop_report','target_id') is null alter table dbo.hishop_report add target_id int not null constraint DF_hishop_report_target_id_java default 0");
            st.executeUpdate("if col_length('dbo.hishop_report','target_name') is null alter table dbo.hishop_report add target_name nvarchar(160) null");
            st.executeUpdate("if col_length('dbo.hishop_report','merchant_id') is null alter table dbo.hishop_report add merchant_id int null");
            st.executeUpdate("if col_length('dbo.hishop_report','user_id') is null alter table dbo.hishop_report add user_id int null");
            st.executeUpdate("if col_length('dbo.hishop_report','order_id') is null alter table dbo.hishop_report add order_id int null");
            st.executeUpdate("if col_length('dbo.hishop_report','product_id') is null alter table dbo.hishop_report add product_id int null");
            st.executeUpdate("if col_length('dbo.hishop_report','review_id') is null alter table dbo.hishop_report add review_id int null");
            st.executeUpdate("if col_length('dbo.hishop_report','report_type') is null alter table dbo.hishop_report add report_type nvarchar(60) not null constraint DF_hishop_report_report_type_java default N'其他'");
            st.executeUpdate("if col_length('dbo.hishop_report','reason') is null alter table dbo.hishop_report add reason nvarchar(300) not null constraint DF_hishop_report_reason_java default N''");
            st.executeUpdate("if col_length('dbo.hishop_report','description') is null alter table dbo.hishop_report add description nvarchar(1000) null");
            st.executeUpdate("if col_length('dbo.hishop_report','evidence_urls') is null alter table dbo.hishop_report add evidence_urls nvarchar(max) null");
            st.executeUpdate("if col_length('dbo.hishop_report','status') is null alter table dbo.hishop_report add status nvarchar(20) not null constraint DF_hishop_report_status_java default N'PENDING'");
            st.executeUpdate("if col_length('dbo.hishop_report','admin_id') is null alter table dbo.hishop_report add admin_id int null");
            st.executeUpdate("if col_length('dbo.hishop_report','admin_name') is null alter table dbo.hishop_report add admin_name nvarchar(100) null");
            st.executeUpdate("if col_length('dbo.hishop_report','handle_opinion') is null alter table dbo.hishop_report add handle_opinion nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishop_report','handle_result') is null alter table dbo.hishop_report add handle_result nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishop_report','create_time') is null alter table dbo.hishop_report add create_time datetime2 not null constraint DF_hishop_report_create_time_java default sysdatetime()");
            st.executeUpdate("if col_length('dbo.hishop_report','update_time') is null alter table dbo.hishop_report add update_time datetime2 not null constraint DF_hishop_report_update_time_java default sysdatetime()");
            st.executeUpdate("if col_length('dbo.hishop_report','handle_time') is null alter table dbo.hishop_report add handle_time datetime2 null");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_report_status_time' and object_id=object_id(N'dbo.hishop_report')) create index IX_hishop_report_status_time on dbo.hishop_report(status, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_report_reporter' and object_id=object_id(N'dbo.hishop_report')) create index IX_hishop_report_reporter on dbo.hishop_report(reporter_role, reporter_id, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_report_target' and object_id=object_id(N'dbo.hishop_report')) create index IX_hishop_report_target on dbo.hishop_report(target_role, target_id)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_report_merchant' and object_id=object_id(N'dbo.hishop_report')) create index IX_hishop_report_merchant on dbo.hishop_report(merchant_id, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_report_user' and object_id=object_id(N'dbo.hishop_report')) create index IX_hishop_report_user on dbo.hishop_report(user_id, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishop_report_order' and object_id=object_id(N'dbo.hishop_report')) create index IX_hishop_report_order on dbo.hishop_report(order_id, create_time desc)");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, int value) throws SQLException {
        if (value > 0) ps.setInt(index, value);
        else ps.setNull(index, java.sql.Types.INTEGER);
    }

    private int getInt(ResultSet rs, String column) {
        try {
            Object value = rs.getObject(column);
            return value == null ? 0 : rs.getInt(column);
        } catch (SQLException e) {
            return 0;
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

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class Snapshot {
        String name;
        int merchantId;
        int userId;
        int orderId;
        int productId;
        int reviewId;
    }

    private static class QuerySpec {
        String where;
        List<Object> params = new ArrayList<Object>();
    }
}
