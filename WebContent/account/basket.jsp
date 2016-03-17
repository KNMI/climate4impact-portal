<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <%
   String Home="/impactportal/"; 
   %>
    <jsp:include page="../includes-ext.jsp" />
    
   <!--  <link rel="stylesheet" type="text/css" href="../js/ux/css/CheckHeader.css" /> -->
     
    <script type="text/javascript" src="../js/components/processors/useProcessor.js"></script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
    <script type="text/javascript" src="../js/components/basket/basketwidget.js"></script>
     <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>

    <script type="text/javascript" src="/impactportal/data/catalogbrowser/catalogbrowser.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/fileviewer.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychoosers.js"></script>
   	<script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch.js"></script>
   	<link rel="stylesheet"        href="/impactportal/data/esgfsearch/esgfsearch.css" />
	<link rel="stylesheet"        href="/impactportal/data/esgfsearch/simplecomponent.css" />
    <link rel="stylesheet"        href="/impactportal/data/fileviewer/fileviewer.css"></link>
    
	
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <script type="text/javascript">
    var impactBase = '<%=Home%>';
    var impactService=impactBase+'ImpactService?';

    var task;
    var openid = "";
    var serverurl = "";
    var serverurlhttps = "";
    var removeId = function(id){
    	
    	var passFn = function(e){
    		var json= Ext.JSON.decode(e.responseText);
   		    if(json.error){
  		    	alert(json.error);
   		    }else{
   				adjustNumberOfJobsDisplayedInMenuBar(json);
   		    }
    		populateJobList();
    	};
    	Ext.Ajax.request({
   		    url: impactService,
   		    success: passFn,   
   		    failure: passFn,
   		    timeout:5000,
   		 	method:'GET',
   		    params: { service:'processor',request:'removeFromList',id:id }  
   		 });
    }
    

    
    
    Ext.Loader.setConfig({
        enabled: true
    });
    Ext.Loader.setPath('Ext.ux', '../js/ux');

  /* Ext.require([
        'Ext.selection.CellModel',
        'Ext.grid.*',
        'Ext.data.*',
        'Ext.util.*',
        'Ext.state.*',
        'Ext.form.*',
        'Ext.ux.CheckColumn',
        'Ext.ux.ButtonColumn'
    ]);*/
    Ext.QuickTips.init();


    Ext.onReady(function(){
    	basketWidget.embed(document.getElementById("basketwidget"));
    	
    });
    </script>
  </head>
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->

		<jsp:include page="loginmenu.jsp" />
 
		<div class="impactcontent">
		<div class="breadcrumb"><a href="login.jsp">Account</a> » Basket </div>
		<h1>Basket</h1>
		<%
			ImpactUser user = null;
				try{
					user = LoginManager.getUser(request);
				}catch(Exception e){
			
				}
				
				 if (user==null){
		%>
			<p>You are not logged in, please go to the <a href="/impactportal/account/login.jsp">login page</a> and log in</p>
			<%
		}else{
			
			%>
			<script type="text/javascript">
				openid = '<%=user.getOpenId()%>';
				serverurl = '<%=Configuration.getHomeURLHTTP()%>';
				serverurlhttps = '<%=Configuration.getHomeURLHTTPS()%>';
			</script>
			<div id="basketwidget"></div>
			<div class="iframeshere"></div>
			<%

		}
		%>

  		</div>
		
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>