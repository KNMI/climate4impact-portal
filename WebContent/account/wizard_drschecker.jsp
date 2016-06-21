<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.Configuration" import="impactservice.LoginManager" import="impactservice.ImpactUser"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <%
   String Home="/impactportal/"; 
   %>
    <jsp:include page="../includes-ext.jsp" />
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
    <script type="text/javascript" src="../js/components/basket/basketwidget.js"></script>
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>
    
    <script type="text/javascript" src="/impactportal/data/catalogbrowser/catalogbrowser.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/fileviewer.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychoosers.js"></script>
   	<script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch.js"></script>
   	<link rel="stylesheet"        href="/impactportal/data/esgfsearch/esgfsearch.css" />
	<link rel="stylesheet"        href="/impactportal/data/esgfsearch/simplecomponent.css" />
    <link rel="stylesheet"        href="/impactportal/data/fileviewer/fileviewer.css"></link>
    <link rel="stylesheet"        href="/impactportal/account/wizard_drschecker/wizard_drschecker.css"></link>

    <script type="text/javascript">
    var impactBase = '<%=Home%>';
    var impactService=impactBase+'ImpactService?';
    
    var WPSURL='<%=Configuration.getHomeURLHTTP()+"/WPS?"%>';
    
    
    </script>
    
    <!-- Proj4 -->
    <script type="text/javascript" src="../adagucviewer/proj4js/lib/proj4js.js"></script>

    <script src="../adagucviewer/jquery/jquery.mousewheel.js"></script>
    <script src="../adagucviewer/jquery/jquery-ui-timepicker-addon.js"></script>
    
       <!-- webmapjs -->
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSTools.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSISO8601.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSProj4Definitions.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSJqueryprototypes.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WebMapJS.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSLayer.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSBBOX.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSDimension.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSService.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSListener.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSTimer.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSTimeSlider.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSProcessing.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSCoverage.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSImage.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSImageStore.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSDivBuffer.js"></script>
    <script type="text/javascript" src="../adagucviewer/webmapjs/WMJSTimeSelector.js"></script>
    <script type="text/javascript" src="../adagucviewer/config.js"></script>
    
    
    <link rel="stylesheet" type="text/css" href="../adagucviewer/webmapjs/WMJSStyles.css" />

          

    <script type="text/javascript" src="./wizard_drschecker/wizard_drschecker.js"></script>
  
  </head>
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->

		<jsp:include page="loginmenu.jsp" />
 
		<div class="impactcontent">
		<div class="breadcrumb"><a href="login.jsp">Account</a> » <a href="processing.jsp">Processing</a> » CLIPC DRS Checker </div>
		
		<table class="headertable" ><tr><td><h1>CLIPC DRS Checker</h1></td><td class="headerhelptd"><button class="c4i_wizard_drschecker_helpbutton">Help</button></td></tr></table>
		<%
			ImpactUser user = null;
				try{
			user = LoginManager.getUser(request);
				}catch(Exception e){
			
				}
				
				 if (user==null){
		%>
			<p>You are not signed in, please <button onclick="generateLoginDialog(true)">sign in</button></p>
		
			<%
		}else{
			%>
			
<div id="first">
      <table class="mytable">
        <tr>
        <td>
          <div class="inputdiv">
          <table class="wpsinput">
              <tr><th><b>Resource</b></th><th></th></tr>
              <tr><td><input class="resource" style="width:800px;" value="---"/></td><td><button onclick="showBasketWidget();"><code class="ui-icon codeshoppingcarticon" style="width:0px;"></code></button><button onclick="showFileInfo();"><span class="ui-icon ui-icon-info"></span></button></td></tr>
            </table>
            <table class="wpsinput">
              <tr><th><b>Variable</b></th><th></th></tr>
              <tr><td><div class="coverage"><select class="coveragecombo"><option>---</option></select></div></td></tr>
            </table>
             <table class="wpsinput">
              <tr><th><button id="startcalculation">Check this file</button><div id="progressbar" style="display:none"><div class="progress-label"></div></div></th><th></th></tr>
            </table>
          </div>
          
        </td></tr>
        <tr><td><div class="c4i-wizard-drschecker-results"></div></td></tr>
        
        <tr>
        <td>
          <div style="border:0px solid gray;padding:0px;margin:0px;">
            <div id="webmap1" style="width:950px;height:350px;"></div>
          </div>
        </td>
        </tr>
      </table>
    </div>			
			
			<% 
		}
		%>

  		</div>
		
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>