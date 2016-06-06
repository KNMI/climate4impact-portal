package esgfsearch;

import impactservice.ImpactUser;
import impactservice.LoginManager;
import impactservice.THREDDSCatalogBrowser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.DiskCache;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.JSONResponse;
import tools.JSONResponse.JSONResponseException;
import tools.KVPKey;
import tools.LockOnQuery;
import tools.MyXMLParser;
import tools.MyXMLParser.XMLElement;
import tools.Tools;

public class Search {
  private int maxAmountOfDataSetsInJSON = 250;
  private int searchCacheTimeValiditySec = 10*60;
  private int catalogContentsTimeValiditySec = 10*60;
  private int catalogCheckerTimeValiditySec = 60*5;
  private int catalogCheckerTimeOutMS = 2000;
  private int searchGetTimeOutMS = 15000;
  
  private String searchEndPoint = null;
  private String cacheLocation = null;
  private ExecutorService getCatalogExecutor = null;
  
  public Search(String searchEndPoint, String cacheLocation, ExecutorService getCatalogExecutor) {
    this.searchEndPoint = searchEndPoint;
    this.cacheLocation = cacheLocation;
    this.getCatalogExecutor =getCatalogExecutor;
  }

  public JSONResponse getFacets(String facets, String query,int pageNumber,int pageLimit) {
    
    try{
      LockOnQuery.lock(facets+query,0);
      JSONResponse r = _getFacetsImp(facets,query,pageNumber,pageLimit);
      LockOnQuery.release(facets+query);
      return r;
    }catch(Exception e){
      JSONResponse r = new JSONResponse();
      r.setException(e.getClass().getName(), e);
      
      e.printStackTrace();
      return r;
    }
    
  }
  
