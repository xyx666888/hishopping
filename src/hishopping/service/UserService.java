package hishopping.service;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import hishopping.dao.UserDao;
import hishopping.entity.Admin;
import hishopping.entity.User;

public class UserService {
    private static final int MAX_ACCOUNT_ID_RETRY = 30;
    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UserDao userDao = new UserDao();
    private Random random = new Random();
    private CouponService couponService = new CouponService();

    public User login(String account, String password) {
        if (empty(account) || empty(password)) {
            return null;
        }
        return userDao.findByLoginAndPassword(account.trim(), password.trim());
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
}
