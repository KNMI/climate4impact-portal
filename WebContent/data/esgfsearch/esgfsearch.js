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



var SearchInterface = function(options){
  var _this = this;
  //var impactESGFSearchEndPoint = "http://bhw485.knmi.nl:8280/impactportal/esgfsearch?";
  var impactESGFSearchEndPoint = "esgfsearch?";
  
  var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";
  query="";//data_node=albedo2.dkrz.de&experiment=rcp45&project=CMIP5&time_frequency=day&variable=tas&model=EC-EARTH&";
  var rootElement = null;
  
  
  
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
  * Splits a url into key value pairs.
  */
  function KVP(query){
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
            if(!kvplist[key])kvplist[key] = [];
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
  
  /**
  * A normal unordered set, e.g. a list containing no duplicates.
  */
  var MySet = function() {}
  MySet .prototype.add = function(o) { this[o] = true; }
  MySet .prototype.remove = function(o) { delete this[o]; }
  

  
  this.renderSearchInterface = function(options){
    $.blockUI.defaults.message='<div class="esgfsearch-loader"></div>';
    $.blockUI.defaults.css.border='none';
    $.blockUI.defaults.overlayCSS.backgroundColor="white";
    rootElement = options.element;
    options.element.html('<div class="simplecomponent-container">'+
    '  <div class="simplecomponent esgfsearch-facetoverview">'+
    '    <div class="simplecomponent-header">Filters</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent esgfsearch-selectedelements">'+
    '    <div class="simplecomponent-header">Selected filters</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent esgfsearch-results">'+
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
      el.html('<div class="esgfsearch-loader"></div>');
      var helpReturned = function(data){
        el.html(data);    
      }
      $.ajax({
        url: "esgfsearch/esgfsearchhelp.html"     
      }).done(function(d) {
        helpReturned(d)
      })
    });
    
    addFilterProperty();
  };
  
 
  
  var recentlyCheckedResponses = [];
  
  var _checkResponse = function(arg,ready){
    var setResult = function(arg,a){
      var el = rootElement.find("span[name=\""+arg+"\"]").first();
      el.removeClass("esgfsearch-resultitem-checking");
      if(a.ok=="ok"){
        el.addClass("esgfsearch-resultitem-ok");
        recentlyCheckedResponses[arg]=a;
        el.children().first().html("");
      }else{
        el.addClass("esgfsearch-resultitem-wrong");
        el.children().first().html(a.message);
        recentlyCheckedResponses[arg]=false;
      }
    }
    if(recentlyCheckedResponses[arg]){
      setResult(arg,recentlyCheckedResponses[arg]);
      ready();
      return;
    }
    
    var httpCallback = function(a){
      setResult(arg,a);
    };
    
    var url = impactESGFSearchEndPoint+"service=search&request=checkurl&query="+encodeURIComponent(arg);
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
    for(var r in data.response.results){
      checkResponseFifo.call(data.response.results[r].url);
    }
  };
  
  var addToBasket = function(){
    var el = jQuery('<div></div>', {
        title: 'Dataset',
      }).dialog({
        width:450,
        height:400
      });
      el.html('<div class="esgfsearch-loader"></div>');
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
    
    var limit = data.response.limit;
    if(limit>data.response.numfound)limit = data.response.numfound;
    var html="";
    rootElement.find(".esgfsearch-results").find(".simplecomponent-header").html("Datasets: Found "+data.response.numfound+", displaying "+limit+" of "+ data.response.numfound+" results.");
    function getPageName(url) {
      var index = url.lastIndexOf("/") + 1;
      var extensionIndex = url.lastIndexOf(".")-index;
      var filename = url.substr(index,extensionIndex);
      //filename = filename.replace(/\./g," ");
      return filename;          
    };
    html+="<div class=\"esgfsearch-resultlist esgfsearch-roundedborder\">";
    for(var r in data.response.results){
      html+="<span class=\"esgfsearch-resultitem resultSelector esgfsearch-resultitem-checking\" name=\""+data.response.results[r].url+"\">"+data.response.results[r].id+" <span>checking...</span></span>";
    }
    html+="</div>";
    var addToBasketButton = "<button class=\"button_addtobasket\">Add results to basket</button>";
    //html+="<div style=\"clear: both;\">"+addToBasketButton+"<br/></div>";
    rootElement.find(".esgfsearch-results").find(".simplecomponent-body").first().html(html);
    
    rootElement.find(".button_addtobasket").button().attr('onclick','').click(function(t){
      addToBasket();
    });
    
    rootElement.find(".esgfsearch-resultitem").attr('onclick','').click(function(t){
      var clickedURL = $(this).attr("name");
      var catalogObject = null;
      for(var r in data.response.results){
        if(clickedURL == data.response.results[r].url){
          catalogObject = data.response.results[r];
          break;
        }
      }
      if(catalogObject == null){
        alert("Catalog contains no valid URL / link.");
        //alert("Internal error at esgfsearch.js at line 173");
        return;
      }
      
      if($(this).hasClass("esgfsearch-facetselected")){
        $(this).removeClass("esgfsearch-facetselected");
      }else{
        $(this).addClass("esgfsearch-facetselected");
      }
      
      var el = jQuery('<div></div>', {
        title: 'Dataset '+catalogObject.id,
      }).dialog({
        width:950,
        height:600
      });
      
      
      
      renderCatalogBrowser({element:el,url:catalogObject.url});
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
    //  console.log("propertyClick:"+$(this).attr("name"));
  };
  
  /**
  * Happens when a facet, e.g. standard_name, model, experiment is clicked
  */
  var facetClick = function(container,name){
    
    var facetselectordiv=container.find(".esgfsearch-selectfacet");
    
    
    
    
    // facetselectordiv.addClass("facetLoading");
    //facetselectordiv.html("Loading "+name);
    
    facetselectordiv.block();//setLoading();
    
    var callback = function(facetList){
      var html="";
      var even = 0;
      for(var i in facetList){
        if(even == 0)oddEvenClass="esgfsearch-property-even";else oddEvenClass="esgfsearch-property-odd"
          
          html+="<span name=\""+facetList[i]+"\" class=\"esgfsearch-property "+oddEvenClass+"\">"+facetList[i]+"</span>";
        even = 1-even;
      }
      var autocomplete = "Filter: <input class=\"searchautocomplete\" =\"text\"></input>";
      
      var appliedElements = "<div>&nbsp;<span class=\"esgfsearch-selectedelementlist\"></span></div>";
      var applyButton = "<button class=\"esgfsearch-property-buttonapply\">Apply</button>";
      facetselectordiv.html("<div style=\"margin:0 0 4px 5px;\">"+autocomplete+"</div><div class=\"esgfsearch-property-container esgfsearch-roundedborder\">"+html+"</div>"+"<div style=\"padding:5px;margin-bottom:5px;\">"+appliedElements+"<hr/><div style=\"clear: both;\">"+applyButton+"<br/></div></div>");
      
      rootElement.find(".esgfsearch-property-buttonapply").button().attr('onclick','').click(function(t){
        selectedPropertiesForFacet();
      });
      
      var selectElement = function(element,alwaysselect){
        if(element.hasClass("esgfsearch-property-selected")){
          if(alwaysselect !== true){
            element.removeClass("esgfsearch-property-selected");
          }
        }else{
          element.addClass("esgfsearch-property-selected");
        }
        propertyClick(element);
        
        
        var selEl = rootElement.find(".esgfsearch-property-selected");
        
        
        var subquery="";
        
        var html="";
        for(var j=0;j<selEl.length;j++){
          if(j>0)html+="+";
          html+="<span class=\"esgfsearch-facets esgfsearch-roundedborder\">"+$(selEl[j]).attr("name")+"</span>";
          
        }
        rootElement.find(".esgfsearch-selectedelementlist").first().html(html);
        
        
        
      };
      
      rootElement.find(".searchautocomplete" ).first().autocomplete({
        source: function( request, response ) {
          var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( request.term ), "i" );
          response( $.grep( facetList, function( item ){
            
            return matcher.test( item );
          }) );
        },
        select:function(event,ui){
          var el =rootElement.find("span[name=\""+ui.item.value+"\"]");
          selectElement(el.first());
        }
      });
      
      rootElement.find(".esgfsearch-property").attr('onclick','').click(function(evt){
        selectElement($(this));
      });
      rootElement.find(".esgfsearch-property").attr('ondblclick','').dblclick(function(evt){
        selectElement($(this),true);
        if (evt.ctrlKey == false){
          selectedPropertiesForFacet();
        }
      });
      
    };
    getPropertiesForFacetName({name:name,callback:callback});
  };
  
  var addFilterProperty = function(){
    //showFilters();
    getAllFacets();
  };
  var removeFilterProperty = function(facet,element){
    //alert(facet+":"+element);
    var k = new KVP(query);
    var kvps = k.getKeyValues();
    query = "";
    for(var kvp in kvps){
      for(var vkey in kvps[kvp]){
        var v= kvps[kvp][vkey];
        if(!(kvp==facet&&element==v)){
          query+=kvp+"="+encodeURIComponent(v)+"&";
        }
      }
    }
    
    showFilters();
    getAllFacets();
  };
  var showFilters = function(){
    var k = new KVP(query);
    var kvps = k.getKeyValues();
    var html = "";
    for(var kvp in kvps){
      for(var vkey in kvps[kvp]){
        var v= kvps[kvp][vkey];
        html+="<span class=\"esgfsearch-facets esgfsearch-roundedborder\">"+kvp+" : "+decodeURIComponent(v)+"&nbsp;<button class=\"esgfsearch-removefilterproperty\" name=\""+kvp+","+v+"\">X</button></span>";
      }
    }
    if(html.length==0){
      html="none";
    }
    //html+=getFilterResultsButton();
    var el = rootElement.find(".esgfsearch-selectedelements").find(".simplecomponent-body").first();
    el.html(html);
    el.find(".esgfsearch-removefilterproperty").attr('onclick','').click(function(t){
      var facetAndValue = ($(this).attr('name')).split(",");
      removeFilterProperty(facetAndValue[0],facetAndValue[1]);
    });
    
    
    
  };
  
  var selectedPropertiesForFacet = function(){
    var selEl = rootElement.find(".esgfsearch-property-selected");
    var selFacet = rootElement.find(".esgfsearch-facetselected").attr("name");
    var subquery="";
    for(var j=0;j<selEl.length;j++){
      subquery+=selFacet+"="+encodeURIComponent($(selEl[j]).attr("name"))+"&";
    }
    query+=subquery;
    addFilterProperty();
  };
  

  var _getAllFacets = function(args,ready){
    showFilters();
    rootElement.find(".esgfsearch-facetoverview").find(".simplecomponent-body").first().block();
    var callback = function(result){
      //Show found results
      showResponse(result);
      
      //result.facets.sort();
      
      var primaryFacets = ["project", "variable", "time_frequency", "experiment", "domain", "models"];
      
      //List corresponding facets
      var facets= [];
      
      for(var j=0;j<primaryFacets.length;j++){
        var key = primaryFacets[j];
       // console.log(key);
        if(result.facets[key]){
          facets[key]=result.facets[key];
        }
      }
      for(var key in result.facets){
        //console.log(key);
        if(!facets[key]){
          facets[key]=result.facets[key];
        }
      }
      var count;
      var html = "";
      for(var facetkey in facets){
        count =0;
        for(var property in facets[facetkey]){
          count++;
        }
        if(count>1){
          html+="<span name=\""+facetkey+"\" class=\"clickablefacet esgfsearch-facets esgfsearch-roundedborder\">"+facetkey+" ("+count+")</span>";
        }else if(count>0){
          html+="<span name=\""+facetkey+"\" class=\"clickablefacet esgfsearch-facetdisabled esgfsearch-facets esgfsearch-roundedborder\">"+facetkey+" ("+count+")</span>";
        }
      }
      html+="<div class=\"esgfsearch-selectfacet\"></div>";
      html+="</div>";
      
      rootElement.find(".esgfsearch-facetoverview").find(".simplecomponent-body").first().html(html);
      
      showFilters();
      
      rootElement.find(".clickablefacet").attr('onclick','').click(function(t){
        rootElement.find(".clickablefacet").removeClass("esgfsearch-facetselected");
        $(this).addClass("esgfsearch-facetselected");
        facetClick($(this).parent(),$(this).attr("name"));
      });
      
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
  
  
  this.renderSearchInterface(options);
};
