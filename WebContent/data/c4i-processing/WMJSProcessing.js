var WMJSProcessing = function(options){
  var WPSURL = options.url;
  var _this = this;
  var running = false;
  
  var c4iProcessingGetKeys = function(obj){
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
    
  var _stripNS = function(newObj,obj){
    
    var keys = c4iProcessingGetKeys(obj);
    
    for(var j=0;j<keys.length;j++){
      var key = keys[j];
      var i = key.indexOf(":");
      var newkey=key.substring(i+1);
      var value = obj[key];
      if(typeof value === 'object'){
        newObj[newkey] = {};
        _stripNS(newObj[newkey],value);
      }else{
        newObj[newkey]=value;
      }
    }
  };
  
  var stripNS = function(currentObj){
    var newObj = {};
    _stripNS(newObj,currentObj);
    return newObj;
  };
  
  if(!options.failure){
    options.failure = function(message){
      alert(dump(message));
    }
  }
  
      
  /* Called on successfull statuslocation polling */
  var WPSMonitorSuccess = function(_data,statusLocation){
    console.log("WPSMonitorSuccess");
    var data = stripNS(_data);
    //console.log(data);
    if(running == false)return;
    var processCompleted = undefined;
    var percentCompleted = undefined;
    var statusMessage = undefined;
    var processFailed = undefined;
    var exceptionMessage = undefined;
  
    if(data.error){
        processFailed = true;
        exceptionMessage = data.error;
    }
    
    try{
      processFailed = true;
      exceptionMessage = dump(data['ExceptionReport']['Exception']);
    }catch(e){
    }
    
    try{
    
      processFailed = data['ExecuteResponse']['Status']['ProcessFailed'];
      exceptionMessage = "Process failed";
      if(isDefined(data['ExecuteResponse']['Status']['ProcessFailed']['ExceptionReport']['Exception']['ExceptionText'])){
        exceptionMessage = data['ExecuteResponse']['Status']['ProcessFailed']['ExceptionReport']['Exception']['ExceptionText'].value;
      }else{
        exceptionMessage = data['ExecuteResponse']['Status']['ProcessFailed']['ExceptionReport']['Exception'].attr.exceptionCode;
        exceptionMessage += ": "+data['ExecuteResponse']['Status']['ProcessFailed']['ExceptionReport']['Exception'].attr.locator;
        
      }
    }catch(e){
    }

    if(isDefined(processFailed)){
      running = false;
      options.failure({'message':exceptionMessage},data,statusLocation);
      return;
    }
    
    try{
      processCompleted = data['ExecuteResponse']['Status']['ProcessSucceeded'].value;
    }catch(e){
    }
    
    try{
      percentCompleted = data['ExecuteResponse']['Status']['ProcessStarted'].attr.percentCompleted;
      statusMessage = data['ExecuteResponse']['Status']['ProcessStarted'].value;
      
      if(statusMessage.indexOf("processstarted")==0){
        statusMessage = statusMessage.substr("processstarted".length);
      }
    }catch(e){
    }
    
    if(!isDefined(processCompleted)){
      if(isDefined(statusMessage) && parseFloat(percentCompleted) != NaN){
        options.progress(parseFloat(percentCompleted),statusMessage);
      }
      
    }else{
      running = false;
      //Process is complete!
//        console.log("Complete!");
//        console.log(data);
      var processOutput = data['ExecuteResponse']['ProcessOutputs']['Output'];
      
      options.progress(100,"completed");
      options.success(processOutput,data);
      
    }
  };
  
  /* WPS Success callback */
  var executeSuccess = function(_data){
    var data = stripNS(_data);

    var processAccepted = undefined;
    try{
      processAccepted = data['ExecuteResponse']['Status']['ProcessAccepted'].value;
    }catch(e){
    }
    
    if(!isDefined(processAccepted)){
      var reason = "";
      try{
        reason = dump(data['ExceptionReport']['Exception'].attr);
        options.failure({'message':'Process was not accepted, ExceptionReport:\n'+reason});
      }catch(e){
        if(data.error){
          if(!data.message)data.message=data.error;
          options.failure(data);
          return;
        }
        reason = dump(data);
        options.failure({'message':'Process was not accepted (dump):\n'+reason});
      }
      return;
    }
    
    var statusLocation = data['ExecuteResponse'].attr.statusLocation;
    makeWPSMonitorCall(statusLocation);
  };
  
  var makeWPSMonitorCall = function(statusLocation){
    console.log("makeWPSMonitorCall for "+statusLocation);
    var timer = new WMJSTimer();
    var firstTime = 10;
    var _makeWPSMonitorCall = function(){
      $.ajax({
        dataType: "jsonp",
        url: xml2jsonrequestURL+"request="+URLEncode(statusLocation),
        data:'',
        success: function(d){WPSMonitorSuccess(d,statusLocation);}
      }).fail(function(e){
        running = false;
        options.failure({'message':"<h1>Invalid JSON returned from server:</h1><hr/>"+e.responseText});
        return;  
      });

      if(running){
        if(firstTime>0){
          firstTime--;
          timer.init(200, _makeWPSMonitorCall);
        }else{
          timer.init(1000, _makeWPSMonitorCall);
        }
      }
    };
    _makeWPSMonitorCall();
  };
 
  
 
  
  /* Make the WPS execute request */
  _this.execute = function(identifier,wpsarguments){
    running = true;
    var wpsExecuteRequest = WPSURL+"service=WPS&request=execute&identifier="+identifier+"&version=1.0.0&storeExecuteResponse=true&status=true&";//&startLon=5&startLat=10&startHeight=0
    
    wpsExecuteRequest+='datainputs=';
    
    var dataInputs = "";
    for(var key in wpsarguments){
      
      if(typeof(wpsarguments[key])=='object'){
        for(var j in wpsarguments[key]){
          if(dataInputs.length>0)dataInputs+=";";
          dataInputs+=key+'='+encodeURIComponent(wpsarguments[key][j]);
        }
      }else{
        if(dataInputs.length>0)dataInputs+=";";
        dataInputs+=key+'='+encodeURIComponent(wpsarguments[key]);
      }
    }
    wpsExecuteRequest+=dataInputs;
    $.ajax({
      type: "POST",
      dataType: "jsonp",
      url: xml2jsonrequestURL,
      data:{'request':wpsExecuteRequest},
      success: executeSuccess
    }).fail(function(e){options.failure({'message':e.responseText});;});
  };
  
  _this.parseStatuslocation = function(_statuslocation){
    running = true;
    makeWPSMonitorCall(_statuslocation);
  };
    
};


      
