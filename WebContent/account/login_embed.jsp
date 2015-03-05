<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.Debug,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes-ui.jsp" />
<link rel="stylesheet" href="/impactportal/account/login.css"
	type="text/css" />
<style type="text/css">
body,.x-body {
	background: white;
	height: auto;
	border: none;
	overflow: hidden;
}
</style>
<script type="text/javascript" src="/impactportal/account/js/login.js"></script>
<script type="text/javascript">
  try {
    opener.setReloadAfterLogin(getUrlVar('doreload') + '');
  } catch (e) {
  }
  try {
    window.parent.setReloadAfterLogin(getUrlVar('doreload') + '');
  } catch (e) {
  }
</script>
</head>
<body>
	<jsp:include page="login_include_openidcomposition.jsp" />
	<!-- Contents -->
	<%
		String Home = "/impactportal/";
		ImpactUser user = null;
		try {
			user = LoginManager.getUser(request);
		} catch (Exception e) {
		}

		if (user == null) {
	%>
	<div class="impactcontent">
		<div class="cmscontent">
			<h1>Sign in with your ESGF OpenID account</h1>
			<jsp:include page="login_include_form.jsp" />
		</div>
	</div>
	<%
		}
		if (user != null) {
	%>
	<div style="text-align: center">
		<h1>You are signed in.</h1>
		You have successfully signed in with the following OpenID:<br /> <br />
		<strong><%=user.getOpenId()%></strong><br /> <br />
		<h1>This window will now close.</h1>
	</div>

	<script type="text/javascript">
    var t = new Timer();
    t.InitializeTimer(250, closeLoginPopupDialog);
  </script>

	<%
		//Print warning when retrieving SLCS has failed.
			try {
				impactservice.LoginManager.checkLogin(user.getId(),request);
			} catch (Exception e) {
				impactservice.MessagePrinters.printWarningMessage(out, e);
			}
		}
	%>

</body>
</html>