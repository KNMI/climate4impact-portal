package impactservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.DebugConsole;
import tools.HTTPTools;
import tools.JSONMessageDecorator;
import tools.MyXMLParser;
import tools.MyXMLParser.XMLElement;

public class THREDDSCatalogBrowser {
  static class Service{
    String name;
    Vector<AccesType>accesTypes = new Vector<AccesType>();
    static class AccesType{
      String serviceType;
      String base;
    }
    void addAccessType(String serviceType,String base){
      AccesType accesType = new AccesType();
      accesType.serviceType=serviceType;
      accesType.base=base;
      accesTypes.add(accesType);
    }
  }

  public static JSONArray browseThreddsCatalog(HttpServletRequest request,   HttpServletResponse response, JSONMessageDecorator errorResponder1,String variableFilter,String textFilter) throws MalformedURLException, Exception {
    String nodeStr=request.getParameter("node");
    
    if(nodeStr!=null){nodeStr=URLDecoder.decode(nodeStr,"UTF-8");}else{
      throw new Exception("Invalid node argument given");//errorResponder.printexception("nodeStr="+nodeStr);return null;
    }
    if(nodeStr.indexOf("http")!=0){
      throw new Exception("Invalid URL given");
    }
    
  
    nodeStr= HTTPTools.makeCleanURL(nodeStr);
    String rootCatalog =  new URL(nodeStr).toString();    
    String path = new URL(rootCatalog).getFile();
    String hostPath = rootCatalog.substring(0,rootCatalog.length()-path.length());
    //DebugConsole.println("Catalog: "+rootCatalog);
    //DebugConsole.println("hostPath: "+hostPath);
    
    MyXMLParser.XMLElement catalogElement = new MyXMLParser.XMLElement();
    try {
      catalogElement.parse(new URL(rootCatalog));
    } catch (Exception e1) {
      DebugConsole.errprintln("Unable to load catalog: "+e1.getMessage());
      throw e1;
    }
    
    Vector<Service> supportedServices = getSupportedServices(catalogElement.get("catalog"));
    
    /*DebugConsole.println("SupportedServices:");
    for(int j=0;j<supportedServices.size();j++){
      DebugConsole.println(supportedServices.get(j).name);
      for(int i=0;i<supportedServices.get(j).accesTypes.size();i++){
        DebugConsole.println("--"+supportedServices.get(j).accesTypes.get(i).serviceType+" with base "+supportedServices.get(j).accesTypes.get(i).base);
      }
    }*/
    
    JSONArray a = new JSONArray();
    
    
    
    /*JSONObject b = new JSONObject();
    b.put("text", "bla");
    b.put("expanded", true);
    b.put("cls", "folder");
    JSONArray c = new JSONArray();
    JSONObject d = new JSONObject();
    d.put("text", "bla2");
    d.put("leaf", true);
    c.put(d);
    b.put("children",c);
    a.put(b);*/
    long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
    addDatasets(rootCatalog,hostPath,supportedServices,a,catalogElement.get("catalog"),variableFilter,textFilter,null);
    long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
    DebugConsole.println("Finished parsing THREDDS catalog to JSON in ("+(stopTimeInMillis-startTimeInMillis)+" ms)");
    return a;
    
  }
  
