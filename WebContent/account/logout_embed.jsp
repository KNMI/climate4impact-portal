<%
session.setAttribute("openid_identifier",null);
session.setAttribute("user_identifier",null);
session.setAttribute("email",null);
response.sendRedirect("login_embed.jsp");
%>