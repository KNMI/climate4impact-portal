<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <jsp:include page="../includes.jsp" />
    
  
    <style type="text/css">
     .x-form-field{
    	margin: 0 0 0 0;
    	font:normal 12px courier new;
  		}
  	</style>
  </head>
 <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />

	<div class="impactcontent">
		<div class="cmscontent">  
	
	 

		</div>
  		<div class="bodycontent"> 
			<div id="container" style="width:100%;"></div>
			
			   <div class="cmscontent">
      <%try{out.print(DrupalEditor.showDrupalContent("?q=customcatalog",request,false));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%>
      </div>
      <form method="get" action="/impactportal/data/catalogbrowser.jsp?">
      <input type="text" name="catalog"  class="textbox" size="110" value=""/>
      <input type="submit" name="login" value="Go" />
      </form> 

      
<!-- 			<div id="contexthelp"><%//try{out.print(DrupalEditor.showDrupalContent("?q=catalogbrowsercontext",request));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%></div>-->
		</div>
	</div>
	<jsp:include page="../footer.jsp" />
  </body>
</html>