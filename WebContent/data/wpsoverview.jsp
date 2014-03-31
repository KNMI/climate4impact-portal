<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>

    <jsp:include page="../includes-ext.jsp" />
    <script type="text/javascript" src="/impactportal/account/login.js"></script>
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
 		   	minHeight:600,
 		    scripts:true,
 		    autoScroll:false, 
 		    items:[wpsOverView],
 		    padding:'10 10 10 10',
 		    loader: {} 
    	});
    });
    <% } %>
    </script>
  </head>
  <body>
		<jsp:include page="../header.jsp" /> 
		<!-- Contents -->
		<jsp:include page="datamenu.jsp" />
		<div class="impactcontent"> 
		<div class="cmscontent"> 
		<h1>Browse Web Processing Services</h1>
		<%
		if (user==null){
			 %>
			 	<ul>
			 	<li>Before you can start processing, you need to <a href="#" onclick="generateLoginDialog(true)">sign in</a>
           and <a  href="" onclick='location.reload();'>refresh this page</a>.</li>
			 	<li>Please read why you need to login at <a href="../help/howto.jsp#why_login">Howto: Why Login?</a></li>
			 	</ul>
			 <%
		 	}
		if(user!=null){
		%>
	
		
			<div id="content2"></div>
			<div id="container"></div>
		</div>
		</div>
		<%} %>
		<!-- /Contents -->
	  <jsp:include page="../footer.jsp" />
  </body>
</html>