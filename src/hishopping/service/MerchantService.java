package hishopping.service;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import hishopping.dao.MerchantDao;
import hishopping.dao.ReportDao;
import hishopping.entity.Merchant;

public class MerchantService {
    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private MerchantDao merchantDao = new MerchantDao();
    private ReportDao reportDao = new ReportDao();
    private AccountRestrictionService restrictionService = new AccountRestrictionService();
    private Random random = new Random();

    public Merchant register(Merchant merchant) {
        require(merchant.getMerchantName(), "请输入商家名称。");
        require(merchant.getPassword(), "请输入登录密码。");
        require(merchant.getContactName(), "请输入联系人姓名。");
        require(merchant.getContactPhone(), "请输入联系电话。");
        require(merchant.getShopName(), "请输入店铺名称。");
        merchant.setMerchantCode(generateCode());
        merchant.setRegisterPasswordDemo(merchant.getPassword());
        return merchantDao.save(merchant);
    }

    public Merchant login(String account, String password) {
        require(account, "请输入商家独立ID或商家账号。");
        require(password, "请输入密码。");
        Merchant merchant = merchantDao.findByLogin(account.trim(), password.trim());
        if (merchant == null) throw new RuntimeException("商家账号或密码错误。");
        if ("CANCEL_PENDING".equals(merchant.getStatus())) {
            if (expired(merchant.getCancelDeadlineTime())) {
                merchantDao.finishExpiredCancel(merchant.getMerchantId());
                throw new RuntimeException("商家账号已注销，无法登录。");
            }
            merchantDao.cancelPendingCancel(merchant.getMerchantId());
            return merchantDao.findById(merchant.getMerchantId());
        }
        if ("CANCELLED".equals(merchant.getStatus())) throw new RuntimeException("商家账号已注销，无法登录。");
        if (isTemporaryBlocked(merchant.getStatus()) && expired(merchant.getPunishEndTime())) {
            merchantDao.restorePunishmentIfExpired(merchant.getMerchantId());
            reportDao.expirePunishments("MERCHANT", merchant.getMerchantId());
            merchant = merchantDao.findById(merchant.getMerchantId());
        }
        if ("PENDING".equals(merchant.getStatus())) throw new RuntimeException("商家账号正在审核中，请等待管理员审核通过后再登录。");
        if ("REJECTED".equals(merchant.getStatus())) throw new RuntimeException("商家注册申请未通过，请联系管理员或重新提交资料。");
        if ("FROZEN".equals(merchant.getStatus())) throw new RuntimeException("商家账号已被冻结，预计恢复时间：" + safeTime(merchant.getPunishEndTime()) + "。冻结原因：" + safeReason(merchant.getPunishReason()));
        if ("DISABLED".equals(merchant.getStatus())) throw new RuntimeException("商家账号已被停用，预计恢复时间：" + safeTime(merchant.getPunishEndTime()) + "。停用原因：" + safeReason(merchant.getPunishReason()));
        if ("BANNED".equals(merchant.getStatus())) throw new RuntimeException("商家账号已被封禁。封禁原因：" + safeReason(merchant.getPunishReason()));
        restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_login");
        return merchant;
    }

    public Merchant findById(int id) {
        return merchantDao.findById(id);
    }

    public List<Merchant> merchants() {
        return merchantDao.findAll();
    }

    public List<Merchant> findByContact(String contact) {
        require(contact, "请输入注册时绑定的邮箱或手机号。");
        return merchantDao.findByContact(contact.trim());
    }

    public void audit(int merchantId, String status, String opinion, int adminId) {
        if (merchantId <= 0) throw new RuntimeException("请选择商家。");
        merchantDao.updateStatus(merchantId, status, opinion, adminId);
    }

    public void punishMerchant(int merchantId, String status, String reason, Integer durationDays, boolean offSaleProducts) {
        if (merchantId <= 0) throw new RuntimeException("请选择商家。");
        if (!"FROZEN".equals(status) && !"DISABLED".equals(status) && !"BANNED".equals(status)) throw new RuntimeException("商家处罚状态不正确。");
        if (!"BANNED".equals(status) && (durationDays == null || durationDays.intValue() <= 0)) throw new RuntimeException("冻结/停用商家需要填写处罚天数。");
        merchantDao.applyPunishment(merchantId, status, reason, "BANNED".equals(status) ? null : durationDays, offSaleProducts);
    }

