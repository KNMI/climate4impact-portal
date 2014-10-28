<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>

    <jsp:include page="../includes-ext.jsp" />
    <link rel="stylesheet" href="/impactportal/account/login.css" type="text/css" />
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <%
    	ImpactUser user = null;
    	try{
    		user = LoginManager.getUser(request);
    	}catch(Exception e){
    	}
    	if(user!=null){
    %>
    
    <script type="text/javascript" src="../js/components/processors/wpsOverview.js"></script>
    
    <script type="text/javascript">
    Ext.Loader.setConfig({
      enabled: true
  });

  Ext.QuickTips.init();
    Ext.onReady(function(){
    	var container = Ext.create('Ext.container.Container', {
 	        layout: 'fit',
 			renderTo:'container',
 		   	minHeight:500,
 		    scripts:true,
 		    autoScroll:false, 
 		    items:[wpsOverView],
 		
 		    loader: {} 
    	});
    });
    <% } %>
    </script>
  </head>
  <body>
		<jsp:include page="../header.jsp" /> 
		<!-- Contents -->
		<jsp:include page="loginmenu.jsp" />
		<div class="impactcontent"> 
		
		<div class="breadcrumb"><a href="login.jsp">Account</a> » Processing </div>
		<h1>Processing</h1>
		<%
		if (user==null){
			 %>
			 	<ul>
			 	<li>Before you can start processing, you need to <a href="#" onclick="generateLoginDialog(true)">sign in</a>
           and <a  href="" onclick='location.reload();'>refresh this page</a>.</li>
			 	<li>See  <a href="../documentation/processing.jsp">processing documentation section.</a></li>
			 	</ul>
			 	
			 <%
			
		  		
		 	}
		if(user!=null){
		%>
	
		
			
			<div id="container"></div>
		
		<%} %>
	
		</div>
		<!-- /Contents -->
	  <jsp:include page="../footer.jsp" />
  </body>
</html>