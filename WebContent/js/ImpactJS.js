var mouseXPosition;
var mouseYPosition; 
function IsNumeric(sText){
   var ValidChars = "0123456789.";
   var IsNumber=true;
   var Char;
  for (i = 0; i < sText.length && IsNumber == true; i++) 
   { 
     Char = sText.charAt(i); 
     if (ValidChars.indexOf(Char) == -1) 
     {
       IsNumber = false;
     }
   }
   return IsNumber;
 };
 
 function dump(arr,level,path) {
   var dumped_text = "";
   if(!path)path="";
   if(!level) level = 0;
   //var level_padding=path;   
   if(typeof(arr) == 'object') { //Array/Hashes/Objects 
     for(var item in arr) {
       var value = arr[item];
       var newpath=path;
       if(IsNumeric(item)){
         item="["+item+"]"; 
         newpath = path.substr(0,path.length-1)+item; 
       }else{
         newpath = path+item;
       }
       if(typeof(value) == 'object') { //If it is an array,
//         dumped_text += newpath + "=object<br>\n";
         dumped_text += dump(value,level+1,newpath+".");
       } else {
         dumped_text += newpath + "=\"" + value + "\"<br>\n";
       }
     }
   } else { //Stings/Chars/Numbers etc.
     dumped_text = "===>"+arr+"<===("+typeof(arr)+")";
   }
   return dumped_text;
 };
 
  
  var startSearchKeyPressed = function(e,form){
	  var key=e.keyCode || e.which;
	  if (key==13){
		  startSearch();
	  }

  };
  
  var startSearch = function(page){
      var searchboxId = document.getElementById("searchbox");
      
	  var newPage="?q=search/node/"+searchboxId.value;
	  if(page)newPage=page+newPage;
	  window.location = newPage;
  }
  
  
  var customAlertDiv = undefined;
  
  var removeCustomAlert = function(){
	  document.body.removeChild(customAlertDiv);
  };
  var customAlertKeyPress = function(event){
	  removeCustomAlert(); 
  };
  var customalert = function(msg){
	  if(!customAlertDiv){
		 /* customAlertDiv = document.createElement('div');
		  
		  
		  customAlertDiv.className+="customAlert";
	
		  customAlertDiv.customFocus = function(){
			  try{document.getElementById('customAlertButton').focus();}catch(e){}
		  }
		  customAlertDiv.onkeypress = customAlertDiv.customFocus;
		  customAlertDiv.onclick = customAlertDiv.customFocus;
		  
		  var cnt = document.createElement('div');
		  cnt.style.margin='0px';
		  cnt.style.padding='14px 6px 6px 6px';
		  customAlertDiv.appendChild(cnt);
		  var btn = document.createElement('div');
		  btn.style.padding='0px';
		  btn.style.margin='0px';
		  btn.style.paddingTop='4px'; 
		  
		  
		  btn.style.marginLeft='455px';
		  btn.innerHTML='<input id=\'customAlertButton\' type="button" value="Ok" onclick="removeCustomAlert()" />';
		  customAlertDiv.appendChild(btn);
		  customAlertDiv.cnt=cnt;
		  customAlertDiv.btn=btn;
		  cnt.onkeypress = customAlertKeyPress;
		  btn.onkeypress = customAlertKeyPress;*/
		  customAlertDiv = $( "<div/>");
		  customAlertDiv.dialog({
			  //modal: true,
			  title:'Message',
			  width:500,
			  height:300,
			  buttons: {
			  'Ok': function() {
			  $( this ).dialog( "close" );
			  }
			  }
			  });
	 }
	  customAlertDiv.html(msg);
	  customAlertDiv.dialog();
	  //customAlertDiv.show();
	  //customAlertDiv.dialog.open();
	  //customAlertDiv.cnt.innerHTML = "<textarea style=\"display:block;padding:0px;margin:0;width:100%;height:200px;border:none;background-color:#DFE9FF;overflow:auto;\">"+msg+"</textarea>";
	  //document.body.appendChild(customAlertDiv);
	  //document.getElementById('customAlertButton').focus();
  };
  
  
  var toISO8601YYYYMMHH=function (thisdate){
    function prf(input,width){
      //print decimal with fixed length (preceding zero's)
      var string=input+'';
      var len = width-string.length;
      var j,zeros='';
      for(j=0;j<len;j++)zeros+="0"+zeros;
      string=zeros+string;
      return string;
    }
    var iso=prf(thisdate.getUTCFullYear(),4)+
        "-"+prf(thisdate.getUTCMonth()+1,2)+
            "-"+prf(thisdate.getUTCDate()+1,2)+
                "T"+prf(0,2)+
                    ":"+prf(0,2)+
                        ":"+prf(0,2)+'Z';
                        return iso;
  };
  
  var toISO8601=function (thisdate){
      function prf(input,width){
        //print decimal with fixed length (preceding zero's)
        var string=input+'';
        var len = width-string.length;
        var j,zeros='';
        for(j=0;j<len;j++)zeros+="0"+zeros;
        string=zeros+string;
        return string;
      }
      var iso=prf(thisdate.getUTCFullYear(),4)+
          "-"+prf(thisdate.getUTCMonth()+1,2)+
              "-"+prf(thisdate.getUTCDate()+1,2)+
                  "T"+prf(thisdate.getUTCHours(),2)+
                      ":"+prf(thisdate.getUTCMinutes(),2)+
                          ":"+prf(thisdate.getUTCSeconds(),2)+'Z';
                          return iso;
  };
  
  var fromISO8601=function (iso8601datestring){
    //1968-04-08T22:00:00Z
    //01234567890123456789
   return new Date( iso8601datestring.substring(0,4), iso8601datestring.substring(5,7)-1, iso8601datestring.substring(8,10), 
        iso8601datestring.substring(11,13), iso8601datestring.substring(14,16), iso8601datestring.substring(17,18));

  };
  

  var alert = (function(msg) { return function(msg) {customalert(msg);}})();
  
  
  /**
   * Updates the number of datasets in the basket in the menu bar
   */
  var adjustNumberOfDataSetsDisplayedInMenuBar = function(json){
  	var numproducts = json.numproducts;
  
  	try{
  	    if(numproducts!=0){
  	      $('#baskettext1').html('('+numproducts+')');
  	      $('#baskettext2').html('('+numproducts+')');
  	    }else{
  	      $('#baskettext1').html("(-)");
  	      $('#baskettext2').html("(-)");
  	    }
  	}catch(e){
  	}

  	 var numproductsadded = json.numproductsadded;
     if(numproductsadded == undefined){
  		return;
  	 }
     var msgHTML='Added '+numproductsadded+' product(s) to your basket.</br></br>';

     if(numproductsadded==0){
       var msgHTML='This product is already in your basket.</br></br>';
     }
     
     if(numproductsadded == undefined){
    	 msgHTML = "";
     }
     
     msgHTML+="You have "+numproducts+" product(s) in your basket.";
     var itemAddedToolTip = $( "<div/>" );
     itemAddedToolTip.dialog({
    	 autoOpen: false,
    	 dialogClass: "noTitleStuff",
    	 height:90,
    	 width:300,
    	 show: {
        	 effect: "fade",
        	 duration: 100
    	 },
    	 hide: {
        	 effect: "fade",
        	 duration: 100
    	 },
    	 position:[mouseXPosition-360,mouseYPosition-40]
    	 
	 });

	 itemAddedToolTip.dialog("open");
    	 
     itemAddedToolTip.html(msgHTML);
     setTimeout(function(){itemAddedToolTip.dialog("close")},1000);
    
  };

  /**
   * Updates the number of jobs in the basket in the menu bar
   */
  var adjustNumberOfJobsDisplayedInMenuBar = function(json){
  	var numproducts = json.numproducts;
  	try{
  	    if(numproducts!=0){
  	      Ext.fly('jobnumber').dom.innerHTML='('+numproducts+')';
  	      
  	    }else{
  	      Ext.fly('jobnumber').dom.innerHTML ="(-)";
  	      
  	    }
  	}catch(e){
  	}
  };
 
  

  function Timer(){
    var timerID = null;
    var timerRunning = false;
    var delay = 10;
    var secs;
    var initsecs;
    var timehandler='';
    var i = this;
    this.InitializeTimer = function(secstime,functionhandler){
      // Set the length of the timer, in seconds
      secs = secstime;
      initsecs=secs;
      timehandler=functionhandler;
      StopTheClock();
      if(secs>0)StartTheTimer();
    };
    this.ResetTimer =function(){
      secs=initsecs;
    };
    this.StopTimer =function(){
      StopTheClock();
    };
    function StopTheClock(){
      if(timerRunning)clearTimeout(timerID);
      timerRunning = false;
    };
    function TimeEvent(){
      if(timehandler!='')timehandler();
    };
    function StartTheTimer (){
      if (secs==0){
        StopTheClock();
        TimeEvent();
      }
      else{
        secs = secs - 1;
        timerRunning = true;
        timerID = self.setTimeout(function(){StartTheTimer();}, delay);
      }
    };
  }

  /**
   * Encodes plain text to URL encoding
   */
  var URLEncode = function(plaintext){
    if(!plaintext)return plaintext;
    var SAFECHARS = "0123456789" +          // Numeric
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +  // Alphabetic
        "abcdefghijklmnopqrstuvwxyz" +
        "-_.!~*'()";          // RFC2396 Mark characters
    var HEX = "0123456789ABCDEF";
    plaintext=plaintext.replace(/\%/g,"%25");
    plaintext=plaintext.replace(/\+/g,"%2B");
    plaintext=plaintext.replace(/\ /g,"%20");
    plaintext=plaintext.replace(/\^/g,"%5E");
    plaintext=plaintext.replace(/\&/g,"%26");
    plaintext=plaintext.replace(/\?/g,"%3F");
    plaintext=plaintext.replace(/\>/g,"%3E");
    plaintext=plaintext.replace(/\</g,"%3C");
    plaintext=plaintext.replace(/\\/g,"%5C");
    var encoded = "";
    for (var i = 0; i < plaintext.length; i++ ) {
      var ch = plaintext.charAt(i);
      if (ch == " ") {
        encoded += "%20";        // x-www-urlencoded, rather than %20
      } else if (SAFECHARS.indexOf(ch) != -1) {
        encoded += ch;
      } else {
        var charCode = ch.charCodeAt(0);
        if (charCode > 255) {
          alert( "Unicode Character '" 
              + ch 
              + "' cannot be encoded using standard URL encoding.\n" +
              "(URL encoding only supports 8-bit characters.)\n" +
              "A space (+) will be substituted." );
          encoded += "+";
        } else {
          encoded += "%";
          encoded += HEX.charAt((charCode >> 4) & 0xF);
          encoded += HEX.charAt(charCode & 0xF);
        }
      }
    } 
    return encoded;
  };

  /** 
   * For a string, replaces all instances of the given substring.
   */
  String.prototype.replaceAll = function(
      strTarget, // The substring you want to replace
      strSubString // The string you want to replace in.
      ){
    var strText = this;
    var intIndexOfMatch = strText.indexOf( strTarget );
    // Keep looping while an instance of the target string
    // still exists in the string.
    while (intIndexOfMatch != -1){
      // Relace out the current instance.
      strText = strText.replace( strTarget, strSubString );
      // Get the index of any next matching substring.
      intIndexOfMatch = strText.indexOf( strTarget );
    }
    // Return the updated string with ALL the target strings
    // replaced out with the new substring.
    return( strText );
  };

  /** 
   * For a string, trims the spaces from the string
   */
  String.prototype.trim = function () {
    var value=this;
    value = value.replace(/^\s+/,'');
    value = value.replace(/\s+$/,'');
    return value;
  };

  /** 
   * Checks whether an object is numeric
   */
  function IsNumeric(sText){
    var ValidChars = "0123456789.";
    var IsNumber=true;
    var Char;
    for (i = 0; i < sText.length && IsNumber == true; i++){ 
      Char = sText.charAt(i); 
      if (ValidChars.indexOf(Char) == -1) {
            IsNumber = false;
          }
      }
    return IsNumber;

  }

  /**
   * returns the object as a bunch of text
   * @param arr  The object to be dumped to text
   */
  function dumpObject(arr,level,path) {
     var dumped_text = "";
     if(!path)path="";
     if(!level) level = 0;
     var level_padding=path;    
     if(typeof(arr) == 'object') { //Array/Hashes/Objects 
       for(var item in arr) {
         var value = arr[item];
         var newpath=path;
         if(IsNumeric(item)){
           item="["+item+"]";     
           newpath = path.substr(0,path.length-1)+item;   
         }else{
           newpath = path+item;
         }
         if(typeof(value) == 'object') { //If it is an array,
//           dumped_text += newpath + "=object<br>\n";
           dumped_text += dumpObject(value,level+1,newpath+".");
         } else {
           dumped_text += newpath + "=\"" + value + "\"<br>\n";
         }
       }
     } else { //Stings/Chars/Numbers etc.
       dumped_text = "===>"+arr+"<===("+typeof(arr)+")";
     }
     return dumped_text;
   }
   

  function createXHR(){
	  try { return new XMLHttpRequest(); } catch(e) {}
	  try { return new ActiveXObject("Msxml2.XMLHTTP.6.0"); } catch (e) {}
	  try { return new ActiveXObject("Msxml2.XMLHTTP.3.0"); } catch (e) {}
	  try { return new ActiveXObject("Msxml2.XMLHTTP"); } catch (e) {}
	  try { return new ActiveXObject("Microsoft.XMLHTTP"); } catch (e) {}
	  alert("XMLHttpRequest not supported");
	  return false;
	}
	/**
	 * Function to create JSON requests. For a given URL, a JSON object is returned using a callback function.
	 *
	 * @param urlRequest  The URL to do the request on
	 * @param callbackJSON  The function to call with as first argument the JSON object.
	 * @param callbackError  (optional) The function to call when a error occurs. When undefined, callbackfunction with an undefined object is used.
	 * @param passObjPointer  (optional) Pointer which will be passed to the callback function as second argument
	 * @param tryProxyRequestOnFailure  (optional) Overrides wether to use direct client server http requests, or server routed http requests.
	 */

	var useProxyByDefault=false;
	function makeJSONRequest(urlRequest,callbackJSON,callbackError,passObjPointer,tryProxyRequestOnFailure){
	  var origRequest=urlRequest;
	  function requestError(errorMessage,xhr){
	    if(xhr){
	      errorMessage='Code '+xhr.status+'.'+" Unable to get "+origRequest+" "+errorMessage;
	    }else errorMessage="Unable to get "+origRequest+" "+errorMessage;
	    if(callbackError)callbackError(errorMessage,passObjPointer);
	    else if(callbackHTTP)callbackHTTP(undefined,passObjPointer);
	  }
	  
		var callbackHTTP=function(responseText,passObjPointer){
			if(responseText==undefined){
				if(callbackJSON)callbackJSON(undefined,passObjPointer);
				return;
			}
			try{
				var data=eval("(" + responseText + ")");
			}catch(err){
				requestError('JSON Exception occured: '+err);
				return;
			}
			if(data==undefined||data==''){
				requestError('Request returned no JSON data');
			}else{
				if(callbackJSON)callbackJSON(data,passObjPointer);
			}
		};
		makeHTTPRequest(urlRequest,callbackHTTP,callbackError,passObjPointer,tryProxyRequestOnFailure);
	}

	function makeHTTPRequest(urlRequest,callbackHTTP,callbackError,passObjPointer,tryProxyRequestOnFailure){
	  if(urlRequest==undefined)return;
	  if(urlRequest.indexOf('?')==-1)urlRequest+='?';
	  if(tryProxyRequestOnFailure==undefined)tryProxyRequestOnFailure=true;
	  urlRequest+="&rand="+Math.random();
	  var origRequest=urlRequest;
	  if(tryProxyRequestOnFailure==false){
	    origRequest=urlRequest;
	  }
	  function requestError(errorMessage,xhr){
	    if(xhr){
	      errorMessage='Code '+xhr.status+'.'+" Unable to get "+origRequest+" "+errorMessage;
	    }else errorMessage="Unable to get "+origRequest+" "+errorMessage;
	    if(callbackError)callbackError(errorMessage,passObjPointer);
	    else if(callbackHTTP)callbackHTTP(undefined,passObjPointer);
	  }
	  
	  function makeProxyURL(urlRequest){ 
	    return;
		  alert("proxy url not set\n"+urlRequest);
	   // return "http://localhost:8083/ImpactPortal/get?request="+URLEncode(urlRequest);
	  }
	  function redirRequest(xhr){
	    //Let try an alternative way: redirect using JSP
	    if(tryProxyRequestOnFailure==true){
	      makeHTTPRequest(makeProxyURL(urlRequest),callbackHTTP,callbackError,passObjPointer,false);
	    }else{
	  		switch(xhr.status){ 
	  		  case 401:requestError(xhr.status+' Unauthorized',xhr);break;
	  		  case 403:requestError(xhr.status+' Forbidden',xhr);break;
	  		  case 404:requestError(xhr.status+' Not found',xhr);break;
	  		  default:requestError('Code '+xhr.status+'.',xhr);
	      }
	      
	    }
	  }
	  
	  try{
	    var xhr=createXHR();
	    //xhr.open("GET", urlRequest+"&rand="+Math.random(),true);
	    var newURL=urlRequest;
	    if(useProxyByDefault){
	      newURL=makeProxyURL(urlRequest);
	      tryProxyRequestOnFailure=false;
	    }
	  
	    xhr.open("GET", newURL,true);
	    xhr.onreadystatechange=function(){
	      if (xhr.readyState == 4){
	        if (xhr.status == 200){
	            if(callbackHTTP)callbackHTTP(xhr.responseText,passObjPointer);
	        }else{
	          if(xhr.status == 0){
	            redirRequest(xhr);
	          }else requestError("",xhr);
	        }
	      }
	    };
	    xhr.send(null);
	  }catch(err){
	    redirRequest();
	  }
	}

	
	$(document).ready(function () {
   
        $(document).mousemove(function (e) {
        	mouseXPosition = e.pageX- $(window).scrollLeft();
        	mouseYPosition = e.pageY- $(window).scrollTop();
        });
	});
