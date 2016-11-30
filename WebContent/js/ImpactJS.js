///* This redirect is a temporal workaround until HTTPS is forced by tomcat - updated 20160621 by mplieger*/
try{
  if(c4i_https){
    if(c4i_https!="null"){
      if (window.location.protocol != "https:"){
        var newHome = c4i_https.replace("/impactportal","");
        var newlocation= newHome+window.location.pathname;
        window.location.href = newlocation;
      }
    }
  }
}catch(e){
}

var c4iconfigjs = {
  processingservice:"/impactportal/wps?",
  searchservice:"/impactportal/esgfsearch?",
  impactservice:"/impactportal/ImpactService?",
  provenanceservice:"/impactportal/PROV?",
  basketservice:"/impactportal/basket?",
  adagucservice:"/impactportal/adagucserver?",
  adagucviewer:"/impactportal/adagucviewer/",
  howtologinlink:"/impactportal/help/howto.jsp?q=create_esgf_account",
  contactexpertlink:"/impactportal/help/contactexpert.jsp",
  xml2jsonservice:"/impactportal/wps?"
}; 

var mouseXPosition;
var mouseYPosition; 

var makeconsole = function(){
	console = [];
	console.log = function(){
	};
};

try{
	if(!console){
		makeconsole();
	}
	if(!console.log){
		makeconsole();
	}
}catch(e){
	makeconsole();
}

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
 
  
  var startSearchKeyPressed = function(e,form,elementId){
	  var key=e.keyCode || e.which;
	  if (key==13){
		  startSearch(undefined,elementId);
	  }

  };
  
  var startSearch = function(page,elementId){
    var element = elementId
    if(!element)element= "searchbox";
    var searchboxId = document.getElementById(element);
      
	  var newPage="/impactportal/general/index.jsp?q=search/node/"+searchboxId.value;
	  
	  try{
  	  if(window.location.search.indexOf("?q=")!=-1){
  	    newPage="?q=search/node/"+searchboxId.value;
  	  }
	  }catch(e){
	  }
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
			  width:550,
			  height:350,
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
    	 }
    	 //,    	 position:[mouseXPosition-360,mouseYPosition-40]
    	 
	 });

	 itemAddedToolTip.dialog("open");
	 itemAddedToolTip.dialog('option', 'position', [mouseXPosition-360,mouseYPosition-40]);
    	 
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
      // Set the length of the timer, in tenths of seconds
      secs = parseInt((secstime / 10)+0.5);
      initsecs=secs;
      timehandler=functionhandler;
      StopTheClock();
      if(secs>0)StartTheTimer();
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
    for (i = 0; i < sText.length && IsNumber == true; i++){ c4i
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
	  console.log(urlRequest)
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
		console.log("Error: makeProxyURL empty")
	    return;
		  //alert("proxy url not set\n"+urlRequest);
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

	
//============================================================================
//Name        : tools
//Author      : MaartenPlieger (plieger at knmi.nl)
//Version     : 0.5 (September 2010)
//Description : All kinds of small usable functions
//============================================================================  
function isDefined(variable){
 if(typeof variable === 'undefined'){
   return false;
 }
 return true;
};



function toArray(array){
 if(!array)return '';
 if(array.length){
   return array;
 }else{
   var newArray = new Array();
   newArray[0]=array;
   return newArray;
 }
}

var preventdefault_event = function(e){
  var event = e || window.event;
       // standaard-actie voorkomen
  if (event.preventDefault) { // Firefox
    event.preventDefault();
  }
  else { // IE
    event.returnValue = false;
  } 
}




 var attach_event = function(obj,evType, fn){
 // Attach event that works on all browsers
   if(evType=="mousewheel"){
     function wheel(event,handler){
       var delta = 0;
       if (!event){ /* For IE. */
         event = window.event;
         window.event.cancelBubble = true;
         window.event.returnValue = false;
       }
       
       if (event.wheelDelta) { /* IE/Opera. */
         delta = event.wheelDelta/120;
         
       } else if (event.detail) { /** Mozilla case. */
       /** In Mozilla, sign of delta is different than in IE.
       * Also, delta is multiple of 3.
       */
         delta = -event.detail/3;
       }
       /** If delta is nonzero, handle it.
       * Basically, delta is now positive if wheel was scrolled up,
       * and negative, if wheel was scrolled down.
       */
       if (delta)
         handler(delta);
       /** Prevent default actions caused by mouse wheel.
       * That might be ugly, but we handle scrolls somehow
       * anyway, so don't bother here..
       */
       if (event.preventDefault)
         event.preventDefault();
       event.returnValue = false;
     }
     if (obj.addEventListener)
       obj.addEventListener('DOMMouseScroll', function(e){wheel(e,fn);}, false);
     obj.onmousewheel = document.onmousewheel = function(e){wheel(e,fn);};
     return;
   }
 
  if (browser.isNS){
    obj.addEventListener(evType,fn,   true);
  } 
  else  {
    obj.attachEvent(("on"+evType), fn);
    if(window.event==undefined)return;
    window.event.cancelBubble = true;
    window.event.returnValue = false;
  }
  
}
 
var del_event = function (obj,event_id, funct){
  var flag = true; if (browser.isOP){flag=false;}
  if (obj.removeEventListener)
  {
    obj.removeEventListener(event_id, funct, flag);
  } else if(obj.detachEvent)
  {
    obj.detachEvent(event_id, funct);
    obj.detachEvent('on'+event_id, funct);
  }
}
var getClick_X = function(p_event){
  var my_x;
  if (browser.isNS)
  {
    my_x = p_event.clientX + window.scrollX;
  } else
  {
    my_x = window.event.clientX + document.documentElement.scrollLeft
        + document.body.scrollLeft;
  }
  return my_x;
}
 
var getClick_Y = function(p_event){
  var my_y;
  if (browser.isNS)
  {
    my_y = p_event.clientY + window.scrollY;
  } else
  {
    my_y = window.event.clientY + document.documentElement.scrollTop
        + document.body.scrollTop;
  }
  return my_y;
}

var findElementPos = function(obj){
 var el=obj;
  var curleft = curtop = 0;
  while(obj)
  {
    curleft += obj.offsetLeft;
    curtop += obj.offsetTop;
    obj = obj.offsetParent;
  } 
  return [curleft,curtop,parseInt(el.style.width+curleft),parseInt(el.style.height+curtop)];
}

function Browser(){
  var ua, s, i;
  this.isIE    = false;
  this.isNS    = false;
  this.isOP    = false;
  this.isKonqueror = false;
  this.name    = navigator.appName;
  this.version = null;

  ua = navigator.userAgent;
  if ((navigator.userAgent).indexOf("Opera")!=-1)
  {
    this.isOP = true;
  } else
    if (navigator.appName=="Netscape"||navigator.appName=="Konqueror")
    {
      this.isNS = true;
      this.isKonqueror = true;
    } else
      if ( (navigator.appName).indexOf("Microsoft") != -1 )
      {
        this.isIE = true;
      }
    return;
}
var browser = new Browser();



     
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
}

