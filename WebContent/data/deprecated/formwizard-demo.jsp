<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    
    
    </head>
    <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
		<div class="impactcontent">

<div id="demoWrapper">
							<h3>Wizard Demo</h3>
		 					<form id="demoForm" method="post" action="json.html">
		 						<div id="fieldWrapper">
		 						<span class="step" id="first">
		 							<span class="font_normal_07em_black">First step - Common starting point</span><br />
			 						<label for="firstname">First name</label><br />
			 						<input class="input_field_12em" name="firstname" id="firstname"/><br />
			 						<label for="surname">Surname</label><br />
			 						<input class="input_field_12em" name="surname" id="surname"/><br />
			 						<label for="country">Country - will decide the route of the wizard</label><br />
			 						<select class="input_field_12em link required" name="country" id="country">
			 							<option value=""></option>
			 							<option value="sweden">Sweden</option>
			 							<option value="finland">Finland</option>
			 							<option value="norway">Norway</option>
			 						</select><br />
		 						</span>
		 						<span id="finland" class="step">
		 							<span>Step 2 - Finland</span><br />
		 							<label for="day_fi">Social Security Number</label><br />
			 						<input class="input_field_25em" name="day" id="day_fi" value="DD"/>
			 						<input class="input_field_25em" name="month" id="month_fi" value="MM"/>
			 						<input class="input_field_3em" name="year" id="year_fi" value="YYYY"/> - 
			 						<input class="input_field_3em" name="lastFour" id="lastFour_fi" value="XXXX"/><br />
									<label for="countryPrefix_fi">Phone number</label><br />
			 						<input class="input_field_35em" name="countryPrefix" id="countryPrefix_fi" value="+358"/> - 
			 						<input class="input_field_3em" name="areaCode" id="areaCode_fi"/> - 
			 						<input class="input_field_12em" name="phoneNumber" id="phoneNumber_fi"/><br />
			 						<label for="email_fi">*Email</label><br />
			 						<input class="input_field_12em required email" name="email" id="email_fi"/><br />	 						
		 						</span>
		 						<span class="step" id="finland_optional">
		 							<span class="font_normal_07em_black">Step 3 - Finland</span><br />
		 							<label for="optional">Optional information for finnish users</label><br />
		 							<textarea cols="25" rows="4" name="optional" id="optional"></textarea><br />
		 							<input type="hidden" class="link" value="confirmation" />
		 						</span>
		 						<span id="sweden" class="step">	
		 							<span class="font_normal_07em_black">Step 2 - Sweden</span><br />
		 							<label for="year_swe">Social Security Number</label><br />
			 						<input class="input_field_3em" name="year" id="year_swe" value="YYYY"/> 
			 						<input class="input_field_25em" name="month" id="month_swe" value="MM"/>
			 						<input class="input_field_25em" name="day" id="day_swe" value="DD"/> - 
			 						<input class="input_field_3em" name="lastFour" id="lastFour_swe" value="XXXX"/><br />
									<label for="countryPrefix_swe">Phone number</label><br />
			 						<input class="input_field_35em" name="countryPrefix" id="countryPrefix_swe" value="+46"/> - 
			 						<input class="input_field_3em" name="areaCode" id="areaCode_swe"/> - 
			 						<input class="input_field_12em" name="phoneNumber" id="phoneNumber_swe"/><br />
			 						<label for="email_swe">*Email</label><br />
			 						<input class="input_field_12em required email" name="email" id="email_swe"/><br />	 						
			 						<input type="hidden" class="link" value="confirmation" />
		 						</span>
		 						<span id="norway" class="step submit_step">
		 							<span class="font_normal_07em_black">Step 2 - Norway - alternative submit step</span><br />
			 						<label for="email_no">*Email</label><br />
			 						<input class="input_field_12em required email" name="email" id="email_no"/><br />
			 						<label for="username_no">User name</label><br />
			 						<input class="input_field_12em" name="username" id="username_no"/><br />
			 						<label for="password_no">Password</label><br />
			 						<input type="password" class="input_field_12em" name="password" id="password_no"/><br />
			 						<label for="retypePassword_no">Retype password</label><br />
			 						<input type="password" class="input_field_12em" name="retypePassword" id="retypePassword_no"/><br /> 						
		 						</span>
		 						<span id="confirmation" class="step">
		 							<span class="font_normal_07em_black">Last step - Common confirmation</span><br />
		 							<label for="username">User name</label><br />
			 						<input class="input_field_12em" name="username" id="username"/><br />
			 						<label for="password">Password</label><br />
			 						<input type="password" class="input_field_12em" name="password" id="password"/><br />
			 						<label for="retypePassword">Retype password</label><br />
			 						<input type="password" class="input_field_12em" name="retypePassword" id="retypePassword"/><br />
		 						</span>
		 						</div>
		 						<div id="demoNavigation"> 							
		 							<input type="reset" class="navigation_button" value="Reset Form" />
		 							<input type="submit" class="navigation_button" value="Submit" />
		 						</div>
		 					</form>
		 				</div>

	<jsp:include page="../footer.jsp" />
	</div>
	
	
		<!-- <script type="text/javascript" src="./js/jquery-1.4.2.min.js"></script>		
		<script type="text/javascript" src="./js/jquery-ui-1.8.5.custom.min.js"></script>-->
		<script type="text/javascript" src="/impactportal/js/formwizard-3.0.7/bbq.js"></script>
		<script type="text/javascript" src="/impactportal/js/formwizard-3.0.7/jquery.form.js"></script>
		<script type="text/javascript" src="/impactportal/js/formwizard-3.0.7/jquery.validate.js"></script>
		<script type="text/javascript" src="/impactportal/js/formwizard-3.0.7/jquery.form.wizard.js"></script>
		<script type="text/javascript">
		$(function(){
	$("#demoForm").formwizard({ //wizard settings
	 	historyEnabled : true, 
	 	formPluginEnabled: true, 
	 	validationEnabled : true,
	 	focusFirstInput : true,
	 	validationOptions : {
			messages: {	email: "Felaktig email (localized into Swedish)"}																
	 	},
		formOptions : {
			success: function(data){alert(data.registration.statusMessage);},
			beforeSubmit: function(data){alert("about to send the following data: \n\n" + $.param(data))},
			dataType: 'json',
			resetForm: true
		}
	 });
});
		</script>
  </body>
  
</html>