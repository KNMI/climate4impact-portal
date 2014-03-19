 $.widget('ui.iconbutton', $.extend({}, $.ui.button.prototype, {
    	    _init: function() {
    	        $.ui.button.prototype._init.call(this);
    	        this.element.removeClass('ui-corner-all')
    	                    .addClass('ui-iconbutton')
    	                    .unbind('.button');
    	    }           
    	}));
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
};// /loadfacetlist 



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
};// /startbasicsearch