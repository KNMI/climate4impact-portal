<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="impactservice.* "%>
		<%
			String removeId=request.getParameter("remove");
				GenericCart shoppingCart = null;
				try{
			shoppingCart = LoginManager.getUser(request).getShoppingCart();
			try{
				if(removeId!=null){
					shoppingCart.removeDataLocator(removeId);
				}
			}catch(Exception e){
				tools.DebugConsole.printStackTrace(e);
			}
			
				}catch(Exception e){
				}
		%>
	
 
		<div class="impactcontent">
		<h1>Basket</h1>
		<%
		
	
		
		
		if(shoppingCart==null){
			%>
			<p>You are not logged in, please go to the <a href="../login.jsp">login page</a> and log in</p>
			<%
		}else{
			out.println(GenericCart.CartPrinters.showDataSetList(shoppingCart, request));
		}
		%>
		
		<!-- 
  	    <div class="cmscontent">
  		<%try{out.print(DrupalEditor.showDrupalContent("?q=basket",request));}catch(DrupalEditor.DrupalEditorException e){out.print(e.getMessage());response.setStatus(e.getCode());}%>
  		</div>
  		 -->
  		
		</div>