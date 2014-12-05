
(function($){
    $.fn.extend({ 
    setLoading : function(config) {
      var t= this;
      var p = t.position();
      
        
      var overlay=  jQuery('<div/>', {
        "class":"overlay",
        width:t.width(),
        height:t.height()
      }).css({left:p.left,top:p.top});
      
      this.append(overlay);  
      var x = (parseInt(t.width())/2)-16;
      var y = (parseInt(t.height())/2)-16;
      
      jQuery('<div></div>', {
        width:32,
        height:32,
        html:'<img src="ajax-loader.gif"/>'
      }).css({left:x,top:y,position:'absolute'}).appendTo(overlay);
      
      
    }
    })
})(jQuery);

//var impactESGFSearch = "http://bhw485.knmi.nl:8280/impactportal/esgfsearch?";
var impactESGFSearch = "/impactportal/esgfsearch?";

var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";

/**
 * Class to make Ajax Calls in the right order.
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

var MySet = function() {}
MySet .prototype.add = function(o) { this[o] = true; }
MySet .prototype.remove = function(o) { delete this[o]; }


var checkResponseFifo = new AsyncFifo();

var recentlyCheckedResponses = [];

var _checkResponse = function(arg,ready){
  var setResult = function(arg,a){
     var el = $("span[name=\""+arg+"\"]").first();
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
    //console.log("span[name=\""+arg+"\"]");

    setResult(arg,a);
   
   
  };
  $.ajax({
    url: impactESGFSearch+"service=search&request=checkurl&query="+encodeURIComponent(arg),
    crossDomain:true,
    dataType:"jsonp"
  }).done(function(d) {
    httpCallback(d)
  }).fail(function() {
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
    checkResponseFifo.call(_checkResponse,data.response.results[r]);
  }
};

var showResponse = function(data){
  var limit = data.response.limit;
  if(limit>data.response.numfound)limit = data.response.numfound;
  var html="";
  $("#searchResults").find(".searchCompHeader").html("Datasets: Found "+data.response.numfound+", displaying "+limit+" of "+ data.response.numfound+" results.");
  function getPageName(url) {
    var index = url.lastIndexOf("/") + 1;
    var extensionIndex = url.lastIndexOf(".")-index;
    var filename = url.substr(index,extensionIndex);
    //filename = filename.replace(/\./g," ");
    return filename;          
  }
  html+="<div class=\"resultList roundborder\">";
  for(var r in data.response.results){
    html+="<span class=\"resultItem resultSelector resultItemChecking\" name=\""+data.response.results[r]+"\">"+getPageName(data.response.results[r])+" <span>checking...</span></span>";
  }
  html+="</div>";
  $("#searchResults").find(".searchCompBody").first().html(html);
  
  $(".resultItem").attr('onclick','').click(function(t){
	  
    //$(".resultItem").removeClass("facetSelected");
    if($(this).hasClass("facetSelected")){
      $(this).removeClass("facetSelected");
    }else{
      $(this).addClass("facetSelected");
    }
    var newLoc =("/impactportal/data/catalogbrowser.jsp?catalog="+encodeURIComponent($(this).attr("name")));
    window.open(newLoc);
    //window.location.href = "http://stackoverflow.com";
    //facetClick($(this).parent(),$(this).attr("name"));
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
      url: impactESGFSearch+"service=search&request=getfacets&query="+encodeURIComponent(query),
      crossDomain:true,
      dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
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
  console.log("elementSelectorClick:"+$(this).attr("name"));
};

/**
 * Happens when a category, e.g. standard_name, model, experiment is clicked
 */
