package hishopping.service;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import hishopping.dao.UserDao;
import hishopping.dao.ReportDao;
import hishopping.entity.Admin;
import hishopping.entity.User;

public class UserService {
    private static final int MAX_ACCOUNT_ID_RETRY = 30;
    private static final String DELETED_STATUS = "\u5df2\u5220\u9664";
    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UserDao userDao = new UserDao();
    private ReportDao reportDao = new ReportDao();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();
    private Random random = new Random();
    private CouponService couponService = new CouponService();

    public User login(String account, String password) {
        if (empty(account) || empty(password)) {
            return null;
        }
        User user = userDao.findAnyByLoginAndPassword(account.trim(), password.trim());
        if (user == null) return null;
        if ("注销中".equals(user.getStatus())) {
            if (expired(user.getCancelDeadlineTime())) {
                userDao.finishExpiredCancel(user.getId());
                throw new RuntimeException("账号已注销，无法登录。");
            }
            userDao.cancelPendingCancel(user.getId());
            return userDao.findById(user.getId());
        }
        if ("已注销".equals(user.getStatus())) {
            throw new RuntimeException("账号已注销，无法登录。");
        }
        if (isTemporaryBlocked(user.getStatus()) && expired(user.getPunishEndTime())) {
            userDao.restorePunishmentIfExpired(user.getId());
            reportDao.expirePunishments("USER", user.getId());
            return userDao.findById(user.getId());
        }
        restrictionService.require("USER", user.getId(), "can_login");
        if ("冻结".equals(user.getStatus())) {
            throw new RuntimeException("账号已被冻结，预计恢复时间：" + safeTime(user.getPunishEndTime()) + "。冻结原因：" + safeReason(user.getPunishReason()));
        }
        if ("停用".equals(user.getStatus())) {
            throw new RuntimeException("账号已被停用，预计恢复时间：" + safeTime(user.getPunishEndTime()) + "。停用原因：" + safeReason(user.getPunishReason()));
        }
        if ("封禁".equals(user.getStatus())) {
            throw new RuntimeException("账号已被封禁。封禁原因：" + safeReason(user.getPunishReason()));
        }
        if (DELETED_STATUS.equals(user.getStatus())) return null;
        return user;
    }

    public Admin adminLogin(String adminName, String password) {
        if (empty(adminName) || empty(password)) {
            return null;
        }
        return userDao.findAdmin(adminName.trim(), password.trim());
    }

