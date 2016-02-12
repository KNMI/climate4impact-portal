<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.Debug,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>



<jsp:include page="../includes-ui.jsp" />
<link rel="stylesheet" href="/impactportal/account/login.css"
	type="text/css" />
<style type="text/css">
body,.x-body {
	background: white;
	height: auto;
	border: none;
	overflow: scroll;

}
</style>
<script type="text/javascript" src="/impactportal/account/js/login.js"></script>
 <!-- <script type="text/javascript">
	//Set redir parameter in login.js, for parent window (this is the Iframe popup)
	//If set to true the parent frame will  reload when the iframe is finished logging in.
  try {
    opener.setReloadAfterLogin(getUrlVar('c4i_redir') + '');
  } catch (e) {
  }
  try {
    window.parent.setReloadAfterLogin(getUrlVar('c4i_redir') + '');
  } catch (e) {
  }
</script>-->
</head>
<body>
<div >
	<jsp:include page="login_include_openidcomposition.jsp" />
	<!-- Contents -->
	<%
		String Home = "/impactportal/";
		ImpactUser user = null;
		try {
			user = LoginManager.getUser(request);
		} catch (Exception e) {
		}
		

		if (user == null) {
	%>
	<script type="text/javascript">
	 c4i_user=false;
	 console.log("Setting user info to false");
	</script>
	<div class="impactcontent">
		<div class="cmscontent">
		
			<jsp:include page="login_include_form.jsp" />
		</div>
	</div>
	<%
		}
		if (user != null) {
	%>
	<div style="text-align: center">
		<h1>You are signed in.</h1>
		You have successfully signed in with the following OpenID:<br /> <br />
		<strong><%=user.getOpenId()%></strong><br /> <br />
		<div id="c4i_info"></div>
		<h1>This window will now close.</h1>
	</div>

	<script type="text/javascript">
	c4i_user=true;
	console.log("Setting user info to true");
    var t = new Timer();
    console.log("starting closeLoginPopupDialog");
    var c4i_redir=getUrlVar('c4i_redir')
    if(c4i_redir.length==0){
    	console.log('c4i_redir not found tryung returnurl');
    	var returnurl=getUrlVar('returnurl')
    	c4i_redir="/impactportal/account/processing.jsp";
    	if(returnurl.length!=0){
    		var result = new RegExp('c4i_redir' + "=([^&]*)", "i").exec(returnurl);
    		c4i_redir = result && unescape(result[1]) || "";
    	}
    }
    $("#c4i_info").html("Redirecting to "+c4i_redir);
    t.InitializeTimer(250, function(){
    	console.log("triggerd closeLoginPopupDialog");
    	closeLoginPopupDialog();
    	//window.location.replace(c4i_redir);
    });
    </script>
  

	<%
		//Print warning when retrieving SLCS has failed.
			try {
				impactservice.LoginManager.checkLogin(user.getId(),request);
			} catch (Exception e) {
				impactservice.MessagePrinters.printWarningMessage(out, e);
			}
		}
	%>
</div>
</body>
</html>