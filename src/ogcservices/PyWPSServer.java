package ogcservices;

import impactservice.Configuration;
import impactservice.LoginManager;
import impactservice.ImpactUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.CGIRunner;
import tools.Debug;
import tools.HTTPTools;
import tools.Tools;


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
    String[] environmentVariables = Configuration.PyWPSServerConfig.getPyWPSEnvironment();

    //Try to get homedir
    String userHomeDir="";
    ImpactUser user = null;
   
      user = LoginManager.getUser(request,response);
      if(user == null)return;
      userHomeDir=user.getWorkspace();
      Debug.println("WPS for user: "+user.getId());
 
    
    if(userHomeDir.length()>0){
      environmentVariables=Tools.appendString( environmentVariables,"HOME="+userHomeDir);
    }else{
      throw new Exception("User : "+user.getId()+" has no home dir");
    }
    
    String userDataDir = user.getDataDir();
    environmentVariables=Tools.appendString( environmentVariables,"POF_OUTPUT_PATH="+userDataDir);
    
    String pofOutputURL = Configuration.getHomeURLHTTPS()+"/DAP/"+user.internalName+"/";
    pofOutputURL = HTTPTools.makeCleanURL(pofOutputURL);
    pofOutputURL = pofOutputURL.replace("?", "");
    environmentVariables=Tools.appendString( environmentVariables,"POF_OUTPUT_URL="+pofOutputURL);
    
    //Try to get query string
    if(queryString == null){
      queryString = request.getQueryString();
    }
                            
    if(queryString!=null){
      if(queryString.length()>0){
        environmentVariables=Tools.appendString( environmentVariables,"QUERY_STRING="+queryString);
      }
    }
    
    //Get the pywps location
    String commands[] = Configuration.PyWPSServerConfig.getPyWPSExecutable();
    Debug.println("PyWPSExec:"+Configuration.PyWPSServerConfig.getPyWPSExecutable()[0]);
    

    
    
    
    CGIRunner.runCGIProgram(commands,environmentVariables,userHomeDir,response,outputStream,dataToPost);
  }
  
  private void handleWPSRequests(HttpServletRequest request, HttpServletResponse response) {
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
    
    handleWPSRequests(request,response);
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    
    Debug.println("WPS QueryString: "+request.getQueryString());
    //Check if this is a WPS status request, which means reading a local XML file which resides on disk.
    try{
        String output = null;
        output = request.getParameter("OUTPUT");
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
          Debug.println("WPS GET status request: "+portalOutputPath+output);
          OutputStream out1 = null;
          String data;
          try{
            Tools.checkValidCharsForFile(output);
            data = Tools.readFile(fileName);
          }catch(Exception e){
            Debug.errprintln(e.getMessage());
            try {
              out1 = response.getOutputStream();
            } catch (IOException e1) {
              Debug.errprint(e1.getMessage());
              return;
            }
            response.setContentType("text/html");
            out1.write(("Invalid file: "+output).getBytes());   
            return;
          }
         try {
            out1 = response.getOutputStream();
          } catch (IOException e) {
            Debug.errprint(e.getMessage());
            return;
          }
          response.setContentType("text/xml");
          out1.write(data.getBytes());          
          return;
        }
    }catch(Exception e){
    }
  
    //Otherwise, just call pyWPS
    Debug.println("WPS GET request received");
    handleWPSRequests(request,response);
  }

}
