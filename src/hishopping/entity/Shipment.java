package hishopping.entity;

public class Shipment {
    private int shipmentId;
    private int orderId;
    private int merchantId;
    private String expressCompany;
    private String trackingNo;
    private String shipTime;

    public int getShipmentId() { return shipmentId; }
    public void setShipmentId(int shipmentId) { this.shipmentId = shipmentId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }
    public String getExpressCompany() { return expressCompany; }
    public void setExpressCompany(String expressCompany) { this.expressCompany = expressCompany; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getShipTime() { return shipTime; }
    public void setShipTime(String shipTime) { this.shipTime = shipTime; }
}
