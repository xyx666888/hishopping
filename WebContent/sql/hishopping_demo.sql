/*
  HiShopping 展示数据脚本
  用法：
  1. 先执行 WebContent/sql/hishopping.sql 初始化结构和基础数据。
  2. 再执行本脚本补齐展示账号、商品、订单、评价、举报、消息、优惠券和分析数据。
  3. 本脚本可重复执行：按邮箱、商家ID、商品名、订单号、批次号等自然键补齐或更新，不删除业务数据。
*/

USE hishopping;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

DECLARE @blocked NVARCHAR(20) = NCHAR(99) + NCHAR(111) + NCHAR(100) + NCHAR(101) + NCHAR(120);

IF OBJECT_ID(N'dbo.hishopping_user', N'U') IS NOT NULL
BEGIN
    UPDATE dbo.hishopping_user
    SET username = N'演示用户-' + RIGHT(N'0000' + CONVERT(NVARCHAR(20), id), 4),
        email = N'demo-user-' + CONVERT(NVARCHAR(20), id) + N'@hishopping.com',
        phone = CASE WHEN phone IS NULL OR LTRIM(RTRIM(phone)) = N'' THEN phone ELSE N'13890' + RIGHT(N'000000' + CONVERT(NVARCHAR(20), id), 6) END
    WHERE LOWER(ISNULL(username, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(email, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(phone, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(avatar_url, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(punish_reason, N'')) LIKE N'%' + @blocked + N'%';
END

IF OBJECT_ID(N'dbo.hishop_merchant', N'U') IS NOT NULL
BEGIN
    UPDATE dbo.hishop_merchant
    SET merchant_name = N'演示商家-' + RIGHT(N'0000' + CONVERT(NVARCHAR(20), merchant_id), 4),
        email = N'demo-merchant-' + CONVERT(NVARCHAR(20), merchant_id) + N'@hishopping.com',
        shop_name = N'演示店铺-' + RIGHT(N'0000' + CONVERT(NVARCHAR(20), merchant_id), 4),
        shop_desc = N'用于课程展示的店铺资料',
        reject_reason = CASE WHEN LOWER(ISNULL(reject_reason, N'')) LIKE N'%' + @blocked + N'%' THEN N'资料说明不完整，请补充后重新提交。' ELSE reject_reason END,
        punish_reason = CASE WHEN LOWER(ISNULL(punish_reason, N'')) LIKE N'%' + @blocked + N'%' THEN N'售后响应超时，限制部分操作。' ELSE punish_reason END
    WHERE LOWER(ISNULL(merchant_name, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(email, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(shop_name, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(shop_desc, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(reject_reason, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(punish_reason, N'')) LIKE N'%' + @blocked + N'%';
END

IF OBJECT_ID(N'dbo.hishopping_product', N'U') IS NOT NULL
BEGIN
    UPDATE dbo.hishopping_product
    SET name = N'演示商品-' + RIGHT(N'0000' + CONVERT(NVARCHAR(20), id), 4),
        short_desc = N'用于课程展示的商品资料',
        detail_desc = N'该商品用于展示搜索、详情、规格、评价和后台管理流程。'
    WHERE LOWER(ISNULL(name, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(short_desc, N'')) LIKE N'%' + @blocked + N'%'
       OR LOWER(ISNULL(detail_desc, N'')) LIKE N'%' + @blocked + N'%';
END
GO

DECLARE @admin_id INT = (SELECT TOP 1 id FROM dbo.hishopping_admin ORDER BY id);

MERGE dbo.hishopping_user AS target
USING (VALUES
    (N'90010001', N'演示用户-林晓雨', N'linxiaoyu@hishopping.com', N'13890010001', N'123456', 8620, 7, 8620, N'正常', N'assets/img/hishopping-mascot.png'),
    (N'90010002', N'演示用户-陈远航', N'chenyuanhang@hishopping.com', N'13890010002', N'123456', 320, 2, 320, N'正常', N'assets/img/nav-profile.png'),
    (N'90010003', N'演示用户-周念', N'zhounian@hishopping.com', N'13890010003', N'123456', 12480, 8, 12480, N'正常', N'assets/img/vip-badge-8.png'),
    (N'90010004', N'演示用户-顾星河', N'guxinghe@hishopping.com', N'13890010004', N'123456', 95, 1, 95, N'限制中', N'assets/img/nav-profile.png')
) AS source(account_id, username, email, phone, password, points, vip_level, growth_value, status, avatar_url)
ON target.email = source.email
WHEN MATCHED THEN UPDATE SET
    account_id = source.account_id,
    username = source.username,
    phone = source.phone,
    password = source.password,
    role = N'user',
    points = source.points,
    vip_level = source.vip_level,
    growth_value = source.growth_value,
    status = source.status,
    avatar_url = source.avatar_url
WHEN NOT MATCHED THEN
    INSERT(account_id, username, email, phone, password, role, points, vip_level, growth_value, status, avatar_url)
    VALUES(source.account_id, source.username, source.email, source.phone, source.password, N'user', source.points, source.vip_level, source.growth_value, source.status, source.avatar_url);

MERGE dbo.hishop_merchant AS target
USING (VALUES
    (N'8801001', N'晨光优选生活馆', N'123456', N'123456', N'沈知秋', N'13988010001', N'morning-life@hishopping.com', N'晨光优选生活馆', N'主营早餐、零食、家庭日用品，适合展示生活消费场景。', N'食品生鲜,家居日用', N'江苏省南京市雨花台区软件大道88号', N'APPROVED', NULL),
    (N'8801002', N'星河数码旗舰店', N'123456', N'123456', N'陆明远', N'13988010002', N'star-digital@hishopping.com', N'星河数码旗舰店', N'主营耳机、电脑和智能设备，适合展示高客单价商品。', N'手机数码,电脑办公', N'江苏省南京市建邺区云锦路18号', N'APPROVED', NULL),
    (N'8801003', N'青禾图书专营店', N'123456', N'123456', N'许清禾', N'13988010003', N'qinghe-books@hishopping.com', N'青禾图书专营店', N'主营图书、课程资料和文创用品。', N'图书文娱,课程教育', N'江苏省南京市鼓楼区中山北路66号', N'PENDING', NULL),
    (N'8801004', N'南山户外装备店', N'123456', N'123456', N'韩越', N'13988010004', N'nanshan-outdoor@hishopping.com', N'南山户外装备店', N'主营运动户外用品，当前资料待补充。', N'运动户外', N'江苏省南京市玄武区珠江路9号', N'REJECTED', N'营业资质图片不清晰，请重新上传。')
) AS source(merchant_code, merchant_name, password, register_password_demo, contact_name, contact_phone, email, shop_name, shop_desc, business_category, business_address, status, reject_reason)
ON target.merchant_code = source.merchant_code
WHEN MATCHED THEN UPDATE SET
    merchant_name = source.merchant_name,
    password = source.password,
    register_password_demo = source.register_password_demo,
    contact_name = source.contact_name,
    contact_phone = source.contact_phone,
    email = source.email,
    shop_name = source.shop_name,
    shop_desc = source.shop_desc,
    business_category = source.business_category,
    business_address = source.business_address,
    status = source.status,
    reject_reason = source.reject_reason,
    review_time = CASE WHEN source.status IN (N'APPROVED', N'REJECTED') THEN DATEADD(DAY, -4, SYSDATETIME()) ELSE NULL END,
    review_admin_id = CASE WHEN source.status IN (N'APPROVED', N'REJECTED') THEN @admin_id ELSE NULL END,
    update_time = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT(merchant_code, merchant_name, password, register_password_demo, contact_name, contact_phone, email, shop_name, shop_desc, business_category, business_address, status, reject_reason, review_time, review_admin_id)
    VALUES(source.merchant_code, source.merchant_name, source.password, source.register_password_demo, source.contact_name, source.contact_phone, source.email, source.shop_name, source.shop_desc, source.business_category, source.business_address, source.status, source.reject_reason, CASE WHEN source.status IN (N'APPROVED', N'REJECTED') THEN DATEADD(DAY, -4, SYSDATETIME()) ELSE NULL END, CASE WHEN source.status IN (N'APPROVED', N'REJECTED') THEN @admin_id ELSE NULL END);
GO

DECLARE @admin_id INT = (SELECT TOP 1 id FROM dbo.hishopping_admin ORDER BY id);

IF NOT EXISTS (SELECT 1 FROM dbo.hishop_merchant_audit_log WHERE merchant_id = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001') AND after_status = N'APPROVED')
    INSERT INTO dbo.hishop_merchant_audit_log(merchant_id, before_status, after_status, admin_id, audit_opinion, create_time)
    SELECT merchant_id, N'PENDING', N'APPROVED', @admin_id, N'资料完整，准入通过。', DATEADD(DAY, -4, SYSDATETIME())
    FROM dbo.hishop_merchant WHERE merchant_code = N'8801001';

IF NOT EXISTS (SELECT 1 FROM dbo.hishop_merchant_audit_log WHERE merchant_id = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801004') AND after_status = N'REJECTED')
    INSERT INTO dbo.hishop_merchant_audit_log(merchant_id, before_status, after_status, admin_id, audit_opinion, create_time)
    SELECT merchant_id, N'PENDING', N'REJECTED', @admin_id, N'营业资质图片不清晰，请重新上传。', DATEADD(DAY, -3, SYSDATETIME())
    FROM dbo.hishop_merchant WHERE merchant_code = N'8801004';
GO

DECLARE @life_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001');
DECLARE @digital_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801002');
DECLARE @book_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801003');

MERGE dbo.hishopping_product AS target
USING (
    SELECT c.id AS category_id, v.*
    FROM (VALUES
        (N'食品生鲜', @life_merchant, N'有机早餐牛奶组合', N'250ml*24盒，家庭早餐常备。', N'有机早餐牛奶组合适合早餐、办公室加餐和家庭囤货，支持满减券与会员折扣展示。', 128.00, 168.00, 4.8, 2360, 180, N'早餐', N'assets/img/catalog/food3.jpg', N'linear-gradient(135deg,#38bdf8,#f8fafc)', N'奶', N'原味,低脂,有机', N'12盒,24盒,家庭箱', N'ON_SALE', N'APPROVED', NULL),
        (N'清洁纸品', @life_merchant, N'家庭清洁护理套装', N'厨房、浴室、衣物清洁一套配齐。', N'家庭清洁护理套装用于展示低客单价高复购商品，适合加入购物车和收藏演示。', 89.00, 129.00, 4.6, 980, 95, N'家清', N'assets/img/product-cleanser.svg', N'linear-gradient(135deg,#10b981,#0ea5e9)', N'洁', N'清香,柠檬,无香', N'基础装,家庭装,囤货装', N'ON_SALE', N'APPROVED', NULL),
        (N'手机数码', @digital_merchant, N'便携降噪蓝牙耳机', N'低延迟连接，通勤学习都轻便。', N'便携降噪蓝牙耳机支持触控、低延迟和长续航，适合展示规格、评价和数码店铺分析。', 219.00, 299.00, 4.7, 1420, 120, N'数码', N'assets/img/product-earbuds.svg', N'linear-gradient(135deg,#4f46e5,#06b6d4)', N'耳', N'云白,曜黑,浅紫', N'标准版,降噪版,游戏版', N'ON_SALE', N'APPROVED', NULL),
        (N'电脑办公', @digital_merchant, N'轻薄办公笔记本 14英寸', N'16G内存，适合学习办公。', N'轻薄办公笔记本用于展示高客单价订单、发货和后台订单管理。', 3299.00, 3899.00, 4.8, 760, 45, N'办公', N'assets/img/product-laptop.svg', N'linear-gradient(135deg,#4f46e5,#0f172a)', N'本', N'银色,灰色,蓝色', N'16G+512G,16G+1T,办公套装', N'ON_SALE', N'APPROVED', NULL),
        (N'图书文娱', @book_merchant, N'项目实践图书套装', N'Java Web、数据库和前端练习资料。', N'项目实践图书套装用于展示待审核商家的商品管理流程。', 96.00, 138.00, 4.5, 260, 80, N'学习', N'assets/img/catalog/book1.jpg', N'linear-gradient(135deg,#16a34a,#0f766e)', N'书', N'平装,精装,资料包', N'单套,小组套装,班级套装', N'OFF_SALE', N'PENDING', N'等待管理员审核商品信息。'),
        (N'课程教育', @book_merchant, N'数据库课程练习资料', N'SQL Server 表设计与查询练习。', N'数据库课程练习资料用于展示商品审核驳回和修改再提交流程。', 49.00, 99.00, 4.2, 120, 60, N'课程', N'assets/img/product-notebook.svg', N'linear-gradient(135deg,#0ea5e9,#6366f1)', N'课', N'基础版,进阶版,项目版', N'个人版,小组版,班级版', N'OFF_SALE', N'REJECTED', N'商品详情缺少交付说明，请补充后重新提交。')
    ) AS v(category_name, merchant_id, name, short_desc, detail_desc, price, old_price, rating, sales, stock, tag, image_url, gradient, icon_text, color_options, spec_options, sale_status, audit_status, audit_opinion)
    JOIN dbo.hishopping_category c ON c.name = v.category_name
) AS source
ON target.name = source.name
WHEN MATCHED THEN UPDATE SET
    category_id = source.category_id,
    merchant_id = source.merchant_id,
    short_desc = source.short_desc,
    detail_desc = source.detail_desc,
    price = source.price,
    old_price = source.old_price,
    rating = source.rating,
    sales = source.sales,
    stock = source.stock,
    tag = source.tag,
    image_url = source.image_url,
    gradient = source.gradient,
    icon_text = source.icon_text,
    color_options = source.color_options,
    spec_options = source.spec_options,
    sale_status = source.sale_status,
    audit_status = source.audit_status,
    audit_opinion = source.audit_opinion,
    status = CASE WHEN source.sale_status = N'ON_SALE' THEN N'上架中' ELSE N'已下架' END,
    submit_time = DATEADD(DAY, -5, SYSDATETIME()),
    review_time = CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN DATEADD(DAY, -4, SYSDATETIME()) ELSE NULL END,
    review_admin_id = CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN 1 ELSE NULL END
WHEN NOT MATCHED THEN
    INSERT(category_id, merchant_id, name, short_desc, detail_desc, price, old_price, rating, sales, stock, tag, image_url, gradient, icon_text, color_options, spec_options, status, sale_status, audit_status, audit_opinion, submit_time, review_time, review_admin_id)
    VALUES(source.category_id, source.merchant_id, source.name, source.short_desc, source.detail_desc, source.price, source.old_price, source.rating, source.sales, source.stock, source.tag, source.image_url, source.gradient, source.icon_text, source.color_options, source.spec_options, CASE WHEN source.sale_status = N'ON_SALE' THEN N'上架中' ELSE N'已下架' END, source.sale_status, source.audit_status, source.audit_opinion, DATEADD(DAY, -5, SYSDATETIME()), CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN DATEADD(DAY, -4, SYSDATETIME()) ELSE NULL END, CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN 1 ELSE NULL END);
GO

DECLARE @life_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001');
DECLARE @digital_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801002');
DECLARE @book_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801003');
DECLARE @outdoor_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801004');

MERGE dbo.hishopping_product AS target
USING (
    SELECT c.id AS category_id, v.*
    FROM (VALUES
        (N'酒水饮料', @life_merchant, N'冷萃咖啡随享装', N'低糖冷萃，适合通勤和办公室。', N'冷萃咖啡随享装用于展示饮料类商品，多口味多包装 SKU 可在详情页切换。', 39.90, 59.90, 4.7, 860, 160, N'咖啡', NULL, N'linear-gradient(135deg,#78350f,#f59e0b)', N'冷', N'原味,拿铁', N'6瓶装,12瓶装', N'[{"name":"口味","values":["原味","拿铁"]},{"name":"规格","values":["6瓶装","12瓶装"]}]', N'[{"skuId":"coffee-original-6","values":["原味","6瓶装"],"color":"原味","spec":"6瓶装","price":39.90,"oldPrice":59.90,"stock":60,"enabled":true,"skuCode":"SHOW-COF-001"},{"skuId":"coffee-original-12","values":["原味","12瓶装"],"color":"原味","spec":"12瓶装","price":69.90,"oldPrice":89.90,"stock":45,"enabled":true,"skuCode":"SHOW-COF-002"},{"skuId":"coffee-latte-6","values":["拿铁","6瓶装"],"color":"拿铁","spec":"6瓶装","price":42.90,"oldPrice":62.90,"stock":35,"enabled":true,"skuCode":"SHOW-COF-003"},{"skuId":"coffee-latte-12","values":["拿铁","12瓶装"],"color":"拿铁","spec":"12瓶装","price":76.90,"oldPrice":98.90,"stock":20,"enabled":true,"skuCode":"SHOW-COF-004"}]', N'[{"name":"保质期","value":"180天"},{"name":"储存方式","value":"常温避光"}]', N'ON_SALE', N'APPROVED', NULL),
        (N'厨具餐具', @life_merchant, N'陶瓷便当餐具套装', N'碗盘杯筷组合，适合宿舍和办公室。', N'陶瓷便当餐具套装用于展示厨具餐具类商品，包含颜色和件数组合。', 79.00, 109.00, 4.6, 420, 100, N'餐具', NULL, N'linear-gradient(135deg,#0f766e,#f8fafc)', N'陶', N'月白,雾蓝', N'四件套,六件套', N'[{"name":"颜色","values":["月白","雾蓝"]},{"name":"套装","values":["四件套","六件套"]}]', N'[{"skuId":"tableware-white-4","values":["月白","四件套"],"color":"月白","spec":"四件套","price":79.00,"oldPrice":109.00,"stock":35,"enabled":true,"skuCode":"SHOW-TAB-001"},{"skuId":"tableware-white-6","values":["月白","六件套"],"color":"月白","spec":"六件套","price":99.00,"oldPrice":129.00,"stock":28,"enabled":true,"skuCode":"SHOW-TAB-002"},{"skuId":"tableware-blue-4","values":["雾蓝","四件套"],"color":"雾蓝","spec":"四件套","price":79.00,"oldPrice":109.00,"stock":25,"enabled":true,"skuCode":"SHOW-TAB-003"},{"skuId":"tableware-blue-6","values":["雾蓝","六件套"],"color":"雾蓝","spec":"六件套","price":99.00,"oldPrice":129.00,"stock":12,"enabled":true,"skuCode":"SHOW-TAB-004"}]', N'[{"name":"材质","value":"陶瓷"},{"name":"适用场景","value":"宿舍/办公室"}]', N'ON_SALE', N'APPROVED', NULL),
        (N'清洁纸品', @life_merchant, N'抽纸湿巾家庭囤货箱', N'抽纸湿巾组合，家庭日用高频复购。', N'抽纸湿巾家庭囤货箱用于展示家清纸品类商品和大包装 SKU。', 65.00, 89.00, 4.8, 1320, 240, N'纸品', NULL, N'linear-gradient(135deg,#22c55e,#bae6fd)', N'抽', N'原木,柔润', N'小箱,大箱', N'[{"name":"类型","values":["原木","柔润"]},{"name":"规格","values":["小箱","大箱"]}]', N'[{"skuId":"paper-wood-small","values":["原木","小箱"],"color":"原木","spec":"小箱","price":65.00,"oldPrice":89.00,"stock":80,"enabled":true,"skuCode":"SHOW-PAP-001"},{"skuId":"paper-wood-large","values":["原木","大箱"],"color":"原木","spec":"大箱","price":119.00,"oldPrice":159.00,"stock":60,"enabled":true,"skuCode":"SHOW-PAP-002"},{"skuId":"paper-soft-small","values":["柔润","小箱"],"color":"柔润","spec":"小箱","price":69.00,"oldPrice":92.00,"stock":55,"enabled":true,"skuCode":"SHOW-PAP-003"},{"skuId":"paper-soft-large","values":["柔润","大箱"],"color":"柔润","spec":"大箱","price":126.00,"oldPrice":168.00,"stock":45,"enabled":true,"skuCode":"SHOW-PAP-004"}]', N'[{"name":"适用人群","value":"家庭日用"},{"name":"发货方式","value":"整箱发货"}]', N'ON_SALE', N'APPROVED', NULL),
        (N'手机数码', @digital_merchant, N'磁吸快充移动电源', N'小巧便携，支持磁吸快充。', N'磁吸快充移动电源用于展示数码配件、容量和颜色组合 SKU。', 129.00, 169.00, 4.7, 740, 130, N'快充', NULL, N'linear-gradient(135deg,#111827,#22d3ee)', N'磁', N'黑色,白色', N'5000mAh,10000mAh', N'[{"name":"颜色","values":["黑色","白色"]},{"name":"容量","values":["5000mAh","10000mAh"]}]', N'[{"skuId":"power-black-5k","values":["黑色","5000mAh"],"color":"黑色","spec":"5000mAh","price":129.00,"oldPrice":169.00,"stock":40,"enabled":true,"skuCode":"SHOW-POW-001"},{"skuId":"power-black-10k","values":["黑色","10000mAh"],"color":"黑色","spec":"10000mAh","price":169.00,"oldPrice":219.00,"stock":35,"enabled":true,"skuCode":"SHOW-POW-002"},{"skuId":"power-white-5k","values":["白色","5000mAh"],"color":"白色","spec":"5000mAh","price":129.00,"oldPrice":169.00,"stock":30,"enabled":true,"skuCode":"SHOW-POW-003"},{"skuId":"power-white-10k","values":["白色","10000mAh"],"color":"白色","spec":"10000mAh","price":169.00,"oldPrice":219.00,"stock":25,"enabled":true,"skuCode":"SHOW-POW-004"}]', N'[{"name":"接口","value":"Type-C"},{"name":"保修","value":"一年"}]', N'ON_SALE', N'APPROVED', NULL),
        (N'电脑办公', @digital_merchant, N'无线静音办公键鼠', N'低噪按键，长续航办公套装。', N'无线静音办公键鼠用于展示办公外设类商品，支持颜色和套装 SKU。', 89.00, 129.00, 4.5, 510, 110, N'办公', NULL, N'linear-gradient(135deg,#475569,#cbd5e1)', N'无', N'黑色,粉色', N'键鼠套装,键盘单品', N'[{"name":"颜色","values":["黑色","粉色"]},{"name":"规格","values":["键鼠套装","键盘单品"]}]', N'[{"skuId":"keyboard-black-set","values":["黑色","键鼠套装"],"color":"黑色","spec":"键鼠套装","price":89.00,"oldPrice":129.00,"stock":36,"enabled":true,"skuCode":"SHOW-KEY-001"},{"skuId":"keyboard-black-only","values":["黑色","键盘单品"],"color":"黑色","spec":"键盘单品","price":69.00,"oldPrice":99.00,"stock":28,"enabled":true,"skuCode":"SHOW-KEY-002"},{"skuId":"keyboard-pink-set","values":["粉色","键鼠套装"],"color":"粉色","spec":"键鼠套装","price":92.00,"oldPrice":132.00,"stock":22,"enabled":true,"skuCode":"SHOW-KEY-003"},{"skuId":"keyboard-pink-only","values":["粉色","键盘单品"],"color":"粉色","spec":"键盘单品","price":72.00,"oldPrice":102.00,"stock":18,"enabled":true,"skuCode":"SHOW-KEY-004"}]', N'[{"name":"连接方式","value":"2.4G无线"},{"name":"适用系统","value":"Windows/macOS"}]', N'ON_SALE', N'APPROVED', NULL),
        (N'家用电器', @digital_merchant, N'桌面空气循环扇', N'小体积大风量，宿舍办公均适用。', N'桌面空气循环扇用于展示小家电类商品，多档位多颜色 SKU。', 159.00, 219.00, 4.6, 360, 90, N'小家电', NULL, N'linear-gradient(135deg,#06b6d4,#64748b)', N'桌', N'奶白,深灰', N'基础款,遥控款', N'[{"name":"颜色","values":["奶白","深灰"]},{"name":"款式","values":["基础款","遥控款"]}]', N'[{"skuId":"fan-white-basic","values":["奶白","基础款"],"color":"奶白","spec":"基础款","price":159.00,"oldPrice":219.00,"stock":24,"enabled":true,"skuCode":"SHOW-FAN-001"},{"skuId":"fan-white-remote","values":["奶白","遥控款"],"color":"奶白","spec":"遥控款","price":189.00,"oldPrice":249.00,"stock":18,"enabled":true,"skuCode":"SHOW-FAN-002"},{"skuId":"fan-gray-basic","values":["深灰","基础款"],"color":"深灰","spec":"基础款","price":159.00,"oldPrice":219.00,"stock":21,"enabled":true,"skuCode":"SHOW-FAN-003"},{"skuId":"fan-gray-remote","values":["深灰","遥控款"],"color":"深灰","spec":"遥控款","price":189.00,"oldPrice":249.00,"stock":16,"enabled":true,"skuCode":"SHOW-FAN-004"}]', N'[{"name":"档位","value":"三档调节"},{"name":"噪音","value":"低噪运行"}]', N'ON_SALE', N'APPROVED', NULL),
        (N'图书文娱', @book_merchant, N'经典文学阅读计划', N'精选文学作品，适合阅读打卡。', N'经典文学阅读计划用于展示图书类商品和待审核商家商品。', 68.00, 98.00, 4.7, 330, 70, N'文学', NULL, N'linear-gradient(135deg,#92400e,#facc15)', N'经', N'平装,精装', N'单套,双套', N'[{"name":"装帧","values":["平装","精装"]},{"name":"规格","values":["单套","双套"]}]', N'[{"skuId":"literature-paper-one","values":["平装","单套"],"color":"平装","spec":"单套","price":68.00,"oldPrice":98.00,"stock":30,"enabled":true,"skuCode":"SHOW-LIT-001"},{"skuId":"literature-paper-two","values":["平装","双套"],"color":"平装","spec":"双套","price":126.00,"oldPrice":178.00,"stock":22,"enabled":true,"skuCode":"SHOW-LIT-002"},{"skuId":"literature-hard-one","values":["精装","单套"],"color":"精装","spec":"单套","price":98.00,"oldPrice":138.00,"stock":18,"enabled":true,"skuCode":"SHOW-LIT-003"},{"skuId":"literature-hard-two","values":["精装","双套"],"color":"精装","spec":"双套","price":186.00,"oldPrice":258.00,"stock":10,"enabled":true,"skuCode":"SHOW-LIT-004"}]', N'[{"name":"适用","value":"课外阅读"},{"name":"包装","value":"纸箱保护"}]', N'OFF_SALE', N'PENDING', N'待管理员审核图书套装说明。'),
        (N'课程教育', @book_merchant, N'前端基础练习册', N'HTML、CSS、JavaScript 基础练习。', N'前端基础练习册用于展示课程教育类商品，适合搜索和类目筛选。', 45.00, 79.00, 4.4, 180, 65, N'练习', NULL, N'linear-gradient(135deg,#2563eb,#14b8a6)', N'前', N'纸质版,电子版', N'基础版,进阶版', N'[{"name":"形式","values":["纸质版","电子版"]},{"name":"版本","values":["基础版","进阶版"]}]', N'[{"skuId":"frontend-paper-basic","values":["纸质版","基础版"],"color":"纸质版","spec":"基础版","price":45.00,"oldPrice":79.00,"stock":25,"enabled":true,"skuCode":"SHOW-FE-001"},{"skuId":"frontend-paper-plus","values":["纸质版","进阶版"],"color":"纸质版","spec":"进阶版","price":69.00,"oldPrice":99.00,"stock":20,"enabled":true,"skuCode":"SHOW-FE-002"},{"skuId":"frontend-digital-basic","values":["电子版","基础版"],"color":"电子版","spec":"基础版","price":29.00,"oldPrice":49.00,"stock":999,"enabled":true,"skuCode":"SHOW-FE-003"},{"skuId":"frontend-digital-plus","values":["电子版","进阶版"],"color":"电子版","spec":"进阶版","price":49.00,"oldPrice":79.00,"stock":999,"enabled":true,"skuCode":"SHOW-FE-004"}]', N'[{"name":"交付","value":"纸质/电子"},{"name":"适用阶段","value":"入门练习"}]', N'OFF_SALE', N'PENDING', N'待管理员确认电子资料交付说明。'),
        (N'数字内容', @book_merchant, N'学习资料电子包', N'电子资料，即买即用。', N'学习资料电子包用于展示数字内容类商品和非实物商品说明。', 19.90, 39.90, 4.3, 260, 999, N'电子', NULL, N'linear-gradient(135deg,#6366f1,#0ea5e9)', N'学', N'基础包,专题包', N'月度版,年度版', N'[{"name":"内容","values":["基础包","专题包"]},{"name":"周期","values":["月度版","年度版"]}]', N'[{"skuId":"material-basic-month","values":["基础包","月度版"],"color":"基础包","spec":"月度版","price":19.90,"oldPrice":39.90,"stock":999,"enabled":true,"skuCode":"SHOW-MAT-001"},{"skuId":"material-basic-year","values":["基础包","年度版"],"color":"基础包","spec":"年度版","price":99.00,"oldPrice":159.00,"stock":999,"enabled":true,"skuCode":"SHOW-MAT-002"},{"skuId":"material-topic-month","values":["专题包","月度版"],"color":"专题包","spec":"月度版","price":29.90,"oldPrice":49.90,"stock":999,"enabled":true,"skuCode":"SHOW-MAT-003"},{"skuId":"material-topic-year","values":["专题包","年度版"],"color":"专题包","spec":"年度版","price":139.00,"oldPrice":199.00,"stock":999,"enabled":true,"skuCode":"SHOW-MAT-004"}]', N'[{"name":"商品类型","value":"虚拟内容"},{"name":"交付方式","value":"订单完成后查看"}]', N'OFF_SALE', N'REJECTED', N'虚拟商品交付规则不够清晰，请补充。'),
        (N'运动户外', @outdoor_merchant, N'轻量徒步双肩包', N'日常通勤和短途徒步均适用。', N'轻量徒步双肩包用于展示户外类商品和驳回商家商品。', 139.00, 199.00, 4.5, 290, 80, N'户外', NULL, N'linear-gradient(135deg,#15803d,#84cc16)', N'轻', N'墨绿,黑色', N'18L,25L', N'[{"name":"颜色","values":["墨绿","黑色"]},{"name":"容量","values":["18L","25L"]}]', N'[{"skuId":"bag-green-18","values":["墨绿","18L"],"color":"墨绿","spec":"18L","price":139.00,"oldPrice":199.00,"stock":24,"enabled":true,"skuCode":"SHOW-BAG-001"},{"skuId":"bag-green-25","values":["墨绿","25L"],"color":"墨绿","spec":"25L","price":169.00,"oldPrice":229.00,"stock":18,"enabled":true,"skuCode":"SHOW-BAG-002"},{"skuId":"bag-black-18","values":["黑色","18L"],"color":"黑色","spec":"18L","price":139.00,"oldPrice":199.00,"stock":20,"enabled":true,"skuCode":"SHOW-BAG-003"},{"skuId":"bag-black-25","values":["黑色","25L"],"color":"黑色","spec":"25L","price":169.00,"oldPrice":229.00,"stock":15,"enabled":true,"skuCode":"SHOW-BAG-004"}]', N'[{"name":"面料","value":"防泼水尼龙"},{"name":"适用","value":"通勤/短途徒步"}]', N'OFF_SALE', N'REJECTED', N'商家入驻未通过，商品暂不可售。'),
        (N'运动户外', @outdoor_merchant, N'速干运动短袖', N'透气速干，适合训练和户外。', N'速干运动短袖用于展示服饰尺码类 SKU。', 59.00, 89.00, 4.4, 420, 140, N'速干', NULL, N'linear-gradient(135deg,#ef4444,#0f172a)', N'速', N'白色,黑色', N'M,L', N'[{"name":"颜色","values":["白色","黑色"]},{"name":"尺码","values":["M","L"]}]', N'[{"skuId":"shirt-white-m","values":["白色","M"],"color":"白色","spec":"M","price":59.00,"oldPrice":89.00,"stock":40,"enabled":true,"skuCode":"SHOW-SHI-001"},{"skuId":"shirt-white-l","values":["白色","L"],"color":"白色","spec":"L","price":59.00,"oldPrice":89.00,"stock":38,"enabled":true,"skuCode":"SHOW-SHI-002"},{"skuId":"shirt-black-m","values":["黑色","M"],"color":"黑色","spec":"M","price":59.00,"oldPrice":89.00,"stock":35,"enabled":true,"skuCode":"SHOW-SHI-003"},{"skuId":"shirt-black-l","values":["黑色","L"],"color":"黑色","spec":"L","price":59.00,"oldPrice":89.00,"stock":27,"enabled":true,"skuCode":"SHOW-SHI-004"}]', N'[{"name":"材质","value":"聚酯纤维"},{"name":"洗护","value":"冷水机洗"}]', N'OFF_SALE', N'REJECTED', N'商家入驻未通过，商品暂不可售。'),
        (N'运动户外', @outdoor_merchant, N'折叠露营椅', N'轻便折叠，车载露营好收纳。', N'折叠露营椅用于展示户外装备类商品和库存 SKU。', 99.00, 149.00, 4.5, 210, 75, N'露营', NULL, N'linear-gradient(135deg,#a16207,#65a30d)', N'折', N'卡其,军绿', N'普通款,加宽款', N'[{"name":"颜色","values":["卡其","军绿"]},{"name":"款式","values":["普通款","加宽款"]}]', N'[{"skuId":"chair-khaki-normal","values":["卡其","普通款"],"color":"卡其","spec":"普通款","price":99.00,"oldPrice":149.00,"stock":22,"enabled":true,"skuCode":"SHOW-CHA-001"},{"skuId":"chair-khaki-wide","values":["卡其","加宽款"],"color":"卡其","spec":"加宽款","price":129.00,"oldPrice":179.00,"stock":16,"enabled":true,"skuCode":"SHOW-CHA-002"},{"skuId":"chair-green-normal","values":["军绿","普通款"],"color":"军绿","spec":"普通款","price":99.00,"oldPrice":149.00,"stock":20,"enabled":true,"skuCode":"SHOW-CHA-003"},{"skuId":"chair-green-wide","values":["军绿","加宽款"],"color":"军绿","spec":"加宽款","price":129.00,"oldPrice":179.00,"stock":17,"enabled":true,"skuCode":"SHOW-CHA-004"}]', N'[{"name":"承重","value":"约120kg"},{"name":"收纳","value":"附收纳袋"}]', N'OFF_SALE', N'REJECTED', N'商家入驻未通过，商品暂不可售。')
    ) AS v(category_name, merchant_id, name, short_desc, detail_desc, price, old_price, rating, sales, stock, tag, image_url, gradient, icon_text, color_options, spec_options, sku_attrs, sku_options, product_attrs, sale_status, audit_status, audit_opinion)
    JOIN dbo.hishopping_category c ON c.name = v.category_name
) AS source
ON target.name = source.name
WHEN MATCHED THEN UPDATE SET
    category_id = source.category_id,
    merchant_id = source.merchant_id,
    short_desc = source.short_desc,
    detail_desc = source.detail_desc,
    price = source.price,
    old_price = source.old_price,
    rating = source.rating,
    sales = source.sales,
    stock = source.stock,
    tag = source.tag,
    image_url = source.image_url,
    gradient = source.gradient,
    icon_text = source.icon_text,
    color_options = source.color_options,
    spec_options = source.spec_options,
    sku_attrs = source.sku_attrs,
    sku_options = source.sku_options,
    product_attrs = source.product_attrs,
    sale_status = source.sale_status,
    audit_status = source.audit_status,
    audit_opinion = source.audit_opinion,
    status = CASE WHEN source.sale_status = N'ON_SALE' THEN N'上架中' ELSE N'已下架' END,
    submit_time = DATEADD(DAY, -3, SYSDATETIME()),
    review_time = CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN DATEADD(DAY, -2, SYSDATETIME()) ELSE NULL END,
    review_admin_id = CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN 1 ELSE NULL END
WHEN NOT MATCHED THEN
    INSERT(category_id, merchant_id, name, short_desc, detail_desc, price, old_price, rating, sales, stock, tag, image_url, gradient, icon_text, color_options, spec_options, sku_attrs, sku_options, product_attrs, status, sale_status, audit_status, audit_opinion, submit_time, review_time, review_admin_id)
    VALUES(source.category_id, source.merchant_id, source.name, source.short_desc, source.detail_desc, source.price, source.old_price, source.rating, source.sales, source.stock, source.tag, source.image_url, source.gradient, source.icon_text, source.color_options, source.spec_options, source.sku_attrs, source.sku_options, source.product_attrs, CASE WHEN source.sale_status = N'ON_SALE' THEN N'上架中' ELSE N'已下架' END, source.sale_status, source.audit_status, source.audit_opinion, DATEADD(DAY, -3, SYSDATETIME()), CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN DATEADD(DAY, -2, SYSDATETIME()) ELSE NULL END, CASE WHEN source.audit_status IN (N'APPROVED', N'REJECTED') THEN 1 ELSE NULL END);
GO

INSERT INTO dbo.hishopping_product_media(product_id, media_type, media_url, sort_no, cover_flag)
SELECT p.id, N'IMAGE', p.image_url, 1, 1
FROM dbo.hishopping_product p
WHERE p.name IN (N'有机早餐牛奶组合', N'家庭清洁护理套装', N'便携降噪蓝牙耳机', N'轻薄办公笔记本 14英寸', N'项目实践图书套装', N'数据库课程练习资料')
  AND p.image_url IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM dbo.hishopping_product_media m WHERE m.product_id = p.id AND m.media_url = p.image_url);

MERGE dbo.hishopping_hall_banner AS target
USING (VALUES
    (N'assets/img/feed-featured.png', N'暑期精选好物', N'数码、食品、图书一站式备齐', 1, N'PRODUCT', N'便携降噪蓝牙耳机', N'LEFT'),
    (N'assets/img/feed-hot.png', N'会员专享优惠', N'高等级会员可叠加平台券和店铺券', 2, N'COUPON', N'VIP', N'RIGHT'),
    (N'assets/img/feed-discover.png', N'店铺上新审核', N'后台可查看商家与商品审核状态', 3, N'MERCHANT', N'8801003', N'LEFT')
) AS source(media_url, title, subtitle, sort_no, link_type, link_target, text_position)
ON target.title = source.title
WHEN MATCHED THEN UPDATE SET
    media_type = N'IMAGE',
    media_url = source.media_url,
    subtitle = source.subtitle,
    enabled = 1,
    sort_no = source.sort_no,
    link_enabled = 1,
    link_type = source.link_type,
    link_target = source.link_target,
    overlay_enabled = 1,
    text_position = source.text_position,
    update_time = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT(media_type, media_url, title, subtitle, enabled, sort_no, link_enabled, link_type, link_target, overlay_enabled, text_position)
    VALUES(N'IMAGE', source.media_url, source.title, source.subtitle, 1, source.sort_no, 1, source.link_type, source.link_target, 1, source.text_position);
GO

DECLARE @lin INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'linxiaoyu@hishopping.com');
DECLARE @chen INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'chenyuanhang@hishopping.com');
DECLARE @zhou INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'zhounian@hishopping.com');
DECLARE @gu INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'guxinghe@hishopping.com');

MERGE dbo.hishopping_address AS target
USING (VALUES
    (@lin, N'林晓雨', N'13890010001', N'江苏省', N'南京市', N'雨花台区', N'软件大道88号A座1206室', 1),
    (@lin, N'林晓雨', N'13890010001', N'江苏省', N'南京市', N'建邺区', N'云锦路18号3栋502室', 0),
    (@chen, N'陈远航', N'13890010002', N'江苏省', N'南京市', N'鼓楼区', N'中山北路66号8楼', 1),
    (@zhou, N'周念', N'13890010003', N'江苏省', N'南京市', N'玄武区', N'珠江路9号创业园2楼', 1),
    (@gu, N'顾星河', N'13890010004', N'江苏省', N'南京市', N'秦淮区', N'中华路100号2单元601室', 1)
) AS source(user_id, receiver_name, phone, province, city, district, detail, is_default)
ON target.user_id = source.user_id AND target.receiver_name = source.receiver_name AND target.detail = source.detail
WHEN MATCHED THEN UPDATE SET phone = source.phone, province = source.province, city = source.city, district = source.district, is_default = source.is_default
WHEN NOT MATCHED THEN
    INSERT(user_id, receiver_name, phone, province, city, district, detail, is_default)
    VALUES(source.user_id, source.receiver_name, source.phone, source.province, source.city, source.district, source.detail, source.is_default);

INSERT INTO dbo.hishopping_cart_item(user_id, product_id, quantity, selected_color, selected_spec, sku_id, sku_text, sku_price)
SELECT @lin, p.id, v.quantity, v.selected_color, v.selected_spec, N'DEMO-' + CONVERT(NVARCHAR(20), p.id), v.selected_color + N' / ' + v.selected_spec, p.price
FROM (VALUES
    (N'便携降噪蓝牙耳机', 1, N'云白', N'降噪版'),
    (N'有机早餐牛奶组合', 2, N'有机', N'24盒'),
    (N'家庭清洁护理套装', 1, N'柠檬', N'家庭装')
) AS v(product_name, quantity, selected_color, selected_spec)
JOIN dbo.hishopping_product p ON p.name = v.product_name
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_cart_item c WHERE c.user_id = @lin AND c.product_id = p.id);

INSERT INTO dbo.hishopping_favorite(user_id, product_id)
SELECT v.user_id, p.id
FROM (VALUES
    (@lin, N'便携降噪蓝牙耳机'),
    (@lin, N'轻薄办公笔记本 14英寸'),
    (@chen, N'有机早餐牛奶组合'),
    (@zhou, N'项目实践图书套装')
) AS v(user_id, product_name)
JOIN dbo.hishopping_product p ON p.name = v.product_name
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_favorite f WHERE f.user_id = v.user_id AND f.product_id = p.id);
GO

DECLARE @life_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001');
DECLARE @digital_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801002');

MERGE dbo.hishop_coupon_template AS target
USING (VALUES
    (N'展示新人满减券', N'NEW_USER', 12.00, 1.00, 69.00, N'NEW_USER', N'VIP1', 1, 500, 1, 15, 1, 0, N'PLATFORM', NULL, 0, N'ALL', N'新用户首单演示券', N'满69减12', N'适合展示新用户领券和下单抵扣。', N'ENABLED'),
    (N'展示会员折扣券', N'DISCOUNT', 0.00, 0.92, 199.00, N'VIP_LEVEL', N'7', 7, 300, 1, 30, 0, 1, N'PLATFORM', NULL, 1, N'ALL', N'VIP专享折扣', N'满199享9.2折', N'适合展示高等级会员权益和可叠加券。', N'ENABLED'),
    (N'展示平台通用券', N'AMOUNT', 20.00, 1.00, 159.00, N'ALL', N'ALL', 1, 800, 2, 30, 0, 0, N'PLATFORM', NULL, 0, N'ALL', N'平台通用满减', N'满159减20', N'适合购物车结算演示。', N'ENABLED'),
    (N'晨光店铺满减券', N'AMOUNT', 10.00, 1.00, 99.00, N'ALL', N'ALL', 1, 300, 2, 20, 0, 0, N'MERCHANT', @life_merchant, 0, N'MERCHANT', N'晨光生活专属', N'满99减10', N'店铺专属券，用于展示商家优惠券。', N'ENABLED'),
    (N'星河数码折扣券', N'DISCOUNT', 0.00, 0.95, 299.00, N'ALL', N'ALL', 1, 200, 1, 20, 0, 0, N'MERCHANT', @digital_merchant, 0, N'MERCHANT', N'星河数码专属', N'满299享9.5折', N'数码店铺券，用于展示商家发券。', N'ENABLED')
) AS source(coupon_name, coupon_type, amount, discount_rate, min_amount, target_type, target_value, vip_level, total_quantity, per_user_limit, valid_days, is_new_user_coupon, is_vip_coupon, coupon_owner_type, merchant_id, stackable, use_scope, home_title, home_subtitle, description, status)
ON target.coupon_name = source.coupon_name
WHEN MATCHED THEN UPDATE SET
    coupon_type = source.coupon_type,
    amount = source.amount,
    discount_rate = source.discount_rate,
    min_amount = source.min_amount,
    target_type = source.target_type,
    target_value = source.target_value,
    vip_level = source.vip_level,
    total_quantity = source.total_quantity,
    per_user_limit = source.per_user_limit,
    valid_days = source.valid_days,
    is_new_user_coupon = source.is_new_user_coupon,
    is_vip_coupon = source.is_vip_coupon,
    coupon_owner_type = source.coupon_owner_type,
    merchant_id = source.merchant_id,
    stackable = source.stackable,
    use_scope = source.use_scope,
    home_title = source.home_title,
    home_subtitle = source.home_subtitle,
    description = source.description,
    status = source.status,
    end_time = DATEADD(DAY, source.valid_days, SYSDATETIME()),
    update_time = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT(coupon_name, coupon_type, amount, discount_rate, min_amount, target_type, target_value, vip_level, total_quantity, per_user_limit, end_time, valid_days, is_new_user_coupon, is_vip_coupon, coupon_owner_type, merchant_id, stackable, use_scope, home_title, home_subtitle, description, status)
    VALUES(source.coupon_name, source.coupon_type, source.amount, source.discount_rate, source.min_amount, source.target_type, source.target_value, source.vip_level, source.total_quantity, source.per_user_limit, DATEADD(DAY, source.valid_days, SYSDATETIME()), source.valid_days, source.is_new_user_coupon, source.is_vip_coupon, source.coupon_owner_type, source.merchant_id, source.stackable, source.use_scope, source.home_title, source.home_subtitle, source.description, source.status);

DECLARE @lin INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'linxiaoyu@hishopping.com');
DECLARE @chen INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'chenyuanhang@hishopping.com');
DECLARE @zhou INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'zhounian@hishopping.com');

INSERT INTO dbo.hishop_user_coupon(coupon_id, user_id, coupon_name, coupon_type, amount, discount_rate, min_amount, vip_level, status, receive_time, expire_time, use_time, order_id, issue_batch_no, coupon_owner_type, merchant_id, stackable, use_scope, description)
SELECT t.coupon_id, v.user_id, t.coupon_name, t.coupon_type, t.amount, t.discount_rate, t.min_amount, t.vip_level, v.status, DATEADD(DAY, v.receive_offset, SYSDATETIME()), DATEADD(DAY, v.expire_offset, SYSDATETIME()), CASE WHEN v.status = N'USED' THEN DATEADD(DAY, -1, SYSDATETIME()) ELSE NULL END, NULL, v.batch_no, t.coupon_owner_type, t.merchant_id, t.stackable, t.use_scope, t.description
FROM (VALUES
    (@lin, N'展示平台通用券', N'UNUSED', -2, 28, N'SHOW-COUPON-001'),
    (@lin, N'展示会员折扣券', N'UNUSED', -1, 29, N'SHOW-COUPON-002'),
    (@lin, N'晨光店铺满减券', N'UNUSED', -1, 19, N'SHOW-COUPON-003'),
    (@chen, N'展示新人满减券', N'USED', -6, 9, N'SHOW-COUPON-004'),
    (@zhou, N'星河数码折扣券', N'EXPIRED', -40, -2, N'SHOW-COUPON-005')
) AS v(user_id, coupon_name, status, receive_offset, expire_offset, batch_no)
JOIN dbo.hishop_coupon_template t ON t.coupon_name = v.coupon_name
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.hishop_user_coupon uc
    WHERE uc.user_id = v.user_id AND uc.coupon_id = t.coupon_id AND uc.issue_batch_no = v.batch_no
);

INSERT INTO dbo.hishop_coupon_issue_log(coupon_id, issue_batch_no, issue_type, target_value, issue_count, skip_count, admin_id, issue_time, remark)
SELECT t.coupon_id, N'SHOW-ISSUE-' + RIGHT(N'000' + CONVERT(NVARCHAR(20), ROW_NUMBER() OVER (ORDER BY t.coupon_id)), 3), N'DEMO', N'展示账号', 3, 0, 1, DATEADD(DAY, -2, SYSDATETIME()), N'课程展示预置发券记录'
FROM dbo.hishop_coupon_template t
WHERE t.coupon_name IN (N'展示新人满减券', N'展示会员折扣券', N'展示平台通用券', N'晨光店铺满减券', N'星河数码折扣券')
  AND NOT EXISTS (SELECT 1 FROM dbo.hishop_coupon_issue_log l WHERE l.coupon_id = t.coupon_id AND l.remark = N'课程展示预置发券记录');
GO

DECLARE @lin INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'linxiaoyu@hishopping.com');
DECLARE @chen INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'chenyuanhang@hishopping.com');
DECLARE @zhou INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'zhounian@hishopping.com');
DECLARE @life_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001');
DECLARE @digital_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801002');

