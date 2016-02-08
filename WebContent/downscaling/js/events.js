/*
 * Event handlers
 */

$(document).on('click', '.link', function(event, ui){
  showDialog('Predictand');
  $('#predictand-details').appendTo('#dialog-content');
  var firedInput = $(event.target).parent().prev('input');
  var zone = $(firedInput).attr('data-zone');
  var predictor = $(firedInput).attr('data-predictor');
  var predictand = $(firedInput).attr('data-predictand');
  loadPredictandDetails(zone,predictor,predictand);
});

$(document).ready(function() {
  $("#date-range-start, #date-range-end").keyup(function (e) {
    if (e.keyCode == 13) {
      reloadSlider();
    }
  });
});

$(document).on('change','#date-range-start, #date-range-end', function(event, ui){
  reloadSlider();
});

function reloadSlider(){
  var textFieldStart = $("#date-range-start").val();
  var textFieldEnd = $("#date-range-end").val();
  if( textFieldStart => $("#slider-range").slider("option", "min") && textFieldStart <= $("#slider-range").slider("option", "max")){
    $("#slider-range").slider("values",[textFieldStart,textFieldEnd]);
  }else{
    alert("Out of bounds value");
  }
}


$(document).on('click', 'button', function(event, ui){
  var id = $(this).attr('id');
  if(id == "button-load-scenarios"){
    insertHashProperty("sYear", $('#date-range-start').val(), sortedKeys);
    insertHashProperty("eYear", $('#date-range-end').val(), sortedKeys);
    loadScenarios();
  }else if(id=="button-downscale"){
    if(getValueFromHash("scenario") != null)
      downscalingSubmit();
    else
      alert("You have to fill in the whole form to Downscale");
  }else if(id=="button-saveconfig"){
    showSaveConfigDialog("Do you want to save this configuration?");
  }
});

$(document).on('change', '#select-saved-downscalings', function() {
  var optionSelected = $("option:selected", this);
  location.hash = this.value;
  loadContent();
});

$("#select-saved-downscalings").on('change', function (e) {

});

$(window).on("hashchange", function(e){
    var oldHash = e.originalEvent.oldURL;
    var newHash = e.originalEvent.newURL;
});

$(document).on('change', 'input:radio', function(event, ui){
  if($(this).attr('name') === 'variableType'){
    //delete elements from here
    var variableType = $(this).attr('value');
    insertHashProperty('variableType', variableType, sortedKeys);
    if($(this).is(':checked')){
      removeHashProperty("variable");
      removeHashProperty("zone");
      removeHashProperty("predictand");
      removeHashProperty("downscalingMethod");
      loadVariables();
    }
  }else if($(this).attr('name') == 'variable'){
    var variable = encodeURIComponent($(this).attr('data-variable'));
    if($(this).is(':checked')){
      insertHashProperty('variable', variable, sortedKeys);
      removeHashProperty("zone");
      removeHashProperty("predictand");
      removeHashProperty("downscalingMethod");
      loadPredictands();
    }
  }else if($(this).attr('name') == 'predictand'){
    var zone = encodeURIComponent($(this).attr('data-zone'));
    var predictor = encodeURIComponent($(this).attr('data-predictor'));
    var predictand = encodeURIComponent($(this).attr('data-predictand'));
    if($(this).is(':checked')){
      insertHashProperty('zone', zone, sortedKeys);
      insertHashProperty('predictand', predictand, sortedKeys);
      removeHashesFrom("downscalingType", sortedKeys);
      loadDownscalingMethods();
    }
  }else if($(this).attr('name') == 'downscalingType'){
      var downscalingType = encodeURIComponent($(this).attr('data-downscaling-type'));
      insertHashProperty('downscalingType', downscalingType, sortedKeys);
      loadDownscalingMethods(); //send type
  }else if($(this).attr('name') == 'downscalingMethod'){
    var zone = encodeURIComponent($(this).attr('data-zone'));
    var predictand = encodeURIComponent($(this).attr('data-predictand'));
    var downscalingMethod = encodeURIComponent($(this).attr('data-downscaling-method'));
    if($(this).is(':checked')){
      insertHashProperty('downscalingMethod', downscalingMethod, sortedKeys);
      $('#validation').html("<a href='../DownscalingService/validation?zone="+zone+"&predictand="+predictand+"&downscalingMethod="+downscalingMethod+"' download='report'>Download validation report</a>");
      $('#downscalingmethod-header').collapsible('open');
      loadValidationReport();
      loadDatasets();
    }
  }else if($(this).attr('name') == 'datasetType'){
    var datasetType = encodeURIComponent($(this).attr('data-dataset-type'));
    if($(this).is(':checked')){
      insertHashProperty('datasetType', datasetType, sortedKeys);
      loadDatasets(); 
    }
  }else if($(this).attr('name') == 'dataset'){
    var dataset = encodeURIComponent($(this).attr('data-name'));
    if($(this).is(':checked')){
      insertHashProperty('dataset', dataset, sortedKeys);
      loadScenarios();
    }
  }else if($(this).attr('name') == 'scenario'){
    var scenario = encodeURIComponent($(this).attr('data-name'));
    if($(this).is(':checked')){
      insertHashProperty('scenario', scenario, sortedKeys);
    }
  }
});