<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" import="impactservice.*,tools.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <jsp:include page="../includes-ext.jsp" />
      <link rel="stylesheet" href="/impactportal/account/login.css" type="text/css" />
     <script type="text/javascript" src="/impactportal/account/js/login.js"></script>
  
  <%
    	String processorId = null;
  		try{
  			processorId = HTTPTools.getHTTPParam(request, "processor");
  		}catch(Exception e){
  			processorId = null;
  		}
      	ImpactUser user = null;
    	try{
    		user = LoginManager.getUser(request);
    	}catch(Exception e){
    	}
    	if(user!=null&&processorId!=null){
    %>

  
  
  
    
    <!-- <link rel="stylesheet" type="text/css" href="../js/ux/css/CheckHeader.css" /> -->
     
    <script type="text/javascript" src="../js/components/processors/useProcessor.js"></script>
    <script type="text/javascript" src="../js/components/basket/basketwidget.js"></script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
    
    <script type="text/javascript" src="../js/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>
    
    <script type="text/javascript" src="/impactportal/data/catalogbrowser/catalogbrowser.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/vkbeautify.js"></script>
    <script type="text/javascript" src="/impactportal/data/fileviewer/fileviewer.js"></script>    
    <script type="text/javascript" src="/impactportal/data/esgfsearch/property_descriptions.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychooserconf.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch-propertychoosers.js"></script>
    <script type="text/javascript" src="/impactportal/data/esgfsearch/esgfsearch.js"></script>
    <link rel="stylesheet"        href="/impactportal/data/esgfsearch/esgfsearch.css" />
  <link rel="stylesheet"        href="/impactportal/data/esgfsearch/simplecomponent.css" />
    <link rel="stylesheet"        href="/impactportal/data/fileviewer/fileviewer.css"></link>
    
    <style>
     .x-toolbar-footer{
     background:none;
     }
      .c4i-wpsuseprocessor-table  {
          border-collapse: collapse;
          width: 100%;
      
          border-spacing: 0;
      }
      
      .c4i-wpsuseprocessor-table th, .c4i-wpsuseprocessor-table td {
        border: 1px solid #ddd;
        padding: 3px 8px 3px 8px;
        text-align: left;
          text-align: left;
          padding: 8px;
      }
      .c4i-wpsuseprocessor-table tr:nth-child(even){background-color: #f5f5f5}
      
      /*.c4i-wpsuseprocessor-table tr:hover {background-color: #f0f0f0 !important}*/
      
      .c4i-wpsuseprocessor-table th{
        background: none repeat scroll 0 0 #428bca;
        color: white;
        font-size: 16px;
        font-weight: bold;
      }
      
      .x-panel-default-framed{
        border-radius: 0px !important;
      }
     </style>  
        
    <script type="text/javascript">
    var impactService = '/impactportal/ImpactService?';
    Ext.Loader.setConfig({
        enabled: true
    });
    /*Ext.Loader.setPath('Ext.ux', '../js/ux');

    Ext.require([
        'Ext.selection.CellModel',
        'Ext.grid.*',
        'Ext.data.*',
        'Ext.util.*',
        'Ext.state.*',
        'Ext.form.*',
        'Ext.ux.CheckColumn',
        'Ext.ux.ButtonColumn'
    ]);*/
    Ext.QuickTips.init();


    Ext.onReady(function(){
       
       	var container = Ext.create('Ext.container.Container', {
  	        layout: 'fit',
  			renderTo:'container',
  		   	
  		   	border:false,
  		    scripts:true,
  		    autoScroll:false,

  		    items:[{
  				region:'center',
  				xtype:'panel',
  				layout:'form',
  				//minHeight:800,
  				border:false,
  				//padding:'4 4 4 4',
  				//margin:'4 4 4 4',
  				items:[{
  			   		
  			   			xtype:'panel',
  			   			//title:'Processor '+processorIding details and options' ,
  			   			id:'wpsstart',
  			   			//height:220, 
  			   			frame:false,
  			   			border:false,
  			   			//padding:2,
  			  			margin:'0 0 14 0',
  			   			buttons:[{iconCls:'codejobsicon',text:'Start processing',handler:function(){startProcessing(configuredWPSItems,currentWPSId);}}],
  			   		
  			   			
  			   				html:'<div id="wpsdivdescription" ><h2>... Loading Web Processing Service description...</h2></div><div id="wpsdivresult"/>'
  			   				
  			   		
  		    		},{
  		    		//	xtype:'panel',
  		    
  		    		
  						//title:'Settings',
  						id:'wpsparams',
  						layout: {
  						    type: 'vbox',
  						    align : 'stretch'
  						},
  						frame:false,border:false//,  						buttons:[{text:'Load preset',handler:function(){alert("not yet implemented");}},{text:'Save preset',handler:function(){alert("not yet implemented");}}]
  					}			
  				]
  	            
  	   		}]//,  		    padding:'10 10 10 10'
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
		<jsp:include page="loginmenu.jsp" />
		<div class="impactcontent"> 
	
		<div class="breadcrumb"><a href="login.jsp">Account</a> » <a href="processing.jsp">Processing </a> » Use a processor » <%=processorId %></div>
		
	
		
		 
		 <%

			
		 	if (user==null){
			 %>
			 	<ul>
			 	<li>Before you can use this processor, you need to <a href="#" onclick="generateLoginDialog(true)">sign in</a> and <a  href="" onclick='location.reload();'>refresh this page</a>.</li>
			 	<li><a href="processing.jsp">Go back to processing overview</a></li>
			 	</ul>
			 <%
		 	}
		 	
		 	if(processorId == null){
	 		%>
			 	<ul>
			 	<li>Before you can start processing, you need to select a processor</li>
			 	<li><a href="processing.jsp">Go back to processing overview to select one.</a></li>
			 	</ul>
			<%	
		 	}
			if(user!=null&&processorId!=null){
			%>

				
				<div id="content2"></div>
				<div id="container" style="padding-bottom:20px; "></div>
				
			 <%
		 	}
		 %>
		 
			
		
		</div>
		<!-- /Contents -->
	  <jsp:include page="../footer.jsp" />
  </body>
</html>