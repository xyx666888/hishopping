package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hishopping.util.DBUtil;

public class MessageDao {
    private static boolean schemaReady = false;

    public MessageDao() {
        ensureSchema();
    }

    public List<Map<String, Object>> conversations(String role, int actorId) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> system = systemConversation(role, actorId);
        if (system != null) list.add(system);
        String sql = "select top 100 c.*, " +
            "(select count(1) from hishopping_message m where m.conversation_id=c.conversation_id and m.receiver_role=? and m.receiver_id=? and m.read_status=N'UNREAD') unread_count " +
            "from hishopping_conversation c where (c.user_a_type=? and c.user_a_id=?) or (c.user_b_type=? and c.user_b_id=?) " +
            "order by c.update_time desc, c.conversation_id desc";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ps.setInt(2, actorId);
            ps.setString(3, role);
            ps.setInt(4, actorId);
            ps.setString(5, role);
            ps.setInt(6, actorId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(conversationMap(rs, role, actorId));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> messages(String role, int actorId, int conversationId) {
        if (conversationId == 0) return systemMessages(role, actorId);
        if (!isMember(conversationId, role, actorId)) throw new RuntimeException("无权查看该会话。");
        String sql = "select top 300 m.*, q.sender_name quote_sender_name, q.sender_role quote_sender_role, q.content_type quote_content_type, " +
            "q.content_text quote_content_text, q.content quote_content, q.file_name quote_file_name, q.ref_type quote_ref_type, q.extra_json quote_extra_json, q.recalled quote_recalled " +
            "from hishopping_message m left join hishopping_message q on m.quote_message_id=q.message_id where m.conversation_id=? order by m.create_time asc, m.message_id asc";
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, conversationId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(messageMap(rs, role, actorId));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public int unreadCount(String role, int actorId) {
        String sql = "select count(1) from hishopping_message where receiver_role=? and (receiver_id=? or receiver_id=0) and read_status=N'UNREAD'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ps.setInt(2, actorId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> targets(String role, int actorId, String keyword, String targetRole) {
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if ("ADMIN".equals(role)) {
            if (targetRole == null || targetRole.length() == 0 || "USER".equals(targetRole)) {
                list.addAll(userTargetQuery("select top 30 id, account_id, username, phone, email, avatar_url from hishopping_user where username like ? or account_id like ? or phone like ? or email like ? order by id desc", kw));
            }
            if (targetRole == null || targetRole.length() == 0 || "MERCHANT".equals(targetRole)) {
                list.addAll(merchantTargetQuery("select top 30 merchant_id, merchant_code, merchant_name, shop_name, contact_phone, email, avatar_url from hishop_merchant where merchant_name like ? or shop_name like ? or merchant_code like ? or contact_phone like ? or email like ? order by merchant_id desc", kw));
            }
            return list;
        }
        if ("USER".equals(role)) {
            if (targetRole == null || targetRole.length() == 0 || "USER".equals(targetRole)) {
                list.addAll(userTargetQuery("select top 30 id, account_id, username, phone, email, avatar_url from hishopping_user where id<> " + actorId + " and (username like ? or account_id like ? or phone like ? or email like ?) order by id desc", kw));
            }
            if (targetRole == null || targetRole.length() == 0 || "MERCHANT".equals(targetRole)) {
                list.addAll(merchantTargetQuery("select top 30 m.merchant_id, m.merchant_code, m.merchant_name, m.shop_name, m.contact_phone, m.email, m.avatar_url from hishop_merchant m where m.status=N'APPROVED' and (m.merchant_name like ? or m.shop_name like ? or m.merchant_code like ? or m.contact_phone like ? or m.email like ?) and exists (select 1 from hishopping_order o where o.user_id=" + actorId + " and o.merchant_id=m.merchant_id union select 1 from hishopping_product_view v where v.user_id=" + actorId + " and v.merchant_id=m.merchant_id) order by m.merchant_id desc", kw));
            }
            if (targetRole == null || targetRole.length() == 0 || "ADMIN".equals(targetRole)) {
                list.addAll(adminTargetQuery("select top 10 id, admin_name, real_name from hishopping_admin where admin_name like ? or real_name like ? order by id", kw));
            }
            return list;
        }
        if ("MERCHANT".equals(role)) {
            if (targetRole == null || targetRole.length() == 0 || "USER".equals(targetRole)) {
                list.addAll(userTargetQuery("select top 50 u.id, u.account_id, u.username, u.phone, u.email, u.avatar_url from hishopping_user u where (u.username like ? or u.account_id like ? or u.phone like ? or u.email like ?) and exists (select 1 from hishopping_order o where o.merchant_id=" + actorId + " and o.user_id=u.id union select 1 from hishopping_product_view v where v.merchant_id=" + actorId + " and v.user_id=u.id) order by u.id desc", kw));
            }
            if (targetRole == null || targetRole.length() == 0 || "ADMIN".equals(targetRole)) {
                list.addAll(adminTargetQuery("select top 10 id, admin_name, real_name from hishopping_admin where admin_name like ? or real_name like ? order by id", kw));
            }
        }
        return list;
    }

    public int openConversation(String actorRole, int actorId, String actorName, String actorAvatar, String peerRole, int peerId) {
        Map<String, Object> target = targetDetail(peerRole, peerId);
        if (target == null) throw new RuntimeException("联系人不存在。");
        String peerName = String.valueOf(target.get("name"));
        String peerAvatar = String.valueOf(target.get("avatarUrl") == null ? "" : target.get("avatarUrl"));
        int existing = findConversation(actorRole, actorId, peerRole, peerId);
        if (existing > 0) return existing;
        String sql = "insert into hishopping_conversation(conversation_type,user_a_type,user_a_id,user_a_name,user_a_avatar,user_b_type,user_b_id,user_b_name,user_b_avatar,last_message,last_message_type,last_message_time) values(N'CHAT',?,?,?,?,?,?,?,?,N'会话已创建',N'SYSTEM',sysdatetime())";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, actorRole);
            ps.setInt(2, actorId);
            ps.setString(3, actorName);
            ps.setString(4, actorAvatar);
            ps.setString(5, peerRole);
            ps.setInt(6, peerId);
            ps.setString(7, peerName);
            ps.setString(8, peerAvatar);
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : findConversation(actorRole, actorId, peerRole, peerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(keys, ps, conn);
        }
    }

    public void sendChat(int conversationId, String senderRole, int senderId, String senderName, String receiverRole, int receiverId, String contentType, String text, String mediaUrl, String fileName, long fileSize) {
        sendChat(conversationId, senderRole, senderId, senderName, receiverRole, receiverId, contentType, text, mediaUrl, fileName, fileSize, "", 0, "", "", 0);
    }

    public void sendChat(int conversationId, String senderRole, int senderId, String senderName, String receiverRole, int receiverId, String contentType, String text, String mediaUrl, String fileName, long fileSize, String refType, int refId, String extraJson) {
        sendChat(conversationId, senderRole, senderId, senderName, receiverRole, receiverId, contentType, text, mediaUrl, fileName, fileSize, refType, refId, extraJson, "", 0);
    }

    public void sendChat(int conversationId, String senderRole, int senderId, String senderName, String receiverRole, int receiverId, String contentType, String text, String mediaUrl, String fileName, long fileSize, String refType, int refId, String extraJson, String clientMessageId, int quoteMessageId) {
        if (!isMember(conversationId, senderRole, senderId)) throw new RuntimeException("无权向该会话发送消息。");
        Map<String, Object> peer = peerOf(conversationId, senderRole, senderId);
        if (peer == null) throw new RuntimeException("会话不存在。");
        String finalClientMessageId = nullToEmpty(clientMessageId).trim();
        if (finalClientMessageId.length() > 64) finalClientMessageId = finalClientMessageId.substring(0, 64);
        if (finalClientMessageId.length() > 0 && existingClientMessage(conversationId, senderRole, senderId, finalClientMessageId) > 0) return;
        if (quoteMessageId > 0 && !messageInConversation(conversationId, quoteMessageId)) throw new RuntimeException("只能引用当前会话内的消息。");
        String finalReceiverRole = receiverRole == null || receiverRole.length() == 0 ? String.valueOf(peer.get("role")) : receiverRole;
        int finalReceiverId = receiverId <= 0 ? ((Number) peer.get("id")).intValue() : receiverId;
        String summary = summary(contentType, text, fileName);
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ups = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("insert into hishopping_message(conversation_id,sender_role,sender_id,sender_name,receiver_role,receiver_id,receiver_name,title,content,content_type,content_text,media_url,file_name,file_size,client_message_id,quote_message_id,ref_type,ref_id,extra_json,read_status) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,N'UNREAD')");
            ps.setInt(1, conversationId);
            ps.setString(2, senderRole);
            ps.setInt(3, senderId);
            ps.setString(4, senderName);
            ps.setString(5, finalReceiverRole);
            ps.setInt(6, finalReceiverId);
            ps.setString(7, String.valueOf(peer.get("name")));
            ps.setString(8, "聊天消息");
            ps.setString(9, summary);
            ps.setString(10, contentType);
            ps.setString(11, text);
            ps.setString(12, mediaUrl);
            ps.setString(13, fileName);
            ps.setLong(14, fileSize);
            ps.setString(15, finalClientMessageId.length() == 0 ? null : finalClientMessageId);
            if (quoteMessageId > 0) ps.setInt(16, quoteMessageId); else ps.setNull(16, java.sql.Types.INTEGER);
            ps.setString(17, refType);
            ps.setInt(18, refId);
            ps.setString(19, extraJson);
            ps.executeUpdate();
            ups = conn.prepareStatement("update hishopping_conversation set last_message=?, last_message_type=?, last_message_time=sysdatetime(), update_time=sysdatetime() where conversation_id=?");
            ups.setString(1, summary);
            ups.setString(2, contentType);
            ups.setInt(3, conversationId);
            ups.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            if (finalClientMessageId.length() > 0 && existingClientMessage(conversationId, senderRole, senderId, finalClientMessageId) > 0) return;
            throw new RuntimeException(e);
        } finally {
            try { if (ups != null) ups.close(); } catch (SQLException ignored) {}
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void recallMessage(String role, int actorId, int messageId) {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ups = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement("select message_id, conversation_id, sender_role, sender_id, recalled, create_time from hishopping_message where message_id=?");
            ps.setInt(1, messageId);
            rs = ps.executeQuery();
            if (!rs.next()) throw new RuntimeException("消息不存在。");
            int conversationId = rs.getObject("conversation_id") == null ? 0 : rs.getInt("conversation_id");
            if (conversationId <= 0 || !isMember(conversationId, role, actorId)) throw new RuntimeException("无权操作该消息。");
            if (!role.equals(rs.getString("sender_role")) || actorId != rs.getInt("sender_id")) throw new RuntimeException("只能撤回自己发送的消息。");
            if (rs.getBoolean("recalled")) throw new RuntimeException("该消息已撤回。");
            Timestamp created = rs.getTimestamp("create_time");
            if (created == null || System.currentTimeMillis() - created.getTime() > 120000L) throw new RuntimeException("只能撤回 2 分钟内发送的消息。");
            DBUtil.closeDBResource(rs, ps, null);
            rs = null;
            ps = conn.prepareStatement("update hishopping_message set recalled=1, recalled_at=sysdatetime(), content=N'撤回了一条消息' where message_id=?");
            ps.setInt(1, messageId);
            ps.executeUpdate();
            ups = conn.prepareStatement("update hishopping_conversation set last_message=N'撤回了一条消息', last_message_type=N'RECALL', last_message_time=sysdatetime(), update_time=sysdatetime() where conversation_id=? and not exists (select 1 from hishopping_message where conversation_id=? and ((create_time > (select create_time from hishopping_message where message_id=?)) or (create_time = (select create_time from hishopping_message where message_id=?) and message_id > ?)))");
            ups.setInt(1, conversationId);
            ups.setInt(2, conversationId);
            ups.setInt(3, messageId);
            ups.setInt(4, messageId);
            ups.setInt(5, messageId);
            ups.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try { if (ups != null) ups.close(); } catch (SQLException ignored) {}
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void markConversationRead(int conversationId, String role, int actorId) {
        String sql = conversationId == 0
            ? "update hishopping_message set read_status=N'READ', read_time=sysdatetime() where receiver_role=? and (receiver_id=? or receiver_id=0)"
            : "update hishopping_message set read_status=N'READ', read_time=sysdatetime() where conversation_id=? and receiver_role=? and receiver_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            if (conversationId == 0) {
                ps.setString(1, role);
                ps.setInt(2, actorId);
            } else {
                ps.setInt(1, conversationId);
                ps.setString(2, role);
                ps.setInt(3, actorId);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public boolean canContact(String senderRole, int senderId, String receiverRole, int receiverId) {
        if ("ADMIN".equals(senderRole)) return true;
        if ("ADMIN".equals(receiverRole)) return receiverId > 0;
        if ("USER".equals(senderRole) && "USER".equals(receiverRole)) return senderId != receiverId;
        if (receiverId <= 0) return false;
        String sql = null;
        if ("MERCHANT".equals(senderRole) && "USER".equals(receiverRole)) {
            sql = "select count(1) from (select user_id from hishopping_order where merchant_id=? and user_id=? union select user_id from hishopping_product_view where merchant_id=? and user_id=?) t";
        } else if ("USER".equals(senderRole) && "MERCHANT".equals(receiverRole)) {
            sql = "select count(1) from (select merchant_id from hishopping_order where user_id=? and merchant_id=? union select merchant_id from hishopping_product_view where user_id=? and merchant_id=?) t";
        }
        if (sql == null) return false;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setInt(3, senderId);
            ps.setInt(4, receiverId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void logProductView(int userId, int productId, int merchantId) {
        if (userId <= 0 || productId <= 0 || merchantId <= 0) return;
        String sql = "insert into hishopping_product_view(user_id,product_id,merchant_id) values(?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, merchantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void send(String senderRole, int senderId, String senderName, String receiverRole, int receiverId, String receiverName, String title, String content, String linkType, String linkTarget) {
        String sql = "insert into hishopping_message(sender_role,sender_id,sender_name,receiver_role,receiver_id,receiver_name,title,content,content_type,content_text,link_type,link_target) values(?,?,?,?,?,?,?,?,N'SYSTEM',?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, senderRole);
            ps.setInt(2, senderId);
            ps.setString(3, senderName);
            ps.setString(4, receiverRole);
            ps.setInt(5, receiverId);
            ps.setString(6, receiverName);
            ps.setString(7, title);
            ps.setString(8, content);
            ps.setString(9, content);
            ps.setString(10, linkType == null || linkType.trim().length() == 0 ? "NONE" : linkType);
            ps.setString(11, linkTarget);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            st.executeUpdate("if object_id(N'dbo.hishopping_conversation', N'U') is null create table dbo.hishopping_conversation (conversation_id int identity(1,1) primary key, conversation_type nvarchar(30) not null default N'CHAT', user_a_type nvarchar(20) not null, user_a_id int not null, user_a_name nvarchar(100) null, user_a_avatar nvarchar(300) null, user_b_type nvarchar(20) not null, user_b_id int not null, user_b_name nvarchar(100) null, user_b_avatar nvarchar(300) null, last_message nvarchar(500) null, last_message_type nvarchar(20) not null default N'TEXT', last_message_time datetime2 null, create_time datetime2 not null default sysdatetime(), update_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if object_id(N'dbo.hishopping_message', N'U') is null create table dbo.hishopping_message (message_id int identity(1,1) primary key, conversation_id int null, sender_role nvarchar(20) not null, sender_id int not null default 0, sender_name nvarchar(80) null, receiver_role nvarchar(20) not null, receiver_id int not null default 0, receiver_name nvarchar(80) null, title nvarchar(120) not null default N'消息', content nvarchar(1000) not null default N'', content_type nvarchar(20) not null default N'TEXT', content_text nvarchar(max) null, media_url nvarchar(500) null, file_name nvarchar(260) null, file_size bigint not null default 0, client_message_id nvarchar(64) null, recalled bit not null default 0, recalled_at datetime2 null, quote_message_id int null, link_type nvarchar(30) not null default N'NONE', link_target nvarchar(200) null, read_status nvarchar(20) not null default N'UNREAD', create_time datetime2 not null default sysdatetime(), read_time datetime2 null)");
            st.executeUpdate("if col_length('dbo.hishopping_message','conversation_id') is null alter table dbo.hishopping_message add conversation_id int null");
            st.executeUpdate("if col_length('dbo.hishopping_message','content_type') is null alter table dbo.hishopping_message add content_type nvarchar(20) not null constraint DF_hishopping_message_content_type default N'TEXT'");
            st.executeUpdate("if col_length('dbo.hishopping_message','content_text') is null alter table dbo.hishopping_message add content_text nvarchar(max) null");
            st.executeUpdate("if col_length('dbo.hishopping_message','media_url') is null alter table dbo.hishopping_message add media_url nvarchar(500) null");
            st.executeUpdate("if col_length('dbo.hishopping_message','file_name') is null alter table dbo.hishopping_message add file_name nvarchar(260) null");
            st.executeUpdate("if col_length('dbo.hishopping_message','file_size') is null alter table dbo.hishopping_message add file_size bigint not null constraint DF_hishopping_message_file_size default 0");
            st.executeUpdate("if col_length('dbo.hishopping_message','client_message_id') is null alter table dbo.hishopping_message add client_message_id nvarchar(64) null");
            st.executeUpdate("if col_length('dbo.hishopping_message','recalled') is null alter table dbo.hishopping_message add recalled bit not null constraint DF_hishopping_message_recalled default 0");
            st.executeUpdate("if col_length('dbo.hishopping_message','recalled_at') is null alter table dbo.hishopping_message add recalled_at datetime2 null");
            st.executeUpdate("if col_length('dbo.hishopping_message','quote_message_id') is null alter table dbo.hishopping_message add quote_message_id int null");
            st.executeUpdate("if col_length('dbo.hishopping_message','ref_type') is null alter table dbo.hishopping_message add ref_type nvarchar(30) null");
            st.executeUpdate("if col_length('dbo.hishopping_message','ref_id') is null alter table dbo.hishopping_message add ref_id int null");
            st.executeUpdate("if col_length('dbo.hishopping_message','extra_json') is null alter table dbo.hishopping_message add extra_json nvarchar(max) null");
            st.executeUpdate("update dbo.hishopping_message set content_text=content where content_text is null");
            st.executeUpdate("if object_id(N'dbo.hishopping_product_view', N'U') is null create table dbo.hishopping_product_view (view_id int identity(1,1) primary key, user_id int not null, product_id int not null, merchant_id int not null, view_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if object_id(N'dbo.hishopping_user_friend', N'U') is null create table dbo.hishopping_user_friend (friend_id int identity(1,1) primary key, user_id int not null, friend_user_id int not null, status nvarchar(20) not null default N'ACTIVE', create_time datetime2 not null default sysdatetime())");
            st.executeUpdate("if col_length('dbo.hishopping_user', 'avatar_url') is null alter table dbo.hishopping_user add avatar_url nvarchar(300) null");
            st.executeUpdate("if col_length('dbo.hishop_merchant', 'avatar_url') is null alter table dbo.hishop_merchant add avatar_url nvarchar(300) null");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_message_receiver_unread' and object_id=object_id(N'dbo.hishopping_message')) create index IX_hishopping_message_receiver_unread on dbo.hishopping_message(receiver_role, receiver_id, read_status, create_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_message_conversation' and object_id=object_id(N'dbo.hishopping_message')) create index IX_hishopping_message_conversation on dbo.hishopping_message(conversation_id, create_time)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'UX_hishopping_message_client_id' and object_id=object_id(N'dbo.hishopping_message')) create unique index UX_hishopping_message_client_id on dbo.hishopping_message(conversation_id, sender_role, sender_id, client_message_id) where client_message_id is not null and client_message_id<>N''");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_conversation_user_a' and object_id=object_id(N'dbo.hishopping_conversation')) create index IX_hishopping_conversation_user_a on dbo.hishopping_conversation(user_a_type, user_a_id, update_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_conversation_user_b' and object_id=object_id(N'dbo.hishopping_conversation')) create index IX_hishopping_conversation_user_b on dbo.hishopping_conversation(user_b_type, user_b_id, update_time desc)");
            st.executeUpdate("if not exists(select 1 from sys.indexes where name=N'IX_hishopping_product_view_merchant_user' and object_id=object_id(N'dbo.hishopping_product_view')) create index IX_hishopping_product_view_merchant_user on dbo.hishopping_product_view(merchant_id, user_id, view_time desc)");
            seedNotice(st);
            schemaReady = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(null, st, conn);
        }
    }

    private static void seedNotice(Statement st) throws SQLException {
        st.executeUpdate("if not exists(select 1 from dbo.hishopping_message where sender_role=N'SYSTEM' and receiver_role=N'USER' and title=N'欢迎使用消息中心') insert into dbo.hishopping_message(sender_role,sender_name,receiver_role,receiver_id,receiver_name,title,content,content_type,content_text) values(N'SYSTEM',N'系统通知',N'USER',0,N'全部用户',N'欢迎使用消息中心',N'订单、优惠券、账号和售后提醒会在这里集中展示。',N'SYSTEM',N'订单、优惠券、账号和售后提醒会在这里集中展示。')");
        st.executeUpdate("if not exists(select 1 from dbo.hishopping_message where sender_role=N'SYSTEM' and receiver_role=N'MERCHANT' and title=N'商家消息中心已启用') insert into dbo.hishopping_message(sender_role,sender_name,receiver_role,receiver_id,receiver_name,title,content,content_type,content_text) values(N'SYSTEM',N'系统通知',N'MERCHANT',0,N'全部商家',N'商家消息中心已启用',N'商品审核、订单发货、资料审核和账号状态提醒会在这里展示。',N'SYSTEM',N'商品审核、订单发货、资料审核和账号状态提醒会在这里展示。')");
        st.executeUpdate("if not exists(select 1 from dbo.hishopping_message where sender_role=N'SYSTEM' and receiver_role=N'ADMIN' and title=N'后台消息中心已启用') insert into dbo.hishopping_message(sender_role,sender_name,receiver_role,receiver_id,receiver_name,title,content,content_type,content_text) values(N'SYSTEM',N'系统通知',N'ADMIN',0,N'全部管理员',N'后台消息中心已启用',N'商家审核、头像审核、账号冻结恢复等后台任务会统一进入消息中心。',N'SYSTEM',N'商家审核、头像审核、账号冻结恢复等后台任务会统一进入消息中心。')");
    }

    private Map<String, Object> systemConversation(String role, int actorId) {
        String sql = "select top 1 title, content, create_time, (select count(1) from hishopping_message where receiver_role=? and (receiver_id=? or receiver_id=0) and read_status=N'UNREAD' and conversation_id is null) unread_count from hishopping_message where receiver_role=? and (receiver_id=? or receiver_id=0) and conversation_id is null order by create_time desc, message_id desc";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ps.setInt(2, actorId);
            ps.setString(3, role);
            ps.setInt(4, actorId);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("conversationId", 0);
            map.put("peerRole", "SYSTEM");
            map.put("peerId", 0);
            map.put("peerName", "系统通知");
            map.put("peerAvatar", "assets/img/nav-message.png");
            map.put("lastMessage", rs.getString("content"));
            map.put("lastMessageType", "SYSTEM");
            map.put("lastMessageTime", time(rs.getTimestamp("create_time")));
            map.put("unreadCount", rs.getInt("unread_count"));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private List<Map<String, Object>> systemMessages(String role, int actorId) {
        String sql = "select top 100 m.*, q.sender_name quote_sender_name, q.sender_role quote_sender_role, q.content_type quote_content_type, " +
            "q.content_text quote_content_text, q.content quote_content, q.file_name quote_file_name, q.ref_type quote_ref_type, q.extra_json quote_extra_json, q.recalled quote_recalled " +
            "from hishopping_message m left join hishopping_message q on m.quote_message_id=q.message_id where m.receiver_role=? and (m.receiver_id=? or m.receiver_id=0) and m.conversation_id is null order by m.create_time asc, m.message_id asc";
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ps.setInt(2, actorId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(messageMap(rs, role, actorId));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private Map<String, Object> conversationMap(ResultSet rs, String role, int actorId) throws SQLException {
        boolean a = role.equals(rs.getString("user_a_type")) && actorId == rs.getInt("user_a_id");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("conversationId", rs.getInt("conversation_id"));
        map.put("peerRole", a ? rs.getString("user_b_type") : rs.getString("user_a_type"));
        map.put("peerId", a ? rs.getInt("user_b_id") : rs.getInt("user_a_id"));
        map.put("peerName", a ? rs.getString("user_b_name") : rs.getString("user_a_name"));
        map.put("peerAvatar", a ? rs.getString("user_b_avatar") : rs.getString("user_a_avatar"));
        map.put("lastMessage", rs.getString("last_message"));
        map.put("lastMessageType", rs.getString("last_message_type"));
        map.put("lastMessageTime", time(rs.getTimestamp("last_message_time")));
        map.put("unreadCount", rs.getInt("unread_count"));
        return map;
    }

    private Map<String, Object> messageMap(ResultSet rs, String role, int actorId) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("messageId", rs.getInt("message_id"));
        map.put("conversationId", rs.getObject("conversation_id") == null ? 0 : rs.getInt("conversation_id"));
        map.put("senderRole", rs.getString("sender_role"));
        map.put("senderId", rs.getInt("sender_id"));
        map.put("senderName", rs.getString("sender_name"));
        map.put("receiverRole", rs.getString("receiver_role"));
        map.put("receiverId", rs.getInt("receiver_id"));
        map.put("receiverName", rs.getString("receiver_name"));
        map.put("title", rs.getString("title"));
        map.put("content", rs.getString("content"));
        map.put("contentType", rs.getString("content_type"));
        map.put("contentText", rs.getString("content_text"));
        map.put("mediaUrl", rs.getString("media_url"));
        map.put("fileName", rs.getString("file_name"));
        map.put("fileSize", rs.getLong("file_size"));
        map.put("clientMessageId", rs.getString("client_message_id"));
        boolean recalled = rs.getBoolean("recalled");
        map.put("recalled", recalled);
        map.put("recalledAt", time(rs.getTimestamp("recalled_at")));
        map.put("canRecall", canRecall(rs, role, actorId));
        int quoteMessageId = rs.getObject("quote_message_id") == null ? 0 : rs.getInt("quote_message_id");
        map.put("quoteMessageId", quoteMessageId);
        if (quoteMessageId > 0) {
            boolean quoteRecalled = rs.getBoolean("quote_recalled");
            map.put("quoteSenderName", rs.getString("quote_sender_name"));
            map.put("quoteSenderType", rs.getString("quote_sender_role"));
            map.put("quoteContentType", rs.getString("quote_content_type"));
            map.put("quoteRecalled", quoteRecalled);
            map.put("quoteSummary", quoteRecalled ? "原消息已撤回" : quoteSummary(rs));
        } else {
            map.put("quoteRecalled", false);
        }
        map.put("refType", rs.getString("ref_type"));
        map.put("refId", rs.getObject("ref_id") == null ? 0 : rs.getInt("ref_id"));
        map.put("extraJson", rs.getString("extra_json"));
        map.put("readStatus", rs.getString("read_status"));
        map.put("createTime", time(rs.getTimestamp("create_time")));
        map.put("own", role.equals(rs.getString("sender_role")) && actorId == rs.getInt("sender_id"));
        return map;
    }

    private boolean canRecall(ResultSet rs, String role, int actorId) throws SQLException {
        if (!role.equals(rs.getString("sender_role")) || actorId != rs.getInt("sender_id")) return false;
        if (rs.getBoolean("recalled")) return false;
        Timestamp created = rs.getTimestamp("create_time");
        return created != null && System.currentTimeMillis() - created.getTime() <= 120000L;
    }

    private String quoteSummary(ResultSet rs) throws SQLException {
        String type = nullToEmpty(rs.getString("quote_content_type")).toUpperCase();
        String text = nullToEmpty(rs.getString("quote_content_text"));
        if (text.length() == 0) text = nullToEmpty(rs.getString("quote_content"));
        String fileName = nullToEmpty(rs.getString("quote_file_name"));
        if ("IMAGE".equals(type)) return "[图片]";
        if ("VIDEO".equals(type)) return "[视频]";
        if ("FILE".equals(type)) return "[文件] " + fileName;
        if ("ORDER_CARD".equals(type)) return "[订单] " + firstNonEmpty(jsonValue(rs.getString("quote_extra_json"), "orderNo"), text);
        if ("PRODUCT_CARD".equals(type)) return "[商品] " + firstNonEmpty(jsonValue(rs.getString("quote_extra_json"), "name"), text);
        if ("REFUND_CARD".equals(type)) return "[退款] 退款申请";
        if ("FRIEND_REQUEST".equals(type)) return "[好友申请]";
        if ("SYSTEM".equals(type)) return "[系统通知]";
        String value = text.replace('\n', ' ').trim();
        return value.length() > 48 ? value.substring(0, 48) + "..." : value;
    }

    private String firstNonEmpty(String a, String b) {
        return a != null && a.length() > 0 ? a : nullToEmpty(b);
    }

    private String jsonValue(String json, String key) {
        if (json == null || key == null) return "";
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) return "";
        start += pattern.length();
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                value.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                break;
            } else {
                value.append(ch);
            }
        }
        return value.toString();
    }

    private boolean isMember(int conversationId, String role, int actorId) {
        String sql = "select count(1) from hishopping_conversation where conversation_id=? and ((user_a_type=? and user_a_id=?) or (user_b_type=? and user_b_id=?))";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, conversationId);
            ps.setString(2, role);
            ps.setInt(3, actorId);
            ps.setString(4, role);
            ps.setInt(5, actorId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private int existingClientMessage(int conversationId, String senderRole, int senderId, String clientMessageId) {
        String sql = "select top 1 message_id from hishopping_message where conversation_id=? and sender_role=? and sender_id=? and client_message_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, conversationId);
            ps.setString(2, senderRole);
            ps.setInt(3, senderId);
            ps.setString(4, clientMessageId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private boolean messageInConversation(int conversationId, int messageId) {
        String sql = "select count(1) from hishopping_message where conversation_id=? and message_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, conversationId);
            ps.setInt(2, messageId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private int findConversation(String aRole, int aId, String bRole, int bId) {
        String sql = "select top 1 conversation_id from hishopping_conversation where ((user_a_type=? and user_a_id=? and user_b_type=? and user_b_id=?) or (user_a_type=? and user_a_id=? and user_b_type=? and user_b_id=?))";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, aRole);
            ps.setInt(2, aId);
            ps.setString(3, bRole);
            ps.setInt(4, bId);
            ps.setString(5, bRole);
            ps.setInt(6, bId);
            ps.setString(7, aRole);
            ps.setInt(8, aId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private Map<String, Object> peerOf(int conversationId, String role, int actorId) {
        String sql = "select * from hishopping_conversation where conversation_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, conversationId);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            boolean a = role.equals(rs.getString("user_a_type")) && actorId == rs.getInt("user_a_id");
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("role", a ? rs.getString("user_b_type") : rs.getString("user_a_type"));
            map.put("id", a ? rs.getInt("user_b_id") : rs.getInt("user_a_id"));
            map.put("name", a ? rs.getString("user_b_name") : rs.getString("user_a_name"));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private Map<String, Object> targetDetail(String role, int id) {
        List<Map<String, Object>> rows;
        if ("USER".equals(role)) rows = userTargetQuery("select top 1 id, account_id, username, phone, email, avatar_url from hishopping_user where id=" + id, "%");
        else if ("MERCHANT".equals(role)) rows = merchantTargetQuery("select top 1 merchant_id, merchant_code, merchant_name, shop_name, contact_phone, email, avatar_url from hishop_merchant where merchant_id=" + id, "%");
        else if ("ADMIN".equals(role)) rows = adminTargetQuery("select top 1 id, admin_name, real_name from hishopping_admin where id=" + id, "%");
        else rows = new ArrayList<Map<String, Object>>();
        return rows.isEmpty() ? null : rows.get(0);
    }

    private List<Map<String, Object>> adminTargetQuery(String sql, String kw) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 1; i <= parameterCount(sql); i++) ps.setString(i, kw);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("role", "ADMIN");
                map.put("id", rs.getInt("id"));
                map.put("name", rs.getString("real_name") == null ? rs.getString("admin_name") : rs.getString("real_name"));
                map.put("avatarUrl", "");
                map.put("subtitle", "平台客服 · 管理员 #" + rs.getInt("id"));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private List<Map<String, Object>> userTargetQuery(String sql, String kw) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 1; i <= parameterCount(sql); i++) ps.setString(i, kw);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("role", "USER");
                map.put("id", rs.getInt("id"));
                map.put("name", rs.getString("username"));
                map.put("avatarUrl", rs.getString("avatar_url"));
                map.put("subtitle", "用户 #" + rs.getString("account_id") + " · " + nullToEmpty(rs.getString("phone")));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private List<Map<String, Object>> merchantTargetQuery(String sql, String kw) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            for (int i = 1; i <= parameterCount(sql); i++) ps.setString(i, kw);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("role", "MERCHANT");
                map.put("id", rs.getInt("merchant_id"));
                map.put("name", rs.getString("shop_name"));
                map.put("avatarUrl", rs.getString("avatar_url"));
                map.put("subtitle", "商家 #" + rs.getString("merchant_code") + " · " + rs.getString("merchant_name"));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    private String summary(String contentType, String text, String fileName) {
        if ("IMAGE".equals(contentType)) return "[图片] " + nullToEmpty(fileName);
        if ("VIDEO".equals(contentType)) return "[视频] " + nullToEmpty(fileName);
        if ("FILE".equals(contentType)) return "[文件] " + nullToEmpty(fileName);
        String value = nullToEmpty(text).replace('\n', ' ').trim();
        return value.length() > 120 ? value.substring(0, 120) : value;
    }

    private String time(Timestamp timestamp) {
        return timestamp == null ? "" : String.valueOf(timestamp);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private int parameterCount(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') count++;
        }
        return count;
    }
}
