<%@page import="tools.Debug"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor,tools.HTTPTools,java.net.URLEncoder"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

  <head>
    <jsp:include page="../includes-ext.jsp" />
    <script type="text/javascript" src="/impactportal/js/jqueryextensions/jquery.collapsible.min.js"></script>
    <link href="/impactportal/js/es-doc/esdoc-min.css" media="screen" rel="stylesheet" type="text/css" /> 
    <script src="/impactportal/js/es-doc/esdoc-min.js" type="text/javascript"></script>
      <script type="text/javascript" src="fileviewer/fileviewer.js"></script>
        <script type="text/javascript" src="/impactportal/js/components/catalogbrowser/fileviewer.js"></script>
    <script type="text/javascript" src="/impactportal/js/components/basket/basket.js"></script> 

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
    
	    var preventSubmit = function(event) {
	        if(event.keyCode == 13) {
	            event.preventDefault();
	            setVarFilter();
	            return false;
	        }
	    };
		var baseName = '<%=baseName%>';
		var variableFilter = undefined;
		var textFilter = undefined;
    	var setVarFilter = function(){
    		variableFilter = '';
    		$("form#varFilter :input[type=checkbox]").each(function(){
    			 var input = $(this);
    			 if(input.is(":checked")){
    				 if(variableFilter.length>0)variableFilter+="|";
    				 variableFilter+=input.attr('id');
    			 }
    			
    		});
    		if(variableFilter.length==0)variableFilter = undefined;
    		
    		textFilter = '';
    		textFilter = $("#textfilter").val();
    		if(textFilter.length<1)textFilter = undefined;
    		
    		loadCatalogDescription(variableFilter,textFilter);
    	};
    	
	   
	    
  		var loadCatalogDescription = function(variableFilter,textFilter){   
  		  if(errorMessage){
  		    $('#datasetinfo').html(URLDecode(errorMessage));
  		    return;
  		  }
 		  catalogURL = catalogURL.split("#")[0];
  		  $('#datasetinfo').html('<br/><table class=\"basket\"><tr><td><img src="/impactportal/images/ajax-loader.gif"/></td></tr></table>');
  		  var url='/impactportal/ImpactService?service=catalogbrowser&node='+encodeURI(catalogURL);
  		  //$('#catalogasjson').html('<a target="_blank" href="'+url+'&format=text/json">json</a>');
  		  $('#catalogasjson').html("<span class=\"shoppingbasketicon\" onclick=\"basket.postIdentifiersToBasket({id:'"+catalogURL+"',catalogURL:'"+catalogURL+"'})\"/>");
  		  
  		  var filters = "";
  		  if(textFilter!=undefined){filters+="&filter="+encodeURIComponent(textFilter);}
  		  
  		 // alert(variableFilter);
  		  
  	  	  if(variableFilter!=undefined){filters+="&variables="+encodeURIComponent(variableFilter);}
  		
		  $.get(url+'&format=text/html'+filters, function(data) {
			
		  	$('#datasetinfo').html(data);
		    $("#varFilter").keypress(preventSubmit);
		    $("#varFilter").keydown(preventSubmit);
		    $("#varFilter").keyup(preventSubmit);
		    
		  }); 
  		}
  		

  		var onViewDataset = function(datasetId){
  			var project = "CMIP5";
  		
  			
/*   			if(datasetId.split(".")[0] == 'cordex'){
  				project = "CORDEX";
  			}
 */  		
    		ESDOC.viewer.renderFromDatasetID({
    		    id : datasetId,
    		    project : project
    		});
    	};
    	
    	var ESDOCMetadataIsLoaded = false;
    	
  		$(document).ready(function(){
  		  	var varFilter = window.location.hash.substring(1).split("#");
  		  
  		  	loadCatalogDescription(varFilter);
  		  
  		   //collapsible management
            $('.collapsible').collapsible({
            	defaultOpen: 'datasetcontainer'
            });
            
  		  	ESDOC.viewer.setOptions({
	    		//dialogWidthInPixels:600,
	    		showNullFields:false,
	    		showFooter:true,
	    		uiContainer:'.es-doc-info',
	    		uiMode:'tabbed'//tabbed or linear
   			});
  	  		$('#buttonmetadataopen').click(function(){
  	  			var b = $('#buttonmetadataopen');
  	  			if(b.attr("class") == 'collapse-open'){
  	  				if(ESDOCMetadataIsLoaded !== true){
  	  					ESDOCMetadataIsLoaded = true;
  	  					//alert($('#buttonmetadataopen').attr("class"));
  	  					onViewDataset(baseName);
  	  				}
  	  			}
  	  		})
  			
  		  
  		});
	</script>
	<style type="text/css">
     .catalogbrowser{
      	margin: 10px 0;
    	width: 100%;
	  }
	  .catalogbrowser tr{
	  	border-bottom:1px dashed #DDD;
	  }
	  .es-doc-container{
	  	margin:4px;
	  	padding:4px;
		background-color: #DEEBFF;

	  }
	
	  #variableandtextfilter{
		margin:4px;
		padding:6px;
		background-color: #DEEBFF;
		
	  }
	  #datasetfilelist{
	  	
	  	margin:4px;
	  	padding:0px;
		
	  }
	  #datasetinfo{
	  	background-color: white;
	  }
	  
	  
  	</style>
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
			
			
   		 	<!--  Metadata container -->
			<!--  Overview  -->
   		 	<div class="collapsible" style="padding:0px;height:30px;" > 
		        <table width="100%" >
		       		<tr> 
				        <td class="collapsibletitle" style="width:300px;">
				        	ES-DOC Metadata 
				        </td>
				        <td style="padding:2px;"><span id="buttonmetadataopen" class="collapse-close"></span></td>
			        </tr>
		        </table>
	        </div>
   		 	
   		 	<!-- Details -->
	        <div class="collapsiblecontainer">
		        <div class="collapsiblecontent">
		        	<div class="es-doc-container">
		            <b>Metadata for dataset:</b><br/><i><%=baseName%></i><br/><br/>
		  					<div class="es-doc-info"></div>
		  		</div>
	        	</div>
        	</div>
        
        	<!--  /Metadata container -->
        	
		</div>
	</div></div>

	<jsp:include page="../footer.jsp" />
  </body>
</html>