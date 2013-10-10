<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"  import="impactservice.ImpactService,tools.DebugConsole" import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes-ui.jsp" />
  <style>



fieldset { padding:0; border:0; margin-top:25px; }

div#users-contain { width: 350px; margin: 20px 0; }
div#users-contain table { margin: 1em 0; border-collapse: collapse; width: 100%; }
div#users-contain table td, div#users-contain table th { border: 1px solid #eee; padding: .6em 10px; text-align: left; }
.ui-dialog .ui-state-error { padding: .3em; }
.validateTips { border: 1px solid transparent; padding: 0.3em; }
</style>
 	<script type="text/javascript">
 	var checkOpenIdCookie = function(a){
 		if(a.checked==false){
 			makeHTTPRequest("/impactportal/consumer?keepid=off");
 		}else{
 			makeHTTPRequest("/impactportal/consumer?keepid=on");
 		}
 		
 	}
 	 $(function() {
 		 var name = $( "#name" ),
 		
 		allFields = $( [] ).add( name ),
 		tips = $( ".validateTips" );
 		name.keyup(function (e) {
 		    if (e.keyCode == 13) {
 		   	$( '#composeidentifierbutton' ).click();
 		    }
 		});
 		 function updateTips( t ) {
 			tips
 			.text( t )
 			.addClass( "ui-state-highlight" );
 			setTimeout(function() {
 			tips.removeClass( "ui-state-highlight", 1500 );
 			}, 500 );
 			}
 			function checkLength( o, n, min, max ) {
 			if ( o.val().length > max || o.val().length < min ) {
 			o.addClass( "ui-state-error" );
 			updateTips( "Length of " + n + " must be between " +
 			min + " and " + max + "." );
 			return false;
 			} else {
 			return true;
 			}
 			}
 			function checkRegexp( o, regexp, n ) {
 			if ( !( regexp.test( o.val() ) ) ) {
 			o.addClass( "ui-state-error" );
 			updateTips( n );
 			return false;
 			} else {
 			return true;
 			}
 			}
 			
 	 $( "#dialog-form" ).dialog({
 		autoOpen: false,
 		height: 200,
 		width: 400,
 		modal: true,
 		buttons: {
 		  "Compose identifier": {
 		    text : "Compose identifier",
 			id:'composeidentifierbutton',
 			click:function() {
 		      var bValid = true;
 		      bValid = bValid && checkLength( name, "username", 3, 16 );
 		      bValid = bValid && checkRegexp( name, /^[a-z]([0-9a-z_.])+$/i, "Username may consist of a-z, 0-9, underscores, begin with a letter." );
 		      if ( bValid ) {
 			    var dataCentreName = $( this ).dialog( "option" )["datacentre"];
 			    var username = name.val();
 			    var openid = "";
 			    if(dataCentreName == 'PCMDI'){openid = "https://pcmdi9.llnl.gov/esgf-idp/openid/"+username;}
 			    if(dataCentreName == 'CEDA'){openid = "https://ceda.ac.uk/openid/"+username;}
 			    if(dataCentreName == 'DKRZ'){openid = "https://esgf-data.dkrz.de/esgf-idp/openid/"+username;}
 			  
 			   
 			    $('#openid_identifier_input').val(openid);
 			    $('#login_button').click();
 			   
 			    $( this ).dialog( "close" );
 		      }
 		      return false;
  		    }
 		},
 		Cancel: function() {
 		
 		$( this ).dialog( "close" );
 		}
 		
 		},
 		
 		close: function() {
 		allFields.val( "" ).removeClass( "ui-state-error" );
 		}
 		});
 	 });
 	 var openDialog = function(datacentre){
 		$( '#dialog-form' ).dialog( "option", "datacentre", datacentre );
 		$( '#dialog-form' ).dialog( 'open' );
 		//alert('blah'+datacentre);
 		
 	 }
 	
 	</script>
  </head>
  <body>
 
<div id="dialog-form" title="Compose OpenID identifier">
<p class="validateTips">After completion you OpenID identifier is put in the login box.</p>


<label for="name">Name</label>
<input type="text" name="name" id="name" class="text ui-widget-content ui-corner-all" />



