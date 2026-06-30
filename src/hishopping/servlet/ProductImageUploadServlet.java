package hishopping.servlet;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import hishopping.entity.Merchant;
import hishopping.service.BusinessService;
import hishopping.service.MerchantService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

@WebServlet("/merchant/productImage")
@MultipartConfig(maxFileSize = 80 * 1024 * 1024, maxRequestSize = 90 * 1024 * 1024, fileSizeThreshold = 1024 * 1024)
public class ProductImageUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_IMAGE_SIZE = 8L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 80L * 1024 * 1024;
    private static final int MAX_IMAGE_SIDE = 1200;
    private BusinessService businessService = new BusinessService();
    private MerchantService merchantService = new MerchantService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("Please login as merchant first."));
            return;
        }
        Merchant refreshed = merchantService.findById(merchant.getMerchantId());
        if (refreshed == null || !"APPROVED".equals(refreshed.getStatus())) {
            JsonUtil.write(response, ServletUtil.fail("Merchant account is not approved for product media upload."));
            return;
        }
        request.getSession().setAttribute("merchant", refreshed);
        merchant = refreshed;

        Part part = mediaPart(request);
        if (part == null || part.getSize() == 0) {
            JsonUtil.write(response, ServletUtil.fail("Please choose a product image or video."));
            return;
        }

        String submitted = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
        boolean video = submitted.endsWith(".mp4") || submitted.endsWith(".webm");
        boolean image = submitted.endsWith(".jpg") || submitted.endsWith(".jpeg") || submitted.endsWith(".png") || submitted.endsWith(".webp") || submitted.endsWith(".gif");
        if (!video && !image) {
            JsonUtil.write(response, ServletUtil.fail("Only jpg/jpeg/png/webp/gif images and mp4/webm videos are supported."));
            return;
        }
        if (image && part.getSize() > MAX_IMAGE_SIZE) {
            JsonUtil.write(response, ServletUtil.fail("Product image cannot exceed 8MB."));
            return;
        }
        if (video && part.getSize() > MAX_VIDEO_SIZE) {
            JsonUtil.write(response, ServletUtil.fail("Product video cannot exceed 80MB."));
            return;
        }

        String ext = extension(submitted, video ? ".mp4" : ".jpg");
        String dirPath = getServletContext().getRealPath("/assets/upload/product");
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        String fileName = "product-" + merchant.getMerchantId() + "-" + System.currentTimeMillis() + ext;
        File target = new File(dir, fileName);

        if (video || ".gif".equals(ext) || ".webp".equals(ext)) {
            copy(part, target);
        } else {
            BufferedImage source = ImageIO.read(part.getInputStream());
            if (source == null) {
                JsonUtil.write(response, ServletUtil.fail("Unsupported image content."));
                return;
            }
            BufferedImage scaled = scaleImage(source, ext);
            String format = ".png".equals(ext) ? "png" : "jpg";
            ImageIO.write(scaled, format, target);
        }

        java.util.Map<String, Object> result = ServletUtil.ok();
        String mediaUrl = "assets/upload/product/" + fileName;
        int resourceId = businessService.saveUpload(merchant.getMerchantId(), fileName, mediaUrl, target.length());
        result.put("mediaUrl", mediaUrl);
        result.put("mediaType", video ? "VIDEO" : "IMAGE");
        result.put("imageUrl", mediaUrl);
        result.put("resourceId", resourceId);
        JsonUtil.write(response, result);
    }

    private Part mediaPart(HttpServletRequest request) throws IOException, ServletException {
        Part part = request.getPart("media");
        return part == null ? request.getPart("image") : part;
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
