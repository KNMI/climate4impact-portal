<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>

    <jsp:include page="../includes-ext.jsp" />
    <link rel="stylesheet" href="/impactportal/account/login.css" type="text/css" />
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>

    

  </head>
  <body>
		<jsp:include page="../header.jsp" /> 
		<!-- Contents -->
		<jsp:include page="datamenu.jsp" />
		<div class="impactcontent"> 
		 
		<h1>Processing overview</h1>

			<ul>
				<li><a href="../account/processing.jsp">Go to processing options (Account »  Processing).</a></li>
			 	<li><a href="../documentation/processing.jsp">Visit the processing documentation section.</a></li>
			 	
		 	</ul>
	
		
		</div> 
		<!-- /Contents -->
	  <jsp:include page="../footer.jsp" />
  </body>
</html>