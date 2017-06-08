<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.*,tools.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <jsp:include page="../includes-ext.jsp" />
      <link rel="stylesheet" href="/impactportal/account/login.css" type="text/css" />
     <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
  
  <%
    	String processorId = null;
  		String wpsEndpoint = Configuration.getHomeURLHTTPS() + "/WPS?";
  		try{
  			processorId = HTTPTools.getHTTPParam(request, "processor");
  		}catch(Exception e){
  		}
  		try{
  			wpsEndpoint = HTTPTools.getHTTPParam(request, "service");
  			Debug.println("wpsEndpoint = "+wpsEndpoint);
  		}catch(Exception e){
  		}
  		
  		if(wpsEndpoint.endsWith("&") == false && wpsEndpoint.endsWith("?") == false){
  			wpsEndpoint += "?";
  		}
  		
      	ImpactUser user = null;
    	try{
    		user = LoginManager.getUser(request);
    	}catch(Exception e){
    	}
    	if(user!=null&&processorId!=null){
    %>

  
  
  
    
    <!-- <link rel="stylesheet" type="text/css" href="../js/ux/css/CheckHeader.css" /> -->
     
    <script type="text/javascript" src="../js/components/processors/useProcessor.js"></script>
    <script type="text/javascript" src="../js/components/basket/basketwidget.js"></script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
    
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>
    
    <script type="text/javascript" src="/impactportal/data/catalogbrowser/catalogbrowser.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/vkbeautify.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/fileviewer.js"></script>    
    <script type="text/javascript" src="/impactportal/data/esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychoosers.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch.js"></script>
    <link rel="stylesheet"        href="/impactportal/data/esgfsearch/esgfsearch.css" />
  <link rel="stylesheet"        href="/impactportal/data/esgfsearch/simplecomponent.css" />
    <link rel="stylesheet"        href="/impactportal/data/fileviewer/fileviewer.css"></link>
    
          <script type="text/javascript" src="/impactportal/data/c4i-processing/c4i-processing.js"></script>
    <script type="text/javascript" src="/impactportal/data/c4i-processing/WMJSProcessing.js"></script> 
    <script type="text/javascript" src="/impactportal/data/c4i-processing/WMJSTimer.js"></script> 
    <script type="text/javascript" src="/impactportal/data/c4i-processing/climate_indices_DEF.js"></script>
    
    <link rel="stylesheet" href="/impactportal/data/c4i-processing/c4i-processing.css" />
     
    <jsp:include page="../includes-adaguc-webmapjs.jsp" />
 
    <script type="text/javascript">
    $( document ).ready(function() {
      renderProcessingInterface(
        {
          element:$('#content2'),
          wpsservice:'<%=wpsEndpoint%>',
          identifier:'<%=processorId%>',
          adagucservice:c4iconfigjs.adagucservice,
          adagucviewer:c4iconfigjs.adagucviewer,
          adagucviewerservice:c4iconfigjs.adagucviewerservice
        }
      );
    });
    </script>
    	 <%
 	}
    %>
  </head>
  <body>
		<jsp:include page="../header.jsp" /> 
		<!-- Contents -->
		<jsp:include page="loginmenu.jsp" />
		<div class="impactcontent"> 
	
		<div class="breadcrumb"><a href="login.jsp">Account</a> » <a href="processing.jsp">Processing </a> » Use a processor » <%=processorId %></div>
		
	
		
		 
		 <%

			
		 	if (user==null){
			 %>
			 	<ul>
			 	<li>Before you can use this processor, you need to <a href="#" onclick="generateLoginDialog(true)">sign in</a> and <a  href="" onclick='location.reload();'>refresh this page</a>.</li>
			 	<li><a href="processing.jsp">Go back to processing overview</a></li>
			 	</ul>
			 <%
		 	}
		 	
		 	if(processorId == null){
	 		%>
			 	<ul>
			 	<li>Before you can start processing, you need to select a processor</li>
			 	<li><a href="processing.jsp">Go back to processing overview to select one.</a></li>
			 	</ul>
			<%	
		 	}
			if(user!=null&&processorId!=null){
			%>

				
				<div id="content2"></div>
				<div id="container" style="padding-bottom:20px; "></div>
				
			 <%
		 	}
		 %>
		 
			
		
		</div>
		<!-- /Contents -->
	  <jsp:include page="../footer.jsp" />
  </body>
</html>