    public void updateProfile(Merchant merchant) {
        updateProfile(merchant, true);
    }

    public void updateProfile(Merchant merchant, boolean checkRestriction) {
        if (merchant.getMerchantId() <= 0) throw new RuntimeException("\u8bf7\u9009\u62e9\u5546\u5bb6\u3002");
        Merchant current = merchantDao.findById(merchant.getMerchantId());
        if (current == null) throw new RuntimeException("商家不存在。");
        if (checkRestriction) restrictionService.require("MERCHANT", merchant.getMerchantId(), "can_edit_profile");
        if (blank(merchant.getPassword())) merchant.setPassword(current.getPassword());
        if (blank(merchant.getContactName())) merchant.setContactName(current.getContactName());
        if (blank(merchant.getShopDesc())) merchant.setShopDesc(current.getShopDesc());
        require(merchant.getMerchantName(), "\u8bf7\u8f93\u5165\u5546\u5bb6\u540d\u79f0\u3002");
        require(merchant.getPassword(), "\u8bf7\u8f93\u5165\u767b\u5f55\u5bc6\u7801\u3002");
        require(merchant.getContactName(), "请输入联系人姓名。");
        require(merchant.getContactPhone(), "\u8bf7\u8f93\u5165\u7ed1\u5b9a\u624b\u673a\u53f7\u3002");
        require(merchant.getShopName(), "\u8bf7\u8f93\u5165\u5e97\u94fa\u540d\u79f0\u3002");
        if (!validMainlandPhone(merchant.getContactPhone().trim())) throw new RuntimeException("请输入有效的中国大陆手机号。");
        if (!blank(merchant.getEmail()) && !validEmail(merchant.getEmail().trim())) throw new RuntimeException("请输入正确的邮箱格式。");
        merchantDao.updateProfile(merchant);
    }

    public Merchant updateAvatar(int merchantId, String avatarUrl) {
        if (merchantId <= 0 || avatarUrl == null || avatarUrl.trim().length() == 0) throw new RuntimeException("头像地址无效。");
        restrictionService.require("MERCHANT", merchantId, "can_edit_avatar");
        merchantDao.updateAvatar(merchantId, avatarUrl.trim());
        return merchantDao.findById(merchantId);
    }

    public void requestCancel(int merchantId) {
        if (merchantId <= 0) throw new RuntimeException("商家编号不正确。");
        restrictionService.require("MERCHANT", merchantId, "can_cancel_account");
        merchantDao.requestCancel(merchantId);
    }

    private String generateCode() {
        for (int i = 0; i < 40; i++) {
            int len = 6 + random.nextInt(3);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) sb.append(random.nextInt(10));
            String code = sb.toString();
            if (!merchantDao.existsCode(code)) return code;
        }
        throw new RuntimeException("生成商家独立ID失败，请重试。");
    }

    private void require(String value, String message) {
        if (value == null || value.trim().length() == 0) throw new RuntimeException(message);
    }

    private boolean blank(String value) {
        return value == null || value.trim().length() == 0;
    }

    private boolean validEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean validMainlandPhone(String phone) {
        return phone != null && MAINLAND_PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isTemporaryBlocked(String status) {
        return "FROZEN".equals(status) || "DISABLED".equals(status);
    }

    private boolean expired(String time) {
        if (time == null || time.trim().length() == 0) return false;
        try {
            java.sql.Timestamp ts = java.sql.Timestamp.valueOf(time.substring(0, Math.min(19, time.length())));
            return ts.getTime() <= System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    private String safeTime(String value) {
        return value == null || value.trim().length() == 0 ? "未知" : value.substring(0, Math.min(19, value.length()));
    }

    private String safeReason(String value) {
        return value == null || value.trim().length() == 0 ? "平台风控处理" : value;
    }
}
