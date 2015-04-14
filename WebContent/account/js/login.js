
  var OpenIDProviders = {
      'SMHI-NSC-LIU':{
        name:'SMHI-NSC-LIU',
        openidprefix:'https://esg-dn1.nsc.liu.se/esgf-idp/openid/',
        createaccount:'https://esg-dn1.nsc.liu.se/esgf-web-fe/createAccount',
        accountinfo:'http://esg-dn1.nsc.liu.se/esgf-web-fe/accountsview',
        logocls:'logo_Sweden'},
      'PCMDI':       {
    	  name:'PCMDI'       ,
    	  openidprefix:'https://pcmdi9.llnl.gov/esgf-idp/openid/',
    	  createaccount:'https://pcmdi9.llnl.gov/esgf-web-fe/createAccount',
    	  accountinfo:'http://pcmdi9.llnl.gov/esgf-web-fe/accountsview',
    	  logocls:'logo_USA'},
  	  'IPSL':       {
        name:'IPSL'       ,
        openidprefix:'https://esgf-node.ipsl.fr/esgf-idp/openid/',
        createaccount:'https://esgf-node.ipsl.fr/esgf-web-fe/createAccount',
        accountinfo:'http://esgf-node.ipsl.fr/esgf-web-fe/accountsview',
        logocls:'logo_France'},
      'CEDA':        {
    	  name:'BADC/CEDA'   ,
    	  openidprefix:'https://ceda.ac.uk/openid/',
    	  createaccount:'https://services.ceda.ac.uk/cedasite/register/info/',        
    	  accountinfo:'https://services.ceda.ac.uk/cedasite/register/info/',
    	  logocls:'logo_UK'},
      'DKRZ':        {
    	  name:'DKRZ'        ,
    	  openidprefix:'https://esgf-data.dkrz.de/esgf-idp/openid/',
    	  createaccount:'https://esgf-data.dkrz.de/esgf-web-fe/createAccount',
    	  accountinfo:'https://esgf-data.dkrz.de/esgf-web-fe/accountsview',
    	  logocls:'logo_Germany'},
	  /*'NCI':        {
    	  name:'NCI'        ,
    	  openidprefix:'https://esg2.nci.org.au/esgf-idp/openid/' ,
    	  createaccount:'https://esg2.nci.org.au/esgf-web-fe/createAccount',
    	  logocls:'logo_Germany'},*/
      'PIK':         {
    	  name:'PIK Potsdam' ,
    	  openidprefix:'https://esg.pik-potsdam.de/esgf-idp/openid/',
    	  createaccount:'https://esg.pik-potsdam.de/esgf-web-fe/createAccount',
    	  accountinfo:'https://esg.pik-potsdam.de/esgf-web-fe/accountsview',
    	  logocls:'logo_Germany'}
      
  };


 var getOpenIDProviderFromOpenId = function(openidIdentifier){
	 for(var id in OpenIDProviders){
		 var provider = OpenIDProviders[id];
		 if(openidIdentifier.indexOf(provider.openidprefix)!=-1){
			 return provider;
		 }
	 }
 }
  

var checkOpenIdCookie = function(a) {
  if (a.checked == false) {
    makeHTTPRequest("/impactportal/consumer?keepid=off");
  } else {
    makeHTTPRequest("/impactportal/consumer?keepid=on");
  }

};

$( document ).ready(function() {
  var dataNodeButtons = $("#datanodebuttons");
  var html='<ul>';
  for(var id in OpenIDProviders){
    html+='<li><button id="dnb_'+id+'" class="datanodebutton" onclick="openDialog(\''+id+'\');">'+OpenIDProviders[id].name+'</button></li>';// - '+ OpenIDProviders[id].url+ '&lt;username&gt;</li>' ;
    //alert(id.name);
  }
  html+='</ul>';
  dataNodeButtons.html(html);
  for(var id in OpenIDProviders){
 
    $("#dnb_"+id).button({icons: {primary: OpenIDProviders[id].logocls},text:true});
  }
  $('#login_button').button();
});

