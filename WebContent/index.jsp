<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh-CN">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>嗨购商城 购物平台</title>
	<link rel="icon" type="image/png" sizes="64x64" href="${ctx}/assets/img/favicon.png?v=site-logo-20260614">
	<link rel="apple-touch-icon" sizes="180x180" href="${ctx}/assets/img/apple-touch-icon.png?v=site-logo-20260614">
	<link rel="stylesheet" href="${ctx}/assets/css/style.css?v=report-nav-icon-20260707-1">
</head>
<body>
	<jsp:include page="/WEB-INF/jsp/auth.jsp" />
	<jsp:include page="/WEB-INF/jsp/app-shell.jsp" />
	<script>window.HISHOPPING_CONTEXT_PATH = "${ctx}";</script>
	<script charset="UTF-8" src="${ctx}/assets/js/regions-mainland.js?v=utf8-login-20260630"></script>
	<script charset="UTF-8" src="${ctx}/assets/js/app.js?v=report-nav-icon-20260707-1"></script>
</body>
</html>

