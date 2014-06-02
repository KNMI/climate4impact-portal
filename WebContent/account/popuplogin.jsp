<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.DebugConsole,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes-ui.jsp" />

<script type="text/javascript" src="/impactportal/account/js/login.js"></script>
</head>
<body>
	<jsp:include page="../header.jsp" />
	<!-- Contents -->
	<div class="impactcontent">
		<div class="cmscontent">
	    	<button onclick="generateLoginDialog(true)">Sign in</button>
		</div>
	</div>
	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
</body>
</html>