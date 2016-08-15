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
    <script type="text/javascript" src="/impactportal/data/fileviewer/vkbeautify.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/fileviewer.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychoosers.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch.js"></script>
    <link rel="stylesheet"        href="/impactportal/data/esgfsearch/esgfsearch.css" />
  <link rel="stylesheet"        href="/impactportal/data/esgfsearch/simplecomponent.css" />
    <link rel="stylesheet"        href="/impactportal/data/fileviewer/fileviewer.css"></link>
    
    <script type="text/javascript">
    var impactBase = '<%=Home%>';
    var impactService=impactBase+'ImpactService?';
    
    var WPSURL='<%=Configuration.getHomeURLHTTPS()+"/WPS?"%>';
    
    
    </script>
   <script type="text/javascript" src="/impactportal/account/js/tokenapi.js"></script>
   <style>
    .c4i-tokenapi-loader{
      background-image:url("../images/ajax-loader.gif") !important;
      width:32px;
      height:32px;
    }
      .c4i-tokenapi-table  {
          border-collapse: collapse;
          width: 100%;
      
          border-spacing: 0;
      }
      
      .c4i-tokenapi-table th, .c4i-tokenapi-table td {
        border: 1px solid #ddd;
        padding: 3px 8px 3px 8px;
        text-align: left;
          text-align: left;
          padding: 8px;
      }
      .c4i-tokenapi-table tr:nth-child(even){background-color: #f5f5f5}
      
      /*.c4i-tokenapi-table tr:hover {background-color: #f0f0f0 !important}*/
      
      .c4i-tokenapi-table th{
        background: none repeat scroll 0 0 #428bca;
        color: white;
        font-size: 16px;
        font-weight: bold;
      }
      .c4i-tokenapi-filelinks{
      margin-top:10px;
      
      
      background-color:#EFEFFF;
      padding:10px;
      }
      .c4i-tokenapi-code{
        margin:5px 0 5px 0;
        padding:5px 0 5px 0;
	      font-family: Courier New,Courier,Lucida Sans Typewriter,Lucida Typewriter,monospace;
			  font-size: 13px;
			  font-style: normal;
			  font-variant: normal;
			  font-weight: 400;
			  line-height: 15px;
			  display:block;
			  text-align:left;
      }
      
   </style>
  </head>
  <body>
  <jsp:include page="../header.jsp" />

    <jsp:include page="../account/loginmenu.jsp" />
      <div class="impactcontent">
    <h1>Token API</h1>
    <div class="textstandardleft">
    The token API provides command line access to webservices and API's provided by climate4impact. Please have a look here: 
    <a target="_blank" href="https://dev.knmi.nl/projects/impactportal/wiki/API">https://dev.knmi.nl/projects/impactportal/wiki/API</a>.
    


    
    
    <h2>Generate a new token</h2>
    <button class="c4i-tokenapi-buttongenerate">Generate token</button>
    <h2>Select a file</h2>
    <p>This helps you to create the right link which you can use at your workstation.</p>
    <button class="c4i-tokenapi-buttondoforfile">Use token on basket file</button>
    <div class="c4i-tokenapi-filelinks"></div>
    <h2>Current tokens</h2>
    <div class="c4i-tokenapi-overview"></div>
    </div>
    <h1>API endpoints</h1>
    <div class="textstandardleft">
    <table class="c4i-tokenapi-table ">
    <tr><th>service</th><th>link</th></tr>
    <tr><td>WMS</td><td>/impactportal/adagucserver/&lt;accesstoken&gt;/?source=&lt;opendapurl&gt;&amp;service=WMS&amp;request=GetCapabilities</td></tr>
    <tr><td>WCS</td><td>/impactportal/adagucserver/&lt;accesstoken&gt;/?source=&lt;opendapurl&gt;&amp;service=WCS&amp;request=GetCapabilities</td></tr>
    <tr><td>WPS</td><td>/impactportal/WPS/&lt;accesstoken&gt;/?service=WMS&amp;request=GetCapabilities</td></tr>
    <tr><td>OpenDAP</td><td>/impactportal/DAP/&lt;accesstoken&gt;/&lt;userid&gt;/&lt;file&gt;</td></tr>
    <tr><td>HTTP</td><td>/impactportal/DAP/&lt;accesstoken&gt;/&lt;userid&gt;/&lt;file&gt;</td></tr>
    </table>
 
    </div>
    </div>
  <!-- /Contents -->
  <jsp:include page="../footer.jsp" />
  </body>
</html>