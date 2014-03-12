<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <%
  //Automatically redirect to the catalogbrowser if accidently a catalog was given 
  String a=request.getParameter("dataset") ;
  if(a==null)a=request.getParameter("catalog") ;
  if(a==null){
	  out.println("Invalid catalog or dataset");
  }
  if(a!=null){
	  //Automatically redirect to the datasetviewer if accidently a dataset was given 
	  if(a.endsWith(".nc")==true){
	      String redirectURL = "/impactportal/data/datasetviewer.jsp?dataset="+a;
	      response.sendRedirect(redirectURL);
	  }

  %>
    <jsp:include page="../includes-ext.jsp" />
    
    <script type="text/javascript">
	

      var serviceURL='/impactportal/ImpactService?';
      var datasetViewerURL='/impactportal/data/datasetviewer.jsp?dataset=';
      var catalogURL = undefined;
      catalogURL='<%= request.getParameter("catalog") %>';
      var datasetViewerSession=undefined;
	    <%
	   /* impactservice.SessionManager.DatasetViewerSession datasetViewerSession=(impactservice.SessionManager.DatasetViewerSession) session.getAttribute("datasetviewersession");
	    if(datasetViewerSession!=null){
	      out.println("datasetViewerSession="+datasetViewerSession.getAsJSON());
	    }*/
	    %>
    </script>
    <script type="text/javascript" src="../js/components/search/searchmenus.js"></script>
    <script type="text/javascript" src="../js/components/catalogbrowser/datasetviewer.js"></script>
    <script type="text/javascript" src="../js/components/catalogbrowser/explorer.js"></script>
    <script type="text/javascript">
    Ext.Loader.setConfig({enabled: true});
    Ext.require([
        'Ext.grid.*',
        'Ext.data.*',
        'Ext.util.*',
        'Ext.form.*',
        'Ext.state.*',
        'Ext.urlDecode',
        'Ext.toolbar.Paging',
        'Ext.ModelManager',
        'Ext.tip.QuickTipManager',
        'Ext.getCmp'
    ]); 

    Ext.onReady(function(){
    	setSearchServiceURL('<%=impactservice.Configuration.getHomeURLPrefix()%>/ImpactService?'); 
    	var container = Ext.create('Ext.container.Container', {
            layout: 'fit',
    		renderTo:'container',
    		id:'ext_container',
      	    minHeight:550,
    	    scripts:true,
    	    autoScroll:false, 
    	    items:createExplorer(),
    	    loader: {} });
    	container.setLoading(true);
      if(datasetViewerSession){
        if(datasetViewerSession.datasetURL){
          Ext.getCmp('cataloglisting').loadDapURL(datasetViewerSession.datasetURL);
        }
      }
      

    });
    </script>
    <style type="text/css">
     .x-form-field{
    	margin: 0 0 0 0;
    	font:normal 12px courier new;
  		}
  	</style>
  </head>
 <body>
	<jsp:include page="../header.jsp" />
	<jsp:include page="datamenu.jsp" />


	<div class="impactcontent">
		<div class="cmscontent"> 
			<h1>Catalog browser - browse THREDDS catalogs</h1>
			<div class="bodycontent">    
   		 Catalog <a href="<%= request.getParameter("catalog").replace(".xml", ".html") %>"><%= request.getParameter("catalog").replace(".xml", ".html") %></a><br/>
			<div id="datasetinfo"/>
				<table>
					<tr>
						<td class="container"><div id="container" ></div></td>
						<td class="split"></td>
						<td class="contexthelp"><%try{out.print(DrupalEditor.showDrupalContent("?q=datasetviewercontext",request));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%></td>
					</tr>
				</table>
			</div>
		</div>
	</div>
<%} %>
	<jsp:include page="../footer.jsp" />
  </body>
</html>