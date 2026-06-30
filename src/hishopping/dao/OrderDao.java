package hishopping.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

import hishopping.entity.Address;
import hishopping.entity.CartItem;
import hishopping.entity.Order;
import hishopping.entity.OrderItem;
import hishopping.entity.Product;
import hishopping.entity.UserCoupon;
import hishopping.util.DBUtil;
import hishopping.util.SkuUtil;

public class OrderDao {
    private BusinessDao businessDao = new BusinessDao();

    public OrderDao() {
        MerchantDao.ensureSchema();
    }

    public List<Order> findByUserId(int userId) {
        String sql = "select * from hishopping_order where user_id=? order by create_time desc,id desc";
        List<Order> orders = new ArrayList<Order>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setItems(findItems(order.getId(), conn));
                enrichOrder(order, conn);
                orders.add(order);
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Order> findAll() {
        String sql = "select * from hishopping_order order by create_time desc,id desc";
        List<Order> orders = new ArrayList<Order>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setItems(findItems(order.getId(), conn));
                enrichOrder(order, conn);
                orders.add(order);
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, st, conn);
        }
    }

    public Order findById(int orderId) {
        String sql = "select * from hishopping_order where id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            Order order = mapOrder(rs);
            order.setItems(findItems(order.getId(), conn));
            enrichOrder(order, conn);
            return order;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public Order findByIdAndUserId(int orderId, int userId) {
        String sql = "select * from hishopping_order where id=? and user_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            Order order = mapOrder(rs);
            order.setItems(findItems(order.getId(), conn));
            enrichOrder(order, conn);
            return order;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public Map<String, Object> afterSaleSnapshot(int afterSaleId, String role, int actorId) {
        String sql = "select a.*,o.order_no,o.shop_name,p.name product_name from hishop_after_sale a join hishopping_order o on a.order_id=o.id left join hishopping_product p on a.product_id=p.id where a.after_sale_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, afterSaleId);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            if ("USER".equals(role) && rs.getInt("user_id") != actorId) return null;
            if ("MERCHANT".equals(role) && rs.getInt("merchant_id") != actorId) return null;
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("afterSaleId", rs.getInt("after_sale_id"));
            map.put("orderId", rs.getInt("order_id"));
            map.put("orderNo", rs.getString("order_no"));
            map.put("shopName", rs.getString("shop_name"));
            map.put("productId", rs.getInt("product_id"));
            map.put("productName", rs.getString("product_name"));
            map.put("userId", rs.getInt("user_id"));
            map.put("merchantId", rs.getInt("merchant_id"));
            map.put("reason", rs.getString("reason"));
            map.put("refundAmount", rs.getDouble("refund_amount"));
            map.put("status", rs.getString("status"));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public List<Order> findByMerchantId(int merchantId) {
        String sql = "select distinct o.* from hishopping_order o left join hishopping_order_item oi on o.id=oi.order_id left join hishopping_product p on oi.product_id=p.id where o.merchant_id=? or (o.merchant_id is null and p.merchant_id=?) order by o.create_time desc,o.id desc";
        List<Order> orders = new ArrayList<Order>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, merchantId);
            ps.setInt(2, merchantId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Order order = mapOrder(rs);
                if (order.getMerchantId() > 0) {
                    order.setItems(findItems(order.getId(), conn));
                } else {
                    order.setItems(findItemsByMerchant(order.getId(), merchantId, conn));
                    order.setTotalAmount(merchantSubtotal(order));
                }
                enrichOrder(order, conn);
                orders.add(order);
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeDBResource(rs, ps, conn);
        }
    }

    public void updateStatus(int orderId, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            int userId = 0;
            double totalAmount = 0;
            String oldStatus = null;
            boolean growthAwarded = false;
            ps = conn.prepareStatement("select user_id,total_amount,status,growth_awarded from hishopping_order where id=?");
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("user_id");
                totalAmount = rs.getDouble("total_amount");
                oldStatus = rs.getString("status");
                growthAwarded = rs.getBoolean("growth_awarded");
            }
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            ps = conn.prepareStatement("update hishopping_order set status=? where id=?");
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
            if ("\u5df2\u5b8c\u6210".equals(status) && !"\u5df2\u5b8c\u6210".equals(oldStatus) && !growthAwarded && userId > 0) {
                int addGrowth = (int) totalAmount + 20;
                int[] vipSnapshot = currentVipSnapshot(conn, userId);
                int addPoints = (int) Math.floor(((int) totalAmount) * UserDao.pointRateForVipLevel(UserDao.calculateVipLevel(vipSnapshot[0])));
                businessDao.awardGrowth(conn, userId, addGrowth, addPoints, "ORDER_COMPLETE", orderId, "\u8ba2\u5355\u5b8c\u6210\u5956\u52b1");
                PreparedStatement markPs = conn.prepareStatement("update hishopping_order set growth_awarded=1 where id=?");
                markPs.setInt(1, orderId);
                markPs.executeUpdate();
                markPs.close();
            }
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ignored) {
            }
            DBUtil.closeDBResource(null, ps, conn);
        }
    }

    public void updateStatusIfMerchantOrder(int orderId, int merchantId, String status) {
        updateStatusIfMerchantOrder(orderId, merchantId, status, null, null);
    }

    public void updateStatusIfMerchantOrder(int orderId, int merchantId, String status, String expressCompany, String trackingNo) {
        Connection conn = null;
        PreparedStatement checkPs = null;
        PreparedStatement statusPs = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            checkPs = conn.prepareStatement("select count(1) from hishopping_order_item oi join hishopping_product p on oi.product_id=p.id where oi.order_id=? and p.merchant_id=?");
            checkPs.setInt(1, orderId);
            checkPs.setInt(2, merchantId);
            rs = checkPs.executeQuery();
            if (!rs.next() || rs.getInt(1) == 0) {
                throw new RuntimeException("\u53ea\u80fd\u64cd\u4f5c\u672c\u5e97\u76f8\u5173\u8ba2\u5355\u3002");
            }
            if (expressCompany != null || trackingNo != null) {
                businessDao.saveShipment(orderId, merchantId, expressCompany, trackingNo, conn);
            }
            statusPs = conn.prepareStatement("update hishopping_order set status=? where id=?");
            statusPs.setString(1, status);
            statusPs.setInt(2, orderId);
            statusPs.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            try {
                if (statusPs != null) statusPs.close();
            } catch (SQLException ignored) {
            }
            DBUtil.closeDBResource(rs, checkPs, conn);
        }
    }
    private int[] currentVipSnapshot(Connection conn, int userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("select growth_value,points from hishopping_user where id=?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            return rs.next() ? new int[] { rs.getInt("growth_value"), rs.getInt("points") } : new int[] { 0, 0 };
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    public void delete(int orderId) {
        Connection conn = null;
        PreparedStatement itemPs = null;
        PreparedStatement orderPs = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            itemPs = conn.prepareStatement("delete from hishopping_order_item where order_id=?");
            itemPs.setInt(1, orderId);
            itemPs.executeUpdate();
            orderPs = conn.prepareStatement("delete from hishopping_order where id=?");
            orderPs.setInt(1, orderId);
            orderPs.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            try {
                if (itemPs != null) itemPs.close();
                if (orderPs != null) orderPs.close();
            } catch (SQLException ignored) {
            }
            DBUtil.closeDBResource(null, null, conn);
        }
    }

    public Order createFromCart(int userId, int userCouponId, int addressId) {
        return createFromCart(userId, userCouponId, 0, 0, addressId);
    }

    public Order createFromCart(int userId, int platformCouponId, int stackableCouponId, int merchantCouponId, int addressId) {
        return createFromCart(userId, platformCouponId, stackableCouponId, merchantCouponId, addressId, null);
    }

    public Order createFromCart(int userId, int platformCouponId, int stackableCouponId, int merchantCouponId, int addressId, int[] cartItemIds) {
        Connection conn = null;
        PreparedStatement orderPs = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConn();
            conn.setAutoCommit(false);
            CartDao cartDao = new CartDao();
            List<CartItem> cart = cartDao.findByUserId(userId);
            if (cartItemIds != null) {
                java.util.Set<Integer> selectedIds = new java.util.HashSet<Integer>();
                for (int i = 0; i < cartItemIds.length; i++) {
                    if (cartItemIds[i] > 0) {
                        selectedIds.add(Integer.valueOf(cartItemIds[i]));
                    }
                }
                java.util.List<CartItem> selectedCart = new java.util.ArrayList<CartItem>();
                for (CartItem item : cart) {
                    if (selectedIds.contains(Integer.valueOf(item.getId()))) {
                        selectedCart.add(item);
                    }
                }
                cart = selectedCart;
            }
            if (cart.isEmpty()) {
                throw new RuntimeException("\u8bf7\u5148\u9009\u62e9\u8981\u7ed3\u7b97\u7684\u5546\u54c1\u3002");
            }
            AddressDao addressDao = new AddressDao();
            Address address = addressId > 0 ? addressDao.findById(userId, addressId, conn) : null;
            if (address == null) address = addressDao.findDefault(userId, conn);
            if (address == null) throw new RuntimeException("\u8bf7\u5148\u6dfb\u52a0\u6536\u8d27\u5730\u5740\u3002");

            double total = 0;
            java.util.Map<Integer, Double> merchantTotals = new java.util.HashMap<Integer, Double>();
            java.util.Map<Integer, java.util.List<CartItem>> merchantGroups = new java.util.LinkedHashMap<Integer, java.util.List<CartItem>>();
            java.util.Map<Integer, String> merchantNames = new java.util.HashMap<Integer, String>();
            for (CartItem item : cart) {
                if (item.getQuantity() <= 0) throw new RuntimeException("\u8d2d\u7269\u8f66\u5546\u54c1\u6570\u91cf\u5f02\u5e38\uff0c\u8bf7\u91cd\u65b0\u9009\u62e9\u3002");
                SkuUtil.Sku sku = SkuUtil.choose(item.getProduct(), item.getSkuId(), item.getSelectedColor(), item.getSelectedSpec());
                if (!sku.isEnabled()) throw new RuntimeException(item.getProduct().getName() + "\u5f53\u524d\u89c4\u683c\u4e0d\u53ef\u8d2d\u4e70\u3002");
                if (sku.getStock() < item.getQuantity()) throw new RuntimeException(item.getProduct().getName() + "\u5e93\u5b58\u4e0d\u8db3\uff0c\u8bf7\u8c03\u6574\u8d2d\u7269\u8f66\u6570\u91cf\u3002");
                item.setSelectedColor(sku.getColor());
                item.setSelectedSpec(sku.getSpec());
                item.setSkuId(sku.getSkuId());
                item.setSkuText(SkuUtil.skuText(sku));
                item.setSkuPrice(sku.getPrice());
                double line = sku.getPrice() * item.getQuantity();
                total += line;
                int merchantId = item.getProduct().getMerchantId();
                merchantTotals.put(merchantId, Double.valueOf((merchantTotals.containsKey(merchantId) ? merchantTotals.get(merchantId).doubleValue() : 0) + line));
                if (!merchantGroups.containsKey(merchantId)) {
                    merchantGroups.put(merchantId, new java.util.ArrayList<CartItem>());
                    merchantNames.put(merchantId, item.getProduct().getShopName() == null || item.getProduct().getShopName().length() == 0 ? "\u5e73\u53f0\u81ea\u8425" : item.getProduct().getShopName());
                }
                merchantGroups.get(merchantId).add(item);
            }

            int[] vipSnapshot = currentVipSnapshot(conn, userId);
            int vipLevel = UserDao.calculateVipLevel(vipSnapshot[0]);
            double memberDiscount = vipDiscount(total, vipLevel);
            double remaining = Math.max(total - memberDiscount, 0);
            double couponDiscount = 0;
            double platformDiscount = 0;
            double stackableDiscount = 0;
            double merchantCouponDiscount = 0;
            StringBuilder couponTitle = new StringBuilder();
            java.util.List<UserCoupon> usedCoupons = new java.util.ArrayList<UserCoupon>();

            UserCoupon platformCoupon = platformCouponId > 0 ? findUsableCoupon(conn, userId, platformCouponId) : null;
            if (platformCoupon != null) {
                validatePlatformCoupon(platformCoupon, false, total, vipLevel);
                platformDiscount = Math.min(couponDiscount(platformCoupon, total, vipLevel), remaining);
                couponDiscount += platformDiscount;
                remaining = Math.max(remaining - platformDiscount, 0);
                usedCoupons.add(platformCoupon);
                appendCouponTitle(couponTitle, platformCoupon.getCouponName());
            }

            UserCoupon stackableCoupon = stackableCouponId > 0 ? findUsableCoupon(conn, userId, stackableCouponId) : null;
            if (stackableCoupon != null) {
                validatePlatformCoupon(stackableCoupon, true, total, vipLevel);
                stackableDiscount = Math.min(couponDiscount(stackableCoupon, total, vipLevel), remaining);
                couponDiscount += stackableDiscount;
                remaining = Math.max(remaining - stackableDiscount, 0);
                usedCoupons.add(stackableCoupon);
                appendCouponTitle(couponTitle, stackableCoupon.getCouponName());
            }

            UserCoupon merchantCoupon = merchantCouponId > 0 ? findUsableCoupon(conn, userId, merchantCouponId) : null;
            if (merchantCoupon != null) {
                validateMerchantCoupon(merchantCoupon, merchantTotals, vipLevel);
                double merchantBase = merchantTotals.containsKey(merchantCoupon.getMerchantId()) ? merchantTotals.get(merchantCoupon.getMerchantId()).doubleValue() : 0;
                merchantCouponDiscount = Math.min(couponDiscount(merchantCoupon, merchantBase, vipLevel), Math.min(merchantBase, remaining));
                couponDiscount += merchantCouponDiscount;
                remaining = Math.max(remaining - merchantCouponDiscount, 0);
                usedCoupons.add(merchantCoupon);
                appendCouponTitle(couponTitle, merchantCoupon.getCouponName());
            }

            double globalDiscount = Math.min(total, Math.max(0, memberDiscount + platformDiscount + stackableDiscount));
            double globalRemainder = roundMoney(globalDiscount);
            double merchantCouponRemainder = roundMoney(merchantCouponDiscount);
            String stamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            String batchNo = "BATCH-" + stamp + "-" + userId;
            String orderPrefix = "SO-" + stamp + "-" + userId;
            int groupIndex = 0;
            int groupCount = merchantGroups.size();
            int firstOrderId = 0;
            String firstOrderNo = null;
            double firstPayable = 0;
            double firstDiscount = 0;
            for (java.util.Map.Entry<Integer, java.util.List<CartItem>> entry : merchantGroups.entrySet()) {
                groupIndex++;
                int merchantId = entry.getKey().intValue();
                double goodsAmount = roundMoney(merchantTotals.get(Integer.valueOf(merchantId)).doubleValue());
                double groupGlobalDiscount = groupIndex == groupCount ? globalRemainder : roundMoney(total <= 0 ? 0 : globalDiscount * goodsAmount / total);
                groupGlobalDiscount = Math.max(0, Math.min(groupGlobalDiscount, globalRemainder));
                globalRemainder = roundMoney(globalRemainder - groupGlobalDiscount);
                double groupMerchantDiscount = 0;
                if (merchantCoupon != null && merchantCoupon.getMerchantId() == merchantId) {
                    groupMerchantDiscount = merchantCouponRemainder;
                    merchantCouponRemainder = 0;
                }
                double groupDiscount = Math.min(goodsAmount, roundMoney(groupGlobalDiscount + groupMerchantDiscount));
                double payable = roundMoney(Math.max(goodsAmount - groupDiscount, 0));
                String orderNo = orderPrefix + "-" + groupIndex;
                orderPs = conn.prepareStatement("insert into hishopping_order(order_no,batch_no,user_id,merchant_id,shop_name,goods_amount,total_amount,discount_amount,coupon_title,status,receiver_name,receiver_phone,receiver_address) values(?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                orderPs.setString(1, orderNo);
                orderPs.setString(2, batchNo);
                orderPs.setInt(3, userId);
                orderPs.setInt(4, merchantId);
                orderPs.setString(5, merchantNames.get(Integer.valueOf(merchantId)));
                orderPs.setDouble(6, goodsAmount);
                orderPs.setDouble(7, payable);
                orderPs.setDouble(8, groupDiscount);
                orderPs.setString(9, couponTitle.length() == 0 ? null : couponTitle.toString());
                orderPs.setString(10, "\u5f85\u4ed8\u6b3e");
                orderPs.setString(11, address.getReceiverName());
                orderPs.setString(12, address.getPhone());
                orderPs.setString(13, address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
                orderPs.executeUpdate();
                keys = orderPs.getGeneratedKeys();
                keys.next();
                int orderId = keys.getInt(1);
                if (firstOrderId == 0) {
                    firstOrderId = orderId;
                    firstOrderNo = orderNo;
                    firstPayable = payable;
                    firstDiscount = groupDiscount;
                }
                saveItems(orderId, entry.getValue(), conn);
                keys.close();
                keys = null;
                orderPs.close();
                orderPs = null;
            }
            for (UserCoupon used : usedCoupons) {
                markCouponUsed(conn, used.getUserCouponId(), userId, firstOrderId);
            }
            if (cartItemIds == null) {
                cartDao.clear(userId, conn);
            } else {
                cartDao.clearSelected(userId, cartItemIds, conn);
            }
            conn.commit();
            Order order = new Order();
            order.setId(firstOrderId);
            order.setOrderNo(firstOrderNo);
            order.setBatchNo(batchNo);
            order.setUserId(userId);
            order.setTotalAmount(firstPayable);
            order.setDiscountAmount(firstDiscount);
            order.setStatus("\u5f85\u4ed8\u6b3e");
            return order;
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            try {
                if (keys != null) keys.close();
            } catch (SQLException ignored) {
            }
            DBUtil.closeDBResource(null, orderPs, conn);
        }
    }
    private void saveItems(int orderId, List<CartItem> cart, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        PreparedStatement stockPs = null;
        java.util.Map<Integer, String> skuJsonByProduct = new java.util.HashMap<Integer, String>();
        try {
            ps = conn.prepareStatement("insert into hishopping_order_item(order_id,product_id,quantity,price,selected_color,selected_spec,sku_id,sku_text,snapshot_name,snapshot_image,item_unit_price,item_subtotal) values(?,?,?,?,?,?,?,?,?,?,?,?)");
            stockPs = conn.prepareStatement("update hishopping_product set stock=stock-?, sales=sales+?, sku_options=? where id=? and stock>=?");
            for (CartItem item : cart) {
                if (skuJsonByProduct.containsKey(item.getProduct().getId())) {
                    item.getProduct().setSkuOptions(skuJsonByProduct.get(item.getProduct().getId()));
                }
                SkuUtil.Sku sku = SkuUtil.choose(item.getProduct(), item.getSkuId(), item.getSelectedColor(), item.getSelectedSpec());
                double unitPrice = sku.getPrice();
                double subtotal = unitPrice * item.getQuantity();
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProduct().getId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, unitPrice);
                ps.setString(5, sku.getColor());
                ps.setString(6, sku.getSpec());
                ps.setString(7, sku.getSkuId());
                ps.setString(8, SkuUtil.skuText(sku));
                ps.setString(9, item.getProduct().getName());
                ps.setString(10, item.getProduct().getImageUrl());
                ps.setDouble(11, unitPrice);
                ps.setDouble(12, subtotal);
                ps.executeUpdate();
                String nextSkuJson = SkuUtil.decrementStockJson(item.getProduct(), sku.getSkuId(), item.getQuantity());
                skuJsonByProduct.put(item.getProduct().getId(), nextSkuJson);
                stockPs.setInt(1, item.getQuantity());
                stockPs.setInt(2, item.getQuantity());
                stockPs.setString(3, nextSkuJson);
                stockPs.setInt(4, item.getProduct().getId());
                stockPs.setInt(5, item.getQuantity());
                if (stockPs.executeUpdate() == 0) {
                    throw new SQLException(item.getProduct().getName() + "\u5e93\u5b58\u4e0d\u8db3");
                }
            }
        } finally {
            if (ps != null) ps.close();
            if (stockPs != null) stockPs.close();
        }
    }

    private UserCoupon findUsableCoupon(Connection conn, int userId, int userCouponId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("update hishop_user_coupon set status=N'EXPIRED' where user_id=? and status=N'UNUSED' and expire_time<sysdatetime(); select uc.* from hishop_user_coupon uc join hishop_coupon_template t on uc.coupon_id=t.coupon_id where uc.user_coupon_id=? and uc.user_id=? and uc.status=N'UNUSED' and (uc.expire_time is null or uc.expire_time>=sysdatetime()) and t.status=N'ENABLED'");
            ps.setInt(1, userId);
            ps.setInt(2, userCouponId);
            ps.setInt(3, userId);
            ps.execute();
            ps.getMoreResults();
            rs = ps.getResultSet();
            if (!rs.next()) {
                throw new RuntimeException("\u4f18\u60e0\u5238\u4e0d\u5b58\u5728\u3001\u5df2\u4f7f\u7528\u6216\u5df2\u8fc7\u671f\u3002");
            }
            UserCoupon coupon = new UserCoupon();
            coupon.setUserCouponId(rs.getInt("user_coupon_id"));
            coupon.setCouponId(rs.getInt("coupon_id"));
            coupon.setUserId(rs.getInt("user_id"));
            coupon.setCouponName(rs.getString("coupon_name"));
            coupon.setCouponType(rs.getString("coupon_type"));
            coupon.setAmount(rs.getDouble("amount"));
            coupon.setDiscountRate(rs.getDouble("discount_rate"));
            coupon.setMinAmount(rs.getDouble("min_amount"));
            coupon.setVipLevel(rs.getInt("vip_level"));
            coupon.setStatus(rs.getString("status"));
            coupon.setCouponOwnerType(rs.getString("coupon_owner_type"));
            coupon.setMerchantId(rs.getInt("merchant_id"));
            coupon.setStackable(rs.getBoolean("stackable"));
            coupon.setUseScope(rs.getString("use_scope"));
            coupon.setDescription(rs.getString("description"));
            return coupon;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    private void validatePlatformCoupon(UserCoupon coupon, boolean stackable, double total, int vipLevel) {
        if (!"PLATFORM".equals(coupon.getCouponOwnerType())) {
            throw new RuntimeException("\u8bf7\u9009\u62e9\u5e73\u53f0\u4f18\u60e0\u5238\u3002");
        }
        if (coupon.isStackable() != stackable) {
            throw new RuntimeException(stackable ? "\u8bf7\u5728\u53ef\u53e0\u52a0\u4f18\u60e0\u5238\u4e2d\u9009\u62e9\u3002" : "\u8bf7\u5728\u5e73\u53f0\u666e\u901a\u5238\u4e2d\u9009\u62e9\u3002");
        }
        couponDiscount(coupon, total, vipLevel);
    }

    private void validateMerchantCoupon(UserCoupon coupon, java.util.Map<Integer, Double> merchantTotals, int vipLevel) {
        if (!"MERCHANT".equals(coupon.getCouponOwnerType())) {
            throw new RuntimeException("\u8bf7\u9009\u62e9\u5e97\u94fa\u4f18\u60e0\u5238\u3002");
        }
        if (!merchantTotals.containsKey(coupon.getMerchantId()) || merchantTotals.get(coupon.getMerchantId()).doubleValue() <= 0) {
            throw new RuntimeException("\u5e97\u94fa\u5238\u4e0e\u8d2d\u7269\u8f66\u5546\u54c1\u4e0d\u5339\u914d\u3002");
        }
        couponDiscount(coupon, merchantTotals.get(coupon.getMerchantId()).doubleValue(), vipLevel);
    }

    private void appendCouponTitle(StringBuilder builder, String title) {
        if (title == null || title.trim().length() == 0) return;
        if (builder.length() > 0) builder.append(" + ");
        builder.append(title.trim());
    }

    private double couponDiscount(UserCoupon coupon, double total, int vipLevel) {
        if (total < coupon.getMinAmount()) {
            throw new RuntimeException("\u672a\u6ee1\u8db3\u4f18\u60e0\u5238\u4f7f\u7528\u95e8\u69db\u3002");
        }
        if (coupon.getVipLevel() > 0 && vipLevel < coupon.getVipLevel()) {
            throw new RuntimeException("\u5f53\u524dVIP\u7b49\u7ea7\u4e0d\u6ee1\u8db3\u8be5\u4f18\u60e0\u5238\u6761\u4ef6\u3002");
        }
        if ("DISCOUNT".equals(coupon.getCouponType())) {
            double rate = coupon.getDiscountRate() <= 0 ? 1 : coupon.getDiscountRate();
            return Math.max(0, total * (1 - rate));
        }
        return Math.min(Math.max(coupon.getAmount(), 0), total);
    }

    private double vipDiscount(double amount, int vipLevel) {
        double rate = vipDiscountRate(vipLevel);
        if (rate <= 0 || rate >= 1) return 0;
        return Math.max(0, amount * (1 - rate));
    }

    private double vipDiscountRate(int vipLevel) {
        if (vipLevel >= 10) return 0.80;
        if (vipLevel >= 9) return 0.82;
        if (vipLevel >= 8) return 0.85;
        if (vipLevel >= 7) return 0.88;
        if (vipLevel >= 6) return 0.90;
        if (vipLevel >= 5) return 0.93;
        if (vipLevel >= 4) return 0.95;
        if (vipLevel >= 3) return 0.97;
        if (vipLevel >= 2) return 0.98;
        return 1;
    }

    private void markCouponUsed(Connection conn, int userCouponId, int userId, int orderId) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("update hishop_user_coupon set status=N'USED', use_time=sysdatetime(), order_id=? where user_coupon_id=? and user_id=? and status=N'UNUSED'");
            ps.setInt(1, orderId);
            ps.setInt(2, userCouponId);
            ps.setInt(3, userId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("coupon status changed");
            }
        } finally {
            if (ps != null) ps.close();
        }
    }
    private List<OrderItem> findItems(int orderId, Connection conn) throws SQLException {
        String sql = "select oi.id item_id, oi.order_id, oi.quantity, oi.price item_price, p.*, c.name category_name from hishopping_order_item oi join hishopping_product p on oi.product_id=p.id left join hishopping_category c on p.category_id=c.id where oi.order_id=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<OrderItem> items = new ArrayList<OrderItem>();
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            ProductDao productDao = new ProductDao();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("item_price"));
                item.setSelectedColor(getString(rs, "selected_color"));
                item.setSelectedSpec(getString(rs, "selected_spec"));
                item.setSkuId(getString(rs, "sku_id"));
                item.setSkuText(getString(rs, "sku_text"));
                item.setSnapshotName(getString(rs, "snapshot_name"));
                item.setSnapshotImage(getString(rs, "snapshot_image"));
                item.setSubtotal(getDouble(rs, "item_subtotal", item.getPrice() * item.getQuantity()));
                Product p = productDao.mapProduct(rs);
                item.setProduct(p);
                enrichItem(item, conn);
                items.add(item);
            }
            return items;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    private List<OrderItem> findItemsByMerchant(int orderId, int merchantId, Connection conn) throws SQLException {
        String sql = "select oi.id item_id, oi.order_id, oi.quantity, oi.price item_price, p.*, c.name category_name from hishopping_order_item oi join hishopping_product p on oi.product_id=p.id left join hishopping_category c on p.category_id=c.id where oi.order_id=? and p.merchant_id=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<OrderItem> items = new ArrayList<OrderItem>();
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            ps.setInt(2, merchantId);
            rs = ps.executeQuery();
            ProductDao productDao = new ProductDao();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("item_price"));
                item.setSelectedColor(getString(rs, "selected_color"));
                item.setSelectedSpec(getString(rs, "selected_spec"));
                item.setSkuId(getString(rs, "sku_id"));
                item.setSkuText(getString(rs, "sku_text"));
                item.setSnapshotName(getString(rs, "snapshot_name"));
                item.setSnapshotImage(getString(rs, "snapshot_image"));
                item.setSubtotal(getDouble(rs, "item_subtotal", item.getPrice() * item.getQuantity()));
                item.setProduct(productDao.mapProduct(rs));
                enrichItem(item, conn);
                items.add(item);
            }
            return items;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    private double merchantSubtotal(Order order) {
        double total = 0;
        for (OrderItem item : order.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void enrichOrder(Order order, Connection conn) throws SQLException {
        order.setShipments(businessDao.shipmentsForOrder(order.getId(), conn));
        PreparedStatement ps = null;
        ResultSet rs = null;
        java.util.List<hishopping.entity.AfterSale> afterSales = new java.util.ArrayList<hishopping.entity.AfterSale>();
        try {
            ps = conn.prepareStatement("select * from hishop_after_sale where order_id=? order by after_sale_id desc");
            ps.setInt(1, order.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                hishopping.entity.AfterSale a = new hishopping.entity.AfterSale();
                a.setAfterSaleId(rs.getInt("after_sale_id"));
                a.setOrderId(rs.getInt("order_id"));
                a.setUserId(rs.getInt("user_id"));
                a.setMerchantId(rs.getInt("merchant_id"));
                a.setProductId(rs.getInt("product_id"));
                a.setAfterSaleType(rs.getString("after_sale_type"));
                a.setReason(rs.getString("reason"));
                a.setRefundAmount(rs.getDouble("refund_amount"));
                a.setStatus(rs.getString("status"));
                a.setApplyTime(String.valueOf(rs.getTimestamp("apply_time")));
                a.setHandleOpinion(rs.getString("handle_opinion"));
                a.setHandleTime(rs.getTimestamp("handle_time") == null ? "" : String.valueOf(rs.getTimestamp("handle_time")));
                afterSales.add(a);
            }
            order.setAfterSales(afterSales);
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    private void enrichItem(OrderItem item, Connection conn) throws SQLException {
        if (item.getProduct() == null) return;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("select top 1 status from hishop_after_sale where order_id=? and product_id=? order by after_sale_id desc");
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProduct().getId());
            rs = ps.executeQuery();
            if (rs.next()) item.setAfterSaleStatus(rs.getString("status"));
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select count(1) from hishop_product_review where order_id=? and product_id=?");
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProduct().getId());
            rs = ps.executeQuery();
            item.setReviewed(rs.next() && rs.getInt(1) > 0);
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setBatchNo(getString(rs, "batch_no"));
        order.setUserId(rs.getInt("user_id"));
        order.setMerchantId(getInt(rs, "merchant_id", 0));
        order.setShopName(getString(rs, "shop_name"));
        order.setGoodsAmount(getDouble(rs, "goods_amount", 0));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setDiscountAmount(rs.getDouble("discount_amount"));
        order.setStatus(rs.getString("status"));
        order.setCreateTime(rs.getString("create_time"));
        order.setReceiverName(rs.getString("receiver_name"));
        order.setReceiverPhone(rs.getString("receiver_phone"));
        order.setReceiverAddress(rs.getString("receiver_address"));
        return order;
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private String getString(ResultSet rs, String column) {
        try {
            Object value = rs.getObject(column);
            return value == null ? "" : String.valueOf(value);
        } catch (SQLException e) {
            return "";
        }
    }

    private double getDouble(ResultSet rs, String column, double fallback) {
        try {
            Object value = rs.getObject(column);
            return value == null ? fallback : rs.getDouble(column);
        } catch (SQLException e) {
            return fallback;
        }
    }

    private int getInt(ResultSet rs, String column, int fallback) {
        try {
            Object value = rs.getObject(column);
            return value == null ? fallback : rs.getInt(column);
        } catch (SQLException e) {
            return fallback;
        }
    }
}


