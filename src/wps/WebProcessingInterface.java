package wps;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

import impactservice.Configuration;
import impactservice.GenericCart;
import impactservice.LoginManager;
import impactservice.ImpactUser;









import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ogcservices.PyWPSServer;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import tools.Debug;
import tools.HTTPTools;
import tools.MyXMLParser;
import tools.Tools;
import tools.MyXMLParser.Options;
import tools.MyXMLParser.XMLElement;

public class WebProcessingInterface {

  //public static String WPSURL="http://webgis.nmdc.eu/cgi-bin/wps/pywps.cgi";
  //public static String WPSURL="http://bhw222.knmi.nl:8081/impactportal/WPS";
 // public static String WPSURL="http://climate4impact.eu/impactportal/WPS";
 // public static String WPSURL = "http://localhost:8094/wps?";
  /**
   * Returns the external URL of the Web Processing Service
   * @return the external URL of the Web Processing Service
   */
  private static String getWPSURL() {
    //return WPSURL;
    //return "http://mouflon.dkrz.de/wps?";
    //return "https://compute-test.c3s-magic.eu:9000/wps?";
    return Configuration.getHomeURLHTTPS()+"/WPS?";
  }
  
  private static boolean isLocal(){
    return true;
  }
  
  
   private static WebProcessingInterface instance = null;
   protected WebProcessingInterface() {
      // Exists only to defeat instantiation.
   }
   public static WebProcessingInterface getInstance() {
      if(instance == null) {
         instance = new WebProcessingInterface();
      }
      return instance;
   }


