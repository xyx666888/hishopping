package hishopping.service;

import java.util.List;

import hishopping.dao.BusinessDao;
import hishopping.dao.MessageDao;
import hishopping.dao.ProductDao;
import hishopping.dao.ReportDao;
import hishopping.entity.Report;

public class ReportService {
    private ReportDao dao = new ReportDao();
    private MessageDao messageDao = new MessageDao();
    private UserService userService = new UserService();
    private MerchantService merchantService = new MerchantService();
    private ProductDao productDao = new ProductDao();
    private BusinessService businessService = new BusinessService();
    private BusinessDao businessDao = new BusinessDao();

    public int create(Report report) {
        if (!validReporterRole(report.getReporterRole()) || report.getReporterId() <= 0) throw new RuntimeException("请先登录后再提交举报。");
        if (!validTargetRole(report.getTargetRole()) || report.getTargetId() <= 0) throw new RuntimeException("举报对象不正确。");
        if (empty(report.getReason())) throw new RuntimeException("请填写举报原因。");
        if (empty(report.getReportType())) report.setReportType("其他");
        report.setReason(limit(report.getReason(), 300));
        report.setDescription(limit(report.getDescription(), 1000));
        report.setEvidenceUrls(limit(report.getEvidenceUrls(), 2000));
        return dao.create(report);
    }

    public List<Report> myReports(String reporterRole, int reporterId) {
        if (!validReporterRole(reporterRole) || reporterId <= 0) throw new RuntimeException("请先登录后查看举报。");
        return dao.findByReporter(reporterRole, reporterId);
    }

    public List<Report> merchantRelated(int merchantId) {
        if (merchantId <= 0) throw new RuntimeException("请先登录商家账号。");
        return dao.findByMerchant(merchantId);
    }

    public List<Report> adminReports(String status, String keyword) {
        if (!empty(status) && !"all".equalsIgnoreCase(status) && !validStatus(status)) throw new RuntimeException("举报状态不正确。");
        return dao.findForAdmin(status, keyword);
    }

    public List<Report> adminReports(String status, String keyword, int page, int pageSize) {
        if (!empty(status) && !"all".equalsIgnoreCase(status) && !validStatus(status)) throw new RuntimeException("举报状态不正确。");
        return dao.findForAdmin(status, keyword, page, pageSize);
    }

    public int adminReportTotal(String status, String keyword) {
        if (!empty(status) && !"all".equalsIgnoreCase(status) && !validStatus(status)) throw new RuntimeException("举报状态不正确。");
        return dao.countForAdmin(status, keyword);
    }

    public Report detail(int reportId) {
        if (reportId <= 0) throw new RuntimeException("举报编号不正确。");
        Report report = dao.findById(reportId);
        if (report == null) throw new RuntimeException("举报记录不存在。");
        return report;
    }

    public Report handle(int reportId, String status, int adminId, String adminName, String opinion, String result) {
        if (reportId <= 0) throw new RuntimeException("举报编号不正确。");
        if (!validStatus(status) || "PENDING".equals(status)) throw new RuntimeException("处理状态不正确。");
        if (adminId <= 0) throw new RuntimeException("请先登录管理员账号。");
        dao.handle(reportId, status, adminId, adminName, limit(opinion, 500), limit(result, 500));
        Report report = detail(reportId);
        notifyReporter(report);
        return report;
    }

    public Report handle(int reportId, String status, int adminId, String adminName, String opinion, String result, String actionType, String punishTargetRole, int punishTargetId, Integer durationDays, String punishReason) {
        if (empty(actionType)) actionType = "RECORD_ONLY";
        Report report = detail(reportId);
        String targetRole = empty(punishTargetRole) ? suggestedTargetRole(report, actionType) : punishTargetRole.trim().toUpperCase();
        int targetId = punishTargetId > 0 ? punishTargetId : suggestedTargetId(report, targetRole, actionType);
        String reason = empty(punishReason) ? report.getReason() : punishReason.trim();
        String actionResult = applyAction(report, actionType, targetRole, targetId, durationDays, reason, adminId, adminName);
        String finalResult = empty(result) ? actionResult : result.trim() + (empty(actionResult) ? "" : "\n" + actionResult);
        dao.handle(reportId, status, adminId, adminName, limit(opinion, 500), limit(finalResult, 500));
        Report handled = detail(reportId);
        notifyReporter(handled);
        try {
            businessDao.logAdminOperation(adminId, "REPORT_HANDLE", "REPORT", reportId, actionText(actionType) + " " + targetRole + "#" + targetId + " " + reason);
        } catch (RuntimeException e) {
            System.err.println("[ReportService] 记录管理员操作日志失败：" + e.getMessage());
        }
        return handled;
    }

