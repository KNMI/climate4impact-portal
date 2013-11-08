package wps;


import impactservice.Configuration;
import impactservice.GenericCart;
import impactservice.LoginManager;
import impactservice.User;
import impactservice.GenericCart.DataLocator;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import ogcservices.PyWPSServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.DebugConsole;
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
	public static Vector<ProcessorDescriptor> getAvailableProcesses() {
		
		Vector <ProcessorDescriptor> processorList = null;
		//if(processorList==null){
			processorList= new Vector<ProcessorDescriptor>() ;
			MyXMLParser.XMLElement  a = new MyXMLParser.XMLElement();
			try{
				String getcaprequest=getWPSURL()+"service=WPS&request=getcapabilities";
				DebugConsole.println("getProcessorsDescriptors: "+getcaprequest);
				a.parse(new URL(getcaprequest));
				System.out.println(a.get("wps:Capabilities").get("ows:ServiceIdentification").get("ows:Title").getValue());
				Vector<MyXMLParser.XMLElement> listOfProcesses = a.get("wps:Capabilities").get("wps:ProcessOfferings").getList("wps:Process");
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
	  return Configuration.GlobalConfig.getServerHomeURL()+Configuration.getHomeURL()+"/WPS?";
  }
	
	/**
	 * Encapsulates a String message into a JSON object which can be shown by the UI
	 * @param message The error message to display
	 * @return JSON Object which can be printed and is recognized by the UI
	 */
  private static JSONObject returnErrorMessage(String message){
	  try {
	    DebugConsole.errprintln(message);
	    JSONObject error = new JSONObject();
      error.put("error",message);
      
      return error;
    } catch (JSONException e) { 
      DebugConsole.errprintln(e.getMessage());
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
	          }catch(Exception e){}
	        }catch(Exception e){}
	        message+="\n\n"+extraInfo;
	        DebugConsole.errprintln("error");
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
	
	public static JSONObject describeProcess(String id) throws Exception{
	  DebugConsole.println("DescribeProcess "+id);
	  String getcaprequest=getWPSURL()+"service=WPS&version=1.0.0&request=describeprocess&identifier="+id;
	  MyXMLParser.XMLElement  a = new MyXMLParser.XMLElement();
    try{
      a.parse(new URL(getcaprequest));
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    //Check if an Exception has been thrown:
    JSONObject exception = checkException(a,getcaprequest);
    if(exception!=null){
      DebugConsole.println("Exception in DescribeProcess: "+exception.toString());
      return exception;
    }
    //DebugConsole.println("toJSON");
    //Convert the XML structure to JSOn
    JSONObject jsonData = a.toJSONObject(MyXMLParser.Options.STRIPNAMESPACES);
    jsonData.put("url",getcaprequest);
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
	  String data="        <wps:Input>\n"
        +  "          <ows:Identifier>"+identifier+"</ows:Identifier>\n";
	  if(value!=null){
        data+=  "          <wps:Data>\n"
        +  "            <wps:LiteralData>"+value+"</wps:LiteralData>\n"
        +  "          </wps:Data>\n";
	  }
    data+="        </wps:Input>\n";
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
	
  public static JSONObject executeProcess(String procId, String dataInputs, HttpServletRequest request) throws Exception {
    User user = LoginManager.getUser(request);
    //http://bhw222.knmi.nl:8080/cgi-bin/wps.cgi?version=1.0.0&service=WPS&request=execute&identifier=Rint&datainputs=[startIndex=1;stopIndex=100]
    DebugConsole.println("executeprocess "+procId+" datainput="+dataInputs);
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
      DebugConsole.println("DataInputs="+trimmedInput);
      if(!trimmedInput.equals("[]")){
        postData   +=   "      <wps:DataInputs>\n";
        //postData+=addLiteralData("startIndex","1");
        //postData+=addLiteralData("stopIndex","4");
        
        //DebugConsole.println(dataInputs);
        
      
        //Remove [] and split on ","
        String [] dataInputArray=trimmedInput.substring(1,trimmedInput.length()-1).split(";");
        for(int j=0;j<dataInputArray.length;j++){
          //KVP key=value
          DebugConsole.println(dataInputArray[j]);
          try{
          String[] kvp=dataInputArray[j].split("=");
          if(kvp.length<2){
            postData+=addLiteralData(kvp[0],"");
          }else{
            postData+=addLiteralData(kvp[0],kvp[1]);
          }
          }catch(Exception e){
            e.printStackTrace();
            DebugConsole.errprintln("error");
            return returnErrorMessage("Invalid values given for '"+dataInputArray[j]+"' \nCause: "+e.toString());  
          }
        }
        
        postData     +=  "      </wps:DataInputs>\n";
      }
      
      postData     +=  "    </wps:Execute>\n";
      
      
      //We have composed the data to post, now post it to our internal CGI.
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      
      PyWPSServer.runPyWPS(request,null,out,postData);
      //a.parse(new URL(WPSURL),postData);
      DebugConsole.println(getWPSURL()+" for "+user.id);
      
      DebugConsole.println("Process has been started with the following command:.");
      //DebugConsole.println(postData+"\nThe result is:\n"+a.toString());
      DebugConsole.println("Process has been started.");
    
    
      MyXMLParser.XMLElement  a = new MyXMLParser.XMLElement();
      a.parseString(out.toString());
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

    
      DebugConsole.println("WPS statusLocation = '"+statusLocation+"'");
      DebugConsole.println("WPS creationTime = '"+creationTime+"'");
    
   
      data.put("wpsurl", statusLocation);
      data.put("status", "accepted");
      data.put("statusLocation", statusLocation);
      data.put("creationTime", creationTime);
      data.put("id", procId);
      
      MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
      b.parseString(postData);
      JSONObject inputDataAsJSON = b.toJSONObject(Options.STRIPNAMESPACES);
        
      data.put("postData", inputDataAsJSON);
      String uniqueID=statusLocation.substring(statusLocation.lastIndexOf("/")+1);
      data.put("uniqueid", uniqueID);
      //Track this job
      trackJobForUser(user,uniqueID,data.toString());
   
      return data;
    
    } catch (Exception e) {
      DebugConsole.errprintln("error");
      return returnErrorMessage(e.toString());  
    }
   
 
  }
  
  
  private static void trackJobForUser(User user, String id, String data) {
    try {
      GenericCart p=user.getProcessingJobList();
      p.addDataLocator(id, data);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  /**
   * Reads a statuslocation and provides a JSON object with progress report
   * @param statusLocation  The statuslocation to read (URL)
   * @return  JSONObject with status information
   */
  public static JSONObject monitorProcess( String statusLocation,HttpServletRequest request) {
    DebugConsole.println("monitorProcess for statusLocation "+statusLocation);
    JSONObject data = new JSONObject();
    JSONObject exception = null;
    
    try {
      MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
      b.parse(new URL(statusLocation));
      //Check if an Exception has been thrown:
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
        DebugConsole.println("Process succeeded!: "+status.get("wps:ProcessSucceeded").getValue());
        JSONObject submittedData=getSubmittedJobInformation(statusLocation,request);
        data.put("postData", submittedData);
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
      DebugConsole.errprintln("error");
      return returnErrorMessage(e.getMessage());  
    }
    return data;
  }
	
  private static JSONObject getSubmittedJobInformation(String statusLocation,HttpServletRequest request) throws Exception {
    DebugConsole.println("getSubmittedJobInformation with statusLocation"+statusLocation);
    User user = LoginManager.getUser(request);
   
    Iterator<DataLocator> itr = user.getProcessingJobList().dataLocatorList.iterator();
    while(itr.hasNext()) {
      DataLocator element = itr.next(); 
      try {
        JSONObject elementProps =  (JSONObject) new JSONTokener(element.cartData).nextValue();
        String jobStatusLocation = elementProps.getString("wpsurl");
        if(statusLocation.equals(jobStatusLocation)){
          DebugConsole.println("Found job with statusLocation"+statusLocation);
          String id= elementProps.getString("id");
          DebugConsole.println("This job has processor id "+id);
         // return describeProcess(id);
          return elementProps.getJSONObject("postData");
        }
      }catch(Exception e){
        return null;
        
      }
      
     }
      // String statusLocation = elementProps.getString("wpsurl");
    // TODO Auto-generated method stub
    return null;
  }
  /**
   * Returns an image based on statusLocation and identifier
   * @param statusLocation
   * @param identifier
   * @return
   */
  public static byte[] getImageFromStatusLocation(String statusLocation,String identifier){
    DebugConsole.println("Get image from statusLocation "+statusLocation+" and id "+identifier);
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
              byte[] btDataFile = new sun.misc.BASE64Decoder().decodeBuffer( complexData.getValue().replaceAll("\n",""));  
              return btDataFile;
            }
          }catch(Exception e){}
        }
      }catch(Exception e){
        DebugConsole.errprintln("error");
        return returnErrorMessage(e.getMessage()+"\n"+b.toString()).toString().getBytes();
      }
    }catch(Exception e){
      DebugConsole.errprintln("error");
      return returnErrorMessage(e.getMessage()+"\n").toString().getBytes();
    }
    return null;
  }
  
  
  public static String generateReportFromStatusLocation(String statusLocation){
    DebugConsole.println("Get HTML from statusLocation "+statusLocation);
    String html = "";
    html+="<link rel=\"stylesheet\" href=\"/impactportal/wps.css\" type=\"text/css\" />";

    try {
      MyXMLParser.XMLElement  b = new MyXMLParser.XMLElement();
      b.parse(new URL(statusLocation));
      
      html += "<h1>Report for "+b.get("wps:ExecuteResponse").get("wps:Process").get("ows:Title").getValue()+"</h1>";
      html+="See <a href=\""+statusLocation+"\">"+statusLocation+"</a> (XML).";
      html+="<hr/>";
      //html+=b.toString();
      try {
        html+="<table class=\"wpsreport\">";
        html+="<tr><th>Identifier</th><th>Title</th><th>Value</th></tr>"; 
        Vector<XMLElement> wpsData=b.get("wps:ExecuteResponse").get("wps:ProcessOutputs").getList("wps:Output");
        
        for(int j=0;j<wpsData.size();j++){
          String identifier=wpsData.get(j).get("ows:Identifier").getValue();
          String title=wpsData.get(j).get("ows:Title").getValue();
          String data="";
          XMLElement dataEl =wpsData.get(j).get("wps:Data");
          //Literaldata are integers and strings
          try{
            data = dataEl.get("wps:LiteralData").getValue();
          }catch(Exception e){}
          
          //Complextdata are images and netcdfs
          
          try{
            dataEl.get("wps:ComplexData").getValue();
            String mimeType = dataEl.get("wps:ComplexData").getAttrValue("mimeType");
            DebugConsole.println("MimeType = "+mimeType);
            if(mimeType.equalsIgnoreCase("image/png")){
            //getImageFromStatusLocation
              data+="<img height=360 src='"+Configuration.getImpactServiceLocation()+"service=processor&request=getimage&outputId="+identifier+"&statusLocation="+statusLocation+"' />";
            }
             //data+=getWPSURL();
          }catch(Exception e){}
         // http://localhost:8080/impactportal/WPS?OUTPUT=/pywps-138296332037.xml
          
          
          html+="<tr><td>"+identifier+"</td><td>"+title+"</td><td>"+data+"</td></tr>"; 
        }
        html+="</table>";
      }catch(Exception e){
        DebugConsole.errprintln("error in generateReportFromStatusLocation: "+e.getMessage());
        return "No results available";//(e.getMessage()+"\n"+b.toString()).toString();
      }
    }catch(Exception e){
      DebugConsole.errprintln("error");
      return (e.getMessage()+"\n").toString();
    }
  
    return html;
  }
  
}
