<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>

    <jsp:include page="../includes-ext.jsp" />
    <script type="text/javascript" src="../js/components/processors/wpsOverview.js"></script>
    <script type="text/javascript">
    Ext.Loader.setConfig({
      enabled: true
  });
  Ext.Loader.setPath('Ext.ux', '../js/ux');

  Ext.require([
      'Ext.selection.CellModel',
      'Ext.grid.*',
      'Ext.data.*',
      'Ext.util.*',
      'Ext.state.*',
      'Ext.form.*',
      'Ext.ux.CheckColumn',
      'Ext.ux.ButtonColumn'
  ]);
  Ext.QuickTips.init();
    Ext.onReady(function(){
    	var container = Ext.create('Ext.container.Container', {
 	        layout: 'fit',
 			renderTo:'container',
 		   	minHeight:600,
 		    scripts:true,
 		    autoScroll:false, 
 		    items:[wpsOverView],
 		    padding:'10 10 10 10',
 		    loader: {} 
    	});
    });
    </script>
  </head>
  <body>
		<jsp:include page="../header.jsp" /> 
		<!-- Contents -->
		<jsp:include page="datamenu.jsp" />
		<div class="impactcontent"> 
		<div class="cmscontent"> 
		<h1>Browse Web Processing Services</h1>
			<div id="content2"></div>
			<div id="container"></div>
		</div>
		</div>
		<!-- /Contents -->
	  <jsp:include page="../footer.jsp" />
  </body>
</html>