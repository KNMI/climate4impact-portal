<!-- Footer -->
<%="</div></div></div>"%>
<%@page import="tools.HTTPTools" import="impactservice.*"%>
<%
ImpactUser user = null;
try{
	user = LoginManager.getUser(request);
}catch(Exception e){
	
}
%>
<div class="impactfooter"><span></span> </div> 

  <div class="bottomnav">
    <p>
    &#160;
    <%
    if(user!=null){
    	out.print("You are logged in as "+user.id);
    }
    %>
    </p>
  </div> 
<%="</div>"%>