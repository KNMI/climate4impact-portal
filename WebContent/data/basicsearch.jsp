<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    
    <script type="text/javascript" src="../js/jqueryextensions/jquery.collapsible.min.js"></script>
    <script type="text/javascript" src="/impactportal/adagucviewer/webmapjs/WMJSTools.js"></script>
	  <script type="text/javascript" src="basicsearchsettings/variables.js"></script>
	  <script type="text/javascript" src="basicsearch.js"></script>
	  
	  <style>
		.refreshinfo{
			
			display:inline;
			float:left;
			overflow:hidden;
			width:740px;
			height:20px;
			
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
    
      var initializeBasicSearch = function(){
    	  
       	  $('#refreshvariable').css({width:'0px',height:'18px',margin:'0px 6px 4px 2px',paddingLeft:'24px'});
       	  $('#refreshvariable').click(function(){loadFacetList('variable');}); 
       	  $('#refreshvariable').iconbutton({text:false, icons:{primary:'refreshbutton24'}});
       	  
       	  $('#refreshtime_frequency').css({width:'0px',height:'18x',margin:'0px 6px 4px 2px',paddingLeft:'24px'});
     	  $('#refreshtime_frequency').click(function(){loadFacetList('time_frequency');}); 
     	  $('#refreshtime_frequency').iconbutton({text:false, icons:{primary:'refreshbutton24'}});
     	  
     	  
    	  $('#refreshmodels').css({width:'0px',margin:'4px 6px 0px 2px',paddingLeft:'24px'});
    	  $('#refreshmodels').click(function(){loadFacetList('model',true);}); 
    	  $('#refreshmodels').iconbutton({text:false, icons:{primary:'refreshbutton24'}});

    	  
    	  $('#refreshdomain').css({width:'0px',margin:'4px 6px 0px 2px',paddingLeft:'24px'});
    	  $('#refreshdomain').click(function(){loadFacetList('domain',true);}); 
    	  $('#refreshdomain').iconbutton({text:false, icons:{primary:'refreshbutton24'}});

    	  $('#refreshprojects').css({width:'0px',width:'0px',margin:'4px 6px 0px 2px',paddingLeft:'24px'});
    	  $('#refreshprojects').click(function(){loadFacetList('project',true);}); 
    	  $('#refreshprojects').iconbutton({text:false, icons:{primary:'refreshbutton24'}});

    	  $('#refreshexperiments').css({width:'0px',height:'24px',margin:'0px 6px 4px 2px',paddingLeft:'24px'});
    	  $('#refreshexperiments').click(function(){loadFacetList('experiment');}); 
    	  $('#refreshexperiments').iconbutton({text:false, icons:{primary:'refreshbutton24'}});

    	  
    	  
    	  $('#startsearch').css({width:'0px',margin:'4px 6px 0px 2px',paddingLeft:'24px'}); 
    	  $('#startsearch').click(function(){startBasicSearch();}); 
    	  $('#startsearch').iconbutton({text:false, icons:{primary:'refreshbutton24'}});
    	  
    	  
    	
      };// /initializeBasicSearch
            
      
	
     
       
      $(document).ready(function() {
          //collapsible management
          $('.collapsible').collapsible({
          });
          

          initializeBasicSearch();
          $("input:checkbox").prop('checked', false);
          setCheckBoxes(true);
          loadFacetList('project');
          loadFacetList('model');
          if(window.location.hash.length>3){
          	startBasicSearch(); 
          }
    	  
        });
    </script>
  </head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
	
	<!--  <div style="line-height:0px;height:0px;margin:32px 10px;float: right;clear:both;overflow:none; border: none;"></div>

      <%try{out.print(DrupalEditor.showDrupalContent("?q=search_help",request,false,false));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%>
      -->
  			<div class="impactcontent">
  			<div id="info"></div>
			<h1>Search</h1>
        	
        	
        	
  		
  			
  			<!-- Project overview -->
  			<div class="facetoverview collapsible" id="projectheader" > 
		        <table width="100%" >
		       		<tr>
				        <td class="collapsibletitle" >
				        	Project 
				        </td>
				        <td style="padding:0px;">
							<form>
					  			<table class="collapsibletable" width="100%">
						  			<tr>
							  			<td><input type="checkbox" name="project_CMIP5"/><abbr title="Climate Model Intercomparison Project 5">CMIP5</abbr></td>
							  			<td><input type="checkbox" name="project_CORDEX"/><abbr title="Coordinated Regional Climate Downscaling Experiment">CORDEX</abbr></td>
						  			</tr>
					  			</table>
				  			</form>
				        </td>
				        <td style="padding:2px;"><span class="collapse-close"></span></td>
			        </tr>
		        </table>
	        </div>
	        
	        <!-- Project details -->
	        <div class="collapsiblecontainer">
		        <div class="collapsiblecontent">
		             <table width="100%" >
			             <tr>
		  					<td style="width:30px;"><div id="refreshprojects"></div></td>
		  					<td style="height:25px;vertical-align:middle;"><span id="refreshprojectinfo" class="refreshinfo"></span></td>
		  				</tr>
	  				</table>
		        	<div id="projectselection"></div>
	        	</div>
        	</div>
        	
        	<!-- Variable Selection -->
        	
        	<div class="facetoverview collapsible" id="variableheader" style="height:185px;"> 
		        <table width="100%" ><tr><td class="collapsibletitle" >
		        Variable 
		        </td><td  style="padding:0px;">
				<form>
		  			<table class="collapsibletable" width="100%">
		  			<tr>
		  			
		  			<td><input type="checkbox" name="variable_tas"/><abbr title="tas - Near-Surface Air Temperature">Temperature</abbr></td>
		  			<td><input type="checkbox" name="variable_pr"/><abbr title="pr - Total Precipitation">Precipitation</abbr></td>
		  			<td><input type="checkbox" name="variable_sfcWind"/><abbr title="sfcWind - Near-Surface Wind Speed">Windspeed</abbr></td>
		  			<td><input type="checkbox" name="variable_rsds"/><abbr title="rsds - Surface Downwelling Shortwave Radiation">Shortwave radiation down</abbr></td>
		  			<td><input type="checkbox" name="variable_huss"/><abbr title="huss - Near-Surface Specific Humidity">Surface specific humidity</abbr></td>

		  			</tr><tr>

		  			<td><input type="checkbox" name="variable_tasmin"/><abbr title="tasmin - Daily Minimum Near-Surface Air Temperature">Min temperature</abbr></td>
		  			<td><input type="checkbox" name="variable_prc"/><abbr title="prc - Convective Precipitation">Conv. precipitation</abbr></td>
		  			<td><input type="checkbox" name="variable_sfcWindmax"/><abbr title="sfcWindmax - Daily Maximum Near-Surface Wind Speed">Max windspeed</abbr></td>
		  			<td><input type="checkbox" name="variable_rsus"/><abbr title="rsus - Surface Upwelling Shortwave Radiation">Shortwave radiation up</abbr></td>
		  			<td><input type="checkbox" name="variable_hurs"/><abbr title="hurs - Near-Surface Relative Humidity">Surface relative humidity</abbr></td>
		  			
		  			</tr><tr>

		  			<td><input type="checkbox" name="variable_tasmax"/><abbr title="tasmax - Daily Maximum Near-Surface Air Temperature">Max temperature</abbr></td>
		  			<td><input type="checkbox" name="variable_prsn"/><abbr title="prsn - Snowfall flux">Snow</abbr></td>
		  			<td><input type="checkbox" name="variable_uas"/><abbr title="uas - Eastward Near-Surface Wind">Eastward wind</abbr></td>
		  			<td><input type="checkbox" name="variable_rlds"/><abbr title="rlds - Surface Downwelling Longwave Radiation">Longwave radiation down</abbr></td>
		  			<td><input type="checkbox" name="variable_hus"/><abbr title="hus - Specific Humidity">Specific humidity</abbr></td>
		  			
		  			</tr><tr>

		  			<td>&nbsp;</td>
		  			<td>&nbsp;</td>
		  			<td><input type="checkbox" name="variable_vas"/><abbr title="vas - Northward Near-Surface Wind Speed">Northward wind</abbr></td>
		  			<td><input type="checkbox" name="variable_rlus"/><abbr title="rlus - Surface Upwelling Longwave Radiation">Longwave radiation up</abbr></td>
		  			<td><input type="checkbox" name="variable_hur"/><abbr title="hur - Relative Humidity">Relative humidity</abbr></td>
		  			
		  			</tr><tr>
		  			<td>&nbsp;</td>
		  			<td>&nbsp;</td>
		  			<td>&nbsp;</td>
		  			<td><input type="checkbox" name="variable_rsdsdiff"/><abbr title="rsdsdiff - Surface Diffuse Downwelling Shortwave Radiation">Diffuse radiation</abbr></td>
		  			<td><input type="checkbox" name="variable_rhs"/><abbr title="rhs - Daily Near-Surface Specific Humidity">Surface relative humidity</abbr></td>
		  			
		  			</tr><tr>
		  			<td><input type="checkbox" name="variable_evspsbl"/><abbr title="evspsbl - Actual Evaporation">Evaporation</abbr></td>
		  			<td><input type="checkbox" name="variable_psl"/><abbr title="psl - Sea-Level Air Pressure">Surface pressure</abbr></td>
		  			<td>&nbsp;</td>
		  			<td><input type="checkbox" name="variable_clt"/><abbr title="clt - Total Cloud Fraction">Total cloud cover</abbr></td>
		  			<td><input type="checkbox" name="variable_rhsmax"/><abbr title="rhsmax - Daily Maximum Near-Surface Relative Humidity">Max relative humidity</abbr></td>
		  			
		  			</tr>
		  			<tr>
		  			<td><input type="checkbox" name="variable_evspsblpot"/><abbr title="evspsblpot - Potential Evaporation">Potential evaporation</abbr></td>
		  			<td><input type="checkbox" name="variable_ps"/><abbr title="ps - Surface Air Pressure">Pressure</abbr></td>
		  			<td>&nbsp;</td>
		  			<td>&nbsp;</td>
		  			<td><input type="checkbox" name="variable_rhsmin"/><abbr title="rhsmin - Daily Minimium Near-Surface Relative Humidity">Minimum relative humidity</abbr></td>
		  			</tr>
		  			</table>
	  			</form>
		        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table>
	        </div>
	        
	        <div class="collapsiblecontainer"><div class="collapsiblecontent">
	        <table class="tablenoborder"><tr><td><div id="refreshvariable"></div></td><td ><div id="refreshvariableinfo"  class="refreshinfo">Click to load the full list with all possible variables matching the current query.</div></td></tr></table>
        	<div id="variableselection"></div>
        	</div></div>
        	
        	
        	<div class="facetoverview collapsible"> 
	        <table width="100%" ><tr><td class="collapsibletitle" >
	        Frequency 
	        </td><td  style="padding:0px;">
			<form>
  			<table class="collapsibletable" width="100%"><tr>
			<td><input type="checkbox" name="time_frequency_3hr"/><abbr title="3 hourly sampling frequency">3 hourly</abbr></td>
  			<td><input type="checkbox" name="time_frequency_day"/><abbr title="Daily sampling frequency">daily</abbr></td>
  			<td><input type="checkbox" name="time_frequency_mon"/><abbr title="Monthly sampling frequency">monthly</abbr></td>
  			</tr></table>
  			</form>
	        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table></div><div class="collapsiblecontainer"><div class="collapsiblecontent">
	        <table class="tablenoborder"><tr><td><div id="refreshtime_frequency"></div></td><td ><div id="refreshtime_frequencyinfo"  class="refreshinfo">Click to load list from server</div></td></tr></table>
	       
        	<div id="time_frequencyselection"></div>
        	</div></div>
        	
  			
  		 	
  		 	
  		 	

	        <div class="facetoverview collapsible" id="timeframeheader"><table width="100%" ><tr><td class="collapsibletitle" >
	        Time frame 
	        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table></div>
    		<div class="collapsiblecontainer">
        	<div class="collapsiblecontent">
        	
        	
  		 	
  			<form>
  			From:
  			<table  class="tablefilled" width="100%">
  			<tr>
  			<td><input type="radio" name="timebox_start" value="" checked="checked"/>&lt;</td>
  			<td><input type="radio" name="timebox_start" value="1800-01-01T00:00:00Z"/>1800</td>
  			<td><input type="radio" name="timebox_start" value="1850-01-01T00:00:00Z"/>1850</td>
  			<td><input type="radio" name="timebox_start" value="1900-01-01T00:00:00Z"/>1900</td>
  			<td><input type="radio" name="timebox_start" value="1950-01-01T00:00:00Z"/>1950</td>
  			<td><input type="radio" name="timebox_start" value="2000-01-01T00:00:00Z"/>2000</td>
  			<td><input type="radio" name="timebox_start" value="2050-01-01T00:00:00Z"/>2050</td>
  			<td><input type="radio" name="timebox_start" value="2100-01-01T00:00:00Z"/>2100</td>
  			<td><input type="radio" name="timebox_start" value="2150-01-01T00:00:00Z"/>2150</td>
  			</tr></table>
  			Till:
  			<table class="tablefilled" width="100%"><tr>
  			<td><input type="radio" name="timebox_stop" value="1800-01-01T00:00:00Z"/>1800</td>
  			<td><input type="radio" name="timebox_stop" value="1850-01-01T00:00:00Z"/>1850</td>
  			<td><input type="radio" name="timebox_stop" value="1900-01-01T00:00:00Z"/>1900</td>
  			<td><input type="radio" name="timebox_stop" value="1950-01-01T00:00:00Z"/>1950</td>
  			<td><input type="radio" name="timebox_stop" value="2000-01-01T00:00:00Z"/>2000</td>
  			<td><input type="radio" name="timebox_stop" value="2050-01-01T00:00:00Z"/>2050</td>
  			<td><input type="radio" name="timebox_stop" value="2100-01-01T00:00:00Z"/>2100</td>
  			<td><input type="radio" name="timebox_stop" value="2150-01-01T00:00:00Z"/>2150</td>
  			<td><input type="radio" name="timebox_stop" value="" checked="checked"/>&gt;</td>
  			</tr>
  			</table>
  			
  			</form>  		 
  			</div></div>
  			
  			
  		
  			
	        <div class="facetoverview collapsible"> 
	        <table width="100%" ><tr><td class="collapsibletitle" >
	        Experiment 
	        </td><td  style="padding:0px;">
			<form>
  			<table class="collapsibletable" width="100%"><tr>
  			<td><input type="checkbox" name="experiment_historical"/><abbr title="Historical">Historical</abbr></td>
  			<td><input type="checkbox" name="experiment_rcp26"/><abbr title="Representative concentration pathway with radiative forcing of 2.6 W m-2">RCP26</abbr></td>
  			<td><input type="checkbox" name="experiment_rcp45"/><abbr title="Representative concentration pathway with radiative forcing of 4.5 W m-2">RCP45</abbr></td>
  			<td><input type="checkbox" name="experiment_rcp60"/><abbr title="Representative concentration pathway with radiative forcing of 6.0 W m-2">RCP60</abbr></td>
  			<td><input type="checkbox" name="experiment_rcp85"/><abbr title="Representative concentration pathway with radiative forcing of 8.5 W m-2">RCP85</abbr></td> 
  			<td><input type="checkbox" name="experiment_evaluation"/><abbr title="Evaluation">Evaluation</abbr></td>
  			<td><input type="checkbox" name="experiment_1pctCO2"/><abbr title="1pctCO2">1pctCO2</abbr></td>
  			
  			</tr></table>
  			</form>
	        </td><td style="padding:2px;"><span class="collapse-close"></span></td></tr></table></div>
	        
	        <div class="collapsiblecontainer"><div class="collapsiblecontent">
	        
	        <form>
	        
  			
  			<ul><li><a target="_blank" href="http://cmip-pcmdi.llnl.gov/cmip5/docs/Taylor_CMIP5_dec31.pdf">A Summary of the CMIP5 Experiment Design (PDF)</a></li></ul>
  			
  			</form>
  			
	        <table class="tablenoborder"><tr><td><div id="refreshexperiments"></div></td><td ><div id="refreshexperimentinfo"  class="refreshinfo">Click to load list from server</div></td></tr></table>
	       
        	<div id="experimentselection"></div>
        	</div></div>
        	
        	
        	<!--
	        <div class="collapsible" id="projectheader" style="padding:0px;height:36px;"> 
	        <table width="100%" ><tr><td class="collapsibletitle" >
	        Project 
	        </td><td  style="padding:0px;">
  			<table class="collapsibletable" width="100%"><tr>
  			<td><div  style="float:left;" id="refreshprojects"></div><div style="display:inline;" id="refreshprojectinfo"></div></td>
  			</tr></table>
	        </td><td style="padding:6px;"><span class="collapse-close"></span></td></tr></table></div>
	        <div class="collapsiblecontainer"><div class="collapsiblecontent">
        	<div id="projectselection"></div>
        	</div></div>  -->
        	
        	<div class="facetoverview collapsible" id="domainheader"> 
	        	<table width="100%" >
		        	<tr>
			        	<td class="collapsibletitle">Domain</td>
			        	<td style="padding:0px;">
			  				<table class="collapsibletable" width="100%">
			  					<tr>
			  						<td>
			  							<div  style="float:left;padding:0px;top:-2px;" id="refreshdomain" ></div>
			  							<div style="display:inline;" id="refreshdomaininfo"  class="refreshinfo">Search domain (CORDEX)</div>
			  						</td>
			  					</tr>
		  					</table>
				        </td>
			        	<td style="padding:2px;"><span class="collapse-close"></span></td>
		        	</tr>
		        </table>
	        </div>
	        
	        <div class="collapsiblecontainer"><div class="collapsiblecontent">
        	<div id="domainselection"></div>
        	</div></div>
        	

	        <div class="facetoverview collapsible" id="modelheader"> 
	        	<table width="100%" >
		        	<tr>
			        	<td class="collapsibletitle">Models</td>
			        	<td style="padding:0px;">
			  				<table class="collapsibletable" width="100%">
			  					<tr>
			  						<td>
			  							<div  style="float:left;padding:0px;top:-2px;" id="refreshmodels"></div>
			  							<div style="display:inline;" id="refreshmodelinfo"  class="refreshinfo">Search models</div>
			  						</td>
			  					</tr>
		  					</table>
				        </td>
			        	<td style="padding:2px;"><span class="collapse-close"></span></td>
		        	</tr>
		        </table>
	        </div>
	        
	        <div class="collapsiblecontainer"><div class="collapsiblecontent">
        	<div id="modelselection"></div>
        	</div></div>
        	




			<h1>Search datasets</h1>
  			
  			<table width="100%" ><tr><!-- <td style="width:200px;">
	  			<div class="facetoverview collapsible" >
	  			<div  class="collapsibletitle" style="margin:5px;">Facets</div>
	  			Maarten is working on this topic (20140318)
	  			</div>
	  			
  			</td> -->
  			<td>
	  			<!-- Search Panel -->
	  			<div class="facetoverview collapsible" id="searchresultsHeader" >
	  			<table width="100%" >
			        	<tr>
			        	
				        	<td style="padding:0px;">
			        			<table class="collapsibletable" width="100%">
			       					<tr>
					        			<td>
					        				<div style="float:left;padding:0px;top:-2px;" id="startsearch"></div>
					        				<div style="display:inline;" id="searchinfo">Start search</div>
					        			</td>
					        			<td style="padding:2px;"><span class="collapse-close"></span></td>
				        			</tr>
			        			</table>
		        			</td>
	        			</tr>
	       			</table>
		        </div>
	    		<div class="collapsiblecontainer">
	        		<div class="collapsiblecontent">
	        			<div id="searchresults"></div>
	       			</div>
	     		</div>
	        	<!-- /Search Panel -->
        	
        	</td></tr></table>
      	</div>
   
	 	
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>