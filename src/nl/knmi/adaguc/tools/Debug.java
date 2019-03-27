package nl.knmi.adaguc.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


public class Debug {
  static boolean showFileAndLineNumber = true;
  protected static String getSignature(StackTraceElement trace) {
    if(!showFileAndLineNumber||trace==null){return "";}
    //String classNameParts[] = trace.getClassName().split("\\.");
    String signature = "(UNKNOWN)";
    try{
       signature = "(" + trace.getFileName() + ":"
        + trace.getLineNumber() + ") ";
    }catch(Exception e){
      
    }

    return signature;
  }

  
  public static void printStackTrace(Exception e){
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    e.printStackTrace(printWriter);
    String signature = getSignature(getCaller(3)) ;
    System.err.print(signature+result.toString());
  }

  protected static StackTraceElement getCaller(int depth) {
    if(!showFileAndLineNumber){return null;}
    String thisClassName = Watcher.class.getSimpleName();
    StackTraceElement[] traces = new Throwable().getStackTrace();
    int foundN = depth;
    for (StackTraceElement trace : traces) {
      String traceClassName = trace.getClassName();
      boolean found = isTargetTrace(traceClassName, thisClassName, null);
      if (foundN == 0)
        return trace;
      if (found)
        foundN--;

    }
    return null;
  }

  private static boolean isTargetTrace(String traceClassName,
      String pWatcherName, String pIgnoredClassNames[]) {
    if (traceClassName.endsWith(pWatcherName) == true)
      return false;
    if (traceClassName.contains("$") == true)
      return false;
    if (pIgnoredClassNames == null)
      return true;
    for (String ignoredClassName : pIgnoredClassNames) {
      if (traceClassName.endsWith(ignoredClassName) == true)
        return false;
    }
    return true;
  }
  static public void print(String message){
    String signature = getSignature(getCaller(2)) ;
    System.out.print(signature+message);
  }
  static public void println(String message){
    String signature = getSignature(getCaller(2)) ;
    System.out.println(signature+message);
  }
  
  
  static public void errprint(String message){
    String signature = getSignature(getCaller(2)) ;
    System.err.print(signature+message);
  }
  static public void errprintln(String message){
    String signature = getSignature(getCaller(2)) ;
    System.err.println(signature+message);
  }


  public static void throwMessage(String string) throws Exception {
    errprintln(string);
    throw new Exception(string);
    
  }


  public static void println(String string, Exception e) {
    println(string+" Message: "+e.getMessage());
  }

  public static void errprintln(String string, Exception e) {
    errprintln(string+" Message: "+e.getMessage());
  }

}
