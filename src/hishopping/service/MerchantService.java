package hishopping.service;

import java.util.List;
import java.util.Random;

import hishopping.dao.MerchantDao;
import hishopping.entity.Merchant;

public class MerchantService {
    private MerchantDao merchantDao = new MerchantDao();
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
        if ("PENDING".equals(merchant.getStatus())) throw new RuntimeException("商家账号正在审核中，请等待管理员审核通过后再登录。");
        if ("REJECTED".equals(merchant.getStatus())) throw new RuntimeException("商家注册申请未通过，请联系管理员或重新提交资料。");
        if ("DISABLED".equals(merchant.getStatus())) throw new RuntimeException("商家账号已被禁用。");
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
    public void updateProfile(Merchant merchant) {
        if (merchant.getMerchantId() <= 0) throw new RuntimeException("\u8bf7\u9009\u62e9\u5546\u5bb6\u3002");
        require(merchant.getMerchantName(), "\u8bf7\u8f93\u5165\u5546\u5bb6\u540d\u79f0\u3002");
        require(merchant.getPassword(), "\u8bf7\u8f93\u5165\u767b\u5f55\u5bc6\u7801\u3002");
        require(merchant.getContactPhone(), "\u8bf7\u8f93\u5165\u7ed1\u5b9a\u624b\u673a\u53f7\u3002");
        require(merchant.getShopName(), "\u8bf7\u8f93\u5165\u5e97\u94fa\u540d\u79f0\u3002");
        merchantDao.updateProfile(merchant);
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
}
