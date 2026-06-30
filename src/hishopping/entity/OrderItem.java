package hishopping.entity;

public class OrderItem {
    private int id;
    private int orderId;
    private Product product;
    private int quantity;
    private double price;
    private String selectedColor;
    private String selectedSpec;
    private String skuId;
    private String skuText;
    private String snapshotName;
    private String snapshotImage;
    private double subtotal;
    private boolean reviewed;
    private String afterSaleStatus;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }
    public String getSelectedSpec() { return selectedSpec; }
    public void setSelectedSpec(String selectedSpec) { this.selectedSpec = selectedSpec; }
    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    public String getSkuText() { return skuText; }
    public void setSkuText(String skuText) { this.skuText = skuText; }
    public String getSnapshotName() { return snapshotName; }
    public void setSnapshotName(String snapshotName) { this.snapshotName = snapshotName; }
    public String getSnapshotImage() { return snapshotImage; }
    public void setSnapshotImage(String snapshotImage) { this.snapshotImage = snapshotImage; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
    public String getAfterSaleStatus() { return afterSaleStatus; }
    public void setAfterSaleStatus(String afterSaleStatus) { this.afterSaleStatus = afterSaleStatus; }
}

