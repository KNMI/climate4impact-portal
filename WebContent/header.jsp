<!-- Header -->
<%@page import="tools.HTTPTools" import="impactservice.ImpactUser" import="impactservice.LoginManager"  import="impactservice.DrupalEditor"  import="impactservice.Configuration"%>
<% String mode = Configuration.getPortalMode();
out.println("<!-- mode=["+mode+"] -->");
%>

<%
	String Home="/impactportal/";
    out.println("<div id=\"bodycontainer\">");
    String loginStr = "<a href=\""+Home+"account/login.jsp\">Sign in</a>";
    ImpactUser user = null;
    try{
    	user = LoginManager.getUser(request);
    	loginStr = "<a href=\""+Home+"account/login.jsp\">Account</a>&nbsp;| <a href=\""+Home+"account/basket.jsp\"><code class=\"codeshoppingcarticon\"></code></a>&nbsp;| <a href=\""+Home+"account/processing.jsp\"><code class=\"codejobsicon\"></code></a>&nbsp;";
    }catch(Exception e){
    
    }
   
    
%>

<% if (mode.equals("c4i")){ %>   
 
  <div class="eimpactheader" style="z-index:2;"> 
    <div class="logo"><a href="/impactportal/"><img class="headerlogo" src="<%=Home%>images/IS-ENES2_logo_small.png" alt="IS-ENES2 Logo"/></a></div>
    <div style="float: right; margin-top: 0.5em;">
    <table><tr style="line-height:20px;">
     <td valign="middle"><a href="https://www.linkedin.com/groups/Climate4Impact-Webportal-8137458?home=&gid=8137458&trk=groups_members-h-logo" target="_blank">
     <img src="/impactportal/images/logo_in_nav_44x36.png" alt="linked in logo" height="20"/></a></td>
      <td valign="middle">| <a target="_blank" href="https://is.enes.org/">IS-ENES</a></td>
       <td valign="middle">| <a href="<%=Home%>help/contactexpert.jsp">Contact</a></td>
        <td class="c4i-mainmenu-login-header" valign="middle">| <%=loginStr%></td>
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
 	
 <%}%>
 
 
<% if (mode.equals("c3s-magic")){ %>   
 
<div id="headertopwrapper" class="wrapper clearfix border-box">


    <div id="headertopcontainer" class="container">
        <div id="headertop">
            <div class="row">
                



  <div class="region region-header-top-left col-xs-12 col-sm-8 col-md-8 col-lg-8 ">

    

    <div class="row-fluid">
        <div id="block-block-16" class="block block-block col-xs-12 col-sm-12 col-md-12 col-lg-12 clearfix">

       
  <p class="text-left">
	<a href="http://www.copernicus.eu/"><img id="logo" alt="" src="/impactportal/general/theme/copernicus-logo.png"></a>
	<a href="http://climate.copernicus.eu/"><img id="logo2" alt="" src="/impactportal/general/theme/c3s-logo.png"></a>
</p>
<!--div class="text-feedback">
<strong class="beta__label">Beta</strong> <span>Your <a href="MailTo:feedback@copernicus-climate.eu?subject=Beta evaluation:">feedback</a> will help us to improve.</span>
</div-->
</div> <!-- /.block -->
    </div>
  </div>






  <div class="region region-header-top-right col-xs-12 col-sm-4 col-md-4 col-lg-4 ">

    

    <div class="row-fluid">
	
	<div id="block-block-36" class="block block-block col-xs-12 col-sm-8 col-md-8 col-lg-8 hidden-xs hidden-sm clearfix">

      
  <div class="text-right">
  <a href="https://twitter.com/CopernicusECMWF"><i class="fa fa-twitter-square fa-2x"></i></a> <a href="https://instagram.com/copernicusecmwf/"><i class="fa fa-instagram fa-2x"></i></a> <a href="http://www.slideshare.net/CopernicusECMWF"><i style="font-size: 1.8em" class="fa fa-slideshare"></i></a>
</div>

</div> <!-- /.block -->
	
        <div id="block-menu-menu-top-menu" class="block block-menu col-xs-12 col-sm-4 col-md-4 col-lg-4 hidden-xs hidden-sm clearfix">

      
  <ul class="nav navbar-nav n-size-sm pull-right"><li class="first last leaf"><a class="btn btn-info" href="http://climate.copernicus.eu/contact-us">Contact us</a></li>
</ul>
</div> <!-- /.block -->


    </div>
  </div>


            </div>
        </div>
    </div>
</div>
  <div class="impactheader"><span></span> </div>
  <%="<div class=\"c1\"><div class=\"c2\"><div class=\"impactcontainer\">"%>
 	<jsp:include page="mainmenu.jsp" />
 	
 <%}%>
 
