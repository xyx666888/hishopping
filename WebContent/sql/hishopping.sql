/*
  嗨购商城数据库脚本
  用法：
  1. 第一次运行会自动创建 hishopping 数据库、业务表和基础演示数据。
  2. 后续运行只补齐缺失结构、补齐基础数据并输出查询结果，不会删除用户、购物车、地址、订单等业务数据。
  3. 运行脚本末尾 SELECT 语句可以查看表数据量和明细。
*/


IF DB_ID(N'hishopping') IS NULL
BEGIN
    CREATE DATABASE hishopping;
END
GO

USE hishopping;
GO

IF OBJECT_ID(N'dbo.hishopping_admin', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_admin (
        id INT IDENTITY(1,1) PRIMARY KEY,
        admin_name NVARCHAR(50) NOT NULL UNIQUE,
        password NVARCHAR(50) NOT NULL,
        real_name NVARCHAR(50) NOT NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'姝ｅ父',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_user', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_user (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL,
        email NVARCHAR(100) NOT NULL UNIQUE,
        phone NVARCHAR(30) NULL,
        password NVARCHAR(50) NOT NULL,
        role NVARCHAR(20) NOT NULL DEFAULT N'user',
        points INT NOT NULL DEFAULT 0,
        vip_level INT NOT NULL DEFAULT 1,
        growth_value INT NOT NULL DEFAULT 0,
        status NVARCHAR(20) NOT NULL DEFAULT N'姝ｅ父',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF EXISTS (
    SELECT 1
    FROM sys.stats
    WHERE name = N'UQ_hishopping_user_phone'
      AND object_id = OBJECT_ID(N'dbo.hishopping_user')
)
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_hishopping_user_phone'
      AND object_id = OBJECT_ID(N'dbo.hishopping_user')
)
    DROP STATISTICS dbo.hishopping_user.UQ_hishopping_user_phone;

IF NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = N'UQ_hishopping_user_phone'
         AND object_id = OBJECT_ID(N'dbo.hishopping_user')
   )
   AND NOT EXISTS (
       SELECT phone
       FROM dbo.hishopping_user
       WHERE phone IS NOT NULL AND LTRIM(RTRIM(phone)) <> N''
       GROUP BY phone
       HAVING COUNT(1) > 1
   )
