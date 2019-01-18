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
  if(!options.adagucviewerservice)options.adagucviewerservice=c4iconfigjs.adagucviewerservice;
  
  
  options.prettyquery=options.query.split("#")[0];
  
  //var impactFileViewerEndPoint = "http://bhw485.knmi.nl:8280/impactportal/fileviewer?";
  var impactFileViewerEndPoint = "fileviewer?";
 
  
  var query = "";//project=CMIP5&variable=tas&time_frequency=day&experiment=historical&model=EC-EARTH&";
  query=(window.location.hash).replace("#","");//data_node=albedo2.dkrz.de&experiment=rcp45&project=CMIP5&time_frequency=day&variable=tas&model=EC-EARTH&";
  
  
  
  /**
   * Cross browser indexOf function
   */
  var fileViewerIndexOf = function(thisobj,obj, start) {
    for (var i = (start || 0), j = thisobj.length; i < j; i++) {
      if (thisobj[i] === obj) { return i; }
    }
    return -1;
  }
  /**
   * Cross browser startsWth function
   */
  
  function fileViewerStartsWith(str, prefix) {
    return str.indexOf(prefix) === 0;
  };
  

  /**
   * Cross browser compatible endsWith function 
   */
  function fileViewerEndsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
  }
  
  /**
   * Cross browser method to return keys for an object
   */
  
  var fileViewerGetKeys = function(obj){
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
        dapurl : url
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
    
  var viewerType = {
      transfermethod:{
        'opendap':false,
        'http':false
      },
      filetype:{
        'csv':false,
        'png':false,
        'geojson':false,
        'netcdf':false,
        'txt':false
      }
      };

  this.renderFileViewerInterface = function(options){
    $.blockUI.defaults.message='<div class="c4i-fileviewer-loader"></div>';
    $.blockUI.defaults.css.border='none';
    $.blockUI.defaults.overlayCSS.backgroundColor="white";
    var literalDataValueLowerCase = options.prettyquery;
    if( literalDataValueLowerCase.indexOf("http")!=-1 &&
        literalDataValueLowerCase.indexOf(".nc")!=-1){
        viewerType.transfermethod.opendap=true;
        viewerType.filetype.netcdf=true;
    }else if(literalDataValueLowerCase.indexOf("http")!=-1 && 
        fileViewerEndsWith(literalDataValueLowerCase,"csv")){
        viewerType.filetype.csv=true;
        viewerType.transfermethod.http=true;
    }else if(literalDataValueLowerCase.indexOf("http")!=-1 && 
        fileViewerEndsWith(literalDataValueLowerCase,"png")){
      viewerType.filetype.png=true;
      viewerType.transfermethod.http=true;
    }else if(literalDataValueLowerCase.indexOf("http")!=-1 && 
        fileViewerEndsWith(literalDataValueLowerCase,"txt")){
      viewerType.filetype.txt=true;
      viewerType.transfermethod.http=true;
    }else if(literalDataValueLowerCase.indexOf("http")!=-1 && 
        fileViewerEndsWith(literalDataValueLowerCase,".geojson")){
      viewerType.filetype.geojson=true;
      viewerType.transfermethod.opendap=true;
    }
    
    var title = "Fileviewer";
    
    if(viewerType.filetype.netcdf){
      title = 'NetCDF Metadata retrieved via OPeNDAP';
    }
    
    if(viewerType.filetype.geojson){
      title = 'GeoJSON info retrieved via OPeNDAP';
    }
    
    if(viewerType.filetype.csv){
      title = 'CSV table retrieved via HTTP download';
    }
    
    if(viewerType.filetype.png){
      title = 'PNG image retrieved via HTTP download';
    }
    
     
    
    if(options.dialog){
      options.element.dialog({
        title:title,
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
    
    var html='<div class="simplecomponent-container">';
    if(!viewerType.transfermethod.opendap && !viewerType.transfermethod.http){
      html+="Unable to show file info";
    }else if(viewerType.transfermethod.http){
      var subtitle = "";
      if(viewerType.filetype.csv){
        subtitle = "CSV Table";
      }
      if(viewerType.filetype.txt){
        subtitle = "Text file";
      }
      html+='  <div class="simplecomponent c4i-fileviewer-globalmetadata">'+
        '    <div class="simplecomponent-body">' +options.prettyquery+'</div>'+
        '    <div class="simplecomponent-footer"></div>'+
        '  </div>'+
        '  <div class="simplecomponent c4i-fileviewer-variables">'+
        '    <div class="simplecomponent-header">'+subtitle+'</div>'+
        '    <div class="simplecomponent-body c4i-fileviewer-variables-body"></div>'+
        '    <div class="simplecomponent-footer"></div>'+
        '  </div>';
    }else if(viewerType.transfermethod.opendap){
      
      html+='  <div class="simplecomponent c4i-fileviewer-globalmetadata">'+
        '    <div class="simplecomponent-body">' +options.prettyquery+'</div>'+
        '    <div class="simplecomponent-footer"></div>'+
        '  </div>'+
        ''+
        '  <div class="simplecomponent c4i-fileviewer-wmsgetcapabilities">'+
        '    <div class="simplecomponent-header">Geographical information (WMS)</div>'+
        '    <div class="simplecomponent-body c4i-fileviewer-wmsgetcapabilities-body"><img src="../images/ajax-loader.gif"/>Reading GetCapabilities from Web Map Service ... </div>'+
        '    <div class="simplecomponent-footer"></div>'+
        '  </div>'+
        ''+
        '  <div class="simplecomponent c4i-fileviewer-variables">'+
        '    <div class="simplecomponent-header">File metadata and variables</div>'+
        '    <div class="simplecomponent-body c4i-fileviewer-variables-body"></div>'+
        '    <div class="simplecomponent-footer"></div>'+
        '  </div>';
    }
    if(!options.dialog){
      html+='<button class="c4i-fileviewer-downloadbutton">Download</button>'+
        '<button class="c4i-fileviewer-addtobasketbutton">Add to basket</button>'+
        '<button class="c4i-fileviewer-reloadbutton">Reload</button>';
    }
    html+=    '</div>';

    options.element.html(html);
    
    if(viewerType.transfermethod.opendap){
      getVariableInfoFromServerViaOpenDAP();
    }
    if(viewerType.transfermethod.http){
      if(viewerType.filetype.csv || viewerType.filetype.txt){
        getVariableInfoFromServerViaHTTPJSONP();
      }
      if(viewerType.filetype.png){
        
        if(options.dialog){
          var b= [downloadbutton,addtobasketbutton,reloadbutton]
          options.element.dialog("option","buttons",b);
        }else{
          rootElement.find(".c4i-fileviewer-downloadbutton").button(downloadbutton).click(downloadbutton.click);
          rootElement.find(".c4i-fileviewer-addtobasketbutton").button(addtobasketbutton).click(addtobasketbutton.click);
          rootElement.find(".c4i-fileviewer-reloadbutton").button(reloadbutton).click(reloadbutton.click);   
        }
        var html="<img src=\""+options.prettyquery+"\"/>";
        rootElement.find(".c4i-fileviewer-variables").find(".simplecomponent-body").html(html);
      }
    }
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
        var url=options.adagucservice+"source="+URLEncode(options.prettyquery)+ "&service=WMS&request=getmap&format=image/png&layers=baselayer,"+variable.variable+",overlay&width="+maxAllowedWidth+"&CRS=EPSG:4326&STYLES=&EXCEPTIONS=INIMAGE&showlegend=true&";
        url+=Math.random();
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
    
    /* Called when provenance metadata returns */
    var httpCallbackProvenance = function(data){
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
      httpCallbackProvenance(d)
    }).fail(function() {
      //alert("fail 154");
      console.log("Ajax call failed: "+url);
      httpCallbackProvenance({"error":"Request failed for "+url});
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
    console.log('handleErrorMessage',data);
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
    html+='<li><span class="c4i-fileviewer-error-downloadbutton c4i-fileviewer-spanlink" >Open this file directly in your browser to get a hint</span>.</li>';

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
  
  /* Called when metadata returns */
  function httpCallbackMetadata(data){
    if(data.error){
      handleErrorMessage(data);
      return;
    }

 
    
    if(options.dialog){
      var b= [downloadbutton,addtobasketbutton,reloadbutton]
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
      
    
    
    if(viewerType.transfermethod.http){
      console.log(data);
      if(viewerType.filetype.txt){
        function MyHTMLEncode(str){
          var i = str.length,
              aRet = [];

          while (i--) {
            var iC = str[i].charCodeAt();
            if (iC < 65 || iC > 127 || (iC>90 && iC<97)) {
              if(iC == 10)aRet[i] = '<br/>';else aRet[i] = '&#'+iC+';';
            } else {
              aRet[i] = str[i];
            }
           }
          return aRet.join('');
        };
        rootElement.find(".c4i-fileviewer-variables").find(".simplecomponent-body").html(MyHTMLEncode(data.data));
      }
      if(viewerType.filetype.csv){
        var html="";
        html+="<table class=\"drupal\"><tr>";
        var keys = fileViewerGetKeys(data[0]);
        for(var j=0;j<keys.length;j++){
          html+="<th>"+keys[j]+"</th>";
        }
        html+="</tr>";
        for(var j=0;j<data.length;j++){
          html+="<tr>";
          for(var k=0;k<keys.length;k++){
            html+="<td>"+data[j][keys[k]]+"</td>";
          }
          html+="</tr>";
        }
        html+="</table>";
        rootElement.find(".c4i-fileviewer-variables").find(".simplecomponent-body").html(html);
      }
      
      return;
    }
    
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
    rootElement.find(".c4i-fileviewer-variables").find(".simplecomponent-body").find(".c4i-fileviewer-dataset-expandedarea").hide();
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
  
  function httpCallbackWMSCapabilities(layerNames,serviceURL){
    console.log(layerNames);
    if(layerNames.error){
      // alert(layerNames);
      return;
    }
    var layerName = layerNames[0];
    var html="";
    var first = true;
    for(var j=0;j<layerNames.length;j++){
      if(layerNames[j].indexOf('baselayer') ==-1 &&
          layerNames[j].indexOf('overlay') ==-1 &&
          layerNames[j].indexOf('grid') ==-1
          ){
        layerName = layerNames[j];

    
        var layer = new WMJSLayer({service:serviceURL,name:layerName,onReady:function(layer){
          if( first ==true){
            first = false;
            var getCapabiltiesAbstract = layer.WMJSService.abstract;
            html+="<span class=\"c4i-fileviewer-resultitem\">";     
            html+="  <span class=\"c4i-fileviewer-dataset-baseimage c4i-fileviewer-dataset-collapsible c4i-fileviewer-dataset-imgcollapsed \" name=\""+layerName+"\"></span>";
            html+='    <span class="c4i-fileviewer-resultitem-content ">';
            html+="Data abstract";
            html+='    </span>';
            html+="  <span class=\"c4i-fileviewer-dataset-expandedarea\">";
            html+='    <div class="c4i-fileviewer-body">';
            html+="<p>"+getCapabiltiesAbstract+"</p>";
            html+="    </div>";
            html+="  </span>";
            html+="</span>";
            
          }
          html+="<span class=\"c4i-fileviewer-resultitem\">";     
          html+="  <span class=\"c4i-fileviewer-dataset-baseimage c4i-fileviewer-dataset-collapsible c4i-fileviewer-dataset-imgcollapsed \" name=\""+layerName+"\"></span>";
          html+='    <span class="c4i-fileviewer-resultitem-content ">';
          html+=layer.title;
          html+='    </span>';
          html+="  <span class=\"c4i-fileviewer-dataset-expandedarea\">";
          html+='    <div class="c4i-fileviewer-body">';

          html+="<table class='drupal'>";  
         
          html+="<tr><td>variable name / layer name</td><td>"+layer.name+"</td></tr>";
          html+="<tr><td>title</td><td>"+layer.title+"</td></tr>";
          html+="<tr><td>abstract</td><td>"+layer.abstract+"</td></tr>";
          var maxAllowedWidth=300;
          var url=options.adagucservice+"source="+URLEncode(options.prettyquery)+ "&service=WMS&request=getmap&format=image/png&layers=baselayer,"+layer.name+",overlay&width="+maxAllowedWidth+"&CRS=EPSG:4326&STYLES=&EXCEPTIONS=INIMAGE&showlegend=true&";
          url+=Math.random();
          var img='<div class="c4i-fileviewer-previewstyle" name="'+layer.name+'"><span>Preview</span>: <img src="'+url+'"/></div>';
          
          
          console.log(layer);
    //      //Styles
    //      html+="<tr><td>styles</td><td><ul>";
    //      for(var j=0;j<layer.styles.length;j++){
    //        html+="<li>"+layer.styles[j].name+"</li>";
    //      }
    //      html+="</ul></td></tr>";
    //
          //Projections
          html+="<tr><td>Area</td><td><ol>";
          for(var j=0;j<layer.projectionProperties.length;j++){
            if(layer.projectionProperties[j].srs == 'CRS:84'){
              var bbox = layer.projectionProperties[j].bbox;
              html+="<li>LatLon boundingbox: "+layer.projectionProperties[j].bbox.toString()+"</li>";
              html+="<ul><li>left: "+layer.projectionProperties[j].bbox.left+" degrees</li></ul>";
              html+="<ul><li>bottom: "+layer.projectionProperties[j].bbox.bottom+" degrees</li></ul>";
              html+="<ul><li>right: "+layer.projectionProperties[j].bbox.right+" degrees</li></ul>";
              html+="<ul><li>top: "+layer.projectionProperties[j].bbox.top+" degrees</li></ul>";
              html+="</li>";
            }
          }
          html+="</ol></td></tr>";
    
          //Dimensions
          html+="<tr><td>dimensions</td><td><ol>";
          for(var j=0;j<layer.dimensions.length;j++){
            html+="<li>"+layer.dimensions[j].name;
            html+="<ul><li>values: "+layer.dimensions[j].values+"</li></ul>";
            html+="<ul><li>units: "+layer.dimensions[j].units+"</li></ul>";
            html+="<ul><li>number of steps: "+layer.dimensions[j].size()+"</li></ul>";
            html+="<ul><li>Start: "+layer.dimensions[j].getValueForIndex(0)+"</li></ul>";
            html+="<ul><li>Stop: "+layer.dimensions[j].getValueForIndex(layer.dimensions[j].size()-1)+"</li></ul>";
            html+="</li>";
          }
          html+="</ol></td></tr>";
          //html+="<tr><td>preview "+layer.name+"</td><td>"+img+"</td></tr>";
          html+="</table>";
          html+="</div>";
          html+="</span>";
          html+="</span>";
                
        }});

   
        
      }
    }
   
    
    rootElement.find(".c4i-fileviewer-wmsgetcapabilities").find(".simplecomponent-body").html(html);
    rootElement.find(".c4i-fileviewer-wmsgetcapabilities").find(".simplecomponent-body").find(".c4i-fileviewer-dataset-expandedarea").hide();
    
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
  
    rootElement.find(".c4i-fileviewer-wmsgetcapabilities").find(".simplecomponent-body").find(".c4i-fileviewer-resultitem-content").attr('onclick','').click(expandcollapse);
    rootElement.find(".c4i-fileviewer-wmsgetcapabilities").find(".simplecomponent-body").find(".c4i-fileviewer-dataset-collapsible").attr('onclick','').click(expandcollapse);
  };
  
  
  function getVariableInfoFromServerViaHTTPJSONP(){
    var url = options.prettyquery+"?format=application/json";
    $.ajax({
      type: "GET",
      url: url,
      dataType:"jsonp",
      success:function(d) {
        httpCallbackMetadata(d);
      },
      error : function(jqXHR, textStatus, errorThrown) {
        if(jqXHR && jqXHR.responseJSON && jqXHR.responseJSON.error){
          httpCallbackMetadata(jqXHR.responseJSON );
        }else{
          httpCallbackMetadata({"error":"Request failed for "+url});
        }
      }
    });
  }
  
  function getVariableInfoFromServerViaOpenDAP(){
    /* Make WMS GetCapabilites request */
    var WMSGetCapabiltiesURL = options.adagucservice+ 'source='+URLEncode(options.prettyquery);
    
    xml2jsonrequestURL = options.adagucviewerservice+'SERVICE=XML2JSON&';
    
    var service = null;
    try{
    	service = WMJSGetServiceFromStore(WMSGetCapabiltiesURL);
    }catch(e){
    	service = WMJSgetServiceFromStore(WMSGetCapabiltiesURL);
    }
    
    service.getCapabilities(function(){
      service.getLayerNames(
            function(data){httpCallbackWMSCapabilities(data,WMSGetCapabiltiesURL);},
            function(error){console.log(error);});
      },
      function(error){console.log(error);},
      true
    );

    /* Make Metadata request */
    var url = options.service+"service=getvariables&request="+URLEncode(options.prettyquery);
    $.ajax({
      url: url,
          crossDomain:true,
          dataType:"jsonp"
    }).done(function(d) {
      httpCallbackMetadata(d)
    }).fail(function() {
      //alert("fail 154");
      console.log("Ajax call failed: "+url);
      httpCallbackMetadata({"error":"Request failed for "+url});
    }).always(function(){
      
      if(ready){
        ready();
      }
    });
  };
  
  this.renderFileViewerInterface(options);
};
