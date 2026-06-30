package hishopping.entity;

public class Favorite {
    private int id;
    private int userId;
    private int productId;
    private Product product;
    private String createTime;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
