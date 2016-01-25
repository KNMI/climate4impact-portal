/**
* Renders a search interface to given element.
* Arguments via:
* options{
*  element : the element to render to.
*  url: the location of the search servlet endpoint.
*  query: predefined query, loads facets in advance.
* }
*/
var renderSearchInterface = function(options){
  return new SearchInterface(options);
};

var esgfSearchIndexOf = function(thisobj,obj, start) {
    for (var i = (start || 0), j = thisobj.length; i < j; i++) {
        if (thisobj[i] === obj) { return i; }
    }
    return -1;
}

/**
* Splits a url into key value pairs.
*/
function ESGFSearch_KVP(query){
  var kvplist = [];
  function parse(query){
    var splittedKVP = query.split("&");
    if(splittedKVP){
      for(var kvpkey in splittedKVP){
        var kvp = splittedKVP[kvpkey];
        var kvps=kvp.split("=");
        if(kvps.length==2){
          var key = kvps[0];
          var value = kvps[1];
          if(!(kvplist[key] instanceof Array))kvplist[key] = [];
          kvplist[key].push(value);
        }
      }
    }
  };
  this.getKeys = function(){
    var keys = new MySet();
    for(key in kvplist){
      keys.add(key);
    }
    return keys;
  };
  this.getValues = function(key){
    return kvplist[key];
  }
  this.getKeyValues = function(){
    return kvplist;
  };
  parse(query);
};


