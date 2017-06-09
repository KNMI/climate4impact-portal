<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="impactservice.DrupalEditor" import="impactservice.Configuration"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes.jsp" />
</head>
<body>
	<jsp:include page="../header.jsp" />
	<!-- Contents -->

	<div class="impactcontent">
		<div class="cmscontent">
			<%
		    if(Configuration.getPortalMode()=="c4i"){
		   	 try{out.print(DrupalEditor.showDrupalContent("?q=home",request,response));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}
		    }else{
		    	try{out.print(DrupalEditor.showDrupalContent("?q=c3s-magic-home",request,response));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}
			    
		    }
		    
		    %>
		</div>
	</div>



	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
</body>
</html>