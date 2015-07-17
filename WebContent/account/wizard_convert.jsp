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
    <script type="text/javascript" src="../js/components/catalogbrowser/fileviewer.js"></script>
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>
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
    
    <script type="text/javascript" src="../adagucviewer/config.js"></script>
    
    
    <link rel="stylesheet" type="text/css" href="../adagucviewer/webmapjs/WMJSStyles.css" />
    
     <style>
      .ui-progressbar {
        position: relative;
      }
      .progress-label {
        position: absolute;
        left: 36%;
        top: 4px;
        font-weight: bold;
        text-shadow: 1px 1px 0 #fff;
      }
      
      .wpsinput{
       text-align:left;
        background:#e0ebeb;
         border-collapse: collapse;
          width:100%;
         margin-bottom:4px;
      }
      .wpsinput table{
      
      }
      .wpsinput td{
        padding:5px;
      }
      .wpsinput th{
        
      background-color: #428bca;
    color: white;
    padding:5px;
    font-weight:bold;
    font-size:14px;
        
      }
      .inputdiv{
        width:460px;
      
       
      }
      .mytable tr{
         vertical-align:top;
      }
    
    </style>
          

    <script type="text/javascript" src="./wizard_convert/wizard_convert.js"></script>
  
  </head>
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->

		<jsp:include page="loginmenu.jsp" />
 
		<div class="impactcontent">
		<div class="breadcrumb"><a href="login.jsp">Account</a> » <a href="processing.jsp">Processing</a> » Wizard convert and subset </div>
		
		<table class="headertable" ><tr><td><h1>Convert and subset</h1></td><td class="headerhelptd"><button class="headerhelpbutton">Help</button></td></tr></table>
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
              <tr><td><input class="resource" style="width:330px;" value="---"/></td><td><button onclick="showBasketWidget();"><code class="ui-icon codeshoppingcarticon" style="width:0px;"></code></button><button onclick="showFileInfo();"><span class="ui-icon ui-icon-info"></span></button></td></tr>
            </table>
            <table class="wpsinput">
              <tr><th><b>Variable</b></th><th></th></tr>
              <tr><td><div class="coverage"><select class="coveragecombo"><option>---</option></select></div></td></tr>
            </table>
            <table class="wpsinput">
              <tr><th><b>Projection</b></th><th></th></tr>
              <tr><td><div class="projectionselector"><select name="projectioncombo"><option>---</option></select></div></td></tr>
            </table>
            <table class="wpsinput">
              <tr><th><b>Bounding box</b></th><th></th><th></th></tr>
              <tr><td></td><td style="width:150px;">North:<input class="bboxnorth" style="width:100px;" type="text" value="---"/></td></tr>
              <tr><td>West:<input class="bboxwest" style="width:100px;" type="text" value="---"/></td><td></td><td>East:<input class="bboxeast" style="width:100px;" type="text" value="---"/></td></tr>
              <tr><td></td><td>South:<input class="bboxsouth" style="width:100px;" type="text" value="---"/></td></tr>
            </table>
            <table class="wpsinput">
              <tr><th><b>Resolution</b></th><th></th><th></th><th></th></tr>
              <tr><td>Horizontal:</td><td><input class="resolutionx" style="width:150px;" type="text" value="---"/></td><td>width:</td><td><div class="resolutionxinfo">---</div></td></tr>
              <tr><td>Vertical:</td><td><input class="resolutiony" style="width:150px;" type="text" value="---"/></td><td>height:</td><td><div class="resolutionyinfo">---</div></td></tr>
            </table>
            <table class="wpsinput">
              <tr><th><b>Dates</b></th><th></th></tr>
              <tr><td>Start date:</td><td><input class="startdate" style="width:150px;" type="text" value="2014-01-01T00:00:00Z"/></td></tr>
              <tr><td>Stop date:</td><td><input class="stopdate" style="width:150px;" type="text" value="2015-01-01T00:00:00Z"/></td></tr>
              <tr><td>Time resolution:</td><td><input class="timeresolution" style="width:150px;" type="text" value="P1D"/></td></tr>
            </table>
            <table class="wpsinput">
              <tr><th><b>Format</b></th><th></th></tr>
              <tr><td><select class="outputFormat"><option>netcdf</option><option>geotiff</option><option>aaigrid</option></select></td></tr>
            </table>
       
            
            <table class="wpsinput">
              <tr><th><b>Output file name</b></th></tr>
              <tr><td ><input class="outputFileName" style="width:350px;" type="text" value="out.nc"/></td></tr>
            </table>
          </div>
          <button id="startcalculation">Start processing</button>
          <div class="inputdiv">
            <div id="progressbar" style="display:none"><div class="progress-label"></div></div>
          
          

     <!--        
            <div id="results">
              <hr/>
              <b>Results:</b><br/>
              <div id="layerlist"></div><hr/>
              <b>Select date:</b><br/>
              <div id="timeheader">
                <div id="timeslider"></div>
                <h2>Time: <span id="currenttime"></span></h2>
                <span id="timeinfo"></span>
              </div>
            </div> -->
            
          </div>
        </td>
        <td>
          <div style="border:1px solid gray;">
            <div id="webmap1" style="width:480px;height:680px;"></div>
          </div>
        </td>
        </tr>
      </table>
    </div>			
			
			<% 
			
			
			
			
			
			//out.println(GenericCart.CartPrinters.showJobList(jobList,request));
		}
		%>

  		</div>
		
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>