$(function() {

  
  
  var name = $("#name"),  allFields = $([]).add(name), tips = $(".validateTips");
  name.keyup(function(e) {
    if (e.keyCode == 13) {
      $('#composeidentifierbutton').click();
    }
  });
  name.on('input',function(){
    composeId();
  });
  
  // Following lines are for IE7 Fix:
  var namebusy=false;
  name.on('propertychange',function(){
	  if(namebusy == true)return;
	  namebusy = true;
	  composeId();
	  namebusy = false;
  });
  
  
  function updateTips(t) {
    tips.text(t).addClass("ui-state-highlight");
    setTimeout(function() {
      tips.removeClass("ui-state-highlight", 1500);
    }, 500);
  }
  function checkLength(o, n, min, max,showtips) {
    if (o.val().length > max || o.val().length < min) {
      if(showtips){
        o.addClass("ui-state-error");
        updateTips("*Length of " + n + " must be between " + min + " and " + max
            + " characters.");
      }
      return false;
    } else {
      return true;
    }
  }
  function checkRegexp(o, regexp, n,showtips) {
    if (!(regexp.test(o.val()))) {
      if(showtips){
        o.addClass("ui-state-error");
        updateTips(n);
      }
      return false;
    } else {
      return true;
    }
  }

  var composeId = function(showtips){
    name.removeClass("ui-state-error");
    tips.removeClass("ui-state-highlight");
    tips.text("");
    var bValid = true;
    bValid = bValid && checkLength(name, "username", 1, 50,showtips);
    bValid = bValid && checkRegexp(name, /^[A-Za-z0-9 _]*[A-Za-z0-9][A-Za-z0-9 _]*$/i,  "*Username may consist of a-z, 0-9, underscores.",showtips);
   
    var dataCentreName = $('#dialog-form').dialog("option")["datacentre"];
    var username = name.val();
    var openid = "";
    var dataCentreOpenIdProvider = OpenIDProviders[dataCentreName].openidprefix;
  
    
    openid = dataCentreOpenIdProvider+username;

    $("#composedopenididentifier").text(openid);
    if (bValid) {
      return openid;
    }
    return undefined;
  };

  $("#dialog-form")
  .dialog(
      {
        autoOpen : false,
        height : 350,
        width : 500,
        modal : true,
        buttons : {
          Cancel : function() {

            $(this).dialog("close");
          },
          "Compose identifier" : {
            text : "Sign in",
            id : 'composeidentifierbutton',
            click : function() {
              var openid=composeId(true);
              if(openid){
                $('#openid_identifier_input').val(openid);
                $('#login_button').click();
                $(this).dialog("close");
              }
            }
          }
        }

      ,

      close : function() {
        allFields.val("").removeClass("ui-state-error");
      }
});
});
var openDialog = function(datacentre) {
  
  $('#dialog-form').dialog('open');
  $('#dialog-form').dialog("option", "datacentre", datacentre);
  $("#composedopenididentifier").text(OpenIDProviders[datacentre].openidprefix);
  if(OpenIDProviders[datacentre].createaccount){
	  $("#datacentreurl").html("- <a target=\"_blank\" href=\""+OpenIDProviders[datacentre].createaccount+"\">Create an account on this data node.</a><br/>" +
	  		"- <a target=\"_blank\" href=\"/impactportal/help/howto.jsp?q=create_esgf_account\">Read why you will be directed to another website.</a>");
  }
}

var closeLoginPopupDialog = function(){
  
  var isDialogTrueOrWindowFalse = false;
  
  if(window.parent){
    isDialogTrueOrWindowFalse = true;
  }
  
  //console.log("closeLoginPopupDialog, isDialogTrueOrWindowFalse: "+isDialogTrueOrWindowFalse );
  

  var reloadWindow = function(){
    //console.log('reloadWindow: isDialogTrueOrWindowFalse'+isDialogTrueOrWindowFalse);
    var doReload = false;
    if(isDialogTrueOrWindowFalse == false){
      try{doReload = window.opener.getReloadAfterLogin();}catch(e){
        //console.log("Window: opener window has no reload function: "+e);
      }
      if(doReload == true){
        //console.log('start window');
        window.opener.location.reload(true)
      }
    }
    if(isDialogTrueOrWindowFalse == true){
      try{doReload = window.parent. getReloadAfterLogin();}catch(e){
        //console.log("Dialog: parent window has no reload function: "+e);
      }
      if(doReload == true){
        //console.log('start reload dialog');
        window.parent.location.reload(true);
      }
    }
  }
  if(isDialogTrueOrWindowFalse == true){
    window.parent.$('#loginDialog').dialog('close');
    var t = new Timer();
    //console.log('calling reload by timer');
    t.InitializeTimer(50,reloadWindow);
  }else{
    //console.log('calling reload directly');
    reloadWindow();
  }
  
};

var getUrlVar = function(key){
  var result = new RegExp(key + "=([^&]*)", "i").exec(window.location.search);
  return result && unescape(result[1]) || "";
};

var doReloadAfterLogin = false;
/*Being called by the dialog popup*/
var setReloadAfterLogin = function(reload){
  //console.log("Reload function called with value "+reload);
  if(reload == 'true'){
    doReloadAfterLogin = true;
  }
}
var getReloadAfterLogin = function(){
  if(doReloadAfterLogin == false){
    setReloadAfterLogin(getUrlVar('doreload')) ;
  }
  return doReloadAfterLogin;
}

var generateLoginDialog = function(doReload){
  
//  console.log(generateLoginDialog);
  var iframe = $('<iframe frameborder="0" marginwidth="0" marginheight="0" scrolling="0" style="overflow:auto;padding:0px;margin:0px;" ></iframe>');
  //var footer = $('<div class="logindialogfooter" ><a onclick="window.open(\'/impactportal/account/login_embed.jsp?doreload=true\',\'targetWindow\',\'toolbar=no,location=no,status=no,directories=no,titlebar=no,toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=yes,width=800,height=500\')" >Do you encounter an untrusted connection? Click here.</a></div>');
  var footer = $('<div class="logindialogfooter" ><i>Do you encounter an untrusted connection or do you have other problems? <a href="#" onclick="window.history.back();">Go back</a> or <a target="_blank" href=\'/impactportal/account/login.jsp\'>Go to the main login page.</a></i></div>');
  
 
    var loginDialog = $("<div id=\"loginDialog\" class=\"loginDialog\" ></div>").append(iframe).append(footer).appendTo("body").dialog({
        autoOpen: false,
        modal: true,
        resizable: false,
        width: "900px",
        dialogClass: 'topDialog',
        close: function () {
            iframe.attr("src", "");
        },
        hide: {
          effect: "fade",
          duration: 200
         }
    });
  
   
      var src = "/impactportal/account/login_embed.jsp";
      if(doReload === true){
        src+="?doreload=true";
      }else{
        loginDialog.bind('dialogclose', function(event) {
          if(doReload){
            doReload();
          }
        });
        
      }
      var title = "Sign in with your ESGF OpenID account";
      var width = 900;
      var height = 500;
      iframe.attr({
          width: +width,
          height: +height,
          src: src
      });
      if(loginDialog.dialog){
        loginDialog.dialog("option", "title", title).dialog("open");
      }
     

}