BEGIN
    EXEC(N'CREATE UNIQUE INDEX UQ_hishopping_user_phone
    ON dbo.hishopping_user(phone)
    WHERE phone IS NOT NULL AND phone <> N'''';');
END
GO

IF OBJECT_ID(N'dbo.hishopping_vip_rule', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_vip_rule (
        vip_level INT NOT NULL PRIMARY KEY,
        vip_name NVARCHAR(20) NOT NULL,
        min_growth INT NOT NULL,
        max_growth INT NULL,
        discount DECIMAL(4,2) NOT NULL,
        coupon_count INT NOT NULL,
        point_rate DECIMAL(4,2) NOT NULL,
        service_level NVARCHAR(50) NOT NULL DEFAULT N'普通售后',
        benefit_desc NVARCHAR(500) NOT NULL
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_category', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_category (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(50) NOT NULL UNIQUE,
        icon_text NVARCHAR(10) NOT NULL,
        description NVARCHAR(200) NULL,
        sort_no INT NOT NULL DEFAULT 0
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_product', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_product (
        id INT IDENTITY(1,1) PRIMARY KEY,
        category_id INT NOT NULL,
        name NVARCHAR(100) NOT NULL UNIQUE,
        short_desc NVARCHAR(300) NOT NULL,
        detail_desc NVARCHAR(800) NOT NULL,
        price DECIMAL(10,2) NOT NULL,
        old_price DECIMAL(10,2) NOT NULL,
        rating DECIMAL(3,1) NOT NULL DEFAULT 5.0,
        sales INT NOT NULL DEFAULT 0,
        stock INT NOT NULL DEFAULT 0,
        tag NVARCHAR(20) NOT NULL,
        image_url NVARCHAR(300) NULL,
        gradient NVARCHAR(160) NOT NULL,
        icon_text NVARCHAR(20) NOT NULL,
        color_options NVARCHAR(200) NOT NULL,
        spec_options NVARCHAR(200) NOT NULL,
        sku_attrs NVARCHAR(MAX) NULL,
        sku_options NVARCHAR(MAX) NULL,
        product_attrs NVARCHAR(MAX) NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'上架中',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_hishopping_product_category FOREIGN KEY(category_id) REFERENCES dbo.hishopping_category(id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_product_media', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_product_media (
        id INT IDENTITY(1,1) PRIMARY KEY,
        product_id INT NOT NULL,
        media_type NVARCHAR(20) NOT NULL DEFAULT N'IMAGE',
        media_url NVARCHAR(300) NOT NULL,
        sort_no INT NOT NULL DEFAULT 0,
        cover_flag BIT NOT NULL DEFAULT 0,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_hishopping_product_media_product FOREIGN KEY(product_id) REFERENCES dbo.hishopping_product(id)
    );
    CREATE INDEX IX_hishopping_product_media_product ON dbo.hishopping_product_media(product_id, sort_no, id);
END
GO

IF OBJECT_ID(N'dbo.hishopping_cart_item', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_cart_item (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL DEFAULT 1 CHECK(quantity > 0),
        selected_color NVARCHAR(50) NULL,
        selected_spec NVARCHAR(50) NULL,
        sku_id NVARCHAR(120) NOT NULL DEFAULT N'DEFAULT',
        sku_text NVARCHAR(500) NULL,
        sku_price DECIMAL(10,2) NOT NULL DEFAULT 0,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_hishopping_cart_user_product UNIQUE(user_id, product_id),
        CONSTRAINT FK_hishopping_cart_user FOREIGN KEY(user_id) REFERENCES dbo.hishopping_user(id),
        CONSTRAINT FK_hishopping_cart_product FOREIGN KEY(product_id) REFERENCES dbo.hishopping_product(id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_address', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_address (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        receiver_name NVARCHAR(50) NOT NULL,
        phone NVARCHAR(30) NOT NULL,
        province NVARCHAR(50) NOT NULL,
        city NVARCHAR(50) NOT NULL,
        district NVARCHAR(50) NOT NULL,
        detail NVARCHAR(200) NOT NULL,
        is_default BIT NOT NULL DEFAULT 0,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_hishopping_address_user FOREIGN KEY(user_id) REFERENCES dbo.hishopping_user(id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_order', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_order (
        id INT IDENTITY(1,1) PRIMARY KEY,
        order_no NVARCHAR(40) NOT NULL UNIQUE,
        batch_no NVARCHAR(40) NULL,
        user_id INT NOT NULL,
        merchant_id INT NULL,
        shop_name NVARCHAR(100) NULL,
        goods_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        total_amount DECIMAL(10,2) NOT NULL,
        discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        coupon_title NVARCHAR(50) NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'待付款',
        receiver_name NVARCHAR(50) NOT NULL,
        receiver_phone NVARCHAR(30) NOT NULL,
        receiver_address NVARCHAR(300) NOT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_hishopping_order_user FOREIGN KEY(user_id) REFERENCES dbo.hishopping_user(id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_order_item', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_order_item (
        id INT IDENTITY(1,1) PRIMARY KEY,
        order_id INT NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL CHECK(quantity > 0),
        price DECIMAL(10,2) NOT NULL,
        selected_color NVARCHAR(50) NULL,
        selected_spec NVARCHAR(50) NULL,
        sku_id NVARCHAR(120) NULL,
        sku_text NVARCHAR(500) NULL,
        snapshot_name NVARCHAR(100) NULL,
        snapshot_image NVARCHAR(300) NULL,
        item_unit_price DECIMAL(10,2) NULL,
        item_subtotal DECIMAL(10,2) NULL,
        CONSTRAINT FK_hishopping_order_item_order FOREIGN KEY(order_id) REFERENCES dbo.hishopping_order(id),
        CONSTRAINT FK_hishopping_order_item_product FOREIGN KEY(product_id) REFERENCES dbo.hishopping_product(id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_favorite', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_favorite (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        product_id INT NOT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_hishopping_favorite_user_product UNIQUE(user_id, product_id),
        CONSTRAINT FK_hishopping_favorite_user FOREIGN KEY(user_id) REFERENCES dbo.hishopping_user(id),
        CONSTRAINT FK_hishopping_favorite_product FOREIGN KEY(product_id) REFERENCES dbo.hishopping_product(id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_hall_banner', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_hall_banner (
        id INT IDENTITY(1,1) PRIMARY KEY,
        media_type NVARCHAR(20) NOT NULL DEFAULT N'IMAGE',
        media_url NVARCHAR(300) NOT NULL,
        title NVARCHAR(100) NULL,
        subtitle NVARCHAR(300) NULL,
        enabled BIT NOT NULL DEFAULT 1,
        sort_no INT NOT NULL DEFAULT 0,
        link_enabled BIT NOT NULL DEFAULT 0,
        link_type NVARCHAR(30) NULL,
        link_target NVARCHAR(200) NULL,
        product_id INT NULL,
        overlay_enabled BIT NOT NULL DEFAULT 1,
        text_position NVARCHAR(20) NOT NULL DEFAULT N'LEFT',
        title_color NVARCHAR(20) NOT NULL DEFAULT N'#ffffff',
        subtitle_color NVARCHAR(20) NOT NULL DEFAULT N'#e2e8f0',
        video_muted_default BIT NOT NULL DEFAULT 1,
        video_disable_seek BIT NOT NULL DEFAULT 0,
        video_disable_pause BIT NOT NULL DEFAULT 0,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        update_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF COL_LENGTH('dbo.hishopping_hall_banner', 'overlay_enabled') IS NULL
    ALTER TABLE dbo.hishopping_hall_banner ADD overlay_enabled BIT NOT NULL CONSTRAINT DF_hishopping_hall_overlay DEFAULT 1;
IF COL_LENGTH('dbo.hishopping_hall_banner', 'text_position') IS NULL
    ALTER TABLE dbo.hishopping_hall_banner ADD text_position NVARCHAR(20) NOT NULL CONSTRAINT DF_hishopping_hall_position DEFAULT N'LEFT';
IF COL_LENGTH('dbo.hishopping_hall_banner', 'title_color') IS NULL
    ALTER TABLE dbo.hishopping_hall_banner ADD title_color NVARCHAR(20) NOT NULL CONSTRAINT DF_hishopping_hall_title_color DEFAULT N'#ffffff';
IF COL_LENGTH('dbo.hishopping_hall_banner', 'subtitle_color') IS NULL
    ALTER TABLE dbo.hishopping_hall_banner ADD subtitle_color NVARCHAR(20) NOT NULL CONSTRAINT DF_hishopping_hall_subtitle_color DEFAULT N'#e2e8f0';
GO

IF OBJECT_ID(N'dbo.hishopping_conversation', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_conversation (
        conversation_id INT IDENTITY(1,1) PRIMARY KEY,
        conversation_type NVARCHAR(30) NOT NULL DEFAULT N'CHAT',
        user_a_type NVARCHAR(20) NOT NULL,
        user_a_id INT NOT NULL,
        user_a_name NVARCHAR(100) NULL,
        user_a_avatar NVARCHAR(300) NULL,
        user_b_type NVARCHAR(20) NOT NULL,
        user_b_id INT NOT NULL,
        user_b_name NVARCHAR(100) NULL,
        user_b_avatar NVARCHAR(300) NULL,
        last_message NVARCHAR(500) NULL,
        last_message_type NVARCHAR(20) NOT NULL DEFAULT N'TEXT',
        last_message_time DATETIME2 NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        update_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_message', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_message (
        message_id INT IDENTITY(1,1) PRIMARY KEY,
        conversation_id INT NULL,
        sender_role NVARCHAR(20) NOT NULL,
        sender_id INT NOT NULL DEFAULT 0,
        sender_name NVARCHAR(80) NULL,
        receiver_role NVARCHAR(20) NOT NULL,
        receiver_id INT NOT NULL DEFAULT 0,
        receiver_name NVARCHAR(80) NULL,
        title NVARCHAR(120) NOT NULL,
        content NVARCHAR(1000) NOT NULL,
        content_type NVARCHAR(20) NOT NULL DEFAULT N'TEXT',
        content_text NVARCHAR(MAX) NULL,
        media_url NVARCHAR(500) NULL,
        file_name NVARCHAR(260) NULL,
        file_size BIGINT NOT NULL DEFAULT 0,
        client_message_id NVARCHAR(64) NULL,
        recalled BIT NOT NULL DEFAULT 0,
        recalled_at DATETIME2 NULL,
        quote_message_id INT NULL,
        link_type NVARCHAR(30) NOT NULL DEFAULT N'NONE',
        link_target NVARCHAR(200) NULL,
        read_status NVARCHAR(20) NOT NULL DEFAULT N'UNREAD',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        read_time DATETIME2 NULL
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_product_view', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_product_view (
        view_id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        product_id INT NOT NULL,
        merchant_id INT NOT NULL,
        view_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_user_friend', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_user_friend (
        friend_id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        friend_user_id INT NOT NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_friend_request', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_friend_request (
        id INT IDENTITY(1,1) PRIMARY KEY,
        from_user_id INT NOT NULL,
        to_user_id INT NOT NULL,
        remark NVARCHAR(100) NULL,
        message NVARCHAR(500) NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'PENDING',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        handle_time DATETIME2 NULL
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_friend', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_friend (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        friend_user_id INT NOT NULL,
        remark NVARCHAR(100) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_hishopping_friend UNIQUE(user_id, friend_user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_account_request', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_account_request (
        request_id INT IDENTITY(1,1) PRIMARY KEY,
        actor_role NVARCHAR(20) NOT NULL,
        actor_id INT NOT NULL,
        actor_name NVARCHAR(100) NULL,
        request_type NVARCHAR(30) NOT NULL,
        title NVARCHAR(120) NOT NULL,
        content NVARCHAR(1000) NOT NULL,
        attachment_url NVARCHAR(300) NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'PENDING',
        opinion NVARCHAR(500) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        review_time DATETIME2 NULL,
        review_admin_id INT NULL
    );
END
GO

IF COL_LENGTH('dbo.hishopping_account_request', 'attachment_url') IS NULL
    ALTER TABLE dbo.hishopping_account_request ADD attachment_url NVARCHAR(300) NULL;
GO

IF COL_LENGTH('dbo.hishopping_order', 'discount_amount') IS NULL
    ALTER TABLE dbo.hishopping_order ADD discount_amount DECIMAL(10,2) NOT NULL CONSTRAINT DF_hishopping_order_discount_amount DEFAULT 0;
IF COL_LENGTH('dbo.hishopping_user', 'avatar_url') IS NULL
    ALTER TABLE dbo.hishopping_user ADD avatar_url NVARCHAR(300) NULL;
IF COL_LENGTH('dbo.hishopping_user', 'punish_reason') IS NULL
    ALTER TABLE dbo.hishopping_user ADD punish_reason NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishopping_user', 'punish_start_time') IS NULL
    ALTER TABLE dbo.hishopping_user ADD punish_start_time DATETIME2 NULL;
IF COL_LENGTH('dbo.hishopping_user', 'punish_end_time') IS NULL
    ALTER TABLE dbo.hishopping_user ADD punish_end_time DATETIME2 NULL;
IF COL_LENGTH('dbo.hishop_merchant', 'avatar_url') IS NULL
    ALTER TABLE dbo.hishop_merchant ADD avatar_url NVARCHAR(300) NULL;
IF COL_LENGTH('dbo.hishop_merchant', 'punish_reason') IS NULL
    ALTER TABLE dbo.hishop_merchant ADD punish_reason NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishop_merchant', 'punish_start_time') IS NULL
    ALTER TABLE dbo.hishop_merchant ADD punish_start_time DATETIME2 NULL;
IF COL_LENGTH('dbo.hishop_merchant', 'punish_end_time') IS NULL
    ALTER TABLE dbo.hishop_merchant ADD punish_end_time DATETIME2 NULL;
IF COL_LENGTH('dbo.hishopping_message', 'conversation_id') IS NULL
    ALTER TABLE dbo.hishopping_message ADD conversation_id INT NULL;
IF COL_LENGTH('dbo.hishopping_message', 'content_type') IS NULL
    ALTER TABLE dbo.hishopping_message ADD content_type NVARCHAR(20) NOT NULL CONSTRAINT DF_hishopping_message_content_type DEFAULT N'TEXT';
IF COL_LENGTH('dbo.hishopping_message', 'content_text') IS NULL
    ALTER TABLE dbo.hishopping_message ADD content_text NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.hishopping_message', 'media_url') IS NULL
    ALTER TABLE dbo.hishopping_message ADD media_url NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishopping_message', 'file_name') IS NULL
    ALTER TABLE dbo.hishopping_message ADD file_name NVARCHAR(260) NULL;
IF COL_LENGTH('dbo.hishopping_message', 'file_size') IS NULL
    ALTER TABLE dbo.hishopping_message ADD file_size BIGINT NOT NULL CONSTRAINT DF_hishopping_message_file_size DEFAULT 0;
IF COL_LENGTH('dbo.hishopping_message', 'client_message_id') IS NULL
    ALTER TABLE dbo.hishopping_message ADD client_message_id NVARCHAR(64) NULL;
IF COL_LENGTH('dbo.hishopping_message', 'recalled') IS NULL
    ALTER TABLE dbo.hishopping_message ADD recalled BIT NOT NULL CONSTRAINT DF_hishopping_message_recalled DEFAULT 0;
IF COL_LENGTH('dbo.hishopping_message', 'recalled_at') IS NULL
    ALTER TABLE dbo.hishopping_message ADD recalled_at DATETIME2 NULL;
IF COL_LENGTH('dbo.hishopping_message', 'quote_message_id') IS NULL
    ALTER TABLE dbo.hishopping_message ADD quote_message_id INT NULL;
IF COL_LENGTH('dbo.hishopping_message', 'ref_type') IS NULL
    ALTER TABLE dbo.hishopping_message ADD ref_type NVARCHAR(30) NULL;
IF COL_LENGTH('dbo.hishopping_message', 'ref_id') IS NULL
    ALTER TABLE dbo.hishopping_message ADD ref_id INT NULL;
IF COL_LENGTH('dbo.hishopping_message', 'extra_json') IS NULL
    ALTER TABLE dbo.hishopping_message ADD extra_json NVARCHAR(MAX) NULL;
UPDATE dbo.hishopping_message SET content_text = content WHERE content_text IS NULL;
IF COL_LENGTH('dbo.hishopping_order', 'coupon_title') IS NULL
    ALTER TABLE dbo.hishopping_order ADD coupon_title NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.hishopping_order', 'batch_no') IS NULL
    ALTER TABLE dbo.hishopping_order ADD batch_no NVARCHAR(40) NULL;
IF COL_LENGTH('dbo.hishopping_order', 'merchant_id') IS NULL
    ALTER TABLE dbo.hishopping_order ADD merchant_id INT NULL;
IF COL_LENGTH('dbo.hishopping_order', 'shop_name') IS NULL
    ALTER TABLE dbo.hishopping_order ADD shop_name NVARCHAR(100) NULL;
IF COL_LENGTH('dbo.hishopping_order', 'goods_amount') IS NULL
    ALTER TABLE dbo.hishopping_order ADD goods_amount DECIMAL(10,2) NOT NULL CONSTRAINT DF_hishopping_order_goods_amount DEFAULT 0;
IF COL_LENGTH('dbo.hishopping_product', 'sku_options') IS NULL
    ALTER TABLE dbo.hishopping_product ADD sku_options NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.hishopping_product', 'sku_attrs') IS NULL
    ALTER TABLE dbo.hishopping_product ADD sku_attrs NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.hishopping_product', 'product_attrs') IS NULL
    ALTER TABLE dbo.hishopping_product ADD product_attrs NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.hishopping_cart_item', 'selected_color') IS NULL
    ALTER TABLE dbo.hishopping_cart_item ADD selected_color NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.hishopping_cart_item', 'selected_spec') IS NULL
    ALTER TABLE dbo.hishopping_cart_item ADD selected_spec NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.hishopping_cart_item', 'sku_id') IS NULL
    ALTER TABLE dbo.hishopping_cart_item ADD sku_id NVARCHAR(120) NOT NULL CONSTRAINT DF_hishopping_cart_sku_id DEFAULT N'DEFAULT';
IF COL_LENGTH('dbo.hishopping_cart_item', 'sku_text') IS NULL
    ALTER TABLE dbo.hishopping_cart_item ADD sku_text NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishopping_cart_item', 'sku_price') IS NULL
    ALTER TABLE dbo.hishopping_cart_item ADD sku_price DECIMAL(10,2) NOT NULL CONSTRAINT DF_hishopping_cart_sku_price DEFAULT 0;
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = N'UQ_hishopping_cart_user_product' AND parent_object_id = OBJECT_ID(N'dbo.hishopping_cart_item'))
    ALTER TABLE dbo.hishopping_cart_item DROP CONSTRAINT UQ_hishopping_cart_user_product;
IF NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = N'UQ_hishopping_cart_user_product_sku' AND parent_object_id = OBJECT_ID(N'dbo.hishopping_cart_item'))
    ALTER TABLE dbo.hishopping_cart_item ADD CONSTRAINT UQ_hishopping_cart_user_product_sku UNIQUE(user_id, product_id, sku_id);
IF COL_LENGTH('dbo.hishopping_order_item', 'selected_color') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD selected_color NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.hishopping_order_item', 'selected_spec') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD selected_spec NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.hishopping_order_item', 'sku_id') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD sku_id NVARCHAR(120) NULL;
IF COL_LENGTH('dbo.hishopping_order_item', 'sku_text') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD sku_text NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishopping_order_item', 'snapshot_name') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD snapshot_name NVARCHAR(100) NULL;
IF COL_LENGTH('dbo.hishopping_order_item', 'snapshot_image') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD snapshot_image NVARCHAR(300) NULL;
IF COL_LENGTH('dbo.hishopping_order_item', 'item_unit_price') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD item_unit_price DECIMAL(10,2) NULL;
IF COL_LENGTH('dbo.hishopping_order_item', 'item_subtotal') IS NULL
    ALTER TABLE dbo.hishopping_order_item ADD item_subtotal DECIMAL(10,2) NULL;
GO

IF OBJECT_ID(N'dbo.hishop_merchant', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_merchant (
        merchant_id INT IDENTITY(1,1) PRIMARY KEY,
        merchant_code NVARCHAR(8) NOT NULL UNIQUE,
        merchant_name NVARCHAR(80) NOT NULL,
        password NVARCHAR(80) NOT NULL,
        register_password_demo NVARCHAR(80) NULL,
        contact_name NVARCHAR(50) NOT NULL,
        contact_phone NVARCHAR(30) NOT NULL,
        email NVARCHAR(100) NULL,
        shop_name NVARCHAR(100) NOT NULL,
        shop_desc NVARCHAR(500) NULL,
        business_category NVARCHAR(100) NULL,
        business_address NVARCHAR(200) NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'PENDING',
        reject_reason NVARCHAR(500) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        update_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        review_time DATETIME2 NULL,
        review_admin_id INT NULL
    );
END
GO

IF OBJECT_ID(N'dbo.hishop_merchant_audit_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_merchant_audit_log (
        log_id INT IDENTITY(1,1) PRIMARY KEY,
        merchant_id INT NOT NULL,
        before_status NVARCHAR(20) NULL,
        after_status NVARCHAR(20) NOT NULL,
        admin_id INT NULL,
        audit_opinion NVARCHAR(500) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF COL_LENGTH('dbo.hishopping_product', 'merchant_id') IS NULL
    ALTER TABLE dbo.hishopping_product ADD merchant_id INT NULL;
IF COL_LENGTH('dbo.hishopping_product', 'sale_status') IS NULL
    ALTER TABLE dbo.hishopping_product ADD sale_status NVARCHAR(20) NOT NULL CONSTRAINT DF_hishopping_product_sale_status DEFAULT N'ON_SALE';
IF COL_LENGTH('dbo.hishopping_product', 'audit_status') IS NULL
    ALTER TABLE dbo.hishopping_product ADD audit_status NVARCHAR(20) NOT NULL CONSTRAINT DF_hishopping_product_audit_status DEFAULT N'APPROVED';
IF COL_LENGTH('dbo.hishopping_product', 'audit_opinion') IS NULL
    ALTER TABLE dbo.hishopping_product ADD audit_opinion NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishopping_product', 'submit_time') IS NULL
    ALTER TABLE dbo.hishopping_product ADD submit_time DATETIME2 NULL;
IF COL_LENGTH('dbo.hishopping_product', 'review_time') IS NULL
    ALTER TABLE dbo.hishopping_product ADD review_time DATETIME2 NULL;
IF COL_LENGTH('dbo.hishopping_product', 'review_admin_id') IS NULL
    ALTER TABLE dbo.hishopping_product ADD review_admin_id INT NULL;
GO

IF OBJECT_ID(N'dbo.hishop_product_audit_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_product_audit_log (
        log_id INT IDENTITY(1,1) PRIMARY KEY,
        product_id INT NOT NULL,
        merchant_id INT NULL,
        before_audit_status NVARCHAR(20) NULL,
        after_audit_status NVARCHAR(20) NOT NULL,
        before_sale_status NVARCHAR(20) NULL,
        after_sale_status NVARCHAR(20) NOT NULL,
        admin_id INT NULL,
        audit_opinion NVARCHAR(500) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishop_coupon_template', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_coupon_template (
        coupon_id INT IDENTITY(1,1) PRIMARY KEY,
        coupon_name NVARCHAR(100) NOT NULL,
        coupon_type NVARCHAR(20) NOT NULL,
        amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        discount_rate DECIMAL(5,2) NOT NULL DEFAULT 1,
        min_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        target_type NVARCHAR(30) NULL,
        target_value NVARCHAR(100) NULL,
        vip_level INT NULL,
        total_quantity INT NOT NULL DEFAULT 0,
        per_user_limit INT NOT NULL DEFAULT 1,
        start_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        end_time DATETIME2 NOT NULL DEFAULT DATEADD(DAY, 30, SYSDATETIME()),
        valid_days INT NOT NULL DEFAULT 30,
        is_new_user_coupon BIT NOT NULL DEFAULT 0,
        is_vip_coupon BIT NOT NULL DEFAULT 0,
        status NVARCHAR(20) NOT NULL DEFAULT N'ENABLED',
        coupon_owner_type NVARCHAR(20) NOT NULL DEFAULT N'PLATFORM',
        merchant_id INT NULL,
        stackable BIT NOT NULL DEFAULT 0,
        home_title NVARCHAR(100) NULL,
        home_subtitle NVARCHAR(200) NULL,
        use_scope NVARCHAR(20) NOT NULL DEFAULT N'ALL',
        description NVARCHAR(500) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        update_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishop_user_coupon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_user_coupon (
        user_coupon_id INT IDENTITY(1,1) PRIMARY KEY,
        coupon_id INT NOT NULL,
        user_id INT NOT NULL,
        coupon_name NVARCHAR(100) NOT NULL,
        coupon_type NVARCHAR(20) NOT NULL,
        amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        discount_rate DECIMAL(5,2) NOT NULL DEFAULT 1,
        min_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        vip_level INT NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'UNUSED',
        receive_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        expire_time DATETIME2 NULL,
        use_time DATETIME2 NULL,
        order_id INT NULL,
        issue_batch_no NVARCHAR(50) NULL,
        coupon_owner_type NVARCHAR(20) NOT NULL DEFAULT N'PLATFORM',
        merchant_id INT NULL,
        stackable BIT NOT NULL DEFAULT 0,
        use_scope NVARCHAR(20) NOT NULL DEFAULT N'ALL',
        description NVARCHAR(500) NULL
    );
END
GO

IF COL_LENGTH('dbo.hishop_coupon_template', 'coupon_owner_type') IS NULL ALTER TABLE dbo.hishop_coupon_template ADD coupon_owner_type NVARCHAR(20) NOT NULL CONSTRAINT DF_sql_coupon_template_owner DEFAULT N'PLATFORM';
IF COL_LENGTH('dbo.hishop_coupon_template', 'merchant_id') IS NULL ALTER TABLE dbo.hishop_coupon_template ADD merchant_id INT NULL;
IF COL_LENGTH('dbo.hishop_coupon_template', 'stackable') IS NULL ALTER TABLE dbo.hishop_coupon_template ADD stackable BIT NOT NULL CONSTRAINT DF_sql_coupon_template_stackable DEFAULT 0;
IF COL_LENGTH('dbo.hishop_coupon_template', 'home_title') IS NULL ALTER TABLE dbo.hishop_coupon_template ADD home_title NVARCHAR(100) NULL;
IF COL_LENGTH('dbo.hishop_coupon_template', 'home_subtitle') IS NULL ALTER TABLE dbo.hishop_coupon_template ADD home_subtitle NVARCHAR(200) NULL;
IF COL_LENGTH('dbo.hishop_coupon_template', 'use_scope') IS NULL ALTER TABLE dbo.hishop_coupon_template ADD use_scope NVARCHAR(20) NOT NULL CONSTRAINT DF_sql_coupon_template_scope DEFAULT N'ALL';
IF COL_LENGTH('dbo.hishop_coupon_template', 'description') IS NULL ALTER TABLE dbo.hishop_coupon_template ADD description NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishop_user_coupon', 'coupon_owner_type') IS NULL ALTER TABLE dbo.hishop_user_coupon ADD coupon_owner_type NVARCHAR(20) NOT NULL CONSTRAINT DF_sql_user_coupon_owner DEFAULT N'PLATFORM';
IF COL_LENGTH('dbo.hishop_user_coupon', 'merchant_id') IS NULL ALTER TABLE dbo.hishop_user_coupon ADD merchant_id INT NULL;
IF COL_LENGTH('dbo.hishop_user_coupon', 'stackable') IS NULL ALTER TABLE dbo.hishop_user_coupon ADD stackable BIT NOT NULL CONSTRAINT DF_sql_user_coupon_stackable DEFAULT 0;
IF COL_LENGTH('dbo.hishop_user_coupon', 'use_scope') IS NULL ALTER TABLE dbo.hishop_user_coupon ADD use_scope NVARCHAR(20) NOT NULL CONSTRAINT DF_sql_user_coupon_scope DEFAULT N'ALL';
IF COL_LENGTH('dbo.hishop_user_coupon', 'description') IS NULL ALTER TABLE dbo.hishop_user_coupon ADD description NVARCHAR(500) NULL;
GO

IF OBJECT_ID(N'dbo.hishop_coupon_issue_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_coupon_issue_log (
        issue_log_id INT IDENTITY(1,1) PRIMARY KEY,
        coupon_id INT NOT NULL,
        issue_batch_no NVARCHAR(50) NOT NULL,
        issue_type NVARCHAR(30) NOT NULL,
        target_value NVARCHAR(100) NULL,
        issue_count INT NOT NULL DEFAULT 0,
        skip_count INT NOT NULL DEFAULT 0,
        admin_id INT NULL,
        issue_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        remark NVARCHAR(500) NULL
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.hishop_merchant WHERE merchant_code = N'0798682')
BEGIN
    INSERT INTO dbo.hishop_merchant(merchant_code, merchant_name, password, register_password_demo, contact_name, contact_phone, email, shop_name, shop_desc, business_category, business_address, status, review_time, review_admin_id)
    VALUES(N'0798682', N'课程演示商家', N'123456', N'123456', N'演示联系人', N'13900000001', N'merchant-demo@hishopping.com', N'课程演示店铺', N'课程验收演示店铺', N'综合类目', N'江苏省南京市软件大道88号', N'APPROVED', SYSDATETIME(), 1);
END
GO

IF COL_LENGTH('dbo.hishopping_order', 'growth_awarded') IS NULL
    ALTER TABLE dbo.hishopping_order ADD growth_awarded BIT NOT NULL CONSTRAINT DF_hishopping_order_growth_awarded DEFAULT 0;
GO

IF OBJECT_ID(N'dbo.hishop_order_shipment', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_order_shipment (
        shipment_id INT IDENTITY(1,1) PRIMARY KEY,
        order_id INT NOT NULL,
        merchant_id INT NOT NULL,
        express_company NVARCHAR(80) NOT NULL,
        tracking_no NVARCHAR(80) NOT NULL,
        ship_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_hishop_order_shipment_order_merchant UNIQUE(order_id, merchant_id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishop_after_sale', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_after_sale (
        after_sale_id INT IDENTITY(1,1) PRIMARY KEY,
        order_id INT NOT NULL,
        user_id INT NOT NULL,
        merchant_id INT NOT NULL,
        product_id INT NOT NULL,
        after_sale_type NVARCHAR(20) NOT NULL,
        reason NVARCHAR(500) NOT NULL,
        description NVARCHAR(1000) NULL,
        evidence_urls NVARCHAR(MAX) NULL,
        refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        status NVARCHAR(20) NOT NULL DEFAULT N'待审核',
        apply_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        handle_opinion NVARCHAR(500) NULL,
        admin_opinion NVARCHAR(500) NULL,
        update_time DATETIME2 NULL,
        handle_time DATETIME2 NULL
    );
END
GO

IF COL_LENGTH('dbo.hishop_after_sale', 'description') IS NULL
    ALTER TABLE dbo.hishop_after_sale ADD description NVARCHAR(1000) NULL;
IF COL_LENGTH('dbo.hishop_after_sale', 'evidence_urls') IS NULL
    ALTER TABLE dbo.hishop_after_sale ADD evidence_urls NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.hishop_after_sale', 'admin_opinion') IS NULL
    ALTER TABLE dbo.hishop_after_sale ADD admin_opinion NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishop_after_sale', 'update_time') IS NULL
    ALTER TABLE dbo.hishop_after_sale ADD update_time DATETIME2 NULL;
GO

IF EXISTS (
    SELECT 1
    FROM sys.stats
    WHERE name = N'IX_hishop_after_sale_order'
      AND object_id = OBJECT_ID(N'dbo.hishop_after_sale')
)
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_hishop_after_sale_order'
      AND object_id = OBJECT_ID(N'dbo.hishop_after_sale')
)
    DROP STATISTICS dbo.hishop_after_sale.IX_hishop_after_sale_order;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_hishop_after_sale_order'
      AND object_id = OBJECT_ID(N'dbo.hishop_after_sale')
)
    EXEC(N'CREATE INDEX IX_hishop_after_sale_order ON dbo.hishop_after_sale(order_id, user_id);');
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_order_user_time' AND object_id = OBJECT_ID(N'dbo.hishopping_order'))
    EXEC(N'CREATE INDEX IX_hishopping_order_user_time ON dbo.hishopping_order(user_id, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_order_merchant_status' AND object_id = OBJECT_ID(N'dbo.hishopping_order'))
    EXEC(N'CREATE INDEX IX_hishopping_order_merchant_status ON dbo.hishopping_order(merchant_id, status, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_order_batch' AND object_id = OBJECT_ID(N'dbo.hishopping_order'))
    EXEC(N'CREATE INDEX IX_hishopping_order_batch ON dbo.hishopping_order(batch_no);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_order_item_order' AND object_id = OBJECT_ID(N'dbo.hishopping_order_item'))
    EXEC(N'CREATE INDEX IX_hishopping_order_item_order ON dbo.hishopping_order_item(order_id);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_cart_user' AND object_id = OBJECT_ID(N'dbo.hishopping_cart_item'))
    EXEC(N'CREATE INDEX IX_hishopping_cart_user ON dbo.hishopping_cart_item(user_id);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_product_merchant_status' AND object_id = OBJECT_ID(N'dbo.hishopping_product'))
    EXEC(N'CREATE INDEX IX_hishopping_product_merchant_status ON dbo.hishopping_product(merchant_id, audit_status, sale_status);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_favorite_user' AND object_id = OBJECT_ID(N'dbo.hishopping_favorite'))
    EXEC(N'CREATE INDEX IX_hishopping_favorite_user ON dbo.hishopping_favorite(user_id, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_message_receiver_unread' AND object_id = OBJECT_ID(N'dbo.hishopping_message'))
    EXEC(N'CREATE INDEX IX_hishopping_message_receiver_unread ON dbo.hishopping_message(receiver_role, receiver_id, read_status, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_message_conversation' AND object_id = OBJECT_ID(N'dbo.hishopping_message'))
    EXEC(N'CREATE INDEX IX_hishopping_message_conversation ON dbo.hishopping_message(conversation_id, create_time);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_hishopping_message_client_id' AND object_id = OBJECT_ID(N'dbo.hishopping_message'))
    EXEC(N'CREATE UNIQUE INDEX UX_hishopping_message_client_id ON dbo.hishopping_message(conversation_id, sender_role, sender_id, client_message_id) WHERE client_message_id IS NOT NULL AND client_message_id <> N'''';');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_conversation_user_a' AND object_id = OBJECT_ID(N'dbo.hishopping_conversation'))
    EXEC(N'CREATE INDEX IX_hishopping_conversation_user_a ON dbo.hishopping_conversation(user_a_type, user_a_id, update_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_conversation_user_b' AND object_id = OBJECT_ID(N'dbo.hishopping_conversation'))
    EXEC(N'CREATE INDEX IX_hishopping_conversation_user_b ON dbo.hishopping_conversation(user_b_type, user_b_id, update_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_product_view_merchant_user' AND object_id = OBJECT_ID(N'dbo.hishopping_product_view'))
    EXEC(N'CREATE INDEX IX_hishopping_product_view_merchant_user ON dbo.hishopping_product_view(merchant_id, user_id, view_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_friend_request_to_status' AND object_id = OBJECT_ID(N'dbo.hishopping_friend_request'))
    EXEC(N'CREATE INDEX IX_hishopping_friend_request_to_status ON dbo.hishopping_friend_request(to_user_id, status, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_after_sale_merchant_status' AND object_id = OBJECT_ID(N'dbo.hishop_after_sale'))
    EXEC(N'CREATE INDEX IX_hishop_after_sale_merchant_status ON dbo.hishop_after_sale(merchant_id, status, apply_time DESC);');