	public static class ProcessorDescriptor{
		private MyXMLParser.XMLElement description;
		String getVersion(){
			try{return description.getAttrValue("wps:processVersion");} catch (Exception e) {return null;}
		}
		public String getIdentifier(){
			try{return description.get("ows:Identifier").getValue();} catch (Exception e) {return null;}
		}
		public String getTitle(){
			try{return description.get("ows:Title").getValue();} catch (Exception e) {return null;}
		}
		public String getAbstract() {
			try{return description.get("ows:Abstract").getValue();} catch (Exception e) {return null;}
		}

	};
	public static Vector<ProcessorDescriptor> getAvailableProcesses(HttpServletRequest request) {
	  boolean localWPS = isLocal();
	  String WPSService = getWPSURL();

	  try {
		  String kvpWPSService = HTTPTools.getHTTPParam(request, "wpsservice");
		  Debug.println("Found ["+kvpWPSService+"]");
		  if(kvpWPSService!=null){
		    if(WPSService.equals(kvpWPSService)== false){
          Debug.println("replacing local WPS with ["+WPSService+"]" );

		      WPSService = kvpWPSService;
		      
		      Debug.println("running remote WPS ["+WPSService+"]" );
	        localWPS = false;
		    }
		  }
		  
		} catch (Exception e1) {
      
    }
	  if(localWPS){
	    Debug.println("running local WPS ["+WPSService+"]" );
      
	  }
		Vector <ProcessorDescriptor> processorList = null;
		//if(processorList==null){
			processorList= new Vector<ProcessorDescriptor>() ;
			MyXMLParser.XMLElement  getCapabilitiesTree = new MyXMLParser.XMLElement();
			try{
				String getcaprequest="service=WPS&request=GetCapabilities";
				Debug.println("getProcessorsDescriptors: "+getcaprequest);
				
			
		   
		    if(localWPS == false){
		      String data = HTTPTools.makeHTTPGetRequestX509ClientAuthentication(
		          WPSService+getcaprequest, 
		          LoginManager.getUser(request).certificateFile, 
		          Configuration.LoginConfig.getTrustStoreFile(), 
		          Configuration.LoginConfig.getTrustStorePassword(), 
		          100000);
		      getCapabilitiesTree.parseString(data);//(new URL(WPSService+getcaprequest));
		  
		    }
		    
		    if(localWPS == true){
  				ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream();
  	      PyWPSServer.runPyWPS(request, null, stringOutputStream, getcaprequest, null);
  	      getCapabilitiesTree.parseString(stringOutputStream.toString());
  	      stringOutputStream = null;
		    }
				//a.parse(new URL(getcaprequest));//For remote services...
				System.out.println(getCapabilitiesTree.get("wps:Capabilities").get("ows:ServiceIdentification").get("ows:Title").getValue());
				Vector<MyXMLParser.XMLElement> listOfProcesses = getCapabilitiesTree.get("wps:Capabilities").get("wps:ProcessOfferings").getList("wps:Process");
				for(int j=0;j<listOfProcesses.size();j++){
					System.out.print(listOfProcesses.get(j).getAttrValue("wps:processVersion")+"\t");
					System.out.print(listOfProcesses.get(j).get("ows:Identifier").getValue()+"\t");
					System.out.println("\""+listOfProcesses.get(j).get("ows:Title").getValue()+"\"");
					ProcessorDescriptor e = new ProcessorDescriptor();
					e.description = listOfProcesses.get(j);
					processorList.add(e );
				}
				return processorList;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		//}
		//return null;
	}
	
	

	
	/**
	 * Encapsulates a String message into a JSON object which can be shown by the UI
	 * @param message The error message to display
	 * @return JSON Object which can be printed and is recognized by the UI
	 */
  private static JSONObject returnErrorMessage(String message){
	  try {
	    //DebugConsole.errprintln(message);
	    JSONObject error = new JSONObject();
      error.put("error",message);
      
      return error;
    } catch (JSONException e) { 
      Debug.errprintln(e.getMessage());
    }
    return null;
	}
	
  /**
   * Checks whether the python Web Processing Service has raised an exception in the form of an XML serviceexception report document
   * @param pyWPSXMLStructure The parsed XML document to check
   * @param extraInfo Additional information to displayed with the error
   * @return null if no exception occured, otherwise a JSON structure containing the message to display in the UI
   */
	private static JSONObject checkException(MyXMLParser.XMLElement pyWPSXMLStructure,String extraInfo){
	    if(extraInfo == null)extraInfo="";
	    try{
	      //DebugConsole.println(a.getFirst().getName());
	      if(pyWPSXMLStructure.getFirst().getName().indexOf("ExceptionReport")>=0){
	        String message = "Web Proccessing Service Exception:\n";
	        try{
	          message+=pyWPSXMLStructure.getFirst().getFirst().getAttrValue("exceptionCode");
	          try{
	          message+=" ";
	          message+=pyWPSXMLStructure.getFirst().getFirst().getAttrValue("locator");
	          }catch(Exception e){}
	          message+="\n";
	          try{
	            message+=pyWPSXMLStructure.getFirst().getFirst().get("ows:ExceptionText").getValue();
	            message=message.replaceAll("\\\\","");
	          }catch(Exception e){}
	        }catch(Exception e){}
	        message+="\n\n"+extraInfo;
	        //Debug.errprintln("error: "+message);
	        return returnErrorMessage(message);
	      }
	    }catch(Exception e){}
	    return null;
	  
	}
	
	/**
	 * Describes the WPS process and returns a JSON object representing the process
	 * @param id The identifier of the process
	 * @return JSON representation of the XML describeProcess reslt
	 * @throws Exception
	 */
	
	public static JSONObject describeProcess(HttpServletRequest request,String id) throws Exception{
	  Debug.println("DescribeProcess "+id);
	  String getcaprequest="service=WPS&version=1.0.0&request=describeprocess&identifier="+id;
	  
	  MyXMLParser.XMLElement  a = new MyXMLParser.XMLElement();
    Debug.println("getProcessorsDescriptors: "+getcaprequest);
    
    
    if(isLocal() == false){
      a.parse(new URL(getWPSURL()+getcaprequest));
  
    }
    if(isLocal() == true){
      ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream();
      PyWPSServer.runPyWPS(request, null, stringOutputStream, getcaprequest, null);
      a.parseString(stringOutputStream.toString());
      stringOutputStream = null;
    }

	  
	
    
    //Check if an Exception has been thrown:
    JSONObject exception = checkException(a,getcaprequest);
    if(exception!=null){
      Debug.println("Exception in DescribeProcess: "+exception.toString());
      return exception;
    }
    //DebugConsole.println("toJSON");
    //Convert the XML structure to JSOn
    JSONObject jsonData = a.toJSONObject(MyXMLParser.Options.STRIPNAMESPACES);
    jsonData.put("url","/"+Configuration.getHomeURLPrefix()+"/WPS?"+getcaprequest);
    return jsonData;
    
    /*
    DebugConsole.println(a.getFirst().getName());
    for(int j=0;j<a.getFirst().getElements().size();j++){
      DebugConsole.println("  "+a.getFirst().getElements().get(j).getName());
    }
    //
    JSONObject data = new JSONObject();
    XMLElement processDescription = null;
    try{
      processDescription = a.get("wps:ProcessDescriptions").get("ProcessDescription");
      data.put("id", processDescription.get("ows:Identifier").getValue());
      data.put("title", processDescription.get("ows:Title").getValue());
      data.put("description", processDescription.get("ows:Abstract").getValue());
      data.put("wpsurl",getcaprequest);
      JSONArray inputs = new JSONArray();
      data.put("inputs",inputs);
      try{
        Vector<XMLElement> dataInputs=processDescription.get("DataInputs").getList("Input");
     
        for(int j=0;j<dataInputs.size();j++){
          XMLElement di = dataInputs.get(j);
          JSONObject input = new JSONObject();
          inputs.put(input);
          input.put("id",di.get("ows:Identifier").getValue());
          input.put("title",di.get("ows:Title").getValue());
          
          
          try{
            XMLElement literalData = di.get("LiteralData");
            input.put("type",literalData.get("ows:DataType").getValue());
            input.put("default",literalData.get("DefaultValue").getValue());
          }catch(Exception e){
            DebugConsole.errprintln("Exception in getting DataInput '"+di.get("ows:Identifier").getValue()+"':\n"+e.getMessage());
            input.put("default","");
          }
        }
      }catch(Exception e){
        DebugConsole.errprintln("Exception in getting DataInputs from WPS description:\n"+e.getMessage());
        
      }
      
    }catch(Exception j){
      DebugConsole.errprintln("error");
      return returnErrorMessage(j.getMessage()+"\n"+a.toString());
    }
    return data;*/
	}
	

	
	
	
	
	
	/**
	 * Executes a WPS command based on processor identifier and datainputs. Datainputs should be in the form of how KVP datainputs would normally be encoded in a GET URL.
	 * 
	 * @param procId The processor to use
	 * @param dataInputs E.g. datainputs=[input1=value1;input2=value2;input3=[a,b,c]]
	 * @param request For session (user) information 
	 * @return JSON object containing the statusLocation of the process.
	 * @throws Exception 
	 */
	
  public static JSONObject executeProcess(String procId, String dataInputs, HttpServletRequest request,HttpServletResponse response) throws Exception {
    ImpactUser user = LoginManager.getUser(request);
    if(user == null){
      return null;
    }
    Debug.println("executeprocess "+procId+" datainput="+dataInputs);
    try{
      String postData=PyWPSServer.convertQueryStringToPost(dataInputs, procId);  
      MyXMLParser.XMLElement  wpsExecuteResponseDocument = new MyXMLParser.XMLElement();
      if(isLocal() == false){
        wpsExecuteResponseDocument.parse(new URL(getWPSURL()),postData);
      }
      if(isLocal() == true){
        //We have composed the data to post, now post it to our internal CGI.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PyWPSServer.runPyWPS(request,null,out,null,postData);
        Debug.println("Process has been started.");
        wpsExecuteResponseDocument.parseString(out.toString());
        out = null;
      }
      //Track this job
      JSONObject data = trackJobForUser(user,wpsExecuteResponseDocument,postData);
      return data;
    } catch (Exception e) {
      Debug.errprintln("error");
      return returnErrorMessage(e.toString());  
    }
  }
  
 
  /**
   * Stores the job in the users basket for monitoring.
   * @param user
   * @param wpsExecuteResponseDocument
   * @param xmlInputPostData
   * @return
   * @throws Exception
   */
  public static JSONObject trackJobForUser(ImpactUser user, XMLElement wpsExecuteResponseDocument,String xmlInputPostData) throws Exception {
    Debug.println("TrackJob For user");
    //Check if an Exception has been thrown:
    JSONObject exception = checkException(wpsExecuteResponseDocument,xmlInputPostData+"\n\n"+wpsExecuteResponseDocument.toString());
    if(exception!=null)return exception;
    
    //Get the reference to the started process (statuslocation)
    String statusLocation=null;
    String creationTime = null;
    JSONObject data = new JSONObject();
   
    try{
    statusLocation=wpsExecuteResponseDocument.get("wps:ExecuteResponse").getAttrValue("statusLocation");
    }catch(Exception e){
      Debug.errprintln("Unable to track job for user: statuslocation not set! Is this really started asynchronousely?");
      return null;
    }
    
    wpsExecuteResponseDocument.get("wps:ExecuteResponse").get("wps:Status").get("wps:ProcessAccepted");
    creationTime = wpsExecuteResponseDocument.get("wps:ExecuteResponse").get("wps:Status").getAttrValue("creationTime");
    String procId = wpsExecuteResponseDocument.get("wps:ExecuteResponse").get("wps:Process").get("ows:Identifier").getValue();
  
    Debug.println("WPS statusLocation = '"+statusLocation+"'");
    Debug.println("WPS creationTime = '"+creationTime+"'");
  
 
    data.put("statuslocation", statusLocation);
    data.put("status", "accepted");
    data.put("creationTime", creationTime);
    //String id = statusLocation.substring(statusLocation.lastIndexOf("/")+1);
    data.put("id", procId);
    
    MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
    if(xmlInputPostData!=null){
      b.parseString(xmlInputPostData);
      JSONObject inputDataAsJSON = b.toJSONObject(Options.STRIPNAMESPACES);
        
      data.put("wpspostdata", inputDataAsJSON);
    }
    String uniqueID=statusLocation.substring(statusLocation.lastIndexOf("/")+1);
    data.put("uniqueid", uniqueID);
    try {
      GenericCart p=user.getProcessingJobList();
      p.addDataLocator(uniqueID, data.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    //Save also in user basket.
    String baseName = statusLocation.substring(statusLocation.lastIndexOf("/")).replace(".xml", ".wpssettings");
    String wpsSettingsFile = user.getDataDir()+"/WPS_Settings/";
    tools.Tools.mksubdirs(wpsSettingsFile);
    wpsSettingsFile+=baseName;
    Tools.writeFile(wpsSettingsFile, data.toString());
    
    return data;
  }
  
  /**
   * Reads a statuslocation and provides a JSON object with progress report
   * @param statusLocation  The statuslocation to read (URL)
   * @return  JSONObject with status information
   */
  public static JSONObject monitorProcess( String statusLocation,HttpServletRequest request) {
  
    Debug.println("monitorProcess for statusLocation "+statusLocation);
    JSONObject data = new JSONObject();
    JSONObject exception = null;
    //statusLocation = statusLocation.replace("8091", "80");
    try {
      MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
      
      /*
       * It seems that the statuslocation XML file is sometimes not completely written by PyWPS, or not immediately available after the process has been executed.
       * Solution: Retry to read the XML file a couple of times until we have a good result.       * 
       */
      int maximumTries = 3;
      boolean success = false;
      do{
        maximumTries--;
        try{
          ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream();
          PyWPSServer.runPyWPS(request, null, stringOutputStream, statusLocation, null);
          b.parseString(stringOutputStream.toString());
          success = true;
        }catch(SAXException s){
          Debug.errprintln("Statuslocation does not contain valid XML, retrying..., attempts left: "+maximumTries);
          Thread.sleep(500);
          if(maximumTries == 0){
            GenericCart jobList;
            try {
              jobList = LoginManager.getUser(request).getProcessingJobList();
              String basename = statusLocation.substring(statusLocation.lastIndexOf("/")+1);
              jobList.removeDataLocator(basename);
            } catch (Exception e1) {
              e1.printStackTrace();
            }
            throw s;
          }
        }
      }while(maximumTries>0 && success == false);
      
      //Check if an WPS Exception has been thrown:
      XMLElement status = null;
      try{
        exception = checkException(b,"Location: "+statusLocation);;
        if(exception!=null)return exception;
        
        status = b.get("wps:ExecuteResponse").get("wps:Status");
        exception = checkException(status.get("wps:ProcessFailed"),"Location: "+statusLocation);
        if(exception!=null)return exception;
      }catch(Exception e){}
      
      try{
        //DebugConsole.println(status.get("wps:ProcessStarted").getAttr("percentCompleted")+"% - "+status.get("wps:ProcessStarted").getValue());
        data.put("wpsurl",statusLocation);
        data.put("statuslocation",statusLocation);
        data.put("progress",status.get("wps:ProcessStarted").getAttrValue("percentCompleted"));//Progress in percentage
        String statusString=status.get("wps:ProcessStarted").getValue();
        int a= statusString.indexOf("processstarted");
        if(a==0){
          statusString=statusString.substring("processstarted".length()+1);
        }
        data.put("status", statusString);
      }catch(Exception e){
      }
      
      //If process is succeeded break!
      try{
        Debug.println("Process succeeded!: "+status.get("wps:ProcessSucceeded").getValue());
        //JSONObject submittedData=getSubmittedJobInformation(statusLocation,request);
        //data.put("postData", submittedData);
        data.put("progress",100 );
        data.put("status","Process completed." );
        data.put("ready", true);
        
       /* try {
          Vector<XMLElement> wpsData=b.get("wps:ExecuteResponse").get("wps:ProcessOutputs").getList("wps:Output");
          for(int j=0;j<wpsData.size();j++){
            try{
              XMLElement complexData=wpsData.get(j).get("wps:Data").get("wps:ComplexData");
              data.put("base64image", complexData.getValue().replaceAll("\n",""));
            }catch(Exception e){}
          }
          //System.out.println( a.get("wps:ExecuteResponse").get("wps:ProcessOutputs").get("wps:Output").get("wps:Data").get("wps:ComplexData").getValue().replaceAll("\n",""));
        } catch (Exception e) {
          //Silently skip.
        }*/
        
      }catch(Exception e){
        //Silently skip until something usefull comes by..
      }
      
    }catch(Exception e){
      Debug.errprintln("error for "+statusLocation);
      
      //Debug.println("Returning null");
      //return null;
      return returnErrorMessage(e.getMessage());  
    }
    return data;
  }
	
 
  /**
   * Returns an image based on statusLocation and identifier
   * @param statusLocation
   * @param identifier
   * @return
   */
  public static byte[] getImageFromStatusLocation(String statusLocation,String identifier){
    Debug.println("Get image from statusLocation "+statusLocation+" and id "+identifier);
    try {
      MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
      b.parse(new URL(statusLocation));
      
      try {
        Vector<XMLElement> wpsData=b.get("wps:ExecuteResponse").get("wps:ProcessOutputs").getList("wps:Output");
        for(int j=0;j<wpsData.size();j++){
          try{
            String outputId=wpsData.get(j).get("ows:Identifier").getValue();
            if(outputId.equals(identifier)){
              XMLElement complexData=wpsData.get(j).get("wps:Data").get("wps:ComplexData");
              byte[] btDataFile = Base64.decodeBase64( complexData.getValue().replaceAll("\n",""));  
              return btDataFile;
            }
          }catch(Exception e){}
        }
      }catch(Exception e){
        Debug.errprintln("error");
        return returnErrorMessage(e.getMessage()+"\n"+b.toString()).toString().getBytes();
      }
    }catch(Exception e){
      Debug.errprintln("error");
      return returnErrorMessage(e.getMessage()+"\n").toString().getBytes();
    }
    return null;
  }
  
  
  public static String generateReportFromStatusLocation(String statusLocation){
    Debug.println("Get HTML from statusLocation "+statusLocation);
    String html = "";
    html+="<link rel=\"stylesheet\" href=\"/impactportal/styles.css\" type=\"text/css\" />";

    try {
      ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream();
      PyWPSServer.runPyWPS(null, null, stringOutputStream, statusLocation, null);
      
      MyXMLParser.XMLElement  b  = null;
      try{
          b = new MyXMLParser.XMLElement();
        b.parseString(stringOutputStream.toString());
        
      }catch(SSLException e){
        Debug.printStackTrace(e);
        Debug.errprintln("error");
        return (e.getMessage()+"\n").toString();
       
      }catch(Exception e){
        Debug.printStackTrace(e);
        return (e.getMessage()+"\n").toString();
      }
    
      html += "<h1>Report for "+b.get("wps:ExecuteResponse").get("wps:Process").get("ows:Title").getValue()+"</h1>";
      html+="See <a href=\""+statusLocation+"\">"+statusLocation+"</a> (XML).";
      html+="<hr/>";
      //html+=b.toString();
      try {
         
        Vector<XMLElement> wpsData=b.get("wps:ExecuteResponse").get("wps:ProcessOutputs").getList("wps:Output");
        html+="<table class=\"wpsreport\">";
        html+="<tr><th>Identifier</th><th>Title</th><th>Value</th></tr>";
        for(int j=0;j<wpsData.size();j++){
          String identifier=wpsData.get(j).get("ows:Identifier").getValue();
          String title=wpsData.get(j).get("ows:Title").getValue();
          String data="";
          XMLElement dataEl =wpsData.get(j).get("wps:Data");
          //We are going to check if the data is one of wps:LiteralData or wps:CompexData: 
          
          //Literaldata are integers and strings
          try{
            String literalDataValue = dataEl.get("wps:LiteralData").getValue();
            if(literalDataValue.startsWith("base64:")){
              literalDataValue = new String(Base64.decodeBase64(literalDataValue.substring(7)));
            }
            
            String literalDataValueLowerCase = literalDataValue.toLowerCase();
            //Check if this output is an OPENDAP URL, in that case we can make a link to our file viewer
            if(literalDataValueLowerCase.indexOf("dap")!=-1&&literalDataValueLowerCase.indexOf("http")!=-1&&literalDataValueLowerCase.indexOf(".nc")!=-1){
              //This is an OPENDAP URL
              String datasetViewerLocation = "/"+Configuration.getHomeURLPrefix()+"/data/datasetviewer.jsp?dataset=";
              data+="<a target=\"_blank\" href=\""+datasetViewerLocation+URLEncoder.encode(literalDataValue,"UTF-8")+"\">"+literalDataValue+"</a>";
            }else{
              literalDataValue = literalDataValue.replace("\n","<br/>");
              data = literalDataValue;
            }
          }catch(Exception e){}
          
          //Complexdata are images and netcdfs
          try{
            dataEl.get("wps:ComplexData").getValue();
            String mimeType = dataEl.get("wps:ComplexData").getAttrValue("mimeType");
            Debug.println("MimeType = "+mimeType);
            if(mimeType.equalsIgnoreCase("image/png")){
            //getImageFromStatusLocation
              String imageUrl = Configuration.getImpactServiceLocation()+"service=processor&request=getimage&outputId="+identifier+"&statusLocation="+statusLocation+"&image=img.png";
              //data+="<a class=\"fancybox\" rel=\"group\" href=\"http://localhost:8080/impactportal/ImpactService?service=processor&request=getimage&outputId=buffer&statusLocation=http://localhost:8080/impactportal/WPS?OUTPUT=/pywps-138504392568.xml\"><img width=\"600\" src=\""+imageUrl+"\" alt=\"\"/></a>";
              data+="<a class=\"fancybox\" rel=\"group\" href=\""+imageUrl+"\"><img width=\"600\" src=\""+imageUrl+"\" alt=\"\" /></a>";
            }
             //data+=getWPSURL();
          }catch(Exception e){}
         // http://localhost:8080/impactportal/WPS?OUTPUT=/pywps-138296332037.xml
          
          
          html+="<tr><td>"+identifier+"</td><td>"+title+"</td><td>"+data+"</td></tr>"; 
        }
        html+="</table>";
      }catch(Exception e){
        Debug.errprintln("error in generateReportFromStatusLocation: "+e.getMessage());

       // html+= "<h1>No results available</h1><br/>"+"See <a href=\""+statusLocation+"\">"+statusLocation+"</a> (XML).";
        try{
          String exceptionMessage= checkException(b.get("wps:ExecuteResponse").get("wps:Status").get("wps:ProcessFailed"), null).getString("error");
          Debug.errprintln("Process failed: "+exceptionMessage);
          html+="<h2>An error occured while executing the process:</h2>"+exceptionMessage;
        }catch(Exception e2){
        }
        return html;
      }
    }catch(Exception e){
      Debug.errprintln("error");
      return (e.getMessage()+"\n").toString();
    }
  
    return html;
  }
  
}
