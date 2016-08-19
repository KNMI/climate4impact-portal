package stats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import impactservice.Configuration;
import tools.Debug;

public class StatLogger {


  public static void logger(HttpServletRequest request, String sessionType,  String userId) {
    String logfile = Configuration.getStatLogfile();
    if(logfile == null)return;
    String logString = tools.DateFunctions.getCurrentDateInISO8601()+":"+userId+":"+sessionType+":"+request.getServletPath()+":"+request.getPathInfo()+":"+request.getQueryString();
    try(FileWriter fw = new FileWriter(logfile, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw))
    {
        out.println(logString);
    } catch (IOException e) {
       Debug.errprintln("Unable to write logfile");
    }
  }
}
