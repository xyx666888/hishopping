package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.dao.AccountRequestDao;
import hishopping.dao.MessageDao;
import hishopping.dao.MerchantDao;
import hishopping.dao.UserDao;
import hishopping.entity.Admin;
import hishopping.entity.Merchant;
import hishopping.entity.User;
import hishopping.service.MerchantService;
import hishopping.service.UserService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AccountRequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AccountRequestDao dao = new AccountRequestDao();
    private MessageDao messageDao = new MessageDao();
    private MerchantDao merchantDao = new MerchantDao();
    private UserDao userDao = new UserDao();
    private UserService userService = new UserService();
    private MerchantService merchantService = new MerchantService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean adminPath = request.getRequestURI().indexOf("/admin/") >= 0;
        Map<String, Object> result = ServletUtil.ok();
        if (adminPath) {
            if (ServletUtil.currentAdmin(request) == null) {
                JsonUtil.write(response, ServletUtil.fail("请先使用管理员账号登录。"));
                return;
            }
            result.put("requests", dao.all());
        } else {
            Identity identity = identity(request);
            if (identity == null) {
                JsonUtil.write(response, ServletUtil.fail("请先登录后查看申请。"));
                return;
            }
            result.put("requests", dao.mine(identity.role, identity.id));
        }
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        boolean adminPath = request.getRequestURI().indexOf("/admin/") >= 0;
        if (adminPath) {
            Admin admin = ServletUtil.currentAdmin(request);
            if (admin == null) {
                JsonUtil.write(response, ServletUtil.fail("请先使用管理员账号登录。"));
                return;
            }
            int requestId = ServletUtil.intParam(request, "requestId", 0);
            String status = "APPROVED".equals(request.getParameter("status")) ? "APPROVED" : "REJECTED";
            String opinion = text(request.getParameter("opinion"));
            Map<String, Object> item = dao.find(requestId);
            if (item == null) {
                JsonUtil.write(response, ServletUtil.fail("申请不存在。"));
                return;
            }
            dao.review(requestId, status, opinion, admin.getId());
            if ("APPROVED".equals(status)) applyApproved(item);
            messageDao.send("ADMIN", admin.getId(), admin.getRealName(), String.valueOf(item.get("actorRole")), ((Number) item.get("actorId")).intValue(), String.valueOf(item.get("actorName")), "账号申请审核结果", "您的申请《" + item.get("title") + "》已" + ("APPROVED".equals(status) ? "通过" : "驳回") + (opinion.length() > 0 ? "：" + opinion : "。"), "NONE", "");
            Map<String, Object> result = ServletUtil.ok();
            result.put("requests", dao.all());
            JsonUtil.write(response, result);
            return;
        }
        Identity identity = identity(request);
        if (identity == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录后提交申请。"));
            return;
        }
        String action = text(request.getParameter("action"));
        try {
            if ("profile".equals(action)) {
                Map<String, Object> result = ServletUtil.ok();
                if ("USER".equals(identity.role)) {
                    User updated = userService.updateOwnProfile(identity.id, request.getParameter("username"), request.getParameter("email"), request.getParameter("phone"), request.getParameter("oldPassword"), request.getParameter("password"));
                    request.getSession().setAttribute("user", updated);
                    result.put("user", ServletUtil.user(updated));
                } else {
                    Merchant current = merchantService.findById(identity.id);
                    Merchant merchant = new Merchant();
                    merchant.setMerchantId(identity.id);
                    merchant.setMerchantName(text(request.getParameter("merchantName")));
                    merchant.setPassword(text(request.getParameter("password")).length() == 0 && current != null ? current.getPassword() : text(request.getParameter("password")));
                    merchant.setContactName(text(request.getParameter("contactName")));
                    merchant.setContactPhone(text(request.getParameter("contactPhone")));
                    merchant.setEmail(text(request.getParameter("email")));
                    merchant.setShopName(text(request.getParameter("shopName")));
                    merchant.setShopDesc(text(request.getParameter("shopDesc")));
                    merchant.setBusinessCategory(text(request.getParameter("businessCategory")));
                    merchant.setBusinessAddress(text(request.getParameter("businessAddress")));
                    merchantService.updateProfile(merchant);
                    Merchant updated = merchantService.findById(identity.id);
                    request.getSession().setAttribute("merchant", updated);
                    result.put("merchant", ServletUtil.merchant(updated));
                }
                JsonUtil.write(response, result);
                return;
            }
            if ("cancel".equals(action)) {
                if ("USER".equals(identity.role)) {
                    userService.requestCancel(identity.id);
                    messageDao.send("SYSTEM", 0, "系统通知", "USER", identity.id, identity.name, "账号注销已进入冷静期", "你的账号已进入 7 天注销冷静期，7 天内重新登录会自动取消注销。", "NONE", "");
                } else {
                    merchantService.requestCancel(identity.id);
                    messageDao.send("SYSTEM", 0, "系统通知", "MERCHANT", identity.id, identity.name, "商家账号注销已进入冷静期", "你的商家账号已进入 7 天注销冷静期，7 天内重新登录会自动取消注销。", "NONE", "");
                }
                request.getSession().invalidate();
                JsonUtil.write(response, ServletUtil.ok());
                return;
            }
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
            return;
        }
        String type = normalizeType(request.getParameter("requestType"));
        String title = titleFor(type);
        String content = text(request.getParameter("content"));
        if (content.length() == 0) {
            JsonUtil.write(response, ServletUtil.fail("请填写申请说明。"));
            return;
        }
        String attachmentUrl = text(request.getParameter("attachmentUrl"));
        if ("AVATAR".equals(type) && attachmentUrl.length() == 0) {
            JsonUtil.write(response, ServletUtil.fail("请先上传头像图片。"));
            return;
        }
        dao.submit(identity.role, identity.id, identity.name, type, title, content, attachmentUrl);
        messageDao.send(identity.role, identity.id, identity.name, "ADMIN", 0, "管理员", "新的账号资料申请", identity.name + " 提交了《" + title + "》申请，请在后台审核。", "PAGE", "adminAccountRequests");
        Map<String, Object> result = ServletUtil.ok();
        result.put("requests", dao.mine(identity.role, identity.id));
        JsonUtil.write(response, result);
    }

    private void applyApproved(Map<String, Object> item) {
        String role = String.valueOf(item.get("actorRole"));
        int actorId = ((Number) item.get("actorId")).intValue();
        String type = String.valueOf(item.get("requestType"));
        if ("CANCEL".equals(type)) {
            if ("USER".equals(role)) merchantDao.rawUpdate("update hishopping_user set status=N'停用' where id=" + actorId);
            if ("MERCHANT".equals(role)) merchantDao.updateStatus(actorId, "DISABLED", "账号注销申请通过", 0);
        } else if ("AVATAR".equals(type)) {
            String avatarUrl = String.valueOf(item.get("attachmentUrl") == null ? "" : item.get("attachmentUrl"));
            if (avatarUrl.length() > 0 && "USER".equals(role)) userDao.updateAvatar(actorId, avatarUrl);
            if (avatarUrl.length() > 0 && "MERCHANT".equals(role)) merchantDao.updateAvatar(actorId, avatarUrl);
        }
    }

    private Identity identity(HttpServletRequest request) {
        User user = ServletUtil.currentUser(request);
        if (user != null) return new Identity("USER", user.getId(), user.getUsername());
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant != null) return new Identity("MERCHANT", merchant.getMerchantId(), merchant.getShopName());
        return null;
    }

    private String normalizeType(String type) {
        String value = text(type).toUpperCase();
        if ("AVATAR".equals(value) || "PROFILE".equals(value) || "CANCEL".equals(value) || "RESTORE".equals(value)) return value;
        return "PROFILE";
    }

    private String titleFor(String type) {
        if ("AVATAR".equals(type)) return "头像审核";
        if ("CANCEL".equals(type)) return "注销账号";
        if ("RESTORE".equals(type)) return "恢复账号";
        return "资料修改";
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private static class Identity {
        String role;
        int id;
        String name;
        Identity(String role, int id, String name) {
            this.role = role;
            this.id = id;
            this.name = name == null || name.trim().length() == 0 ? role : name;
        }
    }
}
