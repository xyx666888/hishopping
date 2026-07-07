package hishopping.service;

import java.util.List;

import hishopping.dao.AccountRestrictionDao;
import hishopping.entity.AccountRestriction;

public class AccountRestrictionService {
    private AccountRestrictionDao dao = new AccountRestrictionDao();

    public List<AccountRestriction> active(String targetRole, int targetId) {
        return dao.active(normalizeRole(targetRole), targetId);
    }

    public List<AccountRestriction> history(String targetRole, int targetId) {
        return dao.history(normalizeRole(targetRole), targetId);
    }

    public AccountRestriction restricted(String targetRole, int targetId, String permissionKey) {
        if (targetId <= 0 || empty(permissionKey)) return null;
        return dao.activeOne(normalizeRole(targetRole), targetId, permissionKey.trim());
    }

    public boolean can(String targetRole, int targetId, String permissionKey) {
        return restricted(targetRole, targetId, permissionKey) == null;
    }

    public void require(String targetRole, int targetId, String permissionKey) {
        AccountRestriction r = restricted(targetRole, targetId, permissionKey);
        if (r != null) throw new RuntimeException(message(r));
    }

    public int restrict(String targetRole, int targetId, String permissionKey, String reason, String sourceType, int sourceId, Integer durationDays, int adminId, String adminName) {
        String role = normalizeRole(targetRole);
        if (!"USER".equals(role) && !"MERCHANT".equals(role)) throw new RuntimeException("限制对象类型不正确。");
        if (targetId <= 0) throw new RuntimeException("请选择限制对象。");
        if (empty(permissionKey)) throw new RuntimeException("请选择限制权限。");
        Integer days = durationDays == null || durationDays.intValue() <= 0 ? null : durationDays;
        return dao.add(role, targetId, permissionKey.trim(), empty(reason) ? "平台风控处理" : reason.trim(), empty(sourceType) ? "ADMIN" : sourceType.trim().toUpperCase(), sourceId, days, adminId, adminName);
    }

    public void cancel(int restrictionId) {
        if (restrictionId <= 0) throw new RuntimeException("请选择要解除的限制。");
        dao.cancel(restrictionId);
    }

    public String message(AccountRestriction r) {
        String text = r.getReason() == null || r.getReason().trim().length() == 0 ? "平台风控处理" : r.getReason().trim();
        String end = r.getEndTime() == null || r.getEndTime().trim().length() == 0 ? "永久限制" : "预计恢复：" + r.getEndTime().substring(0, Math.min(19, r.getEndTime().length()));
        return "当前权限受限：" + text + "，" + end + "。";
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase();
    }

    private boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