MERGE dbo.hishopping_order AS target
USING (VALUES
    (N'SHOW202607090001', N'SHOWBATCH2026070901', @lin, @digital_merchant, N'星河数码旗舰店', 219.00, 199.00, 20.00, N'展示平台通用券', N'待付款', N'林晓雨', N'13890010001', N'江苏省南京市雨花台区软件大道88号A座1206室', -2, 0),
    (N'SHOW202607090002', N'SHOWBATCH2026070902', @lin, @life_merchant, N'晨光优选生活馆', 345.00, 315.00, 30.00, N'晨光店铺满减券', N'待发货', N'林晓雨', N'13890010001', N'江苏省南京市雨花台区软件大道88号A座1206室', -4, 0),
    (N'SHOW202607090003', N'SHOWBATCH2026070903', @zhou, @digital_merchant, N'星河数码旗舰店', 3299.00, 3134.05, 164.95, N'星河数码折扣券', N'已发货', N'周念', N'13890010003', N'江苏省南京市玄武区珠江路9号创业园2楼', -6, 0),
    (N'SHOW202607090004', N'SHOWBATCH2026070904', @chen, @life_merchant, N'晨光优选生活馆', 128.00, 116.00, 12.00, N'展示新人满减券', N'已完成', N'陈远航', N'13890010002', N'江苏省南京市鼓楼区中山北路66号8楼', -12, 1),
    (N'SHOW202607090005', N'SHOWBATCH2026070905', @lin, @digital_merchant, N'星河数码旗舰店', 219.00, 219.00, 0.00, NULL, N'售后中', N'林晓雨', N'13890010001', N'江苏省南京市雨花台区软件大道88号A座1206室', -10, 1),
    (N'SHOW202607090006', N'SHOWBATCH2026070906', @chen, @life_merchant, N'晨光优选生活馆', 89.00, 89.00, 0.00, NULL, N'已取消', N'陈远航', N'13890010002', N'江苏省南京市鼓楼区中山北路66号8楼', -8, 0)
) AS source(order_no, batch_no, user_id, merchant_id, shop_name, goods_amount, total_amount, discount_amount, coupon_title, status, receiver_name, receiver_phone, receiver_address, create_offset, growth_awarded)
ON target.order_no = source.order_no
WHEN MATCHED THEN UPDATE SET
    batch_no = source.batch_no,
    user_id = source.user_id,
    merchant_id = source.merchant_id,
    shop_name = source.shop_name,
    goods_amount = source.goods_amount,
    total_amount = source.total_amount,
    discount_amount = source.discount_amount,
    coupon_title = source.coupon_title,
    status = source.status,
    receiver_name = source.receiver_name,
    receiver_phone = source.receiver_phone,
    receiver_address = source.receiver_address,
    growth_awarded = source.growth_awarded
