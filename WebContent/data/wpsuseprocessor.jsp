<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <jsp:include page="../includes-ext.jsp" />
  <%
  	String processorId = request.getParameter("processor");
  	User user = null;
	try{
		user = LoginManager.getUser(request);
	}catch(Exception e){
	}
	if(user!=null&&processorId!=null){
	%>

  
  
  
    
    <link rel="stylesheet" type="text/css" href="../js/ux/css/CheckHeader.css" />
     
    <script type="text/javascript" src="../js/components/processors/useProcessor.js"></script>
    
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
  		    items:[{
  				region:'center',
  				xtype:'panel',
  				minHeight:800,
  				items:[{
  			   			region:'south',
  			   			xtype:'panel',
  			   			title:'Start' ,
  			   			id:'wpsstart',
  			   			height:200, 
  			   			frame:true,border:false,
  			   			buttons:[{text:'Start processing',handler:function(){startProcessing(configuredWPSItems,currentWPSId);}}],
  			   		
  			   			
  			   				html:'<div id="wpsdivdescription" >Loading Web Processing Service description...</div><div id="wpsdivresult"/>'
  			   				
  			   		
  		    		},{
  		    			xtype:'panel',
  						title:'Settings',
  						id:'wpsparams',
  						layout: {
  						    type: 'vbox',
  						    align : 'stretch'
  						},
  						frame:true,border:false//,  						buttons:[{text:'Load preset',handler:function(){alert("not yet implemented");}},{text:'Save preset',handler:function(){alert("not yet implemented");}}]
  					}			
  				]
  	            
  	   		}],
  		    padding:'10 10 10 10',
  		    loader: {} 
       	});
    	
    	wpsProcessorDetails('<%=processorId%>');
    });
    </script>
    	 <%
 	}
    %>
  </head>
  <body>
		<jsp:include page="../header.jsp" /> 
		<!-- Contents -->
		<jsp:include page="datamenu.jsp" />
		<div class="impactcontent"> 
		<div class="cmscontent"> 
		<h1>Use a Processing Service</h1><br/>
	
		 
		 <%

			
		 	if (user==null){
			 %>
			 	<ul>
			 	<li><a href="wpsoverview.jsp">Go back to processing overview</a></li>
			 	<li>Before you can start processing, you need to <a target="_blank" href="../account/login.jsp">login</a>. When you are logged in, you can <a href="" onclick="document.location.reload(true);">refresh</a> this page.</li>
			 	<li>Please read why you need to login at <a href="../help/howto.jsp#why_login">Howto: Why Login?</a></li>
			 	</ul>
			 <%
		 	}
		 	
		 	if(processorId == null){
	 		%>
			 	<ul>
			 	<li>Before you can start processing, you need to select a processor</li>
			 	<li><a href="wpsoverview.jsp">Go back to processing overview to select one.</a></li>
			 	</ul>
			<%	
		 	}
			if(user!=null&&processorId!=null){
			%>
				<a href="wpsoverview.jsp">Back to overview</a>
				<div id="content2"></div>
				<div id="container" style="padding-bottom:20px; "></div>
			 <%
		 	}
		 %>
		 
			
		</div>
		</div>
		<!-- /Contents -->
	  <jsp:include page="../footer.jsp" />
  </body>
</html>