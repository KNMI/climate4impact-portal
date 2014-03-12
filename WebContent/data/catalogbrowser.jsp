<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor,tools.HTTPTools,java.net.URLEncoder"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

  <head>
    <jsp:include page="../includes.jsp" />
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
	
    <script type="text/javascript">
        var catalogURL = '';
        var errorMessage = '';
        <% 
        	String catalogURL= "";
        	try{
        		catalogURL = HTTPTools.getHTTPParam(request,"catalog").replaceAll(".html", ".xml").trim();
        		out.println("catalogURL='"+catalogURL+"';");
        	}catch(Exception e){
        		out.println("errorMessage='"+URLEncoder.encode(e.getMessage(),"UTF-8")+"';");
        	}
        %>
    
	    var preventSubmit = function(event) {
	        if(event.keyCode == 13) {
	            event.preventDefault();
	            setVarFilter();
	            return false;
	        }
	    };

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
  	  	  if(variableFilter!=undefined){filters+="&variables="+encodeURIComponent(variableFilter);}
  		
		  $.get(url+'&format=text/html'+filters, function(data) {
			
		  	$('#datasetinfo').html(data);
		    $("#varFilter").keypress(preventSubmit);
		    $("#varFilter").keydown(preventSubmit);
		    $("#varFilter").keyup(preventSubmit);
		    
		  }); 
  		}
  		$(document).ready(function(){loadCatalogDescription();});
	</script>
	<style type="text/css">
     .catalogbrowser{
      	margin: 10px 0;
    	width: 100%;
	  }
	  .catalogbrowser tr{
	  	border-bottom:1px dashed #DDD;
	  }
  	</style>
  </head>
 <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />


	<div class="impactcontent">
		<div class="cmscontent"> 
			<h1>Catalog browser - browse THREDDS catalogs</h1>
			<div class="bodycontent">    
			<table><tr><td>Catalog: </td><td> <a target="_blank" href="<%= request.getParameter("catalog").replace(".xml", ".html") %>"><%= request.getParameter("catalog").replace(".xml", ".html") %></a></td><td>&nbsp;</td><td>- </td><td><div id="catalogasjson"></div></td></tr></table>
   		 
   		 	
   		 	
			<div id="datasetinfo">
			</div>
		</div>
	</div></div>

	<jsp:include page="../footer.jsp" />
  </body>
</html>