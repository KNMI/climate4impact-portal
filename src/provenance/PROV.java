package provenance;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import impactservice.Configuration;
import impactservice.OpendapViewer;

import tools.Debug;
import tools.JSONResponse;
import tools.ProcessRunner;
//https://bhw485.knmi.nl:9443/impactportal/PROV?request=getprovenance&source=https%3A%2F%2Fbhw485.knmi.nl%3A9443%2Fimpactportal%2FDAP%2Fceda.ac.uk.openid.Maarten.Plieger%2FNORMADV.nc
/**
 * Servlet implementation class PROV
 */
public class PROV extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PROV() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  JSONResponse jsonResponse = new JSONResponse(request);
	  /*
	   * Get all arguments needed to convert NetCDF with provenance
	   */
	  String requestKVP = null,sourceKVP = null, formatKVP = null;
	  try {
      requestKVP = tools.HTTPTools.getKVPItemDecoded(request.getQueryString(), "request");
    } catch (Exception e) {
      jsonResponse.setException("parameter request has erros",e,request.getQueryString());
    }
	  try {
	    sourceKVP = tools.HTTPTools.getKVPItemDecoded(request.getQueryString(), "source");
    } catch (Exception e) {
      jsonResponse.setException("parameter source has errors",e,request.getQueryString());
    }
	  try {
	    formatKVP = tools.HTTPTools.getKVPItemDecoded(request.getQueryString(), "format");
	    if(formatKVP == null)formatKVP="image/png";
    } catch (Exception e) {
      jsonResponse.setException("parameter format has errors",e,request.getQueryString());
    }
	  if(jsonResponse.hasError()){
	    try {
	      jsonResponse.print(response);
	    } catch (Exception e1) {
	    }
	    return;
	  }
	  if(requestKVP == null){
	    jsonResponse.setErrorMessage("parameter request is not set",400);
	  }
	  if(sourceKVP == null){
      jsonResponse.setErrorMessage("parameter source is not set",400);
    }
	  
	  /*
	   * Run provenance
	   */
	  if(!jsonResponse.hasError()){
  	  if(requestKVP.equalsIgnoreCase("getprovenance")){
  	    Debug.println(requestKVP);
  	    Debug.println(sourceKVP);
  	    jsonResponse=null;
  	    //Get the provenance attributes as JSON
  	    jsonResponse = getProvenanceAttributes(request,sourceKVP);
  	    
  	    //If no error, just continue
  	    if(!jsonResponse.hasError()){
  	      try {
  	        jsonResponse.disableJSONP();
            String provdm = ((JSONObject) new JSONTokener(jsonResponse.getMessage()).nextValue()).getString("prov-dm");
            //Convert stuff to image
            if(formatKVP.equalsIgnoreCase("image/png")){
              try {
                byte[] pngFile = runPythonProvOnXML(provdm,"png");
                response.setContentType("image/png");
                OutputStream output = response.getOutputStream();
                output.write(pngFile);
                output.close();
                return;
              } catch (Exception e) {
                jsonResponse.setException("Unable run runPythonProvOnXML",e);
              }
            }
            
            if(formatKVP.equalsIgnoreCase("image/svg")){
              try {
                byte[] svgFile = runPythonProvOnXML(provdm,"svg");
                JSONObject svgObj = new JSONObject();
                svgObj.put("svg", new String(svgFile));
                jsonResponse.setMessage(svgObj);
              } catch (Exception e) {
                jsonResponse.setException("Unable run runPythonProvOnXML",e);
              }
            }
            
            if(formatKVP.equalsIgnoreCase("text/html")){
              try {
                byte[] svgFile = runPythonProvOnXML(provdm,"svg");
                response.setContentType("text/html");
                OutputStream output = response.getOutputStream();
                output.write(svgFile);
                output.close();
                return;
              } catch (Exception e) {
                jsonResponse.setException("Unable run runPythonProvOnXML",e);
              }
            }
            
            //Convert stuff to xml
            if(formatKVP.equalsIgnoreCase("text/xml")){
              response.setContentType("text/xml");
              response.getWriter().write(provdm);
              return;
            }
            
          } catch (JSONException e) {
            jsonResponse.setException("Unable to get prov-dm attribute from jsonresponse",e);
          }
  	      
  	    }
  	  }else{
  	    jsonResponse.setErrorMessage("request type is unknown, expected getprovenance",400);
  	  }
  	}
	  
	  try {
	    jsonResponse.setJSONP(request);
      jsonResponse.print(response);
    } catch (Exception e1) {
    }
	}
	
	/**
	 * Creates a temporary directory
	 * @return File object pointing to the tmp dir
	 * @throws IOException
	 */
	private static File createTempDirectory()
	    throws IOException{
	    final File temp;
	    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
	    if(!(temp.delete())){
	        throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	    }
	    if(!(temp.mkdir())){
	        throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
	    }
	    return (temp);
	}
	
	/**
	 * Runs a python script using the prov library to visualize PROV XML data
	 * @param provdm String with XML data containing provenance
	 * @param format Can either be png or svg
	 * @return Bytes of the image
	 * @throws Exception
	 */
	private byte[] runPythonProvOnXML(String provdm,String format)throws Exception {
	  String script=
	      "import prov\n"+
	      "import io\n"+
	      "import StringIO\n"+
	      "from prov.model import ProvDocument, ProvBundle, ProvException, first, Literal\n"+
	      "from prov.dot import prov_to_dot\n"+
	      "from prov.tests.test_model import AllTestsBase\n"+
	      "from prov.tests.utility import DocumentBaseTestCase\n"+
	      "xml_doc = StringIO.StringIO()\n"+
	      "xml = open(\"testWPS_PROV.xml\").read()\n"+
	      "xml_doc.write(str(xml))\n"+
	      "xml_doc.seek(0, 0)\n"+
	      "doc=ProvDocument.deserialize(xml_doc,format=\"xml\")\n"+
	      "dot = prov_to_dot(doc)\n"+
	      "svg_content = dot.create(format=\""+format+"\")\n"+
	      "with open(\"testWPS_PROV.dat\",\"w+\") as text_file:\n"+
	      "    text_file.write(str(svg_content))\n";

	  File tmpDir = createTempDirectory();
	  
	  Debug.println("Using tmp dir "+tmpDir);
	  tools.Tools.writeFile(tmpDir+"/provtopng.py", script);
	  tools.Tools.writeFile(tmpDir+"/testWPS_PROV.xml", provdm);
	  
	  

	  

	  class StderrPrinter implements ProcessRunner.StatusPrinterInterface{public void print(byte[] message,int bytesRead) {
	    errorMessage= errorMessage+(new String(message,0,bytesRead));
	  }
	  String errorMessage = "";
	  @Override
	  public void setError(String message) {
	    // TODO Auto-generated method stub

	  }

	  @Override
	  public String getError() {
	    return errorMessage;
	  }}
	  class StdoutPrinter implements ProcessRunner.StatusPrinterInterface{public void print(byte[] message,int bytesRead) {
	    Debug.println(new String(message));
	  }

	  @Override
	  public void setError(String message) {
	    // TODO Auto-generated method stub

	  }

	  @Override
	  public String getError() {
	    // TODO Auto-generated method stub
	    return null;
	  }
	  }

	  ProcessRunner.StatusPrinterInterface stdoutPrinter = new StdoutPrinter();
    ProcessRunner.StatusPrinterInterface stderrPrinter = new StderrPrinter();
    String[] environmentVariables = Configuration.PyWPSServerConfig.getPyWPSEnvironment();
    ProcessRunner processRunner = new ProcessRunner (stdoutPrinter,stderrPrinter,environmentVariables,tmpDir.toString());
    try{
      String commands[]={"python","provtopng.py"};
      processRunner.runProcess(commands,null);
    }
    catch (Exception e){
      Debug.errprint(e.getMessage());
      throw new Exception ("Unable to do run python: "+e.getMessage());
    }
    String error = stderrPrinter.getError();
    if(error.length()>0){
      Debug.errprintln(error);
      throw new Exception ("Unable to do run python: "+error);
    }
    byte[] pngFile = tools.Tools.readFileRaw(tmpDir+"/testWPS_PROV.dat");
    
    tools.Tools.rmdir(tmpDir);
    return pngFile;
  }

	/**
	 * Returns the provenance attributes from the NetCDF file's variable knmi_provenance
	 * @param request
	 * @param netCDFFileLocation
	 * @return
	 */
  private synchronized JSONResponse getProvenanceAttributes(HttpServletRequest request,
      String netCDFFileLocation) {
    
	  JSONResponse jsonResponse = new JSONResponse(request);
    OpendapViewer viewer = new OpendapViewer(Configuration.getImpactWorkspace()+"/diskCache/");
    JSONResponse viewerResponse = viewer.viewOpenDap(netCDFFileLocation,request);
    if(viewerResponse.hasError()){
      return viewerResponse;
    }
    viewerResponse.disableJSONP();
    JSONArray ncDump = null;
    try {
      ncDump = (JSONArray) new JSONTokener(viewerResponse.getMessage()).nextValue();
    } catch (JSONException e) {
      jsonResponse.setException("Unable to tokenize JSONObject from OpenDapViewer",e);
    }
    
    JSONObject provVariable = null;
    String knmi_provenance_prov_dmStr = null;
    String knmi_provenance_prov_lineageStr = null;
    String knmi_provenance_prov_bundleStr = null;
    
    for(int j=0;j<ncDump.length();j++){
      try {
        String variableName = ncDump.getJSONObject(j).getString("variable");
        if(variableName.equals("knmi_provenance")){
          provVariable = ncDump.getJSONObject(j);
          break;
        }
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    
    if(provVariable==null){
      jsonResponse.setErrorMessage("Provenance variable knmi_provenance was not found.", 400);
    }else{
      
      try {
        JSONArray knmi_provenance_attr=provVariable.getJSONArray("attributes");
        for(int j=0;j<knmi_provenance_attr.length();j++){
          String name = knmi_provenance_attr.getJSONObject(j).getString("name");
          if(name.equals("prov-dm")){
            knmi_provenance_prov_dmStr = knmi_provenance_attr.getJSONObject(j).getString("value");
          }
          if(name.equals("bundle")){
            knmi_provenance_prov_bundleStr = knmi_provenance_attr.getJSONObject(j).getString("value");
          }
          if(name.equals("lineage")){
            knmi_provenance_prov_lineageStr = knmi_provenance_attr.getJSONObject(j).getString("value");
          }
        }
      } catch (JSONException e) {
        jsonResponse.setException("Unable to get knmi_provenance attribute prov-dm",e);
      }
    }
    
    if(knmi_provenance_prov_dmStr==null){
      jsonResponse.setErrorMessage("Provenance attribute prov-dm in variable knmi_provenance was not found.", 400);
    }
    
    
    if(!viewerResponse.hasError()&&!jsonResponse.hasError()){
      JSONObject result = new JSONObject();
      try {
        result.put("prov-dm",knmi_provenance_prov_dmStr);
        result.put("bundle",knmi_provenance_prov_bundleStr);
        result.put("lineage",knmi_provenance_prov_lineageStr);
      } catch (JSONException e) {
        jsonResponse.setException("Error creating prov-dm jsonobject",e);
      }
      jsonResponse.setMessage(result);
    }
  
  
    return jsonResponse;
  }

  /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
