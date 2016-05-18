<%@page import="impactservice.SessionManager,impactservice.LoginManager,impactservice.ImpactUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes.jsp" />
    <script type="text/javascript" src="catalogbrowser/catalogbrowser.js"></script>
    <script type="text/javascript" src="fileviewer/fileviewer.js"></script>
    <link rel="stylesheet" href="fileviewer/fileviewer.css" />
    <script type="text/javascript" src="esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="esgfsearch/esgfsearch-propertychoosers.js"></script>
    <script type="text/javascript" src="esgfsearch/esgfsearch.js"></script>
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <script type="text/javascript" src="/impactportal/js/components/basket/basket.js"></script> 
    
	<link rel="stylesheet" href="esgfsearch/esgfsearch.css" />
	<link rel="stylesheet" href="esgfsearch/simplecomponent.css" />
	 <%
			ImpactUser user = null;
				try{
					user = LoginManager.getUser(request);
				}catch(Exception e){
			
				}
				
				 if (user!=null){
		%>
	  	<script type="text/javascript">
				openid = '<%=user.getOpenId()%>';
				
			</script>
		<%} %>
      <script type="text/javascript">
     
    

      $( document ).ready(function() {
        var el = $("#searchcontainer");
        renderSearchInterface({element:el,service:"/impactportal/esgfsearch?",query:""});
        
   
      });
    </script>
  </head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
		
<div class="impactcontent">
	

<!--  <table class="headertable" ><tr><td><h1>Faceted search</h1></td><td class="headerhelptd"><button class="c4i_esgfsearch_help">Help</button></td></tr></table>-->
<button class="c4i_esgfsearch_help">Help</button>
<div id="searchcontainer" style="margin-top:15px;"></div>


</div>   
	 	
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>