var SearchInterface = function(options){
  var _this = this;
  //var impactESGFSearchEndPoint = "http://bhw485.knmi.nl:8280/impactportal/esgfsearch?";
  var impactESGFSearchEndPoint = "esgfsearch?";
  
  var primaryFacets = ["project", "variable", "time_frequency", "experiment", "domain", "model","access"];
  var facetNameMapping = {
    "project":"Project",
    "variable":"Parameter",
    "time_frequency":"Frequency",
    "experiment":"RCP/Experiment",
    "cf_standard_name":"CF name",
    "cmor_table":"CMOR table",   
    "data_node":"Data node",
    "experiment_family":"Experiment family",
    "variable_long_name":"Variable long name"
  };
  
  var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";
  query=(window.location.hash).replace("#","");//data_node=albedo2.dkrz.de&experiment=rcp45&project=CMIP5&time_frequency=day&variable=tas&model=EC-EARTH&";
  
 //query="variable=tas";
  var currentFacetList = undefined;
  var currentSelectedFacet = undefined;
  var rootElement = null;
  
  
 
  
  var propertyChooser = [];
  var propertyTabMenu = "";
  
  /**
  * Class to make Ajax Calls which will complete in the same order as called; e.g. it blocks calls.
  * 
  * How it works: 
  * 1) Make your own ajax function with two arguments: the first is for passing arguments and the second is called when you finished
  *  e.g. var myfunction = function(myargs,ready){}; You have to call ready when your done, this is a trigger for the following call.
  * 2) Make a new object of this class (e.g. var a = new AsyncFifo(myfunction);
  * 3) Pass your function with your arguments to AsyncFifo like this : a.call(myargs);
  * 4) Callback function with provided args will now be called asynchronousely in the same order as requested.
  */
  function AsyncFifo(callback,timeout){
    var fnToCall = callback;
    var calls = [];
    var busy = false;
    this.call = function(args){
      calls.push(args);
      if(busy === true)return;
      busy = true;
      function go(){
        if(calls.length == 0){
          busy = false;
        }else{
          var f = calls.shift();
          if(isDefined(timeout)){
            setTimeout(function(){ go();}, timeout); 
          }
          try{
            fnToCall(f,go);
          }catch(e){
            console.log("Something went wrong in AsyncFifo:");
            console.log(e);
            go();
          }
        }
      };
      go();
    };
    this.stop = function(){
      calls = [];
      busy=false;
      
    };
  };
  

  
  /**
  * A normal unordered set, e.g. a list containing no duplicates.
  */
  var MySet = function() {}
  MySet .prototype.add = function(o) { this[o] = true; }
  MySet .prototype.remove = function(o) { delete this[o]; }
  

  
  this.renderSearchInterface = function(options){
    $.blockUI.defaults.message='<div class="c4i-esgfsearch-loader"></div>';
    $.blockUI.defaults.css.border='none';
    $.blockUI.defaults.overlayCSS.backgroundColor="white";
    rootElement = options.element;
    options.element.html('<div class="simplecomponent-container">'+
    '  <div class="simplecomponent c4i-esgfsearch-selectedelements">'+
    '    <div class="simplecomponent-header">Selected filters:</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent c4i-esgfsearch-facetoverview">'+
    '    <div class="simplecomponent-header">Filters:</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent c4i-esgfsearch-results">'+
    '    <div class="simplecomponent-header">Results</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '</div>');
    
    if(options.query){
      query = options.query;
    }
    
    if(options.service){
      impactESGFSearchEndPoint = options.service;
    }
    
 
    
    $(".headerhelpbutton").button({
      
      icons: {
        primary: "ui-icon-help"
      },
    }).click(function(){
      var el = jQuery('<div title="Search help" class="headerhelpdiv"></div>', {}).dialog({
        width:800,
        height:400,
        modal:true
      });
      el.html('<div class="c4i-esgfsearch-loader"></div>');
      var helpReturned = function(data){
        el.html(data);    
      }
      $.ajax({
        url: "esgfsearch/esgfsearchhelp.html"     
      }).done(function(d) {
        helpReturned(d)
      })
    });
    
    getAllFacets();
    
    propertyChooser["project"] = new PropertyChooser(esgfsearch_pc_project);
    propertyChooser["variable"] = new NestedPropertyChooser(esgfsearch_pc_variables);
    propertyChooser["experiment"] = new NestedPropertyChooser(esgfsearch_pc_experiments);
    propertyChooser["time_frequency"] = new PropertyChooser(esgfsearch_pc_time_frequency);
  };
  
 
  
  var recentlyCheckedResponses = [];
  
  var _checkResponse = function(arg,ready){
  
    var setResult = function(arg,a){    
      var el = rootElement.find("span[name=\""+arg.id+"\"]").first();
      
      if(a.ok=="ok"){
    	  el.removeClass("c4i-esgfsearch-resultitem-checking");
    	  el.addClass("c4i-esgfsearch-resultitem-ok");
    	  el.find(".c4i-esgfsearch-resultitem-checker").html("");
      }else if(a.ok=="busy"){
    	  var el = rootElement.find("span[name=\""+arg.id+"\"]").first();
    	  el.addClass(".c4i-esgfsearch-resultitem-checking"); 
    	  el.find(".c4i-esgfsearch-resultitem-checker").html(" - checking .. ");
    	  
    	 // console.log("Busy for "+arg.id);
    	  
    	  function retry(arg){
    		  el.find(".c4i-esgfsearch-resultitem-checker").html(" - checking ... ");
    		  setTimeout(function(){
    			  checkResponseFifo.call(arg);
    		  }, 300); 
    		  
    	  };
    	  
    	  setTimeout(function(){
    		  retry(arg);
		  }, 5000); 
      }else {
    	  el.removeClass("c4i-esgfsearch-resultitem-checking");
    	  el.addClass("c4i-esgfsearch-resultitem-wrong");
    	  el.find(".c4i-esgfsearch-resultitem-checker").html(" - "+a.message);
      }
    };
    
    if(recentlyCheckedResponses[arg.id]){
      if(recentlyCheckedResponses[arg.id].ok=="ok"){
	    setResult(arg,recentlyCheckedResponses[arg.id]);
	    ready();
	    return;
      }
    }
    
    var httpCallback = function(a){
      recentlyCheckedResponses[arg.id]=a;
      setResult(arg,recentlyCheckedResponses[arg.id]);
 
      return;
    };
    
    //console.log(arg);
    if(!arg.url){
      httpCallback({message:"Error: no URL defined."});
      ready();
      return;
    }
    
    var url = impactESGFSearchEndPoint+"service=search&request=checkurl&query="+encodeURIComponent(arg.url);
    $.ajax({
      url: url,
          crossDomain:true,
          dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      //alert("fail 154");
      console.log("Ajax call failed: "+url);
      httpCallback("Failed for "+arg);
    }).always(function(){
      
      if(ready){
        ready();
      }
    });
    
  };
  
  var checkResponseFifo = new AsyncFifo(_checkResponse,5000);
   
  var checkResponses = function(data){
    checkResponseFifo.stop();
    if(!data.response){
      return;
    }
    for(var r in data.response.results){
      checkResponseFifo.call(data.response.results[r]);
    }
  };
  
  var addToBasket = function(){
    var el = jQuery('<div></div>', {
        title: 'Dataset',
      }).dialog({
        width:450,
        height:400
      });
      el.html('<div class="c4i-esgfsearch-loader"></div>');
    var callback = function(data){
      
      //renderCatalogBrowser({element:el,url:"https://localhost/impactportal/DAP/pcmdi9.llnl.gov.esgf-idp.openid.c4m/test.catalog"});
        el.html("Found "+data.numFiles+" files with totalsize of "+data.fileSize+" bytes");
    }
    $.ajax({
      url: impactESGFSearchEndPoint+"service=search&request=addtobasket&query="+encodeURIComponent(query),
          crossDomain:true,
          dataType:"jsonp"
    }).done(function(d) {
      callback(d);
    }).fail(function() {
      alert("fail 474");
    }).always(function(){
    
      //ready();
    });
  }
  
  /*
   * Callback for ajax query, both facets and results are included.
   */
  var showResponse = function(data){
   
    if(data.response == undefined){
      checkResponses(data);
      return;
    }
    var limit = data.response.limit;
    if(limit>data.response.numfound)limit = data.response.numfound;
    var html="";
    rootElement.find(".c4i-esgfsearch-results").find(".simplecomponent-header").html("Datasets: Found "+data.response.numfound+", displaying "+limit+" of "+ data.response.numfound+" results.");
    function getPageName(url) {
      var index = url.lastIndexOf("/") + 1;
      var extensionIndex = url.lastIndexOf(".")-index;
      var filename = url.substr(index,extensionIndex);
      //filename = filename.replace(/\./g," ");
      return filename;          
    };
    html+="<div class=\"c4i-esgfsearch-resultlist\">";
    for(var r in data.response.results){
     // html+="<span  name=\""+data.response.results[r].id+"\">";
      html+="<span class=\"c4i-esgfsearch-resultitem c4i-esgfsearch-resultitem-checking\" name=\""+data.response.results[r].id+"\">";
      
      html+="<span class=\"c4i-esgfsearch-dataset-baseimage c4i-esgfsearch-dataset-collapsible c4i-esgfsearch-dataset-imgcollapsed\"></span>";
       html+="<span class=\"c4i-esgfsearch-resultitem-content\">";
       html+= data.response.results[r].id.replaceAll("."," ");
       html+= " <span class=\"c4i-esgfsearch-resultitem-checker\">checking .</span><br/>";
//        html+="<span style=\"margin-left:0px;\">Url: "+data.response.results[r].url+"</span>";
       html+="</span>";
       //html+= "<span style=\"float:right;\">";
//        html+="<span style=\"float:right;\" class=\"c4i-esgfsearch-dataset-baseimage c4i-esgfsearch-dataset-download\"></span>";
//        html+="<span style=\"float:right;\" class=\"c4i-esgfsearch-dataset-baseimage c4i-esgfsearch-dataset-process\"></span>";
      // html+="<span style=\"float:right;\" class=\"c4i-esgfsearch-dataset-baseimage c4i-esgfsearch-dataset-selectable c4i-esgfsearch-dataset-imgunchecked\"></span>";
       html+="<span class=\"c4i-esgfsearch-dataset-expandedarea\">";
       html+="<span class=\"c4i-esgfsearch-dataset-catalogurl\"></span>";
       html+="<span class=\"c4i-esgfsearch-dataset-catalogdetails\"></span>";
      html+="</span>";
       html+="</span>";
    }
    html+="</div>";
    var addToBasketButton = "<button class=\"button_addtobasket\">Add results to basket</button>";
    //html+="<div style=\"clear: both;\">"+addToBasketButton+"<br/></div>";
    rootElement.find(".c4i-esgfsearch-results").find(".simplecomponent-body").first().html(html);
    
    rootElement.find(".button_addtobasket").button().attr('onclick','').click(function(t){
      addToBasket();
    });
   
    rootElement.find(".c4i-esgfsearch-dataset-expandedarea").hide();
    
    rootElement.find(".c4i-esgfsearch-dataset-selectable").attr('onclick','').click(function(t){
      if($(this).hasClass("c4i-esgfsearch-dataset-imgchecked")){
        $(this).removeClass("c4i-esgfsearch-dataset-imgchecked");
        $(this).addClass("c4i-esgfsearch-dataset-imgunchecked");
      }else{
        $(this).removeClass("c4i-esgfsearch-dataset-imgunchecked");
        $(this).addClass("c4i-esgfsearch-dataset-imgchecked");
      }
    });
    
    rootElement.find(".c4i-esgfsearch-dataset-collapsible").attr('onclick','').click(function(t){
   
      if($(this).hasClass("c4i-esgfsearch-dataset-imgcollapsed")){
        $(this).removeClass("c4i-esgfsearch-dataset-imgcollapsed");
        $(this).addClass("c4i-esgfsearch-dataset-imgexpand");
        //if(!getSelection().toString())
        {
          var clickedID = $(this).parent().attr("name");
          var catalogObject = null;
          for(var r in data.response.results){
            if(clickedID == data.response.results[r].id){
              catalogObject = data.response.results[r];
              break;
            }
          }
          if(catalogObject == null){
            alert("Catalog contains no valid URL / link.");
            //alert("Internal error at esgfsearch.js at line 173");
            return;
          }
          el = $(this).parent().find(".c4i-esgfsearch-dataset-expandedarea").first().show();
          el = $(this).parent().find(".c4i-esgfsearch-dataset-catalogurl");
          el.html("<b>Catalog Url:</b> <a href=\""+catalogObject.url+"\">"+catalogObject.url+"</a>");
          el = $(this).parent().find(".c4i-esgfsearch-dataset-catalogdetails");
          
          var k = new ESGFSearch_KVP(query);
          var selectedFacets = k.getKeyValues();
          var selectedPropertiesForFacet = selectedFacets["variable"];
          var variableFilter='';
          if(selectedPropertiesForFacet){
            console.log(selectedPropertiesForFacet);
        
            for(var j=0;j<selectedPropertiesForFacet.length;j++){
              if(variableFilter.length>0)variableFilter+="|";
              variableFilter+=selectedPropertiesForFacet[j];
            }
          }
          renderCatalogBrowser({element:el,url:catalogObject.url,variables:variableFilter});
        }
      }else{
        $(this).removeClass("c4i-esgfsearch-dataset-imgexpand");
        $(this).addClass("c4i-esgfsearch-dataset-imgcollapsed");
        $(this).parent().find(".c4i-esgfsearch-dataset-expandedarea").hide();

      }
//     });
// 
//     
//     rootElement.find(".c4i-esgfsearch-resultitem").attr('onclick','').click(function(t){

    });
    
    checkResponses(data);
  };
  

  
  var _getPropertiesForFacetName = function(args,ready){
    var name = args.name;
    var callback= args.callback;
    
    
    
    var httpCallback = function(result){
      showResponse(result);
      var data = result.facets;
      var facet = data[name];

      callback(facet);
      
    };
    
    $.ajax({
      url: impactESGFSearchEndPoint+"service=search&request=getfacets&query="+encodeURIComponent(query),
          crossDomain:true,
          dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      alert("fail 239");
      callback("Failed");
    }).always(function(){
      if(ready){
        ready();
      }
    });
  };
    
  var facetFIFO = new AsyncFifo(_getPropertiesForFacetName);
  
  var getPropertiesForFacetName = function(arg){
    facetFIFO.call(arg);
  };
  
  /**
  * Happens when a property inside a facet is clicked.
  */
  
  var propertyClick = function(id){
    showFilters();
  };
  
  /* Generate the propertylist for selected facet */
  var generatePropertyListSelector = function(facetList,facetName){
    if(!isDefined(facetList)){
      return;
    }
    
    //Get property_descriptions
    var descriptions = "";
    if(property_descriptions){
     if(property_descriptions[facetName]){
       descriptions = property_descriptions[facetName];
     }
    }
    
    //Get selected filters and properties from query.
    var k = new ESGFSearch_KVP(query);
    var selectedFacets = k.getKeyValues();
    var selectedPropertiesForFacet = selectedFacets[facetName];
    var html="";
    var even = 0;
    var autocompleteList = [];
    for(var i in facetList){
      var oddEvenClass = "";
      var selectedClass = "";  
      var checkboxclass = "c4i-esgfsearch-checkboxclear";
      if(even == 0)oddEvenClass="c4i-esgfsearch-property-even";else oddEvenClass="c4i-esgfsearch-property-odd";
      if(selectedPropertiesForFacet){
        if(esgfSearchIndexOf(selectedPropertiesForFacet,facetList[i])!=-1){
          selectedClass = "c4i-esgfsearch-property-selected";
          checkboxclass = "c4i-esgfsearch-checkbox";
        }
      }
      var description = "";
      description = descriptions[facetList[i]];
      if(!description)description = "";
      if(description.length>0) {
        autocompleteList.push(description);
      }
      html+="<span name=\""+facetList[i]+"\" class=\"c4i-esgfsearch-property "+selectedClass+" "+oddEvenClass+"\">"+
      "<span class=\"c4i-esgfsearch-property-checkbox "+checkboxclass+"\"></span><span style=\"width:170px;display:inline-block;\">"+facetList[i]+"</span><i>"+description+"</i></span>";
      even = 1-even;
      
      autocompleteList.push(facetList[i]);
      
    }
    
    var tabPropertySelector = "<div class=\"c4i-esgfsearch-property-container c4i-esgfsearch-tabcontainer\">"+html+"</div>"+
       "<div class=\"c4i-esgfsearch-autocomplete\"><input class=\"c4i-esgfsearch-searchautocomplete\" ></input></div>";

    rootElement.find(".c4i-esgfsearch-selectfacet-container").html(tabPropertySelector);
    
    rootElement.find(".c4i-esgfsearch-property-container").show();
    var noPropertyChooser = true;
    //if(!selectedPropertiesForFacet)
    {
      if(propertyChooser){
        if(propertyChooser[facetName]){
          noPropertyChooser = false;
          rootElement.find(".c4i-esgfsearch-selectfacet-container").prepend(
            "<div class=\"c4i-esgfsearch-property-menu c4i-esgfsearch-tabcontainer\">"+
              propertyChooser[facetName].html+
            "</div>"
          );
          var numprops = propertyChooser[facetName].init(rootElement,facetName,facetList,query,_this.addFilterProperty);
          
        
          
          rootElement.find(".c4i-esgfsearch-selectfacet-container").prepend(
            "<div class=\"c4i-esgfsearch-tabmain\">"+
            "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-tab-menu\">Quick select</div>"+
            "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-tab-properties\">All "+facetName+" properties ("+facetList.length+")</div>"+
            "</div>"
          );
          if(numprops >0){
            rootElement.find(".c4i-esgfsearch-property-container").hide();
            rootElement.find(".c4i-esgfsearch-tab-menu").addClass("c4i-esgfsearch-tab-selected");
          }else{
            rootElement.find(".c4i-esgfsearch-property-menu").hide();
            rootElement.find(".c4i-esgfsearch-tab-properties").addClass("c4i-esgfsearch-tab-selected");
          }
        }
      }
      
      if(noPropertyChooser){
         rootElement.find(".c4i-esgfsearch-selectfacet-container").prepend(
            "<div class=\"c4i-esgfsearch-tabmain\">"+
            "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-tab-properties c4i-esgfsearch-tab-selected\">"+facetName+" ("+facetList.length+")</div>"+
            "</div>"
          );
      }
    }
    
    var showPropertyTab = function(){
      rootElement.find(".c4i-esgfsearch-property-menu").hide();
      rootElement.find(".c4i-esgfsearch-tab-menu").removeClass("c4i-esgfsearch-tab-selected");
      
      rootElement.find(".c4i-esgfsearch-property-container").show();
      rootElement.find(".c4i-esgfsearch-tab-properties").addClass("c4i-esgfsearch-tab-selected");
      propertyTabMenu = "allproperties";
    };
    
    if(propertyTabMenu == "allproperties"){
      showPropertyTab();
    }
    
    rootElement.find(".c4i-esgfsearch-tab-properties").attr('onclick','').click(function(evt){showPropertyTab();});
    
    rootElement.find(".c4i-esgfsearch-tab-menu").attr('onclick','').click(function(evt){
      rootElement.find(".c4i-esgfsearch-property-container").hide();
      rootElement.find(".c4i-esgfsearch-tab-properties").removeClass("c4i-esgfsearch-tab-selected");
      
      rootElement.find(".c4i-esgfsearch-property-menu").show();
      rootElement.find(".c4i-esgfsearch-tab-menu").addClass("c4i-esgfsearch-tab-selected");
      propertyTabMenu = "quickmenu";
    });
    

    
    var selectElement = function(element){
      if(element.hasClass("c4i-esgfsearch-property-selected")){
     
        element.removeClass("c4i-esgfsearch-property-selected");
        element.find(".c4i-esgfsearch-property-checkbox").removeClass("c4i-esgfsearch-checkbox");
        element.find(".c4i-esgfsearch-property-checkbox").addClass("c4i-esgfsearch-checkboxclear");
        
        removeFilterProperty(facetName,element.attr("name"));
        
        
        if(isCtrl == false){
          if(rootElement.find(".c4i-esgfsearch-property-selected").length==0){
            removeFilterProperty(facetName);
            loadAndDisplayFacet(facetName);
          }
        }
      
      }else{
        element.addClass("c4i-esgfsearch-property-selected");
        element.find(".c4i-esgfsearch-property-checkbox").removeClass("c4i-esgfsearch-checkboxclear");
        element.find(".c4i-esgfsearch-property-checkbox").addClass("c4i-esgfsearch-checkbox");
     
        _this.addFilterProperty(facetName,element.attr("name"));
     
      }
      
      propertyClick(element);
      
    };
    
    rootElement.find(".c4i-esgfsearch-searchautocomplete" ).first().autocomplete({
      autoFocus:true,
      source: function( request, response ) {
        var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( request.term ), "i" );
        response( $.grep( autocompleteList, function( item ){
          
          return matcher.test( item );
        }) );
      },
      select:function(event,ui){
        //Search first in dom
        var el =rootElement.find("span[name=\""+ui.item.value+"\"]");
        if(el.length == 0){
          //Search in descriptions
          if(descriptions){
            for(var i in facetList){
              if(descriptions[facetList[i]] == ui.item.value){
//                 console.log("Found "+facetList[i]);
//                 console.log("Found "+i);
                el =rootElement.find("span[name=\""+facetList[i]+"\"]");
                break;
              }
            }
          }
        }
        selectElement(el.first());
      }
    });
    
    rootElement.find(".c4i-esgfsearch-property").attr('onclick','').click(function(evt){
      //if(!getSelection().toString()){
        selectElement($(this));
      //}
    });
  };
  
  /**
  * Happens when a facet, e.g. standard_name, model, experiment is clicked
  */
  var loadAndDisplayFacet = function(facetName){
    var facetselectordiv=rootElement.find(".c4i-esgfsearch-selectfacet-container");
    facetselectordiv.block();
    getPropertiesForFacetName({name:facetName,callback:function(facetList){currentFacetList = facetList;currentSelectedFacet=facetName;generatePropertyListSelector(facetList,facetName);}});
  };
  
  this.addFilterProperty = function(facet,property){
    var k = new ESGFSearch_KVP(query);
    var kvps = k.getKeyValues();
    query = "";
    for(var kvp in kvps){
      for(var vkey in kvps[kvp]){
        var v= kvps[kvp][vkey];
        //if(!(kvp==facet&&property==v)){
          query+=kvp+"="+encodeURIComponent(v)+"&";
        //}
      }
    }
     query+=facet+"="+encodeURIComponent(property)+"&";
     
     window.location.hash = query;
     
    if(isCtrl == false){
//      showFilters();
      getAllFacets();
    }
  };
  var removeFilterProperty = function(facet,property){
    
    //console.log("Remove");
    var k = new ESGFSearch_KVP(query);
    var kvps = k.getKeyValues();
    query = "";
    for(var kvp in kvps){
      for(var vkey in kvps[kvp]){
        var v= kvps[kvp][vkey];
        if(isDefined(property)){
          if(!(kvp==facet&&property==v)){
            query+=kvp+"="+encodeURIComponent(v)+"&";
          }
        }else{
           if(!(kvp==facet)){
            query+=kvp+"="+encodeURIComponent(v)+"&";
          }
        }
      }
    }
    window.location.hash = query;
    if(isCtrl == false){
//      showFilters();
      getAllFacets();
    }
    
  };
  var showFilters = function(){
    var k = new ESGFSearch_KVP(query);
    var kvps = k.getKeyValues();
    var html = "";
    for(var kvp in kvps){
      for(var vkey in kvps[kvp]){
        var v= kvps[kvp][vkey];
        html+="<span class=\"c4i-esgfsearch-facets c4i-esgfsearch-facetchoosed c4i-esgfsearch-roundedborder\">";
        html+="<span class=\"c4i-esgfsearch-removefilterproperty c4i-esgfsearch-checkbox\" name=\""+kvp+","+v+"\"></span>"+_getFacetName(kvp)+" : "+decodeURIComponent(v);
        html+="</span>";
      }
    }
    if(html.length==0){
      html="none";
    }
    //html+=getFilterResultsButton();
    var el = rootElement.find(".c4i-esgfsearch-selectedelements").find(".simplecomponent-body").first();
    el.html(html);
    el.find(".c4i-esgfsearch-removefilterproperty").attr('onclick','').click(function(t){
      var facetAndValue = ($(this).attr('name')).split(",");
      removeFilterProperty(facetAndValue[0],facetAndValue[1]);
      loadAndDisplayFacet(currentSelectedFacet);
    });
    
    
    
  };
  
  var _getFacetName = function(id){

    if(facetNameMapping[id]){
      return facetNameMapping[id];
    }else{
      var d=id;
      return d.charAt(0).toUpperCase() + d.slice(1);
    }
  };
  
  var listAllFacets = false;

  var _getAllFacets = function(args,ready){
    showFilters();
    rootElement.find(".c4i-esgfsearch-facetoverview").find(".simplecomponent-body").first().block();
    var callback = function(result){
      
      if(result == undefined)return;
      if(result.response == undefined){
        checkResponses(result);
        return;
      } 
      if(result.facets == undefined)return;
      //Show found results
      showResponse(result);
      
      //List corresponding facets
      var facets= [];
      
      //Get all selected filter names
      var k = new ESGFSearch_KVP(query);
      var selectedFacets = k.getKeyValues();
      
      //Sort them so that primaryFacets are always in front.
      for(var j=0;j<primaryFacets.length;j++){
        var key = primaryFacets[j];
        if(result.facets[key]){
          facets[key]=result.facets[key];
        }
      }
      

      
      //Add remaining facets sorted
      if(listAllFacets){
        var sortedList = [];for(var key in result.facets){sortedList.push(key);};sortedList.sort();
        for(var j=0;j<sortedList.length;j++){
          var key = sortedList[j];
          //console.log(key);
          if(!facets[key]){
            facets[key]=result.facets[key];
          }
        }
      }

      var count;
      var html = "";
      for(var facetkey in facets){
        count =0;
        for(var property in facets[facetkey]){
          count++;
        }
        var facet = "<span name=\""+facetkey+"\" class=\"c4i-esgfsearch-clickablefacet c4i-esgfsearch-facets c4i-esgfsearch-roundedborder";
        
        
        //Gray out facets which are already selected of cannot be selected again.
        if(selectedFacets[facetkey]){
          facet+=" c4i-esgfsearch-facetchoosed";
        }else{
          if(count==1){
            facet+=" c4i-esgfsearch-facetdisabled";
          }
        }
        facet+="\">"+_getFacetName(facetkey)+" ("+count+")</span>";
        
        //if(!selectedFacets[facetkey]){
          if(count>0){
            html+=facet;
          }
        //}
      }
      
      if(!listAllFacets){
        html+="<span name=\"showall\" class=\"c4i-esgfsearch-listallfacets c4i-esgfsearch-facets c4i-esgfsearch-facets-selectmoreless c4i-esgfsearch-roundedborder\">&gt; more...</span>";
      }else{
         html+="<span name=\"showdefault\" class=\"c4i-esgfsearch-listallfacets c4i-esgfsearch-facets c4i-esgfsearch-facets-selectmoreless c4i-esgfsearch-roundedborder\">^ less</span>";
      }
      html+="<div class=\"c4i-esgfsearch-selectfacet-container\"></div>";
      html+="</div>";
      
      rootElement.find(".c4i-esgfsearch-facetoverview").find(".simplecomponent-body").first().html(html);
      
      showFilters();
      
      rootElement.find(".c4i-esgfsearch-clickablefacet").attr('onclick','').click(function(t){
        rootElement.find(".c4i-esgfsearch-clickablefacet").removeClass("c4i-esgfsearch-facetselected");
        $(this).addClass("c4i-esgfsearch-facetselected");
        loadAndDisplayFacet($(this).attr("name"));
      });
      rootElement.find(".c4i-esgfsearch-listallfacets").attr('onclick','').click(function(t){
        if($(this).attr("name")=="showall"){
          listAllFacets=true;
        }else{
           listAllFacets=false;
        }
         getAllFacets();
      });
      generatePropertyListSelector(currentFacetList,currentSelectedFacet);
    };
    
    $.ajax({
      url: impactESGFSearchEndPoint+"service=search&request=getfacets&query="+encodeURIComponent(query),
          crossDomain:true,
          dataType:"jsonp"
    }).done(function(d) {
      callback(d);
    }).fail(function() {
      alert("fail 474");
    }).always(function(){
    
      ready();
    });
  };
  
  var getAllFacetsFIFO = new AsyncFifo(_getAllFacets);
  
  var getAllFacets = function(){
    getAllFacetsFIFO.call();
  };
  
  
  
  var isCtrl = false;
  $(document).keydown(function(e) {
    if(e.ctrlKey === true) {
      if(e.keyCode==67 || e.keyCode==88){
        isCtrl = false;
        return;
      }
      isCtrl = true;
    }
  });
  $(document).keyup(function(e) {
    if(e.ctrlKey == false){
      if(isCtrl == true){
        showFilters();
        getAllFacets();
      }
      isCtrl = false;
    }
    
  });        
  
  this.renderSearchInterface(options);
};
