<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<jsp:include page="../includes-ui.jsp" />


</head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
	<div class="impactcontent">
	
	
		<form id="demoForm" method="post" action="json.html" class="bbq">
			<div id="fieldWrapper">
				<span class="step" id="step1"> <span
					class="font_normal_07em_black">First step - Select time and
						domain</span><br /> <label for="timeslice">Time slice</label><br /> <select
					class="input_field_12em required" name="timeslice"
					id="timeslice">
						<option value=""></option>
						<option value="annual">Annual</option>
						<option value="winter">Winter (ONDJFM)</option>
						<option value="summer">Summer (AMJJAS)</option>
						<option value="seasons">Seasons (DJF MAM JJA OND)</option>
						<option value="months">Months (J F M A M J J A S O N D)</option>
				</select> <br /> <label for="timeperiod">Time period</label><br /> From: <input
					class="input_field_25em" name="day" id="day_start" value="DD" /> <input
					class="input_field_25em" name="month" id="month_start" value="MM" />
					<input class="input_field_3em" name="year" id="year_start"
					value="YYYY" /> To: <input class="input_field_25em" name="day"
					id="day_end" value="DD" /> <input class="input_field_25em"
					name="month" id="month_end" value="MM" /> <input
					class="input_field_3em" name="year" id="year_end" value="YYYY" /><br />

					<label for="domain">Domain</label><br /> <select
					class="input_field_12em required" name="domain" id="domain">
						<option value="world">World</option>
						<option value="southamerica">Region 1: South America</option>
						<option value="centralamarica">Region 2: Central America</option>
						<option value="northamerica">Region 3: North America</option>
						<option value="europe">Region 4: Europe</option>
						<option value="africa">Region 5: Africa</option>
						<option value="southasia">Region 6: South Asia</option>
						<option value="eastasia">Region 7: East Asia</option>
						<option value="centralasia">Region 8 Central Asia</option>
						<option value="australasia">Region 9: Australasia</option>
						<option value="antarctica">Region 10: Antarctica</option>
						<option value="arctic">Region 11: Arctic</option>
						<option value="med">Region 12: Mediterranean domain (MED)</option>
						<option value="mena">Region 13: MENA domain</option>
				</select>
				</span> <span id="step2" class="step"> <span
					class="font_normal_07em_black">Step 2 - Personal information</span><br />
				</span> <span id="confirmation" class="step"> <span
					class="font_normal_07em_black">Last step - Username</span><br /> <label
					for="username">User name</label><br /> <input
					class="input_field_12em" name="username" id="username" /><br /> <label
					for="password">Password</label><br /> <input
					class="input_field_12em" name="password" id="password"
					type="password" /><br /> <label for="retypePassword">Retype
						password</label><br /> <input class="input_field_12em"
					name="retypePassword" id="retypePassword" type="password" /><br />
				</span>

			</div>
			<div id="demoNavigation">
				<input class="navigation_button" id="back" value="Back" type="reset" />
				<input class="navigation_button" id="next" value="Next"
					type="submit" />
			</div>
		</form>
		<hr />

		<jsp:include page="../footer.jsp" />
	</div>


	<!-- <script type="text/javascript" src="./js/jquery-1.4.2.min.js"></script>		
		<script type="text/javascript" src="./js/jquery-ui-1.8.5.custom.min.js"></script>-->
	<script type="text/javascript"
		src="/impactportal/js/formwizard-3.0.7/bbq.js"></script>
	<script type="text/javascript"
		src="/impactportal/js/formwizard-3.0.7/jquery.form.js"></script>
	<script type="text/javascript"
		src="/impactportal/js/formwizard-3.0.7/jquery.validate.js"></script>
	<script type="text/javascript"
		src="/impactportal/js/formwizard-3.0.7/jquery.form.wizard.js"></script>
	<script type="text/javascript">
    $(function() {
      $("#demoForm").formwizard({ //wizard settings
        historyEnabled : true,
        formPluginEnabled : true,
        validationEnabled : true,
        focusFirstInput : true,
        validationOptions : {
          messages : {
            email : "Felaktig email (localized into Swedish)"
          }
        },
        formOptions : {
          success : function(data) {
            alert(data.registration.statusMessage);
          },
          beforeSubmit : function(data) {
            alert("about to send the following data: \n\n" + $.param(data))
          },
          dataType : 'json',
          resetForm : true
        }
      });
    });
  </script>
</body>

</html>