<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.DebugConsole,impactservice.LoginManager,impactservice.User"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes-ui.jsp" />
<style type="text/css">
fieldset {
	padding: 0;
	border: 0;
	margin-top: 25px;
}

div#users-contain {
	width: 350px;
	margin: 20px 0;
}

div#users-contain table {
	margin: 1em 0;
	border-collapse: collapse;
	width: 100%;
}

div#users-contain table td,div#users-contain table th {
	border: 1px solid #eee;
	padding: .6em 10px;
	text-align: left;
}

.ui-dialog .ui-state-error {
	padding: .3em;
}

.validateTips {
	border: 1px solid transparent;
	padding: 0.3em;
}

.datanodebutton{
	width:175px;
	
}
#datanodebuttons{
	padding:0px;
	margin:0px;
}
</style>
<script type="text/javascript" src="login.js"></script>
</head>
<body>

	


	<jsp:include page="../header.jsp" />
	<!-- Contents -->

	<%
		String Home = "/impactportal/";

		User user = null;
		try {
			user = LoginManager.getUser(request);
		} catch (Exception e) {
		}

		if (user == null) {
		
	%>
		
		
		<div id="dialog-form" title="Compose OpenID identifier">
	Please fill in your username. Your OpenID identifier will be automatically composed.
		<p class="validateTips"></p>
		<label for="name">Name:* </label> <input type="text" name="name" id="name" class="text ui-widget-content ui-corner-all" /><br/><br/>
		<br/><span id="composedopenididentifier"></span><br/><br></br>
		<span id="datacentreurl"></span>
	</div>
	
<div class="impactcontent">
	<div class="cmscontent">
	<div style="float: right;clear:both;overflow:none; border: none;">
			<a target="_blank" href="http://openid.net"><img src="../images/openid.jpg" alt="openid logo" width="250"/></a><br/>
			<a target="_blank" href="http://esgf.org"><img src="../images/esgf_network.png" alt="esgf network" width="250"/></a><br/>
			<div align="center" style="width:250px;text-align:center;"><a target="_blank" href="http://esgf.org/">The ESGF Federation</a></div>
			
		</div>

		<h1>Sign in with your ESGF OpenID account</h1>
		<div class="textstandardleft  " >

		<%
			String openid_identifier = "";
				String keep_openid_identifier = "";
				Cookie cookies[] = request.getCookies();
				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						if (cookies[i].getName().equals("openid_identifier")) {
							openid_identifier = cookies[i].getValue();
						}
						if (cookies[i].getName().equals(
								"keep_openid_identifier")) {
							keep_openid_identifier = cookies[i].getValue();
						}
					}
				}
				boolean checkKeepId = true;
				if (keep_openid_identifier.equals("false")) {
					checkKeepId = false;
				}

				out.print("<form method=\"post\" action=\"/impactportal/consumer\">"
						
						+ "<input id=\"openid_identifier_input\" type=\"text\" name=\"openid_identifier\"  class=\"openid_identifier\" value=\""
						+ openid_identifier
						+ "\"/><br/> ");
				
				
				
				//Remember me
				out.print( "<span style=\"float:right;margin-top:2px;\"><span><input type=\"checkbox\" ");
				if (checkKeepId)out.print("checked=\"checked\"");
				out.print(" name=\"keepid\" onclick=\"checkOpenIdCookie(this);\"/> Remember me </span>");

				//Sign in
				out.print("<input id=\"login_button\" type=\"submit\" name=\"login\" value=\"Sign in\" />");
				out.print("</span>");
				
				//Not registered?				
				out.print(
						"<span style=\"padding:0px;margin:0px;float:left;\"><a href=\"/impactportal/help/howto.jsp?q=create_esgf_account\"><i>Not registered?</i></a></span>"
						+ "</form>"
				//+"<br/>A PCMDI OpenID identifier is usually in the form: <i>https://pcmdi3.llnl.gov/esgcet/myopenid/<b>username</b></i><br/><br/><br/>"        

				);
		%>
		
		
		<br/><br/><br/>
		 You can compose your OpenID based on user name and data node:<br/>
			<div id="datanodebuttons"></div>

	</div>
		</div>
<!-- 
		<br /> <b>Don't have an account yet?</b>
		<ul>
			<li>Detailed instructions on how to create an account can be
				found here: <a
				href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo:
					Create an ESGF account.</a>
			</li>

		</ul>
		<br /> <strong>To get your OpenID identifier:</strong>
		<ul>
			<li>(1) Go to the <a
				href="http://pcmdi9.llnl.gov/esgf-web-fe/login" target="_blank">PCMDI</a>
				website and log in with your account
			</li>
			<li>(2) To get your PCMDI's OpenID identifier, click on
				'Account'-&gt;'Account Summary' at PCMDI</li>
			<li>(3) Copy the OpenID URL from PCMDI to this page and press
				login</li>
			
			<li>OpenID identifiers from <a
				href="http://pcmdi3.llnl.gov/esgcet/home.htm">PCMDI</a> are
				formatted like:<br />&nbsp;&nbsp;&nbsp;&nbsp;<i>https://pcmdi9.llnl.gov/esgf-idp/openid/&lt;username&gt;</i></li>

		</ul>
		<br /> <b>Other OpenID providers</b><br />
		<ul>
			<li>Please note that OpenID identifiers from other trusted
				providers are also accepted.</li>
			<li>OpenID identifiers from <a href="http://www.ceda.ac.uk/">http://www.ceda.ac.uk/</a>
				have the form:<br />&nbsp;&nbsp;&nbsp;&nbsp;<i>https://ceda.ac.uk/openid/&lt;username&gt;</i></li>
		</ul>
		<br /> <b>Why Login?</b>
		<ul>
			<li>Please read why you need to login at <a
				href="/impactportal/help/howto.jsp#why_login">Howto: Why Login?</a>.
			</li>
		</ul>

		<br /> -->
		<%
			} else {

				//Print menu structure
		%>
		<jsp:include page="loginmenu.jsp" />

			<div class="impactcontent">
			<div class="cmscontent">
			

				<h1>You are signed in.</h1>
				<div class="textstandardleft">
				You have successfully signed in with the following OpenID:<br /> <br />
				<strong><%=user.id%></strong><br /> <br /> <b>Actions:</b>
				<ul>
					<li>If you are not a member of the CMIP5 group: <a
						href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo:
							Create an ESGF account.</a><br /></li>
					<li><a href="logout.jsp">Log out</a><br /></li>
				</ul>
				</div>
			</div>
				<%
					//Print warning when retrieving SLCS has failed.
						try {
							impactservice.LoginManager.checkLogin(user.id);
						} catch (Exception e) {
							impactservice.MessagePrinters.printWarningMessage(out, e);
						}

					}
				%>
				<br />
			
			</div>
		</div>

	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
</body>
</html>