  private JSONResponse _getFacetsImp(String facets,String query, int pageNumer,int searchLimit) throws JSONException {
  
    JSONResponse r = new JSONResponse();
    
    String esgfQuery = "facets=*&offset="+(pageNumer*searchLimit)+"&limit="+searchLimit+"&sort=true&";
    
    if(facets!=null){
      esgfQuery = "facets="+facets+"&limit="+searchLimit+"&sort=true&";
    }
    
    if(query!=null){
      //Debug.println("QUERY is "+query);
       KVPKey kvp = HTTPTools.parseQueryString(query);
       SortedSet<String> kvpKeys = kvp.getKeys();
       for(String k : kvpKeys){
         if(!k.equalsIgnoreCase("time_start_stop")&&!k.equalsIgnoreCase("bbox")&&!k.equalsIgnoreCase("query")){
           //Debug.println("KEY "+k+" = "+kvp.getValue(k));
           for(String value : kvp.getValue(k)){
             try {
              esgfQuery = esgfQuery+k+"="+URLEncoder.encode(value,"UTF-8")+"&";
            } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
            }
           }
         }else{
           if(k.equalsIgnoreCase("time_start_stop")){
             String timeStartStopValue = kvp.getValue(k).firstElement();
             if(timeStartStopValue.length()==9){
               String [] timeStartStopValues = timeStartStopValue.split("/");
               if(timeStartStopValues.length==2){
                 int yearStart = Integer.parseInt(timeStartStopValues[0]);
                 int yearStop = Integer.parseInt(timeStartStopValues[1]);
                 if(yearStart>=0&&yearStart<=9999&&yearStop>=0&&yearStop<=9999){
                   esgfQuery += "start="+String.format("%04d", yearStart)+"-01-01T00:00:00Z&";
                   esgfQuery += "end="+String.format("%04d", yearStop)+"-01-01T00:00:00Z&";
                   
                 }
               }
             }
           }
         }
         if(k.equalsIgnoreCase("bbox")){
           String bboxValue = kvp.getValue(k).firstElement();
           if(bboxValue.length()>3){
             String[] bboxValueItems =bboxValue.split(",");
             if(bboxValueItems.length == 4){
               esgfQuery += "bbox=%5B"+bboxValue+"%5D&";
             }
           }
         
         }
         if(k.equalsIgnoreCase("query")){
           String freeTextValue = kvp.getValue(k).firstElement();
           if(freeTextValue.length()>0){
             try {
              esgfQuery += "query="+URLEncoder.encode(freeTextValue,"UTF-8")+"&";
            } catch (UnsupportedEncodingException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
           }
         
         }
       }
    }
    
    Debug.println("Query is "+searchEndPoint+esgfQuery);
    
    String identifier = "ESGFSearch.getFacets"+esgfQuery;
    
    String XML = DiskCache.get(cacheLocation, identifier+".xml",searchCacheTimeValiditySec);
    if(XML == null){
      String url = searchEndPoint+esgfQuery;
      try {
        XML = HTTPTools.makeHTTPGetRequest(new URL(url),searchGetTimeOutMS);
        DiskCache.set(cacheLocation,identifier+".xml",XML);
      } catch (MalformedURLException e2) {
        r.setException("MalformedURLException",e2,url);
        return r;
      } catch (WebRequestBadStatusException e2) {
        r.setException("WebRequestBadStatusException",e2,url);
        return r;
      } catch (IOException e2) {
        r.setException("IOException",e2,url);
        return r;
      }
    }
    
    
    
    MyXMLParser.XMLElement el = new MyXMLParser.XMLElement();
    
    try {
      el.parseString(XML);
    } catch (Exception e1) {
      r.setErrorMessage("Unable to parse XML",500);
      return r;
    }
    
    JSONObject facetsObj = new JSONObject();
  
    
    try {
      Vector<XMLElement> lst=el.get("response").getList("lst");
      
      for(XMLElement a : lst){
        
        try{
          if(a.getAttrValue("name").equals("facet_counts")){
            Vector<XMLElement> facet_counts=a.getList("lst");
            for(XMLElement facet_count : facet_counts){
              if(facet_count.getAttrValue("name").equals("facet_fields")){
                Vector<XMLElement> facet_fields=facet_count.getList("lst");
                for(XMLElement facet_field : facet_fields){
                 
                  Vector<XMLElement> facet_names=facet_field.getList("int");
                  SortedMap<String,Integer> sortedFacetElements = new TreeMap<String,Integer>();
                  for(XMLElement facet_name : facet_names){
                    sortedFacetElements.put(facet_name.getAttrValue("name"),Integer.parseInt(facet_name.getValue()));
                  }
                
                  JSONArray facet = new JSONArray();
                  
                  //int first = 0;
                  for (SortedMap.Entry<String, Integer> entry : sortedFacetElements.entrySet()){
                    //if(first <5){
                    //Debug.println(entry.getKey());
                    //first++;
                      facet.put(entry.getKey());//, entry.getValue());
                    //}
                  }
                  facetsObj.put(facet_field.getAttrValue("name"),facet);
                }
              }
            }
          }
        }catch(Exception e){
          r.setErrorMessage("No name attribute",500);
          return r;
        }
      }
      
      
      facetsObj.put("time_start_stop",new JSONArray("[1850%2F1950]"));
      facetsObj.put("bbox",new JSONArray("[0]"));
      facetsObj.put("query",new JSONArray("[0]"));
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    
    
    JSONObject result = new JSONObject();
    JSONObject responseObj = new JSONObject();
    result.put("response",responseObj);
    
    responseObj.put("limit",searchLimit);
    
   
    
    JSONArray searchResults = new JSONArray(); 
    
    try {
      Vector<XMLElement> result1=el.get("response").getList("result");
      
      for(XMLElement a : result1){
        
        try{
          if(a.getAttrValue("name").equals("response")){
            responseObj.put("numfound",Integer.parseInt(a.getAttrValue("numFound")));
            Vector<XMLElement> doclist=a.getList("doc");
            
            for(XMLElement doc : doclist){
              JSONObject searchResult = new JSONObject();
              searchResults.put(searchResult);
              Vector<XMLElement> arrlist = doc.getList("arr");
              Vector<XMLElement> strlist = doc.getList("str");
              for(XMLElement arr : arrlist){
                String attrName = arr.getAttrValue("name");
                if(attrName.equals("url")){
                  String urlToCheck = arr.get("str").getValue().split("#")[0];
                  urlToCheck = urlToCheck.split("\\|")[0];
                  searchResult.put("url",urlToCheck);
                }
              }
              for(XMLElement str : strlist){
                String attrName = str.getAttrValue("name");
                if(attrName.equals("id")){
                  searchResult.put("id",str.getValue().split("\\|")[0]);
                  //
                }
              }
              
            }
          }
        }catch(Exception e){
          r.setErrorMessage("No name attribute",500);
          return r;
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    
        
    
    
  
    
    
    try {
      responseObj.put("results",searchResults );
    } catch (JSONException e) {
      r.setException("JSONException unable to put response", e);
      return r;
    }
    
    try {
      result.put("facets", facetsObj);
    } catch (JSONException e) {
      r.setException("JSONException unable to put facets", e);
      return r;
    }
    
   
    r.setMessage(result.toString());
  
    
    return r;
  }


  
  public class ASyncGetCatalogRequest implements Callable<ASyncGetCatalogResponse> {
    private String url;
    private HttpServletRequest request;
    public ASyncGetCatalogRequest(String url, HttpServletRequest request) {
        this.url = url;
        this.request = request;
    }

    @Override
    public ASyncGetCatalogResponse call() throws Exception {
     Exception e = null;
     String a = null;
      try{
        a=getCatalog(url,request);
        //Debug.println("Catalog OK ");
      }catch(Exception e2){
        //Debug.errprintln("ASyncGetCatalogResponse: exception: "+e2.getMessage());
        e=e2;
      }
      return new ASyncGetCatalogResponse(a,e);
    }
  }
  
  public class ASyncGetCatalogResponse {
      private String body;
      private Exception exception;
//      boolean _isFinished=false;
  
      public ASyncGetCatalogResponse(String string, Exception exception) {
          this.body = string;
          this.exception = exception;
//          _isFinished=true;
      }
  
      public String getBody() {
          return body;
      }
      
      public Exception getException(){
        return exception;
      }
      
//      public boolean isFin2ished(){
//        return _isFinished;
//      }
  }
  

  static Map<String,URLBeingChecked> urlsBeingChecked = new  ConcurrentHashMap<String, URLBeingChecked> ();
  
  class URLBeingChecked{
    public Future<ASyncGetCatalogResponse> response;
    private long creationDate;
    public long getCreationDate(){
      return creationDate;
    }
    public URLBeingChecked(String query, HttpServletRequest request) {
      creationDate = tools.DateFunctions.getCurrentDateInMillis();
      try {
        response = getCatalogExecutor.submit(new ASyncGetCatalogRequest(query,request));
        creationDate = tools.DateFunctions.getCurrentDateInMillis();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
  }

  public  JSONResponse  checkURL(String query,HttpServletRequest request) {
    synchronized(urlsBeingChecked){
      //Debug.println("Checking: "+query);
      JSONResponse r = new JSONResponse(request);
      if(urlsBeingChecked.containsKey((String)query)==false){
        //Debug.println("INSERT");
        URLBeingChecked urlBeingChecked = new URLBeingChecked(query,request);
        urlsBeingChecked.put((String)query, urlBeingChecked);
        r.setMessage("{\"message\":\"start checking\",\"ok\":\"busy\"}");
      }else{
        URLBeingChecked urlBeingChecked =  urlsBeingChecked.get((String)query);
        
        if(urlBeingChecked.getCreationDate()+(catalogCheckerTimeValiditySec*1000)<tools.DateFunctions.getCurrentDateInMillis()){
          urlsBeingChecked.remove(query);
          urlBeingChecked.response.cancel(true);
          Debug.println("Refiring "+query);
          return checkURL(query,request);
        }
        if(urlBeingChecked.response.isDone()==false){
          r.setMessage("{\"message\":\"still checking\",\"ok\":\"busy\"}");
          //Debug.println("STILL");
        }else{
          try {
            ASyncGetCatalogResponse response = urlBeingChecked.response.get();
            Exception e = response.getException();
            if(e!=null){
              if(e instanceof WebRequestBadStatusException){
                WebRequestBadStatusException w = ((WebRequestBadStatusException)e);
                JSONObject m = new JSONObject();
                try {
                  String message = "Error, code: "+w.getStatusCode();
                  m.put("message",message);
                  m.put("ok", "false" );
                  urlsBeingChecked.remove(query);
                } catch (JSONException e1) {
                }
                r.setMessage(m);
              }else{
                Debug.println("Exception"+e.getMessage());
                JSONObject m = new JSONObject();
                try {
                  String message = e.getClass().getSimpleName();
                  m.put("message",message);
                  m.put("ok", "false" );
                  urlsBeingChecked.remove(query);
                } catch (JSONException e1) {
                }
                r.setMessage(m);
              }
            }else{
              //String body = urlBeingChecked.response.get().getBody();
              r.setMessage("{\"message\":\"status ok\",\"ok\":\"ok\"}");
            }
            
            
            
          } catch (Exception e) {
            Debug.printStackTrace(e);
            JSONObject m = new JSONObject();
            try {
              String message = e.getCause().getMessage();
              m.put("message",message);
              m.put("ok", "false" );
              urlsBeingChecked.remove(query);
            } catch (JSONException e1) {
            }
            r.setMessage(m);
            
            
          }
         
          //Debug.println("Done");
        }
      }
      return r;
    }
  }
  
  public String getCatalog(String catalogURL,HttpServletRequest request) throws Exception{
  

    String response = DiskCache.get(cacheLocation, "dataset_"+catalogURL, catalogContentsTimeValiditySec);
    if(response!=null){
      //Debug.println("CATALOG FROM CACHE "+catalogURL);
      return response;
    }
    boolean ISOK = false;
    String errorMessage = "";
    try {
      Debug.println("CATALOG GET "+catalogURL);
      ImpactUser user = null;
      try{
        user = LoginManager.getUser(request);
      }catch(Exception e){
      }
      if(user!=null){
        
        Debug.println("Data URL = "+ user.getDataURL());
        if(catalogURL.indexOf(user.getDataURL())==0){
          String fileName = catalogURL.substring(user.getDataURL().length());
          Debug.println("Filename = "+fileName);
          Debug.println("Filelocation = "+user.getDataDir()+"/"+fileName);
          fileName = tools.HTTPTools.validateInputTokens(fileName);
          if(fileName.indexOf("..")!=-1){
            throw new Exception("Unable to GET catalog "+catalogURL);
          }
          
          return Tools.readFile(user.getDataDir()+"/"+fileName);
          
            
          
        }
      }
 
      int hashTagLoc = catalogURL.indexOf("#");
      if(hashTagLoc != -1){
        catalogURL = catalogURL.split("#")[0];
      }
      
      
      
      response = HTTPTools.makeHTTPGetRequest(catalogURL,catalogCheckerTimeOutMS);
      ISOK = true;
//    } catch (WebRequestBadStatusException e) {
//      Debug.println("CATALOG GET WebRequestBadStatusException");
//      if(e.getStatusCode()==404){
//        errorMessage = "Not found (404)";
//      }else if(e.getStatusCode()==403){
//        errorMessage = "Unauthorized (403)";
//      }else if(e.getStatusCode()==504){
//        errorMessage = "Gateway timeout (504)";
//      }else{
//        errorMessage = e.getMessage()+" code ("+e.getStatusCode()+")";
//      }
    } catch (IOException e) {
      Debug.println("CATALOG GET IOException");
      errorMessage = e.getMessage();
      
    }
    if(ISOK == false){
      throw new Exception("Unable to GET catalog "+catalogURL+" : "+errorMessage);
    }
    //Debug.println("CATALOG GET SET");
    DiskCache.set(cacheLocation, "dataset_"+catalogURL, response);
    //Debug.println("CATALOG GET SET DONE");
    return response;
  }
  
  


  private JSONObject makeJSONFromSearchQuery(String query,HttpServletRequest request) throws JSONResponse.JSONResponseException {
    
    JSONObject jsonresult = new JSONObject();
    try{
      //DOSTUFF
      
      LockOnQuery.lock(query,0);
      JSONResponse r = _getFacetsImp(null,query,0,maxAmountOfDataSetsInJSON);
      LockOnQuery.release(query);

      if(r.hasError()){
       throw new JSONResponse.JSONResponseException(r);
      }
      JSONObject searchResults =  (JSONObject) new JSONTokener(r.getMessage()).nextValue();
      long numFound = searchResults.getJSONObject("response").getLong("numfound");
      
      if(numFound>maxAmountOfDataSetsInJSON){
        throw new JSONResponse.JSONResponseException("Too many results, maximum of "+maxAmountOfDataSetsInJSON+" datasets allowed.",200);
      }else{
        jsonresult.put("numDatasets", searchResults.getJSONObject("response").getLong("numfound"));
        jsonresult.put("ok", "ok");//For client to check whether its all OK.
      
        
        KVPKey kvp = HTTPTools.parseQueryString(query);
        Vector<String> variableList = kvp.getValue("variable");
        String variableFilter = "";
        for(int k=0;k<variableList.size();k++){
          if(k>0){
            variableFilter+="|";
          }
          variableFilter+=variableList.get(k);
        }
      
       
        JSONArray results = searchResults.getJSONObject("response").getJSONArray("results");
        int numFiles = 0;
        int numDap = 0;
        long totalFileSize = 0;
        JSONArray catalogAggregation = new JSONArray();
        for(int j=0;j<results.length();j++){
          //Debug.println("---------- Converting catalog to json nr "+j+" ----------");
          try{
            String url = results.getJSONObject(j).getString("url");
        
       
            JSONArray files = THREDDSCatalogBrowser.browseThreddsCatalog(request,url, variableFilter,null);
            //Debug.println(files.toString());
            catalogAggregation.put(files.getJSONObject(0));
            THREDDSCatalogBrowser.MakeFlat b = new THREDDSCatalogBrowser.MakeFlat();
            JSONArray flat = b.makeFlat(files);
            
            for(int i=0;i<flat.length();i++){
              
              String openDAPURL=null;
              String httpURL=null;
              String fileSize = "";
              JSONObject a=flat.getJSONObject(i);
  
              try{openDAPURL = a.getString("opendap");}catch (JSONException e) {}
              try{httpURL = a.getString("httpserver");}catch (JSONException e) {}
  
              try{fileSize = a.getString("fileSize");   totalFileSize=totalFileSize + Long.parseLong(fileSize);}catch (Exception e) {}
             
              if(openDAPURL!=null){numDap++;}
              if(httpURL!=null){numFiles++;}
             
              
            }
          }catch(Exception e){
            Debug.errprintln("CATALOG Error for nr "+j+"): "+results.getJSONObject(j).getString("id"));
            try{
              JSONObject b = new JSONObject();
              b.put("catalogurl", results.getJSONObject(j).getString("url"));
              JSONArray childs = new JSONArray();
              b.put("children", childs);
              b.put("text", "undefined");
              
              catalogAggregation.put(b);
            }catch(Exception e2){
              Debug.printStackTrace(e2);
            }
          }
        }
        jsonresult.put("query", query);
        jsonresult.put("text", query);
        jsonresult.put("numFiles", numFiles);
        jsonresult.put("numDap", numDap);
        jsonresult.put("cls", "folder");
        jsonresult.put("fileSize", totalFileSize);
        jsonresult.put("children", catalogAggregation);
       
      }
      
     
    }catch(JSONException e){
      Debug.printStackTrace(e);
      throw new JSONResponse.JSONResponseException("Unable to query",200);
    }
    return jsonresult;
  }
  
  
  public JSONResponse getSearchResultAsJSON(String query,HttpServletRequest request) {
    JSONResponse result = new JSONResponse(request);
    JSONObject jsonresult;
  
      try {
        jsonresult = makeJSONFromSearchQuery(query,request);
        result.setMessage(jsonresult);
      } catch (JSONResponseException e) {
        result.setException(e);
      }
     

    return result;
  }
  
  public String getSearchResultAsCSV(String query,HttpServletRequest request){
    StringBuffer result = new StringBuffer();
    int numdatasets = 0;
    int numcatalogsfailed = 0;
    int numopendap = 0;
    int numgridftp=0;
    int numhttpserver=0;
    JSONObject jsonresult;
      
      try {
        jsonresult = makeJSONFromSearchQuery(query,request);
        
        JSONArray catalogs = jsonresult.getJSONArray("children");
        Debug.println("NR of catalogs:"+catalogs.length());
        for(int j=0;j<catalogs.length();j++){
          String catalogURL = null;
          try{
            catalogURL = catalogs.getJSONObject(j).getString("catalogurl");
            for(int mode=0;mode<3;mode++){
              String text = catalogs.getJSONObject(j).getString("text");
              JSONArray files = catalogs.getJSONObject(j).getJSONArray("children");
              
              if(mode == 0){
                if(files.length()>0){
                  numdatasets++;
                  result.append("catalog;");result.append(text);result.append(";");result.append(catalogURL);result.append("\n");
                }else{
                  throw new Exception("No records for this set.");
                }
              }
              for(int i=0;i<files.length();i++){
                
                if(mode==0){
                  //httpserver
                  try{
                    String file = files.getJSONObject(i).getString("httpserver");
                    result.append("httpserver;");result.append(text);result.append(";");result.append(file);result.append("\n");
                    numhttpserver++;
                  }catch(Exception e){
                  }
                }else if(mode ==1){
                
                  //opendap
                  try{
                    String file = files.getJSONObject(i).getString("opendap");
                    result.append("opendap;");result.append(text);result.append(";");result.append(file);result.append("\n");
                    numopendap++;
                  }catch(Exception e){
                  }
                }else if(mode==2){
                  //gridftp
                  try{
                    String file = files.getJSONObject(i).getString("gridftp");
                    result.append("gridftp;");result.append(text);result.append(";");result.append(file);result.append("\n");
                    numgridftp++;
                  }catch(Exception e){
                    
                  }
                }
              } 
            }
          }catch(Exception e){
            Debug.errprintln(e.getMessage());
            //Debug.printStackTrace(e);
            numcatalogsfailed++;
            if(catalogURL == null){
              catalogURL = "undefined";
            }
            result.append("catalog_failed;");result.append("undefined");result.append(";");result.append(catalogURL);result.append("\n");
          }
        }
        
      } catch (JSONException e) {
        return e.getMessage();
      } catch (JSONResponseException e) {
        return e.getMessage();
      }
     
      
    String header = "type;dataset;link\n";
    header += "info;numdataset;"+numdatasets+"\n";
    header += "info;numhttpserver;"+numhttpserver+"\n";
    header += "info;numopendap;"+numopendap+"\n";
    header += "info;numgridftp;"+numgridftp+"\n";
    header += "info;numcatalogsfailed;"+numcatalogsfailed+"\n";
    header += "info;query;"+query+"\n";
    result.insert(0,header);
    return result.toString();
  }
  
  public JSONResponse addtobasket(String query,HttpServletRequest request) {
    JSONResponse result = new JSONResponse(request);
    
    JSONObject jsonresult;
    try {
      jsonresult = makeJSONFromSearchQuery(query,request);
      String message = jsonresult.toString();
      
      try {
        ImpactUser user = LoginManager.getUser(request);
        String dataDir = user.getDataDir();
        tools.Tools.writeFile(dataDir+"/test.catalog", message);
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      
      result.setMessage(message);
    } catch (Exception e1) {
      result.setException("Unable to make a file list from the search query.",e1);
    }


    return result;
  }


}
