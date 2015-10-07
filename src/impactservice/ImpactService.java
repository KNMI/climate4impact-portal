package impactservice;

import impactservice.SessionManager.SearchSession;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ogcservices.AdagucServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.JSONMessageDecorator;
import tools.JSONResponse;
import wps.WebProcessingInterface;
//import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;


/**
 * Servlet implementation class ImpactService
 */
public class ImpactService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImpactService() {
        super();
    }

   

    private void handleProcessorRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
  		/*if(request.getQueryString()!=null){
  		  DebugConsole.println("SERVICE PROCESSOR: "+request.getQueryString());
  		}*/
  		
  		String requestStr=request.getParameter("request");
  		if(requestStr!=null){requestStr=URLDecoder.decode(requestStr,"UTF-8");}else{errorResponder.printexception("urlStr="+requestStr);return;}
  		
  		Debug.println("PROCESSOR REQUEST="+requestStr);
  		
  		/**
  		 * Remove processor from status list
  		 */
  		if(requestStr.equals("removeFromList")){
		 
        GenericCart jobList = null;
        try{
          response.setContentType("text/plain");
          String procId=request.getParameter("id");
          if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
          jobList = LoginManager.getUser(request,response).getProcessingJobList();
          jobList.removeDataLocator(procId);
          response.getWriter().println("{\"numproducts\":\""+(jobList.getNumProducts())+"\"}");
        }catch(Exception e){
          response.getWriter().println(e.getMessage());
        }
      
  		}
  		
  		/**
  		 *  getprocessor status list as HTML
  		 */
  		if(requestStr.equals("getProcessorStatusOverview")){
  		  GenericCart jobList = null;
  	    try{
  	      response.setContentType("text/html");
  	      jobList = LoginManager.getUser(request,response).getProcessingJobList();
  	      String html = GenericCart.CartPrinters.showJobList(jobList,request);
  	      response.getWriter().println(html);
  	    }catch(Exception e){
  	      response.getWriter().println(e.getMessage());
  	    }
  		}
  		
  	  /**
       * Get processor list as JSON
       */
  		if(requestStr.equals("getProcessorList")){
  			PrintWriter out1 = null;
      		response.setContentType("application/json");
  			try {
      			out1 = response.getWriter();
      		} catch (IOException e) {
      			Debug.errprint(e.getMessage());
      			return;
      		}
  			Vector<WebProcessingInterface.ProcessorDescriptor> listOfProcesses = WebProcessingInterface.getAvailableProcesses(request);
  			if(listOfProcesses == null){
  			  out1.print("{\"error\":\"No processes available\"}");
  			  return;
  			}
  			
  			try {
  				JSONArray a = new JSONArray();
  				for(int j=0;j<listOfProcesses.size();j++){
  					JSONObject record = new JSONObject();
  					record.put("name",listOfProcesses.get(j).getTitle());
  					//record.put("id","mpa");
  					record.put("id",listOfProcesses.get(j).getIdentifier());
  					a.put(record);
  					
  					record.put("abstract",listOfProcesses.get(j).getAbstract());
  					a.put(record);
  				//	out.println(listOfProcesses.get(j).getTitle());
  				}
  				JSONObject myJSONObject = new JSONObject();
      			myJSONObject.put("processors",a);  
      			out1.print(myJSONObject.toString());
      		} catch (JSONException e) {
      			out1.print(e.getMessage());
      			return;
      		}
      		
  			
  		}
  		/**
  		 * Describe processor
  		 */
  		if(requestStr.equals("describeProcessor")){
  			String procId=request.getParameter("id");
  			if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
  			 try {
  			    response.setContentType("application/json");
            response.getWriter().print(WebProcessingInterface.describeProcess(request,procId).toString());
          } catch (Exception e) {
            response.getWriter().print(e.getMessage());
            return;
         }
  		
  		}
      /**
       * Execute processor
       */
  		if(requestStr.equals("executeProcessor")){
        String procId=request.getParameter("id");
        if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
        String dataInputs=request.getParameter("dataInputs");
        if(dataInputs!=null){dataInputs=URLDecoder.decode(dataInputs,"UTF-8");}else{errorResponder.printexception("dataInputs="+dataInputs);return;}
         try {
            response.setContentType("application/json");
            response.getWriter().print(WebProcessingInterface.executeProcess(procId,dataInputs,request,response));
          } catch (Exception e) {
            Debug.errprintln(e.getMessage());
            try {
              JSONObject error = new JSONObject();
              error.put("error",e.getMessage());
              response.getWriter().print(error.toString());
            } catch (JSONException e1) {}

            return;
         }
      
      }
  		
  		/**
       * Monitor processor
       */
      if(requestStr.equals("monitorProcessor")){
        String procId=request.getParameter("id");
        if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
        String statusLocation=request.getParameter("statusLocation");
        if(statusLocation!=null){statusLocation=URLDecoder.decode(statusLocation,"UTF-8");}else{errorResponder.printexception("statusLocation="+statusLocation);return;}
         try {
            response.setContentType("application/json");
            response.getWriter().print(WebProcessingInterface.monitorProcess(statusLocation,request).toString());
          } catch (Exception e) {
            response.getWriter().print(e.getMessage());
            return;
         }
      }
      /**
       * Get Image from statuslocation
       */
      if(requestStr.equals("getimage")){
        
        String statusLocation=request.getParameter("statusLocation");
        if(statusLocation!=null){statusLocation=URLDecoder.decode(statusLocation,"UTF-8");}else{errorResponder.printexception("statusLocation="+statusLocation);return;}
        String outputId=request.getParameter("outputId");
        if(outputId!=null){outputId=URLDecoder.decode(outputId,"UTF-8");}else{errorResponder.printexception("outputId="+outputId);return;}
        ServletOutputStream out = response.getOutputStream();
         try {
           out.write(WebProcessingInterface.getImageFromStatusLocation(statusLocation,outputId));//.print(ProcessorRegister.getImageFromStatusLocation(statusLocation,outputId));
          } catch (Exception e) {
            out.print(e.getMessage());
            return;
         }
      
      }
      
      /**
       * Get HTML from statuslocation
       */
      if(requestStr.equalsIgnoreCase("getstatusreport")){
        
        String statusLocation=request.getParameter("statusLocation");
        if(statusLocation!=null){statusLocation=URLDecoder.decode(statusLocation,"UTF-8");}else{errorResponder.printexception("statusLocation="+statusLocation);return;}
        
       
        ServletOutputStream out = response.getOutputStream();
         try {
           response.setContentType("text/html");
           out.write(WebProcessingInterface.generateReportFromStatusLocation(statusLocation).getBytes());//.print(ProcessorRegister.getImageFromStatusLocation(statusLocation,outputId));
          } catch (Exception e) {
            out.print(e.getMessage());
            return;
         }
      
      }
    }
    
    String buildHTML(JSONArray array,String root,int oddEven,String openid){
      if(array == null)return null;
      StringBuffer html = new StringBuffer();
      try {
        //Try to get the catalogURL 
        
        for(int j=0;j<array.length();j++){
          
          String openDAPURL=null;
          String httpURL=null;
          String hrefURL=null;
          String catalogURL=null;
          String nodeText = null;
          String fileSize = "";
          JSONObject a=array.getJSONObject(j);
          nodeText = a.getString("text");
          try{openDAPURL = a.getString("OPENDAP");}catch (JSONException e) {}
          try{httpURL = a.getString("HTTPServer");}catch (JSONException e) {}
          try{catalogURL = a.getString("catalogURL");}catch (JSONException e) {}
          try{hrefURL = a.getString("href");}catch (JSONException e) {}
          try{fileSize = a.getString("dataSize");}catch (JSONException e) {}

          oddEven = 1-oddEven;
          if(oddEven==0){
            //html+="<tr class=\"even\"><td>";
            html.append("<tr class=\"even\"><td>");
          }else{
            //html+="<tr class=\"odd\"><td>";
            html.append("<tr class=\"odd\"><td>");
          }
          if(hrefURL == null){
           // html+=root+nodeText+"<br/>";
            html.append(root+nodeText+"<br/>");
          }else{
            //html+=root+"<a href=\""+hrefURL+"\">"+nodeText+"</a>";
            html.append(root+"<a href=\""+hrefURL+"\">"+nodeText+"</a>");
          }
          
         // html+="<td>"+dataSize+"</td>";
          html.append("<td>"+fileSize+"</td>");
          //html.append("<td>");html.append(dataSize);html.append("</td>");
          
          String dapLink = "";
          String httpLink = "";
          
          /*
           * Only show opendap when it is really advertised by the server.
           */ if(openDAPURL == null && httpURL !=null){
            openDAPURL = httpURL.replace("fileServer", "dodsC")+"#";
          }
          
          if(openDAPURL!=null){
           
              //dapLink="<a href=\"/impactportal/data/datasetviewer.jsp?dataset="+URLEncoder.encode(openDAPURL,"UTF-8")+"\">view</a>";
              dapLink="<a href=\"\" onclick=\"renderFileViewer({url:'"+openDAPURL+"'});\">view</a>";
            
          }
          if(httpURL!=null){
            if(openid!=null){
              httpLink="<a href=\""+httpURL+"\" target=\"_blank\" onclick=\"window.open('"+httpURL+"?openid="+openid+"')\">download</a>";
            }else{
              httpLink="<button href=\"window.open('"+httpURL+"')\">download</button>";
            }
          }
          //html+="</td><td>"+dapLink+"</td><td>"+httpLink;
          html.append("</td><td>");html.append(dapLink);html.append("</td><td>");html.append(httpLink);
          if(httpURL == null && openDAPURL == null && catalogURL == null){
            //html+="</td><td>-";  
            html.append("</td><td>");
          }else{
            //html+="</td><td><span onclick=\"postIdentifierToBasket({id:'"+nodeText+"',HTTPServer:'"+httpURL+"',OPENDAP:'"+openDAPURL+"',catalogURL:'"+catalogURL+"'});\" class=\"shoppingbasketicon\"/>";
            html.append("</td><td><span onclick=\"basket.postIdentifiersToBasket({id:'"+nodeText+"',HTTPServer:'"+httpURL+"',OPENDAP:'"+openDAPURL+"',catalogURL:'"+catalogURL+"',"+"filesize:'"+fileSize+"'});\" class=\"shoppingbasketicon\"/>\n");
          }
          
          
          //html+="</td></tr>";
          html.append("</td></tr>");
          try{
            JSONArray children = a.getJSONArray("children");
            //html+=buildHTML(children,root+"&nbsp;&nbsp;&nbsp;&nbsp;",oddEven);
            html.append(buildHTML(children,root+"&nbsp;&nbsp;&nbsp;&nbsp;",1-oddEven,openid));
          } catch (JSONException e) {
          }
        }
      } catch (JSONException e) {
      }
      return html.toString();
    }
    
    
    private void handleCatalogBrowserRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
  		Debug.println("SERVICE CATALOGBROWSER: "+request.getQueryString());
      String format= request.getParameter("format");
      if(format==null)format="";
  	  try{
  	    String variableFilter="",textFilter = "";
  	    try{
  	      variableFilter = HTTPTools.getHTTPParam(request, "variables");
  	    }catch(Exception e){
  	    }
        
  	    try{
  	      textFilter = HTTPTools.getHTTPParam(request, "filter");
  	    }catch(Exception e){
  	    }
        
        HTTPTools.validateInputTokens(variableFilter);
        HTTPTools.validateInputTokens(textFilter);
        
  	    Debug.println("Starting CATALOG: "+request.getQueryString());
  		  long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
  		  JSONArray treeElements = null;
  		  try{
  		    treeElements = THREDDSCatalogBrowser.browseThreddsCatalog(request, variableFilter,textFilter); 
  		  }catch(Exception e){
  		    response.setContentType("text/html");
  		    response.getWriter().print("Unable to read catalog: "+e.getMessage());
          return;
  		  }
  		  
        long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
        Debug.println("Finished CATALOG: "+" ("+(stopTimeInMillis-startTimeInMillis)+" ms) "+request.getQueryString());

        if(treeElements == null){
          response.setContentType("text/html");
          response.getWriter().print("Unable to read catalog");
          return;
        }
          
  		  if(format.equals("text/html")){
          response.setContentType("text/html");
          String html="";
          /*for(int j=0;j<treeElements.getJSONObject(0).getJSONArray("children").length();j++){
            html+="j="+treeElements.getJSONObject(0).getString("text")+"<br/>"; 
          } */
        
          try{
            Vector<String> availableVars = new Vector<String>();
            try{
              JSONArray variablesToChoose = treeElements.getJSONObject(0).getJSONArray("variables");
              for(int j=0;j<variablesToChoose.length();j++){
                availableVars.add(variablesToChoose.getJSONObject(j).getString("name"));
                //DebugConsole.println("a "+variablesToChoose.getJSONObject(j).getString("name"));
              }
            }catch(Exception e){
              
            }
            
            
            Debug.println("variableFilter: '"+variableFilter+"'");
            Debug.println("textFilter: '"+textFilter+"'");
            html+="<div id=\"variableandtextfilter\"><form id=\"varFilter\" class=\"varFilter\">";
            if(availableVars.size()>0){
              html+="<b>Variables:</b>";
              for(int j=0;j<availableVars.size();j++){
                if(j!=0)html+="&nbsp;";
                String checked="";//checked=\"yes\"";
                if(variableFilter.length()>0){
                  checked="";
                  if(availableVars.get(j).matches(variableFilter))checked="checked=\"yes\"";
                }
                html+="<input type=\"checkbox\" name=\"variables\" id=\""+availableVars.get(j)+"\" "+checked+">"+availableVars.get(j);
              }
              html+="<hr/>";
            }
            html+="<b>Text filter:</b> <input type=\"textarea\" class=\"textfilter\" id=\"textfilter\" value=\""+textFilter+"\" />";
            html+="&nbsp; <input style=\"float:right;\" type=\"button\" value=\"Go\" onclick=\"setVarFilter();\"/>";
            
            html+="</form></div>";
          }catch(Exception e){
            e.printStackTrace();
          }
          
          html += "<div id=\"datasetfilelist\"><table class=\"basket\">";
          html+="<tr><td width=\"100%\" class=\"basketheader\"><b>Title</b></td><td class=\"basketheader\"><b>Size</b></td><td class=\"basketheader\"><b>OPENDAP</b></td><td class=\"basketheader\"><b>HTTP</b></td><td class=\"basketheader\"><b>Basket</b></td></tr>";
          
          long startTimeInMillis1 = Calendar.getInstance().getTimeInMillis();
          
          String openid = null;
          try{
            openid= LoginManager.getUser(request).getOpenId();
          }catch(Exception e){            
          }
          html+=buildHTML(treeElements,"",0,openid)+"</table></div>";
          long stopTimeInMillis1 = Calendar.getInstance().getTimeInMillis();
          Debug.println("Finished building HTML with length "+html.length() +" in ("+(stopTimeInMillis1-startTimeInMillis1)+" ms)");
  		    response.getWriter().print(html);
  		    Debug.println("Catalog request finished.");
  		  }else{
          response.setContentType("application/json");
  		    response.getWriter().print(treeElements.toString());
  		  }
  	  }catch(WebRequestBadStatusException e){
  	    if(format.equals("text/html")){
  	      response.setContentType("text/html");
          String html="";
          if(e.getStatusCode() == 404){
            html="Catalog not found! (404)";
          }else{
            html=e.getMessage();
          }
          response.getWriter().print(html);
  	    }else{
    	    try {
            JSONObject error = new JSONObject();
            error.put("error",e.getMessage());
            response.getWriter().print(error.toString());
          } catch (JSONException e1) {}
  	    }
  	  
  	  
      } catch (Exception e2) {
        e2.printStackTrace();
      
        if(format.equals("text/html")){
          response.setContentType("text/html");
          String html="";
          html=e2.getMessage();
          html+="<form><input type=button value=\"Refresh\" onClick=\"history.go()\"></form>";
          response.getWriter().print(html);
        }else{
          try {
            JSONObject error = new JSONObject();
            error.put("error",e2.getMessage());
            response.getWriter().print(error.toString());
          } catch (JSONException e1) {}
        }
      }
    }
    
   
  private void handleSearchRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
		response.setContentType("application/json");
		Debug.println("SERVICE SEARCH: "+request.getQueryString());
		//Params: URLEncoded verquery, pagenumber, pagesize.
		// Mode=distinct|search
		// mode=distinct: category=any
		String mode=null;
		
		try{
	    HttpSession session = request.getSession();
		  SearchSession searchSession=(SearchSession) session.getAttribute("searchsession");
      if(searchSession==null){ 
        Debug.println("Creating new searchsession");
        searchSession = new SearchSession();session.setAttribute("searchsession",searchSession);
      }
      
			mode=HTTPTools.getHTTPParam(request,"mode");
		
		

/*			if(mode.equals("distinct")){
				DebugConsole.println("SERVICE.SEARCH.DISTINCT");
				String query="";
				try{
					query=HTTPTools.getHTTPParam(request,"query");
				}catch(Exception e){}
				//JSONObject resultJSON = VercSearchInterface.getCategoriesForQuery(query);
				JSONObject resultJSON = VercSearchInterface.getVercQuery(query,0,0,true,false,false);
				//DebugConsole.println(resultJSON.toString().replaceAll("\\[", "\n["));
				//DebugConsole.println("Query : "+query);
				//DebugConsole.println("Result: "+resultJSON.toString());
				PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e) {DebugConsole.errprint(e.getMessage());return;}
	    		out1.print(resultJSON.toString());
			}*/
			 String dataSetType = "Dataset";
       try{
         String type = HTTPTools.getHTTPParam(request,"type");
         if(type!=null){
           if(type.equals("file"))dataSetType="File";
           if(type.equals("aggregation"))dataSetType="Aggregation";
         }
       }catch(Exception e){}
       
			if(mode.equals("search")||mode.equals("distinct")){
				//DebugConsole.println("SERVICE.SEARCH.SEARCH");
				String query="";
				try{
				  query=HTTPTools.getHTTPParam(request,"query");
				}catch(Exception e){}
				
				int pageSize=1;//Session manager
				int currentPage=1;

				try{String limitStr=HTTPTools.getHTTPParam(request,"limit");pageSize=Integer.parseInt(limitStr);}catch(Exception e){}
				try{String pageStr=HTTPTools.getHTTPParam(request,"page");currentPage=Integer.parseInt(pageStr);}catch(Exception e){}
				String queryStr="";try{ queryStr=HTTPTools.getHTTPParam(request,"query");}catch(Exception e){}

				boolean includeFacets = false;
				String inclFacets = null;
				try{
				  inclFacets = HTTPTools.getHTTPParam(request,"includefacets");
  				if(inclFacets.equals("true")){
  				  includeFacets = true;
  				}
  				if(inclFacets.equals("false")){
  				  includeFacets = false;
          }
        }catch(Exception e){
          
        }

				Debug.println("inclFacets: "+includeFacets);
		
				searchSession.variable=HTTPTools.getKVPItemDecoded(queryStr,"variable");
        searchSession.time_frequency=HTTPTools.getKVPItemDecoded(queryStr,"time_frequency");
        searchSession.institute=HTTPTools.getKVPItemDecoded(queryStr,"institute");
        searchSession.experiment=HTTPTools.getKVPItemDecoded(queryStr,"experiment");
        searchSession.model=HTTPTools.getKVPItemDecoded(queryStr,"model");
        searchSession.realm=HTTPTools.getKVPItemDecoded(queryStr,"realm");
        
        searchSession.from=HTTPTools.getKVPItemDecoded(queryStr,"tc_start");
        searchSession.to=HTTPTools.getKVPItemDecoded(queryStr,"tc_end");
        searchSession.advancedsearchpagenr=currentPage;
        searchSession.pagelimit=pageSize;
		
        try{
          searchSession.where=HTTPTools.getHTTPParam(request,"where");
        }catch(Exception e){}
        
       
        
        
				//The ext pagingtoolbar store starts pagecount with 1
								
				
				//JSONObject resultJSON = VercSearchInterface.makeCached_vercsearch_RequestAsJson(query,currentPage,pageSize) ;
	//			JSONObject resultJSON = VercSearchInterface.getVercQuery(query,currentPage,pageSize,true,false,true);
        boolean includeSearchResults = true;
        if(mode.equals("distinct")){
          includeSearchResults = false;
        }
        JSONObject resultJSON = null;
        resultJSON = ESGFSearch.doCachedESGFSearchQuery(query,currentPage,pageSize,includeFacets,false,includeSearchResults,dataSetType);
        
//				DebugConsole.println("pageSize     : "+pageSize);
//				DebugConsole.println("currentPage  : "+currentPage);
				//DebugConsole.println("Start : "+start);
				
				Debug.println("Query : "+query);
//				DebugConsole.println("ResultLength: "+resultJSON.length());
				PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e) {Debug.errprint(e.getMessage());return;}
				response.setContentType("application/json");
				out1.print(resultJSON.toString());
			}
			
			if(mode.equals("getfacet")){
			  String facet=HTTPTools.getHTTPParam(request,"facet");
			  
			  String queryStr="";try{ queryStr=HTTPTools.getHTTPParam(request,"query");}catch(Exception e){}
			  Debug.println("Facet to query = "+facet);
			  Debug.println("Facet queryStr = "+queryStr);

			  JSONObject resultJSON = null;
        resultJSON = ESGFSearch.getFacetForQuery(facet,queryStr+"&type="+dataSetType);
 	      PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e) {Debug.errprint(e.getMessage());return;}
        response.setContentType("application/json");
        out1.print(resultJSON.toString());
			}
		}catch(Exception e){
      Debug.printStackTrace(e);
			Debug.errprintln("Exception catched "+e.getMessage());

			JSONObject error = new JSONObject();
			try {
				error.put("error","ESGF Query Error: "+e.getMessage());
			} catch (JSONException e2) {
				return;
			}
			PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e1) {Debug.errprint(e1.getMessage());return;}
			out1.print(error.toString());
			return;
		}
		
    }
    
 
   /* private void handleDataRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws IOException  {
      String fileId=null;
      String requestStr=null;
      
      try {
        requestStr=HTTPTools.getHTTPParam(request,"request");
      } catch (Exception e3) {
        requestStr="";
      }
      
      try {
        fileId=HTTPTools.getHTTPParam(request,"file");
      } catch (Exception e3) {
        fileId="";
      }
      
      DebugConsole.print("Data request received "+requestStr+" for file "+fileId);
      
      ImpactUser user = null;
      try{
        user = LoginManager.getUser(request,response);
      }catch(Exception e){
        DebugConsole.println("Exception "+e.getMessage());
      }
      if(user == null){
        errorResponder.printexception("You are not logged in");
        return;
      }
      
      String absoluteFile = null;
      
      try {
        absoluteFile = user.checkFileAndGetAbsolutePath(fileId);
      } catch(IOException e){
        errorResponder.printexception("File not found");
        response.setStatus(404);
        return;
      } catch (FileAccessForbiddenException e1) {
        errorResponder.printexception(e1.getMessage());
        response.setStatus(403);
        return;
      }
      response.getWriter().println(absoluteFile);
    }*/
    
    
 

    



    private void handleBasketRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
      
    
    	String requestStr=null;
    	
    	try {
        requestStr=HTTPTools.getHTTPParam(request,"request");
      } catch (Exception e3) {
        requestStr="";
      }
    	 /**
       *  get basket list as HTML
       */
      if(requestStr.equals("getoverview")){
        ImpactUser user = null;
        user = checkUserAndPrintJSONError(request,response);
        if(user == null)return;
        
        GenericCart basketList = null;
        try{
          response.setContentType("application/json");
          basketList = user.getShoppingCart();
          JSONObject datasetList = GenericCart.CartPrinters.showDataSetList(basketList,request);
          response.getWriter().println(datasetList.toString());
        }catch(Exception e){
          Debug.printStackTrace(e);
          response.getWriter().println(e.getMessage());
        }
        return;
      }
      
      /**
       * Remove file from status list
       */
      if(requestStr.equals("removeFromList")){
        
        GenericCart basketList = null;
        try{
          response.setContentType("text/plain");
          String[] procId=request.getParameterValues("id[]");
          Debug.println("Delete file from basket: "+procId);
          //if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
          basketList = LoginManager.getUser(request,response).getShoppingCart();
          basketList.removeDataLocators(procId);
          response.setContentType("application/json");
          response.getWriter().println("{\"numproducts\":\""+(basketList.getNumProducts())+"\"}");
        }catch(Exception e){
          response.getWriter().println(e.getMessage());
        }
        return;      
      }
      
    	
    	
    	//Mode add
    	try{
    		if(HTTPTools.getHTTPParam(request,"mode").equals("add")){
    		  JSONResponse jsonResponse = new JSONResponse(request);
    		  try{
      		  GenericCart shoppingCart = null;
      		  shoppingCart = LoginManager.getUser(request,response).getShoppingCart();
      		
      		  Debug.println("Adding data to basket"+request.getParameter("id"));
      		  
      		  int currentNumProducts=shoppingCart.getNumProducts();
      		  
      		  if(request.getParameter("id")!=null){
      		    JSONObject el=new JSONObject();
      		    el.put("id", request.getParameter("id"));
      		    el.put("OPENDAP", request.getParameter("OPENDAP"));
      		    el.put("HTTPServer", request.getParameter("HTTPServer"));
      		    el.put("catalogURL", request.getParameter("catalogURL"));
      		    el.put("filesize", request.getParameter("filesize"));
      		    addFileToBasket(shoppingCart,el);
      		  }
  
      			
      			try{
        			JSONArray jsonData = (JSONArray) new JSONTokener(request.getParameter("json")).nextValue();
        			for(int j=0;j<jsonData.length();j++){
        				
        				JSONObject el=((JSONObject)jsonData.get(j));
        				addFileToBasket(shoppingCart,el);
        			}
      			}catch(Exception e){}
      			String result = "{\"numproductsadded\":\""+(shoppingCart.getNumProducts()-currentNumProducts)+"\",";
      			result += "\"numproducts\":\""+(shoppingCart.getNumProducts())+"\"}";
      			Debug.println(result);
      			jsonResponse.setMessage(result);
  		    } catch(Exception e){
  		      jsonResponse.setException("Unable to add file to basket",e);
          }
    		  jsonResponse.print(response);
    		}
    	}catch(Exception e){

  			return;
    	}
    }
    
    
    
 
    
	private ImpactUser checkUserAndPrintJSONError(HttpServletRequest request,HttpServletResponse response) throws IOException {
	  ImpactUser user = null;
	  try{
      user = LoginManager.getUser(request,response);
    }catch(Exception e){
      response.setContentType("application/json");
      //response.setStatus(401);  <--Does not work with ExtJS proxy 
      response.getWriter().println("{ \"success\": \"false\",\"statuscode\":401,\"message\":\"You are not signed in\"}");
      return null;
    }
    return user;
  }



  private void addFileToBasket(GenericCart shoppingCart,JSONObject el) throws JSONException {
	  Debug.println(el.toString());
    String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    Calendar cal = Calendar.getInstance();
    String currentISOTimeString = sdf.format(cal.getTime())+"Z";
    long addDateMilli=cal.getTimeInMillis();
    String addDate=currentISOTimeString;
    Debug.println("Adding dataset "+el.getString("id")+" with date "+addDate);
    JSONObject fileInfo = new JSONObject();
    try{
      String str=el.getString("OPENDAP");
      if(str.length()>0){
        fileInfo.put("OPENDAP",str);
      }
    }catch(Exception e){}
    try{
      String str=el.getString("HTTPServer");
      if(str.length()>0){
        fileInfo.put("HTTPServer",str);
      }
    }catch(Exception e){}
    try{
      String str=el.getString("catalogURL");
      if(str.length()>0){
        fileInfo.put("catalogURL",str);
      }
    }catch(Exception e){}
    try{
      String str=el.getString("filesize");
      if(str.length()>0){
        fileInfo.put("filesize",str);
      }
    }catch(Exception e){}
    Debug.println("Data="+fileInfo.toString());
    
    shoppingCart.addDataLocator(el.getString("id"),fileInfo.toString(), addDate, addDateMilli,true);
      
  }



  static PrintWriter getPrintWriter(HttpServletResponse response) {
		PrintWriter out1 = null;
		try {out1 = response.getWriter();} catch (IOException e) {Debug.errprint(e.getMessage());}
		return out1;
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  if(request.getQueryString()!=null){
	    Debug.println("Request received query string "+request.getQueryString());
	  }
		JSONMessageDecorator errorResponder = new JSONMessageDecorator (response);
		String serviceStr = null;

			serviceStr=request.getParameter("service");
		if(serviceStr==null){
		  serviceStr=request.getParameter("SERVICE");
		}
	

		
		if(serviceStr!=null){serviceStr=URLDecoder.decode(serviceStr,"UTF-8");}else{errorResponder.printexception("serviceStr="+serviceStr);return;}
		
		/**
		 * Handle WMS requests
		 */
    if(serviceStr.equalsIgnoreCase("WMS")){
     handleWMSRequests(request,response);

    }
    /**
     * Handle WCS requests
     */
    if(serviceStr.equalsIgnoreCase("WCS")){
     handleWMSRequests(request,response);

    }
    
    
    /**
     * Handle Admin requests 
     */
    if(serviceStr.equalsIgnoreCase("ADMIN")){
      HandleAdminRequests.handleAdminRequests(request,response);
    }
    
		
    /**
     * Handle Session requests
     */
    if(serviceStr.equalsIgnoreCase("session")){
     handleSessionRequests(request,response);

    }
    
		/**
		 * Handle Processor requests
		 */
		if(serviceStr.equals("processor")){
			handleProcessorRequest(request,response,errorResponder);
		}
		/*
		 * Handle catalog requests
		 */
		if(serviceStr.equals("catalogbrowser")){
			handleCatalogBrowserRequest(request,response,errorResponder);
		}
		
		/*
		 * Handle NCDUMP requests
		 */
		if(serviceStr.equals("ncdump")){
			//handleNCDUMPRequest(request,response,errorResponder);
		  Debug.println("Deprecated command");
		}
		
		/*
		 * Handle getvariables request for the variable browser.
		 */
		if(serviceStr.equals("getvariables")){
		  OpenDAPViewer viewer = new OpenDAPViewer(Configuration.getImpactWorkspace()+"/diskCache/");
		  viewer.doGet(request, response);
			//handleVariablesRequest(request,response,errorResponder);
		}
		
		/*
		 * Handle search requests
		 */
		if(serviceStr.equals("search")){
			handleSearchRequest(request,response,errorResponder);
		}
		
		/*
		 * Handle basket requests
		 */
		if(serviceStr.equals("basket")){
			handleBasketRequest(request,response,errorResponder);
		}
		
		/*
		 * Handle BasicSearch requests
		 */ 
//		if(serviceStr.equals("basicsearch")){
//			BasicSearch.handleBasicSearchRequest(request,response,errorResponder);
//		}
	
		/*
		 * Handle data requests
		 */
    /*if(serviceStr.equals("data")){
      handleDataRequest(request,response,errorResponder);
    }*/
  
		

	}
  
	private void handleSessionRequests(HttpServletRequest request,HttpServletResponse response) {
    HttpSession session = request.getSession();
    {
      ServletOutputStream out = null;
      try {
        response.setContentType("application/json");
        out = response.getOutputStream();
        String mode=HTTPTools.getHTTPParam(request,"mode");
        if(mode.equalsIgnoreCase("advancedsearch")){
          SearchSession searchSession = (SearchSession) session.getAttribute("searchsession");  
          if(searchSession==null){
            Debug.println("Creating new searchsession");
            searchSession = new SearchSession();session.setAttribute("searchsession",searchSession);
          }
          out.print(searchSession.getAsJSON());
          return;
        }
        out.print("{\"error\":\"Invalid mode\"}");
      } catch (IOException e) {
        Debug.errprint(e.getMessage());
        return;
      } catch (Exception e) {
        Debug.errprint(e.getMessage());
        return;
      }
    }
	}



  private void handleWMSRequests(HttpServletRequest request, HttpServletResponse response) {
	  Debug.println("Handle WMS requests");
    OutputStream out1 = null;
    //response.setContentType("application/json");
    try {
      out1 = response.getOutputStream();
    } catch (IOException e) {
      Debug.errprint(e.getMessage());
      return;
    }
  
    try {
      AdagucServer.runADAGUCWMS(request,response,request.getQueryString(),out1);

    } catch (Exception e) {
      response.setStatus(401);
      try {
        out1.write(e.getMessage().getBytes());
      } catch (IOException e1) {
        Debug.errprintln("Unable to write to stream");
        Debug.printStackTrace(e);
      }
    }    
  }

  
  
  
  


  /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}

