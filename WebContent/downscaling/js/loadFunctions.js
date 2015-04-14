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
  $('#dialog').dialog({
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
  var URL = '../DownscalingService/users/'+loggedInUser+'/predictands';
  var variableName = getValueFromHash("variableName");
  var defaultPredictandName = getValueFromHash("predictandName");
  $("#predictands").html('');
  if(variableName != null){
    URL += '?variableName=' + variableName;
    $.get( URL, function( data ) {
      $.each(data.values, function(index, value){
          $('#predictands').append("<td><input class='input-predictand' data-predictand='"+value.predictandName+"' data-id-zone='"+value.idZone+"' data-predictor='"+ value.predictorName+"' type='radio' name='predictand' value='"+value.name+"'/><abbr title='Click to see more info'><span class='link'>"+value.predictandName+"</span></abbr></td>");
          if(defaultPredictandName != null && defaultPredictandName == value.predictandName)
            $('#predictands').find("[data-predictand='" + value.predictandName + "']").prop('checked',true);
      });
    });
    $('#predictand-type-header').collapsible('open');
    }else{
      $('#predictand-type-header').collapsible('close'); 
    }
  
}

function loadPredictandDetails(zone,predictor,predictand){
  $.get( "../DownscalingService/users/"+loggedInUser+"/zones/"+zone+"/predictors/"+predictor+"/predictands/" + predictand, function( data ) {
        $('#predictand-info').html('Name: ' + data.value.predictandName +'</br> Variable: ' + data.value.variable + ' </br> Variable type: ' + data.value.variableType + '</br> Dataset: ' + data.value.dataset);
        loadMap(data.value.startLat, data.value.startLon, data.value.endLat, data.value.endLon,zone,predictand);
      });
}

function loadDownscalingMethods(){
  var zone = getValueFromHash("zone");
  var predictandName = getValueFromHash("predictandName");
  var defaultDownscalingMethod = getValueFromHash("dMethodName");
  var dMethodType = getValueFromHash("dMethodType");
  var parameters = "";
  $('#downscaling-methods').html('');
  if(dMethodType == null){
    dMethodType = "ALL";
    insertHashProperty("dMethodType",dMethodType, sortedKeys);
  }
  $("input:radio[name='downscaling-method-type'][value='"+dMethodType+"']").prop('checked', true);
  if(dMethodType != "ALL") 
    parameters = "?dMethodType=" + dMethodType;
  if(zone != null && predictandName != null){
    $.get( "../DownscalingService/users/"+loggedInUser+"/zones/"+zone+"/predictands/" + predictandName + "/downscalingMethods" + parameters, function( data ) {
        $.each(data.values, function(index, value){
          $('#downscaling-methods').append("<td><input class='input-downscaling-method' data-zone='"+zone+"' data-predictand='"+ predictandName+"' data-downscaling-method='"+value.name+"' type='radio' name='downscalingMethod' value='"+value.name+"'/>"+value.name+"</td>");
          if(defaultDownscalingMethod != null && defaultDownscalingMethod == value.name){
              $("input[name='downscalingMethod'][value='"+value.name+"']").prop('checked',true);
              loadValidationReport();
          }
        });
        $('#downscalingmethod-header').collapsible('open');
    });
  }else{
    $('#downscalingmethod-header').collapsible('close');
  }
}

function loadValidationReport(){
  var zone = getValueFromHash("zone");
  var predictandName = getValueFromHash("predictandName");
  var dMethodName = getValueFromHash("dMethodName");
  if(zone != null && predictandName != null && dMethodName != null){
    $('#validation').html("<a href='../DownscalingService/validation?idZone="+zone+"&predictandName="+predictandName+"&dMethodName="+dMethodName+"' download='report'>Download validation report</a>");
  }
}

function loadDatasets(){
    var zone = getValueFromHash("zone");
    var datasetType = getValueFromHash("datasetType");
    var defaultDatasetValue = getValueFromHash("datasetName");
    $('#datasets').html('');
    if(datasetType == null){
      datasetType = "CLIMATE";
      insertHashProperty("datasetType",datasetType, sortedKeys);
    }
    $("input:radio[name=dataset-type][value="+datasetType+"]").prop('checked', true);
    if(zone != null && datasetType != null && zone != '' && datasetType != ''){
    $.get( "../DownscalingService"+"/datasets?username=" + loggedInUser + "&zone=" + zone, function( data ) {
        $.each(data.values, function(index, value){
          $('#datasets').append("<td><input class='input-dataset' data-name='"+value.name+"' type='radio' name='dataset' value='"+value.name+"'/><abbr><span>"+value.name+"</span></abbr></td>");
          if(defaultDatasetValue == value.name)
            $("input[name=dataset][value='"+value.name+"']").prop('checked',true);
        });
    });
    $('#dataset-header').collapsible('open');
    }
}

function loadScenarios(){
    var zone = getValueFromHash("zone");
    var datasetName = getValueFromHash("datasetName");
    var defaultScenarioValue = getValueFromHash("scenarioName");
    var sYear = getValueFromHash("sYear");
    var eYear = getValueFromHash("eYear");
    if(sYear != null){
      $('#date-range-start').val(sYear);
      $('#date-range-start').change();
    }
    if(eYear != null){
      $('#date-range-end').val(eYear);
      $('#date-range-end').change();
    }
    $('#scenarios').html('');
    if(zone != null && datasetName != null){
    $.get( "../DownscalingService"+"/datasets/"+datasetName+"/scenarios?zone=" + zone +"&sYear=" + $('#date-range-start').val() + "&eYear=" + $('#date-range-end').val(), function( data ) {
        $.each(data.values, function(index, value){
          $('#scenarios').append("<td><input class='input-scenario' data-name='"+value.name+"' type='radio' name='scenario' value='"+value.name+"'/><abbr title='"+'From ' + value.metadata.split(';')[1].split('=')[1]+' to '+value.metadata.split(';')[2].split('=')[1]+"'><span >"+ value.name +"</span></abbr></td>");
          if(defaultScenarioValue == value.name)
            $("input[name=scenario][value='"+value.name+"']").prop('checked',true);
        });
    });
    $('#scenario-header').collapsible('open');
    }
    $('#scenarios').html('').triggerHandler('contentChanged');
}

function loadContent(){
  loadVariableTypes();
  loadVariables();loadPredictands();loadDownscalingMethods();loadDatasets();loadScenarios();
}