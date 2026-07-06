package hishopping.servlet;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import hishopping.entity.User;
import hishopping.service.BusinessService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/reviewMediaUpload")
@MultipartConfig(maxFileSize = 80 * 1024 * 1024, maxRequestSize = 90 * 1024 * 1024, fileSizeThreshold = 1024 * 1024)
public class ReviewMediaUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_IMAGE_SIZE = 8L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 80L * 1024 * 1024;
    private static final int MAX_IMAGE_SIDE = 1200;
    private BusinessService businessService = new BusinessService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录普通用户账号。"));
            return;
        }
        int productId = ServletUtil.intParam(request, "productId", 0);
        if (productId <= 0) {
            JsonUtil.write(response, ServletUtil.fail("商品信息不正确。"));
            return;
        }
        Part part = request.getPart("media");
        if (part == null || part.getSize() == 0) part = request.getPart("file");
        if (part == null || part.getSize() == 0) {
            JsonUtil.write(response, ServletUtil.fail("请选择评价图片或视频。"));
            return;
        }

        String submitted = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
        boolean video = submitted.endsWith(".mp4") || submitted.endsWith(".webm");
        boolean image = submitted.endsWith(".jpg") || submitted.endsWith(".jpeg") || submitted.endsWith(".png") || submitted.endsWith(".webp") || submitted.endsWith(".gif");
        if (!video && !image) {
            JsonUtil.write(response, ServletUtil.fail("仅支持 jpg/jpeg/png/webp/gif 图片和 mp4/webm 视频。"));
            return;
        }
        if (image && part.getSize() > MAX_IMAGE_SIZE) {
            JsonUtil.write(response, ServletUtil.fail("评价图片不能超过 8MB。"));
            return;
        }
        if (video && part.getSize() > MAX_VIDEO_SIZE) {
            JsonUtil.write(response, ServletUtil.fail("评价视频不能超过 80MB。"));
            return;
        }

        String ext = extension(submitted, video ? ".mp4" : ".jpg");
        File dir = new File(getServletContext().getRealPath("/assets/upload/review"));
        if (!dir.exists()) dir.mkdirs();
        String fileName = "review-" + user.getId() + "-" + System.currentTimeMillis() + ext;
        File target = new File(dir, fileName);

        if (video || ".gif".equals(ext) || ".webp".equals(ext)) {
            copy(part, target);
        } else {
            BufferedImage source = ImageIO.read(part.getInputStream());
            if (source == null) {
                JsonUtil.write(response, ServletUtil.fail("图片内容无法识别。"));
                return;
            }
            BufferedImage scaled = scaleImage(source, ext);
            ImageIO.write(scaled, ".png".equals(ext) ? "png" : "jpg", target);
        }

        String mediaUrl = "assets/upload/review/" + fileName;
        int mediaId = businessService.saveReviewMedia("USER", user.getId(), productId, video ? "VIDEO" : "IMAGE", mediaUrl, fileName, target.length());
        Map<String, Object> ok = ServletUtil.ok();
        ok.put("mediaId", mediaId);
        ok.put("id", mediaId);
        ok.put("mediaType", video ? "VIDEO" : "IMAGE");
        ok.put("mediaUrl", mediaUrl);
        ok.put("fileName", fileName);
        ok.put("fileSize", target.length());
        JsonUtil.write(response, ok);
    }

    private String extension(String submitted, String fallback) {
        int dot = submitted.lastIndexOf('.');
        return dot >= 0 ? submitted.substring(dot) : fallback;
    }

    private void copy(Part part, File target) throws IOException {
        InputStream in = part.getInputStream();
        try {
            Files.copy(in, target.toPath());
        } finally {
            in.close();
        }
    }

    private BufferedImage scaleImage(BufferedImage source, String ext) {
        int width = source.getWidth();
        int height = source.getHeight();
        double scale = Math.min(1.0, (double) MAX_IMAGE_SIDE / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        int type = ".png".equals(ext) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return target;
    }
}
