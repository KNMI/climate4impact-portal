/*
 * Event handlers
 */

$(document).on('click', '.link', function(event, ui){
  
  var firedInput = $(event.target).parent().prev('input');
  var inputName = $(firedInput).attr('name');
  if(inputName == 'predictand'){
    showMapDialog('Predictand');
    $('#predictand-details').appendTo('#dialog-content');
    var firedInput = $(event.target).parent().prev('input');
    var zone = $(firedInput).attr('data-zone');
    var predictor = $(firedInput).attr('data-predictor');
    var predictand = $(firedInput).attr('data-predictand');
    loadPredictandDetails(zone,predictor,predictand);
  }
  if(inputName == 'domain'){
    showMapDialog('Domain');
    $('#domain-details').appendTo('#dialog-content');
    var firedInput = $(event.target).parent().prev('input');
    var domain = $(firedInput).attr('data-domain');
    loadDomainDetails(domain);
  }
  if(inputName == 'model'){
    showMapDialog('Model');
    $('#domain-details').appendTo('#dialog-content');
    var firedInput = $(event.target).parent().prev('input');
    var model = $(firedInput).attr('data-model');
    loadModelDetails(model)
  }
});

$(document).ready(function() {
  $("#date-range-start, #date-range-end").keyup(function (e) {
    if (e.keyCode == 13) {
      updateSlider();
    }
  });
});

$(document).on('change','#date-range-start, #date-range-end', function(event, ui){
  reloadSlider();
});


function updateSlider(sYear, eYear){
  $("#date-range-start").val(getValueFromHash("sYear"));
  $("#date-range-end").val(getValueFromHash("eYear"));
  reloadSlider();
}

function reloadSlider(){
  var textFieldStart = $("#date-range-start").val();
  var textFieldEnd = $("#date-range-end").val();
  insertHashProperty("sYear", textFieldStart, sortedKeys);
  insertHashProperty("eYear", textFieldEnd, sortedKeys);
  if( textFieldStart => $("#slider-range").slider("option", "min") && textFieldStart <= $("#slider-range").slider("option", "max")){
    $("#slider-range").slider("values",[textFieldStart,textFieldEnd]);
  }else{
    alert("Out of bounds value");
  }
}


$(document).on('click', 'button', function(event, ui){
  var id = $(this).attr('id');
  if(id == "button-load-experiments"){
    insertHashProperty("sYear", $('#date-range-start').val(), sortedKeys);
    insertHashProperty("eYear", $('#date-range-end').val(), sortedKeys);
    loadExperiments();
  }else if(id=="button-downscale"){
    if(getValueFromHash("experiment") != null)
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
      loadVariables(false);
    }
  }else if($(this).attr('name') == 'variable'){
    var variable = encodeURIComponent($(this).attr('data-variable'));
    if($(this).is(':checked')){
      insertHashProperty('variable', variable, sortedKeys);
      removeHashProperty("zone");
      removeHashProperty("predictand");
      removeHashProperty("downscalingMethod");
      loadDomains(false);
    }
  }else if($(this).attr('name') == 'domain'){
    var domain = encodeURIComponent($(this).attr('data-domain'));
    if($(this).is(':checked')){
      insertHashProperty('domain', domain, sortedKeys);
      removeHashProperty("zone");
      removeHashProperty("predictand");
      removeHashProperty("downscalingMethod");
      loadDatasets(false);
    }
  }else if($(this).attr('name') == 'dataset'){
    var domain = encodeURIComponent($(this).attr('data-dataset'));
    if($(this).is(':checked')){
      insertHashProperty('dataset', domain, sortedKeys);
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
    }
  }else if($(this).attr('name') == 'modelProject'){
    var modelProject = encodeURIComponent($(this).attr('data-model-project'));
    if($(this).is(':checked')){
      insertHashProperty('modelProject', modelProject, sortedKeys);
      loadModels();
    }
  }else if($(this).attr('name') == 'model'){
    var model = encodeURIComponent($(this).attr('data-model'));
    if($(this).is(':checked')){
      insertHashProperty('model', model, sortedKeys);
      loadExperiments();
    }
  }else if($(this).attr('name') == 'experiment'){
    var experiment = encodeURIComponent($(this).attr('data-name'));
    var sDate = encodeURIComponent($(this).attr('data-sDate'));
    var eDate = encodeURIComponent($(this).attr('data-eDate'));
    if($(this).is(':checked')){
      insertHashProperty('experiment', experiment, sortedKeys);
      insertHashProperty('sYear', sDate, sortedKeys);
      insertHashProperty('eYear', eDate, sortedKeys);
    }
    loadPeriod();
  }
});