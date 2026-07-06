# 🛒 HiShopping 嗨购商城

> 🎓 一个由在校学生完成的 Java Web 电商实践项目  
> 💡 一个由AI辅助完成的小组项目作品。

![Java](https://img.shields.io/badge/Java-8%2B-orange?style=flat-square)
![Servlet](https://img.shields.io/badge/Servlet-JSP-blue?style=flat-square)
![Database](https://img.shields.io/badge/Database-SQL%20Server-green?style=flat-square)
![Tomcat](https://img.shields.io/badge/Server-Tomcat-yellow?style=flat-square)
![Status](https://img.shields.io/badge/Status-Learning%20Project-brightgreen?style=flat-square)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

---

## 🙏 写在最前面

我是一名在校学生，这是我代表我们整个小组在 GitHub 上传的项目。

本项目开发过程中，我们借助AI工具辅助，提升基础编码效率。由于经验还在积累中，项目中一定还有很多不成熟、不完善、甚至可以被更优雅实现的地方。

真心希望大家能提出建议、指出问题，也欢迎交流改进思路！✨

---

## 📌 项目简介

**HiShopping 嗨购商城** 是一个基于 **Java Servlet / JSP** 的综合购物商城系统，围绕电商平台的常见业务流程进行设计与实现。

项目包含用户端、商家端和管理员端，尝试模拟一个较完整的线上购物平台，包括商品浏览、规格选择、购物车、订单、优惠券、VIP 成长体系、评价互动、举报处理、商家管理以及后台审核等功能。

---

## ✨ 主要功能

### 👤 用户端

- 用户注册、登录、验证码校验与会话管理
- 商品浏览、搜索、详情查看与规格选择
- 购物车管理
- 收藏商品
- 下单、订单管理与售后申请
- 收货地址管理
- 优惠券领取与使用
- 积分、成长值与 VIP 等级
- 商品评价、匿名评价、emoji 互动与图片/视频上传
- 商品、商家、订单和评价举报
- 我的举报记录与处理进度查看

### 🏪 商家端

- 商家注册、验证码校验与入驻申请
- 商品发布、规格配置、库存与上下架管理
- 商品图片上传
- 商家订单处理
- 优惠券管理
- 商家数据统计
- 举报管理：查看自己提交的举报，以及涉及本店商品、订单或评价的举报进度

### 🛠 管理员端

- 用户管理
- 商家审核
- 商品审核
- 商品、订单、优惠券管理
- 首页轮播图管理
- 账号资料、头像、注销与恢复申请审核
- 举报管理：筛选、查看并处理用户和商家提交的举报
- 平台运营数据统计

---

## 🧰 技术栈

- **后端**：Java、Servlet、JSP、JDBC
- **前端**：HTML、CSS、JavaScript
- **数据库**：SQL Server
- **服务器**：Tomcat
- **开发环境**：Eclipse / Java Web 项目结构

---

## 📁 项目结构

```text
src/                         Java 后端源码
WebContent/                  Web 页面、静态资源与配置文件
WebContent/assets/           CSS、JavaScript、图片资源
WebContent/sql/              数据库初始化脚本
WebContent/WEB-INF/web.xml   Web 应用配置
```

---

## 🚀 运行方式

1. 将项目导入 Eclipse 或其他 Java Web 开发环境。
2. 配置 Tomcat 服务器。
3. 执行 `WebContent/sql/hishopping.sql` 初始化数据库。
4. 根据本地数据库环境修改数据库连接配置。
5. 启动 Tomcat，访问项目首页。

---

## 🖼 素材说明

本项目中使用的部分图片、图标等素材来源于网络，仅用于学习交流和课程实践。

如有侵权，请联系删除。

联系方式：3840632919@qq.com

---

感谢你看到这里，本项目仍在优化完善中……期待你的想法和建议！🌱

---

<details>
<summary>English version</summary>

# 🛒 HiShopping

> 🎓 A Java Web e-commerce practice project created by students  
> 💡 A group project completed with AI assistance.

![Java](https://img.shields.io/badge/Java-8%2B-orange?style=flat-square)
![Servlet](https://img.shields.io/badge/Servlet-JSP-blue?style=flat-square)
![Database](https://img.shields.io/badge/Database-SQL%20Server-green?style=flat-square)
![Tomcat](https://img.shields.io/badge/Server-Tomcat-yellow?style=flat-square)
![Status](https://img.shields.io/badge/Status-Learning%20Project-brightgreen?style=flat-square)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

---

## 🙏 Before Everything

I am currently a student, and this is a project that I uploaded to GitHub on behalf of our whole group.

During development, we used AI tools as assistance to improve basic coding efficiency. Since I am still learning and gaining experience, there must be many parts that are not mature enough and can be improved.

Suggestions, feedback, issues, and improvement ideas are sincerely welcome! ✨

---

## 📌 About The Project

**HiShopping** is a comprehensive shopping mall system based on **Java Servlet / JSP**.

The project is designed around common e-commerce business workflows. It includes user-side, merchant-side, and administrator-side features, trying to simulate a relatively complete online shopping platform with product browsing, specification selection, shopping cart, orders, coupons, VIP growth, review interaction, report handling, merchant management, and admin review workflows.

---

## ✨ Main Features

### 👤 User Side

- User registration, login, captcha verification, and session management
- Product browsing, searching, detail pages, and specification selection
- Shopping cart management
- Product favorites
- Order creation, order management, and after-sales requests
- Address management
- Coupon collection and usage
- Points, growth value, and VIP levels
- Product reviews, anonymous reviews, emoji interaction, and image/video uploads
- Reporting products, merchants, orders, and reviews
- Viewing personal report records and handling progress

### 🏪 Merchant Side

- Merchant registration, captcha verification, and application
- Product publishing, specification configuration, stock management, and sale status management
- Product image upload
- Merchant order processing
- Coupon management
- Merchant analytics
- Report management: viewing reports submitted by the merchant and reports related to the merchant's products, orders, or reviews

### 🛠 Admin Side

- User management
- Merchant review
- Product review
- Product, order, and coupon management
- Homepage banner management
- Account profile, avatar, cancellation, and restoration request review
- Report management: filtering, viewing, and handling reports submitted by users and merchants
- Platform analytics

---

## 🧰 Tech Stack

- **Backend**: Java, Servlet, JSP, JDBC
- **Frontend**: HTML, CSS, JavaScript
- **Database**: SQL Server
- **Server**: Tomcat
- **Development Environment**: Eclipse / Java Web project structure

---

## 📁 Project Structure

```text
src/                         Java backend source code
WebContent/                  Web pages, static assets, and config files
WebContent/assets/           CSS, JavaScript, and image assets
WebContent/sql/              Database initialization script
WebContent/WEB-INF/web.xml   Web application configuration
```

---

## 🚀 How To Run

1. Import the project into Eclipse or another Java Web development environment.
2. Configure a Tomcat server.
3. Run `WebContent/sql/hishopping.sql` to initialize the database.
4. Update the database connection configuration according to your local environment.
5. Start Tomcat and open the project homepage.

---

## 🖼 Asset Notice

Some images, icons, and other assets used in this project are sourced from the Internet and are used only for learning, communication, and course practice.

If there is any infringement, please contact me for removal.

Contact: 3840632919@qq.com

---

Thank you for reading this far, this project is still being optimized and improved... I look forward to your thoughts and suggestions! 🌱

</details>
