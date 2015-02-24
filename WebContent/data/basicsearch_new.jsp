<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    
    <script type="text/javascript" src="../js/jqueryextensions/jquery.collapsible.min.js"></script>
    <script type="text/javascript" src="/impactportal/adagucviewer/webmapjs/WMJSTools.js"></script>
	  <script type="text/javascript" src="basicsearchsettings/variables.js"></script>
	  <script type="text/javascript" src="basicsearch.js"></script>
	  
	  <style>
		.refreshinfo{
			
			display:inline;
			float:left;
			overflow:hidden;
			width:740px;
			height:20px;
			
		}
	  </style>
	   
	  

      <script type="text/javascript">
     
      var searchSession=undefined;  
      <%
      impactservice.SessionManager.SearchSession searchSession=(impactservice.SessionManager.SearchSession) session.getAttribute("searchsession");
      if(searchSession!=null){
        out.println("  searchSession="+searchSession.getAsJSON());
      }
      %>
      var impactservice='<%=impactservice.Configuration.getImpactServiceLocation()%>service=search&';
    
    </script>
  </head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
	
	<!--  <div style="line-height:0px;height:0px;margin:32px 10px;float: right;clear:both;overflow:none; border: none;"></div>

      <%try{out.print(DrupalEditor.showDrupalContent("?q=search_help",request,response,false,false));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%>
      -->
  			<div class="impactcontent">
  			<div id="info"></div>
			<h1>Search</h1>
			Search catagories:<br/>
			
        	
        	
        	
  		</div>
   
	 	
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>