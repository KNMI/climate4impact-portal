<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes.jsp" />
   
   
   
   
  </head>
  <body>
	<jsp:include page="../header.jsp" />
	 <script type="text/javascript">
		 $( document ).ready(function() {
			 $.ajax('/impactportal/oauth?makeform').done(function(data){
				 
				 var html ="";
				 for(var j=0;j<data.providers.length;j++){
				 	var provider = data.providers[j];
					html+='<div class="oauth2loginbox" onclick="document.location.href=\'/impactportal/oauth?provider='+provider.id+'&\'">';
					html+=' <a class="oauth2loginbutton" href="#"><img src="'+provider.logo+'"/> '+provider.description+'</a>';
					if(provider.registerlink){
						html+=' - <a href="'+provider.registerlink+'"><i> Register</i></a>';
					}
					html+='</div><br/>';
				 }
				 $('.oauthform').html(html);
			 });
		 });
	 </script>

	<div class="impactcontent">
		<h1>OAuth2 login</h1>
		<div class="textstandardleft">
			<div class="oauthform"></div>
		</div>
	</div>
	<!-- /Contents -->
	
	<jsp:include page="../footer.jsp" />
  </body>
</html>