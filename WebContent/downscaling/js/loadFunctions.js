function loadMap(startLat, startLon, endLat, endLon, zone, predictand){
        var a = [startLat, startLon];
        var b = [startLat, endLon];
        var c = [endLat, endLon];
        var d = [endLat, startLon];
        var center = [(b[0]+c[0])/2, (a[1]+b[1])/2]
        var map = L.map('map').setView(center, 5);
        L.tileLayer('https://{s}.tiles.mapbox.com/v3/mannuk.jj9612k9/{z}/{x}/{y}.png', {
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://meteo.unican.es">UC</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>'
    }).addTo(map);
    var polygon = L.polygon([a,b,c,d]).addTo(map);
    function onEachFeature(feature, layer) {
      // does this feature have a property named popupContent?
      if (feature.properties && feature.properties.popupContent) {
          layer.bindPopup(feature.properties.popupContent);
      }
    }
    $.get( "../DownscalingService/zones/"+zone+"/predictands/"+predictand+"/stations", function( data ) {
       L.geoJson(data, {onEachFeature: onEachFeature}).addTo(map);
  });

}

function showDialog(title){
  $("#dialog").dialog({
    dialogClass: 'custom-dialog', 
    height: 600,
    width: 850,
    modal: true,
    resizable: true,
    title: title,
    open: function(event, ui){
      $('#dialog-content').empty()
      $('#map').css('height','500px');
    },
    close: function( event, ui ) {
      $('#dialog').remove();
      $('body').append('<div id="dialog"><div id="dialog-content"></div></div>');
      $('body').append('<div id="predictand-details"><div id="predictand-info"></div><div id="map"></div></div>');
      $('#map').css('height','');
    }
  });
 }

function showSaveConfigDialog(message){
  showMessageDialog(message);
  $('#dialog-content').append("<p><label for='config-name'>Config name</label><input type='text' id='input-configname'name='config-name'><\p>");
  $("#dialog").dialog({
      buttons: { 
      Save: function() {
        saveConfig($('#input-configname').val());
        $( this ).dialog( "close" );
      },
      Cancel: function() {
        $( this ).dialog( "close" );
      }
    }
    });
}

function showOKDialog(message, url){
  showMessageDialog(message);
  $("#dialog").dialog({
      buttons: { 
      Launch: function() {
        postData(url);
        $( this ).dialog( "close" );
      },
      Cancel: function() {
        $( this ).dialog( "close" );
      }
    }
    });
}

function showMessageDialog(message){
  $('#dialog-content').empty().append(message);
  $("#dialog").dialog({
    open: function(event, ui){
      $('#dialog-content').empty().append(message);
    },
    close: function(event, ui){
      $( this ).dialog( "close" );
      $('#dialog').remove();
      $('body').append('<div id="dialog"><div id="dialog-content"></div></div>');
    },
    resizable: false,
    height: 350,
      width: 500,
      modal: true,
      title: 'System message',  
    });
}
function loadDatasets(){
  var domainType = getValueFromHash("datasetType");
  $("#datasets").html('');
  //if(domainType != null){
    $.get( "../DownscalingService/datasets", function( data ) {
      $.each(data.values, function(index, value){
        $('#datasets').append("<td><input class='input-domain' + data-dataset="+value.code+" type='radio' name='dataset' value='"+value.code+"'/><abbr title='"+value.description+"'><span >"+value.alias+"</span></abbr></td>");
          var defaultDatasetName = getValueFromHash("datasetName");
          if(defaultDatasetName != null && defaultDatasetName == value.code){
              $('#datasets').find("[data-dataset='" + value.code + "']").prop('checked',true);
          }
      });
    });
    $('#dataset-type-header').collapsible('open');
  //}else{
 //   $('#domain-type-header').collapsible('close');
 // }

}

function loadDomains(){
  var domainType = getValueFromHash("domainType");
  $("#domains").html('');
  //if(domainType != null){
    $.get( "../DownscalingService/domains", function( data ) {
      $.each(data.values, function(index, value){
        $('#domains').append("<td><input class='input-domain' + data-domain="+value.code+" type='radio' name='domain' value='"+value.code+"'/><abbr title='"+value.description+"'><span >"+value.alias+"</span></abbr></td>");
          var defaultDomainName = getValueFromHash("domainName");
          if(defaultDomainName != null && defaultDomainName == value.code){
              $('#domains').find("[data-domain='" + value.code + "']").prop('checked',true);
          }
      });
    });
    $('#domain-type-header').collapsible('open');
  //}else{
 //   $('#domain-type-header').collapsible('close');
 // }
}
function loadVariableTypes(){
  var defaultVariableType = getValueFromHash("variableType");
  if(defaultVariableType != null)
    $(".input-variable-type[data-variable-type='"+defaultVariableType+"']").attr('checked',true);
  
}

