<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<div class="impacttopmenu datamenu">

<%

		String Home="/impactportal/";
		//String header=ImpactPages.createHeader(request.getServletPath());
		
		int numProducts = 0,numJobs = 0;
		String numProductsString="-",numJobsString = "-";
		
		try{
			numProducts = LoginManager.getUser(request).getShoppingCart().getNumProducts(request);
		}catch(Exception e){				
		}
		if(numProducts!=0){
			numProductsString = ""+numProducts;
		}
		
		try{
			numJobs = LoginManager.getUser(request).getProcessingJobList().getNumProducts(request);
		}catch(Exception e){				
		}
		if(numJobs!=0){
			numJobsString = ""+numJobs;
		}
				
		String pageName=request.getServletPath();
	
		boolean highlightBasket = false,highlightAccount = false;
		if(pageName.indexOf("basket.jsp")!=-1)highlightBasket = true;
		if(pageName.indexOf("upload.jsp")!=-1)highlightBasket = true;
		if(pageName.indexOf("fileuploadresult")!=-1)highlightBasket = true;
		
		if(pageName.indexOf("login.jsp")!=-1)highlightAccount = true;
		if(pageName.indexOf("getcredential.jsp")!=-1)highlightAccount = true;
			
		
		//out.print(header); //returns file name and path
		%>

  <ul style="height:20px;">
  	<li <% if(highlightAccount)out.print("class=\"sel\""); %>><a href="<%=Home%>account/login.jsp" ><code class="codeusersicon"></code>&nbsp;Account</a></li>
   	<li  <% if(highlightBasket)out.print("class=\"sel\""); %>><a href="<%=Home%>account/basket.jsp" ><code class="codeshoppingcarticon"></code>&nbsp;Basket <code id="baskettext2">(<%=numProductsString%>)</code></a></li>
  	<li  <% if(pageName.indexOf("jobs.jsp")!=-1)out.print("class=\"sel\""); %>><a href="<%=Home%>account/jobs.jsp" ><code class="codejobsicon"></code>&nbsp;Jobs <code id="jobnumber">(<%=numJobsString%>)</code></a></li>
  	
   
  </ul>  
 </div>
 
 