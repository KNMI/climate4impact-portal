<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes.jsp" />
  </head>
  <body>
	<jsp:include page="../header.jsp" />

		<jsp:include page="../account/loginmenu.jsp" />
			<div class="impactcontent">
		<h1>OAuth2 login</h1>
		<div class="textstandardleft">
		<div class="oauth2loginbox" onclick="document.location.href='/impactportal/oauth'">
<a class="oauth2loginbutton" href="#"><img src="/impactportal/images/google.png"/> Sign in with Google (beta)</a>
</div>
		</div>
		</div>
	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>