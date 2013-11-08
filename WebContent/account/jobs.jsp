<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   
    <jsp:include page="../includes-ext.jsp" />
    
    <link rel="stylesheet" type="text/css" href="../js/ux/css/CheckHeader.css" />
     
    <script type="text/javascript" src="../js/components/processors/useProcessor.js"></script>
    <script type="text/javascript" src="../js/ImpactJS.js"></script>
    <script type="text/javascript">
    var impactBase = '/impactportal/';
    var impactService=impactBase+'ImpactService?';
    var task;
    var removeId = function(id){
    	
    	var passFn = function(e){
    		var json= Ext.JSON.decode(e.responseText);
   		    if(json.error){
  		    	alert(json.error);
   		    }else{
   				adjustNumberOfJobsDisplayedInMenuBar(json);
   		    }
    		populateJobList();
    	};
    	Ext.Ajax.request({
   		    url: impactService,
   		    success: passFn,   
   		    failure: passFn,
   		    timeout:5000,
   		 	method:'GET',
   		    params: { service:'processor',request:'removeFromList',id:id }  
   		 });
    }
    
    var populateJobList = function(){
   	
   		var passFn = function(data){
    		Ext.fly('joblist').dom.innerHTML=data.responseText;

   	      task.stop();
   	      task.start();
   		}
   		var failFn = function(msg){
   			var data = {};
   			data.responseText='Unable to retrieve processing list from server';
   			passFn(data);
   		}
   	 	Ext.Ajax.request({
   		    url: impactService,
   		    success: passFn,   
   		    failure: failFn,
   		    timeout:10000,
   		 	method:'GET',
   		    params: { service:'processor',request:'getProcessorStatusOverview' }  
   		  });
    }
    
    
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
		var runner = new Ext.util.TaskRunner();
      	task = runner.newTask({
        	run: function () {
        		populateJobList();
       	},
        	repeat: 1,
        	interval:5000
       });
       //task.start();
     	populateJobList();
    });
    </script>
  </head>
  <body>
		<jsp:include page="../header.jsp" />
		<!-- Contents -->

		<jsp:include page="loginmenu.jsp" />
 
		<div class="impactcontent">
		<h1>Processing jobs</h1>
		<%
		
	

		User user = null;
		try{
			user = LoginManager.getUser(request);
		}catch(Exception e){
			
		}
		
		 if (user==null){ 
			%>
			<p>You are not logged in, please go to the <a href="../login.jsp">login page</a> and log in</p>
			<%
		}else{
			out.println("<div id='joblist'/>");
			//out.println(GenericCart.CartPrinters.showJobList(jobList,request));
		}
		%>
		
		<!-- 
  	    <div class="cmscontent">
  		
  		</div>
  		 -->
  		
		</div>
	 
  <!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>