<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    
    <script type="text/javascript" src="esgfsearch.js"></script>

	 <link rel="stylesheet" href="esgfsearch.css" />
	  

      <script type="text/javascript">
     
    
      var impactservice='<%=impactservice.Configuration.getImpactServiceLocation()%>';
      $( document ).ready(function() {

    	  addFilter();
    	});
    </script>
  </head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
		
<div class="impactcontent">
	

<h1>Faceted search</h1>

<div class="searchCompContainer">



<div id="facetOverview" class="searchComp facetOverview">
  <div class="searchCompHeader">Filters</div>
  <div class="searchCompBody"></div>
  <div class="searchCompFooter"></div>
</div>

<div id="selectedElements" class="searchComp selectedElements">
  <div class="searchCompHeader">Selected filters</div>
  <div class="searchCompBody"></div>
  <div class="searchCompFooter"></div>
</div>

<div id="searchResults" class="searchComp searchResults">
  <div class="searchCompHeader">Results</div>
  <div class="searchCompBody"></div>
  <div class="searchCompFooter"></div>
</div>

</div>

</div>
   
	 	
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>