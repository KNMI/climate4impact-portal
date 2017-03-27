<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="impactservice.* "%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="impacttopmenu datamenu">

<!-- TAB HIGHLIGHTING -->
	<%

		String Home="/impactportal/";
		//String header=ImpactPages.createHeader(request.getServletPath());
		int numProducts = 0;
		String numProductsString="-";
		
		try{
			numProducts = LoginManager.getUser(request).getShoppingCart().getNumFiles();
		}catch(Exception e){				
		}
		if(numProducts!=0){
			numProductsString = ""+numProducts;
		}
				
		String pageName=request.getServletPath();
		boolean highLightDownscaling = false;
		boolean highLightSubscription = false;
		boolean highLightDocumentation = false;
		boolean highLightDownscalingJobs = false;
		boolean highLightDownscalings = false;

		if(pageName.indexOf("downscaling/create.jsp")!=-1)highLightDownscaling=true;
		else if(pageName.indexOf("downscaling/downscalingdocs.jsp")!=-1)highLightDocumentation=true;
		else if(pageName.indexOf("downscaling/downscalingjobs.jsp")!=-1)highLightDownscalingJobs=true;
		else if(pageName.indexOf("downscaling/downscalings.jsp")!=-1)highLightDownscalings=true;

		//out.print(header); //returns file name and path
	%>

  <ul>
  <li class="impacttopmenu-firstli"></li>
	<li <% if(highLightDocumentation)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/downscalingdocs.jsp" >Documentation</a></li>
	<li <% if(highLightDownscaling)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/create.jsp" >Create <span class="text-small"> beta</span></a> </li>
	<li <% if(highLightDownscalingJobs)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/downscalingjobs.jsp" >Jobs <span class="text-small"> beta</span></a> </li>
	<li <% if(highLightDownscalings)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/downscalings.jsp" >Downscalings <span class="text-small"> beta</span></a> </li>
  </ul>  
 </div>
 

 