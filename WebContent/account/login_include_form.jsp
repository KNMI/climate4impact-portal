<div class="c4i-login-screen">

<div onclick="openDialog('CEDA')" class="oauth2loginbox"> <a href="#" class="oauth2loginbutton"><img src="/impactportal/images/this_is_NOT_the_BADC_logo.jpg"> BADC/CEDA</a><span class="c4i_openidcompositor_registerspan"><a href="https://services.ceda.ac.uk/cedasite/register/info/" class="c4i_openidcompositor_registerlink"><i> Register</i></a></span></div>
<!-- We are working on <a href="/impactportal/account/OAuth2.jsp">OAuth2</a> support (beta).-->
<br/>You can use the same account to access all datanodes: <a href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo.</a><br/>
<br/>
<button class="c4i-login-screen-showothersbutton">Show other providers...</button>
<div class="c4i-login-screen-others">


<div class="openidform"></div>
<div class="oauthform"></div>

<div class="oauth2loginbox" onclick="c4i_openidcompositor_show()">
  <a class="oauth2loginbutton" href="#">Other OpenId ...</a>
</div><br/>

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

	out.print("<div id=\"c4i_customopenidcomposer\"><form method=\"post\" action=\"/impactportal/consumer\">"
			
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
			+ "</form><br/></br><button id=\"openidcompositor\">Compose your openid</button></div>"
	);
%>

</div>

</div>
