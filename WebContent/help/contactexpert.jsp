<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.DrupalEditor"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes.jsp" />
   <script type="text/javascript">
   
   	function checkTextBox(element){
	   	if(element.value.length<2){
	   		element.style.border="2px solid red";
	   		return false;
	   	}else{
	   		element.style.border="";
	   		return true;
	   	}
   	}
   	function checkEmail(email) {
		var filter  = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
		if (!filter.test(email.value)) {
			email.style.border="2px solid blue";
			email.focus;
			alert('Please provide a valid email address');
			return false;
		}
		return true;
	};

	function checkform(){
		var dataIsOK=true;
		if(checkTextBox(document.getElementById("contact_name"))==false)dataIsOK=false;
		if(checkTextBox(document.getElementById("contact_email"))==false)dataIsOK=false;
		if(dataIsOK){
			if(checkEmail(document.getElementById("contact_email"))==false)dataIsOK=false;
		}
	   	return dataIsOK;
	};
	
   </script>
   
  </head> 
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->
		<jsp:include page="helpmenu.jsp" />
		<div class="impactcontent">
		 <div class="cmscontent">
    <%try{out.print(DrupalEditor.showDrupalContent("?q=contactexpert",request));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%>                  			
  			</div>
  			<br/><br/>
  			<form name="htmlform" method="post" action="sendfeedback.jsp" onsubmit="return checkform()">
				<table width="900px">
					<tr> 
					 <td valign="top">
					  <label for="name">Name: </label>
					 </td>
					 <td valign="top">
					  <input  id="contact_name" type="text" name="name" maxlength="50" size="30"/>
					 </td>
					</tr>
					 
					<tr>
					 <td valign="top">
					  <label for="email">Email Address: </label>
					 </td>
					 <td valign="top">
					  <input  id="contact_email" type="text" name="email" maxlength="80" size="30"/>
					 </td>
					 
					</tr>
					<tr><td>&nbsp;</td><td></td></tr>
					<tr>
					 <td valign="top">
					  <label for="comments">Comments: </label> 
					 </td>
					 <td valign="top">
					  <textarea  name="comments"  cols="90" rows="20"></textarea>
					 </td>
					 
					</tr>
					<tr>
					 <td colspan="2" style="text-align:center">
					  <input type="submit" value="Submit"/> 
					 </td>
					</tr>
				</table>
			</form> 
		</div>
		
		
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>