<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<section id="appPage" class="app-page hidden">
	<div class="app-shell">
		<aside class="sidebar">
			<div class="brand logo-brand sidebar-logo">
				<img class="brand-logo" src="assets/img/hishopping-logo.png" alt="嗨购商城">
			</div>
			<nav id="sideNav" class="nav"></nav>
		</aside>

		<main class="main-panel">
			<header class="topbar">
				<div>
					<h1 id="pageTitle">购物主页</h1>
				</div>
				<div class="search-box">
					<span>⌕</span>
					<input id="searchInput" placeholder="搜索商品、店铺、分类、游戏道具、虚拟商品">
					<div class="search-dropdown" id="searchDropdown"></div>
				</div>
				<div class="top-actions">
					<button class="icon-btn coupon-jump" type="button" title="我的优惠券" aria-label="我的优惠券"><img src="assets/img/top-coupon-icon.png?v=top-icon-20260601" alt=""><span id="couponBadge">0</span></button>
					<button class="icon-btn cart-jump" type="button" title="购物车" aria-label="购物车"><img src="assets/img/top-cart-icon.png?v=top-icon-20260601" alt=""><span id="cartBadge">0</span></button>
					<button class="user-chip" id="userChip" type="button">游客 <small>未登录</small></button>
					<button class="vip-entry-btn" id="vipEntryBtn" type="button" title="VIP会员"><img src="assets/img/vip-badge-1.png?v=vip-crop-20260531" alt="VIP会员"></button>
				</div>
			</header>
			<div id="pageRoot" class="page-content"></div>
		</main>
	</div>
	<button id="logoutBtn" class="logout-btn" type="button">返回登录/注册</button>
</section>

