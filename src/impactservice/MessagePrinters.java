package impactservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;

import tools.Debug;
import tools.SendMail;

public class MessagePrinters {
  public static void printWarningMessage(JspWriter out,Exception exception) throws IOException{
    printWarningMessage(out,"default",exception);
  }
  public static void emailFatalErrorException(String subject,Exception exception) throws IOException{
    try {
      String[] to=Configuration.Admin.getEmailAddresses();
      final Writer result = new StringWriter();
      final PrintWriter printWriter = new PrintWriter(result);
      exception.printStackTrace(printWriter);
      String msg=exception.getMessage()+"\n\n"+result.toString();
      Debug.errprintln(msg);
      SendMail.sendMail(to,"c4i@climate4impact.eu","[CLIMATE4IMPACT:"+subject+"]", msg);
      
    } catch (Exception e) {
      Debug.errprintln(e.getMessage());
    }

  }
  public static void emailFatalErrorException(String subject,Throwable exception) throws IOException{
    try {
      String[] to=Configuration.Admin.getEmailAddresses();
      final Writer result = new StringWriter();
      final PrintWriter printWriter = new PrintWriter(result);
      exception.printStackTrace(printWriter);
      String msg=exception.getMessage()+"\n\n"+result.toString();
      Debug.errprintln(msg);
      SendMail.sendMail(to,"c4i@climate4impact.eu","[CLIMATE4IMPACT:"+subject+"]", msg);
      
    } catch (Exception e) {
      Debug.errprintln(e.getMessage());
    }

  }
  
  public static void emailFatalErrorMessage(String subject,String message) throws IOException{
    try {
      String[] to=Configuration.Admin.getEmailAddresses();
      Debug.errprintln(message);
      SendMail.sendMail(to,"c4i@climate4impact.eu","[CLIMATE4IMPACT: FATAL ERROR]: "+subject, message);
      
    } catch (Exception e) {
      Debug.errprintln(e.getMessage());
    }

  }
  public static void printWarningMessage(JspWriter out,String subject,Exception exception) throws IOException{
 
    emailFatalErrorException(subject,exception);
//    out.print("<div class=\"error\"><p class=\"error\"><table class=\"error\"><tr><td class=\"error\" style=\"padding:10px;\"><img src=\"/"+Configuration.getHomeURLPrefix()+"/images/warning.png\"/></td><td class=\"error\">");
//    out.print(tools.HTMLParser.textToHTML(exception.getMessage()));
//    out.print("</td></tr></table></p></div>");
    String message = tools.HTMLParser.textToHTML(exception.getMessage());
    out.print("<div class=\"alert-box warning\"><span>warning: </span>"+message+"</div>");
  }
}
