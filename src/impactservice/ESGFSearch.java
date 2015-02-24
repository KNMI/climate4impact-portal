package impactservice;



import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;


import java.util.Calendar;
import java.util.List;

import java.util.Vector;

/*
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import jpa.JPAStaticResourceBean;
import jpa.PersistentCachedString;
*/

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ASyncRunner.ASyncRunner;
import ASyncRunner.MyRunnableWaiter;


import tools.Debug;
import tools.HTTPTools;
import tools.MyXMLParser;

import tools.MyXMLParser.XMLElement;

/**
 * 
 * @author maartenplieger
 *
 */
public class ESGFSearch {
//  String[] facets = {"project","variable","time_frequency","institute","experiment","model","realm","domain"};

  
  /**
   * Loads a string from the filesystem identified with an id
   * @param identifier
   * @return null if not found
   */
  private static String getFileFromImpactStorage(String identifier){
    identifier = identifier.replaceAll("\\?", "_");
    identifier = identifier.replaceAll("&", "_");
    identifier = identifier.replaceAll(":", "_");
    identifier = identifier.replaceAll("/", "_");
    identifier = identifier.replaceAll("=", "_");
    identifier+=".json";
    Debug.println("getDiskCachedString:: Getting from diskcache: "+identifier);
    String diskCacheLocation = Configuration.getImpactWorkspace()+"/staticdata/";
    try {
      return tools.Tools.readFile(diskCacheLocation+"/"+identifier);
    } catch (IOException e) {
      Debug.println("getDiskCachedString:: Diskcache not available for "+diskCacheLocation+"/"+identifier);
    }
    return null;
  }
  
  private static String getDiskCachedString(String uniqueId, int mustbeYoungerThanNSeconds) {
    uniqueId = uniqueId.replaceAll("\\?", "_");
    uniqueId = uniqueId.replaceAll("&", "_");
    uniqueId = uniqueId.replaceAll(":", "_");
    uniqueId = uniqueId.replaceAll("/", "_");
    uniqueId = uniqueId.replaceAll("=", "_");
    uniqueId+=".json";
    
    Debug.println("Getting from diskcache: "+uniqueId);
    String diskCacheLocation = Configuration.getImpactWorkspace()+"/diskCache/";
    try {
      
      if(mustbeYoungerThanNSeconds!=0){
        Path fileCacheId = new File(diskCacheLocation+uniqueId).toPath();
        
        
        BasicFileAttributes attributes = Files.readAttributes(fileCacheId, BasicFileAttributes.class);
        FileTime creationTime = attributes.creationTime();
        long createdHowManySecondsAgo = ( Calendar.getInstance().getTimeInMillis()-creationTime.toMillis())/1000;
        //DebugConsole.println("Created:"+createdHowManySecondsAgo);
        if(createdHowManySecondsAgo>mustbeYoungerThanNSeconds)
        {
          Debug.println("Ignoring "+uniqueId+"Because too old.");
          tools.Tools.rm(diskCacheLocation+"/"+uniqueId);
          return null;
        }else{
          Debug.println(fileCacheId.toString()+"("+createdHowManySecondsAgo+"<"+mustbeYoungerThanNSeconds+")");
        }
      }
      return tools.Tools.readFile(diskCacheLocation+"/"+uniqueId);
    } catch (IOException e) {
    }
    return null;
  }
  
  /**
   * Store a string in the diskcache system identified with an id
   * @param data The data to store
   * @param identifier The identifier of this string
   */
  private static void storeDiskCachedString(String data, String identifier){
    identifier = identifier.replaceAll("\\?", "_");
    identifier = identifier.replaceAll("&", "_");
    identifier = identifier.replaceAll(":", "_");
    identifier = identifier.replaceAll("/", "_");
    identifier = identifier.replaceAll("=", "_");
    identifier+=".json";
    Debug.println("Storing to diskcache: "+identifier);
       String diskCacheLocation = Configuration.getImpactWorkspace()+"/diskCache/";
       
       try {
        tools.Tools.mksubdirs(diskCacheLocation);
        tools.Tools.writeFile(diskCacheLocation+"/"+identifier, data);
      } catch (IOException e) {

        e.printStackTrace();
      }
  }
  

  
  /**
   * Loads a string from the database identified with an id
   * @param identifier
   * @return null if not found
   */
  /* private static String getPersistentCachedString(String identifier){
    EntityManager entityManager = (EntityManager) JPAStaticResourceBean.getEMF().createEntityManager();
    checkPersistentCachedStringValidity(entityManager);
    PersistentCachedString queryResult = entityManager.find(PersistentCachedString.class,identifier);
    if(queryResult==null)return null;
    return queryResult.getQueryResult();    
  }*/
  
