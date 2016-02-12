package esgfsearch;

import impactservice.ImpactUser;
import impactservice.LoginManager;
import impactservice.THREDDSCatalogBrowser;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.DiskCache;
import tools.HTTPTools;
import tools.LockOnQuery;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.KVPKey;
import tools.MyXMLParser;
import tools.MyXMLParser.XMLElement;
import tools.JSONResponse;
import tools.Tools;

public class Search {
  private String searchEndPoint = null;
  private String cacheLocation = null;
  private ExecutorService getCatalogExecutor = null;
  
  public Search(String searchEndPoint, String cacheLocation, ExecutorService getCatalogExecutor) {
    this.searchEndPoint = searchEndPoint;
    this.cacheLocation = cacheLocation;
    this.getCatalogExecutor =getCatalogExecutor;
  }

  public JSONResponse getFacets(String facets, String query) {
    int searchLimit = 25;
    try{
      LockOnQuery.lock(facets+query,0);
      JSONResponse r = _getFacetsImp(facets,query,searchLimit);
      LockOnQuery.release(facets+query);
      return r;
    }catch(Exception e){
      JSONResponse r = new JSONResponse();
      r.setException(e.getClass().getName(), e);
      
      e.printStackTrace();
      return r;
    }
    
  }
  
  private JSONResponse _getFacetsImp(String facets,String query, int searchLimit) throws JSONException {
  
    JSONResponse r = new JSONResponse();
    
    String esgfQuery = "facets=*&limit="+searchLimit+"&sort=true&";
    
    if(facets!=null){
      esgfQuery = "facets="+facets+"&limit="+searchLimit+"&sort=true&";
    }
    
    if(query!=null){
      Debug.println("QUERY is "+query);
       KVPKey kvp = HTTPTools.parseQueryString(query);
       SortedSet<String> kvpKeys = kvp.getKeys();
       for(String k : kvpKeys){
         Debug.println("KEY "+k+" = "+kvp.getValue(k));
         for(String value : kvp.getValue(k)){
           try {
            esgfQuery = esgfQuery+k+"="+URLEncoder.encode(value,"UTF-8")+"&";
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
         }
       }
    }
    
    Debug.println("Query is "+searchEndPoint+esgfQuery);
    
    String identifier = "ESGFSearch.getFacets"+esgfQuery;
    
    String XML = DiskCache.get(cacheLocation, identifier+".xml", 200000);
    if(XML == null){
      try {
        XML = HTTPTools.makeHTTPGetRequest(new URL(searchEndPoint+esgfQuery));
        DiskCache.set_2(cacheLocation,identifier+".xml",XML);
      } catch (MalformedURLException e2) {
        r.setException("MalformedURLException",e2);
        return r;
      } catch (WebRequestBadStatusException e2) {
        r.setException("WebRequestBadStatusException",e2);
        return r;
      } catch (IOException e2) {
        r.setException("IOException",e2);
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
        if(urlBeingChecked.getCreationDate()+1000*60*5<tools.DateFunctions.getCurrentDateInMillis()){
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
  
    String response = DiskCache.get(cacheLocation, "dataset_"+catalogURL, 5*60);
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
      
      
      
      response = HTTPTools.makeHTTPGetRequest(catalogURL);
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
    DiskCache.set_2(cacheLocation, "dataset_"+catalogURL, response);
    //Debug.println("CATALOG GET SET DONE");
    return response;
  }
  
  

  private class MakeFlat{
    JSONArray result = null;
   
    JSONArray makeFlat(JSONArray catalog) throws JSONException{
      result = new JSONArray();
    
      _rec(catalog);
      return result;
      
    }

    void _rec(JSONArray catalog) throws JSONException{
      for(int i=0;i<catalog.length();i++){
        JSONObject a=catalog.getJSONObject(i);
        JSONObject b = new  JSONObject();
        JSONArray names = a.names();
        for (int j=0;j<names.length();j++){
          String key = names.getString(j);
          if(key.equals("children")==false){
            b.put(key, a.get(key));
            //Debug.println(a.getString(key));
          }
         
        }
        result.put(b);
        
        try{
          _rec(a.getJSONArray("children"));
        } catch (JSONException e) {
         
        }
      }
    }
  }
  public JSONResponse addtobasket(String query,HttpServletRequest request) {
    JSONResponse result = new JSONResponse(request);
    JSONObject jsonresult = new JSONObject();
    try{
      //DOSTUFF
      int searchLimit = 200;
      LockOnQuery.lock(query,0);
      JSONResponse r = _getFacetsImp(null,query,searchLimit);
      LockOnQuery.release(query);

      JSONObject searchResults =  (JSONObject) new JSONTokener(r.getMessage()).nextValue();
      long numFound = searchResults.getJSONObject("response").getLong("numfound");
      
      if(numFound>searchLimit){
        result.setErrorMessage("Too many results, maximum of "+searchLimit+" datasets allowed.", 200);
      }else{
        jsonresult.put("numDatasets", searchResults.getJSONObject("response").getLong("numfound"));
        jsonresult.put("ok", "ok");
      
        
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
          try{
            String url = results.getJSONObject(j).getString("url");
           
       
            JSONArray files = THREDDSCatalogBrowser.browseThreddsCatalog(request,url, variableFilter,null);
            //Debug.println(files.toString());
            catalogAggregation.put(files.getJSONObject(0));
            
           
            
            
            MakeFlat b = new MakeFlat();
            JSONArray flat = b.makeFlat(files);
            
            Debug.println("Found "+flat.length());
            for(int i=0;i<flat.length();i++){
              
              String openDAPURL=null;
              String httpURL=null;
              String fileSize = "";
              JSONObject a=flat.getJSONObject(i);
  
              try{openDAPURL = a.getString("OPENDAP");}catch (JSONException e) {}
              try{httpURL = a.getString("HTTPServer");}catch (JSONException e) {}
  
              try{fileSize = a.getString("fileSize");   totalFileSize=totalFileSize + Long.parseLong(fileSize);}catch (Exception e) {}
             
              if(openDAPURL!=null){numDap++;}
              if(httpURL!=null){numFiles++;}
             
              
            }
          }catch(Exception e){
            Debug.errprintln(j+"): "+results.getJSONObject(j).getString("id"));
            Debug.printStackTrace(e);
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
      
     
    }catch(Exception e){
       result.setException("Unable to query", e);
    }
    String message = jsonresult.toString();
    
    try {
      ImpactUser user = LoginManager.getUser(request);
      String dataDir = user.getDataDir();
      tools.Tools.writeFile(dataDir+"/test.catalog", message);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    
    result.setMessage(message);
    return result;
  }


}
