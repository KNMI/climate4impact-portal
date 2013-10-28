package impactservice;

import tools.DebugConsole;
import tools.ProcessRunner;

public class NetCDFC {
  static class NCDump{
    private String ncdumpResult="";
    class StderrPrinter implements ProcessRunner.StatusPrinterInterface{public void print(byte[] message,int bytesRead) {DebugConsole.errprint(new String(message));}}
    class StdoutPrinter implements ProcessRunner.StatusPrinterInterface{public void print(byte[] message,int bytesRead) {
        ncdumpResult+=new String(message,0,bytesRead);

      }
    }

     
    public String doNCDump(User user,String url) throws Exception{
      ncdumpResult="";
     
     
      ProcessRunner.StatusPrinterInterface stdoutPrinter = new StdoutPrinter();
      ProcessRunner.StatusPrinterInterface stderrPrinter = new StderrPrinter();
      String userHome="";
      try{
        userHome=user.getWorkspace();
      }catch(Exception e){
      }
      String[] environmentVariables = {"HOME="+userHome};
      ProcessRunner processRunner = new ProcessRunner (stdoutPrinter,stderrPrinter,environmentVariables);
      try{
        String commands[]={"ncdump","-h","-x",url};
        DebugConsole.println("starting ncdump for "+url);
        processRunner.runProcess(commands,null);
      }
      catch (Exception e){
        DebugConsole.errprint(e.getMessage());
        throw new Exception ("Unable to do ncdump: "+e.getMessage());
      }
      return ncdumpResult;        
    }
  }
  
  
  
  public static String ncdump(User user,String url) throws Exception{
    String result;
    NCDump doNCDump = new NCDump();
    result=doNCDump.doNCDump(user,url);
    doNCDump = null;
    return result;
  }
}