  /**
   * Store a string in the database identified with an id
   * @param data The data to store
   * @param identifier The identifier of this string
   */
  /*private static void storePersistentCachedString(String data, String identifier){
    
        EntityManager entityManager = (EntityManager) JPAStaticResourceBean.getEMF().createEntityManager();
        entityManager.getTransaction().begin();
        PersistentCachedString queryResult = entityManager.find(PersistentCachedString.class,identifier);
        if(queryResult == null){
          queryResult= new PersistentCachedString();
        }
        queryResult.setQueryResult(data);
        queryResult.setQuery(identifier);
        entityManager.persist(queryResult);
        entityManager.getTransaction().commit();
    
  }  
  
  private static void checkPersistentCachedStringValidity(EntityManager entityManager){
    String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(cal.getTimeInMillis()-1000*60*60);
    //cal.setTimeInMillis(cal.getTimeInMillis());
    String currentISOTimeString = sdf.format(cal.getTime())+"Z";
    DebugConsole.println("Starting cleanup "+currentISOTimeString);
    Query q = entityManager.createQuery("select e FROM PersistentCachedString e where creationDate < '"+currentISOTimeString+"'");
    @SuppressWarnings("unchecked")
    Collection<PersistentCachedString> queryResults=q.getResultList();
    entityManager.getTransaction().begin();
    for (Iterator<PersistentCachedString> i = queryResults.iterator(); i.hasNext();) {
      PersistentCachedString e = i.next();
      DebugConsole.println("Cleaning PersistentCachedString " + e.getCreationDate());
      entityManager.remove(e);
    }
    entityManager.getTransaction().commit();
  }*/
  
  /**
   * Queries the describe service from DKRZ
   * @param identifier The query string
   * @return 
   */  
  private static String queryDescribeService(String identifier){
    String data = null;

 
    Debug.println("desribeservice "+identifier);
    data = getFileFromImpactStorage((String)identifier);
    if(data == null){
      Debug.errprintln("Ünable to get descriptions for search items:"+identifier);
    }
    return data;
   
        
  }
  /**
   * This method provides an interface to the ESGF Search rest interface. It can return the search results and facets
   * @param query The query to do, can be "" to retrieve only the possible facets
   * @param pageNumber The page number we want results for
   * @param pageLimit The size of each page with results
   * @param includeFacets The JSONObject result will contain the facets shortnames with their descriptions.
   * @param includeLongNameFacets The JSONObject will contain the facets longnames with their descriptions. This option Is ignored when includeFacets is set to false.
   * @param includeQueryResults The JSONObject will contain the search results
   * @param dataSetType 0 = Dataset, 1 = File,  3 = aggregation
   * @return A JSON object with the results stored in "facets" and "topics"
   * @throws Exception 
   */
  static public JSONObject doCachedESGFSearchQuery(String query,int pageNumber,int pageLimit,boolean includeFacets,boolean includeLongNameFacets,boolean includeQueryResults,String datasetType) throws Exception{
    String uniqueId="ESGSearch_"+query+"_"+pageNumber+"_"+pageLimit+"_"+includeFacets+"_"+includeLongNameFacets+"_"+includeQueryResults+"_"+datasetType;
    Debug.println("*** uniqueId "+uniqueId);
    
    
    String data = getDiskCachedString(uniqueId,600);//null;//getPersistentCachedString(uniqueId);
    if(data ==null){
      JSONObject d = null;
      
      d = doESGFSearchQuery(query,pageNumber,pageLimit,includeFacets,includeLongNameFacets,includeQueryResults,datasetType);
      
      if(d==null){
        Debug.errprintln("Unable to search  for identifier "+uniqueId);
        return null;
      }
       data=d.toString();
       storeDiskCachedString(data,uniqueId);
      return d;
    }
    
    JSONObject d=new JSONObject();
    try {
      d = (JSONObject) new JSONTokener(data).nextValue();
    } catch (JSONException e) {
      Debug.errprintln("Unable to tokenize string for identifier "+uniqueId);
      return null;
    }
    
    return d;
      
  }
  


