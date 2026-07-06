package hishopping.service;

import java.util.List;

import hishopping.dao.MessageDao;
import hishopping.dao.ReportDao;
import hishopping.entity.Report;

public class ReportService {
    private ReportDao dao = new ReportDao();
    private MessageDao messageDao = new MessageDao();

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

    private void notifyReporter(Report report) {
        try {
            String title = "举报处理结果";
            String content = "举报 #" + report.getReportId() + " 已更新为 " + statusText(report.getStatus()) + "。处理意见：" + nullToEmpty(report.getHandleOpinion());
            messageDao.send("SYSTEM", 0, "系统通知", report.getReporterRole(), report.getReporterId(), report.getReporterName(), title, content, "REPORT", String.valueOf(report.getReportId()));
        } catch (RuntimeException ignored) {
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
