<%@ page language="java" contentType="text/html; charset=UTF-8" import="impactservice.* "%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="downscaling.* "%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <!-- VIEW RESTRICTION: LOGGED AND SUBSCRIBED -->
<c:set var="isSubscribed" value="false" scope="request"/>

 <%
    try{
		ImpactUser user = LoginManager.getUser(request);
		if(DownscalingAuth.isUserSubscribed(user)){
			request.setAttribute("isSubscribed", "true");
		}else{
			out.print("<div class='warning'>");
			out.print("<p> Dear user,</p>");
			out.print("<p> You are not subscribed to the Downscaling Service. By Clicking the following button you accept the <a href=''>Terms of Use</a> of this service");
	   		out.print("<form name='subscription-form' action='../DownscalingService/subscribe' method='post'>");
	   		out.print("<input type='text' name='username' value='"+user.internalName+"' hidden></input>");
	   		out.print("<input type='text' name='openID' value='"+user.id+"' hidden></input>");
			out.print("<input type='text' name='email'value='"+user.getEmailAddress()+"' hidden></input>");
			out.print("<button name='submit' type='submit'>Subscribe</button>");
			out.print("</form>");
			out.print("</div>");
			}
  	}catch(Exception e){			
  		out.print("<div class='warning'>");
  		out.print(e.getMessage());
  		out.print("</div>");
	}
%>	
