var state = {
	authType: "login",
	authMode: "user",
	page: "home",
	user: null,
	admin: null,
	merchant: null,
	products: [],
	categories: [],
	selectedProduct: null,
	detailReturnPage: "home",
	selectedCategoryId: "all",
	activeFeed: "recommend",
	selectedColor: "",
	selectedSpec: "",
	selectedSkuValues: {},
	detailQuantity: 1,
	detailMediaIndex: 0,
	searchKeyword: "",
	cart: [],
	selectedCartItemIds: {},
	orders: [],
	adminUsers: [],
	adminOrders: [],
	adminAddresses: [],
	adminUserCoupons: [],
	merchants: [],
	merchantProducts: [],
	merchantOrders: [],
	merchantOrderStatusFilter: "all",
	adminAuditProducts: [],
	couponTemplates: [],
	userCoupons: [],
	couponLogs: [],
	hallBanners: [],
	hallSlideIndex: 0,
	messages: [],
	conversations: [],
	activeConversationId: null,
	messageTargets: [],
	friendSearchResults: [],
	friendRequests: [],
	messageTargetRole: "",
	messageSearchKeyword: "",
	chatModal: null,
	chatSending: false,
	chatQuoteMessage: null,
	chatContextMenu: null,
	chatScrollToBottom: false,
	emojiPanelOpen: false,
	unreadMessages: 0,
	messageContacts: [],
	accountRequests: [],
	couponManageTab: "templates",
	couponIssueForm: { couponId: "", issueType: "VIP_LEVEL", targetValue: "2", batch: true },
	merchantCoupons: [],
	merchantCouponUsers: [],
	afterSales: [],
	growthLogs: [],
	adminLogs: [],
	favoriteIds: [],
	favorites: [],
	productReviews: [],
	reviewStats: { reviewCount: 0, likedCount: 0 },
	productReviewStats: { totalCount: 0, averageRating: 0, mediaCount: 0, starCounts: {} },
	reviewFilter: "all",
	reviewRatingFilter: 0,
	reviewDraftMedia: [],
	activeReviewReplyId: null,
	merchantAnalytics: null,
	adminAnalytics: null,
	merchantAuditFilter: "pending",
	merchantManageSearch: "",
	merchantExpandedId: null,
	merchantProductAuditFilter: "all",
	merchantProductSaleFilter: "all",
	adminSelectedUserId: null,
	adminUserSearch: "",
	adminUserStatusFilter: "all",
	adminUserVipFilter: "all",
	adminUserOrderFilter: "all",
	adminUserSort: "createDesc",
	adminUserDetailTab: "basic",
	adminUserOrderStatusFilter: "all",
	adminUserPasswordVisible: {},
	adminOrderStatusFilter: "all",
	adminOrderKeyword: "",
	adminSelectedMerchantId: null,
	reports: [],
	merchantMyReports: [],
	merchantRelatedReports: [],
	adminReports: [],
	adminReportStatusFilter: "all",
	adminReportKeyword: "",
	adminReportPage: 1,
	adminReportPageSize: 20,
	adminReportTotal: 0,
	adminReportModal: null,
	reportModal: null,
	addresses: [],
	selectedAddressId: null,
	coupons: [],
	selectedCouponId: null,
	selectedPlatformCouponId: null,
	selectedStackableCouponId: null,
	selectedMerchantCouponId: null,
	couponCenterCollapsed: false
};

var apiBasePath = String(window.HISHOPPING_CONTEXT_PATH || "").replace(/\/$/, "");

function apiUrl(url) {
	url = String(url || "");
	if (!url || /^(?:[a-z][a-z0-9+.-]*:)?\/\//i.test(url) || url.charAt(0) === "/") return url;
	return apiBasePath + "/" + url.replace(/^\/+/, "");
}

try {
	state.couponCenterCollapsed = localStorage.getItem("hishoppingCouponCenterCollapsed") === "1";
} catch (e) {}

function cloneCoupon(coupon) {
	var copy = {};
	for (var key in coupon) {
		if (Object.prototype.hasOwnProperty.call(coupon, key)) copy[key] = coupon[key];
	}
	return copy;
}

function cloneCoupons(coupons) {
	return coupons.map(cloneCoupon);
}

var couponCatalog = cloneCoupons(state.coupons);
var orderRefreshTimer = null;
var toastTimer = null;
var captchaVisible = false;

var userNavItems = [
	{ key: "home", label: "购物主页", icon: "assets/img/nav-home.png" },
	{ key: "cart", label: "购物车", icon: "assets/img/nav-cart.png" },
	{ key: "orders", label: "订单", icon: "assets/img/nav-orders.png" },
	{ key: "reports", label: "我的举报", icon: "assets/img/nav-message-icon.png" },
	{ key: "profile", label: "个人中心", icon: "assets/img/nav-profile.png" },
	{ key: "messages", label: "我的消息", icon: "assets/img/nav-message-icon.png" },
	{ key: "vip", label: "VIP中心", icon: "assets/img/nav-vip-center.png?v=nav-vip-tight-20260601" },
	{ key: "address", label: "地址管理", icon: "assets/img/nav-address.png" }
];

var merchantNavItems = [
	{ key: "merchantCenter", label: "商家首页", icon: "assets/img/auth-merchant-role.png" },
	{ key: "merchantProductList", label: "商品管理", icon: "assets/img/nav-detail.png" },
	{ key: "merchantOrders", label: "订单管理", icon: "assets/img/nav-orders.png" },
	{ key: "merchantReports", label: "举报管理", icon: "assets/img/nav-message-icon.png" },
	{ key: "merchantAnalytics", label: "数据分析", icon: "assets/img/nav-hall-display-icon.png" },
	{ key: "merchantCoupons", label: "店铺优惠券", icon: "assets/img/top-coupon-icon.png" },
	{ key: "merchantProfile", label: "店铺资料", icon: "assets/img/nav-profile.png" },
	{ key: "merchantMessages", label: "我的消息", icon: "assets/img/nav-message-icon.png" }
];

var adminNavItems = [
	{ key: "admin", label: "后台概览", icon: "assets/img/nav-admin.png" },
	{ key: "adminHall", label: "大厅展示", icon: "assets/img/nav-hall-display-icon.png" },
	{ key: "adminMessages", label: "我的消息", icon: "assets/img/nav-message-icon.png" },
	{ key: "adminAccountRequests", label: "资料审核", icon: "assets/img/nav-profile.png" },
	{ key: "adminMerchantAudit", label: "商家管理", icon: "assets/img/auth-merchant-role.png" },
	{ key: "adminUsers", label: "用户管理", icon: "assets/img/nav-profile.png" },
	{ key: "adminOrders", label: "订单管理", icon: "assets/img/nav-orders.png" },
	{ key: "adminReports", label: "举报管理", icon: "assets/img/nav-message-icon.png" },
	{ key: "adminAnalytics", label: "数据分析", icon: "assets/img/nav-hall-display-icon.png" },
	{ key: "adminCouponCenter", label: "优惠券管理", icon: "assets/img/top-coupon-icon.png" }
];

var pageTitles = {
	coupons: "我的优惠券",
	detail: "商品详情",
	favorites: "我的收藏",
	settings: "我的设置",
	messages: "我的消息",
	merchantMessages: "我的消息",
	adminMessages: "我的消息",
	adminHall: "大厅展示"
	, adminAccountRequests: "资料审核",
	merchantAnalytics: "数据分析",
	adminAnalytics: "数据分析",
	reports: "我的举报",
	merchantReports: "举报管理",
	adminReports: "举报管理"
};

function activeNavItems() {
	return state.admin ? adminNavItems : (state.merchant ? merchantNavItems : userNavItems);
}

function pageTitleText(page) {
	var labels = {
		home: "购物主页",
		cart: "购物车",
		orders: "我的订单",
		reports: "我的举报",
		profile: "个人中心",
		favorites: "我的收藏",
		coupons: "我的优惠券",
		vip: "VIP中心"
	};
	return labels[page] || page || "";
}

function positionText(position) {
	var text = String(position || "LEFT").toUpperCase();
	if (text === "CENTER") return "居中";
	if (text === "RIGHT") return "右侧";
	return "左侧";
}

function isAdminPage(page) {
	return adminNavItems.some(function(item) { return item.key === page; });
}

function isUserHiddenPage(page) {
	return page === "detail" || page === "favorites" || page === "settings";
}

function isMerchantPage(page) {
	if (page === "merchantProductAdd" || page === "merchantProductEdit") return true;
	return merchantNavItems.some(function(item) { return item.key === page; });
}

function money(value) {
	return "￥" + Number(value || 0).toFixed(0);
}

function regionData() {
	return (window.MAINLAND_REGION_DATA || []).slice().sort(function(a, b) {
		return a.name.localeCompare(b.name, "zh-Hans-u-co-pinyin");
	});
}

function regionCities(provinceName) {
	var province = regionData().filter(function(item) { return item.name === provinceName; })[0] || regionData()[0];
	return province ? province.cities.slice().sort(function(a, b) {
		return a.name.localeCompare(b.name, "zh-Hans-u-co-pinyin");
	}) : [];
}

function regionDistricts(provinceName, cityName) {
	var city = regionCities(provinceName).filter(function(item) { return item.name === cityName; })[0] || regionCities(provinceName)[0];
	return city ? city.districts.slice().sort(function(a, b) {
		return a.name.localeCompare(b.name, "zh-Hans-u-co-pinyin");
	}) : [];
}

function optionHtml(items, selectedName) {
	return items.map(function(item) {
		return '<option value="' + escapeHtml(item.name) + '" ' + (item.name === selectedName ? "selected" : "") + '>' + escapeHtml(item.name) + '</option>';
	}).join("");
}

function post(url, data) {
	return fetch(apiUrl(url), {
		method: "POST",
		headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
		body: new URLSearchParams(data || {})
	}).then(parseJsonResponse);
}

function get(url) {
	return fetch(apiUrl(url)).then(parseJsonResponse);
}

function needsRegisterCaptcha() {
	return state.authType !== "login" && (state.authMode === "user" || state.authMode === "merchant");
}

function refreshCaptcha() {
	var img = document.getElementById("captchaImage");
	if (!img) return;
	img.src = apiUrl("captcha") + "?t=" + Date.now() + Math.floor(Math.random() * 1000);
}

function clearCaptchaInput() {
	var input = document.getElementById("captchaInput");
	if (input) input.value = "";
}

function resetCaptcha() {
	clearCaptchaInput();
	refreshCaptcha();
}

function updateCaptchaView(show) {
	var field = document.getElementById("captchaField");
	if (!field) return;
	field.classList.toggle("hidden", !show);
	if (show && !captchaVisible) {
		resetCaptcha();
	}
	if (!show) {
		clearCaptchaInput();
	}
	captchaVisible = show;
}

function uploadFormData(url, formData, onProgress) {
	return new Promise(function(resolve, reject) {
		var xhr = new XMLHttpRequest();
		xhr.open("POST", apiUrl(url), true);
		xhr.upload.onprogress = function(e) {
			if (e.lengthComputable && onProgress) onProgress(Math.round(e.loaded * 100 / e.total));
		};
		xhr.onload = function() {
			try {
				resolve(JSON.parse(xhr.responseText || "{}"));
			} catch (e) {
				resolve({
					success: false,
					message: "服务器没有返回有效 JSON，HTTP " + xhr.status + "。返回内容：" + String(xhr.responseText || "空响应").replace(/\s+/g, " ").slice(0, 160)
				});
			}
		};
		xhr.onerror = function() { reject(new Error("上传接口未响应")); };
		xhr.send(formData);
	});
}

function parseJsonResponse(res) {
	return res.text().then(function(text) {
		try {
			return JSON.parse(text);
		} catch (e) {
			var preview = text ? text.replace(/\s+/g, " ").slice(0, 160) : "空响应";
			return {
				success: false,
				message: "服务器没有返回有效 JSON，HTTP " + res.status + "。返回内容：" + preview
			};
		}
	});
}

function badge(text, tone) {
	return '<span class="badge ' + (tone || "") + '">' + escapeHtml(text || "") + '</span>';
}

function escapeHtml(text) {
	return String(text == null ? "" : text).replace(/[&<>"']/g, function(ch) {
		return ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[ch];
	});
}

function productVisual(product, large) {
	var image = product.imageUrl ? ' style="background-image:url(' + product.imageUrl + ')"' : "";
	var bg = product.imageUrl ? "" : ' style="background:' + escapeHtml(product.gradient || "linear-gradient(135deg,#8b5cf6,#4f46e5)") + '"';
	var icon = product.imageUrl ? "" : '<div class="visual-icon">' + escapeHtml(product.iconText || "品") + '</div>';
	return '<div class="product-visual ' + (large ? "large" : "") + (product.imageUrl ? " has-image" : "") + '"' + (image || bg) + '>' +
		'<span class="visual-tag">' + escapeHtml(product.tag) + '</span>' + icon + '</div>';
}

function productThumb(product, sizeClass) {
	var cls = "product-thumb" + (sizeClass ? " " + sizeClass : "");
	if (product.imageUrl) {
		return '<div class="' + cls + ' has-image" style="background-image:url(' + escapeHtml(product.imageUrl) + ')"></div>';
	}
	return '<div class="' + cls + '" style="background:' + escapeHtml(product.gradient || "linear-gradient(135deg,#8b5cf6,#4f46e5)") + '"><span>' + escapeHtml(product.iconText || "品") + '</span></div>';
}

function productMediaList(product) {
    var list = (product && product.mediaList || []).filter(function(item) { return item && item.mediaUrl; }).slice(0, 6);
    if (!list.length && product && product.imageUrl) list.push({ mediaType: "IMAGE", mediaUrl: product.imageUrl, coverFlag: true, sortNo: 1 });
    return list;
}

function mediaFallbackHtml(className, text) {
    return '<span class="media-fallback ' + escapeHtml(className || "") + '">' + escapeHtml(text || "媒体加载失败") + '</span>';
}

function mediaErrorAttr(className, text) {
    var html = JSON.stringify(mediaFallbackHtml(className, text)).replace(/"/g, "&quot;");
    return ' onerror="this.outerHTML=' + html + '"';
}

function productMediaHtml(media, className) {
    if (!media || !media.mediaUrl) return mediaFallbackHtml(className, "暂无媒体");
    var type = String(media.mediaType || "IMAGE").toUpperCase();
    if (type === "VIDEO") return '<video class="' + className + '" src="' + escapeHtml(media.mediaUrl) + '" controls playsinline preload="metadata"' + mediaErrorAttr(className, "视频加载失败") + '></video>';
    return '<img class="' + className + '" src="' + escapeHtml(media.mediaUrl) + '" alt="商品媒体" loading="lazy"' + mediaErrorAttr(className, "图片加载失败") + '>';
}

function productMediaCarousel(product) {
    var mediaList = productMediaList(product);
    if (!mediaList.length) return productVisual(product, true);
    var index = Math.max(0, Math.min(state.detailMediaIndex || 0, mediaList.length - 1));
    var active = mediaList[index];
    var arrows = mediaList.length > 1 ? '<button class="product-media-arrow product-media-prev" type="button">&lsaquo;</button><button class="product-media-arrow product-media-next" type="button">&rsaquo;</button>' : "";
    var thumbs = mediaList.map(function(media, i) {
        var badge = String(media.mediaType || "IMAGE").toUpperCase() === "VIDEO" ? '<span>视频</span>' : "";
        return '<button class="product-media-thumb ' + (i === index ? "active" : "") + '" data-index="' + i + '" type="button">' + productMediaHtml(media, "product-media-thumb-img") + badge + '</button>';
    }).join("");
    return '<div class="product-media-carousel" data-index="' + index + '"><div class="product-media-stage">' + productMediaHtml(active, "product-media-main") + arrows + '</div><div class="product-media-thumbs">' + thumbs + '</div></div>';
}

function cartCount() {
	return state.cart.reduce(function(sum, item) { return sum + item.quantity; }, 0);
}

function couponCount() {
	return state.coupons.filter(function(coupon) { return coupon.claimed; }).length;
}

function applyCouponState() {
	state.coupons = [];
	state.selectedCouponId = null;
	state.selectedPlatformCouponId = null;
	state.selectedStackableCouponId = null;
	state.selectedMerchantCouponId = null;
}

function markCouponUsed(couponId) {
	state.selectedCouponId = null;
	return loadUserCoupons();
}

var vipRules = [
	{ level: 1, name: "普通会员", min: 0, next: 200, discount: 1, coupons: 0, pointRate: 1, serviceLevel: "普通售后", benefits: ["基础购物服务"] },
	{ level: 2, name: "青铜会员", min: 200, next: 500, discount: .98, coupons: 1, pointRate: 1, serviceLevel: "普通售后", benefits: ["商品 9.8 折", "每月 1 张优惠券", "基础会员优惠券"] },
	{ level: 3, name: "白银会员", min: 500, next: 1000, discount: .97, coupons: 2, pointRate: 1.1, serviceLevel: "普通售后", benefits: ["商品 9.7 折", "每月 2 张优惠券", "积分 1.1 倍累计"] },
	{ level: 4, name: "黄金会员", min: 1000, next: 2000, discount: .95, coupons: 3, pointRate: 1.2, serviceLevel: "优先售后", benefits: ["商品 9.5 折", "每月 3 张优惠券", "优先售后服务"] },
	{ level: 5, name: "铂金会员", min: 2000, next: 4000, discount: .93, coupons: 4, pointRate: 1.3, serviceLevel: "优先售后", benefits: ["商品 9.3 折", "每月 4 张优惠券", "优先发货提醒", "优先售后服务"] },
	{ level: 6, name: "钻石会员", min: 4000, next: 7000, discount: .9, coupons: 5, pointRate: 1.4, serviceLevel: "优先售后", benefits: ["商品 9.0 折", "每月 5 张优惠券", "生日专属礼券", "积分 1.4 倍累计"] },
	{ level: 7, name: "星耀会员", min: 7000, next: 11000, discount: .88, coupons: 6, pointRate: 1.5, serviceLevel: "高级售后", benefits: ["商品 8.8 折", "每月 6 张优惠券", "专属活动权益", "高级售后服务"] },
	{ level: 8, name: "黑金会员", min: 11000, next: 16000, discount: .85, coupons: 8, pointRate: 1.6, serviceLevel: "高级售后", benefits: ["商品 8.5 折", "每月 8 张优惠券", "黑金会员专区", "重点售后服务"] },
	{ level: 9, name: "至尊会员", min: 16000, next: 24000, discount: .82, coupons: 10, pointRate: 1.8, serviceLevel: "专属售后", benefits: ["商品 8.2 折", "每月 10 张优惠券", "高级客服服务", "新品优先体验"] },
	{ level: 10, name: "荣耀会员", min: 24000, next: null, discount: .8, coupons: 12, pointRate: 2, serviceLevel: "专属售后", benefits: ["商品 8.0 折", "每月 12 张优惠券", "全站尊享权益", "专属客服服务", "新品优先体验"] }
];

function growthValue(user) {
	return Math.max(0, Number(user && (user.growthValue != null ? user.growthValue : user.points) || 0));
}

function currentVip(user) {
	var growth = growthValue(user);
	for (var i = vipRules.length - 1; i >= 0; i--) {
		if (growth >= vipRules[i].min) return vipRules[i];
	}
	return vipRules[0];
}

function vipBadgeSrc(level) {
	return "assets/img/vip-badge-" + level + ".png?v=vip-crop-20260531";
}

function vipDiscountText(rule) {
	if (!rule || rule.discount >= 1) return "无折扣";
	var fold = rule.discount * 10;
	return (fold % 1 ? fold.toFixed(1) : fold.toFixed(0)) + " 折";
}

function vipDiscountAmount(total, rule) {
	if (!rule || rule.discount >= 1) return 0;
	return Math.max(0, Math.round(Number(total || 0) * (1 - rule.discount)));
}

function vipLevelLabel(rule) {
	return rule ? "VIP" + rule.level + " " + rule.name : "VIP1 普通会员";
}

function compactNumber(value) {
	var num = Number(value || 0);
	if (num >= 100000000) return Math.floor(num / 10000000) / 10 + "亿+";
	if (num >= 10000) return Math.floor(num / 1000) / 10 + "万+";
	return String(num);
}

function vipProgress(user) {
	var growth = growthValue(user);
	var vip = currentVip(user);
	var nextVip = vip.next ? vipRules.filter(function(item) { return item.level === vip.level + 1; })[0] : null;
	var progressMax = vip.next || growth || 1;
	return {
		growth: growth,
		growthText: compactNumber(growth),
		vip: vip,
		nextVip: nextVip,
		nextText: nextVip ? "距离 VIP" + nextVip.level + " " + nextVip.name + " 还差 " + compactNumber(Math.max(vip.next - growth, 0)) + " 成长值" : "已达到最高会员等级",
		progressMax: progressMax,
		progressText: nextVip ? compactNumber(growth) + " / " + compactNumber(progressMax) : "已满级",
		progressPct: vip.next ? Math.max(0, Math.min(100, ((growth - vip.min) / (vip.next - vip.min)) * 100)) : 100
	};
}

function orderOriginAmount(order) {
	if (Number(order.goodsAmount || 0) > 0) return Number(order.goodsAmount || 0);
	return Number(order.totalAmount || 0) + Number(order.discountAmount || 0);
}

function shipmentSummary(order) {
	var rows = order.shipments || [];
	if (!rows.length) return "";
	return rows.map(function(s) {
		return escapeHtml((s.expressCompany || "") + " " + (s.trackingNo || "")) + (s.shipTime ? " · " + escapeHtml(String(s.shipTime).slice(0, 19)) : "");
	}).join("<br>");
}

function afterSaleForItem(order, item) {
	var productId = item && item.product ? item.product.id : 0;
	var rows = order.afterSales || [];
	for (var i = 0; i < rows.length; i++) {
		if (String(rows[i].productId) === String(productId)) return rows[i];
	}
	return null;
}

function adminLogsForUser(userId) {
	return (state.adminLogs || []).filter(function(log) {
		if (String(log.targetType) === "USER" && String(log.targetId) === String(userId)) return true;
		return adminUserOrders(userId).some(function(order) {
			return String(log.targetType) === "ORDER" && String(log.targetId) === String(order.id);
		});
	});
}

function updateVipEntry() {
	var btn = document.getElementById("vipEntryBtn");
	if (!btn) return;
	if (state.admin) {
		btn.title = "后台管理";
		return;
	}
	var img = btn.querySelector("img");
	var rule = currentVip(state.user);
	if (img) {
		img.src = vipBadgeSrc(rule.level);
		img.alt = "VIP" + rule.level + " " + rule.name;
	}
	btn.title = "VIP" + rule.level + " " + rule.name;
}

function updateCouponBadge() {
	var badge = document.getElementById("couponBadge");
	if (badge) badge.textContent = couponCount();
}

function cartSubtotal(items) {
	return (items || state.cart).reduce(function(sum, item) { return sum + cartLineSubtotal(item); }, 0);
}

function syncCartSelection(selectNew) {
	var previous = state.selectedCartItemIds || {};
	var next = {};
	state.cart.forEach(function(item) {
		var key = String(item.id);
		next[key] = previous.hasOwnProperty(key) ? previous[key] : selectNew !== false;
	});
	state.selectedCartItemIds = next;
}

function selectedCartItems() {
	syncCartSelection(true);
	return state.cart.filter(function(item) {
		return !!state.selectedCartItemIds[String(item.id)];
	});
}

function selectedCartItemIds() {
	return selectedCartItems().map(function(item) { return item.id; });
}

function productSkus(product) {
	var skus = product && product.skuOptions && product.skuOptions.length ? product.skuOptions : [];
	if (skus.length) {
		return skus.map(function(sku) {
			var values = Array.isArray(sku.values) && sku.values.length ? sku.values : [sku.color || "默认", sku.spec || "标准"].filter(function(v) { return v; });
			return Object.assign({}, sku, {
				values: values,
				text: sku.text || values.join(" / "),
				color: sku.color || values[0] || "默认",
				spec: sku.spec || values[1] || "标准"
			});
		});
	}
	return [{
		skuId: "DEFAULT",
		values: [(product.colorOptions || [])[0] || "默认", (product.specOptions || [])[0] || "标准"],
		text: [((product.colorOptions || [])[0] || "默认"), ((product.specOptions || [])[0] || "标准")].join(" / "),
		color: (product.colorOptions || [])[0] || "默认",
		spec: (product.specOptions || [])[0] || "标准",
		price: Number(product.price || 0),
		oldPrice: Number(product.oldPrice || product.price || 0),
		stock: Number(product.stock || 0),
		enabled: true
	}];
}

function productAttrs(product) {
	if (product && product.skuAttrs && product.skuAttrs.length) return product.skuAttrs;
	var attrs = [];
	if (product && product.colorOptions && product.colorOptions.length) attrs.push({ name: "颜色/款式", values: product.colorOptions });
	if (product && product.specOptions && product.specOptions.length) attrs.push({ name: "规格", values: product.specOptions });
	if (!attrs.length) attrs.push({ name: "规格", values: ["默认"] });
	return attrs;
}

function productDisplayAttrs(product) {
	var raw = product && product.productAttrs || [];
	if (typeof raw === "string") {
		try { raw = JSON.parse(raw || "[]"); } catch (e) { raw = []; }
	}
	if (!Array.isArray(raw)) raw = [];
	return raw.map(function(attr) {
		return {
			name: String(attr && attr.name || "").trim(),
			value: String(attr && attr.value || "").trim()
		};
	}).filter(function(attr) {
		return attr.name && attr.value;
	}).slice(0, 20);
}

function skuText(sku) {
	if (!sku) return "";
	if (sku.text) return sku.text;
	if (sku.values && sku.values.length) return sku.values.join(" / ");
	return [sku.color, sku.spec].filter(function(v) { return v; }).join(" / ");
}

function selectedValuesForProduct(product, overrideIndex, overrideValue) {
	var attrs = productAttrs(product);
	return attrs.map(function(attr, index) {
		if (index === overrideIndex) return overrideValue;
		var current = state.selectedSkuValues && state.selectedSkuValues[index];
		return current || (attr.values && attr.values[0]) || "";
	});
}

function skuMatchesValues(sku, values, partial) {
	var skuValues = sku.values || [sku.color, sku.spec].filter(function(v) { return v; });
	for (var i = 0; i < values.length; i++) {
		if (!values[i] && partial) continue;
		if (String(skuValues[i] || "") !== String(values[i] || "")) return false;
	}
	return true;
}

function chooseSku(product, color, spec, skuId) {
	var skus = productSkus(product).filter(function(sku) { return sku.enabled !== false; });
	if (!skus.length) skus = productSkus(product);
	var found = skus.filter(function(sku) { return skuId && String(sku.skuId) === String(skuId); })[0];
	if (found) return found;
	var selected = selectedValuesForProduct(product);
	found = skus.filter(function(sku) { return skuMatchesValues(sku, selected, false); })[0];
	if (found) return found;
	found = skus.filter(function(sku) {
		return (!color || sku.color === color) && (!spec || sku.spec === spec);
	})[0];
	return found || skus[0] || productSkus(product)[0];
}

function currentDetailSku(product) {
	return chooseSku(product, state.selectedColor, state.selectedSpec, null);
}

function skuAvailable(product, color, spec) {
	var sku = chooseSku(product, color, spec, null);
	return sku && sku.enabled !== false && Number(sku.stock || 0) > 0;
}

function skuValueAvailable(product, index, value) {
	var values = selectedValuesForProduct(product, index, value);
	return productSkus(product).some(function(sku) {
		return sku.enabled !== false && Number(sku.stock || 0) > 0 && skuMatchesValues(sku, values, false);
	});
}

function cartLineUnitPrice(item) {
	return Number(item.skuPrice || (item.product && item.product.price) || 0);
}

function cartLineSubtotal(item) {
	return cartLineUnitPrice(item) * Number(item.quantity || 0);
}

function isFavorited(productId) {
	return (state.favoriteIds || []).some(function(id) { return String(id) === String(productId); });
}

function favoriteButton(productId, extraClass) {
	var active = isFavorited(productId);
	return '<button class="favorite-btn ' + (extraClass || "") + ' ' + (active ? "active" : "") + '" data-id="' + productId + '" type="button" title="' + (active ? "取消收藏" : "收藏商品") + '" aria-label="' + (active ? "取消收藏" : "收藏商品") + '">' +
		'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.84 4.61c-1.54-1.42-3.94-1.34-5.45.17L12 8.17 8.61 4.78C7.1 3.27 4.6 3.19 3.16 4.61c-1.65 1.63-1.61 4.24.08 5.93L12 19.29l8.76-8.75c1.69-1.69 1.73-4.3.08-5.93z"></path></svg>' +
		'</button>';
}

function couponAvailable(coupon, total, items) {
	if (!coupon.claimed || total < coupon.threshold || coupon.status && coupon.status !== "UNUSED") return false;
	if (coupon.couponOwnerType === "MERCHANT") return merchantSubtotal(coupon.merchantId, items) >= coupon.threshold;
	return true;
}

function couponDiscountAmount(coupon, total) {
	if (!coupon) return 0;
	if (coupon.couponType === "DISCOUNT") {
		var rate = Number(coupon.discountRate || 1);
		return Math.max(0, total * (1 - rate));
	}
	return Number(coupon.discount || 0);
}

function selectedCoupon(total, items) {
	return state.coupons.filter(function(coupon) {
		return coupon.id === state.selectedCouponId && couponAvailable(coupon, total, items);
	})[0] || null;
}

function merchantSubtotal(merchantId, items) {
	return (items || state.cart).reduce(function(sum, item) {
		return String(item.product.merchantId || 0) === String(merchantId || 0) ? sum + cartLineSubtotal(item) : sum;
	}, 0);
}

function platformCouponSelected(total, items) {
	return state.coupons.filter(function(coupon) {
		return coupon.id === state.selectedPlatformCouponId && coupon.couponOwnerType !== "MERCHANT" && !coupon.stackable && couponAvailable(coupon, total, items);
	})[0] || null;
}

function stackableCouponSelected(total, items) {
	return state.coupons.filter(function(coupon) {
		return coupon.id === state.selectedStackableCouponId && coupon.couponOwnerType !== "MERCHANT" && coupon.stackable && couponAvailable(coupon, total, items);
	})[0] || null;
}

function merchantCouponSelected(total, items) {
	return state.coupons.filter(function(coupon) {
		return coupon.id === state.selectedMerchantCouponId && coupon.couponOwnerType === "MERCHANT" && couponAvailable(coupon, total, items);
	})[0] || null;
}

function couponBusinessLabel(coupon) {
	if (coupon.couponOwnerType === "MERCHANT") return (coupon.shopName || "店铺") + "专属";
	if (coupon.stackable) return "平台可叠加";
	if (coupon.newUserCoupon || coupon.couponType === "NEW_USER") return "新人专享";
	if (coupon.vipCoupon || Number(coupon.vipLevel || 0) > 0) return "VIP专享";
	return "平台券";
}

function couponValueText(c) {
	if (!c) return "";
	if (c.couponType === "DISCOUNT") return (Number(c.discountRate || 1) * 10).toFixed(1) + "折";
	return "满" + Number(c.minAmount || 0).toFixed(0) + "减" + Number(c.amount || c.discount || 0).toFixed(0);
}

function couponOptionHtml(items, selectedId, total, emptyText, cartItems) {
	return '<option value="">' + emptyText + '</option>' + items.map(function(item) {
		var usable = couponAvailable(item, total, cartItems);
		var text = item.title + " · " + couponBusinessLabel(item) + (usable ? "" : "（未满足）");
		return '<option value="' + item.id + '" ' + (String(selectedId || "") === String(item.id) ? "selected" : "") + ' ' + (usable ? "" : "disabled") + '>' + escapeHtml(text) + '</option>';
	}).join("");
}

function selectedAddress() {
	return state.addresses.filter(function(address) {
		return String(address.id) === String(state.selectedAddressId);
	})[0] || state.addresses.filter(function(address) {
		return address.defaultAddress;
	})[0] || state.addresses[0] || null;
}

function addressUseScore(address) {
	var fullAddress = [address.province, address.city, address.district, address.detail].join("");
	return state.orders.filter(function(order) {
		var used = String(order.receiverAddress || "");
		return used && (used.indexOf(fullAddress) >= 0 || used.indexOf(address.detail || "") >= 0);
	}).length;
}

function sortedCheckoutAddresses() {
	return state.addresses.slice().sort(function(a, b) {
		if (a.defaultAddress !== b.defaultAddress) return a.defaultAddress ? -1 : 1;
		var scoreDiff = addressUseScore(b) - addressUseScore(a);
		if (scoreDiff !== 0) return scoreDiff;
		return Number(b.id || 0) - Number(a.id || 0);
	});
}

function productMatchesSearch(product) {
	var keyword = state.searchKeyword.trim().toLowerCase();
	if (!keyword) return true;
	return [product.name, product.categoryName, product.shortDesc, product.detailDesc, product.tag].join(" ").toLowerCase().indexOf(keyword) >= 0;
}

function adminMatches(values) {
	var keyword = state.searchKeyword.trim().toLowerCase();
	if (!keyword) return true;
	return values.join(" ").toLowerCase().indexOf(keyword) >= 0;
}

function avatarMarkup(url, fallback, cls) {
	var klass = cls || "avatar";
	if (url) return '<div class="' + klass + ' avatar-image"><img src="' + escapeHtml(url) + '" alt=""></div>';
	return '<div class="' + klass + '">' + escapeHtml(fallback || "人") + '</div>';
}

function updateUserChip() {
	var chip = document.getElementById("userChip");
	if (state.admin) {
		chip.innerHTML = avatarMarkup("", "管", "chip-avatar") + '<span>' + escapeHtml(state.admin.realName || state.admin.adminName) + " <small>管理员</small></span>";
		updateVipEntry();
		return;
	}
	if (state.merchant) {
		chip.innerHTML = avatarMarkup(state.merchant.avatarUrl, "商", "chip-avatar") + '<span>' + escapeHtml(state.merchant.shopName || state.merchant.merchantName) + " <small>" + escapeHtml(state.merchant.merchantCode) + "</small></span>";
		updateVipEntry();
		return;
	}
	if (state.user) {
		chip.innerHTML = avatarMarkup(state.user.avatarUrl, "人", "chip-avatar") + '<span>' + escapeHtml(state.user.username) + " <small>ID " + escapeHtml(state.user.accountId || state.user.id) + "</small></span>";
		updateVipEntry();
		return;
	}
	chip.innerHTML = "游客 <small>未登录</small>";
	updateVipEntry();
}

function updateShellForRole() {
	var appPage = document.getElementById("appPage");
	var search = document.getElementById("searchInput");
	if (appPage) {
		appPage.classList.toggle("admin-mode", !!state.admin);
		appPage.classList.toggle("merchant-mode", !!state.merchant);
	}
	if (search) search.placeholder = state.admin ? "搜索商品、用户、订单..." : (state.merchant ? "搜索本店商品..." : "搜索商品、订单、用户...");
	updateUserChip();
}

function scrollToCouponCenter() {
	setPage("coupons");
}

function scrollToProductCenter() {
	if (state.page !== "home") {
		setPage("home");
		setTimeout(scrollToProductCenter, 120);
		return;
	}
	var productCenter = document.getElementById("productCenter");
	if (productCenter) productCenter.scrollIntoView({ behavior: "smooth", block: "start" });
}

function showMessage(text, ok) {
	var el = document.getElementById("formMessage");
	el.innerHTML = escapeHtml(text || "");
	el.className = "form-message " + (ok ? "ok" : "");
}

function showMerchantRegisterSuccess(data) {
	var merchant = data.merchant || {};
	var el = document.getElementById("formMessage");
	el.className = "form-message merchant-register-success";
	el.innerHTML = '<strong>商家注册申请已提交</strong><span>商家独立ID：<b>' + escapeHtml(merchant.merchantCode || "") + '</b></span><small>请保存该ID，管理员审核通过后可用它登录商家后台。此提示将保留 10 秒。</small>';
}

function showMerchantProgressCountdown(seconds) {
	var el = document.getElementById("merchantProgressCountdown");
	var left = seconds || 5;
	if (window.merchantProgressCountdownTimer) clearInterval(window.merchantProgressCountdownTimer);
	function render() {
		el.className = "merchant-progress-countdown";
		el.innerHTML = "查询结果将在 <b>" + left + "</b> 秒后收起，并返回商家登录状态。";
	}
	render();
	window.merchantProgressCountdownTimer = setInterval(function() {
		left -= 1;
		if (left <= 0) {
			clearInterval(window.merchantProgressCountdownTimer);
			window.merchantProgressCountdownTimer = null;
			state.authMode = "merchant";
			state.authType = "login";
			document.getElementById("merchantProgressInput").value = "";
			document.getElementById("merchantProgressResult").innerHTML = "";
			el.classList.add("hidden");
			el.innerHTML = "";
			updateAuthView();
			return;
		}
		render();
	}, 1000);
}

function showToast(text) {
	var toast = document.getElementById("toastMessage");
	if (!toast) {
		toast = document.createElement("div");
		toast.id = "toastMessage";
		toast.className = "toast-message";
		document.body.appendChild(toast);
	}
	toast.textContent = text;
	toast.classList.add("show");
	clearTimeout(toastTimer);
	toastTimer = setTimeout(function() {
		toast.classList.remove("show");
	}, 1800);
}

function renderNav() {
	var html = activeNavItems().map(function(item) {
		var navCount = navBadgeCount(item.key);
		var suffix = navCount ? '<span>' + navCount + '</span>' : "";
		var iconClass = item.key.indexOf("Messages") >= 0 || item.key === "messages" ? " nav-icon-message" : "";
		return '<button type="button" class="' + (state.page === item.key ? "active" : "") + '" data-page="' + item.key + '">' +
			'<span class="nav-label"><img class="nav-icon' + iconClass + '" src="' + item.icon + '" alt="">' + item.label + '</span>' + suffix + '</button>';
	}).join("");
	document.getElementById("sideNav").innerHTML = html;
	Array.prototype.forEach.call(document.querySelectorAll("#sideNav button"), function(btn) {
		btn.onclick = function() { setPage(btn.getAttribute("data-page")); };
	});
}

function navBadgeCount(key) {
	var count = 0;
	if (key === "cart") count = cartCount();
	if (key === "messages" || key === "merchantMessages" || key === "adminMessages") count = state.unreadMessages || 0;
	if (key === "adminMerchantAudit") count = state.merchants.filter(function(m) { return m.status === "PENDING"; }).length + state.adminAuditProducts.filter(function(p) { return p.auditStatus === "PENDING"; }).length;
	if (count <= 0) return "";
	return count >= 99 ? "99+" : String(count);
}

function setPage(page) {
	if (page === "adminProducts" || page === "adminProductAudit") page = "adminMerchantAudit";
	if (isAdminPage(page) && !state.admin) {
		alert("请使用管理员账号 admin 登录后进入后台。");
		return;
	}
	if (isMerchantPage(page) && !state.merchant) {
		alert("请先使用审核通过的商家账号登录。");
		return;
	}
	if (state.admin && !isAdminPage(page)) page = "admin";
	if (state.merchant && !isMerchantPage(page)) page = "merchantCenter";
	if (!state.admin && isAdminPage(page)) page = "home";
	if (!state.merchant && isMerchantPage(page)) page = "home";
	state.page = page;
	try {
		sessionStorage.setItem("hishoppingPage", page);
	} catch (e) {
	}
	var item = activeNavItems().filter(function(nav) { return nav.key === page; })[0];
	document.getElementById("pageTitle").textContent = item ? item.label : (pageTitles[page] || "购物平台");
	updateShellForRole();
	renderNav();
	renderPage();
	loadPageData(page).then(renderPage).catch(function(err) {
		renderPage();
		showToast(err.message || "数据加载失败，请稍后重试。");
	});
	updateOrderRefresh();
}

function updateOrderRefresh() {
	if (orderRefreshTimer) {
		clearInterval(orderRefreshTimer);
		orderRefreshTimer = null;
	}
	if (state.user && state.page === "orders") {
		orderRefreshTimer = setInterval(function() {
			if (!state.user || state.page !== "orders") return;
			loadOrders().then(renderPage);
		}, 5000);
	}
}

function loadPageData(page) {
	if (page === "home") return Promise.all([loadProducts(), loadUserCoupons(), loadFavorites()]);
	if (page === "detail") return Promise.all([state.selectedProduct ? loadProductReviews(state.selectedProduct.id) : Promise.resolve(), state.user ? loadOrders() : Promise.resolve()]);
	if (page === "cart") return Promise.all([loadCart(), loadUserCoupons(), loadFavorites()]);
	if (page === "orders") return loadOrders();
	if (page === "reports") return loadReports();
	if (page === "address") return loadAddresses();
	if (page === "profile") return Promise.all([loadAccountRequests(false), loadReviewStats()]);
	if (page === "settings" || page === "merchantProfile") return loadAccountRequests(false);
	if (page === "merchantReports") return loadMerchantReports();
	if (page === "merchantAnalytics") return loadMerchantAnalytics();
	if (isMerchantPage(page)) return loadMerchantProducts();
	if (page === "adminHall") return loadHallBanners(true);
	if (page === "messages" || page === "merchantMessages" || page === "adminMessages") {
		return Promise.all([
			loadMessages(),
			state.user ? loadOrders() : Promise.resolve()
		]);
	}
	if (page === "adminAccountRequests") return loadAccountRequests(true);
	if (page === "adminReports") return loadAdminReports();
	if (page === "adminAnalytics") return loadAdminAnalytics();
	if (isAdminPage(page)) return loadAdminProducts();
	if (page === "coupons") return loadUserCoupons();
	if (page === "favorites") return loadFavorites();
	return Promise.resolve();
}

function loadProducts() {
	return get("products?feed=" + encodeURIComponent(state.activeFeed || "recommend")).then(function(data) {
		if (!data.success) throw new Error(data.message || "商品加载失败");
		state.categories = data.categories || [];
		state.products = data.products || [];
		state.selectedProduct = state.selectedProduct || state.products[0];
		syncDetailOptions();
	});
}

function loadProductReviews(productId) {
	if (!productId) {
		state.productReviews = [];
		state.productReviewStats = { totalCount: 0, averageRating: 0, mediaCount: 0, starCounts: {} };
		return Promise.resolve();
	}
	var query = "reviews?productId=" + encodeURIComponent(productId);
	if (state.reviewFilter === "media") query += "&filter=media";
	if (Number(state.reviewRatingFilter || 0) > 0) query += "&rating=" + encodeURIComponent(state.reviewRatingFilter);
	return get(query).then(function(data) {
		state.productReviews = data.success ? (data.reviews || []) : [];
		state.productReviewStats = data.success ? (data.stats || state.productReviewStats || {}) : { totalCount: 0, averageRating: 0, mediaCount: 0, starCounts: {} };
	});
}

function loadReviewStats() {
	if (!state.user) {
		state.reviewStats = { reviewCount: 0, likedCount: 0 };
		return Promise.resolve();
	}
	return get("reviews?action=myStats").then(function(data) {
		if (data.success) state.reviewStats = data.stats || { reviewCount: 0, likedCount: 0 };
	});
}

function loadMerchantAnalytics() {
	if (!state.merchant) return Promise.resolve();
	return get("merchant/analytics").then(function(data) {
		if (data.success) state.merchantAnalytics = data.analytics || {};
		else state.merchantAnalytics = {};
	});
}

function loadAdminAnalytics() {
	if (!state.admin) return Promise.resolve();
	var merchantId = state.adminSelectedMerchantId || 0;
	return get("admin/analytics?merchantId=" + encodeURIComponent(merchantId)).then(function(data) {
		if (data.success) state.adminAnalytics = data.analytics || {};
		else state.adminAnalytics = {};
	});
}

function loadHallBanners(adminMode) {
	return get(adminMode ? "admin/hallBanners" : "hallBanners").then(function(data) {
		if (data.success) state.hallBanners = data.banners || [];
		if (state.hallSlideIndex >= state.hallBanners.length) state.hallSlideIndex = 0;
	});
}

function loadMessages() {
	if (!state.user && !state.merchant && !state.admin) return Promise.resolve();
	return get("messages?action=conversations").then(function(data) {
		if (data.success) {
			state.conversations = data.conversations || [];
			state.unreadMessages = Number(data.unreadCount || 0);
			var hasActive = state.conversations.some(function(c) { return Number(c.conversationId) === Number(state.activeConversationId); });
			if (!hasActive) {
				state.messages = [];
				state.activeConversationId = null;
			}
			if (state.activeConversationId == null && state.conversations.length) {
				state.activeConversationId = state.conversations[0].conversationId;
			}
			if (state.activeConversationId != null) {
				return loadConversationMessages(state.activeConversationId);
			}
		}
	});
}

function loadConversationMessages(conversationId) {
	return get("messages?action=messages&conversationId=" + encodeURIComponent(conversationId)).then(function(data) {
		if (data.success) {
			state.activeConversationId = Number(conversationId);
			state.messages = data.messages || [];
			state.unreadMessages = Number(data.unreadCount || 0);
			state.chatScrollToBottom = true;
			return get("messages?action=conversations").then(function(convData) {
				if (convData.success) state.conversations = convData.conversations || [];
			});
		}
		alert(data.message || "消息加载失败");
	});
}

function loadMessageTargets() {
	var role = state.messageTargetRole || "";
	var keyword = state.messageSearchKeyword || "";
	return get("messages?action=targets&targetRole=" + encodeURIComponent(role) + "&keyword=" + encodeURIComponent(keyword)).then(function(data) {
		if (data.success) {
			state.messageTargets = data.targets || [];
		} else {
			alert(data.message || "联系人搜索失败");
		}
	});
}

function loadAccountRequests(adminMode) {
	if (!state.user && !state.merchant && !state.admin) return Promise.resolve();
	return get(adminMode ? "admin/accountRequests" : "accountRequests").then(function(data) {
		if (data.success) state.accountRequests = data.requests || [];
	});
}

function loadCart() {
	if (!state.user) return Promise.resolve();
	return get("cart").then(function(data) {
		if (data.success) {
			state.cart = data.cart || [];
			syncCartSelection(true);
		}
		document.getElementById("cartBadge").textContent = cartCount();
	});
}

function loadOrders() {
	if (!state.user) return Promise.resolve();
	return get("orders").then(function(data) {
		if (data.success) {
			state.orders = data.orders || [];
			state.afterSales = data.afterSales || state.afterSales || [];
			state.growthLogs = data.growthLogs || state.growthLogs || [];
		}
	});
}

function loadAddresses() {
	if (!state.user) return Promise.resolve();
	return get("addresses").then(function(data) {
		if (data.success) state.addresses = data.addresses || [];
		if (!selectedAddress()) {
			state.selectedAddressId = null;
		} else if (!state.selectedAddressId) {
			state.selectedAddressId = selectedAddress().id;
		}
	});
}

function loadFavorites() {
	if (!state.user) {
		state.favoriteIds = [];
		state.favorites = [];
		return Promise.resolve();
	}
	return get("favorites").then(function(data) {
		if (data.success) {
			state.favoriteIds = data.favoriteIds || [];
			state.favorites = data.favorites || [];
		}
	});
}

function loadAdminProducts() {
	return Promise.all([
		get("admin/products"),
		get("admin/users"),
		get("admin/orders"),
		get("admin/merchants"),
		get("admin/productAudit"),
		get("admin/coupons")
	]).then(function(results) {
		if (results[0].success) state.products = results[0].products || state.products;
		if (results[1].success) {
			applyAdminUserData(results[1]);
		}
		if (results[2].success && (!results[1].success || !(results[1].orders || []).length)) state.adminOrders = results[2].orders || [];
		if (results[2].success) {
			state.afterSales = results[2].afterSales || state.afterSales || [];
			state.adminLogs = results[2].adminLogs || state.adminLogs || [];
		}
		if (results[3].success) {
			state.merchants = results[3].merchants || [];
			state.adminAuditProducts = results[3].products || state.adminAuditProducts;
			state.categories = results[3].categories || state.categories;
		}
		if (results[4].success) state.adminAuditProducts = results[4].products || [];
		if (results[5].success) {
			state.couponTemplates = results[5].templates || [];
			state.couponLogs = results[5].logs || [];
		}
	});
}

function applyAdminUserData(data) {
	state.adminUsers = data.users || [];
	state.adminOrders = data.orders || state.adminOrders || [];
	state.adminAddresses = data.addresses || [];
	state.adminUserCoupons = data.userCoupons || [];
	state.couponTemplates = data.couponTemplates || state.couponTemplates || [];
	state.adminLogs = data.adminLogs || state.adminLogs || [];
	var selectedExists = state.adminUsers.some(function(user) { return String(user.id) === String(state.adminSelectedUserId); });
	if (!selectedExists) {
		state.adminSelectedUserId = state.adminUsers.length ? state.adminUsers[0].id : null;
	}
}

function applyAdminMerchantData(data) {
	state.merchants = data.merchants || state.merchants;
	state.adminAuditProducts = data.products || state.adminAuditProducts;
	state.categories = data.categories || state.categories;
}

function loadMerchantProducts() {
	return Promise.all([get("merchant/products"), get("merchant/coupons"), get("merchant/orders")]).then(function(results) {
		var data = results[0];
		if (data.success) {
			state.merchant = data.merchant || state.merchant;
			state.categories = data.categories || state.categories;
			state.merchantProducts = data.products || [];
		}
		if (results[1].success) {
			state.merchantCoupons = results[1].templates || [];
			state.merchantCouponUsers = results[1].userCoupons || [];
			state.couponLogs = results[1].logs || state.couponLogs;
		}
		if (results[2].success) {
			state.merchantOrders = results[2].orders || [];
			state.afterSales = results[2].afterSales || state.afterSales || [];
		}
	});
}

function loadUserCoupons() {
	if (!state.user) return Promise.resolve();
	return get("user/coupons").then(function(data) {
		if (data.success) {
			state.userCoupons = data.coupons || [];
			var dbCoupons = state.userCoupons.filter(function(c) { return c.status === "UNUSED"; }).map(function(c) {
				return {
					id: Number(c.userCouponId),
					userCouponId: Number(c.userCouponId),
					title: c.couponName,
					threshold: Number(c.minAmount || 0),
					discount: c.couponType === "DISCOUNT" ? 0 : Number(c.amount || 0),
					discountRate: Number(c.discountRate || 1),
					couponType: c.couponType,
					couponOwnerType: c.couponOwnerType || "PLATFORM",
					merchantId: Number(c.merchantId || 0),
					shopName: c.shopName || "",
					stackable: !!c.stackable,
					useScope: c.useScope || "ALL",
					description: c.description || "",
					vipLevel: Number(c.vipLevel || 0),
					desc: couponTypeText(c.couponType) + " · " + couponValueText(c),
					scope: couponScopeText(c),
					expireTime: c.expireTime || "",
					status: c.status || "",
					claimed: true
				};
			});
			var receivedTemplateIds = {};
			state.userCoupons.forEach(function(c) { receivedTemplateIds[c.couponId] = true; });
			var claimable = (data.templates || []).filter(function(t) {
				return t.status === "ENABLED" && !receivedTemplateIds[t.couponId];
			}).map(function(t) {
				return {
					id: "tpl-" + t.couponId,
					templateId: t.couponId,
					title: t.homeTitle || t.couponName,
					threshold: Number(t.minAmount || 0),
					discount: t.couponType === "DISCOUNT" ? 0 : Number(t.amount || 0),
					discountRate: Number(t.discountRate || 1),
					couponType: t.couponType,
					couponOwnerType: t.couponOwnerType || "PLATFORM",
					merchantId: Number(t.merchantId || 0),
					shopName: t.shopName || "",
					stackable: !!t.stackable,
					useScope: t.useScope || "ALL",
					description: t.description || t.homeSubtitle || "",
					vipLevel: Number(t.vipLevel || 0),
					desc: couponTypeText(t.couponType) + " · " + couponValueText(t),
					scope: couponScopeText(t),
					expireTime: t.endTime || "",
					status: t.status || "",
					claimed: false
				};
			});
			state.coupons = dbCoupons.concat(claimable);
			if (!state.coupons.some(function(coupon) { return coupon.id === state.selectedCouponId; })) {
				state.selectedCouponId = null;
			}
			if (!state.coupons.some(function(coupon) { return coupon.id === state.selectedPlatformCouponId; })) state.selectedPlatformCouponId = null;
			if (!state.coupons.some(function(coupon) { return coupon.id === state.selectedStackableCouponId; })) state.selectedStackableCouponId = null;
			if (!state.coupons.some(function(coupon) { return coupon.id === state.selectedMerchantCouponId; })) state.selectedMerchantCouponId = null;
		}
	});
}

function addToCart(productId, goCart, quantity) {
	if (!state.user) {
		alert("请先使用普通用户登录。");
		return Promise.resolve();
	}
	var product = (state.selectedProduct && String(state.selectedProduct.id) === String(productId)) ? state.selectedProduct : state.products.filter(function(p) { return String(p.id) === String(productId); })[0];
	var sku = product ? chooseSku(product, state.selectedColor, state.selectedSpec, null) : null;
	var qty = Math.max(1, Number(quantity || 1));
	return post("cart", {
		action: "add",
		productId: productId,
		selectedColor: sku ? sku.color : "",
		selectedSpec: sku ? sku.spec : "",
		skuId: sku ? sku.skuId : "DEFAULT",
		skuText: sku ? skuText(sku) : "",
		quantity: qty
	}).then(function(data) {
		if (!data.success) {
			alert(data.message || "加入购物车失败");
			return;
		}
		state.cart = data.cart || [];
		syncCartSelection(true);
		document.getElementById("cartBadge").textContent = cartCount();
		renderNav();
		showToast("已加入购物车");
		if (goCart) setPage("cart");
	});
}

function openDetail(productId, sourcePage) {
	return get("productDetail?id=" + encodeURIComponent(productId)).then(function(data) {
		if (data.success) {
			state.detailReturnPage = sourcePage || (state.page === "detail" ? state.detailReturnPage : state.page) || "home";
			state.selectedProduct = data.product;
			state.productReviews = data.reviews || [];
			state.productReviewStats = data.reviewStats || state.productReviewStats || {};
			state.reviewFilter = "all";
			state.reviewRatingFilter = 0;
			state.reviewDraftMedia = [];
			state.activeReviewReplyId = null;
			state.detailMediaIndex = 0;
			syncDetailOptions();
			setPage("detail");
			renderPage();
		}
	});
}

var smartFeeds = [
	{ key: "recommend", name: "推荐", iconText: "荐", description: "结合你的浏览、收藏和购买偏好", title: "为你推荐", subtitle: "按你的历史行为优先展示更可能喜欢的商品。" },
	{ key: "hot", name: "热门", iconText: "热", description: "销量和评分更高的热门商品", title: "热门商品", subtitle: "按销量、评分和热度展示当前更受欢迎的商品。" },
	{ key: "featured", name: "精选", iconText: "精", description: "高评分好评商品优先展示", title: "精选好物", subtitle: "优先展示评分 4 分以上、口碑更稳的商品。" },
	{ key: "discover", name: "发现", iconText: "发", description: "收藏和讨论热度更高的商品", title: "发现好物", subtitle: "按收藏、评论和销量发现更多值得看的商品。" }
];

function activeFeedInfo() {
	return smartFeeds.filter(function(feed) { return feed.key === state.activeFeed; })[0] || smartFeeds[0];
}

function productStatsLine(product) {
	var rating = Number(product.rating || product.averageRating || 0);
	var parts = [
		"销量 " + Number(product.sales || 0),
		"评分 " + (rating ? rating.toFixed(1) : "暂无"),
		"评论 " + Number(product.reviewCount || 0),
		"收藏 " + Number(product.favoriteCount || 0)
	];
	return parts.join(" · ");
}

function reviewableOrderOptions(productId) {
	var options = [];
	(state.orders || []).forEach(function(order) {
		if (order.status !== "已完成") return;
		(order.items || []).forEach(function(item) {
			if (!item.product || Number(item.product.id) !== Number(productId) || item.reviewed) return;
			options.push('<option value="' + order.id + '">' + escapeHtml(order.orderNo || ("订单 " + order.id)) + ' · ' + escapeHtml(item.snapshotName || item.product.name || "商品") + '</option>');
		});
	});
	return options.join("");
}

function renderProductReviews(product) {
	var reviews = state.productReviews || [];
	var stats = state.productReviewStats || {};
	var starCounts = stats.starCounts || {};
	var filters = [
		{ key: "all", rating: 0, label: "全部", count: Number(stats.totalCount || stats.reviewCount || 0) },
		{ key: "media", rating: 0, label: "有图/视频", count: Number(stats.mediaCount || 0) },
		{ key: "rating", rating: 5, label: "★★★★★", count: Number(starCounts["5"] || 0) },
		{ key: "rating", rating: 4, label: "★★★★", count: Number(starCounts["4"] || 0) },
		{ key: "rating", rating: 3, label: "★★★", count: Number(starCounts["3"] || 0) },
		{ key: "rating", rating: 2, label: "★★", count: Number(starCounts["2"] || 0) },
		{ key: "rating", rating: 1, label: "★", count: Number(starCounts["1"] || 0) }
	];
	var filterHtml = filters.map(function(item) {
		var active = (item.key === "media" && state.reviewFilter === "media") || (item.key === "all" && state.reviewFilter !== "media" && Number(state.reviewRatingFilter || 0) === 0) || (item.key === "rating" && Number(state.reviewRatingFilter || 0) === item.rating);
		return '<button class="review-filter-btn ' + (active ? "active" : "") + '" data-filter="' + item.key + '" data-rating="' + item.rating + '" type="button">' + item.label + ' <span>' + item.count + '</span></button>';
	}).join("");
	var rows = reviews.map(function(review) {
		var reviewId = review.reviewId || review.id;
		var media = (review.mediaList || []).map(function(item) {
			var url = escapeHtml(item.mediaUrl || "");
			return String(item.mediaType || "IMAGE").toUpperCase() === "VIDEO"
				? '<video class="review-video" src="' + url + '" controls preload="metadata"></video>'
				: '<button class="review-media-img" data-src="' + url + '" type="button"><img src="' + url + '" alt="评价图片"></button>';
		}).join("");
		var sku = [review.skuText, review.selectedColor, review.selectedSpec].filter(function(value, index, arr) {
			return value && arr.indexOf(value) === index;
		}).join(" / ");
		var replies = (review.replies || []).map(function(reply) {
			var role = roleName(reply.userType);
			var name = reply.userName || role;
			return '<div class="review-reply"><b>' + escapeHtml(role ? role + " · " + name : name) + '</b><span>' + escapeHtml(reply.content || "") + '</span><time>' + escapeHtml(shortDate(reply.createTime || "")) + '</time></div>';
		}).join("");
		var replyForm = state.activeReviewReplyId === reviewId ? '<form class="review-reply-form" data-id="' + reviewId + '"><textarea rows="2" placeholder="写下你的回复"></textarea><div><button class="ghost-btn review-reply-cancel" type="button">取消</button><button class="primary-btn" type="submit">发布回复</button></div></form>' : "";
		return '<article class="review-item">' +
			avatarMarkup(review.userAvatar, String(review.username || "匿").charAt(0), "review-avatar") +
			'<div class="review-body"><div class="review-head"><strong>' + escapeHtml(review.username || "匿名用户") + '</strong><span class="rating">' + reviewStars(review.rating) + '</span><time>' + escapeHtml(shortDate(review.createTime || "")) + '</time></div>' +
			(sku ? '<div class="review-sku">' + escapeHtml(sku) + '</div>' : '') +
			'<p>' + escapeHtml(review.content || "这位用户上传了图片/视频，没有填写文字评价。") + '</p>' +
			(media ? '<div class="review-media-grid">' + media + '</div>' : '') +
			'<div class="review-actions"><button class="ghost-btn review-like ' + (review.liked ? "active" : "") + '" data-id="' + reviewId + '" type="button">赞 <span>' + Number(review.likeCount || 0) + '</span></button><button class="ghost-btn review-reply-open" data-id="' + reviewId + '" type="button">回复</button><button class="ghost-btn open-report" data-target-role="REVIEW" data-target-id="' + reviewId + '" data-review-id="' + reviewId + '" data-product-id="' + (review.productId || (product && product.id) || 0) + '" data-report-type="恶意评价" type="button">举报评价</button></div>' +
			(replies ? '<div class="review-replies">' + replies + '</div>' : '') + replyForm + '</div></article>';
	}).join("") || '<div class="empty-cart compact-empty"><h3>暂无评价</h3><p class="muted">完成订单后可以发表真实评价，帮助其他用户判断这件商品是否适合自己。</p></div>';
	var options = state.user ? reviewableOrderOptions(product.id) : "";
	var draftMedia = (state.reviewDraftMedia || []).map(function(item, index) {
		var url = escapeHtml(item.mediaUrl || "");
		var preview = String(item.mediaType || "IMAGE").toUpperCase() === "VIDEO" ? '<video src="' + url + '" controls preload="metadata"></video>' : '<img src="' + url + '" alt="待提交媒体">';
		return '<div class="review-draft-media">' + preview + '<button class="review-media-remove" data-index="' + index + '" type="button">×</button></div>';
	}).join("");
	var form = "";
	if (state.user) {
		form = options ? '<form class="review-form" id="detailReviewForm"><label class="field"><span>评价订单</span><select id="detailReviewOrder">' + options + '</select></label><label class="field"><span>评分</span><select id="detailReviewRating"><option>5</option><option>4</option><option>3</option><option>2</option><option>1</option></select></label><label class="field wide"><span>评价内容</span><textarea id="detailReviewContent" rows="3" placeholder="分享真实体验，支持 emoji 😊"></textarea></label><div class="review-form-tools"><button class="review-emoji" data-emoji="😊" type="button">😊</button><button class="review-emoji" data-emoji="👍" type="button">👍</button><button class="review-emoji" data-emoji="✨" type="button">✨</button><label class="review-anonymous"><input id="detailReviewAnonymous" type="checkbox"> 匿名评价</label><label class="ghost-btn review-upload-btn">上传图片/视频<input id="detailReviewMedia" type="file" accept="image/jpeg,image/png,image/webp,image/gif,video/mp4,video/webm" multiple></label></div>' + (draftMedia ? '<div class="review-draft-grid">' + draftMedia + '</div>' : '') + '<button class="primary-btn" type="submit">提交评价</button></form>' : '<p class="muted review-hint">购买并完成该商品订单后，可以在这里或订单页发表评价。</p>';
	} else {
		form = '<p class="muted review-hint">登录后可以点赞、回复；购买并完成订单后可以发表评价。</p>';
	}
	return '<section class="panel-card product-review-panel"><div class="section-head"><div><h2>评论评分</h2><p>真实购买后的评价会参与商品评分和推荐排序。</p></div><div class="review-summary"><b>' + Number(stats.averageRating || product.rating || 0).toFixed(1) + '</b><span>' + Number(stats.totalCount || product.reviewCount || reviews.length || 0) + ' 条评价</span></div></div><div class="review-stats-row"><span>有图/视频 ' + Number(stats.mediaCount || 0) + '</span><span>★★★★★ ' + Number(starCounts["5"] || 0) + '</span><span>★★★★ ' + Number(starCounts["4"] || 0) + '</span><span>★★★及以下 ' + (Number(starCounts["3"] || 0) + Number(starCounts["2"] || 0) + Number(starCounts["1"] || 0)) + '</span></div><div class="review-filters">' + filterHtml + '</div>' + form + '<div class="review-list">' + rows + '</div></section>';
}

function reviewStars(rating) {
	var n = Math.max(1, Math.min(5, Number(rating || 5)));
	var html = "";
	for (var i = 1; i <= 5; i++) html += i <= n ? "★" : "☆";
	return html;
}

function syncDetailOptions() {
	var product = state.selectedProduct;
	if (!product) return;
	var sku = productSkus(product).filter(function(item) { return item.enabled !== false && Number(item.stock || 0) > 0; })[0] || productSkus(product)[0];
	state.selectedSkuValues = {};
	(sku && sku.values || []).forEach(function(value, index) {
		state.selectedSkuValues[index] = value;
	});
	state.selectedColor = sku ? sku.color : ((product.colorOptions || [])[0] || "");
	state.selectedSpec = sku ? sku.spec : ((product.specOptions || [])[0] || "");
	state.detailQuantity = 1;
}

function productBusinessBadges(product) {
	var tags = [];
	if (product.shopName) tags.push("店铺：" + product.shopName);
	tags.push(Number(product.stock || 0) > 0 ? "库存充足" : "库存不足");
	if ((state.coupons || []).some(function(c) { return c.status === "UNUSED" || c.claimed === false; })) tags.push("可用优惠券");
	if (state.user && currentVip(state.user).discount < 1) tags.push("适用VIP折扣");
	return tags.map(function(text, index) {
		return badge(text, index === 1 && Number(product.stock || 0) <= 0 ? "red" : (index === 0 ? "green" : "amber"));
	}).join(" ");
}

function renderHallHero(previewOnly) {
	var banners = (state.hallBanners || []).filter(function(item) { return item.enabled !== false; });
	if (!banners.length) {
		return '<div class="hero-card"><span class="badge">春夏焕新 · 满399减80</span><h2>精选好物，让购物体验更轻盈</h2><p>严选食品、家电与图书好物，优惠下单、便捷收货，日常所需安心带回家。</p><button class="primary-btn view-detail" data-id="1" type="button">查看爆款</button> <button class="ghost-btn coupon-btn" type="button">领取优惠券</button></div>';
	}
	var index = Math.max(0, Math.min(state.hallSlideIndex || 0, banners.length - 1));
	var banner = banners[index];
	var media = String(banner.mediaType || "IMAGE").toUpperCase() === "VIDEO"
		? '<video class="hall-media" src="' + escapeHtml(banner.mediaUrl) + '" ' + (banner.videoMutedDefault ? "muted " : "") + 'autoplay loop playsinline ' + (banner.videoDisablePause ? 'data-lock-pause="1" ' : '') + (banner.videoDisableSeek ? 'data-lock-seek="1" ' : '') + '></video>'
		: '<img class="hall-media" src="' + escapeHtml(banner.mediaUrl) + '" alt="">';
	var controls = banners.length > 1 ? '<button class="hall-arrow hall-prev" type="button">&lsaquo;</button><button class="hall-arrow hall-next" type="button">&rsaquo;</button><div class="hall-dots">' + banners.map(function(_, i) { return '<button class="' + (i === index ? "active" : "") + '" data-index="' + i + '" type="button"></button>'; }).join("") + '</div>' : "";
	var linkClass = banner.linkEnabled && !previewOnly ? " hall-clickable" : "";
	var titleStyle = ' style="color:' + escapeHtml(banner.titleColor || "#ffffff") + '"';
	var subtitleStyle = ' style="color:' + escapeHtml(banner.subtitleColor || "#e2e8f0") + '"';
	var pos = String(banner.textPosition || "LEFT").toLowerCase();
	var copy = banner.overlayEnabled === false ? "" : '<div class="hall-copy hall-copy-' + escapeHtml(pos) + '"><span class="badge">' + escapeHtml(banner.title || "大厅展示") + '</span><h2' + titleStyle + '>' + escapeHtml(banner.title || "精选好物正在展示") + '</h2><p' + subtitleStyle + '>' + escapeHtml(banner.subtitle || "后台可配置图片、视频和跳转规则。") + '</p></div>';
	var sound = !previewOnly && String(banner.mediaType || "IMAGE").toUpperCase() === "VIDEO" ? '<button class="hall-sound-toggle" type="button" title="切换视频声音">' + (banner.videoMutedDefault ? "静" : "声") + '</button>' : "";
	return '<div class="hall-carousel' + linkClass + (banner.overlayEnabled === false ? " no-overlay" : "") + '" data-index="' + index + '">' + media + copy + sound + controls + '</div>';
}

function renderHallStatsCard() {
	var onSaleCount = state.products.filter(function(p) { return p.status !== "下架" && p.saleStatus !== "OFF_SALE" && p.auditStatus !== "PENDING"; }).length;
	return '<div class="stats-card"><div class="section-head"><h3>今日数据</h3>' + badge("实时") + '</div>' +
		'<div class="stat-row"><span>在售商品</span><b>' + onSaleCount + '</b></div><div class="stat-row"><span>购物车商品</span><b>' + cartCount() + '</b></div><div class="stat-row"><span>可用优惠券</span><b>' + state.coupons.length + '</b></div></div>';
}

function renderHome() {
	var feedInfo = activeFeedInfo();
	var categoryHtml = smartFeeds.map(function(feed) {
		var active = String(state.activeFeed || "recommend") === feed.key;
		return '<button class="simple-card category-card smart-feed-card ' + (active ? "selected" : "") + '" data-feed="' + feed.key + '" type="button"><div class="icon-tile">' + escapeHtml(feed.iconText) + '</div><strong>' + escapeHtml(feed.name) + '</strong><span>' + escapeHtml(feed.title) + '</span><small>' + escapeHtml(feed.description) + '</small></button>';
	}).join("");

	var visibleProducts = state.products.filter(function(p) {
		return p.status !== "下架" && productMatchesSearch(p);
	});
	var productHtml = visibleProducts.map(function(product) {
		return '<article class="product-card">' +
			'<button class="plain visual-open" data-id="' + product.id + '" type="button">' + productVisual(product) + '</button>' +
			'<div class="product-title"><div><span class="category-label">' + escapeHtml(product.categoryName) + '</span><h4>' + escapeHtml(product.name) + '</h4></div>' + favoriteButton(product.id, "card-favorite") + '</div>' +
			'<div class="product-flags">' + productBusinessBadges(product) + '</div>' +
			'<p>' + escapeHtml(product.shortDesc) + '</p>' +
			'<p class="product-stat-line">' + escapeHtml(productStatsLine(product)) + '</p>' +
			'<div class="price-row"><div><span class="price">' + money(product.price) + '</span><span class="old-price">' + money(product.oldPrice) + '</span></div><span class="rating">★ ' + Number(product.rating || 0).toFixed(1) + '</span></div>' +
			'<div class="card-actions"><button class="primary-btn add-cart" data-id="' + product.id + '" type="button">加入购物车</button><button class="ghost-btn view-detail" data-id="' + product.id + '" type="button">查看</button></div>' +
			'</article>';
	}).join("") || '<div class="empty-cart"><h3>暂无匹配商品</h3><p class="muted">可以换个关键词或切回全部商品继续浏览。</p></div>';
	var couponHtml = state.coupons.map(function(coupon) {
		var button = coupon.claimed ? '<button class="primary-btn use-coupon" data-id="' + coupon.id + '" type="button">去使用</button>' : '<button class="primary-btn claim-coupon" data-id="' + coupon.templateId + '" type="button">领取</button>';
		return '<article class="coupon-card claimed"><div><strong>' + escapeHtml(coupon.title) + '</strong><span>' + escapeHtml(couponBusinessLabel(coupon) + " · " + (coupon.desc || "")) + '</span><small>' + escapeHtml(coupon.scope || "全场商品可用") + (coupon.expireTime ? " · 有效至 " + escapeHtml(shortDate(coupon.expireTime)) : "") + '</small></div>' + button + '</article>';
	}).join("") || '<div class="coupon-empty home-coupon-empty"><h3>暂无可用优惠券</h3></div>';
	var hasClaimableCoupon = state.coupons.some(function(coupon) { return !coupon.claimed; });
	var claimAllBtn = hasClaimableCoupon ? '<button class="primary-btn claim-all-coupons" type="button">一键领取</button>' : '<button class="ghost-btn claim-all-coupons" type="button" disabled>已全部领取</button>';
	var couponCollapsed = !!state.couponCenterCollapsed;
	var couponToggleBtn = '<button class="coupon-collapse-toggle ' + (couponCollapsed ? "is-collapsed" : "") + '" type="button" aria-expanded="' + (!couponCollapsed) + '" aria-controls="homeCouponGrid" title="' + (couponCollapsed ? "展开优惠券" : "收起优惠券") + '"><span>' + (couponCollapsed ? "展开" : "收起") + '</span><b>^</b></button>';
	var couponPanel = couponCollapsed ? '<div class="coupon-collapsed-note" id="homeCouponGrid"><span>优惠券列表已收起</span><strong>' + state.coupons.length + '</strong><small>张优惠券可查看</small></div>' : '<div class="coupon-grid home-coupon-grid" id="homeCouponGrid">' + couponHtml + '</div>';
	return '<section class="hero-grid">' +
		renderHallHero(false) +
		renderHallStatsCard() +
		'</section><section class="category-grid">' + categoryHtml + '</section>' +
		'<section class="coupon-strip ' + (couponCollapsed ? "collapsed" : "") + '" id="couponCenter"><div class="section-head coupon-section-head"><div><h3>优惠券中心</h3><p>后台发放后可在购物车结算时选择使用。</p></div><div class="coupon-head-actions">' + claimAllBtn + couponToggleBtn + '</div></div>' + couponPanel + '</section>' +
		'<section id="productCenter"><div class="section-head"><div><h3>' + escapeHtml(feedInfo.title) + '</h3><p>' + escapeHtml(feedInfo.subtitle) + '</p></div><button class="ghost-btn show-all-products" type="button">刷新推荐</button></div><div class="product-grid">' + productHtml + '</div></section>';
}

function renderProductDisplayAttrs(product) {
	var attrs = productDisplayAttrs(product);
	if (!attrs.length) return "";
	return '<section class="detail-product-attrs"><div class="detail-product-attrs-head"><strong>商品属性</strong><span>展示信息，不参与规格和库存计算</span></div><div class="detail-product-attrs-list">' + attrs.map(function(attr) {
		return '<p><strong>' + escapeHtml(attr.name) + '：</strong><span>' + escapeHtml(attr.value) + '</span></p>';
	}).join("") + '</div></section>';
}

function renderDetail() {
	var product = state.selectedProduct || state.products[0];
	if (!product) return '<div class="empty-cart"><h3>暂无商品</h3></div>';
	var sku = currentDetailSku(product);
	var stock = Number(sku && sku.stock || 0);
	if (state.detailQuantity > stock && stock > 0) state.detailQuantity = stock;
	if (state.detailQuantity < 1) state.detailQuantity = 1;
	var attrHtml = productAttrs(product).map(function(attr, index) {
		var buttons = (attr.values || []).map(function(name) {
			var available = skuValueAvailable(product, index, name);
			var selected = String(state.selectedSkuValues && state.selectedSkuValues[index] || "") === String(name);
			return '<button class="detail-option ' + (selected ? "selected" : "") + '" data-index="' + index + '" data-value="' + escapeHtml(name) + '" type="button" ' + (available ? "" : "disabled") + '>' + escapeHtml(name) + '</button>';
		}).join("");
		return '<div class="option-group"><strong>' + escapeHtml(attr.name || ("参数" + (index + 1))) + '</strong>' + buttons + '</div>';
	}).join("");
	var soldOut = !sku || sku.enabled === false || stock <= 0;
	var subtotal = Number(sku && sku.price || product.price || 0) * state.detailQuantity;
	var contactBtn = product.merchantId ? '<button class="ghost-btn detail-contact-merchant" data-merchant="' + product.merchantId + '" data-product="' + product.id + '" type="button">联系商家</button>' : '<button class="ghost-btn detail-contact-admin" data-product="' + product.id + '" type="button">联系平台</button>';
	var reportBtns = state.user ? '<button class="ghost-btn open-report" data-target-role="PRODUCT" data-target-id="' + product.id + '" data-product-id="' + product.id + '" data-merchant-id="' + (product.merchantId || 0) + '" data-report-type="商品违规" type="button">举报商品</button>' + (product.merchantId ? '<button class="ghost-btn open-report" data-target-role="MERCHANT" data-target-id="' + product.merchantId + '" data-merchant-id="' + product.merchantId + '" data-report-type="商家违规" type="button">举报商家</button>' : '') : "";
	return '<div class="detail-page-head"><button class="detail-back-btn" type="button" title="返回"><img src="assets/img/back-return.png" alt="返回"></button><div><h2>商品详情</h2><p>查看规格、库存、优惠和店铺信息。</p></div></div><div class="detail-grid"><div class="panel-card detail-media-panel">' + productMediaCarousel(product) +
		renderProductDisplayAttrs(product) + '<div class="detail-benefits"><span>正品保障</span><span>极速发货</span><span>7天无理由</span></div></div>' +
		'<div class="panel-card detail-info"><div class="detail-title-actions"><div>' + badge(product.tag) + ' ' + badge("销量 " + product.sales, "green") + ' ' + badge("评分 " + product.rating, "amber") + '</div>' + favoriteButton(product.id, "detail-favorite") + '</div>' +
		'<h2>' + escapeHtml(product.name) + '</h2><p class="muted">' + escapeHtml(product.detailDesc) + '</p>' +
		'<div class="product-flags detail-flags">' + productBusinessBadges(product) + '</div>' +
		'<div class="price-box"><span>当前单价</span><strong>' + money(sku ? sku.price : product.price) + '</strong> <span class="old-price">' + money(sku ? sku.oldPrice : product.oldPrice) + '</span><p class="muted">库存：' + stock + ' · 小计：' + money(subtotal) + '</p></div>' +
		attrHtml +
		'<div class="detail-purchase"><strong>数量</strong><div class="qty"><button class="detail-qty" data-delta="-1" type="button">-</button><b>' + state.detailQuantity + '</b><button class="detail-qty" data-delta="1" type="button" ' + (state.detailQuantity >= stock ? "disabled" : "") + '>+</button></div><span class="muted">已选：' + escapeHtml(skuText(sku) || "默认") + ' · ' + money(sku ? sku.price : product.price) + ' × ' + state.detailQuantity + ' = ' + money(subtotal) + '</span></div>' +
		(soldOut ? '<p class="muted sku-warning">当前规格暂无库存，请选择其他规格。</p>' : '') +
		'<div class="dual-actions"><button class="primary-btn detail-add-cart" data-id="' + product.id + '" type="button" ' + (soldOut ? "disabled" : "") + '>加入购物车</button><button class="ghost-btn detail-buy-now" data-id="' + product.id + '" type="button" ' + (soldOut ? "disabled" : "") + '>立即购买</button>' + contactBtn + reportBtns + '</div></div></div>' + renderProductReviews(product);
}

function renderCart() {
	syncCartSelection(true);
	var selectedItems = selectedCartItems();
	var total = cartSubtotal(selectedItems);
	var selectedCount = selectedItems.length;
	var allChecked = state.cart.length > 0 && selectedCount === state.cart.length;
	var vip = currentVip(state.user);
	var memberDiscount = vipDiscountAmount(total, vip);
	var platformCoupons = state.coupons.filter(function(c) { return c.couponOwnerType !== "MERCHANT" && !c.stackable; });
	var stackableCoupons = state.coupons.filter(function(c) { return c.couponOwnerType !== "MERCHANT" && c.stackable; });
	var merchantCoupons = state.coupons.filter(function(c) { return c.couponOwnerType === "MERCHANT"; });
	var platformCoupon = platformCouponSelected(total, selectedItems);
	var stackableCoupon = stackableCouponSelected(total, selectedItems);
	var merchantCoupon = merchantCouponSelected(total, selectedItems);
	var remaining = Math.max(total - memberDiscount, 0);
	var platformDiscount = Math.min(couponDiscountAmount(platformCoupon, total), remaining);
	remaining = Math.max(remaining - platformDiscount, 0);
	var stackableDiscount = Math.min(couponDiscountAmount(stackableCoupon, total), remaining);
	remaining = Math.max(remaining - stackableDiscount, 0);
	var merchantBase = merchantCoupon ? merchantSubtotal(merchantCoupon.merchantId, selectedItems) : 0;
	var merchantDiscount = Math.min(couponDiscountAmount(merchantCoupon, merchantBase), Math.min(merchantBase, remaining));
	var discount = memberDiscount + platformDiscount + stackableDiscount + merchantDiscount;
	var payable = Math.max(total - discount, 0);
	var expectedGrowth = selectedCount ? Math.floor(payable) + 20 : 0;
	var expectedPoints = Math.floor(Math.floor(payable) * vip.pointRate);
	var address = selectedAddress();
	var list = state.cart.length ? state.cart.map(function(item) {
		var product = item.product;
		var lineSubtotal = cartLineSubtotal(item);
		var checked = state.selectedCartItemIds[String(item.id)] ? "checked" : "";
		return '<article class="cart-item ' + (checked ? "selected" : "") + '"><label class="cart-check cart-item-check" title="选择商品"><input class="cart-select-input" data-cart="' + item.id + '" type="checkbox" ' + checked + '><span></span></label>' + productVisual(product) +
			'<div class="cart-item-info"><span class="badge">' + escapeHtml(product.categoryName) + '</span><h3>' + escapeHtml(product.name) + '</h3><p class="muted">' + escapeHtml(product.shortDesc) + '</p><p class="muted">' + escapeHtml(product.shopName || "平台自营") + '</p></div>' +
			'<div class="cart-item-actions"><div class="qty"><button class="qty-btn" data-cart="' + item.id + '" data-qty="' + (item.quantity - 1) + '" type="button">-</button><b>' + item.quantity + '</b><button class="qty-btn" data-cart="' + item.id + '" data-qty="' + (item.quantity + 1) + '" type="button">+</button></div><div class="cart-line-total"><span>小计</span><strong>' + money(lineSubtotal) + '</strong></div><button class="ghost-btn remove-btn" data-cart="' + item.id + '" type="button" style="margin-top:12px;">删除</button></div></article>';
	}).join("") : '<div class="empty-cart"><h3>购物车为空</h3><p class="muted">可从购物主页添加喜欢的商品。</p><button class="primary-btn go-products" type="button">添加商品</button></div>';
	if (state.cart.length) {
		list = '<div class="cart-select-bar"><label class="cart-check cart-check-all"><input id="cartSelectAll" type="checkbox" ' + (allChecked ? "checked" : "") + '><span></span><b>全选</b></label><small>已选 ' + selectedCount + ' 件商品</small></div>' + list;
	}
	var addressList = sortedCheckoutAddresses();
	var addressItems = addressList.map(function(addr) {
		var checked = address && String(address.id) === String(addr.id) ? "checked" : "";
		return '<label class="checkout-address ' + (checked ? "selected" : "") + '"><input type="radio" name="checkoutAddress" value="' + addr.id + '" ' + checked + '><span><b>' + escapeHtml(addr.receiverName) + ' ' + escapeHtml(addr.phone) + '</b><small>' + escapeHtml(addr.province + addr.city + addr.district + addr.detail) + '</small></span>' + (addr.defaultAddress ? badge("默认", "green") : "") + '</label>';
	}).join("");
	var addressHint = addressList.length > 3 ? '<p class="checkout-address-hint">默认地址优先展示，其余按常用地址排序，可在列表内滚动选择。</p>' : "";
	var addressHtml = state.addresses.length ? '<div class="checkout-address-list">' + addressItems + '</div>' + addressHint : '<div class="empty-address"><p class="muted">还没有收货地址，请先添加后再提交订单。</p><button class="ghost-btn go-address" type="button">添加地址</button></div>';
	return '<div class="cart-grid"><div class="cart-list">' + list + '</div><aside class="panel-card summary"><h3>订单结算</h3>' +
		'<div class="checkout-block"><h4>收货地址</h4>' + addressHtml + '</div><div class="summary-line"><span>商品金额</span><b>' + money(total) + '</b></div><div class="summary-line vip-summary-line"><span>当前会员</span><b>VIP' + vip.level + ' ' + escapeHtml(vip.name) + ' · ' + vipDiscountText(vip) + '</b></div><div class="summary-line"><span>VIP 优惠</span><b style="color:#059669;">-' + money(memberDiscount) + '</b></div><div class="summary-line"><span>运费</span><b>￥0</b></div>' +
		'<label class="field coupon-select"><span>平台普通券</span><select id="platformCouponSelect">' + couponOptionHtml(platformCoupons, state.selectedPlatformCouponId, total, "不使用平台券", selectedItems) + '</select></label><div class="summary-line"><span>平台券优惠</span><b style="color:#059669;">-' + money(platformDiscount) + '</b></div>' +
		'<label class="field coupon-select"><span>可叠加优惠券</span><select id="stackableCouponSelect">' + couponOptionHtml(stackableCoupons, state.selectedStackableCouponId, total, "无可叠加券", selectedItems) + '</select></label><div class="summary-line"><span>叠加券优惠</span><b style="color:#059669;">-' + money(stackableDiscount) + '</b></div>' +
		'<label class="field coupon-select"><span>商家优惠券</span><select id="merchantCouponSelect">' + couponOptionHtml(merchantCoupons, state.selectedMerchantCouponId, total, "无商家券", selectedItems) + '</select></label><div class="summary-line"><span>商家券优惠</span><b style="color:#059669;">-' + money(merchantDiscount) + '</b></div>' +
		'<div class="summary-line"><span>预计成长值</span><b>+' + expectedGrowth + '</b></div><div class="summary-line"><span>预计积分</span><b>+' + expectedPoints + '</b></div><div class="summary-total"><span>应付合计</span><strong>' + money(payable) + '</strong></div><button class="primary-btn full submit-order" type="button" ' + (selectedCount ? "" : "disabled") + '>提交订单</button></aside></div>';
}
function renderOrders() {
	if (!state.orders.length) {
		return '<div class="empty-cart"><h3>暂无订单</h3><p class="muted">提交购物车后，订单会出现在这里。</p></div>';
	}
	return state.orders.map(function(order) {
		var tone = order.status === "已完成" ? "green" : (order.status === "待付款" || order.status === "待发货" || order.status === "待收货" ? "amber" : "");
		var items = (order.items || []).map(function(item) {
			var name = item.snapshotName || (item.product && item.product.name) || "";
			var spec = item.skuText || [item.selectedColor, item.selectedSpec].filter(function(v) { return v; }).join(" / ");
			var subtotal = Number(item.subtotal || (Number(item.price || 0) * Number(item.quantity || 0)));
			var afterSale = afterSaleForItem(order, item);
			var afterSaleText = afterSale ? " · 售后：" + escapeHtml(afterSale.status || "") : "";
			return escapeHtml(name) + (spec ? "（" + escapeHtml(spec) + "）" : "") + " × " + item.quantity + " · 小计 " + money(subtotal) + afterSaleText;
		}).join("<br>");
		var payBtn = order.status === "待付款" ? '<button class="primary-btn pay-order" data-id="' + order.id + '" type="button">立即支付</button>' : "";
		var confirmBtn = order.status === "待收货" ? '<button class="primary-btn confirm-order" data-id="' + order.id + '" type="button">确认收货</button>' : "";
		var cancelBtn = order.status === "待付款" ? '<button class="ghost-btn cancel-order" data-id="' + order.id + '" type="button">取消订单</button>' : "";
		var eligibleAfterSaleItems = (order.items || []).filter(function(item) {
			return ["待发货", "待收货", "已完成"].indexOf(order.status) >= 0 && item.product && !afterSaleForItem(order, item);
		});
		var afterSaleBtns = eligibleAfterSaleItems.map(function(item, index) {
			var itemName = item.snapshotName || (item.product && item.product.name) || ("商品" + (index + 1));
			var shortName = itemName.length > 8 ? itemName.slice(0, 8) + "..." : itemName;
			var label = eligibleAfterSaleItems.length > 1 ? ("售后：" + shortName) : "申请售后";
			return '<button class="ghost-btn apply-after-sale" data-order="' + order.id + '" data-product="' + item.product.id + '" type="button" title="申请售后：' + escapeHtml(itemName) + '">' + escapeHtml(label) + '</button>';
		}).join("");
		var reviewBtns = (order.items || []).filter(function(item) { return order.status === "已完成" && item.product && !item.reviewed; }).map(function(item) { return '<button class="primary-btn review-product" data-order="' + order.id + '" data-product="' + item.product.id + '" type="button">评价商品</button>'; }).join("");
		var logistics = shipmentSummary(order);
		var batchText = order.batchNo ? '<p class="muted">同批次下单：' + escapeHtml(order.batchNo) + '</p>' : "";
		var consultMerchant = order.merchantId ? '<button class="ghost-btn order-contact-merchant" data-merchant="' + order.merchantId + '" data-order="' + order.id + '" type="button">咨询商家</button>' : "";
		var consultAdmin = '<button class="ghost-btn order-contact-admin" data-order="' + order.id + '" type="button">联系平台</button>';
		var reportOrderBtn = '<button class="ghost-btn open-report" data-target-role="ORDER" data-target-id="' + order.id + '" data-order-id="' + order.id + '" data-merchant-id="' + (order.merchantId || 0) + '" data-report-type="订单纠纷" type="button">举报订单</button>';
		return '<article class="order-card"><div class="order-top"><div><span class="muted">' + escapeHtml(order.orderNo) + '</span><h3>' + escapeHtml(order.shopName || "平台自营") + '</h3><p class="order-items-text">' + items + '</p>' + batchText + '<p class="muted">下单时间：' + escapeHtml(order.createTime) + '</p><p class="muted">收货地址：' + escapeHtml(order.receiverAddress) + '</p>' + (logistics ? '<p class="muted">物流信息：' + logistics + '</p>' : '') + '<p class="muted">商品总额：' + money(orderOriginAmount(order)) + ' · 优惠：-' + money(order.discountAmount || 0) + '</p></div><div style="text-align:right;">' + badge(order.status, tone) + '<div class="price" style="margin-top:10px;">' + money(order.totalAmount) + '</div></div></div><div class="order-actions">' + payBtn + confirmBtn + cancelBtn + consultMerchant + consultAdmin + afterSaleBtns + reviewBtns + reportOrderBtn + '<button class="ghost-btn rebuy-order" data-id="' + order.id + '" type="button">再次购买</button></div></article>';
	}).join("");
}

function renderProfile() {
	if (state.admin) {
		return '<div class="profile-grid"><div class="profile-card"><div class="avatar">管</div><h2>' + escapeHtml(state.admin.realName || state.admin.adminName) + '</h2><p class="muted">管理员账号 · 可维护商品、用户和订单</p><div class="profile-metrics"><div><b>' + state.products.length + '</b><span>商品</span></div><div><b>' + state.adminUsers.length + '</b><span>用户</span></div><div><b>' + state.adminOrders.length + '</b><span>订单</span></div></div></div><div class="profile-actions"><button class="simple-card profile-link" data-page="admin" type="button"><div class="icon-tile">管</div><strong>后台管理</strong><span>查看商品、用户资料和订单状态</span></button></div></div>';
	}
	var user = state.user || { username: "游客", points: 0 };
	var progress = vipProgress(user);
	var growth = progress.growth;
	var vip = progress.vip;
	var benefitList = vip.benefits.map(function(item) {
		return '<span>' + escapeHtml(item) + '</span>';
	}).join("");
	var actions = [
		["VIP中心", "查看等级规则、权益和成长方式", "vip"],
		["我的订单", "查看全部订单与物流状态", "orders"],
		["我的举报", "查看提交给平台的举报处理进度", "reports"],
		["我的收藏", "查看收藏商品，快速回到详情", "favorites"],
		["我的优惠券", "查看已领取优惠券和可用门槛", "coupons"],
		["地址管理", "维护收货地址与默认地址", "address"],
		["我的设置", "维护头像、资料、账号申请", "settings"]
	].map(function(item) {
		var icon = item[2] === "vip" ? "VIP" : (item[2] === "coupons" ? "券" : (item[2] === "favorites" ? "藏" : "设"));
		return '<button class="simple-card profile-link" data-page="' + item[2] + '" type="button"><div class="icon-tile">' + icon + '</div><strong>' + item[0] + '</strong><span>' + item[1] + '</span></button>';
	}).join("");
	return '<div class="profile-grid vip-profile-grid"><div class="profile-card vip-profile-card"><div class="vip-card-top">' + avatarMarkup(user.avatarUrl, "人", "profile-avatar") + '<div><span class="vip-level-pill">VIP' + vip.level + '</span><h2>' + escapeHtml(user.username) + '</h2><p>当前等级：' + vipLevelLabel(vip) + '</p></div></div><div class="vip-progress-head"><strong>' + progress.nextText + '</strong><span>' + progress.progressText + '</span></div><div class="vip-progress"><i style="width:' + progress.progressPct.toFixed(1) + '%"></i></div><div class="profile-metrics compact vip-metrics"><div><b>' + escapeHtml(user.accountId || user.id) + '</b><span>登录ID</span></div><div><b>' + progress.growthText + '</b><span>成长值</span></div><div><b>' + compactNumber(user.points || 0) + '</b><span>积分</span></div><div><b>' + vip.coupons + '</b><span>每月券数</span></div><div><b>' + Number(state.reviewStats.reviewCount || 0) + '</b><span>我的评价</span></div><div><b>' + Number(state.reviewStats.likedCount || 0) + '</b><span>评价获赞</span></div></div></div><div class="profile-actions compact"><article class="simple-card vip-benefit-card"><div class="icon-tile"><img src="' + vipBadgeSrc(vip.level) + '" alt="VIP' + vip.level + '"></div><strong>我的会员权益</strong><span>' + vipDiscountText(vip) + ' · 积分 ' + vip.pointRate + ' 倍 · ' + escapeHtml(vip.serviceLevel) + '</span><div class="vip-benefit-list">' + benefitList + '</div></article>' + actions + '</div></div>';
}

function renderSettings() {
	if (!state.user) return '<div class="empty-cart"><h3>请先登录</h3><p class="muted">登录后可维护账号设置。</p></div>';
	return '<section class="favorite-page settings-page"><div class="section-head"><div><h2>我的设置</h2><p>头像、资料、注销和恢复申请统一在这里提交，审核结果进入消息中心。</p></div><button class="ghost-btn profile-link" data-page="profile" type="button">返回个人中心</button></div>' + renderAccountRequestPanel("用户") + '</section>';
}

function renderFavorites() {
	if (!state.user) {
		return '<div class="empty-cart"><h3>请先登录</h3><p class="muted">登录普通用户账号后可查看收藏商品。</p></div>';
	}
	var rows = (state.favorites || []).map(function(favorite) {
		var product = favorite.product || {};
		var invalid = !product.id;
		var unavailable = invalid || product.status === "下架" || product.saleStatus === "OFF_SALE" || product.auditStatus === "REJECTED" || product.auditStatus === "PENDING";
		var statusBadge = invalid ? badge("商品已失效", "red") : (unavailable ? badge("暂不可买", "amber") : badge("可购买", "green"));
		var heart = invalid ? "" : favoriteButton(product.id, "favorite-inline");
		var actions = invalid ? '<button class="ghost-btn favorite-remove" data-id="' + favorite.productId + '" type="button">移除收藏</button>' :
			'<button class="ghost-btn view-detail" data-id="' + product.id + '" type="button">查看详情</button><button class="primary-btn add-cart" data-id="' + product.id + '" type="button" ' + (unavailable ? "disabled" : "") + '>加入购物车</button>';
		return '<article class="favorite-item">' + (invalid ? '<div class="favorite-invalid">已失效</div>' : productThumb(product, "favorite-thumb")) +
			'<div><div class="favorite-title-row"><h3>' + escapeHtml(product.name || "商品已失效") + '</h3>' + heart + '</div><p class="muted">' + escapeHtml(product.shortDesc || "该商品已删除或不可访问") + '</p><p class="muted">店铺：' + escapeHtml(product.shopName || "平台自营") + ' · 收藏时间：' + escapeHtml(shortDate(favorite.createTime)) + '</p><div class="product-flags">' + statusBadge + (product.stock != null ? badge("库存 " + product.stock, Number(product.stock) > 0 ? "green" : "red") : "") + '</div></div>' +
			'<div class="favorite-side"><span class="price">' + (invalid ? "-" : money(product.price)) + '</span><div class="card-actions">' + actions + '</div></div></article>';
	}).join("");
	if (!rows) {
		rows = '<div class="empty-cart"><h3>暂无收藏</h3><p class="muted">在商品卡片或详情页点击爱心后，会出现在这里。</p><button class="primary-btn profile-link" data-page="home" type="button">去逛商品</button></div>';
	}
	return '<section class="favorite-page"><div class="section-head"><div><h2>我的收藏</h2><p>收藏商品会落库保存，可从这里返回详情或加入购物车。</p></div><button class="ghost-btn profile-link" data-page="profile" type="button">返回个人中心</button></div><div class="favorite-list">' + rows + '</div></section>';
}

function roleName(role) {
	if (role === "MERCHANT") return "商家";
	if (role === "ADMIN") return "管理员";
	if (role === "SYSTEM") return "系统";
	return "用户";
}

function avatarHtml(url, name) {
	if (url) return '<span class="chat-avatar" style="background-image:url(' + escapeHtml(url) + ')"></span>';
	return '<span class="chat-avatar text-avatar">' + escapeHtml(String(name || "聊").charAt(0)) + '</span>';
}

function fileSizeText(size) {
	var n = Number(size || 0);
	if (n >= 1024 * 1024) return (n / 1024 / 1024).toFixed(1) + " MB";
	if (n >= 1024) return (n / 1024).toFixed(1) + " KB";
	return n + " B";
}

function parseExtraJson(text) {
	try {
		return text ? JSON.parse(text) : {};
	} catch (e) {
		return {};
	}
}

function newClientMessageId() {
	return "msg-" + Date.now() + "-" + Math.random().toString(16).slice(2);
}

function findChatMessage(messageId) {
	var id = Number(messageId);
	return (state.messages || []).filter(function(item) { return Number(item.messageId) === id; })[0] || null;
}

function chatMessageSummary(item) {
	if (!item) return "";
	if (item.recalled) return "原消息已撤回";
	var type = String(item.contentType || "TEXT").toUpperCase();
	var extra = parseExtraJson(item.extraJson);
	if (type === "IMAGE") return "[图片]";
	if (type === "VIDEO") return "[视频]";
	if (type === "FILE") return "[文件] " + (item.fileName || "");
	if (type === "ORDER_CARD") return "[订单] " + (extra.orderNo || item.contentText || item.content || "");
	if (type === "PRODUCT_CARD") return "[商品] " + (extra.name || item.contentText || item.content || "");
	if (type === "REFUND_CARD") return "[退款] 退款申请";
	if (type === "FRIEND_REQUEST") return "[好友申请]";
	if (type === "SYSTEM") return "[系统通知]";
	var text = String(item.contentText || item.content || "").replace(/\s+/g, " ").trim();
	return text.length > 48 ? text.slice(0, 48) + "..." : text;
}

function chatQuoteBlock(item) {
	if (!item || !item.quoteMessageId) return "";
	return '<div class="chat-quote-block"><b>' + escapeHtml(item.quoteSenderName || roleName(item.quoteSenderType)) + '</b><span>' + escapeHtml(item.quoteSummary || (item.quoteRecalled ? "原消息已撤回" : "")) + '</span></div>';
}

function chatRecallText(item) {
	if (item.own) return "你撤回了一条消息";
	if (state.admin && item.senderRole && item.senderRole !== "ADMIN") return roleName(item.senderRole) + "撤回了一条消息";
	return "对方撤回了一条消息";
}

function closeChatMenu() {
	state.chatContextMenu = null;
}

function renderChatContextMenu() {
	if (!state.chatContextMenu) return "";
	var item = findChatMessage(state.chatContextMenu.messageId);
	if (!item || item.recalled) return "";
	var recall = item.canRecall ? '<button class="chat-menu-recall" data-id="' + item.messageId + '" type="button">撤回</button>' : "";
	return '<div class="chat-context-menu" style="left:' + state.chatContextMenu.x + 'px;top:' + state.chatContextMenu.y + 'px"><button class="chat-menu-quote" data-id="' + item.messageId + '" type="button">引用</button>' + recall + '</div>';
}

function renderChatQuotePreview(disabled) {
	if (!state.chatQuoteMessage || disabled) return "";
	return '<div class="chat-quote-preview"><div><b>引用 ' + escapeHtml(state.chatQuoteMessage.senderName || roleName(state.chatQuoteMessage.senderRole)) + '</b><span>' + escapeHtml(chatMessageSummary(state.chatQuoteMessage)) + '</span></div><button class="chat-quote-clear" type="button" aria-label="取消引用">×</button></div>';
}

function businessCardHtml(type, extra, own) {
	if (type === "ORDER_CARD") {
		return '<div class="chat-card order-card-mini"><b>订单咨询</b><span>' + escapeHtml(extra.orderNo || "") + '</span><p>' + escapeHtml(extra.shopName || "") + ' · ' + escapeHtml(extra.items || "") + '</p><strong>' + money(extra.amount || 0) + ' · ' + escapeHtml(extra.status || "") + '</strong></div>';
	}
	if (type === "PRODUCT_CARD") {
		return '<div class="chat-card product-card-mini">' + (extra.imageUrl ? '<img src="' + escapeHtml(extra.imageUrl) + '" alt="">' : '<span class="mini-product-fallback">品</span>') + '<div><b>' + escapeHtml(extra.name || "") + '</b><p>' + escapeHtml(extra.shopName || "") + '</p><strong>' + money(extra.price || 0) + '</strong></div></div>';
	}
	if (type === "REFUND_CARD") {
		var actions = state.merchant && !own && (extra.status === "待审核" || extra.status === "PENDING") ? '<div class="chat-card-actions"><button class="primary-btn refund-handle" data-id="' + escapeHtml(extra.afterSaleId || 0) + '" data-action="approve" type="button">同意退款</button><button class="ghost-btn refund-handle" data-id="' + escapeHtml(extra.afterSaleId || 0) + '" data-action="reject" type="button">拒绝</button></div>' : '';
		return '<div class="chat-card refund-card-mini"><b>退款申请</b><span>订单 ' + escapeHtml(extra.orderNo || "") + '</span><p>' + escapeHtml(extra.productName || "") + ' · ' + escapeHtml(extra.reason || "") + '</p><strong>' + money(extra.amount || 0) + ' · ' + escapeHtml(extra.status || "") + '</strong>' + actions + '</div>';
	}
	if (type === "FRIEND_REQUEST") {
		var friendActions = !own ? '<div class="chat-card-actions"><button class="primary-btn friend-handle" data-id="' + escapeHtml(extra.requestId || 0) + '" data-action="accept" type="button">同意</button><button class="ghost-btn friend-handle" data-id="' + escapeHtml(extra.requestId || 0) + '" data-action="reject" type="button">拒绝</button></div>' : '';
		return '<div class="chat-card friend-card-mini"><b>好友申请</b><p>' + escapeHtml(extra.fromName || "用户") + '：' + escapeHtml(extra.message || "请求添加你为好友") + '</p>' + friendActions + '</div>';
	}
	return "";
}

function renderChatMessage(item) {
	var type = String(item.contentType || "TEXT").toUpperCase();
	var body = "";
	if (item.recalled) {
		body = '<p class="chat-recalled-text">' + escapeHtml(chatRecallText(item)) + '</p>';
	} else if (type === "IMAGE" && item.mediaUrl) {
		body = '<a class="chat-image" href="' + escapeHtml(item.mediaUrl) + '" target="_blank"><img src="' + escapeHtml(item.mediaUrl) + '" alt="' + escapeHtml(item.fileName || "图片") + '"></a>';
	} else if (type === "VIDEO" && item.mediaUrl) {
		body = '<video class="chat-video" src="' + escapeHtml(item.mediaUrl) + '" controls></video><div class="chat-file-name">' + escapeHtml(item.fileName || "视频") + '</div>';
	} else if (type === "FILE" && item.mediaUrl) {
		body = '<a class="chat-file" href="' + escapeHtml(item.mediaUrl) + '" target="_blank"><b>' + escapeHtml(item.fileName || "附件") + '</b><span>' + fileSizeText(item.fileSize) + '</span></a>';
	} else if (["ORDER_CARD", "PRODUCT_CARD", "REFUND_CARD", "FRIEND_REQUEST"].indexOf(type) >= 0) {
		body = businessCardHtml(type, parseExtraJson(item.extraJson), item.own);
	} else {
		body = '<p>' + escapeHtml(item.contentText || item.content || "").replace(/\n/g, "<br>") + '</p>';
	}
	return '<div class="chat-message ' + (item.own ? "mine" : "theirs") + (item.recalled ? " recalled" : "") + '" data-message-id="' + item.messageId + '"><div class="chat-bubble">' + chatQuoteBlock(item) + body + '<time>' + escapeHtml(shortDate(item.createTime || "")) + '</time></div></div>';
}

function renderMessages() {
	var current = (state.conversations || []).filter(function(c) { return Number(c.conversationId) === Number(state.activeConversationId); })[0];
	var role = state.admin ? "管理员" : (state.merchant ? "商家" : "用户");
	var convRows = (state.conversations || []).map(function(c) {
		var unread = Number(c.unreadCount || 0);
		return '<button class="conversation-item ' + (Number(c.conversationId) === Number(state.activeConversationId) ? "active" : "") + '" data-id="' + c.conversationId + '" type="button">' +
			avatarHtml(c.peerAvatar, c.peerName) + '<span class="conversation-main"><b>' + escapeHtml(c.peerName || "会话") + '</b><small>' + escapeHtml(c.lastMessage || "暂无消息") + '</small></span>' +
			'<span class="conversation-side"><em>' + escapeHtml(shortDate(c.lastMessageTime || "")) + '</em>' + (unread > 0 ? '<i>' + (unread > 98 ? "99+" : unread) + '</i>' : '') + '</span></button>';
	}).join("") || '<div class="chat-empty-mini">暂无会话，先搜索联系人发起聊天。</div>';
	var targetRoleOptions = state.admin ? '<option value="">全部</option><option value="USER">用户</option><option value="MERCHANT">商家</option>' :
		(state.merchant ? '<option value="">全部</option><option value="USER">用户</option><option value="ADMIN">平台客服</option>' : '<option value="">全部</option><option value="USER">用户</option><option value="MERCHANT">商家</option><option value="ADMIN">平台客服</option>');
	var targetRows = (state.messageTargets || []).map(function(t) {
		return '<button class="chat-target" data-role="' + escapeHtml(t.role) + '" data-id="' + t.id + '" type="button">' + avatarHtml(t.avatarUrl, t.name) + '<span><b>' + escapeHtml(t.name) + '</b><small>' + escapeHtml(t.subtitle || roleName(t.role)) + '</small></span></button>';
	}).join("");
	var messages = current ? (state.messages || []).map(renderChatMessage).join("") : '<div class="chat-empty"><h3>选择一个会话</h3><p class="muted">左侧选择会话，或搜索联系人发起聊天。</p></div>';
	var disabled = !current || Number(current.conversationId) === 0 ? "disabled" : "";
	var sendDisabled = disabled || state.chatSending ? "disabled" : "";
	var sendText = state.chatSending ? "发送中..." : "发送";
	var header = current ? avatarHtml(current.peerAvatar, current.peerName) + '<div><h3>' + escapeHtml(current.peerName) + '</h3><p>' + roleName(current.peerRole) + ' #' + escapeHtml(current.peerId) + '</p></div>' : '<span class="chat-avatar text-avatar">聊</span><div><h3>我的消息</h3><p>真实会话、附件和未读提醒已接入数据库</p></div>';
	var modal = renderChatModal();
	var friendEntry = state.user ? '<button class="primary-btn chat-modal-open" data-modal="friend" type="button">添加好友</button>' : '';
	var emojiPanelClass = state.emojiPanelOpen ? " show" : "";
	return '<section class="message-page chat-page"><div class="message-hero chat-hero"><img src="assets/img/nav-message.png" alt=""><div><span class="badge">业务沟通中心</span><h2>我的消息</h2><p>' + role + '端消息支持好友、订单咨询、商品卡片、退款售后和附件消息。</p></div>' + friendEntry + '</div>' +
		'<div class="chat-shell"><aside class="chat-sidebar"><div class="chat-search"><select id="chatTargetRole">' + targetRoleOptions + '</select><input id="chatTargetKeyword" value="' + escapeHtml(state.messageSearchKeyword || "") + '" placeholder="搜索联系人"><button class="ghost-btn" id="chatTargetSearch" type="button">搜索</button></div><div class="chat-targets">' + targetRows + '</div><div class="conversation-list">' + convRows + '</div></aside>' +
		'<section class="chat-window"><header class="chat-window-head">' + header + '</header><main class="chat-messages" id="chatMessages">' + messages + '</main><form class="chat-input" id="chatInputForm"><div class="emoji-panel' + emojiPanelClass + '" id="emojiPanel"><button type="button">&#128512;</button><button type="button">&#128077;</button><button type="button">&#10084;&#65039;</button><button type="button">&#128514;</button><button type="button">&#128546;</button><button type="button">&#128558;</button></div>' + renderChatQuotePreview(disabled) + '<textarea id="chatInputText" ' + disabled + ' rows="2" placeholder="' + (disabled ? "系统通知不可回复，请选择聊天会话" : "输入消息，Enter 发送，Shift+Enter 换行") + '"></textarea><div class="chat-tools"><button class="ghost-btn" id="emojiToggle" type="button" ' + disabled + '>表情</button><button class="ghost-btn chat-modal-open" data-modal="order" type="button" ' + disabled + '>发送订单</button><button class="ghost-btn chat-send-product" type="button" ' + disabled + '>发送商品</button><button class="ghost-btn chat-modal-open" data-modal="refund" type="button" ' + disabled + '>申请退款</button><label class="ghost-btn ' + disabled + '">图片<input id="chatImageFile" type="file" accept="image/*" ' + disabled + '></label><label class="ghost-btn ' + disabled + '">文件<input id="chatDocFile" type="file" accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.zip,.rar,.ppt,.pptx,.csv" ' + disabled + '></label><label class="ghost-btn ' + disabled + '">视频<input id="chatVideoFile" type="file" accept="video/mp4,video/webm" ' + disabled + '></label><span class="chat-upload-progress" id="chatUploadProgress"><i></i></span><button class="primary-btn chat-send-btn" type="submit" ' + sendDisabled + '>' + sendText + '</button></div></form></section></div>' + modal + renderChatContextMenu() + '</section>';
}

function renderChatModal() {
	if (!state.chatModal) return "";
	if (state.chatModal === "friend") {
		var results = (state.friendSearchResults || []).map(function(u) {
			var btn = u.friend ? '<button class="ghost-btn friend-start-chat" data-id="' + u.id + '" type="button">进入聊天</button>' : '<button class="primary-btn friend-request" data-id="' + u.id + '" type="button">添加好友</button>';
			return '<article class="modal-list-item">' + avatarHtml(u.avatarUrl, u.username) + '<div><b>' + escapeHtml(u.username) + '</b><p class="muted">ID ' + escapeHtml(u.accountId || u.id) + ' · ' + (u.friend ? "已是好友" : (u.requestStatus === "PENDING" ? "申请待处理" : "可添加")) + '</p></div>' + btn + '</article>';
		}).join("") || '<p class="muted">输入用户ID、登录ID或昵称搜索好友。</p>';
		return '<div class="chat-modal"><div class="chat-modal-card"><div class="section-head"><div><h3>添加好友 / 发起会话</h3><p>用户之间可通过好友申请建立聊天关系。</p></div><button class="ghost-btn chat-modal-close" type="button">关闭</button></div><div class="chat-modal-search"><input id="friendKeyword" placeholder="输入用户ID、登录ID或昵称"><button class="primary-btn" id="friendSearchBtn" type="button">搜索</button></div><div class="modal-list">' + results + '</div></div></div>';
	}
	if (state.chatModal === "order") {
		var orders = (state.orders || []).map(function(o) { return '<article class="modal-list-item"><div><b>' + escapeHtml(o.orderNo) + '</b><p class="muted">' + escapeHtml(o.shopName || "平台自营") + ' · ' + money(o.totalAmount) + ' · ' + escapeHtml(o.status) + '</p></div><button class="primary-btn chat-send-order" data-id="' + o.id + '" type="button">发送</button></article>'; }).join("") || '<p class="muted">暂无可发送订单。</p>';
		return '<div class="chat-modal"><div class="chat-modal-card"><div class="section-head"><div><h3>选择订单</h3><p>发送订单卡片用于咨询售后、物流或金额问题。</p></div><button class="ghost-btn chat-modal-close" type="button">关闭</button></div><div class="modal-list">' + orders + '</div></div></div>';
	}
	if (state.chatModal === "refund") {
		var refundItems = [];
		(state.orders || []).forEach(function(o) {
			(o.items || []).forEach(function(item) {
				if (!item.product) return;
				refundItems.push('<option value="' + o.id + ':' + item.product.id + '">' + escapeHtml(o.orderNo) + ' · ' + escapeHtml(item.snapshotName || item.product.name || "商品") + '</option>');
			});
		});
		var refundOptions = refundItems.join("") || '<option value="">暂无可申请的订单商品</option>';
		return '<div class="chat-modal"><div class="chat-modal-card"><div class="section-head"><div><h3>申请退款</h3><p>提交后会生成售后记录，并给商家发送退款卡片。</p></div><button class="ghost-btn chat-modal-close" type="button">关闭</button></div><form class="address-form" id="chatRefundForm"><label class="field wide"><span>订单商品</span><select id="refundOrderItem">' + refundOptions + '</select></label><label class="field"><span>原因</span><input id="refundReason" placeholder="如：商品破损 / 不想要了"></label><label class="field wide"><span>说明</span><textarea id="refundDesc" rows="3" placeholder="补充退款说明"></textarea></label><button class="primary-btn" type="submit">提交退款申请</button></form></div></div>';
	}
	return "";
}

function renderAdminHall() {
	var pageOptions = [
		["home", "购物主页"],
		["cart", "购物车"],
		["orders", "我的订单"],
		["profile", "个人中心"],
		["favorites", "我的收藏"],
		["coupons", "我的优惠券"],
		["vip", "VIP中心"]
	].map(function(item) { return '<option value="' + item[0] + '">' + item[1] + '</option>'; }).join("");
	var productOptions = state.products.map(function(p) {
		return '<option value="' + p.id + '">' + escapeHtml("#" + p.id + " · " + p.name) + '</option>';
	}).join("");
	var rows = (state.hallBanners || []).map(function(b) {
		var preview = String(b.mediaType || "IMAGE").toUpperCase() === "VIDEO" ? '<video src="' + escapeHtml(b.mediaUrl) + '" muted></video>' : '<img src="' + escapeHtml(b.mediaUrl) + '" alt="">';
		var linkText = b.linkEnabled ? (b.linkType === "PRODUCT" ? "商品 #" + b.productId : "页面 " + pageTitleText(b.linkTarget || "")) : "不跳转";
		return '<tr><td>' + b.id + '</td><td><div class="hall-admin-thumb">' + preview + '</div></td><td><b>' + escapeHtml(b.title || "未命名") + '</b><p class="muted">' + escapeHtml(b.subtitle || "") + '</p><p class="muted">' + (b.overlayEnabled === false ? "不显示标题层" : "标题层：" + positionText(b.textPosition)) + '</p></td><td>' + escapeHtml(b.mediaType || "IMAGE") + '</td><td>' + b.sortNo + '</td><td>' + badge(b.enabled ? "启用" : "禁用", b.enabled ? "green" : "amber") + '</td><td>' + escapeHtml(linkText) + '</td><td><button class="ghost-btn hall-edit" data-id="' + b.id + '" type="button">编辑</button><button class="ghost-btn hall-delete" data-id="' + b.id + '" type="button">删除</button></td></tr>';
	}).join("") || '<tr><td colspan="8">暂无大厅展示项，上传图片或视频后保存即可。</td></tr>';
	return '<section class="panel-card admin-section hall-admin-page"><div class="section-head"><div><h2>大厅展示</h2><p>配置用户首页顶部轮播。每一项可单独上传图片或视频，所以列表中可以图片、视频混搭。</p></div></div><section class="hero-grid hall-admin-preview">' + renderHallHero(true) + renderHallStatsCard() + '</section><form class="address-form hall-config-form" id="hallBannerForm"><input type="hidden" id="hallId"><input type="hidden" id="hallMediaUrl"><label class="field"><span>上传媒体</span><input id="hallMediaFile" type="file" accept="image/*,video/*"><small id="hallUploadName" class="muted">选择图片或视频，上传成功后保存展示项</small><div class="upload-progress" id="hallUploadProgress"><i></i></div></label><label class="field"><span>媒体类型</span><select id="hallMediaType"><option value="IMAGE">图片</option><option value="VIDEO">视频</option></select></label><label class="field"><span>展示状态</span><select id="hallEnabled"><option value="true">启用</option><option value="false">暂不展示</option></select></label><label class="field"><span>排序</span><input id="hallSortNo" type="number" value="1"></label><label class="field"><span>标题层</span><select id="hallOverlayEnabled"><option value="true">显示标题文字</option><option value="false">只展示媒体</option></select></label><label class="field"><span>文字位置</span><select id="hallTextPosition"><option value="LEFT">左侧</option><option value="CENTER">居中</option><option value="RIGHT">右侧</option></select></label><label class="field"><span>标题颜色</span><input id="hallTitleColor" type="color" value="#ffffff"></label><label class="field"><span>副标题颜色</span><input id="hallSubtitleColor" type="color" value="#e2e8f0"></label><label class="field wide"><span>标题</span><input id="hallTitle" placeholder="如：春夏焕新"></label><label class="field wide"><span>副标题</span><input id="hallSubtitle" placeholder="大厅展示说明"></label><label class="field"><span>点击后跳转</span><select id="hallLinkType"><option value="NONE">不跳转</option><option value="PRODUCT">选择商品详情</option><option value="PAGE">选择内置页面</option></select></label><label class="field hall-product-field"><span>跳转商品</span><select id="hallProductId"><option value="0">请选择商品</option>' + productOptions + '</select></label><label class="field hall-page-field"><span>跳转页面</span><select id="hallLinkTarget"><option value="">请选择页面</option>' + pageOptions + '</select></label><label class="field"><span>视频静音</span><select id="hallMuted"><option value="true">默认静音</option><option value="false">不静音</option></select></label><label class="field"><span>视频进度</span><select id="hallDisableSeek"><option value="false">允许调整</option><option value="true">禁止调整</option></select></label><label class="field"><span>视频暂停</span><select id="hallDisablePause"><option value="false">允许暂停</option><option value="true">禁止暂停</option></select></label><button class="primary-btn" type="submit">保存展示项</button><button class="ghost-btn" id="hallResetForm" type="button">清空表单</button></form><div class="admin-table"><table><thead><tr><th>ID</th><th>预览</th><th>文案</th><th>类型</th><th>排序</th><th>状态</th><th>跳转</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div></section>';
}

function requestStatusBadge(status) {
	if (status === "APPROVED") return badge("已通过", "green");
	if (status === "REJECTED") return badge("已驳回", "amber");
	return badge("待审核", "rose");
}

function requestTypeText(type) {
	if (type === "AVATAR") return "头像审核";
	if (type === "CANCEL") return "注销账号";
	if (type === "RESTORE") return "恢复账号";
	return "资料修改";
}

function renderAccountRequestPanel(roleName) {
	var rows = (state.accountRequests || []).map(function(item) {
		var attach = item.attachmentUrl ? '<div class="request-avatar-preview"><img src="' + escapeHtml(item.attachmentUrl) + '" alt=""></div>' : '';
		return '<tr><td>' + item.requestId + '</td><td>' + requestTypeText(item.requestType) + attach + '</td><td>' + escapeHtml(item.content || "") + '</td><td>' + requestStatusBadge(item.status) + '<p class="muted">' + escapeHtml(item.opinion || "") + '</p></td><td>' + escapeHtml(item.createTime || "") + '</td></tr>';
	}).join("") || '<tr class="account-request-empty"><td colspan="5"><b>暂无申请记录</b><span>提交头像、资料、注销或恢复申请后会显示在这里。</span></td></tr>';
	return '<section class="panel-card admin-section account-request-user-panel"><div class="section-head"><div><h2>我的设置</h2><p>' + roleName + '可在这里提交头像、资料、注销或恢复申请，管理员审核后通过消息中心反馈。</p></div></div><form class="address-form account-request-form" id="accountRequestForm"><input type="hidden" id="accountRequestAttachment"><label class="field"><span>设置内容</span><select id="accountRequestType"><option value="PROFILE">资料修改</option><option value="AVATAR">头像审核</option><option value="CANCEL">注销账号</option><option value="RESTORE">恢复账号</option></select></label><div class="field wide account-avatar-field"><span>头像图片</span><div class="avatar-upload-row"><div class="request-avatar-preview" id="accountAvatarPreview"></div><div><input id="accountAvatarFile" type="file" accept="image/*"><p class="muted" id="accountAvatarHint">上传后提交审核，通过后替换当前头像。</p><div class="upload-progress" id="avatarUploadProgress"><i></i></div></div></div></div><label class="field wide"><span>申请说明</span><textarea id="accountRequestContent" rows="4" placeholder="请写清楚要修改的资料、头像说明，或注销/恢复原因。"></textarea></label><button class="primary-btn account-request-submit" type="submit">提交审核</button></form><section class="account-request-history"><div class="section-head compact-head"><div><h3>申请记录</h3><p>查看每次申请的处理状态和管理员反馈。</p></div></div><div class="admin-table"><table><thead><tr><th>ID</th><th>类型</th><th>说明</th><th>状态</th><th>提交时间</th></tr></thead><tbody>' + rows + '</tbody></table></div></section></section>';
}

function renderAdminAccountRequests() {
	var rows = (state.accountRequests || []).map(function(item) {
		var pending = item.status === "PENDING";
		var attach = item.attachmentUrl ? '<div class="request-avatar-preview"><img src="' + escapeHtml(item.attachmentUrl) + '" alt=""></div>' : '';
		return '<tr><td><b>#' + item.requestId + '</b><p class="muted">' + escapeHtml(item.createTime || "") + '</p></td><td>' + escapeHtml(item.actorRole) + ' #' + item.actorId + '<p class="muted">' + escapeHtml(item.actorName || "") + '</p></td><td>' + requestTypeText(item.requestType) + attach + '</td><td>' + escapeHtml(item.content || "") + '</td><td>' + requestStatusBadge(item.status) + '<p class="muted">' + escapeHtml(item.opinion || "") + '</p></td><td><input class="admin-input account-opinion" data-id="' + item.requestId + '" placeholder="审核意见" ' + (pending ? "" : "disabled") + '><div class="merchant-pending-buttons"><button class="primary-btn account-review" data-id="' + item.requestId + '" data-status="APPROVED" type="button" ' + (pending ? "" : "disabled") + '>通过</button><button class="ghost-btn account-review" data-id="' + item.requestId + '" data-status="REJECTED" type="button" ' + (pending ? "" : "disabled") + '>驳回</button></div></td></tr>';
	}).join("") || '<tr><td colspan="6">暂无账号资料申请。</td></tr>';
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>资料审核</h2><p>集中处理用户和商家的头像、资料、注销与恢复申请，审核结果会进入对方消息中心。</p></div></div><div class="admin-table tall"><table><thead><tr><th>申请</th><th>提交人</th><th>类型</th><th>内容</th><th>状态</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div></section>';
}

function renderVipCenter() {
	var user = state.user || { username: "游客", points: 0 };
	var progress = vipProgress(user);
	var vip = progress.vip;
	var nextVip = progress.nextVip;
	var benefitList = vip.benefits.map(function(item) {
		return '<span>' + escapeHtml(item) + '</span>';
	}).join("");
	var levelRows = vipRules.map(function(rule) {
		var maxText = rule.next ? String(rule.next - 1) : "及以上";
		return '<tr class="' + (rule.level === vip.level ? "current" : "") + '"><td><span class="admin-vip"><img src="' + vipBadgeSrc(rule.level) + '" alt="VIP' + rule.level + '"><b>VIP' + rule.level + '</b><small>' + escapeHtml(rule.name) + '</small></span></td><td>' + rule.min + ' - ' + maxText + '</td><td>' + vipDiscountText(rule) + '</td><td>' + rule.coupons + ' 张</td><td>' + rule.pointRate + ' 倍</td><td>' + escapeHtml(rule.serviceLevel) + '</td></tr>';
	}).join("");
	var growthWays = [
		["注册账号", "+100 成长值"],
		["完成订单", "实付金额取整 + 20"],
		["评价商品", "+10 成长值"]
	].map(function(item) {
		return '<article class="simple-card vip-way-card"><strong>' + item[0] + '</strong><span>' + item[1] + '</span></article>';
	}).join("");
	return '<section class="vip-center-page"><div class="vip-center-hero"><div><span class="vip-level-pill">' + vipLevelLabel(vip) + '</span><h2>' + escapeHtml(user.username) + ' 的 VIP 中心</h2><p>' + progress.nextText + '</p><div class="vip-progress-head"><strong>成长进度</strong><span>' + progress.progressText + '</span></div><div class="vip-progress"><i style="width:' + progress.progressPct.toFixed(1) + '%"></i></div></div></div><div class="profile-metrics vip-center-metrics"><div><b>' + progress.growthText + '</b><span>当前成长值</span></div><div><b>' + compactNumber(user.points || 0) + '</b><span>当前积分</span></div><div><b>' + vip.coupons + '</b><span>每月优惠券</span></div><div><b>' + vip.pointRate + ' 倍</b><span>积分倍率</span></div><div><b>' + vipDiscountText(vip) + '</b><span>商品折扣</span></div><div><b>' + escapeHtml(vip.serviceLevel) + '</b><span>售后等级</span></div></div><div class="vip-center-grid"><article class="simple-card vip-benefit-card"><div class="icon-tile"><img src="' + vipBadgeSrc(vip.level) + '" alt="VIP' + vip.level + '"></div><strong>当前权益</strong><span>' + (nextVip ? "继续提升可解锁 " + vipLevelLabel(nextVip) : "已解锁最高等级权益") + '</span><div class="vip-benefit-list">' + benefitList + '</div></article><div class="vip-growth-panel"><div class="section-head compact-head"><div><h3>成长值获取</h3><p>完成购物、资料和地址等行为后提升等级。</p></div></div><div class="vip-way-grid">' + growthWays + '</div></div></div><section class="panel-card admin-section vip-rule-section"><div class="section-head"><div><h2>VIP等级权益规则</h2><p>系统按照成长值自动判断等级，结算时同步应用会员折扣。</p></div></div><div class="admin-table"><table><thead><tr><th>等级</th><th>成长值范围</th><th>折扣</th><th>月券</th><th>积分</th><th>售后</th></tr></thead><tbody>' + levelRows + '</tbody></table></div></section></section>';
}

function renderCoupons() {
	if (state.user && state.userCoupons.length) {
		var dbRows = state.userCoupons.map(function(c) {
			return '<article class="coupon-card wallet-coupon claimed"><div><strong>' + escapeHtml(c.couponName) + '</strong><span>' + escapeHtml(couponTypeText(c.couponType) + " · " + couponValueText(c)) + '</span><small>' + escapeHtml(couponScopeText(c)) + '</small><small>领取 ' + escapeHtml(shortDate(c.receiveTime)) + ' · 有效至 ' + escapeHtml(shortDate(c.expireTime)) + '</small></div>' + badge(statusText(c.status), c.status === "UNUSED" ? "green" : "amber") + '</article>';
		}).join("");
		return '<section class="coupon-wallet" id="section-user-coupon"><div class="section-head"><div><h2>我的优惠券</h2><p>展示后台发放、新人自动发放和 VIP 等级发放的优惠券。</p></div><button class="ghost-btn profile-link" data-page="cart" type="button">去购物车</button></div><div class="coupon-grid wallet-grid wallet-owned-grid">' + dbRows + '</div></section>';
	}
	var claimed = state.coupons.filter(function(coupon) { return coupon.claimed; });
	var unclaimed = state.coupons.filter(function(coupon) { return !coupon.claimed; });
	var couponCard = function(coupon, owned) {
		var labels = [couponBusinessLabel(coupon), coupon.scope || couponScopeText(coupon)];
		if (coupon.description) labels.push(coupon.description);
		var action = owned ? '<button class="primary-btn use-coupon" data-id="' + coupon.id + '" type="button">去使用</button>' : '<button class="primary-btn claim-coupon" data-id="' + coupon.templateId + '" type="button">领取</button>';
		return '<article class="coupon-card wallet-coupon ' + (owned ? "claimed" : "") + '"><div><strong>' + escapeHtml(coupon.title) + '</strong><span>' + escapeHtml(labels.join(" · ")) + '</span><small>' + escapeHtml(couponValueText(coupon)) + ' · 有效期至 ' + escapeHtml(shortDate(coupon.expireTime)) + '</small></div>' + action + '</article>';
	};
	var ownedHtml = claimed.map(function(coupon) { return couponCard(coupon, true); }).join("") || '<div class="coupon-empty wallet-empty"><h3>暂未领取优惠券</h3><p class="muted">可以先领取下方优惠券，结算时满足条件即可使用。</p></div>';
	var unclaimedHtml = unclaimed.map(function(coupon) { return couponCard(coupon, false); }).join("") || '<div class="coupon-empty wallet-empty"><h3>暂无可领取优惠券</h3><p class="muted">已领取的优惠券会展示在上方。</p></div>';
	return '<section class="coupon-wallet"><div class="section-head"><div><h2>我的优惠券</h2><p>查看已领取优惠券，结算时系统会按使用条件进行选择。</p></div><button class="ghost-btn profile-link" data-page="cart" type="button">去购物车</button></div><div class="profile-metrics coupon-summary"><div><b>' + claimed.length + '</b><span>已领取</span></div><div><b>' + unclaimed.length + '</b><span>可领取</span></div><div><b>' + (state.selectedCouponId ? "1" : "0") + '</b><span>已选择</span></div></div><div class="section-head compact-head"><div><h3>已领取</h3><p>可用优惠券会在购物车结算区显示。</p></div></div><div class="coupon-grid wallet-grid wallet-owned-grid">' + ownedHtml + '</div><div class="section-head compact-head"><div><h3>可领取</h3><p>领取后会加入“我的优惠券”。</p></div></div><div class="coupon-grid wallet-grid wallet-claim-grid">' + unclaimedHtml + '</div></section>';
}

function statusText(status) {
	return ({ PENDING: "待审核", APPROVED: "已通过", REJECTED: "已驳回", DISABLED: "已禁用", ON_SALE: "已上架", OFF_SALE: "已下架", ENABLED: "启用", UNUSED: "未使用", USED: "已使用", EXPIRED: "已过期" })[status] || status || "";
}

function merchantStatusTone(status) {
	return status === "APPROVED" ? "green" : (status === "REJECTED" || status === "DISABLED" ? "rose" : "amber");
}

function couponValueText(coupon) {
	if (coupon.couponType === "DISCOUNT") {
		var fold = Number(coupon.discountRate || 1) * 10;
		return (fold % 1 ? fold.toFixed(1) : fold.toFixed(0)) + "折";
	}
	return "满 " + money(coupon.minAmount || coupon.threshold || 0) + " 减 " + money(coupon.amount || coupon.discount || 0);
}

function couponTypeText(type) {
	return ({ AMOUNT: "满减券", DISCOUNT: "折扣券", NEW_USER: "新人券", VIP: "VIP专属券" })[type] || type || "优惠券";
}

function couponScopeText(coupon) {
	if (coupon.couponOwnerType === "MERCHANT") return (coupon.shopName || "店铺") + "专属，仅限本店商品";
	if (coupon.stackable) return "平台可叠加券";
	if (coupon.couponType === "NEW_USER") return "新用户专享";
	if (coupon.couponType === "VIP") return coupon.vipLevel ? "VIP" + coupon.vipLevel + " 及以上可用" : "VIP会员可用";
	return "全场商品可用";
}

function shortDate(value) {
	return value ? String(value).slice(0, 10) : "-";
}

function renderMerchantCoupons() {
	var rows = state.merchantCoupons.map(function(c) {
		var next = c.status === "ENABLED" ? "DISABLED" : "ENABLED";
		return '<tr><td>' + c.couponId + '</td><td><b>' + escapeHtml(c.couponName) + '</b><p class="muted">' + couponValueText(c) + ' · ' + escapeHtml(c.description || "仅限本店商品") + '</p></td><td>' + couponTypeText(c.couponType) + '</td><td>' + c.perUserLimit + '</td><td>' + c.validDays + '天</td><td>' + badge(statusText(c.status), c.status === "ENABLED" ? "green" : "amber") + '</td><td><button class="ghost-btn merchant-coupon-status" data-id="' + c.couponId + '" data-status="' + next + '" type="button">' + statusText(next) + '</button><button class="ghost-btn merchant-coupon-delete" data-id="' + c.couponId + '" type="button">删除</button></td></tr>';
	}).join("") || '<tr><td colspan="7">暂无店铺优惠券。</td></tr>';
	var userRows = (state.merchantCouponUsers || []).map(function(c) {
		return '<tr><td>' + c.userCouponId + '</td><td>' + escapeHtml(c.couponName || "") + '</td><td>用户 ' + c.userId + '</td><td>' + badge(statusText(c.status), c.status === "UNUSED" ? "green" : "amber") + '</td><td>' + escapeHtml(shortDate(c.receiveTime)) + '</td><td>' + escapeHtml(shortDate(c.expireTime)) + '</td></tr>';
	}).join("") || '<tr><td colspan="6">暂无用户领取记录。</td></tr>';
	return '<section class="panel-card admin-section merchant-coupon-page"><div class="section-head"><div><h2>店铺优惠券</h2><p>商家只能创建和发放本店专属优惠券，结算时仅抵扣本店商品。</p></div></div><form class="address-form merchant-coupon-form" id="merchantCouponForm"><label class="field"><span>优惠券名称</span><input id="mcName" placeholder="如：本店满减券"></label><label class="field"><span>类型</span><select id="mcType"><option value="AMOUNT">满减券</option><option value="DISCOUNT">折扣券</option></select></label><label class="field merchant-coupon-amount"><span>优惠金额</span><input id="mcAmount" type="number" step="0.01" value="5"></label><label class="field merchant-coupon-discount hidden"><span>折扣比例</span><input id="mcDiscountRate" type="number" step="0.01" value="0.95"></label><label class="field"><span>最低消费</span><input id="mcMinAmount" type="number" step="0.01" value="39"></label><label class="field"><span>每人限领</span><input id="mcLimit" type="number" value="1"></label><label class="field"><span>有效天数</span><input id="mcValidDays" type="number" value="30"></label><label class="field wide"><span>使用说明</span><input id="mcDescription" placeholder="仅限本店商品使用"></label><div class="form-actions"><button class="primary-btn" type="submit">保存店铺券</button></div></form><div class="admin-table merchant-coupon-table"><table><thead><tr><th>ID</th><th>名称</th><th>类型</th><th>限领</th><th>有效</th><th>状态</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div><div class="section-head compact-head merchant-coupon-subhead"><div><h3>发放店铺券</h3><p>支持全体用户、指定用户、本店下单用户和本店购物车用户。</p></div></div>' + renderCouponIssueForm(true) + '<div class="section-head compact-head merchant-coupon-subhead"><div><h3>领取记录</h3><p>查看用户持有的本店优惠券。</p></div></div><div class="admin-table merchant-coupon-table"><table><thead><tr><th>ID</th><th>优惠券</th><th>用户</th><th>状态</th><th>领取</th><th>过期</th></tr></thead><tbody>' + userRows + '</tbody></table></div></section>';
}

function orderItemsSummary(order) {
	return (order.items || []).map(function(item) {
		return escapeHtml((item.product && item.product.name || "商品") + " x " + item.quantity);
	}).join("、") || "无商品明细";
}

function merchantOrderRows() {
	return (state.merchantOrders || []).filter(function(order) {
		return state.merchantOrderStatusFilter === "all" || order.status === state.merchantOrderStatusFilter;
	});
}

function renderMerchantOrders() {
	var statuses = ["待付款", "待发货", "待收货", "已完成", "已取消"];
	var rows = merchantOrderRows().map(function(order) {
		var canShip = order.status === "待发货";
		var afterSales = (order.afterSales || []).map(function(a) { return '#' + a.afterSaleId + ' 商品' + a.productId + ' ' + escapeHtml(a.status || ""); }).join("<br>") || "-";
		var reportUser = '<button class="ghost-btn open-report" data-target-role="USER" data-target-id="' + order.userId + '" data-order-id="' + order.id + '" data-merchant-id="' + (order.merchantId || 0) + '" data-report-type="用户违规" type="button">举报用户</button>';
		return '<tr><td><b>' + escapeHtml(order.orderNo) + '</b><p class="muted">' + escapeHtml(order.createTime || "") + '</p>' + (order.batchNo ? '<p class="muted">批次：' + escapeHtml(order.batchNo) + '</p>' : '') + '</td><td>' + orderItemsSummary(order) + '</td><td>用户 ' + escapeHtml(order.userId) + '</td><td>' + escapeHtml(order.receiverName || "") + '<p class="muted">' + escapeHtml(order.receiverPhone || "") + '</p></td><td>' + escapeHtml(order.receiverAddress || "") + '</td><td>' + (shipmentSummary(order) || "-") + '</td><td>' + afterSales + '</td><td><b>' + money(order.totalAmount) + '</b><p class="muted">商品 ' + money(orderOriginAmount(order)) + '</p></td><td>' + badge(order.status, order.status === "已完成" ? "green" : (order.status === "已取消" ? "red" : "amber")) + '</td><td><button class="primary-btn merchant-order-ship" data-id="' + order.id + '" type="button" ' + (canShip ? "" : "disabled") + '>发货</button>' + reportUser + '</td></tr>';
	}).join("") || '<tr><td colspan="11">暂无匹配订单。</td></tr>';
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>订单管理</h2><p>仅展示本店商品相关订单，待发货订单可由商家填写物流发货。</p></div><select class="admin-input compact-select" id="merchantOrderStatusFilter"><option value="all">全部状态</option>' + statuses.map(function(status) { return '<option value="' + status + '" ' + (state.merchantOrderStatusFilter === status ? "selected" : "") + '>' + status + '</option>'; }).join("") + '</select></div><div class="admin-table tall merchant-orders-table"><table><thead><tr><th>订单编号</th><th>本店商品</th><th>买家</th><th>收货人</th><th>收货地址</th><th>物流</th><th>售后</th><th>本店金额</th><th>状态</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div></section>';
}

function renderMerchantProfile() {
	var m = state.merchant || {};
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>店铺资料</h2><p>店铺基础资料由管理员维护，商家端用于查看当前经营信息。</p></div></div><div class="admin-user-form"><label class="field"><span>商家编号</span><input class="admin-input" value="' + escapeHtml(m.merchantCode || "") + '" disabled></label><label class="field"><span>商家名称</span><input class="admin-input" value="' + escapeHtml(m.merchantName || "") + '" disabled></label><label class="field"><span>店铺名称</span><input class="admin-input" value="' + escapeHtml(m.shopName || "") + '" disabled></label><label class="field"><span>绑定手机号</span><input class="admin-input" value="' + escapeHtml(m.contactPhone || "") + '" disabled></label><label class="field"><span>绑定邮箱</span><input class="admin-input" value="' + escapeHtml(m.email || "") + '" disabled></label><label class="field"><span>经营类目</span><input class="admin-input" value="' + escapeHtml(m.businessCategory || "") + '" disabled></label><label class="field wide"><span>经营地址</span><input class="admin-input" value="' + escapeHtml(m.businessAddress || "") + '" disabled></label><label class="field wide"><span>店铺简介</span><input class="admin-input" value="' + escapeHtml(m.shopDesc || "") + '" disabled></label></div></section>' + renderAccountRequestPanel("商家");
}

function renderMerchantCenter() {
	var m = state.merchant || {};
	var total = state.merchantProducts.length;
	var onSale = state.merchantProducts.filter(function(p) { return p.saleStatus === "ON_SALE"; }).length;
	var offSale = state.merchantProducts.filter(function(p) { return p.saleStatus !== "ON_SALE"; }).length;
	var pending = state.merchantProducts.filter(function(p) { return p.auditStatus === "PENDING"; }).length;
	var rejected = state.merchantProducts.filter(function(p) { return p.auditStatus === "REJECTED"; }).length;
	var orderCount = (state.merchantOrders || []).length;
	var salesAmount = (state.merchantOrders || []).filter(function(order) { return order.status === "已完成"; }).reduce(function(sum, order) { return sum + Number(order.totalAmount || 0); }, 0);
	var recent = state.merchantProducts.slice(0, 5).map(function(p) {
		return '<tr><td>' + p.id + '</td><td>' + escapeHtml(p.name) + '</td><td>' + statusText(p.auditStatus) + '</td><td>' + escapeHtml(p.submitTime || "") + '</td></tr>';
	}).join("") || '<tr><td colspan="4">暂无商品</td></tr>';
	return '<section class="profile-grid"><div class="profile-card">' + avatarMarkup(m.avatarUrl, "商", "profile-avatar") + '<h2>' + escapeHtml(m.shopName || "") + '</h2><p class="muted">' + escapeHtml(m.merchantName || "") + ' · ID ' + escapeHtml(m.merchantCode || "") + '</p><div class="profile-metrics"><div><b>' + statusText(m.status) + '</b><span>商家状态</span></div><div><b>' + total + '</b><span>商品总数</span></div><div><b>' + pending + '</b><span>待审核</span></div><div><b>' + onSale + '</b><span>已上架</span></div><div><b>' + offSale + '</b><span>已下架</span></div><div><b>' + orderCount + '</b><span>本店订单</span></div><div><b>' + money(salesAmount) + '</b><span>本店销售额</span></div></div></div><section class="panel-card admin-section"><div class="section-head"><div><h2>最近提交审核</h2><p>商品审核通过后才会在商城前台展示。</p></div></div><div class="admin-table"><table><thead><tr><th>ID</th><th>商品</th><th>审核</th><th>提交时间</th></tr></thead><tbody>' + recent + '</tbody></table></div></section></section>';
}

function mapValue(row, key) {
	if (!row) return "";
	if (row[key] != null) return row[key];
	var lower = key.toLowerCase();
	for (var k in row) {
		if (Object.prototype.hasOwnProperty.call(row, k) && String(k).toLowerCase() === lower) return row[k];
	}
	return "";
}

function renderAnalyticsCards(summary, cards) {
	return '<section class="admin-stats analytics-stats">' + cards.map(function(card) {
		var value = mapValue(summary, card.key);
		if (card.money) value = money(value);
		else if (card.rating) value = Number(value || 0).toFixed(1);
		return '<div class="simple-card admin-stat-card"><div class="icon-tile">' + escapeHtml(card.icon || "数") + '</div><span>' + escapeHtml(card.label) + '</span><strong>' + escapeHtml(value == null || value === "" ? 0 : value) + '</strong></div>';
	}).join("") + '</section>';
}

function renderProductAnalyticsRows(products) {
	return (products || []).map(function(p) {
		return '<tr><td>' + escapeHtml(mapValue(p, "id")) + '</td><td><b>' + escapeHtml(mapValue(p, "name")) + '</b><p class="muted">' + escapeHtml(mapValue(p, "categoryName")) + '</p></td><td>' + money(mapValue(p, "price")) + '</td><td>' + escapeHtml(mapValue(p, "stock")) + '</td><td>' + escapeHtml(mapValue(p, "sales")) + '</td><td>' + Number(mapValue(p, "averageRating") || 0).toFixed(1) + '</td><td>' + escapeHtml(mapValue(p, "reviewCount") || 0) + '</td><td>' + escapeHtml(mapValue(p, "favoriteCount") || 0) + '</td></tr>';
	}).join("") || '<tr><td colspan="8">暂无商品分析数据。</td></tr>';
}

function renderMerchantAnalytics() {
	var data = state.merchantAnalytics || {};
	var summary = data.summary || {};
	var reviews = (data.recentReviews || []).map(function(r) {
		return '<tr><td>' + escapeHtml(mapValue(r, "productName")) + '</td><td>★ ' + Number(mapValue(r, "rating") || 0).toFixed(1) + '</td><td>' + escapeHtml(mapValue(r, "username")) + '</td><td>' + escapeHtml(mapValue(r, "content")) + '</td><td>' + escapeHtml(shortDate(mapValue(r, "createTime"))) + '</td></tr>';
	}).join("") || '<tr><td colspan="5">暂无近期评价。</td></tr>';
	return renderAnalyticsCards(summary, [
		{ key: "productCount", label: "商品数", icon: "品" },
		{ key: "orderCount", label: "订单数", icon: "单" },
		{ key: "salesAmount", label: "销售额", icon: "额", money: true },
		{ key: "avgRating", label: "店铺星级", icon: "星", rating: true },
		{ key: "reviewCount", label: "评价数", icon: "评" },
		{ key: "favoriteCount", label: "收藏数", icon: "藏" }
	]) + '<section class="panel-card admin-section"><div class="section-head"><div><h2>商品表现</h2><p>按销量、评分、收藏和评论综合查看本店商品表现。</p></div></div><div class="admin-table"><table><thead><tr><th>ID</th><th>商品</th><th>价格</th><th>库存</th><th>销量</th><th>评分</th><th>评论</th><th>收藏</th></tr></thead><tbody>' + renderProductAnalyticsRows(data.products) + '</tbody></table></div></section>' +
		'<section class="panel-card admin-section"><div class="section-head"><div><h2>近期评价</h2><p>用户评价会影响商品排序和店铺星级。</p></div></div><div class="admin-table"><table><thead><tr><th>商品</th><th>评分</th><th>用户</th><th>内容</th><th>时间</th></tr></thead><tbody>' + reviews + '</tbody></table></div></section>';
}

function renderAdminAnalytics() {
	var data = state.adminAnalytics || {};
	var summary = data.summary || {};
	var shops = (data.shops || []).map(function(s) {
		return '<tr><td>' + escapeHtml(mapValue(s, "merchantId")) + '</td><td><b>' + escapeHtml(mapValue(s, "shopName")) + '</b><p class="muted">' + escapeHtml(mapValue(s, "merchantName")) + '</p></td><td>' + escapeHtml(mapValue(s, "productCount") || 0) + '</td><td>' + escapeHtml(mapValue(s, "sales") || 0) + '</td><td>' + Number(mapValue(s, "avgRating") || 0).toFixed(1) + '</td><td>' + escapeHtml(mapValue(s, "reviewCount") || 0) + '</td><td>' + escapeHtml(mapValue(s, "favoriteCount") || 0) + '</td></tr>';
	}).join("") || '<tr><td colspan="7">暂无店铺分析数据。</td></tr>';
	return renderAnalyticsCards(summary, [
		{ key: "productCount", label: "平台商品", icon: "品" },
		{ key: "orderCount", label: "平台订单", icon: "单" },
		{ key: "salesAmount", label: "平台销售额", icon: "额", money: true },
		{ key: "avgRating", label: "平均评分", icon: "星", rating: true },
		{ key: "reviewCount", label: "评价总数", icon: "评" },
		{ key: "favoriteCount", label: "收藏总数", icon: "藏" }
	]) + '<section class="panel-card admin-section"><div class="section-head"><div><h2>店铺分析</h2><p>按店铺汇总销量、评分、收藏和评论。</p></div></div><div class="admin-table"><table><thead><tr><th>商家ID</th><th>店铺</th><th>商品</th><th>销量</th><th>星级</th><th>评论</th><th>收藏</th></tr></thead><tbody>' + shops + '</tbody></table></div></section>' +
		'<section class="panel-card admin-section"><div class="section-head"><div><h2>商品排行</h2><p>后台用于观察首页智能入口排序效果。</p></div></div><div class="admin-table"><table><thead><tr><th>ID</th><th>商品</th><th>价格</th><th>库存</th><th>销量</th><th>评分</th><th>评论</th><th>收藏</th></tr></thead><tbody>' + renderProductAnalyticsRows(data.products) + '</tbody></table></div></section>';
}

function renderMerchantProductList() {
	var rows = state.merchantProducts.map(function(p) {
		return '<tr><td>' + p.id + '</td><td>' + productThumb(p, "merchant-list-thumb") + '</td><td><b>' + escapeHtml(p.name) + '</b><p class="muted">' + escapeHtml(p.shortDesc) + '</p></td><td>' + escapeHtml(p.categoryName) + '</td><td>' + money(p.price) + '</td><td>' + p.stock + '</td><td>' + p.sales + '</td><td>' + badge(statusText(p.saleStatus), p.saleStatus === "ON_SALE" ? "green" : "amber") + '</td><td>' + badge(statusText(p.auditStatus), p.auditStatus === "APPROVED" ? "green" : (p.auditStatus === "REJECTED" ? "amber" : "")) + '<p class="muted">' + escapeHtml(p.auditOpinion || "") + '</p></td><td><button class="ghost-btn merchant-edit" data-id="' + p.id + '" type="button">编辑</button><button class="ghost-btn merchant-submit" data-id="' + p.id + '" type="button">提交审核</button><button class="ghost-btn merchant-offsale" data-id="' + p.id + '" type="button">下架</button></td></tr>';
	}).join("") || '<tr><td colspan="10">暂无商品，请先新增商品。</td></tr>';
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>商家商品列表</h2><p>商家只能管理自己发布的商品。</p></div><button class="primary-btn merchant-add-product" data-page="merchantProductAdd" type="button">新增商品</button></div><div class="admin-table merchant-product-list-table"><table><thead><tr><th>ID</th><th>图片</th><th>商品</th><th>分类</th><th>价格</th><th>库存</th><th>销量</th><th>销售</th><th>审核</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div></section>';
}

function optionListText(value, fallback) {
	var list = Array.isArray(value) ? value : String(value || "").split(/[,，、\n]/);
	list = list.map(function(item) { return String(item || "").trim(); }).filter(function(item) { return item; });
	return (list.length ? list : [fallback]).join("，");
}

function skuRowsForProduct(product) {
	var skus = productSkus(product || {});
	if (skus.length) return skus;
	return [{
		skuId: "DEFAULT",
		color: "默认",
		spec: "标准",
		price: Number(product.price || 0),
		oldPrice: Number(product.oldPrice || product.price || 0),
		stock: Number(product.stock || 0),
		enabled: true
	}];
}

function readListInput(id, fallback) {
	var value = document.getElementById(id) ? document.getElementById(id).value : "";
	var list = String(value || "").split(/[,，、\n]/).map(function(item) { return item.trim(); }).filter(function(item) { return item; });
	return list.length ? list : [fallback];
}

function skuSummaryHtml(product) {
	var rows = skuRowsForProduct(product);
	var activeRows = rows.filter(function(row) { return row.enabled !== false; });
	var priceValues = activeRows.map(function(row) { return Number(row.price || 0); });
	var stockTotal = activeRows.reduce(function(sum, row) { return sum + Number(row.stock || 0); }, 0);
	var priceText = priceValues.length ? (money(Math.min.apply(Math, priceValues)) + (Math.max.apply(Math, priceValues) !== Math.min.apply(Math, priceValues) ? " - " + money(Math.max.apply(Math, priceValues)) : "")) : money(product.price || 0);
	var detail = rows.map(function(row) {
		return '<span>' + escapeHtml(skuText(row) || "默认") + ' ' + money(row.price) + ' 库存' + Number(row.stock || 0) + (row.enabled === false ? ' 停用' : '') + '</span>';
	}).join("");
	return '<div class="sku-summary"><strong>' + priceText + ' · 总库存 ' + stockTotal + '</strong><div>' + detail + '</div></div>';
}

function skuAttrsForEditor(product) {
	var attrs = productAttrs(product || {}).map(function(attr) {
		return { name: attr.name || "规格", values: (attr.values || []).slice() };
	});
	return attrs.length ? attrs.slice(0, 4) : [{ name: "规格", values: ["默认"] }];
}

function skuKeyFromValues(values) {
	return (values || []).join("|");
}

function skuAttrsHtml(attrs) {
	return attrs.map(function(attr, index) {
		return '<article class="sku-attr-card" data-index="' + index + '"><div class="sku-attr-head"><strong>参数层级 ' + (index + 1) + '</strong><button class="ghost-btn sku-attr-remove" type="button">删除</button></div><label class="field"><span>参数名称</span><input class="sku-attr-name" value="' + escapeHtml(attr.name || "") + '" placeholder="如：机身颜色"></label><label class="field wide"><span>参数值</span><input class="sku-attr-values" value="' + escapeHtml((attr.values || []).join("，")) + '" placeholder="多个值用逗号分隔，如：绝对黑，雾光紫"></label></article>';
	}).join("");
}

function readSkuAttrs() {
	var attrs = [];
	Array.prototype.forEach.call(document.querySelectorAll(".sku-attr-card"), function(card) {
		var name = (card.querySelector(".sku-attr-name").value || "").trim();
		var values = (card.querySelector(".sku-attr-values").value || "").split(/[,，、\n]/).map(function(item) { return item.trim(); }).filter(function(item) { return item; });
		values = values.filter(function(item, index) { return values.indexOf(item) === index; });
		if (name && values.length) attrs.push({ name: name, values: values });
	});
	return attrs.length ? attrs.slice(0, 4) : [{ name: "规格", values: ["默认"] }];
}

function cartesianSkuValues(attrs) {
	var rows = [[]];
	attrs.forEach(function(attr) {
		var next = [];
		rows.forEach(function(row) {
			(attr.values || []).forEach(function(value) {
				next.push(row.concat([value]));
			});
		});
		rows = next;
	});
	return rows;
}

function buildSkuRows(attrs, product, currentRows) {
	attrs = attrs && attrs.length ? attrs : [{ name: "规格", values: ["默认"] }];
	var oldMap = {};
	(currentRows || []).forEach(function(row) {
		oldMap[skuKeyFromValues(row.values || [row.color, row.spec].filter(function(v) { return v; }))] = row;
	});
	return cartesianSkuValues(attrs).slice(0, 80).map(function(values) {
		var key = skuKeyFromValues(values);
		var old = oldMap[key] || {};
		return {
			skuId: old.skuId || key,
			values: values,
			text: old.text || values.join(" / "),
			color: values[0] || "默认",
			spec: values[1] || "标准",
			price: Number(old.price || product.price || 0),
			oldPrice: Number(old.oldPrice || product.oldPrice || product.price || 0),
			stock: Number(old.stock || product.stock || 0),
			enabled: old.enabled !== false,
			skuCode: old.skuCode || "",
			imageUrl: old.imageUrl || ""
		};
	});
}

function skuRowsHtml(rows) {
	return rows.map(function(row, index) {
		var valuesJson = escapeHtml(JSON.stringify(row.values || []));
		return '<tr class="sku-row"><td><b>' + escapeHtml(skuText(row) || "默认") + '</b><input class="sku-values" type="hidden" value="' + valuesJson + '"><input class="sku-id" type="hidden" value="' + escapeHtml(row.skuId || ("SKU-" + index)) + '"></td><td><input class="sku-price" type="number" step="0.01" value="' + escapeHtml(row.price || 0) + '"></td><td><input class="sku-old-price" type="number" step="0.01" value="' + escapeHtml(row.oldPrice || row.price || 0) + '"></td><td><input class="sku-stock" type="number" value="' + escapeHtml(row.stock || 0) + '"></td><td><input class="sku-code" value="' + escapeHtml(row.skuCode || "") + '" placeholder="可选"></td><td><input class="sku-image" value="' + escapeHtml(row.imageUrl || "") + '" placeholder="可选"></td><td><label class="check-field sku-enabled"><input class="sku-enable" type="checkbox" ' + (row.enabled === false ? "" : "checked") + '> 启用</label></td></tr>';
	}).join("");
}

function renderSkuEditor(product) {
	product = product || {};
	var attrs = skuAttrsForEditor(product);
	var rows = buildSkuRows(attrs, product, skuRowsForProduct(product));
	return '<div class="field wide sku-editor"><div class="sku-editor-head"><span>商品参数/SKU配置</span><div class="sku-editor-actions"><button class="ghost-btn" id="addSkuAttr" type="button">添加参数</button><button class="ghost-btn" id="generateSkuRows" type="button">生成组合</button></div></div><div id="skuAttrsBox" class="sku-attrs-box">' + skuAttrsHtml(attrs) + '</div><div class="sku-batch-row"><input id="batchSkuPrice" placeholder="批量销售价"><input id="batchSkuOldPrice" placeholder="批量原价"><input id="batchSkuStock" placeholder="批量库存"><button class="ghost-btn" id="applySkuBatch" type="button">批量填充</button></div><div class="admin-table sku-editor-table"><table><thead><tr><th>参数组合</th><th>销售价</th><th>原价</th><th>库存</th><th>SKU编码</th><th>SKU图片</th><th>状态</th></tr></thead><tbody id="skuRowsBody">' + skuRowsHtml(rows) + '</tbody></table></div><p class="muted">最多 4 个参数层级、80 个 SKU 组合。修改参数或 SKU 后，商品会重新进入待审核。</p></div>';
}

function productAttrRowsHtml(attrs) {
	attrs = attrs || [];
	if (!attrs.length) return '<div class="product-attrs-empty">可添加产地、品牌、保质期、储存方式等展示信息。</div>';
	return attrs.map(function(attr, index) {
		return '<div class="product-attr-row" data-index="' + index + '"><input class="product-attr-name" value="' + escapeHtml(attr.name || "") + '" placeholder="属性名，如产地"><input class="product-attr-value" value="' + escapeHtml(attr.value || "") + '" placeholder="属性值，如上海"><button class="ghost-btn product-attr-delete" type="button">删除</button></div>';
	}).join("");
}

function renderProductAttrsEditor(product) {
	var attrs = productDisplayAttrs(product || {});
	return '<aside class="product-attrs-editor"><div class="product-attrs-head"><div><span>商品属性</span><small>普通展示属性，不参与 SKU、价格、库存和下单组合计算。</small></div><button class="ghost-btn" id="addProductAttr" type="button">添加属性</button></div><div id="productAttrsBox" class="product-attrs-box">' + productAttrRowsHtml(attrs) + '</div><p class="muted">最多 20 个；空属性行不会保存。</p></aside>';
}

function readProductAttrs() {
	var attrs = [];
	Array.prototype.forEach.call(document.querySelectorAll(".product-attr-row"), function(row) {
		var name = String(row.querySelector(".product-attr-name").value || "").trim();
		var value = String(row.querySelector(".product-attr-value").value || "").trim();
		if (name && value) attrs.push({ name: name, value: value });
	});
	return attrs.slice(0, 20);
}

function updateProductAttrsEditor(attrs) {
	var box = document.getElementById("productAttrsBox");
	if (box) box.innerHTML = productAttrRowsHtml((attrs || []).slice(0, 20));
	bindProductAttrsEditor();
}

function bindProductAttrsEditor() {
	var addBtn = document.getElementById("addProductAttr");
	if (addBtn) {
		addBtn.onclick = function() {
			var rows = Array.prototype.map.call(document.querySelectorAll(".product-attr-row"), function(row) {
				return {
					name: String(row.querySelector(".product-attr-name").value || "").trim(),
					value: String(row.querySelector(".product-attr-value").value || "").trim()
				};
			});
			if (rows.length >= 20) {
				alert("商品属性最多支持 20 个。");
				return;
			}
			rows.push({ name: "", value: "" });
			updateProductAttrsEditor(rows);
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".product-attr-delete"), function(btn) {
		btn.onclick = function() {
			var row = btn.closest(".product-attr-row");
			if (row) row.parentNode.removeChild(row);
			if (!document.querySelector(".product-attr-row")) updateProductAttrsEditor([]);
		};
	});
}

function loadReports() {
	if (!state.user && !state.merchant) {
		state.reports = [];
		return Promise.resolve();
	}
	return get("reports?action=my").then(function(data) {
		if (data.success) state.reports = data.reports || [];
		else state.reports = [];
	});
}

function loadMerchantReports() {
	if (!state.merchant) {
		state.merchantMyReports = [];
		state.merchantRelatedReports = [];
		return Promise.resolve();
	}
	return get("merchant/reports?action=list").then(function(data) {
		if (data.success) {
			state.merchantMyReports = data.myReports || [];
			state.merchantRelatedReports = data.relatedReports || [];
		} else {
			state.merchantMyReports = [];
			state.merchantRelatedReports = [];
		}
	});
}

function loadAdminReports() {
	if (!state.admin) {
		state.adminReports = [];
		state.adminReportTotal = 0;
		return Promise.resolve();
	}
	var page = Math.max(1, Number(state.adminReportPage || 1));
	var pageSize = Math.max(1, Math.min(100, Number(state.adminReportPageSize || 20)));
	var query = "admin/reports?action=list&status=" + encodeURIComponent(state.adminReportStatusFilter || "all") + "&keyword=" + encodeURIComponent(state.adminReportKeyword || "") + "&page=" + page + "&pageSize=" + pageSize;
	return get(query).then(function(data) {
		if (data.success) {
			state.adminReports = data.reports || [];
			state.adminReportPage = Number(data.page || page);
			state.adminReportPageSize = Number(data.pageSize || pageSize);
			state.adminReportTotal = Number(data.total || state.adminReports.length || 0);
		} else {
			state.adminReports = [];
			state.adminReportTotal = 0;
		}
	});
}

function readSkuRows() {
	return Array.prototype.map.call(document.querySelectorAll("#skuRowsBody .sku-row"), function(row, index) {
		var values = [];
		try { values = JSON.parse(row.querySelector(".sku-values").value || "[]"); } catch (e) {}
		return {
			skuId: row.querySelector(".sku-id").value || skuKeyFromValues(values) || ("SKU-" + index),
			values: values,
			text: values.join(" / "),
			color: values[0] || "默认",
			spec: values[1] || "标准",
			price: Number(row.querySelector(".sku-price").value || 0),
			oldPrice: Number(row.querySelector(".sku-old-price").value || row.querySelector(".sku-price").value || 0),
			stock: Number(row.querySelector(".sku-stock").value || 0),
			enabled: row.querySelector(".sku-enable").checked,
			skuCode: row.querySelector(".sku-code").value || "",
			imageUrl: row.querySelector(".sku-image").value || ""
		};
	});
}

function normalizeProductMedia(mediaList) {
    var seen = {};
    return (mediaList || []).filter(function(item) { return item && item.mediaUrl; }).map(function(item) {
        return {
            id: item.id || 0,
            productId: item.productId || 0,
            mediaType: String(item.mediaType || "IMAGE").toUpperCase() === "VIDEO" ? "VIDEO" : "IMAGE",
            mediaUrl: item.mediaUrl,
            sortNo: item.sortNo || 0,
            coverFlag: !!item.coverFlag
        };
    }).filter(function(item) {
        var key = item.mediaType + "|" + item.mediaUrl;
        if (seen[key]) return false;
        seen[key] = true;
        return true;
    }).slice(0, 6).map(function(item, index) {
        item.sortNo = index + 1;
        item.coverFlag = index === 0;
        return item;
    });
}

function mediaListForEditor(product) {
    return normalizeProductMedia(productMediaList(product || {}));
}

function merchantMediaItemsHtml(mediaList) {
    mediaList = normalizeProductMedia(mediaList);
    if (!mediaList.length) return '<div class="merchant-media-empty">上传图片或视频后会在这里预览。</div>';
    return mediaList.map(function(media, index) {
        var preview = productMediaHtml(media, "merchant-media-preview");
        var typeText = media.mediaType === "VIDEO" ? "视频" : "图片";
        return '<article class="merchant-media-card ' + (index === 0 ? "is-cover" : "") + '" data-index="' + index + '"><div class="merchant-media-visual">' + preview + '<span>' + typeText + '</span></div><div class="merchant-media-meta"><strong>' + (index === 0 ? "主图" : ("媒体 " + (index + 1))) + '</strong><small>' + escapeHtml(media.mediaUrl) + '</small></div><div class="merchant-media-actions"><button class="ghost-btn merchant-media-main" data-index="' + index + '" type="button">设主图</button><button class="ghost-btn merchant-media-up" data-index="' + index + '" type="button">上移</button><button class="ghost-btn merchant-media-down" data-index="' + index + '" type="button">下移</button><button class="ghost-btn merchant-media-delete" data-index="' + index + '" type="button">删除</button></div></article>';
    }).join("");
}

function renderMerchantMediaManager(product) {
    var mediaList = mediaListForEditor(product || {});
    var imageUrl = mediaList.length ? mediaList[0].mediaUrl : (product && product.imageUrl || "");
    return '<div class="field wide merchant-media-manager"><span>商品媒体</span><input id="mpImageUrl" type="hidden" value="' + escapeHtml(imageUrl) + '"><input id="mpMediaList" type="hidden" value="' + escapeHtml(JSON.stringify(mediaList)) + '"><div class="merchant-media-upload"><input id="mpMediaFile" type="file" accept="image/*,video/mp4,video/webm"><label class="upload-file-btn" for="mpMediaFile">上传媒体</label><span id="mpMediaFileName" class="upload-file-name">最多 6 个图片或视频，第一项为主图</span></div><div class="merchant-media-grid" id="mpMediaGrid">' + merchantMediaItemsHtml(mediaList) + '</div></div>';
}

function readMerchantMedia() {
    var el = document.getElementById("mpMediaList");
    if (!el) return [];
    try { return normalizeProductMedia(JSON.parse(el.value || "[]")); } catch (e) { return []; }
}

function updateMerchantMedia(mediaList) {
    mediaList = normalizeProductMedia(mediaList);
    var hidden = document.getElementById("mpMediaList");
    var image = document.getElementById("mpImageUrl");
    var grid = document.getElementById("mpMediaGrid");
    if (hidden) hidden.value = JSON.stringify(mediaList);
    if (image) image.value = mediaList.length ? mediaList[0].mediaUrl : "";
    if (grid) grid.innerHTML = merchantMediaItemsHtml(mediaList);
    bindMerchantMediaActions();
}

function moveMerchantMedia(index, delta) {
    var mediaList = readMerchantMedia();
    var target = index + delta;
    if (target < 0 || target >= mediaList.length) return;
    var item = mediaList[index];
    mediaList[index] = mediaList[target];
    mediaList[target] = item;
    updateMerchantMedia(mediaList);
}

function bindMerchantMediaActions() {
    Array.prototype.forEach.call(document.querySelectorAll(".merchant-media-main"), function(btn) {
        btn.onclick = function() {
            var index = Number(btn.getAttribute("data-index") || 0);
            var mediaList = readMerchantMedia();
            if (index <= 0 || index >= mediaList.length) return;
            var item = mediaList.splice(index, 1)[0];
            mediaList.unshift(item);
            updateMerchantMedia(mediaList);
        };
    });
    Array.prototype.forEach.call(document.querySelectorAll(".merchant-media-up"), function(btn) {
        btn.onclick = function() { moveMerchantMedia(Number(btn.getAttribute("data-index") || 0), -1); };
    });
    Array.prototype.forEach.call(document.querySelectorAll(".merchant-media-down"), function(btn) {
        btn.onclick = function() { moveMerchantMedia(Number(btn.getAttribute("data-index") || 0), 1); };
    });
    Array.prototype.forEach.call(document.querySelectorAll(".merchant-media-delete"), function(btn) {
        btn.onclick = function() {
            var index = Number(btn.getAttribute("data-index") || 0);
            var mediaList = readMerchantMedia();
            mediaList.splice(index, 1);
            updateMerchantMedia(mediaList);
        };
    });
}

function renderMerchantProductForm(product) {
	product = product || {};
	var options = state.categories.map(function(c) {
		return '<option value="' + c.id + '" ' + (Number(product.categoryId) === Number(c.id) ? "selected" : "") + '>' + escapeHtml(c.name) + '</option>';
	}).join("");
	var idInput = product.id ? '<input type="hidden" id="merchantProductId" value="' + product.id + '">' : '';
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>' + (product.id ? "编辑商品" : "新增商品") + '</h2><p>保存后进入待审核，审核通过后按已启用规格展示价格和库存。</p></div></div><form class="address-form" id="merchantProductForm">' + idInput + '<label class="field"><span>商品名称</span><input id="mpName" value="' + escapeHtml(product.name || "") + '"></label><label class="field"><span>商品分类</span><select id="mpCategory">' + options + '</select></label><label class="field"><span>默认价格</span><input id="mpPrice" type="number" step="0.01" value="' + (product.price || "") + '"></label><label class="field"><span>默认原价</span><input id="mpOldPrice" type="number" step="0.01" value="' + (product.oldPrice || "") + '"></label><label class="field"><span>默认库存</span><input id="mpStock" type="number" value="' + (product.stock || 100) + '"></label>' + renderMerchantMediaManager(product) + '<label class="field wide"><span>商品简介</span><input id="mpShortDesc" value="' + escapeHtml(product.shortDesc || "") + '"></label><label class="field wide"><span>商品详情</span><input id="mpDetailDesc" value="' + escapeHtml(product.detailDesc || "") + '"></label>' + renderSkuEditor(product) + renderProductAttrsEditor(product) + '<button class="primary-btn merchant-submit-btn" type="submit">保存并提交审核</button><button class="ghost-btn merchant-back-btn" data-page="merchantProductList" type="button">返回列表</button></form></section>';
}
function adminMerchantProducts(merchantId) {
	return state.adminAuditProducts.filter(function(p) { return String(p.merchantId) === String(merchantId); });
}

function merchantProductStats(merchantId) {
	var products = adminMerchantProducts(merchantId);
	return {
		total: products.length,
		onSale: products.filter(function(p) { return p.saleStatus === "ON_SALE"; }).length,
		offSale: products.filter(function(p) { return p.saleStatus !== "ON_SALE"; }).length,
		pending: products.filter(function(p) { return p.auditStatus === "PENDING"; }).length
	};
}

function adminCategoryOptions(selectedId) {
	return state.categories.map(function(c) {
		return '<option value="' + c.id + '" ' + (String(c.id) === String(selectedId) ? "selected" : "") + '>' + escapeHtml(c.name) + '</option>';
	}).join("");
}

function merchantMatchesSearch(merchant) {
	var kw = String(state.merchantManageSearch || "").trim().toLowerCase();
	if (!kw) return true;
	return [merchant.merchantCode, merchant.merchantName, merchant.shopName, merchant.contactPhone, merchant.email].some(function(value) {
		return String(value || "").toLowerCase().indexOf(kw) >= 0;
	});
}

function syncAdminProductMediaPayload(payload) {
    if (!payload || !payload.imageUrl) return;
    var mediaList = [];
    try { mediaList = JSON.parse(payload.mediaList || "[]"); } catch (e) { mediaList = []; }
    mediaList = normalizeProductMedia(mediaList);
    if (mediaList.length) {
        mediaList[0].mediaUrl = payload.imageUrl;
        mediaList[0].mediaType = "IMAGE";
    } else {
        mediaList.push({ mediaType: "IMAGE", mediaUrl: payload.imageUrl, sortNo: 1, coverFlag: true });
    }
    payload.mediaList = JSON.stringify(normalizeProductMedia(mediaList));
}

function adminProductMediaSummary(product) {
    var mediaList = productMediaList(product || {});
    var count = mediaList.length;
    var previews = mediaList.slice(0, 4).map(function(media) { return '<span class="admin-product-media-mini ' + (media.mediaType === "VIDEO" ? "video" : "") + '">' + productMediaHtml(media, "admin-product-media-img") + '</span>'; }).join("");
    return '<div class="admin-product-media-summary">' + productThumb(product, "admin-merchant-thumb") + '<small>媒体 ' + count + '</small><div>' + previews + '</div></div>';
}

function renderAdminMerchantProducts(merchant) {
	var auditFilter = state.merchantProductAuditFilter || "all";
	var saleFilter = state.merchantProductSaleFilter || "all";
	var rows = adminMerchantProducts(merchant.merchantId).filter(function(p) {
		return (auditFilter === "all" || p.auditStatus === auditFilter) && (saleFilter === "all" || p.saleStatus === saleFilter);
	}).map(function(p) {
		var saleAction = p.saleStatus === "ON_SALE" ? "OFF_SALE" : "ON_SALE";
		var saleText = p.saleStatus === "ON_SALE" ? "下架" : "上架";
		var skuJson = JSON.stringify(skuRowsForProduct(p));
		var skuAttrsJson = JSON.stringify(skuAttrsForEditor(p));
		var colors = optionListText(p.colorOptions || skuRowsForProduct(p).map(function(row) { return row.color; }), "默认");
		var specs = optionListText(p.specOptions || skuRowsForProduct(p).map(function(row) { return row.spec; }), "标准");
		return '<tr><td><b>' + p.id + '</b><p class="muted">' + badge(statusText(p.auditStatus), p.auditStatus === "APPROVED" ? "green" : "amber") + ' ' + badge(statusText(p.saleStatus), p.saleStatus === "ON_SALE" ? "green" : "amber") + '</p><p class="muted">' + escapeHtml(p.auditOpinion || "") + '</p></td>' +
			'<td>' + adminProductMediaSummary(p) + '</td>' +
			'<td><input class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="name" value="' + escapeHtml(p.name || "") + '"><input class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="shortDesc" value="' + escapeHtml(p.shortDesc || "") + '" placeholder="商品简介"><input class="merchant-product-field" type="hidden" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="colorOptions" value="' + escapeHtml(colors) + '"><input class="merchant-product-field" type="hidden" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="specOptions" value="' + escapeHtml(specs) + '"><input class="merchant-product-field" type="hidden" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="skuAttrs" value="' + escapeHtml(skuAttrsJson) + '"><input class="merchant-product-field" type="hidden" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="skuOptions" value="' + escapeHtml(skuJson) + '"><input class="merchant-product-field" type="hidden" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="mediaList" value="' + escapeHtml(JSON.stringify(mediaListForEditor(p))) + '"></td>' +
			'<td><select class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="categoryId">' + adminCategoryOptions(p.categoryId) + '</select></td>' +
			'<td><input class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="price" type="number" step="0.01" value="' + escapeHtml(p.price || 0) + '"><input class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="oldPrice" type="number" step="0.01" value="' + escapeHtml(p.oldPrice || 0) + '" placeholder="原价"></td>' +
			'<td><input class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="stock" type="number" value="' + escapeHtml(p.stock || 0) + '"></td>' +
			'<td>' + skuSummaryHtml(p) + '</td>' +
			'<td><input class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="imageUrl" value="' + escapeHtml(p.imageUrl || "") + '" placeholder="图片地址"><input class="admin-input merchant-product-field" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-field="detailDesc" value="' + escapeHtml(p.detailDesc || "") + '" placeholder="商品详情"></td>' +
			'<td><div class="merchant-product-actions"><input class="admin-input product-audit-opinion" data-id="' + p.id + '" placeholder="驳回原因"><button class="ghost-btn merchant-product-save" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" type="button">保存</button><button class="ghost-btn merchant-product-audit" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-action="approve" type="button">通过</button><button class="ghost-btn merchant-product-audit" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-action="reject" type="button">驳回</button><button class="ghost-btn merchant-product-sale" data-id="' + p.id + '" data-merchant="' + merchant.merchantId + '" data-status="' + saleAction + '" type="button">' + saleText + '</button></div></td></tr>';
	}).join("") || '<tr><td colspan="9">暂无符合条件的商品。</td></tr>';
	function filterBtn(kind, value, text) {
		var active = (kind === "audit" ? auditFilter : saleFilter) === value ? " active" : "";
		return '<button class="merchant-product-filter' + active + '" data-kind="' + kind + '" data-value="' + value + '" type="button">' + text + '</button>';
	}
	return '<div class="merchant-products-panel"><div class="merchant-products-head"><div><h3>商家商品</h3><p>审核、上下架、规格价格和基础资料都在当前商家下完成。</p></div><div class="segmented merchant-product-filterbar">' + filterBtn("audit", "all", "全部审核") + filterBtn("audit", "PENDING", "待审核") + filterBtn("audit", "APPROVED", "已通过") + filterBtn("audit", "REJECTED", "已驳回") + filterBtn("sale", "all", "全部上下架") + filterBtn("sale", "ON_SALE", "上架中") + filterBtn("sale", "OFF_SALE", "已下架") + '</div></div><div class="admin-table merchant-product-table"><table><thead><tr><th>状态</th><th>图片</th><th>商品</th><th>分类</th><th>价格</th><th>库存</th><th>规格</th><th>详情/图片地址</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div></div>';
}
function renderAdminMerchantAudit() {
	var tab = state.merchantAuditFilter || "pending";
	var allPendingMerchants = state.merchants.filter(function(m) { return m.status === "PENDING" || m.status === "REJECTED"; });
	var allReviewedMerchants = state.merchants.filter(function(m) { return m.status === "APPROVED" || m.status === "DISABLED"; });
	var pendingMerchants = allPendingMerchants.filter(merchantMatchesSearch);
	var reviewedMerchants = allReviewedMerchants.filter(merchantMatchesSearch);
	var pendingRows = pendingMerchants.map(function(m) {
		return '<tr><td><b>' + escapeHtml(m.merchantCode) + '</b></td><td>' + escapeHtml(m.merchantName) + '<p class="muted">' + escapeHtml(m.shopName) + '</p></td><td>' + escapeHtml(m.contactName || "") + '<p class="muted">' + escapeHtml(m.contactPhone || "") + '</p></td><td>' + escapeHtml(m.email || "") + '</td><td>' + escapeHtml(m.businessCategory || "") + '<p class="muted">' + escapeHtml(m.businessAddress || "") + '</p></td><td>' + badge(statusText(m.status), m.status === "REJECTED" ? "amber" : "") + '<p class="muted">' + escapeHtml(m.rejectReason || "") + '</p></td><td><div class="merchant-pending-actions"><input class="admin-input audit-opinion" data-id="' + m.merchantId + '" placeholder="驳回原因"><div class="merchant-pending-buttons"><button class="primary-btn merchant-audit" data-id="' + m.merchantId + '" data-status="APPROVED" type="button">通过</button><button class="ghost-btn merchant-audit" data-id="' + m.merchantId + '" data-status="REJECTED" type="button">驳回</button></div></div></td></tr>';
	}).join("") || '<tr><td colspan="7">暂无待审核商家。</td></tr>';
	var reviewedRows = reviewedMerchants.map(function(m) {
		var stats = merchantProductStats(m.merchantId);
		var expanded = String(state.merchantExpandedId || "") === String(m.merchantId);
		var toggleStatus = m.status === "DISABLED" ? "APPROVED" : "DISABLED";
		var toggleText = m.status === "DISABLED" ? "启用" : "禁用";
		var row = '<tr><td><b>' + escapeHtml(m.merchantCode) + '</b></td>' +
			'<td><input class="admin-input merchant-field" data-id="' + m.merchantId + '" data-field="merchantName" value="' + escapeHtml(m.merchantName || "") + '"><input class="admin-input merchant-field" data-id="' + m.merchantId + '" data-field="shopName" value="' + escapeHtml(m.shopName || "") + '" placeholder="店铺名称"></td>' +			'<td><div class="password-cell"><input class="admin-input merchant-field merchant-password-input" data-id="' + m.merchantId + '" data-field="password" type="password" value="' + escapeHtml(m.registerPasswordDemo || "") + '"><button class="ghost-btn password-toggle" data-id="' + m.merchantId + '" type="button">显示</button></div></td>' +
			'<td><input class="admin-input merchant-field" data-id="' + m.merchantId + '" data-field="contactPhone" value="' + escapeHtml(m.contactPhone || "") + '"><input class="admin-input merchant-field" data-id="' + m.merchantId + '" data-field="email" value="' + escapeHtml(m.email || "") + '" placeholder="邮箱"></td>' +
			'<td><input class="admin-input merchant-field" data-id="' + m.merchantId + '" data-field="businessCategory" value="' + escapeHtml(m.businessCategory || "") + '"><input class="admin-input merchant-field" data-id="' + m.merchantId + '" data-field="businessAddress" value="' + escapeHtml(m.businessAddress || "") + '" placeholder="经营地址"></td>' +
			'<td>' + badge(statusText(m.status), m.status === "APPROVED" ? "green" : "amber") + '</td><td><b>' + stats.total + '</b></td><td>' + stats.onSale + '</td><td>' + stats.offSale + '</td><td>' + stats.pending + '<p class="muted">待处理</p></td>' +			'<td><div class="merchant-action-group"><button class="primary-btn merchant-save" data-id="' + m.merchantId + '" type="button">保存资料</button><button class="ghost-btn merchant-audit" data-id="' + m.merchantId + '" data-status="' + toggleStatus + '" type="button">' + toggleText + '</button><button class="ghost-btn merchant-products-toggle" data-id="' + m.merchantId + '" type="button">' + (expanded ? "收起商品" : "查看/管理商品") + '</button></div></td></tr>';
		return row;
	}).join("") || '<tr><td colspan="11">暂无已审核商家。</td></tr>';
	var expandedMerchant = reviewedMerchants.filter(function(m) { return String(state.merchantExpandedId || "") === String(m.merchantId); })[0];
	var productPanel = expandedMerchant ? renderAdminMerchantProducts(expandedMerchant) : "";
	var pendingActive = tab === "pending" ? "active" : "";
	var reviewedActive = tab === "approved" ? "active" : "";
	var pendingView = '<div class="admin-table tall merchant-pending-table"><table><thead><tr><th>商家编号</th><th>商家/店铺</th><th>联系人</th><th>邮箱</th><th>经营信息</th><th>状态</th><th>操作</th></tr></thead><tbody>' + pendingRows + '</tbody></table></div>';
	var reviewedView = '<div class="section-head compact-head"><div><h3>已审核商家</h3><p>按商家维护资料，并在展开区管理该商家的商品。</p></div></div><div class="admin-table tall merchant-reviewed-table"><table><thead><tr><th>商家编号</th><th>商家/店铺</th><th>登录密码</th><th>手机号/邮箱</th><th>经营信息</th><th>状态</th><th>商品数</th><th>上架</th><th>下架</th><th>待审</th><th>操作</th></tr></thead><tbody>' + reviewedRows + '</tbody></table></div>' + productPanel;
	var toolbar = '<div class="merchant-audit-toolbar"><label class="field admin-search-field"><span>搜索</span><input id="merchantManageSearch" value="' + escapeHtml(state.merchantManageSearch || "") + '" placeholder="商家名称、编号、手机号、邮箱"></label></div>';
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>商家管理</h2><p>后台按商家统一管理入驻审核、资料维护和商家商品。</p></div><div class="segmented merchant-segmented"><button class="merchant-filter pending-filter ' + pendingActive + '" data-filter="pending" type="button">待审核 <span class="segment-badge danger">' + allPendingMerchants.length + '</span></button><button class="merchant-filter approved-filter ' + reviewedActive + '" data-filter="approved" type="button">已审核 <span class="segment-badge">' + allReviewedMerchants.length + '</span></button></div></div>' + toolbar + (tab === "approved" ? reviewedView : pendingView) + '</section>';
}
function couponOwnerText(c) {
	return c.couponOwnerType === "MERCHANT" ? ((c.shopName || "店铺") + "专属") : (c.stackable ? "平台可叠加" : "平台券");
}

function renderAdminCouponTabs() {
	var tabs = [["templates", "模板管理"], ["issue", "发放优惠券"], ["home", "首页展示"], ["logs", "发放记录"]];
	return '<div class="admin-user-tabs coupon-center-tabs">' + tabs.map(function(tab) { return '<button class="admin-user-tab coupon-center-tab ' + (state.couponManageTab === tab[0] ? "active" : "") + '" data-tab="' + tab[0] + '" type="button">' + tab[1] + '</button>'; }).join("") + '</div>';
}

function renderAdminCouponTemplateTab() {
	var rows = state.couponTemplates.filter(function(c) { return c.couponOwnerType !== "MERCHANT"; }).map(function(c) {
		var next = c.status === "ENABLED" ? "DISABLED" : "ENABLED";
		return '<tr><td>' + c.couponId + '</td><td><b>' + escapeHtml(c.couponName) + '</b><p class="muted">' + escapeHtml(couponOwnerText(c)) + ' · ' + couponValueText(c) + '</p></td><td>' + couponTypeText(c.couponType) + '</td><td>' + (c.stackable ? badge("可叠加", "green") : badge("普通", "")) + '</td><td>' + escapeHtml(c.homeTitle || c.couponName || "") + '<p class="muted">' + escapeHtml(c.homeSubtitle || "") + '</p></td><td>' + escapeHtml(c.description || "") + '</td><td>' + badge(statusText(c.status), c.status === "ENABLED" ? "green" : "amber") + '</td><td><button class="ghost-btn coupon-status" data-id="' + c.couponId + '" data-status="' + next + '" type="button">' + statusText(next) + '</button><button class="ghost-btn coupon-delete" data-id="' + c.couponId + '" type="button">删除</button></td></tr>';
	}).join("") || '<tr><td colspan="8">暂无平台优惠券模板。</td></tr>';
	return '<form class="address-form" id="couponTemplateForm"><label class="field"><span>优惠券名称</span><input id="ctName" placeholder="如：会员加码券"></label><label class="field"><span>类型</span><select id="ctType"><option value="AMOUNT">满减券</option><option value="DISCOUNT">折扣券</option><option value="NEW_USER">新人券</option><option value="VIP">VIP专属券</option></select></label><label class="field coupon-amount-field"><span>优惠金额</span><input id="ctAmount" type="number" step="0.01" value="5"></label><label class="field coupon-discount-field hidden"><span>折扣比例</span><input id="ctDiscountRate" type="number" step="0.01" value="0.95"></label><label class="field"><span>最低消费</span><input id="ctMinAmount" type="number" step="0.01" value="39"></label><label class="field coupon-vip-field hidden"><span>适用VIP</span><input id="ctVipLevel" type="number" min="0" max="10" value="0"></label><label class="field"><span>每人限领</span><input id="ctLimit" type="number" value="1"></label><label class="field"><span>有效天数</span><input id="ctValidDays" type="number" value="30"></label><label class="field"><span>首页主标题</span><input id="ctHomeTitle" placeholder="首页展示标题"></label><label class="field"><span>首页副标题</span><input id="ctHomeSubtitle" placeholder="如：满99减5"></label><label class="field wide"><span>使用说明</span><input id="ctDescription" placeholder="用于用户端展示的简短说明"></label><label class="check-field coupon-new-field hidden"><input id="ctNewUser" type="checkbox"> 新人优惠券</label><label class="check-field coupon-vip-field hidden"><input id="ctVipCoupon" type="checkbox"> VIP专属优惠券</label><label class="check-field"><input id="ctStackable" type="checkbox"> 可作为叠加券</label><button class="primary-btn" type="submit">保存模板</button></form><div class="admin-table"><table><thead><tr><th>ID</th><th>名称</th><th>类型</th><th>叠加</th><th>首页展示</th><th>说明</th><th>状态</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderCouponIssueForm(isMerchant) {
	var form = state.couponIssueForm || { couponId: "", issueType: "VIP_LEVEL", targetValue: "2", batch: true };
	var templates = isMerchant ? state.merchantCoupons : state.couponTemplates.filter(function(c) { return c.couponOwnerType !== "MERCHANT"; });
	var groupValues = String(form.targetValue || "").split(",");
	var groupOptions = [["NEW", "新用户（注册30天内）"], ["ORDERED", isMerchant ? "本店有订单用户" : "已下单用户"], ["NO_ORDER", "未下单用户"], ["CART", isMerchant ? "本店购物车用户" : "购物车有商品用户"], ["HIGH", "高价值用户"]].map(function(item) { return '<label class="target-group-option"><input type="checkbox" value="' + item[0] + '" ' + (groupValues.indexOf(item[0]) >= 0 ? "checked" : "") + '> ' + item[1] + '</label>'; }).join("");
	var options = templates.filter(function(c) { return c.status === "ENABLED"; }).map(function(c) { return '<option value="' + c.couponId + '" ' + (String(form.couponId) === String(c.couponId) ? "selected" : "") + '>' + escapeHtml(c.couponName) + ' · ' + couponValueText(c) + '</option>'; }).join("");
	return '<form class="address-form coupon-issue-form ' + (isMerchant ? "merchant-coupon-issue-form" : "admin-coupon-issue-form") + '" id="couponIssueForm" data-merchant="' + (isMerchant ? "1" : "0") + '">' + (!isMerchant ? '<label class="check-field wide"><input id="ciBatch" type="checkbox" ' + (form.batch ? "checked" : "") + '> 批量发放匹配模板</label>' : '<input id="ciBatch" type="checkbox" class="hidden">') + '<label class="field wide"><span>优惠券模板</span><select id="ciCouponId"><option value="">请选择模板</option>' + options + '</select></label><label class="field"><span>发放方式</span><select id="ciIssueType"><option value="VIP_LEVEL" ' + (form.issueType === "VIP_LEVEL" ? "selected" : "") + '>按VIP等级</option><option value="USER" ' + (form.issueType === "USER" ? "selected" : "") + '>按用户ID</option><option value="USERNAME" ' + (form.issueType === "USERNAME" ? "selected" : "") + '>按用户名/手机号</option><option value="USER_GROUP" ' + (form.issueType === "USER_GROUP" ? "selected" : "") + '>按用户类别</option><option value="ALL" ' + (form.issueType === "ALL" ? "selected" : "") + '>全体用户</option></select></label><label class="field"><span>目标值</span><input id="ciTargetValue" value="' + escapeHtml(form.targetValue || "") + '" placeholder="请输入VIP等级，如：5"><details id="ciTargetGroups" class="target-group-select hidden"><summary id="ciTargetGroupSummary">请选择用户类别</summary><div class="target-group-menu">' + groupOptions + '</div></details></label><div class="form-actions"><button class="primary-btn" type="submit">发放优惠券</button></div></form>';
}

function renderAdminCouponIssueTab() {
	return renderCouponIssueForm(false);
}

function renderAdminCouponHomeTab() {
	var rows = state.couponTemplates.filter(function(c) { return c.status === "ENABLED"; }).map(function(c) { return '<tr><td>' + escapeHtml(c.homeTitle || c.couponName || "") + '</td><td>' + escapeHtml(c.homeSubtitle || couponValueText(c)) + '</td><td>' + escapeHtml(couponOwnerText(c)) + '</td><td>' + escapeHtml(c.description || "") + '</td></tr>'; }).join("") || '<tr><td colspan="4">暂无可展示优惠券。</td></tr>';
	return '<div class="admin-table"><table><thead><tr><th>主标题</th><th>副标题</th><th>归属</th><th>说明</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderCouponLogTable(logs) {
	var rows = (logs || []).map(function(log) { return '<tr><td>' + log.issueLogId + '</td><td>' + escapeHtml(log.couponName || "") + '</td><td>' + escapeHtml(log.issueType) + '</td><td>' + escapeHtml(log.targetValue || "") + '</td><td>' + log.issueCount + '</td><td>' + log.skipCount + '</td><td>' + escapeHtml(log.issueBatchNo || "") + '</td><td>' + escapeHtml(log.issueTime || "") + '</td><td>' + escapeHtml(log.adminId || "") + '</td></tr>'; }).join("") || '<tr><td colspan="9">暂无发放记录。</td></tr>';
	return '<div class="admin-table"><table><thead><tr><th>ID</th><th>优惠券</th><th>对象</th><th>目标</th><th>成功</th><th>跳过</th><th>批次</th><th>时间</th><th>操作人</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderAdminCouponCenter() {
	var body = state.couponManageTab === "issue" ? renderAdminCouponIssueTab() : (state.couponManageTab === "home" ? renderAdminCouponHomeTab() : (state.couponManageTab === "logs" ? renderCouponLogTable(state.couponLogs) : renderAdminCouponTemplateTab()));
	return '<section class="panel-card admin-section coupon-center"><div class="section-head"><div><h2>优惠券管理</h2><p>统一管理平台券、可叠加券、首页展示和发放记录。</p></div></div>' + renderAdminCouponTabs() + body + '</section>';
}

function renderAdminCouponManage() { return renderAdminCouponCenter(); }
function renderAdminCouponIssue() { state.couponManageTab = "issue"; return renderAdminCouponCenter(); }
function renderAddress() {
	var provinces = regionData();
	var defaultProvince = provinces[0] ? provinces[0].name : "";
	var cities = regionCities(defaultProvince);
	var defaultCity = cities[0] ? cities[0].name : "";
	var districts = regionDistricts(defaultProvince, defaultCity);
	var defaultDistrict = districts[0] ? districts[0].name : "";
	var cards = state.addresses.map(function(addr) {
		return '<article class="address-card"><div class="row"><div><h3>' + escapeHtml(addr.receiverName) + '</h3><p class="muted">' + escapeHtml(addr.phone) + '</p></div>' + (addr.defaultAddress ? badge("默认", "green") : '<span class="icon-tile">址</span>') + '</div><strong>' + escapeHtml(addr.province + addr.city + addr.district) + '</strong><p class="muted">' + escapeHtml(addr.detail) + '</p><div class="order-actions"><button class="ghost-btn delete-address" data-id="' + addr.id + '" type="button">删除</button>' + (!addr.defaultAddress ? '<button class="primary-btn default-address" data-id="' + addr.id + '" type="button">设为默认</button>' : '') + '</div></article>';
	}).join("");
	return '<div class="section-head"><div><h2>收货地址</h2><p>填写手机号、省市区和详细地址后保存，可设为默认地址。</p></div></div><form class="panel-card address-form" id="addressForm"><label class="field"><span>收货人</span><input id="addrName" value="' + escapeHtml(state.user ? state.user.username : "") + '" placeholder="请输入收货人"></label><label class="field"><span>手机号</span><input id="addrPhone" value="' + escapeHtml(state.user ? state.user.phone || "" : "") + '" placeholder="请输入中国大陆手机号"></label><label class="field"><span>省份</span><select id="addrProvince">' + optionHtml(provinces, defaultProvince) + '</select></label><label class="field"><span>城市</span><select id="addrCity">' + optionHtml(cities, defaultCity) + '</select></label><label class="field"><span>区县</span><select id="addrDistrict">' + optionHtml(districts, defaultDistrict) + '</select></label><label class="field wide"><span>详细地址</span><input id="addrDetail" placeholder="街道、门牌号、楼层房间号"></label><label class="check-field"><input id="addrDefault" type="checkbox" ' + (state.addresses.length ? "" : "checked") + '> 设为默认地址</label><button class="primary-btn" type="submit">保存地址</button></form><div class="address-grid">' + cards + '</div>';
}

function selectedAdminUser() {
	return state.adminUsers.filter(function(user) { return String(user.id) === String(state.adminSelectedUserId); })[0] || state.adminUsers[0] || null;
}

function adminUserOrders(userId) {
	return state.adminOrders.filter(function(order) { return String(order.userId) === String(userId); });
}

function adminUserAddresses(userId) {
	return (state.adminAddresses || []).filter(function(address) { return String(address.userId) === String(userId); });
}

function adminUserCoupons(userId) {
	return (state.adminUserCoupons || []).filter(function(coupon) { return String(coupon.userId) === String(userId); });
}

function adminUserSpend(userId) {
	return adminUserOrders(userId).reduce(function(sum, order) { return sum + Number(order.totalAmount || 0); }, 0);
}

function adminUserMatchesFilters(user) {
	var keyword = String(state.adminUserSearch || "").trim().toLowerCase();
	var values = [user.id, user.accountId, user.username, user.phone, user.email].join(" ").toLowerCase();
	if (keyword && values.indexOf(keyword) < 0) return false;
	if (state.adminUserStatusFilter !== "all" && String(user.status || "") !== state.adminUserStatusFilter) return false;
	if (state.adminUserVipFilter !== "all" && String(currentVip(user).level) !== String(state.adminUserVipFilter)) return false;
	var hasOrder = adminUserOrders(user.id).length > 0;
	if (state.adminUserOrderFilter === "has" && !hasOrder) return false;
	if (state.adminUserOrderFilter === "none" && hasOrder) return false;
	return true;
}

function adminUserRows() {
	var rows = state.adminUsers.filter(adminUserMatchesFilters);
	rows.sort(function(a, b) {
		if (state.adminUserSort === "spendDesc") return adminUserSpend(b.id) - adminUserSpend(a.id);
		if (state.adminUserSort === "spendAsc") return adminUserSpend(a.id) - adminUserSpend(b.id);
		var at = Date.parse(a.createTime || "") || 0;
		var bt = Date.parse(b.createTime || "") || 0;
		return state.adminUserSort === "createAsc" ? at - bt : bt - at;
	});
	return rows;
}

function renderAdminUserFilters() {
	var vipOptions = ['<option value="all">全部VIP</option>'];
	vipRules.forEach(function(rule) {
		vipOptions.push('<option value="' + rule.level + '" ' + (String(state.adminUserVipFilter) === String(rule.level) ? "selected" : "") + '>VIP' + rule.level + '</option>');
	});
	return '<div class="admin-user-toolbar"><label class="field"><span>搜索</span><input id="adminUserSearch" value="' + escapeHtml(state.adminUserSearch || "") + '" placeholder="用户ID、用户名、手机号、邮箱"></label><label class="field"><span>状态</span><select id="adminUserStatusFilter"><option value="all">全部状态</option><option value="正常" ' + (state.adminUserStatusFilter === "正常" ? "selected" : "") + '>正常</option><option value="停用" ' + (state.adminUserStatusFilter === "停用" ? "selected" : "") + '>停用</option></select></label><label class="field"><span>VIP</span><select id="adminUserVipFilter">' + vipOptions.join("") + '</select></label><label class="field"><span>订单</span><select id="adminUserOrderFilter"><option value="all">全部用户</option><option value="has" ' + (state.adminUserOrderFilter === "has" ? "selected" : "") + '>有订单</option><option value="none" ' + (state.adminUserOrderFilter === "none" ? "selected" : "") + '>无订单</option></select></label><label class="field"><span>排序</span><select id="adminUserSort"><option value="createDesc" ' + (state.adminUserSort === "createDesc" ? "selected" : "") + '>注册时间从新到旧</option><option value="createAsc" ' + (state.adminUserSort === "createAsc" ? "selected" : "") + '>注册时间从旧到新</option><option value="spendDesc" ' + (state.adminUserSort === "spendDesc" ? "selected" : "") + '>消费金额从高到低</option><option value="spendAsc" ' + (state.adminUserSort === "spendAsc" ? "selected" : "") + '>消费金额从低到高</option></select></label></div>';
}

function renderAdminUserList() {
	var rows = adminUserRows().map(function(user) {
		var orders = adminUserOrders(user.id);
		var coupons = adminUserCoupons(user.id);
		var vip = currentVip(user);
		var active = String(state.adminSelectedUserId) === String(user.id) ? " active" : "";
		return '<tr class="admin-user-row' + active + '" data-id="' + user.id + '"><td><b>' + escapeHtml(user.accountId || user.id) + '</b></td><td>' + escapeHtml(user.username) + '</td><td>' + escapeHtml(user.phone || "") + '</td><td>' + escapeHtml(user.email || "") + '</td><td>' + badge(user.status || "正常", user.status === "停用" ? "red" : "green") + '</td><td><span class="admin-vip"><img src="' + vipBadgeSrc(vip.level) + '" alt="VIP' + vip.level + '"><b>VIP' + vip.level + '</b><small>' + escapeHtml(vip.name) + '</small></span></td><td>' + growthValue(user) + '</td><td>' + Number(user.points || 0) + '</td><td>' + orders.length + '</td><td><b>' + money(adminUserSpend(user.id)) + '</b></td><td>' + coupons.length + '</td><td>' + escapeHtml((user.createTime || "").slice(0, 19)) + '</td><td><button class="danger-btn admin-user-delete" data-id="' + user.id + '" type="button">删除</button></td></tr>';
	}).join("") || '<tr><td colspan="13">暂无匹配用户。</td></tr>';
	return '<div class="admin-table tall admin-user-table"><table><thead><tr><th>用户ID</th><th>用户名</th><th>手机号</th><th>邮箱</th><th>状态</th><th>VIP等级</th><th>成长值</th><th>积分</th><th>订单数</th><th>消费总额</th><th>优惠券</th><th>注册时间</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderAdminUserTabs() {
	var tabs = [
		{ key: "basic", label: "基础信息" },
		{ key: "orders", label: "订单记录" },
		{ key: "addresses", label: "地址信息" },
		{ key: "vip", label: "会员积分" },
		{ key: "coupons", label: "优惠券" },
		{ key: "logs", label: "操作记录" }
	];
	return '<div class="admin-user-tabs">' + tabs.map(function(tab) { return '<button class="admin-user-tab ' + (state.adminUserDetailTab === tab.key ? "active" : "") + '" data-tab="' + tab.key + '" type="button">' + tab.label + '</button>'; }).join("") + '</div>';
}

function renderAdminUserBasic(user) {
	var visible = !!state.adminUserPasswordVisible[user.id];
	var passwordText = visible ? (user.currentPassword || "") : "&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;";
	return '<div class="admin-user-form"><label class="field"><span>用户名</span><input class="admin-input" id="adminUserName" value="' + escapeHtml(user.username) + '"></label><label class="field"><span>手机号</span><input class="admin-input" id="adminUserPhone" value="' + escapeHtml(user.phone || "") + '"></label><label class="field"><span>邮箱</span><input class="admin-input" id="adminUserEmail" value="' + escapeHtml(user.email || "") + '"></label><label class="field"><span>当前密码</span><div class="admin-password-view"><code>' + (visible ? escapeHtml(passwordText) : passwordText) + '</code><button class="ghost-btn admin-user-password-toggle ' + (visible ? "visible" : "") + '" data-id="' + user.id + '" type="button" title="' + (visible ? "隐藏当前密码" : "显示当前密码") + '" aria-label="' + (visible ? "隐藏当前密码" : "显示当前密码") + '"><span class="eye-icon" aria-hidden="true"></span></button></div></label><label class="field"><span>新密码</span><input class="admin-input" id="adminUserPassword" type="password" placeholder="留空则不修改"></label><label class="field"><span>状态</span><select class="admin-input" id="adminUserStatus"><option ' + (user.status === "正常" ? "selected" : "") + '>正常</option><option ' + (user.status === "停用" ? "selected" : "") + '>停用</option></select></label><div class="profile-metrics"><div><b>' + adminUserOrders(user.id).length + '</b><span>订单</span></div><div><b>' + money(adminUserSpend(user.id)) + '</b><span>消费</span></div><div><b>' + adminUserCoupons(user.id).length + '</b><span>优惠券</span></div></div><button class="primary-btn admin-user-save" data-id="' + user.id + '" type="button">保存基础信息</button></div>';
}

function renderAdminUserOrderItems(order) {
	return (order.items || []).map(function(item) { return escapeHtml((item.product && item.product.name || "商品") + " x " + item.quantity); }).join("、") || "无商品明细";
}

function renderAdminUserOrders(user) {
	var statuses = ["待付款", "待发货", "待收货", "已完成", "售后", "已取消"];
	var orders = adminUserOrders(user.id).filter(function(order) { return state.adminUserOrderStatusFilter === "all" || order.status === state.adminUserOrderStatusFilter; });
	var rows = orders.map(function(order) {
		var options = statuses.map(function(status) { return '<option ' + (order.status === status ? "selected" : "") + '>' + status + '</option>'; }).join("");
		return '<tr><td><b>' + escapeHtml(order.orderNo) + '</b><p class="muted">' + escapeHtml(order.createTime || "") + '</p></td><td>' + renderAdminUserOrderItems(order) + '</td><td>' + escapeHtml(order.receiverName || "") + '<p class="muted">' + escapeHtml(order.receiverPhone || "") + '</p></td><td>' + escapeHtml(order.receiverAddress || "") + '</td><td><b>' + money(order.totalAmount) + '</b><p class="muted">优惠 -' + money(order.discountAmount || 0) + '</p></td><td><select class="admin-input admin-user-order-status" data-id="' + order.id + '">' + options + '</select></td><td><div class="user-center-actions"><button class="primary-btn admin-user-order-save" data-id="' + order.id + '" type="button">修改状态</button><button class="ghost-btn admin-user-order-delete" data-id="' + order.id + '" type="button">删除订单</button></div></td></tr>';
	}).join("") || '<tr><td colspan="7">该用户暂无订单。</td></tr>';
	return '<div class="section-head compact-head"><div><h3>订单记录</h3><p>在用户详情内查看并维护该用户订单。</p></div><select class="admin-input compact-select" id="adminUserOrderStatusFilter"><option value="all">全部状态</option>' + statuses.map(function(status) { return '<option value="' + status + '" ' + (state.adminUserOrderStatusFilter === status ? "selected" : "") + '>' + status + '</option>'; }).join("") + '</select></div><div class="admin-table user-center-orders"><table><thead><tr><th>订单编号</th><th>商品摘要</th><th>收货人</th><th>收货地址</th><th>金额</th><th>订单状态</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderAdminUserAddresses(user) {
	var rows = adminUserAddresses(user.id).map(function(addr) {
		return '<tr><td>' + escapeHtml(addr.receiverName || "") + '</td><td>' + escapeHtml(addr.phone || "") + '</td><td>' + escapeHtml([addr.province, addr.city, addr.district].join("")) + '</td><td>' + escapeHtml(addr.detail || "") + '</td><td>' + (addr.defaultAddress ? badge("默认", "green") : badge("普通", "")) + '</td><td><div class="user-center-actions"><button class="primary-btn admin-user-address-default" data-id="' + addr.id + '" data-user="' + user.id + '" type="button" ' + (addr.defaultAddress ? "disabled" : "") + '>设为默认</button><button class="ghost-btn admin-user-address-delete" data-id="' + addr.id + '" data-user="' + user.id + '" type="button">删除地址</button></div></td></tr>';
	}).join("") || '<tr><td colspan="6">该用户暂无地址。</td></tr>';
	return '<div class="admin-table user-center-addresses"><table><thead><tr><th>收货人</th><th>手机号</th><th>省市区</th><th>详细地址</th><th>默认</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderAdminUserVip(user) {
	var progress = vipProgress(user);
	return '<div class="admin-user-form vip-admin-panel"><div class="vip-mini-card"><img src="' + vipBadgeSrc(progress.vip.level) + '" alt="VIP' + progress.vip.level + '"><div><h3>VIP' + progress.vip.level + ' ' + escapeHtml(progress.vip.name) + '</h3><p class="muted">' + escapeHtml(progress.nextText) + '</p><p>当前折扣：<b>' + vipDiscountText(progress.vip) + '</b>，权益：' + escapeHtml(progress.vip.benefits.join("、")) + '</p></div></div><label class="field"><span>成长值</span><input class="admin-input" id="adminUserGrowth" value="' + growthValue(user) + '"></label><label class="field"><span>积分</span><input class="admin-input" id="adminUserPoints" value="' + Number(user.points || 0) + '"></label><button class="primary-btn admin-user-vip-save" data-id="' + user.id + '" type="button">调整会员积分</button></div>';
}

function renderAdminUserCoupons(user) {
	var templateOptions = (state.couponTemplates || []).filter(function(c) { return c.status === "ENABLED"; }).map(function(c) { return '<option value="' + c.couponId + '">' + escapeHtml(c.couponName) + '</option>'; }).join("");
	var rows = adminUserCoupons(user.id).map(function(coupon) {
		var value = coupon.couponType === "DISCOUNT" ? (Number(coupon.discountRate || 1) * 10).toFixed(1) + "折" : money(coupon.amount || 0);
		return '<tr><td>' + escapeHtml(coupon.couponName || "") + '</td><td>' + escapeHtml(coupon.couponType || "") + '</td><td>' + value + '</td><td>' + money(coupon.minAmount || 0) + '</td><td>' + badge(coupon.status || "", coupon.status === "UNUSED" ? "green" : "") + '</td><td>' + escapeHtml((coupon.receiveTime || "").slice(0, 19)) + '</td><td>' + escapeHtml((coupon.expireTime || "").slice(0, 19)) + '</td><td>' + (coupon.orderId ? escapeHtml(coupon.orderId) : "-") + '</td><td>' + (coupon.status === "UNUSED" ? '<button class="ghost-btn admin-user-coupon-void" data-id="' + coupon.userCouponId + '" data-user="' + user.id + '" type="button">作废</button>' : '-') + '</td></tr>';
	}).join("") || '<tr><td colspan="9">该用户暂无优惠券。</td></tr>';
	return '<div class="coupon-issue-inline"><select class="admin-input" id="adminUserCouponTemplate">' + (templateOptions || '<option value="">暂无可发放模板</option>') + '</select><button class="primary-btn admin-user-coupon-issue" data-user="' + user.id + '" type="button">发放优惠券</button></div><div class="admin-table user-center-coupons"><table><thead><tr><th>优惠券名称</th><th>类型</th><th>优惠</th><th>门槛</th><th>状态</th><th>领取时间</th><th>过期时间</th><th>使用订单</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderAdminUserLogs(user) {
	var rows = adminLogsForUser(user.id).map(function(log) {
		return '<tr><td>' + escapeHtml((log.operationTime || "").slice(0, 19)) + '</td><td>' + escapeHtml(log.operationType || "") + '</td><td>' + escapeHtml(log.targetType || "") + ' #' + escapeHtml(log.targetId || "") + '</td><td>' + escapeHtml(log.content || "") + '</td><td>管理员 ' + escapeHtml(log.adminId || "") + '</td></tr>';
	}).join("") || '<tr><td colspan="5">暂无后台操作记录。</td></tr>';
	return '<div class="admin-table user-center-logs"><table><thead><tr><th>时间</th><th>操作类型</th><th>对象</th><th>内容</th><th>操作人</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderAdminUserDetail() {
	var user = selectedAdminUser();
	if (!user) return '<section class="panel-card admin-user-detail"><h3>暂无用户</h3></section>';
	state.adminSelectedUserId = user.id;
	var userMeta = [user.accountId || user.id, user.phone || "", user.email || ""].filter(function(value) {
		return String(value || "").trim();
	}).join(" · ");
	var body = "";
	if (state.adminUserDetailTab === "orders") body = renderAdminUserOrders(user);
	else if (state.adminUserDetailTab === "addresses") body = renderAdminUserAddresses(user);
	else if (state.adminUserDetailTab === "vip") body = renderAdminUserVip(user);
	else if (state.adminUserDetailTab === "coupons") body = renderAdminUserCoupons(user);
	else if (state.adminUserDetailTab === "logs") body = renderAdminUserLogs(user);
	else body = renderAdminUserBasic(user);
	return '<section class="panel-card admin-user-detail"><div class="admin-user-detail-head"><div class="admin-user-title"><h2>' + escapeHtml(user.username) + '</h2><p>' + escapeHtml(userMeta) + '</p></div>' + badge(user.status || "正常", user.status === "停用" ? "red" : "green") + '</div>' + renderAdminUserTabs() + body + '</section>';
}

function adminOrderRows() {
	var keyword = String(state.adminOrderKeyword || "").trim().toLowerCase();
	return (state.adminOrders || []).filter(function(order) {
		if (state.adminOrderStatusFilter !== "all" && order.status !== state.adminOrderStatusFilter) return false;
		if (!keyword) return true;
		var haystack = [order.orderNo, order.batchNo, order.shopName, order.userId, order.receiverName, order.receiverPhone, order.receiverAddress, orderItemsSummary(order)].join(" ").toLowerCase();
		return haystack.indexOf(keyword) >= 0;
	});
}

function renderAdminOrderManage() {
	var statuses = ["待付款", "待发货", "待收货", "已完成", "已取消"];
	var rows = adminOrderRows().map(function(order) {
		var options = statuses.map(function(status) { return '<option ' + (order.status === status ? "selected" : "") + '>' + status + '</option>'; }).join("");
		var afterSales = (order.afterSales || []).map(function(a) { return '#' + a.afterSaleId + ' 商品' + a.productId + ' ' + escapeHtml(a.status || ""); }).join("<br>") || "-";
		return '<tr><td><b>' + escapeHtml(order.orderNo) + '</b><p class="muted">' + escapeHtml(order.createTime || "") + '</p>' + (order.batchNo ? '<p class="muted">批次：' + escapeHtml(order.batchNo) + '</p>' : '') + '</td><td>' + escapeHtml(order.shopName || "平台自营") + '</td><td>用户 ' + escapeHtml(order.userId) + '</td><td>' + orderItemsSummary(order) + '</td><td>' + escapeHtml(order.receiverName || "") + '<p class="muted">' + escapeHtml(order.receiverPhone || "") + '</p></td><td>' + escapeHtml(order.receiverAddress || "") + '</td><td>' + (shipmentSummary(order) || "-") + '</td><td>' + afterSales + '</td><td><b>' + money(order.totalAmount) + '</b><p class="muted">商品 ' + money(orderOriginAmount(order)) + ' · 优惠 -' + money(order.discountAmount || 0) + '</p></td><td><select class="admin-input admin-order-status" data-id="' + order.id + '">' + options + '</select></td><td><div class="user-center-actions"><button class="primary-btn admin-order-save" data-id="' + order.id + '" type="button">修改状态</button><button class="ghost-btn admin-order-delete" data-id="' + order.id + '" type="button">删除订单</button></div></td></tr>';
	}).join("") || '<tr><td colspan="10">暂无匹配订单。</td></tr>';
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>订单管理</h2><p>集中查看物流、售后和订单状态，删除订单前需要二次确认。</p></div></div><div class="admin-user-toolbar"><label class="field"><span>搜索</span><input id="adminOrderKeyword" value="' + escapeHtml(state.adminOrderKeyword || "") + '" placeholder="订单号、批次、店铺、用户、收货人、商品"></label><label class="field"><span>状态</span><select id="adminOrderStatusFilter"><option value="all">全部状态</option>' + statuses.map(function(status) { return '<option value="' + status + '" ' + (state.adminOrderStatusFilter === status ? "selected" : "") + '>' + status + '</option>'; }).join("") + '</select></label></div><div class="admin-table tall admin-orders-table"><table><thead><tr><th>订单编号</th><th>店铺</th><th>用户</th><th>商品明细</th><th>收货人</th><th>收货地址</th><th>物流</th><th>售后</th><th>金额</th><th>订单状态</th><th>操作</th></tr></thead><tbody>' + rows + '</tbody></table></div></section>';
}

function renderAdmin() {
	var stats = [
		["销售额", money(state.adminOrders.reduce(function(sum, order) { return sum + Number(order.totalAmount || 0); }, 0))],
		["订单数", state.adminOrders.length],
		["商品数", state.products.length],
		["用户数", state.adminUsers.length]
	].map(function(item) {
		return '<div class="simple-card admin-stat-card"><div class="icon-tile">数</div><span>' + item[0] + '</span><strong>' + item[1] + '</strong></div>';
	}).join("");
	var userSection = '<section class="panel-card admin-section admin-user-center"><div class="section-head"><div><h2>用户管理</h2><p>先选择用户，再集中维护账号、订单、地址、会员积分和优惠券。</p></div></div>' + renderAdminUserFilters() + '<div class="admin-user-center-grid"><div class="admin-user-main-column">' + renderAdminUserList() + '</div><div class="admin-user-side">' + renderAdminUserDetail() + '</div></div></section>';
	if (state.page === "adminUsers") return userSection;
	if (state.page === "adminOrders") return renderAdminOrderManage();
	return '<section class="admin-stats">' + stats + '</section>' + userSection;
}

function reportRoleText(role) {
	if (role === "MERCHANT") return "商家";
	if (role === "ADMIN") return "管理员";
	if (role === "PRODUCT") return "商品";
	if (role === "ORDER") return "订单";
	if (role === "REVIEW") return "评价";
	return "用户";
}

function reportStatusText(status) {
	if (status === "PROCESSING") return "处理中";
	if (status === "APPROVED") return "已通过";
	if (status === "REJECTED") return "已驳回";
	if (status === "CLOSED") return "已关闭";
	return "待处理";
}

function reportStatusTone(status) {
	if (status === "APPROVED") return "green";
	if (status === "REJECTED" || status === "CLOSED") return "red";
	if (status === "PROCESSING") return "amber";
	return "";
}

function reportRows(reports, adminMode) {
	return (reports || []).map(function(r) {
		var reason = String(r.reason || "");
		var summary = reason.length > 42 ? reason.slice(0, 42) + "..." : reason;
		var actions = adminMode ? '<button class="ghost-btn admin-report-view" data-id="' + r.reportId + '" type="button">查看/处理</button>' : '<span class="muted">' + escapeHtml(r.handleOpinion || "等待平台处理") + '</span>';
		return '<tr><td>#' + r.reportId + '</td><td>' + reportRoleText(r.reporterRole) + '<p class="muted">' + escapeHtml(r.reporterName || "") + '</p></td><td>' + reportRoleText(r.targetRole) + '<p class="muted">' + escapeHtml(r.targetName || ("#" + r.targetId)) + '</p></td><td>' + escapeHtml(r.reportType || "") + '</td><td>' + escapeHtml(summary) + '</td><td>' + badge(reportStatusText(r.status), reportStatusTone(r.status)) + '</td><td>' + escapeHtml(shortDate(r.createTime || "")) + '</td><td class="report-actions">' + actions + '</td></tr>';
	}).join("");
}

function renderReportTable(reports, adminMode, emptyText) {
	var rows = reportRows(reports, adminMode) || '<tr><td colspan="8">' + escapeHtml(emptyText || "暂无举报记录。") + '</td></tr>';
	return '<div class="admin-table report-table"><table><thead><tr><th>编号</th><th>举报人</th><th>对象</th><th>类型</th><th>原因摘要</th><th>状态</th><th>提交时间</th><th>操作/处理意见</th></tr></thead><tbody>' + rows + '</tbody></table></div>';
}

function renderReportDetailRows(report) {
	var items = [
		["举报人", reportRoleText(report.reporterRole) + " #" + (report.reporterId || "") + " " + (report.reporterName || "")],
		["举报对象", reportRoleText(report.targetRole) + " #" + (report.targetId || "") + " " + (report.targetName || "")],
		["举报类型", report.reportType || ""],
		["举报原因", report.reason || ""],
		["详细说明", report.description || ""],
		["证据链接", report.evidenceUrls || ""],
		["提交时间", report.createTime || ""],
		["当前状态", reportStatusText(report.status)],
		["处理人", report.adminName || ""],
		["处理意见", report.handleOpinion || ""],
		["处理结果", report.handleResult || ""],
		["处理时间", report.handleTime || ""]
	];
	return items.map(function(item) {
		var value = item[0] === "证据链接" && item[1] ? String(item[1]).split(/[,，\s]+/).filter(function(url) { return url; }).map(function(url) {
			return '<a href="' + escapeHtml(url) + '" target="_blank" rel="noopener">' + escapeHtml(url) + '</a>';
		}).join("<br>") : escapeHtml(item[1] || "-");
		return '<div class="report-detail-row"><span>' + item[0] + '</span><strong>' + value + '</strong></div>';
	}).join("");
}

function renderAdminReportModal() {
	var report = state.adminReportModal;
	if (!report) return "";
	var statuses = ["PROCESSING", "APPROVED", "REJECTED", "CLOSED"];
	var options = statuses.map(function(status) {
		return '<option value="' + status + '" ' + (report.status === status ? "selected" : "") + '>' + reportStatusText(status) + '</option>';
	}).join("");
	return '<div class="report-modal-backdrop"><form class="report-modal admin-report-modal" id="adminReportHandleForm"><div class="section-head"><div><h2>举报 #' + report.reportId + '</h2><p>' + escapeHtml(reportRoleText(report.targetRole) + " · " + (report.targetName || ("#" + report.targetId))) + '</p></div><button class="ghost-btn admin-report-modal-close" type="button">关闭</button></div><div class="report-detail-grid">' + renderReportDetailRows(report) + '</div><label class="field"><span>处理状态</span><select id="adminReportHandleStatus">' + options + '</select></label><label class="field wide"><span>处理意见</span><textarea id="adminReportHandleOpinion" rows="3" placeholder="写给举报人的处理意见">' + escapeHtml(report.handleOpinion || "") + '</textarea></label><label class="field wide"><span>处理结果</span><textarea id="adminReportHandleResult" rows="3" placeholder="记录平台最终处理结果">' + escapeHtml(report.handleResult || "") + '</textarea></label><div class="form-actions"><button class="ghost-btn admin-report-modal-close" type="button">取消</button><button class="primary-btn" type="submit">提交处理</button></div></form></div>';
}

function renderAdminReportPager() {
	var page = Math.max(1, Number(state.adminReportPage || 1));
	var pageSize = Math.max(1, Number(state.adminReportPageSize || 20));
	var total = Math.max(0, Number(state.adminReportTotal || 0));
	var totalPages = Math.max(1, Math.ceil(total / pageSize));
	return '<div class="report-pager"><span>共 ' + total + ' 条，第 ' + page + ' / ' + totalPages + ' 页</span><div><button class="ghost-btn admin-report-page" data-page="' + (page - 1) + '" type="button" ' + (page <= 1 ? "disabled" : "") + '>上一页</button><button class="ghost-btn admin-report-page" data-page="' + (page + 1) + '" type="button" ' + (page >= totalPages ? "disabled" : "") + '>下一页</button></div></div>';
}

function renderReports() {
	if (!state.user && !state.merchant) return '<div class="empty-cart"><h3>请先登录</h3><p class="muted">登录后可以查看自己提交的举报。</p></div>';
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>我的举报</h2><p>这里展示你提交给平台处理的举报及最新处理意见。</p></div></div>' + renderReportTable(state.reports, false, "暂无举报记录。") + '</section>';
}

function renderMerchantReports() {
	var my = renderReportTable(state.merchantMyReports, false, "暂无我提交的举报。");
	var related = renderReportTable(state.merchantRelatedReports, false, "暂无涉及本店的举报。");
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>举报管理</h2><p>商家可查看自己提交的举报，以及用户举报本店、商品、订单或评价的处理进度。</p></div></div><div class="report-split"><section><h3>我提交的举报</h3>' + my + '</section><section><h3>涉及本店的举报</h3>' + related + '</section></div></section>';
}

function renderAdminReports() {
	var statuses = ["all", "PENDING", "PROCESSING", "APPROVED", "REJECTED", "CLOSED"];
	var options = statuses.map(function(status) {
		var text = status === "all" ? "全部状态" : reportStatusText(status);
		return '<option value="' + status + '" ' + (state.adminReportStatusFilter === status ? "selected" : "") + '>' + text + '</option>';
	}).join("");
	return '<section class="panel-card admin-section"><div class="section-head"><div><h2>举报管理</h2><p>统一筛选、查看并处理用户和商家提交的举报。</p></div></div><div class="admin-user-toolbar"><label class="field"><span>状态</span><select id="adminReportStatusFilter">' + options + '</select></label><label class="field"><span>搜索</span><input id="adminReportKeyword" value="' + escapeHtml(state.adminReportKeyword || "") + '" placeholder="举报人、对象、类型、原因"></label></div>' + renderReportTable(state.adminReports, true, "暂无匹配举报。") + renderAdminReportPager() + '</section>';
}

function renderReportModal() {
	var modal = state.reportModal;
	if (!modal) return "";
	var label = reportRoleText(modal.targetRole) + " #" + modal.targetId;
	var typeOptions = ["商品违规", "商家违规", "用户违规", "恶意评价", "订单纠纷", "其他"].map(function(item) {
		return '<option value="' + item + '" ' + (item === modal.reportType ? "selected" : "") + '>' + item + '</option>';
	}).join("");
	return '<div class="report-modal-backdrop"><form class="report-modal" id="reportForm"><div class="section-head"><div><h2>提交举报</h2><p>' + escapeHtml(label) + '</p></div><button class="ghost-btn report-modal-close" type="button">关闭</button></div><label class="field"><span>举报类型</span><select id="reportType">' + typeOptions + '</select></label><label class="field wide"><span>举报原因</span><input id="reportReason" placeholder="请简要说明举报原因"></label><label class="field wide"><span>详细说明</span><textarea id="reportDescription" rows="4" placeholder="补充时间、订单、沟通情况等信息"></textarea></label><label class="field wide"><span>证据链接/图片地址</span><input id="reportEvidenceUrls" placeholder="多个链接可用逗号分隔"></label><div class="form-actions"><button class="ghost-btn report-modal-close" type="button">取消</button><button class="primary-btn" type="submit">提交举报</button></div></form></div>';
}

function openReportModal(options) {
	if (!state.user && !state.merchant) {
		alert("请先登录后提交举报。");
		return;
	}
	state.reportModal = options || null;
	renderPage();
}

function bindChatEvents() {
	Array.prototype.forEach.call(document.querySelectorAll(".conversation-item"), function(btn) {
		btn.onclick = function() {
			closeChatMenu();
			state.chatQuoteMessage = null;
			loadConversationMessages(btn.getAttribute("data-id")).then(function() {
				renderPage();
				scrollChatToBottom();
			});
		};
	});
	var searchBtn = document.getElementById("chatTargetSearch");
	if (searchBtn) {
		searchBtn.onclick = function() {
			var role = document.getElementById("chatTargetRole");
			var keyword = document.getElementById("chatTargetKeyword");
			state.messageTargetRole = role ? role.value : "";
			state.messageSearchKeyword = keyword ? keyword.value : "";
			loadMessageTargets().then(renderPage);
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".chat-target"), function(btn) {
		btn.onclick = function() {
			closeChatMenu();
			state.chatQuoteMessage = null;
			post("messages", {
				action: "start",
				targetRole: btn.getAttribute("data-role"),
				targetId: btn.getAttribute("data-id")
			}).then(function(data) {
				if (!data.success) { alert(data.message || "无法发起会话"); return; }
				state.activeConversationId = Number(data.conversationId);
				state.conversations = data.conversations || [];
				state.messages = data.messages || [];
				state.messageTargets = [];
				state.unreadMessages = Number(data.unreadCount || 0);
				state.chatScrollToBottom = true;
				renderPage();
				scrollChatToBottom();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".chat-modal-open"), function(btn) {
		btn.onclick = function() {
			state.chatModal = btn.getAttribute("data-modal");
			var needsOrders = state.user && (state.chatModal === "order" || state.chatModal === "refund");
			(needsOrders ? loadOrders() : Promise.resolve()).then(renderPage);
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".chat-modal-close"), function(btn) {
		btn.onclick = function() {
			state.chatModal = null;
			renderPage();
		};
	});
	var friendSearchBtn = document.getElementById("friendSearchBtn");
	if (friendSearchBtn) {
		friendSearchBtn.onclick = function() {
			var kw = document.getElementById("friendKeyword");
			get("friends?keyword=" + encodeURIComponent(kw ? kw.value.trim() : "")).then(function(data) {
				if (!data.success) { alert(data.message || "好友搜索失败"); return; }
				state.friendSearchResults = data.users || [];
				renderPage();
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".friend-request"), function(btn) {
		btn.onclick = function() {
			var message = prompt("给对方留言", "你好，我想添加你为好友。") || "";
			post("friends", { action: "request", targetUserId: btn.getAttribute("data-id"), message: message }).then(function(data) {
				if (!data.success) { alert(data.message || "好友申请失败"); return; }
				state.conversations = data.conversations || state.conversations;
				state.activeConversationId = Number(data.conversationId || state.activeConversationId);
				state.chatModal = null;
				state.friendSearchResults = [];
				showToast("好友申请已发送");
				loadConversationMessages(state.activeConversationId).then(renderPage);
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".friend-start-chat"), function(btn) {
		btn.onclick = function() {
			startChatWith("USER", btn.getAttribute("data-id"));
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".friend-handle"), function(btn) {
		btn.onclick = function() {
			post("friends", { action: btn.getAttribute("data-action"), requestId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "处理好友申请失败"); return; }
				showToast(btn.getAttribute("data-action") === "accept" ? "已添加好友" : "已拒绝好友申请");
				loadConversationMessages(state.activeConversationId).then(renderPage);
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".chat-send-order"), function(btn) {
		btn.onclick = function() {
			sendChatCard("ORDER_CARD", btn.getAttribute("data-id"));
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".chat-send-product"), function(btn) {
		btn.onclick = function() {
			var productId = state.selectedProduct && state.selectedProduct.id ? state.selectedProduct.id : prompt("请输入要发送的商品ID");
			if (!productId) return;
			sendChatCard("PRODUCT_CARD", productId);
		};
	});
	var refundForm = document.getElementById("chatRefundForm");
	if (refundForm) {
		refundForm.onsubmit = function(e) {
			e.preventDefault();
			var pair = (document.getElementById("refundOrderItem").value || "").split(":");
			var orderId = pair[0];
			var productId = pair[1];
			var reason = document.getElementById("refundReason").value.trim();
			var desc = document.getElementById("refundDesc").value.trim();
			if (!orderId || !productId || !reason) { alert("请选择订单商品并填写退款原因。"); return; }
			post("orders", { action: "afterSale", orderId: orderId, productId: productId, afterSaleType: "退款", reason: reason, description: desc }).then(function(data) {
				if (!data.success) { alert(data.message || "退款申请失败"); return; }
				state.orders = data.orders || state.orders;
				state.afterSales = data.afterSales || state.afterSales;
				state.chatModal = null;
				showToast("退款申请已提交，并已通知商家");
				loadMessages().then(renderPage);
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".refund-handle"), function(btn) {
		btn.onclick = function() {
			var opinion = prompt(btn.getAttribute("data-action") === "approve" ? "同意退款说明" : "拒绝原因", "") || "";
			post("merchant/orders", { action: "afterSale", afterSaleId: btn.getAttribute("data-id"), handleAction: btn.getAttribute("data-action"), opinion: opinion }).then(function(data) {
				if (!data.success) { alert(data.message || "售后处理失败"); return; }
				state.merchantOrders = data.orders || state.merchantOrders;
				state.afterSales = data.afterSales || state.afterSales;
				showToast("售后处理结果已发送给用户");
				loadConversationMessages(state.activeConversationId).then(renderPage);
			});
		};
	});
	var form = document.getElementById("chatInputForm");
	var input = document.getElementById("chatInputText");
	var quoteClear = document.querySelector(".chat-quote-clear");
	if (quoteClear) {
		quoteClear.onclick = function() {
			state.chatQuoteMessage = null;
			renderPage();
			var nextInput = document.getElementById("chatInputText");
			if (nextInput) nextInput.focus();
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".chat-message"), function(row) {
		row.oncontextmenu = function(e) {
			var item = findChatMessage(row.getAttribute("data-message-id"));
			if (!item || item.recalled) return;
			e.preventDefault();
			var menuWidth = 150;
			var menuHeight = item.canRecall ? 92 : 52;
			state.chatContextMenu = {
				messageId: item.messageId,
				x: Math.max(8, Math.min(e.clientX, window.innerWidth - menuWidth - 8)),
				y: Math.max(8, Math.min(e.clientY, window.innerHeight - menuHeight - 8))
			};
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".chat-menu-quote"), function(btn) {
		btn.onclick = function(e) {
			e.stopPropagation();
			state.chatQuoteMessage = findChatMessage(btn.getAttribute("data-id"));
			closeChatMenu();
			renderPage();
			var nextInput = document.getElementById("chatInputText");
			if (nextInput) nextInput.focus();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".chat-menu-recall"), function(btn) {
		btn.onclick = function(e) {
			e.stopPropagation();
			var messageId = btn.getAttribute("data-id");
			closeChatMenu();
			post("messages", { action: "recall", messageId: messageId, conversationId: state.activeConversationId }).then(function(data) {
				if (!data.success) { alert(data.message || "撤回失败"); return; }
				state.messages = data.messages || state.messages;
				state.conversations = data.conversations || state.conversations;
				state.unreadMessages = Number(data.unreadCount || 0);
				renderPage();
			});
		};
	});
	document.onclick = function(e) {
		if (state.chatContextMenu && !e.target.closest(".chat-context-menu")) {
			closeChatMenu();
			renderPage();
		}
	};
	if (form && input) {
		input.onkeydown = function(e) {
			if (e.key === "Enter" && !e.shiftKey) {
				e.preventDefault();
				form.dispatchEvent(new Event("submit"));
			}
		};
		form.onsubmit = function(e) {
			e.preventDefault();
			if (state.chatSending) return;
			var text = input.value.trim();
			if (!text) return;
			state.chatSending = true;
			var sendBtn = form.querySelector(".chat-send-btn");
			if (sendBtn) {
				sendBtn.disabled = true;
				sendBtn.textContent = "发送中...";
			}
			post("messages", {
				action: "send",
				conversationId: state.activeConversationId,
				content: text,
				clientMessageId: newClientMessageId(),
				quoteMessageId: state.chatQuoteMessage ? state.chatQuoteMessage.messageId : ""
			}).then(function(data) {
				if (!data.success) { alert(data.message || "发送失败"); return; }
				input.value = "";
				state.chatQuoteMessage = null;
				state.messages = data.messages || [];
				state.conversations = data.conversations || [];
				state.unreadMessages = Number(data.unreadCount || 0);
				state.chatScrollToBottom = true;
				renderPage();
				scrollChatToBottom();
			}).catch(function(err) {
				alert(err.message || "发送失败");
			}).then(function() {
				state.chatSending = false;
				var latestBtn = document.querySelector(".chat-send-btn");
				if (latestBtn) {
					latestBtn.disabled = false;
					latestBtn.textContent = "发送";
				}
			});
		};
	}
	var emojiToggle = document.getElementById("emojiToggle");
	var emojiPanel = document.getElementById("emojiPanel");
	if (emojiToggle && emojiPanel) {
		emojiToggle.onclick = function(e) {
			e.preventDefault();
			state.emojiPanelOpen = !state.emojiPanelOpen;
			emojiPanel.classList.toggle("show", state.emojiPanelOpen);
		};
		Array.prototype.forEach.call(emojiPanel.querySelectorAll("button"), function(btn) {
			btn.onclick = function(e) {
				e.preventDefault();
				if (!input) return;
				input.value += btn.textContent;
				input.focus();
			};
		});
	}
	bindChatUpload("chatImageFile");
	bindChatUpload("chatDocFile");
	bindChatUpload("chatVideoFile");
	if (state.chatScrollToBottom && document.getElementById("chatMessages")) {
		state.chatScrollToBottom = false;
		scrollChatToBottom();
	}
}

function bindChatUpload(inputId) {
	var input = document.getElementById(inputId);
	if (!input) return;
	input.onchange = function() {
		if (!input.files || !input.files[0]) return;
		if (state.chatSending) {
			input.value = "";
			return;
		}
		state.chatSending = true;
		var progress = document.getElementById("chatUploadProgress");
		var bar = progress ? progress.querySelector("i") : null;
		if (progress) progress.classList.add("show");
		if (bar) bar.style.width = "0%";
		var formData = new FormData();
		formData.append("action", "upload");
		formData.append("conversationId", state.activeConversationId);
		formData.append("clientMessageId", newClientMessageId());
		if (state.chatQuoteMessage) formData.append("quoteMessageId", state.chatQuoteMessage.messageId);
		formData.append("file", input.files[0]);
		uploadFormData("messages", formData, function(percent) {
			if (bar) bar.style.width = percent + "%";
		}).then(function(data) {
			input.value = "";
			if (progress) setTimeout(function() { progress.classList.remove("show"); }, 600);
			if (!data.success) { alert(data.message || "上传失败"); return; }
			state.messages = data.messages || [];
			state.conversations = data.conversations || [];
			state.unreadMessages = Number(data.unreadCount || 0);
			state.chatQuoteMessage = null;
			state.chatScrollToBottom = true;
			renderPage();
			scrollChatToBottom();
		}).catch(function(err) {
			input.value = "";
			if (progress) progress.classList.remove("show");
			alert(err.message || "上传失败");
		}).then(function() {
			state.chatSending = false;
		});
	};
}

function startChatWith(role, id, afterStart) {
	post("messages", { action: "start", targetRole: role, targetId: id }).then(function(data) {
		if (!data.success) { alert(data.message || "无法发起会话"); return; }
		state.activeConversationId = Number(data.conversationId);
		state.conversations = data.conversations || [];
		state.messages = data.messages || [];
		state.chatModal = null;
		state.messageTargets = [];
		state.unreadMessages = Number(data.unreadCount || 0);
		state.chatScrollToBottom = true;
		if (typeof afterStart === "function") {
			afterStart(data);
		} else {
			setPage(state.admin ? "adminMessages" : (state.merchant ? "merchantMessages" : "messages"));
		}
	});
}

function sendChatCard(cardType, refId) {
	if (!state.activeConversationId || Number(state.activeConversationId) === 0) {
		alert("请先选择一个聊天会话。");
		return;
	}
	if (state.chatSending) return;
	state.chatSending = true;
	post("messages", {
		action: "sendCard",
		conversationId: state.activeConversationId,
		cardType: cardType,
		refId: refId,
		clientMessageId: newClientMessageId(),
		quoteMessageId: state.chatQuoteMessage ? state.chatQuoteMessage.messageId : ""
	}).then(function(data) {
		if (!data.success) { alert(data.message || "业务卡片发送失败"); return; }
		state.messages = data.messages || [];
		state.conversations = data.conversations || [];
		state.unreadMessages = Number(data.unreadCount || 0);
		state.chatModal = null;
		state.chatQuoteMessage = null;
		state.chatScrollToBottom = true;
		showToast("业务卡片已发送");
		renderPage();
		scrollChatToBottom();
	}).catch(function(err) {
		alert(err.message || "业务卡片发送失败");
	}).then(function() {
		state.chatSending = false;
	});
}

function scrollChatToBottom() {
	setTimeout(function() {
		var box = document.getElementById("chatMessages");
		if (box) box.scrollTop = box.scrollHeight;
	}, 0);
}

function bindPageActions() {
	Array.prototype.forEach.call(document.querySelectorAll(".open-report"), function(btn) {
		btn.onclick = function() {
			openReportModal({
				targetRole: btn.getAttribute("data-target-role"),
				targetId: Number(btn.getAttribute("data-target-id") || 0),
				reportType: btn.getAttribute("data-report-type") || "其他",
				merchantId: Number(btn.getAttribute("data-merchant-id") || 0),
				orderId: Number(btn.getAttribute("data-order-id") || 0),
				productId: Number(btn.getAttribute("data-product-id") || 0),
				reviewId: Number(btn.getAttribute("data-review-id") || 0)
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".report-modal-close"), function(btn) {
		btn.onclick = function() {
			state.reportModal = null;
			renderPage();
		};
	});
	var reportForm = document.getElementById("reportForm");
	if (reportForm) {
		reportForm.onsubmit = function(e) {
			e.preventDefault();
			var modal = state.reportModal || {};
			var reason = document.getElementById("reportReason").value.trim();
			if (!reason) {
				alert("请填写举报原因。");
				return;
			}
			post("reports", {
				action: "create",
				targetRole: modal.targetRole,
				targetId: modal.targetId,
				reportType: document.getElementById("reportType").value,
				reason: reason,
				description: document.getElementById("reportDescription").value,
				evidenceUrls: document.getElementById("reportEvidenceUrls").value,
				merchantId: modal.merchantId || 0,
				orderId: modal.orderId || 0,
				productId: modal.productId || 0,
				reviewId: modal.reviewId || 0
			}).then(function(data) {
				if (!data.success) { alert(data.message || "举报提交失败"); return; }
				state.reportModal = null;
				state.reports = data.reports || state.reports || [];
				showToast("举报已提交");
				if (state.page === "reports") loadReports().then(renderPage);
				else if (state.page === "merchantReports") loadMerchantReports().then(renderPage);
				else renderPage();
			});
		};
	}
	var adminReportStatusFilter = document.getElementById("adminReportStatusFilter");
	if (adminReportStatusFilter) {
		adminReportStatusFilter.onchange = function() {
			state.adminReportStatusFilter = adminReportStatusFilter.value;
			state.adminReportPage = 1;
			loadAdminReports().then(renderPage);
		};
	}
	var adminReportKeyword = document.getElementById("adminReportKeyword");
	if (adminReportKeyword) {
		var reportSearchTimer = null;
		adminReportKeyword.oninput = function() {
			state.adminReportKeyword = adminReportKeyword.value;
			state.adminReportPage = 1;
			clearTimeout(reportSearchTimer);
			reportSearchTimer = setTimeout(function() { loadAdminReports().then(renderPage); }, 220);
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".admin-report-page"), function(btn) {
		btn.onclick = function() {
			if (btn.disabled) return;
			state.adminReportPage = Math.max(1, Number(btn.getAttribute("data-page") || 1));
			loadAdminReports().then(renderPage);
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-report-view"), function(btn) {
		btn.onclick = function() {
			get("admin/reports?action=detail&reportId=" + encodeURIComponent(btn.getAttribute("data-id"))).then(function(data) {
				if (!data.success) { alert(data.message || "无法加载举报详情"); return; }
				state.adminReportModal = data.report || null;
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-report-modal-close"), function(btn) {
		btn.onclick = function() {
			state.adminReportModal = null;
			renderPage();
		};
	});
	var adminReportHandleForm = document.getElementById("adminReportHandleForm");
	if (adminReportHandleForm) {
		adminReportHandleForm.onsubmit = function(e) {
			e.preventDefault();
			var report = state.adminReportModal || {};
			var status = document.getElementById("adminReportHandleStatus").value;
			var opinion = document.getElementById("adminReportHandleOpinion").value.trim();
			var result = document.getElementById("adminReportHandleResult").value.trim();
			if (!opinion) {
				alert("请填写处理意见");
				return;
			}
			post("admin/reports", {
				action: "handle",
				reportId: report.reportId,
				status: status,
				handleOpinion: opinion,
				handleResult: result,
				filterStatus: state.adminReportStatusFilter || "all",
				keyword: state.adminReportKeyword || "",
				page: state.adminReportPage || 1,
				pageSize: state.adminReportPageSize || 20
			}).then(function(data) {
				if (!data.success) { alert(data.message || "处理失败"); return; }
				state.adminReports = data.reports || [];
				state.adminReportPage = Number(data.page || state.adminReportPage || 1);
				state.adminReportPageSize = Number(data.pageSize || state.adminReportPageSize || 20);
				state.adminReportTotal = Number(data.total || state.adminReports.length || 0);
				state.adminReportModal = null;
				showToast("举报状态已更新");
				renderPage();
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".add-cart,.buy-now"), function(btn) {
		btn.onclick = function() { addToCart(Number(btn.getAttribute("data-id")), btn.classList.contains("buy-now")); };
	});
	Array.prototype.forEach.call(document.querySelectorAll(".detail-add-cart,.detail-buy-now"), function(btn) {
		btn.onclick = function() {
			var productId = Number(btn.getAttribute("data-id"));
			var goCart = btn.classList.contains("detail-buy-now");
			var chain = addToCart(productId, false, state.detailQuantity);
			if (goCart) chain = chain.then(function() { setPage("cart"); });
			return chain;
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".detail-contact-merchant"), function(btn) {
		btn.onclick = function() {
			var productId = btn.getAttribute("data-product");
			startChatWith("MERCHANT", btn.getAttribute("data-merchant"), function() {
				sendChatCard("PRODUCT_CARD", productId);
				setPage("messages");
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".detail-contact-admin"), function(btn) {
		btn.onclick = function() {
			var productId = btn.getAttribute("data-product");
			startChatWith("ADMIN", 1, function() {
				sendChatCard("PRODUCT_CARD", productId);
				setPage("messages");
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".view-detail,.visual-open"), function(btn) {
		btn.onclick = function() { openDetail(Number(btn.getAttribute("data-id")), state.page || "home"); };
	});
    Array.prototype.forEach.call(document.querySelectorAll(".product-media-thumb"), function(btn) {
        btn.onclick = function() {
            state.detailMediaIndex = Number(btn.getAttribute("data-index") || 0);
            renderPage();
        };
    });
    Array.prototype.forEach.call(document.querySelectorAll(".product-media-prev,.product-media-next"), function(btn) {
        btn.onclick = function() {
            var mediaList = productMediaList(state.selectedProduct || {});
            if (!mediaList.length) return;
            var delta = btn.classList.contains("product-media-prev") ? -1 : 1;
            state.detailMediaIndex = (Number(state.detailMediaIndex || 0) + delta + mediaList.length) % mediaList.length;
            renderPage();
        };
    });
	Array.prototype.forEach.call(document.querySelectorAll(".favorite-btn"), function(btn) {
		btn.onclick = function(e) {
			e.stopPropagation();
			if (!state.user) {
				alert("请先登录普通用户账号。");
				return;
			}
			post("favorites", { action: "toggle", productId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "收藏操作失败"); return; }
				state.favoriteIds = data.favoriteIds || [];
				state.favorites = data.favorites || [];
				showToast(data.favorited ? "已收藏商品" : "已取消收藏");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".favorite-remove"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确定移除该收藏商品吗？")) return;
			post("favorites", { action: "remove", productId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "移除失败"); return; }
				state.favoriteIds = data.favoriteIds || [];
				state.favorites = data.favorites || [];
				showToast("已移除收藏");
				renderPage();
			});
		};
	});
	var detailBack = document.querySelector(".detail-back-btn");
	if (detailBack) {
		detailBack.onclick = function() {
			setPage(state.detailReturnPage || "home");
		};
	}
	var hall = document.querySelector(".hall-carousel");
	if (hall) {
		var banners = state.hallBanners || [];
		var goHall = function(next) {
			if (!banners.length) return;
			state.hallSlideIndex = (next + banners.length) % banners.length;
			renderPage();
		};
		var prev = hall.querySelector(".hall-prev");
		var next = hall.querySelector(".hall-next");
		if (prev) prev.onclick = function(e) { e.stopPropagation(); goHall((state.hallSlideIndex || 0) - 1); };
		if (next) next.onclick = function(e) { e.stopPropagation(); goHall((state.hallSlideIndex || 0) + 1); };
		Array.prototype.forEach.call(hall.querySelectorAll(".hall-dots button"), function(dot) {
			dot.onclick = function(e) { e.stopPropagation(); goHall(Number(dot.getAttribute("data-index") || 0)); };
		});
		var video = hall.querySelector("video");
		if (video && video.getAttribute("data-lock-pause") === "1") {
			video.onpause = function() { video.play().catch(function() {}); };
		}
		var soundToggle = hall.querySelector(".hall-sound-toggle");
		if (soundToggle && video) {
			soundToggle.onclick = function(e) {
				e.stopPropagation();
				video.muted = !video.muted;
				soundToggle.textContent = video.muted ? "静" : "声";
				soundToggle.classList.toggle("active", !video.muted);
				video.play().catch(function() {});
			};
		}
		if (hall.classList.contains("hall-clickable")) {
			hall.onclick = function() {
				var banner = banners[state.hallSlideIndex || 0] || {};
				if (banner.linkType === "PRODUCT" && banner.productId) openDetail(Number(banner.productId), "home");
				else if (banner.linkType === "PAGE" && banner.linkTarget) setPage(banner.linkTarget);
			};
		}
	}
	Array.prototype.forEach.call(document.querySelectorAll(".category-card"), function(btn) {
		btn.onclick = function() {
			var feed = btn.getAttribute("data-feed");
			if (feed) {
				state.activeFeed = feed;
				loadProducts().then(renderPage);
			}
		};
	});
	var showAll = document.querySelector(".show-all-products");
	if (showAll) {
		showAll.onclick = function() {
			loadProducts().then(renderPage);
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".use-coupon"), function(btn) {
		btn.onclick = function() {
			var id = Number(btn.getAttribute("data-id"));
			var coupon = state.coupons.filter(function(item) { return item.id === id; })[0];
			if (coupon && coupon.couponOwnerType === "MERCHANT") state.selectedMerchantCouponId = id;
			else if (coupon && coupon.stackable) state.selectedStackableCouponId = id;
			else state.selectedPlatformCouponId = id;
			state.selectedCouponId = id;
			setPage("cart");
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".claim-coupon"), function(btn) {
		btn.onclick = function() {
			post("user/coupons", { action: "claim", couponId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "领取失败"); return; }
				state.userCoupons = data.coupons || [];
				return loadUserCoupons().then(function() {
					updateCouponBadge();
					showToast("优惠券已领取");
					renderPage();
				});
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".claim-all-coupons"), function(btn) {
		if (btn.disabled) return;
		btn.onclick = function() {
			if (!state.user) {
				alert("请先使用普通用户登录后领取优惠券。");
				return;
			}
			post("user/coupons", { action: "claimAll" }).then(function(data) {
				if (!data.success) { alert(data.message || "一键领取失败"); return; }
				state.userCoupons = data.coupons || [];
				return loadUserCoupons().then(function() {
					updateCouponBadge();
					showToast(Number(data.issueCount || 0) > 0 ? ("已领取 " + data.issueCount + " 张优惠券") : "没有新的可领取优惠券");
					renderPage();
				});
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".coupon-collapse-toggle"), function(btn) {
		btn.onclick = function() {
			state.couponCenterCollapsed = !state.couponCenterCollapsed;
			try {
				localStorage.setItem("hishoppingCouponCenterCollapsed", state.couponCenterCollapsed ? "1" : "0");
			} catch (e) {}
			renderPage();
			var section = document.getElementById("couponCenter");
			if (section && section.scrollIntoView) section.scrollIntoView({ behavior: "smooth", block: "nearest" });
		};
	});
	var hallForm = document.getElementById("hallBannerForm");
	if (hallForm) {
		var hallFile = document.getElementById("hallMediaFile");
		var setHallField = function(id, value) {
			var el = document.getElementById(id);
			if (el) el.value = value == null ? "" : value;
		};
		var resetHallForm = function() {
			setHallField("hallId", "");
			setHallField("hallMediaUrl", "");
			setHallField("hallMediaType", "IMAGE");
			setHallField("hallTitle", "");
			setHallField("hallSubtitle", "");
			setHallField("hallSortNo", "1");
			setHallField("hallEnabled", "true");
			setHallField("hallOverlayEnabled", "true");
			setHallField("hallTextPosition", "LEFT");
			setHallField("hallTitleColor", "#ffffff");
			setHallField("hallSubtitleColor", "#e2e8f0");
			setHallField("hallLinkType", "NONE");
			setHallField("hallProductId", "0");
			setHallField("hallLinkTarget", "");
			setHallField("hallMuted", "true");
			setHallField("hallDisableSeek", "false");
			setHallField("hallDisablePause", "false");
			var name = document.getElementById("hallUploadName");
			if (name) name.textContent = "支持图片和视频";
			if (hallFile) hallFile.value = "";
			updateHallLinkFields();
		};
		var updateHallLinkFields = function() {
			var typeEl = document.getElementById("hallLinkType");
			var type = typeEl ? typeEl.value : "NONE";
			var productField = document.querySelector(".hall-product-field");
			var pageField = document.querySelector(".hall-page-field");
			if (productField) productField.style.display = type === "PRODUCT" ? "" : "none";
			if (pageField) pageField.style.display = type === "PAGE" ? "" : "none";
		};
		var hallLinkType = document.getElementById("hallLinkType");
		if (hallLinkType) {
			hallLinkType.onchange = updateHallLinkFields;
			updateHallLinkFields();
		}
		if (hallFile) {
			hallFile.onchange = function() {
				if (!hallFile.files || !hallFile.files[0]) return;
				setHallField("hallMediaUrl", "");
				var uploadName = document.getElementById("hallUploadName");
				var progress = document.getElementById("hallUploadProgress");
				var bar = progress ? progress.querySelector("i") : null;
				if (progress) progress.classList.add("show");
				if (bar) bar.style.width = "0%";
				if (uploadName) uploadName.textContent = "正在上传：" + hallFile.files[0].name;
				var formData = new FormData();
				formData.append("media", hallFile.files[0]);
				uploadFormData("admin/hallBannerUpload", formData, function(percent) {
					if (bar) bar.style.width = percent + "%";
					if (uploadName) uploadName.textContent = "正在上传：" + hallFile.files[0].name + "（" + percent + "%）";
				})
					.then(function(data) {
						if (!data.success) {
							if (uploadName) uploadName.textContent = "上传失败：" + (data.message || "请检查登录状态和文件格式");
							alert(data.message || "上传失败");
							return;
						}
						if (bar) bar.style.width = "100%";
						setHallField("hallMediaUrl", data.mediaUrl || "");
						setHallField("hallMediaType", data.mediaType || "IMAGE");
						var name = document.getElementById("hallUploadName");
						if (name) name.textContent = "已上传：" + (hallFile.files[0].name || data.mediaUrl);
						showToast("大厅媒体已上传");
					})
					.catch(function() {
						if (uploadName) uploadName.textContent = "上传失败：接口未响应";
						alert("上传失败，请检查文件格式或重新登录管理员账号。");
					});
			};
		}
		hallForm.onsubmit = function(e) {
			e.preventDefault();
			if (!document.getElementById("hallMediaUrl").value) {
				alert(hallFile && hallFile.files && hallFile.files[0] ? "媒体仍未上传成功，请等待上传完成或重新选择文件。" : "请先上传大厅展示媒体。");
				return;
			}
			var payload = {
				action: "save",
				id: document.getElementById("hallId").value || 0,
				mediaUrl: document.getElementById("hallMediaUrl").value,
				mediaType: document.getElementById("hallMediaType").value,
				title: document.getElementById("hallTitle").value,
				subtitle: document.getElementById("hallSubtitle").value,
				sortNo: document.getElementById("hallSortNo").value || 0,
				enabled: document.getElementById("hallEnabled").value,
				linkType: document.getElementById("hallLinkType").value,
				linkTarget: document.getElementById("hallLinkTarget").value,
				productId: document.getElementById("hallProductId").value || 0,
				linkEnabled: document.getElementById("hallLinkType").value !== "NONE",
				overlayEnabled: document.getElementById("hallOverlayEnabled").value,
				textPosition: document.getElementById("hallTextPosition").value,
				titleColor: document.getElementById("hallTitleColor").value,
				subtitleColor: document.getElementById("hallSubtitleColor").value,
				videoMutedDefault: document.getElementById("hallMuted").value,
				videoDisableSeek: document.getElementById("hallDisableSeek").value,
				videoDisablePause: document.getElementById("hallDisablePause").value
			};
			post("admin/hallBanners", payload).then(function(data) {
				if (!data.success) { alert(data.message || "保存失败"); return; }
				state.hallBanners = data.banners || [];
				state.hallSlideIndex = 0;
				showToast("大厅展示已保存");
				renderPage();
			});
		};
		var resetHall = document.getElementById("hallResetForm");
		if (resetHall) resetHall.onclick = resetHallForm;
		Array.prototype.forEach.call(document.querySelectorAll(".hall-edit"), function(btn) {
			btn.onclick = function() {
				var id = Number(btn.getAttribute("data-id"));
				var b = (state.hallBanners || []).filter(function(item) { return Number(item.id) === id; })[0];
				if (!b) return;
				setHallField("hallId", b.id);
				setHallField("hallMediaUrl", b.mediaUrl);
				setHallField("hallMediaType", b.mediaType || "IMAGE");
				setHallField("hallTitle", b.title || "");
				setHallField("hallSubtitle", b.subtitle || "");
				setHallField("hallSortNo", b.sortNo || 0);
				setHallField("hallEnabled", b.enabled ? "true" : "false");
				setHallField("hallOverlayEnabled", b.overlayEnabled === false ? "false" : "true");
				setHallField("hallTextPosition", b.textPosition || "LEFT");
				setHallField("hallTitleColor", b.titleColor || "#ffffff");
				setHallField("hallSubtitleColor", b.subtitleColor || "#e2e8f0");
				setHallField("hallLinkType", b.linkType || "NONE");
				setHallField("hallProductId", b.productId || 0);
				setHallField("hallLinkTarget", b.linkTarget || "");
				setHallField("hallMuted", b.videoMutedDefault ? "true" : "false");
				setHallField("hallDisableSeek", b.videoDisableSeek ? "true" : "false");
				setHallField("hallDisablePause", b.videoDisablePause ? "true" : "false");
				var name = document.getElementById("hallUploadName");
				if (name) name.textContent = "当前媒体：" + (b.mediaUrl || "");
				updateHallLinkFields();
				showToast("已载入展示项，可修改后保存");
			};
		});
		Array.prototype.forEach.call(document.querySelectorAll(".hall-delete"), function(btn) {
			btn.onclick = function() {
				if (!confirm("确定删除该大厅展示项吗？")) return;
				post("admin/hallBanners", { action: "delete", id: btn.getAttribute("data-id") }).then(function(data) {
					if (!data.success) { alert(data.message || "删除失败"); return; }
					state.hallBanners = data.banners || [];
					state.hallSlideIndex = 0;
					showToast("大厅展示项已删除");
					renderPage();
				});
			};
		});
	}
	var couponJump = document.querySelector(".coupon-btn");
	if (couponJump) {
		couponJump.onclick = scrollToCouponCenter;
	}
	bindChatEvents();
	var accountForm = document.getElementById("accountRequestForm");
	if (accountForm) {
		var requestType = document.getElementById("accountRequestType");
		var avatarField = document.querySelector(".account-avatar-field");
		var toggleAvatarField = function() {
			if (avatarField) avatarField.style.display = requestType && requestType.value === "AVATAR" ? "" : "none";
		};
		if (requestType) {
			requestType.onchange = toggleAvatarField;
			toggleAvatarField();
		}
		var avatarFile = document.getElementById("accountAvatarFile");
		if (avatarFile) {
			avatarFile.onchange = function() {
				if (!avatarFile.files || !avatarFile.files[0]) return;
				var hint = document.getElementById("accountAvatarHint");
				var hidden = document.getElementById("accountRequestAttachment");
				var progress = document.getElementById("avatarUploadProgress");
				var bar = progress ? progress.querySelector("i") : null;
				if (hidden) hidden.value = "";
				if (progress) progress.classList.add("show");
				if (bar) bar.style.width = "0%";
				if (hint) hint.textContent = "头像正在上传：" + avatarFile.files[0].name;
				var formData = new FormData();
				formData.append("avatar", avatarFile.files[0]);
				uploadFormData("avatarUpload", formData, function(percent) {
					if (bar) bar.style.width = percent + "%";
					if (hint) hint.textContent = "头像正在上传：" + avatarFile.files[0].name + "（" + percent + "%）";
				})
					.then(function(data) {
						if (!data.success) {
							if (hint) hint.textContent = "上传失败：" + (data.message || "请重新选择图片");
							alert(data.message || "头像上传失败");
							return;
						}
						if (bar) bar.style.width = "100%";
						if (hidden) hidden.value = data.avatarUrl || "";
						var preview = document.getElementById("accountAvatarPreview");
						if (preview) preview.innerHTML = '<img src="' + escapeHtml(data.avatarUrl || "") + '" alt="">';
						if (hint) hint.textContent = "头像已上传，提交后等待管理员审核。";
						showToast("头像已上传");
					})
					.catch(function() {
						if (hint) hint.textContent = "上传失败：接口未响应";
						alert("头像上传失败，请重新登录或检查图片格式。");
					});
			};
		}
		accountForm.onsubmit = function(e) {
			e.preventDefault();
			var type = document.getElementById("accountRequestType").value;
			if (type === "CANCEL" && !confirm("注销账号通过后账号将被禁用，确定提交申请吗？")) return;
			if (type === "AVATAR" && !document.getElementById("accountRequestAttachment").value) {
				alert("请先上传头像图片。");
				return;
			}
			post("accountRequests", {
				requestType: type,
				content: document.getElementById("accountRequestContent").value,
				attachmentUrl: document.getElementById("accountRequestAttachment").value
			}).then(function(data) {
				if (!data.success) { alert(data.message || "提交失败"); return; }
				state.accountRequests = data.requests || [];
				showToast("申请已提交，等待管理员审核");
				renderPage();
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".account-review"), function(btn) {
		if (btn.disabled) return;
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			var opinion = document.querySelector('.account-opinion[data-id="' + id + '"]');
			post("admin/accountRequests", {
				requestId: id,
				status: btn.getAttribute("data-status"),
				opinion: opinion ? opinion.value : ""
			}).then(function(data) {
				if (!data.success) { alert(data.message || "审核失败"); return; }
				state.accountRequests = data.requests || [];
				showToast("审核结果已发送到消息中心");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".detail-option"), function(btn) {
		btn.onclick = function() {
			var index = Number(btn.getAttribute("data-index"));
			if (!state.selectedSkuValues) state.selectedSkuValues = {};
			state.selectedSkuValues[index] = btn.getAttribute("data-value");
			var sku = currentDetailSku(state.selectedProduct || {});
			state.selectedColor = sku ? sku.color : "";
			state.selectedSpec = sku ? sku.spec : "";
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".detail-qty"), function(btn) {
		btn.onclick = function() {
			var sku = currentDetailSku(state.selectedProduct || {});
			var stock = Number(sku && sku.stock || 1);
			state.detailQuantity = Math.max(1, Math.min(stock, state.detailQuantity + Number(btn.getAttribute("data-delta"))));
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".qty-btn"), function(btn) {
		btn.onclick = function() {
			var nextQty = Number(btn.getAttribute("data-qty") || 1);
			var payload = nextQty < 1
				? { action: "remove", cartItemId: btn.getAttribute("data-cart") }
				: { action: "update", cartItemId: btn.getAttribute("data-cart"), quantity: nextQty };
			post("cart", payload).then(function(data) {
				if (!data.success) { alert(data.message || "操作失败"); return; }
				state.cart = data.cart || [];
				syncCartSelection(true);
				renderNav();
				renderPage();
				document.getElementById("cartBadge").textContent = cartCount();
			});
		};
	});
	var selectAll = document.getElementById("cartSelectAll");
	if (selectAll) {
		selectAll.indeterminate = selectedCartItems().length > 0 && selectedCartItems().length < state.cart.length;
		selectAll.onchange = function() {
			var checked = selectAll.checked;
			state.cart.forEach(function(item) {
				state.selectedCartItemIds[String(item.id)] = checked;
			});
			renderPage();
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".cart-select-input"), function(input) {
		input.onchange = function() {
			state.selectedCartItemIds[String(input.getAttribute("data-cart"))] = input.checked;
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".pay-order"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认立即支付该订单？")) return;
			post("orders", { action: "pay", orderId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "支付失败"); return; }
				state.orders = data.orders || [];
				state.afterSales = data.afterSales || state.afterSales || [];
				state.growthLogs = data.growthLogs || state.growthLogs || [];
				if (data.user) {
					state.user = data.user;
					updateUserChip();
				}
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".confirm-order"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认已收到商品？")) return;
			post("orders", { action: "confirm", orderId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "确认失败"); return; }
				state.orders = data.orders || [];
				state.afterSales = data.afterSales || state.afterSales || [];
				state.growthLogs = data.growthLogs || state.growthLogs || [];
				if (data.user) {
					state.user = data.user;
					updateUserChip();
				}
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".cancel-order"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认取消该订单？")) return;
			post("orders", { action: "cancel", orderId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "取消失败"); return; }
				state.orders = data.orders || [];
				state.afterSales = data.afterSales || state.afterSales || [];
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".order-contact-merchant"), function(btn) {
		btn.onclick = function() {
			var orderId = btn.getAttribute("data-order");
			startChatWith("MERCHANT", btn.getAttribute("data-merchant"), function() {
				sendChatCard("ORDER_CARD", orderId);
				setPage("messages");
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".order-contact-admin"), function(btn) {
		btn.onclick = function() {
			var orderId = btn.getAttribute("data-order");
			startChatWith("ADMIN", 1, function() {
				sendChatCard("ORDER_CARD", orderId);
				setPage("messages");
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".apply-after-sale"), function(btn) {
		btn.onclick = function() {
			var reason = prompt("请输入售后原因");
			if (!reason) return;
			var type = prompt("售后类型：退款 / 退货退款", "退款") || "退款";
			post("orders", { action: "afterSale", orderId: btn.getAttribute("data-order"), productId: btn.getAttribute("data-product"), afterSaleType: type, reason: reason }).then(function(data) {
				if (!data.success) { alert(data.message || "申请失败"); return; }
				state.orders = data.orders || [];
				state.afterSales = data.afterSales || [];
				showToast("售后申请已提交");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-product"), function(btn) {
		btn.onclick = function() {
			var rating = prompt("请输入评分 1-5", "5");
			if (!rating) return;
			var content = prompt("请输入评价内容", "商品体验不错") || "";
			post("orders", { action: "review", orderId: btn.getAttribute("data-order"), productId: btn.getAttribute("data-product"), rating: rating, content: content }).then(function(data) {
				if (!data.success) { alert(data.message || "评价失败"); return; }
				state.orders = data.orders || [];
				state.growthLogs = data.growthLogs || state.growthLogs || [];
				if (data.user) {
					state.user = data.user;
					updateUserChip();
				}
				showToast("评价成功，成长值 +10");
				renderPage();
			});
		};
	});
	var detailReviewForm = document.getElementById("detailReviewForm");
	if (detailReviewForm) {
		detailReviewForm.onsubmit = function(e) {
			e.preventDefault();
			if (!state.selectedProduct) return;
			var contentEl = document.getElementById("detailReviewContent");
			var content = contentEl ? contentEl.value.trim() : "";
			if (!content) {
				alert("请填写评价内容。");
				return;
			}
			post("reviews", {
				action: "add",
				productId: state.selectedProduct.id,
				orderId: document.getElementById("detailReviewOrder").value,
				rating: document.getElementById("detailReviewRating").value,
				content: content
			}).then(function(data) {
				if (!data.success) { alert(data.message || "评价失败"); return; }
				showToast("评价已发布");
				Promise.all([loadProductReviews(state.selectedProduct.id), loadProducts(), loadOrders(), loadReviewStats()]).then(renderPage);
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".review-like"), function(btn) {
		btn.onclick = function() {
			post("reviews", { action: "like", reviewId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "点赞失败"); return; }
				if (state.selectedProduct) loadProductReviews(state.selectedProduct.id).then(renderPage);
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-reply-open"), function(btn) {
		btn.onclick = function() {
			var content = prompt("回复这条评价");
			if (!content) return;
			post("reviews", { action: "reply", reviewId: btn.getAttribute("data-id"), content: content }).then(function(data) {
				if (!data.success) { alert(data.message || "回复失败"); return; }
				showToast("回复已发布");
				if (state.selectedProduct) loadProductReviews(state.selectedProduct.id).then(renderPage);
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-filter-btn"), function(btn) {
		btn.onclick = function() {
			var filter = btn.getAttribute("data-filter") || "all";
			var rating = Number(btn.getAttribute("data-rating") || 0);
			state.reviewFilter = filter === "media" ? "media" : "all";
			state.reviewRatingFilter = filter === "rating" ? rating : 0;
			if (state.selectedProduct) loadProductReviews(state.selectedProduct.id).then(renderPage);
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-emoji"), function(btn) {
		btn.onclick = function() {
			var textarea = document.getElementById("detailReviewContent");
			if (!textarea) return;
			textarea.value += btn.getAttribute("data-emoji") || "";
			textarea.focus();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-media-img"), function(btn) {
		btn.onclick = function() {
			var src = btn.getAttribute("data-src");
			if (src) window.open(src, "_blank");
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-media-remove"), function(btn) {
		btn.onclick = function() {
			state.reviewDraftMedia.splice(Number(btn.getAttribute("data-index") || 0), 1);
			renderPage();
		};
	});
	var detailReviewMedia = document.getElementById("detailReviewMedia");
	if (detailReviewMedia) {
		detailReviewMedia.onchange = function() {
			if (!state.selectedProduct) return;
			var files = Array.prototype.slice.call(detailReviewMedia.files || []);
			if (!files.length) return;
			if ((state.reviewDraftMedia || []).length + files.length > 6) {
				alert("单条评价最多上传 6 个图片或视频。");
				detailReviewMedia.value = "";
				return;
			}
			var chain = Promise.resolve();
			files.forEach(function(file) {
				chain = chain.then(function() {
					var formData = new FormData();
					formData.append("productId", state.selectedProduct.id);
					formData.append("media", file);
					return uploadFormData("reviewMediaUpload", formData).then(function(data) {
						if (!data.success) throw new Error(data.message || "上传失败");
						state.reviewDraftMedia.push(data);
					});
				});
			});
			chain.then(function() {
				detailReviewMedia.value = "";
				renderPage();
			}).catch(function(err) {
				detailReviewMedia.value = "";
				alert(err.message || "上传失败");
			});
		};
	}
	var enhancedReviewForm = document.getElementById("detailReviewForm");
	if (enhancedReviewForm) {
		enhancedReviewForm.onsubmit = function(e) {
			e.preventDefault();
			if (!state.selectedProduct) return;
			var contentEl = document.getElementById("detailReviewContent");
			var content = contentEl ? contentEl.value.trim() : "";
			var mediaIds = (state.reviewDraftMedia || []).map(function(item) { return item.mediaId || item.id; }).filter(Boolean);
			if (!content && !mediaIds.length) {
				alert("请填写评价内容或上传图片/视频。");
				return;
			}
			post("reviews", {
				action: "add",
				productId: state.selectedProduct.id,
				orderId: document.getElementById("detailReviewOrder").value,
				rating: document.getElementById("detailReviewRating").value,
				content: content,
				anonymous: document.getElementById("detailReviewAnonymous") && document.getElementById("detailReviewAnonymous").checked ? "true" : "false",
				mediaIds: mediaIds.join(",")
			}).then(function(data) {
				if (!data.success) { alert(data.message || "评价失败"); return; }
				state.reviewDraftMedia = [];
				state.activeReviewReplyId = null;
				showToast("评价已发布");
				Promise.all([loadProductReviews(state.selectedProduct.id), loadProducts(), loadOrders(), loadReviewStats()]).then(renderPage);
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".review-like"), function(btn) {
		btn.onclick = function() {
			if (!state.user && !state.merchant && !state.admin) {
				alert("请先登录后操作评论。");
				return;
			}
			post("reviews", { action: "like", reviewId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "点赞失败"); return; }
				if (state.selectedProduct) loadProductReviews(state.selectedProduct.id).then(renderPage);
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-reply-open"), function(btn) {
		btn.onclick = function() {
			if (!state.user && !state.merchant && !state.admin) {
				alert("请先登录后操作评论。");
				return;
			}
			state.activeReviewReplyId = Number(btn.getAttribute("data-id") || 0);
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-reply-cancel"), function(btn) {
		btn.onclick = function() {
			state.activeReviewReplyId = null;
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".review-reply-form"), function(form) {
		form.onsubmit = function(e) {
			e.preventDefault();
			var textarea = form.querySelector("textarea");
			var content = textarea ? textarea.value.trim() : "";
			if (!content) {
				alert("请输入回复内容。");
				return;
			}
			post("reviews", { action: "reply", reviewId: form.getAttribute("data-id"), content: content }).then(function(data) {
				if (!data.success) { alert(data.message || "回复失败"); return; }
				state.activeReviewReplyId = null;
				showToast("回复已发布");
				if (state.selectedProduct) loadProductReviews(state.selectedProduct.id).then(renderPage);
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".remove-btn"), function(btn) {
		btn.onclick = function() {
			post("cart", { action: "remove", cartItemId: btn.getAttribute("data-cart") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				state.cart = data.cart || [];
				syncCartSelection(true);
				renderNav();
				renderPage();
				document.getElementById("cartBadge").textContent = cartCount();
			});
		};
	});
	var submit = document.querySelector(".submit-order");
	if (submit) {
		submit.onclick = function() {
			var address = selectedAddress();
			if (!address) {
				alert("请先选择或添加收货地址。");
				setPage("address");
				return;
			}
			var checkedIds = selectedCartItemIds();
			if (!checkedIds.length) {
				alert("请先选择要结算的商品。");
				return;
			}
			post("orders", {
				selectedPlatformCouponId: state.selectedPlatformCouponId || 0,
				selectedStackableCouponId: state.selectedStackableCouponId || 0,
				selectedMerchantCouponId: state.selectedMerchantCouponId || 0,
				cartItemIds: checkedIds.join(","),
				addressId: address.id
			}).then(function(data) {
				if (!data.success) { alert(data.message || "提交失败"); return; }
				state.orders = data.orders || [];
				if (data.user) {
					state.user = data.user;
					updateUserChip();
				}
				state.selectedPlatformCouponId = null;
				state.selectedStackableCouponId = null;
				state.selectedMerchantCouponId = null;
				checkedIds.forEach(function(id) {
					delete state.selectedCartItemIds[String(id)];
				});
				Promise.all([loadCart(), loadUserCoupons()]).then(function() {
					updateCouponBadge();
					setPage("orders");
				});
			});
		};
	}
	var platformCouponSelect = document.getElementById("platformCouponSelect");
	if (platformCouponSelect) platformCouponSelect.onchange = function() { state.selectedPlatformCouponId = platformCouponSelect.value ? Number(platformCouponSelect.value) : null; renderPage(); };
	var stackableCouponSelect = document.getElementById("stackableCouponSelect");
	if (stackableCouponSelect) stackableCouponSelect.onchange = function() { state.selectedStackableCouponId = stackableCouponSelect.value ? Number(stackableCouponSelect.value) : null; renderPage(); };
	var merchantCouponSelect = document.getElementById("merchantCouponSelect");
	if (merchantCouponSelect) merchantCouponSelect.onchange = function() { state.selectedMerchantCouponId = merchantCouponSelect.value ? Number(merchantCouponSelect.value) : null; renderPage(); };
	Array.prototype.forEach.call(document.querySelectorAll('input[name="checkoutAddress"]'), function(input) {
		input.onchange = function() {
			state.selectedAddressId = input.value;
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".go-address"), function(btn) {
		btn.onclick = function() { setPage("address"); };
	});
	Array.prototype.forEach.call(document.querySelectorAll(".go-products"), function(btn) {
		btn.onclick = scrollToProductCenter;
	});
	var provinceSelect = document.getElementById("addrProvince");
	var citySelect = document.getElementById("addrCity");
	var districtSelect = document.getElementById("addrDistrict");
	if (provinceSelect && citySelect && districtSelect) {
		provinceSelect.onchange = function() {
			var cities = regionCities(provinceSelect.value);
			var cityName = cities[0] ? cities[0].name : "";
			citySelect.innerHTML = optionHtml(cities, cityName);
			districtSelect.innerHTML = optionHtml(regionDistricts(provinceSelect.value, cityName), "");
		};
		citySelect.onchange = function() {
			districtSelect.innerHTML = optionHtml(regionDistricts(provinceSelect.value, citySelect.value), "");
		};
	}
	var addressForm = document.getElementById("addressForm");
	if (addressForm) {
		addressForm.onsubmit = function(e) {
			e.preventDefault();
			var phoneValue = document.getElementById("addrPhone").value.trim();
			var payload = {
				action: "add",
				receiverName: document.getElementById("addrName").value.trim(),
				phone: phoneValue,
				province: document.getElementById("addrProvince").value,
				city: document.getElementById("addrCity").value,
				district: document.getElementById("addrDistrict").value,
				detail: document.getElementById("addrDetail").value.trim(),
				defaultAddress: document.getElementById("addrDefault").checked ? "1" : "0"
			};
			if (!payload.receiverName || !payload.phone || !payload.province || !payload.city || !payload.district || !payload.detail) {
				alert("请完整填写收货人、手机号、省市区和详细地址。");
				return;
			}
			if (!/^1[3-9]\d{9}$/.test(phoneValue)) {
				alert("请输入有效的中国大陆手机号。");
				return;
			}
			post("addresses", payload).then(function(data) {
				if (data.success) { state.addresses = data.addresses || []; renderPage(); }
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".default-address"), function(btn) {
		btn.onclick = function() {
			post("addresses", { action: "default", addressId: btn.getAttribute("data-id") }).then(function(data) {
				if (data.success) { state.addresses = data.addresses || []; renderPage(); }
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".delete-address"), function(btn) {
		btn.onclick = function() {
			post("addresses", { action: "delete", addressId: btn.getAttribute("data-id") }).then(function(data) {
				if (data.success) { state.addresses = data.addresses || []; renderPage(); }
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".profile-link"), function(btn) {
		btn.onclick = function() { setPage(btn.getAttribute("data-page")); };
	});
	var adminUserSearch = document.getElementById("adminUserSearch");
	if (adminUserSearch) {
		adminUserSearch.oninput = function() {
			state.adminUserSearch = adminUserSearch.value;
			renderPage();
		};
	}
	["adminUserStatusFilter", "adminUserVipFilter", "adminUserOrderFilter", "adminUserSort"].forEach(function(id) {
		var el = document.getElementById(id);
		if (!el) return;
		el.onchange = function() {
			if (id === "adminUserStatusFilter") state.adminUserStatusFilter = el.value;
			if (id === "adminUserVipFilter") state.adminUserVipFilter = el.value;
			if (id === "adminUserOrderFilter") state.adminUserOrderFilter = el.value;
			if (id === "adminUserSort") state.adminUserSort = el.value;
			renderPage();
		};
	});
	var adminOrderKeyword = document.getElementById("adminOrderKeyword");
	if (adminOrderKeyword) {
		adminOrderKeyword.oninput = function() {
			state.adminOrderKeyword = adminOrderKeyword.value;
			renderPage();
		};
	}
	var adminOrderStatusFilter = document.getElementById("adminOrderStatusFilter");
	if (adminOrderStatusFilter) {
		adminOrderStatusFilter.onchange = function() {
			state.adminOrderStatusFilter = adminOrderStatusFilter.value;
			renderPage();
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".admin-order-save"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			post("admin/orders", { orderId: id, status: document.querySelector('.admin-order-status[data-id="' + id + '"]').value }).then(function(data) {
				if (!data.success) { alert(data.message || "更新失败"); return; }
				state.adminOrders = data.orders || [];
				state.afterSales = data.afterSales || state.afterSales || [];
				state.adminLogs = data.adminLogs || state.adminLogs || [];
				showToast("订单状态已更新");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-order-delete"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认删除该订单？删除后订单和明细都会移除。")) return;
			post("admin/orders", { action: "delete", orderId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				state.adminOrders = data.orders || [];
				state.afterSales = data.afterSales || state.afterSales || [];
				state.adminLogs = data.adminLogs || state.adminLogs || [];
				showToast("订单已删除");
				renderPage();
			});
		};
	});
	var merchantOrderStatusFilter = document.getElementById("merchantOrderStatusFilter");
	if (merchantOrderStatusFilter) {
		merchantOrderStatusFilter.onchange = function() {
			state.merchantOrderStatusFilter = merchantOrderStatusFilter.value;
			renderPage();
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-order-ship"), function(btn) {
		btn.onclick = function() {
			var expressCompany = prompt("请输入快递公司", "顺丰速运");
			if (!expressCompany) return;
			var trackingNo = prompt("请输入运单号");
			if (!trackingNo) return;
			post("merchant/orders", { action: "ship", orderId: btn.getAttribute("data-id"), expressCompany: expressCompany, trackingNo: trackingNo }).then(function(data) {
				if (!data.success) { alert(data.message || "发货失败"); return; }
				state.merchantOrders = data.orders || [];
				state.afterSales = data.afterSales || state.afterSales || [];
				showToast("订单已发货");
				renderPage();
			});
		};
	});
	var adminUserOrderStatusFilter = document.getElementById("adminUserOrderStatusFilter");
	if (adminUserOrderStatusFilter) {
		adminUserOrderStatusFilter.onchange = function() {
			state.adminUserOrderStatusFilter = adminUserOrderStatusFilter.value;
			renderPage();
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-row,.admin-user-select"), function(btn) {
		btn.onclick = function() {
			state.adminSelectedUserId = btn.getAttribute("data-id");
			if (btn.classList.contains("admin-user-select")) state.adminUserDetailTab = "basic";
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-delete"), function(btn) {
		btn.onclick = function(e) {
			if (e) e.stopPropagation();
			if (!confirm("确定删除该用户账号吗？删除后不可恢复。")) return;
			post("admin/users", { action: "deleteUser", userId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				applyAdminUserData(data);
				showToast("用户已删除");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-password-toggle"), function(btn) {
		btn.onclick = function(e) {
			if (e) e.stopPropagation();
			var id = btn.getAttribute("data-id");
			state.adminUserPasswordVisible[id] = !state.adminUserPasswordVisible[id];
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-tab"), function(btn) {
		btn.onclick = function() {
			state.adminUserDetailTab = btn.getAttribute("data-tab");
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-save,.admin-user-vip-save"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			var user = state.adminUsers.filter(function(item) { return String(item.id) === String(id); })[0] || {};
			var nextStatus = document.getElementById("adminUserStatus") ? document.getElementById("adminUserStatus").value : (user.status || "正常");
			if (nextStatus === "停用" && user.status !== "停用" && !confirm("确认禁用该用户账号？禁用后用户不能登录和下单，历史订单仍可查看。")) return;
			post("admin/users", {
				userId: id,
				username: document.getElementById("adminUserName") ? document.getElementById("adminUserName").value : user.username,
				email: document.getElementById("adminUserEmail") ? document.getElementById("adminUserEmail").value : user.email,
				phone: document.getElementById("adminUserPhone") ? document.getElementById("adminUserPhone").value : user.phone,
				password: document.getElementById("adminUserPassword") ? document.getElementById("adminUserPassword").value : "",
				growthValue: document.getElementById("adminUserGrowth") ? document.getElementById("adminUserGrowth").value : growthValue(user),
				points: document.getElementById("adminUserPoints") ? document.getElementById("adminUserPoints").value : Number(user.points || 0),
				status: nextStatus
			}).then(function(data) {
				if (!data.success) { alert(data.message || "保存失败"); return; }
				applyAdminUserData(data);
				showToast("用户信息已保存");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-order-save"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			post("admin/users", { action: "orderStatus", orderId: id, status: document.querySelector('.admin-user-order-status[data-id="' + id + '"]').value }).then(function(data) {
				if (!data.success) { alert(data.message || "修改失败"); return; }
				applyAdminUserData(data);
				showToast("订单状态已更新");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-order-delete"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认删除该订单？删除后不可恢复。")) return;
			post("admin/users", { action: "deleteOrder", orderId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				applyAdminUserData(data);
				showToast("订单已删除");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-address-default"), function(btn) {
		btn.onclick = function() {
			post("admin/users", { action: "addressDefault", userId: btn.getAttribute("data-user"), addressId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "设置失败"); return; }
				applyAdminUserData(data);
				showToast("默认地址已更新");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-address-delete"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认删除该收货地址？")) return;
			post("admin/users", { action: "addressDelete", userId: btn.getAttribute("data-user"), addressId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				applyAdminUserData(data);
				showToast("地址已删除");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-coupon-issue"), function(btn) {
		btn.onclick = function() {
			var select = document.getElementById("adminUserCouponTemplate");
			if (!select || !select.value) { alert("暂无可发放的优惠券模板"); return; }
			post("admin/users", { action: "issueCoupon", userId: btn.getAttribute("data-user"), couponId: select.value }).then(function(data) {
				if (!data.success) { alert(data.message || "发放失败"); return; }
				applyAdminUserData(data);
				showToast((data.issueCount || 0) > 0 ? "优惠券已发放" : "该用户已达到领取上限");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".admin-user-coupon-void"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认作废该未使用优惠券？")) return;
			post("admin/users", { action: "voidCoupon", userId: btn.getAttribute("data-user"), userCouponId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "作废失败"); return; }
				applyAdminUserData(data);
				showToast("优惠券已作废");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".product-status"), function(btn) {
		btn.onclick = function() {
			post("admin/products", { productId: btn.getAttribute("data-id"), status: btn.getAttribute("data-status") }).then(function(data) {
				if (data.success) { state.products = data.products || []; renderPage(); }
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".save-user"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			post("admin/users", {
				userId: id,
				username: document.querySelector('.user-name[data-id="' + id + '"]').value,
				email: document.querySelector('.user-email[data-id="' + id + '"]').value,
				phone: document.querySelector('.user-phone[data-id="' + id + '"]').value,
				growthValue: document.querySelector('.user-growth[data-id="' + id + '"]').value,
				points: document.querySelector('.user-points[data-id="' + id + '"]').value,
				status: document.querySelector('.user-status[data-id="' + id + '"]').value
			}).then(function(data) {
				if (!data.success) { alert(data.message || "保存失败"); return; }
				state.adminUsers = data.users || [];
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".save-order"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			post("admin/orders", { orderId: id, status: document.querySelector('.order-status[data-id="' + id + '"]').value }).then(function(data) {
				if (!data.success) { alert(data.message || "更新失败"); return; }
				state.adminOrders = data.orders || [];
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".delete-order"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认删除该订单？")) return;
			post("admin/orders", { action: "delete", orderId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				state.adminOrders = data.orders || [];
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-edit"), function(btn) {
		btn.onclick = function() {
			var product = state.merchantProducts.filter(function(p) { return String(p.id) === String(btn.getAttribute("data-id")); })[0];
			document.getElementById("pageRoot").innerHTML = renderMerchantProductForm(product);
			bindPageActions();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-add-product"), function(btn) {
		btn.onclick = function() {
			state.page = "merchantProductAdd";
			try {
				sessionStorage.setItem("hishoppingPage", "merchantProductAdd");
			} catch (e) {
			}
			document.getElementById("pageTitle").textContent = "新增商品";
			document.getElementById("pageRoot").innerHTML = renderMerchantProductForm();
			bindPageActions();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-submit,.merchant-offsale"), function(btn) {
		btn.onclick = function() {
			post("merchant/products", { action: btn.classList.contains("merchant-submit") ? "submit" : "offSale", productId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "操作失败"); return; }
				state.merchantProducts = data.products || [];
				renderPage();
			});
		};
	});
	var merchantProductForm = document.getElementById("merchantProductForm");
	if (merchantProductForm) {
		bindMerchantMediaActions();
		bindProductAttrsEditor();
		var mpMediaFile = document.getElementById("mpMediaFile");
		if (mpMediaFile) {
			mpMediaFile.onchange = function() {
				if (!mpMediaFile.files || !mpMediaFile.files.length) return;
				var mediaList = readMerchantMedia();
				if (mediaList.length >= 6) { alert("商品媒体最多支持 6 个。"); mpMediaFile.value = ""; return; }
				var fileName = document.getElementById("mpMediaFileName");
				var originalHint = fileName ? fileName.textContent : "";
				if (fileName) fileName.textContent = mpMediaFile.files[0].name + " 上传中...";
				mpMediaFile.disabled = true;
				var fd = new FormData();
				fd.append("media", mpMediaFile.files[0]);
				fetch(apiUrl("merchant/productImage"), { method: "POST", body: fd }).then(parseJsonResponse).then(function(data) {
					if (!data.success) {
						alert(data.message || "上传失败");
						if (fileName) fileName.textContent = originalHint || "最多 6 个图片或视频，第一项为主图";
						return;
					}
					mediaList.push({ mediaType: data.mediaType || "IMAGE", mediaUrl: data.mediaUrl || data.imageUrl, sortNo: mediaList.length + 1 });
					updateMerchantMedia(mediaList);
					if (fileName) fileName.textContent = "已上传 " + mediaList.length + " 个媒体";
					mpMediaFile.value = "";
				}).catch(function(err) {
					alert(err && err.message ? err.message : "上传接口未响应，请稍后重试。");
					if (fileName) fileName.textContent = originalHint || "最多 6 个图片或视频，第一项为主图";
				}).finally(function() {
					mpMediaFile.disabled = false;
					mpMediaFile.value = "";
				});
			};
		}
		var generateSkuRows = document.getElementById("generateSkuRows");
		var addSkuAttr = document.getElementById("addSkuAttr");
		if (addSkuAttr) {
			addSkuAttr.onclick = function() {
				var attrs = readSkuAttrs();
				if (attrs.length >= 4) { alert("最多支持 4 个参数层级。"); return; }
				attrs.push({ name: "参数" + (attrs.length + 1), values: ["默认"] });
				document.getElementById("skuAttrsBox").innerHTML = skuAttrsHtml(attrs);
				bindPageActions();
			};
		}
		Array.prototype.forEach.call(document.querySelectorAll(".sku-attr-remove"), function(btn) {
			btn.onclick = function() {
				var attrs = readSkuAttrs();
				var index = Number(btn.closest(".sku-attr-card").getAttribute("data-index"));
				attrs.splice(index, 1);
				document.getElementById("skuAttrsBox").innerHTML = skuAttrsHtml(attrs.length ? attrs : [{ name: "规格", values: ["默认"] }]);
				bindPageActions();
			};
		});
		if (generateSkuRows) {
			generateSkuRows.onclick = function() {
				var attrs = readSkuAttrs();
				var combos = cartesianSkuValues(attrs).length;
				if (combos > 80) { alert("SKU 组合超过 80 个，请减少参数值。"); return; }
				var rows = buildSkuRows(attrs, {
					price: Number(document.getElementById("mpPrice").value || 0),
					oldPrice: Number(document.getElementById("mpOldPrice").value || document.getElementById("mpPrice").value || 0),
					stock: Number(document.getElementById("mpStock").value || 0)
				}, readSkuRows());
				document.getElementById("skuRowsBody").innerHTML = skuRowsHtml(rows);
			};
		}
		var applySkuBatch = document.getElementById("applySkuBatch");
		if (applySkuBatch) {
			applySkuBatch.onclick = function() {
				var price = document.getElementById("batchSkuPrice").value;
				var oldPrice = document.getElementById("batchSkuOldPrice").value;
				var stock = document.getElementById("batchSkuStock").value;
				Array.prototype.forEach.call(document.querySelectorAll("#skuRowsBody .sku-row"), function(row) {
					if (price !== "") row.querySelector(".sku-price").value = price;
					if (oldPrice !== "") row.querySelector(".sku-old-price").value = oldPrice;
					if (stock !== "") row.querySelector(".sku-stock").value = stock;
				});
			};
		}
		merchantProductForm.onsubmit = function(e) {
			e.preventDefault();
			var idEl = document.getElementById("merchantProductId");
			var skuAttrs = readSkuAttrs();
			var skuRows = readSkuRows();
			if (!skuRows.some(function(row) { return row.enabled && row.price > 0 && row.stock >= 0; })) {
				alert("请至少保留一个启用且价格有效的 SKU。");
				return;
			}
			post("merchant/products", {
				action: idEl ? "update" : "add",
				productId: idEl ? idEl.value : "",
				name: document.getElementById("mpName").value,
				categoryId: document.getElementById("mpCategory").value,
				price: document.getElementById("mpPrice").value,
				oldPrice: document.getElementById("mpOldPrice").value,
				stock: document.getElementById("mpStock").value,
				imageUrl: document.getElementById("mpImageUrl").value,
				mediaList: document.getElementById("mpMediaList") ? document.getElementById("mpMediaList").value : "",
				shortDesc: document.getElementById("mpShortDesc").value,
				detailDesc: document.getElementById("mpDetailDesc").value,
				colorOptions: (skuAttrs[0] && skuAttrs[0].values || ["默认"]).join(","),
				specOptions: (skuAttrs[1] && skuAttrs[1].values || ["标准"]).join(","),
				skuAttrs: JSON.stringify(skuAttrs),
				skuOptions: JSON.stringify(skuRows),
				productAttrs: JSON.stringify(readProductAttrs())
			}).then(function(data) {
				if (!data.success) { alert(data.message || "保存失败"); return; }
				state.merchantProducts = data.products || [];
				setPage("merchantProductList");
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-audit"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			var status = btn.getAttribute("data-status");
			if (status === "DISABLED" && !confirm("确认禁用该商家账号？禁用后该商家商品会下架，商家不能继续登录经营。")) return;
			if (status === "REJECTED" && !confirm("确认驳回该商家入驻申请？")) return;
			var opinionEl = document.querySelector('.audit-opinion[data-id="' + id + '"]');
			post("admin/merchants", { merchantId: id, status: status, opinion: opinionEl ? opinionEl.value : "" }).then(function(data) {
				if (!data.success) { alert(data.message || "操作失败"); return; }
				applyAdminMerchantData(data);
				showToast("操作成功");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-filter"), function(btn) {
		btn.onclick = function() {
			state.merchantAuditFilter = btn.getAttribute("data-filter");
			renderPage();
		};
	});
	var merchantSearch = document.getElementById("merchantManageSearch");
	if (merchantSearch) {
		var merchantSearchTimer = null;
		merchantSearch.oninput = function() {
			state.merchantManageSearch = merchantSearch.value;
			clearTimeout(merchantSearchTimer);
			merchantSearchTimer = setTimeout(renderPage, 180);
		};
		merchantSearch.onchange = function() {
			state.merchantManageSearch = merchantSearch.value;
			renderPage();
		};
		merchantSearch.onkeydown = function(e) {
			if (e.key === "Enter") merchantSearch.onchange();
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-products-toggle"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			state.merchantExpandedId = String(state.merchantExpandedId || "") === String(id) ? null : id;
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".password-toggle"), function(btn) {
		btn.onclick = function() {
			var input = document.querySelector('.merchant-password-input[data-id="' + btn.getAttribute("data-id") + '"]');
			if (!input) return;
			var visible = input.type === "text";
			input.type = visible ? "password" : "text";
			btn.textContent = visible ? "显示" : "隐藏";
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".coupon-center-tab"), function(btn) {
		btn.onclick = function() {
			state.couponManageTab = btn.getAttribute("data-tab");
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-product-filter"), function(btn) {
		btn.onclick = function() {
			if (btn.getAttribute("data-kind") === "audit") state.merchantProductAuditFilter = btn.getAttribute("data-value");
			if (btn.getAttribute("data-kind") === "sale") state.merchantProductSaleFilter = btn.getAttribute("data-value");
			renderPage();
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-save"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			var payload = { action: "update", merchantId: id };
			Array.prototype.forEach.call(document.querySelectorAll('.merchant-field[data-id="' + id + '"]'), function(input) {
				payload[input.getAttribute("data-field")] = input.value;
			});
			post("admin/merchants", payload).then(function(data) {
				if (!data.success) { alert(data.message || "保存失败"); return; }
				applyAdminMerchantData(data);
				showToast("商家资料已保存");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-product-save"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			var payload = { action: "productUpdate", productId: id, merchantId: btn.getAttribute("data-merchant") };
			Array.prototype.forEach.call(document.querySelectorAll('.merchant-product-field[data-id="' + id + '"]'), function(input) {
				payload[input.getAttribute("data-field")] = input.value;
			});
			if (!payload.productAttrs) {
				var originalProduct = state.adminAuditProducts.filter(function(item) { return String(item.id) === String(id); })[0];
				payload.productAttrs = JSON.stringify(productDisplayAttrs(originalProduct || {}));
			}
			syncAdminProductMediaPayload(payload);
			post("admin/merchants", payload).then(function(data) {
				if (!data.success) { alert(data.message || "保存失败"); return; }
				applyAdminMerchantData(data);
				showToast("商品资料已保存");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-product-audit"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			if (btn.getAttribute("data-action") === "reject" && !confirm("确认驳回该商品审核？")) return;
			var opinionEl = document.querySelector('.product-audit-opinion[data-id="' + id + '"]');
			post("admin/merchants", { action: "productAudit", productId: id, merchantId: btn.getAttribute("data-merchant"), auditAction: btn.getAttribute("data-action"), opinion: opinionEl ? opinionEl.value : "" }).then(function(data) {
				if (!data.success) { alert(data.message || "审核失败"); return; }
				applyAdminMerchantData(data);
				showToast("商品审核已更新");
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-product-sale"), function(btn) {
		btn.onclick = function() {
			if (btn.getAttribute("data-status") === "OFF_SALE" && !confirm("确认下架该商品？")) return;
			post("admin/merchants", { action: "productSale", productId: btn.getAttribute("data-id"), merchantId: btn.getAttribute("data-merchant"), saleStatus: btn.getAttribute("data-status") }).then(function(data) {
				if (!data.success) { alert(data.message || "上下架失败"); return; }
				applyAdminMerchantData(data);
				showToast("商品状态已更新");
				renderPage();
			});
		};
	});	Array.prototype.forEach.call(document.querySelectorAll(".product-audit"), function(btn) {
		btn.onclick = function() {
			var id = btn.getAttribute("data-id");
			var opinionEl = document.querySelector('.product-audit-opinion[data-id="' + id + '"]');
			post("admin/productAudit", { productId: id, action: btn.getAttribute("data-action"), opinion: opinionEl ? opinionEl.value : "" }).then(function(data) {
				if (!data.success) { alert(data.message || "审核失败"); return; }
				state.adminAuditProducts = data.products || [];
				renderPage();
			});
		};
	});
	var couponTemplateForm = document.getElementById("couponTemplateForm");
	if (couponTemplateForm) {
		var ctType = document.getElementById("ctType");
		var updateCouponTemplateFields = function() {
			var type = ctType.value;
			Array.prototype.forEach.call(document.querySelectorAll(".coupon-discount-field"), function(el) { el.classList.toggle("hidden", type !== "DISCOUNT"); });
			Array.prototype.forEach.call(document.querySelectorAll(".coupon-amount-field"), function(el) { el.classList.toggle("hidden", type === "DISCOUNT"); });
			Array.prototype.forEach.call(document.querySelectorAll(".coupon-vip-field"), function(el) { el.classList.toggle("hidden", type !== "VIP" && type !== "DISCOUNT"); });
			Array.prototype.forEach.call(document.querySelectorAll(".coupon-new-field"), function(el) { el.classList.toggle("hidden", type !== "NEW_USER"); });
			document.getElementById("ctNewUser").checked = type === "NEW_USER";
			document.getElementById("ctVipCoupon").checked = type === "VIP" || type === "DISCOUNT";
		};
		ctType.onchange = updateCouponTemplateFields;
		updateCouponTemplateFields();
		couponTemplateForm.onsubmit = function(e) {
			e.preventDefault();
			post("admin/coupons", {
				action: "add",
				couponName: document.getElementById("ctName").value,
				couponType: document.getElementById("ctType").value,
				amount: document.getElementById("ctAmount").value,
				discountRate: document.getElementById("ctDiscountRate").value,
				minAmount: document.getElementById("ctMinAmount").value,
				vipLevel: document.getElementById("ctVipLevel").value,
				perUserLimit: document.getElementById("ctLimit").value,
				validDays: document.getElementById("ctValidDays").value,
				newUserCoupon: document.getElementById("ctNewUser").checked ? "1" : "0",
				vipCoupon: document.getElementById("ctVipCoupon").checked ? "1" : "0",
				stackable: document.getElementById("ctStackable").checked ? "1" : "0",
				homeTitle: document.getElementById("ctHomeTitle").value,
				homeSubtitle: document.getElementById("ctHomeSubtitle").value,
				useScope: "ALL",
				description: document.getElementById("ctDescription").value
			}).then(function(data) {
				if (!data.success) { alert(data.message || "新增失败"); return; }
				state.couponTemplates = data.templates || [];
				state.couponLogs = data.logs || [];
				renderPage();
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".coupon-status"), function(btn) {
		btn.onclick = function() {
			post("admin/coupons", { action: "status", couponId: btn.getAttribute("data-id"), status: btn.getAttribute("data-status") }).then(function(data) {
				if (!data.success) { alert(data.message || "更新失败"); return; }
				state.couponTemplates = data.templates || [];
				state.couponLogs = data.logs || [];
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".coupon-delete"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认删除该优惠券模板？已发放过的模板会被数据库保护，不能删除。")) return;
			post("admin/coupons", { action: "delete", couponId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				state.couponTemplates = data.templates || [];
				state.couponLogs = data.logs || [];
				renderPage();
			});
		};
	});
	var merchantCouponForm = document.getElementById("merchantCouponForm");
	if (merchantCouponForm) {
		var mcType = document.getElementById("mcType");
		var updateMerchantCouponFields = function() {
			var discount = mcType.value === "DISCOUNT";
			Array.prototype.forEach.call(document.querySelectorAll(".merchant-coupon-discount"), function(el) { el.classList.toggle("hidden", !discount); });
			Array.prototype.forEach.call(document.querySelectorAll(".merchant-coupon-amount"), function(el) { el.classList.toggle("hidden", discount); });
		};
		mcType.onchange = updateMerchantCouponFields;
		updateMerchantCouponFields();
		merchantCouponForm.onsubmit = function(e) {
			e.preventDefault();
			post("merchant/coupons", {
				action: "save",
				couponName: document.getElementById("mcName").value,
				couponType: document.getElementById("mcType").value,
				amount: document.getElementById("mcAmount").value,
				discountRate: document.getElementById("mcDiscountRate").value,
				minAmount: document.getElementById("mcMinAmount").value,
				perUserLimit: document.getElementById("mcLimit").value,
				validDays: document.getElementById("mcValidDays").value,
				description: document.getElementById("mcDescription").value
			}).then(function(data) {
				if (!data.success) { alert(data.message || "保存失败"); return; }
				state.merchantCoupons = data.templates || [];
				state.merchantCouponUsers = data.userCoupons || [];
				state.couponLogs = data.logs || state.couponLogs;
				showToast("店铺优惠券已保存");
				renderPage();
			});
		};
	}
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-coupon-status"), function(btn) {
		btn.onclick = function() {
			post("merchant/coupons", { action: "status", couponId: btn.getAttribute("data-id"), status: btn.getAttribute("data-status") }).then(function(data) {
				if (!data.success) { alert(data.message || "更新失败"); return; }
				state.merchantCoupons = data.templates || [];
				state.merchantCouponUsers = data.userCoupons || [];
				renderPage();
			});
		};
	});
	Array.prototype.forEach.call(document.querySelectorAll(".merchant-coupon-delete"), function(btn) {
		btn.onclick = function() {
			if (!confirm("确认删除该店铺优惠券模板？已发放过的模板不能删除。")) return;
			post("merchant/coupons", { action: "delete", couponId: btn.getAttribute("data-id") }).then(function(data) {
				if (!data.success) { alert(data.message || "删除失败"); return; }
				state.merchantCoupons = data.templates || [];
				state.merchantCouponUsers = data.userCoupons || [];
				renderPage();
			});
		};
	});
	var couponIssueForm = document.getElementById("couponIssueForm");
	if (couponIssueForm) {
		var issueTargetPlaceholders = {
			VIP_LEVEL: "请输入VIP等级，如：5",
			USER: "请输入用户ID，如：10001",
			USERNAME: "请输入用户名，如：zhangsan",
			USER_GROUP: "请选择一个或多个用户类别",
			ALL: "全体用户可留空"
		};
		var selectedTargetGroups = function() {
			var groupSelect = document.getElementById("ciTargetGroups");
			if (!groupSelect) return "";
			var values = [];
			Array.prototype.forEach.call(groupSelect.querySelectorAll("input[type='checkbox']"), function(option) {
				if (option.checked) values.push(option.value);
			});
			return values.join(",");
		};
		var updateTargetGroupSummary = function() {
			var groupSelect = document.getElementById("ciTargetGroups");
			var summary = document.getElementById("ciTargetGroupSummary");
			if (!groupSelect || !summary) return;
			var labels = [];
			Array.prototype.forEach.call(groupSelect.querySelectorAll("input[type='checkbox']"), function(option) {
				if (option.checked) labels.push(option.parentNode.textContent.trim());
			});
			summary.textContent = labels.length ? labels.join("、") : "请选择用户类别";
		};
		var updateIssueTargetPlaceholder = function() {
			var issueType = document.getElementById("ciIssueType").value;
			var targetInput = document.getElementById("ciTargetValue");
			var groupSelect = document.getElementById("ciTargetGroups");
			var groupMode = issueType === "USER_GROUP";
			targetInput.classList.toggle("hidden", groupMode);
			if (groupSelect) groupSelect.classList.toggle("hidden", !groupMode);
			targetInput.setAttribute("placeholder", issueTargetPlaceholders[issueType] || issueTargetPlaceholders.VIP_LEVEL);
			if (groupMode) {
				targetInput.value = selectedTargetGroups();
				updateTargetGroupSummary();
			}
		};
		var resetIssueTargetValue = function() {
			var issueType = document.getElementById("ciIssueType").value;
			var targetInput = document.getElementById("ciTargetValue");
			if (issueType === "USER_GROUP") {
				targetInput.value = selectedTargetGroups();
			} else if (issueType === "ALL") {
				targetInput.value = "";
			} else {
				targetInput.value = "";
			}
		};
		var rememberIssueForm = function() {
			updateIssueTargetPlaceholder();
			state.couponIssueForm = {
				couponId: document.getElementById("ciCouponId").value,
				issueType: document.getElementById("ciIssueType").value,
				targetValue: document.getElementById("ciTargetValue").value,
				batch: document.getElementById("ciBatch").checked
			};
			document.getElementById("ciCouponId").disabled = state.couponIssueForm.batch;
		};
		document.getElementById("ciCouponId").onchange = rememberIssueForm;
		document.getElementById("ciIssueType").onchange = function() {
			resetIssueTargetValue();
			updateIssueTargetPlaceholder();
			rememberIssueForm();
		};
		document.getElementById("ciTargetValue").oninput = rememberIssueForm;
		document.getElementById("ciTargetGroups").onchange = rememberIssueForm;
		document.getElementById("ciBatch").onchange = rememberIssueForm;
		updateIssueTargetPlaceholder();
		rememberIssueForm();
		couponIssueForm.onsubmit = function(e) {
			e.preventDefault();
			rememberIssueForm();
			var isMerchantIssue = couponIssueForm.getAttribute("data-merchant") === "1";
			post(isMerchantIssue ? "merchant/coupons" : "admin/coupons", {
				action: "issue",
				couponId: document.getElementById("ciCouponId").value,
				couponIds: (!isMerchantIssue && document.getElementById("ciBatch").checked) ? "AUTO" : "",
				issueType: document.getElementById("ciIssueType").value,
				targetValue: document.getElementById("ciTargetValue").value
			}).then(function(data) {
				if (!data.success) { alert(data.message || "发放失败"); return; }
				if (isMerchantIssue) {
					state.merchantCoupons = data.templates || [];
					state.merchantCouponUsers = data.userCoupons || [];
				} else {
					state.couponTemplates = data.templates || [];
				}
				state.couponLogs = data.logs || [];
				alert("发放完成：成功 " + data.issueCount + "，跳过 " + data.skipCount + "，批次 " + data.batchNo);
				renderPage();
			});
		};
	}
}

function bindDelegatedPageActions() {
	if (document.body.getAttribute("data-page-delegate-bound") === "true") return;
	document.body.setAttribute("data-page-delegate-bound", "true");
	document.addEventListener("click", function(e) {
		var target = e.target;
		if (!target || !target.closest) return;
		var action = target.closest(".view-detail,.visual-open,.add-cart,.buy-now,.detail-add-cart,.detail-buy-now,.product-media-thumb,.product-media-prev,.product-media-next");
		if (!action || action.onclick) return;
		var appPage = document.getElementById("appPage");
		if (appPage && !appPage.contains(action)) return;
		if (action.classList.contains("view-detail") || action.classList.contains("visual-open")) {
			e.preventDefault();
			openDetail(Number(action.getAttribute("data-id")), state.page || "home");
			return;
		}
		if (action.classList.contains("add-cart") || action.classList.contains("buy-now")) {
			e.preventDefault();
			addToCart(Number(action.getAttribute("data-id")), action.classList.contains("buy-now"));
			return;
		}
		if (action.classList.contains("detail-add-cart") || action.classList.contains("detail-buy-now")) {
			e.preventDefault();
			var productId = Number(action.getAttribute("data-id"));
			var goCart = action.classList.contains("detail-buy-now");
			var chain = addToCart(productId, false, state.detailQuantity);
			if (goCart) chain.then(function() { setPage("cart"); });
			return;
		}
		if (action.classList.contains("product-media-thumb")) {
			e.preventDefault();
			state.detailMediaIndex = Number(action.getAttribute("data-index") || 0);
			renderPage();
			return;
		}
		if (action.classList.contains("product-media-prev") || action.classList.contains("product-media-next")) {
			e.preventDefault();
			var mediaList = productMediaList(state.selectedProduct || {});
			if (!mediaList.length) return;
			var delta = action.classList.contains("product-media-prev") ? -1 : 1;
			state.detailMediaIndex = (Number(state.detailMediaIndex || 0) + delta + mediaList.length) % mediaList.length;
			renderPage();
		}
	});
}

function renderPage() {
	var root = document.getElementById("pageRoot");
	var html = "";
	if (state.page === "home") html = renderHome();
	if (state.page === "detail") html = renderDetail();
	if (state.page === "cart") html = renderCart();
	if (state.page === "orders") html = renderOrders();
	if (state.page === "reports") html = renderReports();
	if (state.page === "profile") html = renderProfile();
	if (state.page === "favorites") html = renderFavorites();
	if (state.page === "settings") html = renderSettings();
	if (state.page === "messages" || state.page === "merchantMessages" || state.page === "adminMessages") html = renderMessages();
	if (state.page === "vip") html = renderVipCenter();
	if (state.page === "coupons") html = renderCoupons();
	if (state.page === "address") html = renderAddress();
	if (state.page === "merchantCenter") html = renderMerchantCenter();
	if (state.page === "merchantProductList") html = renderMerchantProductList();
	if (state.page === "merchantProductAdd") html = renderMerchantProductForm();
	if (state.page === "merchantOrders") html = renderMerchantOrders();
	if (state.page === "merchantReports") html = renderMerchantReports();
	if (state.page === "merchantAnalytics") html = renderMerchantAnalytics();
	if (state.page === "merchantCoupons") html = renderMerchantCoupons();
	if (state.page === "merchantProfile") html = renderMerchantProfile();
	if (isAdminPage(state.page)) html = renderAdmin();
	if (state.page === "adminMessages") html = renderMessages();
	if (state.page === "adminHall") html = renderAdminHall();
	if (state.page === "adminAccountRequests") html = renderAdminAccountRequests();
	if (state.page === "adminMerchantAudit") html = renderAdminMerchantAudit();
	if (state.page === "adminReports") html = renderAdminReports();
	if (state.page === "adminAnalytics") html = renderAdminAnalytics();
	if (state.page === "adminCouponCenter") html = renderAdminCouponCenter();
	if (state.page === "adminCouponManage") html = renderAdminCouponManage();
	if (state.page === "adminCouponIssue") html = renderAdminCouponIssue();
	root.innerHTML = html + renderReportModal() + renderAdminReportModal();
	bindDelegatedPageActions();
	bindPageActions();
}

function updateAuthView() {
	var isAdmin = state.authMode === "admin";
	var isMerchant = state.authMode === "merchant";
	var isLogin = isAdmin || isMerchant || state.authType === "login";
	if (isMerchant) isLogin = state.authType === "login";
	var icon = document.getElementById("authHeroIcon");
	document.getElementById("authTitle").textContent = isAdmin ? "管理员登录" : (isMerchant ? (isLogin ? "商家登录" : "商家注册") : (isLogin ? "用户登录" : "创建账号"));
	document.getElementById("authSubtitle").textContent = isAdmin ? "登录后进入商城后台管理" : (isMerchant ? (isLogin ? "审核通过后进入商家工作台" : "提交资料后等待管理员审核") : (isLogin ? "登录后继续探索精选商品" : "注册成为嗨购商城会员"));
	document.getElementById("enterBtn").textContent = isAdmin ? "管理员登录" : (isMerchant ? (isLogin ? "商家登录" : "提交商家申请") : (isLogin ? "登录进入" : "注册并进入"));
	document.getElementById("switchHint").textContent = isLogin ? "还没有账号？" : "已有账号？";
	document.getElementById("switchAuth").textContent = isLogin ? "立即注册" : "去登录";
	document.getElementById("accountLabel").textContent = isAdmin ? "管理员账号" : (isMerchant ? (isLogin ? "商家独立ID / 商家名称" : "商家名称") : (isLogin ? "账号" : "邮箱"));
	document.getElementById("accountInput").placeholder = isAdmin ? "请输入管理员账号" : (isMerchant ? (isLogin ? "请输入商家独立ID或商家名称" : "请输入商家名称") : (isLogin ? "请输入用户ID / 邮箱 / 手机号" : "member@hishopping.com"));
	document.getElementById("passwordInput").placeholder = isLogin ? "请输入密码" : "请设置登录密码";
	Array.prototype.forEach.call(document.querySelectorAll(".register-only"), function(el) { el.classList.toggle("hidden", isLogin || isMerchant); });
	Array.prototype.forEach.call(document.querySelectorAll(".login-only"), function(el) { el.classList.toggle("hidden", !isLogin); });
	document.getElementById("merchantRegisterFields").classList.toggle("hidden", !(isMerchant && !isLogin));
	document.getElementById("merchantProgressBox").classList.toggle("hidden", !isMerchant);
	document.querySelector(".switch-text").classList.toggle("hidden", isAdmin);
	icon.src = isAdmin ? "assets/img/admin-login-entry-icon.png?v=login-admin-icon-20260601" : (isMerchant ? "assets/img/auth-merchant-role.png?v=role-merchant-20260601" : "assets/img/hishopping-mascot.png");
	updateCaptchaView(needsRegisterCaptcha());
	updateAuthRoleButtons();
	showMessage("");
}

function updateAuthRoleButtons() {
	Array.prototype.forEach.call(document.querySelectorAll(".auth-role-btn"), function(btn) {
		btn.classList.toggle("active", btn.getAttribute("data-auth-mode") === state.authMode);
	});
}

document.getElementById("switchAuth").onclick = function() {
	if (state.authMode !== "merchant") state.authMode = "user";
	state.authType = state.authType === "login" ? "register" : "login";
	document.getElementById("passwordInput").value = "";
	updateAuthView();
};

Array.prototype.forEach.call(document.querySelectorAll(".auth-role-btn"), function(btn) {
	btn.onclick = function() {
		state.authMode = btn.getAttribute("data-auth-mode");
		state.authType = "login";
		document.getElementById("accountInput").value = "";
		document.getElementById("passwordInput").value = "";
		updateAuthView();
	};
});

document.getElementById("captchaImage").onclick = resetCaptcha;
document.getElementById("captchaRefresh").onclick = resetCaptcha;

document.getElementById("merchantProgressBtn").onclick = function() {
	var input = document.getElementById("merchantProgressInput");
	var resultBox = document.getElementById("merchantProgressResult");
	var countdownBox = document.getElementById("merchantProgressCountdown");
	var contact = input.value.trim();
	if (window.merchantProgressCountdownTimer) clearInterval(window.merchantProgressCountdownTimer);
	window.merchantProgressCountdownTimer = null;
	countdownBox.classList.add("hidden");
	countdownBox.innerHTML = "";
	if (!contact) {
		resultBox.innerHTML = '<p class="merchant-progress-empty">请输入注册时绑定的邮箱或手机号。</p>';
		return;
	}
	resultBox.innerHTML = '<p class="merchant-progress-empty">正在查询...</p>';
	get("merchant/progress?contact=" + encodeURIComponent(contact)).then(function(data) {
		if (!data.success) {
			resultBox.innerHTML = '<p class="merchant-progress-empty">' + escapeHtml(data.message || "查询失败") + '</p>';
			return;
		}
		var rows = data.merchants || [];
		if (!rows.length) {
			resultBox.innerHTML = '<p class="merchant-progress-empty">没有查询到商家注册记录，请确认邮箱或手机号是否填写正确。</p>';
			return;
		}
		resultBox.innerHTML = rows.map(function(item) {
			return '<article class="merchant-progress-card"><div><b>' + escapeHtml(item.shopName || item.merchantName || "") + '</b><small>商家ID：' + escapeHtml(item.merchantCode || "") + '</small></div>' +
				badge(statusText(item.status), merchantStatusTone(item.status)) +
				'<p>联系人：' + escapeHtml(item.contactName || "") + ' · ' + escapeHtml(item.contactPhone || "") + '</p>' +
				'<p>邮箱：' + escapeHtml(item.email || "") + '</p>' +
				'<p>经营类目：' + escapeHtml(item.businessCategory || "") + '</p>' +
				'<p>营业地址：' + escapeHtml(item.businessAddress || "") + '</p>' +
				(item.rejectReason ? '<p class="merchant-progress-reason">审核意见：' + escapeHtml(item.rejectReason) + '</p>' : '') +
				(item.reviewTime ? '<p>审核时间：' + escapeHtml(item.reviewTime) + '</p>' : '<p>提交时间：' + escapeHtml(item.createTime || "") + '</p>') +
				'</article>';
		}).join("");
		showMerchantProgressCountdown(5);
	}).catch(function(err) {
		resultBox.innerHTML = '<p class="merchant-progress-empty">' + escapeHtml(err.message || "查询失败") + '</p>';
	});
};

document.getElementById("enterBtn").onclick = function() {
	var account = document.getElementById("accountInput").value.trim();
	var password = document.getElementById("passwordInput").value.trim();
	var username = document.getElementById("usernameInput").value.trim();
	var phone = document.getElementById("phoneInput").value.trim();
	var captcha = document.getElementById("captchaInput").value.trim();
	if (!account) {
		showMessage(state.authMode === "admin" ? "请输入管理员账号。" : (state.authType === "login" ? "请输入用户ID、邮箱或手机号。" : "请输入邮箱。"));
		return;
	}
	if (!password) {
		showMessage("请输入密码。");
		return;
	}
	if (state.authMode === "user" && state.authType !== "login" && !username) {
		showMessage("请输入用户名。");
		return;
	}
	if (state.authMode === "user" && state.authType !== "login" && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(account)) {
		showMessage("请输入正确的邮箱格式。");
		return;
	}
	if (state.authMode === "user" && state.authType !== "login" && !/^1[3-9]\d{9}$/.test(phone)) {
		showMessage("请输入有效的中国大陆手机号。");
		return;
	}
	if (needsRegisterCaptcha() && !captcha) {
		showMessage("请输入验证码。");
		return;
	}
	showMessage("正在处理...", true);
	if (state.authMode === "merchant" && state.authType !== "login") {
		post("merchant/register", {
			merchantName: account,
			password: password,
			contactName: document.getElementById("merchantContactName").value.trim(),
			contactPhone: document.getElementById("merchantContactPhone").value.trim(),
			email: document.getElementById("merchantEmail").value.trim(),
			shopName: document.getElementById("merchantShopName").value.trim(),
			shopDesc: document.getElementById("merchantDesc").value.trim(),
			businessCategory: document.getElementById("merchantCategory").value.trim(),
			businessAddress: document.getElementById("merchantAddress").value.trim(),
			captcha: captcha
		}).then(function(data) {
			if (!data.success) { showMessage(data.message || "提交失败"); resetCaptcha(); return; }
			showMerchantRegisterSuccess(data);
			state.authType = "login";
			setTimeout(updateAuthView, 10000);
		}).catch(function(err) { showMessage(err.message); });
	} else if (state.authMode === "admin") {
		post("login", {
			mode: "admin",
			account: account,
			password: password
		}).then(afterAuth).catch(function(err) { showMessage(err.message); });
	} else if (state.authMode === "merchant") {
		post("login", {
			mode: "merchant",
			account: account,
			password: password
		}).then(afterAuth).catch(function(err) { showMessage(err.message); });
	} else if (state.authType === "login") {
		post("login", {
			mode: "user",
			account: account,
			password: password
		}).then(afterAuth).catch(function(err) { showMessage(err.message); });
	} else {
		post("register", {
			username: username,
			email: account,
			phone: phone,
			password: password,
			captcha: captcha
		}).then(function(data) {
			if (!data.success) {
				showMessage(data.message || "操作失败");
				resetCaptcha();
				return;
			}
			afterAuth(data);
		}).catch(function(err) { showMessage(err.message); resetCaptcha(); });
	}
};

function afterAuth(data, restored) {
	if (!data.success) {
		showMessage(data.message || "操作失败");
		return;
	}
	state.user = data.user || null;
	state.admin = data.admin || null;
	state.merchant = data.merchant || null;
	applyCouponState();
	document.getElementById("authPage").classList.add("hidden");
	document.getElementById("appPage").classList.remove("hidden");
	updateShellForRole();
	Promise.all([loadProducts(), loadHallBanners(false)]).then(function() {
		return Promise.all([loadCart(), loadOrders(), loadAddresses(), loadUserCoupons(), loadFavorites(), loadMessages()]);
	}).then(function() {
		document.getElementById("cartBadge").textContent = cartCount();
		updateCouponBadge();
		var savedPage = "home";
		try {
			savedPage = sessionStorage.getItem("hishoppingPage") || "home";
		} catch (e) {
		}
		setPage(state.admin ? (restored && isAdminPage(savedPage) ? savedPage : "admin") : (state.merchant ? (restored && isMerchantPage(savedPage) ? savedPage : "merchantCenter") : (restored && !isAdminPage(savedPage) && !isMerchantPage(savedPage) ? savedPage : "home")));
	}).catch(function(err) {
		document.getElementById("pageRoot").innerHTML = '<div class="empty-cart"><h3>数据加载失败</h3><p class="muted">' + escapeHtml(err.message) + '</p></div>';
	});
}

function restoreSession() {
	get("session").then(function(data) {
		if (data.success) {
			afterAuth(data, true);
		}
	}).catch(function() {
	});
}

document.getElementById("logoutBtn").onclick = function() {
	post("logout", {}).then(function() {
		state.user = null;
		state.admin = null;
		state.merchant = null;
		state.cart = [];
		state.selectedCartItemIds = {};
		state.orders = [];
		state.adminOrders = [];
		state.adminUsers = [];
		state.adminAddresses = [];
		state.adminUserCoupons = [];
		state.merchants = [];
		state.merchantProducts = [];
		state.merchantOrders = [];
		state.merchantCoupons = [];
		state.merchantCouponUsers = [];
		state.adminAuditProducts = [];
		state.couponTemplates = [];
		state.userCoupons = [];
		state.favoriteIds = [];
		state.favorites = [];
		state.couponLogs = [];
		state.addresses = [];
		state.selectedAddressId = null;
		applyCouponState();
		state.selectedCouponId = null;
		try {
			sessionStorage.removeItem("hishoppingPage");
		} catch (e) {
		}
		updateCouponBadge();
		updateOrderRefresh();
		document.getElementById("appPage").classList.add("hidden");
		document.getElementById("authPage").classList.remove("hidden");
		updateShellForRole();
	});
};

document.querySelector(".cart-jump").onclick = function() {
	if (state.admin || state.merchant) return;
	setPage("cart");
};

document.querySelector(".coupon-jump").onclick = function() {
	if (state.admin || state.merchant) return;
	scrollToCouponCenter();
};

document.getElementById("vipEntryBtn").onclick = function() {
	if (state.admin) {
		setPage("admin");
		return;
	}
	if (state.merchant) return;
	setPage("vip");
};

document.getElementById("searchInput").oninput = function() {
	state.searchKeyword = this.value;
	if (state.admin) {
		renderPage();
		return;
	}
	if (state.page !== "home") {
		setPage("home");
		return;
	}
	renderPage();
};

updateAuthView();
updateCouponBadge();
restoreSession();



