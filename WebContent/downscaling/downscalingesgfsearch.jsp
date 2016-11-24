<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="impactservice.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="downscaling.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" import="model.Predictand"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ext.jsp" />
    <script type="text/javascript" src="/impactportal/js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="/impactportal/js/jqueryextensions/jquery.collapsible.min.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/fileviewer.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/vkbeautify.js"></script>
    <link rel="stylesheet"         href="/impactportal/data/fileviewer/fileviewer.css" />
    <script type="text/javascript" src="/impactportal/data/catalogbrowser/catalogbrowser.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychoosers.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch.js"></script>
    <script type="text/javascript" src="/impactportal/js/components/basket/basket.js"></script>
    <script type="text/javascript" src="/impactportal/js/components/basket/basketwidget.js"></script>
    <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
    <link rel="stylesheet"         href="/impactportal/data/esgfsearch/esgfsearch.css" />
    <link rel="stylesheet"         href="/impactportal/data/esgfsearch/simplecomponent.css" />

   
      <title>ESGF Search</title>
      <script type="text/javascript">
    
      /*
      * Callback is called when a user selects a file in his basket.
      */
      var callbackBasketSelect = function(files){
        alert("basket.postIdentifiersToBasket called, got nr files: "+files.length);
        console.log(files);
      };
      
      /*
      * Dialog popup showing the basket
      */
      var showBasketWidget= function(){
    	  console.log("showBasketWidget");
          basketWidget.show(function(selectedNodes) {
            callbackBasketSelect(selectedNodes);
    	    return true;//True means the basket dialog closes, otherwise the dialog is kept open.
    	  });
        };
        
        
      /*
      * YOu can override the post identifiers function, e.g. when a user selects a file in the search window, this callback is called.
      * If this function is not set, the file is going into the users basket under remote data instead.
      */
      basket.postIdentifiersToBasket = function(dataToAdd){
         /*
          * See reference implementation:
          * /impactportal/js/components/basket/basket.js
          */
          
         console.log(dataToAdd);//Catalogs are not yet expanded
          
         var callback = function(files){
           console.log(files);//Catalogs are now expanded to files
           alert("basket.postIdentifiersToBasket called, got nr files: "+files.length);
         };
         basket.expandNodes([{data:dataToAdd}],callback);
       };
     
      
      /* Configuration options, for downscaling*/
      var c4iconfigjs = {
        searchservice:"/impactportal/DownscalingSearch?",/*Downscaling endpoint */
        impactservice:"/impactportal/ImpactService?",
        adagucservice:"/impactportal/adagucserver?",
        adagucviewer:"/impactportal/adagucviewer/",
        howtologinlink:"/impactportal/help/howto.jsp?q=create_esgf_account",
        contactexpertlink:"/impactportal/help/contactexpert.jsp",
      }; 
    
      $( document ).ready(function() {
        var floating=false;/* YOu can choose either as floating dialog element or as embedded element */
        if(floating){
	        var el=jQuery('<div/>');
	        renderSearchInterface({
	          element:el,
	          service:c4iconfigjs.searchservice,
	          query:"",
	          catalogbrowserservice:c4iconfigjs.impactservice,
	          dialog:true
	        });
        }else{
            var el = $("#searchcontainer");
	        renderSearchInterface({
	          element:el,
	          service:c4iconfigjs.searchservice,
	          query:"project=clipc&variable=tas",//<-- Optional, prefill the search with a custom query.
	          catalogbrowserservice:c4iconfigjs.impactservice
	        });
        }
      });
    </script>
</head>
<body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="downscalingmenu.jsp" />
	
	<div style="width:954px;margin:auto;margin-top:10px;padding:10px">
	<button onclick="showBasketWidget();">
	  <span style="float:left;margin-right:5px;"><code class="ui-icon codeshoppingcarticon" style="width:0px;"></code></span>
	  <span>Show basket</span>
    </button>
	<div id="searchcontainer"></div> 
	</div>

	<jsp:include page="../footer.jsp" />
	<div class="modal"><!-- Place at bottom of page --></div>
</body>
</html>