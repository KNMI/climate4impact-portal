<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.DebugConsole,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes.jsp" />
</head>
<body>
	<jsp:include page="../header.jsp" />
		
		
	<!-- Contents -->
	<%
		String Home = "/impactportal/";
		ImpactUser user = null;
		try {
			user = LoginManager.getUser(request);
		} catch (Exception e) {
		}

		if (user == null) {
		
		}
		if(request.getServerPort()!=443){
			//response.sendRedirect("");
			//Configuration
		}
	%>
	<jsp:include page="loginmenu.jsp" />



	<div class="impactcontent">
		<div class="cmscontent">
		<h1>Get ESGF credential</h1>
		<div class="textstandardleft">
		<%=request.getServerPort()  %>
		
		
		</div>
		</div>
	</div>
	<jsp:include page="../footer.jsp" />
</body>
</html>