</div>


		<jsp:include page="../header.jsp" />
		<!-- Contents -->
		
		<%
		
		String Home="/impactportal/";
	 	
		
	    if (session.getAttribute("openid_identifier")==null){ 
	    	   %>
	    	   <div class="impactcontent"><div class="cmscontent">
	    	   	<div style="float:right;border:none;"><img src=../images/openid.jpg alt="openidlogo" width="300"></img></div>
	
	           <h1>Login with your OpenID account</h1>
           <br/>
	           
          <%
          String openid_identifier= "";
          String keep_openid_identifier= "";
          Cookie cookies [] = request.getCookies ();
          if(cookies!=null){
	          for (int i = 0; i < cookies.length; i++){
	        	  if (cookies [i].getName().equals ("openid_identifier")){
	        		      openid_identifier=cookies [i].getValue();
	        		  }
	        	  if (cookies [i].getName().equals ("keep_openid_identifier")){
        		      keep_openid_identifier=cookies [i].getValue();
        		  }
	          }
          }
          boolean checkKeepId = true;
          if(keep_openid_identifier.equals("false")){
        	  checkKeepId=false;
          }
          
	      out.print(
	    		  "<form method=\"POST\" action=\"/impactportal/consumer\">"
	                              +"<strong>OpenID account:</strong>"
	                              +"<ul><li><input id=\"openid_identifier_input\" type=\"text\" name=\"openid_identifier\"  class=\"openid_identifier\" size=\"50\" value=\""+openid_identifier+"\"/>"
	                              +" <input id=\"login_button\" type=\"submit\" name=\"login\" value=\"Login\" /></li>"
	                           	  +" <li><input type=\"checkbox\" ");
	      
	      if(checkKeepId)out.print("checked");
	      
          out.print(" name=\"keepid\" onclick=\"checkOpenIdCookie(this);\"/> Keep identifier on this computer</li></ul>"
	                              +"</form>"
	                              //+"<br/>A PCMDI OpenID identifier is usually in the form: <i>https://pcmdi3.llnl.gov/esgcet/myopenid/<b>username</b></i><br/><br/><br/>"        
	                           	 
	    		  );
	      %>
	      <br/>
	      <b>Compose OpenID based on username and corresponding ENES data portal</b><br/>
	      <ul>
	      <li onclick="openDialog('PCMDI');" style="cursor:pointer;">PCMDI - https://pcmdi9.llnl.gov/esgf-idp/openid/&lt;username&gt;</li>
	      <li onclick="openDialog('CEDA');" style="cursor:pointer;">CEDA - https://ceda.ac.uk/openid/&lt;username&gt;</li>
	      <li onclick="openDialog('DKRZ');" style="cursor:pointer;">DKRZ - https://esgf-data.dkrz.de/esgf-idp/openid/&lt;username&gt;</li>
	    
	      </ul>
	      
	      <br/>
	      <b>Don't have an account yet?</b>
           <ul>
           <li>Detailed instructions on how to create an account can be found here: <a href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo: Create an ESGF account.</a></li>
           
           </ul>
           <br/>
            <strong>To get your OpenID identifier:</strong>
            <ul>
            <li>(1) Go to the  <a href="http://pcmdi9.llnl.gov/esgf-web-fe/login" target="_blank">PCMDI</a> website and log in with your account</li> 
            <li>(2) To get your PCMDI's OpenID identifier, click on 'Account'-&gt;'Account Summary' at PCMDI</li>
            <li>(3) Copy the OpenID URL from PCMDI to this page and press login</li>
            <!-- <li>OpenID identifiers from  <a href="http://pcmdi3.llnl.gov/esgcet/home.htm">PCMDI</a> are formatted like:<br/>&nbsp;&nbsp;&nbsp;&nbsp;<i>https://pcmdi3.llnl.gov/esgcet/myopenid/&lt;username&gt;</i></li> -->
                        <li>OpenID identifiers from  <a href="http://pcmdi3.llnl.gov/esgcet/home.htm">PCMDI</a> are formatted like:<br/>&nbsp;&nbsp;&nbsp;&nbsp;<i>https://pcmdi9.llnl.gov/esgf-idp/openid/&lt;username&gt;</i></li>

            </ul> <br/>
           
           <b>Other OpenID providers</b><br/>
            <ul>
            <li>Please note that OpenID identifiers from other trusted providers are also accepted.</li>
            <li>OpenID identifiers from  <a href="http://www.ceda.ac.uk/">http://www.ceda.ac.uk/</a> have the form:<br/>&nbsp;&nbsp;&nbsp;&nbsp;<i>https://ceda.ac.uk/openid/&lt;username&gt;</i></li>
            </ul><br/>
           
           
          
	       <b>Why Login?</b><ul><li> Please read why you need to login at <a href="/impactportal/help/howto.jsp#why_login">Howto: Why Login?</a>.</li></ul>
  
	          <br/>
	        <% 
			}else{ 
			
				
				//Print menu structure
				%>
				<jsp:include page="loginmenu.jsp" />
				<%
				out.println("<div class=\"impactcontent\"><div class=\"cmscontent\">");
			  	%>
			  	
			  	<h1>You are logged in</h1>You have successfully logged in with the following OpenID:<br/><br/><strong><%=session.getAttribute("openid_identifier") %></strong><br/><br/>
			  	<b>Actions:</b>
			 	<ul>
			  	<li>If you are not a member of the CMIP5 group: <a href="/impactportal/help/howto.jsp?q=create_esgf_account">HowTo: Create an ESGF account.</a><br/></li>
			  	<li><a href="logout.jsp">Log out</a><br/></li>
			  	</ul>
			
			  	<%
			   	    //Print warning when retrieving SLCS has failed.
				try{
				 	impactservice.LoginManager.checkLogin((String) session.getAttribute("openid_identifier"));
				 }catch(Exception e){
					impactservice.MessagePrinters.printWarningMessage(out, e);
				}
			  	
			
			}
		
    %>
    <br/>
   
    </div>
	  </div>
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>