GO

IF OBJECT_ID(N'dbo.hishop_product_review', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_product_review (
        review_id INT IDENTITY(1,1) PRIMARY KEY,
        order_id INT NOT NULL,
        order_item_id INT NULL,
        product_id INT NOT NULL,
        user_id INT NOT NULL,
        rating INT NOT NULL,
        content NVARCHAR(1000) NULL,
        anonymous_flag BIT NOT NULL DEFAULT 0,
        status NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
        like_count INT NOT NULL DEFAULT 0,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        update_time DATETIME2 NULL,
        CONSTRAINT UQ_hishop_product_review_order_product UNIQUE(order_id, product_id, user_id)
    );
END
GO

IF COL_LENGTH('dbo.hishop_product_review', 'order_item_id') IS NULL
    ALTER TABLE dbo.hishop_product_review ADD order_item_id INT NULL;
IF COL_LENGTH('dbo.hishop_product_review', 'status') IS NULL
    ALTER TABLE dbo.hishop_product_review ADD status NVARCHAR(20) NOT NULL CONSTRAINT DF_hishop_product_review_status DEFAULT N'ACTIVE';
IF COL_LENGTH('dbo.hishop_product_review', 'like_count') IS NULL
    ALTER TABLE dbo.hishop_product_review ADD like_count INT NOT NULL CONSTRAINT DF_hishop_product_review_like_count DEFAULT 0;
IF COL_LENGTH('dbo.hishop_product_review', 'update_time') IS NULL
    ALTER TABLE dbo.hishop_product_review ADD update_time DATETIME2 NULL;
IF COL_LENGTH('dbo.hishop_product_review', 'anonymous_flag') IS NULL
    ALTER TABLE dbo.hishop_product_review ADD anonymous_flag BIT NOT NULL CONSTRAINT DF_hishop_product_review_anonymous_flag DEFAULT 0;
GO

IF OBJECT_ID(N'dbo.hishopping_review_reply', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_review_reply (
        reply_id INT IDENTITY(1,1) PRIMARY KEY,
        review_id INT NOT NULL,
        parent_reply_id INT NULL,
        user_type NVARCHAR(20) NOT NULL,
        user_id INT NOT NULL,
        user_name NVARCHAR(80) NULL,
        user_avatar NVARCHAR(500) NULL,
        content NVARCHAR(1000) NOT NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishopping_review_like', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishopping_review_like (
        like_id INT IDENTITY(1,1) PRIMARY KEY,
        review_id INT NOT NULL,
        user_type NVARCHAR(20) NOT NULL,
        user_id INT NOT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_hishopping_review_like_user UNIQUE(review_id, user_type, user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.hishop_product_review_media', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_product_review_media (
        media_id INT IDENTITY(1,1) PRIMARY KEY,
        review_id INT NULL,
        owner_type NVARCHAR(20) NOT NULL,
        owner_id INT NOT NULL,
        product_id INT NOT NULL,
        media_type NVARCHAR(20) NOT NULL,
        media_url NVARCHAR(500) NOT NULL,
        file_name NVARCHAR(200) NULL,
        file_size BIGINT NOT NULL DEFAULT 0,
        sort_no INT NOT NULL DEFAULT 0,
        status NVARCHAR(20) NOT NULL DEFAULT N'TEMP',
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_product_review_product_status' AND object_id = OBJECT_ID(N'dbo.hishop_product_review'))
    EXEC(N'CREATE INDEX IX_hishop_product_review_product_status ON dbo.hishop_product_review(product_id, status, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_review_reply_review' AND object_id = OBJECT_ID(N'dbo.hishopping_review_reply'))
    EXEC(N'CREATE INDEX IX_hishopping_review_reply_review ON dbo.hishopping_review_reply(review_id, create_time ASC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishopping_review_like_review' AND object_id = OBJECT_ID(N'dbo.hishopping_review_like'))
    EXEC(N'CREATE INDEX IX_hishopping_review_like_review ON dbo.hishopping_review_like(review_id);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_product_review_media_review' AND object_id = OBJECT_ID(N'dbo.hishop_product_review_media'))
    EXEC(N'CREATE INDEX IX_hishop_product_review_media_review ON dbo.hishop_product_review_media(review_id, status, sort_no, media_id);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_product_review_media_owner' AND object_id = OBJECT_ID(N'dbo.hishop_product_review_media'))
    EXEC(N'CREATE INDEX IX_hishop_product_review_media_owner ON dbo.hishop_product_review_media(owner_type, owner_id, status, create_time DESC);');
GO

IF OBJECT_ID(N'dbo.hishop_report', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_report (
        report_id INT IDENTITY(1,1) PRIMARY KEY,
        reporter_role NVARCHAR(20) NOT NULL,
        reporter_id INT NOT NULL,
        reporter_name NVARCHAR(100) NULL,
        target_role NVARCHAR(20) NOT NULL,
        target_id INT NOT NULL,
        target_name NVARCHAR(160) NULL,
        merchant_id INT NULL,
        user_id INT NULL,
        order_id INT NULL,
        product_id INT NULL,
        review_id INT NULL,
        report_type NVARCHAR(60) NOT NULL,
        reason NVARCHAR(300) NOT NULL,
        description NVARCHAR(1000) NULL,
        evidence_urls NVARCHAR(MAX) NULL,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_hishop_report_status DEFAULT N'PENDING',
        admin_id INT NULL,
        admin_name NVARCHAR(100) NULL,
        handle_opinion NVARCHAR(500) NULL,
        handle_result NVARCHAR(500) NULL,
        create_time DATETIME2 NOT NULL CONSTRAINT DF_hishop_report_create_time DEFAULT SYSDATETIME(),
        update_time DATETIME2 NOT NULL CONSTRAINT DF_hishop_report_update_time DEFAULT SYSDATETIME(),
        handle_time DATETIME2 NULL
    );
END
GO

IF COL_LENGTH('dbo.hishop_report', 'reporter_role') IS NULL
    ALTER TABLE dbo.hishop_report ADD reporter_role NVARCHAR(20) NOT NULL CONSTRAINT DF_hishop_report_reporter_role DEFAULT N'USER';
IF COL_LENGTH('dbo.hishop_report', 'reporter_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD reporter_id INT NOT NULL CONSTRAINT DF_hishop_report_reporter_id DEFAULT 0;
IF COL_LENGTH('dbo.hishop_report', 'reporter_name') IS NULL
    ALTER TABLE dbo.hishop_report ADD reporter_name NVARCHAR(100) NULL;
IF COL_LENGTH('dbo.hishop_report', 'target_role') IS NULL
    ALTER TABLE dbo.hishop_report ADD target_role NVARCHAR(20) NOT NULL CONSTRAINT DF_hishop_report_target_role DEFAULT N'PRODUCT';
IF COL_LENGTH('dbo.hishop_report', 'target_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD target_id INT NOT NULL CONSTRAINT DF_hishop_report_target_id DEFAULT 0;
IF COL_LENGTH('dbo.hishop_report', 'target_name') IS NULL
    ALTER TABLE dbo.hishop_report ADD target_name NVARCHAR(160) NULL;
IF COL_LENGTH('dbo.hishop_report', 'merchant_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD merchant_id INT NULL;
IF COL_LENGTH('dbo.hishop_report', 'user_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD user_id INT NULL;
IF COL_LENGTH('dbo.hishop_report', 'order_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD order_id INT NULL;
IF COL_LENGTH('dbo.hishop_report', 'product_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD product_id INT NULL;
IF COL_LENGTH('dbo.hishop_report', 'review_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD review_id INT NULL;
IF COL_LENGTH('dbo.hishop_report', 'report_type') IS NULL
    ALTER TABLE dbo.hishop_report ADD report_type NVARCHAR(60) NOT NULL CONSTRAINT DF_hishop_report_report_type DEFAULT N'其他';
IF COL_LENGTH('dbo.hishop_report', 'reason') IS NULL
    ALTER TABLE dbo.hishop_report ADD reason NVARCHAR(300) NOT NULL CONSTRAINT DF_hishop_report_reason DEFAULT N'';
IF COL_LENGTH('dbo.hishop_report', 'description') IS NULL
    ALTER TABLE dbo.hishop_report ADD description NVARCHAR(1000) NULL;
IF COL_LENGTH('dbo.hishop_report', 'evidence_urls') IS NULL
    ALTER TABLE dbo.hishop_report ADD evidence_urls NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.hishop_report', 'status') IS NULL
    ALTER TABLE dbo.hishop_report ADD status NVARCHAR(20) NOT NULL CONSTRAINT DF_hishop_report_status_compat DEFAULT N'PENDING';
IF COL_LENGTH('dbo.hishop_report', 'admin_id') IS NULL
    ALTER TABLE dbo.hishop_report ADD admin_id INT NULL;
IF COL_LENGTH('dbo.hishop_report', 'admin_name') IS NULL
    ALTER TABLE dbo.hishop_report ADD admin_name NVARCHAR(100) NULL;
IF COL_LENGTH('dbo.hishop_report', 'handle_opinion') IS NULL
    ALTER TABLE dbo.hishop_report ADD handle_opinion NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishop_report', 'handle_result') IS NULL
    ALTER TABLE dbo.hishop_report ADD handle_result NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.hishop_report', 'create_time') IS NULL
    ALTER TABLE dbo.hishop_report ADD create_time DATETIME2 NOT NULL CONSTRAINT DF_hishop_report_create_time_compat DEFAULT SYSDATETIME();
IF COL_LENGTH('dbo.hishop_report', 'update_time') IS NULL
    ALTER TABLE dbo.hishop_report ADD update_time DATETIME2 NOT NULL CONSTRAINT DF_hishop_report_update_time_compat DEFAULT SYSDATETIME();
IF COL_LENGTH('dbo.hishop_report', 'handle_time') IS NULL
    ALTER TABLE dbo.hishop_report ADD handle_time DATETIME2 NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_report_status_time' AND object_id = OBJECT_ID(N'dbo.hishop_report'))
    EXEC(N'CREATE INDEX IX_hishop_report_status_time ON dbo.hishop_report(status, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_report_reporter' AND object_id = OBJECT_ID(N'dbo.hishop_report'))
    EXEC(N'CREATE INDEX IX_hishop_report_reporter ON dbo.hishop_report(reporter_role, reporter_id, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_report_target' AND object_id = OBJECT_ID(N'dbo.hishop_report'))
    EXEC(N'CREATE INDEX IX_hishop_report_target ON dbo.hishop_report(target_role, target_id);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_report_merchant' AND object_id = OBJECT_ID(N'dbo.hishop_report'))
    EXEC(N'CREATE INDEX IX_hishop_report_merchant ON dbo.hishop_report(merchant_id, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_report_user' AND object_id = OBJECT_ID(N'dbo.hishop_report'))
    EXEC(N'CREATE INDEX IX_hishop_report_user ON dbo.hishop_report(user_id, create_time DESC);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_report_order' AND object_id = OBJECT_ID(N'dbo.hishop_report'))
    EXEC(N'CREATE INDEX IX_hishop_report_order ON dbo.hishop_report(order_id, create_time DESC);');
GO

IF OBJECT_ID(N'dbo.hishop_punishment', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_punishment (
        punishment_id INT IDENTITY(1,1) PRIMARY KEY,
        report_id INT NULL,
        target_role NVARCHAR(20) NOT NULL,
        target_id INT NOT NULL,
        action_type NVARCHAR(50) NOT NULL,
        duration_days INT NULL,
        start_time DATETIME2 NOT NULL CONSTRAINT DF_hishop_punishment_start DEFAULT SYSDATETIME(),
        end_time DATETIME2 NULL,
        reason NVARCHAR(500) NULL,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_hishop_punishment_status DEFAULT N'ACTIVE',
        admin_id INT NULL,
        admin_name NVARCHAR(100) NULL,
        create_time DATETIME2 NOT NULL CONSTRAINT DF_hishop_punishment_create DEFAULT SYSDATETIME()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_punishment_target' AND object_id = OBJECT_ID(N'dbo.hishop_punishment'))
    EXEC(N'CREATE INDEX IX_hishop_punishment_target ON dbo.hishop_punishment(target_role, target_id, status, end_time);');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_hishop_punishment_report' AND object_id = OBJECT_ID(N'dbo.hishop_punishment'))
    EXEC(N'CREATE INDEX IX_hishop_punishment_report ON dbo.hishop_punishment(report_id, create_time DESC);');
GO

IF NOT EXISTS (SELECT 1 FROM dbo.hishop_report WHERE reason = N'演示举报记录')
BEGIN
    INSERT INTO dbo.hishop_report(reporter_role, reporter_id, reporter_name, target_role, target_id, target_name, merchant_id, user_id, product_id, report_type, reason, description, status)
    SELECT TOP 1 N'USER', u.id, u.username, N'PRODUCT', p.id, p.name, p.merchant_id, u.id, p.id, N'商品违规', N'演示举报记录', N'这是举报模块的演示数据，可重复执行脚本且不会重复插入。', N'PENDING'
    FROM dbo.hishopping_user u CROSS JOIN dbo.hishopping_product p
    ORDER BY u.id, p.id;
END
GO

IF OBJECT_ID(N'dbo.hishop_growth_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_growth_log (
        log_id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        growth_delta INT NOT NULL DEFAULT 0,
        points_delta INT NOT NULL DEFAULT 0,
        source_type NVARCHAR(40) NOT NULL,
        source_id INT NULL,
        remark NVARCHAR(500) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF EXISTS (
    SELECT 1
    FROM sys.stats
    WHERE name = N'UQ_hishop_growth_log_source'
      AND object_id = OBJECT_ID(N'dbo.hishop_growth_log')
)
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_hishop_growth_log_source'
      AND object_id = OBJECT_ID(N'dbo.hishop_growth_log')
)
    DROP STATISTICS dbo.hishop_growth_log.UQ_hishop_growth_log_source;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_hishop_growth_log_source'
      AND object_id = OBJECT_ID(N'dbo.hishop_growth_log')
)
    EXEC(N'CREATE UNIQUE INDEX UQ_hishop_growth_log_source ON dbo.hishop_growth_log(user_id, source_type, source_id) WHERE source_id IS NOT NULL;');
GO

IF OBJECT_ID(N'dbo.hishop_admin_operation_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_admin_operation_log (
        log_id INT IDENTITY(1,1) PRIMARY KEY,
        admin_id INT NOT NULL,
        operation_type NVARCHAR(50) NOT NULL,
        target_type NVARCHAR(50) NOT NULL,
        target_id INT NULL,
        content NVARCHAR(1000) NULL,
        operation_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.hishop_upload_resource', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hishop_upload_resource (
        resource_id INT IDENTITY(1,1) PRIMARY KEY,
        merchant_id INT NOT NULL,
        file_name NVARCHAR(200) NOT NULL,
        access_url NVARCHAR(500) NOT NULL,
        file_size BIGINT NOT NULL DEFAULT 0,
        upload_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        product_id INT NULL
    );
END
GO

UPDATE dbo.hishopping_product
SET merchant_id = (SELECT TOP 1 merchant_id FROM dbo.hishop_merchant WHERE merchant_code = N'0798682'),
    sale_status = N'ON_SALE',
    audit_status = N'APPROVED'
WHERE merchant_id IS NULL;
GO

MERGE dbo.hishop_coupon_template AS target
USING (VALUES
    (N'新人专享券',N'NEW_USER',5,1.00,39,N'NEW_USER',N'VIP1',1,9999,1,7,1,0,N'ENABLED'),
    (N'青铜会员券',N'VIP',5,1.00,59,N'VIP_LEVEL',N'2',2,9999,1,30,0,1,N'ENABLED'),
    (N'白银会员基础券',N'VIP',8,1.00,79,N'VIP_LEVEL',N'3',3,9999,1,30,0,1,N'ENABLED'),
    (N'黄金会员进阶券',N'VIP',20,1.00,199,N'VIP_LEVEL',N'4',4,9999,1,30,0,1,N'ENABLED'),
    (N'铂金会员专享券',N'VIP',50,1.00,399,N'VIP_LEVEL',N'5',5,9999,1,30,0,1,N'ENABLED'),
    (N'钻石会员大额券',N'VIP',100,1.00,699,N'VIP_LEVEL',N'6',6,9999,1,30,0,1,N'ENABLED'),
    (N'黑金折扣券',N'DISCOUNT',0,0.95,199,N'VIP_LEVEL',N'8',8,9999,1,30,0,1,N'ENABLED'),
    (N'通用满减券',N'AMOUNT',10,1.00,99,N'ALL',N'ALL',1,9999,1,30,0,0,N'ENABLED')
) AS source(coupon_name,coupon_type,amount,discount_rate,min_amount,target_type,target_value,vip_level,total_quantity,per_user_limit,valid_days,is_new_user_coupon,is_vip_coupon,status)
ON target.coupon_name = source.coupon_name
WHEN MATCHED THEN UPDATE SET coupon_type=source.coupon_type, amount=source.amount, discount_rate=source.discount_rate, min_amount=source.min_amount, target_type=source.target_type, target_value=source.target_value, vip_level=source.vip_level, total_quantity=source.total_quantity, per_user_limit=source.per_user_limit, valid_days=source.valid_days, is_new_user_coupon=source.is_new_user_coupon, is_vip_coupon=source.is_vip_coupon, status=source.status, update_time=SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT(coupon_name,coupon_type,amount,discount_rate,min_amount,target_type,target_value,vip_level,total_quantity,per_user_limit,end_time,valid_days,is_new_user_coupon,is_vip_coupon,status)
    VALUES(source.coupon_name,source.coupon_type,source.amount,source.discount_rate,source.min_amount,source.target_type,source.target_value,source.vip_level,source.total_quantity,source.per_user_limit,DATEADD(DAY, source.valid_days, SYSDATETIME()),source.valid_days,source.is_new_user_coupon,source.is_vip_coupon,source.status);
GO

IF COL_LENGTH('dbo.hishopping_user', 'account_id') IS NULL
    ALTER TABLE dbo.hishopping_user ADD account_id NVARCHAR(8) NULL;
GO

IF COL_LENGTH('dbo.hishopping_user', 'vip_level') IS NULL
    ALTER TABLE dbo.hishopping_user ADD vip_level INT NOT NULL CONSTRAINT DF_hishopping_user_vip_level DEFAULT 1;
GO

IF COL_LENGTH('dbo.hishopping_user', 'growth_value') IS NULL
    ALTER TABLE dbo.hishopping_user ADD growth_value INT NOT NULL CONSTRAINT DF_hishopping_user_growth_value DEFAULT 0;
IF COL_LENGTH('dbo.hishopping_vip_rule', 'service_level') IS NULL
    ALTER TABLE dbo.hishopping_vip_rule ADD service_level NVARCHAR(50) NOT NULL CONSTRAINT DF_hishopping_vip_rule_service_level DEFAULT N'普通售后';
ALTER TABLE dbo.hishopping_vip_rule ALTER COLUMN point_rate DECIMAL(4,2) NOT NULL;
ALTER TABLE dbo.hishopping_vip_rule ALTER COLUMN benefit_desc NVARCHAR(500) NOT NULL;
GO

UPDATE dbo.hishopping_user
SET account_id = RIGHT('100000' + CAST(100000 + id AS VARCHAR(8)), CASE WHEN id < 900000 THEN 6 ELSE 8 END)
WHERE account_id IS NULL OR LTRIM(RTRIM(account_id)) = '';
GO

IF EXISTS (
    SELECT 1
    FROM sys.stats
    WHERE name = N'UQ_hishopping_user_account_id'
      AND object_id = OBJECT_ID(N'dbo.hishopping_user')
)
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_hishopping_user_account_id'
      AND object_id = OBJECT_ID(N'dbo.hishopping_user')
)
    DROP STATISTICS dbo.hishopping_user.UQ_hishopping_user_account_id;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_hishopping_user_account_id'
      AND object_id = OBJECT_ID(N'dbo.hishopping_user')
)
    EXEC(N'CREATE UNIQUE INDEX UQ_hishopping_user_account_id ON dbo.hishopping_user(account_id);');
GO

UPDATE dbo.hishopping_user
SET growth_value = CASE WHEN growth_value > 0 THEN growth_value ELSE points END;
GO

UPDATE dbo.hishopping_user
SET vip_level = CASE
    WHEN growth_value >= 24000 THEN 10
    WHEN growth_value >= 16000 THEN 9
    WHEN growth_value >= 11000 THEN 8
    WHEN growth_value >= 7000 THEN 7
    WHEN growth_value >= 4000 THEN 6
    WHEN growth_value >= 2000 THEN 5
    WHEN growth_value >= 1000 THEN 4
    WHEN growth_value >= 500 THEN 3
    WHEN growth_value >= 200 THEN 2
    ELSE 1
END;
GO

MERGE dbo.hishopping_vip_rule AS target
USING (VALUES
    (1,N'普通会员',0,199,1.00,0,1.00,N'普通售后',N'基础购物服务'),
    (2,N'青铜会员',200,499,0.98,1,1.00,N'普通售后',N'商品9.8折，每月1张优惠券'),
    (3,N'白银会员',500,999,0.97,2,1.10,N'普通售后',N'商品9.7折，每月2张优惠券，积分1.1倍累计'),
    (4,N'黄金会员',1000,1999,0.95,3,1.20,N'优先售后',N'商品9.5折，每月3张优惠券，积分1.2倍累计'),
    (5,N'铂金会员',2000,3999,0.93,4,1.30,N'优先售后',N'商品9.3折，每月4张优惠券，积分1.3倍累计'),
    (6,N'钻石会员',4000,6999,0.90,5,1.40,N'优先售后',N'商品9.0折，每月5张优惠券，积分1.4倍累计'),
    (7,N'星耀会员',7000,10999,0.88,6,1.50,N'高级售后',N'商品8.8折，每月6张优惠券，积分1.5倍累计'),
    (8,N'黑金会员',11000,15999,0.85,8,1.60,N'高级售后',N'商品8.5折，每月8张优惠券，积分1.6倍累计'),
    (9,N'至尊会员',16000,23999,0.82,10,1.80,N'专属售后',N'商品8.2折，每月10张优惠券，积分1.8倍累计'),
    (10,N'荣耀会员',24000,NULL,0.80,12,2.00,N'专属售后',N'商品8.0折，每月12张优惠券，积分2.0倍累计')
) AS source(vip_level,vip_name,min_growth,max_growth,discount,coupon_count,point_rate,service_level,benefit_desc)
ON target.vip_level = source.vip_level
WHEN MATCHED THEN UPDATE SET vip_name=source.vip_name,min_growth=source.min_growth,max_growth=source.max_growth,discount=source.discount,coupon_count=source.coupon_count,point_rate=source.point_rate,service_level=source.service_level,benefit_desc=source.benefit_desc
WHEN NOT MATCHED THEN INSERT(vip_level,vip_name,min_growth,max_growth,discount,coupon_count,point_rate,service_level,benefit_desc)
VALUES(source.vip_level,source.vip_name,source.min_growth,source.max_growth,source.discount,source.coupon_count,source.point_rate,source.service_level,source.benefit_desc);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.hishopping_admin WHERE admin_name = N'admin')
BEGIN
    INSERT INTO dbo.hishopping_admin(admin_name, password, real_name, status)
    VALUES(N'admin', N'123456', N'嗨购小组管理员', N'正常');
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.hishopping_user WHERE email = N'member@hishopping.com')
BEGIN
    INSERT INTO dbo.hishopping_user(username, email, phone, password, role, points, vip_level, growth_value, status)
    VALUES(N'嗨购小组', N'member@hishopping.com', N'13800000000', N'123456', N'user', 8620, 7, 8620, N'正常');
END
GO

MERGE dbo.hishopping_category AS target
USING (VALUES
    (N'食品', N'食', N'奶粉、巧克力、牛奶与地方特产', 1),
    (N'家用电器', N'电', N'厨房电器、洗护电器与电脑数码设备', 2),
    (N'图书', N'书', N'古典文学、名著与儿童启蒙读物', 3)
) AS source(name, icon_text, description, sort_no)
ON target.name = source.name
WHEN MATCHED THEN
    UPDATE SET icon_text = source.icon_text, description = source.description, sort_no = source.sort_no
WHEN NOT MATCHED THEN
    INSERT(name, icon_text, description, sort_no)
    VALUES(source.name, source.icon_text, source.description, source.sort_no);
GO

MERGE dbo.hishopping_product AS target
USING (
    SELECT c.id AS category_id, v.name, v.short_desc, v.detail_desc, v.price, v.old_price, v.rating, v.sales, v.stock, v.tag, v.image_url, v.gradient, v.icon_text, v.color_options, v.spec_options, N'上架中' AS status
    FROM (VALUES
    (N'食品', N'德运高钙全脂成人牛奶粉', N'来自澳大利亚天然牧场，口感醇厚。', N'德运高钙全脂成人牛奶粉来自澳大利亚天然牧场，奶香浓郁，适合早餐、烘焙和日常营养补充。', 100.00, 128.00, 4.8, 1360, 100, N'营养', N'assets/img/catalog/food1.jpg', N'linear-gradient(135deg,#f59e0b,#fbbf24)', N'奶', N'原味,加钙,全脂', N'单罐装,双罐装,家庭装'),
    (N'食品', N'瑞士莲软心进口巧克力', N'精选多种口味，入口丝滑。', N'瑞士莲软心进口巧克力精选多种经典口味，外壳细腻，内馅柔滑，适合作为零食、礼物和下午茶搭配。', 50.00, 69.00, 4.7, 2200, 100, N'甜品', N'assets/img/catalog/food2.jpg', N'linear-gradient(135deg,#7c2d12,#f97316)', N'巧', N'牛奶味,榛仁味,黑巧味', N'分享装,礼盒装,家庭装'),
    (N'家用电器', N'苏泊尔电饭煲 4L', N'4L 容量，带可视窗口。', N'苏泊尔电饭煲采用 4L 大容量设计，满足家庭日常煮饭、煲粥和蒸煮需求。', 300.00, 399.00, 4.6, 890, 100, N'厨房', N'assets/img/catalog/equip1.jpg', N'linear-gradient(135deg,#94a3b8,#334155)', N'锅', N'白色,金色,深灰', N'基础款,预约款,智能款'),
    (N'家用电器', N'美的洗烘一体机', N'M-smart 智能家居系统，支持 WIFI 控制。', N'美的洗烘一体机集洗涤、脱水、烘干于一体，支持智能家居系统和 WIFI 控制。', 3000.00, 3599.00, 4.8, 760, 100, N'智能', N'assets/img/catalog/equip2.jpg', N'linear-gradient(135deg,#2563eb,#64748b)', N'洗', N'银色,白色,深灰', N'8KG,10KG,洗烘旗舰款'),
    (N'图书', N'《楚乔传》全三册', N'正版包邮，全三册经典文学读本。', N'《楚乔传》全三册收录完整故事篇章，适合休闲阅读、小说收藏和礼品赠送。', 50.00, 68.00, 4.9, 980, 100, N'文学', N'assets/img/catalog/book1.jpg', N'linear-gradient(135deg,#92400e,#d97706)', N'传', N'平装,精装,收藏版', N'单套,两套,礼盒套装'),
    (N'图书', N'红楼梦', N'中国古代四大名著之一。', N'《红楼梦》是中国古代四大名著之一，人物众多、结构宏大，适合课外阅读和文学鉴赏。', 60.00, 88.00, 4.9, 1680, 100, N'名著', N'assets/img/catalog/book2.jpg', N'linear-gradient(135deg,#dc2626,#f97316)', N'红', N'平装,精装,注释版', N'单本,上下册,阅读套装'),
    (N'图书', N'三国演义', N'阅读古典名著，品味经典文化。', N'《三国演义》讲述群雄逐鹿与英雄谋略，是中国古典章回小说代表作。', 45.00, 69.00, 4.8, 1510, 100, N'经典', N'assets/img/catalog/book3.jpg', N'linear-gradient(135deg,#1f2937,#ef4444)', N'三', N'平装,精装,青少版', N'单本,上下册,名著套装'),
    (N'图书', N'我不乱发脾气', N'3-6 岁亲子共读启蒙图书。', N'《我不乱发脾气》面向 3-6 岁儿童，通过故事引导孩子识别情绪、表达情绪，适合亲子共读。', 45.00, 58.00, 4.7, 880, 100, N'童书', N'assets/img/catalog/book4.jpg', N'linear-gradient(135deg,#22c55e,#06b6d4)', N'童', N'绘本版,注音版,礼盒版', N'单本,三本套装,亲子套装'),
    (N'家用电器', N'三星轻薄笔记本', N'16G 内存，超轻薄窄边框。', N'三星轻薄笔记本搭载 16G 内存和窄边框屏幕，机身轻薄便携，适合学习、办公、网课和影音娱乐。', 3000.00, 3699.00, 4.7, 1120, 100, N'办公', N'assets/img/catalog/equip3.jpg', N'linear-gradient(135deg,#4f46e5,#0f172a)', N'本', N'银色,灰色,蓝色', N'16G+512G,16G+1T,办公套装'),
    (N'食品', N'蒙牛特仑苏有机纯牛奶', N'250ml*24，家庭早餐常备。', N'蒙牛特仑苏有机纯牛奶规格为 250ml*24，奶味香醇，适合早餐、加餐和家庭囤货。', 150.00, 188.00, 4.8, 2460, 100, N'有机', N'assets/img/catalog/food3.jpg', N'linear-gradient(135deg,#38bdf8,#f8fafc)', N'奶', N'有机纯牛奶,低脂牛奶,梦幻盖', N'12盒,24盒,家庭箱'),
    (N'食品', N'楼兰蜜语红枣', N'500g*2袋，新疆特产。', N'楼兰蜜语红枣来自新疆产区，果肉饱满，甜度自然，适合直接食用、煲汤、泡茶和办公室零食。', 45.00, 59.00, 4.7, 1830, 100, N'特产', N'assets/img/catalog/food4.jpg', N'linear-gradient(135deg,#b91c1c,#f59e0b)', N'枣', N'原味,夹核桃,礼盒装', N'500g,500g*2袋,节日礼盒')
    ) AS v(category_name, name, short_desc, detail_desc, price, old_price, rating, sales, stock, tag, image_url, gradient, icon_text, color_options, spec_options)
    JOIN dbo.hishopping_category c ON c.name = v.category_name
) AS source
ON target.name = source.name
WHEN MATCHED THEN
    UPDATE SET
        category_id = source.category_id,
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
        spec_options = source.spec_options
WHEN NOT MATCHED THEN
    INSERT(category_id, name, short_desc, detail_desc, price, old_price, rating, sales, stock, tag, image_url, gradient, icon_text, color_options, spec_options, status)
    VALUES(source.category_id, source.name, source.short_desc, source.detail_desc, source.price, source.old_price, source.rating, source.sales, source.stock, source.tag, source.image_url, source.gradient, source.icon_text, source.color_options, source.spec_options, source.status);
GO

DECLARE @user_id INT;
SELECT @user_id = id FROM dbo.hishopping_user WHERE email = N'member@hishopping.com';

IF @user_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.hishopping_address WHERE user_id = @user_id)
    BEGIN
        INSERT INTO dbo.hishopping_address(user_id, receiver_name, phone, province, city, district, detail, is_default)
        VALUES(@user_id, N'嗨购小组', N'13800000000', N'江苏省', N'南京市', N'雨花台区', N'软件大道 88 号 1206 室', 1);
    END

    INSERT INTO dbo.hishopping_cart_item(user_id, product_id, quantity)
    SELECT @user_id, p.id, 1
    FROM dbo.hishopping_product p
    WHERE p.name IN (N'瑞士莲软心进口巧克力', N'《楚乔传》全三册')
      AND NOT EXISTS (
          SELECT 1 FROM dbo.hishopping_cart_item ci WHERE ci.user_id = @user_id AND ci.product_id = p.id
      );

    INSERT INTO dbo.hishop_user_coupon(coupon_id, user_id, coupon_name, coupon_type, amount, discount_rate, min_amount, vip_level, status, expire_time, issue_batch_no)
    SELECT t.coupon_id, @user_id, t.coupon_name, t.coupon_type, t.amount, t.discount_rate, t.min_amount, t.vip_level, N'UNUSED', DATEADD(DAY, t.valid_days, SYSDATETIME()), N'DEMO-SEED'
    FROM dbo.hishop_coupon_template t
    WHERE t.status = N'ENABLED'
      AND t.coupon_name IN (N'新人专享券', N'青铜会员券', N'通用满减券')
      AND NOT EXISTS (
          SELECT 1 FROM dbo.hishop_user_coupon uc WHERE uc.user_id = @user_id AND uc.coupon_id = t.coupon_id AND uc.status = N'UNUSED'
      );
END
GO

SELECT N'当前连接信息' AS info_name, @@SERVERNAME AS server_name, DB_NAME() AS database_name, SUSER_SNAME() AS login_name;

SELECT N'hishopping_admin' AS table_name, COUNT(*) AS row_count, N'管理员账号' AS detail FROM dbo.hishopping_admin
UNION ALL SELECT N'hishopping_user', COUNT(*), N'普通用户账号' FROM dbo.hishopping_user
UNION ALL SELECT N'hishopping_category', COUNT(*), N'购物主页分类' FROM dbo.hishopping_category
UNION ALL SELECT N'hishopping_product', COUNT(*), N'商品明细' FROM dbo.hishopping_product
UNION ALL SELECT N'hishopping_product_media', COUNT(*), N'商品媒体资源' FROM dbo.hishopping_product_media
UNION ALL SELECT N'hishopping_cart_item', COUNT(*), N'购物车明细' FROM dbo.hishopping_cart_item
UNION ALL SELECT N'hishop_merchant', COUNT(*), N'商家账号' FROM dbo.hishop_merchant
UNION ALL SELECT N'hishop_coupon_template', COUNT(*), N'优惠券模板' FROM dbo.hishop_coupon_template
UNION ALL SELECT N'hishop_user_coupon', COUNT(*), N'用户优惠券' FROM dbo.hishop_user_coupon
UNION ALL SELECT N'hishop_coupon_issue_log', COUNT(*), N'优惠券发放日志' FROM dbo.hishop_coupon_issue_log
UNION ALL SELECT N'hishopping_address', COUNT(*), N'收货地址' FROM dbo.hishopping_address
UNION ALL SELECT N'hishopping_order', COUNT(*), N'订单主表' FROM dbo.hishopping_order
UNION ALL SELECT N'hishopping_order_item', COUNT(*), N'订单商品明细' FROM dbo.hishopping_order_item;

SELECT id, admin_name, real_name, status, create_time FROM dbo.hishopping_admin ORDER BY id DESC;
SELECT id, account_id, username, email, phone, role, points, vip_level, growth_value, status, create_time FROM dbo.hishopping_user ORDER BY id DESC;
SELECT vip_level, vip_name, min_growth, max_growth, discount, coupon_count, point_rate, service_level, benefit_desc FROM dbo.hishopping_vip_rule ORDER BY vip_level;
SELECT id, name, icon_text, description, sort_no FROM dbo.hishopping_category ORDER BY sort_no, id;
SELECT id, name, price, old_price, rating, sales, stock, tag, image_url, status, sale_status, audit_status FROM dbo.hishopping_product ORDER BY id;
SELECT id, product_id, media_type, media_url, sort_no, cover_flag, create_time FROM dbo.hishopping_product_media ORDER BY product_id, sort_no, id;
SELECT id, user_id, product_id, quantity, selected_color, selected_spec, sku_id FROM dbo.hishopping_cart_item ORDER BY id DESC;
SELECT merchant_id, merchant_code, merchant_name, shop_name, status, create_time FROM dbo.hishop_merchant ORDER BY merchant_id DESC;
SELECT coupon_id, coupon_name, coupon_type, amount, discount_rate, min_amount, status FROM dbo.hishop_coupon_template ORDER BY coupon_id DESC;
