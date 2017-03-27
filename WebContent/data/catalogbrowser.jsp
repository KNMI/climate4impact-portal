<%@page import="tools.Debug"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor,tools.HTTPTools,java.net.URLEncoder"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

  <head>
    <jsp:include page="../includes.jsp" />
    <script type="text/javascript" src="/impactportal/js/jqueryextensions/jquery.collapsible.min.js"></script>
     
    
    <link href="/impactportal/data/esgfsearch/esgfsearch.css" media="screen" rel="stylesheet" type="text/css" />
    
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>    
    <script type="text/javascript" src="fileviewer/fileviewer.js"></script>
    <script type="text/javascript" src="fileviewer/vkbeautify.js"></script>
    <link rel="stylesheet" href="esgfsearch/simplecomponent.css" />
    <link rel="stylesheet" href="fileviewer/fileviewer.css" />
    <jsp:include page="../includes-adaguc-webmapjs.jsp" />
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <script type="text/javascript" src="/impactportal/js/components/basket/basket.js"></script> 
    <script type="text/javascript" src="catalogbrowser/catalogbrowser.js"></script>
  <script type="text/javascript">

        var catalogURL = '';
        var errorMessage = '';
        <% 
        	String catalogURL= "";
        	try{
        		catalogURL = HTTPTools.getHTTPParam(request,"catalog").replaceAll(".html", ".xml").trim();
        		if(catalogURL.length()==0){
        			catalogURL = "undefined";
        		}
        		out.println("catalogURL='"+catalogURL+"';");
        	}catch(Exception e){
        		out.println("errorMessage='"+URLEncoder.encode(e.getMessage(),"UTF-8")+"';");
        	}
        	String userURL = catalogURL.replace(".xml", ".html");
        	
        	String baseURL = userURL.split("#")[0];
        	String baseName = "";
        	try{
        	 	baseName = userURL.substring(userURL.lastIndexOf("/")+1);
        		baseName = baseName.substring(0,baseName.lastIndexOf("."));
        		System.out.println("basename: "+baseName);
        	}catch(Exception e){
        	}
        	
        	
        %>
    </script>
<script type="text/javascript">
$( document ).ready(function() {
  $('#catalogasjson').html("<span class=\"shoppingbasketicon\" onclick=\"basket.postIdentifiersToBasket({id:'"+catalogURL+"',catalogURL:'"+catalogURL+"'})\"/>");
renderCatalogBrowser({element:$("#datasetinfo"),url:catalogURL,service:c4iconfigjs.impactservice});
});
</script>
  </head>
 <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />


	<div class="impactcontent">
		<div class="cmscontent"> 
			<h1>Catalog browser</h1>
			
			<div class="bodycontent">    

			
   		 
			<table>
			<tr>
			<td>Catalog: </td>
			<td  style="max-width:860px;word-wrap: break-word;"> <a target="_blank" href="<%= catalogURL.replace(".xml", ".html") %>"><%= catalogURL.replace(".xml", ".html") %></a></td>
			<td><div id="catalogasjson"></div></td></tr></table>
   		 
        	<!-- Dataset container -->
			<div id="datasetcontainer" class="collapsible" style="padding:0px;height:30px;" > 
		        <table width="100%" >
		       		<tr> 
				        <td class="collapsibletitle" style="width:300px;">
				        	Catalog 
				        </td>
				        <td style="padding:2px;"><span class="collapse-open"></span></td>
			        </tr>
		        </table>
	        </div>
	        
	         <div class="collapsiblecontainer">
		        <div class="collapsiblecontent">
		 			<div id="datasetinfo"></div>
	        	</div>
        	</div>
			<!-- /Dataset container -->
			
		
		</div>
	</div></div>

	<jsp:include page="../footer.jsp" />
  </body>
</html>