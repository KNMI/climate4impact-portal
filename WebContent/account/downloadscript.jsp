<%@page import="java.io.PrintWriter"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  import="impactservice.ImpactService,tools.Debug,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters"
  import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<link rel="stylesheet" href="bootstrap.css"></link>
<jsp:include page="../includes-ui.jsp" />

<!-- <script src="bootstrap.min.js"></script>-->
<script src="js/jquery.iframe-transport.js"></script>
</head>
<body>
  <jsp:include page="../header.jsp" />

  <!-- Contents -->
  <%
		String Home = "/impactportal/";
		ImpactUser user = null;
		try {
			user = LoginManager.getUser(request);
		} catch (Exception e) {
		}
		String protocol=request.getProtocol();
		if (!request.isSecure()) {
			response.sendRedirect("http://www.knmi.nl");//TODO: send an error if not secure
		}
		if(user == null){
		    response.sendRedirect(Home+"/account/login.jsp");
		}
		if (user != null) {
%>
  <jsp:include page="loginmenu.jsp" />
  <div class="impactcontent">
    <h1>Get script to download file (set)</h1>
    <div class="textstandardleft">
      <form method="post"
        action="/impactportal/GetDownloadScriptHandler">
        <table style="width: 100%">
          <tbody>
            <tr>
              <td class="collapsibletitle">User name (OpenID):</td>
              <td><input id="openid" type="text" size="50"
                name="openid" value="<%=user.getOpenId()%>"></input><br /></td>
            </tr>
          </tbody>
        </table>
        <br />
        <table style="width: 100%">
          <tbody>
            <tr>
              <td class="collapsibletitle">Password:</td>
              <td><input id="password" type="password"
                name="password" size="50"></input></td>
            </tr>
          </tbody>
        </table>
        <br /> <input type="hidden" name="urls"
          value="<%=request.getParameter("urls")%>" /> <input
          type="submit" value="Get script"></input>
      </form>
      <br />
    </div>
    <div>
      <br /> <b>Actions:</b>
      <ul>
        <li><a href="basket.jsp">Go to your basket</a></li>
        <li><a href="../help/contactexpert.jsp">Having
            problems?</a></li>
      </ul>
    </div>

    <br />
    <div id="showdownloadset">
      <b>The following files will be downloaded:</b><br />
      <div id="downloadfiles" class="files">
        <ul>
          <% String files=request.getParameter("urls");
            if (files!=null) {
            	for (String url:files.split("\r\n")) {
            		out.print("<li>"+url+"</li>\n");
            	}
            }
          %>
        </ul>
      </div>
    </div>
  </div>
  <%} %>

  <jsp:include page="../footer.jsp" />
</body>
</html>