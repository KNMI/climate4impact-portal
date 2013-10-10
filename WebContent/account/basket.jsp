<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes.jsp" />
     
    <script type="text/javascript">
    var impactBase = '/impactportal/';
    var impactService=impactBase+'ImpactService?';
  
    var removeId = function(id){
    	var doneFunction = function(json){
    		 if(json.error){
   		    	alert(json.error);
    		    }else{
    				adjustNumberOfDataSetsDisplayedInMenuBar(json);
    		    }
     		populateBasketList();
    	}
    	
    	$.ajax({
    		type: "GET",
    		url: impactService,
    		data: { service:'basket',request:'removeFromList',id:id }  ,
    		success:doneFunction,
    		dataType: 'json'
    		});
    	
    
    }
    
    var populateBasketList = function(){
   	
   		var passFn = function(data){
    		$('#basketlist').html(data);
   		}
   		$.ajax({
    		type: "GET",
    		url: impactService,
    		data: { service:'basket',request:'getoverview' }  ,
    		success:passFn,
    		dataType: 'html'
    		});
   		/*var failFn = function(msg){
   			var data = {};
   			data.responseText='Unable to retrieve basket from server';
   			passFn(data);
   		}*/
   	 	
    }
    
    $(document).ready(populateBasketList)

    </script>
  </head>
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->
		<jsp:include page="../account/loginmenu.jsp" />
		<div class="impactcontent">
		<h1>Basket</h1>
		<%
		
	
		
		
		 if (session.getAttribute("openid_identifier")==null){ 
			%>
			<p>You are not logged in, please go to the <a href="../login.jsp">login page</a> and log in</p>
			<%
		}else{
			out.println("<div id='basketlist'/>");
		}
		%>
		</div>
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>