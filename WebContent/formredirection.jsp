<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="includes.jsp" />
  </head>
<body onload="document.forms['openid-form-redirection'].submit();">
 <jsp:include page="header.jsp" /> 
<div class="impactcontent">
  <h1> Redirecting to OpenID server, please wait...</h1>
      <form name="openid-form-redirection" action="${message.OPEndpoint}" method="post" accept-charset="utf-8">
        <c:forEach var="parameter" items="${message.parameterMap}"> 
        <input type="hidden" name="${parameter.key}" value="${parameter.value}"/>
        </c:forEach>
        <!-- <button type="submit">.</button> -->
    </form>
</div>
<jsp:include page="footer.jsp" />
    
</body>
</html>

