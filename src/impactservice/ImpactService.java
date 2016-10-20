package impactservice;

import impactservice.SessionManager.SearchSession;

import java.io.BufferedReader;
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

    String requestStr=HTTPTools.getHTTPParamNoExceptionButNull(request,"request");
    if(requestStr!=null){requestStr=URLDecoder.decode(requestStr,"UTF-8");}else{errorResponder.printexception("urlStr="+requestStr);return;}

    //Debug.println("PROCESSOR REQUEST="+requestStr);

  
    
    
    /**
     * Remove statuslocation from status list
     */
    if(requestStr.equalsIgnoreCase("removeFromList")){
      JSONResponse jsonResponse = new JSONResponse(request);
      GenericCart jobList = null;
      try{
        String procId=HTTPTools.getHTTPParam(request,"id");
        if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
        jobList = LoginManager.getUser(request).getProcessingJobList();
        jobList.removeDataLocator(procId);
        jsonResponse.setMessage("{\"numproducts\":\""+(jobList.getNumProducts())+"\"}");
      }catch(Exception e){
        jsonResponse.setException("Processor: removefromlist failed", e);
      }
      jsonResponse.printNE(response);
    }

    /**
     *  getprocessor status list as JSON
     */
    if(requestStr.equalsIgnoreCase("getProcessorStatusOverview")){
      JSONResponse jsonResponse = new JSONResponse(request);
      GenericCart jobList = null;
      try{
        jobList = LoginManager.getUser(request).getProcessingJobList();
        JSONObject json = GenericCart.CartPrinters.showJobList(jobList,request);
        jsonResponse.setMessage(json);
      }catch(Exception e){
        jsonResponse.setException("Processor: getProcessorStatusOverview failed", e);
      }
      jsonResponse.printNE(response);
    }

    /**
     * Get processor list as JSON
     */
    if(requestStr.equalsIgnoreCase("getProcessorList")){
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
    if(requestStr.equalsIgnoreCase("describeProcessor")){
      String procId=HTTPTools.getHTTPParamNoExceptionButNull(request,"id");
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
    if(requestStr.equalsIgnoreCase("executeProcessor")){
    
      
      
      String procId=HTTPTools.getHTTPParamNoExceptionButNull(request,"id");
      if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
      String dataInputs=HTTPTools.getHTTPParamNoExceptionButNull(request,"dataInputs");
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
    if(requestStr.equalsIgnoreCase("monitorProcessor")){
      String procId=HTTPTools.getHTTPParamNoExceptionButNull(request,"id");
      if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
      String statusLocation=HTTPTools.getHTTPParamNoExceptionButNull(request,"statusLocation");
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
    if(requestStr.equalsIgnoreCase("getimage")){

      String statusLocation=HTTPTools.getHTTPParamNoExceptionButNull(request,"statusLocation");
      if(statusLocation!=null){statusLocation=URLDecoder.decode(statusLocation,"UTF-8");}else{errorResponder.printexception("statusLocation="+statusLocation);return;}
      String outputId=HTTPTools.getHTTPParamNoExceptionButNull(request,"outputId");
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

      String statusLocation=HTTPTools.getHTTPParamNoExceptionButNull(request,"statusLocation");
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

  class BuildHTMlResult{
    String result;
    int rn;
  }

  BuildHTMlResult buildHTML(JSONArray array,String root,int oddEven,String openid,int rn){
    if(array == null)return null;
    StringBuffer html = new StringBuffer();
    try {
      //Try to get the catalogURL 

      for(int j=0;j<array.length();j++){
        rn++;
        String opendapURL=null;
        String httpserverURL=null;
        String hrefURL=null;
        String catalogURL=null;
        String nodeText = null;
        String fileSize = "";
        JSONObject a=array.getJSONObject(j);
        nodeText = a.getString("text");
        try{opendapURL = a.getString("opendap");}catch (JSONException e) {}
        try{httpserverURL = a.getString("httpserver");}catch (JSONException e) {}
        try{catalogURL = a.getString("catalogURL");}catch (JSONException e) {}
        try{hrefURL = a.getString("href");}catch (JSONException e) {}
        try{fileSize = a.getString("dataSize");}catch (JSONException e) {}

        oddEven = 1-oddEven;
        if(oddEven==0){
          //html+="<tr class=\"even\"><td>";
          html.append("<tr class=\"even\"><td>"+rn+"</td><td>");
        }else{
          //html+="<tr class=\"odd\"><td>";
          html.append("<tr class=\"odd\"><td>"+rn+"</td><td>");
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
         */ if(opendapURL == null && httpserverURL !=null){
           opendapURL = httpserverURL.replace("fileServer", "dodsC")+"#";
         }

         if(opendapURL!=null){
           dapLink="<span class=\"link\" onclick=\"renderFileViewer({url:'"+opendapURL+"'});\">view</span>";
           //dapLink="<a href=\"#\" onclick=\"renderFileViewer({url:'"+openDAPURL+"'});\">view</a>";
         }
         if(httpserverURL!=null){
           if(openid!=null){
             httpLink="<a class=\"c4i-wizard-catalogbrowser-downloadlinkwithopenid\"  href=\""+httpserverURL+"?openid="+openid+"\" target=\"_blank\"\">download</a>";
           }else{
             httpLink="<a class=\"c4i-wizard-catalogbrowser-downloadlinknoopenid\" href=\""+httpserverURL+"\">download</a>";
           }
         }
         //html+="</td><td>"+dapLink+"</td><td>"+httpLink;
         html.append("</td><td>");html.append(dapLink);html.append("</td><td>");html.append(httpLink);
         html.append("</td>");
         if(httpserverURL == null && opendapURL == null && catalogURL == null){
           //html+="</td><td>-";  
           html.append("<td></td>");
         }else{
           html.append("<td><span onclick=\"basket.postIdentifiersToBasket({id:'"+nodeText+"',httpserver:'"+httpserverURL+"',opendap:'"+opendapURL+"',catalogURL:'"+catalogURL+"',"+"filesize:'"+fileSize+"'});\" class=\"shoppingbasketicon\"/></td>\n");
         }


         //html+="</td></tr>";
         html.append("</tr>");

         try{
           JSONArray children = a.getJSONArray("children");
           //html+=buildHTML(children,root+"&nbsp;&nbsp;&nbsp;&nbsp;",oddEven);
           BuildHTMlResult b=buildHTML(children,root+"&nbsp;&nbsp;&nbsp;&nbsp;",1-oddEven,openid,rn);
           html.append(b.result);
           rn=b.rn;
         } catch (JSONException e) {
         }
      }
    } catch (JSONException e) {
    }

    BuildHTMlResult b=new BuildHTMlResult();
    b.result=html.toString();
    b.rn=rn;
    return b;
  }


  private void handleCatalogBrowserRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
    Debug.println("SERVICE CATALOGBROWSER: "+request.getQueryString());
    String format= HTTPTools.getHTTPParamNoExceptionButNull(request,"format");
    JSONResponse jsonResponse = new JSONResponse(request);
    if(format==null)format="";
    boolean flat = false;
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
      try{
        String mode= HTTPTools.getHTTPParam(request, "mode");
        if(mode.equals("flat")){
          flat=true;
        }
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
        jsonResponse.setException("Unable to read catalog", e);
        try {
          jsonResponse.print(response);
        } catch (Exception e1) {
           e1.printStackTrace();
        }
        return;
      
      }

      long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
      Debug.println("Finished CATALOG: "+" ("+(stopTimeInMillis-startTimeInMillis)+" ms) "+request.getQueryString());

      if(treeElements == null){
        jsonResponse.setErrorMessage("Unable to read catalog", 200);
        try {
          jsonResponse.print(response);
        } catch (Exception e1) {
           e1.printStackTrace();
        }
        return;
      
      }
      
      
      
      
      if(jsonResponse.hasError()==false){
        if(flat == false){
  
    
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
            html+="<div class=\"c4i-catalogbrowser-variableandtextfilter\"><form  class=\"varFilter\">";
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
              html+="<br/><br/>";
            }
            html+="<b>Text filter:</b> <input type=\"textarea\" class=\"textfilter\" id=\"textfilter\" value=\""+textFilter+"\" ></input>";
            
            //html+="<br/>";
            //html+="&nbsp; <input style=\"float:right;\" type=\"button\" value=\"Go\" onclick=\"setVarFilter();\"/>";
    
            html+="</form></div>";
            
          }catch(Exception e){
            e.printStackTrace();
          }
    
          html+="<div id=\"datasetfilelist\"><table class=\"c4i-catalogbrowser-table\">";
          html+="<tr>";
          html+="<th>#</th>";
          html+="<th width=\"100%\" class=\"c4i-catalogbrowser-th\">Resource title</th>";
          html+="<th class=\"c4i-catalogbrowser-th\"><b>Size</b></th>";
          html+="<th class=\"c4i-catalogbrowser-th\"><b>Opendap</b></th>";
          html+="<th class=\"c4i-catalogbrowser-th\"><b>Download</b></th>";
          html+="<th class=\"c4i-catalogbrowser-th\"><b>Basket</b></th>";
          html+="</tr>";
    
          long startTimeInMillis1 = Calendar.getInstance().getTimeInMillis();
    
          String openid = null;
          try{
            openid= LoginManager.getUser(request).getOpenId();
          }catch(Exception e){            
          }
          html+=buildHTML(treeElements,"",0,openid,0).result+"</table></div>";
          long stopTimeInMillis1 = Calendar.getInstance().getTimeInMillis();
          Debug.println("Finished building HTML with length "+html.length() +" in ("+(stopTimeInMillis1-startTimeInMillis1)+" ms)");
          if(format.equals("text/html")){
            response.setContentType("text/html");
            response.getWriter().print(html);
          }else if(format.equals("application/json")){
            JSONObject a = new JSONObject();
            a.put("html", html);
            try{
              jsonResponse.setMessage(a);
            } catch(Exception e){
              jsonResponse.setException("Catalogbrowser error:",e);
            }
            try {
              jsonResponse.print(response);
            } catch (Exception e1) {
    
            }
          }else{
            try{
              jsonResponse.setMessage(treeElements.toString());
            } catch(Exception e){
              jsonResponse.setException("Catalogbrowser error:",e);
            }
            try {
              jsonResponse.print(response);
            } catch (Exception e1) {
    
            }
          }
        }
        
        if(flat == true){
          THREDDSCatalogBrowser.MakeFlat b = new THREDDSCatalogBrowser.MakeFlat();
          JSONArray allFilesFlat = b.makeFlat(treeElements);
          JSONObject data = new JSONObject();
          data.put("files",allFilesFlat);
          jsonResponse.setMessage(data);
          Debug.println("Found "+allFilesFlat.length());
  
          try {
            jsonResponse.print(response);
          } catch (Exception e1) {
  
          }
        }
        Debug.println("Catalog request finished.");
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
        jsonResponse.setException("error", e);
        try {jsonResponse.print(response);} catch (Exception e1) {}
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
        jsonResponse.setException("error", e2);
        try {jsonResponse.print(response);} catch (Exception e1) {}
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
        user = LoginManager.getUser(request);
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
     *  get basket list
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
    
    if(requestStr.equals("getfile")){
      JSONResponse result = new JSONResponse(request);
      String fileName = null;
      try {
        fileName=tools.HTTPTools.getHTTPParam(request, "file");
      } catch (Exception e1) {
      }
      if(fileName == null){
        return;
      }
      
      ImpactUser user = null;
      user = checkUserAndPrintJSONError(request,response);
      if(user == null)return;
      
      JSONObject json = new JSONObject (); 
      
      if(!fileName.startsWith(Configuration.getHomeURLHTTPS())){
        result.setErrorMessage("File does not start with "+Configuration.getHomeURLHTTPS(), 500);
      }
      
      Debug.println(fileName);
      if(result.hasError() == false){
        String data = null;
        try{
          data = tools.HTTPTools.makeHTTPGetRequestX509ClientAuthentication(fileName, user.certificateFile,Configuration.LoginConfig.getTrustStoreFile(), Configuration.LoginConfig.getTrustStorePassword(),0);
          if(data == null){
            result.setErrorMessage("Unable to load data for "+fileName, 500);
          }
        }catch(Exception e){
          result.setException("Unable to load data for "+fileName, e);
        }
     
        if(result.hasError() == false){
          try {
            
            json.put("data", new JSONTokener(data).nextValue());
          } catch (JSONException e) {
            result.setException("Unable to return data for "+fileName, e);
          }
        }
        
        result.setMessage(json);
      }
      result.print(response);
  
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
        Debug.println("Delete file from basket ");
        //if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
        basketList = LoginManager.getUser(request).getShoppingCart();
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
          shoppingCart = LoginManager.getUser(request).getShoppingCart();

          Debug.println("Adding data to basket "+HTTPTools.getHTTPParam(request,"id"));

          int currentNumProducts=shoppingCart.getNumProducts();

          if(HTTPTools.getHTTPParam(request,"id")!=null){
            JSONObject el=new JSONObject();
            
            String opendap = HTTPTools.getHTTPParamNoExceptionButNull(request,"opendap");
            String httpserver = HTTPTools.getHTTPParamNoExceptionButNull(request,"httpserver");
            String catalogURL = HTTPTools.getHTTPParamNoExceptionButNull(request,"catalogURL");
            if(opendap!=null){
              if(opendap.equals("null")==false){
                if(HTTPTools.isURL(opendap)==false){
                  jsonResponse.setErrorMessage("{\"error\":\"Not a valid URL\"}",200);
                }
              }else{
                opendap = null;
              }
            }
            if(httpserver!=null){
              if(httpserver.equals("null")==false){
                if(HTTPTools.isURL(httpserver)==false){
                  jsonResponse.setErrorMessage("{\"error\":\"Not a valid URL\"}",200);
                }
              }else{
                httpserver = null;
              }
            }
            if(catalogURL!=null){
              if(catalogURL.equals("null")){
                catalogURL = null;
              }
            }
            
            Debug.println("opendap="+opendap);
            Debug.println("httpserver="+httpserver);
            Debug.println("catalogURL="+catalogURL);
            Debug.println("opendap="+(opendap==null));
            Debug.println("httpserver="+(httpserver==null));
            Debug.println("catalogURL="+(catalogURL==null));
            
            if(opendap == null && httpserver == null &&catalogURL == null){
              jsonResponse.setErrorMessage("{\"error\":\"Not a valid URL found.\"}",200);
            }
            
            if(jsonResponse.hasError() == false){
              el.put("id", HTTPTools.getHTTPParamNoExceptionButNull(request,"id"));
              el.put("opendap", HTTPTools.getHTTPParamNoExceptionButNull(request,"opendap"));
              el.put("httpserver", HTTPTools.getHTTPParamNoExceptionButNull(request,"httpserver"));
              el.put("catalogURL", HTTPTools.getHTTPParamNoExceptionButNull(request,"catalogURL"));
              el.put("filesize", HTTPTools.getHTTPParamNoExceptionButNull(request,"filesize"));
              addFileToBasket(shoppingCart,el);
            }
          }

          if(jsonResponse.hasError() == false){
            try{
              JSONArray jsonData = (JSONArray) new JSONTokener(HTTPTools.getHTTPParam(request,"json")).nextValue();
              for(int j=0;j<jsonData.length();j++){
  
                JSONObject el=((JSONObject)jsonData.get(j));
                addFileToBasket(shoppingCart,el);
              }
            }catch(Exception e){}
            String result = "{\"numproductsadded\":\""+(shoppingCart.getNumProducts()-currentNumProducts)+"\",";
            result += "\"numproducts\":\""+(shoppingCart.getNumProducts())+"\"}";
            Debug.println(result);
            jsonResponse.setMessage(result);
          }
        } catch(Exception e){
          jsonResponse.setException("Unable to add file to basket",e);
        }
        jsonResponse.print(response);
      }
    }catch(Exception e){
      Debug.printStackTrace(e);
      return;
    }
  }





  private ImpactUser checkUserAndPrintJSONError(HttpServletRequest request,HttpServletResponse response) throws IOException {
    ImpactUser user = null;
    try{
      user = LoginManager.getUser(request);
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
      String str=el.getString("opendap");
      if(str.length()>0){
        fileInfo.put("opendap",str);
      }
    }catch(Exception e){}
    try{
      String str=el.getString("httpserver");
      if(str.length()>0){
        fileInfo.put("httpserver",str);
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
//    if(request.getQueryString()!=null){
//      Debug.println("Query string:["+request.getQueryString()+"]");
//    }
//    
    JSONMessageDecorator errorResponder = new JSONMessageDecorator (response);
    String serviceStr = null;

    serviceStr=HTTPTools.getHTTPParamNoExceptionButNull(request,"service");
    if(serviceStr==null){
      serviceStr=HTTPTools.getHTTPParamNoExceptionButNull(request,"SERVICE");
    }



    if(serviceStr!=null){serviceStr=URLDecoder.decode(serviceStr,"UTF-8");}else{errorResponder.printexception("serviceStr="+serviceStr);return;}

    /**
     * Handle WMS requests (deprecated since 2015-Oct-07, use adagucserver servlet instead!)
     */
    if(serviceStr.equalsIgnoreCase("WMS")){
      handleWMSRequests(request,response);

    }
    /**
     * Handle WCS requests (deprecated since 2015-Oct-07, use adagucserver servlet instead!)
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
      OpendapViewer viewer = new OpendapViewer(Configuration.getImpactWorkspace()+"/diskCache/");
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
     * Handle getopenid requests
     */
    if(serviceStr.equals("getopenid")){
      JSONResponse jsonResponse = new JSONResponse(request);
      try{
        ImpactUser user = checkUserAndPrintJSONError(request,response);
        if(user == null){
          jsonResponse.setErrorMessage("You are not signed in.", 401);
        }else{
          JSONObject openid=new JSONObject();
          openid.put("openid", user.getOpenId());
          jsonResponse.setMessage(openid);
        }
      }catch(Exception e){
        jsonResponse.setException("Processor: removefromlist failed", e);
      }
      jsonResponse.printNE(response);
    }
    
    

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



  //Deprecated
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

