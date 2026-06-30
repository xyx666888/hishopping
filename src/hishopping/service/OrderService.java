package hishopping.service;

import java.util.List;

import hishopping.dao.OrderDao;
import hishopping.entity.Order;

public class OrderService {
    private OrderDao orderDao = new OrderDao();

    public List<Order> list(int userId) {
        return orderDao.findByUserId(userId);
    }

    public List<Order> all() {
        return orderDao.findAll();
    }

    public List<Order> merchantOrders(int merchantId) {
        if (merchantId <= 0) {
            throw new RuntimeException("商家编号不能为空。");
        }
        return orderDao.findByMerchantId(merchantId);
    }

    public Order create(int userId, int userCouponId, int addressId) {
        return orderDao.createFromCart(userId, userCouponId, addressId);
    }

    public Order create(int userId, int platformCouponId, int stackableCouponId, int merchantCouponId, int addressId) {
        return orderDao.createFromCart(userId, platformCouponId, stackableCouponId, merchantCouponId, addressId);
    }

    public Order create(int userId, int platformCouponId, int stackableCouponId, int merchantCouponId, int addressId, int[] cartItemIds) {
        return orderDao.createFromCart(userId, platformCouponId, stackableCouponId, merchantCouponId, addressId, cartItemIds);
    }

    public void updateStatus(int orderId, String status) {
        if (orderId <= 0 || status == null || status.trim().length() == 0) {
            throw new RuntimeException("订单编号和状态不能为空。");
        }
        orderDao.updateStatus(orderId, status.trim());
    }

    public void pay(int userId, int orderId) {
        Order order = requireUserOrder(userId, orderId);
        if (!"待付款".equals(order.getStatus())) {
            throw new RuntimeException("只有待付款订单可以支付。");
        }
        orderDao.updateStatus(orderId, "待发货");
    }

    public void confirm(int userId, int orderId) {
        Order order = requireUserOrder(userId, orderId);
        if (!"待收货".equals(order.getStatus())) {
            throw new RuntimeException("只有待收货订单可以确认收货。");
        }
        orderDao.updateStatus(orderId, "已完成");
    }

    public void cancel(int userId, int orderId) {
        Order order = requireUserOrder(userId, orderId);
        if (!"待付款".equals(order.getStatus())) {
            throw new RuntimeException("只有待付款订单可以取消。");
        }
        orderDao.updateStatus(orderId, "已取消");
    }

    public void ship(int merchantId, int orderId) {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在。");
        }
        if (!"待发货".equals(order.getStatus())) {
            throw new RuntimeException("只有待发货订单可以发货。");
        }
        orderDao.updateStatusIfMerchantOrder(orderId, merchantId, "待收货");
    }

    public void ship(int merchantId, int orderId, String expressCompany, String trackingNo) {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("\u8ba2\u5355\u4e0d\u5b58\u5728\u3002");
        }
        if (!"\u5f85\u53d1\u8d27".equals(order.getStatus())) {
            throw new RuntimeException("\u53ea\u6709\u5f85\u53d1\u8d27\u8ba2\u5355\u53ef\u4ee5\u53d1\u8d27\u3002");
        }
        if (expressCompany == null || expressCompany.trim().length() == 0 || trackingNo == null || trackingNo.trim().length() == 0) {
            throw new RuntimeException("\u8bf7\u586b\u5199\u5feb\u9012\u516c\u53f8\u548c\u8fd0\u5355\u53f7\u3002");
        }
        orderDao.updateStatusIfMerchantOrder(orderId, merchantId, "\u5f85\u6536\u8d27", expressCompany.trim(), trackingNo.trim());
    }

    public void delete(int orderId) {
        if (orderId <= 0) {
            throw new RuntimeException("订单编号不能为空。");
        }
        orderDao.delete(orderId);
    }

    private Order requireUserOrder(int userId, int orderId) {
        if (userId <= 0 || orderId <= 0) {
            throw new RuntimeException("订单编号不能为空。");
        }
        Order order = orderDao.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在或不属于当前用户。");
        }
        return order;
    }
}

