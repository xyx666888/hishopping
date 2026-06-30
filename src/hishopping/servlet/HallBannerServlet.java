package hishopping.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.HallBanner;
import hishopping.service.HallBannerService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class HallBannerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private HallBannerService service = new HallBannerService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean adminPath = request.getRequestURI().indexOf("/admin/") >= 0;
        if (adminPath && ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u5148\u4f7f\u7528\u7ba1\u7406\u5458\u8d26\u53f7\u767b\u5f55\u3002"));
            return;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("banners", ServletUtil.hallBanners(adminPath ? service.all() : service.enabled()));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u5148\u4f7f\u7528\u7ba1\u7406\u5458\u8d26\u53f7\u767b\u5f55\u3002"));
            return;
        }
        request.setCharacterEncoding("UTF-8");
        try {
            String action = request.getParameter("action");
            if ("delete".equals(action)) {
                service.delete(ServletUtil.intParam(request, "id", 0));
            } else {
                HallBanner banner = new HallBanner();
                banner.setId(ServletUtil.intParam(request, "id", 0));
                banner.setMediaType(value(request, "mediaType", "IMAGE"));
                banner.setMediaUrl(request.getParameter("mediaUrl"));
                banner.setTitle(request.getParameter("title"));
                banner.setSubtitle(request.getParameter("subtitle"));
                banner.setEnabled(bool(request, "enabled"));
                banner.setSortNo(ServletUtil.intParam(request, "sortNo", 0));
                banner.setLinkEnabled(bool(request, "linkEnabled"));
                banner.setLinkType(value(request, "linkType", "NONE"));
                banner.setLinkTarget(request.getParameter("linkTarget"));
                banner.setProductId(ServletUtil.intParam(request, "productId", 0));
                banner.setOverlayEnabled(bool(request, "overlayEnabled"));
                banner.setTextPosition(value(request, "textPosition", "LEFT"));
                banner.setTitleColor(color(request.getParameter("titleColor"), "#ffffff"));
                banner.setSubtitleColor(color(request.getParameter("subtitleColor"), "#e2e8f0"));
                banner.setVideoMutedDefault(bool(request, "videoMutedDefault"));
                banner.setVideoDisableSeek(bool(request, "videoDisableSeek"));
                banner.setVideoDisablePause(bool(request, "videoDisablePause"));
                service.save(banner);
            }
            Map<String, Object> result = ServletUtil.ok();
            result.put("banners", ServletUtil.hallBanners(service.all()));
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private boolean bool(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }

    private String value(HttpServletRequest request, String name, String fallback) {
        String value = request.getParameter(name);
        return value == null || value.trim().length() == 0 ? fallback : value.trim();
    }

    private String color(String value, String fallback) {
        if (value == null) return fallback;
        String text = value.trim();
        return text.matches("^#[0-9a-fA-F]{6}$") ? text : fallback;
    }
}