WHEN NOT MATCHED THEN
    INSERT(order_no, batch_no, user_id, merchant_id, shop_name, goods_amount, total_amount, discount_amount, coupon_title, status, receiver_name, receiver_phone, receiver_address, create_time, growth_awarded)
    VALUES(source.order_no, source.batch_no, source.user_id, source.merchant_id, source.shop_name, source.goods_amount, source.total_amount, source.discount_amount, source.coupon_title, source.status, source.receiver_name, source.receiver_phone, source.receiver_address, DATEADD(DAY, source.create_offset, SYSDATETIME()), source.growth_awarded);

INSERT INTO dbo.hishopping_order_item(order_id, product_id, quantity, price, selected_color, selected_spec, sku_id, sku_text, snapshot_name, snapshot_image, item_unit_price, item_subtotal)
SELECT o.id, p.id, v.quantity, v.price, v.selected_color, v.selected_spec, N'SHOW-SKU-' + CONVERT(NVARCHAR(20), p.id), v.selected_color + N' / ' + v.selected_spec, p.name, p.image_url, v.price, v.price * v.quantity
FROM (VALUES
    (N'SHOW202607090001', N'便携降噪蓝牙耳机', 1, 219.00, N'云白', N'降噪版'),
    (N'SHOW202607090002', N'有机早餐牛奶组合', 2, 128.00, N'有机', N'24盒'),
    (N'SHOW202607090002', N'家庭清洁护理套装', 1, 89.00, N'柠檬', N'家庭装'),
    (N'SHOW202607090003', N'轻薄办公笔记本 14英寸', 1, 3299.00, N'银色', N'16G+512G'),
    (N'SHOW202607090004', N'有机早餐牛奶组合', 1, 128.00, N'低脂', N'24盒'),
    (N'SHOW202607090005', N'便携降噪蓝牙耳机', 1, 219.00, N'曜黑', N'游戏版'),
    (N'SHOW202607090006', N'家庭清洁护理套装', 1, 89.00, N'清香', N'基础装')
) AS v(order_no, product_name, quantity, price, selected_color, selected_spec)
JOIN dbo.hishopping_order o ON o.order_no = v.order_no
JOIN dbo.hishopping_product p ON p.name = v.product_name
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_order_item oi WHERE oi.order_id = o.id AND oi.product_id = p.id);

