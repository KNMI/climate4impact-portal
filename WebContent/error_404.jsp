<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"  import="impactservice.ImpactService"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
String searchCommand=request.getParameter("q");
		//Detect if we found a searchstring
		String searchString = "";
		if(searchCommand!=null){
			try{
		if(searchCommand.indexOf("search/node/")==0){
			searchString=searchCommand.substring("search/node/".length());
			tools.DebugConsole.println(searchString);
		}
			}catch(Exception e){
		searchString="";
			}
		}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="includes.jsp" />
  </head>
  <body>
  <%
  String referrer = request.getHeader("referer"); 
   impactservice.MessagePrinters.emailFatalErrorMessage("Page not found!","Page not found: "+request.getAttribute("javax.servlet.forward.request_uri")+"\nReferrer: "+referrer);
    
  %>
    <jsp:include page="header.jsp" />
    <!-- Contents -->
    
    <div class="impactcontent">

      <h1>404, Page not found.</h1><br/>
          <div style="float: right;clear:both;overflow:none; border: none;">
      <img src="/impactportal/images/Stick_figure-wondering.png" alt="Page not found."/>
	</div>
       <div class="textstandardleft">
      <p>
      Sorry, we cannot find what you're looking for...<br/>You can try to search:
      	<span class="searchbox"><input id="searchbox2" class="searchbox"
		type="text" size="16" value="<%=searchString%>"
		onkeypress="startSearchKeyPressed(event,this.form,'searchbox2')" /></span>
      </p>
      <br/>
      <a href="javascript:javascript:history.go(-1)">Go back</a>
      <br/>

    </div>
    </div>
    <!-- /Contents -->
    <jsp:include page="footer.jsp" />
  </body>
</html>