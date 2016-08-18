<%@page import="tools.HTTPTools"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8" import="impactservice.LoginManager"%><%
	LoginManager.logout(request,response);
	try{
		String redirectURL = HTTPTools.getHTTPParam(request, "redirect");
		response.sendRedirect(redirectURL);
	}catch(Exception e){
		response.sendRedirect("login.jsp");
	}
%>