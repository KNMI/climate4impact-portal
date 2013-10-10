package impactservice;

import impactservice.SessionManager.SearchSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
//import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.DebugConsole;
import tools.HTTPTools;
import tools.JSONMessageDecorator;

public class BasicSearch {
	   static public void handleBasicSearchRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
	   /*
	    * Called by basicSearch.js; switch to init or to search
	    */
 
	      
			String mode = null;
			try {
				mode = HTTPTools.getHTTPParam(request,"mode");
			} catch (Exception e) {
				DebugConsole.errprintln("KVP mode not found: "+request.getQueryString());
			}
			if(mode==null)return;
			if(mode.equals("init")){
				modeInit(request,response,errorResponder);
			}
			if(mode.equals("search")){
				modeSearch(request,response,errorResponder);
			}
			
	    }
	   
	   static private void modeInit(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
		   try{
			   /*
			    *  Initialization method to get the variable list (short names for now) from VERC
			    */
	    		response.setContentType("application/json");

				//JSONObject resultJSON = VercSearchInterface.getCategoriesForQuery("");
	    	JSONObject resultJSON = DeprecatedVercSearchInterface.getVercQuery("",0,0,true,true,false);
			    //Convert the JSON object to a string and return it to the client
				PrintWriter out1 = ImpactService.getPrintWriter(response);
				String result=resultJSON.toString();
				//DebugConsole.println(result);
				out1.print(result);
	    	
	    	}catch(Exception e){
	    		JSONObject error = new JSONObject();
				try {
					DebugConsole.errprintln("Error occured in handleBasicSearchRequest: "+e.getMessage());
					error.put("error",e.getMessage());
				} catch (JSONException e2) {
					return;
				}
				PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e1) {DebugConsole.errprint(e1.getMessage());return;}
				out1.print(error.toString());
				return;
	    	}
	   }
	   
	   static private void modeSearch(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
		   try{
			   /*
			    * Search methode to search on the VERC based on the settings made in basicsearch.jsp 
			    */
		     HttpSession session = request.getSession();
		     SearchSession searchSession=(SearchSession) session.getAttribute("searchsession");
		     if(searchSession==null){ searchSession = new SearchSession();session.setAttribute("searchsession",searchSession);}

			    // get values from fields  
	    		response.setContentType("application/json");
				String variable=HTTPTools.getHTTPParam(request,"variable");
				String whenFrom=HTTPTools.getHTTPParam(request,"whenFrom");
				String whenTo=HTTPTools.getHTTPParam(request,"whenTo");
				String frequency=HTTPTools.getHTTPParam(request,"frequency");
				String where=HTTPTools.getHTTPParam(request,"where");						
				
				int requestedPageNr = 1;
				int pageLimit = 10;
				try{
					String requestedPage=HTTPTools.getHTTPParam(request,"page");
					requestedPageNr = Integer.parseInt(requestedPage);
					String limit=HTTPTools.getHTTPParam(request,"limit");
					pageLimit = Integer.parseInt(limit);
				}catch(Exception e){
					DebugConsole.errprintln(e.getMessage());
				}
				searchSession.variable=variable;
        searchSession.from=whenFrom;
        searchSession.to=whenTo;
        searchSession.time_frequency=frequency;
        searchSession.basicsearchpagenr=requestedPageNr;
        searchSession.pagelimit=pageLimit;
        searchSession.where=where;
        
        
				// replace 'T' by space to conform to VERC formatting
				String tc_start = whenFrom.replace('T', ' ');
				String tc_end = whenTo.replace('T', ' ');
								
				/*
				 * Build query based on input fields
				 */
				String query = "search?datatype=file&";
				if (!variable.isEmpty()) {
					query = query + "variable="+URLEncoder.encode(variable,"UTF-8")+"&";
				}
				if (!frequency.isEmpty()) {
				  if(frequency.equals("all")==false){
				    query = query +"frequency="+URLEncoder.encode(frequency,"UTF-8")+"&";
				  }
				}
				if (!tc_start.isEmpty()) {
					query = query + "tc_start="+URLEncoder.encode(tc_start,"UTF-8")+"&";
				}
				if (!tc_end.isEmpty()) {
					query = query + "tc_end="+URLEncoder.encode(tc_end,"UTF-8")+"&";
				}
				//DebugConsole.println("-------query-----> "+query);
								
				//JSONObject resultJSON = VercSearchInterface.makeCached_vercsearch_RequestAsJson(query, requestedPageNr,pageLimit,false);
				JSONObject resultJSON = DeprecatedVercSearchInterface.getVercQuery(query,requestedPageNr,pageLimit,false,false,true);  
				
		        //Convert the JSON object to a string and return it to the client
				PrintWriter out1 = ImpactService.getPrintWriter(response);
				String result=resultJSON.toString();
				//DebugConsole.println(result);
				out1.print(result);
	    	
	    	}catch(Exception e){
	    		JSONObject error = new JSONObject();
				try {
					DebugConsole.errprintln("Error occured in handleBasicSearchRequest: "+e.getMessage());
					error.put("error","No matches: "+e.getMessage());
				} catch (JSONException e2) {
					return;
				}
				PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e1) {DebugConsole.errprint(e1.getMessage());return;}
				out1.print(error.toString());
				return;
	    	}
	   }
}
