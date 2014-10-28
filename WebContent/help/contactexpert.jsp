<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="impactservice.DrupalEditor"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<jsp:include page="../includes.jsp" />
<script type="text/javascript">
  var submitted = false;
  function checkTextBox(element) {
    if (element.val().length == 0) {
      element.addClass("inputinvalid");
      return false;
    } else {
      element.removeClass("inputinvalid");
      return true;
    }
  }
  function checkEmail(email) {
    var filter = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    if (!filter.test(email.val())) {
      email.addClass("inputinvalid");

      return false;
    } else {
      email.removeClass("inputinvalid");
    }

    return true;
  };

  function checkEmailInput() {
    if (checkEmail($("#contact_email")) == false) {
      $("#contact_email_message").text('*Please provide a valid email address');
      return false;
    }else{
      $("#contact_email_message").text("");
    }
    return true;
  };

  function checkNameInput() {
    if (checkTextBox($("#contact_name")) == false) {
      $("#contact_name_message").text("*Please provide your name");
      return false;
    }else{
      $("#contact_name_message").text("");
    }
    return true;
  };
  
  function checkCommentsInput() {
    if (checkTextBox($("#comments")) == false) {
      $("#comments_message").text("*Please provide some information...");
      return false;
    }else{
      $("#comments_message").text("");
    }
    return true;
  }
  
  function checkform() {
    submitted = true;
    var dataIsOK = true;
    if(checkEmailInput()==false)dataIsOK = false;
    if(checkCommentsInput()==false)dataIsOK = false;
    return dataIsOK;
  };
  $(document).ready(function() {
    
    $("#comments").on('input',function() {
      if(submitted == false)return;
      checkCommentsInput();
    });
    $("#contact_email").on('input',function() {
      if(submitted == false)return;
      checkEmailInput();
    });

    //checkform();
  });
</script>

</head>
<body>
	<jsp:include page="../header.jsp" />
	<!-- Contents -->
	<jsp:include page="helpmenu.jsp" />
	<div class="impactcontent">
		<div class="cmscontent">
		
			<%
				try {
					out.print(DrupalEditor.showDrupalContent("?q=contactexpert",
							request,response));
				} catch (DrupalEditor.DrupalEditorException e) {
					out.print(e.getMessage());
					response.setStatus(e.getCode());
				}
			%>

	<div class="textstandardleft">
			<form name="htmlform" method="post" action="sendfeedback.jsp"
				onsubmit="return checkform()">
				<table width="100%">
					

					<tr>
						<td class='alnright'><label for="email">Email: *</label></td>
						<td valign="top"><input id="contact_email" type="text"
							name="email" maxlength="80" size="30" /> <span
							id="contact_email_message"></span></td>

					</tr>
					
					<tr>
						<td class='alnright'><label for="name">Name:</label></td>
						<td valign="top"><input id="contact_name" type="text"
							name="name" maxlength="50" size="30" /> <span
							id="contact_name_message"></span></td>
					</tr>
					
					<tr>
						<td>&nbsp;</td>
						<td></td>
					</tr>
					<tr>
						<td class='alnright'><label for="comments">Comment:* </label></td>
						<td valign="top"><textarea id="comments" name="comments" cols="70"
								rows="12"></textarea><br/><span
							id="comments_message"></span></td>

					</tr>
					<tr>
						<td colspan="2" style="text-align: right"><input
							type="submit" value="Submit" /></td>
					</tr>
				</table>
			</form>
			</div>
		</div>

	</div>

	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
</body>
</html>