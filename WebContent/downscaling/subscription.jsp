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
  	    <h1>Downscaling portal subscription process</h1>
  	    <div class="textstandardleft">
  	    <%
	  	    try{
	  			ImpactUser user = LoginManager.getUser(request);
	  			if(DownscalingAuth.isUserSubscribed(user)){
	  				out.print("You are already subscribed");
	  			}else{
	  				out.print("<p> Dear user,</p>");
	  				out.print("<p> You are not subscribed to the Downscaling Portal RESTful API (DPRA). By Clicking the following button you accept the <a href=''>Terms of Use</a> of this service");
		    		out.print("<form name='subscription-form' action='../DownscalingService/subscribe' method='post'>");
	 	    		out.print("<input type='text' name='username' value='"+user.internalName+"' hidden></input>");
	 	    		out.print("<input type='text' name='openID' value='"+user.id+"' hidden></input>");
					out.print("<input type='text' name='email'value='"+user.getEmailAddress()+"' hidden></input>");
					out.print("<button name='submit' type='submit'>Subscribe</button>");
					out.print("</form>");
	  			}
	    	}catch(Exception e){			
	    		out.print("Logged user is required in order to subscribe to the Downscaling Portal RESTful API");
			}
  	   %>
  	    
  	    </div>

  		</div>
		</div>
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>