package hishopping.entity;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String orderNo;
    private String batchNo;
    private int userId;
    private int merchantId;
    private String shopName;
    private double goodsAmount;
    private double totalAmount;
    private double discountAmount;
    private String status;
    private String createTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private List<OrderItem> items = new ArrayList<OrderItem>();
    private List<Shipment> shipments = new ArrayList<Shipment>();
    private List<AfterSale> afterSales = new ArrayList<AfterSale>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public double getGoodsAmount() { return goodsAmount; }
    public void setGoodsAmount(double goodsAmount) { this.goodsAmount = goodsAmount; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public List<Shipment> getShipments() { return shipments; }
    public void setShipments(List<Shipment> shipments) { this.shipments = shipments; }
    public List<AfterSale> getAfterSales() { return afterSales; }
    public void setAfterSales(List<AfterSale> afterSales) { this.afterSales = afterSales; }
}