var facetClick = function(container,name){

 var facetselectordiv=container.find(".searchSelectFacet");
 var selectedFacet=$("#facet_"+name);
 


// facetselectordiv.addClass("facetLoading");
 //facetselectordiv.html("Loading "+name);
 
 facetselectordiv.setLoading();
 
 var callback = function(facetList){
  var html="";
  var even = 0;
  for(var i in facetList){
    if(even == 0)oddEvenClass="elementSelectorEven";else oddEvenClass="elementSelectorOdd"
    
    html+="<span name=\""+facetList[i]+"\" class=\"elementSelector "+oddEvenClass+"\">"+facetList[i]+"</span>";
    even = 1-even;
  }
  var autocomplete = "Search: <input id=\"autocomplete\" =\"text\"></input>";

  var appliedElements = "<div>&nbsp;<span id=\"selectedElementList\"></span></div>";
  var collapseButton = "";//<button class=\"button_facetselectorclose\"  onclick=\"addFilter();\">Close</button>";
  var applyButton = "<button class=\"button_elementselectorok\" onclick=\"selectedElementsForFacet();\">Add filter</button>";
  facetselectordiv.html("<div class=\"elementSelectorContainer roundborder\">"+html+"</div>"+"<div style=\"padding:5px;margin-bottom:5px;\">"+appliedElements+"<hr/>"+autocomplete+applyButton+collapseButton+"</div>");
  
  var selectElement = function(element){
    if(element.hasClass("elementSelected")){
      element.removeClass("elementSelected");
    }else{
      element.addClass("elementSelected");
    }
    elementSelectorClick(element);
    
    
    var selEl = $(".elementSelected");
    //var selFacet = $(".facetSelected").attr("name");
    
    var subquery="";
    //console.log("You selected:");
    var html="";
    for(var j=0;j<selEl.length;j++){
      //subquery+=selFacet+"="+encodeURIComponent($(selEl[j]).attr("name"))+"&";
      //console.log($(selEl[j]).attr("name"));
      if(j>0)html+="+";
      html+="<span class=\"facets roundborder\">"+$(selEl[j]).attr("name")+"</span>";
      
    }
    $("#selectedElementList").html(html);
    
    
    
  };
  
  $( "#autocomplete" ).autocomplete({
    source: function( request, response ) {
    var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( request.term ), "i" );
    response( $.grep( facetList, function( item ){
      
    return matcher.test( item );
    }) );
    },
    select:function(event,ui){
      var el =$("span[name=\""+ui.item.value+"\"]");
      selectElement(el.first());
    }
  });

  $(".elementSelector").attr('onclick','').click(function(t){
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
      //console.log("Comparing "+kvp+" == "+facet);
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
          html+="<span class=\"facets roundborder\">"+kvp+" : "+decodeURIComponent(v)+"&nbsp;<button class=\"button_removefilter\" onclick=\"removeFilter('"+kvp+"','"+v+"')\">X</button></span>";
        }
      }
      if(html.length==0){
        html="none";
      }
      html+=getFilterResultsButton();
      $("#selectedElements").find(".searchCompBody").first().html(html);

};

var getFilterResultsButton = function(){
  if($("#facetOverview").find(".searchCompBody").first().html().length==0){
    return "<div class=\"div_filterresults\">&nbsp;<button onclick=\"addFilter();\" class=\"button_filterresults\">Show filters</button></div>";
  }else{
    return "";
  }
};

var selectedElementsForFacet = function(){
  var selEl = $(".elementSelected");
  var selFacet = $(".facetSelected").attr("name");
  
  var subquery="";
  for(var j=0;j<selEl.length;j++){
    subquery+=selFacet+"="+encodeURIComponent($(selEl[j]).attr("name"))+"&";
  }
  
  
  

  //console.log(subquery);
  //$(this).parent().parent().html("<button onclick=\"addFilter();\">Add filter</button>");
  
  query+=subquery;
  
  addFilter();
}


var getAllFacetsFIFO = new AsyncFifo();
var getAllFacets = function(){
  getAllFacetsFIFO.call(_getAllFacets);
};

var _getAllFacets = function(args,ready){
  
    

    showFilters();
    $("#facetOverview").find(".searchCompBody").first().setLoading();//('<div class="overlay"></div>');
    
  // showFilters();
    
    var callback = function(result){
      
      showResponse(result);
      
      var d= result.facets;
      var count;
     
      var html = "";

      for(var a in d){
        count =0;
        for(var b in d[a]){
          count++;//=parseInt(d[a][b]);
        }
        if(count>1){
          html+="<span name=\""+a+"\" class=\"clickablefacet facets roundborder\">"+a+" ("+count+")</span>";
        }else if(count>0){
          html+="<span name=\""+a+"\" class=\"clickablefacet facetDisabled facets roundborder\">"+a+" ("+count+")</span>";
        }
        //console.log(a+count);
      }
      html+="<div class=\"searchSelectFacet\"></div>";
      html+="</div>";
      
      
      
      
      $("#facetOverview").find(".searchCompBody").first().html(html);
      
      showFilters();
      
      $(".clickablefacet").attr('onclick','').click(function(t){
        $(".clickablefacet").removeClass("facetSelected");
        $(this).addClass("facetSelected");
        facetClick($(this).parent(),$(this).attr("name"));
      });
      

      
    };
    
    
    $.ajax({
      url: impactESGFSearch+"service=search&request=getfacets&query="+encodeURIComponent(query),
      crossDomain:true,
      dataType:"jsonp"
    }).done(function(d) {
      callback(d);
    }).fail(function() {
      alert( "error" );
    }).always(function(){
      ready();
    });
 
    
  
};