function loadVariables(){
  var variableType = getValueFromHash("variableType");
  $("#variables").html('');
  if(variableType != null){
    $.get( "../DownscalingService/variables?variableType="+variableType, function( data ) {
      $.each(data.values, function(index, value){
        $('#variables').append("<td><input class='input-variable' + data-variable="+value.code+" type='radio' name='variable' value='"+value.code+"'/><abbr title='"+value.description+"\nUnits: "+value.units+"'><span >"+value.code+"</span></abbr></td>");
          var defaultVariableName = getValueFromHash("variableName");
          if(defaultVariableName != null && defaultVariableName == value.code){
              $('#variables').find("[data-variable='" + value.code + "']").prop('checked',true);
          }
      });
    });
    $('#variable-type-header').collapsible('open');
  }else{
    $('#variable-type-header').collapsible('close');
  }

}

  
function loadPredictands(){
  var URL = '../DownscalingService/predictands?username=' + loggedInUser;
  var variableName = getValueFromHash("variable");
  if(variableName === "Tmax")
    variableName = "TX";
  if(variableName === "Tmin")
    variableName === "TN";
  if(variableName === "Precip")
    variableName = "RR";
  var defaultpredictand = getValueFromHash("predictand");
  $("#predictands").html('');
  if(variableName != null){
    URL += '&variable=' + variableName;
    $.get( URL, function( data ) {
      $.each(data.values, function(index, value){
          $('#predictands').append("<td><div class='predictand'><input class='input-predictand' data-predictand='"+value.predictand+"' data-zone='"+value.zone+"' data-predictor='"+ value.predictor+"' type='radio' name='predictand' value='"+value.name+"'/><abbr title='Click to see more info'><span class='link'>"+value.predictand+"</span></abbr></div></td>");
          if(defaultpredictand != null && defaultpredictand == value.predictand)
            $('#predictands').find("[data-predictand='" + value.predictand + "']").prop('checked',true);
      });
    });
    $('#predictand-type-header').collapsible('open');
    }else{
      $('#predictand-type-header').collapsible('close'); 
    }
  
}

function loadPredictandDetails(zone,predictor,predictand){
  $.get( "../DownscalingService/zones/"+zone+"/predictands/"+predictand+"?username=" + loggedInUser, function( data ) {
        $('#predictand-info').html('Name: ' + data.value.predictand +'</br> Variable: ' + data.value.variable + ' </br> Variable type: ' + data.value.variableType + '</br> Model: ' + data.value.model);
        loadMap(data.value.startLat, data.value.startLon, data.value.endLat, data.value.endLon,zone,predictand);
      });
}

function loadDownscalingMethods(){
  var zone = getValueFromHash("zone");
  var predictand = getValueFromHash("predictand");
  var defaultDownscalingMethod = getValueFromHash("downscalingMethod");
  var downscalingType = getValueFromHash("downscalingType");
  var parameters = "";
  $('#downscaling-methods').html('');
  if(downscalingType == null){
    downscalingType = "ALL";
    insertHashProperty("downscalingType",downscalingType, sortedKeys);
  }
  $("input:radio[name='downscalingType'][value='"+downscalingType+"']").prop('checked', true);
  if(downscalingType != "ALL") 
    parameters = "?username="+loggedInUser+"&downscalingType=" + downscalingType;
  if(zone != null && predictand != null){
    $.get( "../DownscalingService/zones/"+zone+"/predictands/" + predictand + "/downscalingMethods" + parameters, function( data ) {
        $.each(data.values, function(index, value){
          $('#downscaling-methods').append("<td><input class='input-downscaling-method' data-zone='"+zone+"' data-predictand='"+ predictand+"' data-downscaling-method='"+value.name+"' type='radio' name='downscalingMethod' value='"+value.name+"'/>" + "<abbr title='"+ " Name: " + value.name + "&#10; Description: " +value.description + "&#10; Metadata: " + value.metadata + "'><span>"+value.name+"</span></abbr></td>");
          if(defaultDownscalingMethod != null && defaultDownscalingMethod == value.name){
              $("input[name='downscalingMethod'][value='"+value.name+"']").prop('checked',true);
              loadValidationReport();
          }
        });
        $('#downscaling-method-header').collapsible('open');
    });
  }else{
    $('#downscaling-method-header').collapsible('close');
  }
}

