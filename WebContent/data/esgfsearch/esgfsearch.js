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

var esgfSearchGetKeys = function(obj){
  if (!Object.keys) {
    var keys = [],
        k;
    for (k in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, k)) {
            keys.push(k);
        }
    }
    return keys;
  }else{
    return Object.keys(obj);
  }
};

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
  
  var esdocurl="http://view.es-doc.org/?client_id=climate4impact_esgfsearch&";
  
  var _this = this;
  //var impactESGFSearchEndPoint = "http://bhw485.knmi.nl:8280/impactportal/esgfsearch?";
  var impactESGFSearchEndPoint = "esgfsearch?";
  var impactCatalogBrowserEndPoint;
  
  var primaryFacets = ["project", "variable", "time_frequency", "experiment", "domain", "model","access","time_start_stop","bbox","query"];
  var facetNameMapping = {
    "project":"Project",
    "variable":"Parameter",
    "time_frequency":"Frequency",
    "experiment":"Experiment",
    "cf_standard_name":"CF name",
    "cmor_table":"CMOR table",   
    "data_node":"Data node",
    "experiment_family":"Experiment family",
    "variable_long_name":"Variable long name",
    "time_start_stop":"Date",
    "bbox":"Geobox",
    "driving_model": "Driving model", 
    "query":"Free text"
  };
  
  var facetDescription = {
      "domain":"This filter lists the geographical domains for CORDEX projects. For more info check <a target=\"_blank\" href=\"http://www.cordex.org/\">http://www.cordex.org/</a>. ",
      "model":"Choose the climate model. Read more at the <a target=\"_blank\" href=\"/impactportal/general/index.jsp?q=climate_models\">Climate models</a> page.",
      "access":"Choose the type of access. OpenDAP enables visualization and processing on this portal. Use HTTPServer for direct download.",
      "variable":"Choose the Parameter. A list of CMIP5 variables is published at the <a target=\"_blank\" href=\"/impactportal/documentation.jsp?q=listofcmip5variables\">CMIP5 variables</a> page. If you select from 'All Parameters' you can reduce the list by first chosing the appropriate 'Realm'.",
      "ensemble":"Choose ensemble member. For most impact assessments a single member is enough (choose 'r1i1p1'); you do need more members in case you are interested in a) rare (extreme) events, b) changes over  <a target=\"_blank\" href=\"/impactportal/documentation/backgroundandtopics.jsp?q=scenarios_2030\">short periods/time horizons (<30yrs)</a> ",
      "realm": "Choose earth system compartment to reduce the list of 'Parameters'",
      "institute":"Choose institute to reduce list of 'Models'",
      "experiment_family": "Choose family to reduce list of 'experiments', see <a target=\"_blank\" href=\http://cmip-pcmdi.llnl.gov/cmip5/getting_started_CMIP5_experiment.html#_T4\">CMIP5 Experiments</a>",
      "driving_model": "GCM used to force regional climate models",
      "data_node": "Generally not relevant, but for large downloads you may wish to choose the one closest to you",
      "time_frequency": "Frequency at which model data are archived (is not equal to model time step). Note that for some (high) frequencies instantaneous model states are stored, generally for daily and longer periods average states over that period are stored. See respective netcdf metadata for exact details."
 };
  
  var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";
  query=(window.location.hash).replace("#","");//data_node=albedo2.dkrz.de&experiment=rcp45&project=CMIP5&time_frequency=day&variable=tas&model=EC-EARTH&";
  query = query.replaceAll("&amp;","&");
 //query="variable=tas";
  var currentFacetList = undefined;
  var currentSelectedFacet = undefined;
  var rootElement = null;
  
  var currentPage = 1;
 
  
  var propertyChooser = [];
  var propertyTabMenu = [];
  
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
    $.blockUI.defaults.message='<div class="c4i-esgfsearch-loader-box"><div class="c4i-esgfsearch-loader-animation"></div><div class="c4i-esgfsearch-loader-message">Searching ...</div></div>';
    $.blockUI.defaults.css.border='none';
    $.blockUI.defaults.overlayCSS.backgroundColor="white";
    
    if(options.dialog){
      options.element.dialog({
        //title:'Search',
        width:975,
        height:600,
        dialogClass:'c4i-esgfsearch-containerdialog'
      });
    }else{
      options.element.addClass("c4i-esgfsearch-container");
    }
    
    rootElement = options.element;
    options.element.html('<div class="simplecomponent-container">'+
    '  <div class="simplecomponent c4i-esgfsearch-facetoverview">'+
    '    <div class="simplecomponent-header">Filters</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent c4i-esgfsearch-selectedelements">'+
    '    <div class="simplecomponent-header">Selected filters</div>'+
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
    
    
    if(options.catalogbrowserservice){
      impactCatalogBrowserEndPoint = options.catalogbrowserservice;
    }
 
    
    $(".c4i_esgfsearch_help").button({
      
      icons: {
        primary: "ui-icon-help"
      },
    }).click(function(){
      userHasSignedInOrOut();
      return;
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
    propertyChooser["time_start_stop"] = new TimeChooser(esgfsearch_pc_time_start_stop);
    propertyChooser["bbox"] = new AreaChooser(esgfsearch_pc_bbox);
    propertyChooser["query"] = new FreeTextQueryChooser(esgfsearch_pc_query);
  };
  
 
  
  var recentlyCheckedResponses = [];
  
  var _checkResponse = function(arg,ready){
  
    var setResult = function(arg,a){    
      var el = rootElement.find("span[name=\""+arg.url+"\"]");//.first();
      
      if(a.ok=="ok"){
    	  el.removeClass("c4i-esgfsearch-resultitem-checking");
    	  el.addClass("c4i-esgfsearch-resultitem-ok");
    	  el.find(".c4i-esgfsearch-resultitem-checker").html("");
      }else if(a.ok=="busy"){
    	  var el = rootElement.find("span[name=\""+arg.url+"\"]").first();
    	  el.addClass(".c4i-esgfsearch-resultitem-checking"); 
    	  el.find(".c4i-esgfsearch-resultitem-checker").html(" - <span class=\"c4i-esgfsearch-loader-animationsmall\"></span> checking catalog.. ");
    	  

    	  
    	  function retry(arg){
    		  el.find(".c4i-esgfsearch-resultitem-checker").html(" - <span class=\"c4i-esgfsearch-loader-animationsmall\"></span> checking catalog ... ");
    		  setTimeout(function(){
    			  checkResponseFifo.call(arg);
    		  }, 300); 
    		  
    	  };
    	  
    	  setTimeout(function(){
    		  retry(arg);
		  }, 3000); 
      }else {
    	  el.removeClass("c4i-esgfsearch-resultitem-checking");
    	  el.addClass("c4i-esgfsearch-resultitem-wrong");
    	  el.find(".c4i-esgfsearch-resultitem-checker").html(" - "+a.message);
      }
    };
    
    if(recentlyCheckedResponses[arg.url]){
      if(recentlyCheckedResponses[arg.url].ok=="ok"){
	    setResult(arg,recentlyCheckedResponses[arg.url]);
	    ready();
	    return;
      }
    }
    
    var httpCallback = function(a){
      recentlyCheckedResponses[arg.url]=a;
      setResult(arg,recentlyCheckedResponses[arg.url]);
 
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
  
  
  /*
   * Callback for ajax query, both facets and results are included.
   */
  var showResponse = function(data){
    rootElement.find(".c4i-esgfsearch-results").parent().unblock();
    if(data.response == undefined){
      checkResponses(data);
      return;
    }
    var limit = data.response.limit;
    if(limit>data.response.numfound)limit = data.response.numfound;
    var numPages = parseInt((data.response.numfound/data.response.limit))+1;
    var html="";
    rootElement.find(".c4i-esgfsearch-results").find(".simplecomponent-header").html("Found "+data.response.numfound+" datasets. Displaying page "+currentPage+" of "+numPages+".");//+");// with "+limit+" results");
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
      html+="<span class=\"c4i-esgfsearch-resultitem c4i-esgfsearch-resultitem-checking\" name=\""+data.response.results[r].url+"\">";
      
      html+="<span class=\"c4i-esgfsearch-dataset-baseimage c4i-esgfsearch-dataset-collapsible c4i-esgfsearch-dataset-imgcollapsed\" title=\"Expand this dataset and show its content.\"></span>";
      
      //Add catalog to Basket
      html+="  <span title=\"Link this catalog to your basket. It will appear under Remote data in your basket.\" class=\"c4i-esgfsearch-resultitem-addtobasket-content\">";
      html+="  <span class=\"c4i-esgfsearch-dataset-baseimage c4i-esgfsearch-dataset-addtobasket\" onclick=\"basket.postIdentifiersToBasket({id:'"+data.response.results[r].id+"',catalogURL:'"+data.response.results[r].url+"',filesize:'0'});\"></span>";
      html+="  </span>";
      
       html+="<span class=\"c4i-esgfsearch-resultitem-content\">";
       var id = data.response.results[r].esgfid;
    
       html+= id;//.replaceAll("."," ");
       html+= " <span class=\"c4i-esgfsearch-resultitem-checker\">Start checking .</span>";
       html+="</span>";
       //ESDOC
       if(data.response.results[r].esgfid.indexOf("cmip5")==0){
         html+="<span class=\"c4i-esgfsearch-resultitem-esdoc\">";
         html+="<a target=\"_blank\" title=\"Show ESDOC dataset metadata\" href=\""+esdocurl+"renderMethod=datasetid&project=cmip5&id="+data.response.results[r].id+"\"></a>";
         html+="</span>";
       }


       
       html+="<span class=\"c4i-esgfsearch-dataset-expandedarea\">";
       html+="<span class=\"c4i-esgfsearch-dataset-catalogurl\"></span>";
       html+="<span class=\"c4i-esgfsearch-dataset-catalogdetails\"></span>";
       html+="</span>";
       

       
       html+="</span>";
    }
    html+="</div>";
    
//    console.log("currentPage "+currentPage);
    
    /* Handle pagination  */
    
    var startPage = currentPage-4;
    if(startPage < 1)startPage  = 1;
    var stopPage = startPage+10;
    while(startPage>1&&stopPage>numPages-1){
      startPage--;
      stopPage--;
    }
    if(stopPage>numPages){
      stopPage = numPages;
    }
    if(currentPage>numPages){
      currentPage = numPages;
    }
    
//     console.log("currentPage "+currentPage);
//     console.log("numPages "+numPages);
//     console.log("startPage "+startPage);
//     console.log("stopPage "+stopPage);

    var pagination = "";
    pagination = "<span class=\"c4i-esgfsearch-paginator\">";
    pagination +="<span class=\"c4i-esgfsearch-paginator-pagenr c4i-esgfsearch-noselect\" name=\"previous\">&laquo; Previous</span>";
    
    
    pagination +="<span class=\"c4i-esgfsearch-paginator-pagenr c4i-esgfsearch-noselect\" name=\"1\">"+1+"</span>";
    if(startPage>2){
      pagination +="<span class=\"c4i-esgfsearch-paginator-pagenrsplitter c4i-esgfsearch-noselect\">...</span>";
    }
    for(var j=startPage+1;j<stopPage+1;j++){
      pagination +="<span class=\"c4i-esgfsearch-paginator-pagenr\" name=\""+j+"\">"+j+"</span>";
    }
    if(stopPage<numPages){
      //if(stopPage<numPages-2){
        pagination +="<span class=\"c4i-esgfsearch-paginator-pagenrsplitter c4i-esgfsearch-noselect\">...</span>";
      //}
      pagination +="<span class=\"c4i-esgfsearch-paginator-pagenrsplitter c4i-esgfsearch-noselect\" name=\""+(numPages)+"\">"+(numPages)+"</span>";
    }
    pagination +="<span class=\"c4i-esgfsearch-paginator-pagenr c4i-esgfsearch-noselect\" name=\"next\">Next &raquo;</span>";
    pagination += "<span class=\"c4i-esgfsearch-paginator-export\">";
    pagination +="<span class=\"c4i-esgfsearch-paginator-pagenr c4i-esgfsearch-noselect\" name=\"export\" title=\"Export the search query as CSV. All files within datasets are expanded.\">Export to CSV</span>";
    pagination += "</span>";
    pagination += "</span>";
    
    html=pagination+html+pagination;
    
    
    /* Set HTML */
    rootElement.find(".c4i-esgfsearch-results").find(".simplecomponent-body").first().html(html);
    
    
    /* Pagination handler */
    var selectPage = function(pageNr){
      rootElement.find(".c4i-esgfsearch-results").find(".c4i-esgfsearch-paginator").find(".c4i-esgfsearch-paginator-pagenr").removeClass("c4i-esgfsearch-paginator-pagenr-selected");
      rootElement.find(".c4i-esgfsearch-results").find(".c4i-esgfsearch-paginator").find("[name='"+pageNr+"']").addClass("c4i-esgfsearch-paginator-pagenr-selected");
    }
    
    selectPage(currentPage);
    
    rootElement.find(".c4i-esgfsearch-results").find(".c4i-esgfsearch-paginator").find(".c4i-esgfsearch-paginator-pagenr").attr('onclick','').click(function(t){
      var name = $(this).attr("name");
      if(name == "previous"){
        currentPage--;
        if(currentPage<1) currentPage =1;
      }else if(name == "next"){
        currentPage++;
        if(currentPage>numPages) currentPage =numPages;
      }else if(name == "export"){
        if(data.response.numfound>200){
          alert("<h2>Please reduce the amount of results</h2>At most 200 datasets are allowed for exporting. <br/></br>Please drill down your search and try again");
          return;
        }
        
        var url = impactESGFSearchEndPoint+"service=search&request=getSearchResultAsCSV&query="+encodeURIComponent(query);
        var win = window.open(url, '_blank');
        if (win) {
            //Browser has allowed it to be opened
            win.focus();
        } else {
            //Browser has blocked it
            alert('Please allow popups for this website');
        }
      }else{
        currentPage = name;
        
      }

      getAllFacets();
    });
    
    /* Dataset row handler */
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
            if(clickedID == data.response.results[r].url){
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
            //console.log(selectedPropertiesForFacet);
        
            for(var j=0;j<selectedPropertiesForFacet.length;j++){
              if(variableFilter.length>0)variableFilter+="|";
              variableFilter+=selectedPropertiesForFacet[j];
            }
          }
          var service = impactCatalogBrowserEndPoint;
          
          renderCatalogBrowser({element:el,url:catalogObject.url,variables:variableFilter,service:service});
        }
      }else{
        $(this).removeClass("c4i-esgfsearch-dataset-imgexpand");
        $(this).addClass("c4i-esgfsearch-dataset-imgcollapsed");
        $(this).parent().find(".c4i-esgfsearch-dataset-expandedarea").hide();

      }
    });
    
    checkResponses(data);
  };
  

  
  var _getPropertiesForFacetName = function(args,ready){
    var name = args.name;
    var callback= args.callback;
    
    
    
    
    var httpCallback = function(result){
      if(checkForErrors(result,"GetPropertiesForFacetName")!=0){
        return;
      }
      showResponse(result);
      var data = result.facets;
      var facet = data[name];
      callback(facet);
    };
    
    var url = impactESGFSearchEndPoint+"service=search&request=getfacets&query="+encodeURIComponent(query);
    $.ajax({
      url: url,
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
    var facetListNr = 0;
    var sortedKeys=esgfSearchGetKeys(facetList).sort();
    for(var si=0; si<sortedKeys.length;si++){
      var i=sortedKeys[si]
      facetListNr++;
      var oddEvenClass = "";
      var selectedClass = "";  
      var checkboxclass = "c4i-esgfsearch-checkboxclear";
      if(even == 0)oddEvenClass="c4i-esgfsearch-property-even";else oddEvenClass="c4i-esgfsearch-property-odd";
      if(selectedPropertiesForFacet){
        if(esgfSearchIndexOf(selectedPropertiesForFacet,i)!=-1){
          selectedClass = "c4i-esgfsearch-property-selected";
          checkboxclass = "c4i-esgfsearch-checkbox";
        }
      }
      var description = "";
      description = descriptions[i];
      if(!description)description = "";
      if(description.length>0) {
        autocompleteList.push(description);
      }
      html+="<span name=\""+i+"\" class=\"c4i-esgfsearch-property "+selectedClass+" "+oddEvenClass+"\">";
      html+="<span class=\"c4i-esgfsearch-property-counter\" >"+facetListNr+")</span>";
      html+="<span class=\"c4i-esgfsearch-property-checkbox "+checkboxclass+"\"></span>";
      html+="<span class=\"c4i-esgfsearch-property-name\">"+i+"<span class=\"c4i-esgfsearch-property-count\">("+facetList[i]+")</span></span>";
      html+="<span class=\"c4i-esgfsearch-property-description\">"+description+"</span>";
      html+="</span>";
      even = 1-even;
      
      autocompleteList.push(i);
      
    }
    
    var tabPropertySelector = "<div class=\"c4i-esgfsearch-tabcontainer c4i-esgfsearch-property-container\">";
    if(facetDescription[facetName]){
      tabPropertySelector+="<div class=\"c4i-esgfsearch-generallabel\">";
      tabPropertySelector+="<span class=\"c4i-esgfsearch-property-headerlabel\">"+facetDescription[facetName]+"</span>";
      tabPropertySelector+="</div>";
    }
    tabPropertySelector+="<div class=\"c4i-esgfsearch-property-container-properties\">"+html+
       "</div><div class=\"c4i-esgfsearch-autocomplete\">Filter: <input class=\"c4i-esgfsearch-searchautocomplete\" ></input></div></div>";

    rootElement.find(".c4i-esgfsearch-selectfacet-container").html(tabPropertySelector);
    
    rootElement.find(".c4i-esgfsearch-property-container").show();
    var noPropertyChooser = true;
    
    if(propertyChooser[facetName]){
      if(! propertyTabMenu[facetName]){
        propertyTabMenu[facetName] = "quickmenu";
      }
      
      noPropertyChooser = false;
      rootElement.find(".c4i-esgfsearch-selectfacet-container").prepend(
        "<div class=\"c4i-esgfsearch-property-menu c4i-esgfsearch-tabcontainer\">"+
          propertyChooser[facetName].html+
        "</div>"
      );
      var numprops = propertyChooser[facetName].init(rootElement,facetName,facetList,query,_this.addFilterProperty);
      
      var onlyquickselect = false;
      if(propertyChooser[facetName].config.onlyquickselect===true){
        onlyquickselect  = true;
      }
      
      if(onlyquickselect == true){
        rootElement.find(".c4i-esgfsearch-selectfacet-container").prepend(
          "<div class=\"c4i-esgfsearch-tabmain\">"+
          "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-tab-menu c4i-esgfsearch-tab-selected\">"+_getFacetName(facetName)+"</div>"+
          "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-noselect c4i-esgfsearch-tab-collapse c4i-esgfsearch-tab-selected\">^</div>"+
          "</div>"
        );
         rootElement.find(".c4i-esgfsearch-property-container").hide();
         
         propertyTabMenu[facetName] = "quickmenu";
         
      }else{
        rootElement.find(".c4i-esgfsearch-selectfacet-container").prepend(
          "<div class=\"c4i-esgfsearch-tabmain\">"+
          "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-noselect c4i-esgfsearch-tab-menu\">Quick select "+_getFacetName(facetName)+"</div>"+
          "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-noselect c4i-esgfsearch-tab-properties\">All "+_getFacetName(facetName)+" properties ("+esgfSearchGetKeys(facetList).length+")</div>"+
          "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-noselect c4i-esgfsearch-tab-collapse c4i-esgfsearch-tab-selected\">^</div>"+
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
          "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-tab-properties c4i-esgfsearch-tab-selected\">"+_getFacetName(facetName)+" ("+esgfSearchGetKeys(facetList).length+")</div>"+
          "<div class=\"c4i-esgfsearch-tab c4i-esgfsearch-noselect c4i-esgfsearch-tab-collapse c4i-esgfsearch-tab-selected\">^</div>"+
          "</div>"
        );
    }
  
    
    var showPropertyTab = function(){
      rootElement.find(".c4i-esgfsearch-property-menu").hide();
      rootElement.find(".c4i-esgfsearch-tab-menu").removeClass("c4i-esgfsearch-tab-selected");
      rootElement.find(".c4i-esgfsearch-tab-collapse").html("^");
      
      
      
      
      rootElement.find(".c4i-esgfsearch-property-container").show();
      rootElement.find(".c4i-esgfsearch-tab-properties").addClass("c4i-esgfsearch-tab-selected");
      propertyTabMenu[facetName]= "allproperties";
    };
    
    var showQuickSelect = function(){
      rootElement.find(".c4i-esgfsearch-property-container").hide();
      rootElement.find(".c4i-esgfsearch-tab-properties").removeClass("c4i-esgfsearch-tab-selected");
      rootElement.find(".c4i-esgfsearch-tab-collapse").html("^");
      
      
      
      
      rootElement.find(".c4i-esgfsearch-property-menu").show();
      rootElement.find(".c4i-esgfsearch-tab-menu").addClass("c4i-esgfsearch-tab-selected");
      propertyTabMenu[facetName] = "quickmenu";
    };
    
    if(propertyTabMenu[facetName] == "allproperties"){
      showPropertyTab();
    }
    
    rootElement.find(".c4i-esgfsearch-tab-properties").attr('onclick','').click(function(evt){showPropertyTab();});
    
    rootElement.find(".c4i-esgfsearch-tab-menu").attr('onclick','').click(function(evt){
      showQuickSelect();
    });
    
    rootElement.find(".c4i-esgfsearch-tab-collapse").attr('onclick','').click(function(evt){
      if(rootElement.find(".c4i-esgfsearch-tab-collapse").html() == "+"){
        if(propertyTabMenu[facetName] == "allproperties" || !propertyTabMenu[facetName]){
          showPropertyTab();
        }else if(propertyTabMenu[facetName] == "quickmenu"){
          showQuickSelect();
        }
      }else{
        rootElement.find(".c4i-esgfsearch-property-container").hide();
        rootElement.find(".c4i-esgfsearch-property-menu").hide();
        /*rootElement.find(".c4i-esgfsearch-tab-properties").removeClass("c4i-esgfsearch-tab-selected");
        rootElement.find(".c4i-esgfsearch-tab-menu").removeClass("c4i-esgfsearch-tab-selected");*/
        
        rootElement.find(".c4i-esgfsearch-tab-collapse").html("+");
//         rootElement.find(".c4i-esgfsearch-tab-collapse").addClass("c4i-esgfsearch-tab-selected");
      }
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
              if(descriptions[i] == ui.item.value){
                el =rootElement.find("span[name=\""+i+"\"]");
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
    getPropertiesForFacetName({name:facetName,
      callback:function(facetList){
        currentFacetList = facetList;
        currentSelectedFacet=facetName;
        generatePropertyListSelector(facetList,facetName);
      }
    });
  };
  
  this.addFilterProperty = function(facet,property){
    currentPage = 1;
    var k = new ESGFSearch_KVP(query);
    var kvps = k.getKeyValues();
    query = "";
    
    for(var kvp in kvps){
      for(var vkey in kvps[kvp]){
        var v= kvps[kvp][vkey];
        //if(!(kvp==facet&&property==v)){
        if(facet=="time_start_stop"||facet=="bbox"||facet=="query"){
          if(kvp!=facet){          
            query+=kvp+"="+(v)+"&";
          }
        }else{
          query+=kvp+"="+(v)+"&";
        }
        //}
      }
    }
    if(property){
      query+=facet+"="+(property)+"&";
    }
     
     window.location.hash = query;
     
    if(isCtrl == false){
//      showFilters();
      getAllFacets();
    }
  };
  var removeFilterProperty = function(facet,property){
    currentPage = 1;
    //console.log("Remove");
    var k = new ESGFSearch_KVP(query);
    var kvps = k.getKeyValues();
    query = "";
    for(var kvp in kvps){
      for(var vkey in kvps[kvp]){
        var v= kvps[kvp][vkey];
        if(isDefined(property)){
          if(!(kvp==facet&&property==v)){
            query+=kvp+"="+(v)+"&";
          }
        }else{
           if(!(kvp==facet)){
            query+=kvp+"="+(v)+"&";
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

  var checkForErrors = function(data,subject){
  
    if(data.exception){
      var message ="";
      if(data.exception)message    +="\n\n<div class=\"c4i-esgfsearch-exception\">Error "+data.exception+"</div>";
      if(subject)message    +="\n\n<div class=\"c4i-esgfsearch-subject\">Trying to do: "+subject+"</div>";
      if(data.url)message    +="\n\n<div class=\"c4i-esgfsearch-url\">URL: <a href=\""+data.url+"\">"+data.url+"</a></div>";
      message    +="\n\n<div class=\"c4i-esgfsearch-helpcontact\">If the problem persists, please use the contact form to indicate that the search is broken.</div>";
      alert(message);
      return;
    }
    if(data.error){
      alert(data.error);
      return;
    }
    return 0;
  };
  
  var _getAllFacets = function(args,ready){
    showFilters();

    rootElement.find(".c4i-esgfsearch-results").parent().block();
    
    
    
    var callback = function(result){
      if(checkForErrors(result,"GetAllFacets")!=0){
        rootElement.find(".c4i-esgfsearch-results").parent().unblock();
        return;
      }
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
      var facetNr = 0;
      for(var facetkey in facets){
        count =0;
        for(var property in facets[facetkey]){
          count++;
        }
        if(facetkey=="time_start_stop"||facetkey=="bbox"||facetkey=="query"){
          count=-1;
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
        facet+="\">"+_getFacetName(facetkey);
        if(count>0)facet+=" ("+count+")";
        facet+="</span>";
        
        //if(!selectedFacets[facetkey]){
          if(count>0||count==-1){
//            if(facetNr>0){
//              html+="/";
//            }
            html+=facet;
            facetNr++;
          }
        //}
          
      }
      
      if(!listAllFacets){
        html+="<span name=\"showall\" class=\"c4i-esgfsearch-listallfacets c4i-esgfsearch-facets c4i-esgfsearch-facets-selectmoreless c4i-esgfsearch-roundedborder\">&gt; show all filters</span>";
      }else{
         html+="<span name=\"showdefault\" class=\"c4i-esgfsearch-listallfacets c4i-esgfsearch-facets c4i-esgfsearch-facets-selectmoreless c4i-esgfsearch-roundedborder\">^ show less filters</span>";
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

    var url=impactESGFSearchEndPoint+"service=search&request=getfacets&query="+encodeURIComponent(query)+"&pagelimit=25&pagenumber="+(currentPage-1);
    $.ajax({
      url: url,
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
   
      if(e.keyCode!=17){
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