INSERT INTO dbo.hishop_order_shipment(order_id, merchant_id, express_company, tracking_no, ship_time)
SELECT o.id, o.merchant_id, v.express_company, v.tracking_no, DATEADD(DAY, v.ship_offset, SYSDATETIME())
FROM (VALUES
    (N'SHOW202607090003', N'顺丰速运', N'SF8801003001', -4),
    (N'SHOW202607090004', N'京东快递', N'JD8801004001', -10)
) AS v(order_no, express_company, tracking_no, ship_offset)
JOIN dbo.hishopping_order o ON o.order_no = v.order_no
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishop_order_shipment s WHERE s.order_id = o.id AND s.merchant_id = o.merchant_id);
GO

DECLARE @lin INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'linxiaoyu@hishopping.com');
DECLARE @chen INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'chenyuanhang@hishopping.com');
DECLARE @zhou INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'zhounian@hishopping.com');

INSERT INTO dbo.hishop_product_review(order_id, order_item_id, product_id, user_id, rating, content, anonymous_flag, status, like_count, create_time, update_time)
SELECT o.id, oi.id, p.id, v.user_id, v.rating, v.content, v.anonymous_flag, N'ACTIVE', v.like_count, DATEADD(DAY, v.create_offset, SYSDATETIME()), DATEADD(DAY, v.create_offset, SYSDATETIME())
FROM (VALUES
    (N'SHOW202607090004', N'有机早餐牛奶组合', @chen, 5, N'日期新鲜，包装完整，早餐搭配很方便。', 0, 6, -9),
    (N'SHOW202607090005', N'便携降噪蓝牙耳机', @lin, 3, N'连接稳定，但右耳偶尔有电流声，已申请售后。', 1, 2, -8),
    (N'SHOW202607090003', N'轻薄办公笔记本 14英寸', @zhou, 5, N'开机速度快，重量比预期轻，适合上课和办公。', 0, 9, -3)
) AS v(order_no, product_name, user_id, rating, content, anonymous_flag, like_count, create_offset)
JOIN dbo.hishopping_order o ON o.order_no = v.order_no
JOIN dbo.hishopping_product p ON p.name = v.product_name
JOIN dbo.hishopping_order_item oi ON oi.order_id = o.id AND oi.product_id = p.id
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishop_product_review r WHERE r.order_id = o.id AND r.product_id = p.id AND r.user_id = v.user_id);

