  var c4i_user=false;
  var openid = undefined;
  var OpenIDProviders = [
      
      {
        id:'CEDA',
      	name:'BADC/CEDA'   ,
      	openidprefix:'https://ceda.ac.uk/openid/',
      	createaccount:'https://services.ceda.ac.uk/cedasite/register/info/',        
      	accountinfo:'https://services.ceda.ac.uk/cedasite/register/info/',
      	logocls:'logo_UK',
      	logo:'/impactportal/images/this_is_NOT_the_BADC_logo.jpg',
      	needsusername:false
      },
      {
          id:'SMHI-NSC-LIU',
          name:'SMHI-NSC-LIU',
          openidprefix:'https://esg-dn1.nsc.liu.se/esgf-idp/openid/',
          createaccount:'https://esg-dn1.nsc.liu.se/user/add/?next=https://esg-dn1.nsc.liu.se/projects/esgf-liu/',
          accountinfo:'http://esg-dn1.nsc.liu.se/esgf-web-fe/accountsview',
          logocls:'logo_Sweden',
          logo:'/impactportal/images/nsclogo.png',
          needsusername:true
  	  },{
  	    id:'IPSL',
        name:'IPSL',
        openidprefix:'https://esgf-node.ipsl.upmc.fr/esgf-idp/openid/',
        createaccount:'https://esgf-node.ipsl.upmc.fr/user/add/?next=https://esgf-node.ipsl.upmc.fr/projects/esgf-ipsl/',
        accountinfo:'https://esgf-node.ipsl.upmc.fr/esgf-web-fe/accountsview',
        logocls:'logo_France',
        logo:'/impactportal/images/countries/France.png',
        needsusername:true
  	  }]
      /*'PCMDI':       {
    	  name:'PCMDI'       ,
    	  openidprefix:'https://pcmdi9.llnl.gov/esgf-idp/openid/',
    	  createaccount:'https://pcmdi9.llnl.gov/user/add/?next=https://pcmdi9.llnl.gov/projects/cmip5/',
    	  accountinfo:'http://pcmdi9.llnl.gov/esgf-web-fe/accountsview',
    	  logocls:'logo_USA'},
  	  'IPSL':       {
        name:'IPSL'       ,
        openidprefix:'https://esgf-node.ipsl.fr/esgf-idp/openid/',
        createaccount:'https://esgf-node.ipsl.fr/esgf-web-fe/createAccount',
        accountinfo:'http://esgf-node.ipsl.fr/esgf-web-fe/accountsview',
        logocls:'logo_France'},
      
      'DKRZ':        {
    	  name:'DKRZ'        ,
    	  openidprefix:'https://esgf-data.dkrz.de/esgf-idp/openid/',
    	  createaccount:'https://esgf-data.dkrz.de/esgf-web-fe/createAccount',
    	  accountinfo:'https://esgf-data.dkrz.de/esgf-web-fe/accountsview',
    	  logocls:'logo_Germany'},*/
	  /*'NCI':        {
    	  name:'NCI'        ,
    	  openidprefix:'https://esg2.nci.org.au/esgf-idp/openid/' ,
    	  createaccount:'https://esg2.nci.org.au/esgf-web-fe/createAccount',
    	  logocls:'logo_Germany'},*/
     /* 'PIK':         {
    	  name:'PIK Potsdam' ,
    	  openidprefix:'https://esg.pik-potsdam.de/esgf-idp/openid/',
    	  createaccount:'https://esg.pik-potsdam.de/esgf-web-fe/createAccount',
    	  accountinfo:'https://esg.pik-potsdam.de/esgf-web-fe/accountsview',
    	  logocls:'logo_Germany'}*/
      
 //};


  var getIDProviderFromId = function(id){
    for(var j=0;j<OpenIDProviders.length;j++){
      var provider = OpenIDProviders[j];
      if(provider.id==id){
        return provider;
      }
    }
  }
  var getOpenIDProviderFromOpenId = function(openidIdentifier){
    for(var j=0;j<OpenIDProviders.length;j++){
      var provider = OpenIDProviders[j];
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

var oauthClick = function(providerID){
  $(".c4i-login-screen").hide().parent().append("Redirecting to identity provider...");
  var currentRedir=getUrlVar("c4i_redir");
  if(!currentRedir){
    currentRedir="";
  }
  document.location.href='/impactportal/oauth?provider='+providerID+'&c4i_redir='+URLEncode(currentRedir);
};

$( document ).ready(function() {
  $(".c4i-login-screen-others").hide();
  $(".c4i-login-screen-showothersbutton").button().click(function(){
    $(".c4i-login-screen-showothersbutton").hide();
    $(".c4i-login-screen-others").show();
  });

	 $.ajax('/impactportal/oauth?makeform').done(function(data){
	
		 var html ="";
		 for(var j=0;j<data.providers.length;j++){
		 	var provider = data.providers[j];
			html+='<div class="oauth2loginbox" onclick="oauthClick(\''+provider.id+'\');">';
			html+=' <a class="oauth2loginbutton" href="#"><img src="'+provider.logo+'"/> '+provider.description+'</a>';
			if(provider.registerlink){
				html+='<span class="c4i_openidcompositor_registerspan"><a class="c4i_openidcompositor_registerlink" href="'+provider.registerlink+'"><i> Register</i></a></span>';
			}
			html+='</div><br/>';
		 }
		 html+="";
		 $('.oauthform').html(html);

	 });
	 
	var html="";
	
	for(var i = 1; i < OpenIDProviders.length; i++){
	  var provider = OpenIDProviders[i];
		html+='<div class="oauth2loginbox" onclick="openDialog(\''+provider.id+'\')">';
		html+=' <a class="oauth2loginbutton" href="#"><img src="'+provider.logo+'"/> '+provider.name+'</a>';
		if(provider.createaccount){
			html+='<span class="c4i_openidcompositor_registerspan"><a class="c4i_openidcompositor_registerlink" href="'+provider.createaccount+'"><i> Register</i></a></span>';
		}
		html+='</div><br/>';
	}
	$(".openidform").html(html);

  $("#openidcompositor").button().click(function(){openDialog();});
  $('#login_button').button().click(function(){$(".c4i-login-screen").hide().parent().append("Redirecting to identity provider...");});
});

$(function() {

  
  
  var name = $("#c4i_openidcompositor_inputname"),  allFields = $([]).add(name), tips = $(".validateTips");
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
    //bValid = bValid && checkLength(name, "username", 0, 50,showtips);
    if(name.val().length>0){
    	bValid = bValid && checkRegexp(name, /^[A-Za-z0-9 _]*[A-Za-z0-9][A-Za-z0-9 _.@]*$/i,  "*Username may consist of a-z, 0-9, underscores, '@' and '.' .",showtips);
    }
   
    var dataCentreName = $('#dialog-form').dialog("option")["datacentre"];
    var username = name.val();
    var openid = "";
    
    if(!dataCentreName)return undefined;
    var dataCentreOpenIdProvider = getIDProviderFromId(dataCentreName).openidprefix;
  
    
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
                $("#login_button").hide();
                $("#openid_identifier_input").prop('disabled',true);
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

var dataNodeButtonClicked = function(datacentre){
     var provider=getIDProviderFromId(datacentre);
	  if(provider.needsusername==false){
		  $('#openid_identifier_input').val(provider.openidprefix);
		  $('#login_button').click();
		  $("#login_button").hide();
          $("#openid_identifier_input").prop('disabled',true);
          $('#dialog-form').dialog("close");
        
		  return;
	  }
	  $('#dialog-form').dialog("option", "datacentre", datacentre);
	  $("#composedopenididentifier").text(provider.openidprefix);
	  if(provider.createaccount){
		  $("#datacentreurl").html("- <a target=\"_blank\" href=\""+provider.createaccount+"\">Create an account on this data node.</a><br/>" +
		  		"- <a target=\"_blank\" href=\"/impactportal/help/howto.jsp?q=create_esgf_account\">Read why you will be directed to another website.</a>");
	  }
	  $(".c4i_openidcompositor_entername").show();
	  $(".c4i_openidcompositor_chooseprovider").hide();
	  $("#composeidentifierbutton").show();
	  $("#c4i_openidcompositor_inputname").focus();
};

var openDialog = function(datacentre) {
  
  
  if(datacentre){
	  $('#dialog-form').dialog('open');
	  dataNodeButtonClicked(datacentre);
  }else{
	  $(".c4i_openidcompositor_entername").hide();
	  $("#composeidentifierbutton").hide();
	  $(".c4i_openidcompositor_chooseprovider").show();
	  var html='<ul>';
	  for(var j=0;j<OpenIDProviders.length;j++){
	    var provider = OpenIDProviders[j];
	    html+='<li><button id="dnb_'+provider.id+'" class="datanodebutton" onclick="dataNodeButtonClicked(\''+provider.id+'\');">'+provider.name+'</button> <i>('+provider.openidprefix+')<i></li>';
	    //alert(id.name);
	  }
	  html+='</ul>';
	  $("#datanodebuttons").html(html);
	  for(var j=0;j<OpenIDProviders.length;j++){
	    var provider = OpenIDProviders[j];
	    $("#dnb_"+provider.id).button({icons: {primary: provider.logocls},text:true});
	    
	  }
	  $('#dialog-form').dialog('open');
  }

  

};

/**
 * Triggered when a user has signed in or has signed out
 */
var userHasSignedInOrOut = function(){
  console.log("userHasSignedInOrOut")
  console.log("c4i_user"+c4i_user);
  console.log("openid"+openid);
  if(c4i_user==true){
    console.log($(".c4i-mainmenu-login-tab"));
    $(".c4i-mainmenu-login-tab").html("<a href=\"/impactportal/account/login.jsp\">Account&nbsp;<code class=\"codeusersicon\"></code></a>");
    $(".c4i-mainmenu-login-header").html(
        '| <a href="/impactportal/account/login.jsp">Account</a>&nbsp;<a href="/impactportal/account/basket.jsp"><code class="codeshoppingcarticon"></code></a>&nbsp;<a href="/impactportal/account/processing.jsp"><code class="codejobsicon"></code></a>'
        
        
    );
    console.log( "Done ADjusting login fields");
  }
  
}

/**
 * Triggerd when tne login dialog is closed
 */
var closeLoginPopupDialog = function(){
  c4i_user=true;
  console.log("closeLoginPopupDialog")
  var isDialogTrueOrWindowFalse = false;
  
  if(window.parent){
    isDialogTrueOrWindowFalse = true;
  }
  
  console.log("closeLoginPopupDialog, isDialogTrueOrWindowFalse: "+isDialogTrueOrWindowFalse );
  

  var reloadWindow = function(){
    
    console.log('reloadWindow: isDialogTrueOrWindowFalse'+isDialogTrueOrWindowFalse);
    var doReload = false;
    if(isDialogTrueOrWindowFalse == false){
      try{doReload = window.opener.getReloadAfterLogin();}catch(e){
        console.log("Window: opener window has no reload function: "+e);
      }
      if(doReload == true){
        console.log('start window');
        window.opener.location.reload(true)
      }
    }
    if(isDialogTrueOrWindowFalse == true){
      try{doReload = window.parent.getReloadAfterLogin();}catch(e){
        console.log("Dialog: parent window has no reload function: "+e);
      }
      if(doReload == true){
        console.log('start reload dialog');
        window.parent.location.reload(true);
      }
    }
  };
  
  if(isDialogTrueOrWindowFalse == true){
    window.parent.$('.loginDialog').dialog('close');
    var t = new Timer();
    console.log('calling reload by timer');
    t.InitializeTimer(50,reloadWindow);
  }else{
    console.log('calling reload directly');
    reloadWindow();
  }
  userHasSignedInOrOut();
};

/*
 *  return the parameter for a certain key 
 */
var getUrlVar = function(key){
//	console.log("getURLVar"+key)
  var result = new RegExp(key + "=([^&]*)", "i").exec(window.location.search);
  return result && unescape(result[1]) || "";
};

var doReloadAfterLogin = false;
/*Being called by the dialog popup*/
var setReloadAfterLogin = function(reload){
  c4i_user=true;
  //TODO AJAX call to fill in openID
  console.log("setReloadAfterLogin:"+c4i_user);
  console.log("Reload function called by popup with value "+reload);
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

/**
 * Generated a popup login dialog
 * @param doReload Can be true, the page will reload. When a function is provided, this will be triggered when done.
 */
var generateLoginDialogNewPage = function(doReload){
  var src = "/impactportal/account/login_embed.jsp";
  src+="?c4i_redir="+URLEncode(window.location.href) ;
  window.open(src);
};


/**
 * Generated a popup login dialog
 * @param doReload Can be true, the page will reload. When a function is provided, this will be triggered when done.
 */
var generateLoginDialog = function(doReload){
	c4i_user=false;
	console.log("generateLoginDialog:"+c4i_user);
	$.ajax('/impactportal/account/logout.jsp').done(function(data){
		_generateLoginDialog(doReload);
	});
};


var c4i_logindialog_dialog;


var c4i_logindialog_dialog_reload = function(){
	c4i_user=true;
	console.log("c4i_logindialog_dialog_reload:"+c4i_user);
	c4i_logindialog_dialog.dialog('close');
}

var c4i_login_dialog_footer = $('<div class="logindialogfooter" ><i>Do you encounter an untrusted connection or do you have other problems? <a href="#" onclick="window.history.back();">Go back</a> or <a target="_blank" href=\'/impactportal/account/login.jsp\'>Go to the main login page.</a></i></div>');

function c4i_checkiframe(fr) {
	try{
	  if (!fr.contentDocument.location) throw(1);
	}catch(e){
		
		c4i_logindialog_dialog.dialog().html(
				"<div style=\"height:300px;\"><h1>Sorry, an error occured</h1>"+
				"<br/><br/><br/>The identity provider does not allow to show the login dialog in this page."+
				"<br/><br/><br/>Please click here to <a target=\"_blank\" href=\"/impactportal/account/login.jsp\">go to the login page</a>. "+
				"<br/><br/><br/>You can click <a href=\"#\" onclick=\"javascript:c4i_logindialog_dialog_reload()\">continue</a> after logging in and closing that window.</div>"
				).append(c4i_login_dialog_footer);
	}
};
	

var _generateLoginDialog = function(doReload){



//  console.log(generateLoginDialog);
  var iframe = $('<iframe class="c4-login-popupdialog"  frameborder="0" marginwidth="0" marginheight="0"  scrolling="no"  ></iframe>');
  //var footer = $('<div class="logindialogfooter" ><a onclick="window.open(\'/impactportal/account/login_embed.jsp?doreload=true\',\'targetWindow\',\'toolbar=no,location=no,status=no,directories=no,titlebar=no,toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=yes,width=800,height=500\')" >Do you encounter an untrusted connection? Click here.</a></div>');
 
  
	 
  c4i_logindialog_dialog = $("<div style=\"overflow:hidden;\" id=\"loginDialog\" class=\"loginDialog\" ></div>").append(iframe).append(c4i_login_dialog_footer).appendTo("body").dialog({
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
	  
		
  var src = "/impactportal/account/login_embed.jsp?";
  //src+="&c4i_redir="+URLEncode(window.location.href) ;
	  if(doReload === true){
	    src+="&doreload=true";
	  }
	  c4i_logindialog_dialog.bind('dialogclose', function(event) {
	  userHasSignedInOrOut();
		if(c4i_user===true){ //Set in login_embed.jsp
			console.log("Found user info");
	    if (typeof doReload === "function") {
	    	console.log("calling function");
	    	
	    		doReload();
	    	
	    }else if(doReload === true){
	    	console.log("reload page");
	    	location.reload();
	    }
	    }else if(c4i_user===false){
	    	console.log("No user info set.");
	    }else{
	    	console.log("User info undefined.");
	    }
    });
	    
      
      var title = "Sign in with your ESGF OpenID account";
      var width = 900;
      var height = 500;
      iframe.attr({
          width: +width,
          height: +height,
          src: src
      });
      if(c4i_logindialog_dialog.dialog){
    	  c4i_logindialog_dialog.dialog("option", "title", title).dialog("open");
      }
     

};




var c4i_openidcompositor_isshowed =false;
var c4i_openidcompositor_show = function(){
	if(c4i_openidcompositor_isshowed == false){
		$("#c4i_customopenidcomposer").show();
		c4i_openidcompositor_isshowed = true;
	}else{
		$("#c4i_customopenidcomposer").hide();
		c4i_openidcompositor_isshowed = false;
	}
};