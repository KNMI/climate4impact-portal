package impactservice;

import impactservice.SessionManager.DatasetViewerSession;
import impactservice.SessionManager.SearchSession;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLPeerUnverifiedException;
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

import tools.DebugConsole;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.JSONMessageDecorator;
import tools.MyXMLParser;
import tools.MyXMLParser.XMLElement;
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
  		
  		DebugConsole.println("PROCESSOR REQUEST="+requestStr);
  		
  		/**
  		 * Remove processor from status list
  		 */
  		if(requestStr.equals("removeFromList")){
		 
        GenericCart jobList = null;
        try{
          response.setContentType("text/plain");
          String procId=request.getParameter("id");
          if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
          jobList = User.getUser(request).getProcessingJobList();
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
  	      jobList = User.getUser(request).getProcessingJobList();
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
      			DebugConsole.errprint(e.getMessage());
      			return;
      		}
  			Vector<WebProcessingInterface.ProcessorDescriptor> listOfProcesses = WebProcessingInterface.getAvailableProcesses();
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
            response.getWriter().print(WebProcessingInterface.describeProcess(procId).toString());
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
            response.getWriter().print(WebProcessingInterface.executeProcess(procId,dataInputs,request));
          } catch (Exception e) {
            DebugConsole.errprintln(e.getMessage());
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
    
    String buildHTML(JSONArray array,String root,int oddEven){
      
      StringBuffer html = new StringBuffer();
      try {
        //Try to get the catalogURL 
        
        for(int j=0;j<array.length();j++){
          
          String openDAPURL=null;
          String httpURL=null;
          String hrefURL=null;
          String catalogURL=null;
          String nodeText = null;
          String dataSize = "";
          JSONObject a=array.getJSONObject(j);
          nodeText = a.getString("text");
          try{openDAPURL = a.getString("OPENDAP");}catch (JSONException e) {}
          try{httpURL = a.getString("HTTPServer");}catch (JSONException e) {}
          try{catalogURL = a.getString("catalogURL");}catch (JSONException e) {}
          try{hrefURL = a.getString("href");}catch (JSONException e) {}
          try{dataSize = a.getString("dataSize");}catch (JSONException e) {}

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
          html.append("<td>"+dataSize+"</td>");
          //html.append("<td>");html.append(dataSize);html.append("</td>");
          
          String dapLink = "";
          String httpLink = "";
          
          if(openDAPURL == null && httpURL !=null){
            openDAPURL = httpURL.replace("fileServer", "dodsC")+"#fromhttpurl";
          }
          
          if(openDAPURL!=null){
            try {
              dapLink="<a href=\"/impactportal/data/datasetviewer.jsp?dataset="+URLEncoder.encode(openDAPURL,"UTF-8")+"\">view</a>";
            } catch (UnsupportedEncodingException e1) {}
          }
          if(httpURL!=null)httpLink="<a href=\""+httpURL+"\">get</a>";
          //html+="</td><td>"+dapLink+"</td><td>"+httpLink;
          html.append("</td><td>");html.append(dapLink);html.append("</td><td>");html.append(httpLink);
          if(httpURL == null && openDAPURL == null && catalogURL == null){
            //html+="</td><td>-";  
            html.append("</td><td>");
          }else{
            //html+="</td><td><span onclick=\"postIdentifierToBasket({id:'"+nodeText+"',HTTPServer:'"+httpURL+"',OPENDAP:'"+openDAPURL+"',catalogURL:'"+catalogURL+"'});\" class=\"shoppingbasketicon\"/>";
            html.append("</td><td><span onclick=\"postIdentifierToBasket({id:'"+nodeText+"',HTTPServer:'"+httpURL+"',OPENDAP:'"+openDAPURL+"',catalogURL:'"+catalogURL+"'});\" class=\"shoppingbasketicon\"/>");
          }
          
          
          //html+="</td></tr>";
          html.append("</td></tr>");
          try{
            JSONArray children = a.getJSONArray("children");
            //html+=buildHTML(children,root+"&nbsp;&nbsp;&nbsp;&nbsp;",oddEven);
            html.append(buildHTML(children,root+"&nbsp;&nbsp;&nbsp;&nbsp;",oddEven));
          } catch (JSONException e) {
          }
        }
      } catch (JSONException e) {
      }
      return html.toString();
    }
    
    
    private void handleCatalogBrowserRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
  		DebugConsole.println("SERVICE CATALOGBROWSER: "+request.getQueryString());
      String format= request.getParameter("format");
      if(format==null)format="";
  	  try{
  	    String variableFilter=request.getParameter("variables");
        if(variableFilter==null)variableFilter="";
        String textFilter=request.getParameter("filter");
        if(textFilter==null)textFilter="";
        
        HTTPTools.validateInputTokens(variableFilter);
        HTTPTools.validateInputTokens(textFilter);
        
  	    DebugConsole.println("Starting CATALOG: "+request.getQueryString());
  		  long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
  		  JSONArray treeElements = THREDDSCatalogBrowser.browseThreddsCatalog(request, response,errorResponder,variableFilter,textFilter); 
        long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
        DebugConsole.println("Finished CATALOG: "+" ("+(stopTimeInMillis-startTimeInMillis)+" ms) "+request.getQueryString());

  		  
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
            
            
            DebugConsole.println("variableFilter: '"+variableFilter+"'");
            DebugConsole.println("textFilter: '"+textFilter+"'");
            html+="<form id=\"varFilter\">";
            for(int j=0;j<availableVars.size();j++){
              if(j!=0)html+="&nbsp;";
              String checked="";//checked=\"yes\"";
              if(variableFilter.length()>0){
                checked="";
                if(availableVars.get(j).matches(variableFilter))checked="checked=\"yes\"";
              }
              html+="<input type=\"checkbox\" name=\"variables\" id=\""+availableVars.get(j)+"\" "+checked+">"+availableVars.get(j);
            }
            html+="&nbsp;filter: <input type=\"textarea\" id=\"textfilter\" value=\""+textFilter+"\" />";
            html+="&nbsp; <input type=\"button\" value=\"Filter\" onclick=\"setVarFilter();\"/>";
            
            html+="</form>";
          }catch(Exception e){
            e.printStackTrace();
          }
          
          html += "<table class=\"basket\">";
          html+="<tr><td width=\"100%\" class=\"basketheader\"><b>Title</b></td><td class=\"basketheader\"><b>Size</b></td><td class=\"basketheader\"><b>OPENDAP</b></td><td class=\"basketheader\"><b>HTTP</b></td><td class=\"basketheader\"><b>Basket</b></td></tr>";
          
          long startTimeInMillis1 = Calendar.getInstance().getTimeInMillis();
          
          
          html+=buildHTML(treeElements,"",0)+"</table>";
          long stopTimeInMillis1 = Calendar.getInstance().getTimeInMillis();
          DebugConsole.println("Finished building HTML with length "+html.length() +" in ("+(stopTimeInMillis1-startTimeInMillis1)+" ms)");
  		    response.getWriter().print(html);
  		    DebugConsole.println("Catalog request finished.");
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
    
    private void handleNCDUMPRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
  		DebugConsole.println("SERVICE NCDUMP: "+request.getQueryString());
  		String requestStr=request.getParameter("request");
  		if(requestStr!=null){requestStr=URLDecoder.decode(requestStr,"UTF-8");}else{errorResponder.printexception("urlStr="+requestStr);return;}
  		PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e) {DebugConsole.errprint(e.getMessage());return;}
  		response.setContentType("text/plain");
  		try{
  		  out1.println(NetCDFC.executeNCDumpCommand(LoginManager.getUser(request),requestStr));
  		}catch(Exception e){
  			out1.println("Unable to get file "+requestStr);
  			e.printStackTrace(out1);
  		}
    }
    
    private void handleVariablesRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
      HttpSession session = request.getSession();
      DatasetViewerSession datasetViewerSession=(DatasetViewerSession) session.getAttribute("datasetviewersession");
      if(datasetViewerSession==null){ 
        DebugConsole.println("Creating new datasetviewersession");
        datasetViewerSession = new DatasetViewerSession();session.setAttribute("datasetviewersession",datasetViewerSession);
      }
      DebugConsole.println("SERVICE GETVARIABLES: "+request.getQueryString());

      String requestStr=request.getParameter("request");
      if(requestStr!=null){requestStr=URLDecoder.decode(requestStr,"UTF-8");}else{errorResponder.printexception("urlStr="+requestStr);return;}
      PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e) {DebugConsole.errprint(e.getMessage());return;}

      //Check if we really have an URL here and not a localfile
      if(requestStr.indexOf("http")!=0&&requestStr.indexOf("dods")!=0){

        try {
          JSONObject error = new JSONObject();
          error.put("error","Invalid OpenDAP URL given");
          out1.print(error.toString());
        } catch (JSONException e1) {}
        //e.printStackTrace(System.err);

        return;
      }

      datasetViewerSession.datasetURL=requestStr;






      response.setContentType("application/json");
      //NetcdfDataset ncdataset = null;
      //String dodsRequest=requestStr;
      //dodsRequest=dodsRequest.replaceFirst("https", "dods");
      //dodsRequest=dodsRequest.replaceFirst("http", "dods");
      try{

        DebugConsole.println("dodsRequest="+requestStr);

        String ncdumpMessage = "";
        User user = null;
        try{
          user=LoginManager.getUser(request);
        }catch(Exception e){
          DebugConsole.println("WARNING: User not logged in");
        }
        

        
        
        ncdumpMessage=NetCDFC.executeNCDumpCommand(user,requestStr);
        
        
        
          if(ncdumpMessage==""){
            String msg="";
            try{
              String certificateLocation = null;
              if(user!=null){
                if(user.certificateFile != null){
                  certificateLocation = user.certificateFile;
                }
              }
              ncdumpMessage = HTTPTools.makeHTTPGetRequest(requestStr+".ddx",certificateLocation,Configuration.LoginConfig.getTrustStoreFile(),Configuration.LoginConfig.getTrustStorePassword());
            }catch(SSLPeerUnverifiedException e){
              msg="Peer unverified: "+e.getMessage();
              throw new Exception(msg);
            }catch(UnknownHostException e){
              msg="Unknown host '"+e.getMessage()+"'";
              throw new Exception(msg);
            }catch(WebRequestBadStatusException e){
              msg=e.getMessage()+"<br/>";
              if(e.getStatusCode()==401){
                msg="HTTP status code "+e.getStatusCode()+": Unauthorized<br/>";
                if(user == null){
                  msg+="<br/>Warning: You are not logged in.<br/>";
                }
              }
              if(e.getStatusCode()==403){
                msg="HTTP status code "+e.getStatusCode()+": Forbidden<br/>"+e.getResult();
                if(user != null){
                  msg+="<br/>You are logged in as "+user.id+"<br/>";
                }
              }
              if(e.getStatusCode()==404)msg="HTTP status code "+e.getStatusCode()+": Not Found<br/>";
              
              /*else{
                msg+="<br/>You are logged in as "+user.id+"<br/>";
              }*/
              /*String html = e.getResult();
              
              DebugConsole.println("Got:"+html+"]");
              
              DebugConsole.println("1");
              HTMLParser htmlParser = new HTMLParser();
              DebugConsole.println("2");
              HTMLParserNode element = htmlParser.parseHTMLDocument(html);
              DebugConsole.println("3");
              HTMLParserNode body=htmlParser.getBody(element);
              DebugConsole.println("4");
              DebugConsole.println(body.printTree());
              DebugConsole.println("5");
              msg+=body;*/
              throw new Exception(msg);
            }catch(Exception e){
              msg="Exception: "+e.getMessage();
              throw new Exception(msg);
            }
          }
        
        DebugConsole.println("Trying to parse ncdump message");
        MyXMLParser.XMLElement rootElement = new MyXMLParser.XMLElement();
        rootElement.parseString(ncdumpMessage);
        DebugConsole.println("Parsed");
        //DebugConsole.println(rootElement.toString());

        List<XMLElement>dimensions = rootElement.get("netcdf").getList("dimension");
        List<XMLElement>variables = rootElement.get("netcdf").getList("variable");
        
        JSONArray variableInfo = new JSONArray (); 
        
        JSONObject jsonVariable = new JSONObject();

        String varName= "nc_global";
        jsonVariable.put("variable",varName);//.getName());
        jsonVariable.put("longname","Global attributes");//.getName());

        List<XMLElement>attributes = rootElement.get("netcdf").getList("attribute");
        if(attributes.size()>0){
          JSONArray jsonattributeArray = new JSONArray();
          for(int a=0;a<attributes.size();a++){

            JSONObject attribute = new JSONObject();
            attribute.put("name",attributes.get(a).getAttrValue("name"));
            attribute.put("value",attributes.get(a).getAttrValue("value"));
            jsonattributeArray.put(attribute);
          }
          jsonVariable.put("attributes",jsonattributeArray);
        }

        if(attributes.size()>0){
          variableInfo.put(jsonVariable);
        }


      
        
        for(int j=0;j<variables.size();j++){

          
          JSONObject jsonVariable1 = new JSONObject();

          String varName1= variables.get(j).getAttrValue("name");
          jsonVariable1.put("variable",varName1);//.getName());
          String longName=varName1;
          jsonVariable1.put("variabletype", variables.get(j).getAttrValue("type"));
          jsonVariable1.put("service", requestStr);


          /*if(variables.get(j).isCoordinateVariable()){
					JSONArray length = new JSONArray();
					length.put((new JSONObject()).put("length",variables.get(j).getSize()));
					jsonVariable.put("isDimension",length);
				}*/
          try{

            String[] varDimensions = variables.get(j).getAttrValue("shape").split(" ");
            if(varDimensions.length>=2){
              if(variables.get(j).getAttrValue("name").indexOf("bnds")==-1){
                if(variables.get(j).getAttrValue("name").equals("lon")==false&&variables.get(j).getAttrValue("name").equals("lat")==false){
                  jsonVariable1.put("isViewable",1);
                }
              }
            }

           
            JSONArray jsonDimensionArray = new JSONArray();
            for(int d=0;d<varDimensions.length;d++){
              JSONObject dimension = new JSONObject();
              String dimName=varDimensions[d];
              for(int rd=0;rd<dimensions.size();rd++){
                if(dimensions.get(rd).getAttrValue("name").equals(dimName)){
                  String dimname=dimName;
                  if(d<varDimensions.length-1)dimname+=", ";
                  dimension.put("name",dimname);
                  dimension.put("length",dimensions.get(d).getAttrValue("length"));
                  jsonDimensionArray.put(dimension);
                  
                  if(dimName.equals(varName1)){
                    JSONArray length = new JSONArray();
                    length.put((new JSONObject()).put("length",dimensions.get(rd).getAttrValue("length")));
                    jsonVariable1.put("isDimension",length);
                  }
                  
                  break;
                }
              }
              //dimension.put("name",dimName);
            }
            jsonVariable1.put("dimensions",jsonDimensionArray);
            //DebugConsole.println("dimensionArray: "+jsonDimensionArray.toString());
          }catch(Exception e){
         
          }
        

          List<XMLElement>attributes1 = variables.get(j).getList("attribute");
          if(attributes1.size()>0){
            JSONArray jsonattributeArray = new JSONArray();
            for(int a=0;a<attributes1.size();a++){

              JSONObject attribute = new JSONObject();
              String attrName=attributes1.get(a).getAttrValue("name");
              String attrValue=attributes1.get(a).getAttrValue("value");
              attribute.put("name",attrName);
              attribute.put("value",attrValue);
              if(attrName.equals("long_name"))longName=attrValue;
              jsonattributeArray.put(attribute);
            }
            jsonVariable1.put("attributes",jsonattributeArray);
          }
          
          jsonVariable1.put("longname",longName);
          
          if(attributes1.size()>0){
            variableInfo.put(jsonVariable1);
          }
          
        }
        
        



        out1.print(variableInfo.toString());
        variableInfo = null;
        rootElement = null;
        variables.clear();variables=null;

      }catch(WebRequestBadStatusException e){
        String msg="Unable to get file "+requestStr+". <br/><br/>\n"+e.getMessage()+"<br/>\n"+e.getResult();
        MessagePrinters.emailFatalErrorMessage("File access",msg);
        DebugConsole.errprintln(msg);
        JSONArray errorVar = new JSONArray ();
        try {
          JSONObject error = new JSONObject();
          error.put("error",msg);
          errorVar.put(error);
        } catch (JSONException e1) {}
        out1.print(errorVar.toString()+"\n\n\n");

      }catch(Exception e){
        String msg="Unable to get file "+requestStr+". <br/><br/>\n"+e.getMessage();
        MessagePrinters.emailFatalErrorMessage("File access",msg);
        DebugConsole.errprintln(msg);
        JSONArray errorVar = new JSONArray ();
        try {
          JSONObject error = new JSONObject();
          error.put("error",msg);
          errorVar.put(error);
        } catch (JSONException e1) {}
        out1.print(errorVar.toString()+"\n\n\n");
      }


      //MultiThreadedHttpConnectionManager. 
    }

  private void handleSearchRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
		response.setContentType("application/json");
		DebugConsole.println("SERVICE SEARCH: "+request.getQueryString());
		//Params: URLEncoded verquery, pagenumber, pagesize.
		// Mode=distinct|search
		// mode=distinct: category=any
		String mode=null;
		
		try{
	    HttpSession session = request.getSession();
		  SearchSession searchSession=(SearchSession) session.getAttribute("searchsession");
      if(searchSession==null){ 
        DebugConsole.println("Creating new searchsession");
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

				DebugConsole.println("inclFacets: "+includeFacets);
		
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
        JSONObject resultJSON = ESGFSearch.doCachedESGFSearchQuery(query,currentPage,pageSize,includeFacets,false,includeSearchResults,dataSetType);
//				DebugConsole.println("pageSize     : "+pageSize);
//				DebugConsole.println("currentPage  : "+currentPage);
				//DebugConsole.println("Start : "+start);
				
				DebugConsole.println("Query : "+query);
//				DebugConsole.println("ResultLength: "+resultJSON.length());
				PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e) {DebugConsole.errprint(e.getMessage());return;}
				 response.setContentType("application/json");
	    		out1.print(resultJSON.toString());
			}
			
			if(mode.equals("getfacet")){
			  String facet=HTTPTools.getHTTPParam(request,"facet");
			  
			  String queryStr="";try{ queryStr=HTTPTools.getHTTPParam(request,"query");}catch(Exception e){}
			  //DebugConsole.println("Facet to query = "+facet);
			  JSONObject resultJSON = ESGFSearch.getFacetForQuery(facet,queryStr+"&type="+dataSetType);
		     PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e) {DebugConsole.errprint(e.getMessage());return;}
         response.setContentType("application/json");
         out1.print(resultJSON.toString());
			}
		}catch(Exception e){
      DebugConsole.printStackTrace(e);
			DebugConsole.errprintln("Exception catched "+e.getMessage());

			JSONObject error = new JSONObject();
			try {
				error.put("error",e.getMessage());
			} catch (JSONException e2) {
				return;
			}
			PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e1) {DebugConsole.errprint(e1.getMessage());return;}
			out1.print(error.toString());
			return;
		}
		
    }
    
 
    
    private void handleBasketRequest(HttpServletRequest request, HttpServletResponse response,JSONMessageDecorator errorResponder) throws ServletException, IOException {
      
    	String mode=null;
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
        GenericCart basketList = null;
        try{
          response.setContentType("text/plain");
          basketList = User.getUser(request).getShoppingCart();
          String html = GenericCart.CartPrinters.showDataSetList(basketList,request);
          response.getWriter().println(html);
        }catch(Exception e){
          DebugConsole.printStackTrace(e);
          response.getWriter().println(e.getMessage());
        }
        return;
      }
      
      /**
       * Remove processor from status list
       */
      if(requestStr.equals("removeFromList")){
     
        GenericCart basketList = null;
        try{
          response.setContentType("text/plain");
          String procId=request.getParameter("id");
          if(procId!=null){procId=URLDecoder.decode(procId,"UTF-8");}else{errorResponder.printexception("id="+procId);return;}
          basketList = User.getUser(request).getShoppingCart();
          basketList.removeDataLocator(procId);
          response.setContentType("application/json");
          response.getWriter().println("{\"numproducts\":\""+(basketList.getNumProducts())+"\"}");
        }catch(Exception e){
          response.getWriter().println(e.getMessage());
        }
        return;      
      }
      
    	
    	
    	//Mode is add
    	try{
    	
    		mode=HTTPTools.getHTTPParam(request,"mode");
    		if(mode.equals("add")){
    		
    		  GenericCart shoppingCart = null;
    		  shoppingCart = User.getUser(request).getShoppingCart();
    		
    		  DebugConsole.println("Adding data to basket"+request.getParameter("id"));
    		  
    		  int currentNumProducts=shoppingCart.getNumProducts();
    		  
    		  if(request.getParameter("id")!=null){
    		    JSONObject el=new JSONObject();
    		    el.put("id", request.getParameter("id"));
    		    el.put("OPENDAP", request.getParameter("OPENDAP"));
    		    el.put("HTTPServer", request.getParameter("HTTPServer"));
    		    el.put("catalogURL", request.getParameter("catalogURL"));
    		    addFileToBasket(shoppingCart,el);
    		  }

    			
    			try{
      			JSONArray jsonData = (JSONArray) new JSONTokener(request.getParameter("json")).nextValue();
      			for(int j=0;j<jsonData.length();j++){
      				
      				JSONObject el=((JSONObject)jsonData.get(j));
      				addFileToBasket(shoppingCart,el);
      			}
    			}catch(Exception e){}
    			PrintWriter out1 = getPrintWriter(response);
    			String result = "{\"numproductsadded\":\""+(shoppingCart.getNumProducts()-currentNumProducts)+"\",";
    			result += "\"numproducts\":\""+(shoppingCart.getNumProducts())+"\"}";
    			
    			DebugConsole.println(result);
          response.setContentType("application/json");
    			out1.print(result);
    		}
    	}catch(Exception e){
      		JSONObject error = new JSONObject();
  			try {
  				error.put("error",e.getMessage());
  			} catch (JSONException e2) {
  				return;
  			}
  			PrintWriter out1 = null;try {out1 = response.getWriter();} catch (IOException e1) {DebugConsole.errprint(e1.getMessage());return;}
  			out1.print(error.toString());
  			return;
    	}
    }
    
    
    
 
    
	private void addFileToBasket(GenericCart shoppingCart,JSONObject el) throws JSONException {
	  DebugConsole.println(el.toString());
    String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    Calendar cal = Calendar.getInstance();
    String currentISOTimeString = sdf.format(cal.getTime())+"Z";
    long addDateMilli=cal.getTimeInMillis();
    String addDate=currentISOTimeString;
    DebugConsole.println("Adding dataset "+el.getString("id")+" with date "+addDate);
    JSONObject url = new JSONObject();
    try{
      String str=el.getString("OPENDAP");
      if(str.length()>0){
        url.put("OPENDAP",str);
      }
    }catch(Exception e){}
    try{
      String str=el.getString("HTTPServer");
      if(str.length()>0){
        url.put("HTTPServer",str);
      }
    }catch(Exception e){}
    try{
      String str=el.getString("catalogURL");
      if(str.length()>0){
        url.put("catalogURL",str);
      }
    }catch(Exception e){}
    
    DebugConsole.println("Data="+url.toString());
    
    shoppingCart.addDataLocator(el.getString("id"),url.toString(), addDate, addDateMilli,true);
      
  }



  static PrintWriter getPrintWriter(HttpServletResponse response) {
		PrintWriter out1 = null;
		try {out1 = response.getWriter();} catch (IOException e) {DebugConsole.errprint(e.getMessage());}
		return out1;
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  /*if(request.getQueryString()!=null){
	    DebugConsole.println("Request received query string "+request.getQueryString());
	  }*/
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
		  DebugConsole.println("Deprecated command");
		}
		
		/*
		 * Handle getvariables request for the variable browser.
		 */
		if(serviceStr.equals("getvariables")){
			handleVariablesRequest(request,response,errorResponder);
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
		if(serviceStr.equals("basicsearch")){
			BasicSearch.handleBasicSearchRequest(request,response,errorResponder);
		}
		//asfkasd[pgkiasd[pgksdgk]]

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
            DebugConsole.println("Creating new searchsession");
            searchSession = new SearchSession();session.setAttribute("searchsession",searchSession);
          }
          out.print(searchSession.getAsJSON());
          return;
        }
        out.print("{\"error\":\"Invalid mode\"}");
      } catch (IOException e) {
        DebugConsole.errprint(e.getMessage());
        return;
      } catch (Exception e) {
        DebugConsole.errprint(e.getMessage());
        return;
      }
    }
	}



  private void handleWMSRequests(HttpServletRequest request, HttpServletResponse response) {
	  DebugConsole.println("Handle WMS requests");
    OutputStream out1 = null;
    //response.setContentType("application/json");
    try {
      out1 = response.getOutputStream();
    } catch (IOException e) {
      DebugConsole.errprint(e.getMessage());
      return;
    }
  
    try {
      AdagucServer.runADAGUCWMS(request,response,request.getQueryString(),out1);

    } catch (Exception e) {
      response.setStatus(401);
      try {
        out1.write(e.getMessage().getBytes());
      } catch (IOException e1) {
        DebugConsole.errprintln("Unable to write to stream");
        DebugConsole.printStackTrace(e);
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