INSERT INTO dbo.hishop_product_review_media(review_id, owner_type, owner_id, product_id, media_type, media_url, file_name, file_size, sort_no, status, create_time)
SELECT r.review_id, N'USER', r.user_id, r.product_id, N'IMAGE', p.image_url, N'展示评价图片.jpg', 102400, 1, N'ACTIVE', r.create_time
FROM dbo.hishop_product_review r
JOIN dbo.hishopping_product p ON p.id = r.product_id
WHERE p.name IN (N'有机早餐牛奶组合', N'便携降噪蓝牙耳机', N'轻薄办公笔记本 14英寸')
  AND NOT EXISTS (SELECT 1 FROM dbo.hishop_product_review_media m WHERE m.review_id = r.review_id AND m.media_url = p.image_url);

INSERT INTO dbo.hishopping_review_reply(review_id, user_type, user_id, user_name, content, status, create_time)
SELECT r.review_id, N'MERCHANT', p.merchant_id, m.shop_name, v.content, N'ACTIVE', DATEADD(HOUR, 6, r.create_time)
FROM dbo.hishop_product_review r
JOIN dbo.hishopping_product p ON p.id = r.product_id
JOIN dbo.hishop_merchant m ON m.merchant_id = p.merchant_id
JOIN (VALUES
    (N'有机早餐牛奶组合', N'感谢认可，我们会继续保证发货时效和商品品质。'),
    (N'便携降噪蓝牙耳机', N'抱歉带来不便，售后同学会尽快跟进换新方案。'),
    (N'轻薄办公笔记本 14英寸', N'感谢支持，后续使用中有问题可以随时联系我们。')
) AS v(product_name, content) ON v.product_name = p.name
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_review_reply rr WHERE rr.review_id = r.review_id AND rr.content = v.content);