  private static boolean checkNodeNameForFilter(String nodeName,String textFilter){
    if(textFilter!=null&&nodeName!=null){
      textFilter = textFilter.toLowerCase();
      nodeName = nodeName.toLowerCase();
      if(textFilter.length()>0){
        String [] filters = textFilter.split("\\||\\+| ");
        for(int f=0;f<filters.length;f++){
          if(nodeName.indexOf(filters[f])==-1){
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean addDatasets(String rootCatalog,String hostPath,Vector<Service> supportedServices, JSONArray a, XMLElement xmlElement,String variableFilter,String textFilter,XMLElement parent) throws Exception {
 
    Vector<XMLElement> datasets = xmlElement.getList("dataset");
    for(int j=0;j<datasets.size();j++){
      
      XMLElement dataset = datasets.get(j);
      JSONArray c = new JSONArray();
      
      boolean succeeded = addDatasets(rootCatalog,hostPath,supportedServices,c,dataset,variableFilter,textFilter,xmlElement);
      
      if(c.length()>0){
        //Make a folder
        JSONObject folder = new JSONObject();
        String name = dataset.getAttrValue("name");
        a.put(folder);
        folder.put("text", name);
        folder.put("children",c);
        folder.put("expanded", true);
        folder.put("cls", "folder");
        folder.put("variables",putVariableInfo(dataset));
        folder.put("variables2",putVariableInfo(xmlElement));
       
        if(succeeded==false){
          DebugConsole.errprint("Did not succeed!");
          return false;
        }
      }else{
        //This node has no childs
        //if(dataset.getAttrValue("name").indexOf("tas")!=-1)
        {
          //Create a leaf by default
          JSONObject leaf = new JSONObject();
          if(checkMaxChilds(a))return false;
          
          
       
          leaf.put("leaf", true);
          
          //DebugConsole.println("Leaf "+dataset.getAttrValue("name"));
          
          //Try to find defined serviceName, if not defined pick the first occuring one.
          String serviceName=null;
          Service service = null;
          String supportedServicesString = "";
          
          try{
            serviceName = dataset.get("serviceName").getValue();
            //DebugConsole.println("--serviceName:"+serviceName);
            service = getServiceByName(serviceName,supportedServices);
          }catch(Exception e){
            service = supportedServices.get(0);
          };
          //DebugConsole.println("Service="+service.name);
          if(service!=null){
            for(int i=0;i<service.accesTypes.size();i++){
              try{
                leaf.put(service.accesTypes.get(i).serviceType, hostPath+service.accesTypes.get(i).base+dataset.getAttrValue("urlPath"));
                //DebugConsole.println("--serviceType:"+service.accesTypes.get(i).serviceType);
                if(supportedServicesString.length()>0)supportedServicesString+=",";
                supportedServicesString+=service.accesTypes.get(i).serviceType;
              }catch(Exception e){
                
              }
            }
          }
          
          //Try to find additional servicenames based on access elements
          try{
            Vector<XMLElement> access = dataset.getList("access");
            for(int k=0;k<access.size();k++){
              serviceName = access.get(k).getAttrValue("serviceName");
              service = getServiceByName(serviceName,supportedServices);
              if(service!=null){
                for(int i=0;i<service.accesTypes.size();i++){
                 
                  leaf.put(service.accesTypes.get(i).serviceType, hostPath+service.accesTypes.get(i).base+dataset.getAttrValue("urlPath"));
                  if(supportedServicesString.length()>0)supportedServicesString+=",";
                  supportedServicesString+=service.accesTypes.get(i).serviceType;
                  //DebugConsole.println("--accessServiceName:"+service.accesTypes.get(i).serviceType);
                }
              }
            }
           
          }catch(Exception e){}
          
          String nodeName =  dataset.getAttrValue("name");
          leaf.put("text", nodeName);// - ("+supportedServicesString+")");
          
          String dataSize = "-";
          try{
            String units = dataset.get("dataSize").getAttrValue("units");;
            if(units.equals("Tbytes"))units="T";
            if(units.equals("Gbytes"))units="G";
            if(units.equals("Mbytes"))units="M";
            if(units.equals("Kbytes"))units="K";
            dataSize = dataset.get("dataSize").getValue()+""+units;
          }catch(Exception e){}
          leaf.put("dataSize", dataSize);
          
         // if(parent!=null){
           // putVariableInfo(b,parent);
          //}else{
          JSONArray variables = putVariableInfo(dataset);
          if(variables.length() == 0){
            variables = putVariableInfo(xmlElement);
            
          }
          leaf.put("variables",variables);
          //}
          
          boolean put = true;
          
          if(variableFilter!=null){
            try{
              if(variableFilter.length()>0){
                put=false;
                JSONArray variableList=variables;
              
                for(int v=0;v<variableList.length();v++){
                  try{
                    if(variableList.getJSONObject(v).getString("name").matches(variableFilter)){
                      put=true;
                      break;
                    }
                  }catch(Exception e){
                  }
                }
                //if(variableList.length()==0)put=true;
                if(variableList.length()==0){
                  DebugConsole.println(leaf.getString("text")+" - "+c.length());
                 // if(a!=null)put=true;
                        
                }
              }
            }catch(Exception e){
              DebugConsole.errprintln(e.getMessage());
              put = true;
            }
          }
          
          if(put)
          {
            put = checkNodeNameForFilter(nodeName,textFilter);
            if(put)a.put(leaf);
            //b.put("put",put);
           // a.put(b);
          }
          
        }
      }
      //addDatasets(c,datasets.get(j));
    }
    Vector<XMLElement> catalogRefs = xmlElement.getList("catalogRef");
    
  
    
    for(int j=0;j<catalogRefs.size();j++){
      XMLElement catalogRef = catalogRefs.get(j);
      JSONObject b = new JSONObject();
      if(checkMaxChilds(a))return false;
      
      String nodeName =  catalogRef.getAttrValue("xlink:title");
      boolean shouldPut = checkNodeNameForFilter(nodeName,textFilter);
      if(shouldPut){
        a.put(b);
        b.put("text",nodeName);
        b.put("expanded", true);
      
        String href=catalogRef.getAttrValue("xlink:href");
        
        b.put("cls", "folder");
        if(!href.startsWith("/")){
          String base = rootCatalog.substring(0,rootCatalog.lastIndexOf("/"));
          String url = HTTPTools.makeCleanURL(base+"/"+href);
          //b.put("id", HTTPTools.makeCleanURL(base+"/"+href));
          b.put("cls", "folder");
          b.put("leaf", false);
          b.put("href",  "?catalog="+url);
          b.put("catalogURL",url);
        }else{
          String url = HTTPTools.makeCleanURL(hostPath+href);
          b.put("cls", "file");
          b.put("leaf", false);
          b.put("href", "?catalog="+url);
          b.put("catalogURL",url);
        }
      }
     
    }
    return true;
    
    
  }

  private static JSONArray putVariableInfo(XMLElement dataset) throws JSONException {
    //Put variable info
    JSONArray variableInfos = new JSONArray(); 
    if(dataset!=null){
      try{
        
        Vector<XMLElement> variables = null;
        try{
          variables = dataset.get("variables").getList("variable");
        }catch(Exception e){
        }
        for(int j1=0;j1<variables.size();j1++){
          JSONObject varInfo = new JSONObject();
          varInfo.put("name", variables.get(j1).getAttrValue("name"));
          varInfo.put("vocabulary_name", variables.get(j1).getAttrValue("vocabulary_name"));
          varInfo.put("long_name", variables.get(j1).getValue());
          variableInfos.put(varInfo);
        }
      }catch(Exception e){
        //e.printStackTrace()
      }
    }
    return variableInfos;
    //b.put("variables", variableInfos);
    
  }

  private static boolean checkMaxChilds(JSONArray a) throws JSONException {
    int maxNumberOfItems=25000;
    if(a.length()>maxNumberOfItems){
      JSONObject b = new JSONObject();
      a.put(b);
      b.put("text", "..too many items for catalog browser! Only "+maxNumberOfItems+" items shown ...");
      b.put("cls", "leaf");
      b.put("leaf", true);
      return true;
    }
    return false;
  }

  private static Service getServiceByName(String serviceType, Vector<Service> supportedServices) {
    for(int j=0;j<supportedServices.size();j++){
      if(serviceType.equals(supportedServices.get(j).name))return supportedServices.get(j);
    }
    return null;
  }

  private static void recursivelyWalkServiceElement(Vector<Service> supportedServices ,Vector<XMLElement> v){
    for(int j=0;j<v.size();j++){
      String serviceType = "";
      try {
        serviceType = v.get(j).getAttrValue("serviceType");
      } catch (Exception e) {
      }
      
      if(!serviceType.equalsIgnoreCase("compound")){
        try {
          Service service = new Service();
          service.name=v.get(j).getAttrValue("name");
          service.addAccessType(serviceType, v.get(j).getAttrValue("base"));
          supportedServices.add(service);
        } catch (Exception e) {
          
        }
      }else{
        try {
          
          Service service = new Service();
          service.name=v.get(j).getAttrValue("name");
          Vector<XMLElement> w=v.get(j).getList("service");
          for(int i=0;i<w.size();i++){
            service.addAccessType( w.get(i).getAttrValue("serviceType"), w.get(i).getAttrValue("base"));
          }
          supportedServices.add(service);
        } catch (Exception e) {
          
        }
        recursivelyWalkServiceElement(supportedServices,v.get(j).getList("service"));
      }
      
    }
  }
  private static Vector<Service> getSupportedServices(XMLElement catalogElement) {
    Vector<Service> supportedServices = new  Vector<Service>();
    recursivelyWalkServiceElement(supportedServices,catalogElement.getList("service"));
    return supportedServices;
  }

}
