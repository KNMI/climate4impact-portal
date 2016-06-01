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
			
			MessagePrinters.emailFatalErrorMessage("downloadscript.jsp uses insecure connection", "downloadscript.jsp, 30");
			response.sendRedirect("http://www.knmi.nl");
			
		}
		if(user == null){
		    response.sendRedirect(Home+"/account/login.jsp");
		}
		
		String userNamePrefill=user.getOpenId();
		if (user != null) {
			String userNamePrompt="User name (OpenID):";
			String autoCompleteAttr="";
			if (user.getUserMyProxyService().contains("ceda.ac.uk")) {
				userNamePrompt="CEDA User name:";
				userNamePrefill="";
				autoCompleteAttr="autocomplete=\"off\"";
			}
%>
  <jsp:include page="loginmenu.jsp" />
  <div class="impactcontent">
    <h1>Create a script to download a set of files from your basket</h1>
    <div class="textstandardleft" style="width:540px;">
      <form method="post" <%=autoCompleteAttr%>
        action="/impactportal/GetDownloadScriptHandler">
        <table style="width: 100%">
          <tbody>
            <tr>
              <td><b><%=userNamePrompt%></b></td>
              <td><input id="openid" type="text" size="50"
                name="openid" value="<%=userNamePrefill%>"></input><br /></td>
                <td></td>
            </tr>
       
            <tr>
              <td><b>Password:</b></td>
              <td><input id="password" type="password"
                name="password" size="50"></input></td>
                <td><span>?</span></td>
            </tr>
          </tbody>
        </table>
        
        <span style="float:right;margin-top:2px;">
         <input type="hidden" name="urls"
          value="<%=request.getParameter("urls")%>" /> <input
          type="submit" class="ui-button ui-widget ui-state-default ui-corner-all"  value="Get script"></input>
		</span>
        
        
      </form>
      <br />
      <b>Remark:</b>
      <ul>
        <li>
        This screen needs your password to generate a security certificate for access to ESGF data on your behalf.<br/>
        This certificate will be included in the downloaded script and will enable you to download the selected data to your Linux or OS X system.
        </li>
      </ul>
      <br /> <b>Actions:</b>
      <ul>
        <li><a href="basket.jsp">Go to your basket</a></li>
        <li><a href="../help/contactexpert.jsp">Having
            problems?</a></li>
      </ul>
  
    <div id="showdownloadset">
      <b>The download script (for Linux/OSX) will be able to download the following files:</b><br />
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
  </div>
  <%} %>

  <jsp:include page="../footer.jsp" />
  <script>
	$("#openid").val(<%=userNamePrefill%>);
  </script>
</body>
</html>