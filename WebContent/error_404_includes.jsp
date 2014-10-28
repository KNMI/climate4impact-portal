 <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"  import="impactservice.ImpactService,tools.HTTPTools"%>
 <%
	String searchCommand=null;
	try{
		searchCommand=HTTPTools.getHTTPParam(request, "q");
	}catch(Exception e){
	}
	//Detect if we found a searchstring
	String searchString = "";
	if(searchCommand!=null){
		try{
			if(searchCommand.indexOf("search/node/")==0){
		searchString=searchCommand.substring("search/node/".length());
		tools.Debug.println(searchString);
			}
		}catch(Exception e){
			searchString="";
		}
	}
	

  String referrer = request.getHeader("referer");
  
  String queryString = "";
  if(request.getQueryString()!=null){
	  queryString = "?"+request.getQueryString();
  }
   impactservice.MessagePrinters.emailFatalErrorMessage("Page not found!","Page not found: "+request.getAttribute("javax.servlet.forward.request_uri")+queryString+"\nReferrer: "+referrer);
    
  %>
    
    <!-- Contents -->
    
    <div class="impactcontent">

      <h1>404, Page not found.</h1><br/>
          <div style="float: right;clear:both;overflow:none; border: none;">
      <img src="/impactportal/images/Stick_figure-wondering.png" alt="Page not found."/>
	</div>
       <div class="textstandardleft">
      <p>
      <%

      %>
      Sorry, we cannot find what you're looking for...<br/>You can try to search:
      	<span class="searchbox"><input id="searchbox2" class="searchbox"
		type="text" size="16" value="<%=searchString%>"
		onkeypress="startSearchKeyPressed(event,this.form,'searchbox2')" /></span>
      </p>
      <br/>
      <a href="javascript:javascript:history.go(-1)">Go back</a>
      <br/>

    </div>
    </div>