package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.dao.FriendDao;
import hishopping.dao.MessageDao;
import hishopping.entity.User;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class FriendServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private FriendDao friendDao = new FriendDao();
    private MessageDao messageDao = new MessageDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录用户账号。"));
            return;
        }
        Map<String, Object> result = ServletUtil.ok();
        String action = trim(request.getParameter("action"));
        if ("requests".equals(action)) {
            result.put("requests", friendDao.requestsForUser(user.getId()));
        } else {
            result.put("users", friendDao.searchUsers(user.getId(), request.getParameter("keyword")));
        }
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录用户账号。"));
            return;
        }
        try {
            String action = trim(request.getParameter("action"));
            Map<String, Object> result = ServletUtil.ok();
            if ("request".equals(action)) {
                int toUserId = ServletUtil.intParam(request, "toUserId", ServletUtil.intParam(request, "targetUserId", 0));
                int requestId = friendDao.request(user.getId(), toUserId, request.getParameter("remark"), request.getParameter("message"));
                String extra = "{\"requestId\":" + requestId + ",\"fromUserId\":" + user.getId() + ",\"fromName\":\"" + jsonEscape(user.getUsername()) + "\",\"message\":\"" + jsonEscape(request.getParameter("message")) + "\"}";
                int conversationId = messageDao.openConversation("USER", user.getId(), user.getUsername(), user.getAvatarUrl(), "USER", toUserId);
                messageDao.sendChat(conversationId, "USER", user.getId(), user.getUsername(), "USER", toUserId, "FRIEND_REQUEST", "好友申请", "", "", 0, "FRIEND_REQUEST", requestId, extra);
                result.put("requestId", requestId);
                result.put("conversationId", conversationId);
                result.put("conversations", messageDao.conversations("USER", user.getId()));
            } else if ("accept".equals(action) || "reject".equals(action)) {
                Map<String, Object> handled = friendDao.handle(ServletUtil.intParam(request, "requestId", 0), user.getId(), "accept".equals(action) ? "ACCEPTED" : "REJECTED");
                int peerId = ((Number) handled.get("fromUserId")).intValue();
                int conversationId = messageDao.openConversation("USER", user.getId(), user.getUsername(), user.getAvatarUrl(), "USER", peerId);
                String text = "ACCEPTED".equals(handled.get("status")) ? "已同意好友申请，现在可以开始聊天。" : "已拒绝好友申请。";
                messageDao.sendChat(conversationId, "USER", user.getId(), user.getUsername(), "USER", peerId, "SYSTEM", text, "", "", 0);
            } else {
                JsonUtil.write(response, ServletUtil.fail("未知好友操作。"));
                return;
            }
            result.put("requests", friendDao.requestsForUser(user.getId()));
            JsonUtil.write(response, result);
        } catch (RuntimeException e) {
            JsonUtil.write(response, ServletUtil.fail(e.getMessage()));
        }
    }

    private String trim(String text) {
        return text == null ? "" : text.trim();
    }

    private String jsonEscape(String text) {
        return trim(text).replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
