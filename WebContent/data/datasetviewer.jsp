<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.*,tools.*,java.net.URLDecoder"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <%
	  	//Automatically redirect to the catalogbrowser if accidently a catalog was given
	  	  		try{
	  	  	String a=HTTPTools.getHTTPParam(request, "dataset");
	  	  	if(a==null)a=HTTPTools.getHTTPParam(request,"catalog") ;
	  	  	if(a!=null){
	  	  		if(a.length()>0){
	  	  			String catalogURL = URLDecoder.decode(a,"UTF-8");
	  	  			if(catalogURL.indexOf(".xml")>catalogURL.length()-7||catalogURL.indexOf("catalog.html")!=-1){
	  	  			    String redirectURL = "/impactportal/data/catalogbrowser.jsp?catalog="+a;
	  	  			    response.sendRedirect(redirectURL);
	  	  			}
	  	  		}
	  	  	}
	  	  		}catch(Exception e){
	  	  	MessagePrinters.emailFatalErrorException("XSS encountered at datasetviewer.jsp", e);
	  	  		}
	  	  		
	  	  		String dataset = null;
	  	    		try{
	  	    			dataset = HTTPTools.getHTTPParam(request, "dataset");
	  	    		}catch(Exception e){}
	  	  		String dapURL = "undefined";
	  	  		if(dataset!=null){
	  	  	dataset = URLDecoder.decode(dataset,"UTF-8");
	  	  	dataset=dataset.replace("thredds/fileServer","thredds/dodsC");
	  	  	dapURL = dataset.split("\\|")[0];
	  	  	dapURL = dapURL.split("\\#")[0];
	  	  	int inda = dapURL.indexOf(".nc.html");
	  	  	if(inda>0){
	  	  	  dapURL = dapURL.substring(0,inda+3);
	  	  	}
	  	  		}
	  	  		
	  	  		ImpactUser user = null;
	  	  		String openid="";
	  	  		try{
	  	  	user = LoginManager.getUser(request);
	  	  	openid = user.getOpenId();
	  	  		}catch(Exception e){
	  	  		
	  	  		}
	  %>
  
    <jsp:include page="../includes.jsp" />
       <script type="text/javascript">
    	var serviceURL='/impactportal/ImpactService?';
    	var dataset=undefined;;
    	var openid="<%=openid%>";
    	dataset='<%= dapURL %>';
    </script>
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <link rel="stylesheet" href="/impactportal/account/login.css" type="text/css" />
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script>

    <script type="text/javascript" src="fileviewer/fileviewer.js"></script>
    <script type="text/javascript" src="fileviewer/vkbeautify.js"></script>
    <link rel="stylesheet" href="fileviewer/fileviewer.css" />
    <link rel="stylesheet" href="esgfsearch/simplecomponent.css" />
    
    <script type="text/javascript">


	  $( document ).ready(function() {
	  var el = $("#fileviewercontainer");
      renderFileViewerInterface({element:el,
        service:c4iconfigjs.impactservice,
        adagucservice:c4iconfigjs.adagucservice,
        provenanceservice:c4iconfigjs.provenanceservice,
        adagucviewer:c4iconfigjs.adagucviewer,
        //query:"http://opendap.knmi.nl/knmi/thredds/dodsC/CLIPC/jrc/tier2/SPI3.nc",
        query:dataset,
        dialog:false
      });   
	  });

    </script>
    <style type="text/css">
     .x-form-field{
    	margin: 0 0 0 0;
    	font:normal 12px courier new;
  		}
  	</style>
  </head>
  <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />

	<div class="impactcontent">
		<div class="cmscontent"> 
			<h1>File information</h1>
			<div class="bodycontent">
			  <div id="fileviewercontainer" ></div>   
		<!--<table>
		 <tr><td>File: </td><td> <a target="_blank" href="<%= dataset%>"><%= dataset %></a></td></tr>
		<tr><td>Description: </td><td><a target="_blank" href="<%= dataset+".dds"%>">Show opendap descriptor</a></td></tr>
		</table>-->
			<div id="datasetinfo"/>
			<div id="container" style="border:1px solid lightgray;border-radius:4px;"></div>
			</div>
		</div>
	</div>
	<%="</div>" %>
	<jsp:include page="../footer.jsp" />
  </body>
</html>