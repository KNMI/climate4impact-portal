<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.DebugConsole,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes-ui.jsp" />
<link rel="stylesheet" href="/impactportal/account/login.css" type="text/css" />
<script type="text/javascript" src="/impactportal/account/login.js"></script>
</head>
<body>
	<jsp:include page="../header.jsp" />
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
			<div style="float: right; clear: both; overflow: none; border: none;">
				<a target="_blank" href="http://openid.net"><img
					src="../images/openid.jpg" alt="openid logo" width="250" /></a><br />
				<a target="_blank" href="http://esgf.org"><img
					src="../images/esgf_network.png" alt="esgf network" width="250" /></a><br />
				<div align="center" style="width: 250px; text-align: center;">
					<a target="_blank" href="http://esgf.org/">The ESGF Federation</a>
				</div>

			</div>

			<h1>Sign in with your ESGF OpenID account</h1>
			<div class="textstandardleft  ">

				
		<jsp:include page="login_include_form.jsp" />

			</div>
		</div>
	</div>

	<%
			} else {

				//Print menu structure
		%>
	<jsp:include page="loginmenu.jsp" />

	<div class="impactcontent">
		<div class="cmscontent">


			<h1>You are signed in.</h1>
			<div class="textstandardleft">
				You have successfully signed in with the following OpenID:<br /> <br />
				<strong><%=user.id%></strong><br /> <br /> <b>Actions:</b>
				<ul>
					<li>View account details: <span id="accountinfo">-</span></li>
					<li>If you are not a member of the CMIP5 group: <a
						href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo:
							Create an ESGF account.</a><br /></li>
					<li><a href="/impactportal/account/logout.jsp">Sign out</a><br /></li>
				</ul>
	

	<%
					//Print warning when retrieving SLCS has failed.
						try {
							impactservice.LoginManager.checkLogin(user.id);
						} catch (Exception e) {
							impactservice.MessagePrinters.printWarningMessage(out, e);
						}
		%>
				</div>
		</div>
	</div>
	<%

					}
				%>
	<br />


	<% if(user!=null){ %>
	<script type="">
				var accountInfo = '';
					try{
						accountInfo = getOpenIDProviderFromOpenId('<%=user.id%>').accountinfo;
					}catch(e){}
					$('#accountinfo').html('<a href="'+accountInfo+'">'+accountInfo+'</a>');
				</script>
	<% } %>

	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
</body>
</html>