  static private JSONObject doESGFSearchQuery(String query,int pageNumber,int pageLimit,boolean includeFacets,boolean includeLongNameFacets,boolean includeQueryResults,String datasetType) throws Exception{
    Debug.println("Starting Search");
    Debug.println("Query: "+query);
    
    String[] queryParts=query.split("\\?");

    String queryString = "";
    if(queryParts.length==2)queryString=queryParts[1];else queryString = queryParts[0];

   
    Debug.println("queryString: "+queryString);
    
    JSONObject result = new JSONObject();
  
    JSONObject facets = new JSONObject();
    //try {
    result.put("facets", facets);

    MyRunnableWaiter t = new MyRunnableWaiter();
    t.setMaxThreads(10);
    if(includeFacets){
      t.add(new ASyncFacetFinder("project",queryString+"&type="+datasetType+"&"));
      t.add(new ASyncFacetFinder("variable",queryString+"&type="+datasetType+"&"));
      t.add(new ASyncFacetFinder("time_frequency",queryString+"&type="+datasetType+"&"));
      t.add(new ASyncFacetFinder("institute",queryString+"&type="+datasetType+"&"));
      t.add(new ASyncFacetFinder("experiment",queryString+"&type="+datasetType+"&"));
      t.add(new ASyncFacetFinder("model",queryString+"&type="+datasetType+"&"));
      t.add(new ASyncFacetFinder("realm",queryString+"&type="+datasetType+"&"));
      t.add(new ASyncFacetFinder("domain",queryString+"&type="+datasetType+"&"));
    }
    if(includeQueryResults){
      String s=queryString+"&limit="+pageLimit+"&offset="+((pageNumber-1)*pageLimit)+"&";
      s+="type="+datasetType+"&";
      
      Debug.println("Mainquery:"+s);
      t.add(new ASyncSearch(s));
    }
   
    Debug.println("Waiting");
    int returnCode = t.waitForCompletion();
    if(returnCode != 0){
      Debug.println("Error");
      throw new Exception(t.getErrorMessage());
    }
    
    Debug.println("Finished");
    
    if(includeFacets){
      facets.put("project",((ASyncFacetFinder)t.get(0)).data);
      facets.put("variable",((ASyncFacetFinder)t.get(1)).data);
      facets.put("time_frequency",((ASyncFacetFinder)t.get(2)).data);
      facets.put("institute",((ASyncFacetFinder)t.get(3)).data);
      facets.put("experiment",((ASyncFacetFinder)t.get(4)).data);
      facets.put("model",((ASyncFacetFinder)t.get(5)).data);
      facets.put("realm",((ASyncFacetFinder)t.get(6)).data);
      facets.put("domain",((ASyncFacetFinder)t.get(7)).data);
    }
    
    if(includeQueryResults){
      int offset = 0;
      if(includeFacets)offset=8;
      result.put("topics", ((ASyncSearch)t.get(offset)).data);
      result.put("query", ((ASyncSearch)t.get(offset)).ESGFQuery);
      result.put("totalCount", ((ASyncSearch)t.get(offset)).totalCount);
      result.put("currentPage",pageNumber);
      result.put("numPages", (((ASyncSearch)t.get(offset)).totalCount/pageLimit));
    }
    
      
    
    return result;
  }

  
  private static JSONArray addFacet(String facetToDo, String queryString) throws MalformedURLException, Exception {
    String ESGFQueryString="";
    Debug.println("addFacet "+facetToDo+"["+queryString+"]");
    
    List<String> project = HTTPTools.getKVPList(queryString, "project");
    List<String> variable = HTTPTools.getKVPList(queryString, "variable");
    List<String> frequency = HTTPTools.getKVPList(queryString, "time_frequency");
    List<String> institute = HTTPTools.getKVPList(queryString, "institute");
    List<String> experiment = HTTPTools.getKVPList(queryString, "experiment");
    List<String> model = HTTPTools.getKVPList(queryString, "model");
    List<String> realm = HTTPTools.getKVPList(queryString, "realm");
    List<String> domain = HTTPTools.getKVPList(queryString, "domain");
    
   
    
    
   
    
    
    
    //String startDate = URLDecoder.decode(HTTPTools.getKVP(queryString, "tc_start"),"UTF-8");
    //String stopDate = URLDecoder.decode(HTTPTools.getKVP(queryString, "tc_end"),"UTF-8");
    
    String startDate = HTTPTools.getKVPItemDecoded(queryString, "tc_start");
    String stopDate = HTTPTools.getKVPItemDecoded(queryString, "tc_end");
    
    if(!facetToDo.equals("project")){
      for(int j=0;j<project.size();j++){ESGFQueryString+="project="+project.get(j)+"&";}
    }
    if(!facetToDo.equals("variable")){
      for(int j=0;j<variable.size();j++){ESGFQueryString+="variable="+variable.get(j)+"&";}
    }
    if(!facetToDo.equals("time_frequency")){
      for(int j=0;j<frequency.size();j++){ESGFQueryString+="time_frequency="+frequency.get(j)+"&";}
    }
    if(!facetToDo.equals("institute")){
      for(int j=0;j<institute.size();j++){ESGFQueryString+="institute="+institute.get(j)+"&";}
    }
    if(!facetToDo.equals("experiment")){
      for(int j=0;j<experiment.size();j++){ESGFQueryString+="experiment="+experiment.get(j)+"&";}
    }
    if(!facetToDo.equals("model")){
      for(int j=0;j<model.size();j++){ESGFQueryString+="model="+model.get(j)+"&";}
    }
    if(!facetToDo.equals("realm")){
      for(int j=0;j<realm.size();j++){ESGFQueryString+="realm="+realm.get(j)+"&";} 
    }
    if(!facetToDo.equals("domain")){
      for(int j=0;j<domain.size();j++){ESGFQueryString+="domain="+domain.get(j)+"&";} 
    }
    
    Debug.println("addFacet "+ESGFQueryString);
    
    
    /*
    DebugConsole.println("From / To ="+startDate+"/"+stopDate);
    
    Date dateStartTime = null;
    try {
      dateStartTime = DateFunctions.ISO8601DateTimeFormat.parse(startDate);
    } catch (ParseException e1) {
      throw new Exception("Unable to parse start date: "+startDate+" Exception message: "+e1.getMessage());
    }
    Date dateStopTime = null;
    try {
      dateStopTime = DateFunctions.ISO8601DateTimeFormat.parse(stopDate);
    } catch (ParseException e1) {
      throw new Exception("Unable to parse start date: "+stopDate+" Exception message: "+e1.getMessage());
    }
    SimpleDateFormat ESGFTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    ESGFTimeFormat.format(dateStopTime)
    */
    if(startDate!=null)ESGFQueryString+="start="+startDate+"&";
    if(stopDate!=null)ESGFQueryString+="end="+stopDate+"&";
 
    String datasetType = HTTPTools.getKVPItem(queryString, "type");
    if(datasetType == null){
      datasetType="Dataset";
      Debug.println("Forcing type="+datasetType);
    } else {
      Debug.println("Found type="+datasetType);
    }
    
    
    String ESGFSearchRequest=Configuration.VercSearchConfig.getEsgfSearchURL()+"&type="+datasetType+"&facets="+facetToDo+"&"+ESGFQueryString;
    
    MyXMLParser.XMLElement el = new MyXMLParser.XMLElement();
  
    el.parse(new URL(ESGFSearchRequest));
 
    Vector<MyXMLParser.XMLElement> lst=el.get("response").getList("lst");
    
    //JSONArray facetItemAll = new JSONArray();
    /*facetItemAll.put("all");//Select and list
    facetItemAll.put("all");//Tooltip*/
   
    String descatFacet=facetToDo;
    if(descatFacet.equals("time_frequency"))descatFacet="frequency";

    String data = queryDescribeService("discribefacet?descat="+descatFacet);
    if(data == null){
      Debug.errprintln("Unable to describe facets, is static diskCache configured correctly?");
    }

    JSONObject queryResultsObject = null;
    try{
      queryResultsObject =  (JSONObject) new JSONTokener(data).nextValue();
    } catch (Exception e2) {
      Debug.errprintln("Unable to describe facets: Exception: "+e2.getMessage());
    }
    
    JSONArray catDesc = null;
    if(queryResultsObject!=null){
      catDesc = (JSONArray) queryResultsObject.get(descatFacet);
    }
   
    /*for(int n=0;n<catDesc.length();n++){
      JSONArray cat = (JSONArray) catDesc.get(n);
      DebugConsole.println(cat.getString(0)+" = "+cat.getString(1));
    }*/
    for(int j=0;j<lst.size();j++){
      //DebugConsole.println("lst name: "+lst.get(j).getAttrValue("name"));
      if(lst.get(j).getAttrValue("name").equals("facet_counts")){
        Vector<MyXMLParser.XMLElement> facetCounts=lst.get(j).getList("lst");
        for(int k=0;k<facetCounts.size();k++){
          if(facetCounts.get(k).getAttrValue("name").equals("facet_fields")){
            Vector<MyXMLParser.XMLElement> facetFields=facetCounts.get(k).getList("lst");
            for(int l=0;l<facetFields.size();l++){
              String facetName=facetFields.get(l).getAttrValue("name");
              if(facetName.equals(facetToDo)){
                Vector<XMLElement> facetItems=facetFields.get(l).getElements();
                //DebugConsole.println("-"+facetName+" ("+facetItems.size()+")");
                JSONArray facetItemsArray = new JSONArray();
                //facetItemsArray.put(facetItemAll);
           
                for(int m=0;m<facetItems.size();m++){
                  String facetItemShortName = facetItems.get(m).getAttrValue("name");
                  JSONArray facetItem = new JSONArray();
                  facetItem.put(facetItemShortName);//Select and list
                  
                  String longName = facetItemShortName;
                  if(catDesc!=null){
                    for(int n=0;n<catDesc.length();n++){
                      JSONArray cat = (JSONArray) catDesc.get(n);
                      if(cat.getString(0).equals(facetItemShortName)){
                        longName = cat.getString(1);
                        if(longName.equalsIgnoreCase("None"))longName=facetItemShortName;
                        break;
                      }
                    }
                  }
                  facetItem.put(longName);//Tooltip
                  //System.out.print("\""+facetItemShortName+"\" ");
                  facetItemsArray.put(facetItem);
                }
                //System.out.println("");
                return facetItemsArray;
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Makes an asynchronous requests.
   * @author plieger
   * 
   */
  private static class ASyncFacetFinder extends ASyncRunner{
    String facetToDo, queryString;
    boolean failed = false;
    String errorMessage = "";
    public ASyncFacetFinder(String facetToDo, String queryString) {
        super();
        this.facetToDo=facetToDo;
        this.queryString=queryString;
    }
    JSONArray data = null;
    public void succes(Object data) {
        this.data=(JSONArray) data;
    }
  
    public void fail(String message) {
      failed=true;
      errorMessage = message;
      Debug.println(message);
    }
    
    public boolean hasFailed(){
      return failed;
    }
    
    public void execute(){
      try {
        succes(addFacet(facetToDo, queryString));
      }catch (Exception e) {
        Debug.errprintln(e.getMessage());
        fail(e.getMessage());
      }
    }

    @Override
    public String getErrorMessage() {

      return errorMessage;
    }
  }
  /**
   * Makes an asynchronous requests.
   * @author plieger
   * 
   */
  private static class ASyncSearch extends ASyncRunner{
    int totalCount = 1000;
    String queryString;
    String ESGFQuery = "";
    String errorMessage = "";
    boolean failed = false;
    public ASyncSearch(String queryString) {
        super();
     
        this.queryString=queryString;
    }
    JSONArray data = null;
    
    public boolean hasFailed(){
      return failed;
    }
    
    public void fail(String message) {
      failed= true;
      errorMessage = message;
      Debug.errprintln(message);
    }
    
    public void execute(){
      try {
        String ESGFQueryString="";
        List<String> project = HTTPTools.getKVPList(queryString, "project");
        List<String> variable = HTTPTools.getKVPList(queryString, "variable");
        List<String> frequency = HTTPTools.getKVPList(queryString, "time_frequency");
        List<String> institute = HTTPTools.getKVPList(queryString, "institute");
        List<String> experiment = HTTPTools.getKVPList(queryString, "experiment");
        List<String> model = HTTPTools.getKVPList(queryString, "model");
        List<String> realm = HTTPTools.getKVPList(queryString, "realm");
        
        List<String> domain = HTTPTools.getKVPList(queryString, "domain");
        
        String limit = HTTPTools.getKVPItem(queryString, "limit");
        String offset = HTTPTools.getKVPItem(queryString, "offset");
        String datasetType = HTTPTools.getKVPItem(queryString, "type");
        
        //String startDate = URLDecoder.decode(HTTPTools.getKVP(queryString, "tc_start"),"UTF-8");
        //String stopDate = URLDecoder.decode(HTTPTools.getKVP(queryString, "tc_end"),"UTF-8");
        String startDate = HTTPTools.getKVPItem(queryString, "tc_start");
        String stopDate = HTTPTools.getKVPItem(queryString, "tc_end");

        
        for(int j=0;j<project.size();j++){ESGFQueryString+="project="+project.get(j)+"&";}
        for(int j=0;j<variable.size();j++){ESGFQueryString+="variable="+variable.get(j)+"&";}
        for(int j=0;j<frequency.size();j++){ESGFQueryString+="time_frequency="+frequency.get(j)+"&";}
        for(int j=0;j<institute.size();j++){ESGFQueryString+="institute="+institute.get(j)+"&";}
        for(int j=0;j<experiment.size();j++){ESGFQueryString+="experiment="+experiment.get(j)+"&";}
        for(int j=0;j<model.size();j++){ESGFQueryString+="model="+model.get(j)+"&";}
        for(int j=0;j<realm.size();j++){ESGFQueryString+="realm="+realm.get(j)+"&";}
        
        for(int j=0;j<domain.size();j++){ESGFQueryString+="domain="+domain.get(j)+"&";}
        

        
        if(limit!=null)ESGFQueryString+="limit="+limit+"&";
        if(offset!=null)ESGFQueryString+="offset="+offset+"&";
        if(datasetType!=null)ESGFQueryString+="type="+datasetType+"&";
        ESGFQueryString+="latest=true&replica=false&";
        
        if(startDate!=null)ESGFQueryString+="start="+startDate+"&";
        if(stopDate!=null)ESGFQueryString+="end="+stopDate+"&";
        
        
     
                
        String ESGFSearchRequest=Configuration.VercSearchConfig.getEsgfSearchURL()+ESGFQueryString;
        ESGFQuery = ESGFSearchRequest;
        
        MyXMLParser.XMLElement el = new MyXMLParser.XMLElement();
        el.parse(new URL(ESGFSearchRequest));
        MyXMLParser.XMLElement result=el.get("response").get("result");

        JSONArray topics = new JSONArray();

       
        totalCount=Integer.parseInt(result.getAttrValue("numFound"));
        
        Vector<XMLElement> docs=result.getElements();
        for(int j=0;j<docs.size();j++){
          
          JSONObject product = new JSONObject();
          
          Vector<XMLElement> str=docs.get(j).getList("str");
          for(int k=0;k<str.size();k++){
            String arrName=str.get(k).getAttrValue("name");
            String arrValue=str.get(k).getValue();
            if(arrName.equals("id"))product.put("id",arrValue);
            if(arrName.equals("instance_id"))product.put("instance_id",arrValue);
          }
          
          Vector<XMLElement> arr=docs.get(j).getList("arr");
          
          try{
            for(int k=0;k<arr.size();k++){
              String arrName=arr.get(k).getAttrValue("name");
              try{
                String arrValue="";
                for(int l=0;l<arr.get(k).getElements().size();l++){
                  if(l>0)arrValue+=", ";
                  arrValue+=arr.get(k).getElements().get(l).getValue();
                  //arr.get(k).get("str").getValue();
                }
                
                if(arrName.equals("activity"))product.put("activity",arrValue);
                if(arrName.equals("product"))product.put("product",arrValue);
                if(arrName.equals("institute"))product.put("institute",arrValue);
                if(arrName.equals("model"))product.put("model",arrValue);
                if(arrName.equals("realm"))product.put("realm",arrValue);
                if(arrName.equals("domain"))product.put("domain",arrValue);
                if(arrName.equals("project"))product.put("project",arrValue);
               
                //if(arrName.equals("cmor_table"))product.put("MIP_table",arrValue);
                if(arrName.equals("experiment"))product.put("experiment",arrValue);
                if(arrName.equals("time_frequency"))product.put("time_frequency",arrValue);
                if(arrName.equals("ensemble"))product.put("ensemble",arrValue);
                if(arrName.equals("variable"))product.put("variable",arrValue);
                if(arrName.equals("url")){
                  product.put("url",arrValue);
                  boolean opendapFound = false;
                  String HTTPServer = null;
                  for(int l=0;l<arr.get(k).getElements().size();l++){
                    String arrValue2=arr.get(k).getElements().get(l).getValue();
                    String dapURL = arrValue2.split("\\|")[0];
                    int a = dapURL.indexOf(".nc.html");
                    if(a>0){
                      dapURL = dapURL.substring(0,a+3);
                    }
                    
                    
                    if(arrValue2.toLowerCase().indexOf("application/xml")>0){product.put("catalogURL",arrValue2.split("\\|")[0]);}
                    if(arrValue2.toLowerCase().indexOf("|opendap")>0){product.put("OPENDAP",dapURL);opendapFound = true;}
                    if(arrValue2.toLowerCase().indexOf("|gridftp")>0){product.put("GridFTP",arrValue2.split("\\|")[0]);}
                    if(arrValue2.toLowerCase().indexOf("|httpserver")>0){HTTPServer = arrValue2.split("\\|")[0]; product.put("HTTPServer",HTTPServer);}
                    
                  }
                  if(!opendapFound){
                    //TODO Should ask why opendap is sometimes not listed in the ESGF search query.
                    if(HTTPServer!=null){
                      product.put("OPENDAP",HTTPServer.replace("/fileserver/","/dodsC/")+"#derivedfromhttpserver");
                    }
                  }
                }
                
              }catch(Exception e){
                
              }
              
              
            }
          
          }catch(Exception e){
            Debug.errprintln("Skipping file: "+e.getMessage());
          }
          
          Vector<XMLElement> longtype=docs.get(j).getList("long");
          
          try{
            for(int k=0;k<longtype.size();k++){
              String name=longtype.get(k).getAttrValue("name");
              try{
                
                if(name.equals("size")){
                  String value=longtype.get(k).getValue();
                  double  fileSize = Long.parseLong(value);
                
                  product.put("size",fileSize);
                
                
                  String dataSize = ""+fileSize;
                  if(fileSize>1024*1024*1024){
                    dataSize = String.format("%.3g",fileSize/(1024*1024*1024))+"G";
                  }else if(fileSize>1024*1024){
                    dataSize = String.format("%.3g",fileSize/(1024*1024))+"M";
                  }else if(fileSize>1024){
                    dataSize = String.format("%.3g",fileSize/(1024))+"K";
                  }else{
                    dataSize = fileSize +"b";
                  }
                  product.put("dataSize",dataSize);
                }
              }catch(Exception e){
                e.printStackTrace();
              }
              
              
            }
           
          }catch(Exception e){
            Debug.errprintln("Skipping file: "+e.getMessage());
          }
          topics.put(product);
          
         // topics.put("query",ESGFSearchRequest);
        
        }
        
        data=topics;
        
      }catch (Exception e) {
        fail(e.getMessage());
      }
    }

    
    public String getErrorMessage() {
      return errorMessage;
    }
  }
  public static JSONObject getFacetForQuery(String facet, String queryStr) throws MalformedURLException, Exception {
    String uniqueId = "getFacetForQuery_"+facet+"_"+queryStr;
    String data = getDiskCachedString(uniqueId,600);//null;//getPersistentCachedString(uniqueId);
    if(data ==null){
      JSONArray facets = addFacet(facet, queryStr);
      
      if(facets==null){
        Debug.errprintln("Unable to search for identifier "+uniqueId);
        return null;
      }
      JSONObject result = new JSONObject();
      result.put("facets", facets);
       data=result.toString();
      //storePersistentCachedString(data,uniqueId);
       storeDiskCachedString(data,uniqueId);
      return result;
    }
    
    JSONObject d=new JSONObject();
    try {
      d = (JSONObject) new JSONTokener(data).nextValue();
    } catch (JSONException e) {
      Debug.errprintln("Unable to tokenize string for identifier "+uniqueId);
      return null;
    }
    return d;
    
  }
  
}