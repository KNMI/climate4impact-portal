<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.Debug,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters,java.net.URLDecoder"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes-ui.jsp" />
<link rel="stylesheet" href="/impactportal/account/login.css" type="text/css" />
<script type="text/javascript" src="/impactportal/account/js/login.js"></script>

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
				<a target="_blank" href="http://esgf.llnl.gov/"><img
					src="../images/esgf_network.png" alt="esgf network" width="250" /></a><br />
				<div align="center" style="width: 250px; text-align: center;">
					<a target="_blank" href="http://esgf.llnl.gov/">The ESGF Federation</a>
				</div>

			</div>

			<h1>Sign in</h1>
			<div class="textstandardleft" style="width:540px;">

				
		<jsp:include page="login_include_form.jsp" />

			</div>
		</div>
	</div>

	<%
	
				String redirectURL = request.getParameter("redirect");
				if(redirectURL!=null){
					redirectURL = URLDecoder.decode(redirectURL,"utf-8");
					request.getSession().setAttribute("redirect", redirectURL);
				}
			} else {
				String redirectURL = (String)request.getSession().getAttribute("redirect");
				if(redirectURL!=null){
					redirectURL = URLDecoder.decode(redirectURL,"utf-8");
					response.sendRedirect(redirectURL);
					request.getSession().removeAttribute("redirect");
					return;
				}
				//Print menu structure
		%>
	<jsp:include page="loginmenu.jsp" />

	<div class="impactcontent">
	

			<div class="breadcrumb"><a href="login.jsp">Account</a> » Account </div>
			<div class="cmscontent">
			<h1>You are signed in</h1>
			<%
			%>
			
			
			<div class="textstandardleft">
				
				
				<!--  Your climate4impact identifier is:<br/><strong><%=user.getId()%></strong><br /><br/>-->
				Your ESGF OpenId identifier is:<br/><strong><%=user.getOpenIdAsString()%></strong><br /><br/>
				Your email is:<br/><strong><%=user.getEmailAddress()%></strong><br /> 
				
	
		<%
			//Print warning when retrieving SLCS has failed.
						try {
							impactservice.LoginManager.checkLogin(user.getId(),request);
							out.println("<br /> <strong>Installed credential info:</strong><br/>"+tools.HTMLParser.textToHTML(user.getLoginInfo())+"<br/><br/>");
						} catch (Exception e) {
							impactservice.MessagePrinters.printWarningMessage(out, e);
						}
		%>
			      
				 <b>Actions:</b>
				<ul>
					
					<li>To register for groups: <a
						href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo:
							Create an ESGF account.</a><br /></li>
					<li><a href="/impactportal/account/logout.jsp">Sign out</a><br /></li>
				</ul>
	


				</div>
		</div>
	</div>
	<%
		}
	%>



	<%
		if(user!=null){
	%>
	<script type="">
				var accountInfo = '';
					try{
						accountInfo = getOpenIDProviderFromOpenId('<%=user.getOpenId()%>').accountinfo;
					}catch(e){}
					$('#accountinfo').html('<a href="'+accountInfo+'">'+accountInfo+'</a>');
				</script>
	<% } %>

	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
</body>
</html>