$( document ).ready(function() {
  $(".c4i-tokenapi-filelinks").hide();
  var updateTokenList = function(){
    $(".c4i-tokenapi-overview").html("Loading");

    var httpCallback = function(data){
      if(handleErrorFromJSONP(data)){
        return false;
      }
      var html='';
      html+='<table class="c4i-tokenapi-tokenlisttable c4i-tokenapi-table ">';
      html+='<tr><th>#</th><th>creationdate</th><th>notbefore</th><th>notafter</th><th>token</th><th>X</th></tr>';

      for(var j=0;j<data.length;j++){
        html+='<tr><td>'+(j+1)+'</td><td>'+data[j].creationdate+'</td><td>'+data[j].notbefore+'</td><td>'+data[j].notafter+'</td><td>'+data[j].token+'</td><td><button class="c4i-tokenapi-revoketoken" name="'+data[j].token+'">X</button></td></tr>';
      }

      html+='</table>';
      $(".c4i-tokenapi-overview").html(html);

      $(".c4i-tokenapi-revoketoken").button().click(function(){
        //console.log("Clicked "+$(this).attr("name"));
        revokeToken($(this).attr("name"));
      });
    };
    var url="/impactportal/tokenapi?service=account&request=listtokens";
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
  };

  updateTokenList();

  function isFunction(functionToCheck) {
    if(!functionToCheck)return false;
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
   }
  
  var generateToken = function(doneCallback){
    var httpCallback = function(data){
      if(handleErrorFromJSONP(data)){
        return false;
      }
      if(isFunction(doneCallback))doneCallback();
    };
    var url="/impactportal/tokenapi?service=account&request=generatetoken";
    $.ajax({
      url: url,
      crossDomain:true,
      dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      httpCallback({"error":"Request failed for "+url});
    }).always(function(){
      updateTokenList();  
    });

  };



  var revokeToken = function(tokenId){
    var httpCallback = function(data){
      if(handleErrorFromJSONP(data)){
        return false;
      }
    };
    var url="/impactportal/tokenapi?service=account&request=revoketoken&token="+tokenId;
    $.ajax({
      url: url,
      crossDomain:true,
      dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      httpCallback({"error":"Request failed for "+url});
    }).always(function(){
      updateTokenList();  
    });

  };

  var showBasketWidget= function(){
    var showBasketAfterChecksAreOK = function(data){
      basketWidget.show(function(selectedNodes) {
        if(selectedNodes.length!=1){
          alert("Please select one file from My Data");
          return false;
        }
        if(selectedNodes[0].dapurl){
          if(selectedNodes[0].dapurl.indexOf("DAP")==-1){
            alert("Please select one file from My Data");
            return false;
          }
        }
        if(selectedNodes[0].httpurl){
          if(selectedNodes[0].httpurl.indexOf("DAP")==-1){
            alert("Please select one file from My Data");
            return false;
          }
        }
        console.log(selectedNodes[0].httpurl);
  
  
  
        var html="The file is now directly accessible via commandline over HTTP and OpenDAP with the following link:<br/>";
        var url = selectedNodes[0].dapurl.replace("/DAP/","/DAP/"+data[0].token+"/");
        html+="<span class=\"c4i-tokenapi-code\"><a href=\""+url+"\">"+url+"</a></span>";
        html+="You can for example try the following command:<br/>";
        html+="<span class=\"c4i-tokenapi-code\">ncdump -h "+url+" </span>";
        html+="Please note that webservices like WMS, WCS and WPS can be used in a similar way, see below. ESGF resources accessible to your account will also be accessible to these services.";
        $(".c4i-tokenapi-filelinks").html(html);
        $(".c4i-tokenapi-filelinks").show();
   
  
  
        return true;
      });
    };

    //Callback from get tokenlist
    var httpCallback = function(data){
      if(handleErrorFromJSONP(data)){
        return false;
      }
      if(data.length == 0){
        //No tokens, so generate one, when finished, call this function again.
        generateToken(function(){showBasketWidget();});
        return false;
      }
      //OK!
      showBasketAfterChecksAreOK(data);
    };
    // Get tokenlist from server
    $.ajax({
      url: "/impactportal/tokenapi?service=account&request=listtokens",
      crossDomain:true,
      dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      httpCallback({"error":"Request failed for "+url});
    });
  };

  $(".c4i-tokenapi-buttongenerate").button().click(generateToken);

  $(".c4i-tokenapi-buttondoforfile").button().click(showBasketWidget);


  var handleErrorFromJSONP = function(data){
    if(!data.error&&!data.exception)return false;
    if(data.statuscode){
      if(data.statuscode==401){
        generateLoginDialog(function(){ 
          updateTokenList();
        });
        return;
      }
    }
    alert(data.error);
    console.log(data.exception);
    return true;
  };


});