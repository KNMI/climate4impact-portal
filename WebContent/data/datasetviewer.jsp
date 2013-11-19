<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.DrupalEditor"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <%
		//Automatically redirect to the catalogbrowser if accidently a catalog was given 
		String a=request.getParameter("dataset") ;
		if(a==null)a=request.getParameter("catalog") ;
		if(a!=null){
			if(a.length()>0){
				if(a.indexOf(".xml")>a.length()-7||a.indexOf("catalog.html")!=-1){
				    String redirectURL = "/impactportal/data/catalogbrowser.jsp?catalog="+a;
				    response.sendRedirect(redirectURL);
				}
			}
		}
		String dataset = request.getParameter("dataset");
		dataset=dataset.replace("thredds/fileServer","thredds/dodsC");
		String dapURL = dataset.split("\\|")[0];
		int inda = dapURL.indexOf(".nc.html");
		if(inda>0){
		  dapURL = dapURL.substring(0,inda+3);
		}
	  %>
  
    <jsp:include page="../includes-ext.jsp" />
       <script type="text/javascript">
    	var serviceURL='/impactportal/ImpactService?';
    	var dataset=undefined;;
    	dataset='<%= dapURL %>';
    </script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script>
    <script type="text/javascript" src="../js/components/search/searchmenus.js"></script>
    <script type="text/javascript" src="../js/components/catalogbrowser/fileviewer.js"></script>
    <script type="text/javascript">
  
	
                     
    Ext.onReady(function(){
    	Ext.override(Ext.data.proxy.Ajax,{ timeout:240000});
    	setSearchServiceURL('<%=impactservice.Configuration.getHomeURL()%>/ImpactService?'); 
    	var fileViewer = new FileViewer();
		var container = Ext.create('Ext.container.Container', {
		  layout: 'fit',
		  renderTo:'container',
		  minHeight:600,
		  scripts:true,
		  autoScroll:false, 
		  items:fileViewer.getViewer(),
		  loader: {} 
		});
		fileViewer.load(dataset);
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
			<h1>NetCDF metadata viewer</h1>
			<div class="bodycontent">    
		
   		 Dataset <a target="_blank" href="<%= dataset+".dds" %>"><%= dataset %></a><br/>
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
	<jsp:include page="../footer.jsp" />
  </body>
</html>