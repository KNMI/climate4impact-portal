/**
 * Renders a processing interface to given element.
 * Arguments via:
 * options{
 *  element : the element to render to.
 *  url: the location of the web processing servlet endpoint.
 *  query: predefined query, loads facets in advance.
 * }
 */
var renderProcessingInterface = function(options){
  return new C4IProcessingInterface(options);
};

var xml2jsonrequestURL;

var C4IProcessingInterface = function(options){
  xml2jsonrequestURL = c4iconfigjs.xml2jsonservice+"SERVICE=XML2JSON&";
  var _this = this;
  
  var WPSServiceURL = "";
  var WPSIdentifier = "";
  var WPSDescribeCoverageURL = "";
  var rootElement;
  
  var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";
  query=(window.location.hash).replace("#","");//data_node=albedo2.dkrz.de&experiment=rcp45&project=CMIP5&time_frequency=day&variable=tas&model=EC-EARTH&";
  if (query.length==0) {
    query="clear=onload";
  }
  query = query.replaceAll("&amp;","&");
  //query="variable=tas";
  
  /**
   * Cross browser indexOf function
   */
  var c4iProcessingIndexOf = function(thisobj,obj, start) {
    for (var i = (start || 0), j = thisobj.length; i < j; i++) {
      if (thisobj[i] === obj) { return i; }
    }
    return -1;
  }
  /**
   * Cross browser startsWth function
   */
  
  function c4iProcessingStartsWith(str, prefix) {
    return str.indexOf(prefix) === 0;
  };
  

  /**
   * Cross browser compatible endsWith function 
   */
  function c4iProcessingEndsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
  }
  
  /**
   * Cross browser method to return keys for an object
   */
  
  var c4iProcessingGetKeys = function(obj){
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
  function C4iProcessingKVP(query){
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
  var inputMappings = {
    sliceMode:{
      None:'Use the same period as selected time range',
      year:'Yearly time period',
      month:'Monthly time period',
      ONDJFM:'Half year winter period from October till March',
      AMJJAS:'Half year summer period from April till September',
      DJF:'Winter weather season in December, January & February',
      MAM:'Spring weather season in March, April & May',
      JJA:'Summer weather season in June, July & August',
      SON:'Autumn weather season in September, October & November'
    }
  };
  
  var getInputNameMapping = function(inputname,value){
    //inputmappings
    if(inputname == 'indiceName'){
      var found = false;
      for(var j=0;j<climate_indices_DEF.length;j++){
        if(climate_indices_DEF[j].varname.toLowerCase() == value.toLowerCase()){
          try{
            return value+" - "+climate_indices_DEF[j].output.long_name+' calculated from '+climate_indices_DEF[j].input[0].known_variables;
          }catch(e){
            try{
              return value+" - "+climate_indices_DEF[j].output.long_name;
            }catch(e){
            }
          }
        }
      }
    }
    if(inputMappings[inputname] ){
      var newValue = inputMappings[inputname][value];
      if(newValue) return value+" - "+newValue;
    }
    return value;
  };
  
  
  /**
   * A normal unordered set, e.g. a list containing no duplicates.
   */
  var MySet = function() {}
  MySet .prototype.add = function(o) { this[o] = true; }
  MySet .prototype.remove = function(o) { delete this[o]; }

  
  /*
   * Function to pass errors from ajax calls to
   */
  var error = function(d,url){
        
    var overview = rootElement.find(".c4i-processing-overview").find(".simplecomponent-body").first();
    var inputs = rootElement.find(".c4i-processing-inputs").find(".simplecomponent-body").first();
    var html="AJAX call to server failed.<br/><a target=\"_blank\" href=\""+url+"\">"+url+"</a><br/><br/>";
    if(typeof d === 'object'){
      if(d.error){
        html+="Error: "+d.error+"<br/>";
      }
      if(d.exception){
        html+="Exception: "+d.exception+"<br/>";
      }
      if(d.statuscode){
        html+="Statuscode: "+d.statuscode+"<br/>";
        if(d.statuscode == 401){
          generateLoginDialog(function(){_this.renderProcessingInterface(options);});
        }
      }
    }
    
    html+="<hr/><button class=\"c4i-processing-reload\">Reload</button>";
      
    overview.html(html);
    
    rootElement.find(".c4i-processing-reload").button({icons: {primary: "ui-icon-refresh"}}).unbind('click').click(function(){_this.renderProcessingInterface(options);})
    
    
    inputs.html("");

  };
  
  this.renderProcessingInterface = function(options){
    //console.log("renderProcessingInterface");
    $.blockUI.defaults.message='<div class="c4i-processing-loader-box"><div class="c4i-processing-loader-animation"></div><div class="c4i-processing-loader-message">Loading ...</div></div>';
    $.blockUI.defaults.css.border='none';
    $.blockUI.defaults.overlayCSS.backgroundColor="white";
    
    if(options.dialog){
      var title = options.identifier;
      if(options.statuslocation){
        title+=" - ";
        title+=options.statuslocation.replace(/\\/g,'/').replace( /.*\//, '' );
      }
      options.element.css({zIndex:1000}).dialog({
        title:title,
        width:975,
        height:600,
        zIndex: 1000,
        dialogClass:'c4i-processing-containerdialog'
      }).dialogExtend({
        "maximizable" : true,
        "dblclick" : "maximize",
        "icons" : { "maximize" : "ui-icon-arrow-4-diag" }
      });
    }else{
      options.element.addClass("c4i-processing-container");
    }
    
    rootElement = options.element;
    options.element.html('<div class="simplecomponent-container">'+
    '  <div class="simplecomponent c4i-processing-overview">'+
    '    <div class="simplecomponent-header">Overview</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent c4i-processing-report">'+
    '    <div class="simplecomponent-header">Processing report</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent c4i-processing-inputs">'+
    '    <div class="simplecomponent-header">Processing inputs'+
    //    '      <button class="c4i_processing_clear">Clear</button>'+
    '    </div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    '  <div class="simplecomponent c4i-processing-footer">'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
//     ''+
//     '  <div class="simplecomponent c4i-processing-results">'+
//     '    <div class="simplecomponent-header">Results</div>'+
//     '    <div class="simplecomponent-body"></div>'+
//     '    <div class="simplecomponent-footer"></div>'+
//     '  </div>'+
//     ''+
    '</div>');
    rootElement.find(".c4i-processing-report").hide();
    if(options.query){
      query = options.query;
    }
    
    if(options.wpsservice){
      WPSServiceURL = options.wpsservice;
    }
    
    if(options.identifier){
      WPSIdentifier = options.identifier;
    }
    
    
    $(".c4i_processing_help").button({
      
      icons: {
        primary: "ui-icon-help"
      },
    }).unbind('click').click(function(){
      userHasSignedInOrOut();
      return;
      var el = jQuery('<div title="Processing help" class="headerhelpdiv"></div>', {}).dialog({
        width:800,
        height:400,
        modal:true
      });
      el.html('<div class="c4i-processing-loader"></div>');
      var helpReturned = function(data){
        el.html(data);    
      }
      $.ajax({
        url: "processing/processinghelp.html"     
      }).done(function(d) {
        helpReturned(d)
      })
    });
    
    
    WPSDescribeCoverageURL = WPSServiceURL+"service=WPS&version=1.0.0&request=describeprocess&identifier="+WPSIdentifier
    var url=(c4iconfigjs.xml2jsonservice+"SERVICE=XML2JSON&request="+URLEncode(WPSDescribeCoverageURL));
    
    

    if(options.statuslocation){
      var wps = new WMJSProcessing({
        url:url,
        success:function(d){wpsComplete(d,url);},
        progress:wpsProgress,
        failure:wpsFailure
      });
      wps.parseStatuslocation(options.statuslocation);
    };
    
    if(options.inputdata){
      describeProcess(options.inputdata,url)
    }else{
 
    
      rootElement.block();
    
      $.ajax({
        url: url,
        crossDomain:true,
        dataType:"jsonp"
      }).done(function(d) {
        describeProcess(d,url)
      }).fail(function(d) {
        //alert("fail 154");
        console.log("Ajax call failed: "+url);
        error(d,url);
        
        
      }).always(function(){
        rootElement.unblock();
  
      });
    }
    
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
  

  
  
  var _stripNS = function(newObj,obj){
    
    var keys = c4iProcessingGetKeys(obj);
    
    for(var j=0;j<keys.length;j++){
      var key = keys[j];
      var i = key.indexOf(":");
      var newkey=key.substring(i+1);
      var value = obj[key];
      if(typeof value === 'object'){
        newObj[newkey] = {};
        _stripNS(newObj[newkey],value);
      }else{
        newObj[newkey]=value;
      }
    }
  };
  
  var stripNS = function(currentObj){
    var newObj = {};
    _stripNS(newObj,currentObj);
    return newObj;
  };
  
  var decodeBase64 = function(s) {
    var e={},i,b=0,c,x,l=0,a,r='',w=String.fromCharCode,L=s.length;
    var A="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    for(i=0;i<64;i++){e[A.charAt(i)]=i;}
    for(x=0;x<L;x++){
        c=e[s.charAt(x)];b=(b<<6)+c;l+=6;
        while(l>=8){((a=(b>>>(l-=8))&0xff)||(x<(L-2)))&&(r+=w(a));}
    }
    return r;
  };
  
  function htmlEncode(str) {
      return String(str)
              .replace(/&/g, '&amp;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#39;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/ /g, '&nbsp;')
              .replace(/\n/g, '<br/>');
  }
  
  var checkifHasBasket = function(inputname,myabstract){
    if(c4iProcessingStartsWith(inputname,"wpsnetcdfinput")){
      return true;
    }
    
    if(!isDefined(myabstract)){
      myabstract="";
    }
    if(!isDefined(myabstract.indexOf)){
      myabstract="";
    }
    if(myabstract.indexOf("application/netcdf")!=-1){
      return true;
    }
    return false;
  };
    
  var initializeLiteralInputButtons = function(){
    rootElement.find(".c4i-processing-showbasket-button").button({text: false,icons: {primary: "ui-icon-cart"}}).unbind('click').click(function(){showBasketWidget($(this).parent());}).attr('title','Open your basket and select files');
    
   
    rootElement.find(".c4i-processing-showpreview-button").button({text: false,icons: {primary: "ui-icon-video"}}).unbind('click').click(function(){
        var literalinput = $(this).parent().find(".c4i-processing-literalinput-input");
        var value = literalinput.val();
        var el=jQuery('<div/>');
        renderFileViewerInterface({
          element:el,
          service:c4iconfigjs.impactservice,
          adagucservice:c4iconfigjs.adagucservice,
          adagucviewer:c4iconfigjs.adagucviewer,
          provenanceservice:c4iconfigjs.provenanceservice,
          query:value,
          dialog:true
        });   
    }).attr('title','Show file metadata and information');
    
    rootElement.find(".c4i-processing-input-moreless-buttonplus").empty().button({text: false,icons: {primary: "ui-icon-plus"}}).unbind('click').click(function(){
      var name = $(this).parent().parent().find(".c4i-processing-literalinput-span").attr("name");
      var thisabstractText = $(this).parent().parent().parent().find(".c4i-processing-input-tile-abstract").html();
      var hasBasket=checkifHasBasket(name,thisabstractText);
      var cardinality = $(this).parent().parent().parent().find(".c4i-processing-input-tile-cardinality").attr("name");
      var max = cardinality.split(',')[1];
      var min = cardinality.split(',')[0];
      var html=createLiteralInput(name,"",hasBasket,true);
      $(this).parent().parent().find(".c4i-processing-literalinput-span").append(html);
      initializeLiteralInputButtons();
    }).attr('title','Add an extra input field');
    
    rootElement.find(".c4i-processing-input-moreless-buttonmin").button({text: false,icons: {primary: "ui-icon-minus"}}).unbind('click').click(function(){
      //console.log( $(this).parent().parent().parent().find(".c4i-processing-inputfieldspanner").size());
      $(this).parent().remove();
    }).attr('title','Remove this input field');
    
    rootElement.find(".c4i-processing-input-moreless-buttonremoveall").button({text: false,icons: {primary: "ui-icon-trash"}}).unbind('click').click(function(){
      $(this).parent().parent().parent().find(".c4i-processing-literalinput-span").empty();
    }).attr('title','Remove all input fields');
    
    //TODO Disable remove buttons if only one is left
    
    
    rootElement.find(".c4i-processing-input-tile-linkmetadata").button().unbind('click').click(function(){
      /* Show available option for an input */
      var inputname = $(this).attr('name');
      var inputatype = inputname.split("~")[0].split("_")[0]; 
      var inputaname = inputname.split("~")[0].split("_")[1];
      var inputb = inputname.split("~")[1];
      var settings = getInputSettings();
      var inputvalues = settings[inputb];
      var newEl = $(this).parent().find(".c4i-processing-input-tile-linkmetadata-info");
      newEl.empty();
      
      
      for(var j=0;j<inputvalues.length;j++){
        var value = inputvalues[j];
        function fillInLayerNames(value,el){
          var WMSGetCapabiltiesURL = options.adagucservice+ 'source='+URLEncode(value);
          xml2jsonrequestURL = options.adagucviewerservice+'SERVICE=XML2JSON&';
          var service = WMJSgetServiceFromStore(WMSGetCapabiltiesURL);
          service.getLayerNames(function(layerNames){
            for(var j=0;j<layerNames.length;j++){
              if(layerNames[j].indexOf('baselayer') ==-1 &&
                  layerNames[j].indexOf('overlay') ==-1 &&
                  layerNames[j].indexOf('features') ==-1 &&
                  layerNames[j].indexOf('grid') ==-1
                  ){
                el.html("done");
                if(inputatype == 'wpsvariable'){
                  var html = "Variables for "+value.substring(value.lastIndexOf("/")+1);
                  html+="<ul><li>"+layerNames[j]+"</ul></li>";
                  el.html(html);  
                }
                if(inputatype == 'wpstimerange' || inputatype == 'wpstime' ||inputatype == 'wpsnlevel'){
                  var showLayerInfo = function(layer){
                    var html = "No info found.";
                    for(var j=0;j<layer.dimensions.length;j++){
                      if(layer.dimensions[j].name === 'time' && (inputatype == 'wpstimerange' || inputatype == 'wpstime')){
                        html= "Dates for "+value.substring(value.lastIndexOf("/")+1);
                        html+="<ul><li>number of steps: "+layer.dimensions[j].size()+"</li>";
                        html+="<li>Start: "+layer.dimensions[j].getValueForIndex(0)+"</li>";
                        html+="<li>Stop: "+layer.dimensions[j].getValueForIndex(layer.dimensions[j].size()-1)+"</li>";
                        html+="</ul>";
                        html+="All timesteps:";
                        html+="<ul>";
                        for(var i=0;i<layer.dimensions[j].size();i++){
                          html+="<li>"+i+") "+layer.dimensions[j].getValueForIndex(i)+"</li>";
                        }
                        
                        html+="</ul>";
                      }
                      if(layer.dimensions[j].name === 'elevation' && inputatype == 'wpsnlevel'){
                        html= "Levels for "+value.substring(value.lastIndexOf("/")+1);
                        html+="<ul>";
                        for(var i=0;i<layer.dimensions[j].size();i++){
                          html+="<li>Pressure level "+i+" with value "+layer.dimensions[j].getValueForIndex(i)+" "+layer.dimensions[j].units+"</li>";
                        }
                      }
                    }
                    el.html(html);
                  };
                  new WMJSLayer({service:WMSGetCapabiltiesURL,name:layerNames[0],onReady:showLayerInfo});
                };
                
              }
            }
          },
          function(error){alert(error);});
        };
        
        
        newEl.append("<div class=\"c4i-processing-input-tile-linkmetadata-container\">Reading info ... </div>");
        fillInLayerNames(value,newEl.children().last());
      }
    }).attr('title','Get metadata for this input');
    
  };
  
  var createLiteralInput = function(inputname,defaulttext,hasBasket,showMinButton){
    var html="<span class=\"c4i-processing-inputfieldspanner\" name=\""+inputname+"\"><input class=\"c4i-processing-literalinput-input\" name=\""+inputname+"\" value=\""+defaulttext+"\"/>";
    if(hasBasket){
      html+="<button class=\"c4i-processing-showbasket-button\"></button>"
      html+="<button class=\"c4i-processing-showpreview-button\"></button>"
      html+="<button class=\"c4i-processing-input-moreless-buttonmin\"></button>";
    }else{
      if(showMinButton){
        html+="<button class=\"c4i-processing-input-moreless-buttonmin\"></button>";
      }
    }
    html+="</span>";
    return html;
  };
  
  var showBasketWidget= function(el){
    basketWidget.show(function(selectedNodes) {
      //console.log(selectedNodes);
      var num = 0;
      for ( var j = 0; j < selectedNodes.length; j++) {
        if(selectedNodes[j].dapurl){
          if(num == 0){
            $(el).find(".c4i-processing-literalinput-input").val(selectedNodes[j].dapurl);
          }else{
           
            $(el).parent().append(createLiteralInput($(el).attr("name"),selectedNodes[j].dapurl,true,true));
          }
          num++;
        }
      }
      if(num>1){
        initializeLiteralInputButtons();
      }
      return true;
    });
  };
  
  
  var makeWPSErrorReport = function(message,url){
    rootElement.find(".c4i-processing-report").show();
    rootElement.find(".c4i-processing-report").find(".simplecomponent-header").css({"backgroundColor":"red"}).html("Processing did not succeed ...");
    var html="<span class=\"c4i-processing-input-tile\">"
    html+="<span class=\"c4i-processing-report-container\">";
    
    html+="<button class=\"c4i-processing-hidewpserrorreport\"></button>"
    
    html+="<span class=\"c4i-processing-errormessage-container\"><span class=\"c4i-processing-errormessage-header\">Sorry! Those settings didn't work ...</span><br/><br/><br/>We got the following message:<hr/><span class=\"c4i-processing-errormessage\">"+message.message+"</span><hr/>Location: <a target=\"_blank\" href=\""+url+"\">"+url+"</a></span>";
    html+="</span>";
    html+="</span>";
    rootElement.find(".c4i-processing-report").find(".simplecomponent-body").html(html);
    
    rootElement.find(".c4i-processing-hidewpserrorreport").button({icons: {primary: "ui-icon-circle-close"}}).unbind('click').click(function(){rootElement.find(".c4i-processing-report").hide();})
  }

  
  var makeWPSSuccessReport = function(data,url){
    rootElement.find(".c4i-processing-report").show();
    rootElement.find(".c4i-processing-report").find(".simplecomponent-header").css({"backgroundColor":"#0A0"}).html("Processing succeeded! Showing report:");
    var html="<span class=\"c4i-processing-input-tile\">"
    
    html+="<span class=\"c4i-processing-report-container\">";
    //html+="<b>Processing report</b><br/>";
    html+="<table class=\"c4i-processing-table c4i-processing-report-table\">";
    html+="<tr><th>Identifier</th><th>Title</th><th>Type</th><th>MimeType</th><th>Value</th></tr>";
    var keys = c4iProcessingGetKeys(data);
    if(c4iProcessingIndexOf(keys,'0')==-1){
      data={'0':data};
      keys = c4iProcessingGetKeys(data);
    }
    
    
    for(var j=0;j<keys.length;j++){
      var item = data[keys[j]];
      console.log(item);
      html+="<tr><td class=\"c4i-processing-report-table-identifier\">"+item.Identifier.value+"</td><td class=\"c4i-processing-report-table-title\">"+htmlEncode(item.Title.value)+"</td>";
      
      var type="";
      if(item.Data && item.Data.LiteralData && item.Data.LiteralData.attr && item.Data.LiteralData.attr.dataType){
        type = item.Data.LiteralData.attr.dataType;
      }
      

      var mimeType="";
      if(item.Data && item.Data.ComplexData && item.Data.ComplexData.attr && item.Data.ComplexData.attr.mimeType){
    	  mimeType = item.Data.ComplexData.attr.mimeType;
      }
      
      
      var value="";
      if(item.Data && item.Data.LiteralData && item.Data.LiteralData.value){
        value = item.Data.LiteralData.value;
      }else{
    	if(item.Data && item.Data.ComplexData && item.Data.ComplexData.value){
    	  value = item.Data.ComplexData.value;
    	}
      }
      
      
      
      if(c4iProcessingStartsWith(value,"base64:")){
        value = decodeBase64(value.substring("base64:".length));
      }
      var literalDataValueLowerCase = value.toLowerCase();
      
      
      
      html+="<td class=\"c4i-processing-report-table-type\">"+type+"</td>";
      html+="<td class=\"c4i-processing-report-table-type\">"+mimeType+"</td>";
      
      if( (literalDataValueLowerCase.indexOf("dap")!=-1&&
          literalDataValueLowerCase.indexOf("http")!=-1 &&
          literalDataValueLowerCase.indexOf(".nc")!=-1) ||
          
          (literalDataValueLowerCase.indexOf("http")!=-1 &&
          (c4iProcessingEndsWith(literalDataValueLowerCase,"csv")||
           c4iProcessingEndsWith(literalDataValueLowerCase,"png")||
           c4iProcessingEndsWith(literalDataValueLowerCase,"txt")))
      ){
        /* NetCDF viewable file */
        html+="<td class=\"c4i-processing-report-table-value\">";
        html+="<table><tr><td class=\"c4i-processing-report-table-noborder\"><span class=\"c4i-processing-report-table-value-viewable\">"+htmlEncode(value)+"</span></td>";
        html+="<td class=\"c4i-processing-report-table-noborder\"><button class=\"c4i-processing-report-table-value-viewablebutton\">Show</button></td></tr></table></td>";
      } else{
    	if(mimeType && mimeType=='image/png'){
    		html+="<td class=\"c4i-processing-report-table-value\"><img class=\"c4i-processing-previewstyle\" src=\"data:image/png;base64, " +value+ "\" alt=\"Image\" /></td>";
    	}else{
    		html+="<td class=\"c4i-processing-report-table-value\">"+htmlEncode(value)+"</td>";
    	}
      }
      
      
      html+="</tr>";
    }
    html+="</table>";
    html+="</span>";
    html+="</span>";
    reportEl = rootElement.find(".c4i-processing-report").find(".simplecomponent-body");
    reportEl.html(html);
    
    reportEl.find(".c4i-processing-report-table-value-viewablebutton").button({text: false,icons: {primary: "ui-icon-video"}}).unbind('click').click(function(){
        var literalinput = $(this).parent().parent().find(".c4i-processing-report-table-value-viewable");
        var value = literalinput.html();
        var el=jQuery('<div/>');
        renderFileViewerInterface({
          element:el,
          service:c4iconfigjs.impactservice,
          adagucservice:c4iconfigjs.adagucservice,
          adagucviewer:c4iconfigjs.adagucviewer,
          provenanceservice:c4iconfigjs.provenanceservice,
          query:value,
          dialog:true
        });   
    }).attr('title','Show file information 1');
    
    reportEl.find(".c4i-processing-report-table-value-viewable").unbind('click').click(function(){
        var literalinput = $(this).parent().find(".c4i-processing-report-table-value-viewable");
        var value = literalinput.html();
        var el=jQuery('<div/>');
        renderFileViewerInterface({
          element:el,
          service:c4iconfigjs.impactservice,
          adagucservice:c4iconfigjs.adagucservice,
          adagucviewer:c4iconfigjs.adagucviewer,
          provenanceservice:c4iconfigjs.provenanceservice,
          query:value,
          dialog:true
        });   
    }).attr('title','Show file information 2');
  };
  
  var wpsComplete = function(data,url){
    rootElement.find(".c4i-processing-startbutton").show();
    rootElement.find(".c4i-processing-progressbar").progressbar({value: 0}).hide();
    makeWPSSuccessReport(data,url);
  };
  
  var wpsProgress = function(percentCompleted,message){
    rootElement.find(".c4i-processing-progressbar").progressbar({value: percentCompleted}).show();
    rootElement.find(".c4i-processing-progresslabel").text(message+"("+percentCompleted + "%)" );
  };
  
  var wpsFailure = function(message,data,url){
    rootElement.find(".c4i-processing-startbutton").show();
    rootElement.find(".c4i-processing-progressbar").progressbar({value: 0}).hide();
    makeWPSErrorReport(message,url);
    //alert("<span class=\"c4i-processing-errormessage-container\"><span class=\"c4i-processing-errormessage-header\">Sorry! Those settings didn't work ...</span><br/><br/><br/>We got the following message:<hr/><span class=\"c4i-processing-errormessage\">"+message.message+"</span><hr/>Location: <a target=\"_blank\" \"href=\""+url+"\">"+url+"</a></span>");
  };
  
  var executeProcess = function(data,url){
    rootElement.find(".c4i-processing-report").hide();
    rootElement.find(".c4i-processing-startbutton").hide();
    rootElement.find(".c4i-processing-progressbar").progressbar({value: 0}).show();
    rootElement.find(".c4i-processing-progresslabel").text("Initializing ...");

    /*Create new processing object*/
    var wps = new WMJSProcessing({
      url:url,
      success:function(d){wpsComplete(d,url);},
      progress:wpsProgress,
      failure:wpsFailure
    });
    wps.execute(WPSIdentifier,data);
  };
  
  var getInputSettings = function(){
    var settings = {};
    var inputs = rootElement.find(".c4i-processing-inputs").find(".simplecomponent-body").first().find(".c4i-processing-inputfieldspanner");
    for(var j=0;j<inputs.length;j++){
      var inputfield=$(inputs[j]);
      var literalinput = inputfield.find(".c4i-processing-literalinput-input");
      var literalinputcombo = inputfield.find(".c4i-processing-literalinput-select");
      //console.log(literalinput);
      if(literalinput.length==1){
        var name = inputfield.attr("name");
        var value = literalinput.first().val();
        if(!settings[name])settings[name]=[value];else settings[name].push(value);
        
      }
      if(literalinputcombo.length==1){
        //console.log(literalinputcombo);
        var name = inputfield.attr("name");
        var value = literalinputcombo.val();
        if(!settings[name])settings[name]=[value];else settings[name].push(value);
      }
    }
    return settings;
  }
  
  /*Called on succesfull describeprocess callback*/
  var describeProcess = function(data,url){
    if(data.error || data.exception){
      error(data,url);
      return;
    }
    
    
    var describeProcessDoc = stripNS(data);
    //console.log(describeProcessDoc);
    
    var overview = rootElement.find(".c4i-processing-overview").find(".simplecomponent-body").first();
    var footer = rootElement.find(".c4i-processing-footer").find(".simplecomponent-body").first();
    var inputs = rootElement.find(".c4i-processing-inputs").find(".simplecomponent-body").first();
    
    var ProcessDescription;
    if(describeProcessDoc.ProcessDescriptions){
      ProcessDescription = describeProcessDoc.ProcessDescriptions.ProcessDescription;
    }
    if(describeProcessDoc.Execute){
      ProcessDescription = describeProcessDoc.Execute;
    }
    
    
    var html="<span class=\"c4i-processing-input-tile\">";
    if(isDefined(ProcessDescription.Title)){html+="<h1>Processor "+ProcessDescription.Title.value+"</h1>";}
    html+="<table class=\"c4i-processing-table\">";
    if(isDefined(ProcessDescription.Title)){html+="<tr><th>Title</th><th>"+ProcessDescription.Title.value+"</th></tr>";}
    if(isDefined(ProcessDescription.Identifier)){html+="<tr><td>Identifier</td><td>"+ProcessDescription.Identifier.value+"</td></tr>";}
    if(isDefined(ProcessDescription.Abstract)){html+="<tr><td>Abstract</td><td>"+ProcessDescription.Abstract.value+"</td></tr>";}
    html+="<tr><td>Location</td><td><a target=\"_blank\" href=\""+WPSDescribeCoverageURL+"\">"+WPSDescribeCoverageURL+"</a></td></tr>";
    html+="</table>";
    
    html+="<span class=\"c4i-processing-start-span\"><button class=\"c4i-processing-startbutton\">Start processing</button></span>";
    html+="<div class=\"c4i-processing-progressbar\" style=\"display:none\"><div class=\"c4i-processing-progresslabel\"></div></div>";
    
    overview.html(html);
    
//    html="<span class=\"c4i-processing-start-span\"><button class=\"c4i-processing-startbutton\">Start processing</button></span>";
//    html+="<div class=\"c4i-processing-progressbar\" style=\"display:none\"><div class=\"c4i-processing-progresslabel\"></div></div>";
//    footer.html(html);
    rootElement.find(".c4i-processing-startbutton").button({
      icons: {
        primary: "codejobsicon"
      },
    }).unbind('click').click(function(){
      //alert("StartProc");

      executeProcess(getInputSettings(),WPSServiceURL);
    })
    
    html="</span>";
    
    /* Creates an input field for the process input */
    var createDataInput = function(DataInput){

      if( !DataInput.Identifier)return;
      var inputname = DataInput.Identifier.value;
      var myabstract="";
      
      var minOccurs = 0;var maxOccurs = 1;var defaulttext="";
      if(DataInput.attr && isDefined(DataInput.attr.minOccurs))minOccurs = DataInput.attr.minOccurs;
      if(DataInput.attr && isDefined(DataInput.attr.maxOccurs))maxOccurs = DataInput.attr.maxOccurs;
      
      if(DataInput.Data && DataInput.Data.LiteralData && DataInput.Data.LiteralData.value)defaulttext = DataInput.Data.LiteralData.value;
      if(DataInput.LiteralData && DataInput.LiteralData.DefaultValue && isDefined(DataInput.LiteralData.DefaultValue.value))defaulttext = DataInput.LiteralData.DefaultValue.value;
      
      
      

      var counter = minOccurs;
      if(counter==0)counter=1;
      var title=inputname;
      if(DataInput.Title){
       title = DataInput.Title.value;
      }
      var html="<span class=\"c4i-processing-input-tile\" name=\""+inputname+"\">";
      html+="<span class=\"c4i-processing-input-tile-span\"><span class=\"c4i-processing-input-tile-title\">"+title+"</span> <span class=\"c4i-processing-input-tile-identifier\">("+inputname+")</span>";
      html+="<span class=\"c4i-processing-input-tile-cardinality\" name="+minOccurs+","+maxOccurs+">min:"+minOccurs+" / max: "+maxOccurs+"</span>";
      html+="</span>";
      if(DataInput.Abstract && isDefined(DataInput.Abstract.value)){
        myabstract = DataInput.Abstract.value;
        html+="<span class=\"c4i-processing-input-tile-abstract\">"+myabstract+"</span>";
      }
//      if(defaulttext.length>0){
//        html+="<span class=\"c4i-processing-input-tile-default\">Default: ";
//        html+="<span class=\"c4i-processing-input-tile-defaultvalue\">"+defaulttext+"</span></span>";
//      }
//  
      


      
      var hasBasket = checkifHasBasket(inputname,myabstract);
    

      var isCombo = false;
      if(DataInput.LiteralData && DataInput.LiteralData.AllowedValues && DataInput.LiteralData.AllowedValues.Value){
        isCombo = true;
      }
      
   
      html+="<span class=\"c4i-processing-input-tile-inputs\">";
      for(var j=0;j<counter;j++){
        html+="<span class=\"c4i-processing-literalinput-span\" name=\""+inputname+"\">";
        if(isCombo){
          html+="<span class=\"c4i-processing-inputfieldspanner\" name=\""+inputname+"\">";
          html+="<select class=\"c4i-processing-literalinput-select\" name=\""+inputname+"\">";
          var values = DataInput.LiteralData.AllowedValues.Value;
          var keys = c4iProcessingGetKeys(values);
          keys=keys.sort();
          for(var i=0;i<keys.length;i++){
            var value = values[keys[i]].value;
            var selected = "";
            if(value == defaulttext)selected="selected";
            html+="<option value=\""+value+"\" "+selected+" >"+getInputNameMapping(inputname,value)+"</option>";
          }
          html+="</select>";
          html+="</span>";
        }else{
          var defaulttexts = defaulttext.split(",");
          for(var i=0;i<defaulttexts.length;i++){
            html+=createLiteralInput(inputname,defaulttexts[i],hasBasket,true);
          }
          
        }
        html+="</span>";
      }
            
      //if(counter<maxOccurs)
      {
        html+="<span class=\"c4i-processing-input-moreless\"></button>"+
          //"<button class=\"c4i-processing-input-moreless-buttonremoveall\">"+
          "</button><button class=\"c4i-processing-input-moreless-buttonplus\"></button></span>";
      }
  
      if(inputname.indexOf("~")!=-1){
        var inputatype = inputname.split("~")[0].split("_")[0]; 
        var inputaname = inputname.split("~")[0].split("_")[1];
        var inputb = inputname.split("~")[1];
        html+="<span class=\"c4i-processing-input-tile-linkmetadata-span\">";
        html+="<button class=\"c4i-processing-input-tile-linkmetadata\" name=\""+inputname+"\">Show options based on "+inputb+"</button>";
        html+="<span class=\"c4i-processing-input-tile-linkmetadata-info\"></span>";
        html+="</span>";
      }
      html+="</span>";
      
      html+="</span>";

      
      return html
    };
    
    
    var DataInputs;
    if(ProcessDescription.DataInputs){
      DataInputs = ProcessDescription.DataInputs.Input;
    }else{
      if(ProcessDescription.Execute){
        DataInputs = ProcessDescription.Execute.DataInputs.Input;
      }
    }
    
    // Single inputs are not nested into an array, check it out:
    if (DataInputs.attr){
    	DataInputs = [].concat(DataInputs);
    }

    if(DataInputs){
      var keys = c4iProcessingGetKeys(DataInputs);
      keys=keys.sort();
      for(var j=0;j<keys.length;j++){
        html+=createDataInput(DataInputs[keys[j]]);//.Identifier.value+"</br>";
      }
    }else{
      html+="<span class=\"c4i-processing-input-tile\">";
      html+="<span class=\"c4i-processing-input-tile-span\"><span class=\"c4i-processing-input-tile-title\">";
      html+="No inputs defined for this process";
      html+="</span>";
      html+="</span>";
        
    }
    inputs.html(html);
    initializeLiteralInputButtons();
  
    
    
    
    
  };
  this.renderProcessingInterface(options);
};
