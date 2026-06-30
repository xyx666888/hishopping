package hishopping.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class HallBannerUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_MEDIA_SIZE = 300L * 1024 * 1024;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletUtil.currentAdmin(request) == null) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u5148\u4f7f\u7528\u7ba1\u7406\u5458\u8d26\u53f7\u767b\u5f55\u3002"));
            return;
        }
        Part part = request.getPart("media");
        if (part == null || part.getSize() == 0) {
            JsonUtil.write(response, ServletUtil.fail("\u8bf7\u9009\u62e9\u5927\u5385\u5c55\u793a\u56fe\u7247\u6216\u89c6\u9891\u3002"));
            return;
        }
        if (part.getSize() > MAX_MEDIA_SIZE) {
            JsonUtil.write(response, ServletUtil.fail("\u5927\u5385\u5c55\u793a\u5a92\u4f53\u4e0d\u80fd\u8d85\u8fc7 300MB\uff0c\u8bf7\u538b\u7f29\u540e\u91cd\u65b0\u4e0a\u4f20\u3002"));
            return;
        }
        String submitted = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
        boolean video = submitted.endsWith(".mp4") || submitted.endsWith(".webm") || submitted.endsWith(".mov");
        boolean image = submitted.endsWith(".jpg") || submitted.endsWith(".jpeg") || submitted.endsWith(".png") || submitted.endsWith(".webp") || submitted.endsWith(".gif");
        if (!video && !image) {
            JsonUtil.write(response, ServletUtil.fail("\u4ec5\u652f\u6301 jpg/png/webp/gif \u56fe\u7247\u548c mp4/webm/mov \u89c6\u9891\u3002"));
            return;
        }
        String ext = submitted.lastIndexOf('.') >= 0 ? submitted.substring(submitted.lastIndexOf('.')) : (video ? ".mp4" : ".jpg");
        String dirPath = getServletContext().getRealPath("/assets/upload/hall");
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        String fileName = "hall-" + System.currentTimeMillis() + ext;
        File target = new File(dir, fileName);
        InputStream in = part.getInputStream();
        try {
            Files.copy(in, target.toPath());
        } finally {
            in.close();
        }
        java.util.Map<String, Object> result = ServletUtil.ok();
        result.put("mediaUrl", "assets/upload/hall/" + fileName);
        result.put("mediaType", video ? "VIDEO" : "IMAGE");
        JsonUtil.write(response, result);
    }
}
