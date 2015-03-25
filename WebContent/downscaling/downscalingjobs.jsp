<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="impactservice.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="downscaling.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="model.Job"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <%
   String Home="/impactportal/"; 
   %>
    <jsp:include page="../includes-ext.jsp" />
    
   <!--  <link rel="stylesheet" type="text/css" href="../js/ux/css/CheckHeader.css" /> -->
     
    <script type="text/javascript" src="../js/components/processors/useProcessor.js"></script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
    <script type="text/javascript" src="../js/components/basket/basketwidget.js"></script>
    <script type="text/javascript" src="../js/components/catalogbrowser/fileviewer.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>
    <script type="text/javascript">
    var impactBase = '<%=Home%>';
    var impactService=impactBase+'ImpactService?';
    var task;

    </script>
  </head>
  <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="downscalingmenu.jsp" />
	<jsp:include page="subscription.jsp" />
 	<c:if test="${isSubscribed}">
		<div class="impactcontent">
			<div class="breadcrumb"><a href="login.jsp">Downscaling</a> » Jobs </div>
			<h1>Submitted downscaling jobs</h1>
	
				Jobs for: <strong><%out.print(LoginManager.getUser(request,null).getUserName());%> </strong><br/>
			<table class="basket">
				<tr>
					<td style="width:150px;background-color:#DDD;"><b>Job ID:</b></td>
<!-- 					<td style="width:150px;background-color:#DDD;"><b>Started on:</b></td> -->
					<td style="width:360px;background-color:#DDD;"><b>Job type</b></td>
					<td style="width:360px;background-color:#DDD;"><b>Status location</b></td>
					<td style="width:30px;background-color:#DDD;"><b>Progress</b></td>
					<td style="background-color:#DDD;"><b>View</b></td>
					<td style="background-color:#DDD;"><b>X</b></td>
				</tr>
				<%
				List<Job> jobs = DownscalingService.getUserJobs(LoginManager.getUser(request).getInternalName());
				for(int i=0;i<jobs.size();i++){
					Job job = jobs.get(i);
					out.print("<tr><td>"+job.getId()+"</td>"+"<td>"+job.getType().trim()+"</td>"+"<td>"+job.getJobStatus()+"</td><td>none</td><td>none</td><td>none</td>"+"</tr>");
				}
				 %>
			</table>
  		</div>
	</c:if>	
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>