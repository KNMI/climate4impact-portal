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
  
  newoptions.service=c4iconfigjs.impactservice;
  newoptions.adagucservice=c4iconfigjs.adagucservice;
  newoptions.adagucviewer=c4iconfigjs.adagucviewer;
  newoptions.howtologinlink=c4iconfigjs.howtologinlink;
  newoptions.contactexpertlink=c4iconfigjs.contactexpertlink;
  newoptions.query=options.url;
  newoptions.dialog=true;
  newoptions.element=jQuery('<div/>');
  return new FileViewerInterface(newoptions);
};



var FileViewerInterface = function(options){
  var _this = this;
  
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
    icons: { primary: "icon-download"},
    click: function( event ) {
      event.preventDefault();
      _this.openDownloadWindow();
    }
  };
  
  var addtobasketbutton = { 
    text:"Add to basket",
    icons: { primary: "icon-shoppingbasket"},
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
    icons: { primary: "icon-reload"},
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
        width:800,
        height:600,
        dialogClass:'c4i-fileviewer-containerdialog'
      });
      
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
  
  function checkForUsableVariable(variable){
      if(variable.variable == "lon"){return "";}
      if(variable.variable == "lat"){return "";}
      if(variable.variable == "x"){return "";}
      if(variable.variable == "y"){return "";}
      if(variable.dimensions&&variable.isViewable==1){
      if(variable.dimensions.length>1){
        for(var d=0;d<variable.dimensions.length;d++){
          if(variable.dimensions[d].name.indexOf("bnds")!=-1)return "";
        }
        var url=options.adagucservice+"source="+URLEncode(options.prettyquery)+ "&service=WMS&request=getmap&format=image/png&layers=baselayer,"+variable.variable+",overlay,grid10&width=390&height=260&CRS=EPSG:4326&STYLES=&EXCEPTIONS=INIMAGE&showlegend=true";
        var html='<div class="c4i-fileviewer-previewstyle"><span>Preview</span>: <img src="'+url+'"/></div>';
        return html;
      }
    }
    return "";
  };
  
  
  function handleErrorMessage(data){
    var html=
    
    '<div class="c4-fileviewer-variable-error">'+
      '<b>'+data.error+'</b><br/>'+
      'You can try the following:<ul>'+
      '<li><span class="c4i-fileviewer-error-signinbutton c4i-fileviewer-spanlink" >Sign in</span></li>'+
      '<li><span class="c4i-fileviewer-error-reloadbutton c4i-fileviewer-spanlink" >Reload</span></li>'+
      '<li><a class="c4i-fileviewer-spanlink" target="_blank" href="'+options.contactexpertlink+'">Request help</a>.</li>'+
      '<li>If you are signed in but still cannot view the data, make sure your account is registered to the right group: <a class="c4i-fileviewer-spanlink" target="_blank" href="'+options.howtologinlink+'">-> HowTo</a>.</li>'+
      '<li>The list of available groups can be found here: <a class="c4i-fileviewer-spanlink" target="_blank" href="https://esgf-node.jpl.nasa.gov/ac/list/">List of ESGF groups</a></li>'+
      '<li>Open this file directly in your browser  <span class="c4i-fileviewer-error-downloadbutton c4i-fileviewer-spanlink" >here</span>, you might get a hint on what is going wrong.</li>'+
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
    '    <div class="simplecomponent-body"></div>'+
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
      
    var html="";
    for(var v=0;v<data.length;v++){
      var preview=checkForUsableVariable(data[v]);

      html+="<span class=\"c4i-fileviewer-resultitem\">";     
      html+="  <span class=\"c4i-fileviewer-dataset-baseimage c4i-fileviewer-dataset-collapsible c4i-fileviewer-dataset-imgcollapsed \" name=\""+data[v].variable+"\"></span>";
    
     
      html+='    <span class="c4i-fileviewer-resultitem-content ">';
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
      }if(preview.length>0)
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
        html+="      <tr><td class=\"c4i-fileviewer-table-td-name\">"+data[v].attributes[j].name+"</td><td class=\"c4i-fileviewer-table-td-value\">"+ data[v].attributes[j].value+"</td></tr>";
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
        
        var adagucViewerAddLayer = $('<span />').addClass('c4i-fileviewer-adagucview').html('Add to viewer');serviceOptions.append(adagucViewerAddLayer);
        var WMS = $('<span />').addClass('c4i-fileviewer-wms').html(
          '<a target="_blank" href="'+options.adagucservice+ 'source='+URLEncode(options.prettyquery)+'&service=WMS&request=GetCapabilities">WMS</a>');
        
        serviceOptions.append(WMS);
        var WCS = $('<span />').addClass('c4i-fileviewer-wcs').html(
          '<a target="_blank" href="'+options.adagucservice+ 'source='+URLEncode(options.prettyquery)+'&service=WCS&request=GetCapabilities">WCS</a>');
        serviceOptions.append(WCS);
        
        var opendap = $('<span />').addClass('c4i-fileviewer-opendap').html(
            '<a target="_blank" href="'+options.prettyquery+'.das">opendap</a>');
        serviceOptions.append(opendap);
        
        var variable = data[v].variable;
        
        adagucViewerAddLayer.attr('onclick','').click(function(event){
          event.preventDefault();          
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
      httpCallback("Failed for "+arg);
    }).always(function(){
      
      if(ready){
        ready();
      }
    });
  };
  
  this.renderFileViewerInterface(options);
};
