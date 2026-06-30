package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hishopping.entity.HallBanner;
import hishopping.util.DBUtil;

public class HallBannerDao {
    private static boolean schemaReady = false;

    public HallBannerDao() {
        ensureSchema();
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishopping_hall_banner', N'U') is null create table dbo.hishopping_hall_banner (id int identity(1,1) primary key, media_type nvarchar(20) not null default N'IMAGE', media_url nvarchar(300) not null, title nvarchar(100) null, subtitle nvarchar(300) null, enabled bit not null default 1, sort_no int not null default 0, link_enabled bit not null default 0, link_type nvarchar(30) null, link_target nvarchar(200) null, product_id int null, overlay_enabled bit not null default 1, text_position nvarchar(20) not null default N'LEFT', title_color nvarchar(20) not null default N'#ffffff', subtitle_color nvarchar(20) not null default N'#e2e8f0', video_muted_default bit not null default 1, video_disable_seek bit not null default 0, video_disable_pause bit not null default 0, create_time datetime2 not null default sysdatetime(), update_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if col_length('dbo.hishopping_hall_banner','overlay_enabled') is null alter table dbo.hishopping_hall_banner add overlay_enabled bit not null constraint DF_hishopping_hall_overlay default 1");
            st.executeUpdate("if col_length('dbo.hishopping_hall_banner','text_position') is null alter table dbo.hishopping_hall_banner add text_position nvarchar(20) not null constraint DF_hishopping_hall_position default N'LEFT'");
            st.executeUpdate("if col_length('dbo.hishopping_hall_banner','title_color') is null alter table dbo.hishopping_hall_banner add title_color nvarchar(20) not null constraint DF_hishopping_hall_title_color default N'#ffffff'");
            st.executeUpdate("if col_length('dbo.hishopping_hall_banner','subtitle_color') is null alter table dbo.hishopping_hall_banner add subtitle_color nvarchar(20) not null constraint DF_hishopping_hall_subtitle_color default N'#e2e8f0'");
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    public List<HallBanner> all() {
        return query("select * from hishopping_hall_banner order by sort_no asc,id desc");
    }

    public List<HallBanner> enabled() {
        return query("select * from hishopping_hall_banner where enabled=1 order by sort_no asc,id desc");
    }

    private List<HallBanner> query(String sql) {
        List<HallBanner> list = new ArrayList<HallBanner>();
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

    public void save(HallBanner banner) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            if (banner.getId() > 0) {
                ps = conn.prepareStatement("update hishopping_hall_banner set media_type=?, media_url=?, title=?, subtitle=?, enabled=?, sort_no=?, link_enabled=?, link_type=?, link_target=?, product_id=?, overlay_enabled=?, text_position=?, title_color=?, subtitle_color=?, video_muted_default=?, video_disable_seek=?, video_disable_pause=?, update_time=sysdatetime() where id=?");
                fill(ps, banner);
                ps.setInt(18, banner.getId());
            } else {
                ps = conn.prepareStatement("insert into hishopping_hall_banner(media_type,media_url,title,subtitle,enabled,sort_no,link_enabled,link_type,link_target,product_id,overlay_enabled,text_position,title_color,subtitle_color,video_muted_default,video_disable_seek,video_disable_pause) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                fill(ps, banner);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private void fill(PreparedStatement ps, HallBanner b) throws SQLException {
        ps.setString(1, b.getMediaType());
        ps.setString(2, b.getMediaUrl());
        ps.setString(3, b.getTitle());
        ps.setString(4, b.getSubtitle());
        ps.setBoolean(5, b.isEnabled());
        ps.setInt(6, b.getSortNo());
        ps.setBoolean(7, b.isLinkEnabled());
        ps.setString(8, b.getLinkType());
        ps.setString(9, b.getLinkTarget());
        ps.setInt(10, b.getProductId());
        ps.setBoolean(11, b.isOverlayEnabled());
        ps.setString(12, b.getTextPosition());
        ps.setString(13, b.getTitleColor());
        ps.setString(14, b.getSubtitleColor());
        ps.setBoolean(15, b.isVideoMutedDefault());
        ps.setBoolean(16, b.isVideoDisableSeek());
        ps.setBoolean(17, b.isVideoDisablePause());
    }

    public void delete(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("delete from hishopping_hall_banner where id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    private HallBanner map(ResultSet rs) throws SQLException {
        HallBanner b = new HallBanner();
        b.setId(rs.getInt("id"));
        b.setMediaType(rs.getString("media_type"));
        b.setMediaUrl(rs.getString("media_url"));
        b.setTitle(rs.getString("title"));
        b.setSubtitle(rs.getString("subtitle"));
        b.setEnabled(rs.getBoolean("enabled"));
        b.setSortNo(rs.getInt("sort_no"));
        b.setLinkEnabled(rs.getBoolean("link_enabled"));
        b.setLinkType(rs.getString("link_type"));
        b.setLinkTarget(rs.getString("link_target"));
        b.setProductId(rs.getInt("product_id"));
        b.setOverlayEnabled(rs.getBoolean("overlay_enabled"));
        b.setTextPosition(rs.getString("text_position"));
        b.setTitleColor(rs.getString("title_color"));
        b.setSubtitleColor(rs.getString("subtitle_color"));
        b.setVideoMutedDefault(rs.getBoolean("video_muted_default"));
        b.setVideoDisableSeek(rs.getBoolean("video_disable_seek"));
        b.setVideoDisablePause(rs.getBoolean("video_disable_pause"));
        b.setCreateTime(String.valueOf(rs.getObject("create_time")));
        b.setUpdateTime(String.valueOf(rs.getObject("update_time")));
        return b;
    }
}
