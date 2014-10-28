<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<div class="impacttopmenu datamenu">

<%

		String Home="/impactportal/";
		//String header=ImpactPages.createHeader(request.getServletPath());
		int numProducts = 0;
		String numProductsString="-";
		
		try{
			numProducts = LoginManager.getUser(request).getShoppingCart().getNumProducts(request);
		}catch(Exception e){				
		}
		if(numProducts!=0){
			numProductsString = ""+numProducts;
		}
				
		String pageName=request.getServletPath();
		boolean highLightDownscaling = false;
		boolean highLightProcessing = false;
		boolean highLightDocumentation = false;

		if(pageName.indexOf("downscaling/downscaling.jsp")!=-1)highLightDownscaling=true;
		else if(pageName.indexOf("downscaling/processing.jsp")!=-1)highLightProcessing=true;
		else if(pageName.indexOf("downscaling/downscalingdocs.jsp")!=-1)highLightDocumentation=true;

		//out.print(header); //returns file name and path
		%>

  <ul style="height:20px;">
	<li <% if(highLightDocumentation)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/downscalingdocs.jsp" >Documentation</a></li>
	<li <% if(highLightDocumentation)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/subscription.jsp" >Subscription</a></li>
	<li <% if(highLightDownscaling)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/downscaling.jsp" >Create</a></li> 
	 <!-- 
      <li <% if(highLightProcessing)out.print("class=\"sel\""); %>><a href="<%=Home%>downscaling/processing.jsp" >Processing</a></li>
      -->
    

   
  </ul>  
 </div>
 
 