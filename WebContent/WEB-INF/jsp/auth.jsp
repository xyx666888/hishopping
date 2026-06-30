<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<section id="authPage" class="auth-page">
	<div class="auth-intro">
		<div class="brand logo-brand">
			<img class="brand-logo" src="assets/img/hishopping-logo.png" alt="嗨购商城">
		</div>
		<h1>精选好物放心购，把日常所需轻松带回家。</h1>
		<p>严选食品、家电与图书好物，优惠下单、便捷收货，享受省心顺畅的购物体验。</p>
		<div class="feature-grid">
			<span class="feature-logo-card"><img src="assets/img/feature-quality-icon.png" alt=""><strong>商品优选</strong></span>
			<span class="feature-logo-card"><img src="assets/img/feature-discount-icon.png" alt=""><strong>优惠多多</strong></span>
			<span class="feature-logo-card"><img src="assets/img/feature-fast-icon.png" alt=""><strong>急速下单</strong></span>
		</div>
	</div>

	<div class="auth-panel">
		<aside class="auth-role-sidebar" aria-label="登录入口">
			<nav class="auth-role-nav">
				<button class="auth-role-btn active" data-auth-mode="user" type="button">
					<img src="assets/img/auth-user-role.png?v=role-user-20260601" alt="">
					<span>用户登录</span>
				</button>
				<button class="auth-role-btn" data-auth-mode="merchant" type="button">
					<img src="assets/img/auth-merchant-role.png?v=role-merchant-20260601" alt="">
					<span>商家登录</span>
				</button>
				<button class="auth-role-btn" data-auth-mode="admin" type="button">
					<img src="assets/img/admin-login-entry-icon.png?v=login-admin-icon-20260601" alt="">
					<span>管理员登录</span>
				</button>
			</nav>
		</aside>
		<div class="auth-card">
			<div class="lock-mark mascot-mark"><img id="authHeroIcon" src="assets/img/hishopping-mascot.png" alt="嗨购商城"></div>
			<h2 id="authTitle">用户登录</h2>
			<p id="authSubtitle">登录后继续探索精选商品</p>
			<form class="form" onsubmit="return false;">
				<label class="field register-only hidden">
					<span>用户名</span>
					<input id="usernameInput" type="text" placeholder="请输入用户名">
				</label>
				<label class="field">
					<span id="accountLabel">账号</span>
					<input id="accountInput" type="text" placeholder="请输入用户ID">
				</label>
				<label class="field register-only hidden">
					<span>手机号</span>
					<input id="phoneInput" type="tel" placeholder="请输入中国大陆手机号">
				</label>
				<div id="merchantRegisterFields" class="merchant-register-fields hidden">
					<label class="field"><span>联系人姓名</span><input id="merchantContactName" type="text" placeholder="请输入联系人姓名"></label>
					<label class="field"><span>联系电话</span><input id="merchantContactPhone" type="tel" placeholder="请输入联系电话"></label>
					<label class="field"><span>联系邮箱</span><input id="merchantEmail" type="email" placeholder="merchant@hishopping.com"></label>
					<label class="field"><span>店铺名称</span><input id="merchantShopName" type="text" placeholder="请输入店铺名称"></label>
					<label class="field"><span>经营类目</span><input id="merchantCategory" type="text" placeholder="食品 / 家电 / 图书"></label>
					<label class="field"><span>营业地址</span><input id="merchantAddress" type="text" placeholder="请输入营业地址"></label>
					<label class="field"><span>店铺简介</span><input id="merchantDesc" type="text" placeholder="一句话介绍店铺"></label>
				</div>
				<label class="field">
					<span>密码</span>
					<input id="passwordInput" type="password" placeholder="请输入密码">
				</label>
				<button class="primary-btn full" id="enterBtn" type="button">登录进入</button>
				<p id="formMessage" class="form-message"></p>
			</form>
			<p class="switch-text"><span id="switchHint">还没有账号？</span><button id="switchAuth" type="button">立即注册</button></p>
			<div id="merchantProgressBox" class="merchant-progress-box hidden">
				<div class="merchant-progress-query">
					<input id="merchantProgressInput" type="text" placeholder="输入注册邮箱或手机号查询进度">
					<button id="merchantProgressBtn" type="button">查询注册进度</button>
				</div>
				<p id="merchantProgressCountdown" class="merchant-progress-countdown hidden"></p>
				<div id="merchantProgressResult" class="merchant-progress-result"></div>
			</div>
		</div>
	</div>
</section>

