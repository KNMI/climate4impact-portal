package tools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;


public class CGIRunner {
  
  /**
   * Runs a CGI program as executable on the system. Emulates the behavior of scripts in a traditional cgi-bin directory of apache http server.
   * @param commands A string array with the executable and the arguments to this executable
   * @param environmentVariables The environment variables used for this run
   * @param response Can be null, when given the content-type for the response will be set.
   * @param outputStream A standard byte output stream in which the data of stdout is captured. Can for example be response.getOutputStream().
   * @param postData The data to post.
   * @throws Exception
   */
  public static void runCGIProgram(String[] commands,String[] environmentVariables,final HttpServletResponse response,OutputStream outputStream,String postData) throws Exception{
    class StderrPrinter implements ProcessRunner.StatusPrinterInterface{public void print(byte[] message,int bytesRead) {DebugConsole.errprint(new String(message));}}
    class StdoutPrinter implements ProcessRunner.StatusPrinterInterface{
      boolean headersSent = false;
      OutputStream output = null;
      String header="";
      public StdoutPrinter(OutputStream outputStream) {
        output=outputStream;
      }
  
     // boolean a = false;
      public void print(byte[] message,int bytesRead) {
    /*    if(a==false){
          a=true;
          for(int j=0;j<bytesRead;j++){
            DebugConsole.println(j+":="+message[j]+" = "+new String(message).substring(j,j+1));
          }
        }*/
        //Try to extract HTML headers and Content-Type 
        if(headersSent==false){
          int endHeaderIndex=0;
          for(int j=0;j<bytesRead;j++){
            if(j>0){
              if((message[j-1]==13&&message[j]==10)||(message[j]==10&&message[j+1]==10)){
                headersSent=true;
                endHeaderIndex=j;
                break;
              }
            }
            header+=(char)message[j];
          }
          if(headersSent){
           
           
            //DebugConsole.println("Found header:\n'"+header+"'");
            String[] headerItem=header.split("\n");
            for(int h=0;h<headerItem.length;h++){
              String[] headerKVP=headerItem[h].split(":");
              if(headerKVP.length==2){
//                DebugConsole.println(headerKVP[0].trim()+"="+headerKVP[1].trim());
                if(headerKVP[0].trim().equalsIgnoreCase("Content-Type")){
                  if(response!=null){
                    headerKVP[1]=headerKVP[1].replaceAll("\\n", "");
                    headerKVP[1]=headerKVP[1].replaceAll("\\r", "");
                    headerKVP[1]=headerKVP[1].replaceAll(" ", "");
                    //DebugConsole.println("Setting Content-Type to '"+headerKVP[1].trim()+"'");
                    response.setContentType(headerKVP[1].trim());
                  }
                }else{
                  DebugConsole.println("Setting header "+headerKVP[0].trim()+"="+headerKVP[1].trim());
                  response.setHeader(headerKVP[0].trim(),headerKVP[1].trim());
                }
              }
            }
            try {
              //System.out.write(message, endHeaderIndex+2,bytesRead-(endHeaderIndex+2));
              output.write(message, endHeaderIndex+2,bytesRead-(endHeaderIndex+2));
            } catch (IOException e) {
            }
          }
        }else{
          //Content Type found, now just forward the data
          try {
            output.write(message,0,bytesRead);
          } catch (IOException e) {}
        }
      }
    }
  
    ProcessRunner.StatusPrinterInterface stdoutPrinter = new StdoutPrinter(outputStream);
    ProcessRunner.StatusPrinterInterface stderrPrinter = new StderrPrinter();


    /*for(int j=0;j<environmentVariables.length;j++){
      DebugConsole.println(environmentVariables[j]);
    }*/
    ProcessRunner processRunner = new ProcessRunner (stdoutPrinter,stderrPrinter,environmentVariables);
    
  

    long startTimeInMillis = Calendar.getInstance().getTimeInMillis();  
    DebugConsole.println("Starting CGI.");
    try{
      //postData
      processRunner.runProcess(commands,postData);
    }catch(Exception e){
      DebugConsole.errprintln(e.getMessage());
      throw(e);
    }
    if(processRunner.exitValue()!=0){
      DebugConsole.errprintln("Warning: exit code: "+processRunner.exitValue());
    }else{
      DebugConsole.errprintln("Exit code 0 == OK");
    }
    
    if(response!=null){
      if(processRunner.exitValue()!=0){
        response.setStatus(500);
        String msg="Internal server error: CGI returned code "+processRunner.exitValue();
        DebugConsole.errprintln(msg);
        outputStream.write(msg.getBytes());
      }
    }
    
    long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
    DebugConsole.println("Finished CGI: "+" ("+(stopTimeInMillis-startTimeInMillis)+" ms)");
  }
}
