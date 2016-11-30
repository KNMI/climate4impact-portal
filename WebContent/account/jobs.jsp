<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <%
   String Home="/impactportal/"; 
   %>
    <jsp:include page="../includes-ext.jsp" />
    
    <script type="text/javascript" src="../js/components/processors/useProcessor.js"></script>
    <script type="text/javascript" src="../js/components/basket/basket.js"></script> 
    <script type="text/javascript" src="../js/components/basket/basketwidget.js"></script>
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
    
          <script type="text/javascript" src="/impactportal/data/c4i-processing/c4i-processing.js"></script>
    <script type="text/javascript" src="/impactportal/data/c4i-processing/WMJSProcessing.js"></script> 
    <script type="text/javascript" src="/impactportal/data/c4i-processing/WMJSTimer.js"></script> 
    <link rel="stylesheet" href="/impactportal/data/c4i-processing/c4i-processing.css" />
  
    
    
    <style>
		.c4i-joblist-loader{
		  background-image:url("../images/ajax-loader.gif") !important;
  		width:32px;
  		height:32px;
		}
			.c4i-joblist-table  {
			    border-collapse: collapse;
			    width: 100%;
			
			    border-spacing: 0;
			}
			
			.c4i-joblist-table th, .c4i-joblist-table td {
			  border: 1px solid #ddd;
			  padding: 3px 8px 3px 8px;
			  text-align: left;
			    text-align: left;
			    padding: 8px;
			}
			.c4i-joblist-table tr:nth-child(even){background-color: #f5f5f5}
			
			/*.c4i-joblist-table tr:hover {background-color: #f0f0f0 !important}*/
			
      .c4i-joblist-table th{
        background: none repeat scroll 0 0 #428bca;
		    color: white;
		    font-size: 16px;
		    font-weight: bold;
			}
	 </style>
	 
    <script type="text/javascript">
    var impactBase = '<%=Home%>';
    var impactService=impactBase+'ImpactService?';

    $( document ).ready(function() {
      console.log("ready");
    	var element = $("#joblist");
    	
    	/* Set loading gif */
    	element.html('<div class="c4i-joblist-loader"></div>');
    	var currentList;
    	
    	/* Generate table based on json data callback */
    	var httpCallback = function(data){
    	  if(data.error){
    	    var html="An error occured";
    	    html+="<hr>"+data.error+"</hr>";
    	    if(data.exception){
    	     html+="<hr>"+data.exception+"</hr>";
    	    }
    	    element.html(html);
    	    return;
    	  }
    	  currentList=data;
    	  
    	  if(data.jobs.length==0){
    	    element.html("No jobs available");
    	    return ;
    	  }
    	  //console.log("Dupdateing "+data.jobs.length);
    	  var html="<table class=\"c4i-joblist-table\"><tr><th>Created at</th><th>Name</th><th>Status location</th><th>Progress</th><th>View</th><th>X</th></tr>";
    	  for(var j=0;j<data.jobs.length;j++){
    	    html+="<tr name=\""+data.jobs[j].processid+"\">";
    	    html+="<td>"+data.jobs[j].creationdate+"</td>";
    	    html+="<td>"+data.jobs[j].wpsid+"</td>";
    	    html+="<td><a href=\""+data.jobs[j].statuslocation+"\">"+data.jobs[j].processid+"</a></td>";
    	    html+="<td>"+data.jobs[j].progress+"</td>";
    	    html+="<td><button class=\"c4i-joblist-viewbutton\" name=\""+data.jobs[j].processid+"\">"+"view"+"</button></td>";
    	    html+="<td><button class=\"c4i-joblist-deletebutton\" name=\""+data.jobs[j].processid+"\">"+"X"+"</button></td>";
    	    html+="</tr>";
    	  }
    	  html+="</table>";
    	  //console.log("Clearing element");
    	  element.empty();
    	  //console.log("setting html");
    	  
    	  element.html(html);
    	  //console.log("setting html done");
    	  /* View Job details buttons */
    	  element.find(".c4i-joblist-viewbutton").button().attr('onclick','').click(function(t){
    	    var found = false;
    	    for(var j=0;j<data.jobs.length;j++){
    	      if(data.jobs[j].processid == this.name){
    	        found = data.jobs[j];
    	      }
    	    }
    	    if(found!==false){
            //showStatusReport(found);
            console.log(found);
            var el=jQuery('<div/>');
            renderProcessingInterface(
              {
                element:el,
                wpsservice:'<%=Configuration.getHomeURLHTTPS()%>/WPS?',
                identifier:found.wpsid,
                inputdata:found.wpspostdata,
                statuslocation:found.statuslocation,
                adagucservice:"http://climate4impact.eu/impactportal/adagucserver?",
                adagucviewer:"http://climate4impact.eu//impactportal/adagucviewer/",
                dialog:true
              }
            );
            
    	    }else{
    	      alert("Process ID "+this.name+" not found");
    	    }
        });
    	  
    	  
    	  
    	  /* Remove Job from list buttons */
        element.find(".c4i-joblist-deletebutton").button().attr('onclick','').click(function(t){
          var foundIndex =-1;
          for(var j=0;j<currentList.jobs.length;j++){
            if(currentList.jobs[j].processid==this.name){
              foundIndex=j;
              break;
            }
          }
          if(foundIndex==-1)return;
          currentList.jobs.splice(foundIndex, 1);
          adjustNumberOfJobsDisplayedInMenuBar({'numproducts':currentList.jobs.length});//Located in ImpactJS.js
          $("#joblist").find('tr[name=\"'+this.name+'\"]').remove();
                    
           $.ajax({
             url: impactService+"service=processor&request=removeFromList&id="+this.name,
             crossDomain:true,
             dataType:"jsonp"
           }).done(function(d) {
           }).fail(function() {
           }).always(function(){
           });
        });
    		console.log("done");
    	};

    	
    	var getJobListFromServer = function(){
    	  console.log("getJobListFromServer");
    	 /* Make the AJAX call to obtain the joblist */
		   	$.ajax({
	   	    url: impactService+"service=processor&request=getProcessorStatusOverview",
		      crossDomain:true,
	   	    dataType:"jsonp"
	   	  }).done(function(d) {
	   	    try{
	   	      httpCallback(d);
	   	     
	   	    }catch(e){
	   	      element.html("Error obtaining joblist: "+e);
	   	    };
	 	    }).fail(function() {
	 	      alert("Failed for "+arg);
	 	    }).always(function(){
	 	      
	 	    });
    	};
    	
    	function loop(){
    	  getJobListFromServer();
 	      setTimeout(function(){loop();},10000);
    	};
    	loop();
    	
    });
    
    </script>
  </head>
  
  <body>
		<jsp:include page="../header.jsp" />
		<jsp:include page="loginmenu.jsp" />
		<div class="impactcontent">
		<div class="breadcrumb"><a href="login.jsp">Account</a> » Monitor jobs </div>
		<h1>Submitted processing jobs</h1>
		<%
			ImpactUser user = null;
			try{
				  user = LoginManager.getUser(request);
			}catch(Exception e){
			}
				
			if (user==null){
		%>
			<p>You are not logged in, please go to the <a href="/impactportal/account/login.jsp">login page</a> and log in</p>
		<%
		}else{
			out.println("<div id='joblist'>loading...</div>");
		}
		%>
	</div>
	<jsp:include page="../footer.jsp" />
  </body>
</html>