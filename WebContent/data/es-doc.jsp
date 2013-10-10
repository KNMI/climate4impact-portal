<%@page import="impactservice.SessionManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ui.jsp" />
    <link href="http://prod.static.esdoc.webfactional.com/js_client/bin/latest/esdoc-min.css" media="screen" rel="stylesheet" type="text/css" /> 
   
    <script type="text/javascript">

    $(document).ready(function() {
    	ESDOC.viewer.setOptions({
    		dialogWidthInPixels:600,
    		showNullFields:false,
    		showFooter:false,
    		uiContainer:'.es-doc-info',
    		uiMode:'tabbed'//tabbed or linear
    		});
    	
    	var onViewExperiment = function(){
    		
    		ESDOC.viewer.renderFromName({
    		    type : 'numericalExperiment',
    		    name : $(".expinput").val(),
    		    project : 'CMIP5'
    		});
    		//alert('hello ' + $(".expinput").val());
    	};
    	$(".es-doc-go-button").click(onViewExperiment);
    	
        var onViewModel = function(){
    		ESDOC.viewer.renderFromName({
    		    type : 'modelComponent',
    		    name : $(".esdoc-modelinput").val(),
    		    project : 'CMIP5'
    		});
    	};
    	$(".es-doc-model-go-button").click(onViewModel);
    	
    	var onViewDataset = function(){
    		ESDOC.viewer.renderFromDatasetID({
    		    id : $(".esdoc-datasetinput").val(),
    		    project : 'CMIP5'
    		});
    	};
    	$(".es-doc-dataset-go-button").click(onViewDataset);
    	
    	var onViewFile = function(){
    		ESDOC.viewer.renderFromFileID({
    		    id : $(".esdoc-fileinput").val(),
    		    project : 'CMIP5'
    		});
    	};
    	$(".es-doc-file-go-button").click(onViewFile);
    	
    	$(":text").attr("size", "60");
    });
    </script>
    </head>
    <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />
		<div class="impactcontent">
	
	<table>
	<tr><td><label >experiment </label></td><td><input type="text" class="expinput" value="rcp45"/><input type="button" value="Go" class="es-doc-go-button" /></td></tr>
  	<tr><td><label >model </label></td><td><input type="text" class="esdoc-modelinput" value="EC-EARTH"/><input type="button" value="Go" class="es-doc-model-go-button"/></td></tr>
   	<tr><td><label >dataset </label></td><td><input type="text" class="esdoc-datasetinput" value="cmip5.output1.MPI-M.MPI-ESM-MR.1pctCO2.day.atmos.day.r1i1p1.v20120523"/><input type="button" value="Go" class="es-doc-dataset-go-button"/></td></tr>
  
   	<tr><td><label >file </label></td><td><input type="text" class="esdoc-fileinput" value="cmip5.output1.MOHC.HadGEM2-ES.1pctCO2.day.atmos.cfDay.r1i1p1.v20120215.hfss_cfDay_HadGEM2-ES_1pctCO2_r1i1p1_19791201-19801130.nc"/><input type="button" value="Go" class="es-doc-file-go-button"/></td></tr>
  	
  	
  	</table>
	
	</tr></table>
  	<div class="es-doc-info"></div>
	<jsp:include page="../footer.jsp" />
	</div>
  </body>
  <script src="http://prod.static.esdoc.webfactional.com/js_client/bin/latest/esdoc-min.js" type="text/javascript"></script>
</html>