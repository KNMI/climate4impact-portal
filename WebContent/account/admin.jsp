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
		<h1>Administration page</h1>
		<h2>User information:</h2>
		<a href="/impactportal/ImpactService?service=admin&request=getusers">Get user overview in JSON</a>
		<h2>OAuth2 test:</h2>
		<a href="/impactportal/account/OAuth2.jsp">OAuth2 test</a>
		</div>
	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>