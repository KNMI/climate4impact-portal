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
            	out.print("You are logged in as "+user.getUserName());
            }
    %>
    </p>
  </div> 
  <div class="footerlogos">
    <table class="footerlogos">
    <tr>
    <td><a href="http://europa.eu/index_en.htm" target="_blank">
    <img src="/impactportal/images/EUemblem_small.png" title="European Commission" class="ecLogo" alt="European Commission"></a></td>
    <td><p>The <a href="http://cordis.europa.eu/projects/index.cfm?fuseaction=app.details&TXT=312979&FRM=1&STP=10&SIC=&PGA=&CCY=&PCY=&SRC=&LNG=en&REF=108647">IS-ENES project</a> has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration.
    <br/><br/><a href="/impactportal/general/index.jsp?q=disclaimer">Disclaimer</a>
    </p>
    </td>
    </tr>
    
    </table>
        
        
        
    
  </div>
<%="</div>"%>