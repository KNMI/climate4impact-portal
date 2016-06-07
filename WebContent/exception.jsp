<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"  import="impactservice.ImpactService,impactservice.MessagePrinters"%>
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
      <h1>Oops!</h1>
      <h2>We are sorry, but we encountered a problem :(</h2>

      The message is:<br/><br/>
      <div class="alert-box error">
      <span> </span>
      <%
      out.print(session.getAttribute("message")); 
      MessagePrinters.emailFatalErrorMessage("Exception Page",(String)session.getAttribute("message"));
      session.setAttribute("message",null);
      session.removeAttribute("message");
      %>
     
      </div>
      <br/>
      We have been notified and hopefully we are able to fix this problem soon.<br/><br/>
      <hr/>
      <br/>
      <b>If you want you can do the following:</b>
      <ul>
      <li><a href="/impactportal/help/contactexpert.jsp">Provide additional feedback via the contact form.</a></li>
      <li><a href="javascript:javascript:history.go(-1)">Go back to previous page.</a></li>
      </ul>
    </div>
    </div>
    
    <!-- /Contents -->
    <jsp:include page="footer.jsp" />
  </body>
</html>