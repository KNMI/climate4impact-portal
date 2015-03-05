<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8" import="impactservice.LoginManager"%><%
	LoginManager.logout(request);
	response.sendRedirect("login.jsp");
%>