package hishopping.entity;

public class CartItem {
    private int id;
    private int userId;
    private Product product;
    private int quantity;
    private String selectedColor;
    private String selectedSpec;
    private String skuId;
    private String skuText;
    private double skuPrice;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }
    public String getSelectedSpec() { return selectedSpec; }
    public void setSelectedSpec(String selectedSpec) { this.selectedSpec = selectedSpec; }
    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    public String getSkuText() { return skuText; }
    public void setSkuText(String skuText) { this.skuText = skuText; }
    public double getSkuPrice() { return skuPrice; }
    public void setSkuPrice(double skuPrice) { this.skuPrice = skuPrice; }
}

