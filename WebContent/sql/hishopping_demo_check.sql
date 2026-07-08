/*
  HiShopping 演示检查脚本
  用法：
  1. 本脚本只查询展示结果，不修改任何数据。
  2. 适合演示当天在 SQL Server 中运行，给老师查看账号、商家、商品、订单、评价、举报等表数据。
  3. 如果数据库是新建空库，请先运行 hishopping.sql，再运行 hishopping_demo.sql，最后运行本脚本查看结果。
*/

USE hishopping;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

SELECT N'当前数据库连接' AS info_name,
       @@SERVERNAME AS server_name,
       DB_NAME() AS database_name,
       SUSER_SNAME() AS login_name,
       SYSDATETIME() AS check_time;

SELECT N'演示登录账号' AS section_name,
       N'管理员' AS role_name,
       N'admin' AS login_account,
       N'123456' AS password,
       N'后台概览、商家审核、商品审核、举报管理、优惠券、数据分析' AS demo_path
UNION ALL SELECT N'演示登录账号', N'用户', N'linxiaoyu@hishopping.com', N'123456', N'购物车、优惠券、订单、售后、举报、消息'
UNION ALL SELECT N'演示登录账号', N'用户', N'chenyuanhang@hishopping.com', N'123456', N'新人券、已完成订单、评价、好友申请'
UNION ALL SELECT N'演示登录账号', N'用户', N'zhounian@hishopping.com', N'123456', N'高等级会员、发货订单、数码商品评价'
UNION ALL SELECT N'演示登录账号', N'商家', N'8801001', N'123456', N'晨光优选生活馆：商品、订单、优惠券、数据分析'
UNION ALL SELECT N'演示登录账号', N'商家', N'8801002', N'123456', N'星河数码旗舰店：数码商品、售后、评价回复'
UNION ALL SELECT N'演示登录账号', N'待审商家', N'8801003', N'123456', N'管理员端审核流程';

SELECT N'核心数据量' AS section_name, N'hishopping_user' AS table_name, COUNT(*) AS row_count, N'用户账号' AS detail FROM dbo.hishopping_user
UNION ALL SELECT N'核心数据量', N'hishop_merchant', COUNT(*), N'商家账号' FROM dbo.hishop_merchant
UNION ALL SELECT N'核心数据量', N'hishopping_category', COUNT(*), N'商品分类' FROM dbo.hishopping_category
UNION ALL SELECT N'核心数据量', N'hishopping_product', COUNT(*), N'商品信息' FROM dbo.hishopping_product
UNION ALL SELECT N'核心数据量', N'hishopping_order', COUNT(*), N'订单主表' FROM dbo.hishopping_order
UNION ALL SELECT N'核心数据量', N'hishopping_order_item', COUNT(*), N'订单明细' FROM dbo.hishopping_order_item
UNION ALL SELECT N'核心数据量', N'hishop_coupon_template', COUNT(*), N'优惠券模板' FROM dbo.hishop_coupon_template
UNION ALL SELECT N'核心数据量', N'hishop_user_coupon', COUNT(*), N'用户优惠券' FROM dbo.hishop_user_coupon
UNION ALL SELECT N'核心数据量', N'hishop_product_review', COUNT(*), N'商品评价' FROM dbo.hishop_product_review
UNION ALL SELECT N'核心数据量', N'hishop_report', COUNT(*), N'举报记录' FROM dbo.hishop_report
UNION ALL SELECT N'核心数据量', N'hishopping_message', COUNT(*), N'消息记录' FROM dbo.hishopping_message
UNION ALL SELECT N'核心数据量', N'hishopping_hall_banner', COUNT(*), N'大厅展示轮播' FROM dbo.hishopping_hall_banner;

SELECT N'演示用户明细' AS section_name,
       id,
       account_id,
       username,
       email,
       phone,
       points,
       vip_level,
       growth_value,
       status
FROM dbo.hishopping_user
WHERE email IN (
    N'linxiaoyu@hishopping.com',
    N'chenyuanhang@hishopping.com',
    N'zhounian@hishopping.com',
    N'guxinghe@hishopping.com'
)
ORDER BY id;

SELECT N'演示商家明细' AS section_name,
       merchant_id,
       merchant_code,
       merchant_name,
       shop_name,
       business_category,
       status,
       reject_reason
FROM dbo.hishop_merchant
WHERE merchant_code IN (N'8801001', N'8801002', N'8801003', N'8801004')
ORDER BY merchant_code;

