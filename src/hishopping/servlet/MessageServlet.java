package hishopping.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import hishopping.dao.MessageDao;
import hishopping.dao.OrderDao;
import hishopping.dao.ProductDao;
import hishopping.entity.Admin;
import hishopping.entity.Merchant;
import hishopping.entity.Order;
import hishopping.entity.OrderItem;
import hishopping.entity.Product;
import hishopping.entity.User;
import hishopping.service.MerchantService;
import hishopping.service.UserService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 120L * 1024 * 1024;
    private MessageDao messageDao = new MessageDao();
    private OrderDao orderDao = new OrderDao();
    private ProductDao productDao = new ProductDao();
    private UserService userService = new UserService();
    private MerchantService merchantService = new MerchantService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Identity identity = identity(request);
        if (identity == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录后查看消息。"));
            return;
        }
        String action = trim(request.getParameter("action"));
        try {
            Map<String, Object> result = ServletUtil.ok();
            if ("messages".equals(action)) {
                int conversationId = ServletUtil.intParam(request, "conversationId", 0);
                result.put("messages", messageDao.messages(identity.role, identity.id, conversationId));
                messageDao.markConversationRead(conversationId, identity.role, identity.id);
                result.put("unreadCount", messageDao.unreadCount(identity.role, identity.id));
            } else if ("unread".equals(action)) {
                result.put("unreadCount", messageDao.unreadCount(identity.role, identity.id));
            } else if ("targets".equals(action)) {
                result.put("targets", messageDao.targets(identity.role, identity.id, trim(request.getParameter("keyword")), normalizeRole(request.getParameter("targetRole"), "")));
            } else {
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
                result.put("unreadCount", messageDao.unreadCount(identity.role, identity.id));
            }
            JsonUtil.write(response, result);
        } catch (Exception e) {
            JsonUtil.write(response, ServletUtil.fail(rootMessage(e)));
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Identity identity = identity(request);
        if (identity == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录后操作消息。"));
            return;
        }
        if (!identity.canSend()) {
            JsonUtil.write(response, ServletUtil.fail("当前账号状态不可发送消息。"));
            return;
        }
        String action = trim(request.getParameter("action"));
        try {
            Map<String, Object> result = ServletUtil.ok();
            if ("start".equals(action)) {
                String targetRole = normalizeRole(request.getParameter("targetRole"), "USER");
                int targetId = ServletUtil.intParam(request, "targetId", 0);
                if (!messageDao.canContact(identity.role, identity.id, targetRole, targetId)) {
                    JsonUtil.write(response, ServletUtil.fail("当前没有联系权限，请先产生浏览、下单或后台指定关系。"));
                    return;
                }
                int conversationId = messageDao.openConversation(identity.role, identity.id, identity.name, identity.avatarUrl, targetRole, targetId);
                result.put("conversationId", conversationId);
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
                result.put("messages", messageDao.messages(identity.role, identity.id, conversationId));
            } else if ("send".equals(action)) {
                int conversationId = ServletUtil.intParam(request, "conversationId", -1);
                String content = trim(request.getParameter("content"));
                String clientMessageId = trim(request.getParameter("clientMessageId"));
                int quoteMessageId = ServletUtil.intParam(request, "quoteMessageId", 0);
                if (conversationId < 0 || content.length() == 0) {
                    JsonUtil.write(response, ServletUtil.fail("请选择会话并填写消息内容。"));
                    return;
                }
                if (conversationId == 0) {
                    JsonUtil.write(response, ServletUtil.fail("系统通知会话不可直接回复，请新建或选择聊天会话。"));
                    return;
                }
                messageDao.sendChat(conversationId, identity.role, identity.id, identity.name, "", 0, "TEXT", content, "", "", 0, "", 0, "", clientMessageId, quoteMessageId);
                result.put("messages", messageDao.messages(identity.role, identity.id, conversationId));
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
            } else if ("upload".equals(action)) {
                int conversationId = ServletUtil.intParam(request, "conversationId", -1);
                String clientMessageId = trim(request.getParameter("clientMessageId"));
                int quoteMessageId = ServletUtil.intParam(request, "quoteMessageId", 0);
                if (conversationId <= 0) {
                    JsonUtil.write(response, ServletUtil.fail("请选择一个聊天会话后再上传附件。"));
                    return;
                }
                UploadFile file = saveUpload(request);
                messageDao.sendChat(conversationId, identity.role, identity.id, identity.name, "", 0, file.contentType, "", file.url, file.originalName, file.size, "", 0, "", clientMessageId, quoteMessageId);
                result.put("mediaUrl", file.url);
                result.put("fileName", file.originalName);
                result.put("fileSize", file.size);
                result.put("contentType", file.contentType);
                result.put("messages", messageDao.messages(identity.role, identity.id, conversationId));
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
            } else if ("sendCard".equals(action)) {
                int conversationId = ServletUtil.intParam(request, "conversationId", -1);
                String clientMessageId = trim(request.getParameter("clientMessageId"));
                int quoteMessageId = ServletUtil.intParam(request, "quoteMessageId", 0);
                if (conversationId <= 0) {
                    JsonUtil.write(response, ServletUtil.fail("请选择聊天会话后再发送业务卡片。"));
                    return;
                }
                String type = trim(request.getParameter("cardType")).toUpperCase(Locale.ROOT);
                int refId = ServletUtil.intParam(request, "refId", 0);
                Card card = buildCard(identity, type, refId);
                messageDao.sendChat(conversationId, identity.role, identity.id, identity.name, "", 0, type, card.summary, "", "", 0, card.refType, refId, card.extraJson, clientMessageId, quoteMessageId);
                result.put("messages", messageDao.messages(identity.role, identity.id, conversationId));
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
            } else if ("recall".equals(action)) {
                int messageId = ServletUtil.intParam(request, "messageId", 0);
                if (messageId <= 0) {
                    JsonUtil.write(response, ServletUtil.fail("请选择要撤回的消息。"));
                    return;
                }
                messageDao.recallMessage(identity.role, identity.id, messageId);
                int conversationId = ServletUtil.intParam(request, "conversationId", -1);
                if (conversationId > 0) result.put("messages", messageDao.messages(identity.role, identity.id, conversationId));
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
            } else if ("read".equals(action)) {
                int conversationId = ServletUtil.intParam(request, "conversationId", 0);
                messageDao.markConversationRead(conversationId, identity.role, identity.id);
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
            } else if ("markAllRead".equals(action)) {
                messageDao.markConversationRead(0, identity.role, identity.id);
                result.put("conversations", messageDao.conversations(identity.role, identity.id));
            } else {
                JsonUtil.write(response, ServletUtil.fail("未知消息操作。"));
                return;
            }
            result.put("unreadCount", messageDao.unreadCount(identity.role, identity.id));
            JsonUtil.write(response, result);
        } catch (Exception e) {
            JsonUtil.write(response, ServletUtil.fail(rootMessage(e)));
        }
    }

    private UploadFile saveUpload(HttpServletRequest request) throws IOException, ServletException {
        Part part = request.getPart("file");
        if (part == null || part.getSize() <= 0) throw new RuntimeException("请选择要上传的附件。");
        String original = cleanFileName(part.getSubmittedFileName());
        String ext = extension(original);
        String contentType = detectContentType(ext);
        long max = "IMAGE".equals(contentType) ? MAX_IMAGE_SIZE : ("VIDEO".equals(contentType) ? MAX_VIDEO_SIZE : MAX_FILE_SIZE);
        if (part.getSize() > max) throw new RuntimeException("附件过大，请压缩后再上传。");
        String dirPath = getServletContext().getRealPath("/assets/upload/message");
        if (dirPath == null) throw new RuntimeException("上传目录不可用。");
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) throw new RuntimeException("无法创建消息附件目录。");
        String stored = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File target = new File(dir, stored);
        part.write(target.getAbsolutePath());
        UploadFile file = new UploadFile();
        file.contentType = contentType;
        file.originalName = original.length() == 0 ? stored : original;
        file.size = part.getSize();
        file.url = "assets/upload/message/" + stored;
        return file;
    }

    private Card buildCard(Identity identity, String type, int refId) {
        if ("ORDER_CARD".equals(type)) return orderCard(identity, refId);
        if ("PRODUCT_CARD".equals(type)) return productCard(identity, refId);
        if ("REFUND_CARD".equals(type)) return refundCard(identity, refId);
        throw new RuntimeException("不支持的业务卡片。");
    }

    private Card orderCard(Identity identity, int orderId) {
        Order order = "USER".equals(identity.role) ? orderDao.findByIdAndUserId(orderId, identity.id) : orderDao.findById(orderId);
        if (order == null) throw new RuntimeException("订单不存在或无权发送。");
        if ("MERCHANT".equals(identity.role) && order.getMerchantId() != identity.id) throw new RuntimeException("只能发送本店订单。");
        String items = orderItems(order);
        String extra = "{\"orderId\":" + order.getId() + ",\"orderNo\":\"" + jsonEscape(order.getOrderNo()) + "\",\"shopName\":\"" + jsonEscape(order.getShopName()) + "\",\"items\":\"" + jsonEscape(items) + "\",\"amount\":" + order.getTotalAmount() + ",\"status\":\"" + jsonEscape(order.getStatus()) + "\",\"createTime\":\"" + jsonEscape(order.getCreateTime()) + "\"}";
        return new Card("ORDER", "咨询订单：" + order.getOrderNo(), extra);
    }

    private Card productCard(Identity identity, int productId) {
        Product product = productDao.findById(productId);
        if (product == null) throw new RuntimeException("商品不存在。");
        if ("MERCHANT".equals(identity.role) && product.getMerchantId() != identity.id) throw new RuntimeException("只能发送本店商品。");
        String extra = "{\"productId\":" + product.getId() + ",\"name\":\"" + jsonEscape(product.getName()) + "\",\"imageUrl\":\"" + jsonEscape(product.getImageUrl()) + "\",\"shopName\":\"" + jsonEscape(product.getShopName()) + "\",\"price\":" + product.getPrice() + ",\"status\":\"" + jsonEscape(product.getStatus()) + "\"}";
        return new Card("PRODUCT", "商品咨询：" + product.getName(), extra);
    }

    private Card refundCard(Identity identity, int afterSaleId) {
        Map<String, Object> row = orderDao.afterSaleSnapshot(afterSaleId, identity.role, identity.id);
        if (row == null) throw new RuntimeException("退款申请不存在或无权查看。");
        String extra = "{\"afterSaleId\":" + afterSaleId + ",\"orderId\":" + row.get("orderId") + ",\"orderNo\":\"" + jsonEscape(row.get("orderNo")) + "\",\"productName\":\"" + jsonEscape(row.get("productName")) + "\",\"amount\":" + row.get("refundAmount") + ",\"status\":\"" + jsonEscape(row.get("status")) + "\",\"reason\":\"" + jsonEscape(row.get("reason")) + "\"}";
        return new Card("REFUND", "退款申请：" + row.get("orderNo"), extra);
    }

    private String orderItems(Order order) {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            if (sb.length() > 0) sb.append("；");
            int productId = item.getProduct() == null ? 0 : item.getProduct().getId();
            sb.append(item.getSnapshotName() == null ? "商品" + productId : item.getSnapshotName()).append("×").append(item.getQuantity());
        }
        return sb.toString();
    }

    private String detectContentType(String ext) {
        String image = ",jpg,jpeg,png,webp,gif,";
        String video = ",mp4,webm,";
        String file = ",pdf,doc,docx,xls,xlsx,txt,zip,rar,ppt,pptx,csv,";
        if (image.indexOf("," + ext + ",") >= 0) return "IMAGE";
        if (video.indexOf("," + ext + ",") >= 0) return "VIDEO";
        if (file.indexOf("," + ext + ",") >= 0) return "FILE";
        throw new RuntimeException("不支持的附件格式。");
    }

    private Identity identity(HttpServletRequest request) {
        User user = ServletUtil.currentUser(request);
        if (user != null) {
            User refreshed = userService.findById(user.getId());
            if (refreshed == null) return null;
            request.getSession().setAttribute("user", refreshed);
            return new Identity("USER", refreshed.getId(), refreshed.getUsername(), refreshed.getAvatarUrl(), refreshed.getStatus());
        }
        Merchant merchant = ServletUtil.currentMerchant(request);
        if (merchant != null) {
            Merchant refreshed = merchantService.findById(merchant.getMerchantId());
            if (refreshed == null) return null;
            request.getSession().setAttribute("merchant", refreshed);
            return new Identity("MERCHANT", refreshed.getMerchantId(), refreshed.getShopName(), refreshed.getAvatarUrl(), refreshed.getStatus());
        }
        Admin admin = ServletUtil.currentAdmin(request);
        if (admin != null) return new Identity("ADMIN", admin.getId(), admin.getRealName(), "", "正常");
        return null;
    }

    private String normalizeRole(String role, String fallback) {
        String text = trim(role).toUpperCase(Locale.ROOT);
        if ("USER".equals(text) || "MERCHANT".equals(text) || "ADMIN".equals(text)) return text;
        return fallback;
    }

    private String cleanFileName(String name) {
        if (name == null) return "";
        return new File(name).getName().replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String extension(String name) {
        int dot = name == null ? -1 : name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) throw new RuntimeException("无法识别附件格式。");
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String trim(String text) {
        return text == null ? "" : text.trim();
    }

    private String rootMessage(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "消息操作失败。" : t.getMessage();
    }

    private String jsonEscape(Object value) {
        return String.valueOf(value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private static class Card {
        String refType;
        String summary;
        String extraJson;
        Card(String refType, String summary, String extraJson) {
            this.refType = refType;
            this.summary = summary;
            this.extraJson = extraJson;
        }
    }

    private static class Identity {
        String role;
        int id;
        String name;
        String avatarUrl;
        String status;
        Identity(String role, int id, String name, String avatarUrl, String status) {
            this.role = role;
            this.id = id;
            this.name = name == null || name.trim().length() == 0 ? role : name;
            this.avatarUrl = avatarUrl == null ? "" : avatarUrl;
            this.status = status == null ? "" : status;
        }
        boolean canSend() {
            return "ADMIN".equals(role) || "正常".equals(status) || "APPROVED".equals(status);
        }
    }

    private static class UploadFile {
        String contentType;
        String originalName;
        String url;
        long size;
    }
}
