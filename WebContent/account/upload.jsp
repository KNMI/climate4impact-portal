<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="impactservice.ImpactService,tools.Debug,impactservice.LoginManager,impactservice.ImpactUser,impactservice.MessagePrinters"
	import="javax.servlet.http.*" import="javax.servlet.http.Cookie"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="stylesheet" href="jquery.fileupload.css"></link>

<link rel="stylesheet" href="bootstrap.css"></link>
<jsp:include page="../includes-ui.jsp" />

<!-- <script src="bootstrap.min.js"></script>-->
<script src="js/jquery.iframe-transport.js"></script>
<script src="js/jquery.fileupload.js"></script>
</head>
<body>
	<jsp:include page="../header.jsp" />
       
	<!-- Contents -->
	<%
		String Home = "/impactportal/";
		ImpactUser user = null;
		try {
			user = LoginManager.getUser(request);
		} catch (Exception e) {
		}
		if(user == null){
		    response.sendRedirect(Home+"/account/login.jsp");
		}
		if (user != null) {
%>
<script>
$(".impactcontent").hide();
$( document ).ready(function () {
  	$(".impactcontent").show();
    'use strict';
    $('#showwhenuploading').hide();
    // Change this to the location of your server-side upload handler:
    var url = '/impactportal/FileUploadHandler';
    $('#fileupload').fileupload({
        url: url,
        dataType: 'json',
        done: function (e, data) {
            //console.log("done");
            $(".fileinput-button").show();
        	$('#showwhenuploading').show();
            $.each(data.result.files, function (index, file) {
            	if(!file.error){
            		$('#uploadedfiles ul').append('<li>'+file.name+'</li>');
            	}else{
            		$('#uploadedfileserror ul').append('<li>FAILED: '+file.name+", "+file.error+'</li>');
            	}
            });
            $("#fileupload_info").html("");
            $('#showwhenuploading').hide();
        },
        progressall: function (e, data) {
            //console.log("progressall");
        	var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .progress-bar').css(
                'width',
                progress + '%'
            );
            $("#fileupload_info").html("Uploading ...");
        },
        start: function(){
          //console.log("fileuploadstart");
          $('#showwhenuploading').show();
          $(".fileinput-button").hide();
          $("#fileupload_info").html("Start Upload ...");
          $('#progress .progress-bar').css('width','1%');
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');
});
</script>
	<jsp:include page="loginmenu.jsp" />
	<div class="impactcontent">
       <h1>Upload file</h1>
       <div class="textstandardleft">
    	 <!-- The fileinput-button span is used to style the file input field as button -->
    <span class="btn btn-success fileinput-button">
        <i class="glyphicon glyphicon-plus"></i>
        <span>Select file(s)</span>
        <!-- The file input field used as target for the file upload widget -->
        <input id="fileupload" type="file" name="files[]" multiple ></input>
        
    </span>
   
    <br/>
    <!-- The global progress bar -->
    <br/>
    <div id="showwhenuploading">
    <span id="fileupload_info"></span>
    <div id="progress" class="progress">
        <div class="progress-bar progress-bar-success"></div>
    </div>
    </div>
    <div class="fileinput-button">
    <b>The following files have been uploaded:</b><br/>
    <div id="uploadedfiles" class="files">
    <ul></ul>	
	</div>
	<div id="uploadedfileserror" class="uploadedfileserror">
    <ul></ul>	
	</div>
	
	

	<br/>
	<div class="fileinput-button">
	    <b>Actions:</b>  
	       <ul>
	       <li><a href="basket.jsp">Go to your basket</a></li>
	       <li><a href="../help/contactexpert.jsp">Having problems?</a></li>
	       </ul>
	       
    </div>          
    
    </div>
        </div>
     </div>
    
 <%} %>
    
<jsp:include page="../footer.jsp" />
</body>
</html>