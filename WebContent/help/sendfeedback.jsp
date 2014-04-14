<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.DebugConsole,impactservice.LoginManager,impactservice.ImpactUser"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"
	import="impactservice.DrupalEditor,org.apache.commons.lang3.StringEscapeUtils,tools.HTMLParser"
	%><!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <jsp:include page="../includes.jsp" />
   
  </head> 
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->
		<jsp:include page="helpmenu.jsp" />
		<div class="impactcontent">
		
		
		
			<%
			
			
			String name =request.getParameter("name");
			String email =request.getParameter("email");
			String comments = request.getParameter("comments");
			boolean validInput = true;
			if(email.length()<3||comments.length()==0){
				validInput = false;
			}
			if(validInput){
				if(!tools.SendMail.isValidEmailAddress(email)){
					validInput = false;
				}
			}
			if(validInput){
				out.println("<h1>Thanks for your feedback!</h1><div class=\"textstandardleft\">Your feedback has been sent to one of our experts. Your comments were:");
				out.println("<div style=\"margin-top:10px;padding:10px;border-top:1px dashed #CCC;border-bottom:1px dashed #CCC;\">");
				String mailMessage="From: "+name+"\n";
				mailMessage+="Email: "+email+"\n\n";
				mailMessage+="--- Comments:---\n "+comments+"\n";
				mailMessage+="----------------\n";
				try{
					tools.SendMail.sendMail(impactservice.Configuration.ExpertContact.getEmailAddresses(),request.getParameter("email"),"[CLIMATE4IMPACT: Message from contact form]", mailMessage);
					
					String to[]={request.getParameter("email")};
					tools.SendMail.sendMail(to,request.getParameter("email"),"[CLIMATE4IMPACT: Message from contact form]", "The following message has been sent:\n\n"+mailMessage);
				}catch(Exception e){
			  		impactservice.MessagePrinters.printWarningMessage(out, "Message could not be delivered!",e);
				}
				
				out.println(((HTMLParser.textToHTML(request.getParameter("comments")))));
				out.println("</div>");
				out.println("<br/>You will be contacted as soon as possible. </div>");
			}else{
				out.println("<h1>Your feedback could not be sent</h1>");
				out.println("<div class=\"textstandardleft\"><div style=\"margin-top:20px;padding:10px;border-top:1px dashed #CCC;border-bottom:1px dashed #CCC;\">");
				out.println("<table><tr><td>Message has not been sent because no comments were given.</td></tr>");
				out.println("<tr><td style=\"text-align:center;\"><FORM><INPUT TYPE=\"button\" VALUE=\"Try again\" onClick=\"history.go(-1);return true;\"></FORM></td></tr></table>");
				out.println("</div></div>");
			}
			
			%>
			</div>
		
		
	
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>