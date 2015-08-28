<!-- Header -->
<%@page import="tools.HTTPTools" import="impactservice.ImpactUser" import="impactservice.LoginManager"  import="impactservice.DrupalEditor"%>
<%
	String Home="/impactportal/";
    out.println("<div id=\"bodycontainer\">");
    String loginStr = "<a href=\""+Home+"account/login.jsp\">Sign in</a>";
    ImpactUser user = null;
    try{
    	user = LoginManager.getUser(request);
    	loginStr = "<a href=\""+Home+"account/login.jsp\">Account</a>&nbsp;<a href=\""+Home+"account/basket.jsp\"><code class=\"codeshoppingcarticon\"></code></a>&nbsp;<a href=\""+Home+"account/processing.jsp\"><code class=\"codejobsicon\"></code></a>";
    }catch(Exception e){
    
    }
   
    
%>
 
  <div class="eimpactheader" style="z-index:2;"> 
    <div class="logo"><a href="/impactportal/"><img class="headerlogo" src="<%=Home%>images/IS-ENES2_logo_small.png" alt="IS-ENES2 Logo"/></a></div>
    <div style="float: right; margin-top: 0.5em;">
    <table><tr style="line-height:20px;">
     <td valign="middle"><a href="https://www.linkedin.com/groups/Climate4Impact-Webportal-8137458?home=&gid=8137458&trk=groups_members-h-logo" target="_blank">
     <img src="/impactportal/images/logo_in_nav_44x36.png" alt="linked in logo" height="20"/></a></td>
      <td valign="middle">| <a target="_blank" href="https://is.enes.org/">IS-ENES</a></td>
       <td valign="middle">| <a href="<%=Home%>help/contactexpert.jsp">Contact</a></td>
        <td valign="middle">| <%=loginStr%></td>
    </tr></table>
    </div>
    <h1 style="padding-top:36px;"><a href="<%=Home%>index.jsp">
    <!-- <span >ENES Portal Interface for <br/>the Climate Impact Communities</span> -->
    <i><span>Exploring climate model data</span></i> 
    </a></h1>
  </div>
  <div class="impactheader"><span></span> </div>
  <%="<div class=\"c1\"><div class=\"c2\"><div class=\"impactcontainer\">"%>
 	<jsp:include page="mainmenu.jsp" />