SELECT N'演示商品按商家统计' AS section_name,
       m.merchant_code,
       m.shop_name,
       COUNT(*) AS product_count,
       SUM(CASE WHEN p.audit_status = N'APPROVED' THEN 1 ELSE 0 END) AS approved_count,
       SUM(CASE WHEN p.audit_status = N'PENDING' THEN 1 ELSE 0 END) AS pending_count,
       SUM(CASE WHEN p.audit_status = N'REJECTED' THEN 1 ELSE 0 END) AS rejected_count,
       SUM(CASE WHEN p.image_url IS NULL OR LTRIM(RTRIM(p.image_url)) = N'' THEN 1 ELSE 0 END) AS icon_thumbnail_count,
       SUM(CASE WHEN p.sku_options IS NULL OR LTRIM(RTRIM(p.sku_options)) = N'' THEN 0 ELSE 1 END) AS sku_product_count
FROM dbo.hishopping_product p
JOIN dbo.hishop_merchant m ON m.merchant_id = p.merchant_id
WHERE m.merchant_code IN (N'8801001', N'8801002', N'8801003', N'8801004')
GROUP BY m.merchant_code, m.shop_name
ORDER BY m.merchant_code;

SELECT N'新增SKU商品明细' AS section_name,
       m.merchant_code,
       m.shop_name,
       p.name,
       c.name AS category_name,
       p.price,
       p.stock,
       p.icon_text,
       CASE WHEN p.image_url IS NULL OR LTRIM(RTRIM(p.image_url)) = N'' THEN N'文字图标' ELSE N'图片缩略图' END AS thumbnail_type,
       CASE WHEN p.sku_options IS NULL OR LTRIM(RTRIM(p.sku_options)) = N'' THEN N'无SKU' ELSE N'有SKU' END AS sku_status,
       p.sale_status,
       p.audit_status
FROM dbo.hishopping_product p
JOIN dbo.hishop_merchant m ON m.merchant_id = p.merchant_id
JOIN dbo.hishopping_category c ON c.id = p.category_id
WHERE p.name IN (
    N'冷萃咖啡随享装', N'陶瓷便当餐具套装', N'抽纸湿巾家庭囤货箱',
    N'磁吸快充移动电源', N'无线静音办公键鼠', N'桌面空气循环扇',
    N'经典文学阅读计划', N'前端基础练习册', N'学习资料电子包',
    N'轻量徒步双肩包', N'速干运动短袖', N'折叠露营椅'
)
ORDER BY m.merchant_code, p.id;

SELECT N'演示订单状态统计' AS section_name,
       status,
       COUNT(*) AS order_count,
       SUM(total_amount) AS total_amount
FROM dbo.hishopping_order
WHERE order_no LIKE N'SHOW20260709%'
GROUP BY status
ORDER BY status;

SELECT N'演示订单明细' AS section_name,
       order_no,
       shop_name,
       total_amount,
       discount_amount,
       coupon_title,
       status,
       receiver_name,
       create_time
FROM dbo.hishopping_order
WHERE order_no LIKE N'SHOW20260709%'
ORDER BY order_no;

SELECT N'评价与售后展示' AS section_name,
       o.order_no,
       p.name AS product_name,
       r.rating,
       r.content AS review_content,
       r.like_count,
       a.after_sale_type,
       a.status AS after_sale_status,
       a.reason AS after_sale_reason
FROM dbo.hishop_product_review r
JOIN dbo.hishopping_order o ON o.id = r.order_id
JOIN dbo.hishopping_product p ON p.id = r.product_id
LEFT JOIN dbo.hishop_after_sale a ON a.order_id = o.id AND a.product_id = p.id
WHERE o.order_no LIKE N'SHOW20260709%'
ORDER BY o.order_no, r.review_id;

SELECT N'举报处理展示' AS section_name,
       report_id,
       reporter_role,
       reporter_name,
       target_role,
       target_name,
       report_type,
       reason,
       status,
       handle_opinion,
       handle_result
FROM dbo.hishop_report
WHERE reason LIKE N'展示举报-%'
ORDER BY report_id;

SELECT N'优惠券展示' AS section_name,
       coupon_name,
       coupon_type,
       amount,
       discount_rate,
       min_amount,
       coupon_owner_type,
       status,
       valid_days
FROM dbo.hishop_coupon_template
WHERE coupon_name LIKE N'展示%' OR coupon_name LIKE N'晨光%' OR coupon_name LIKE N'星河%'
ORDER BY coupon_owner_type, coupon_id;
GO
