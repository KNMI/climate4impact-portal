package ogcservices;

import impactservice.Configuration;
import impactservice.LoginManager;
import impactservice.ImpactUser;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.CGIRunner;
import tools.DebugConsole;
import tools.Tools;


public class AdagucServer {

  /**
   * Runs the ADAGUC WMS server as executable on the system. 
   * Emulates the behavior of scripts in a traditional cgi-bin directory of apache http server.
   * @param response Can be null, when given the content-type for the response will be set. 
   * Results are not sent to this stream, this is done by outputStream parameter
   * @param queryString The querystring for the CGI script
   * @param outputStream A standard byte output stream in which the data of stdout is captured. 
   * Can for example be response.getOutputStream().
   * @throws Exception
   */
  public static void runADAGUCWMS(HttpServletRequest request,HttpServletResponse response,String queryString,OutputStream outputStream) throws Exception{
    String[] environmentVariables = Configuration.ADAGUCServerConfig.getADAGUCEnvironment();

    //Try to get homedir, ignore if failed
    String userHomeDir="";
    try{
      ImpactUser user = LoginManager.getUser(request,response);
      if(user == null)return;
      userHomeDir=user.getWorkspace();
      DebugConsole.println("WMS for user: "+user.id);
    }catch(Exception e){    
      DebugConsole.println("Warning: Anonymous user: '"+e.getMessage()+"'");
    }

    environmentVariables=Tools.appendString( environmentVariables,"HOME="+userHomeDir);
    environmentVariables=Tools.appendString( environmentVariables,"QUERY_STRING="+queryString);
    
    String commands[] = Configuration.ADAGUCServerConfig.getADAGUCExecutable();
    DebugConsole.println("ADAGUCExec"+Configuration.ADAGUCServerConfig.getADAGUCExecutable()[0]);
    CGIRunner.runCGIProgram(commands,environmentVariables,response,outputStream,null);
  }

}
