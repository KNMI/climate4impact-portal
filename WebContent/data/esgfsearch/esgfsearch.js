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

var setVarFilter = function(){
	alert('wrong');
};

var SearchInterface = function(options){
  //var impactESGFSearchEndPoint = "http://bhw485.knmi.nl:8280/impactportal/esgfsearch?";
  var impactESGFSearchEndPoint = "esgfsearch?";
  
  var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";
  
  var rootElement = null;
  


  /**
   * Class to make Ajax Calls which will complete in the same order as called.
   * 
   * How it works: 
   * 1) Make a new object of this class (e.g. var a = new AsyncFifo();
   * 2) Make your own ajax function with two arguments: the first is for passing arguments and the second is called when you finished
   *    e.g. var myfunction = function(myargs,ready){};
   * 3) Pass your function with your arguments to AsyncFifo like this : a.call(myfunction,myargs);
   * 4) Everything will now be called in the right order, one call at a time!
   */
  function AsyncFifo(){
    var calls = [];
    var busy = false;
    this.call = function(fnToCall,args){
      calls.push(args);
      if(busy === true)return;
      busy = true;
      function go(){
        if(calls.length == 0){
          busy = false;
        }else{
          var f = calls.shift();
          try{
            fnToCall(f,go);
          }catch(e){
            console.log("Something when wrong in AsyncFifo:");
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
    $.blockUI.defaults.message='<img src="esgfsearch/ajax-loader.gif"/>';
    $.blockUI.defaults.css.border='none';
    $.blockUI.defaults.overlayCSS.backgroundColor="white";
    rootElement = options.element;
    options.element.html('<div class="searchCompContainer">'+
        '  <div class="searchComp facetOverview">'+
        '    <div class="searchCompHeader">Filters</div>'+
        '    <div class="searchCompBody"></div>'+
        '    <div class="searchCompFooter"></div>'+
        '  </div>'+
        ''+
        '  <div class="searchComp selectedElements">'+
        '    <div class="searchCompHeader">Selected filters</div>'+
        '    <div class="searchCompBody"></div>'+
        '    <div class="searchCompFooter"></div>'+
        '  </div>'+
        ''+
        '  <div class="searchComp searchResults">'+
        '    <div class="searchCompHeader">Results</div>'+
        '    <div class="searchCompBody"></div>'+
        '    <div class="searchCompFooter"></div>'+
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
	  el.html('<div class="ajaxloader"></div>');
	  var helpReturned = function(data){
		  el.html(data);	  
	  }
	  $.ajax({
	      url: "esgfsearch/esgfsearchhelp.html"	    
	  }).done(function(d) {
		  helpReturned(d)
	  })
   });
    
    addFilter();
  };
  
  var checkResponseFifo = new AsyncFifo();
  
  var recentlyCheckedResponses = [];
  
  var _checkResponse = function(arg,ready){
    var setResult = function(arg,a){
       var el = rootElement.find("span[name=\""+arg+"\"]").first();
       el.removeClass("resultItemChecking");
       if(a.ok=="ok"){
        el.addClass("resultItemOk");
        recentlyCheckedResponses[arg]=a;
        el.children().first().html("");
      }else{
        el.addClass("resultItemWrong");
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
    
    $.ajax({
      url: impactESGFSearchEndPoint+"service=search&request=checkurl&query="+encodeURIComponent(arg),
      crossDomain:true,
      dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      alert("fail 154");
      httpCallback("Failed for "+arg);
    }).always(function(){
      if(ready){
        ready();
      }
    });
    
  };
  
  var checkResponses = function(data){
    checkResponseFifo.stop();
    for(var r in data.response.results){
      checkResponseFifo.call(_checkResponse,data.response.results[r].url);
    }
  };
  
  var showResponse = function(data){
    var limit = data.response.limit;
    if(limit>data.response.numfound)limit = data.response.numfound;
    var html="";
    rootElement.find(".searchResults").find(".searchCompHeader").html("Datasets: Found "+data.response.numfound+", displaying "+limit+" of "+ data.response.numfound+" results.");
    function getPageName(url) {
      var index = url.lastIndexOf("/") + 1;
      var extensionIndex = url.lastIndexOf(".")-index;
      var filename = url.substr(index,extensionIndex);
      //filename = filename.replace(/\./g," ");
      return filename;          
    }
    html+="<div class=\"resultList roundborder\">";
    for(var r in data.response.results){
      html+="<span class=\"resultItem resultSelector resultItemChecking\" name=\""+data.response.results[r].url+"\">"+data.response.results[r].id+" <span>checking...</span></span>";
    }
    html+="</div>";
    rootElement.find(".searchResults").find(".searchCompBody").first().html(html);
    
    rootElement.find(".resultItem").attr('onclick','').click(function(t){
  	  var clickedURL = $(this).attr("name");
  	  var catalogObject = null;
  	  for(var r in data.response.results){
  	    if(clickedURL == data.response.results[r].url){
  	      catalogObject = data.response.results[r];
  	      break;
  	    }
  	  }
  	  if(catalogObject == null){
  	    alert("Internal error at esgfsearch.js at line 173");
  	  }

      if($(this).hasClass("facetSelected")){
        $(this).removeClass("facetSelected");
      }else{
        $(this).addClass("facetSelected");
      }
      
      var el = jQuery('<div></div>', {
        title: 'Dataset '+catalogObject.id,
      }).dialog({
        width:900,
        height:500
      });
      
      var renderCatalogBrowser = function(options){
    	  var variableFilter = undefined;
  		  var textFilter = undefined;
     	  var applyCatalogFilters = function(el){
        		variableFilter = '';
        		el.find("form.varFilter :input[type=checkbox]").each(function(){
        			 var input = $(this);
        			 if(input.is(":checked")){
        				 if(variableFilter.length>0)variableFilter+="|";
        				 variableFilter+=input.attr('id');
        			 }
        			
        		});
        		if(variableFilter.length==0)variableFilter = undefined;
        		
        		textFilter = '';
        		textFilter = el.find(".textfilter").val();
        		if(textFilter.length<1)textFilter = undefined;
        		
        		loadCatalog(options,variableFilter,textFilter);
        	};
      	  
    	var preventSubmit = function(event) {
  	        if(event.keyCode == 13) {
  	            event.preventDefault();
  	            setVarFilter();
  	            return false;
  	        }
  	    };
    	  
    	  var loadCatalog = function(options,variableFilter,textFilter){
    		  options.element.empty().html('<div class="ajaxloader"></div>');
    		  
    		  var httpCallback = function(data){
    			
        		  options.element.empty().html(data);
        		  
        		  options.element.find(".varFilter").keypress(preventSubmit);
        		  options.element.find(".varFilter").keydown(preventSubmit);
        		  options.element.find(".varFilter").keyup(preventSubmit);
        		  
        		  options.element.find(".varFilter").find('input[type="button"]').attr('onclick','').click(function(){applyCatalogFilters(options.element);});
        		  options.element.find('a').attr('target','_blank');
        	  };
    		  
    		  var url = "/impactportal/ImpactService?service=catalogbrowser&format=text/html&node="+encodeURIComponent(options.url);
    		  var filters = "";
    		  if(textFilter!=undefined){filters+="&filter="+encodeURIComponent(textFilter);}
 	  	  	  if(variableFilter!=undefined){filters+="&variables="+encodeURIComponent(variableFilter);}
 	  	  	  url+=filters;
 	  	  	  console.log(url);
    		  $.ajax({
		        url:  url
		        //crossDomain:true,
		        //dataType:"jsonp"
		      }).done(function(d) {
		        httpCallback(d)
		      }).fail(function() {
		    	  httpCallback("Failed");
		      }).always(function(){
		      });
    	  }
    	  loadCatalog(options);
    	  
   
       
      }
      
      renderCatalogBrowser({element:el,url:catalogObject.url});
    });
    
    checkResponses(data);
  };
  
  var facetFIFO = new AsyncFifo();
  
  var getFacetsForFacetName = function(arg){
    facetFIFO.call(_getFacetsForFacetName,arg);
  };
  
  var _getFacetsForFacetName = function(args,ready){
      var name = args.name;
      var callback= args.callback;
    
      var k = new KVP(query);
      
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
  
  
  /**
   * Happens when something inside a facet is clicked.
   */
  
  var elementSelectorClick = function(id){
  //  console.log("elementSelectorClick:"+$(this).attr("name"));
  };
  
  /**
   * Happens when a category, e.g. standard_name, model, experiment is clicked
   */
  var facetClick = function(container,name){
  
   var facetselectordiv=container.find(".searchSelectFacet");
   
   
  
  
  // facetselectordiv.addClass("facetLoading");
   //facetselectordiv.html("Loading "+name);
   
   facetselectordiv.block();//setLoading();
   
   var callback = function(facetList){
    var html="";
    var even = 0;
    for(var i in facetList){
      if(even == 0)oddEvenClass="elementSelectorEven";else oddEvenClass="elementSelectorOdd"
      
      html+="<span name=\""+facetList[i]+"\" class=\"elementSelector "+oddEvenClass+"\">"+facetList[i]+"</span>";
      even = 1-even;
    }
    var autocomplete = "Search: <input class=\"searchautocomplete\" =\"text\"></input>";
  
    var appliedElements = "<div>&nbsp;<span class=\"selectedElementList\"></span></div>";
    var applyButton = "<button class=\"button_elementselectorok\">Search</button>";
    facetselectordiv.html("<div class=\"elementSelectorContainer roundborder\">"+html+"</div>"+"<div style=\"padding:5px;margin-bottom:5px;\">"+appliedElements+"<hr/>"+autocomplete+applyButton+"</div>");
    
    rootElement.find(".button_elementselectorok").attr('onclick','').click(function(t){
      selectedElementsForFacet();
    });
    
    var selectElement = function(element){
      if(element.hasClass("elementSelected")){
        element.removeClass("elementSelected");
      }else{
        element.addClass("elementSelected");
      }
      elementSelectorClick(element);
      
      
      var selEl = rootElement.find(".elementSelected");

      
      var subquery="";
      
      var html="";
      for(var j=0;j<selEl.length;j++){
        if(j>0)html+="+";
        html+="<span class=\"facets roundborder\">"+$(selEl[j]).attr("name")+"</span>";
        
      }
      rootElement.find(".selectedElementList").first().html(html);
      
      
      
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
  
    rootElement.find(".elementSelector").attr('onclick','').click(function(t){
      selectElement($(this));
    });
      
   };
   getFacetsForFacetName({name:name,callback:callback});
  };
  
  var addFilter = function(){
    //showFilters();
    getAllFacets();
  };
  var removeFilter = function(facet,element){
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
            html+="<span class=\"facets roundborder\">"+kvp+" : "+decodeURIComponent(v)+"&nbsp;<button class=\"button_removefilter\" name=\""+kvp+","+v+"\">X</button></span>";
          }
        }
        if(html.length==0){
          html="none";
        }
        //html+=getFilterResultsButton();
        var el = rootElement.find(".selectedElements").find(".searchCompBody").first();
        el.html(html);
        el.find(".button_removefilter").attr('onclick','').click(function(t){
          var facetAndValue = ($(this).attr('name')).split(",");
          removeFilter(facetAndValue[0],facetAndValue[1]);
        });
        
        
  
  };
  
  var selectedElementsForFacet = function(){
    var selEl = rootElement.find(".elementSelected");
    var selFacet = rootElement.find(".facetSelected").attr("name");
    var subquery="";
    for(var j=0;j<selEl.length;j++){
      subquery+=selFacet+"="+encodeURIComponent($(selEl[j]).attr("name"))+"&";
    }
    query+=subquery;
    addFilter();
  };
  
  var getAllFacetsFIFO = new AsyncFifo();

  var getAllFacets = function(){
    getAllFacetsFIFO.call(_getAllFacets);
  };
  
  var _getAllFacets = function(args,ready){
      showFilters();
      rootElement.find(".facetOverview").find(".searchCompBody").first().block();//('<div class="overlay"></div>');
      var callback = function(result){
        showResponse(result);
        var d= result.facets;
        var count;
        var html = "";
        for(var a in d){
          count =0;
          for(var b in d[a]){
            count++;
          }
          if(count>1){
            html+="<span name=\""+a+"\" class=\"clickablefacet facets roundborder\">"+a+" ("+count+")</span>";
          }else if(count>0){
            html+="<span name=\""+a+"\" class=\"clickablefacet facetDisabled facets roundborder\">"+a+" ("+count+")</span>";
          }
        }
        html+="<div class=\"searchSelectFacet\"></div>";
        html+="</div>";
        
        rootElement.find(".facetOverview").find(".searchCompBody").first().html(html);
        
        showFilters();
        
        rootElement.find(".clickablefacet").attr('onclick','').click(function(t){
          rootElement.find(".clickablefacet").removeClass("facetSelected");
          $(this).addClass("facetSelected");
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
  
  this.renderSearchInterface(options);
};
