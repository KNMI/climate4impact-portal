    var mainWebmapJS ;
    setBaseURL("../adagucviewer/webmapjs");
    
    var visualizeVariable = function(layer,resource){
    	var el=jQuery('<div/>');
        renderFileViewerInterface({element:el,
          service:c4iconfigjs.impactservice,
          adagucservice:c4iconfigjs.adagucservice,
          adagucviewer:c4iconfigjs.adagucviewer,
          //query:"http://opendap.knmi.nl/knmi/thredds/dodsC/CLIPC/jrc/tier2/SPI3.nc",
          query:resource,
          dialog:true
        });   
    };
    
    //var defaultProjection = {srs:'EPSG:3857',bbox:'13238.944477686076,6372693.810359594,1163089.5564179858,7299992.69095661'};
    //var defaultProjection = {srs:'EPSG:28992',bbox:'-11207.614738397242,269926.44380811695,318293.5111561029,665327.7948815171'};
    var defaultProjection = {srs:'EPSG:4326',bbox:'-180,-90,180,90'};
    //var scaleBarURL       = "http://euro4mvis.knmi.nl/adagucviewer/webmapjs/php/makeScaleBar.php?";
    
    
    //var WPSURL = "http://bhw485.knmi.nl:8080/cgi-bin/wps.cgi?";
    var resource = "Please select a file...";//'http://opendap.knmi.nl/knmi/thredds/dodsC/e-obs_0.25regular/tg_0.25deg_reg_v11.0.nc';
    
    var urlvars = getUrlVars();
    if(urlvars.resource){
     	resource = urlvars.resource;
    	//console.log(resource);
    };
    
    
    var activeLayer = "";
    //resource = 'http://opendap.knmi.nl/knmi/thredds/dodsC/CLIPC/storyline_urbanheat/clipcstorylinedata/test/RADNL_OPER_R___25PCPRR_L3__20120827T171000_20120827T171500_0001.nc';
    //resource = 'http://msgcpp-ogc-realtime.knmi.nl/thredds-rt/dodsC/REALTIME_SEVIR_OPER_R___MSGCPP__L2/SEVIR_OPER_R___MSGCPP__L2__20150520T120000_20150520T121500_0001.nc';
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
      });//('bla'+dim.size());
      update(0);
    }
    
    /* Callback function called when user clicks on the map*/
    var pointOnMapClicked = function(options){
//       var m = options.map;
//       var x = options.x;
//       var y = options.y;
//       
//      
//       var lalo=m.getLatLongFromPixelCoord({x: x, y: y});
//       var geo=m.getGeoCoordFromPixelCoord({x: x, y: y});
//       
//       $("#calcinx").val(coordinateRounder(lalo.x));
//       $("#calciny").val(coordinateRounder(lalo.y));
//      // showInfo('You clicked at ('+coordinateRounder(lalo.x)+", "+coordinateRounder(lalo.y)+')');
      
    };
    var bboxChangedByEvent = function(options){
     // console.log(options);
        
      $("#"+rootElementId).find(".bboxwest").val(options.bbox.left);
      $("#"+rootElementId).find(".bboxnorth").val(options.bbox.top);
      $("#"+rootElementId).find(".bboxeast").val(options.bbox.right);
      $("#"+rootElementId).find(".bboxsouth").val(options.bbox.bottom);
      

      $("#"+rootElementId).find(".resolutionxinfo").html(parseInt(Math.abs((options.bbox.right-options.bbox.left)/$("#"+rootElementId).find(".resolutionx").val())+0.5));
      $("#"+rootElementId).find(".resolutionyinfo").html(parseInt(Math.abs((options.bbox.bottom-options.bbox.top)/$("#"+rootElementId).find(".resolutiony").val())+0.5));
      
      
    };
    
 
  
    
   
    
   
    /* Returns a new webmapjs mapping component based on a div element id*/
    var newMap = function(element){
      var webMapJS  = new WMJSMap(document.getElementById(element));
      webMapJS.setProjection(defaultProjection);
      webMapJS.displayLegendInMap(false);
      var baseLayer = new WMJSLayer({
        //service:"http://birdexp03.knmi.nl/cgi-bin/plieger/wmst.cgi?",
        //name:"satellite",
        service:"http://geoservices.knmi.nl/cgi-bin/worldmaps.cgi?",
        name:"world_raster",
        title:"World base layer",
        enabled:true
      });
      var overLayer = new WMJSLayer({
        //service:"http://birdexp03.knmi.nl/cgi-bin/plieger/wmst.cgi?",
        //name:"satellite",
        service:"http://geoservices.knmi.nl/cgi-bin/worldmaps.cgi?",
        name:"world_line",
        title:"World base layer",
        enabled:true,
        keepOnTop:true
      });
            var grid = new WMJSLayer({
        //service:"http://birdexp03.knmi.nl/cgi-bin/plieger/wmst.cgi?",
        //name:"satellite",
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
		$.blockUI.defaults.message='<img src="wizard_convert/ajax-loader.gif"/>';
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
      
      //showInfo("Choose a location and click start to run processing");
      $("#timeheader").hide();
      $("#startcalculation").hide();
      $("#results").hide();
      /*WPS finished callback*/
      var wpsComplete = function(data){
        $("#startcalculation").show();
        $('#progressbar').hide();
        var visualizeLink = "";
        if(data.lastIndexOf(".nc")==data.length-3){
        	visualizeLink = '<br/><br/>Or <a href="#" onclick="visualizeVariable(\''+activeLayer+'\',\''+data+'\')">visualize</a> directly.<br/><br/>';	
        }
        
        showInfo('<h1>Succesfully completed!</h1>The results are stored in your basket.<br/><br/>'+
        		 'You can download the result directly <a href="'+data+'">here</a> directly.<br/>'+
        		 visualizeLink+
        		 '<br/>The link to your file is<br/><br/>'+data);
        
//         var layerFailed= function(layer,message){
//           showInfo("Operation completed, but WMS failed: "+message);
//         };
//         
//         var layer =  new WMJSLayer({
//           service:data,
//           name:"raypath",
//           failure:layerFailed,
//           style:'alphafade/volume'
//         });
//         layer.onReady = function(){
//           
//           showInfo('Succesfully completed for location ('+ $("#calcinx").val()+", "+$("#calciny").val()+') and height ('+ $("#calcinheight").val()+')');
//           dimensionsUpdate(layer);
//           updateLayerList();
//           
//           mainWebmapJS.draw();
//         };
//         mainWebmapJS.addLayer(layer);
      };
      
      /*Start button function*/
      $("#startcalculation").click(function(){
        //showInfo("bla");
        //return;
        //mainWebmapJS.positionMapPinByLatLon({x:$("#calcinx").val(),y:$("#calciny").val()});
        mainWebmapJS.setMapPin(mainWebmapJS.getPixelCoordFromLatLong({x:$("#calcinx").val(),y:$("#calciny").val()}));
        mainWebmapJS.showMapPin();
        $("#startcalculation").hide();
        
      
        //showInfo('Operation started at location ('+ $("#calcinx").val()+", "+$("#calciny").val()+') and height ('+ $("#calcinheight").val()+')');
        

        
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
        
        var dates = $("#"+rootElementId).find(".startdate").first().val()+"/"+
                    $("#"+rootElementId).find(".stopdate").first().val() +"/"+
                    $("#"+rootElementId).find(".timeresolution").first().val();
                    
      //service=processor&request=executeProcessor&id=WCS_subsetting&dataInputs=[crs=EPSG:4326;dates=2076-01-01T12:00:00Z/2076-02-01T12:00:00Z/P1D;resy=1;resource=http://opendap.knmi.nl/knmi/thredds/dodsC/IS-ENES/TESTSETS/tasmax_day_EC-EARTH_rcp26_r8i1p1_20760101-21001231.nc;outputFormat=netcdf;resx=1;outputFileName=wcs.nc;bbox=-180,-90,180,90;coverage=tasmax]
        wps.execute('WCS_subsetting',
                    {'dates':dates,
                      'resx':$("#"+rootElementId).find(".resolutionx").val(),
                      'resy':$("#"+rootElementId).find(".resolutionx").val(),
                      'bbox':[boundingBoxBBOX.left,boundingBoxBBOX.top,boundingBoxBBOX.right,boundingBoxBBOX.bottom],
                      'resource':resource,
                      'outputFormat':$("#"+rootElementId).find(".outputFormat").val(),
                      'outputFileName':$("#"+rootElementId).find(".outputFileName").val(),
                      'coverage':activeLayer,
                      'crs':currentProjection
                    
                    });
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
      
      $(".c4i_wizard_convert_helpbutton").button({
          
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
            url: "./wizard_convert/wizard_convert_help.html"     
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

      /* Debug for WMS */
//       wpsComplete("http://birdexp02.knmi.nl/cgi-bin/adaguc/dragonsdenwms.cgi?DATASET=WPS_raypath_1400073951_wmsconfig_201405140000&");
//       wpsComplete("http://birdexp02.knmi.nl/cgi-bin/adaguc/dragonsdenwms.cgi?DATASET=WPS_raypath_1400073951_wmsconfig_201405140000&");
    });
    
    var showBasketWidget= function(){
	  console.log("showBasketWidget");
      basketWidget.show(function(selectedNodes) {
	    for ( var j = 0; j < selectedNodes.length && j<1; j++) {
          setNewResource(selectedNodes[j].data.dapurl);
	    }
	    return true;
	  });
    };
    
 