<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"  import="impactservice.ImpactService"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="includes.jsp" />
  </head>
  <body>
    <jsp:include page="header.jsp" />
    <!-- Contents -->
    
    <div class="impactcontent">
     <div class="textstandardleft">
      <h1>A problem occurred!</h1>

      <%
      out.print(session.getAttribute("message")); 
      %>
      <br/><br/>
      The system administrator has been notified.<br/>
      <hr/>
      <b>Actions:</b>
      <ul>
      <li><a href="/impactportal/help/contactexpert.jsp">Provide feedback via the contact form.</a></li>
      <li><a href="javascript:javascript:history.go(-1)">Go back to previous page.</a></li>
      </ul>
    </div>
    </div>
    
    <!-- /Contents -->
    <jsp:include page="footer.jsp" />
  </body>
</html>