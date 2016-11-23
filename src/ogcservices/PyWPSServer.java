package ogcservices;

import impactservice.Configuration;
import impactservice.LoginManager;
import impactservice.ImpactUser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import tools.CGIRunner;
import tools.Debug;
import tools.HTTPTools;
import tools.MyXMLParser;
import tools.Tools;
import wps.WebProcessingInterface;


public class PyWPSServer extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  /**
   * @see HttpServlet#HttpServlet()
   */
  public PyWPSServer() {
      super();
  }


  /**
   * Runs the PyWPS server as executable on the system. Emulates the behavior of scripts in a traditional cgi-bin directory of apache http server.
   * @param request The HTTP request with Query string and session information set.
   * @param response Optional, can be null, when given the content-type for the response will be set.
   * @param outputStream A standard byte output stream in which the data of stdout is captured. Can for example be response.getOutputStream().
   * @param dataToPost optional, can be null, when given this data is posted to the CGI instead.
   * @throws Exception
   */
  public static void runPyWPS(HttpServletRequest request,HttpServletResponse response,OutputStream outputStream,String queryString,String dataToPost) throws Exception{
    Debug.println("runPyWPS");
    String[] environmentVariables = Configuration.PyWPSServerConfig.getPyWPSEnvironment();

    //Try to get homedir
    String userHomeDir="";
    ImpactUser user = null;
 
    try{
      user = LoginManager.getUser(request);
      if(user == null)return;
      userHomeDir=user.getWorkspace();
      Debug.println("WPS for user: "+user.getUserId());
   
      
      if(userHomeDir.length()>0){
        environmentVariables=Tools.appendString( environmentVariables,"HOME="+userHomeDir);
      }else{
        throw new Exception("User : "+user.getUserId()+" has no home dir");
      }
      
      
      environmentVariables=Tools.appendString( environmentVariables,"SERVICE_ADAGUCSERVER="+Configuration.getHomeURLHTTPS()+"/adagucserver?");
      environmentVariables=Tools.appendString( environmentVariables,"CAPATH="+ Configuration.LoginConfig.getTrustRootsLocation());
     
      
      String userDataDir = user.getDataDir();
      Tools.mksubdirs(userDataDir+"/WPS_Scratch/");
      environmentVariables=Tools.appendString( environmentVariables,"POF_OUTPUT_PATH="+userDataDir+"/WPS_Scratch/");
      
      String pofOutputURL = Configuration.getHomeURLHTTPS()+"/DAP/"+user.getUserId()+"/WPS_Scratch/";
      pofOutputURL = HTTPTools.makeCleanURL(pofOutputURL);
      pofOutputURL = pofOutputURL.replace("?", "");
      environmentVariables=Tools.appendString( environmentVariables,"POF_OUTPUT_URL="+pofOutputURL);
    }catch(Exception e){
      //OK... no user info. Only doing statuslocation requests.
      
    }
    //Try to get query string
    if(queryString == null){
      queryString = request.getQueryString();
    }
                            
    if(queryString!=null){
      if(queryString.length()>0){
        environmentVariables=Tools.appendString( environmentVariables,"QUERY_STRING="+queryString);
      }
    }
  
    //  Check for status location first.
    if(queryString!=null){ 
      String output = HTTPTools.getKVPItem(queryString, "OUTPUT");
      if(output!=null){
        String env[] = Configuration.PyWPSServerConfig.getPyWPSEnvironment();
        String portalOutputPath = null;
        for(int j=0;j<env.length;j++){
          String []kvp = env[j].split("=");
          if(kvp.length == 2){
            if(kvp[0].equals("PORTAL_OUTPUT_PATH")){
              portalOutputPath = kvp[1];
            }
          }
        }
  
        
  
        //Remove first "/" token;
        output = output.substring(1);
        portalOutputPath = Tools.makeCleanPath(portalOutputPath);
        String fileName = portalOutputPath+"/"+output;
        Debug.println("WPS GET status request: "+fileName);
  
        Tools.checkValidCharsForFile(output);
        String data = Tools.readFile(fileName);
        if(response!=null){
          response.setContentType("text/xml");
        }
        outputStream.write(data.getBytes());          
        return;
      }
    }
    
    if(user == null){
      Debug.println("Anonymous WPS request received, I am stopping");
      if(response!=null)response.setStatus(401);
      outputStream.write(new String("401: Unauthorized user\n").getBytes());
      return;
    }
      
    //Get the pywps location
    String commands[] = Configuration.PyWPSServerConfig.getPyWPSExecutable();
    Debug.println("PyWPSExec:"+Configuration.PyWPSServerConfig.getPyWPSExecutable()[0]);
    Debug.println("queryString:"+queryString);
    
    
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    
    CGIRunner.runCGIProgram(commands,environmentVariables,userHomeDir,response,os,dataToPost);
    
    _saveJobSettingsForUser(dataToPost,queryString,user,os);
    
    outputStream.write(os.toByteArray());
  }
  
  private static void _saveJobSettingsForUser(String dataToPost, String queryString, ImpactUser user, ByteArrayOutputStream os) {
    //Try to convert a GET Execute into post, because this is how we store it in the joblist.
    try{
      if(dataToPost==null&&queryString!=null){
        String requestStr = HTTPTools.getKVPItem(queryString, "request");
        if(requestStr.equalsIgnoreCase("execute")){
          //Debug.println("GET EXECUTE DETECTED, CONVERTING TO POST ["+queryString+"]");
          String dataInputs = HTTPTools.getKVPItem(queryString, "datainputs");
          String procId = HTTPTools.getKVPItem(queryString, "identifier");
          //Debug.println("dataInputs :["+dataInputs+"]");
          //Debug.println("procId :["+procId+"]");
          if(dataInputs!=null && procId!=null){
            dataToPost=convertQueryStringToPost(dataInputs,procId);
          }
          //Debug.println("DATATOPOST :["+dataToPost+"]");
        }
      }
    }catch(Exception e){
      Debug.printStackTrace(e);
    }
    
    //If this is an execute, store the job!
    if(dataToPost!=null){
      try{
        //Debug.println("PostData:["+dataToPost+"]");
        //Debug.println("Reponse:["+os.toString()+"]");
        MyXMLParser.XMLElement  wpsExecuteResponseDocument = new MyXMLParser.XMLElement();
        wpsExecuteResponseDocument.parseString(os.toString());
        WebProcessingInterface.trackJobForUser(user,wpsExecuteResponseDocument,dataToPost);
      }catch(Exception e){
        Debug.printStackTrace(e);
      }
    }
    
  }


  private static void _handleWPSRequests(HttpServletRequest request, HttpServletResponse response) {
    Debug.println("Handle WPS requests");
    OutputStream out1 = null;
    //response.setContentType("application/json");
    try {
      out1 = response.getOutputStream();
    } catch (IOException e) {
      Debug.errprint(e.getMessage());
      return;
    }
  
    try {
      
      String postData = null;
      StringBuffer jb = new StringBuffer();
      String line = null;
      try {
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
          jb.append(line+"\n");
      } catch (Exception e) {  }
      postData = jb.toString();
      jb = null;
      PyWPSServer.runPyWPS(request,response,out1,null,postData);

    } catch (Exception e) {
      Debug.printStackTrace(e);
      response.setStatus(401);
      try {
        if(e.getMessage()!=null){
          out1.write(e.getMessage().getBytes());
          Debug.errprintln(e.getMessage());
        }
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
    Debug.println("WPS POST request received");
    
    _handleWPSRequests(request,response);
  }
  
  protected void  doGet(HttpServletRequest request, HttpServletResponse response) {
    Debug.println("WPS GET request received");
    _handleWPSRequests(request,response);
  }
  

  private static String _addLiteralData(String identifier,String value){
    String data="";
    
    if(value!=null){
      String [] values = value.split(",");
      for(int j=0;j<values.length;j++){
        data+=  "        <wps:Input>\n";
        data+=  "          <ows:Identifier>"+identifier+"</ows:Identifier>\n";
        data+=  "          <wps:Data>\n";
        data+=  "            <wps:LiteralData>"+values[j]+"</wps:LiteralData>\n";
        data+=  "          </wps:Data>\n";
        data+=  "        </wps:Input>\n";
      }
    }else{
      data+=  "        <wps:Input>\n";
      data+=  "          <ows:Identifier>"+identifier+"</ows:Identifier>\n";
      data+=  "          <wps:Data>\n";
      data+=  "          </wps:Data>\n";
      data+=  "        </wps:Input>\n";
    }
    return data;
  }
  
  public static String convertQueryStringToPost(String dataInputs,String procId) throws Exception {
    String postData = "";
    postData+=URLEncoder.encode("service","UTF-8")+"="+URLEncoder.encode("WPS","UTF-8");
    postData+="&"+URLEncoder.encode("version","UTF-8")+"="+URLEncoder.encode("3.5","UTF-8");
    //URLEncoder.encode("service=WPS&version=1.0.0&request=execute&identifier="+procId+"&datainputs=[startIndex=1;stopIndex=100]","UTF-8")
    
    postData
       ="<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">\n"
       +"      <ows:Identifier>"+procId+"</ows:Identifier>\n"
       +"      <wps:ResponseForm>\n"
       +"        <wps:ResponseDocument storeExecuteResponse=\"true\" status=\"true\">\n"
       //+"          <wps:Output asReference=\"false\">\n"
       //+"            <ows:Identifier>"+procId+"</ows:Identifier>\n"
       //+"          </wps:Output>\n"
       +"        </wps:ResponseDocument>\n"
       +"      </wps:ResponseForm>\n";

    String trimmedInput=dataInputs.trim();
    Debug.println("DataInputs="+trimmedInput);
    if(trimmedInput.equals("[]")==false){
      postData   +=   "      <wps:DataInputs>\n";
      //Remove [] and split on ","
      int startBracket = 0;
      int stopBracket = trimmedInput.length();
      if(trimmedInput.charAt(0)=='['){
        startBracket++;
      }
      if(trimmedInput.charAt(stopBracket-1)==']'){
        stopBracket--;
      }
      String [] dataInputArray=trimmedInput.substring(startBracket,stopBracket).split(";");
      for(int j=0;j<dataInputArray.length;j++){
        //KVP key=value
        
        
        dataInputArray[j] = dataInputArray[j].split("#")[0];
        //Debug.println(dataInputArray[j]);
        try{
          int equalSignIndex = dataInputArray[j].indexOf('=');
          String key = dataInputArray[j].substring(0,equalSignIndex);
          
          if(equalSignIndex==-1){
            postData+=_addLiteralData(key,"");
          }else{
            String value =  dataInputArray[j].substring(equalSignIndex+1);;
//            String v= URLEncoder.encode(value,"utf-8");
//            Debug.println(value);
            postData+=_addLiteralData(key,value);
          }
        }catch(Exception e){
          e.printStackTrace();
          Debug.errprintln("error");
          throw new Exception("Invalid values given for '"+dataInputArray[j]+"' \nCause: "+e.toString());  
        }
      }
      postData     +=  "      </wps:DataInputs>\n";
    }
    postData     +=  "    </wps:Execute>\n";
    return postData;
  }
}
