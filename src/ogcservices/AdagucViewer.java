package ogcservices;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import tools.DebugConsole;
import tools.MyXMLParser;

/**
 * Servlet implementation class AdagucViewer
 */
public class AdagucViewer extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdagucViewer() {
        super();
    }

	/**
	 * This function converts XML to JSON for the ADAGUC viewer, similar to the original xml2jsonrequest.php function
	 * It also determines whether the request is meant for a remote WMS server or for our local ADAGUC impactportal server.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
    String serviceStr=request.getParameter("SERVICE");
    

    
    //Get outputstream
    OutputStream out1 = null;
    try {
      out1 = response.getOutputStream();
    } catch (IOException e) {
      DebugConsole.errprint(e.getMessage());
      return;
    }
    
    if(serviceStr==null){
      String msg="SERVICE param missing";
      DebugConsole.errprintln(msg);
      out1.write(msg.getBytes());
      return;
    }

    if(serviceStr.equals("XML2JSON")){
      XML2JSON(request,out1,response);
      return;
    }
    if(serviceStr.equals("KML")){
      MakeKML(request,out1,response);
      return;
    }
   
  }

	
	private String getKVP(HttpServletRequest request, OutputStream out1,String key) throws IOException{
	  String value=null;
	  try {value=URLDecoder.decode(request.getParameter(key),"UTF-8"); } catch (Exception e) {out1.write((key+" missing").getBytes());}
	  return value;
	}
	
	private void MakeKML(HttpServletRequest request, OutputStream out1,     HttpServletResponse response) throws IOException {
    DebugConsole.println("MakeKML "+request.getQueryString());
    /*
     * srs=EPSG%3A4326&
     * bbox=-180,-138.79746835443038,180,138.79746835443038&
     * service=%252Fimpactportal%252FImpactService%253Fsource%253Dhttp%253A%252F%252Fopendap.nmdc.eu%252Fknmi%252Fthredds%252FdodsC%252FIS-ENES%252FCERFACS%252FCERFACS-SCRATCH2010%252Farpege1%252Fannual%252Farpege1_annual.nc&
     * layer=evapn%2524image%252Fpng%2524true%2524auto%252Fnearest%25241%25240&
     * selected=0&
     * dims=time$2099-01-01T12:00:00Z&
     * baselayers=world_raster$nl_world_line
     */
    //String srs = getKVP(request,out1,"srs");
    String bbox = getKVP(request,out1,"bbox");
    
    String[] bboxList=bbox.split(",");
    
    String service = getKVP(request,out1,"service");
    String layers = getKVP(request,out1,"layer");
    //String selected = getKVP(request,out1,"selected");
    String dims = getKVP(request,out1,"dims");
    //String baselayers = getKVP(request,out1,"baselayers");

    //Compose a list of services
    String[] serviceList=service.split(",");

    //Compose a list of layers
    String[] layerList=layers.split(",");

    
    //Clean dims: Replace all dims except TIME and ELEVATION by DIM_$dim
    dims = dims.replaceAll("$","=");
    String[] dimList = dims.replaceAll("\\$","=").split(",");

    String cleanDimString="";
    for(int j=0;j<dimList.length;j++){
      String[] dimTerms=dimList[j].split("=");
      DebugConsole.println("dimTerms[0]="+dimTerms[0]);
      if(dimTerms[0].equalsIgnoreCase("time")||dimTerms[0].equalsIgnoreCase("elevation")){
        cleanDimString+="&amp;"+URLEncoder.encode(dimList[j],"UTF-8");
      }else{
        cleanDimString+="&amp;"+"DIM_"+URLEncoder.encode(dimList[j],"UTF-8");
      }
    }
    //cleanDimString=URLEncoder.encode(cleanDimString,"UTF-8");
    DebugConsole.println("cleanDimString="+cleanDimString);
    String kml="";
    kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    kml += "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n";
    kml +="<Folder>\n";
    kml +="<name>ADAGUC WMS</name>\n";
    kml +="<open>1</open>\n";
    
    for(int j=0;j<layerList.length;j++){
      String[] layerProperties = layerList[j].split("\\$");
      String layer=URLEncoder.encode(layerProperties[0],"UTF-8");
      //String format=layerProperties[1];
      String enabled=layerProperties[2];
      String styles=URLEncoder.encode(layerProperties[3],"UTF-8");;
      //String opacity=layerProperties[4];
      String serviceIndex=layerProperties[5];
      
      String layerService=serviceList[Integer.parseInt(serviceIndex)];
      if(layerService.indexOf("/")==0){
        String hostname =request.getRequestURL()+"/../..";
        layerService=hostname+layerService.replaceAll("&", "&amp;");
      }
      DebugConsole.println("layerService=\n"+layerService);
      

      if(enabled.equals("true")){
        kml +="<GroundOverlay>\n";
        kml +="    <name>"+layer+"</name>\n";
        kml +="    <Icon>\n";
        kml +="           <href>"+layerService+"&amp;LAYERS="+layer+"&amp;STYLES="+styles+"&amp;"+cleanDimString+"&amp;SERVICE=WMS&amp;VERSION=1.1.1&amp;REQUEST=GetMap&amp;SRS=EPSG:4326&amp;WIDTH=1024&amp;HEIGHT=1024&amp;TRANSPARENT=TRUE&amp;FORMAT=image/png&amp;</href>\n";
        //kml +="           <href>$service&amp;SERVICE=WMS&amp;VERSION=1.1.1&amp;REQUEST=GetMap&amp;SRS=EPSG:4326&amp;WIDTH=1024&amp;HEIGHT=1024&amp;LAYERS=$name&amp;STYLES=$styles&amp;TRANSPARENT=TRUE&amp;FORMAT=image/png&amp;$dimstring</href>\n";
        kml +="           <viewRefreshMode>onStop</viewRefreshMode>\n";
        kml +="           <viewBoundScale>1.0</viewBoundScale>\n";
        kml +="           <refreshMode>onExpire</refreshMode>\n";
        kml +="           <viewRefreshTime>1</viewRefreshTime>\n";
        kml +="    </Icon>\n";
        kml +="    <LatLonBox>\n";
        kml +="          <north>"+bboxList[3]+"</north>\n";
        kml +="           <south>"+bboxList[1]+"</south>\n";
        kml +="           <east>"+bboxList[2]+"</east>\n";
        kml +="           <west>"+bboxList[0]+"</west>\n";
        kml +="    </LatLonBox>\n";
        kml +="</GroundOverlay>\n";
      }
    }
    
    kml +="</Folder>\n";
    kml +="</kml>\n";
    response.setContentType("application/vnd.google-earth.kml+xml");
    response.setHeader("Content-Disposition", "attachment;filename=adagucwms2kml.kml");
    response.setHeader("Pragma", "public");
    response.setHeader("Cache-Control", "max-age=0");

    out1.write(kml.getBytes());
  }

  /**
	 * Converts XML file pointed with request to JSON file
	 * @param requestStr
	 * @param out1
	 * @param response
	 */
	private void XML2JSON(HttpServletRequest request, OutputStream out1, HttpServletResponse response) {
	  DebugConsole.println("XML2JSON "+request.getQueryString());
    
    String requestStr=request.getParameter("request");
    if(requestStr==null){
      requestStr=request.getParameter("REQUEST");
    }
    String callbackStr=request.getParameter("callback");
    if(callbackStr==null){
      callbackStr=request.getParameter("CALLBACK");
    }

    try {
      if(requestStr==null || callbackStr == null){
        String msg="REQUEST param or CALLBACK param missing";
        DebugConsole.errprintln(msg);
        out1.write(msg.getBytes());
        return;
      }
     
      requestStr=URLDecoder.decode(requestStr,"UTF-8");
      MyXMLParser.XMLElement rootElement = new MyXMLParser.XMLElement();
      
      DebugConsole.println("Making XML to JSON request for "+requestStr);

      boolean isLocalADAGUC = false;
      
      //Determine whether the request is to the ADAGUC server in our impactportal or to any other WMS server?
      if(requestStr.indexOf("impactportal/ImpactService?")!=-1){
        isLocalADAGUC=true;
      }

      if(isLocalADAGUC){
        //Local XML2JSON request to our local adagucserver
        DebugConsole.println("Running local CGI with "+requestStr);
        //Remove /impactportal/ImpactService? from the requeststring
        int beginningOfUrl = requestStr.indexOf("?");
        if(beginningOfUrl!=-1){
          requestStr = requestStr.substring(beginningOfUrl+1);
          DebugConsole.println("Truncated URL to "+requestStr);
        }
        ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream();
        AdagucServer.runADAGUCWMS(request,null,requestStr,stringOutputStream);
        try{
          rootElement.parseString(stringOutputStream.toString());
        }catch(Exception e){
          throw new Exception("Unable to parse XML at "+requestStr);
        }
      }else{
        //Remote XML2JSON request to external WMS service
        DebugConsole.println("Converting XML to JSON for "+requestStr);
        rootElement.parse(new URL(requestStr));
      }
     
      response.setContentType("application/json");
      
      
      //Output JSON using JSONP
      out1.write((callbackStr+"("+rootElement.toJSON()+");").getBytes());
      rootElement = null;
      return;
      
    } catch (Exception e) {
      if(e.getMessage()!=null){
        try {
          out1.write(e.getMessage().getBytes());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
      }
      return;
    }
  }

  /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  doGet(request,response);
	}

}