    private String applyAction(Report report, String actionType, String targetRole, int targetId, Integer durationDays, String reason, int adminId, String adminName) {
        if ("RECORD_ONLY".equals(actionType)) return "仅记录处理意见。";
        if ("WARN_USER".equals(actionType)) {
            requireTarget(targetRole, targetId, "USER");
            dao.createPunishment(report.getReportId(), "USER", targetId, actionType, null, reason, adminId, adminName);
            sendSystem("USER", targetId, "平台警告", "你的账号收到平台警告。原因：" + reason, "REPORT", String.valueOf(report.getReportId()));
            return "已警告用户 #" + targetId + "。";
        }
        if ("FREEZE_USER".equals(actionType) || "DISABLE_USER".equals(actionType) || "BAN_USER".equals(actionType)) {
            requireTarget(targetRole, targetId, "USER");
            Integer days = "BAN_USER".equals(actionType) ? null : normalizeDays(durationDays);
            String status = "BAN_USER".equals(actionType) ? "封禁" : ("FREEZE_USER".equals(actionType) ? "冻结" : "停用");
            userService.punishUser(targetId, status, reason, days);
            dao.createPunishment(report.getReportId(), "USER", targetId, actionType, days, reason, adminId, adminName);
            sendSystem("USER", targetId, "账号处理通知", "你的账号已被" + status + ("封禁".equals(status) ? "" : " " + days + " 天") + "。原因：" + reason, "REPORT", String.valueOf(report.getReportId()));
            return "已" + status + "用户 #" + targetId + ("封禁".equals(status) ? "" : " " + days + " 天") + "。";
        }
        if ("WARN_MERCHANT".equals(actionType)) {
            requireTarget(targetRole, targetId, "MERCHANT");
            dao.createPunishment(report.getReportId(), "MERCHANT", targetId, actionType, null, reason, adminId, adminName);
            sendSystem("MERCHANT", targetId, "平台警告", "你的店铺收到平台警告。原因：" + reason, "REPORT", String.valueOf(report.getReportId()));
            return "已警告商家 #" + targetId + "。";
        }
        if ("FREEZE_MERCHANT".equals(actionType) || "DISABLE_MERCHANT".equals(actionType) || "BAN_MERCHANT".equals(actionType)) {
            requireTarget(targetRole, targetId, "MERCHANT");
            Integer days = "BAN_MERCHANT".equals(actionType) ? null : normalizeDays(durationDays);
            String status = "BAN_MERCHANT".equals(actionType) ? "BANNED" : ("FREEZE_MERCHANT".equals(actionType) ? "FROZEN" : "DISABLED");
            String text = "BAN_MERCHANT".equals(actionType) ? "封禁" : ("FREEZE_MERCHANT".equals(actionType) ? "冻结" : "停用");
            merchantService.punishMerchant(targetId, status, reason, days, "BAN_MERCHANT".equals(actionType));
            dao.createPunishment(report.getReportId(), "MERCHANT", targetId, actionType, days, reason, adminId, adminName);
            sendSystem("MERCHANT", targetId, "店铺处理通知", "你的店铺已被" + text + ("BAN_MERCHANT".equals(actionType) ? "" : " " + days + " 天") + "。原因：" + reason, "REPORT", String.valueOf(report.getReportId()));
            return "已" + text + "商家 #" + targetId + ("BAN_MERCHANT".equals(actionType) ? "，并下架该商家全部商品。" : " " + days + " 天") + "。";
        }
        if ("OFF_SALE_PRODUCT".equals(actionType)) {
            requireTarget(targetRole, targetId, "PRODUCT");
            int merchantId = productDao.adminOffSale(targetId, reason, adminId);
            dao.createPunishment(report.getReportId(), "PRODUCT", targetId, actionType, null, reason, adminId, adminName);
            if (merchantId > 0) sendSystem("MERCHANT", merchantId, "商品下架通知", "平台已下架商品 #" + targetId + "。原因：" + reason, "REPORT", String.valueOf(report.getReportId()));
            return "已下架商品 #" + targetId + "。";
        }
        if ("HIDE_REVIEW".equals(actionType)) {
            requireTarget(targetRole, targetId, "REVIEW");
            int[] ids = businessService.hideReview(targetId);
            dao.createPunishment(report.getReportId(), "REVIEW", targetId, actionType, null, reason, adminId, adminName);
            if (ids[1] > 0) sendSystem("USER", ids[1], "评价处理通知", "你的商品评价已被平台隐藏。原因：" + reason, "REPORT", String.valueOf(report.getReportId()));
            return "已隐藏评价 #" + targetId + "。";
        }
        throw new RuntimeException("处理动作不正确。");
    }