INSERT INTO dbo.hishopping_review_like(review_id, user_type, user_id, create_time)
SELECT r.review_id, N'USER', v.user_id, DATEADD(HOUR, 1, r.create_time)
FROM dbo.hishop_product_review r
CROSS JOIN (VALUES (@lin), (@chen), (@zhou)) AS v(user_id)
WHERE v.user_id <> r.user_id
  AND NOT EXISTS (SELECT 1 FROM dbo.hishopping_review_like l WHERE l.review_id = r.review_id AND l.user_type = N'USER' AND l.user_id = v.user_id);
GO

DECLARE @lin INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'linxiaoyu@hishopping.com');
DECLARE @life_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001');
DECLARE @digital_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801002');

INSERT INTO dbo.hishop_after_sale(order_id, user_id, merchant_id, product_id, after_sale_type, reason, description, evidence_urls, refund_amount, status, apply_time, handle_opinion, admin_opinion, update_time, handle_time)
SELECT o.id, o.user_id, o.merchant_id, p.id, v.after_sale_type, v.reason, v.description, p.image_url, v.refund_amount, v.status, DATEADD(DAY, v.apply_offset, SYSDATETIME()), v.handle_opinion, v.admin_opinion, DATEADD(DAY, v.update_offset, SYSDATETIME()), CASE WHEN v.handle_opinion IS NULL THEN NULL ELSE DATEADD(DAY, v.update_offset, SYSDATETIME()) END
FROM (VALUES
    (N'SHOW202607090005', N'便携降噪蓝牙耳机', N'换货', N'耳机右耳偶尔有电流声', N'希望更换同规格新品。', 219.00, N'待商家处理', -8, -8, NULL, NULL),
    (N'SHOW202607090004', N'有机早餐牛奶组合', N'退款', N'其中一盒运输挤压变形', N'已上传外包装图片，申请部分退款。', 12.00, N'已完成', -9, -8, N'情况属实，已同意部分退款。', N'售后流程已完成。')
) AS v(order_no, product_name, after_sale_type, reason, description, refund_amount, status, apply_offset, update_offset, handle_opinion, admin_opinion)
JOIN dbo.hishopping_order o ON o.order_no = v.order_no
JOIN dbo.hishopping_product p ON p.name = v.product_name
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishop_after_sale a WHERE a.order_id = o.id AND a.product_id = p.id AND a.reason = v.reason);

