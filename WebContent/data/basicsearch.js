var searchfacets=['project','variable','experiment','time_frequency', 'model','domain'];

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
		url: '/impactportal/ImpactService?service=basket&mode=add',
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

var setCheckBoxes = function(addNonExistingCheckBoxes){
	
	
	$("input:radio,input:checkbox").off('click');
	$("input:radio,input:checkbox").on('click',
	function(t){
		var hashTag = "";
		$("input:checkbox[name='"+this.name+"']").prop( "checked",this.checked );
		var input = $("form input:checkbox");
		var addedNames = [];
		for(var j=0;j<input.length;j++){
			if(input[j].checked){
				var name = input[j].name+"=1";
				if($.inArray(name,addedNames)==-1){
					addedNames.push(name);
					if(hashTag.length>0)hashTag+="&";
					hashTag+=name;
				}
			}
		}
		var input = $("form input:radio");
		var addedNames = [];
		for(var j=0;j<input.length;j++){
			
			if(input[j].checked){
				var value = input[j].value;
				if(value.length>0){
					//console.debug(value);
					var name = input[j].name+"="+value;
					if($.inArray(name,addedNames)==-1){
						addedNames.push(name);
						if(hashTag.length>0)hashTag+="&";
						hashTag+=name;
					}
				}
			}
		}
		window.location.hash = hashTag;
	});

	//Set the checkboxes on true, which are defined in the hashtag
	var URLVarsFromHashTag = getUrlVarsFromHashTag();

	var theseCheckBoxesAreNotThere = [];

	for(var key in URLVarsFromHashTag){
		var inp = key;
		if(inp!='indexOf'){
			//Checkboxes
			var checkbox = $("input:checkbox[name='"+inp+"']");
			if(checkbox.length>0){
				if(URLVarsFromHashTag[key]==1){
					checkbox.prop( "checked", true );
				}
			}else{
				var items = inp.split("_");
				if(!theseCheckBoxesAreNotThere[items[0]]){
					theseCheckBoxesAreNotThere[items[0]] = [];
				}
				var lkey = items[0];
				var json = [items[1],items[1]];
				alert(lkey + inp);
				theseCheckBoxesAreNotThere[lkey].push(json);
			}
			//Radio buttons
			var radios = $("input:radio[name='"+inp+"']");
			if(radios.length>0){
				var value = URLVarsFromHashTag[key];
		        radios.filter('[value="'+value+'"]').prop('checked', true);
			}
		}
	}

	if(addNonExistingCheckBoxes === true){
		//Add the checkboxes which are in the hash tag but not in the HTML Form
		for(key in theseCheckBoxesAreNotThere){

			var id = '#'+key+'header';

			var items = theseCheckBoxesAreNotThere[key];

			createCheckboxesForFacet(key,items);
			$(id).collapsible('open');



		}
		for(var key in URLVarsFromHashTag){
			var inp = key;
			var checkbox = $("input[name='"+inp+"']");
			if(checkbox.length>0){
				if(URLVarsFromHashTag[key]==1){
					checkbox.prop( "checked", true );
				}
			}
		}
	}
	


};

var createCheckboxesForFacet = function(facetType,facets){
	var html="<form><table class=\"modellist\">";
	html+="<tr ><td class=\"modellistheader\">No.</td><td class=\"modellistheader\">Name</td><td class=\"modellistheader\">Description</td></tr>"
		for(var j=0;j<facets.length;j++){
			if(facets[j]){
				var rowType="even";
				if(j%2==1)rowType="odd";
				
				var modelname = facets[j][0];
				html+='<tr class="'+rowType+'"><td >'+(j+1)+'</td><td><input type="checkbox" name="'+facetType+'_'+modelname+'">'+modelname+'</input></td><td>'+facets[j][1]+'</td></tr>';
			}
		}
	html+="</table></form>";
	$('#'+facetType+'selection').html(html);
}

var loadFacetList = function(facetType,collapse){
	$('#refresh'+facetType+'info').html("Searching <img src=\"/impactportal/images/ajax-bar.gif\"/>");

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


		createCheckboxesForFacet(facetType,json.facets);



		if(collapse === true){
			$('#'+facetType+'header').collapsible('open');
		}

		setCheckBoxes();
	};

	var failure = function(data){
		$('#refresh'+facetType+'info').html('failed: '+data);
	};

	//Get all checkboxes and compose query
	var query="";
	var input = $("form input:checkbox");
	//var URLVarsFromHashTag = getUrlVarsFromHashTag();
	for(var f=0;f<searchfacets.length;f++){
		var facet = searchfacets[f];
		for(var j=0;j<input.length;j++){ 
			var checkbox = input[j];
			//if(checkbox.name == 'variable_sic')console.log("CHECKBOX: "+checkbox.name +" " + checkbox.checked);
			if(checkbox.checked){


				//if(facetType!='project')       if(input[j].name.indexOf('project_')==0){query+="project="+input[j].name.substr(8)+"&";}

				//for(var key in URLVarsFromHashTag){ 
				//var inp = key;//+"_"+URLVarsFromHashTag[key];
				//var checkbox = $("input[name='"+inp+"']");
				//if(checkbox.length>0){
				//checkbox = checkbox[0];
				//console.log(inp+" "+checkbox.checked);
				//if(checkbox.checked){
				if(facetType!=facet)       if(checkbox.name.indexOf(facet+'_')==0){query+=facet+"="+checkbox.name.substr(facet.length+1)+"&";}
			}
			//}
			//	}
			//}
		}
	}
		query+=getTimeFrame();

		var url = impactservice+"mode=getfacet&facet="+facetType+"&type=dataset&query="+URLEncode(query);
		makeJSONRequest(url,function(data){success(data,query);},failure);
	};// /loadfacetlist 



	var startBasicSearch = function(){
		$('#searchinfo').html("Searching <img src=\"/impactportal/images/ajax-bar.gif\" alt=\"loading...\"/>");

		//Get hash
		//alert(window.location.hash.split("#")[1].split("&"));

		var query="";
		var input = $("form input:checkbox");
		var selectedVariables = "";



		for(var f=0;f<searchfacets.length;f++){
			var facet = searchfacets[f];
			for(var j=0;j<input.length;j++){
				if(input[j].checked){
					//if(input[j].name.indexOf('project_')==0){query+="project="+input[j].name.substr(8)+"&";}
					if(input[j].name.indexOf(facet+'_')==0){
						var v = input[j].name.substr(facet.length+1);

						if(facet == "variable"){
							if(selectedVariables.length>0)selectedVariables+='|';
							selectedVariables+=v;
						}

						query+=facet+"="+v+"&";
					}
				}
			}
		}
		query+=getTimeFrame();




		//window.location.hash = query;


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
					html+='<td><a target=\"_blank\" href="/impactportal/data/catalogbrowser.jsp?catalog='+URLEncode(topics[j].catalogURL)+'#'+selectedVariables+'">browse</a></td>';
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

		var failure = function(data){

			$('#searchinfo').html(data);
		} 
		makeJSONRequest(url,success,failure);
	};// /startbasicsearch