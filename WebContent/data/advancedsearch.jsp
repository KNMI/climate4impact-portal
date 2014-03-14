<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
     <jsp:include page="../includes-ext.jsp" />
     <script type="text/javascript" src="../js/components/basket/basket.js"></script>
    <script type="text/javascript" src="../js/components/search/searchmenus.js"></script>
	<script type="text/javascript" src="../js/components/search/advancedsearch.js"></script>

      <script type="text/javascript">
 // var searchSession=undefined;  
<%

//impactservice.SessionManager.SearchSession searchSession=(impactservice.SessionManager.SearchSession) session.getAttribute("searchsession");
//if(searchSession!=null){
//	out.println("  searchSession="+searchSession.getAsJSON());
//}
%>
      Ext.Loader.setConfig({enabled: true});

      Ext.require([
          'Ext.grid.*',
          'Ext.data.*',
          'Ext.util.*',
          'Ext.form.*',
          'Ext.state.*',
          'Ext.toolbar.Paging',
          'Ext.ModelManager',
          'Ext.util.TaskRunner',
          'Ext.window.Window',
          'Ext.tip.QuickTipManager'
      ]); 

      
    Ext.onReady(function(){


    	setSearchServiceURL('<%=impactservice.Configuration.getHomeURLHTTP()%>/ImpactService?');  
    	setDrupalServiceURL('<%=impactservice.Configuration.getHomeURLHTTP()%>/getDrupalNode.jsp?');
    	setCatalogBrowserURL('<%=impactservice.Configuration.getHomeURLHTTP()%>/data/catalogbrowser.jsp?');
    	var container = Ext.create('Ext.container.Container', {
            layout: 'fit',
    		renderTo:'container',
      	    minHeight:600,
    	    scripts:true,
    	    autoScroll:false, 
    	    items:searchPanel,
    	    loader: {} });
        
	        Ext.Ajax.request({
	            url: searchServiceURL+'service=session&mode=advancedsearch',
	            success: function(response){
	            	var jsonResp = Ext.decode(response.responseText);
	            	initialize(jsonResp);
	            },
	            failure: initialize
            });

    });
    </script>
  </head>
  <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />

	<div class="impactcontent">
		<div class="cmscontent"> 
		<!--  	<h1>Advanced search</h1>-->
			 
     <h1 class="title" id="page-title">
          Search        </h1>
                          <div id="block-system-main" class="block block-system">

    
  <div class="content">
    <div id="node-109" class="node node-page node-full clearfix">

      
  
  <div class="content clearfix">
    <div class="field field-name-body field-type-text-with-summary field-label-hidden"><div class="field-items"><div class="field-item even"> <p>Using the filters below you can search throughout the <a class="lexicon-term" href="?q=climate4impactglossary#CMIP5"><acronym title="Coupled Model Intercomparison Project - Phase 5.Read more...&amp;nbsp;">CMIP5</acronym></a> data archives. You can also use this portal to access other data archives, or even add your own archive on the <a href="/impactportal/data/catalogs.jsp" id="catalogs." name="catalogs." title="catalogs.">catalogs page.</a></p>
<p>Data you select for download will appear in your <a href="/impactportal/account/basket.jsp" id="basket." name="basket." title="basket.">basket.</a> You can also do some data processing on the <a href="/impactportal/data/transform.jsp" id="catalogs." name="transformations." title="transformations.">transformations page.</a></p>

</div></div></div></div></div></div></div>		
			<!-- Downloading data is possible for all institutes. However, viewing data is limited for institutes accepting climate4impact's security system.(<a href="/impactportal/documentation.jsp?q=esgfsecurity">more info</a>)--> 
			<div class="bodycontent">    
				<table>
					<tr>
						<td class="container"><div id="container" ></div></td>
						<td class="split"></td>
						<td class="contexthelp"><div id="contexthelp">
						<%try{out.print(DrupalEditor.showDrupalContent("?q=node/8",request));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%>
            </div></td>
					</tr>
				</table>
			</div>
		</div>
	</div>


	
	

	<jsp:include page="../footer.jsp" />
  </body>
</html>