DECLARE @review_id INT = (
    SELECT TOP 1 r.review_id
    FROM dbo.hishop_product_review r
    JOIN dbo.hishopping_product p ON p.id = r.product_id
    WHERE p.name = N'便携降噪蓝牙耳机'
    ORDER BY r.review_id DESC
);

INSERT INTO dbo.hishop_report(reporter_role, reporter_id, reporter_name, target_role, target_id, target_name, merchant_id, user_id, order_id, product_id, review_id, report_type, reason, description, evidence_urls, status, admin_id, admin_name, handle_opinion, handle_result, create_time, update_time, handle_time)
SELECT v.reporter_role, v.reporter_id, v.reporter_name, v.target_role, v.target_id, v.target_name, v.merchant_id, v.user_id, v.order_id, v.product_id, v.review_id, v.report_type, v.reason, v.description, v.evidence_urls, v.status, v.admin_id, v.admin_name, v.handle_opinion, v.handle_result, DATEADD(DAY, v.create_offset, SYSDATETIME()), DATEADD(DAY, v.update_offset, SYSDATETIME()), CASE WHEN v.status IN (N'APPROVED', N'REJECTED') THEN DATEADD(DAY, v.update_offset, SYSDATETIME()) ELSE NULL END
FROM (
    SELECT N'USER' AS reporter_role, @lin AS reporter_id, N'演示用户-林晓雨' AS reporter_name, N'PRODUCT' AS target_role, p.id AS target_id, p.name AS target_name, p.merchant_id, @lin AS user_id, NULL AS order_id, p.id AS product_id, NULL AS review_id, N'商品信息不符' AS report_type, N'展示举报-商品信息不符' AS reason, N'详情页承诺的赠品没有在规格中说明。' AS description, p.image_url AS evidence_urls, N'PENDING' AS status, NULL AS admin_id, NULL AS admin_name, NULL AS handle_opinion, NULL AS handle_result, -2 AS create_offset, -2 AS update_offset
    FROM dbo.hishopping_product p WHERE p.name = N'便携降噪蓝牙耳机'
    UNION ALL
    SELECT N'USER', @lin, N'演示用户-林晓雨', N'MERCHANT', @life_merchant, N'晨光优选生活馆', @life_merchant, @lin, NULL, NULL, NULL, N'商家服务', N'展示举报-商家回复慢', N'咨询售后后长时间未回复。', NULL, N'PROCESSING', 1, N'嗨购小组管理员', N'已联系商家说明情况，等待补充材料。', NULL, -5, -3
    UNION ALL
    SELECT N'MERCHANT', @digital_merchant, N'星河数码旗舰店', N'REVIEW', @review_id, N'便携降噪蓝牙耳机评价', @digital_merchant, NULL, NULL, NULL, @review_id, N'恶意评价', N'展示举报-疑似恶意评价', N'评价内容与实际售后记录不完全一致，申请平台复核。', NULL, N'REJECTED', 1, N'嗨购小组管理员', N'评价描述与售后记录基本一致，不予删除。', N'举报驳回，保留评价。', -6, -4
    UNION ALL
    SELECT N'USER', @lin, N'演示用户-林晓雨', N'ORDER', o.id, o.order_no, o.merchant_id, @lin, o.id, NULL, NULL, N'物流问题', N'展示举报-物流异常', N'物流单号长时间没有更新。', NULL, N'APPROVED', 1, N'嗨购小组管理员', N'已核实物流延迟，要求商家补偿优惠券。', N'举报通过，已通知商家处理。', -7, -5
    FROM dbo.hishopping_order o WHERE o.order_no = N'SHOW202607090003'
) AS v
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishop_report r WHERE r.reason = v.reason);

INSERT INTO dbo.hishop_punishment(report_id, target_role, target_id, action_type, duration_days, start_time, end_time, reason, status, admin_id, admin_name, create_time)
SELECT r.report_id, N'MERCHANT', r.merchant_id, N'限制发券', 7, DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, 2, SYSDATETIME()), N'物流异常处理不及时，临时限制发券。', N'ACTIVE', 1, N'嗨购小组管理员', DATEADD(DAY, -5, SYSDATETIME())
FROM dbo.hishop_report r
WHERE r.reason = N'展示举报-物流异常'
  AND NOT EXISTS (SELECT 1 FROM dbo.hishop_punishment p WHERE p.report_id = r.report_id AND p.action_type = N'限制发券');

INSERT INTO dbo.hishop_account_restriction(target_role, target_id, permission_key, restricted, reason, source_type, source_id, start_time, end_time, status, admin_id, admin_name, create_time, update_time)
SELECT N'USER', u.id, N'REPORT', 1, N'短时间内多次提交无效举报，限制举报功能7天。', N'MANUAL', NULL, DATEADD(DAY, -1, SYSDATETIME()), DATEADD(DAY, 6, SYSDATETIME()), N'ACTIVE', 1, N'嗨购小组管理员', DATEADD(DAY, -1, SYSDATETIME()), SYSDATETIME()
FROM dbo.hishopping_user u
WHERE u.email = N'guxinghe@hishopping.com'
  AND NOT EXISTS (SELECT 1 FROM dbo.hishop_account_restriction ar WHERE ar.target_role = N'USER' AND ar.target_id = u.id AND ar.permission_key = N'REPORT' AND ar.status = N'ACTIVE');
GO

DECLARE @lin INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'linxiaoyu@hishopping.com');
DECLARE @chen INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'chenyuanhang@hishopping.com');
DECLARE @zhou INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'zhounian@hishopping.com');
DECLARE @life_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001');
DECLARE @digital_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801002');

INSERT INTO dbo.hishopping_product_view(user_id, product_id, merchant_id, view_time)
SELECT v.user_id, p.id, p.merchant_id, DATEADD(HOUR, -v.hour_offset, SYSDATETIME())
FROM (VALUES
    (@lin, N'便携降噪蓝牙耳机', 1),
    (@chen, N'便携降噪蓝牙耳机', 3),
    (@zhou, N'轻薄办公笔记本 14英寸', 6),
    (@lin, N'有机早餐牛奶组合', 8),
    (@chen, N'家庭清洁护理套装', 12),
    (@zhou, N'项目实践图书套装', 18)
) AS v(user_id, product_name, hour_offset)
JOIN dbo.hishopping_product p ON p.name = v.product_name
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_product_view pv WHERE pv.user_id = v.user_id AND pv.product_id = p.id AND pv.view_time > DATEADD(DAY, -1, SYSDATETIME()));

