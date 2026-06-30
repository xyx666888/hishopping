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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hishopping.entity.ProductMedia;
import hishopping.util.DBUtil;

public class ProductMediaDao {
    private static final int MAX_MEDIA = 6;
    private static boolean schemaReady = false;

    public ProductMediaDao() {
        ensureSchema();
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishopping_product_media', N'U') is null create table dbo.hishopping_product_media (id int identity(1,1) primary key, product_id int not null, media_type nvarchar(20) not null default N'IMAGE', media_url nvarchar(300) not null, sort_no int not null default 0, cover_flag bit not null default 0, create_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if col_length('dbo.hishopping_product_media','cover_flag') is null alter table dbo.hishopping_product_media add cover_flag bit not null constraint DF_hishopping_product_media_cover default 0");
            st.executeUpdate("if not exists (select 1 from sys.indexes where name=N'IX_hishopping_product_media_product' and object_id=object_id(N'dbo.hishopping_product_media')) create index IX_hishopping_product_media_product on dbo.hishopping_product_media(product_id, sort_no, id)");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    public List<ProductMedia> findByProductId(int productId, String fallbackImageUrl) {
        List<ProductMedia> list = new ArrayList<ProductMedia>();
        if (productId > 0) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = DBUtil.getConn();
                ps = conn.prepareStatement("select * from hishopping_product_media where product_id=? order by sort_no asc,id asc");
                ps.setInt(1, productId);
                rs = ps.executeQuery();
                while (rs.next()) list.add(map(rs));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                DBUtil.closeDBResource(rs, ps, conn);
            }
        }
        if (list.isEmpty() && !empty(fallbackImageUrl)) {
            ProductMedia media = new ProductMedia();
            media.setProductId(productId);
            media.setMediaType("IMAGE");
            media.setMediaUrl(fallbackImageUrl);
            media.setSortNo(1);
            media.setCoverFlag(true);
            list.add(media);
        }
        return list;
    }

    public void replaceProductMedia(int productId, List<ProductMedia> mediaList) {
        Connection conn = null;
        PreparedStatement delete = null;
        PreparedStatement insert = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            delete = conn.prepareStatement("delete from hishopping_product_media where product_id=?");
            delete.setInt(1, productId);
            delete.executeUpdate();
            insert = conn.prepareStatement("insert into hishopping_product_media(product_id,media_type,media_url,sort_no,cover_flag) values(?,?,?,?,?)");
            int sort = 1;
            for (ProductMedia media : normalize(mediaList)) {
                insert.setInt(1, productId);
                insert.setString(2, media.getMediaType());
                insert.setString(3, media.getMediaUrl());
                insert.setInt(4, sort);
                insert.setBoolean(5, sort == 1);
                insert.addBatch();
                sort++;
            }
            insert.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) {}
            }
            closeQuietly(insert);
            DBUtil.closeDBResource(null, delete, conn);
        }
    }

    public static List<ProductMedia> parseMediaJson(String raw) {
        List<ProductMedia> list = new ArrayList<ProductMedia>();
        if (empty(raw)) return list;
        Matcher objectMatcher = Pattern.compile("\\{([^}]*)\\}").matcher(raw);
        while (objectMatcher.find()) {
            String body = objectMatcher.group(1);
            ProductMedia media = new ProductMedia();
            media.setId((int) number(body, "id", 0));
            media.setMediaType(normalizeType(text(body, "mediaType")));
            media.setMediaUrl(firstNonEmpty(text(body, "mediaUrl"), text(body, "imageUrl")));
            media.setSortNo((int) number(body, "sortNo", list.size() + 1));
            media.setCoverFlag(bool(body, "coverFlag", list.isEmpty()));
            if (!empty(media.getMediaUrl())) list.add(media);
        }
        return normalize(list);
    }

    public static List<ProductMedia> normalize(List<ProductMedia> raw) {
        List<ProductMedia> list = new ArrayList<ProductMedia>();
        if (raw == null) return list;
        Map<String, ProductMedia> unique = new LinkedHashMap<String, ProductMedia>();
        for (ProductMedia item : raw) {
            if (item == null || empty(item.getMediaUrl())) continue;
            ProductMedia media = new ProductMedia();
            media.setId(item.getId());
            media.setProductId(item.getProductId());
            media.setMediaType(normalizeType(item.getMediaType()));
            media.setMediaUrl(item.getMediaUrl().trim());
            media.setSortNo(item.getSortNo());
            media.setCoverFlag(item.isCoverFlag());
            unique.put(media.getMediaType() + "|" + media.getMediaUrl(), media);
        }
        int sort = 1;
        for (ProductMedia media : unique.values()) {
            media.setSortNo(sort);
            media.setCoverFlag(sort == 1);
            list.add(media);
            if (list.size() >= MAX_MEDIA) break;
            sort++;
        }
        return list;
    }

    public static String firstMediaUrl(List<ProductMedia> mediaList, String fallback) {
        List<ProductMedia> normalized = normalize(mediaList);
        return normalized.isEmpty() ? fallback : normalized.get(0).getMediaUrl();
    }

    private ProductMedia map(ResultSet rs) throws SQLException {
        ProductMedia media = new ProductMedia();
        media.setId(rs.getInt("id"));
        media.setProductId(rs.getInt("product_id"));
        media.setMediaType(rs.getString("media_type"));
        media.setMediaUrl(rs.getString("media_url"));
        media.setSortNo(rs.getInt("sort_no"));
        media.setCoverFlag(rs.getBoolean("cover_flag"));
        media.setCreateTime(String.valueOf(rs.getObject("create_time")));
        return media;
    }

    private static String normalizeType(String value) {
        String type = value == null ? "" : value.trim().toUpperCase();
        return "VIDEO".equals(type) ? "VIDEO" : "IMAGE";
    }

    private static String firstNonEmpty(String a, String b) {
        return empty(a) ? b : a;
    }

    private static String text(String body, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(body);
        return matcher.find() ? unescape(matcher.group(1)) : "";
    }

    private static double number(String body, String key, double fallback) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(body);
        if (!matcher.find()) return fallback;
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean bool(String body, String key, boolean fallback) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)").matcher(body);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : fallback;
    }

    private static String unescape(String value) {
        return value == null ? "" : value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private void rollback(Connection conn) {
        if (conn == null) return;
        try { conn.rollback(); } catch (SQLException e) {}
    }

    private void closeQuietly(Statement st) {
        if (st == null) return;
        try { st.close(); } catch (SQLException e) {}
    }
}
