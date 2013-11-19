<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes.jsp" />
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
    <script type="text/javascript">
    var impactService = '/impactportal/ImpactService?';
    $(document).ready(function(){basket.populateBasketList($('#basketlist'))});
    </script>
  </head>
  <body>
	<jsp:include page="../header.jsp" />
	<!-- Contents -->
		<jsp:include page="../account/loginmenu.jsp" />
		<div class="impactcontent">
		<h1>Basket</h1>
		<%
		User user = null;
		try{
			user = LoginManager.getUser(request);
		}catch(Exception e){
		}
		if (user == null){ 
			%>
				<p>You are not logged in, please go to the <a href="../login.jsp">login page</a> and log in</p>
			<%
		}else{
			out.println("Basket for: <strong>"+user.id+"</strong>");
			out.println("<div id='basketlist'/>");
		}
		%>
		</div>
	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>