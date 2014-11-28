<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="impactservice.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="downscaling.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="model.Predictand"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    
    <script type="text/javascript" src="../js/jqueryextensions/jquery.collapsible.min.js"></script>
    <script type="text/javascript" src="js/functions.js"></script>
	<script src="js/libraries/leaflet-0.7.3/leaflet.js"></script>
	<script src="js/libraries/spin/spin.min.js"></script>
	<link rel="stylesheet" href="js/libraries/leaflet-0.7.3/leaflet.css" />
	
	<style>
		.refreshinfo{ display:inline;float:left;overflow:hidden;width:740px;height:20px;}
		.link { cursor: pointer; }
		#predictand_info { float: left; padding: 30px;}
		
		.modal {
		    display:    none;
		    position:   fixed;
		    z-index:    1000;
		    top:        0;
		    left:       0;
		    height:     100%;
		    width:      100%;
		    background: rgba( 255, 255, 255, .8 ) 
		                url('../images/ajax-loader.gif') 
		                50% 50% 
		                no-repeat;
		}

		/* When the body has the loading class, we turn
		   the scrollbar off with overflow:hidden */
		body.loading {
		    overflow: hidden;   
		}
		
		/* Anytime the body has the loading class, our
		   modal element will be visible */
		body.loading .modal {
		    display: block;
		}
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
      	
      	<%
      		try{
      			request.setAttribute("loggedInUser", LoginManager.getUser(request).getInternalName());
      		}catch(Exception e){
      			request.setAttribute("loggedInUser", null);
      		}
      	%>
		var loggedInUser = '<%=request.getAttribute("loggedInUser")%>';
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
		      	var map = L.map('map').setView(center, 5);
	      		L.tileLayer('https://{s}.tiles.mapbox.com/v3/mannuk.jj9612k9/{z}/{x}/{y}.png', {
	        	attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://meteo.unican.es">UC</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>'
	    	}).addTo(map);
		    var polygon = L.polygon([a,b,c,d]).addTo(map);
		    function onEachFeature(feature, layer) {
		      // does this feature have a property named popupContent?
		      if (feature.properties && feature.properties.popupContent) {
		          layer.bindPopup(feature.properties.popupContent);
		      }
		  	}
		    $.get( "../DownscalingService/zones/1602/predictands/GSOD_TMAX/stations", function( data ) {
					 L.geoJson(data, {onEachFeature: onEachFeature}).addTo(map);
			});

		}
		
		function loadVariableTypes(){
		  var defaultVariableType = getValueFromHash("variableType");
		  if(defaultVariableType != null)
		  	$(".input-variable-type[data-variable-type='"+defaultVariableType+"']").attr('checked',true);
		  	$('#variable-types').html('').triggerHandler('contentChanged');
		}
		
		function loadVariables(){
		  	var variableType = getValueFromHash("variableType");
			if(variableType != null){
				$.get( "../DownscalingService/variables?variableType="+variableType, function( data ) {
					$.each(data.values, function(index, value){
						$('#variables').append("<td><input class='input-variable' + data-variable="+value.code+" type='radio' name='variable'/><abbr title='"+value.description+"\nUnits: "+value.units+"'><span >"+value.code+"</span></abbr></td>");
						 	var defaultVariableName = getValueFromHash("variableName");
							if(defaultVariableName != null && defaultVariableName == value.code){
				  				$('#variables').find("[data-variable='" + value.code + "']").prop('checked',true);
							}
					});
				});
				$('#variable-type-header').collapsible('open');
			}else{
				$('#variable-type-header').collapsible('close');
			}
			$('#variables').html('').triggerHandler('contentChanged');
		}

			
		function loadPredictands(){
			var URL = '../DownscalingService/users/'+loggedInUser+'/predictands';
			var variableName = getValueFromHash("variableName");
			var defaultPredictandName = getValueFromHash("predictandName");
			if(variableName != null){
				URL += '?variableName=' + variableName;
				$.get( URL, function( data ) {
					$('#predictands').append('List of matched predictands');
					$.each(data.values, function(index, value){
			  			$('#predictands').append("<td><input class='input-predictand' data-predictand='"+value.predictandName+"' data-id-zone='"+value.idZone+"' data-predictor='"+ value.predictorName+"' type='radio' name='predictand'/><abbr title='Click to see more info'><span class='link'>"+value.predictandName+"</span></abbr></td>");
			  			if(defaultPredictandName != null && defaultPredictandName == value.predictandName)
			  			  $('#predictands').find("[data-predictand='" + value.predictandName + "']").prop('checked',true);
					});
				});
				$('#predictand-type-header').collapsible('open');
		  	}else{
				$('#predictand-type-header').collapsible('close'); 
		  	}
			$('#predictands').html('').triggerHandler('contentChanged');
		}
		
		function loadPredictandDetails(zone,predictor,predictand){
			$.get( "../DownscalingService/users/"+loggedInUser+"/zones/"+zone+"/predictors/"+predictor+"/predictands/" + predictand, function( data ) {
		        $('#predictand-info').html('Name: ' + data.value.predictandName +'</br> Variable: ' + data.value.variable + ' </br> Variable type: ' + data.value.variableType + '</br> Dataset: ' + data.value.dataset);
		        loadMap(data.value.startLat, data.value.startLon, data.value.endLat, data.value.endLon);
		      });
		}
		
		function loadDownscalingMethods(){
		  $("body").addClass("loading");
			var zone = getValueFromHash("zone");
			var predictandName = getValueFromHash("predictandName");
			var defaultDownscalingMethod = getValueFromHash("downscalingMethod");
			if(zone != null && predictandName != null){
				$.get( "../DownscalingService/users/"+loggedInUser+"/zones/"+zone+"/predictands/" + predictandName + "/downscalingMethods", function( data ) {
			  		$.each(data.values, function(index, value){
			    		$('#downscaling-methods').append("<td><input class='input-downscaling-method' data-zone='"+zone+"' data-predictand='"+ predictandName+"' data-downscaling-method='"+value+"' type='radio' name='downscalingMethod'/>"+value+"</td>");
				  		if(defaultDownscalingMethod != null && defaultDownscalingMethod == value){
	  			  			$('#downscaling-methods').find("[data-downscaling-method='" + value + "']").prop('checked',true);
	  			  			$('#validation').html("<a href='../DownscalingService/validation?idZone="+zone+"&predictandName="+predictandName+"&downscalingMethod="+defaultDownscalingMethod+"' download='report'>Download validation report</a>");
	  		 				$('#downscalingmethod-header').collapsible('open');
				  		}else{
			 				$('#downscalingmethod-header').collapsible('close');
				  		}
					});
		      	});
			}else{
				$('#downscalingmethod-header').collapsible('close');
			}
			$('#validation').html('');
			$("body").removeClass("loading");
			$('#downscaling-methods').html('').triggerHandler('contentChanged');
		}
		
		function loadContent(){
			loadVariableTypes();
		}
	
		
		$(document).ready(function() {
			//collapsible management
			$('.collapsible').collapsible({});
			$("input:checkbox").prop('checked', false);
			
			$('#variable-types').bind('contentChanged', function(event, data) {
				loadVariables();
	    	});
			
			$('#variables').bind('contentChanged', function(event, data) {
				loadPredictands();
	    	});
			
			$('#predictands').bind('contentChanged', function(event, data) {
			  	loadDownscalingMethods()
    		});
			
			$('#downscaling-methods').bind('contentChanged', function(event, data) {

    		});
			
			$( "#select-saved-downscalings" ).on('change', function (e) {
			  var optionSelected = $("option:selected", this);
			  location.hash = this.value;
			  loadContent();
			});
			
			$(window).on("hashchange", function(e){
			    var oldHash = e.originalEvent.oldURL;
			    var newHash = e.originalEvent.newURL;
			});
			
			
			if(loggedInUser != null)
 				loadContent();
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
		
		$(document).ajaxStart(function () {
			$("body").addClass("loading");
			console.debug("loading class added");
		});
		$(document).ajaxStop(function () {
			$("body").removeClass("loading");
			console.debug("loading class removed");
		});
		
// 		$(document).on({
// 	    	ajaxStart: function() { 
// 	    	  $("body").addClass("loading");
// 	    	  console.log("loading added");
// 			},ajaxStop: function() { 
// 			  $("body").removeClass("loading");
// 			  console.log("loading removed");
// 			}    
// 		});
		
				
// 		//CHANGE EVENTS
		$(document).on('change', 'input:radio', function(event, ui){
			if($(this).attr('name') == 'variable-type'){
				//delete elements from here
				var variableType = $(this).attr('value');
				insertHashProperty('variableType', variableType, sortedKeys);
				if($(this).is(':checked')){
					removeHashProperty("variableName");
					removeHashProperty("zone");
					removeHashProperty("predictandName");
					removeHashProperty("downscalingMethod");
					loadVariables();
				}
			}else if($(this).attr('name') == 'variable'){
				var variable = encodeURIComponent($(this).attr('data-variable'));
				if($(this).is(':checked')){
					insertHashProperty('variableName', variable, sortedKeys);
					removeHashProperty("zone");
					removeHashProperty("predictandName");
					removeHashProperty("downscalingMethod");
					loadPredictands();
				}
			}else if($(this).attr('name') == 'predictand'){
				var idZone = encodeURIComponent($(this).attr('data-id-zone'));
				var predictor = encodeURIComponent($(this).attr('data-predictor'));
				var predictand = encodeURIComponent($(this).attr('data-predictand'));
				if($(this).is(':checked')){
					insertHashProperty('zone', idZone, sortedKeys);
					insertHashProperty('predictandName', predictand, sortedKeys);
			  		removeHashProperty("downscalingMethod");
					loadDownscalingMethods();
				}
 			}else if($(this).attr('name') == 'downscalingMethod'){
				var idZone = encodeURIComponent($(this).attr('data-zone'));
				var predictand = encodeURIComponent($(this).attr('data-predictand'));
				var downscalingMethod = encodeURIComponent($(this).attr('data-downscaling-method'));
				if($(this).is(':checked')){
					insertHashProperty('downscalingMethod', downscalingMethod, sortedKeys);
 					$('#validation').html("<a href='../DownscalingService/validation?idZone="+idZone+"&predictandName="+predictand+"&downscalingMethod="+downscalingMethod+"' download='report'>Download validation report</a>");
 					$('#downscalingmethod-header').collapsible('open');
				}
			}
			
		});
	
//location.hash.substr(1,location.hash.length-1);
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
	<jsp:include page="subscription.jsp" />

    <c:if test="${isSubscribed}">
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
				<select id="select-saved-downscalings" value="empty">
				    <option value="empty"> Select a saved config</option>
					<option value="variableType=TEMPERATURE&variableName=Tmax&zone=1602&predictandName=GSOD_TMAX&downscalingMethod=Analogues (default)"> GSOD_SPAIN_TMAX</option>
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
			  					for(String type : impactservice.DownscalingService.getVariableTypes()){
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
    </c:if>	
		 	
	  <!-- /Contents -->
		<jsp:include page="../footer.jsp" />
		<div class="modal"><!-- Place at bottom of page --></div>
  </body>
</html>