function dump(arr,level,path) {
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
//        dumped_text += newpath + "=object<br>\n";
        dumped_text += dump(value,level+1,newpath+".");
      } else {
        dumped_text += newpath + "=\"" + value + "\"<br>\n";
      }
    }
  } else { //Stings/Chars/Numbers etc.
    dumped_text = "===>"+arr+"<===("+typeof(arr)+")";
  }
  return dumped_text;
}



//============================================================================
//Name        : HTTP_RequestFunctions.js
//Author      : MaartenPlieger (plieger at knmi.nl)
//Version     : 0.5 (September 2010)
//Description : Functions to make HTTP requests 
//============================================================================  

function createXHR(){
 try { return new XMLHttpRequest(); } catch(e) {}
 try { return new ActiveXObject("Msxml2.XMLHTTP.6.0"); } catch (e) {}
 try { return new ActiveXObject("Msxml2.XMLHTTP.3.0"); } catch (e) {}
 try { return new ActiveXObject("Msxml2.XMLHTTP"); } catch (e) {}
 try { return new ActiveXObject("Microsoft.XMLHTTP"); } catch (e) {}
 return false;
}



function MakeJSONRequest(fname,callbackfunction,errorfunction,pointer,useredirect){
   if(fname.indexOf('?')==-1){
   fname+='?';
 }else{
   fname+='&';
 }
 fname+="rand="+Math.random()
 function requestError(errorMessage){
   if(errorfunction)errorfunction(errorMessage,pointer);
   else callbackfunction(undefined,pointer);
 }
 
 function redirRequest(){
   //Let try an alternative way: redirect using PHP
   if(useredirect==false){
     fname=requestProxy+"REQUEST="+URLEncode(fname);
     MakeJSONRequest(fname,callbackfunction,errorfunction,pointer,true)
   }else{
     requestError('status('+xhr.status+') to '+fname);
   }
 }
 if(!useredirect)useredirect=false;
 try{
   var xhr=createXHR();
   xhr.open("GET", fname,true);
   xhr.onreadystatechange=function(){
     if (xhr.readyState == 4){
       if (xhr.status == 200){
         try{

           var data=eval("(" + xhr.responseText + ")");
         }
         catch(err){
           
           requestError("Invalid JSON: '"+xhr.responseText+"'");
           return;
         }
         if(data==undefined){
           requestError('request returned no data:'+fname);
         }else{
           callbackfunction(data,pointer);
         }
       }else{
         redirRequest();
       }
     }
   }
   xhr.send(null);
 }catch(err){
   redirRequest();
 }
}