function loadValidationReport(){
  var zone = getValueFromHash("zone");
  var predictand = getValueFromHash("predictand");
  var downscalingMethod = getValueFromHash("downscalingMethod");
  if(zone != null && predictand != null && downscalingMethod != null){
    $('#validation').html("<a href='../DownscalingService/validation?zone="+zone+"&predictand="+predictand+"&downscalingMethod="+downscalingMethod+"' download='report'>Download validation report</a>");
    $('#validation a').button({icons: {primary: 'ui-icon-document'}});
  }
}

function loadModels(){
    var zone = getValueFromHash("zone");
    var defaultModelValue = getValueFromHash("model");
    $('#models').html('');
    if(zone != null){
    $.get( "../DownscalingService"+"/models?username=" + loggedInUser + "&zone=" + zone, function( data ) {
        $.each(data.values, function(index, value){
          $('#models').append("<td><input class='input-model' data-name='"+value.name+"' type='radio' name='model' value='"+value.name+"'/><abbr title='"+ "Name: " + value.name + "&#10;Description: " +value.description + "&#10;Metadata: " + value.metadata + "'><span>"+value.name+"</span></abbr></td>");
          if(defaultModelValue == value.name)
            $("input[name=model][value='"+value.name+"']").prop('checked',true);
        });
    });
    $('#model-header').collapsible('open');
    }
}

function loadExperiments(){
    var run = getValueFromHash("run");
    var model = getValueFromHash("model");
    var defaultExperimentValue = getValueFromHash("experiment");
    var sYear = getValueFromHash("sYear");
    var eYear = getValueFromHash("eYear");
    if(run==null)
      run=1;
    if(sYear != null){
      $('#date-range-start').val(sYear);
      $('#date-range-start').change();
    }
    if(eYear != null){
      $('#date-range-end').val(eYear);
      $('#date-range-end').change();
    }
    $("input:radio[name='experimentRun'][value='Run 1']").prop('checked', true);
    $('#experiments').html('');
    if(run != null && model != null){
    $.get( "../DownscalingService"+"/models/"+model+"/experiments", function( data ) {
        $.each(data.values, function(index, value){
          var sDate = value.metadata.split(';')[1].split('=')[1];
          var eDate = value.metadata.split(';')[2].split('=')[1]; 
          $('#experiments').append("<td><input class='input-experiment' data-name='"+value.name+"' data-sDate='"+ sDate + "' data-eDate='"+eDate+"' type='radio' name='experiment' value='"+value.name+"'/><abbr title='"+ "Name: " + value.name + "&#10;Description: " + value.description +"&#10;Period: " + sDate+" - "+ eDate +"'><span >"+ value.name +"</span></abbr></td>");
          if(defaultExperimentValue == value.name)
            $("input[name=experiment][value='"+value.name+"']").prop('checked',true);
        });
    });
    $('#experiment-header').collapsible('open');
    }
    $('#experiments').html('').triggerHandler('contentChanged');
}

function loadPeriod(){
  $("#period-selection").append('<div><label for="date-range-start">Start year</label><input type="text" id="date-range-start" class="input-year"/></div><div id="slider-range"></div><div><label>End year</label><input type="text" id="date-range-end" class="input-year"></input></div>');
  var sYear = parseInt(getValueFromHash("sYear"))
  var eYear = parseInt(getValueFromHash("eYear"))
  $(function() { 
    $("#slider-range").slider({
      range : true,
      min : sYear,
      max: eYear,
      step: 1,
      values: [sYear, eYear],
      slide: function( event, ui ) {
          $("#date-range-start" ).val(ui.values[0]);
          $("#date-range-end").val(ui.values[1]);
          insertHashProperty("sYear", ui.values[0], sortedKeys);
          insertHashProperty("eYear", ui.values[1], sortedKeys);
      }
    });
    $("#date-range-start" ).val($( "#slider-range" ).slider( "values", 0));
    $("#date-range-end" ).val($( "#slider-range" ).slider( "values", 1));    
  });
  
  updateSlider();
  $('#downscaling-period-header').collapsible('open');
}

function loadContent(){
  loadVariableTypes();
  loadVariables();loadPredictands();loadDownscalingMethods();loadModels();loadExperiments();
}