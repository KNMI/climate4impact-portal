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
    
    //var defaultProjection = {srs:'EPSG:3857',bbox:'13238.944477686076,6372693.810359594,1163089.5564179858,7299992.69095661'};
    //var defaultProjection = {srs:'EPSG:28992',bbox:'-11207.614738397242,269926.44380811695,318293.5111561029,665327.7948815171'};
    var defaultProjection = {srs:'EPSG:4326',bbox:'-180,-90,180,90'};
    var scaleBarURL       = c4iconfigjs.adagucservice+"service=WMS&request=getscalebar&"
    
    
    //var WPSURL = "http://bhw485.knmi.nl:8080/cgi-bin/wps.cgi?";
    var resource = "Please select a file...";
    
    var wpsProcessSubsettingName='WCS_subsetting';
    
    var urlvars = getUrlVars();
    if(urlvars.resource){
     	resource = urlvars.resource;
    	//console.log(resource);
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
      var bbox;
      if(options.bbox){
        bbox=options.bbox;
      }else{
        bbox=options;
      }
      $("#"+rootElementId).find(".bboxwest").val(bbox.left);
      $("#"+rootElementId).find(".bboxnorth").val(bbox.top);
      $("#"+rootElementId).find(".bboxeast").val(bbox.right);
      $("#"+rootElementId).find(".bboxsouth").val(bbox.bottom);
      

      $("#"+rootElementId).find(".resolutionxinfo").html(parseInt(Math.abs((bbox.right-bbox.left)/$("#"+rootElementId).find(".resolutionx").val())+0.5));
      $("#"+rootElementId).find(".resolutionyinfo").html(parseInt(Math.abs((bbox.bottom-bbox.top)/$("#"+rootElementId).find(".resolutiony").val())+0.5));
      boundingBoxBBOX = bbox;
      mainWebmapJS.showBoundingBox(bbox);

    };
    
 
  
    
   
    
   
    /* Returns a new webmapjs mapping component based on a div element id*/
    var newMap = function(element){
      var webMapJS  = new WMJSMap(document.getElementById(element));
      webMapJS.setProjection(defaultProjection);
      webMapJS.displayLegendInMap(false);
      var baseLayer = new WMJSLayer({
        service:c4iconfigjs.adagucservice,
        name:"baselayer",
        enabled:true
      });
      var overLayer = new WMJSLayer({
        service:c4iconfigjs.adagucservice,
        name:"overlay",
        enabled:true,
        keepOnTop:true
      });
      var grid = new WMJSLayer({
        service:c4iconfigjs.adagucservice,
        name:"grid10",
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
      $("button").button();
      $("#startcalculation").button({icons:{primary:'codejobsicon'}}).hide();
      
      $("#c4i_wizard_convert_savetemplate").button({icons:{primary:'ui-icon-star'}}).hide();
      $("#c4i_wizard_convert_loadtemplate").button({icons:{primary:'ui-icon-arrowthickstop-1-s'}}).click(function(){
      
          basketWidget.show(function(selectedNodes) {
          if(selectedNodes.length!=1){
            alert("Please select one settings file");
            return true;
          }

          //console.log(selectedNodes[0]);
          if(selectedNodes[0].hashttp!="true"){
            alert("This file is not http enabled");
            return;
          }
          
          var fileLocation = selectedNodes[0].httpurl;
          
          if(fileLocation.indexOf(".wpssettings")==-1){
            alert("Please select a file with the wpssettings extension");
            return;
          }
          
          var url = c4iconfigjs.basketservice+"service=basket&request=getfile&file="+encodeURIComponent(fileLocation);
          
          //console.log(url);
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
            //console.log(data.data.wpspostdata.Execute);
            var identifier = data.data.wpspostdata.Execute.Identifier.value;
            var inputs = data.data.wpspostdata.Execute.DataInputs.Input;
            if(wpsProcessSubsettingName!=identifier){
              alert("This settings file was used for process "+identifier+".");
            }
            //console.log(inputs);
            
            var bbox = "";
            var srsname = "";
            var dates = "";
            var resx="";
            var resy="";
            
              
            for(var j=0;j<inputs.length;j++){
              var name = inputs[j].Identifier.value ;
              var value = inputs[j].Data.LiteralData.value;
              //console.log("name:"+name+"="+value);
              if(name =="bbox"){if(bbox.length>0)bbox+=",";bbox+=value;}
              if(name =="crs")srsname=value;
              if(name =="dates")dates=value;
              if(name =="resx")resx=value;
              if(name =="resy")resy=value;
            }
      
            dates = dates.split("/");
            bbox = bbox.split(",");
            var bboxObj = new WMJSBBOX(parseFloat(bbox[0]),parseFloat(bbox[3]),parseFloat(bbox[2]),parseFloat(bbox[1]));
            
            //console.log("startdate:"+dates[0]);
            $("#"+rootElementId).find(".startdate").val(dates[0]);
            $("#"+rootElementId).find(".stopdate").val(dates[1]);
            $("#"+rootElementId).find(".stopdate").change();
            var layer = mainWebmapJS.getLayers()[0];
           
            
            $("#"+rootElementId).find(".projectionselector").find(".projcombo").first().val(srsname);
            //console.log(bboxObj.toString());
          
            var srs = layer.getProjection(srsname);
            //console.log(srs);
            currentProjection = srsname;
            var newSRS={bbox:bboxObj,srs:srs.srs};
            //console.log(newSRS);
            setProjectionDirectly(newSRS);
            $("#"+rootElementId).find(".resolutionx").val(resx);
            $("#"+rootElementId).find(".resolutiony").val(resy);
            bboxChangedByEvent(newSRS);
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
          });
          
          //https://bhw485.knmi.nl:9443/impactportal/basket?&service=basket&request=getfile&file=https%3A%2F%2Fbhw485.knmi.nl%3A9443%2Fimpactportal%2FDAP%2Fceda.ac.uk.openid.Maarten.Plieger%2Ftest.wpssettings
          
              

          return true;
        });
      
      });
     
      
      $("#results").hide();
      /*WPS finished callback*/
      var wpsComplete = function(_data){
        var namespace = 'wps:';
        var data  = _data[namespace+'Data'][namespace+'LiteralData'].value;
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
        var service = WMJSgetServiceFromStore('/impactportal/ImpactService?source='+resource+'&');
        var layer = mainWebmapJS.getLayerByServiceAndName(service.service,activeLayer);
        var numberOfSteps = calculateAndCheckNumberOfStepsToProcess(layer);
        if(numberOfSteps<=0)return;
        var width = parseInt($("#"+rootElementId).find(".resolutionxinfo").html());
        var height = parseInt($("#"+rootElementId).find(".resolutionyinfo").html());
        var nrOfElements = numberOfSteps*width*height;
        
        var dataSize = (nrOfElements*4)/(1024*1024);
        
        var html='<div><h1>Please check and confirm your processing settings</h1>'+
        '<div style="margin-left:30px;">'+
        '<hr/>You are about to start a job that has to process <b style="font-size:16px;color:red;">'+nrOfElements+'</b> elements.<br/><br/>'+
        'The outputted uncompressed data volume will be approximately <b>'+Math.round(dataSize*100)/100+'</b> MB .<hr/>'+
        '<p><You will start a process with the following properties:<br/><table>'+
        '<tr><td>Selected grid width:</td><td><b>'+width+'</b></td></tr>'+    
        '<tr><td>Selected grid height:</td><td><b>'+height+'</b></td></tr>'+
        '<tr><td>Selected number of time steps:</td><td><b>'+numberOfSteps+'</b></td></tr>'+
        '</table>'+
        
        '</table></p><p>Generally:<br/>- a width of ~1000, a height of ~1000 and ~5000 timesteps is a large job.<br/>'+
        '- a width of ~100, a height of ~100 and ~50 timesteps is a small job.<br/>'+
        '<br/>'+
        'Execution time depends on data access speed and the amount of selected elements.<br/>This can vary between a few minutes and a couple of days.'+
        '</p></div>'+
        '</div>'
        
        if(numberOfSteps>10||width>1000||height>1000){
          $('<div></div>').appendTo('body')
          .html(html)
          .dialog({
              modal: true,
              width:600,
              height:450,
              title: 'Confirm processing settings...',
              zIndex: 10000,
              autoOpen: true,
              resizable: false,
              buttons: [{
                    text:'Cancel',
                    click:function () {
                      $(this).dialog("close");
                    }
                  },{
                    text:'Start',
                    icons:{primary:'codejobsicon'},
                    click:function () {
                      if(nrOfElements>10000*10000*10000){
                        alert("Too much elements selected, please reduce.");
                        return;
                      }
                      startCalculation();
                      $(this).dialog("close");
                    }
                  }],
              close: function (event, ui) {
                  $(this).remove();
              }
          });
        }else{
          startCalculation();
        }
        
      });
      
      var startCalculation = function(){
        //showInfo("bla");
        //return;
        //mainWebmapJS.positionMapPinByLatLon({x:$("#calcinx").val(),y:$("#calciny").val()});
        mainWebmapJS.setMapPin(mainWebmapJS.getPixelCoordFromLatLong({x:$("#calcinx").val(),y:$("#calciny").val()}));
        mainWebmapJS.showMapPin();
       
        
      
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
                    $("#"+rootElementId).find(".stopdate").first().val();
        
        var service = WMJSgetServiceFromStore('/impactportal/ImpactService?source='+resource+'&');
        
        var layer = mainWebmapJS.getLayerByServiceAndName(service.service,activeLayer);
        
        var numberOfSteps = calculateAndCheckNumberOfStepsToProcess(layer);

        $("#startcalculation").hide();
        
        var bbox = new WMJSBBOX(parseFloat($("#"+rootElementId).find(".bboxwest").val()),
            parseFloat($("#"+rootElementId).find(".bboxsouth").val()),
            parseFloat($("#"+rootElementId).find(".bboxeast").val()),
            parseFloat($("#"+rootElementId).find(".bboxnorth").val()));
        
        wps.execute(wpsProcessSubsettingName,
          {'dates':dates,
          'resx':$("#"+rootElementId).find(".resolutionx").val(),
          'resy':$("#"+rootElementId).find(".resolutiony").val(),
          'bbox':[bbox.left,bbox.top,bbox.right,bbox.bottom],
          'resource':resource,
          'outputFormat':$("#"+rootElementId).find(".outputFormat").val(),
          'outputFileName':$("#"+rootElementId).find(".outputFileName").val(),
          'coverage':activeLayer,
          'crs':currentProjection
        
        });
      };
      
      
      

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
        	//console.log("Layer ready");
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
                html+="<option>"+decodeURIComponent(currentCoverage.supportedProjections[j].srs)+"</option>";
              }
            }
            html+="</select>";

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
            
            $("#c4i_wizard_convert_fileinfodialog").find(".startdateinfo").html(startdate);
            $("#c4i_wizard_convert_fileinfodialog").find(".stopdateinfo").html(stopdate);
            
            
            var updateNumTimeSteps = function(){
              var numberOfSteps = calculateAndCheckNumberOfStepsToProcess(layer);
              $("#"+rootElementId).find(".numberofdatestoprocess").html(numberOfSteps);
              
              $("#startcalculation span").text('Start processing '+numberOfSteps+' timestep(s)');
              
              
            }
            
            updateNumTimeSteps();
            $("#"+rootElementId).find(".startdate").change(updateNumTimeSteps);
            $("#"+rootElementId).find(".stopdate").change(updateNumTimeSteps);
            
 
            
            
            try{
              if( layer.getDimension("time").values.split("/").length==3){
                timeRes = layer.getDimension("time").values.split("/")[2];
              }
            }catch(e){}
            $("#"+rootElementId).find(".timeresolution").first().val(timeRes);
            
            $("#"+rootElementId).find(".projectionselector").first().html(html);
            $("#"+rootElementId).find(".projectionselector").find(".projcombo").first().on('change',function(){setProjection(layer,this.value);} );
            
            //console.log(currentCoverage);
            $("#c4i_wizard_convert_fileinfodialog").find(".c4i_wizard_convert_projectioninfo").html(decodeURIComponent(currentCoverage.nativeCRS));
            
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionxinfo").html(currentCoverage.cellsizeX);
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionyinfo").html(currentCoverage.cellsizeY);
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionwidthinfo").html(currentCoverage.width);
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionheightinfo").html(currentCoverage.height);

            
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionbbox_left").html(parseFloat(currentCoverage.originX));
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionbbox_bottom").html(parseFloat(currentCoverage.originY));

            var right = parseFloat(currentCoverage.originX)+parseFloat(currentCoverage.width*currentCoverage.cellsizeX);
            var top   = parseFloat(currentCoverage.originY)+parseFloat(currentCoverage.height*currentCoverage.cellsizeY)
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionbbox_right").html(right);
            $("#c4i_wizard_convert_fileinfodialog").find(".nativeresolutionbbox_top").html(top);
            var wcsdescribecoverageURL=layer.service+"service=WCS&request=DescribeCoverage&layer="+layer.name;
            $("#c4i_wizard_convert_fileinfodialog").find(".wcsdescribecoverageURL").html("<a target=\"_blank\" href=\""+wcsdescribecoverageURL+"\">"+wcsdescribecoverageURL+"</a>");
            
            
            mainWebmapJS.draw();
            $("#startcalculation").show();
          },failed);
          
        };
