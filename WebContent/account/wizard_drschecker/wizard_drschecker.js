if (window.location.protocol != "https:")
    window.location.href = "https:" + window.location.href.substring(window.location.protocol.length);  

    var mainWebmapJS ;
    setBaseURL("../adagucviewer/webmapjs");
    
    var visualizeVariable = function(layer,resource){
    	var el=jQuery('<div/>');
        renderFileViewerInterface({element:el,
          service:c4iconfigjs.impactservice,
          adagucservice:c4iconfigjs.adagucservice,
          provenanceservice:c4iconfigjs.provenanceservice,
          adagucviewer:c4iconfigjs.adagucviewer,
          query:resource,
          dialog:true
        });   
    };
    
    var defaultProjection = {srs:'EPSG:4326',bbox:'-180,-90,180,90'};
    //var scaleBarURL       = "http://euro4mvis.knmi.nl/adagucviewer/webmapjs/php/makeScaleBar.php?";
    
    var resource = "Please select a file or paste an opendap URL...";
    //var resource = "http://opendap.knmi.nl/knmi/thredds/dodsC/CLIPC/gsi_nco-4-4-8_CERFACS_multi-platform-tier2v1_day_19890101-20101231.nc";
    
    var urlvars = getUrlVars();
    if(urlvars.resource){
     	resource = urlvars.resource;
    };
    
    
    var activeLayer = "";
    var rootElementId = "first";
    var coordinateRounder = function(original){
      return Math.round(original*1000)/1000;
    };
    
    var boundingBoxBBOX;
    var currentCoverage;
  	var currentProjection = "EPSG:4326";
  	var setNewResource;
  	var showInfo,showFileInfo;
    var dimensionsUpdate = function(layer){
       $("#timeheader").show();
      var dim = layer.getDimension("time");
      $("#timeinfo").html(dim.size() + " dates available from "+dim.getValueForIndex(0)+" till "+dim.getValueForIndex(dim.size()-1));
      function update(index){
        var value = dim.getValueForIndex(index);
        $("#currenttime").html(value);
        layer.parentMaps[0].setDimension("time",value);
        layer.draw();
      }
      $("#timeslider").slider({
        min:0,
        max:dim.size()-1,
        value: 0,                                  
        slide: function( event, ui ) {
          update(ui.value);
          
        }
      });
      update(0);
    }
    
    /* Callback function called when user clicks on the map*/
    var pointOnMapClicked = function(options){
    };

    var bboxChangedByEvent = function(options){
    };
   
    /* Returns a new webmapjs mapping component based on a div element id*/
    var newMap = function(element){
      var webMapJS  = new WMJSMap(document.getElementById(element));
      webMapJS.setProjection(defaultProjection);
      webMapJS.displayLegendInMap(false);
      var baseLayer = new WMJSLayer({
        service:"http://geoservices.knmi.nl/cgi-bin/worldmaps.cgi?",
        name:"world_raster",
        title:"World base layer",
        enabled:true
      });

      var overLayer = new WMJSLayer({
        service:"http://geoservices.knmi.nl/cgi-bin/worldmaps.cgi?",
        name:"world_line",
        title:"World base layer",
        enabled:true,
        keepOnTop:true
      });
      
      var grid = new WMJSLayer({
        service:" http://geoservices.knmi.nl/cgi-bin/SAFNWC_MSG_prod.cgi?",
        name:"grid10",
        title:"World base layer",
        enabled:true,
        keepOnTop:true
      });

      webMapJS.setBaseLayers([baseLayer,overLayer,grid]);
      webMapJS.draw();
      return webMapJS;
    };
    
    /* Document ready function */
    $(function() {
		$.blockUI.defaults.message='<img src="wizard_drschecker/ajax-loader.gif"/>';
		$.blockUI.defaults.css.border='none';
		$.blockUI.defaults.overlayCSS.backgroundColor="white";

    showInfo = function(html,_title){
  	  $("#"+rootElementId).unblock();
 	  	html = html.replace(/(\r\n|\n|\r)/g,"<br/>");
 	 	
   	 	var title = "Processing status";
   	 	if(_title){
   	 		title=_title;
   	 	}
   	 	$("<div class='dialog' title='" + title + "'><p>" + html + "</p></div>").dialog({
          width:600,
          height:400
        });
      };
      
      $("#timeheader").hide();
      $("#startcalculation").button({icons: { primary: "ui-icon-search"}}).hide();
      $("#results").hide();
      $(".c4i-wizard-drschecker-map").hide();
 
      var base64DecodeURLEncode = function(data){
        var d =(decodeBase64(data.substring("base64:".length)));
        d = d.replaceAll("<","&lt;");
        d = d.replaceAll(">","&gt;");
        d=d.replaceAll("\n","<br/>");
        return d;
      }
      
      /*WPS finished callback*/
      var wpsComplete = function(data){
        console.log(data);
        $("#startcalculation").show();
        $('#progressbar').hide();
        var visualizeLink = "";
        var logmessages = undefined,errors = undefined,goodorbad = undefined,DatasetDRS = undefined,nroferrors = undefined,FilenameDRS = undefined;
        
        for(var j=0;j<data.length;j++){
          var identifier = data[j]["ows:Identifier"].value;
          var outputdata= data[j]["wps:Data"]["wps:LiteralData"].value;
          if(identifier == "logmessages")logmessages=outputdata;
          if(identifier == "errors")errors=outputdata;
          if(identifier == "goodorbad")goodorbad=outputdata;
          if(identifier == "DatasetDRS")DatasetDRS=outputdata;
          if(identifier == "nroferrors")nroferrors=outputdata;
          if(identifier == "FilenameDRS")FilenameDRS=outputdata;
        }
        
        var html = "";
        
        
        if(goodorbad=="ERROR"){
          html+="<div class=\"c4i-wizard-drschecker-results-error\">ERROR: Your file is not compliant with the CLIPC DRS standard!<br/>"+nroferrors+" error(s) found.</div>";
          html+="<div class=\"c4i-wizard-drschecker-results-errors\"><span class=\"c4i-wizard-drschecker-results-errormessagestitle\"></span>";
          html+=base64DecodeURLEncode(errors)+"</div>";
        }else{
          html+="<div class=\"c4i-wizard-drschecker-results-good\">Your file is compliant with the CLIPC DRS metadata standards.<br/>Well done!</div>";
        }
        
        
        html+="<div class=\"c4i-wizard-drschecker-results-DatasetDRS\"><span class=\"c4i-wizard-drschecker-results-datasetdrstitle\">Derived DatasetDRS:</span>";
        html+=base64DecodeURLEncode(DatasetDRS)+"</div>";
        html+="<div class=\"c4i-wizard-drschecker-results-FilenameDRS\"><span class=\"c4i-wizard-drschecker-results-filenamedrstitle\">Derived FilenameDRS:</span>";
        html+=base64DecodeURLEncode(FilenameDRS)+"</div>";
        
        var logmessageshtml=base64DecodeURLEncode(logmessages);
        logmessageshtml = logmessageshtml.replaceAll("[OK]","[OK-C4I]");
        logmessageshtml = logmessageshtml.replaceAll("[ERROR]","[ERROR-C4I]");
        logmessageshtml = logmessageshtml.replaceAll("[INFO]","[INFO-C4I]");
        logmessageshtml = logmessageshtml.replaceAll("[DRSFilename]","[INFO-DRSFilename]");
        logmessageshtml = logmessageshtml.replaceAll("[DRSDatasetname]","[INFO-DRSDatasetname]");
        logmessageshtml = logmessageshtml.replaceAll("[OK-C4I]","<span class=\"c4i-wizard-drschecker-results-OK\">[OK]</span>");
        logmessageshtml = logmessageshtml.replaceAll("[ERROR-C4I]","<span class=\"c4i-wizard-drschecker-results-ERROR\">[ERROR]</span>");
        logmessageshtml = logmessageshtml.replaceAll("[INFO-C4I]","<span class=\"c4i-wizard-drschecker-results-INFO\">[INFO]</span>");
        logmessageshtml = logmessageshtml.replaceAll("[INFO-DRSFilename]","<span class=\"c4i-wizard-drschecker-results-FILENAMEDRSSPAN\">[FILENAME DRS]</span>");
        logmessageshtml = logmessageshtml.replaceAll("[INFO-DRSDatasetname]","<span class=\"c4i-wizard-drschecker-results-DATASETDRSSPAN\">[DATASET DRS]</span>");
        
        html+="<div class=\"c4i-wizard-drschecker-results-logmessages\">Log:<br/>"+logmessageshtml+"</div>";
        $(".c4i-wizard-drschecker-results").html(html);
      };
      
      /*Start button function*/
      $("#startcalculation").click(function(){
        $(".c4i-wizard-drschecker-results").html('<div class="ajaxloader"></div>');
        $("#startcalculation").hide();
        /*WPS progress callback*/
        var wpsProgress = function(percentCompleted,message){
          var progressLabel = $( ".progress-label" );
          if(!percentCompleted)percentCompleted = 0;
          if(!message)message = "";
          $( "#progressbar" ).progressbar({
            value: percentCompleted
          }).show();
          progressLabel.text(message+"("+percentCompleted + "%)" );
        };
        
        var wpsFailure = function(message){
          $("#startcalculation").show();
          $('#progressbar').hide();
          console.log(message);
          if(message.statuscode){
        	  if(message.statuscode == 401){
        		  generateLoginDialog();
        		  return;
        	  }
          }
          showInfo('Processing failed: '+message.message);
        };
        
        /*Create new processing object*/
        var wps = new WMJSProcessing({
          url:WPSURL,
          success:wpsComplete,
          progress:wpsProgress,
          failure:wpsFailure
        });
        wps.execute('clipc_drschecker',{'resource':resource});
      });
      
      
      

      /*Create new mapping component*/
      mainWebmapJS = new newMap('webmap1');
      mainWebmapJS.addListener('mouseclicked',pointOnMapClicked,true);
      mainWebmapJS.addListener('bboxchanged',bboxChangedByEvent,true);
      mainWebmapJS.closeAllGFIDialogs();
      mainWebmapJS.enableInlineGetFeatureInfo(false);
    
      var setLayer = function(service,n){
        activeLayer = n;
        
        mainWebmapJS.removeAllLayers();
        
        var layer = new WMJSLayer({service:service.service,name:n});
        layer.onReady = function(){
        	console.log("Layer ready");
        	mainWebmapJS.draw();
        	//showInfo('slfksl;fkasl;fkas;fkasfk');
          var failed = function(e){
            showInfo(e);
          }
          
          /*Make WCS Request*/
          WCJSRequest(layer.service,layer.name,function(jsonDoc){
            currentCoverage = parseDescribeCoverage(jsonDoc);
            var html = "";
            html+="<select name=\"projectioncombo\" class=\"projcombo\">";
            for(var j=0;j<currentCoverage.supportedProjections.length;j++){
              if(currentCoverage.supportedProjections[j].srs=="EPSG:4326"){
           	    currentProjection = "EPSG:4326";
                html+="<option selected>"+currentCoverage.supportedProjections[j].srs+"</option>";
                setProjection(layer,currentCoverage.supportedProjections[j].srs);
              }else{
                html+="<option>"+currentCoverage.supportedProjections[j].srs+"</option>";
              }
              var startdate = "";
              var stopdate = "";
              var timeRes = "";
              try{
                startdate = layer.getDimension("time").getValueForIndex(0);
                timeRes = "P1D";
              }catch(e){
              }
              
              
              
              try{
                stopdate = layer.getDimension("time").getValueForIndex(layer.getDimension("time").size()-1);
                var v = layer.getDimension("time").getValueForIndex(layer.getDimension("time").size());
                if(v){
                  if(v.length>10){
                    stopdate = v;
                  }
                }
              }catch(e){
              }
              $("#"+rootElementId).find(".startdate").first().val(startdate);
              $("#"+rootElementId).find(".stopdate").first().val(stopdate);
              
              try{
                if( layer.getDimension("time").values.split("/").length==3){
                  timeRes = layer.getDimension("time").values.split("/")[2];
                }
              }catch(e){}
              $("#"+rootElementId).find(".timeresolution").first().val(timeRes);
            }
            html+="</select>";
            $("#"+rootElementId).find(".projectionselector").first().html(html);
            $("#"+rootElementId).find(".projectionselector").find(".projcombo").first().on('change',function(){setProjection(layer,this.value);} );
            
  
            
            
            mainWebmapJS.draw();
            $("#startcalculation").show();
          },failed);
          
        };
        console.log("Adding layer"+layer.name)
        mainWebmapJS.addLayer(layer);
      };
      
      
      var setProjection = function (layer,srsname) {
        mainWebmapJS.hideBoundingBox();
        var srs = layer.getProjection(srsname);      
        currentProjection = srsname;
        mainWebmapJS.setProjection(srs);
        mainWebmapJS.zoomOut();
      
        
        function d(){
       
          mainWebmapJS.showBoundingBox(srs.bbox);
        };
        mainWebmapJS.addListener('onmapready',d,false);
        
        mainWebmapJS.draw();
        
        boundingBoxBBOX = srs.bbox;

        $("#"+rootElementId).find(".bboxwest").val(srs.bbox.left);
        $("#"+rootElementId).find(".bboxnorth").val(srs.bbox.top);
        $("#"+rootElementId).find(".bboxeast").val(srs.bbox.right);
        $("#"+rootElementId).find(".bboxsouth").val(srs.bbox.bottom);
        
        var resx=((boundingBoxBBOX.right-boundingBoxBBOX.left)/currentCoverage.width);//if(resx<0)resx=-resx;
        var resy=((boundingBoxBBOX.top-boundingBoxBBOX.bottom)/currentCoverage.height);//if(resy<0)resy=-resy;
        $("#"+rootElementId).find(".resolutionx").val(resx);
        $("#"+rootElementId).find(".resolutiony").val(resy);
        
        bboxChangedByEvent(srs);
        //$("#"+rootElementId).find(".resolutionxinfo").html(parseInt(Math.abs((srs.bbox.right-srs.bbox.left)/resx)+0.5));
        //$("#"+rootElementId).find(".resolutionyinfo").html(parseInt(Math.abs((srs.bbox.bottom-srs.bbox.top)/resy)+0.5));
      };
      
      var WMSReady = function(l,service){
        $(".c4i-wizard-drschecker-map").show();

        var html =  "<select class=\"coveragecombo\">";
        var selectedLayer = undefined;
        for(var j=0;j<l.length;j++){
          var n = l[j];
          if(n!='baselayer'&&n!='overlay'&&n!='grid10'){
            if(selectedLayer == undefined){
              html+="<option selected>"+n+"</option>";
              selectedLayer = n;
              setLayer(service,n);
            }else{
              html+="<option>"+n+"</option>";
            }
            
          }
        }
        html+="</select>";
        $("#"+rootElementId).find(".coverage").html(html);
        
        //mainWebmapJS.draw();
      };
      
      

      
      var bboxChangedByInput = function(){
        var value = $(this).val();
        var className = $(this).attr('class');
        if(className.indexOf("west")>0)boundingBoxBBOX.left = parseFloat(value);
        if(className.indexOf("north")>0)boundingBoxBBOX.top= parseFloat(value);
        if(className.indexOf("east")>0)boundingBoxBBOX.right = parseFloat(value);
        if(className.indexOf("south")>0)boundingBoxBBOX.bottom = parseFloat(value);
        mainWebmapJS.showBoundingBox(boundingBoxBBOX);
        bboxChangedByEvent({bbox:boundingBoxBBOX});
      };
      
      $("#"+rootElementId).find(".bboxwest").change(bboxChangedByInput);
      $("#"+rootElementId).find(".bboxnorth").change(bboxChangedByInput);
      $("#"+rootElementId).find(".bboxeast").change(bboxChangedByInput);
      $("#"+rootElementId).find(".bboxsouth").change(bboxChangedByInput);
      
      $("#"+rootElementId).find(".resolutionx").change(bboxChangedByInput);
      $("#"+rootElementId).find(".resolutiony").change(bboxChangedByInput);
      
      $("#"+rootElementId).find(".outputFormat").change(function(){
        var format= $("#"+rootElementId).find(".outputFormat").val();
        var filename= $("#"+rootElementId).find(".outputFileName").val();
        var extplace = filename.lastIndexOf(".");
        if(extplace>0)filename = filename.substring(0,extplace);
        
        if(format == 'netcdf')filename = filename + ".nc";else filename = filename + ".zip";                                                        
        $("#"+rootElementId).find(".outputFileName").val(filename);
      });
      
      $("#"+rootElementId).find(".coverage").change(function(){
        var coverage= $("#"+rootElementId).find(".coveragecombo").val();
        console.log("coverage");
        setLayer(s,coverage);
      });
      
      $("#"+rootElementId).find(".resource").val(resource);
      
      
      
      $("#"+rootElementId).find(".coverage").change(function(){
    	  setNewResource();
      });

      setNewResource = function(_resource){
        $(".c4i-wizard-drschecker-results").html("");
    	  if(isDefined(_resource)){
				$("#"+rootElementId).find(".resource").val(_resource);
    	  }
    	  resource = $("#"+rootElementId).find(".resource").val();
    	  
    	  resource = resource.trim();
    	  if(resource.indexOf("#")>0){
    		  resource= resource.split("#")[0];
    	  }
    	  
    	  if(resource.length>0){
    	    if(resource.indexOf("http")==0){
    	    	$("#"+rootElementId).block();
    	    
		    	  //console.log(resource);
		          try{
		          	s = WMJSgetServiceFromStore('/impactportal/ImpactService?source='+resource+'&');
		          	s.getLayerNames(function(d){$("#"+rootElementId).unblock();WMSReady(d,s);},showInfo);
		          }catch(e){
		        	$("#"+rootElementId).unblock();
		        	showInfo(e)
		          }
   	  		}
    	  }
      }
      
      $("#"+rootElementId).find(".resource").keypress(function(event) {
          if (event.keyCode == 13) {
        	  setNewResource();
          }
      });
     
      
      setNewResource();
      
      $(".c4i_wizard_drschecker_helpbutton").button({
          
          icons: {
            primary: "ui-icon-help"
          },
        }).click(function(){
          var el = jQuery('<div title="Help" class="headerhelpdiv"></div>', {}).dialog({
            width:800,
            height:400,
            modal:true
          });
          el.html('<div class="ajaxloader"></div>');
          var helpReturned = function(data){
            el.html(data);    
          }
          $.ajax({
            url: "./wizard_drschecker/wizard_drschecker_help.html"     
          }).done(function(d) {
            helpReturned(d)
          })
        });
      
	      showFileInfo = function(){
	    	var html="No layer selected";
	    	try{
		    	if(s){
			    	var layer = mainWebmapJS.getLayerByServiceAndName(s.service,activeLayer);
			    	
			    	if(layer){
				    	var dim = layer.getDimension("time");
				    	html="<b>Dates:</b><br/>";
				       	html+=dim.size() + " dates available from "+dim.getValueForIndex(0)+" till "+dim.getValueForIndex(dim.size()-1)+"<br/>";
				       	
				       	
			    	}
		    	}
	    	}catch(e){}
	      	showInfo(html,'File info');
	      	
	      };
    });
    
    var showBasketWidget= function(){
	  console.log("showBasketWidget");
      basketWidget.show(function(selectedNodes) {
        //console.log(selectedNodes);
	    for ( var j = 0; j < selectedNodes.length && j<1; j++) {
          setNewResource(selectedNodes[j].dapurl);
	    }
	    return true;
	  });
    };
    
 