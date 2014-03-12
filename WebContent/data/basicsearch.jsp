<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    
  
	  <script type="text/javascript" src="../js/jqueryextensions/jquery.collapsible.min.js"></script>

      <script type="text/javascript">
      $.widget('ui.iconbutton', $.extend({}, $.ui.button.prototype, {
    	    _init: function() {
    	        $.ui.button.prototype._init.call(this);
    	        this.element.removeClass('ui-corner-all')
    	                    .addClass('ui-iconbutton')
    	                    .unbind('.button');
    	    }           
    	}));

      var searchSession=undefined;  
      <%
      impactservice.SessionManager.SearchSession searchSession=(impactservice.SessionManager.SearchSession) session.getAttribute("searchsession");
      if(searchSession!=null){
        out.println("  searchSession="+searchSession.getAsJSON());
      }
      %>
      var impactservice='<%=impactservice.Configuration.getImpactServiceLocation()%>service=search&';
    
      var postIdentifierToBasket = function(options){
	    	var doneFunction = function(json){
	    		if(json.error){customalert(json.error);}
	    		adjustNumberOfDataSetsDisplayedInMenuBar(json);
	    	}
	    	
	    	$.ajax({
	    		type: "POST",
	    		url: '<%=impactservice.Configuration.getImpactServiceLocation()%>service=basket&mode=add',
	    		data: options,
	    		success:doneFunction,
	    		dataType: 'json'
	    		});
	    }
      
      var getTimeFrame = function(){
    	  var timeFrame="";
    	  var input = $("form input:radio");
		  for(var j=0;j<input.length;j++){
			  if(input[j].checked){
				  if(input[j].name.indexOf('timebox_start')==0){
					  var v=input[j].value;
					  if(v.length>0)timeFrame+="tc_start="+v+"&";  
				  }
				  if(input[j].name.indexOf('timebox_stop')==0){
					  var v=input[j].value;
					  if(v.length>0)timeFrame+="tc_end="+v+"&";  
				  }
			  }
		  }
		  return timeFrame;
      }
      
    
      var loadFacetList = function(facetType,collapse){
    	  $('#refresh'+facetType+'info').html("Searching <img src=\"/impactportal/images/ajax-bar.gif\"/>");
    	 // $('#modelselection').html("");
    	  
		  var success = function(json,query){
			  if(json.error){
				  failure(json.error);
				  return;
			  }
			  html="Found "+(json.facets.length)+" "+facetType+"(s)";
			  if(query.length>0){
				  var queryt=query.replaceAll("&",", ");
				  html+=" for "+queryt;
			  }
			  $('#refresh'+facetType+'info').html(html);
			  
			  var facets=json.facets;
			  var html="<form><table class=\"modellist\">";
			  html+="<tr ><td class=\"modellistheader\">No.</td><td class=\"modellistheader\">Name</td><td class=\"modellistheader\">Description</td></tr>"
			  for(var j=0;j<facets.length;j++){
				  var rowType="even";
				  if(j%2==1)rowType="odd";
				  var modelname = facets[j][0];
				  html+='<tr class="'+rowType+'"><td >'+(j+1)+'</td><td><input type="checkbox" name="'+facetType+'_'+modelname+'">'+modelname+'</input></td><td>'+facets[j][1]+'</td></tr>';
			  }
			  html+="</table></form>";
			  $('#'+facetType+'selection').html(html);
			  
			
			  
			  if(collapse === true){
			  	$('#'+facetType+'header').collapsible('open');
		  	  }
		  };
		
		  
		  var failure = function(data){
			  $('#refresh'+facetType+'info').html('failed: '+data);
		  } 
		  
		  //Get all checkboxes and compose query
		  var query="";
		  var input = $("form input:checkbox");
		  for(var j=0;j<input.length;j++){ 
			if(input[j].checked){
				if(facetType!='project')       if(input[j].name.indexOf('project_')==0){query+="project="+input[j].name.substr(8)+"&";}
				if(facetType!='variable')      if(input[j].name.indexOf('variable_')==0){query+="variable="+input[j].name.substr(9)+"&";}
			  	if(facetType!='experiment')    if(input[j].name.indexOf('experiment_')==0){query+="experiment="+input[j].name.substr(11)+"&";}
			  	if(facetType!='time_frequency')if(input[j].name.indexOf('time_frequency_')==0){query+="time_frequency="+input[j].name.substr(15)+"&";}
			  	if(facetType!='model')         if(input[j].name.indexOf('model_')==0){query+="model="+input[j].name.substr(6)+"&";}
		  	}
		  }
		  query+=getTimeFrame();
		  
		  var url = impactservice+"mode=getfacet&facet="+facetType+"&type=dataset&query="+URLEncode(query);
		  makeJSONRequest(url,function(data){success(data,query);},failure);
      }; 
      

      
	  var startBasicSearch = function(){
		  $('#searchinfo').html("Searching <img src=\"/impactportal/images/ajax-bar.gif\" alt=\"loading...\"/>");
		  var query="";
		  var input = $("form input:checkbox");
		  for(var j=0;j<input.length;j++){
			if(input[j].checked){
				if(input[j].name.indexOf('project_')==0){query+="project="+input[j].name.substr(8)+"&";}
				if(input[j].name.indexOf('variable_')==0){query+="variable="+input[j].name.substr(9)+"&";}
			  	if(input[j].name.indexOf('experiment_')==0){query+="experiment="+input[j].name.substr(11)+"&";}
			  	if(input[j].name.indexOf('time_frequency_')==0){query+="time_frequency="+input[j].name.substr(15)+"&";}
			  	if(input[j].name.indexOf('model_')==0){query+="model="+input[j].name.substr(6)+"&";}
		  	}
		  }
		  query+=getTimeFrame();
		  //$('#info').html('Query2: '+query);
		  
		  var url = impactservice+"mode=search&limit=100&type=dataset&query="+URLEncode(query);
		  var success = function(json){
			  if(json.error){failure(json.error);return;}
			  var html = "Found "+json.totalCount+" datasets. <a target=\"_blank\" href=\""+json.query+"\">(see esgf query)</a>"; 
			  $('#searchinfo').html(html);
			
			  
			  var html="<table class=\"modellist\">";
			  html+="<tr ><td class=\"modellistheader\">No.</td><td class=\"modellistheader\">Name</td><td class=\"modellistheader\" >Size</td><td class=\"modellistheader\">catalog</td><td class=\"modellistheader\">OPENDAP</td><td class=\"modellistheader\">HTTP</td><td class=\"modellistheader\"><span class=\"shoppingbasketicon\"></span></td></tr>"
			  var topics =  json.topics;
			  for(var j=0;j<topics.length;j++){
				  var rowType="even";
				  if(j%2==1)rowType="odd";
				  var fileid = topics[j].instance_id;
				  var dataSize = topics[j].dataSize;
				  if(!dataSize)dataSize="-";
				  html+='<tr class="'+rowType+'"><td >'+(j+1)+'</td><td><input type="checkbox" checked="checked" name="file_'+fileid+'">'+fileid+'</input></td><td>'+dataSize+'</td>';
				  if(topics[j].catalogURL){
					  html+='<td><a target=\"_blank\" href="/impactportal/data/catalogbrowser.jsp?catalog='+URLEncode(topics[j].catalogURL)+'">browse</a></td>';
			 	  }else{
			 		  html+="<td>-</td>";
			 	  }
				  if(topics[j].OPENDAP){
						  html+='<td><a href="/impactportal/data/datasetviewer.jsp?dataset='+URLEncode(topics[j].OPENDAP)+'">view</a></td>';
				  }else{
					  html+="<td>-</td>";
				  }
			 	  if(topics[j].HTTPServer){
			  	  	html+='<td><a target="_blank" href="'+topics[j].HTTPServer+'">get</a></td>';
			 	  }else{
			 		  html+="<td>-</td>";
			 	  }
			 	  if(topics[j].OPENDAP||topics[j].HTTPServer){
			 	  	html+="<td><span class=\"shoppingbasketicon\" onclick=\"postIdentifierToBasket({id:'"+topics[j].instance_id+"',HTTPServer:'"+topics[j].HTTPServer+"',OPENDAP:'"+topics[j].OPENDAP+"',catalogURL:'null'});\"></span></td>";
			 	  }else{
			 	  	if(topics[j].catalogURL){
			 	  	 html+="<td><span class=\"shoppingbasketicon\" onclick=\"postIdentifierToBasket({id:'"+topics[j].instance_id+"',catalogURL:'"+topics[j].catalogURL+"'});\"></span></td>";
			 	  	}else{
			 			html+="<td>-</td>";
			 	  	}
			 	  }
				  html+='</tr>';
			  }
			  html+="</table>";
			  $('#searchresults').html(html);
			  $('#searchresultsHeader').collapsible('open');
			  
		  };
		  
		  var failure = function(data){$('#refreshmodelsinfo').html('failed: '+data);} 
		  makeJSONRequest(url,success,failure);
	  };
	  
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

    	  $('#refreshprojects').css({width:'0px',width:'0px',margin:'4px 6px 0px 2px',paddingLeft:'24px'});
    	  $('#refreshprojects').click(function(){loadFacetList('project',true);}); 
    	  $('#refreshprojects').iconbutton({text:false, icons:{primary:'refreshbutton24'}});

    	  $('#refreshexperiments').css({width:'0px',height:'24px',margin:'0px 6px 4px 2px',paddingLeft:'24px'});
    	  $('#refreshexperiments').click(function(){loadFacetList('experiment');}); 
    	  $('#refreshexperiments').iconbutton({text:false, icons:{primary:'refreshbutton24'}});

    	  
    	  
    	  $('#startsearch').css({width:'0px',margin:'8px 6px 0px 2px',paddingLeft:'24px'});
    	  $('#startsearch').click(function(){startBasicSearch();}); 
    	  $('#startsearch').iconbutton({text:false, icons:{primary:'refreshbutton24'},css:{width:'24px',display:'block'}});
      };
            
      
	
     
      
      $(document).ready(function() {
          //collapsible management
          $('.collapsible').collapsible({
          });
          
          //$('#timeframeheader').collapsible('open');
          initializeBasicSearch();
          loadFacetList('project');
          loadFacetList('model');
          //loadFacetList('variable');
          loadFacetList('experiment');
         // startBasicSearch();
        });
    </script>
  </head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
	
	<div style="line-height:0px;height:0px;margin:32px 10px;float: right;clear:both;overflow:none; border: none;"></div>

      <%try{out.print(DrupalEditor.showDrupalContent("?q=search_help",request,false,false));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%>
  			<div class="impactcontent">
  			<div id="info"></div>
			<h1>Search</h1>
        	
        	
        	
  			
        	<div class="collapsible" style="padding:0px;height:36px;"> 
	        <table width="100%" ><tr><td class="collapsibletitle" >
	        Variable 
	        </td><td  style="padding:0px;">
			<form>
  			<table class="collapsibletable" width="100%"><tr>
  			<td><input type="checkbox" name="variable_psl"/>Pressure</td>
  			<td><input type="checkbox" name="variable_tas"/>Temperature</td>
  			<td><input type="checkbox" name="variable_pr"/>Precipitation</td>
  			<td><input type="checkbox" name="variable_evspsbl"/>Evaporation</td>
  			<td><input type="checkbox" name="variable_sfcWind"/>Wind</td>
  			<td><input type="checkbox" name="variable_hur"/>Humidity</td>
  			</tr></table>
  			</form>
	        </td><td style="padding:6px;"><span class="collapse-close"></span></td></tr></table></div><div class="collapsiblecontainer"><div class="collapsiblecontent">
	        <table class="tablenoborder"><tr><td><div id="refreshvariable"></div></td><td ><div id="refreshvariableinfo">Click to load list from server</div></td></tr></table>
	       
        	<div id="variableselection"></div>
        	</div></div>
        	
        	
        	<div class="collapsible" style="padding:0px;height:36px;"> 
	        <table width="100%" ><tr><td class="collapsibletitle" >
	        Time frequency 
	        </td><td  style="padding:0px;">
			<form>
  			<table class="collapsibletable" width="100%"><tr>
			<td><input type="checkbox" name="time_frequency_3hr"/>3 hourly</td>
  			<td><input type="checkbox" name="time_frequency_6hr"/>6 hourly</td>
  			<td><input type="checkbox" name="time_frequency_day"/>daily</td>
  			<td><input type="checkbox" name="time_frequency_mon"/>monthly</td>
  			</tr></table>
  			</form>
	        </td><td style="padding:6px;"><span class="collapse-close"></span></td></tr></table></div><div class="collapsiblecontainer"><div class="collapsiblecontent">
	        <table class="tablenoborder"><tr><td><div id="refreshtime_frequency"></div></td><td ><div id="refreshtime_frequencyinfo">Click to load list from server</div></td></tr></table>
	       
        	<div id="time_frequencyselection"></div>
        	</div></div>
        	
  			
  		 	
  		 	
  		 	

	        <div class="collapsible" id="timeframeheader" style="padding:0px;height:36px;"><table width="100%" ><tr><td class="collapsibletitle" >
	        Time frame 
	        </td><td style="padding:6px;"><span class="collapse-close"></span></td></tr></table></div>
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
  			
  			
  		
  			
	        <div class="collapsible" style="padding:0px;height:36px;"> 
	        <table width="100%" ><tr><td class="collapsibletitle" >
	        Experiment 
	        </td><td  style="padding:0px;">
			<form>
  			<table class="collapsibletable" width="100%"><tr>
  			<td><input type="checkbox" name="experiment_piControl"/>piControl</td>
  			<td><input type="checkbox" name="experiment_amip"/>amip</td>
  			<td><input type="checkbox" name="experiment_rcp26"/>rcp26</td>
  			<td><input type="checkbox" name="experiment_rcp45"/>rcp45</td>
  			<td><input type="checkbox" name="experiment_rcp60"/>rcp60</td>
  			<td><input type="checkbox" name="experiment_rcp85"/>rcp85</td> 
  			</tr></table>
  			</form>
	        </td><td style="padding:6px;"><span class="collapse-close"></span></td></tr></table></div><div class="collapsiblecontainer"><div class="collapsiblecontent">
	        <table class="tablenoborder"><tr><td><div id="refreshexperiments"></div></td><td ><div id="refreshexperimentinfo">Click to load list from server</div></td></tr></table>
	       
        	<div id="experimentselection"></div>
        	</div></div>
        	
        	
        	
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
        	</div></div>
        	

	        <div class="collapsible" id="modelheader" style="padding:0px;height:36px;"> 
	        <table width="100%" ><tr><td class="collapsibletitle" >
	        Models 
	        </td><td  style="padding:0px;">
  			<table class="collapsibletable" width="100%"><tr>
  			<td><div  style="float:left;" id="refreshmodels"></div><div style="display:inline;" id="refreshmodelinfo"></div></td>
  			</tr></table>
	        </td><td style="padding:6px;"><span class="collapse-close"></span></td></tr></table></div>
	        <div class="collapsiblecontainer"><div class="collapsiblecontent">
        	<div id="modelselection"></div>
        	</div></div>
        	




			<h1>Search datasets</h1>
  			<div style="float:left;display:block;" ><div id="startsearch"></div></div>
	        <div class="collapsible" id="searchresultsHeader"><div style="float:left;display:inline;" id="searchinfo">No search started.</div><span class="collapse-close"></span></div> 
    		<div class="collapsiblecontainer">
        	<div class="collapsiblecontent">
        	<div id="searchresults">
        	</div></div></div>
        	
        	
        	
        	</div>
   
	 	
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>