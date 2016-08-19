/**
* Renders a search interface to given element.
* Arguments via:
* options{
*  element : the element to render to.
*  url: the location of the search servlet endpoint.
*  query: predefined query, loads facets in advance.
* }
*/
var renderFileViewerInterface = function(options){
  return new FileViewerInterface(options);
};

/**
 * Support for backward compatibility. Other components call renderFileViewer instead
 */
var renderFileViewer = function(options){
  var newoptions = [];
  

  newoptions.query=options.url;
  newoptions.dialog=true;
  newoptions.element=jQuery('<div/>');
  return new FileViewerInterface(newoptions);
};



var FileViewerInterface = function(options){
  var _this = this;
  
  if(!options.service)options.service=c4iconfigjs.impactservice;
  if(!options.adagucservice)options.adagucservice=c4iconfigjs.adagucservice;
  if(!options.adagucviewer)options.adagucviewer=c4iconfigjs.adagucviewer;
  if(!options.howtologinlink)options.howtologinlink=c4iconfigjs.howtologinlink;
  if(!options.contactexpertlink)options.contactexpertlink=c4iconfigjs.contactexpertlink;
  if(!options.provenanceservice)options.provenanceservice=c4iconfigjs.provenanceservice;
  
  
  options.prettyquery=options.query.split("#")[0];
  
  //var impactFileViewerEndPoint = "http://bhw485.knmi.nl:8280/impactportal/fileviewer?";
  var impactFileViewerEndPoint = "fileviewer?";
 
  
  var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";
  query=(window.location.hash).replace("#","");//data_node=albedo2.dkrz.de&experiment=rcp45&project=CMIP5&time_frequency=day&variable=tas&model=EC-EARTH&";
  
  this.visualizeVariable = function(variable, service) {
    var w = window.open(options.adagucviewer + "#addlayer('" + options.adagucservice
        + "source="+URLEncode(service) + "','" + URLEncode(variable) + "')",
        'adagucportal', '');
    w.focus();
    return false;
  };
  
  var downloadWin;
  
  this.openDownloadWindow = function(){
    var request = options.prettyquery;
    if (request.indexOf('aggregation') > 0) {
      alert('Aggregations cannot be downloaded directly.');
      return;
    }
    var downloadURL = request.replace('dodsC', 'fileServer');
    try{
      if(openid){
        if(openid!=""){
          downloadURL+="?openid="+openid;
        }
      }
    }catch(e){
    }

    if (downloadWin)
      downloadWin.close();
      downloadWin = window.open(downloadURL, 'jav','width=900,height=600,resizable=yes');
    return;
  }

  
  var downloadbutton = { 
    text: 'Download',
    icons: { primary: "ui-icon-arrowthick-1-s"},
    click: function( event ) {
      event.preventDefault();
      _this.openDownloadWindow();
    }
  };
  var checkCLIPCDRS = { 
      text:"Check CLIPC DRS",
      icons: { primary: "ui-icon-search"},
      click: function( event ) {
        event.preventDefault();
        var url = options.prettyquery;
        window.open("/impactportal/account/wizard_drschecker.jsp?resource="+url, '_blank');
      }
    };
  var addtobasketbutton = { 
    text:"Add to basket",
    icons: { primary: "ui-icon-cart"},
    click: function( event ) {
      event.preventDefault();
      var url = options.prettyquery;
      var id = url.substring(url.lastIndexOf("/") + 1);

      basket.postIdentifiersToBasket({
        id : id,
        opendap : url
      });
    }
  };
  
  var reloadbutton = {
    text: "Reload",
    icons: { primary: "ui-icon-refresh"},
    click: function( event ) {
      event.preventDefault();
      _this.renderFileViewerInterface(options);
    }
  };
    
  this.renderFileViewerInterface = function(options){
    $.blockUI.defaults.message='<div class="c4i-fileviewer-loader"></div>';
    $.blockUI.defaults.css.border='none';
    $.blockUI.defaults.overlayCSS.backgroundColor="white";
    
    if(options.dialog){
      options.element.dialog({
        title:'NetCDF Metadata',
        width:1000,
        height:600,

        dialogClass:'c4i-fileviewer-containerdialog'
      }).dialogExtend({
        "maximizable" : true,
        "dblclick" : "maximize",
        "icons" : { "maximize" : "ui-icon-arrow-4-diag" }
      });;
      
    }else{
      options.element.addClass("c4i-fileviewer-container");
    }
    rootElement = options.element;

    //rootElement.html("");
    rootElement.block();
    if(options.prettyquery){
      query = options.prettyquery;
    }
 


 
    
    $(".c4i_fileviewer_help").button({
      
      icons: {
        primary: "ui-icon-help"
      },
    }).click(function(){
      var el = jQuery('<div title="Search help" class="headerhelpdiv"></div>', {}).dialog({
        width:800,
        height:400,
        modal:true
      });
      el.html('<div class="c4i-fileviewer-loader"></div>');
      var helpReturned = function(data){
        el.html(data);    
      }
      $.ajax({
        url: "fileviewer/fileviewerhelp.html"     
      }).done(function(d) {
        helpReturned(d)
      })
    });
    getVariableInfoFromServer();
  };
  
  function checkForViewableVariable(variable){
//      if(variable.variable == "lon"){return "";}
//      if(variable.variable == "lat"){return "";}
//      if(variable.variable == "x"){return "";}
//      if(variable.variable == "y"){return "";}
      if(variable.isViewable==1){
//      if(variable.dimensions&&variable.isViewable==1){
//      if(variable.dimensions.length>1){
//        for(var d=0;d<variable.dimensions.length;d++){
//          if(variable.dimensions[d].name.indexOf("bnds")!=-1)return "";
//        }

     
        var maxAllowedWidth=rootElement.width()-120;
        var url=options.adagucservice+"source="+URLEncode(options.prettyquery)+ "&service=WMS&request=getmap&format=image/png&layers=baselayer,"+variable.variable+",overlay&width="+maxAllowedWidth+"&CRS=EPSG:4326&STYLES=&EXCEPTIONS=INIMAGE&showlegend=true";
        var html='<div class="c4i-fileviewer-previewstyle" name="'+variable.variable+'"><span>Preview</span>: <img src="'+url+'"/></div>';
        return html;
//      }
    }
    return "";
  };
  
  function checkForProvenanceVariable(variable){
    if(variable.variable != "knmi_provenance"){return "";}

    var html=
       '<div class="c4i-fileviewer-previewstyle-SVG">'
      +'  <div  class="c4i-fileviewer-provenance"></div>'
      +'  <div class="c4i-fileviewer-provenance-controls">'
      //+'    <div class="c4i-fileviewer-provenance-controls-zoom">'
      +'      <p><i class="btn btn-success fa fa-refresh"></i></p>'
//      +'      <button class="c4i-fileviewer-provenance-controls-zoomout">Zoom out</button>'
//      +'      <button class="c4i-fileviewer-provenance-controls-zoomin">Zoom in</button>'
//      +'      <button class="c4i-fileviewer-provenance-controls-panleft">Left</button>'
//      +'      <button class="c4i-fileviewer-provenance-controls-panup">Up</button>'
//      +'      <button class="c4i-fileviewer-provenance-controls-panright">Right</button>'
//      +'      <button class="c4i-fileviewer-provenance-controls-pandown">Down</button><br/>'
      +'      <button class="c4i-fileviewer-provenance-controls-json">JSON</button>'
      +'      <button class="c4i-fileviewer-provenance-controls-xml">XML</button>'
      +'      <button class="c4i-fileviewer-provenance-controls-png">PNG</button>'
      +'      <button class="c4i-fileviewer-provenance-controls-svg">SVG</button>'
      +'    </div>'
      //+'  </div>'
      +'</div>';

    return html;
  };
  function doForProvenanceVariable(variable){
    if(variable.variable != "knmi_provenance"){return "";}
    var el = options.element.find(".c4i-fileviewer-provenance").first();
    
    options.element.find(".c4i-fileviewer-provenance-controls-json").button({icons: { primary: "ui-icon-arrowthick-1-s"}}).attr('onclick','').click(function(event){
      window.open(options.provenanceservice+"source="+URLEncode(options.prettyquery)+ "&service=prov&request=getprovenance&format=application/json", '_blank');
    });
    options.element.find(".c4i-fileviewer-provenance-controls-xml").button({icons: { primary: "ui-icon-arrowthick-1-s"}}).attr('onclick','').click(function(event){
      window.open(options.provenanceservice+"source="+URLEncode(options.prettyquery)+ "&service=prov&request=getprovenance&format=text/xml", '_blank');
    });
    options.element.find(".c4i-fileviewer-provenance-controls-svg").button({icons: { primary: "ui-icon-arrowthick-1-s"}}).attr('onclick','').click(function(event){
      window.open(options.provenanceservice+"source="+URLEncode(options.prettyquery)+ "&service=prov&request=getprovenance&format=text/html", '_blank');
    });
    options.element.find(".c4i-fileviewer-provenance-controls-png").button({icons: { primary: "ui-icon-arrowthick-1-s"}}).attr('onclick','').click(function(event){
      window.open(options.provenanceservice+"source="+URLEncode(options.prettyquery)+ "&service=prov&request=getprovenance&format=image/png", '_blank');
    });

    var url=options.provenanceservice+"source="+URLEncode(options.prettyquery)+ "&service=prov&request=getprovenance&format=image/svg";
    
    var httpCallback = function(data){
      if(data.error){
        
        alert("<b>"+data.error+"</b><hr/>Note: Exception info has been logged to your browsers console.");
        
        if(data.exception){
          if(console.log){
            console.log(data.exception);
          }
        }
        return;
      }
      var el = options.element.find(".c4i-fileviewer-provenance").first();
      el.html(data.svg);
      var svgEl =  el.find("svg").first();
      
      svgEl.attr('width', '100%');
      svgEl.attr('height', '350px');
      svgEl.attr('id','svgid');
      console.log(svgEl.attr('id'));
      // Expose to window namespase for testing purposes
      window.zoomTiger = svgPanZoom("#"+svgEl.attr('id'), {
        zoomEnabled: true,
        controlIconsEnabled: true,
        fit: true,
        center: true,
        zoomScaleSensitivity: 1
      });
//      "use strict";
//      var mySVG =svgEl.svgPanZoom({
//        mouseWheel: false 
//      });
//      
//      options.element.find(".c4i-fileviewer-provenance-controls-zoomin").button({icons: { primary: "ui-icon-circle-zoomin"}}).button({}).attr('onclick','').click(function(event){mySVG.zoomIn()});
//      options.element.find(".c4i-fileviewer-provenance-controls-zoomout").button({icons: { primary: "ui-icon-circle-zoomout"}}).attr('onclick','').click(function(event){mySVG.zoomOut()});
//      options.element.find(".c4i-fileviewer-provenance-controls-panleft").button({icons: { primary: "ui-icon-circle-arrow-w"}}).attr('onclick','').click(function(event){mySVG.panLeft()});
//      options.element.find(".c4i-fileviewer-provenance-controls-panright").button({icons: { primary: "ui-icon-circle-arrow-e"}}).attr('onclick','').click(function(event){mySVG.panRight()});
//      options.element.find(".c4i-fileviewer-provenance-controls-panup").button({icons: { primary: "ui-icon-circle-arrow-n"}}).attr('onclick','').click(function(event){mySVG.panUp()});
//      options.element.find(".c4i-fileviewer-provenance-controls-pandown").button({icons: { primary: "ui-icon-circle-arrow-s"}}).attr('onclick','').click(function(event){mySVG.panDown()});
//      

      
            

    };
    
    $.ajax({
      url: url,
          crossDomain:true,
          dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      //alert("fail 154");
      console.log("Ajax call failed: "+url);
      httpCallback({"error":"Request failed for "+url});
    })
    
  };
  
  //Use the browser's built-in functionality to quickly and safely escape
  //the string
  function escapeHtml(str) {
     var div = document.createElement('div');
     div.appendChild(document.createTextNode(str));
     var value = div.innerHTML;
     value=value.replaceAll("\n","<br/>");
     value=value.replaceAll(" ","&nbsp;");
     return value;
  };
  
  function handleErrorMessage(data){
    
    var html=
     '<div class="simplecomponent c4i-fileviewer-globalmetadata">'+
     '<div class="simplecomponent-body">'+
     options.prettyquery+
     '</div>'+
     '<div class="simplecomponent-footer"></div>'+
     '</div>'+
     
    '<div class="c4-fileviewer-variable-error">'+
      '<b>'+data.error+'</b><br/>';
    if(data.exception)html+=data.exception+'<br/><hr/>';
    html+='You can try the following:<ul>';
    
    if(!data.userid){
      html+='<li><span class="c4i-fileviewer-error-signinbutton c4i-fileviewer-spanlink" >Sign in</span></li>'
    }
    html+='<li><a class="c4i-fileviewer-spanlink" target="_blank" href="https://esgf-node.jpl.nasa.gov/ac/list/">Become a member of the right ESGF data group</a> and then <span class="c4i-fileviewer-error-reloadbutton c4i-fileviewer-spanlink" >reload</span</li>';
    

    
    html+=
      //'<li><span class="c4i-fileviewer-error-reloadbutton c4i-fileviewer-spanlink" >Reload</span></li>'+
      '<li><a class="c4i-fileviewer-spanlink" target="_blank" href="'+options.contactexpertlink+'">Request help</a></li>';
    html+='<li><span class="c4i-fileviewer-error-downloadbutton c4i-fileviewer-spanlink" >Open this file directly in your browser to get a hint</span>./li>';

    html+=
      '<li>If you are signed in but still cannot view the data, make sure your account is registered to the right group: <a class="c4i-fileviewer-spanlink" target="_blank" href="'+options.howtologinlink+'">-> HowTo</a>.</li>';
      
    
    if(data.userid){
      html+='<li>You are signed in as <b>'+data.userid+'</b>.</li>'
    }
    
    html+=     
      '</ul>'+
   '</div>'
    options.element.html(html);
    
    options.element.find(".c4i-fileviewer-error-signinbutton").attr('onclick','').click(function(event){
      event.preventDefault();          
      generateLoginDialog(function(){_this.renderFileViewerInterface(options);});
    });
    options.element.find(".c4i-fileviewer-error-reloadbutton").attr('onclick','').click(function(event){
      event.preventDefault();          
      _this.renderFileViewerInterface(options);
    });
    options.element.find(".c4i-fileviewer-error-downloadbutton").attr('onclick','').click(function(event){
      event.preventDefault();          
      _this.openDownloadWindow();
    });
  };
  
  function httpCallback(data){
    if(data.error){
      handleErrorMessage(data);
      return;
    }

    var html='<div class="simplecomponent-container">'+
    '  <div class="simplecomponent c4i-fileviewer-globalmetadata">'+
//     '    <div class="simplecomponent-header">NetCDF file info</div>'+
    '    <div class="simplecomponent-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>'+
    ''+
    '  <div class="simplecomponent c4i-fileviewer-variables">'+
//     '    <div class="simplecomponent-header">NetCDF Variables</div>'+
    '    <div class="simplecomponent-body c4i-fileviewer-variables-body"></div>'+
    '    <div class="simplecomponent-footer"></div>'+
    '  </div>';
    if(!options.dialog){
      html+='<button class="c4i-fileviewer-downloadbutton">Download</button>'+
        '<button class="c4i-fileviewer-addtobasketbutton">Add to basket</button>'+
        '<button class="c4i-fileviewer-reloadbutton">Reload</button>';
    }
    html+=    '</div>';

    options.element.html(html);
    
    if(options.dialog){
      var b= [checkCLIPCDRS,downloadbutton,addtobasketbutton,reloadbutton]
      options.element.dialog("option","buttons",b);
    }else{
      rootElement.find(".c4i-fileviewer-downloadbutton").button(downloadbutton).click(downloadbutton.click);
      rootElement.find(".c4i-fileviewer-addtobasketbutton").button(addtobasketbutton).click(addtobasketbutton.click);
      rootElement.find(".c4i-fileviewer-reloadbutton").button(reloadbutton).click(reloadbutton.click);   
    }
   /* var html="<table class=\"c4i-fileviewer-table\">";

    for(var j=0;j<data[0].attributes.length;j++){

      html+="<tr><td class=\"c4i-fileviewer-table-td-name\">"+data[0].attributes[j].name+"</td><td class=\"c4i-fileviewer-table-td-value\">"+ data[0].attributes[j].value+"</td></tr>";
      
    }
    
    html+="</table>";*/
    
   var html=options.prettyquery;
   rootElement.find(".c4i-fileviewer-globalmetadata").find(".simplecomponent-body").html(html);
      
    var html="";
    var provenanceVar = undefined;
    for(var v=0;v<data.length;v++){
      var preview=checkForViewableVariable(data[v]);
      if(preview==""){
        preview=checkForProvenanceVariable(data[v]);
        if(preview!=""){
          provenanceVar = data[v];
        }
      }

      html+="<span class=\"c4i-fileviewer-resultitem\">";     
      html+="  <span class=\"c4i-fileviewer-dataset-baseimage c4i-fileviewer-dataset-collapsible c4i-fileviewer-dataset-imgcollapsed \" name=\""+data[v].variable+"\"></span>";
    
     
      html+='    <span class="c4i-fileviewer-resultitem-content ">';
      html+='    <span class="c4i-fileviewer-resultitem-content-varinfo ">';
      if(data[v].variabletype){
        html+="<span class=\"c4i-fileviewer-variabletype\">"+data[v].variabletype+ "</span>";     
      }else{
        html+="<span class=\"c4i-fileviewer-variabletype\">-</span>";     
      }
      html+=     "<span class=\"c4i-fileviewer-variablename\">"+data[v].variable;
      if(data[v].dimensions&&(!data[v].isDimension)){
        var dimstring = " (";
        for(var d=0;d<data[v].dimensions.length;d++){
          //if(d>0)dimstring+=", ";
          dimstring+=(data[v].dimensions[d].name);
        }
        dimstring += ")";
        
         html+=  dimstring;
      }
      html+='</span>'
      html+=' - <b>'+data[v].longname+'</b>';
      
      if(data[v].isDimension){
        html+=  "<span class=\"c4i-fileviewer-vardimension\">dimension "+data[v].variable+" of length "+data[v].isDimension[0].length+"</span>";
      }
      html+='</span>'
      if(preview.length>0)
      {
        html+=  "<span class=\"c4i-fileviewer-adagucviewershow\"></span>";
      }
      
      
      html+='</span>';
      html+="  <span class=\"c4i-fileviewer-dataset-expandedarea\">";
      html+='    <div class="c4i-fileviewer-body">';
      html+=preview;
      if(preview.length>0){
        data[v].preview=preview;
      }
      
      
      html+="      <table class=\"c4i-fileviewer-table\">";
      for(var j=0;j<data[v].attributes.length;j++){
        var value =  data[v].attributes[j].value;
        if(data[v].attributes[j].name == "bundle" || data[v].attributes[j].name == "lineage"){
          value = vkbeautify.json(data[v].attributes[j].value,2);
        }
        value = escapeHtml(value);
        html+="      <tr><td class=\"c4i-fileviewer-table-td-name\">"+data[v].attributes[j].name+"</td><td class=\"c4i-fileviewer-table-td-value\">"+ value+"</td></tr>";
      }
      html+="      </table>";
      html+='    </div>';
      html+='    <div class="simplecomponent-footer"></div>';
      html+='  </span>';
      html+='</span>';
      
   
    }
    rootElement.find(".c4i-fileviewer-variables").find(".simplecomponent-body").html(html);
    rootElement.find(".c4i-fileviewer-dataset-expandedarea").hide();
    for(var v=0;v<data.length;v++){
      if(data[v].preview){
        var el=rootElement.find("span[name=\""+data[v].variable+"\"]").first();
        $(el).removeClass("c4i-fileviewer-dataset-imgcollapsed");
        $(el).addClass("c4i-fileviewer-dataset-imgexpand");
        $(el).parent().find(".c4i-fileviewer-dataset-expandedarea").show();
        var serviceOptions = el.parent().find(".c4i-fileviewer-adagucviewershow");
        var variable = data[v].variable;
        var adagucViewerAddLayer = $('<span name="'+variable+'" />').addClass('c4i-fileviewer-adagucview').html('Add to viewer');serviceOptions.append(adagucViewerAddLayer);
        var WMS = $('<span />').addClass('c4i-fileviewer-wms').html(
          '<a target="_blank" href="'+options.adagucservice+ 'source='+URLEncode(options.prettyquery)+'&service=WMS&request=GetCapabilities">WMS</a>');
        
        serviceOptions.append(WMS);
        var WCS = $('<span />').addClass('c4i-fileviewer-wcs').html(
          '<a target="_blank" href="'+options.adagucservice+ 'source='+URLEncode(options.prettyquery)+'&service=WCS&request=GetCapabilities">WCS</a>');
        serviceOptions.append(WCS);
        
        var opendap = $('<span />').addClass('c4i-fileviewer-opendap').html(
            '<a target="_blank" href="'+options.prettyquery+'.das">OpenDAP</a>');
        serviceOptions.append(opendap);
        
        
        
        adagucViewerAddLayer.attr('onclick','').click(function(event){
          event.preventDefault();
          var variable =  $(this).attr('name');
          _this.visualizeVariable(variable,options.prettyquery);
          return false;
        });
        
        WMS.attr('onclick','').click(function(event){
          event.stopPropagation();
        });
        
        WCS.attr('onclick','').click(function(event){
          event.stopPropagation();
        });
        
        opendap.attr('onclick','').click(function(event){
          event.stopPropagation();
        });
        
        var previewImage = el.parent().find(".c4i-fileviewer-previewstyle");
        previewImage.attr('onclick','').click(function(event){
          event.preventDefault();        
          var variable =  $(this).attr('name');
          _this.visualizeVariable(variable,options.prettyquery);
          return false;
        });
      }
    }
  
  
    var expandcollapse = function(t){
      var el=$(this).parent().find('.c4i-fileviewer-dataset-collapsible');
      if($(el).hasClass("c4i-fileviewer-dataset-imgcollapsed")){
        $(el).removeClass("c4i-fileviewer-dataset-imgcollapsed");
        $(el).addClass("c4i-fileviewer-dataset-imgexpand");
        $(el).parent().find(".c4i-fileviewer-dataset-expandedarea").show();
      }else{
        $(el).removeClass("c4i-fileviewer-dataset-imgexpand");
        $(el).addClass("c4i-fileviewer-dataset-imgcollapsed");
        $(el).parent().find(".c4i-fileviewer-dataset-expandedarea").hide();

      }
    };
  
    rootElement.find(".c4i-fileviewer-resultitem-content").attr('onclick','').click(expandcollapse);
    rootElement.find(".c4i-fileviewer-dataset-collapsible").attr('onclick','').click(expandcollapse);
    
    if(provenanceVar){
      doForProvenanceVariable(provenanceVar);
    }
  }
  
  function ready(){
  };
  
  function getVariableInfoFromServer(){
    var url = options.service+"service=getvariables&request="+URLEncode(options.prettyquery);
//    console.log(url);
    $.ajax({
      url: url,
          crossDomain:true,
          dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      //alert("fail 154");
      console.log("Ajax call failed: "+url);
      httpCallback({"error":"Request failed for "+url});
    }).always(function(){
      
      if(ready){
        ready();
      }
    });
  };
  
  this.renderFileViewerInterface(options);
};
