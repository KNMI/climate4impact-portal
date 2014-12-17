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
    <script type="text/javascript" src="js/loadFunctions.js"></script>
    <script type="text/javascript" src="js/events.js"></script>
	<script src="js/libraries/leaflet-0.7.3/leaflet.js"></script>
	<script src="js/libraries/spin/spin.min.js"></script>
	<link rel="stylesheet" href="js/libraries/leaflet-0.7.3/leaflet.css" />
	<link rel="stylesheet" href="css/custom-style.css" />
	
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
      	
      	<%
      		try{
      			request.setAttribute("loggedInUser", LoginManager.getUser(request).getInternalName());
      		}catch(Exception e){
      			request.setAttribute("loggedInUser", null);
      		}
      	%>
<%-- 		var loggedInUser = '<%=request.getAttribute("loggedInUser")%>'; --%>
		var loggedInUser = 'antonio';
      	var sortedKeys = ['variableType','variableName','zone','predictandName','dMethodType','downscalingMethod','datasetType','dataset','scenario'];
      	  

     		
		$(document).ready(function() {
			//collapsible management
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
		
		$(function() {
		  $("#slider-range").slider({
		    range : true,
		    min : 1851,
		    max: 2101,
		    step: 1,
		    values: [2001, 2011],
		    slide: function( event, ui ) {
	        	$("#date-range-start" ).val(ui.values[0]);
	        	$("#date-range-end").val(ui.values[1]);
		    }
		  });
		  $("#date-range-start" ).val($( "#slider-range" ).slider( "values", 0));
		  $("#date-range-end" ).val($( "#slider-range" ).slider( "values", 1));    
		});
		
		function downscalingSubmit(){
			var idZone = getValueFromHash("zone");
			var variableName = getValueFromHash("variableName");
			var predictandName = getValueFromHash("predictandName");
			var dMethodName = getValueFromHash("downscalingMethod");
			var datasetType = getValueFromHash("datasetType");
			var datasetName = getValueFromHash("dataset");
			var scenarioName = getValueFromHash("scenario");
		    var sYear = $('#date-range-start').val();
		    var eYear = $('#date-range-end').val();
		  	var cells = sYear+" "+eYear+" "+ datasetName+" 0";
		  	var params ="?username="+loggedInUser+"&idZone="+idZone+"&predictandName="+predictandName+"&dMethodName="+dMethodName+"&scenarioName="+scenarioName+"&cells="+cells;
		  	var url="../DownscalingService/downscale" + params;
		  	showOKDialog("<p>Are you sure you want to launch this configuration?<\p>" + "<p>Variable: "+variableName +"<\p>" + 
		  	    "<p>Predictand: "+predictandName+"<\p>"+"<p>Downscaling method: "+dMethodName+"<\p>" + "<p>Dataset: "+ datasetName+"<\p>"+
		  	    "<p>Scenario: " + scenarioName + "<\p>" + "<p>Period of interest: "+sYear+" - "+ eYear+"<\p>", url);
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
					<option value="variableType=TEMPERATURE&variableName=Tmax&zone=305&predictandName=NorthAtlantic1_TX&dMethodType=ALL&downscalingMethod=mean10&datasetType=CLIMATE&dataset=BCM2&scenario=A2"> North Atlantic</option>
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
  
		      	<!-- Predictand -->
				<div class="facetoverview collapsible" id="predictand-type-header" style="height:35px;"> 
		        	<table width="100%" >
			        	<tr>
			        		<td class="collapsibletitle" >
			        			Predictand
			        		</td>
				        	<td  style="padding:0px;">
				  				<table class="collapsibletable" width="100%">
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
		      			<div id="predictands"></div>
		      		</div>
				</div>
   						       
				<!-- Downscaling methods -->
		      	<div class="facetoverview collapsible" id="downscalingmethod-header" style="height:55px;"> 
		        	<table width="100%" >
		        		<tr>
		        			<td class="collapsibletitle" >
		        			Downscaling methods
		        			</td>
							<td  style="padding:0px;">
		  						<table class="collapsibletable" width="100%">
				  					<tr>
					  					<div id="downscaling-method-types"> 
						  					<%
						  						out.print("<input type='radio' name='downscaling-method-type' data-downscaling-method-type ='ALL' value = 'ALL' class='input-downscaling-method-type'>ALL</input>");
						  						for(String type : DownscalingService.getDownscalingMethodTypes()){
													out.print("<input type='radio' name='downscaling-method-type' data-downscaling-method-type='" + type + "' value = '" + type + "' class='input-downscaling-method-type'>"+type+"</input>");		
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

				<!-- Dataset -->
		      	<div class="facetoverview collapsible" id="dataset-header" style="height:35px;"> 
					<table width="100%" >
						<tr>
			        		<td class="collapsibletitle" >
			        			Dataset
			        		</td>
			        		<td  style="padding:0px;">
								<table class="collapsibletable" width="100%">
				  					<tr>
										<div id="dataset-types"> 
					  						<%
					  							for(String type : DownscalingService.getDatasetTypes()){
					  								if(type.equals("CLIMATE"))
					  									out.print("<td><input type='radio' name='dataset-type' data-dataset-type ='" + type + "' value = '" + type + "' class='input-variable-type'>"+type+"</input></td>");		
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
						<div id="datasets"></div>
		      		</div>
				</div>
	
				<!-- Scenarios -->
		      	<div class="facetoverview collapsible" id="scenario-header" style="height:35px;"> 
		        	<table width="100%" >
			        	<tr>
			        		<td class="collapsibletitle" >
			        			Scenario
			        		</td>
			        		<td  style="padding:0px;">
					  			<table class="collapsibletable" width="100%">
				  					<tr>
										<div id="period-selection">
											<div>
												<label for="date-range-start">Start year</label>
												<input type="text" id="date-range-start" class="input-year"/>
											</div>
				      						<div id="slider-range"></div>
				      						<div>
				      							<label>End year</label>
				      							<input type="text" id="date-range-end" class="input-year"></input>
				      						</div>
				      						<button id="button-load-scenarios" type="button">Reload</button>
										</div>
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
						<div id="scenarios"></div>
					</div>
				</div>
				<button id="submit" type="button">Downscale</button> 	
   		</c:if>	
		 	
	  <!-- /Contents -->
		<jsp:include page="../footer.jsp" />
		<div class="modal"><!-- Place at bottom of page --></div>
  </body>
</html>