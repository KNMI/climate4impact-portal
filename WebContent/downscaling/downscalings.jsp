<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="impactservice.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="downscaling.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="model.Job"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="model.Downscaling"%>
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
			<div class="breadcrumb"><a href="login.jsp">Downscaling</a> » Downscalings </div>
			<h1>Downscalings</h1>
	
				Downscalings for: <strong><%out.print(LoginManager.getUser(request).getUserName());%> </strong><br/>
			<table class="basket">
				<tr>
					<td style="background-color:#DDD;"><b>Job ID:</b></td>
					<td style="background-color:#DDD;"><b>Type</b></td>
					<td style="background-color:#DDD;"><b>Predictand</b></td>
					<td style="background-color:#DDD;"><b>Downscaling Method</b></td>
					<td style="background-color:#DDD;"><b>Project</b></td>
					<td style="background-color:#DDD;"><b>Model</b></td>
					<td style="background-color:#DDD;"><b>Experiment</b></td>
					<td style="background-color:#DDD;"><b>ensemble</b></td>
					<td style="background-color:#DDD;"><b>Period</b></td>
					<td style="background-color:#DDD;"><b>Status</b></td>
					<td style="background-color:#DDD;"><b>Operations</b></td>
				</tr>
				<%
				List<Downscaling> downscalings = DownscalingService.getUserDownscalings(LoginManager.getUser(request).getUserId());
				for(int i=0;i<downscalings.size();i++){
					Downscaling d = downscalings.get(i);
					out.print("<tr><td class='job-id'>"+d.getJobId()+"</td>"+"<td class='type'>"+d.getType()+"</td>"+"<td class='predictand'>"+d.getPredictand()+"</td><td class='d-method'>"+d.getDownscalingMethod()+
							"</td><td class='project'>"+d.getProject()+"<td class='model'>"+d.getModel()+"<td class='experiment'>"+d.getExperiment()+"<td class='ensemble'>"+d.getEnsemble()+"</td>"+"<td class='Period'>"+d.getsDate() + d.geteDate()+"<td class='s-year'>"+d.getsYear()+"</td>"+"<td class='e-year'>"+d.geteYear()+"</td>");
					if(d.getStatus() == 30){
						out.print("<td>Finished</td><td>");
						out.print("<a download='downscaling-"+d.getJobId()+".zip' href='../DownscalingService/downscalings/download?jobId="+d.getJobId()+"&zone="+d.getZone()+"&predictand="+d.getPredictand()+"&downscalingMethod="+d.getDownscalingMethod()+"&model="+d.getModel()+"&experiment="+d.getExperiment()+"&sYear="+d.getsYear()+"&eYear="+d.geteYear()+"&username="+LoginManager.getUser(request).getUserId()+"&type="+d.getType()+"'>CSV</a>");
						out.print("<a download='"+d.getPredictand()+"_"+d.getModel()+"_"+d.getExperiment()+"_"+d.getEnsemble()+".nc' href='../DownscalingService/downscalings/download4?jobId="+d.getJobId()+"&zone="+d.getZone()+"&predictand="+d.getPredictand()+"&downscalingMethod="+d.getDownscalingMethod()+"&model="+d.getModel()+"&experiment="+d.getExperiment()+"&sYear="+d.getsYear()+"&eYear="+d.geteYear()+"&username="+LoginManager.getUser(request).getUserId()+"&type="+d.getType()+"'>NetCDF</a>");
						out.print("</td>");
					}else if(d.getStatus() == 50){
						out.print("<td>Failed</td><td>X</td>");
					}else{
						out.print("<td>Processing</td><td></td>");
					}
				}
				 %>
			</table>
  		</div>
	</c:if>	
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>