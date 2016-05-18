package impactservice;
import impactservice.SessionManager.DatasetViewerSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.JSONResponse;
import tools.MyXMLParser;
import tools.MyXMLParser.XMLElement;


public class OpendapViewer {
 
  String cacheLocation = null;
  public OpendapViewer(String cacheLocation) {

    this.cacheLocation = cacheLocation;
  }

 


  private JSONResponse viewOpenDap(String requestStr,HttpServletRequest request, HttpServletResponse response){
    JSONResponse jsonResponse = new JSONResponse(request);
    /* Check if we really have an URL here and not a localfile */
    String prefixCheck = requestStr.toLowerCase();
    if(prefixCheck.startsWith("http")==false&&prefixCheck.startsWith("dods")==false){
      jsonResponse.setErrorMessage("Invalid opendap URL given, no HTTP or DODS prefix",500);
      return jsonResponse;
    }
    HttpSession session = request.getSession();
    DatasetViewerSession datasetViewerSession=(DatasetViewerSession) session.getAttribute("datasetviewersession");
    if(datasetViewerSession==null){ 
      Debug.println("Creating new datasetviewersession");
      datasetViewerSession = new DatasetViewerSession();session.setAttribute("datasetviewersession",datasetViewerSession);
    }
    datasetViewerSession.datasetURL=requestStr;
    ImpactUser user = null;

    try{
      //Strip the # token.
      requestStr = requestStr.split("#")[0];
      Debug.println("dodsRequest="+requestStr);

      String ncdumpMessage = "";
   
      try{
        user=LoginManager.getUser(request,response);
        Debug.println("INFO: User logged in: "+user.getInternalName());
//        if(user == null){
//          jsonResponse.setErrorMessage("Unable to get user from LoginManager",500);
//        }
      }catch(Exception e){
        Debug.println("INFO: User NOT logged in");
      }
      

      
      
      ncdumpMessage=NetCDFC.executeNCDumpCommand(user,requestStr);
      
      
    
      if(ncdumpMessage==""){
        try{
          jsonResponse = LoginManager.identifyWhyGetRequestFailed(requestStr+".ddx",request,response);
        }catch(WebRequestBadStatusException e2){
        }
        return jsonResponse;
      }
      
      Debug.println("Trying to parse ncdump message");
      MyXMLParser.XMLElement rootElement = new MyXMLParser.XMLElement();
      rootElement.parseString(ncdumpMessage);
      Debug.println("Parsed");
      //DebugConsole.println(rootElement.toString());

      List<XMLElement>dimensions = rootElement.get("netcdf").getList("dimension");
      List<XMLElement>variables = rootElement.get("netcdf").getList("variable");
      
      JSONArray variableInfo = new JSONArray (); 
      
      JSONObject jsonVariable = new JSONObject();

      String varName= "nc_global";
      jsonVariable.put("variable",varName);//.getName());
      jsonVariable.put("longname","File metadata");//.getName());

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


    
        for(int iterateOption=0;iterateOption<2;iterateOption++){
        for(int j=0;j<variables.size();j++){
          boolean isDimension = false;
          
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
                
                boolean show = true;
                for(String dim : varDimensions){
                  if(dim.equals("maxStrlen64")||dim.equals("ngrids")){
                    show=false;break;
                  }
                  boolean foundDim = false;
                  for(int i=0;i<variables.size();i++){
                    if(variables.get(i).getAttrValue("name").equals(dim)){
                      foundDim=true;break;
                    }
                  }
                  if(foundDim==false){show=false;break;}
                }
                if(show){
                  if(variables.get(j).getAttrValue("name").equals("lon")==false&&
                      variables.get(j).getAttrValue("name").equals("lat")==false&&
                      variables.get(j).getAttrValue("name").equals("Actual_latitude")==false&&
                      variables.get(j).getAttrValue("name").equals("Actual_longitude")==false
                      ){
                    jsonVariable1.put("isViewable",1);
                  }
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
                    isDimension = true;
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
          jsonVariable1.put("longname",longName);
          
          //if(attributes1.size()>0){
          if(isDimension&&iterateOption==0){
            variableInfo.put(jsonVariable1);
          }else if(!isDimension&&iterateOption==1){
            variableInfo.put(jsonVariable1);
          }
          //}
          
        }
      }
      
      


      jsonResponse.setMessage(variableInfo.toString());

      variableInfo = null;
      rootElement = null;
      variables.clear();variables=null;

    /*}catch(WebRequestBadStatusException e){
      String msg="Unable to get file "+requestStr+". <br/><br/>\n"+e.getMessage()+"<br/>\n";//+e.getResult();
      MessagePrinters.emailFatalErrorMessage("File access",msg);
      DebugConsole.errprintln(msg);
      JSONArray errorVar = new JSONArray ();
      try {
        JSONObject error = new JSONObject();
        error.put("error",msg);
        errorVar.put(error);
      } catch (JSONException e1) {}
      out1.print(errorVar.toString()+"\n\n\n");

    */
    }catch(Exception e){
      
      String requestStrWithOpenId = requestStr;
      if(user!=null){
        if(user.getOpenId()!=null){
          if(requestStrWithOpenId.indexOf("?")==-1){
            requestStrWithOpenId +="?";
          }else{
            requestStrWithOpenId +="&";
          }
          try {
            requestStrWithOpenId+="openid="+URLEncoder.encode(user.getOpenId(),"utf-8");
          } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
          }
        }
      }
      String msg="Unable to get file <a target=\"_blank\" href=\""+requestStrWithOpenId+"\">"+requestStr+"</a>.\n\n"+e.getMessage();
      
      String userId = "No user.";
      if(user!=null){
        userId = user.getOpenId();
      }
      try {
        MessagePrinters.emailFatalErrorMessage("File access: "+e.getMessage(),msg+"\nUser: '"+userId+"'");
      } catch (IOException e2) {
        e2.printStackTrace();
      }
      //DebugConsole.errprintln(msg);
      JSONArray errorVar = new JSONArray ();
      try {
        JSONObject error = new JSONObject();
        msg = msg.replaceAll("esg-orp", "impactportal/esg-orp");
        msg = msg.replaceAll("\n", "<br/>");
        
        error.put("error",msg);
        errorVar.put(error);
      } catch (JSONException e1) {}
      jsonResponse.setErrorMessage(errorVar.toString()+"\n\n\n",500);

    }



    return jsonResponse;
  }
  
  

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      String service=HTTPTools.getHTTPParam(request,"service");
      if("getvariables".equals(service)){
        String query=HTTPTools.getHTTPParam(request,"request");
        
        String jsonp = null;
        try{
          jsonp=HTTPTools.getHTTPParam(request,"jsonp");
        }catch (Exception e) {
          try{
            jsonp=HTTPTools.getHTTPParam(request,"callback");
          }catch(Exception e2){
          }
        }
  
  
          JSONResponse jsonresponse = viewOpenDap(query,request,response);
          jsonresponse.setJSONP(jsonp);
          response.setContentType(jsonresponse.getMimeType());
          response.getOutputStream().print(jsonresponse.getMessage());
      }
      
    } catch (Exception e) {
    }
  }
}
