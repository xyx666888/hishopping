package hishopping.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import hishopping.entity.Merchant;
import hishopping.entity.User;
import hishopping.service.MerchantService;
import hishopping.service.UserService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AvatarUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();
    private MerchantService merchantService = new MerchantService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (user == null && merchant == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录后上传头像。"));
            return;
        }
        Part part = request.getPart("avatar");
        if (part == null || part.getSize() == 0) {
            JsonUtil.write(response, ServletUtil.fail("请选择头像图片。"));
            return;
        }
        String submitted = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
        boolean image = submitted.endsWith(".jpg") || submitted.endsWith(".jpeg") || submitted.endsWith(".png") || submitted.endsWith(".webp") || submitted.endsWith(".gif");
        if (!image) {
            JsonUtil.write(response, ServletUtil.fail("头像仅支持 jpg、png、webp、gif 图片。"));
            return;
        }
        String ext = submitted.lastIndexOf('.') >= 0 ? submitted.substring(submitted.lastIndexOf('.')) : ".jpg";
        String dirPath = getServletContext().getRealPath("/assets/upload/avatar");
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        String fileName = "avatar-" + System.currentTimeMillis() + ext;
        File target = new File(dir, fileName);
        InputStream in = part.getInputStream();
        try {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            in.close();
        }
        try {
            String avatarUrl = "assets/upload/avatar/" + fileName;
            Map<String, Object> result = ServletUtil.ok();
            result.put("avatarUrl", avatarUrl);
            if (user != null) {
                User updated = userService.updateAvatar(user.getId(), avatarUrl);
                request.getSession().setAttribute("user", updated);
                result.put("user", ServletUtil.user(updated));
            } else {
                Merchant updated = merchantService.updateAvatar(merchant.getMerchantId(), avatarUrl);
                request.getSession().setAttribute("merchant", updated);
                result.put("merchant", ServletUtil.merchant(updated));
            }
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }
}