function MakeHTTPRequest(fname,callbackfunction,errorfunction,pointer,useredirect){
 if(fname.indexOf('?')==-1){
   fname+='?';
 }else{
   fname+='&';
 }
 fname+="rand="+Math.random()
 function requestError(errorMessage){
   if(errorfunction)errorfunction(errorMessage,pointer);
   else callbackfunction(undefined,pointer);
 }
 function redirRequest(){
   //Let try an alternative way: redirect using PHP
   if(useredirect==false){
     //alert(fname);
     fname=requestProxy+"REQUEST="+URLEncode(fname);
     //alert(fname);
     MakeHTTPRequest(fname,callbackfunction,errorfunction,pointer,true)
   }else{
     requestError('status('+xhr.status+') to '+fname);
   }
 }
 if(!useredirect)useredirect=false;
 try{
 var xhr=createXHR();
 xhr.open("GET", fname,true);
 xhr.onreadystatechange=function(){
   if (xhr.readyState == 4){
     if (xhr.status == 200){
       try{
         var data=xhr.responseText;
       }
       catch(err){
         requestError('Exception occured:'+err);
         return;
       }
       if(data==undefined||data==''){
         requestError('request returned no data');
       }else{
         callbackfunction(data,pointer);
       }
     }else{
       redirRequest();
     }
   }
 }
 xhr.send(null);
 }catch(err){
   redirRequest();
 }
}

var URLDecode = function(encodedURL){
 if(!isDefined(encodedURL))return "";
 encodedURL=encodedURL.replaceAll('+'," ");
 encodedURL=encodedURL.replaceAll('%2B',"+");
 encodedURL=encodedURL.replaceAll('%20'," ");
 encodedURL=encodedURL.replaceAll('%5E',"^");
 encodedURL=encodedURL.replaceAll('%26',"&");
 encodedURL=encodedURL.replaceAll('%3F',"?");
 encodedURL=encodedURL.replaceAll('%3E',">");
 encodedURL=encodedURL.replaceAll('%3C',"<");
 encodedURL=encodedURL.replaceAll('%5C',"\\");
 encodedURL=encodedURL.replaceAll('%2F',"/");
 encodedURL=encodedURL.replaceAll('%25',"%");
 encodedURL=encodedURL.replaceAll('%3A',":");
 encodedURL=encodedURL.replaceAll('%27',"'");
 encodedURL=encodedURL.replaceAll('%24',"$");
 encodedURL=encodedURL.replaceAll('%27',"'");
 encodedURL=encodedURL.replaceAll('%23',"#");
 encodedURL=encodedURL.replaceAll('%2C',":");
 encodedURL=encodedURL.replaceAll('%28',"(");
 encodedURL=encodedURL.replaceAll('%29',")");
 return encodedURL;
}

//Encodes plain text to URL encoding
var URLEncode = function(plaintext){

 if(!plaintext)return plaintext;
 if(plaintext==undefined)return plaintext;
 if(plaintext=="")return plaintext;
 if(typeof(plaintext)!='string')return plaintext;
 var SAFECHARS = "0123456789" +          // Numeric
     "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +  // Alphabetic
     "abcdefghijklmnopqrstuvwxyz" +
     "%-_.!~*'()";          // RFC2396 Mark characters
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
}

//Replaces all instances of the given substring.
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
}