    private void requireTarget(String actualRole, int actualId, String expectedRole) {
        if (!expectedRole.equals(actualRole) || actualId <= 0) throw new RuntimeException("处罚对象与处理动作不匹配。");
    }

    private Integer normalizeDays(Integer days) {
        if (days == null || days.intValue() <= 0) throw new RuntimeException("请选择处罚天数。");
        return Integer.valueOf(Math.min(3650, days.intValue()));
    }

    private String suggestedTargetRole(Report report, String actionType) {
        if (actionType.indexOf("_USER") >= 0) return "USER";
        if (actionType.indexOf("_MERCHANT") >= 0) return "MERCHANT";
        if ("OFF_SALE_PRODUCT".equals(actionType)) return "PRODUCT";
        if ("HIDE_REVIEW".equals(actionType)) return "REVIEW";
        return report.getTargetRole();
    }

    private int suggestedTargetId(Report report, String role, String actionType) {
        if ("USER".equals(role)) return report.getUserId() > 0 ? report.getUserId() : ("USER".equals(report.getTargetRole()) ? report.getTargetId() : 0);
        if ("MERCHANT".equals(role)) return report.getMerchantId() > 0 ? report.getMerchantId() : ("MERCHANT".equals(report.getTargetRole()) ? report.getTargetId() : 0);
        if ("PRODUCT".equals(role)) return report.getProductId() > 0 ? report.getProductId() : ("PRODUCT".equals(report.getTargetRole()) ? report.getTargetId() : 0);
        if ("REVIEW".equals(role)) return report.getReviewId() > 0 ? report.getReviewId() : ("REVIEW".equals(report.getTargetRole()) ? report.getTargetId() : 0);
        return report.getTargetId();
    }

    private void sendSystem(String receiverRole, int receiverId, String title, String content, String refType, String refId) {
        try {
            messageDao.send("SYSTEM", 0, "系统通知", receiverRole, receiverId, "", title, content, refType, refId);
        } catch (RuntimeException e) {
            System.err.println("[ReportService] 发送系统消息失败：" + e.getMessage());
        }
    }

    private String actionText(String actionType) {
        if ("WARN_USER".equals(actionType)) return "警告用户";
        if ("FREEZE_USER".equals(actionType)) return "冻结用户";
        if ("DISABLE_USER".equals(actionType)) return "停用用户";
        if ("BAN_USER".equals(actionType)) return "封禁用户";
        if ("WARN_MERCHANT".equals(actionType)) return "警告商家";
        if ("FREEZE_MERCHANT".equals(actionType)) return "冻结商家";
        if ("DISABLE_MERCHANT".equals(actionType)) return "停用商家";
        if ("BAN_MERCHANT".equals(actionType)) return "封禁商家";
        if ("OFF_SALE_PRODUCT".equals(actionType)) return "下架商品";
        if ("HIDE_REVIEW".equals(actionType)) return "隐藏评价";
        return "仅记录处理意见";
    }

    private void notifyReporter(Report report) {
        try {
            String title = "举报处理结果";
            String content = "举报 #" + report.getReportId() + " 已更新为 " + statusText(report.getStatus()) + "。处理意见：" + nullToEmpty(report.getHandleOpinion());
            messageDao.send("SYSTEM", 0, "系统通知", report.getReporterRole(), report.getReporterId(), report.getReporterName(), title, content, "REPORT", String.valueOf(report.getReportId()));
        } catch (RuntimeException e) {
            System.err.println("[ReportService] 通知举报人失败，reportId=" + report.getReportId() + ", reporter=" + report.getReporterRole() + "#" + report.getReporterId() + ", error=" + e.getMessage());
        }
    }

    private boolean validReporterRole(String role) {
        return "USER".equals(role) || "MERCHANT".equals(role) || "ADMIN".equals(role);
    }

    private boolean validTargetRole(String role) {
        return "USER".equals(role) || "MERCHANT".equals(role) || "PRODUCT".equals(role) || "ORDER".equals(role) || "REVIEW".equals(role);
    }

    private boolean validStatus(String status) {
        return "PENDING".equals(status) || "PROCESSING".equals(status) || "APPROVED".equals(status) || "REJECTED".equals(status) || "CLOSED".equals(status);
    }

    private String statusText(String status) {
        if ("PROCESSING".equals(status)) return "处理中";
        if ("APPROVED".equals(status)) return "已通过";
        if ("REJECTED".equals(status)) return "已驳回";
        if ("CLOSED".equals(status)) return "已关闭";
        return "待处理";
    }

    private boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private String limit(String value, int max) {
        String text = value == null ? "" : value.trim();
        return text.length() > max ? text.substring(0, max) : text;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
