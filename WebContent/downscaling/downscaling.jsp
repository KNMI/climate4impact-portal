<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes.jsp" />
  </head>
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->
		<jsp:include page="downscalingmenu.jsp" />
 
		<div class="impactcontent">
  	    <div class="cmscontent">
  	    <h1>Create downscaling process</h1>
  	    <div class="textstandardleft">
  	    
  	    
  	    <%
  	    	try{
  	  			ImpactUser user = LoginManager.getUser(request);
  	  			out.print("<table class=\"drupal\">");
  	    		out.print("<tr><td>internalName</td><td>"+user.internalName+"</td></tr>");
  	    		out.print("<tr><td>certificateFile</td><td>"+user.certificateFile+"</td></tr>");
  	    		out.print("<tr><td>email</td><td>"+user.getEmailAddress()+"</td></tr>");
  	    		out.print("<tr><td>id</td><td>"+user.id+"</td></tr>");
  	    		out.print("<tr><td>getDataDir()</td><td>"+user.getDataDir()+"</td></tr>");
  	    		out.print("<tr><td>getWorkspace()</td><td>"+user.getWorkspace()+"</td></tr>");
  	    		out.print("<tr><td>configured</td><td>"+user.configured+"</td></tr>");
  	    		out.print("<tr><td>credentialError</td><td>"+user.credentialError+"</td></tr>");
  	    		
  	    		out.print("</table>");
  	    	}catch(Exception e){			
  	    		out.print("No user");
  			}
  	    %>
  	    
  	    </div>

  		</div>
		</div>
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>