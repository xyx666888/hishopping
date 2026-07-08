/*
  HiShopping 报告截图 SQL 示例
  用法：
  1. 本脚本用于论文/报告中“重要查询、插入、删除、更新 SQL 语句及运行结果”截图。
  2. 查询语句会直接读取真实业务表。
  3. 插入、删除、更新语句均使用事务并在末尾 ROLLBACK，不会破坏现有演示数据。
  4. 建议在 SQL Server Management Studio 中分段选中执行，分别截图每一段 SQL 和结果。
*/

USE hishopping;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* 1. 重要的查询 SQL 语句：查询商品、商家、分类和 SKU 状态 */
SELECT TOP 20
       p.id AS product_id,
       p.name AS product_name,
       c.name AS category_name,
       m.shop_name,
       p.price,
       p.stock,
       p.sales,
       p.sale_status,
       p.audit_status,
       CASE
           WHEN p.sku_options IS NULL OR LTRIM(RTRIM(p.sku_options)) = N'' THEN N'无 SKU'
           ELSE N'已配置 SKU'
       END AS sku_status
FROM dbo.hishopping_product p
LEFT JOIN dbo.hishopping_category c ON c.id = p.category_id
LEFT JOIN dbo.hishop_merchant m ON m.merchant_id = p.merchant_id
WHERE p.name IN (
    N'冷萃咖啡随享装', N'陶瓷便当餐具套装', N'抽纸湿巾家庭囤货箱',
    N'磁吸快充移动电源', N'无线静音办公键鼠', N'桌面空气循环扇',
    N'经典文学阅读计划', N'前端基础练习册', N'学习资料电子包',
    N'轻量徒步双肩包', N'速干运动短袖', N'折叠露营椅'
)
ORDER BY m.merchant_code, p.id;
GO

/* 2. 重要的插入 SQL 语句：插入一条演示分类，展示后回滚 */
BEGIN TRAN;

DECLARE @demo_category_name NVARCHAR(50) = N'截图演示分类';

IF NOT EXISTS (
    SELECT 1
    FROM dbo.hishopping_category
    WHERE name = @demo_category_name
)
BEGIN
    INSERT INTO dbo.hishopping_category(name, icon_text, description, sort_no)
    VALUES(@demo_category_name, N'示', N'用于报告截图的临时分类数据', 98);
END

SELECT id,
       name,
       icon_text,
       description,
       sort_no
FROM dbo.hishopping_category
WHERE name = @demo_category_name;

ROLLBACK TRAN;
GO

/* 3. 重要的删除 SQL 语句：删除一条临时地址，展示后回滚 */
BEGIN TRAN;

DECLARE @demo_user_id INT;

SELECT @demo_user_id = id
FROM dbo.hishopping_user
WHERE email = N'linxiaoyu@hishopping.com';

INSERT INTO dbo.hishopping_address(user_id, receiver_name, phone, province, city, district, detail, is_default)
VALUES(@demo_user_id, N'删除演示收件人', N'13890019999', N'江苏省', N'南京市', N'雨花台区', N'截图演示临时地址', 0);

SELECT id,
       user_id,
       receiver_name,
       phone,
       province,
       city,
       district,
       detail
FROM dbo.hishopping_address
WHERE receiver_name = N'删除演示收件人';

DELETE FROM dbo.hishopping_address
WHERE receiver_name = N'删除演示收件人'
  AND user_id = @demo_user_id;

SELECT id,
       user_id,
       receiver_name,
       detail
FROM dbo.hishopping_address
WHERE receiver_name = N'删除演示收件人';

ROLLBACK TRAN;
GO

/* 4. 重要的更新 SQL 语句：更新商品库存和审核状态，展示后回滚 */
BEGIN TRAN;

SELECT id,
       name,
       stock,
       sale_status,
       audit_status,
       audit_opinion
FROM dbo.hishopping_product
WHERE name = N'冷萃咖啡随享装';

UPDATE dbo.hishopping_product
SET stock = stock + 10,
    audit_status = N'APPROVED',
    audit_opinion = N'报告截图演示：管理员复核通过',
    review_time = SYSDATETIME(),
    review_admin_id = 1
WHERE name = N'冷萃咖啡随享装';

SELECT id,
       name,
       stock,
       sale_status,
       audit_status,
       audit_opinion,
       review_time
FROM dbo.hishopping_product
WHERE name = N'冷萃咖啡随享装';

ROLLBACK TRAN;
GO

/* 5. 可选查询：统计订单、评价、举报等演示数据 */
SELECT N'订单' AS data_type, COUNT(*) AS row_count FROM dbo.hishopping_order WHERE order_no LIKE N'SHOW20260709%'
UNION ALL SELECT N'评价', COUNT(*) FROM dbo.hishop_product_review
UNION ALL SELECT N'举报', COUNT(*) FROM dbo.hishop_report WHERE reason LIKE N'展示举报-%'
UNION ALL SELECT N'优惠券', COUNT(*) FROM dbo.hishop_coupon_template
UNION ALL SELECT N'消息', COUNT(*) FROM dbo.hishopping_message;
GO
