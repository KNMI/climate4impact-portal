<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<div class="impacttopmenu datamenu">

<%
		String Home="/impactportal/";
		//String header=ImpactPages.createHeader(request.getServletPath());
		
		String[] menuElements={"documentation/guidanceandusecases.jsp","documentation/backgroundandtopics.jsp","documentation/glossary.jsp","documentation/mapandplot.jsp","documentation/publications.jsp"};
		String[] menuNames   ={"Guidance &amp; use cases","Background &amp; topics","Glossary","Map &amp; Plot","Publications"};
				
		String pageName=request.getServletPath();
		
		boolean[] highLighted = new boolean[menuElements.length];

		for(int j=0;j<menuElements.length;j++){
			if(pageName.indexOf(menuElements[j])!=-1)highLighted[j]=true;else highLighted[j]=false;
		}
			out.print("<ul>");
		for(int j=0;j<menuElements.length;j++){
			out.print("<li ");
			if(highLighted[j])out.print("class=\"sel\"");
			out.print(">");
			out.print("<a href=\""+Home+menuElements[j]+"\">"+menuNames[j]+"</a>");
			out.print("</li>");
		}
		out.print("</ul>");
		%>
 
 </div>
 
 