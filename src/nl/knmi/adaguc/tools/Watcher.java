package nl.knmi.adaguc.tools;

/** 
 *  Copyright (c) 2010  Ng Pan Wei
 *  
 *  Permission is hereby granted, free of charge, to any person 
 *  obtaining a copy of this software and associated documentation files 
 *  (the "Software"), to deal in the Software without restriction, 
 *  including without limitation the rights to use, copy, modify, merge, 
 *  publish, distribute, sublicense, and/or sell copies of the Software, 
 *  and to permit persons to whom the Software is furnished to do so, 
 *  subject to the following conditions: 
 *  
 *  The above copyright notice and this permission notice shall be 
 *  included in all copies or substantial portions of the Software. 
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 *  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
 *  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 *  SOFTWARE. 
 */ 


import java.util.HashMap;

/**
* For getting the signature of an executing line, the caller and so on.
* Supports logging and hyper-linking through Eclipse error parser.
* @author panwei
*/
public class Watcher {
       /**
        * Get string signature of current executing line.
        * @return
        */
       public static String getSignature() {
               StackTraceElement trace = getCaller(null)  ;
               String signature = getSignature(trace) ;
               return signature;
       }
       /**
        * Get the signature of the calling class.
        * Count the number of classes == 3 in the normal case.
        * Count the number of classes == 5 if it goes through a dyanmic proxy.
        * This is highly dependent on invocation sequence.
        * - This watcher class, the client class, and the calling class above the client.
        * @return
        */
       public static String getCallingClassSignature(int level) {
               StackTraceElement callingTrace = getCaller(level) ;
               if(callingTrace==null) 
                       return "No Caller" ;
               return getSignature(callingTrace) ;
       }
       /**
        * Get the signature of stack trace conforming to eclipse error parser.
        * @return
        */
       protected static String getSignature(StackTraceElement trace) {
               String classNameParts[] = trace.getClassName().split("\\.");
               String className = classNameParts[classNameParts.length-1] ;
               String signature = className+"."+trace.getMethodName();
               signature += "(" + trace.getFileName() + ":" + trace.getLineNumber() + ")" ;
               return signature ;              
       }
       /**
        * Get the caller by scanning the stack trace.
        * Ignores this class and classes provided by caller.
        * @return
        */
       protected static StackTraceElement getCaller(String pIgnoredClassNames[]) {
               String thisClassName = Watcher.class.getSimpleName() ;
               StackTraceElement[] traces = new Throwable().getStackTrace() ;
               for(StackTraceElement trace : traces) {
                       String traceClassName = trace.getClassName() ;
                       boolean found = isTargetTrace(traceClassName,
                            thisClassName,pIgnoredClassNames) ;
                       if(found) return trace ;
               }
               return null ;
       }
       /**
        * Get caller of the class above from the bottom of the call stack.
        * Levels are computed by the different classes.
        * @param pClassLevel
        * @return
        */
       protected static StackTraceElement getCaller(int pClassLevel) {
               HashMap<String,String> classCounter = new HashMap<String,String>() ;
               StackTraceElement[] traces = new Throwable().getStackTrace() ;
               StackTraceElement callingTrace = null ;
               for(StackTraceElement trace : traces) {
                       String traceClassName = trace.getClassName() ;
                       classCounter.put(traceClassName, "");
                       if(classCounter.size()>pClassLevel) {
                               callingTrace = trace ;
                               break ;
                       }
               }
               return callingTrace ;
               
       }
       /**
        * Part of trace scanning to determine if the trace is to be ignored.
        * @param traceClassName
        * @param pWatcherName
        * @param pIgnoredClassNames
        * @return
        */
       private static boolean isTargetTrace(String traceClassName,
                                                       String pWatcherName,String pIgnoredClassNames[]) {
               if(traceClassName.endsWith(pWatcherName)==true)
                       return false ;
               if(traceClassName.contains("$")==true)
                       return false ;
               if(pIgnoredClassNames==null)
                       return true ;
               for(String ignoredClassName : pIgnoredClassNames) {
                       if(traceClassName.endsWith(ignoredClassName)==true)
                               return false ;
               }
               return true ;
       }
       /**
        * log executing class name, method name and line number.
        */
       public static void log() {
               log(null) ; System.err.println() ;
       }
       /**
        * log a message with executing class name, method name and line number.
        * @param pMessage
        */
       public static void log(String pMessage) {
               log(null,pMessage) ;
       }
       /**
        * Log a message ignoring a number of classes in the stack.
        * @param pIgnoredClassNames
        * @param pMessage
        */
       public static void log(String pIgnoredClassNames[],String pMessage) {
               StackTraceElement trace = getCaller(pIgnoredClassNames)  ;
               String signature = getSignature(trace) ;
               System.err.print(signature) ;
               if(pMessage==null)
                       return ;
               System.err.println(" [" + pMessage + "]");
       }
       /**
        * Begin a new watch session.
        */
       public static void start() {
               System.err.println("----------------------------------") ;
               log(null) ; System.err.println() ;
       }
       protected static String getExternalSignature(String pPackageName,int count) {
               StackTraceElement callingTrace = getTraceAbovePackage(pPackageName,count) ;
               if(callingTrace==null) 
                       return "No Caller" ;
               String signature = getSignature(callingTrace) ;
               return signature ;
       }
       
       public static StackTraceElement getTraceAbovePackage(String pPackageName,int count) {
               StackTraceElement[] traces = new Throwable().getStackTrace() ;
               boolean currentMatch = false ;
               boolean previousMatch = false ;
               int matchCounter = 0 ;
               for(StackTraceElement trace : traces) {
                       String traceClassName = trace.getClassName() ;
                       currentMatch = false ;
                       if(traceClassName.startsWith(pPackageName)) 
                               currentMatch = true ;
                       if(currentMatch==false&&previousMatch==true) {
                               matchCounter++;
                               if(matchCounter>=count)
                                       return trace ;
                       }
                       previousMatch = currentMatch ;
               }
               return null ;
       }
       
}
