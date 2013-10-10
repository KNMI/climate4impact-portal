<%@ page language="java" import="java.util.*" %>
<html>
<body>
Session attributes:
<%


for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements(); ) {     
    String attribName = (String) e.nextElement();
    Object attribValue = session.getAttribute(attribName);

%>
<BR><%= attribName %> - <%= attribValue %>

<%
}
%>
</body>
</html>