<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="impactservice.* "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
   <jsp:include page="../includes.jsp" />
   <script type="text/javascript" src="/impactportal/account/js/admin.js"></script>
    <style>
      .c4i-admin-table-smallheader th{
        font-size:12px !important;
       
      }
      .c4i-admin-table-smallheader tr th{
        padding:2px 5px 2px 5px !important;;
      }
      .c4i-admin-table-smallheader td{
        font-size:10px !important;
        padding:2px !important;
      }
      .c4i-admin-takeuser{
        text-decoration:underline;
        color:blue;
        cursor:pointer;
      }
     </style>
  </head>
  <body>
	<jsp:include page="../header.jsp" />

		<jsp:include page="../account/loginmenu.jsp" />
		<div class="impactcontent">
		<h1>Admin page</h1>
		
		<h2>Server information:</h2>
		
		<div id="c4i_userinfo"></div>
		
		<a href="/impactportal/ImpactService?service=admin&request=getusers">Get user overview in JSON</a>
		<!--<h2>OAuth2 test:</h2>
		<div class="textstandardleft">
		<a href="/impactportal/account/OAuth2.jsp">OAuth2 test</a>
		</div>-->
		</div>
	<!-- /Contents -->
	<jsp:include page="../footer.jsp" />
  </body>
</html>