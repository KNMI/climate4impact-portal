<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"  import="impactservice.ImpactService"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
      <h1>Not found! (404)</h1>
      <br/>
      <p>
      We can't find what you're looking for.
      </p>
      <br/>
      <a href="javascript:javascript:history.go(-1)">Go back</a>
      <br/><br/>
     <!--  <p>
      <img src="/impactportal/images/Stick_figure-wondering.png"/>
      </p>-->
    </div>
    
    <!-- /Contents -->
    <jsp:include page="footer.jsp" />
  </body>
</html>