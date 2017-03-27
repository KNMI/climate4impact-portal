<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<div class="impacttopmenu datamenu">

<%

		String Home="/impactportal/";
		//String header=ImpactPages.createHeader(request.getServletPath());
		
		int numProducts = 0,numJobs = 0,isAdmin = 0;
		String numProductsString="-",numJobsString = "-";
		
		try{
			numProducts = LoginManager.getUser(request).getShoppingCart().getNumFiles();
		}catch(Exception e){				
		} 
		if(numProducts!=0){
			numProductsString = ""+numProducts;
		}
		
		try{
			numJobs = LoginManager.getUser(request).getProcessingJobList().getNumJobs();
		}catch(Exception e){				
		}
		if(numJobs!=0){
			numJobsString = ""+numJobs;
		}
		
		try{
			if(LoginManager.getUser(request).hasRole("admin") == true)isAdmin = 1;
		}catch(Exception e){				
		}
				
		String pageName=request.getServletPath();
	
		boolean highlightBasket = false,highlightAccount = false, highlightProcessing = false;
		if(pageName.indexOf("basket.jsp")!=-1)highlightBasket = true;
		if(pageName.indexOf("upload.jsp")!=-1)highlightBasket = true;
		if(pageName.indexOf("downloadscript.jsp")!=-1)highlightBasket = true;
		if(pageName.indexOf("fileuploadresult")!=-1)highlightBasket = true;
		
		if(pageName.indexOf("login.jsp")!=-1)highlightAccount = true;
		if(pageName.indexOf("OAuth2.jsp")!=-1)highlightAccount = true;
		if(pageName.indexOf("getcredential.jsp")!=-1)highlightAccount = true; 
		if(pageName.indexOf("proc")!=-1)highlightProcessing = true; 
		if(pageName.indexOf("wizard")!=-1)highlightProcessing = true;
		
		//out.print(header); //returns file name and path
		%>

  <ul>
    <li class="impacttopmenu-firstli"></li>
  	<li <% if(highlightAccount)out.print("class=\"sel\""); %>><a href="<%=Home%>account/login.jsp" ><code class="codeusersicon"></code>&nbsp;Account</a></li>
   	<li  <% if(highlightBasket)out.print("class=\"sel\""); %>><a href="<%=Home%>account/basket.jsp" ><code class="codeshoppingcarticon"></code>&nbsp;Basket <code id="baskettext2">(<%=numProductsString%>)</code></a></li>
   	<li  <% if(highlightProcessing)out.print("class=\"sel\""); %>><a href="<%=Home%>account/processing.jsp" ><code class="codejobsicon"></code>&nbsp;Processing</a></li>
  	<li  <% if(pageName.indexOf("jobs.jsp")!=-1)out.print("class=\"sel\""); %>><a href="<%=Home%>account/jobs.jsp" ><code class="codejobsicon"></code>&nbsp;Jobs&nbsp;<code id="jobnumber">(<%=numJobsString%>)</code></a></li>
  	<li  <% if(pageName.indexOf("tokenapi.jsp")!=-1)out.print("class=\"sel\""); %>><a href="<%=Home%>account/tokenapi.jsp" ><code class="codekeyicon"></code>&nbsp;Token API</a></li>
  	
  	<% if (isAdmin==1) {%>
  		<li  <% if(pageName.indexOf("admin.jsp")!=-1)out.print("class=\"sel\""); %>><a href="<%=Home%>account/admin.jsp" >Administration page</a></li>
  	<%} %>
  	
   
  </ul>  
 </div>
 
 