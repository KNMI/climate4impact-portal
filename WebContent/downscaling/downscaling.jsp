<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="impactservice.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="model.Predictand"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    
    <script type="text/javascript" src="../js/jqueryextensions/jquery.collapsible.min.js"></script>
    <script type="text/javascript" src="/impactportal/adagucviewer/webmapjs/WMJSTools.js"></script>
    <script type="text/javascript" src="js/functions.js"></script>
	<script src="js/libraries/leaflet-0.7.3/leaflet.js"></script>
	<link rel="stylesheet" href="js/libraries/leaflet-0.7.3/leaflet.css" />
	
	<style>
		.refreshinfo{ display:inline;float:left;overflow:hidden;width:740px;height:20px;}
		.link { cursor: pointer; }
		#predictand_info { float: left; padding: 30px;}
	</style>
	
	<script type="text/javascript">
     
		var searchSession=undefined;  
		<%
			impactservice.SessionManager.SearchSession searchSession=(impactservice.SessionManager.SearchSession) session.getAttribute("searchsession");
			if(searchSession!=null){
				out.println("  searchSession="+searchSession.getAsJSON());      
      		}
      	%>
      	var impactservice='<%=impactservice.Configuration.getImpactServiceLocation()%>service=search&';

      	var sortedKeys = ['variableType','variableName','zone','predictandName','downscalingMethod'];
      	
		function showDialog(title){
	      	$('#dialog').dialog({
	      		dialogClass: 'custom-dialog', 
	      	 	height: 600,
	          	width: 850,
	          	modal: true,
	      		resizable: true,
	      		title: title,
	      		open: function(event, ui){
	      		  $('#map').css('height','500px');
	      		},
	      		close: function( event, ui ) {
	      			$('#dialog').remove();
	      			$('body').append('<div id="dialog"><div id="dialog-content"></div></div>');
	      			$('body').append('<div id="predictand-details"><div id="predictand-info"></div><div id="map"></div></div>');
	      			$('#map').css('height','');
	      		}
	      	});
      	}
      
		function loadMap(startLat, startLon, endLat, endLon){
		      	var a = [startLat, startLon];
		      	var b = [startLat, endLon];
		      	var c = [endLat, endLon];
		      	var d = [endLat, startLon];
		      	var center = [(b[0]+c[0])/2, (a[1]+b[1])/2]
		      	var map = L.map('map').setView(center, 2);
	      		L.tileLayer('https://{s}.tiles.mapbox.com/v3/mannuk.jj9612k9/{z}/{x}/{y}.png', {
	        	attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://meteo.unican.es">UC</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>'
	    	}).addTo(map);
		      	var polygon = L.polygon([a,b,c,d]).addTo(map);
		}
		
		function loadVariables(variableType,defaultVariable){
			$('#variables').html('').triggerHandler('contentChanged');
			if(variableType != ''){
				$.get( "../DownscalingService/variables?variableType="+variableType, function( data ) {
					$.each(data.values, function(index, value){
						$('#variables').append("<td><input class='input-variable' + data-variable="+value.code+" type='radio' name='variable'/><abbr title='"+value.description+"\nUnits: "+value.units+"'><span class='link'>"+value.code+"</span></abbr></td>");
		  			if(defaultVariable == value.code)
		  			  $('#variables').find("[data-variable='" + value.code + "']").prop('checked',true);
					});
				});
			}
		}

			
		function loadPredictands(variableName, defaultPredictand){
			var URL = '../DownscalingService/users/antonio/predictands';
			$('#predictands').html('').triggerHandler('contentChanged');
			if(variableName != ''){
				URL += '?variableName=' + variableName;
				$.get( URL, function( data ) {
					$('#predictands').append('List of matched predictands');
					$.each(data.values, function(index, value){
			  			$('#predictands').append("<td><input class='input-predictand' data-predictand='"+value.predictandName+"' data-id-zone='"+value.idZone+"' data-predictor='"+ value.predictorName+"' type='radio' name='predictand'/><abbr title='Click to see more info'><span class='link'>"+value.predictandName+"</span></abbr></td>");
			  			if(defaultPredictand == value.predictandName)
			  			  $('#predictands').find("[data-predictand='" + value.predictandName + "']").prop('checked',true);
					});
				});
		  	}
		}
		
		function loadPredictandDetails(zone,predictor,predictand){
			$.get( "../DownscalingService/users/antonio/zone/"+zone+"/predictors/"+predictor+"/predictands/" + predictand, function( data ) {
		        $('#predictand-info').html('Name: ' + data.value.predictandName +'</br> Variable: ' + data.value.variable + ' </br> Variable type: ' + data.value.variableType + '</br> Dataset: ' + data.value.dataset);
		        loadMap(data.value.startLat, data.value.startLon, data.value.endLat, data.value.endLon);
		      });
		}
		
		function loadDownscalingMethods(zone,predictand, defaultDownscalingMethod){
			$('#downscaling-methods').html('').triggerHandler('contentChanged');
			if(zone != '' && predictand != ''){
				$.get( "../DownscalingService/users/antonio/zone/"+zone+"/predictands/" + predictand + "/downscalingMethods", function( data ) {
			  		$.each(data.values, function(index, value){
			    		$('#downscaling-methods').append("<td><input class='input-downscaling-method' data-zone='"+zone+"' data-predictand='"+ predictand+"' data-downscaling-method='"+value+"' type='radio' name='downscalingMethod'/><abbr title='Click to see more info'><span class='link'>"+value+"</span></abbr></td>");
				  		if(defaultDownscalingMethod == value)
	  			  			$('#downscaling-methods').find("[data-downscaling-method='" + value + "']").prop('checked',true);
					});
		      	});
			}
		}
		
		function loadContent(){
			var map = getMapFromHash();
			var variableType = getValue(map, "variableType");
			var variableName = getValue(map, "variableName");
			var zone = getValue(map, "zone");
			var predictandName = getValue(map, "predictandName");
			var downscalingMethod = getValue(map, "downscalingMethod");
			$(".input-variable-type[data-variable-type='"+variableType+"']").attr('checked',true)
			loadVariables(getValue(map, "variableType"),variableName);
			loadPredictands(variableName,predictandName);
			loadDownscalingMethods(zone, predictandName, downscalingMethod);
			replaceHash(map);
		}
	
		
		$(document).ready(function() {
			//collapsible management
			$('.collapsible').collapsible({});
			$("input:checkbox").prop('checked', false);
			
			$('#variables').bind('contentChanged', function(event, data) {
				loadPredictands("","");
				removeHashProperty("variableName");
	    	});
			
			$('#predictands').bind('contentChanged', function(event, data) {
			  	loadDownscalingMethods("","","")
			  	removeHashProperty("zone");
			  	removeHashProperty("predictandName");
			  	removeHashProperty("downscalingMethod");
    		});
			
			$('#downscaling-methods').bind('contentChanged', function(event, data) {

    		});
			
			$(window).on("hashchange", function(e){
			    var oldHash = e.originalEvent.oldURL;
			    var newHash = e.originalEvent.newURL;
			});
 			//loadcontent();
		});
		
		$(document).on('click', '.link', function(event, ui){
			showDialog('Predictand');
			$('#predictand-details').appendTo('#dialog-content');
			var firedInput = $(event.target).parent().prev('input');
			var idZone = $(firedInput).attr('data-id-zone');
			var predictor = $(firedInput).attr('data-predictor');
			var predictand = $(firedInput).attr('data-predictand');
			loadPredictandDetails(idZone,predictor,predictand);
		});
		
				
		//CHANGE EVENTS
		$(document).on('change', 'input:radio', function(event, ui){
			if($(this).attr('name') == 'variable-type'){
				//delete elements from here
				$('#variable').html('');
				if($(this).is(':checked')){
					loadVariables($(this).val());
					insertHashProperty('variableType', $(this).val(), sortedKeys);
				}else{
					removeHashProperty('variableType');
				}
			}else if($(this).attr('name') == 'variable'){
				var variable = $(this).attr('data-variable');
				if($(this).is(':checked')){
					loadPredictands(variable);
					insertHashProperty('variableName', variable, sortedKeys);
				}
			}else if($(this).attr('name') == 'predictand'){
				var idZone = $(this).attr('data-id-zone');
				var predictor = $(this).attr('data-predictor');
				var predictand = $(this).attr('data-predictand');
				$('#downscaling-methods').html('');
				if($(this).is(':checked')){
					loadDownscalingMethods(idZone,predictand,"");
					insertHashProperty('zone', idZone, sortedKeys);
					insertHashProperty('predictandName', predictand, sortedKeys);
				}
 			}else if($(this).attr('name') == 'downscalingMethod'){
				var idZone = $(this).attr('data-zone');
				var predictand = $(this).attr('data-predictand');
				var downscalingMethod = $(this).attr('data-downscaling-method');
				if($(this).is(':checked')){
					insertHashProperty('downscalingMethod', $(this).attr('data-downscaling-method'), sortedKeys);
 					$('#validation').html("<a href='../DownscalingService/validation?idZone="+idZone+"&predictandName="+predictand+"&downscalingMethod="+downscalingMethod+"' download='report'>Download validation report</a>");
				}
			}
			
		});

		$(function() {
		  $("#slider-range").slider({
		    range : true,
		    min : 1850,
		    max: 2100,
		    values: [1970, 2050],
		    
		  });
		});
		    
    </script>
  </head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="downscalingmenu.jsp" />

    <div id="dialog">
   		<div id="dialog-content">
   		</div>
    </div>
    
    <div id="predictand-details">
    	<div id="predictand-info"></div>
    	<div id="map"></div>
    </div>
    
    <div id="test">
    </div>
  			
	<div class="impactcontent">
		<div id="info"></div>
		<h1>Load saved downscalings</h1>
			<select>
				<option value="1"> NorthAtlantic downscaling</option>
				<option value="2"> Namibia downscaling</option>
			</select>
		
		<h1>Configure your Downscaling</h1>
			
			<!-- Variable -->
	      	<div class="facetoverview collapsible" id="variable-type-header" style="height:35px;"> 
	        <table width="100%" >
	        <tr>
	        	<td class="collapsibletitle" >
	        		Variable
	        	</td>
	        <td  style="padding:0px;">
			<form>
	  			<table class="collapsibletable" width="100%">
	  			<tr>
	  				<div id="variable-types"> 
		  				<%
		  					for(String type : DownscalingService.getVariableTypes()){
		  							out.print("<td><input type='radio' name='variable-type' data-variable-type ='" + type + "' value = '" + type + "' class='input-variable-type'>"+type+"</input></td>");		
		  					}
		  				%>
		  			</div>
	  			</tr>
	  			
	  			</table>
	 			</form>
	        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table>
	       </div>
	       
	       <div class="collapsiblecontainer"><div class="collapsiblecontent">
			<table class="tablenoborder">
				<tr>
					<td><div id="refreshvariable"></div></td>
	       	</tr></table>
	      	<div id="variables"></div>
	      	</div></div>
	      	
	      	<!-- Predictand -->
	      	<div class="facetoverview collapsible" id="predictand-type-header" style="height:35px;"> 
	        <table width="100%" >
	        <tr>
	        	<td class="collapsibletitle" >
	        		Predictand
	        	</td>
	        <td  style="padding:0px;">
			<form>
	  			<table class="collapsibletable" width="100%">
	  			<tr>
					Select a variable to load matched predictands
	  			</tr>
	  			
	  			</table>
	 			</form>
	        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table>
	       </div>
	       
	       <div class="collapsiblecontainer"><div class="collapsiblecontent">
			<table class="tablenoborder">
				<tr>
					<td><div id="refresh-predictand"></div></td>
	       	</tr></table>
	      	<div id="predictands"></div>
	      	</div></div>
	      					
	       
			<!-- Downscaling methods -->
	      	<div class="facetoverview collapsible" id="downscalingmethod-header" style="height:55px;"> 
	        <table width="100%" ><tr>
	        <td class="collapsibletitle" >
	        	Downscaling methods
	        </td><td  style="padding:0px;">
			<form>
	  			<table class="collapsibletable" width="100%">
	  			<tr>
	  				<div id="downscaling-methods"> Select one predictand to load downscaling methods</div>
					<!-- Insert downscaling methods dynamically from predictand's choice -->
	  			</tr>
	  			
	  			</table>
	 			</form>
	        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table>
	       </div>
	       
	       <div class="collapsiblecontainer"><div class="collapsiblecontent">
	       <table class="tablenoborder"><tr><td><div id="refreshvariable"></div></td><td ><div id="refresh-downscaling-method-info"  class="refreshinfo"><div id="validation"></div></div></td></tr></table>
	      	
	      	</div></div>
	       