MERGE dbo.hishopping_conversation AS target
USING (VALUES
    (N'CHAT', N'USER', @lin, N'演示用户-林晓雨', N'MERCHANT', @digital_merchant, N'星河数码旗舰店', N'耳机售后问题已经收到，我们会尽快处理。', N'TEXT', -2),
    (N'SYSTEM', N'ADMIN', 1, N'嗨购小组管理员', N'USER', @lin, N'演示用户-林晓雨', N'您的举报已进入处理中状态。', N'TEXT', -1),
    (N'CHAT', N'USER', @chen, N'演示用户-陈远航', N'MERCHANT', @life_merchant, N'晨光优选生活馆', N'牛奶组合今天可以发货。', N'TEXT', -5)
) AS source(conversation_type, user_a_type, user_a_id, user_a_name, user_b_type, user_b_id, user_b_name, last_message, last_message_type, hour_offset)
ON target.user_a_type = source.user_a_type AND target.user_a_id = source.user_a_id AND target.user_b_type = source.user_b_type AND target.user_b_id = source.user_b_id
WHEN MATCHED THEN UPDATE SET
    conversation_type = source.conversation_type,
    user_a_name = source.user_a_name,
    user_b_name = source.user_b_name,
    last_message = source.last_message,
    last_message_type = source.last_message_type,
    last_message_time = DATEADD(HOUR, source.hour_offset, SYSDATETIME()),
    update_time = DATEADD(HOUR, source.hour_offset, SYSDATETIME())
WHEN NOT MATCHED THEN
    INSERT(conversation_type, user_a_type, user_a_id, user_a_name, user_b_type, user_b_id, user_b_name, last_message, last_message_type, last_message_time, create_time, update_time)
    VALUES(source.conversation_type, source.user_a_type, source.user_a_id, source.user_a_name, source.user_b_type, source.user_b_id, source.user_b_name, source.last_message, source.last_message_type, DATEADD(HOUR, source.hour_offset, SYSDATETIME()), DATEADD(DAY, -2, SYSDATETIME()), DATEADD(HOUR, source.hour_offset, SYSDATETIME()));

INSERT INTO dbo.hishopping_message(conversation_id, sender_role, sender_id, sender_name, receiver_role, receiver_id, receiver_name, title, content, content_type, content_text, link_type, link_target, read_status, create_time)
SELECT c.conversation_id, v.sender_role, v.sender_id, v.sender_name, v.receiver_role, v.receiver_id, v.receiver_name, v.title, v.content, N'TEXT', v.content, v.link_type, v.link_target, v.read_status, DATEADD(HOUR, v.hour_offset, SYSDATETIME())
FROM (VALUES
    (N'MERCHANT', @digital_merchant, N'星河数码旗舰店', N'USER', @lin, N'演示用户-林晓雨', N'售后进度提醒', N'耳机售后问题已经收到，我们会尽快处理。', N'ORDER', N'SHOW202607090005', N'UNREAD', -2),
    (N'ADMIN', 1, N'嗨购小组管理员', N'USER', @lin, N'演示用户-林晓雨', N'举报进度更新', N'您的举报已进入处理中状态。', N'REPORT', N'展示举报-商家回复慢', N'UNREAD', -1),
    (N'MERCHANT', @life_merchant, N'晨光优选生活馆', N'USER', @chen, N'演示用户-陈远航', N'发货提醒', N'牛奶组合今天可以发货。', N'ORDER', N'SHOW202607090004', N'READ', -5)
) AS v(sender_role, sender_id, sender_name, receiver_role, receiver_id, receiver_name, title, content, link_type, link_target, read_status, hour_offset)
JOIN dbo.hishopping_conversation c ON c.user_a_id IN (v.sender_id, v.receiver_id) AND c.user_b_id IN (v.sender_id, v.receiver_id)
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_message m WHERE m.title = v.title AND m.content = v.content AND m.receiver_id = v.receiver_id);

INSERT INTO dbo.hishopping_friend_request(from_user_id, to_user_id, remark, message, status, create_time, handle_time)
SELECT @chen, @lin, N'同学', N'想看看你收藏的数码商品。', N'PENDING', DATEADD(HOUR, -9, SYSDATETIME()), NULL
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_friend_request fr WHERE fr.from_user_id = @chen AND fr.to_user_id = @lin AND fr.status = N'PENDING');

INSERT INTO dbo.hishopping_friend(user_id, friend_user_id, remark, create_time)
SELECT @lin, @zhou, N'项目搭档', DATEADD(DAY, -6, SYSDATETIME())
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_friend f WHERE f.user_id = @lin AND f.friend_user_id = @zhou);

INSERT INTO dbo.hishopping_friend(user_id, friend_user_id, remark, create_time)
SELECT @zhou, @lin, N'项目搭档', DATEADD(DAY, -6, SYSDATETIME())
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_friend f WHERE f.user_id = @zhou AND f.friend_user_id = @lin);
GO

DECLARE @lin INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'linxiaoyu@hishopping.com');
DECLARE @gu INT = (SELECT id FROM dbo.hishopping_user WHERE email = N'guxinghe@hishopping.com');
DECLARE @life_merchant INT = (SELECT merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'8801001');

INSERT INTO dbo.hishopping_account_request(actor_role, actor_id, actor_name, request_type, title, content, attachment_url, status, opinion, create_time, review_time, review_admin_id)
SELECT v.actor_role, v.actor_id, v.actor_name, v.request_type, v.title, v.content, v.attachment_url, v.status, v.opinion, DATEADD(DAY, v.create_offset, SYSDATETIME()), CASE WHEN v.status <> N'PENDING' THEN DATEADD(DAY, v.review_offset, SYSDATETIME()) ELSE NULL END, CASE WHEN v.status <> N'PENDING' THEN 1 ELSE NULL END
FROM (VALUES
    (N'USER', @lin, N'演示用户-林晓雨', N'PROFILE', N'修改头像与昵称', N'申请更新头像和展示昵称。', N'assets/img/nav-profile.png', N'PENDING', NULL, -1, 0),
    (N'USER', @gu, N'演示用户-顾星河', N'RESTORE', N'申请恢复举报功能', N'已了解平台规则，申请恢复举报入口。', NULL, N'REJECTED', N'限制期未结束，请稍后再申请。', -4, -2),
    (N'MERCHANT', @life_merchant, N'晨光优选生活馆', N'PROFILE', N'更新店铺资料', N'补充店铺经营说明和服务承诺。', NULL, N'APPROVED', N'资料合规，已通过。', -5, -4)
) AS v(actor_role, actor_id, actor_name, request_type, title, content, attachment_url, status, opinion, create_offset, review_offset)
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishopping_account_request ar WHERE ar.actor_role = v.actor_role AND ar.actor_id = v.actor_id AND ar.title = v.title);

INSERT INTO dbo.hishop_growth_log(user_id, growth_delta, points_delta, source_type, source_id, remark, create_time)
SELECT o.user_id, CONVERT(INT, o.total_amount), CONVERT(INT, o.total_amount / 10), N'ORDER', o.id, N'展示订单完成后成长值入账', DATEADD(DAY, -8, SYSDATETIME())
FROM dbo.hishopping_order o
WHERE o.order_no = N'SHOW202607090004'
  AND NOT EXISTS (SELECT 1 FROM dbo.hishop_growth_log gl WHERE gl.user_id = o.user_id AND gl.source_type = N'ORDER' AND gl.source_id = o.id);

INSERT INTO dbo.hishop_admin_operation_log(admin_id, operation_type, target_type, target_id, content, operation_time)
SELECT 1, v.operation_type, v.target_type, v.target_id, v.content, DATEADD(DAY, v.day_offset, SYSDATETIME())
FROM (VALUES
    (N'商家审核', N'MERCHANT', @life_merchant, N'通过晨光优选生活馆入驻申请。', -4),
    (N'资料审核', N'ACCOUNT_REQUEST', @lin, N'收到用户头像和昵称修改申请。', -1),
    (N'举报处理', N'REPORT', 0, N'处理物流异常举报并通知商家。', -5),
    (N'优惠券发放', N'COUPON', 0, N'批量发放课程展示优惠券。', -2)
) AS v(operation_type, target_type, target_id, content, day_offset)
WHERE NOT EXISTS (SELECT 1 FROM dbo.hishop_admin_operation_log l WHERE l.operation_type = v.operation_type AND l.content = v.content);
GO

SELECT N'展示数据已就绪' AS info_name, DB_NAME() AS database_name, SYSDATETIME() AS finish_time;

SELECT N'管理员' AS role_name, N'admin' AS login_account, N'123456' AS password, N'后台概览、审核、举报、优惠券、数据分析' AS demo_path
UNION ALL SELECT N'用户', N'linxiaoyu@hishopping.com', N'123456', N'购物车、优惠券、订单、售后、举报、消息'
UNION ALL SELECT N'用户', N'chenyuanhang@hishopping.com', N'123456', N'新人券、已完成订单、评价、好友申请'
UNION ALL SELECT N'用户', N'zhounian@hishopping.com', N'123456', N'高等级会员、发货订单、数码商品评价'
UNION ALL SELECT N'商家', N'8801001', N'123456', N'店铺资料、订单、优惠券、举报、数据分析'
UNION ALL SELECT N'商家', N'8801002', N'123456', N'数码商品、售后、评价回复、店铺数据'
UNION ALL SELECT N'待审商家', N'8801003', N'123456', N'管理员商家审核与商品审核';

SELECT N'hishopping_user' AS table_name, COUNT(*) AS row_count FROM dbo.hishopping_user
UNION ALL SELECT N'hishop_merchant', COUNT(*) FROM dbo.hishop_merchant
UNION ALL SELECT N'hishopping_product', COUNT(*) FROM dbo.hishopping_product
UNION ALL SELECT N'hishopping_order', COUNT(*) FROM dbo.hishopping_order
UNION ALL SELECT N'hishop_product_review', COUNT(*) FROM dbo.hishop_product_review
UNION ALL SELECT N'hishop_report', COUNT(*) FROM dbo.hishop_report
UNION ALL SELECT N'hishop_coupon_template', COUNT(*) FROM dbo.hishop_coupon_template
UNION ALL SELECT N'hishopping_message', COUNT(*) FROM dbo.hishopping_message;
GO