//Trims the spaces from the string
String.prototype.trim = function () {
 var value=this;
 value = value.replace(/^\s+/,'');
 value = value.replace(/\s+$/,'');
 return value;
}


function checkValidInputTokens(stringToCheck){
 
 var filter = /^([a-zA-Z'_:~%?\$,\.\0-9 \-=/&])+$/;
 if (filter.test(stringToCheck)){
   var t=URLDecode(stringToCheck);
  
   if(filter.test(t)){
     return true;
   }
 
 }
 
 return false;
};

//Read a page's GET URL variables and return them as an associative array (From Roshambo's code snippets)
function getUrlVars()
{
   var vars = [], hash;

   var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
   for(var i = 0; i < hashes.length; i++)
   {
       hash = hashes[i].split('=');
       
       hash[1] = URLDecode(hash[1]);
       //vars.push({hash[0]:hash[1]});
       if(checkValidInputTokens(hash[0])==false||checkValidInputTokens(hash[1])==false){
         
       }else{
         vars[hash[0]] = hash[1]+"";
       }
   }
   return vars;
} 

//Read a page's GET URL variables and return them as an associative array (From Roshambo's code snippets)
function getUrlVarsFromHashTag()
{
   var vars = [], hash;
   var splitloc = window.location.hash.indexOf('#');
   if(window.location.hash[splitloc+1]=='?')splitloc++;
   var hashString = window.location.hash.slice(splitloc + 1);
   
   var hashes = hashString.split('&');
   for(var i = 0; i < hashes.length; i++)
   {
       hash = hashes[i].split('=');
       
       hash[1] = URLDecode(hash[1]);
       //vars.push({hash[0]:hash[1]});
       if(checkValidInputTokens(hash[0])==false||checkValidInputTokens(hash[1])==false){
         
       }else{
         vars[hash[0]] = hash[1]+"";
       }
   }
   return vars;
} 


//Splits a URL in a location part and separate Key Value Pairs (kvp).
var composeUrlObjectFromURL = function(url){
 var vars = []
 if(!isDefined(url)){
   return vars;
 }
 
 
 var location = '';
 var hashes = [];
 var urlParts = url.split('?');
 
 if(urlParts.length >1){
   location = urlParts[0];
   hashes = urlParts[1].split('&');
 }else{
   hashes = urlParts[0].split('&');
 }
 

 for(var i = 0; i < hashes.length; i++){
   hash = hashes[i].split('=');
   
   if(isDefined(hash[1])){
     hash[1] = URLDecode(hash[1]);
     if(checkValidInputTokens(hash[0])==false||checkValidInputTokens(hash[1])==false){
     }else{
       if(isDefined(hash[1])){
         if(hash[1].length>0){
           vars[hash[0].toLowerCase()] = hash[1]+"";
         }
       }
     }
   }
 }
 if(hashes[0].indexOf("=")==-1){
   if(checkValidInputTokens(hashes[0])){
     location = hashes[0];

   }
 }
 return {location:location,kvp:vars};
};
   
//Check for hash tag changes.
var currentLocationHash = '';
var hashTagCheckerInUse = false;
var _checkIfHashTagChanged = function(callback){
 
 var identifier = window.location.hash; //gets everything after the hashtag i.e. #home
 if(currentLocationHash!=identifier&&identifier.length>0){
   currentLocationHash=identifier;
   if(window.location.href.indexOf("#")!=-1){
     var hashLocation = (window.location.href.split('#')[1]); // Firefox automatically urldecodes the hashtag, so use href instead.
     hashLocation = hashLocation.replaceAll("%27","'"); // Firefox automatically encodes ' tokens into %27 for some reason. We can safely decode.
     urlVars = getUrlVarsFromHashTag();
     if(isDefined(urlVars.clearhash)){
       if(urlVars.clearhash=="1"){
         window.location.hash = "";
         currentLocationHash="";
       }
     }
     // alert(hashLocation);
     
     //alert(identifier+"\n"+window.location.href);
     callback(hashLocation,urlVars);
   }

 }
 setTimeout(function() { _checkIfHashTagChanged(callback)},100);
};

var checkIfHashTagChanged = function(callback){
 if(!isDefined(callback))return;
 if(hashTagCheckerInUse == true){
   alert('checkIfHashTagChanged is in use');
   return;
 }
 hashTagCheckerInUse = true;
 _checkIfHashTagChanged(callback);
};
