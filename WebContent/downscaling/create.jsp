<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="impactservice.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="downscaling.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="model.Predictand"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <!-- ESGF search components -->
 	<jsp:include page="../includes-ext.jsp" />
    <script type="text/javascript" src="/impactportal/js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="/impactportal/js/jqueryextensions/jquery.collapsible.min.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/fileviewer.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/vkbeautify.js"></script>
    <link rel="stylesheet"         href="/impactportal/data/fileviewer/fileviewer.css" />
    <script type="text/javascript" src="/impactportal/data/catalogbrowser/catalogbrowser.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychoosers.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch.js"></script>
    <script type="text/javascript" src="/impactportal/js/components/basket/basket.js"></script>
    <script type="text/javascript" src="/impactportal/js/components/basket/basketwidget.js"></script>
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <link rel="stylesheet"         href="/impactportal/data/esgfsearch/esgfsearch.css" />
    <link rel="stylesheet"         href="/impactportal/data/esgfsearch/simplecomponent.css" />

	<!-- Downscaling view components -->
    <script type="text/javascript" src="js/functions.js"></script>
    <script type="text/javascript" src="js/loadFunctions.js"></script>
    <script type="text/javascript" src="js/events.js"></script>
	<script src="js/libraries/leaflet-0.7.3/leaflet.js"></script>
	<script src="js/libraries/spin/spin.min.js"></script>
	<link rel="stylesheet" href="js/libraries/leaflet-0.7.3/leaflet.css" />
	<link rel="stylesheet" href="css/style-downscaling.css" />
	<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
	
    <title>Downscaling</title>	

    <script type="text/javascript">
    
      /*
      * Callback is called when a user selects a file in his basket.
      */
      var callbackBasketSelect = function(files){
        alert("basket.postIdentifiersToBasket called, got nr files: "+files.length);
        console.log(files);
      };
      
      /*
      * Dialog popup showing the basket
      */
      var showBasketWidget= function(){
    	  console.log("showBasketWidget");
          basketWidget.show(function(selectedNodes) {
            callbackBasketSelect(selectedNodes);
    	    return true;//True means the basket dialog closes, otherwise the dialog is kept open.
    	  });
        };
        
        
      /*
      * YOu can override the post identifiers function, e.g. when a user selects a file in the search window, this callback is called.
      * If this function is not set, the file is going into the users basket under remote data instead.
      */
      basket.postIdentifiersToBasket = function(dataToAdd){
         /*
          * See reference implementation:
          * /impactportal/js/components/basket/basket.js
          */
          
         console.log(dataToAdd);//Catalogs are not yet expanded
          
         var callback = function(files){
           console.log(files);//Catalogs are now expanded to files
           alert("basket.postIdentifiersToBasket called, got nr files: "+files.length);
         };
         basket.expandNodes([{data:dataToAdd}],callback);
       };
     
      
      /* Configuration options, for downscaling*/
      var c4iconfigjs = {
        searchservice:"/impactportal/DownscalingSearch?",/*Downscaling endpoint */
        impactservice:"/impactportal/ImpactService?",
        adagucservice:"/impactportal/adagucserver?",
        adagucviewer:"/impactportal/adagucviewer/",
        howtologinlink:"/impactportal/help/howto.jsp?q=create_esgf_account",
        contactexpertlink:"/impactportal/help/contactexpert.jsp",
      }; 
    
      function showModalSearchInterface(query){
        var floating=true;/* YOu can choose either as floating dialog element or as embedded element */
        var el=jQuery('<div/>');
        renderSearchInterface({
          element:el,
          service:c4iconfigjs.searchservice,
          query:query,
          catalogbrowserservice:c4iconfigjs.impactservice,
          dialog:true
        });
      }
    </script>
    
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
      	var sortedKeys = ['variableType','variable','domain', 'dataset', 'zone', 'predictand','downscalingType','downscalingMethod', 'project', 'experiment', 'ensemble', 'model', 'startYear', 'endYear', ];
      	  

     		
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
		
		function ESGFsearch(){
		   var predictorVariables = ["ta","zg","hus"];
		   var project = ["CMIP5"];
		   var experimentFamily = ["Historical", "RCP"];
		   var timeFrequency = "day";
		   var ensemble = "r1i1p1";
		   var experiment = $("#selectExperiment").val();

		   var query = "#" + "variable=" + predictorVariables[0] + "&variable=" + predictorVariables[1] + "&variable=" + predictorVariables[2];
		   query += "&project=" + project;
		   query += "&experiment=" + experiment;
		   query += "&ensemble=" + ensemble + "&time_frequency=" + timeFrequency;
		   
		   return query;
		}
		
		function downscalingSubmit(){
			var zone = getValueFromHash("zone");
			var variable = getValueFromHash("variable");
			var predictand = getValueFromHash("predictand");
			var domain = getValueFromHash("domain");
			var dataset = getValueFromHash("dataset");
			var downscalingMethod = getValueFromHash("downscalingMethod");
			var modelType = "CLIMATE_CHANGE";
			var model = getValueFromHash("model");
			var project = getValueFromHash("project");
			var experiment = getValueFromHash("experiment");
			var ensemble = getValueFromHash("ensemble");
		    var startYear = getValueFromHash("startYear");
		    var endYear = getValueFromHash("endYear");
		  	var params ="?username="+loggedInUser+"&zone="+zone+"&predictand="+predictand+"&downscalingMethod="+downscalingMethod+"&model="+model+"&project="+project+"&experiment="+experiment+"_"+ensemble+"&sYear="+startYear+"&eYear="+endYear;
		  	var url="../DownscalingService/downscalings/downscale" + params;
		  	showLaunchDialog("<p>Are you sure you want to Downscale this Downscaling configuration?<\p>" + "<p>Variable: "+variable +"<\p>" + "<p>Domain: " + domain + "<\p>" + "<p>Dataset: "+ dataset +"<\p>" + 
		  	    "<p>Predictand: "+predictand+"<\p>"+"<p>Downscaling method: "+downscalingMethod+"<\p>" + "<p>Model: "+ model+"<\p>"+ "<p>Project: "+ project+"<\p>"+
		  	    "<p>Experiment: " + experiment + "<\p>" + "<p>Ensemble: " + ensemble + "<\p>", url);
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
			var modelType = "CLIMATE_CHANGE";
			var project = getValueFromHash("project");
			var model = getValueFromHash("model");
			var experiment = getValueFromHash("experiment");
			var ensemble = getValueFromHash("ensemble");
			$.ajax({
		    	url: '../DownscalingService/config',
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
		    	  'project' : project,
		    	  'model' : model,
		    	  'experiment' : experiment,
		    	  'ensemble' : ensemble
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
		  	$("#button-ESGFsearch").button({
	    	  icons: { primary: "ui-icon-search"}
	    	});
		  	$("#button-localSearch").button({
	    	  icons: { primary: "ui-icon-search"}
	    	});
		  	$("#button-basket").button({
	    	  icons: { primary: "ui-icon-cart"}
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
	    	<div id="searchcontainer"></div> 
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
			
			<h1>Predictand selection</h1>
								
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
 
 			<h1>Downscaling method validation</h1>
 			  						       
				<!-- Downscaling methods -->
		      	<div class="facetoverview collapsible" id="downscaling-method-header" style="height:40px;"> 
		        	<table width="100%" >
		        		<tr>
		        			<td class="collapsibletitle" style="width:200px;">
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
				<h4>You can use downloaded models hosted in the Downscaling Portal (local search) or search compatible models based on your selection (ESGF search).</h4>
				<!-- Models -->
		      	<div class="facetoverview collapsible" id="model-header" style="height:40px;"> 
					<table width="100%" >
						<tr>
			        		<td class="collapsibletitle" >
			        			Model
			        		</td>
			        		<td  style="padding:0px;">
								<table class="collapsibletable" width="100%">
				  					<tr>
					  					<td>
						  					<span>Project</span>
											<select id="selectProject">
											  <option value="CMIP5">CMIP5</option>	
											</select>
										</td> 		
				  						<td>
				  							<span>Experiment</span>
											<select id="selectExperiment">
										  		<option value="historical">historical</option>	
										  		<option value="rcp45">rcp45</option>	
										  		<option value="rcp85">rcp85</option>	
											</select>
				  						</td>  		
				  						<td>
				  							<span>Ensemble</span>
											<select id="selectEnsemble">
										  		<option value="r1i1p1">r1i1p1</option>	
											</select>
				  						</td>
				  						<td>
											<button id="button-localSearch" type="button">Local search</button>
				  						</td>
				  						<td>
											<button id="button-ESGFsearch" type="button" onclick="showModalSearchInterface(ESGFsearch());">ESGF search</button>
				  						</td>
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
				
				<div id="bottom-buttons">
					<button id="button-basket" type="button" onclick="showBasketWidget();">Basket</button>
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