<!-- 			<!-- Period of interest -->
<!-- 	      	<div class="facetoverview collapsible" id="period-header" style="height:35px;">  -->
<!-- 	        <table width="100%" > -->
<!-- 	        <tr> -->
<!-- 	        	<td class="collapsibletitle" > -->
<!-- 	        		Period of interest -->
<!-- 	        	</td> -->
<!-- 	        <td  style="padding:0px;"> -->
<!-- 			<form> -->
<!-- 	  			<table class="collapsibletable" width="100%"> -->
<!-- 	  			<tr> -->
					
<!-- 	  			</tr> -->
	  			
<!-- 	  			</table> -->
<!-- 	 			</form> -->
<!-- 	        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table> -->
<!-- 	       </div> -->
	       
<!-- 	       <div class="collapsiblecontainer"><div class="collapsiblecontent"> -->
<!-- 			<table class="tablenoborder"> -->
<!-- 				<tr> -->
<!-- 					<td><div id="refreshvariable"></div></td> -->
<!-- 	       	</tr></table> -->
<!-- 	      	<div id="period-selection"></div> -->
<!-- 	      		<div id="slider-range"></div> -->
<!-- 	      	</div></div> -->
	      	
	</div>
	   
		 	
	  <!-- /Contents -->
		<jsp:include page="../footer.jsp" />
  </body>
</html>