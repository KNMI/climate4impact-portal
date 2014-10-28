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
	);
%>

<br />
<br />
<br /> If you are already registered, you can compose your OpenID based on your user name and corresponding index node:<br />
<div id="datanodebuttons"></div>
* You can use the same account to access all datanodes: <a href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo.</a>