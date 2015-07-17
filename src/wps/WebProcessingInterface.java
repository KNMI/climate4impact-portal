package wps;


import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

import impactservice.Configuration;
import impactservice.GenericCart;
import impactservice.LoginManager;
import impactservice.ImpactUser;







import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ogcservices.PyWPSServer;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import tools.Debug;
import tools.MyXMLParser;
import tools.MyXMLParser.Options;
import tools.MyXMLParser.XMLElement;

public class WebProcessingInterface {

  //public static String WPSURL="http://webgis.nmdc.eu/cgi-bin/wps/pywps.cgi";
  //public static String WPSURL="http://bhw222.knmi.nl:8081/impactportal/WPS";
 // public static String WPSURL="http://climate4impact.eu/impactportal/WPS";
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
		
		Vector <ProcessorDescriptor> processorList = null;
		//if(processorList==null){
			processorList= new Vector<ProcessorDescriptor>() ;
			MyXMLParser.XMLElement  getCapabilitiesTree = new MyXMLParser.XMLElement();
			try{
				String getcaprequest="service=WPS&request=GetCapabilities";
				Debug.println("getProcessorsDescriptors: "+getcaprequest);
				
			
		   
		    if(isLocal() == false){
		      getCapabilitiesTree.parse(new URL(getWPSURL()+getcaprequest));
		  
		    }
		    
		    if(isLocal() == true){
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
	 * Returns the external URL of the Web Processing Service
	 * @return the external URL of the Web Processing Service
	 */
	private static String getWPSURL() {
	  //return "http://mouflon.dkrz.de/wps?";
	  return Configuration.getHomeURLHTTP()+"/WPS?";
  }
	
	private static boolean isLocal(){
	  return true;
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
	
	public static String addLiteralData(String identifier,String value){
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
    ImpactUser user = LoginManager.getUser(request,response);
    if(user == null){
      return null;
    }
    //http://bhw222.knmi.nl:8080/cgi-bin/wps.cgi?version=1.0.0&service=WPS&request=execute&identifier=Rint&datainputs=[startIndex=1;stopIndex=100]
    Debug.println("executeprocess "+procId+" datainput="+dataInputs);
    //String getcaprequest=WPSURL+"service=WPS&version=1.0.0&request=execute&identifier="+procId+"&datainputs=[startIndex=1;stopIndex=100]";
    
    String postData="";
    try{
      
     
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
      if(!trimmedInput.equals("[]")){
        postData   +=   "      <wps:DataInputs>\n";
        //postData+=addLiteralData("startIndex","1");
        //postData+=addLiteralData("stopIndex","4");
        
        //DebugConsole.println(dataInputs);
        
      
        //Remove [] and split on ","
        String [] dataInputArray=trimmedInput.substring(1,trimmedInput.length()-1).split(";");
        for(int j=0;j<dataInputArray.length;j++){
          //KVP key=value
          
          
          dataInputArray[j] = dataInputArray[j].split("#")[0];
          Debug.println(dataInputArray[j]);
          try{
          String[] kvp=dataInputArray[j].split("=");
          if(kvp.length<2){
            postData+=addLiteralData(kvp[0],"");
          }else{
            postData+=addLiteralData(kvp[0],kvp[1]);
          }
          }catch(Exception e){
            e.printStackTrace();
            Debug.errprintln("error");
            return returnErrorMessage("Invalid values given for '"+dataInputArray[j]+"' \nCause: "+e.toString());  
          }
        }
        
        postData     +=  "      </wps:DataInputs>\n";
      }
      
      postData     +=  "    </wps:Execute>\n";
      
      MyXMLParser.XMLElement  a = new MyXMLParser.XMLElement();
      Debug.println(postData);
     
      
      if(isLocal() == false){
        
        a.parse(new URL(getWPSURL()),postData);
    
      }
      if(isLocal() == true){
        //We have composed the data to post, now post it to our internal CGI.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        PyWPSServer.runPyWPS(request,null,out,null,postData);
        //a.parse(new URL(WPSURL),postData);
        //Debug.println(getWPSURL()+" for "+user.id);
        
        //Debug.println("Process has been started with the following command:.");
        //DebugConsole.println(postData+"\nThe result is:\n"+a.toString());
        Debug.println("Process has been started.");
      
      
       
        a.parseString(out.toString());
        out = null;
      }
      
      //Track this job
      JSONObject data = trackJobForUser(user,a,postData);
      //monitorProcess(statusLocation,request);
      return data;
    
    } catch (Exception e) {
      Debug.errprintln("error");
      return returnErrorMessage(e.toString());  
    }
   
 
  }
  
  
  public static JSONObject trackJobForUser(ImpactUser user, XMLElement a,String postData) throws Exception {
    Debug.println("TrackJob For user");
    //Check if an Exception has been thrown:
    JSONObject exception = checkException(a,postData+"\n\n"+a.toString());
    if(exception!=null)return exception;
    
    //Get the reference to the started process (statuslocation)
    String statusLocation=null;
    String creationTime = null;
    JSONObject data = new JSONObject();
   
    statusLocation=a.get("wps:ExecuteResponse").getAttrValue("statusLocation");
    
    a.get("wps:ExecuteResponse").get("wps:Status").get("wps:ProcessAccepted");
    creationTime = a.get("wps:ExecuteResponse").get("wps:Status").getAttrValue("creationTime");
    String procId = a.get("wps:ExecuteResponse").get("wps:Process").get("ows:Identifier").getValue();
  
    Debug.println("WPS statusLocation = '"+statusLocation+"'");
    Debug.println("WPS creationTime = '"+creationTime+"'");
  
 
    data.put("wpsurl", statusLocation);
    data.put("status", "accepted");
    data.put("statusLocation", statusLocation);
    data.put("creationTime", creationTime);
    //String id = statusLocation.substring(statusLocation.lastIndexOf("/")+1);
    data.put("id", procId);
    
    MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
    if(postData!=null){
      b.parseString(postData);
      JSONObject inputDataAsJSON = b.toJSONObject(Options.STRIPNAMESPACES);
        
      data.put("postData", inputDataAsJSON);
    }
    String uniqueID=statusLocation.substring(statusLocation.lastIndexOf("/")+1);
    data.put("uniqueid", uniqueID);
    try {
      GenericCart p=user.getProcessingJobList();
      p.addDataLocator(uniqueID, data.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
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
      int maximumTries = 2;
      boolean success = false;
      do{
        maximumTries--;
        try{
          b.parse(new URL(statusLocation));
          success = true;
        }catch(SAXException s){
          Debug.errprintln("Statuslocation does not contain valid XML, retrying..., attempts left: "+maximumTries);
          Thread.sleep(100);
          if(maximumTries == 0){
            GenericCart jobList;
            try {
              jobList = LoginManager.getUser(request,null).getProcessingJobList();
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
      MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
      b.parse(new URL(statusLocation));
      
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
            //Check if this output is an OPENDAP URL, in that case we can make a link to our file viewer
            if(literalDataValue.indexOf("DAP")!=-1&&literalDataValue.indexOf("http")!=-1&&literalDataValue.indexOf(".nc")!=-1){
              //This is an OPENDAP URL
              String datasetViewerLocation = "/"+Configuration.getHomeURLPrefix()+"/data/datasetviewer.jsp?dataset=";
              data+="<a target=\"_blank\" href=\""+datasetViewerLocation+URLEncoder.encode(literalDataValue,"UTF-8")+"\">"+literalDataValue+"</a>";
            }else{
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
          e2.printStackTrace();
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