//        console.log("Adding layer"+layer.name)
        mainWebmapJS.addLayer(layer);
      };
      
      
      var setProjection = function (layer,srsname) {
        mainWebmapJS.hideBoundingBox();
        var srs = layer.getProjection(srsname);
        if(!srs){
          alert("Unknown projection: ["+srsname+"]<br/><br/><b>You have to fill out the correct BBOX yourself.</b>");
          srs = [];
          srs.srs=srsname;
          srs.bbox=new WMJSBBOX();
          
        }
        currentProjection = srsname;
        setProjectionDirectly(srs);
      }
      var setProjectionDirectly = function(srs){
        mainWebmapJS.setProjection(srs);
        mainWebmapJS.zoomOut();
      
        
        function d(){
       
          mainWebmapJS.showBoundingBox(srs.bbox);
        };
        mainWebmapJS.addListener('onmapready',d,false);
        
        mainWebmapJS.draw();
        
        boundingBoxBBOX = srs.bbox.clone();

        $("#"+rootElementId).find(".bboxwest").val(boundingBoxBBOX.left);
        $("#"+rootElementId).find(".bboxnorth").val(boundingBoxBBOX.top);
        $("#"+rootElementId).find(".bboxeast").val(boundingBoxBBOX.right);
        $("#"+rootElementId).find(".bboxsouth").val(boundingBoxBBOX.bottom);
        
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
        if(activeLayer){
          if(activeLayer!=""){
            selectedLayer = activeLayer;
            console.log("selectedLayer ="+selectedLayer);
          }
        }
        for(var j=0;j<l.length;j++){
          var n = l[j];
          if(n!='baselayer'&&n!='overlay'&&n!='grid10'){
            if(selectedLayer == undefined){
              selectedLayer = n;
            }
            if(selectedLayer == n){
              html+="<option selected>"+n+"</option>";
             
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
        try{
          var value = $(this).val();
          var className = $(this).attr('class');
          if(className.indexOf("west")>0)boundingBoxBBOX.left = parseFloat(value);
          if(className.indexOf("north")>0)boundingBoxBBOX.top= parseFloat(value);
          if(className.indexOf("east")>0)boundingBoxBBOX.right = parseFloat(value);
          if(className.indexOf("south")>0)boundingBoxBBOX.bottom = parseFloat(value);
        }catch(e){}
        console.log(boundingBoxBBOX);
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
          }
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
      
      $( "#c4i_wizard_convert_fileinfodialog" ).dialog({
        autoOpen: false,
        width:900,
        height:500,
        show: {
          effect: "fade",
          duration: 300
        },
        hide: {
          effect: "fade",
          duration: 300
        }
      });
      
	      showFileInfo = function(){
	        $( "#c4i_wizard_convert_fileinfodialog" ).dialog( "open" );
	      };
	      
	      $(".c4i_wizard_convert_fitboundingboxtowindow").button({
          icons: {
            primary: "ui-icon-arrow-4-diag"
          }
        }).click(function(){
          var srsNew = mainWebmapJS.getProjection();
          var bbox = srsNew.bbox.clone();
          var w=(bbox.right-bbox.left)*0.02;
          var h=(bbox.top-bbox.bottom)*0.02;
          bbox.left+=w;
          bbox.right-=w;
          bbox.top-=h;
          bbox.bottom+=h;
          bboxChangedByEvent(bbox);

        });
	      
	       $(".c4i_wizard_convert_fitboundingboxtolayer").button({
	          icons: {
	            primary: "ui-icon-arrow-4-diag"
	          }
	        }).click(function(){
	          var layer = mainWebmapJS.getLayers()[0];
	          var srsNew = layer.getProjection(currentProjection);
	          var bbox = srsNew.bbox.clone();
	          bboxChangedByEvent(bbox);
	        });
	       
	       $(".c4i_wizard_convert_zoomtobbox").button({
           icons: {
             primary: "ui-icon-arrow-4-diag"
           }
         }).click(function(){
           var layer = mainWebmapJS.getLayers()[0];
           var srsNew = layer.getProjection(currentProjection);
           var bbox = new WMJSBBOX(parseFloat($("#"+rootElementId).find(".bboxwest").val()),
               parseFloat($("#"+rootElementId).find(".bboxsouth").val()),
               parseFloat($("#"+rootElementId).find(".bboxeast").val()),
               parseFloat($("#"+rootElementId).find(".bboxnorth").val()));
           mainWebmapJS.zoomTo(bbox);
           mainWebmapJS.draw();
         });
        
	       
	       
	       var calculateAndCheckNumberOfStepsToProcess = function(layer){
	         try{
	           var dates = $("#"+rootElementId).find(".startdate").first().val()+"/"+
             $("#"+rootElementId).find(".stopdate").first().val();
//	           console.log("dates[0]:"+dates.split("/")[0]);
//	           console.log("dates[1]:"+dates.split("/")[1]);
	           var startIndex =  layer.getDimension("time").getIndexForValue(dates.split("/")[0]);
	           var stopIndex  =  layer.getDimension("time").getIndexForValue(dates.split("/")[1]);
	           if(startIndex == -1){
	             startIndex = 0;//The start date is before the time coverage of the data, OK
	           }
	           if(startIndex == -2){
	             alert("Start date is after time coverage span of the data");
	             return -1;
	           }
	           if(stopIndex == -1){
	             stopIndex = 0;
	           }
	           if(stopIndex == -2){
	             stopIndex = layer.getDimension("time").size()-1;
	           }
	           if(startIndex>stopIndex){
	             alert("End date is before start date");
	             return -1;
	           }
	           
//	           console.log("startIndex:"+startIndex);
//	           console.log("stopIndex:"+stopIndex);
//	           console.log("Number of dates to process:"+((stopIndex-startIndex)+1));
	           return ((stopIndex-startIndex)+1);
	         }catch(e){
	           console.log(e);
	         }
	       };
	    

      /* Debug for WMS */
//       wpsComplete("http://birdexp02.knmi.nl/cgi-bin/adaguc/dragonsdenwms.cgi?DATASET=WPS_raypath_1400073951_wmsconfig_201405140000&");
//       wpsComplete("http://birdexp02.knmi.nl/cgi-bin/adaguc/dragonsdenwms.cgi?DATASET=WPS_raypath_1400073951_wmsconfig_201405140000&");
    });
    
    var showBasketWidget= function(){
	  //console.log("showBasketWidget");
      basketWidget.show(function(selectedNodes) {
        //console.log(selectedNodes);
	    for ( var j = 0; j < selectedNodes.length && j<1; j++) {
          setNewResource(selectedNodes[j].dapurl);
	    }
	    return true;
	  });
    };
    
 