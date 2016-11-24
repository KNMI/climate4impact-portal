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
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../js/jqueryextensions/jquery.collapsible.min.js"></script>
    <script type="text/javascript" src="js/functions.js"></script>
    <script type="text/javascript" src="js/loadFunctions.js"></script>
    <script type="text/javascript" src="js/events.js"></script>
	<script src="js/libraries/leaflet-0.7.3/leaflet.js"></script>
	<script src="js/libraries/spin/spin.min.js"></script>
	<link rel="stylesheet" href="js/libraries/leaflet-0.7.3/leaflet.css" />
	<link rel="stylesheet" href="css/style-downscaling.css" />
	<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
	
	<style>
		
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
      	
      	<%try{
      			request.setAttribute("loggedInUser", LoginManager.getUser(request).getUserId());
      		}catch(Exception e){
      			request.setAttribute("loggedInUser", null);
      		}%>
		var loggedInUser = '${loggedInUser}';
      	var sortedKeys = ['variableType','variable','domain', 'dataset', 'zone', 'predictand','downscalingType','downscalingMethod','modelProject', 'model', 'run', 'experiment', 'sYear', 'eYear', ];
      	  

     		
		$(document).ready(function() {
			$('.collapsible').collapsible({});
			$("input:checkbox").prop('checked', false);
		});
				
		$(document).ajaxStart(function () {
			$("body").addClass("loading");
			console.debug("loading class added");
		});
		$(document).ajaxStop(function () {
			$("body").removeClass("loading");
			console.debug("loading class removed");
		});
		
		function downscalingSubmit(){
			var zone = getValueFromHash("zone");
			var variable = getValueFromHash("variable");
			var predictand = getValueFromHash("predictand");
			var downscalingMethod = getValueFromHash("downscalingMethod");
			var modelType = "CLIMATE";
			var model = getValueFromHash("model");
			var experiment = getValueFromHash("experiment");
		    var sYear = $('#date-range-start').val();
		    var eYear = $('#date-range-end').val();
		  	var params ="?username="+loggedInUser+"&zone="+zone+"&predictand="+predictand+"&downscalingMethod="+downscalingMethod+"&model="+model+"&experiment="+experiment+"&sYear="+sYear+"&eYear="+eYear;
		  	var url="../DownscalingService/downscalings/downscale" + params;
		  	showOKDialog("<p>Are you sure you want to Downscale this Downscaling configuration?<\p>" + "<p>Variable: "+variable +"<\p>" + 
		  	    "<p>Predictand: "+predictand+"<\p>"+"<p>Downscaling method: "+downscalingMethod+"<\p>" + "<p>Model: "+ model+"<\p>"+
		  	    "<p>Experiment: " + experiment + "<\p>" + "<p>Period of interest: "+sYear+" - "+ eYear+"<\p>", url);
		}
		
		function postData(url){
			$.ajax({
		    	url: url,
		    	type: 'POST',
		    	contentType: 'application/x-www-form-urlencoded',
		    	success: function (response) {
		      		alert("Downscaling successfully launched");
		      		$("body").removeClass("loading");
		    	},error: function () {
		        	alert("error");
		    	}
			}); 
		}
		
		function saveConfig(configName){
		  	var domain = getValueFromHash("domain");
		  	var dataset = getValueFromHash("dataset");
		  	var zone = getValueFromHash("zone");
		  	var variableType = getValueFromHash("variableType");
			var variable = getValueFromHash("variable");
			var predictand = getValueFromHash("predictand");
			var downscalingType = getValueFromHash("downscalingType");
			var downscalingMethod = getValueFromHash("downscalingMethod");
			var modelType = "CLIMATE";
			var modelProject = getValueFromHash("modelProject");
			var model = getValueFromHash("model");
			var experiment = getValueFromHash("experiment");
		    var sYear = $('#date-range-start').val();
		    var eYear = $('#date-range-end').val();
			$.ajax({
		    	url: '../DownscalingService/downscalings',
		    	type: 'POST',
		    	data: {
		    	  'configName' : configName,
		    	  'username' : loggedInUser,
		    	  'variableType': variableType,
		    	  'variable': variable,
		    	  'domain' : domain,
		    	  'dataset' : dataset,
		    	  'zone' : zone, 
		    	  'predictand' : predictand,
		    	  'downscalingType' : downscalingType,
		    	  'downscalingMethod' : downscalingMethod,
		    	  'modelProject' : modelProject,
		    	  'model' : model,
		    	  'experiment' : experiment,
		    	  'sYear': sYear,
		    	  'eYear': eYear,
		    	},
		    	contentType: 'application/x-www-form-urlencoded',
		    	success: function (response) {
		      		alert("<p>Configuration successfully saved.<\p> Please, reload this page to select it.");
		      		$("body").removeClass("loading");
		    	},error: function () {
		        	alert("error");
		    	}
			}); 
		}
		
		$(function() {
		  	$("#button-load-experiments").button({
    	  		icons: { primary: "ui-icon-arrowrefresh-1-e"}
   			});
	    	$("#button-saveconfig").button({
	    	  icons: { primary: "ui-icon-folder-open"}
	    	});
	    	$("#button-downscale").button({
	    	  icons: { primary: "ui-icon-circle-check"}
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
	    
<!-- 	    <div id="element-details"> -->
<!-- 	    	<div id="element-info"></div> -->
<!-- 	    	<div id="map"></div> -->
<!-- 	    </div> -->
	    
	    <div id="test">
	    </div>
		
		<div class="impactcontent">
			<div id="info"></div>
			<h1>Load saved downscalings</h1>
			<select id="select-saved-downscalings" value="empty">
			    <option value="empty"> Select a saved config</option>
			    <%
			    	String username = LoginManager.getUser(request).getUserId();
			    		    	Map<String, String> configsMap = DownscalingService.getUserConfigurations(username);
			    		    	if(configsMap != null){
			    		     		for(String key : DownscalingService.getUserConfigurations(username).keySet()){
			    			     		out.print("<option value='"+configsMap.get(key)+"'> "+key+"</option>");
			    			     	}
			    		    	}
			    %>
			</select>
			
			<h1>Select your Predictand</h1>
								
				<!-- Variable -->
		      	<div class="facetoverview collapsible" id="variable-type-header" style="height:35px;"> 
		        	<table width="100%" >
				        <tr>
				        	<td class="collapsibletitle" >
				        		Variable
				        	</td>
				        	<td  style="padding:0px;">
								<table class="collapsibletable" width="100%">
				  					<tr>
				  						<div id="variable-types"> 
					  						<%
					  							for(String type : DownscalingService.getVariableTypes()){
					  									out.print("<td><input type='radio' name='variableType' data-variable-type ='" + type + "' value = '" + type + "' class='input-variable-type'>"+type+"</input></td>");		
					  							}
					  						%>
					  					</div>
									</tr>
		  						</table>
							</td>
							<td style="padding:2px;">
								<span class="collapse-close"/>
							</td>
						</tr>
					</table>
				</div>
				<div class="collapsiblecontainer">
					<div class="collapsiblecontent">
		      			<div id="variables"></div>
		      		</div>
	      		</div>
	      		
	      		<!-- Domain -->
		      	<div class="facetoverview collapsible" id="domain-type-header" style="height:35px;"> 
		        	<table width="100%" >
				        <tr>
				        	<td class="collapsibletitle" >
				        		Domain
				        	</td>
				        	<td  style="padding:0px;">
								<table class="collapsibletable" width="100%">
				  					<tr>
									</tr>
		  						</table>
							</td>
							<td style="padding:2px;">
								<span class="collapse-close"/>
							</td>
						</tr>
					</table>
				</div>
				<div class="collapsiblecontainer">
					<div class="collapsiblecontent">
		      			<div id="domains"></div>
		      		</div>
	      		</div>
	      		
	      		<!-- Dataset -->
		      	<div class="facetoverview collapsible" id="dataset-type-header" style="height:35px;"> 
		        	<table width="100%" >
				        <tr>
				        	<td class="collapsibletitle" >
				        		Dataset
				        	</td>
				        	<td  style="padding:0px;">
								<table class="collapsibletable" width="100%">
				  					<tr>
									</tr>
		  						</table>
							</td>
							<td style="padding:2px;">
								<span class="collapse-close"/>
							</td>
						</tr>
					</table>
				</div>
				<div class="collapsiblecontainer">
					<div class="collapsiblecontent">
		      			<div id="datasets"></div>
		      		</div>
	      		</div>
	      		
  
		      	<!-- Predictand-->
		       
				<div class="collapsiblecontainer" style="display : block;  border-top: 1px solid #bbb; margin-top: 30px;">
					<div class="collapsiblecontent">
		      			<div id="predictands"> No predictand selected</div>
		      		</div>
				</div>
 
 			<h1>Validate your Predictand</h1>
 			  						       
				<!-- Downscaling methods -->
		      	<div class="facetoverview collapsible" id="downscaling-method-header" style="height:55px;"> 
		        	<table width="100%" >
		        		<tr>
		        			<td class="collapsibletitle" >
		        			Downscaling methods
		        			</td>
							<td  style="padding:0px;">
		  						<table class="collapsibletable" width="100%">
				  					<tr>
					  					<div id="downscaling-types"> 
						  					<%
						  						out.print("<input type='radio' name='downscalingType' data-downscaling-type ='ALL' value = 'ALL' class='input-downscaling-type'>ALL</input>");
						  						for(String type : DownscalingService.getDownscalingMethodTypes()){
													out.print("<input type='radio' name='downscalingType' data-downscaling-type='" + type + "' value = '" + type + "' class='input-downscaling-type'>"+type+"</input>");		
					  							}
						  					%>
						  				</div>
				  					</tr>
		  						</table>
		        			</td>
		        			<td style="padding:2px;">
		        				<span class="collapse-close"/>
	        				</td>
        				</tr>
       				</table>
				</div>
				<div class="collapsiblecontainer">
					<div class="collapsiblecontent">
		       			<div id="downscaling-methods"></div>
					</div>
				</div>
				
				<div id="bottom-buttons">
					<div id="validation"></div>
				</div>

				<h1>Run your Downscaling</h1>
				
				<!-- Models -->
		      	<div class="facetoverview collapsible" id="model-header" style="height:35px;"> 
					<table width="100%" >
						<tr>
			        		<td class="collapsibletitle" >
			        			Model
			        		</td>
			        		<td  style="padding:0px;">
								<table class="collapsibletable" width="100%">
				  					<tr>
				  						<input class='input-model-project' type='radio' name='modelProject' data-model-project='CMIP5' value = 'CMIP5'>CMIP5</input>		
				  					</tr>  			
			  					</table>
			        		</td>
			        		<td style="padding:2px;">
			        			<span class="collapse-close"/>
			        		</td>
						</tr>
					</table>
				</div>
		       
				<div class="collapsiblecontainer">
					<div class="collapsiblecontent">
						<div id="models"></div>
		      		</div>
				</div>
	
				<!-- Experiments -->
		      	<div class="facetoverview collapsible" id="experiment-header" style="height:35px;"> 
		        	<table width="100%" >
			        	<tr>
			        		<td class="collapsibletitle" >
			        			Experiment/RCP
			        		</td>
			        		<td  style="padding:0px;">
					  			<table class="collapsibletable" width="100%">
				  					<tr>
						  				<input type='radio' name='experimentRun' data-experiment-run='1' value = 'Run 1'>Run 1</input>		
									</tr>
					  			</table>
							</td>
							<td style="padding:2px;">
								<span class="collapse-close"></span>
							</td>
						</tr>
					</table>
				</div>
		       
				<div class="collapsiblecontainer">
					<div class="collapsiblecontent">
						<div id="experiments"></div>
					</div>
				</div>
				
				<!-- Downscaling methods -->
		      	<div class="facetoverview collapsible" id="downscaling-period-header" style="height:35px;"> 
		        	<table width="100%" >
		        		<tr>
		        			<td class="collapsibletitle" >
		        			Period
		        			</td>
							<td  style="padding:0px;">
		  						<table class="collapsibletable" width="100%">
				  					<tr>
				  					</tr>
		  						</table>
		        			</td>
		        			<td style="padding:2px;">
		        				<span class="collapse-close"/>
	        				</td>
        				</tr>
       				</table>
				</div>
				<div class="collapsiblecontainer">
					<div class="collapsiblecontent">
	  					<div id="period-selection" style="height:30px;">
							
						</div>
					</div>
				</div>
				
				
				
				<div id="bottom-buttons">
					<button id="button-saveconfig" type="button">Save</button>
					<button id="button-downscale" type="button">Downscale</button>
				</div>
			</div>
   		</c:if>	
		 	
	  <!-- /Contents -->
		<jsp:include page="../footer.jsp" />
		<div class="modal"><!-- Place at bottom of page --></div>
  </body>
</html>