    public User register(String username, String email, String phone, String password) {
        if (empty(username) || empty(email) || empty(phone) || empty(password)) {
            throw new RuntimeException("\u7528\u6237\u540d\u3001\u90ae\u7bb1\u3001\u624b\u673a\u53f7\u548c\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        String cleanEmail = email.trim();
        String cleanPhone = phone.trim();
        if (!validEmail(cleanEmail)) {
            throw new RuntimeException("\u8bf7\u8f93\u5165\u6b63\u786e\u7684\u90ae\u7bb1\u683c\u5f0f\u3002");
        }
        if (!validMainlandPhone(cleanPhone)) {
            throw new RuntimeException("\u8bf7\u8f93\u5165\u6709\u6548\u7684\u4e2d\u56fd\u5927\u9646\u624b\u673a\u53f7\u3002");
        }
        if (userDao.existsEmail(cleanEmail)) {
            throw new RuntimeException("\u8be5\u90ae\u7bb1\u5df2\u6ce8\u518c\u3002");
        }
        if (userDao.existsPhone(cleanPhone)) {
            throw new RuntimeException("\u8be5\u624b\u673a\u53f7\u5df2\u6ce8\u518c\u3002");
        }
        User user = new User();
        user.setAccountId(generateUniqueAccountId());
        user.setUsername(username.trim());
        user.setEmail(cleanEmail);
        user.setPhone(cleanPhone);
        user.setPassword(password.trim());
        User saved = userDao.save(user);
        couponService.issueNewUserCoupons(saved.getId());
        return saved;
    }

    public List<User> users() {
        return userDao.findAllUsers();
    }

    public User findById(int id) {
        return userDao.findById(id);
    }

    public void updateUser(int id, String username, String email, String phone, int growthValue, int points, String status) {
        updateUser(id, username, email, phone, growthValue, points, status, null);
    }

    public void updateUser(int id, String username, String email, String phone, int growthValue, int points, String status, String newPassword) {
        if (id <= 0 || empty(username) || empty(email) || empty(phone)) {
            throw new RuntimeException("\u7528\u6237\u7f16\u53f7\u3001\u7528\u6237\u540d\u3001\u90ae\u7bb1\u548c\u624b\u673a\u53f7\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        if (!empty(newPassword) && newPassword.trim().length() < 6) {
            throw new RuntimeException("\u65b0\u5bc6\u7801\u81f3\u5c11\u9700\u89816\u4f4d\u3002");
        }
        String cleanEmail = email.trim();
        String cleanPhone = phone.trim();
        if (!validEmail(cleanEmail)) {
            throw new RuntimeException("\u8bf7\u8f93\u5165\u6b63\u786e\u7684\u90ae\u7bb1\u683c\u5f0f\u3002");
        }
        if (!validMainlandPhone(cleanPhone)) {
            throw new RuntimeException("\u8bf7\u8f93\u5165\u6709\u6548\u7684\u4e2d\u56fd\u5927\u9646\u624b\u673a\u53f7\u3002");
        }
        if (userDao.existsEmailExceptUser(cleanEmail, id)) {
            throw new RuntimeException("\u8be5\u90ae\u7bb1\u5df2\u88ab\u5176\u4ed6\u8d26\u53f7\u7ed1\u5b9a\u3002");
        }
        if (userDao.existsPhoneExceptUser(cleanPhone, id)) {
            throw new RuntimeException("\u8be5\u624b\u673a\u53f7\u5df2\u88ab\u5176\u4ed6\u8d26\u53f7\u7ed1\u5b9a\u3002");
        }
        User user = new User();
        user.setId(id);
        user.setUsername(username.trim());
        user.setEmail(cleanEmail);
        user.setPhone(cleanPhone);
        user.setPoints(Math.max(0, points));
        user.setGrowthValue(Math.max(0, growthValue));
        user.setVipLevel(UserDao.calculateVipLevel(Math.max(0, growthValue)));
        user.setStatus(empty(status) ? "\u6b63\u5e38" : status.trim());
        userDao.updateUser(user, newPassword);
    }

    public User updateOwnProfile(int id, String username, String email, String phone, String oldPassword, String newPassword) {
        if (id <= 0 || empty(username) || empty(email) || empty(phone)) {
            throw new RuntimeException("用户名、邮箱和手机号不能为空。");
        }
        restrictionService.require("USER", id, "can_edit_profile");
        String cleanEmail = email.trim();
        String cleanPhone = phone.trim();
        if (!validEmail(cleanEmail)) throw new RuntimeException("请输入正确的邮箱格式。");
        if (!validMainlandPhone(cleanPhone)) throw new RuntimeException("请输入有效的中国大陆手机号。");
        if (userDao.existsEmailExceptUser(cleanEmail, id)) throw new RuntimeException("该邮箱已被其他账号绑定。");
        if (userDao.existsPhoneExceptUser(cleanPhone, id)) throw new RuntimeException("该手机号已被其他账号绑定。");
        User existing = userDao.findById(id);
        if (existing == null || DELETED_STATUS.equals(existing.getStatus())) throw new RuntimeException("用户不存在。");
        String password = null;
        if (!empty(newPassword)) {
            if (newPassword.trim().length() < 6) throw new RuntimeException("新密码至少需要6位。");
            if (empty(oldPassword) || !existing.getPassword().equals(oldPassword.trim())) throw new RuntimeException("旧密码不正确。");
            password = newPassword.trim();
        }
        userDao.updateOwnProfile(id, username.trim(), cleanEmail, cleanPhone, password);
        return userDao.findById(id);
    }

    public User updateAvatar(int id, String avatarUrl) {
        if (id <= 0 || empty(avatarUrl)) throw new RuntimeException("头像地址无效。");
        restrictionService.require("USER", id, "can_edit_avatar");
        userDao.updateAvatar(id, avatarUrl.trim());
        return userDao.findById(id);
    }

    public void requestCancel(int id) {
        if (id <= 0) throw new RuntimeException("用户编号不正确。");
        restrictionService.require("USER", id, "can_cancel_account");
        userDao.requestCancel(id);
    }

    public void deleteUser(int id) {
        if (id <= 0) {
            throw new RuntimeException("\u7528\u6237\u7f16\u53f7\u4e0d\u6b63\u786e\u3002");
        }
        User user = userDao.findById(id);
        if (user == null || DELETED_STATUS.equals(user.getStatus())) {
            throw new RuntimeException("\u7528\u6237\u4e0d\u5b58\u5728\u3002");
        }
        if (!"user".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("\u4e0d\u5141\u8bb8\u5220\u9664\u7ba1\u7406\u5458\u6216\u975e\u666e\u901a\u7528\u6237\u8d26\u53f7\u3002");
        }
        userDao.markDeleted(id);
    }

    public void restoreUser(int id) {
        if (id <= 0) {
            throw new RuntimeException("\u7528\u6237\u7f16\u53f7\u4e0d\u6b63\u786e\u3002");
        }
        User user = userDao.findById(id);
        if (user == null || DELETED_STATUS.equals(user.getStatus())) {
            throw new RuntimeException("\u7528\u6237\u4e0d\u5b58\u5728\u3002");
        }
        if (!"user".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("\u4e0d\u5141\u8bb8\u64cd\u4f5c\u7ba1\u7406\u5458\u6216\u975e\u666e\u901a\u7528\u6237\u8d26\u53f7\u3002");
        }
        userDao.updateStatus(id, "\u6b63\u5e38");
    }

    public void punishUser(int userId, String status, String reason, Integer durationDays) {
        if (userId <= 0) throw new RuntimeException("请选择用户。");
        if (!"冻结".equals(status) && !"停用".equals(status) && !"封禁".equals(status)) throw new RuntimeException("用户处罚状态不正确。");
        if (!"封禁".equals(status) && (durationDays == null || durationDays.intValue() <= 0)) throw new RuntimeException("冻结/停用用户需要填写处罚天数。");
        userDao.applyPunishment(userId, status, reason, "封禁".equals(status) ? null : durationDays);
    }

    private String generateUniqueAccountId() {
        for (int i = 0; i < MAX_ACCOUNT_ID_RETRY; i++) {
            String accountId = randomAccountId();
            if (!userDao.existsAccountId(accountId)) {
                return accountId;
            }
        }
        throw new RuntimeException("\u751f\u6210\u7528\u6237ID\u5931\u8d25\uff0c\u8bf7\u91cd\u65b0\u6ce8\u518c\u3002");
    }

    private String randomAccountId() {
        int length = 6 + random.nextInt(3);
        StringBuilder builder = new StringBuilder();
        builder.append(1 + random.nextInt(9));
        for (int i = 1; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private boolean validEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean validMainlandPhone(String phone) {
        return phone != null && MAINLAND_PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isTemporaryBlocked(String status) {
        return "冻结".equals(status) || "停用".equals(status);
    }

    private boolean expired(String time) {
        if (empty(time)) return false;
        try {
            java.sql.Timestamp ts = java.sql.Timestamp.valueOf(time.substring(0, Math.min(19, time.length())));
            return ts.getTime() <= System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    private String safeTime(String value) {
        return empty(value) ? "未知" : value.substring(0, Math.min(19, value.length()));
    }

    private String safeReason(String value) {
        return empty(value) ? "平台风控处理" : value;
    }
}
