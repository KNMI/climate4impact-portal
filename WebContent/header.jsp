<!-- Header -->
<%@page import="tools.HTTPTools" import="impactservice.*"%>
<%
	String Home="/impactportal/";
    out.println("<div id=\"bodycontainer\">");
    String loginStr = "Login";
    ImpactUser user = null;
    try{
    	user = LoginManager.getUser(request);
    	loginStr = "Account";
    }catch(Exception e){
    
    }
    	
    
%>
 
  <div class="eimpactheader" style="z-index:2;"> 
    <div class="logo"><a href="https://is.enes.org/"><img class="headerlogo" src="<%=Home%>images/is-enes2-logo.png" alt="IS-ENES2 Logo"/></a></div>
    <div style="float: right; margin-top: 0.5em;">
      <a target="_blank" href="https://is.enes.org/">IS-ENES</a> | <a href="<%=Home%>help/contactexpert.jsp">Contact</a> | <a href="<%=Home%>account/login.jsp"><%=loginStr%></a>
    </div>
    <h1 style="padding-top:26px;"><a href="<%=Home%>index.jsp">
    <span >ENES Portal Interface for <br/>the Climate Impact Communities</span>
    </a></h1>
  </div>
  <div class="impactheader"><span></span> </div>
  <%="<div class=\"c1\"><div class=\"c2\"><div class=\"impactcontainer\">"%>
 	<jsp:include page="mainmenu.jsp" />
